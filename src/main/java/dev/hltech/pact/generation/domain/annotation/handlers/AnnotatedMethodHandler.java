package dev.hltech.pact.generation.domain.annotation.handlers;

import dev.hltech.pact.generation.domain.client.model.RequestProperties;

import java.lang.reflect.Method;

public interface AnnotatedMethodHandler {

    boolean isSupported(Method method);

    RequestProperties handle(Method method);
}