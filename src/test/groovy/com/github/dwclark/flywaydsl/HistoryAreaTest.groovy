package com.github.dwclark.flywaydsl;

import org.junit.rules.TemporaryFolder;
import org.junit.*;
import spock.lang.*;

public class HistoryAreaTest extends Specification {

    @Rule TemporaryFolder tempFolder;
    
    def "Test Single Environment And Stage"() {
        setup:
        List<String> environments = [ 'production' ];
        List<String> stages = [ 'release' ];
        String version = '1.0.0';
        HistoryArea harea = new HistoryArea(tempFolder.root, environments, stages, version);

        expect:
        new File(tempFolder.root, 'production').exists();
        new File(new File(tempFolder.root, 'production'), 'release').exists();
        new File(new File(new File(tempFolder.root, 'production'), 'release'), '1.0.0').exists();
        harea.isLegalStage('release');
        !harea.isLegalStage('qa');
        harea.nextStageIndex('release') == 1;
        harea.nextStageIndex('release') == 2;
    }

    def "Test Reset Version Area"() {
        when:
        List<String> environments = [ 'production' ];
        List<String> stages = [ 'release' ];
        String version = '1.0.0';
        HistoryArea harea = new HistoryArea(tempFolder.root, environments, stages, version);
        File testFile = new File(harea.esvAreas[0].folder, 'myfile.txt');
        testFile.createNewFile();
        
        then:
        testFile.exists();

        when:
        HistoryArea two = new HistoryArea(tempFolder.root, environments, stages, version);
        
        then:
        !testFile.exists();
    }

    def "Test Multiple Environments And Stages"() {
        setup:
        List<String> environments = [ 'dev', 'qa', 'uat', 'prod' ];
        List<String> stages = [ 'pre', 'release', 'post' ];
        String version = '1.0.1';
        HistoryArea area = new HistoryArea(tempFolder.root, environments, stages, version);
        
        expect:
        area.stageOrder('post') == 3;
        area.environmentOrder('dev') == 2; //common will always be 1
        area.isLegalStage('pre'); 
        !area.isLegalStage('other');
        
    }
}