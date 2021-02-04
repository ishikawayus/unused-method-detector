package com.example.unusedmethoddetector.entity;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodDefinition {

    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final List<String> exceptions;
    private final List<CalleeMethod> calleeMethods;
}
