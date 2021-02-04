package com.example.unusedmethoddetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import com.example.unusedmethoddetector.entity.CalleeMethod;
import com.example.unusedmethoddetector.entity.ClassDefinition;
import com.example.unusedmethoddetector.entity.MethodDefinition;
import com.example.unusedmethoddetector.entity.MethodId;
import com.example.unusedmethoddetector.visitor.CalleeFileVisitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnusedMethodDetector {

    public static void main(String[] args) throws Exception {
        new UnusedMethodDetector().run();
    }

    private static String ROOT_PATH = "";

    private void run() throws Exception {
        if (ROOT_PATH == null || ROOT_PATH.isEmpty()) {
            throw new IllegalArgumentException(
                    "ROOT_PATH must be specified: ROOT_PATH=" + ROOT_PATH);
        }


        mvn(Arrays.asList("compile"), new File(ROOT_PATH));

        CalleeFileVisitor fileVisitor = new CalleeFileVisitor();
        Files.walkFileTree(Paths.get(ROOT_PATH), fileVisitor);
        List<ClassDefinition> classDefinitions = fileVisitor.getClassDefinitions();
        log.info("classDefinitions={}", classDefinitions);

        Map<String, List<String>> parentNamesByClassName = new HashMap<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            List<String> parentNames = new ArrayList<>();
            parentNames.add(classDefinition.getSuperName());
            parentNames.addAll(classDefinition.getInterfaces());
            parentNamesByClassName.put(classDefinition.getName(), parentNames);
        }
        log.info("parentNamesByClassName={}", parentNamesByClassName);

        Set<MethodId> methodIds = new HashSet<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            for (MethodDefinition methodDefinition : classDefinition.getMethodDefinitions()) {
                for (CalleeMethod calleeMethod : methodDefinition.getCalleeMethods()) {
                    methodIds.add(MethodId.from(calleeMethod));
                }
            }
        }
        log.info("methodIds={}", methodIds);

        List<MethodId> notCalledMethodIds = new ArrayList<>();
        for (ClassDefinition classDefinition : classDefinitions) {
            for (MethodDefinition methodDefinition : classDefinition.getMethodDefinitions()) {
                log.info("methodDefinition={}", methodDefinition);
                if (!isConstructor(methodDefinition) && !isMainMethod(methodDefinition)
                        && !isControllerMethod(methodDefinition, classDefinition)
                        && !isCalledMethod(methodDefinition, classDefinition.getName(),
                                parentNamesByClassName, methodIds)) {
                    notCalledMethodIds
                            .add(MethodId.from(classDefinition.getName(), methodDefinition));
                }
            }
        }

        log.info("notCalledMethodIds={}", notCalledMethodIds);
    }

    private boolean isConstructor(MethodDefinition methodDefinition) {
        return "<init>".equals(methodDefinition.getName());
    }

    private boolean isMainMethod(MethodDefinition methodDefinition) {
        return "main".equals(methodDefinition.getName())
                && "([Ljava/lang/String;)V".equals(methodDefinition.getDescriptor());
    }

    private boolean isControllerMethod(MethodDefinition methodDefinition,
            ClassDefinition classDefinition) {
        Set<String> annotations = new HashSet<>(classDefinition.getAnnotations());
        return (annotations.contains("Lorg/springframework/stereotype/Controller;")
                || annotations.contains("Lorg/springframework/web/bind/annotation/RestController;"))
                && methodDefinition.getAccess() == Opcodes.ACC_PUBLIC;
    }

    private boolean isCalledMethod(MethodDefinition methodDefinition, String rootOwner,
            Map<String, List<String>> parentNamesByClassName, Set<MethodId> methodIds) {
        Queue<String> owners = new LinkedList<>();
        Set<String> ownersQueued = new HashSet<>();
        owners.add(rootOwner);
        ownersQueued.add(rootOwner);

        while (!owners.isEmpty()) {
            String owner = owners.remove();
            if (methodIds.contains(MethodId.from(owner, methodDefinition))) {
                return true;
            }
            List<String> parentNames = parentNamesByClassName.get(owner);
            if (parentNames != null && !parentNames.isEmpty()) {
                for (String parentName : parentNames) {
                    if (!ownersQueued.contains(parentName)) {
                        owners.add(parentName);
                        ownersQueued.add(parentName);
                    }
                }
            }
        }
        return false;
    }

    private void mvn(List<String> command, File dir) throws InterruptedException, IOException {
        command = new ArrayList<>(command);
        if (isWindows()) {
            command.addAll(0, Arrays.asList("cmd", "/c", "mvn"));
        } else {
            command.addAll(0, Arrays.asList("mvn"));
        }
        new ProcessBuilder(command).directory(dir).inheritIO().start().waitFor();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
}
