package co.il.telran.git;


import co.il.telran.git.service.GitRepository;
import co.il.telran.git.service.impl.GitRepositoryImpl;
import co.il.telran.git.view.ControllerItems;
import co.il.telran.git.view.Item;
import co.il.telran.git.view.Menu;
import co.il.telran.git.view.StandardInputOutput;

public class GitApp {

    public static void main(String... args) {
        GitRepository gitRepository = GitRepositoryImpl.init();
        ControllerItems contr = new ControllerItems(gitRepository);
        StandardInputOutput ios = new StandardInputOutput();
        Menu menu = new Menu("Main menu",
                contr.branchesMenu(),
                contr.commitMenu(),
                contr.infoMenu(),
                contr.naviMenu(),
                Item.of("Exit", io -> {
                    gitRepository.save();
                    io.writeLine("File has been saved");
                }, true));
        menu.perform(ios);
    }
}
