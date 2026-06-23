package com.pigicial.wikirenderer.render.area;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.pigicial.wikirenderer.WikiRenderer;
import com.pigicial.wikirenderer.render.OrthographicSort;
import com.pigicial.wikirenderer.render.area.bounds.MeshBounds;
import com.pigicial.wikirenderer.render.area.side_view.WalkabilityFilter;
import com.pigicial.wikirenderer.util.compatibility.ShaderCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// todo: not a fan of how entities are handled in AreaRenderable and blocks are here, maybe they should be merged
public class WorldBlockMesh {

    public static boolean overrideTerrainTransparencyRenderPipelines = false;
    public static GpuSampler terrainSampler = null;

    public final MeshWorldOverrides world;
    public final MeshBounds bounds;
    private AreaRenderable renderable;

    public final Map<Long, MeshRenderSection> subMeshes = new ConcurrentHashMap<>();

    private MeshState state = MeshState.NEW;
    protected OrthographicSort orthographicTransparencySorting = null;
    private volatile CompletableFuture<Void> sortFuture = null;

    private boolean currentlyFullyBuilding = false; // initial build or rebuilds, not automatic ones from chunk updates
    private boolean currentlyUpdatingWalkabilityFilter = false;
    public boolean lastUpdateUsesWalkabilityFilter = false;

    private float fullBuildProgress = 0;
    protected volatile boolean buildingCancelled = false;
    private boolean isAutomaticUpdateScheduled = false;

    private float lastUsedRotation;
    private double lastUsedSlant;

    public WorldBlockMesh(
            BlockAndTintGetter world,
            MeshBounds bounds
    ) {
        this.bounds = bounds;
        this.world = new MeshWorldOverrides(world, bounds);
        this.lastUsedRotation = Float.MAX_VALUE;
        this.lastUsedSlant = Double.MAX_VALUE;

        this.scheduleRebuild(true);
    }

    public void setRenderable(AreaRenderable renderable) {
        this.renderable = renderable;
    }

    public MeshState getMeshState() {
        return this.state;
    }

    public float getBuildProgress() {
        return this.fullBuildProgress;
    }

    public boolean canRebuild() {
        return !currentlyFullyBuilding;
    }

    public void drawBlocks(PoseStack matrices, Runnable preTranslucencyTask) {
        if (!this.getMeshState().canRender) {
            throw new IllegalStateException("World mesh not prepared!");
        }

        this.updateBuildingStatus();
        this.updateOutdatedMeshSections();

        if (terrainSampler == null) {
            Options options = Minecraft.getInstance().options;
            int maxAnisotropy = options.textureFiltering().get() == TextureFilteringMethod.ANISOTROPIC ? options.maxAnisotropyValue() : 1;
            terrainSampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, maxAnisotropy, OptionalDouble.empty());
        }

        if (WikiRenderer.orthographicSorting != null) {
            this.orthographicTransparencySorting = WikiRenderer.orthographicSorting;
        }

        float currentRotation = AreaPropertyBundle.INSTANCE.getUsedRotation();
        double currentSlant = AreaPropertyBundle.INSTANCE.getUsedSlant();
        if ((this.lastUsedRotation != currentRotation || this.lastUsedSlant != currentSlant) && this.orthographicTransparencySorting != null) {
            boolean isProbablyLargeSpinningObjectShrunkenDown = renderable.getProperties().scale.get() <= 15 && renderable.getProperties().rotationSpeed.get() > 0;
            if (!isProbablyLargeSpinningObjectShrunkenDown) {
                // anything smaller than 15 you probably wont see transparency issues (i.e. rendering the skyblock hub)
                this.lastUsedRotation = currentRotation;
                this.lastUsedSlant = currentSlant;
                scheduleReSort();
            }
        }

        ChunkSectionsToRender sections = renderBlockLayers(matrices.last().pose());

