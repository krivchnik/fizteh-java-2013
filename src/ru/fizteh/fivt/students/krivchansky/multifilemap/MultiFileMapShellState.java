package ru.fizteh.fivt.students.krivchansky.multifilemap;
import java.io.IOException;

import ru.fizteh.fivt.students.krivchansky.filemap.FileMapShellState;
import ru.fizteh.fivt.students.krivchansky.filemap.MyTable;

public class MultiFileMapShellState extends FileMapShellState implements MultifileMapShellStateInterface<MyTable, String, String> {
	    public MyTableProvider tableProvider;

		public MyTable useTable(String name) {
			table = tableProvider.getTable(name);
			return table;
		}

		public MyTable createTable(String args) {
			return tableProvider.createTable(args);
		}

		public void dropTable(String name) throws IOException {
			tableProvider.removeTable(name);
		}

		public String getCurrentTableName() {
			return table.getName();
		}
	    
	    
}
