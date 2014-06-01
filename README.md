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
* Run ``Main``

## Executables
It is also possible to generate executable jar-files. These make it easier to run different configurations.

### Deterministic belief
To run the system with a deterministic belief, create a jar-file from ``MainDeterministic``.

The following properties can be set:

* ``train``: By setting this property the system will first train a model, otherwise an existing model should be provided in the arguments
* ``parser``: This can take three different values
  * ``belief``: Use the belief during parsing (default)
  * ``beliefAction``: Use the belief during parsing and combine the *move*- and *drop*-actions to a single *move-drop*-action
  * ``none``: Don't use the belief during parsing
* ``detLexicon``: By setting this the *definite determiner*-operator won't be used, otherwise it will be used
* ``iterations``: The number of iterations during training

The following arguments should be passed along when executing:

* The resource folder
* The training dataset (only necessary if the system has to train a model)
* The test dataset
* The path to the model (when training a model, this will be where the trained model will be stored)
* The datapath folder

An example is:
``java -jar -Dparser=belief -DdetLexicon=false -Diterations=1 thesis.jar all-det/ test.ccg model evaluation_data``

### Probabilistic belief
To run the system with a probabilistic belief, create a jar-file from ``MainProbabilistic``. This is not able to train a model, but the model that is trained using the deterministic belief can also be used with a probabilistic belief.

The following arguments should be passed along when executing:

* The resource folder
* The test dataset
* The path to the model
* The datapath folder
* The folder containing the problog environment file
* The path to ``problog.py``
* The name of the environment file ([environment-prob.pl](probabilistic/environment-prob.pl))
* The name that will be used for the scene file
* The expected value of the guassian that will be used to generate probabilities for the correct facts in the belief
* The variance of the guassian that will be used to generate probabilities for the correct facts in the belief
* The threshold to add new (wrong) facts to the belief
* The expected value of the guassian that will be used to generate probabilities for the new (wrong) facts in the belief
* The variance of the guassian that will be used to generate probabilities for the new (wrong) facts in the belief