        for (ChunkSectionLayerGroup sectionLayer : new ChunkSectionLayerGroup[]{ChunkSectionLayerGroup.OPAQUE, ChunkSectionLayerGroup.TRANSLUCENT, ChunkSectionLayerGroup.TRIPWIRE}) {
            if (sectionLayer == ChunkSectionLayerGroup.TRANSLUCENT) {
                preTranslucencyTask.run();
            }
            overrideTerrainTransparencyRenderPipelines = sectionLayer == ChunkSectionLayerGroup.OPAQUE;
            sections.renderGroup(sectionLayer, terrainSampler);
        }
    }

    private ChunkSectionsToRender renderBlockLayers(Matrix4fc posMatrix) {
        EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> enumMap = new EnumMap<>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            enumMap.put(layer, new ArrayList<>());
        }

        List<MeshRenderSection> sortedSections = new ArrayList<>(subMeshes.values());

        if (orthographicTransparencySorting instanceof OrthographicSort orthoSort) {
            sortedSections.sort(Comparator.comparingDouble(s -> {
                BlockPos from = s.getFrom();
                BlockPos to = s.getTo();
                float cx = (from.getX() + to.getX()) / 2f;
                float cy = (from.getY() + to.getY()) / 2f;
                float cz = (from.getZ() + to.getZ()) / 2f;
                return orthoSort.projectDepth(cx, cy, cz);
            }));
        }

        List<DynamicUniforms.ChunkSectionInfo> list = new ArrayList<>();
        GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        int width = gpuTextureView.getWidth(0);
        int height = gpuTextureView.getHeight(0);
        int maxIndicesRequired = 0;

        for (MeshRenderSection meshSection : sortedSections) {
            Map<ChunkSectionLayer, SectionBuffers> bufferStorage = meshSection.getBuffers();
            int infoIndex = -1;

            for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                SectionBuffers buffers = bufferStorage.get(layer);
                if (buffers != null) {
                    if (infoIndex == -1) {
                        infoIndex = list.size();
                        list.add(new DynamicUniforms.ChunkSectionInfo(
                                new Matrix4f(posMatrix), 0, 0, 0, 1.0F, width, height));
                    }

                    GpuBuffer gpuBuffer = null;
                    VertexFormat.IndexType indexType = null;
                    if (buffers.getIndexBuffer() == null) {
                        if (buffers.getIndexCount() > maxIndicesRequired) {
                            maxIndicesRequired = buffers.getIndexCount();
                        }
                    } else {
                        gpuBuffer = buffers.getIndexBuffer();
                        indexType = buffers.getIndexType();
                    }

                    int sectionIndex = infoIndex;
                    enumMap.get(layer).add(new RenderPass.Draw<>(
                            0, buffers.getVertexBuffer(), gpuBuffer, indexType, 0,
                            buffers.getIndexCount(),
                            (transforms, uniformUploader) ->
                                    uniformUploader.upload("ChunkSection", transforms[sectionIndex])));
                }
            }
        }

        GpuBufferSlice[] gpuBufferSlices = RenderSystem.getDynamicUniforms()
                .writeChunkSections(list.toArray(new DynamicUniforms.ChunkSectionInfo[0]));
        return new ChunkSectionsToRender(gpuTextureView, enumMap, maxIndicesRequired, gpuBufferSlices);
    }

    public void drawBlockEntities(PoseStack standardStack, SubmitNodeStorage nodeStorage, CameraRenderState cameraRenderState, float tickDelta) {
        BlockPos minCorner = bounds.getMinCorner();
        standardStack.pushPose();
        standardStack.translate(-minCorner.getX(), -minCorner.getY(), -minCorner.getZ());

        BlockEntityRenderDispatcher blockEntityDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        for (MeshRenderSection renderSection : this.subMeshes.values()) {
            renderSection.blockEntities.forEach((blockPos, entity) -> {
                BlockEntityRenderState state = blockEntityDispatcher.tryExtractRenderState(entity, tickDelta, null);
                if (state instanceof BeaconRenderState && AreaPropertyBundle.INSTANCE.hideBeaconBeams.get()) {
                    return;
                }

                standardStack.pushPose();
                standardStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                if (state != null) {
                    blockEntityDispatcher.submit(state, standardStack, nodeStorage, cameraRenderState);
                }

                standardStack.popPose();
            });
        }

        standardStack.popPose();
        renderable.drawSubmittedRenderFeatures();
    }

    public synchronized void scheduleRebuild(boolean async) {
        if (currentlyFullyBuilding || currentlyUpdatingWalkabilityFilter) return;

        this.lastUpdateUsesWalkabilityFilter = AreaPropertyBundle.INSTANCE.useWalkabilityFilter.get();
        this.fullBuildProgress = 0;
        this.state = this.state != MeshState.NEW
                ? MeshState.REBUILDING
                : MeshState.BUILDING;

        this.subMeshes.values().forEach(section -> {
            section.close();
            section.markBuildNotAttempted();
        });
        this.subMeshes.clear();

        this.orthographicTransparencySorting = WikiRenderer.orthographicSorting;

        this.currentlyFullyBuilding = true;
        if (ShaderCheck.isUsingShaders() || !async) {
            this.compileMesh();
        } else {
            CompletableFuture.runAsync(this::compileMesh);
        }
    }

    public synchronized void stopBuilding() {
        this.buildingCancelled = true;
        this.state = MeshState.CANCELLED;
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean force) {
        long sectionIndex = MeshRenderSection.getSectionIndex(sectionX, sectionY, sectionZ);
        MeshRenderSection renderSection = this.subMeshes.get(sectionIndex);

        if (renderSection == null && force) {
            renderSection = createRenderSection(sectionX, sectionY, sectionZ);
            this.subMeshes.put(sectionIndex, renderSection);
        }

        if (renderSection != null) {
            renderSection.setDirty(force);
        }
    }

    private void updateBuildingStatus() {
        int amountBuilt = 0;
        for (MeshRenderSection sections : this.subMeshes.values()) {
            if (sections.hasBuildBeenAttempted()) amountBuilt++;
        }

        if (amountBuilt == this.subMeshes.size()) {
            currentlyFullyBuilding = false;
            if (buildingCancelled) {
                state = MeshState.CANCELLED;
                buildingCancelled = false;
                return;
            }

            if (state == MeshState.CANCELLED) {
                return;
            }

            buildingCancelled = false;
            state = MeshState.READY;
        }

        this.fullBuildProgress = (float) amountBuilt / this.subMeshes.size();
    }

    private void updateOutdatedMeshSections() {
        if (isAutomaticUpdateScheduled || currentlyFullyBuilding || currentlyUpdatingWalkabilityFilter) return;

        boolean allowAutoUpdate = !renderable.getProperties().freezeBlocks.get();
        boolean hasDirty = subMeshes.values().stream().anyMatch(section -> section.isDirty() && (allowAutoUpdate || section.isForceUpdate()));
        if (!hasDirty) return;

        isAutomaticUpdateScheduled = true;
        if (ShaderCheck.isUsingShaders()) {
            this.compileOutdatedMeshSections();
            isAutomaticUpdateScheduled = false;
        } else {
            CompletableFuture.runAsync(() -> {
                try {
                    this.compileOutdatedMeshSections();
                } finally {
                    isAutomaticUpdateScheduled = false;
                }
            });
        }
    }

    private void compileOutdatedMeshSections() {
        if (this.currentlyFullyBuilding) return;
        boolean allowAutoUpdate = !renderable.getProperties().freezeBlocks.get();

        for (MeshRenderSection section : this.subMeshes.values()) {
            if (section.isDirty() && (allowAutoUpdate || section.isForceUpdate())) {
                section.buildAndSubmit(this);
            }
        }
    }

    // Based on SectionCompiler#compile and then SectionRenderDispatcher.RenderSection.RebuildTask#doTask (which calls compile)
    private void compileMesh() {
        if (buildingCancelled) {
            currentlyFullyBuilding = false;
            return;
        }
        currentlyFullyBuilding = true;

        List<MeshRenderSection> unsortedSubMeshes = new ArrayList<>();

        BlockPos minCorner = this.bounds.getMinCorner();
        BlockPos maxCorner = this.bounds.getMaxCorner();

        int minSectionX = MeshRenderSection.getSection(minCorner.getX());
        int minSectionY = MeshRenderSection.getSection(minCorner.getY());
        int minSectionZ = MeshRenderSection.getSection(minCorner.getZ());
        int maxSectionX = MeshRenderSection.getSection(maxCorner.getX());
        int maxSectionY = MeshRenderSection.getSection(maxCorner.getY());
        int maxSectionZ = MeshRenderSection.getSection(maxCorner.getZ());

        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX += 1) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY += 1) {
                for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ += 1) {
                    long sectionIndex = MeshRenderSection.getSectionIndex(sectionX, sectionY, sectionZ);
                    int finalSectionX = sectionX;
                    int finalSectionY = sectionY;
                    int finalSectionZ = sectionZ;

                    MeshRenderSection section = this.subMeshes.computeIfAbsent(sectionIndex, l -> createRenderSection(finalSectionX, finalSectionY, finalSectionZ));
                    unsortedSubMeshes.add(section);
                }
            }
        }

        List<MeshRenderSection> subMeshes = unsortedSubMeshes.stream().sorted(Comparator.comparing(MeshRenderSection::getDistanceFromCenter)).toList();

        if (buildingCancelled) {
            currentlyFullyBuilding = false;
            return;
        }

        this.refreshWalkabilityFilter();

        ModelBlockRenderer.enableCaching();
        for (MeshRenderSection renderSection : subMeshes) {
            renderSection.buildAndSubmit(this);
        }
        ModelBlockRenderer.clearCache();
    }

    protected void refreshWalkabilityFilter() {
        this.currentlyUpdatingWalkabilityFilter = true;
        this.world.setWalkabilityFilter(null);
        WalkabilityFilter walkabilityFilter = null;
        AreaPropertyBundle properties = AreaPropertyBundle.INSTANCE;
        if (properties.perPixel90DegreeRendering.get()) {
            if (properties.useWalkabilityFilter.get()) {
                walkabilityFilter = new WalkabilityFilter(this, renderable);
                walkabilityFilter.cacheData();
            }
        }
        this.world.setWalkabilityFilter(walkabilityFilter);
        this.currentlyUpdatingWalkabilityFilter = false;
    }

    private MeshRenderSection createRenderSection(int sectionX, int sectionY, int sectionZ) {
        BlockPos minCorner = this.bounds.getMinCorner();
        BlockPos maxCorner = this.bounds.getMaxCorner();
        int middleX = maxCorner.getX() - (maxCorner.getX() - minCorner.getX()) / 2;
        int middleZ = maxCorner.getZ() - (maxCorner.getZ() - minCorner.getZ()) / 2;

        MeshRenderSection section = new MeshRenderSection(sectionX, sectionY, sectionZ);

        BlockPos from = section.getFrom();
        BlockPos to = section.getTo();

        int subMiddleX = to.getX() - (to.getX() - from.getX()) / 2;
        int subMiddleZ = to.getZ() - (to.getZ() - from.getZ()) / 2;
        double distance = Math.pow(middleX - subMiddleX, 2) + Math.pow(middleZ - subMiddleZ, 2);

        section.setDistanceFromCenter(distance);

        return section;
    }

    private synchronized void scheduleReSort() {
        if (this.sortFuture != null && !this.sortFuture.isDone()) return;

        this.orthographicTransparencySorting = WikiRenderer.orthographicSorting;
        if (ShaderCheck.isUsingShaders()) {
            this.sortFuture = CompletableFuture.completedFuture(null);
            this.reSortMeshSections();
            this.sortFuture = null;
        } else {
            this.sortFuture = CompletableFuture.runAsync(this::reSortMeshSections).whenComplete((v, t) -> this.sortFuture = null);
        }
    }

    // Based on SectionRenderDispatcher.RenderSection.ResortTransparencyTask#doTask
    private void reSortMeshSections() {
        for (MeshRenderSection section : subMeshes.values()) {
            section.reSortTransparencyAndSubmit(this);
        }
    }

    protected BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> startedLayers, SectionBufferBuilderPack buffers, ChunkSectionLayer layer) {
        return startedLayers.computeIfAbsent(layer, l -> new BufferBuilder(buffers.buffer(layer), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
    }

    public Optional<List<Integer>> getAnimationCompletionTimings() {
        List<Integer> timings = new ArrayList<>();
        for (MeshRenderSection section : subMeshes.values()) {
            timings.addAll(section.animationCompletionTimings);
        }
        return timings.isEmpty() ? Optional.empty() : Optional.of(timings);
    }

    public void dispose() {
        subMeshes.values().forEach(MeshRenderSection::close);
        subMeshes.clear();
    }

    public enum MeshState {
        NEW(false, true),
        CANCELLED(true, true),
        BUILDING(true, true),
        REBUILDING(true, true),
        READY(false, true),
        CORRUPT(false, false);

        public final boolean isBuildStage;
        public final boolean canRender;

        MeshState(boolean buildStage, boolean canRender) {
            this.isBuildStage = buildStage;
            this.canRender = canRender;
        }
    }
}
