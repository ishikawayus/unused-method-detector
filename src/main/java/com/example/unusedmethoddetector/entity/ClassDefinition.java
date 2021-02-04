package com.example.unusedmethoddetector.entity;

import java.nio.file.Path;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClassDefinition {

    private final Path path;
    private final int version;
    private final int access;
    private final String name;
    private final String signature;
    private final String superName;
    private final List<String> interfaces;
    private final List<String> annotations;
    private final List<MethodDefinition> methodDefinitions;
}
