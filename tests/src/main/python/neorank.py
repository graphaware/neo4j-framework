# Simple test of NeoRank Algorithm implemented in
# Neo4j graph database 
import operator 
import networkx as nx
import random as rnd
import pylab

# Allow for adaptive damping? 
# TODO: prepare a rigorous test of the algorithm

# Picks an object from list at random             
# non-universal damping in pagerank?
def pickRandom(lst):                              
  return rnd.choice(lst);                      

N = 100
hyperjump = 0.85
ba = nx.barabasi_albert_graph(N, 2) # nx.erdos_renyi_graph(N, 0.8)# nx.fast_gnp_random_graph(N, p)

steps = 1000
nodes = ba.nodes() # List of nodes in the graph

# Init neoranks for testing purposes
for index in ba.nodes_iter():
    node = ba.node[index]
    node['neorank'] = 0

current = pickRandom(nodes);
normalization = 0;


# Performs a step of a random walker on the graph  
def step():                                        
  global current, normalization

  if rnd.random() < hyperjump:
     current = pickRandom(ba.neighbors(current))
  else: 
     current = pickRandom(ba.nodes())

  # Increment the rank of the visited vertex
  node = ba.node[current]
  if 'neorank' in node:
    node['neorank'] += 1
  else :
    node['neorank'] = 0;

  # Increment normalization factor to present
  # the data consistently
  normalization += 1;
  
for _ in range(steps): 
  step()


pagerank = list(reversed(sorted(nx.pagerank(ba, alpha = 1.0).iteritems(), key = operator.itemgetter(1))))

print "highest pagerank: " + str( pagerank[0:3])
# Perform analysis of NeoRank distribution
neorank = list(reversed(sorted(ba.nodes(data = True), key = lambda (a, dct): dct['neorank'])))

print "highest neoranks: " + str( neorank[0:3])

labels = dict((n,str(n) + ", " + str(d['neorank'])) for n,d in ba.nodes(data = True))
layout = nx.spring_layout(ba)

# TODO: change node size according to its neoRank
nx.draw(ba, layout, labels = labels, node_size = 1000)

pylab.draw()
pylab.show()                        

