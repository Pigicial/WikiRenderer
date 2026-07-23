package com.pigicial.wikirenderer.screen.owo.event;

public interface MouseEnter {
    void onMouseEnter();

    static EventStream<MouseEnter> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseEnter();
            }
        });
    }
}
