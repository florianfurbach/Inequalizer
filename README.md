# Inequalizer



Requirements
======
* [Maven](https://maven.apache.org/)

Installation
======
Set the path and shared libraries variables (replace the latter by DYLD_LIBRARY_PATH in **MacOS**)
```
export PATH=<Inequalizer's root>/:$PATH
export LD_LIBRARY_PATH=<Inequalizer's root>/lib/:$LD_LIBRARY_PATH
```

To build the tool run
```
mvn install:install-file -Dfile=lib/z3-4.3.2.jar -DgroupId=com.microsoft -DartifactId="z3" -Dversion=4.3.2 -Dpackaging=jar
mvn clean install
```

Binaries
======
The precompiled jars can be found in the [release](https://github.com/florianfurbach/Inequalizer/releases) section.

Usage
======

Inequalizer supports petri Nets written in the .spec format.

You can start the tool by running
```
java -jar target/Inequalizer-0.5-jar-with-dependencies.jar -f <input Petri net file> [options]
```

Other optional arguments include:
- -m,--min           Set if minization should be used.
- -v,--verbose       Set if more output is required.
- -b,--bound <max>   Set a bound on the absolute values of entries in k
- -c,--cover         Set if the input is a coverability check.

Authors and Contact
======
**Maintainer:**

* [Florian Furbach](mailto:f.furbach@tu-braunschweig.de)

Please feel free to contact me in case of questions or to send feedback.
