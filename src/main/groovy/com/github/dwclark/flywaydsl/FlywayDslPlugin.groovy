package com.github.dwclark.flywaydsl;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MigrationsExtension {
    public static final String NAME = 'migrations';

    String src = 'src/main/migrations';
    String compileTo = 'src/dist/lib/history';
    String sql = 'src/main/sql';
    String applicationName = 'migrate'
    
    List<String> callbacks;
    List<String> schemas;
    List<String> environments;
    List<String> stages;
}

public class FlywayDslPlugin implements Plugin<Project> {
    public static final String VERSIONING_GROUP_NAME = 'Versioning';

    public void addDependencies(Project project) {
        project.apply(plugin: 'groovy');
        project.apply(plugin: 'application');

        project.dependencies {
            compile(localGroovy())
            //TODO: Make this dynamically found
            compile("com.github.dwclark:flyway-dsl:1.0.0")
        }
    }

    public static final String MIGRATION_TASK_NAME = 'compileMigration';
    public static final String MIGRATION_GROUP_NAME = 'Migration';

    public void writeAppConfiguration(Project project) {
        def ext = project[MigrationsExtension.NAME];
        Map map = [ (ApplicationConfig.STAGES): ext.stages.join(','),
                    (ApplicationConfig.ENVIRONMENTS): ext.environments.join(','),
                    (ApplicationConfig.SCRIPT): ext.applicationName ];

        if(ext.callbacks) {
            map[ApplicationConfig.CALLBACKS] = ext.callbacks.join(',');
        }
        
        if(ext.schemas) {
            map[ApplicationConfig.SCHEMAS] = ext.schemas.join(',');
        }

        File file = new File(project.file(ext.compileTo), ApplicationConfig.RESOURCE);
        file.withWriter { writer -> (map as Properties).store(writer, ''); };
    }

    public void addCompilation(Project project) {
        project.task(MIGRATION_TASK_NAME).with {
            doLast {
                writeAppConfiguration(project);
                def ext = project[MigrationsExtension.NAME];
                HistoryArea harea = new HistoryArea(project.file(ext.compileTo), ext.environments,
                                                    ext.stages, project.version);
                File migrateScript = project.file("${ext.src}/${project.version}/${Dsl.NAME}");
                File sqlFolder = project.file(ext.sql);
                new DslSetup(harea, migrateScript, sqlFolder).executeScript(); };
            
            description = 'Builds permanent, versioned migration files from migrate.groovy';
            group = MIGRATION_GROUP_NAME; };

        project.afterEvaluate { Project after ->
            after.tasks.getByName(MIGRATION_TASK_NAME).with {
                def ext = project[MigrationsExtension.NAME];
                inputs.files(after.files(ext.src, ext.sql));
                outputs.files(after.files(ext.compileTo)); }; };
    }

    public void addPhases(Project project) {
        project.afterEvaluate { Project after ->
            def ext = project[MigrationsExtension.NAME];
            ext.stages.each { String stage ->
                String taskName = HistoryArea.stageToRun(stage);
                after.task(taskName).with {
                    group = MIGRATION_GROUP_NAME;
                    description = "Run the ${stage} phase";
                    doLast {
                        after.javaexec {
                            standardOutput = System.out;
                            errorOutput = System.err;
                            main = Application.name;
                            classpath(project.configurations.compile)
                            classpath(project.file(ext.compileTo));
                            args('-f', project.file('db.properties').absolutePath, taskName); }; }; }; }; };
    }

    public void addVersioning(Project project) {
        def factory = { VersionPosition pos ->
            return new VersionBumper(project.file(project.migrations.src),
                                     project.file('gradle.properties'), pos); };

        project.task('bumpMajorVersion').with {
            group = VERSIONING_GROUP_NAME;
            description = 'Bump major version number';
            doLast { factory(VersionPosition.MAJOR).bump(); }; };

        project.task('bumpMinorVersion').with {
            group = VERSIONING_GROUP_NAME;
            description = 'Bump minor version number';
            doLast { factory(VersionPosition.MINOR).bump(); }; };

        project.task('bumpPointVersion').with {
            group = VERSIONING_GROUP_NAME;
            description = 'Bump point version number';
            doLast { factory(VersionPosition.POINT).bump(); }; };
    }

    public void apply(Project project) {
        project.extensions.create(MigrationsExtension.NAME, MigrationsExtension);
        addDependencies(project);
        addVersioning(project);
        addCompilation(project);
        addPhases(project);
    }
}