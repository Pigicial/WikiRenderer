package com.pigicial.wikirenderer.screen.owo.event;

public interface FocusLost {
    void onFocusLost();

    static EventStream<FocusLost> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusLost();
            }
        });
    }
}
