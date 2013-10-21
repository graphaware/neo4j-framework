__author__ = 'bachmanm'

import matplotlib.pyplot as plt
import numpy as np

filename = "rel-type-vs-property-read.txt"

resultsAsArray = np.loadtxt(open(filename, "rb"), delimiter=";", dtype=str, skiprows=3)

def f(arr, value):
    return arr[np.where(arr == value)[0:1]]


def plot(toPlot, color, ls):
    xaxis = toPlot[:, 0].astype(int)
    data = toPlot[:, 3:].astype(int)
    plt.plot(xaxis, np.mean(data, 1), c=color, ls=ls, linewidth=2.0)
    plt.errorbar(xaxis, np.mean(data,1), c=color, ls=ls, yerr=(np.std(data, 1)))


plot(f(f(resultsAsArray, "nocache"), "RELATIONSHIP_TYPE"), "purple", ":")
plot(f(f(resultsAsArray, "nocache"), "PROPERTY"), "purple", "-")
plot(f(f(resultsAsArray, "lowcache"), "RELATIONSHIP_TYPE"), "green", ":")
plot(f(f(resultsAsArray, "lowcache"), "PROPERTY"), "green", "-")
plot(f(f(resultsAsArray, "highcache"), "RELATIONSHIP_TYPE"), "blue", ":")
plot(f(f(resultsAsArray, "highcache"), "PROPERTY"), "blue", "-")

plt.xlabel('Relationships per Node')
plt.ylabel('Time (microseconds)')
plt.title('Counting Relationships for 10 Nodes (2 Properties per Relationship)')
# plt.legend(("Plain Neo4j (disk)", "RelCount Module (disk)", "Plain Neo4j (low level cache)",
#             "RelCount Module (low level cache)", "Plain Neo4j (high level cache)",
#             "RelCount Module (high level cache)"), loc=0)
plt.yscale('log')
# plt.xscale('log')
plt.show()
