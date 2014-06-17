package com.graphaware.generator.node;

import com.graphaware.common.util.FileScanner;
import com.graphaware.common.util.Pair;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.*;

/**
 * A {@link NodeCreator} that assigns every {@link org.neo4j.graphdb.Node} a "Person" {@link org.neo4j.graphdb.Label},
 * a Male/Female {@link Label} (with equal probabilities), and a randomly generated English name under a property key
 * "name".
 */
public class SocialNetworkNodeCreator implements NodeCreator {

    private static final Label PERSON_LABEL = DynamicLabel.label("Person");
    private static final Label MALE_LABEL = DynamicLabel.label("Male");
    private static final Label FEMALE_LABEL = DynamicLabel.label("Female");
    private static final String NAME = "name";

    private static final SocialNetworkNodeCreator INSTANCE = new SocialNetworkNodeCreator();

    private List<Pair<Label, String>> gendersAndNames = new ArrayList<>();
    private Random random = new Random();

    private SocialNetworkNodeCreator() {
        populateGendersAndNames();
    }

    public static SocialNetworkNodeCreator getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createNode(GraphDatabaseService database) {
        Node node = database.createNode(PERSON_LABEL);

        Pair<Label, String> genderAndName = gendersAndNames.get(random.nextInt(gendersAndNames.size()));
        node.addLabel(genderAndName.first());

        node.setProperty(NAME, genderAndName.second());

        return node;
    }

    private void populateGendersAndNames() {
        for (String line : FileScanner.produceLines(SocialNetworkNodeCreator.class.getClassLoader().getResourceAsStream("fake_names.csv"), 0)) {
            String[] fields = line.split(",");
            gendersAndNames.add(new Pair<>("female".equals(fields[0]) ? FEMALE_LABEL : MALE_LABEL, fields[1] +" "+ fields[2]));
        }
    }
}
