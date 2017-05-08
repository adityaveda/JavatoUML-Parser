# JavatoUML-Parser
A parser which converts Java source code into a UML diagram. 

A project to take input source code and generates UML diagram. Class Diagram is generated.

Tools and Libraries Used:

1.Eclipse JEE MARS: The choice of Java IDE used for the development of this project.

2.Java parser: Java parser is used to parse source code. Then the desired notation is generated which is
given to yUML as input code. The library javaparser from github has been used for generating suitable
grammar.

url: https://github.com/javaparser/javaparser

3. UML generator: The free tool yUML used for the getting the UML class diagram.
url : http://yuml.me/diagram/plain/class/

4. To generate class diagram:

For example:

java -jar u1.jar “G:/test-case-1” output.png

where "output" is name of file and "u1" is the name of the packaged jar.
