package com.graphaware.module.relcount.demo;

import com.graphaware.common.test.TestDataBuilder;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 *
 */
public abstract class BaseDocumentationDemo {

    public enum Rels implements RelationshipType {
        FOLLOWS,
        LIVES_IN
    }

    protected static final String NAME = "name";
    protected static final String TYPE = "type";
    protected static final String STRENGTH = "strength";

    protected static final String PERSON = "person";
    protected static final String PLACE = "place";
    protected static final String GENDER = "gender";
    protected static final String MALE = "M";
    protected static final String FEMALE = "F";


    protected GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    protected void populateDatabase() {
        new TestDataBuilder(database)
                .node().setProp(NAME, "London").setProp(TYPE, PLACE)                                //1
                .node().setProp(NAME, "Tracy").setProp(TYPE, PERSON).setProp(GENDER, FEMALE)        //2
                .relationshipTo(1, Rels.LIVES_IN)

                .node().setProp(NAME, "New York").setProp(TYPE, PLACE)                              //3
                .node().setProp(NAME, "Martin").setProp(TYPE, PERSON).setProp(GENDER, MALE)         //4
                .relationshipTo(3, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 1)

                .node().setProp(NAME, "Michael").setProp(TYPE, PERSON).setProp(GENDER, MALE)        //5
                .relationshipTo(3, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(4, Rels.FOLLOWS)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(4, Rels.FOLLOWS)

                .node().setProp(NAME, "Paul").setProp(TYPE, PERSON).setProp(GENDER, MALE)           //6
                .relationshipTo(1, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(5, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(2, Rels.FOLLOWS)
                .relationshipFrom(4, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(5, Rels.FOLLOWS)

                .node().setProp(NAME, "Peter").setProp(TYPE, PERSON).setProp(GENDER, MALE)          //7
                .relationshipTo(1, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(5, Rels.FOLLOWS)
                .relationshipTo(6, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(5, Rels.FOLLOWS)
                .relationshipFrom(6, Rels.FOLLOWS).setProp(STRENGTH, 2)

                .node().setProp(NAME, "Mary").setProp(TYPE, PERSON).setProp(GENDER, FEMALE)         //8
                .relationshipTo(1, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(5, Rels.FOLLOWS)
                .relationshipTo(6, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(7, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(4, Rels.FOLLOWS)
                .relationshipFrom(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(6, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(7, Rels.FOLLOWS)

                .node().setProp(NAME, "Mark").setProp(TYPE, PERSON).setProp(GENDER, MALE)           //9
                .relationshipTo(3, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(4, Rels.FOLLOWS)
                .relationshipTo(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(6, Rels.FOLLOWS)
                .relationshipTo(7, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(8, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(2, Rels.FOLLOWS)
                .relationshipFrom(4, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(6, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(7, Rels.FOLLOWS)
                .relationshipFrom(8, Rels.FOLLOWS).setProp(STRENGTH, 1)

                .node().setProp(NAME, "Anya").setProp(TYPE, PERSON).setProp(GENDER, FEMALE)         //10
                .relationshipTo(1, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(5, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(6, Rels.FOLLOWS)
                .relationshipTo(7, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(8, Rels.FOLLOWS)
                .relationshipTo(9, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(4, Rels.FOLLOWS)
                .relationshipFrom(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(6, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(7, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(8, Rels.FOLLOWS)
                .relationshipFrom(9, Rels.FOLLOWS).setProp(STRENGTH, 1)

                .node().setProp(NAME, "Paul").setProp(TYPE, PERSON).setProp(GENDER, MALE)           //11
                .relationshipTo(3, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(5, Rels.FOLLOWS)
                .relationshipTo(6, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(7, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(8, Rels.FOLLOWS)
                .relationshipTo(9, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(10, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(6, Rels.FOLLOWS)
                .relationshipFrom(7, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(8, Rels.FOLLOWS)
                .relationshipFrom(9, Rels.FOLLOWS)
                .relationshipFrom(10, Rels.FOLLOWS).setProp(STRENGTH, 1)

                .node().setProp(NAME, "Jane").setProp(TYPE, PERSON).setProp(GENDER, FEMALE)         //12
                .relationshipTo(1, Rels.LIVES_IN)
                .relationshipTo(2, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(5, Rels.FOLLOWS)
                .relationshipTo(6, Rels.FOLLOWS)
                .relationshipTo(7, Rels.FOLLOWS)
                .relationshipTo(8, Rels.FOLLOWS)
                .relationshipTo(9, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipTo(10, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipTo(11, Rels.FOLLOWS)
                .relationshipFrom(2, Rels.FOLLOWS)
                .relationshipFrom(4, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(5, Rels.FOLLOWS).setProp(STRENGTH, 1)
                .relationshipFrom(6, Rels.FOLLOWS)
                .relationshipFrom(7, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(8, Rels.FOLLOWS)
                .relationshipFrom(9, Rels.FOLLOWS)
                .relationshipFrom(10, Rels.FOLLOWS).setProp(STRENGTH, 2)
                .relationshipFrom(11, Rels.FOLLOWS).setProp(STRENGTH, 2);
    }
}
