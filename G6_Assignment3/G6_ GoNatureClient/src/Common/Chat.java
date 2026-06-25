package Common;

import java.io.Serializable;

/**
 * Represents a message exchanged between the client and the server.
 * A Chat object contains the command type and the related data that
 * should be processed by the receiver.
 *
 * @author Fares
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Chat implements Serializable {

    /**
     * The command type sent between the client and the server.
     */
    private String type;

    /**
     * The data attached to the command.
     */
    private Object data;

    /**
     * Creates a new Chat message.
     *
     * @param type the command type.
     * @param data the data associated with the command.
     */
    public Chat(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the command type.
     *
     * @return the command type.
     */
    public String getCommand() {
        return type;
    }

    /**
     * Returns the attached data.
     *
     * @return the data object.
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns a string representation of the Chat object.
     *
     * @return formatted Chat information.
     */
    @Override
    public String toString() {
        return "[Chat] type=" + type + " | data=" + data;
    }
}