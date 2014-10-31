package com.github.dwclark.flywaydsl;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MigrationsExtension {
    String src = 'src/main/migrations';
}

public class FlywayDslPlugin implements Plugin<Project> {

    public static final String VERSIONING_GROUP_NAME = 'Versioning';

    public void addVersioning(Project project) {
        def factory = { VersionPosition pos ->
            return new VersionBumper(new File(project.migrations.src),
                                     new File('gradle.properties'), pos); };

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
        addVersioning(project);
    }
}