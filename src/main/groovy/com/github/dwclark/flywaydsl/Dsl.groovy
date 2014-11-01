package com.github.dwclark.flywaydsl;

import org.codehaus.groovy.control.CompilerConfiguration;

public abstract class Dsl extends Script {

    public static final String NAME = 'migrate.groovy';

    public HistoryArea getHistoryArea() {
        return binding.historyArea;
    }

    public Map<ESVArea, List<MigrationStage>> getAllMigrations() {
        return binding.allMigrations;
    }

    public String toId(String name, def args) {
        return "${name} " + args.collect { key, value -> "${key}: ${value}" }.join(', ');
    }

    public String checkStageName(String name, Map args) {
        if(!historyArea.isLegalStage(name)) {
            throw new RuntimeException("${name} is not a legal stage");
        }
        
        return name;
    }

    public String checkEnvironment(String name, Map args) {
        if(!args.containsKey('env')) {
            throw new IllegalArgumentException("You must specify an environment for ${args}");
        }
        
        if(!historyArea.isLegalEnvironment(args['env'])) {
            throw new RuntimeException("${args['env']} is not a legal environment");
        }
        
        return args.containsKey('env') ? args['env'] : HistoryArea.COMMON;
    }

    public String checkComment(String name, Map args) {
        if(!args.containsKey('comment')) {
            throw new RuntimeException("${toId(name, args)} is missing comment");
        }

        return args['comment'];
    }

    public List<String> checkCommands(String name, Map args) {
        if(!args.containsKey('sql') && !args.containsKey('resource')) {
            throw new RuntimeException("${toId(name, args)} needs either sql or resource");
        }

        if(args.containsKey('sql')) {
            return [ args['sql'] ];
        }

        if(args.containsKey('resource')) {
            return SqlFile.toList(getClass().classLoader, args['resource']);
        }
    }

    public Map checkArgs(def originalArgs) {
        if(!originalArgs.length == 1 && !originalArgs[0] instanceof Map) {
            throw new IllegalArgumentException("Migration phases must use named arguments.  You passed ${args}");
        }

        return originalArgs[0];
    }
    
    public void processLine(String name, def originalArgs) {
        Map args = checkArgs(originalArgs);
        String stage = checkStageName(name, args);
        String environment = checkEnvironment(name, args);
        String comment = checkComment(name, args);
        List<String> commands = checkCommands(name, args);
        Map.Entry<ESVArea, List<MigrationStage>> entry = allMigrations.find { key, val -> key.matches(environment, stage); };
        commands.each { String command -> entry.value.add(new MigrationStage(entry.key, comment, command)); };
    }

    def methodMissing(String name, def args) {
        if(!historyArea.isLegalStage(name)) {
            throw new RuntimeException("Cannot handle methodMissing for ${name}. Can only handle methodMissing for ${historyArea.stages}");
        }
        processLine(name, args);
    }
}

public class DslSetup {
    
    final HistoryArea historyArea;
    final File migrateScript;
    final File sqlFolder;

    final Binding binding;
    final Map<ESVArea, List<MigrationStage>> allMigrations;

    public DslSetup(HistoryArea historyArea, File migrateScript, File sqlFolder) {
        this.historyArea = historyArea;
        this.allMigrations = historyArea.toMigrationsMap();
        this.migrateScript = migrateScript;
        this.sqlFolder = sqlFolder;
        this.binding = new Binding(historyArea: historyArea, allMigrations: allMigrations);
    }

    public void executeScript() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.scriptBaseClass = Dsl.class.name;
        GroovyClassLoader loader = new GroovyClassLoader(DslSetup.classLoader);
        loader.addClasspath(migrateScript.parent);
        loader.addClasspath(sqlFolder.path);
        GroovyShell shell = new GroovyShell(loader, binding, config);
        shell.evaluate(migrateScript);
        
        allMigrations.each { ESVArea esv, List<MigrationStage> list -> 
            list.each { MigrationStage mstage -> mstage.save(); }; };
    }
}