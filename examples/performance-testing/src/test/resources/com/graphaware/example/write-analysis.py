__author__ = 'bachmanm'

import matplotlib.pyplot as plt
import numpy as np

neo_version = "2.1.2"
filename = "2_1/qualifying-relationships-write.txt"
noParams = 1

resultsAsArray = np.loadtxt(open(filename, "rb"), delimiter=";", dtype=str, skiprows=3)

means = np.mean(resultsAsArray[:, noParams:].astype(int), 1)
stddevs = np.std(resultsAsArray[:, noParams:].astype(int), 1)


def f(arr, value):
    return arr[np.where(arr == value)[0:1]][:, 1:]


def plot(toPlot, color):
    # plt.plot(toPlot[:, 0], np.mean(toPlot[:, 1:], 1), c=color, linewidth=2.0)
    plt.errorbar(toPlot[:, 0], np.mean(toPlot[:, 1:], 1), c=color, linewidth=2.0, yerr=(np.std(toPlot[:, 1:], 1)))


plot(f(resultsAsArray, "PROPERTY").astype(int), "#902c8e")
plot(f(resultsAsArray, "RELATIONSHIP_TYPE").astype(int), "#2377ba")

plt.xlabel('Number of Relationships per Transaction')
plt.ylabel('Time (microseconds)')
plt.title('Create 1,000 Relationships (Java API, Neo4j ' + neo_version + ')')
plt.legend(("1 Relationship Type + Properties", "Different Relationship Types"), loc=3)
plt.yscale('log')
plt.xscale('log')
plt.show()
