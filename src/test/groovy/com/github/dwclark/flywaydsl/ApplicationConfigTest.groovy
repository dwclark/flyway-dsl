package com.github.dwclark.flywaydsl;

import org.junit.rules.TemporaryFolder;
import org.junit.*;
import spock.lang.*;

public class ApplicationConfigTest extends Specification {

    @Rule TemporaryFolder folder;

    def "Test Help"() {
        setup:
        ApplicationConfig config;

        when:
        config = new ApplicationConfig([ '-h' ] as String[])

        then:
        config.help;

        when:
        config = new ApplicationConfig([ '--help' ] as String[])
        
        then:
        config.help;

        when:
        config = new ApplicationConfig([] as String[]);
        
        then:
        config.help;
    }

    def "Test Config"() {
        setup:
        Properties dbProps = [ url: 'the-url', user: 'the-user', password: 'the-password' ] as Properties;
        File propsFile = new File(folder.root, 'db.properties');
        propsFile.withWriter { writer -> dbProps.store(writer, ''); };
        ApplicationConfig config;

        when:
        config = new ApplicationConfig([ '-f', propsFile.absolutePath, 'release'] as String[]);

        then:
        config.action == 'release';
        config.url == dbProps.url;
        config.user == dbProps.user;
        config.password == dbProps.password;

        when:
        config = new ApplicationConfig([ '-f', propsFile.absolutePath] as String[]);

        then:
        config.action == null;
    }
}