package com.github.dwclark.flywaydsl;

import spock.lang.*;

public class SqlFileTest extends Specification {
    
    def "Single Item"() {
        setup:
        String sql = "insert into foobar foo values ('bar')";
        List<String> list = SqlFile.toList(new StringReader(sql));

        expect:
        list.size() == 1;
        list[0] == sql;
    }

    def "Multiple Items"() {
        String one = "one two three";
        String two = "four five six";
        String three = "seven eight nine";
        String sql = "${one}\n/\n${two}\n/\n${three}";
        StringReader reader = new StringReader(sql);
        List<String> list = SqlFile.toList(reader);

        expect:
        list.size() == 3;
        list[0] == one;
        list[1] == two;
        list[2] == three;
    }
}