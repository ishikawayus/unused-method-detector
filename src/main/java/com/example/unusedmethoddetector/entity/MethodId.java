package com.example.unusedmethoddetector.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodId {

    private final String owner;
    private final String name;
    private final String descriptor;

    public static MethodId from(CalleeMethod calleeMethod) {
        return MethodId.builder()
                .owner(calleeMethod.getOwner())
                .name(calleeMethod.getName())
                .descriptor(calleeMethod.getDescriptor())
                .build();
    }

    public static MethodId from(String owner, MethodDefinition methodDefinition) {
        return MethodId.builder()
                .owner(owner)
                .name(methodDefinition.getName())
                .descriptor(methodDefinition.getDescriptor())
                .build();
    }
}
