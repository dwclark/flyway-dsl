package com.github.dwclark.flywaydsl;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MigrationsExtension {
    String src = 'src/main/migrations';
    String history = 'src/dist/lib/history';
    String sql = 'src/main/sql';
    String applicationName = 'migrate'

    List<String> environments;
    List<String> stages;
}

public class FlywayDslPlugin implements Plugin<Project> {
    public static final String VERSIONING_GROUP_NAME = 'Versioning';

    public void addDependencies(Project project) {
        project.apply(plugin: 'application');
    }

    public void addVersioning(Project project) {
        def factory = { VersionPosition pos ->
            return new VersionBumper(file(project.migrations.src),
                                     file('gradle.properties'), pos); };

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
        project.extensions.create('migrations', MigrationsExtension);
        addDependencies(project);
        addVersioning(project);
    }
}