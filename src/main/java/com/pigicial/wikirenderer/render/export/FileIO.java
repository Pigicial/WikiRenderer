package com.pigicial.wikirenderer.render.export;

import com.mojang.blaze3d.platform.NativeImage;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.property.GlobalProperties;
import com.pigicial.wikirenderer.screen.RenderScreen;
import com.pigicial.wikirenderer.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FileIO {

    private static final AtomicInteger TASK_COUNT = new AtomicInteger(0);


    public static CompletableFuture<File> saveImage(NativeImage image, ExportPathSpec path) {
        return saveImage(image, path, Map.of());
    }

    public static CompletableFuture<File> saveImage(NativeImage image, ExportPathSpec path, Map<String, String> pngTextMetadata) {
        CompletableFuture<File> future = new CompletableFuture<>();

        TASK_COUNT.incrementAndGet();
        try (ForkJoinPool pool = ForkJoinPool.commonPool()) {
            pool.submit(() -> {
                File imageFile = path.resolveFile("png");

                File exportDirectory = imageFile.getParentFile();
                if (exportDirectory.mkdirs()) {
                    WikiRenderer.LOGGER.info("Made export directory {} to save file {}", exportDirectory, imageFile.getName());
                }

                try {
                    image.writeToFile(imageFile);
                    if (!pngTextMetadata.isEmpty()) {
                        writePngTextMetadata(imageFile, pngTextMetadata);
                    }
                    WikiRenderer.LOGGER.info("Image {} saved", imageFile.getAbsolutePath());
                    future.complete(imageFile);
                } catch (IOException e) {
                    WikiRenderer.LOGGER.warn("Could not save image {}", imageFile.getAbsolutePath(), e);
                    future.completeExceptionally(e);
                } finally {
                    TASK_COUNT.decrementAndGet();
                }
            });
        }

        return future;
    }

    private static void writePngTextMetadata(File imageFile, Map<String, String> pngTextMetadata) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not decode png image to append metadata: " + imageFile.getAbsolutePath());
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG ImageWriter available");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(imageFile)) {
            if (output == null) {
                throw new IOException("Could not create output stream for image metadata: " + imageFile.getAbsolutePath());
            }

            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();

            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, params);
            String nativeFormat = metadata.getNativeMetadataFormatName();

            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(nativeFormat);
            IIOMetadataNode textNode = getOrCreateChild(root, "tEXt");

            for (Map.Entry<String, String> entry : pngTextMetadata.entrySet()) {
                removeExistingTextEntry(textNode, entry.getKey());

                IIOMetadataNode textEntryNode = new IIOMetadataNode("tEXtEntry");
                textEntryNode.setAttribute("keyword", entry.getKey());
                textEntryNode.setAttribute("value", entry.getValue());
                textNode.appendChild(textEntryNode);
            }

            metadata.mergeTree(nativeFormat, root);
            writer.write(null, new IIOImage(image, null, metadata), params);
        } finally {
            writer.dispose();
        }
    }

    private static IIOMetadataNode getOrCreateChild(IIOMetadataNode root, String childName) {
        NodeList children = root.getElementsByTagName(childName);
        if (children.getLength() > 0) {
            return (IIOMetadataNode) children.item(0);
        }

        IIOMetadataNode created = new IIOMetadataNode(childName);
        root.appendChild(created);
        return created;
    }

    private static void removeExistingTextEntry(IIOMetadataNode textNode, String keyword) {
        NodeList textEntries = textNode.getElementsByTagName("tEXtEntry");
        for (int i = textEntries.getLength() - 1; i >= 0; i--) {
            IIOMetadataNode node = (IIOMetadataNode) textEntries.item(i);
            if (keyword.equals(node.getAttribute("keyword"))) {
                textNode.removeChild(node);
            }
        }
    }

    public static CompletableFuture<File> saveText(String text, ExportPathSpec path, String extension) {
        CompletableFuture<File> future = new CompletableFuture<>();

        TASK_COUNT.incrementAndGet();
        try (ForkJoinPool pool = ForkJoinPool.commonPool()) {
            pool.submit(() -> {
                File textFile = path.resolveFile(extension);

                File exportDirectory = textFile.getParentFile();
                if (exportDirectory.mkdirs()) {
                    WikiRenderer.LOGGER.info("Made export directory {} for file {}", exportDirectory, textFile.getName());
                }

                try {
                    Files.writeString(
                            textFile.toPath(),
                            text,
                            StandardCharsets.UTF_8
                    );
                    future.complete(textFile);
                } catch (IOException e) {
                    WikiRenderer.LOGGER.warn("Could not save text {}", textFile.getAbsolutePath(), e);
                    future.completeExceptionally(e);
                } finally {
                    TASK_COUNT.decrementAndGet();
                }
            });
        }

        return future;
    }

    public static void saveTextAndNotify(String text, ExportPathSpec path, RenderScreen renderScreen, String key) {
        saveTextAndNotify(text, path, "txt", renderScreen, key);
    }

    public static void saveTextAndNotify(String text, ExportPathSpec path, String extension, RenderScreen renderScreen, String key) {
        FileIO.saveText(text, path, extension).whenComplete((textFile, t) -> Minecraft.getInstance().execute(() -> renderScreen.notify(
                () -> Util.getPlatform().openFile(textFile),
                Translate.gui(key),
                Component.literal(ExportPathSpec.exportRoot().relativize(textFile.toPath()).toString())
        )));
    }

    public static void deleteSequenceFilesFromPath(Path sequencePath) {
        if (GlobalProperties.get().saveIndividualFrames.get()) {
            return;
        }

        try (Stream<Path> p = Files.list(sequencePath)) {
            p.filter(path -> path.getFileName().toString().matches("seq_\\d+\\.png"))
                    .forEach(deletePath -> {
                        try {
                            Files.delete(deletePath);
                        } catch (IOException e) {
                            WikiRenderer.LOGGER.warn("Could not clean up sequence directory", e);
                        }
                    });
        } catch (IOException e) {
            WikiRenderer.LOGGER.warn("Could not clean up sequence directory", e);
        }

        try {
            Files.delete(sequencePath);
        } catch (IOException e) {
            WikiRenderer.LOGGER.warn("Could not delete up sequence directory", e);
        }
    }

    public static int taskCount() {
        return TASK_COUNT.get();
    }

    public static Component progressText() {
        int jobs = taskCount();
        if (jobs == 0) return Translate.gui("exporter.idle");
        return Translate.gui("exporter.jobs", jobs);
    }

    public static Path next(Path input) {
        String filename = input.getFileName().toString();

        int separatorIndex = filename.lastIndexOf('.');
        if (separatorIndex == -1) separatorIndex = filename.length();

        String name = filename.substring(0, separatorIndex);
        String extension = filename.substring(separatorIndex);

        Path path = input.getParent();

        Path currentPath = path.resolve(join(name, extension, 0));
        Path lastPath = currentPath;

        for (int i = 1; Files.exists(currentPath); i++) {
            lastPath = currentPath;
            currentPath = path.resolve(join(name, extension, i));
        }

        return GlobalProperties.get().overwriteLatest.get() ? lastPath : currentPath;
    }

    private static String join(String filename, String extension, int index) {
        return index == 0
                ? filename + extension
                : filename + "_" + index + (extension.isEmpty() ? "" : "_" + extension);
    }

}
