import com.fasterxml.jackson.databind.util.LRUMap;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ImportTest {

    private Map<String, Long> nameToId = new LRUMap<>(0, 10000);

    private void importSomething(String name) {
        if (nameToId.containsKey(name)) {
            //use id
        }
        else {
            //go to index, find it
        }
    }


    @Test
    public void showImport() {
        BatchInserter inserter = BatchInserters.inserter("/tmp/my-db");

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "Marc");
        properties.put("city", "Barcelona");

        long id = inserter.createNode(properties, DynamicLabel.label("Person"));

        inserter.shutdown();
    }
}
