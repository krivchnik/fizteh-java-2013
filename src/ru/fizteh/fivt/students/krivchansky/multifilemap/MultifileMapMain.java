package src.ru.fizteh.fivt.students.krivchansky.multifilemap;

import java.util.ArrayList;
import java.util.HashSet;

import src.ru.fizteh.fivt.students.krivchansky.filemap.CommitCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.ExitCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.FileMapShellState;
import src.ru.fizteh.fivt.students.krivchansky.filemap.GetCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.PutCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.RemoveKeyCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.RollbackCommand;
import src.ru.fizteh.fivt.students.krivchansky.filemap.SizeCommand;
import src.ru.fizteh.fivt.students.krivchansky.shell.Commands;
import src.ru.fizteh.fivt.students.krivchansky.shell.Shell;


public class MultifileMapMain {
	public static void main(String args[]) {
		HashSet<Commands<FileMapShellState>> com =  new HashSet<Commands<FileMapShellState>>() {{ add(new ExitCommand()); add(new RollbackCommand()); add(new CommitCommand()); 
        add(new PutCommand()); add(new GetCommand()); add(new RemoveKeyCommand()); add(new SizeCommand());}};
        HashSet<Commands<MultiFileMapShellState>> com1 =  new HashSet<Commands<MultiFileMapShellState>>() {{add(new DropCommand()); add(new UseCommand()); 
         add(new CreateCommand());}};
        ArrayList<Commands<?>> res = new ArrayList<Commands<?>>();
        res.addAll(com);
        res.addAll(com1);
        HashSet<Commands<?>> actualResult = new HashSet<Commands<?>>(res);
        Shell<MultiFileMapShellState> shell = new Shell<MultiFileMapShellState>(actualResult);
        try {
        	String dbDirectory = System.getProperty("fizteh.db.dir");
        	MultiFileMapShellState state = new MultiFileMapShellState();
        	DatabaseFactory factory = new DatabaseFactory();
        	state.tableProvider = factory.create(dbDirectory);
        	shell.setShellState(state);
        } catch (IllegalArgumentException e) {
        	System.err.println("error: " + e.getMessage());
        	System.exit(-1);
        }
        shell.run(args, shell);
	}
}
