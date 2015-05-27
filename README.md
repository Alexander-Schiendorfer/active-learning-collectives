# SASO-Submission 29: "Active Learning for Efficient Sampling of Control Models of Collectives"
Source code for the experiments presented in a SASO paper

The evaluation, analysis and plots for the measured data can be found 
at https://github.com/ChrislS/active_learning_for_abstract_models_of_collectives .


This readme file explains all necessary steps to execute
experiments presented in the SASO paper.
Please note that the experiments were actually conducted 
on a cluster with workers having 4 cores and 32 GB RAM each
so the runtime will greatly vary depending on your implementation.

However, with our efforts we hope to make it easier to validate and 
communicate our approach. 

=================================
The active learning algorithm in python itself can be found
in "CSP Model Abstraction/python/activeLearner" and may be executed
by the script "testActiveLearner.py". Integration with the remaining 
experiment suite is subject to step 6.

==================================
= CAVEAT CPLEX =

As our approach highly depends on 
a working IBM ILOG CPLEX installation
you need a working version installed (tested with CPLEX 12.4)

Surely, you can nevertheless look at
all publicly availablle source code. 

-> if interested, go to http://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/
   for a trial version and/or academic/student licenses
   
   Install and include relevant CPLEX directories to Path Variable when asked
==================================

1. Download and install eclipse from http://www.eclipse.org/downloads/
   (we used eclipse for Java developers 2.0, eclipse 4.3)
2. Get the source code from github
3. Import all projects into eclipse
4. Configuring the projects to work with CPLEX
   Select the project "CPLEX Model Abstraction Experiment Suite"
   a) Linux (tested with Ubuntu 14.04): 
      Open "launchers/ExperimentRunner.launch" with your favourite text editor
      Replace
      <mapEntry key="LD_LIBRARY_PATH" value="/home/alexander/Programs/CPLEX/opl/bin/x86-64_sles10_4.1"/>
      with your appropriate CPLEX path (should contain "libcplex124.so" etc.)      
   b) Windows (tested with Windows 7/8):
      Make sure that the environment variables CPLEX_STUDIO_BINARIES124 and CPLEX_STUDIO_DIR124 are set properly
   Run "launchers/ExperimentRunner Win.launch" or "launchers/ExperimentRunner.launch" using eclipse
5. If you see output containing "Writing /tmp/samplingAbstractionDataXYZ.dat" everything is working
6. Make sure to install pyro4, e.g., by "pip install pyro4"
	a) Open a terminal and run "pyro4-ns" to start the name service
	b) Open another terminal and run "CSP Model Abstraction/python/activeLearner/connectActiveLearner.py" to instantiate an AL remote object
7. Examine "SamplingAbstraction-Experiments/launchers" for different run configurations
	a) ParameterSweeper.launch: Starts runs based on a properties file in the "experiments/" subfolder and varying parameters and values
	b) ScalabilitySweep.launch: Is used to generate the results for centralized solutions
	
Please contact the authors in case of troubles installing the examples.

Alexander Schiendorfer,
Augsburg University
----------------------------------

http://www.informatik.uni-augsburg.de/lehrstuehle/swt/se/staff/aschiendorfer/
