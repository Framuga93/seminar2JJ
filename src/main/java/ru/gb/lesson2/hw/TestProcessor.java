package ru.gb.lesson2.hw;

import ru.gb.lesson2.anno.Annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TestProcessor {

    /**
     * Данный метод находит все void методы без аргументов в классе, и запускеет их.
     * <p>
     * Для запуска создается тестовый объект с помощью конструткора без аргументов.
     */
    public static void runTest(Class<?> testClass) {
        final Constructor<?> declaredConstructor;
        try {
            declaredConstructor = testClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Для класса \"" + testClass.getName() + "\" не найден конструктор без аргументов");
        }

        final Object testObj;
        try {
            testObj = declaredConstructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось создать объект класса \"" + testClass.getName() + "\"");
        }

        Map<Integer, List<Method>> methodMap = new HashMap<>();
        int index = 0;
        List<Method> beforeAllList = new ArrayList<>();
        List<Method> afterAllList = new ArrayList<>();
        List<Method> testList = new ArrayList<>();
        methodMap.put(1, beforeAllList);
        methodMap.put(2, testList);
        methodMap.put(3, afterAllList);
        for (Method method : testClass.getDeclaredMethods()) {
            checkTestMethod(method);
            if (method.isAnnotationPresent(Skip.class))
                continue;
            if (method.isAnnotationPresent(Test.class)) {
                testList.add(method);
            }
            if (method.isAnnotationPresent(BeforeAll.class)) {
                beforeAllList.add(method);
            }
            if (method.isAnnotationPresent(AfterAll.class)) {
                afterAllList.add(method);
            }
        }

        List<Method> sortTest = sortTestMethodsByOrder(testList);
        methodMap.put(2, sortTest);



        methodMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .forEach(l -> l.forEach(m -> runTest(m,testObj)));
    }


    private static void checkTestMethod(Method method) {
        if (!method.getReturnType().isAssignableFrom(void.class) || method.getParameterCount() != 0) {
            throw new IllegalArgumentException("Метод \"" + method.getName() + "\" должен быть void и не иметь аргументов");
        }
    }

    private static void runTest(Method testMethod, Object testObj) {
        try {
            testMethod.invoke(testObj);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось запустить тестовый метод \"" + testMethod.getName() + "\"");
        } catch (AssertionError e) {

        }
    }

    private static List<Method> sortTestMethodsByOrder(List<Method> methodList){
        List<Method> res = Arrays.stream(Homework.MyTest.class.getDeclaredMethods())
                .filter(m -> m.getAnnotation(Test.class) != null)
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(Test.class).order()))
                .toList();
        return res;
    }

}
