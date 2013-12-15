__author__ = 'bachmanm'

import matplotlib.pyplot as plt
import numpy as np

filename = "createThousandRelationships-fast-pc.txt"
noParams = 3

resultsAsArray = np.loadtxt(open(filename, "rb"), delimiter=";", dtype=str, skiprows=3)

means = np.mean(resultsAsArray[:, noParams:].astype(int), 1)
stddevs = np.std(resultsAsArray[:, noParams:].astype(int), 1)


def f(arr, value):
    return arr[np.where(arr == value)[0:1]][:, 1:]


def plot(toPlot, color):
    plt.plot(toPlot[:, 0], np.mean(toPlot[:, 1:], 1), c=color, linewidth=2.0)
    # plt.errorbar(toPlot[:,0], np.mean(toPlot[:,1:],1), yerr=(np.std(toPlot[:, 1:], 1)))

# props = "NO_PROPS"
# props = "TWO_PROPS_NO_COMPACT"
props = "TWO_PROPS_COMPACT"
# props = "FOUR_PROPS"
plot(f(f(resultsAsArray, props), "NO_FRAMEWORK").astype(int), "purple")
# plot(f(f(resultsAsArray, props), "EMPTY_FRAMEWORK").astype(int), "green")
# plot(f(f(resultsAsArray, props), "RELCOUNT_NO_PROPS_SINGLE_PROP_STORAGE").astype(int), "red")
# plot(f(f(resultsAsArray, props), "RELCOUNT_NO_PROPS_MULTI_PROP_STORAGE").astype(int), "pink")
plot(f(f(resultsAsArray, props), "FULL_RELCOUNT_SINGLE_PROP_STORAGE").astype(int), "blue")
# plot(f(f(resultsAsArray, props), "FULL_RELCOUNT_MULTI_PROP_STORAGE").astype(int), "black")

plt.xlabel('Number of Relationships per Transaction')
plt.ylabel('Time (microseconds)')
plt.title('Create 1,000 Relationships, 2 Properties per Relationship')
# plt.legend(("NO_FRAMEWORK", "EMPTY_FRAMEWORK", "RELCOUNT_NO_PROPS_SINGLE_PROP_STORAGE",
#             "RELCOUNT_NO_PROPS_MULTI_PROP_STORAGE", "FULL_RELCOUNT_SINGLE_PROP_STORAGE",
#             "FULL_RELCOUNT_MULTI_PROP_STORAGE"), loc=3)
plt.legend(("Plain Neo4j", "RelCount Module"), loc=3)
plt.yscale('log')
plt.xscale('log')
plt.show()
