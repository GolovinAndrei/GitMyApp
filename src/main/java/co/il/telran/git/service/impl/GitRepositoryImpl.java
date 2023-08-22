package co.il.telran.git.service.impl;

import co.il.telran.git.dto.Commit;
import co.il.telran.git.dto.CommitMessage;
import co.il.telran.git.dto.FileState;
import co.il.telran.git.service.GitRepository;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static co.il.telran.git.dto.Status.*;

public class GitRepositoryImpl implements GitRepository {

    private static final long serialVersionUID = 1L;

    private Head head;

    //<Branch, Map of commits <name commit, commit>
    private HashMap<String, LinkedHashMap<String, Commit>> repo;


    private class Head implements Serializable{
        private static final long serialVersionUID = 1L;
        private String branch;
        private Commit commit;
    }

    public GitRepositoryImpl() {
        repo = new HashMap<>();
        head = new Head();
    }


    public static GitRepositoryImpl init() {
        GitRepositoryImpl gitRepository;
            try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(Paths.get(TEST_FOLDER+GitRepository.GIT_FILE)))) {
                gitRepository = (GitRepositoryImpl) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            gitRepository = new GitRepositoryImpl();
        }

        return gitRepository;
    }

    @Override
    public String commit(String commitMessage) {
        String response= "There are not files for commit!";
        String BRANCH_MASTER = "master";
        if (head.branch == null) {
            repo.put(BRANCH_MASTER, new LinkedHashMap<>());
            head.branch = BRANCH_MASTER;
        }
        if (isHeadInLastCommitInBranch()) {
            List<FileState> actualStates = actualizationOfState();
            if (areThereUncommittedFiles(actualStates)) {
                Commit commit = new Commit(commitMessage, getContentFromFiles(actualStates), actualStates);
                repo.get(head.branch).put(commit.getName(), commit);
                head.commit = commit;
                response = "Commit "+commit.getName() + " was created.";
            }
        } else response = "Commit is not last! Impossible to commit!";
        return response;
    }

    private boolean isHeadInLastCommitInBranch (){
       boolean res = true;
       if (head.commit!=null && head.branch!=null && !repo.isEmpty()){
           res = new LinkedList<>(repo.get(head.branch).keySet()).getLast().equals(head.commit.getName());
       }
        return res;
    }

    private Map<String, String[]> getContentFromFiles (List<FileState> states) {
        Map<String, String[]> filesContent = new HashMap<>();
        for (FileState fileState : states) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileState.getFile().getPath()))) {
                filesContent.put(fileState.getFile().getName(), reader.lines().toArray(String[]::new));
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return filesContent;
    }

    public List<FileState> actualizationOfState () {
        List<FileState> states = new ArrayList<>();
        File directory = new File(TEST_FOLDER);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(file -> file.getName().matches("(\\w|\\d)+[.]txt"));
            if (files != null) {
                Arrays.stream(files).forEach(file -> states.add(new FileState(file)));
                if (head.commit == null) {
                    states.forEach(state -> state.setStatus(UNTRACKED));
                } else {
                    states.forEach(state -> {
                        FileState headState = head.commit.getStates().get(state.getFile().getName());
                        if (headState != null) {
                            if (state.getFile().lastModified() != headState.getLastModif()) {
                                state.setStatus(MODIFIED);
                            } else {
                                state.setStatus(COMMITTED);
                            }
                        } else {
                            state.setStatus(UNTRACKED);
                        }
                    });
                }
            }
        }
        return states;
    }

    @Override
    public List<FileState> info() {
        return actualizationOfState();
    }

    @Override
    public String createBranch(String branchName) {
        String res;
        if (repo.putIfAbsent(branchName, new LinkedHashMap<>()) == null){
            res = "New branch \""+branchName+ "\" has been created";
            head.branch = branchName;
            head.commit = null;
        } else {
            res = "Branch with name \"" + branchName + "\" already existed";
        }
        return res;

    }

    @Override
    public String renameBranch(String branchName, String newName) {
        String res;
        if (repo.containsKey(branchName)){
            String resFromCreating = createBranch(newName);
            if (resFromCreating.endsWith("created")){
                repo.replace(newName, repo.get(branchName));
                head.branch = newName;
                repo.remove(branchName);
                res = "Branch \""+branchName+"\" has been renamed to \""+newName+"\"";
            } else {
                res = resFromCreating;
            }
        } else {
            res = "There is not branch with such name!";
        }
        return res;
    }

    @Override
    public String deleteBranch(String branchName) {
        String res;
        if (!branchName.equals(head.branch)){
            if (repo.containsKey(branchName)) {
                repo.remove(branchName);
                res = "Branch \""+branchName+"\" has been deleted";
            } else {
                res = "There is not branch with such name!";
            }
        } else {
            res = "Branch on which referred HEAD cannot be deleted!";
        }
        return res;
    }

    @Override
    public List<CommitMessage> log() {
     List<CommitMessage> logs = new ArrayList<>();
        if (repo!=null && !repo.isEmpty() && head.branch != null) {
            repo.get(head.branch).forEach((key, value) -> logs.add(new CommitMessage(value.getMessage(), value.getName())));
        }
        return logs;
    }

    @Override
    public List<String> branches() {
        List<String> branches = new ArrayList<>();
        if (!repo.isEmpty() && head.branch!=null) {
            List<String> setBranches = new ArrayList<>(repo.keySet());
            setBranches.add(head.branch + "*");
            setBranches.remove(head.branch);
            branches=setBranches;
        }
    return branches;
    }

    @Override
    public List<Path> commitContent(String commitName) {
        List<Path> pathes = new LinkedList<>();
        Commit commitByName = getCommitByName(commitName);
       if (commitByName!=null) {
           for (FileState state : commitByName.getStates().values()) {
               pathes.add(Paths.get(state.getFile().getPath()));
           }
       }
        return pathes;
    }

    private Commit getCommitByName (String commitName){
        Commit commit = null;
        Iterator<Map.Entry<String, LinkedHashMap<String, Commit>>> iter = repo.entrySet().iterator();
        while (iter.hasNext() && commit==null) {
            LinkedHashMap<String, Commit> map = iter.next().getValue();
            if (map.containsKey(commitName)) {
                commit = map.get(commitName);
            }
        }
        return commit;
    }

    @Override
    public String switchTo(String name) {
        String response;
        boolean flag = false;
        List<FileState> actualState = actualizationOfState();
        if (!areThereUncommittedFiles(actualState)) {
            if (repo.containsKey(name)) {
                head.branch = name;
                List<Commit> commits = new ArrayList<>(repo.get(name).values());
                if (!commits.isEmpty()) {
                    head.commit = commits.get(commits.size() - 1);
                    flag = true;
                }
            } else {
                Iterator<Map.Entry<String, LinkedHashMap<String, Commit>>> iter = repo.entrySet().iterator();
                while (iter.hasNext() && !flag) {
                    Map.Entry<String, LinkedHashMap<String, Commit>> entry = iter.next();
                    if (entry.getValue().containsKey(name)) {
                        head.branch = entry.getKey();
                        head.commit = entry.getValue().get(name);
                        flag = true;
                    }
                }
            }
            if (flag) {
                response = replaceFiles(head.commit, actualState);
                head.commit.getStates().forEach((k, v) -> v.setStatus(COMMITTED));
            } else {
                response = "There are not branch or commit with such name!";
            }
        } else {
            response = "There are uncommitted files!";
        }
        return response;
    }

    private String replaceFiles (Commit commit, List<FileState> actualState){
        String res = "All files from commit "+ commit.getName() + " have been recovered.";
        Map<String, String[]> filesForRecovery = new HashMap<>(commit.getFilesForCommit());
        for (FileState state: actualState) {
            File file = state.getFile();
           try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
               String[] strings = filesForRecovery.get(file.getName());
               if (strings != null) {
                   filesForRecovery.remove(file.getName());
                   for (String s : strings) {
                       writer.write(s);
                   }
                   writer.flush();
                   file.setLastModified(commit.getStates().get(file.getName()).getLastModif());
               } else {
                   writer.close();
                   file.delete();
               }
           } catch (IOException e) {
               res = "File not found!";
           }
       }
        if (!filesForRecovery.isEmpty()){
                filesForRecovery.entrySet().forEach(entry -> {
                    try {
                        File file = new File(TEST_FOLDER +"/" +entry.getKey());
                        file.createNewFile();
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                                for (String s : entry.getValue()) {
                                    writer.write(s);
                                }
                                writer.flush();
                            }
                        file.setLastModified(commit.getStates().get(entry.getKey()).getLastModif());
                        } catch(IOException e){
                            //res = "File not found!";
                        }
                });
            }
        return res;
    }

    private boolean areThereUncommittedFiles ( List<FileState> actualStates) {
        return actualStates.stream().anyMatch(fileState -> fileState.getStatus() == UNTRACKED || fileState.getStatus() == MODIFIED);
    }

    @Override
    public String getHead() {
        return head.commit.getName();
    }

    @Override
    public void save() {
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(Paths.get(TEST_FOLDER+GIT_FILE)))){
            output.writeObject(this);
        } catch(Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String addIgnoredFileNameExp(String regex) {
        return null;
    }

    //For testing
    private static final String TEST_FOLDER = "c:/GitTest/";

}
