package ch.ergon.adam.core.reflection;

import com.google.common.reflect.ClassPath;

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ReflectionHelper {

    public final static Map<String, Set<Class<?>>> classesByPackageCache = new HashMap<>();
    public final static Map<String, Set<String>> resourcesByPathCache = new HashMap<>();


    private static Set<Class<?>> findAllClassesForPackage(String packageName) {
        if (!classesByPackageCache.containsKey(packageName)) {
            try {
                Set<Class<?>> classes = ClassPath.from(ReflectionHelper.class.getClassLoader()).getAllClasses()
                    .stream()
                    .filter(c -> c.getPackageName().startsWith(packageName))
                    .map(ClassPath.ClassInfo::getName)
                    .map(ReflectionHelper::getClass)
                    .collect(toSet());
                classesByPackageCache.put(packageName, classes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return classesByPackageCache.get(packageName);
    }

    public static Set<String> findAllRessourcesForPath(String path) {
        if (!resourcesByPathCache.containsKey(path)) {
            try {
                Set<String> resources = ClassPath.from(ClassLoader.getSystemClassLoader()).getResources()
                    .stream()
                    .filter(r -> r.getResourceName().startsWith(path))
                    .map(ClassPath.ResourceInfo::getResourceName)
                    .collect(toSet());
                resourcesByPathCache.put(path, resources);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return resourcesByPathCache.get(path);
    }

    private static Class<?> getClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    public static <T> Set<Class<? extends T>> findAllSubClasses(String packageName, Class<T> superClass) {
            return findAllClassesForPackage(packageName).stream()
                .filter(superClass::isAssignableFrom)
                .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
                .map(c -> (Class<? extends T>)c)
                .collect(toSet());
    }
}
