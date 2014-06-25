Neorank test in Python
======================

This is the most basic test comparing the classic 
pagerank with its variant using a discrete random
walker on the network.

Please make sure networkx and pylab are installed 
on your system to play around with the parameter 
set.

To install, please use PIP (can be installed using 
homebrew on Mac or apt-get (Ubuntu) /yum (Fedora) 
or similar package manager on Linux)


## Notes on Running the Script

The script is written for **Python 2** and is totally incompatible with Python 3.  As stated above, you need to have networkx and pylab installed.  If pip does the business for you then that's great, but, if not, then the following might be of use.

```
sudo zypper install python-numpy
sudo zypper install python-scipy python-matplotlib
sudo zypper install python-matplotlib-tk
```

Of course, `zypper` is for OpenSuSE so choose the package manager that is appropriate for your distribution.