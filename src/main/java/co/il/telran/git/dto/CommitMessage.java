package co.il.telran.git.dto;

import java.io.Serializable;

public class CommitMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    private String commitName;

    public CommitMessage(String message, String commitName) {
        this.message = message;
        this.commitName = commitName;
    }

    @Override
    public String toString() {
        return commitName + " - " + message;
    }
}
