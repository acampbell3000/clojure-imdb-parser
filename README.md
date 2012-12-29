# clojure-imdb-parser

IMDB content parser experiment using Clojure.

### License

Copyright 2012 © Anthony Campbell

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific
language governing permissions and limitations under the License.

### Usage

First download the latest release jar:

	https://github.com/acampbell3000/clojure-imdb-parser/tree/master/release

If it's easier you can clone the whole project using git:

	git clone https://github.com/acampbell3000/clojure-imdb-parser

Once complete the release jars are standalone and can be executed on the
command line as follows:

	java -jar clojure-imdb-parser-[SOME VERSION]-standalone.jar
		"Film Title" "/path/to/output-file.txt"

As this can be a little cumbersome we also have a python script
which wraps the execution of the latest jar.

The python script can be found here:

	https://github.com/acampbell3000/clojure-imdb-parser/blob/master/run.py

The python script expects the latest release jar to be in the same directory
or ./release. In addition, it currently expects the operating system to
be a unix / linux based machine:

	./run.py -q "Film Title" -o "/path/to/output-file.txt"

Finally, please look at the CHANGES.txt file to see update
descriptions since the last release tag.

### Contribute

If you wish to contribute to the project you can find the
latest source code on GitHub:

    http://wiki.github.com/acampbell3000/clojure-imdb-parser

