# SerialInputStateReporter
This application monitors IoEvents from the Iolog and reports their Input States by sending a String out the AUX Port, reporting each Inputs State.

It starts by delclaring 4 Global variables: 

Iolog iolog = new Iolog:                        Creates an instance of the Iolog, where we can grab IoEvents that occurred on the JNIOR.
AUXSerialPort serialPort = new AUXSerialPort:   Creates an instance of the AUX Port object that we can use to send serial commands out the AUX Port.
Long refreshTime = 0:                           This Long gets set to the most recent IoEvent's timestamp pulled from the Iolog. This is used in iolog.refresh(Long)
                                                to pull only recent IoEvents, else we'd be checking every IoEvent from the Iolog everytime.
String lastKnownStates:                         This string is populated with what we build with the stringBuilder variable. This is what gets sent out the AUX Port.

This application first uses a constructor that will call the other functions used in this application. This constructor also is used to open the AUX Port for sending out data.

The first function called in the application is getInputEvents(). 
This function checks the Iolog for IoEvents, and if they are there goes through each one, takes its Input State, and calls the buildInputStateString for each IoEvents Input State. 
If there are no IoEvents pulled from the Iolog, then the JNIOR's current Input State is grabbed instead and is used in the buildInputStateString(Int) function.

The buildInputStateString(Int) function goes through the Input State value its given and loops through the value by its bits, 
shifting each one down to the end and doing an AND operation against each bit to determine for each Input State if the Input is High or Low. 
Every 4th bit is spaced out. These values are appended by a stringBuilder, which after looping through the Input State given to the function, 
generates a String command relaying each Input States value. This is set to the global variable lastKnownStates, and then is used in the sendInputStateString function.

The sendInputStateString() function uses the global variable lastKnownStates String by sending the String out the AUX Port.

Finally, the main function declares a constructor of this applications class, uses it to call the getInputEvents function inside of a while loop. 
This loop then checks every second for all of the IoEvents that occured and uses their Input State for the other functions, 
sending their Input States as Strings out of the AUX Port.

Example String:
If an IoEvent reported input 1 and 7 were high, the string generated and then send out of the AUX Port would be "0000 0100 0001".
