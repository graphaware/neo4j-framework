__author__ = 'bachmanm'

import matplotlib.pyplot as plt
import numpy as np

filename = "rel-type-vs-property-write.txt"
noParams = 1

resultsAsArray = np.loadtxt(open(filename, "rb"), delimiter=";", dtype=str, skiprows=3)

means = np.mean(resultsAsArray[:, noParams:].astype(int), 1)
stddevs = np.std(resultsAsArray[:, noParams:].astype(int), 1)


def f(arr, value):
    return arr[np.where(arr == value)[0:1]][:, 1:]


def plot(toPlot, color):
    plt.plot(toPlot[:, 0], np.mean(toPlot[:, 1:], 1), c=color, linewidth=2.0)
    # plt.errorbar(toPlot[:,0], np.mean(toPlot[:,1:],1), yerr=(np.std(toPlot[:, 1:], 1)))

plot(f(resultsAsArray, "RELATIONSHIP_TYPE").astype(int), "purple")
plot(f(resultsAsArray, "PROPERTY").astype(int), "blue")

plt.xlabel('Number of Relationships per Transaction')
plt.ylabel('Time (microseconds)')
plt.title('Create 1,000 Relationships')
plt.legend(("Relationship Types", "Properties"), loc=3)
plt.yscale('log')
plt.xscale('log')
plt.show()
