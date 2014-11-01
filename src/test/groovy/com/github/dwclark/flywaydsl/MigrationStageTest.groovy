package com.github.dwclark.flywaydsl;

import org.junit.rules.TemporaryFolder;
import org.junit.*;
import spock.lang.*;

public class MigrationStageTest extends Specification {
    
    @Rule TemporaryFolder tempFolder;

    def "Process Comment"() {
        when:
        def comment1 = "foobar boo";

        then:
        MigrationStage.processComment(comment1) == 'foobar_boo';

        when:
        def comment2 = 'blah_________blah';

        then:
        MigrationStage.processComment(comment2) == 'blah_blah';

        when:
        def comment3 = 'one#two@three  four;five';

        then:
        MigrationStage.processComment(comment3) == 'one_two_three_four_five';
    }
}