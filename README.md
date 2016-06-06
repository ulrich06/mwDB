Many-World Graph Project
========================

## Build status: <img src="https://travis-ci.org/kevoree-modeling/mwDB.svg?branch=master" />

Short presentation
==================

What-if oriented graph database for prescriptive analytics....

Design Principles and Features
------------------------------
As a main design principle KMF was from the beginning designed with strict memory usage, runtime performance, and thread safety requirements in mind.
KMF takes efficiency seriously.
This includes implementing custom versions of internal core data structures, like hash maps and red-black trees backed by primitive arrays, to improve performance and memory usage.
To cope at the same time with the large-scale, distributed, and constantly changing nature of modern applications the design of KMF combines ideas from reactive programming, peer-to-peer distribution, big data management, and machine learning.
The distributed aspect of many modern applications lead to the decision to design a completely asynchronous core for KMF.
Models are defined as observable streams of chunks that are exchanged between nodes in a peer-to-peer manner.
A lazy loading strategy allows to transparently access the complete virtual model from every node, although chunks are actually distributed across nodes.
Observers and automatic reloading of chunks enable a reactive programming style.

Features:

* native support for temporal data and reasoning
* asynchronous method calls
* support for Java and JavaScript
* code generation based on a meta model definition **and** dynamically instantiated meta models
* easy-to-use API to traverse models
* native mechanisms for distribution
* native versioning of models on a per-object basis
* ...

Changelog
=========

## Versions:

The strategy to version mwg is lead by the version of the API.
It means that the API version will follow an incremental number (*e.g* like 1 then 2 then 3...) that define the compatibility with user code.
According to that, all compatible core runtime and plugins will have a version prefixed by this number, such as 1.1.
In short the version schema of mwg bundle will have the form <API_VERSION>.<BUGFIX_VERSION>.

### 1.x

Initial API version (issued from the fork of KMF 4.27 ): (planned for beginning of June)

Modules
=============

* Core **(https://github.com/kevoree-modeling/mwDB/tree/master/core)**
* CoreJS **(https://github.com/kevoree-modeling/mwDB/tree/master/js)**
* Plugins **(https://github.com/kevoree-modeling/mwDB/tree/master/plugins)**


Getting started
==============

Coming soon...

Publications:
-------------
Within the scope of the mwdb project several research papers have been published:

**Foundation papers:**

* Francois Fouquet, Gregory Nain, Brice Morin, Erwan Daubert, Olivier Barais, Noel Plouzeau, and Jean-Marc Jézéquel. **An Eclipse Modelling Framework Alternative to Meet the Models@Runtime Requirements**. In ASM/IEEE 15th Model Driven Engineering Languages ​​and Systems (MODELS'12), 2012. [Get the paper here](https://hal.inria.fr/hal-00714558/document)
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, Brice Morin, and Yves Le Traon. **Reasoning at runtime using time-distorted contexts: A models@run.time based approach**. In 26th International Conference on Software Engineering and Knowledge Engineering (SEKE'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/17637)
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Brice Morin, Jacques Klein, Olivier Barais, and Yves Le Traon. **A Native Versioning Concept to Support Historized Models at Runtime**. In ASM/IEEE 17th International Conference on Model Driven Engineering Languages ​​and Systems (MODELS'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/18688)
* Assaad Moawad, Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, and Johann Bourcier. **A Model-Driven Approach for Simpler, Safer, and Evolutive Multi-Objective Optimization Development**. In 3rd International Conference on Model-Driven Engineering and Software Development (MODELSWARD'15), 2015. [Get the paper here](http://orbilu.uni.lu/handle/10993/20392)
* Thomas Hartmdann, Assaad Moawad, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Stream my Models: Reactive Peer-to-Peer Distributed Models@run.time**. [Get the paper here](http://orbilu.uni.lu/handle/10993/22329).
* Assaad Moawad, Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Beyond Discrete Modeling: Continuous and Efficient Models@Run.time for IoT**. [Get the paper here](http://orbilu.uni.lu/handle/10993/22330).

**Application papers:**

* Thomas Hartmann, Francois Fouquet, Jacques Klein, Gregory Nain, and Yves Le Traon. **Reactive Security for Smart Grids Using Models@run.time-Based Simulation and Reasoning**. In Second Open EIT ICT Labs Workshop on Smart Grid Security (SmartGridSec'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/16762)
* Thomas Hartmann, Francois Fouquet, Jacques Klein, and Yves Le Traon, Alexander Pelov, Laurent Toutain, and Tanguy Ropitault. **Generating Realistic Smart Grid Communication Topologies Based on Real-Data**. In IEEE 5th International Conference on Smart Grid Communications (SmartGridComm'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/19009)
* Thomas Hartmann, Assaad Moawad, Francois Fouquet, Yves Reckinger, Tejeddine Mouelhi, Jacques Klein, and Yves Le Traon. **Suspicious Electric Consumption Detection Based on Multi-Profiling Using Live Machine Learning**. In IEEE 6th International Conference on Smart Grid Communications (SmartGridComm'15), 2015. [Get the paper here](http://orbilu.uni.lu/handle/10993/22781).
* Thomas Hartmann, Assaad Moawad, Francois Fouquet, Yves Reckinger, Jacques Klein, Yves Le Traon. **Near Real-Time Electric Load Approximation in Low Voltage Cables of Smart Grids with Models@run.time**. (will appear soon)

