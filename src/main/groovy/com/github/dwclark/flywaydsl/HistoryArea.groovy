package com.github.dwclark.flywaydsl;

import java.util.concurrent.atomic.AtomicInteger;

public class HistoryArea {
    
    public static final String COMMON = 'common';
    
    final File folder;
    final List<String> environments;
    final List<String> stages;
    final Map<String,AtomicInteger> stageIndexes;
    final String version;
    final List<ESVArea> esvAreas;

    public HistoryArea(File folder, List<String> environments,
                       List<String> stages, String version) {
        this.folder = folder;
        this.environments = environments.contains(COMMON) ? environments : [ COMMON ] + environments;
        this.stages = stages;
        this.version = version;
        
        List<ESVArea> tmp = [];
        environments.each { String environment ->
            stages.each { String stage ->
                tmp << new ESVArea(this, environment, stage); }; };
        esvAreas = tmp.asImmutable();

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
        return stages.contains(stage);
    }

    public boolean isLegalEnvironment(String environment) {
        return environments.contains(environment);
    }
}