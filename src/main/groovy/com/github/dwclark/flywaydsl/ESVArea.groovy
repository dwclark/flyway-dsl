package com.github.dwclark.flywaydsl;

public class ESVArea {

    final int stageOrder;
    final String environment;
    final String stage;
    final HistoryArea historyArea;
    final File folder;

    public ESVArea(HistoryArea historyArea, String environment, String stage) {
        this.environment = environment;
        this.stage = stage;
        this.historyArea = historyArea;
        this.stageOrder = historyArea.stageOrder(stage);
        this.folder = prepare();
    }

    public File prepare() {
        if(historyArea.folder) {
            File envFolder = new File(historyArea.folder, environment);
            if(!envFolder.exists()) {
                envFolder.mkdir();
            }
            
            File stageFolder = new File(envFolder, stage);
            if(!stageFolder.exists()) {
                stageFolder.mkdir();
            }
            
            File versionFolder = new File(stageFolder, historyArea.version);
            if(versionFolder.exists()) {
                versionFolder.deleteDir();
            }
            
            versionFolder.mkdir();
            return versionFolder;
        }
        else {
            return null;
        }
    }
    
    @Override
    public boolean equals(Object rhs) {
        if(!(rhs instanceof ESVArea)) {
            return false;
        }

        ESVArea toCmp = (ESVArea) rhs;
        return (environment == toCmp.environment &&
                stage == toCmp.stage)
    }

    @Override
    public int hashCode() {
        return environment.hashCode() + stage.hashCode();
    }

    public String getPath() {
        return "${environment}/${stage}/".toString();
    }
    
    public void save(List<MigrationStage> list) {
        list.eachWithIndex { MigrationStage mfile, int index -> mfile.save(this, index + 1); };
    }

    public int getNextIndex() {
        return historyArea.nextStageIndex(stage);
    }

    public boolean matches(String environment, String stage) {
        return (this.environment == environment && this.stage == stage);
    }
}