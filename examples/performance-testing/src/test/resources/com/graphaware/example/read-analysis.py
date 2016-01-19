__author__ = 'bachmanm'

import matplotlib.pyplot as plt
import numpy as np

cypher_or_java = "Cypher"
neo_version = "2.1.2"
filename = "2_1/qualifying-relationships-read-cypher.txt"

resultsAsArray = np.loadtxt(open(filename, "rb"), delimiter=";", dtype=str, skiprows=3)


def f(arr, value):
    return arr[np.where(arr == value)[0:1]]


propMeans = np.mean(f(resultsAsArray, "PROPERTY")[:, 2:].astype(int), 1)
typeMeans = np.mean(f(resultsAsArray, "RELATIONSHIP_TYPE")[:, 2:].astype(int), 1)

propStd = np.std(f(resultsAsArray, "PROPERTY")[:, 2:].astype(int), 1)
typeStd = np.std(f(resultsAsArray, "RELATIONSHIP_TYPE")[:, 2:].astype(int), 1)

fig = plt.figure()
ax = fig.add_subplot(111)

ind = np.arange(3)
width = 0.35

rects1 = ax.bar(ind, propMeans, width,
                color='#902c8e',
                yerr=propStd,
                error_kw=dict(elinewidth=2, ecolor='#2377ba'))

rects2 = ax.bar(ind + width, typeMeans, width,
                color='#2377ba',
                yerr=typeStd,
                error_kw=dict(elinewidth=2, ecolor='#902c8e'))

# axes and labels
ax.set_xlim(-width, len(ind) + width)
ax.set_ylim(0)
ax.set_ylabel('Time (microseconds)')
ax.set_title(
    'Traversing half of node\'s (cca 100) relationships 100x (' + cypher_or_java + ', Neo4j ' + neo_version + ')')
xTickMarks = ['No Cache', 'Low Level Cache', 'High Level Cache']
ax.set_xticks(ind + width)
xtickNames = ax.set_xticklabels(xTickMarks)
plt.setp(xtickNames, rotation=0, fontsize=10)

## add a legend
ax.legend((rects1[0], rects2[0]), ('1 Relationship Type + Properties', 'Different Relationship Types'))
# plt.yscale('log')

def autolabel(rects):
    # attach some text labels
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width() / 2., height + 500, '%d' % int(height),
                ha='center', va='bottom')


autolabel(rects1)
autolabel(rects2)

plt.show()
