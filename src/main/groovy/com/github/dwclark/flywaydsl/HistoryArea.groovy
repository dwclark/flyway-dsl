package com.github.dwclark.flywaydsl;

import java.util.concurrent.atomic.AtomicInteger;

public class HistoryArea {
    
    public static final String COMMON = 'common';
    public static final String REPAIR = 'runRepair';

    final File folder;
    final List<String> environments;
    final List<String> stages;
    final Map<String,AtomicInteger> stageIndexes;
    final String version;
    final List<ESVArea> esvAreas;

    public HistoryArea(File folder, List<String> environments, List<String> stages, String version) {
        this.folder = folder;
        this.environments = environments.contains(COMMON) ? environments : [ COMMON ] + environments;
        this.stages = stages;
        this.version = version;
        
        List<ESVArea> tmp = [];
        this.environments.each { String environment ->
            this.stages.each { String stage ->
                tmp << new ESVArea(this, environment, stage); }; };
        this.esvAreas = tmp.asImmutable();

        this.stageIndexes = stages.inject([:]) { Map map, String stage ->
            map[stage] = new AtomicInteger(1); return map; }.asImmutable();
    }

    public static int order(List<String> list, String toFind) {
        for(int i = 0; i < list.size(); ++i) {
            if(list[i] == toFind) {
                return (i + 1);
            }
        }

        return -1;
    }

    public int stageOrder(String stage) {
        int ret = order(stages, stage);
        if(ret == -1) {
            throw new IllegalArgumentException("Could not find stage ${stage}");
        }

        return ret;
    }

    public int environmentOrder(String environment) {
        int ret = order(environments, environment);
        if(ret == -1) {
            throw new IllegalArgumentException("Could not find environment ${environment}");
        }

        return ret;
    }

    public Map<ESVArea, List<MigrationStage>> toMigrationsMap() {
        esvAreas.inject([:]) { Map map, ESVArea esvArea -> map[esvArea] = []; map; };
    }

    public int nextStageIndex(String stage) {
        return stageIndexes[stage].andIncrement;
    }

    public boolean isLegalStage(String stage) {
        return (stage == REPAIR || stages.contains(stage));
    }

    public boolean isLegalEnvironment(String environment) {
        return environments.contains(environment);
    }

    public static String runToStage(String runner) {
        return runner.substring(3,4).toLowerCase() + runner.substring(4);
    }

    public static String stageToRun(String stage) {
        return 'run' + stage.substring(0,1).toUpperCase() + stage.substring(1);
    }

    public List<ESVArea> toRun(String argEnvironment, String runner) {
        List<ESVArea> ret = [];
        environmentsToRun(argEnvironment).each { String environment ->
            stagesToRun(runner).each { String stage ->
                ret << esvAreas.find { ESVArea esvArea -> esvArea.matches(environment, stage); }; }; };
        return ret;
    }

    public List<String> environmentsToRun(String environment) {
        if(!isLegalEnvironment(environment)) {
            throw new IllegalArgumentException("${environment} is not a legal environment");
        }

        return [ COMMON, environment ];
    }

    public List<String> stagesToRun(String runner) {
        String stage = runToStage(runner);

        if(!isLegalStage(stage)) {
            throw new IllegalArgumentException("${stage} is not a legal stage");
        }

        if(stage == REPAIR) {
            return stages;
        }
        
        boolean stop = false;
        List<String> ret = [];
        stages.each { String possibleStage ->
            if(!stop) {
                ret << possibleStage;
            }
            
            if(stage == possibleStage) {
                stop = true;
            } };
        
        return ret;
    }
}