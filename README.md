####Object Oriented Programming Assignment
This repository contains code and information for a my third-year undergraduate project for the module **Obeject Oriented Programming**.
The module is taught to undergraduate students at [GMIT](http://www.gmit.ie) in the Department of Computer Science and Applied Physics.
The lecturer is John Healy.
###Overview
This is a multi threaded file server application.
The server is a multi-threaded that shares files through a tcp connection. 
The server offer the option to list the files in a folder and send the chosen file to the client, it also has a separeted thread responsible for logging.
The client uses an console interface and communicate to the server through a tcp connection.
###How to run
To run the server use the command $java oop.jar -cp ie.gmit.sw.WebServer [port] [path of the folder containing the files]
To run the client use the command $java oop.jar -cp ie.gmit.sw.Client

On the Client side, before requesting any files to the server is necessary to run the option 1 on the menu to make a connection

*The client requires the file Connection.xml on the same folder as the oop.jar file


The server's code is based on the code made by the GMIT's Lecturer John Healy.















"