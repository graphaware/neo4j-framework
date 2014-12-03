package com.graphaware.test.util;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.*;

/**
 * Test for {@link TestUtils}.
 */
public class TestUtilsTest extends WrappingServerIntegrationTest {

    @Test
    public void equalJsonStringsShouldBeEqual() {
        assertJsonEquals("{\"name\":\"test\", \n \"age\":50}", "{\"name\":\"test\",\"age\":50}");
        assertJsonEquals("{\"age\":50,\"name\":\"test\"}", "{\"name\":\"test\",\"age\":50}");
    }

    @Test(expected = AssertionError.class)
    public void differentJsonStringsShouldNotBeEqual() {
        assertJsonEquals("{\"name\":\"test\",\"age\":40}", "{\"name\":\"test\",\"age\":50}");
    }

    @Test
    public void shouldBeAbleToExecuteCypherStatement() throws InterruptedException {
        executeCypher(baseNeoUrl(), "CREATE (:Person {name:'Michal'})");

        Thread.sleep(100);

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:Person {name:'Michal'})");
    }

    @Test
    public void shouldBeAbleToExecuteCypherStatements() {
        executeCypher(baseNeoUrl(), "CREATE (:Person {name:'Michal'})", "CREATE (:Person {name:'Vince'})");

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:Person {name:'Michal'}), (:Person {name:'Vince'})");
    }
}
