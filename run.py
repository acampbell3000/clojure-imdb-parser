#!/usr/bin/python
#
# Copyright 2012 Anthony Campbell (anthonycampbell.co.uk)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Required imports
import os, getopt, sys, re, subprocess, exceptions

# Constants
_default_output_file = "./output.txt"
_script_directory = os.path.dirname(os.path.realpath(__file__))

# Help
_help = """
    Clojure IMDB Parser

    __file__ [options]

    Simple wrapper script for the Clojure IMDB parser.

    Options:
        -q --query Option which specifies the query text.

        -f --file Option which writes the search result to the defailt output file: __default_output_file__

        -o --output [path/to/file] If specified, writes the search result to the a file.

        -v --verbose Option to enable verbose output.

        -h -? --help Option to display this text.

    Examples:
        __file__ -q "Clash of the Titans" -o output.txt
        __file__ --query "Clash of the Titans" --output output.txt
"""
_help = _help.replace("__file__", __file__)
_help = _help.replace("__default_output_file__", _default_output_file)

# Main method
def main():
    # Initialise variables
    verbose = False
    output = ""
    query_term = ""
    output_file = ""
    latest_jar = ""

    try:
        opts, args = getopt.getopt(sys.argv[1:], "q:fo:?hv", ["query=", "file", "output=", "help", "verbose"])

    except getopt.GetoptError as error:
        # Print help information and exit:
        print "\n    " + str(error)
        print _help
        sys.exit(2)

    for option, argument in opts:
        if option in ("-q", "--query"):
            query_term = str(argument)

        elif option in ("-f", "--file"):
            output_file = _default_output_file

        elif option in ("-o", "--output"):
            output_file = str(argument)

        elif option in ("-v", "--verbose"):
            verbose = True

        elif option in ("-h", "--help"):
            print _help
            sys.exit(0)
            
    # Check we're good to go
    if query_term == None or query_term == "":
        print _help
        sys.exit(2)   

    if verbose:
        print "\n    Clojure IMDB Parser"     

    try:
        # Determine newest parser
        process = subprocess.Popen(["ls -r " + _script_directory + "/release | grep \"clojure-imdb-parser.*.jar\" | head -n 1"],
            stdout=subprocess.PIPE, shell=True)
        latest_jar, stderr = process.communicate()
        process.wait()

    except exceptions.Exception as error:
        print "\n    Unable to find latest clojure-imdb-parser.jar:"
        print "\n    " + str(error)
        sys.exit(1)

    if latest_jar != None and str(latest_jar) != "":
        latest_jar = _script_directory + "/release/" + str(latest_jar)

        # Clean up path
        pattern = re.compile(r'\n')
        latest_jar = pattern.sub(" ", latest_jar).strip()

        if verbose:
            print "\n    Latest clojure-imdb-parser.jar:"
            print "\n    " + latest_jar + "\n"

        try:
            # Execute the parser
            process = subprocess.Popen(["java", "-jar", latest_jar, query_term, output_file, str(verbose)],
                stdout=subprocess.PIPE)
            output, stderr = process.communicate()
            process.wait()

        except exceptions.Exception as error:
            print "\n    Unable to execute clojure-imdb-parser.jar!"
            print "\n    " + str(error)
            sys.exit(1)

    else:
        print "\n    Unable to find latest clojure-imdb-parser.jar!"
        sys.exit(1)


    # Where we at?
    print output


# If we're being run directly
if __name__ == "__main__":
    main()
