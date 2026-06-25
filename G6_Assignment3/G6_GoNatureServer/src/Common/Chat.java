package Common;

import java.io.Serializable;

/**
 * Represents a message exchanged between the client and the server.
 * Each message contains a command that identifies the requested
 * operation and an optional data object associated with that command.
 * This class is serializable so it can be transmitted through
 * the network using Java object streams.
 */
@SuppressWarnings("serial")
public class Chat implements Serializable {

    /**
     * The command or message type sent between the client and server.
     */
    private String type;

    /**
     * The data associated with the command.
     * The object type depends on the requested operation.
     */
    private Object data;

    /**
     * Creates a new chat message.
     *
     * @param type the command or message type
     * @param data the data associated with the command
     */
    public Chat(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the command associated with this message.
     *
     * @return the command name
     */
    public String getCommand() {
        return type;
    }

    /**
     * Returns the data associated with this message.
     *
     * @return the message data
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns a string representation of the message.
     *
     * @return a formatted string containing the command and data
     */
    @Override
    public String toString() {
        return "[Chat] type=" + type + " | data=" + data;
    }
}