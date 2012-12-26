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
import getopt, sys, re, subprocess, exceptions


# Help
_help = """
    __file__ [options]

    Simple wrapper script for the Clojure IMDB parser.

    Options:
        -q --query Option which specifies the query text.

        -o --output [path/to/file] If specified, writes the search result to the a file.

        -h -? --help Option to display this text.

    Examples:
        __file__ -q "Clash of the Titans" -o output.txt
        __file__ -query "Clash of the Titans" -output output.txt
"""
_help = _help.replace("__file__", __file__)


# Main method
def main():
    # Initialise variables
    output = ""
    query_term = ""
    output_file = ""
    latest_jar = ""

    print "\n    Clojure IMDB Parser\n"

    try:
        opts, args = getopt.getopt(sys.argv[1:], "q:o:?h", ["query=", "output=", "help"])

    except getopt.GetoptError as error:
        # Print help information and exit:
        print "    " + str(error)
        print _help
        sys.exit(2)

    for option, argument in opts:
        if option in ("-q", "--query"):
            query_term = str(argument)

        elif option in ("-o", "--output"):
            output_file = str(argument)

        elif option in ("-h", "--help"):
            print _help
            sys.exit(0)

    try:
        # Determine newest parser
        process = subprocess.Popen(["ls -t ./release | grep \"clojure-imdb-parser.*.jar\" | head -n 1"],
            stdout=subprocess.PIPE, shell=True)
        latest_jar, stderr = process.communicate()
        process.wait()

    except exceptions.Exception as error:
        print "Unable to find latest clojure-imdb-parser.jar:\n"
        print "    " + str(error)
        sys.exit(1)

    # Validate
    if latest_jar != None and str(latest_jar) != "":
        latest_jar = "./release/" + str(latest_jar)

        # Clean up path
        pattern = re.compile(r'\n')
        latest_jar = pattern.sub(" ", latest_jar).strip()

        print "Latest clojure-imdb-parser.jar:\n"
        print "    " + latest_jar + "\n"

        try:
            # Execute the parser
            process = subprocess.Popen(["java", "-jar", latest_jar, query_term, output_file],
                stdout=subprocess.PIPE)
            output, stderr = process.communicate()
            process.wait()

        except exceptions.Exception as error:
            print "Unable to execute clojure-imdb-parser!\n"
            print "    " + str(error)
            sys.exit(1)

    else:
        print "Unable to find latest clojure-imdb-parser.jar!\n"
        sys.exit(1)


    # Where we at?
    print output

# If we're being run directly
if __name__ == "__main__":
    main()
