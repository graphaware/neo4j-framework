package ${package};

import com.graphaware.test.integration.IntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 * {@link IntegrationTest} for {@link NodeCountApi}.
 */
public class NodeCountIntegrationTest extends NeoServerIntegrationTest {

    @Test
    public void apiShouldBeMounted() {
        assertEquals("0", get(baseUrl()+"/graphaware/count", HttpStatus.SC_OK));
    }
}
