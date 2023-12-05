package ru.gb.lesson2.hw;

import ru.gb.lesson2.anno.Annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        for (Method method : testClass.getDeclaredMethods()) {
            checkTestMethod(method);
            if (method.isAnnotationPresent(Test.class)) { // <- проверить на наличие beforeAll и afterAll
                index = 2;
            }
            if (method.isAnnotationPresent(BeforeAll.class)) {
                index = 1;
            }
            if (method.isAnnotationPresent(AfterAll.class)) {
                index = 3;
            }
            methodMap.computeIfAbsent(index,k -> new ArrayList<>()).add(method);
        }

        methodMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .peek(listOfMethods -> listOfMethods.forEach(mtd -> runTest(mtd, testObj)))
                .collect(Collectors.toList());
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

}
