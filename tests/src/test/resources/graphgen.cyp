CREATE CONSTRAINT ON (person:Person) ASSERT person.neogen_id IS UNIQUE;
MERGE (n1:Person {neogen_id: '23b01990b61ae118247ea9a6b13b8280e9eb32b0' })
SET n1.name = 'Isabell McGlynn';
MERGE (n2:Person {neogen_id: '3d85518e3182a82412cdc2982a2b12b8e55d341a' })
SET n2.name = 'Kelton Kuhn';
MERGE (n3:Person {neogen_id: '1a7de07ed950b92263b1c763559d7fa65097763b' })
SET n3.name = 'Chesley Feil';
MERGE (n4:Person {neogen_id: '6059f852ea954e29e5e24493df2cb85291b13684' })
SET n4.name = 'Adrain Daugherty';
MERGE (n5:Person {neogen_id: '001ffaa40b9998c965fdcc02f91e214f0a5a3455' })
SET n5.name = 'Kyleigh Stehr';
MATCH (s1:Person {neogen_id: '23b01990b61ae118247ea9a6b13b8280e9eb32b0'}), (e1:Person { neogen_id: '001ffaa40b9998c965fdcc02f91e214f0a5a3455'})
MERGE (s1)-[edge1:KNOWS]->(e1)
;
MATCH (s2:Person {neogen_id: '23b01990b61ae118247ea9a6b13b8280e9eb32b0'}), (e2:Person { neogen_id: '1a7de07ed950b92263b1c763559d7fa65097763b'})
MERGE (s2)-[edge2:KNOWS]->(e2)
;
MATCH (s3:Person {neogen_id: '3d85518e3182a82412cdc2982a2b12b8e55d341a'}), (e3:Person { neogen_id: '23b01990b61ae118247ea9a6b13b8280e9eb32b0'})
MERGE (s3)-[edge3:KNOWS]->(e3)
;
MATCH (s4:Person {neogen_id: '3d85518e3182a82412cdc2982a2b12b8e55d341a'}), (e4:Person { neogen_id: '23b01990b61ae118247ea9a6b13b8280e9eb32b0'})
MERGE (s4)-[edge4:KNOWS]->(e4)
;
MATCH (s5:Person {neogen_id: '1a7de07ed950b92263b1c763559d7fa65097763b'}), (e5:Person { neogen_id: '001ffaa40b9998c965fdcc02f91e214f0a5a3455'})
MERGE (s5)-[edge5:KNOWS]->(e5)
;
MATCH (s6:Person {neogen_id: '6059f852ea954e29e5e24493df2cb85291b13684'}), (e6:Person { neogen_id: '001ffaa40b9998c965fdcc02f91e214f0a5a3455'})
MERGE (s6)-[edge6:KNOWS]->(e6)
;
MATCH (s7:Person {neogen_id: '6059f852ea954e29e5e24493df2cb85291b13684'}), (e7:Person { neogen_id: '3d85518e3182a82412cdc2982a2b12b8e55d341a'})
MERGE (s7)-[edge7:KNOWS]->(e7)
;
MATCH (n1:Person) REMOVE n1.neogen_id;
