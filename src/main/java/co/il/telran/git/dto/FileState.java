package co.il.telran.git.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;


@Setter
@Getter
public class FileState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Status status;

    private File file;

    private long lastModif;

    public FileState(File file) {
        this.file = file;
        this.lastModif = file.lastModified();
    }

    @Override
    public String toString() {
        return "file \""+file.getName()+"\" - status: "+status.name();
    }
}
