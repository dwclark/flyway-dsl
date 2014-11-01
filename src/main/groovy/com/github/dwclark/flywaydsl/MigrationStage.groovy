package com.github.dwclark.flywaydsl;

public class MigrationStage {

    public static final String ENCODING = 'UTF-8';

    final ESVArea esvArea;
    final String comment;
    final String sql;

    public MigrationStage(ESVArea esvArea, String comment, String sql) {
        this.esvArea = esvArea;
        this.comment = processComment(comment);
        this.sql = sql;
    }

    public static String processComment(String comment) {
        return comment.replaceAll('[^a-zA-Z0-9\\.]', '_').replaceAll('_{1,}', '_');
    }

    public String getVersion() {
        return "${esvArea.historyArea.version}.${esvArea.historyArea.stageOrder(esvArea.stage)}.${esvArea.nextIndex}";
    }

    public String getFileName() {
        return "V${version}__${comment}.sql".toString()
    }

    public void save() {
        File file = new File(esvArea.folder, fileName)
        file.createNewFile();
        file.setText(sql, ENCODING);
    }
}