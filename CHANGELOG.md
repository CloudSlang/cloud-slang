#CHANGE LOG

##Version 0.7

+ Tools
	+ Verifier - Verifies SLANG files are syntactically correct.
+ DSL additions
	+ Loops - A task can contain a for loop to iteratively call an operation or subflow
	+ System Properties - Inputs can declare a `system_property` property to receive values from a system properties file. 
+ DSL improvements
	+ Outputs - Support all serializable types, not just strings.
	+ Python - Support 3rd party Python libraries.
+ DSL syntax changes
	+ Operations - Aligned to flow structure with one operation per file and `name` property 
	+ Tasks - Changed from maps to list of tasks.
	+ Overridable - Former `override` input property changed to `overridable` with opposite meaning. 
+ CLI enhancements
	+ Error messages - Error messages are more clear with helpful guidance on how to fix common errors
	+ Inputs from file - Inputs to CLI can be read from a file instead of, or in addition to, being entered manually.
+ Content additions - Content has been added in the following areas.
	+ Slang
		+ Base
		+ Utils
		+ cAdvisor
		+ REST
		+ Jenkins
		+ Docker
	+ Java @Actions
		+ JSON
+ Organization
	+ Slang content moved to its own repository.
+ Documentation
	+ Restructured and updated DSL Reference.
	+ Added developer content including API references and architecture explanations.


