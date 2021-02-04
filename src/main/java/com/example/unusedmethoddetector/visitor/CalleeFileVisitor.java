package com.example.unusedmethoddetector.visitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.ClassReader;
import com.example.unusedmethoddetector.entity.ClassDefinition;
import lombok.Getter;

@Getter
public class CalleeFileVisitor extends SimpleFileVisitor<Path> {

    private final List<ClassDefinition> classDefinitions = new ArrayList<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        if (file.getFileName().toString().endsWith(".class")) {
            readClassFile(file);
        }
        return FileVisitResult.CONTINUE;
    }

    private void readClassFile(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            readClassFile(path, is);
        }
    }

    private void readClassFile(Path path, InputStream is) throws IOException {
        CalleeClassVisitor classVisitor = new CalleeClassVisitor();
        new ClassReader(is).accept(classVisitor, 0);
        classDefinitions.add(ClassDefinition.builder()
                .path(path)
                .version(classVisitor.getVersion())
                .access(classVisitor.getAccess())
                .name(classVisitor.getName())
                .signature(classVisitor.getSignature())
                .superName(classVisitor.getSuperName())
                .interfaces(classVisitor.getInterfaces())
                .annotations(classVisitor.getAnnotations())
                .methodDefinitions(classVisitor.getMethodDefinitions())
                .build());
    }
}
