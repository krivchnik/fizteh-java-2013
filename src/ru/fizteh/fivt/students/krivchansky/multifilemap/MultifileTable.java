package ru.fizteh.fivt.students.krivchansky.multifilemap;

import ru.fizteh.fivt.students.krivchansky.filemap.*;
import ru.fizteh.fivt.students.krivchansky.shell.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultifileTable extends SomeTable {
	
    private static final int DIR_QUANTITY = 16;
    private static final int FILES_PER_DIR = 16;

    public MultifileTable(String directory, String tableName) {
        super(directory, tableName);
    }

    protected void save() {
        File tableDirectory = getTableDirectory();
        ArrayList<Set<String>> keysToSave = new ArrayList<Set<String>>();
        boolean dirIsEmpty;
        for(int dirNumber = 0; dirNumber < DIR_QUANTITY; ++dirNumber) {
            keysToSave.clear();
            for(int index = 0; index < FILES_PER_DIR; ++index) {
                keysToSave.add(new HashSet<String>());
            }
            dirIsEmpty = true;
            try {
            	for(final String key : unchangedOldData.keySet()) {
            		if (getDirNumber(key) == dirNumber) {
            			int fileNumber = getFileNumber(key);
            			keysToSave.get(fileNumber).add(key);
            			dirIsEmpty = false;
            		}
            	}
            } catch (SomethingIsWrongException e) {
            	System.out.println(e.getMessage());
            }
            String dirName = String.format("%d.dir", dirNumber);
            File directory = new File(tableDirectory, dirName);
            if (dirIsEmpty) {
            	directory.delete();
            }
            for(int fileNumber = 0; fileNumber < FILES_PER_DIR; ++fileNumber) {
            	String fileName = String.format("%d.dat", fileNumber);
            	File file = new File(directory, fileName);
            	if (keysToSave.get(fileNumber).isEmpty()) {
            		file.delete();
            		continue;
            	}
            	if (!directory.exists()) {
            		directory.mkdir();
            	}
            	try {
            		writeOnDisk(keysToSave.get(fileNumber), file.getAbsolutePath());
            	} catch (SomethingIsWrongException e) {
            		System.out.println("Error acquired while writing into a table. Message: " + e.getMessage());
            	}	
            }
        }
    }
    

    protected void load() {
        File tableDirectory = getTableDirectory();
        for(String backet : tableDirectory.list()) {
        	File tempOuter = new File (tableDirectory, backet);
        	if (tempOuter!=null && tempOuter.isDirectory()) {
        		for(String file : tempOuter.list()) {
        			File tempInner = new File (tempOuter, file);
        			try {
        				scanFromDisk(tempInner.getAbsolutePath());
        			} catch (SomethingIsWrongException e) {
        				System.out.println("Error acquired while reading out of table. Message: " + e.getMessage());
        			}
        		}
        	}
        }
    }

    private File getTableDirectory() {
    	File curDir = new File(parentDirectory);
    	String temp = curDir.getAbsolutePath();
		File tableDirectory = new File(temp, getName());
		if (!tableDirectory.exists()) {
	        tableDirectory.mkdir();
	    }
		return tableDirectory;
    }

    private int getDirNumber(String key) throws SomethingIsWrongException {
        byte[] bytes = UtilMethods.getBytes(key, UtilMethods.ENCODING);
        return Math.abs(bytes[0] % DIR_QUANTITY);
    }

    private int getFileNumber(String key) throws SomethingIsWrongException {
        byte[] bytes = UtilMethods.getBytes(key, UtilMethods.ENCODING);
        return Math.abs(bytes[0] / DIR_QUANTITY % FILES_PER_DIR);
    }
}