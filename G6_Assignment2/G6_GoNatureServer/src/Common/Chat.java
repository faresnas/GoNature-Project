package Common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Chat implements Serializable {

    private String type;
    private Object data;

    public Chat(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getCommand() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "[Chat] type=" + type + " | data=" + data;
    }
}