package customstringserialsend;

import com.integpg.comm.AUXSerialPort;
import com.integpg.comm.PortInUseException;
import com.integpg.system.IoEvent;
import com.integpg.system.Iolog;
import com.integpg.system.JANOS;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomStringSerialSend {

    //Iolog we pull our IoEvents from.
    private final Iolog _iolog = new Iolog();
    //The Aux Port object we open and send the serial commands to.
    AUXSerialPort serialPort = new AUXSerialPort();
    //The time we give the Iolog to refresh to. 
    //This will make the Iolog only show IoEvents AFTER the time we give it.
    long refreshTime = 0;
    //The String we create to send out the Aux Port.
    String lastKnownStates;


    /** 
     * Constructor for the CustomStringSerialSend. When the object is created, 
     * the serial port gets opened. 
     */
    public CustomStringSerialSend() {

        try {
            serialPort.open();
        } catch (PortInUseException | IOException ex) {
            Logger.getLogger(CustomStringSerialSend.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    /** 
    * This function takes the String created from the buildString Function and 
    * sends it out the AUX port.
    */
    private void sendInputStateString() {

        try {
            serialPort.getOutputStream().write(lastKnownStates.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(CustomStringSerialSend.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /** Takes Input States of IoEvents and converts them into a serial string as
    * a byte value representing if inputs were on or off.If no IoEvents, creates
    * serial string using current Input State of the JNIOR.
    */
    private void buildInputStateString(int InputStates) {

        StringBuilder stringer = new StringBuilder();
        //Loops through all 12 bit values of the Input States.
        for (int i = 11; i >= 0; i--) {
            //Take bit and shift it to the end of the byte, then do AND operation on it.
            int state = (InputStates >> i) & 1;
            //Take bit and add it to string.
            stringer.append(String.valueOf(state));
            //Every 4 bits we add a space.
            if (i % 4 == 0) {
                stringer.append(" ");
            }
        }
        //Add termination string
        stringer.append("\r\n");
        //lastKnownStates is the String that will be sent out the Aux Port. 
        //It gets set to what the stringBuilder created.
        lastKnownStates = stringer.toString();
        System.out.println(stringer);
        //Call sendInputStateString Function.
        sendInputStateString();
        //Reset stringBuilder so its doesn't keep the string it created.
        stringer.setLength(0);
    }


    /** This function checks IoEvents from the Iolog, and sends the byte value 
    * of the IoEvent's Input States. The Iolog gets refreshed to the last 
    * IoEvents timestamp. If no IoEvents are returned, send the byte value of 
    * the current Input States on the JNIOR.
    */
    private void getInputEvents() {

        //refreshTime is the timestamp of the last IoEvent pulled from the Iolog.
        _iolog.refresh(refreshTime);
        //IoEvent Array that gets populated with IoEvents from the Iolog.
        IoEvent[] ioEvents = _iolog.getInputEvents();
        //If IoEvents present, build serial string for each IoEvent Input States.
        if (ioEvents.length > 0) {

            refreshTime = ioEvents[0].timestamp;
            //Loop through the IoEvents pulled from the Iolog.
            for (int index = ioEvents.length - 1; index >= 0; index--) {

                IoEvent currentIoEvent = ioEvents[index];
                buildInputStateString(currentIoEvent.states);
            }

        } else {

            //If no IoEvents, build serial string using current Input States on the JNIOR.
            buildInputStateString(JANOS.getInputStates());
        }
    }



    public static void main(String[] args) {
        //Creates the CustomStringSerialSend object.
        //This is needed to call the buildString Function.
        CustomStringSerialSend Testing = new CustomStringSerialSend();
        //Loop will never end until application closes.
        while (true) {
            //Uses CustomStringSerialSend object we created to call the createCommand Function.
            Testing.getInputEvents();
            //Every time the loop iterates, it waits 1 second.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {

            }
        }

    }

}
