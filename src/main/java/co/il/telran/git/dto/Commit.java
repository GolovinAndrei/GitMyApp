package co.il.telran.git.dto;
import com.fasterxml.uuid.Generators;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Commit implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String message;

    private final LocalDateTime dateTime;

    private Map<String, String[]> filesForCommit;

    private Map<String, FileState> states;


    public Commit(String message, Map<String, String[]> filesForCommit, List<FileState> fileStates) {
        this.name = Generators.timeBasedGenerator().generate().toString();
        this.message = message;
        this.dateTime = LocalDateTime.now();
        this.filesForCommit = filesForCommit;
        this.states = new HashMap<>();
        fileStates.forEach(state->states.put(state.getFile().getName(), state));
    }
}
