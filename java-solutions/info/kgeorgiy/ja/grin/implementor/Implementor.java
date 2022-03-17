package info.kgeorgiy.ja.grin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;


public class Implementor implements Impler {

    private static final String LINE_SEP = System.lineSeparator();
    private static final String TAB = " ".repeat(4);

    private String genPackage(Class<?> clazz) {
        return clazz.getPackageName().equals("") ? "" :
                "package " + clazz.getPackageName() + ';' + System.lineSeparator();
    }

    private String genHead(Class<?> clazz) {
        return genPackage(clazz) +
                "public class" + " " +
                clazz.getSimpleName() + "Impl" + " " +
                "implements" + " " + clazz.getCanonicalName() + " {" + LINE_SEP;
    }

    private String getExceptions(Class<?>[] exceptions) {
        if (exceptions.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("throws");
        for (Class<?> exception : exceptions) {
            sb.append(" ").append(exception.getCanonicalName());
        }
        return sb.toString();
    }

    private static String getDefaultValue(Class<?> type) {
        if (type.equals(void.class)) {
            return "";
        } else if (type.equals(boolean.class)) {
            return "false";
        } else if (type.isPrimitive()) {
            return "0";
        }
        return "null";
    }

    private String getBodyFunction(Class<?> returnType) {
        return TAB + TAB + "return " + getDefaultValue(returnType) + ";" + LINE_SEP;
    }

    private int removeModifiers(Method method) {
        // Transient keyword can't mark a method
        return method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
    }

    private String getParameters(Parameter[] parameters) {
        StringBuilder sb = new StringBuilder();
        for (Parameter param : parameters) {
            sb.append(param.getType().getCanonicalName()).append(" ").append(param.getName()).append(", ");
        }
        if (parameters.length > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private String genMethod(Method method) {
        return TAB + TAB + Modifier.toString(removeModifiers(method)) +
                " " + method.getReturnType().getCanonicalName() +
                " " + method.getName() + "(" + getParameters(method.getParameters()) + ") "
                + getExceptions(method.getExceptionTypes()) + " {" + LINE_SEP +
                getBodyFunction(method.getReturnType()) +
                TAB + "}";
    }

    private String genMethods(Class<?> clazz) {
        StringBuilder methods = new StringBuilder();
        for (Method method : clazz.getMethods()) {
            methods.append(LINE_SEP).append(genMethod(method)).append(LINE_SEP);
        }
        System.out.println(methods);
        return methods.toString();
    }

    private Path createPath(Class<?> clazz, Path path) throws ImplerException {
        try {
            path = path.resolve(clazz.getPackageName().replace('.', File.separatorChar))
                    .resolve(clazz.getSimpleName() + "Impl" + ".java");
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return path;
        } catch (IOException e) {
            throw new ImplerException("Cannot create directory for class", e);
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token.isPrimitive() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Cannot implement this interface");
        }
        root = createPath(token, root);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(root)) {
            bufferedWriter.write(genHead(token) + genMethods(token) + "}");
        } catch (IOException e) {
            throw new ImplerException("Cannot implement because of IOException ", e);
        }
    }
}