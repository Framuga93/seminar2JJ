package ru.gb.lesson2.hw;

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

        List<Method> methods = new ArrayList<>();
        HashMap<Integer, List<Method>> methodMap = new HashMap<>();
        for (Method method : testClass.getDeclaredMethods()) {
            checkTestMethod(method);
            if (method.isAnnotationPresent(Test.class)) { // <- проверить на наличие beforeAll и afterAll
                List<Method> tempList = new ArrayList<>();
                methodMap.put(2, method);
            }
            if (method.isAnnotationPresent(BeforeAll.class)) {
                methodMap.put(1, method);
            }
            if (method.isAnnotationPresent(AfterAll.class)) {
                methodMap.put(3, method);
            }
        }

        methods = methodMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());


        methods.forEach(it -> runTest(it, testObj));
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
