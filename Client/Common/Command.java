package Common;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * The interface Common.Command.
 */
public interface Command extends Serializable {
    /**
     * Execute.
     *Method for all command to execute their possibility
     * @param par1 the par 1
     * @throws IOException the io exception
     */
    abstract public void execute(String par1, Socket clientSocket,String user) throws IOException;

    /**
     * Gets info about any command
     *
     * @return the info
     */
    abstract public String getInfo();
}

