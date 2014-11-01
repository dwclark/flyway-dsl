package com.github.dwclark.flywaydsl;

import org.junit.rules.TemporaryFolder;
import org.junit.*;
import spock.lang.*;

public class DslTest extends Specification {

    @Rule TemporaryFolder migrateFolder;
    @Rule TemporaryFolder historyFolder;
    @Rule TemporaryFolder sqlFolder;

    def "Single Line Sql File"() {
        setup:
        String insert = "insert into foo (bar) values (1)";
        String version = '1.0.0';
        File migrationScript = new File(migrateFolder.root, 'migrate.groovy');
        migrationScript.setText("release sql: '${insert}', comment: 'No Comment', env: 'prod'");
        List<String> environments = [ 'prod' ];
        List<String> stages = [ 'release' ];
        HistoryArea historyArea = new HistoryArea(historyFolder.root, environments, stages, version);
        DslSetup dslSetup = new DslSetup(historyArea, migrationScript, sqlFolder.root);
        dslSetup.executeScript();
        File outputFolder = new File(historyFolder.root, "prod/release/${version}/");
        File[] sqlFiles = outputFolder.listFiles({ File dir, String name -> name.endsWith('.sql') } as FilenameFilter);

        expect:
        sqlFiles.length == 1;
        sqlFiles[0].getText(MigrationStage.ENCODING) == insert;
    }

    def "Single Line Resource File"() {
        setup:
        String text = 'blah blah blah';
        File sqlScriptFile = new File(sqlFolder.root, 'silly.sql');
        sqlScriptFile.setText(text, MigrationStage.ENCODING);
        String version = '1.0.0';
        File migrationScript = new File(migrateFolder.root, 'migrate.groovy');
        migrationScript.setText("release resource: 'silly.sql', comment: 'No Comment', env: 'prod'");
        List<String> environments = [ 'prod' ];
        List<String> stages = [ 'release' ];
        HistoryArea historyArea = new HistoryArea(historyFolder.root, environments, stages, version);
        DslSetup dslSetup = new DslSetup(historyArea, migrationScript, sqlFolder.root);
        dslSetup.executeScript();
        File outputFolder = new File(historyFolder.root, "prod/release/${version}/");
        File[] sqlFiles = outputFolder.listFiles({ File dir, String name -> name.endsWith('.sql') } as FilenameFilter);

        expect:
        sqlFiles.length == 1;
        sqlFiles[0].getText(MigrationStage.ENCODING) == text;

    }
}