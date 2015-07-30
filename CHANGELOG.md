#CHANGE LOG

##Version 0.8

+ General - project renamed to CloudSlang
+ Tools
	+ Build Tool - The verifier has been replaced by the build tool, which in addition to checking syntactic validity of a project's files runs the projects associated tests.
		+ Tests - Syntax for writing content tests. 
+ DSL Additions
	+ Async Loops - A task can contain a loop which performs an operation for each value in a list in parallel. 
+ DSL Improvements
	+ Imports - Flows no longer need to explicitly import files in the same namespace.
+ CLI Enhancements
	+  Default Classpath - Files in the content folder are automatically placed in the classpath.
	+  Improved Error Messages - Many error messages are more user-friendly. 
	+  Quiet Mode - Run flows without printing the task names to the screen.
+ Content Additions - Content has been added in the following areas.
	+ Docker Swarm
	+ Docker Containers
	+ Git
	+ CoreOS
	+ Jenkins
	+ Base - Linux, zip and ping
+ Content Tests - Tests added for most of the current content.
+ Documentation
	+ In depth tutorial which teaches many language features 	

##Version 0.7

+ Tools
	+ Verifier - Verifies CloudSlang files are syntactically correct.
+ DSL Additions
	+ Loops - A task can contain a for loop to iteratively call an operation or subflow
	+ System Properties - Inputs can declare a `system_property` property to receive values from a system properties file. 
+ DSL Improvements
	+ Outputs - Support all serializable types, not just strings.
	+ Python - Support 3rd party Python libraries.
+ DSL Syntax changes
	+ Operations - Aligned to flow structure with one operation per file and `name` property 
	+ Tasks - Changed from maps to list of tasks.
	+ Overridable - Former `override` input property changed to `overridable` with opposite meaning. 
+ CLI Enhancements
	+ Error messages - Error messages are more clear with helpful guidance on how to fix common errors
	+ Inputs from file - Inputs to CLI can be read from a file instead of, or in addition to, being entered manually.
+ Content Additions - Content has been added in the following areas.
	+ CloudSlang
		+ Base
		+ Utils
		+ cAdvisor
		+ REST
		+ Jenkins
		+ Docker
	+ Java @Actions
		+ JSON
+ Organization
	+ CloudSlang content moved to its own repository.
+ Documentation
	+ Restructured and updated DSL Reference.
	+ Added developer content including API references and architecture explanations.


