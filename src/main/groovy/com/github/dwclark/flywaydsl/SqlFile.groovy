package com.github.dwclark.flywaydsl;

public class SqlFile {
    
    public static final int FOUR_K = 4096;
    public static final String SEPARATOR = '/';

    public static List<String> toList(File file) {
        file.withReader(MigrationStage.ENCODING) { reader -> toList(reader); };
    }

    public static List<String> toList(ClassLoader loader, String resource) {
        loader.getResourceAsStream(resource).withReader(MigrationStage.ENCODING) { reader -> toList(reader); };
    }

    public static List<String> toList(Reader reader) {
        StringBuilder current = new StringBuilder(FOUR_K);
        List all = [];

        def addIfNeeded = {
            String text = current.toString().trim();
            if(text) all.add(text);
            current.length = 0; };

        reader.eachLine { String line ->
            String text = line.trim();
            if(text == SEPARATOR) {
                addIfNeeded();
            }
            else {
                current.append(line);
                current.append('\n');
            } };

        addIfNeeded()
        return all;
    }
}