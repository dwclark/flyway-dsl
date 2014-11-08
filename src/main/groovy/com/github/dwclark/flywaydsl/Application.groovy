package com.github.dwclark.flywaydsl;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.FlywayCallback;

public class Application {

    private static final String INITIAL_VERSION = 1.0;
    private static final String INITIAL_DESCRIPTION = 'flywayinitialize';
    public static final String CURRENT = 'current';
    public static final String HISTORY = 'history';

    final ApplicationConfig config;

    public Application(ApplicationConfig config) {
        this.config = config;
    }

    public boolean isValid() {
        if(!config.action) {
            config.cli.usage();
            return false;
        }

        if(!config.action) {
            System.err.println("No value specified for database url");
            return false;
        }

        if(!config.env) {
            System.err.println("No value specified for database environement");
            return false;
        }

        return true;
    }

    public FlywayCallback[] populateCallbacks() {
        if(!config.callbacks) {
            return [] as FlywayCallback[];
        }
        else {
            return config.callbacks.inject([]) { List list, String callback ->
                Class callbackClass = Class.forName(callback);
                list << callbackClass.getDeclaredConstructor(ApplicationConfig).newInstance(config); } as FlywayCallback[];
        }
    }
    
    public Flyway getFlyway() {
        Flyway target = new Flyway();
        target.classLoader = Application.classLoader;
        target.setDataSource(config.url, config.user, config.password);
        target.outOfOrder = false;
        
        FlywayCallback[] callbacks = populateCallbacks();
        if(callbacks) {
            target.callbacks = callbacks;
        }
        
        if(config.schemas) {
            target.schemas = config.schemas as String[];
        }

        List<String> locations = [ 'classpath:history' ];
        HistoryArea harea = new HistoryArea(null, config.environments, config.stages, '');
        harea.toRun(config.env, config.action).each { ESVArea esvArea ->
            locations.add("classpath:${CURRENT}/${esvArea.path}".toString()); }
        
        target.locations = locations as String[];
        target.validateOnMigrate = false;
        
        if(!target.info().applied()) {
            target.setInitVersion(INITIAL_VERSION);
            target.initDescription = INITIAL_DESCRIPTION;
            target.initOnMigrate = true;
        }

        return target;
    }

    public void run() {
        if(!valid) {
            return;
        }

        flyway.with {
            if(HistoryArea.REPAIR == config.action) {
                repair();
            }
            else {
                migrate();
            } };
    }

    public static void main(String[] args) {
        ApplicationConfig config = new ApplicationConfig(args);
        if(config.help || !config.action) {
            config.cli.usage();
            return;
        }

        new Application(config).run();
    }
}