package com.example.unusedmethoddetector.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import com.example.unusedmethoddetector.entity.CalleeMethod;
import com.example.unusedmethoddetector.entity.MethodDefinition;
import lombok.Getter;

@Getter
public class CalleeClassVisitor extends ClassVisitor {

    private int version;
    private int access;
    private String name;
    private String signature;
    private String superName;
    private List<String> interfaces = new ArrayList<>();
    private final List<String> annotations = new ArrayList<>();
    private final List<MethodDefinition> methodDefinitions = new ArrayList<>();

    public CalleeClassVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces != null ? Arrays.asList(interfaces) : Collections.emptyList();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        annotations.add(desc);
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {
        List<CalleeMethod> calleeMethods = new ArrayList<>();
        methodDefinitions.add(MethodDefinition.builder()
                .access(access)
                .name(name)
                .descriptor(descriptor)
                .signature(signature)
                .exceptions(
                        exceptions != null ? Arrays.asList(exceptions) : Collections.emptyList())
                .calleeMethods(calleeMethods)
                .build());
        return new CalleeMethodVisitor(calleeMethods);
    }
}
