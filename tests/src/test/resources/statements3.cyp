MATCH (c:Company {name:'GraphAware'})
MERGE (d:Person {name:'Daniela'})-[:WORKS_FOR]->(c);