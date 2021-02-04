package com.example.unusedmethoddetector.visitor;

import java.util.List;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import com.example.unusedmethoddetector.entity.CalleeMethod;
import lombok.Getter;

@Getter
public class CalleeMethodVisitor extends MethodVisitor {

    private final List<CalleeMethod> calleeMethods;

    public CalleeMethodVisitor(List<CalleeMethod> calleeMethods) {
        super(Opcodes.ASM5);
        this.calleeMethods = calleeMethods;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
            boolean isInterface) {
        calleeMethods.add(CalleeMethod.builder()
                .opcode(opcode)
                .owner(owner)
                .name(name)
                .descriptor(descriptor)
                .isInterface(isInterface)
                .build());
    }
}
