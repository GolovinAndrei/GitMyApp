package co.il.telran.git.view;

import co.il.telran.git.service.GitRepository;

import java.util.function.Consumer;

public class ControllerItems {

    private final GitRepository repository;

    public ControllerItems(GitRepository repository) {
        this.repository = repository;
    }

    public Item commitMenu() {
        return 	new Menu("Commit",
                Item.of("Commit", getConsumerForCommit()),
                Item.of("Info", getConsumerForInfo()),
                Item.exit());
    }

    public Item infoMenu() {
        return 	new Menu("Info",
                Item.of("Get log", getConsumerForLog()),
                Item.of("Commit content", getConsumerForCommitContent()),
                Item.exit());
    }

    public Item branchesMenu() {
        return 	new Menu("Branches",
                Item.of("Get all branches", getConsumerForAllBranches()),
                Item.of("Create new branch", getConsumerForNewBranch()),
                Item.of("Rename branch", getConsumerForRenameBranch()),
                Item.of("Delete branch", getConsumerForDelete()),
                Item.exit());
    }

    public Item naviMenu() {
        return new Menu("Navigation",
                Item.of("Switch to branch or commit", getConsumerForSwitching()),
                Item.of("Get head", getConsumerForHead()),
                Item.exit());
    }

    private Consumer<InputOutput> getConsumerForCommit(){
        return io -> {
            String message = io.readString("Enter message for commit");
            io.writeLine(repository.commit(message));
        };
    }

    private Consumer<InputOutput> getConsumerForInfo(){
        return io->repository.info().forEach(e -> io.writeLine(e.toString()));
    }

    private Consumer<InputOutput> getConsumerForLog (){
        return io->repository.log().forEach(e -> io.writeLine(e.toString()));
    }

    private Consumer<InputOutput> getConsumerForCommitContent (){
        return io->{
            String commitName = io.readString("Enter commit name");
            repository.commitContent(commitName).forEach(p->io.writeLine(p.getFileName() + " " +p));
        };
    }

    private Consumer<InputOutput> getConsumerForAllBranches() {
        return io->repository.branches().forEach(e -> io.writeLine(e.toString()));
    }

    private Consumer<InputOutput> getConsumerForNewBranch (){
        return io->{
            String branchName = io.readString("Enter branch name");
            io.writeLine(repository.createBranch(branchName));
        };
    }

    private Consumer<InputOutput> getConsumerForRenameBranch(){
        return io->{
            String oldName = io.readString("Enter branch name for renaming");
            String newName = io.readString("Enter new branch name");
            io.writeLine(repository.renameBranch(oldName, newName));
        };
    }

    private Consumer<InputOutput> getConsumerForDelete (){
        return io->{
            String branchName = io.readString("Enter branch name for delete");
            io.writeLine(repository.deleteBranch(branchName));
        };
    }

    private Consumer<InputOutput> getConsumerForSwitching (){
        return io->{
            String name = io.readString("Enter branch name or commit name to switch");
            io.writeLine(repository.switchTo(name));
        };
    }

    private Consumer<InputOutput> getConsumerForHead() {
        return io -> io.writeString("Head on " + repository.getHead());
    }
}

