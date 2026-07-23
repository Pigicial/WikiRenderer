package com.pigicial.wikirenderer.screen.owo.core;

public interface Animatable<T extends Animatable<T>> {

    T interpolate(T next, float delta);

}
