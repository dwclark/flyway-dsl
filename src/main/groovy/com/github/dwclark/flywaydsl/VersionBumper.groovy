package com.github.dwclark.flywaydsl;

public enum VersionPosition {
    MAJOR, MINOR, POINT;
}

public class VersionBumper {

    final File migrationFolder;
    final File propertiesFile;
    final VersionPosition position;
    final String current;

    public VersionBumper(File migrationFolder, File propertiesFile, VersionPosition position) {
        this.migrationFolder = migrationFolder;
        this.propertiesFile = propertiesFile;
        this.position = position;
        
        Properties props = new Properties();
        propertiesFile.withInputStream { InputStream istream -> props.load(istream); }
        this.current = props['version'];
    }

    public String next() {
        String[] ary = current.split('\\.');
        List<Integer> numbers = [];
        ary.each { String str -> numbers.add(str.toInteger()); }

        if(position == VersionPosition.MAJOR) {
            numbers[0] = numbers[0] + 1;
            numbers[1] = 0;
            numbers[2] = 0;
        }
        else if(position == VersionPosition.MINOR) {
            numbers[1] = numbers[1] + 1;
            numbers[2] = 0;
        }
        else {
            numbers[2] = numbers[2] + 1;
        }

        return numbers.join('.');
    }

    public void bump() {
        propertiesFile.withOutputStream { ostream ->
            ([ version: next() ] as Properties).store(ostream, ''); };

        File newFolder = new File(migrationFolder, next());
        newFolder.mkdir();
    }
}