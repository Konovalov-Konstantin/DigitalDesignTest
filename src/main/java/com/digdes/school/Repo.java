package com.digdes.school;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Repo {

    private final String pathToFile = "src/main/resources/file.txt";
    private final String[] KEYS = {"id", "lastName", "age", "cost", "active"};

    List<String> whereConditionsOfRequest;  // часть запроса после 'WHERE'

    Map<String, List<String>> selectConditionsMap = new HashMap<>();  // пары ключ-значение из 'WHERE'

    // выборка
    public List<Map<String, Object>> select(List<Map<String, Object>> myList, String inputString) {

        List<String> request = editInputRequest(inputString);

        List<Map<String, Object>> selectList = new ArrayList<>();   // лист с результатом выборки

        if (request.stream().anyMatch(s -> s.equalsIgnoreCase("WHERE"))) {

            // обрезка от WHERE до конца строки
            int index = 0;
            for (int i = 0; i < request.size(); i++) {
                if (request.get(i).equalsIgnoreCase("WHERE")) {
                    index = i;
                }
            }
            whereConditionsOfRequest = request.stream().skip(index + 1).collect(Collectors.toList());

            boolean containsANDOperator = false;

            if (whereConditionsOfRequest.stream().map(c -> c.toUpperCase()).collect(Collectors.toList()).contains("AND")) {
                containsANDOperator = true;
            }

            // выборка из WHERE пар условий ключ-значение
            for (int i = 0; i < whereConditionsOfRequest.size(); i++) {
                for (int j = 0; j < KEYS.length; j++) {
                    if (whereConditionsOfRequest.get(i).equalsIgnoreCase(KEYS[j])) {
                        ArrayList<String> conditions = new ArrayList<>();
                        conditions.add(whereConditionsOfRequest.get(i + 1));
                        conditions.add(whereConditionsOfRequest.get(i + 2));
                        selectConditionsMap.put(KEYS[j], conditions);
                    }
                }
            }

            // проверка на корректность входных параметров (есть ли они в массиве KEYS)
            for (int i = 0; i < whereConditionsOfRequest.size(); i += 4) {
                int finalI = i;
                if (Arrays.stream(KEYS).noneMatch(c -> c.equalsIgnoreCase(whereConditionsOfRequest.get(finalI)))) {
                    System.out.println("Некорректный параметр в запросе: " + whereConditionsOfRequest.get(finalI));
                    throw new RuntimeException();
                }
            }

            // проверка на корректность типа входных данных
            for (int i = 0; i < whereConditionsOfRequest.size(); i += 4) {
                if (whereConditionsOfRequest.get(i).equalsIgnoreCase("id")) {
                    try {
                        Long.parseLong(whereConditionsOfRequest.get(i + 2));
                    } catch (NumberFormatException e) {
                        System.out.println("Несоответствующий тип данных для колонки id");
                        throw new RuntimeException();
                    }
                }
                if (whereConditionsOfRequest.get(i).equalsIgnoreCase("lastName")) {
                    if (Pattern.matches("[a-zA-Zа-яА-Я]+", whereConditionsOfRequest.get(i + 2).replaceAll("%", ""))) {
                    } else {
                        System.out.println("Несоответствующий тип данных для колонки lastName");
                        throw new RuntimeException();
                    }
                }
                if (whereConditionsOfRequest.get(i).equalsIgnoreCase("cost")) {
                    try {
                        Double.parseDouble(whereConditionsOfRequest.get(i + 2));
                    } catch (NumberFormatException e) {
                        System.out.println("Несоответствующий тип данных для колонки cost");
                        throw new RuntimeException();
                    }
                }
                if (whereConditionsOfRequest.get(i).equalsIgnoreCase("age")) {
                    try {
                        Long.parseLong(whereConditionsOfRequest.get(i + 2));
                    } catch (NumberFormatException e) {
                        System.out.println("Несоответствующий тип данных для колонки age");
                        throw new RuntimeException();
                    }
                }
            }

            // выборка из листа записей удоовлетворяющих условиям из WHERE
            for (Map<String, Object> pair : myList) {
                if (containsANDOperator) {   // если в условии есть AND
                    boolean isAllConditionsHonour = true;

                    for (String condition : selectConditionsMap.keySet()) { // ключи из условия WHERE
                        String operator = selectConditionsMap.get(condition).get(0);

                        if (operator.equals("=")) {
                            if (selectConditionsMap.get(condition).get(1).equalsIgnoreCase((String) pair.get(condition))) {
                                continue;
                            } else {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equals("<=")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v <= Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                continue;
                            } else {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equals(">=")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v >= Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                continue;
                            } else {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equals("<")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v < Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                continue;
                            } else {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equals(">")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v > Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                continue;
                            } else {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equals("!=")) {
                            if (pair.get(condition).equals(selectConditionsMap.get(condition).get(1))) {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equalsIgnoreCase("like")) {
                            String s1 = pair.get(condition).toString();
                            String s2 = selectConditionsMap.get(condition).get(1).replaceAll("%", "");
                            if (!s1.equals(s2)) {
                                isAllConditionsHonour = false;
                                break;
                            }
                        } else if (operator.equalsIgnoreCase("ilike")) {
                            String s1 = pair.get(condition).toString().toLowerCase();
                            String s2 = selectConditionsMap.get(condition).get(1).toLowerCase().replaceAll("%", "");
                            if (!s1.contains(s2)) {
                                isAllConditionsHonour = false;
                                break;
                            }
                        }
                    }
                    if (isAllConditionsHonour) { // если выполняются все условия AND
                        selectList.add(pair);
                    }
                } else { // если в условии нет AND
                    for (String condition : selectConditionsMap.keySet()) { // ключи из условия WHERE  {lastName, id}
                        String operator = selectConditionsMap.get(condition).get(0);

                        if (operator.equals("=")) {
                            if (selectConditionsMap.get(condition).get(1).equalsIgnoreCase((String) pair.get(condition))) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equals("<=")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v <= Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equals(">=")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v >= Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equals("<")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v < Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equals(">")) {
                            double v = Double.parseDouble(String.valueOf(pair.get(condition) == null ? 0 : Double.parseDouble(String.valueOf(pair.get(condition)))));
                            if (v > Double.parseDouble(selectConditionsMap.get(condition).get(1))) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equals("!=")) {
                            if (pair.get(condition).equals(selectConditionsMap.get(condition).get(1))) {
                                break;
                            } else {
                                selectList.add(pair);
                            }
                        } else if (operator.equalsIgnoreCase("like")) {
                            String s1 = pair.get(condition).toString();
                            String s2 = selectConditionsMap.get(condition).get(1).replaceAll("%", "");
                            if (s1.equals(s2)) {
                                selectList.add(pair);
                                break;
                            }
                        } else if (operator.equalsIgnoreCase("ilike")) {
                            String s1 = pair.get(condition).toString().toLowerCase();
                            String s2 = selectConditionsMap.get(condition).get(1).toLowerCase().replaceAll("%", "");
                            if (s1.contains(s2)) {
                                selectList.add(pair);
                                break;
                            }
                        }
                    }
                }
            }
        } else {    // если в условии нет 'WHERE'
            selectList.addAll(myList);
        }
        return selectList;
    }

    // вставка
    public List<Map<String, Object>> insert(List<Map<String, Object>> myList, String inputString) {

        List<String> request = editInputRequest(inputString);

        if (request.contains("INSERT") && request.contains("VALUES")) {

            Map<String, Object> insertMap = new HashMap<>();

            boolean isRequestCorrect = false;

            for (int i = 0; i < request.size(); i++) {
                for (int j = 0; j < KEYS.length; j++) {
                    if (request.get(i).equalsIgnoreCase(KEYS[j])) {
                        try {
                            insertMap.put(KEYS[j], request.get(i + 2));
                            isRequestCorrect = true;
                        } catch (RuntimeException e) {
                            System.out.println("invalid request");
                            isRequestCorrect = false;
                        }
                    }
                }
            }
            if (isRequestCorrect) {
                myList.add(insertMap);
                writeFile(myList);  // запись в файл
            } else {
                System.out.println("invalid request");
            }
        }
        System.out.println("    ***  После вставки: ***");
        myList.stream().forEach(System.out::println);
        return myList;
    }

    // удаление
    public List<Map<String, Object>> delete(List<Map<String, Object>> myList, String request) {

        List<Map<String, Object>> objectsToDelete = select(myList, request);

        myList.removeAll(objectsToDelete);

        System.out.println("    ***  После удаления: ***");
        myList.stream().forEach(System.out::println);

        writeFile(myList); // запись в файл
        return myList;
    }

    // обновление
    public List<Map<String, Object>> update(List<Map<String, Object>> myList, String inputString) {

        List<String> request = editInputRequest(inputString);

        List<Map<String, Object>> objectsToUpdate = select(myList, inputString);

        Map<String, Object> updateConditionsMap = new HashMap<>();  // условия из WHERE
        Map<String, Object> updateValuesMap = new HashMap<>();      // значения для обновления
        List<String> valuesToUpdate = new ArrayList<>();

        if (request.stream().anyMatch(s -> s.equalsIgnoreCase("VALUES"))) {
            valuesToUpdate = request.stream().skip(2).collect(Collectors.toList());    // пары ключ-значение для изменения записи
        } else {
            valuesToUpdate = request.stream().skip(1).collect(Collectors.toList());
        }

        if (request.stream().anyMatch(s -> s.equalsIgnoreCase("WHERE"))) {
            int index = 0;
            for (int i = 0; i < request.size(); i++) {
                if (request.get(i).equalsIgnoreCase("WHERE")) {
                    index = i;
                }
            }

            if (request.stream().anyMatch(s -> s.equalsIgnoreCase("WHERE"))) {

                // выборка из WHERE пар условий ключ-значение
                for (int i = 0; i < whereConditionsOfRequest.size(); i++) {
                    for (int j = 0; j < KEYS.length; j++) {
                        if (whereConditionsOfRequest.get(i).equalsIgnoreCase(KEYS[j])) {
                            updateConditionsMap.put(KEYS[j], whereConditionsOfRequest.get(i + 2));
                        }
                    }
                }

                List<String> valuesToUpdateWithoutWherePart = new ArrayList<>();
                int indexOfWhere = valuesToUpdate.stream().map(c -> c.toUpperCase()).collect(Collectors.toList()).indexOf("WHERE");
                for (int i = 0; i < indexOfWhere; i++) {
                    valuesToUpdateWithoutWherePart.add(valuesToUpdate.get(i));
                }

                for (int i = 0; i < valuesToUpdateWithoutWherePart.size(); i++) {
                    for (int j = 0; j < KEYS.length; j++) {
                        if (valuesToUpdateWithoutWherePart.get(i).equalsIgnoreCase(KEYS[j])) {
                            updateValuesMap.put(KEYS[j], valuesToUpdate.get(i + 2));
                        }
                    }
                }
            } else {
                for (int i = 0; i < valuesToUpdate.size(); i++) {
                    for (int j = 0; j < KEYS.length; j++) {
                        if (valuesToUpdate.get(i).equalsIgnoreCase(KEYS[j])) {
                            updateValuesMap.put(KEYS[j], valuesToUpdate.get(i + 2));
                        }
                    }
                }

            }

            for (Map<String, Object> map : myList) {
                for (Map<String, Object> x : objectsToUpdate) {
                    if (map.equals(x)) {
                        for (Map.Entry<String, Object> stringObjectEntry : updateValuesMap.entrySet()) {
                            map.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                        }
                    }
                }
            }
        } else {    // если в условии нет 'WHERE'
            objectsToUpdate.addAll(myList);
        }

        System.out.println("    ***  После изменения: ***");
        myList.stream().forEach(System.out::println);
        writeFile(myList);  // запись в файл
        return myList;
    }



    // правка входного запроса
    private static List<String> editInputRequest(String inputString) {

        List<String> inputQuery = Arrays.stream(inputString
                .replaceAll("\\s*>=\\s*", " >= ")
                .replaceAll("\\s*<=\\s*", " <= ")
                .replaceAll("\\s*[‘’',]=\\s*", " = ")
                .replaceAll("\\s*[‘’',]<\\s*", " < ")
                .replaceAll("\\s*[‘’',]>\\s*", " > ")
                .replaceAll("\\s*[‘’',]!=\\s*", " != ")
                .replaceAll("\\s,\\s*", ", ")
                .split(" ")).map((c) -> c.replaceAll("[‘’',\\s]", "")).collect(Collectors.toList());
        return inputQuery;
    }

    // запись данных в файл после внесения изменений
    public void writeFile(List<Map<String, Object>> list) {
        File file = new File(pathToFile);
        try (FileWriter fr = new FileWriter(file)) {
            for (Map<String, Object> map : list) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    fr.write(entry.getKey() + " = " + entry.getValue() + ", ");
                }
                fr.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // заполнение листа данными из файла
    public List<Map<String, Object>> initList() {
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            File file = new File(pathToFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                List<String> inputString = editInputString(line);
                Map<String, Object> data = new HashMap<>();
                for (int i = 0; i < inputString.size(); i++) {
                    for (int j = 0; j < KEYS.length; j++) {
                        if (inputString.get(i).equalsIgnoreCase(KEYS[j])) {
                            data.put(KEYS[j], inputString.get(i + 1));
                        }
                    }
                }
                list.add(data);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // преобразование входного запроса
    private static List<String> editInputString(String inputString) {
        List<String> input = Arrays.stream(inputString.replaceAll("\\s*=\\s*", " ").replaceAll("\\s,\\s*", ", ").split(" ")).map((c) -> c.replaceAll("[‘’'’,]", "")).collect(Collectors.toList());
        return input;
    }

    // заполнение файла исходными данными
    public void fillFile() {
        File file = new File(pathToFile);
        try (FileWriter fr = new FileWriter(file)) {
            fr.write("'id'=1, 'lastName' = 'Petrov' , 'age'=30, 'active'=false \n" + "'id'=2, 'lastName' = 'Danilov' , 'age'=35, 'active'=false \n" + "'id'=3, 'lastName' = 'Fedorov' , 'age'=40, 'active'=true\n" + "'id'=4, 'lastName' = 'Arbuzov' , 'age'=38, 'active'=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
