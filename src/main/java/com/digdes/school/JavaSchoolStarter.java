package com.digdes.school;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {

    public JavaSchoolStarter() {
    }

    public List<Map<String, Object>> execute(String request) throws Exception {

        Repo repo = new Repo();

        List<Map<String, Object>> myList = repo.initList();

        if (request.toUpperCase().contains("SELECT")) {
            myList = repo.select(myList, request);
            System.out.println("    ***  Результат SELECT: ***");
            myList.stream().forEach(System.out::println);
        } else if (request.toUpperCase().contains("UPDATE")) {
            myList = repo.update(myList, request);
        } else if (request.toUpperCase().contains("INSERT")) {
            myList = repo.insert(myList, request);
        } else if (request.toUpperCase().contains("DELETE")) {
            myList = repo.delete(myList, request);
        } else {
            System.out.println("Incorrect query");
        }
        return myList;
    }
}
