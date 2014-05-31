# Thesis
This is the code that accompanied my thesis "Converse with a robot: Improving semantic parsing using a robot’s belief".

## Dependencies
* [Stanford Core-NLP](http://nlp.stanford.edu/software/corenlp.shtml)
* [SemEval-2014 Task 6 Java API](http://alt.qcri.org/semeval2014/task6/index.php?id=data-and-tools)
* [The University of Washington Semantic Parsing Framework](https://bitbucket.org/yoavartzi/spf)
* [Apache Commons Math](http://commons.apache.org/proper/commons-math/) (Necessary when using the probabilistic belief)
* [ProbLog](http://dtai.cs.kuleuven.be/problog/) (Necessary when using the probabilistic belief)
* [SWI Prolog](http://www.swi-prolog.org) (Optional)

## Usage
(The instructions below are when using Eclipse)

* Checkout repository
* Follow instructions on https://bitbucket.org/yoavartzi/spf to add spf to Eclipse
* Import the project in Eclipse
* Configure the build path
  * Add all the projects from spf to this project
  * Add *train-robots-nlp.jar* and *train-robots-core.jar* from the SemEval-2014 Task 6 Java API
  * Add *stanford-corenlp-3.3.1.jar*, *stanford-corenlp-3.3.1-models.jar* and *ejml-0.23.jar* from Stanford Core-NLP
  * Add *commons-math3-3.2.jar* from Apache Commons Math
* Run *Main.java*