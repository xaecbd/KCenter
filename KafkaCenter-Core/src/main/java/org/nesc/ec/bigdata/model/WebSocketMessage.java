package org.nesc.ec.bigdata.model;

/**
 * @author Truman.P.Du
 * @date 2020/03/06
 * @description
 */
public class WebSocketMessage {
    private String type;
    private String message;
    private String id;
    private String operate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
