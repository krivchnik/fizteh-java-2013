package ru.fizteh.fivt.students.baldindima.junit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import ru.fizteh.fivt.storage.structured.TableProvider;


public class DataBaseFile {
    protected final Map<String, Node> currentTable = new HashMap<String, Node>();
    protected File dataBaseFile;
    protected String fileName;
    private TableProvider provider;
    private DataBase table;
    private int fileNumber;
    private int directoryNumber;
    static final byte OLD = 0;
    static final byte NEW = 1;
    static final byte DELETED = 2;
    static final byte MODIFIED = 3;

    public final class Node {
        private byte type;
        private String value;
        private String oldValue;
        private boolean wasInBase;

        public Node(String newValue, String newOldValue, byte newType) {
            type = newType;
            value = newValue;
            oldValue = newOldValue;
            wasInBase = false;
        }

        public Node(byte nType) {
            if (nType == DELETED) {
                value = null;
                type = nType;
            }


        }

        public Node(String nValue, byte nType) {
            type = nType;
            if (nType == OLD) {
                value = nValue;
                oldValue = value;
                wasInBase = true;
            } else {
                if (nType == NEW) {
                    value = nValue;
                    oldValue = null;
                    wasInBase = false;

                }
            }


        }

        public void putValue(String nValue) {
            type = MODIFIED;

            if ((oldValue != null) && (oldValue.equals(nValue))) {
                type = OLD;
            }
            value = nValue;
        }

        public void remove() {
            value = null;
            type = DELETED;
        }


    }

    public DataBaseFile(String fullName, int nDirectoryNumber, int nFileNumber,
                        TableProvider nProvider, DataBase nTable) throws IOException {
        fileName = fullName;
        provider = nProvider;
        table = nTable;
        dataBaseFile = new File(fileName);
        fileNumber = nFileNumber;
        directoryNumber = nDirectoryNumber;
        read();
        check();

    }

    public boolean check() throws IOException {
        for (Map.Entry<String, Node> curPair : getCurrentTable().entrySet()) {
            if (!((Math.abs(curPair.getKey().getBytes("UTF-8")[0]) % 16) == directoryNumber)
                    && ((Math.abs(curPair.getKey().getBytes("UTF-8")[0] / 16) % 16 == fileNumber))) {
                throw new IOException("Wrong file format key[0] =  "
                        + String.valueOf(Math.abs(curPair.getKey().getBytes("UTF-8")[0]))
                        + " in file " + fileName);
            }
            try {
                provider.deserialize(table, (curPair.getValue().value));
            } catch (ParseException e) {
                throw new IOException("Invalid file format! (parse exception error!)");
            }
        }
        return true;
    }

    public Map<String, Node> getCurrentTable() {
        return currentTable;
    }


    public void read() throws IOException {
        //     open(shell, fileFunctions);
        File dataBaseDirectory = new File(dataBaseFile.getParent());
        if (dataBaseDirectory.exists() && dataBaseDirectory.list().length == 0) {
            throw new IOException("Empty dir!");
        }
        if (!dataBaseDirectory.exists() || !dataBaseFile.exists()) {
            return;
        }
        RandomAccessFile randomDataBaseFile = new RandomAccessFile(fileName, "rw");
        if (randomDataBaseFile.length() == 0) {
            randomDataBaseFile.close();
            return;
        }

        while (randomDataBaseFile.getFilePointer() < randomDataBaseFile.length() - 1) {
            int keyLength = randomDataBaseFile.readInt();
            int valueLength = randomDataBaseFile.readInt();
            if ((keyLength <= 0) || (valueLength <= 0)) {
                randomDataBaseFile.close();
                throw new IOException("wrong format");
            }

            byte[] key;
            byte[] value;
            try {
                key = new byte[keyLength];
                value = new byte[valueLength];
            } catch (OutOfMemoryError e) {
                randomDataBaseFile.close();
                throw new IOException("too large key or value");
            }
            randomDataBaseFile.read(key);
            randomDataBaseFile.read(value);
            String keyString = new String(key, "UTF-8");
            String valueString = new String(value, "UTF-8");
            getCurrentTable().put(keyString, new Node(valueString, OLD));
        }
        randomDataBaseFile.close();

    }
    public int realMapSize(){
    	int size = 0;
    	for (Map.Entry<String, Node> curPair : getCurrentTable().entrySet()) {
    		if (curPair.getValue().type != DELETED){
    			++size;
    	}
    	}
    	return size;
    }
    public void write() throws IOException {
    	File dataBaseDirectory = new File(dataBaseFile.getParent());
    	if (realMapSize() == 0){
        	if ((dataBaseFile.exists()) && (!dataBaseFile.delete())) {
                throw new DataBaseException("Cannot delete a file!");
            }
        	
        	if (dataBaseDirectory.exists() && dataBaseDirectory.list().length <= 0){
        		if (!dataBaseDirectory.delete()){
        			throw new DataBaseException("Cannot delete a directory");
        		}
        	}
        } else {
        	if (!dataBaseDirectory.exists() && !dataBaseDirectory.mkdir()){
        		throw new DataBaseException("Cannot create a directory");
        	}
        	if (!dataBaseFile.exists()) {
                if (!dataBaseFile.createNewFile()) {
                    throw new DataBaseException("Cannot create a file " + fileName);
                }
            }
        	RandomAccessFile randomDataBaseFile = new RandomAccessFile(fileName, "rw");
        	
            randomDataBaseFile.getChannel().truncate(0);
            for (Map.Entry<String, Node> curPair : getCurrentTable().entrySet()) {
                if (curPair.getValue().type != DELETED) {
                    randomDataBaseFile.writeInt(curPair.getKey().getBytes("UTF-8").length);
                    randomDataBaseFile.writeInt(curPair.getValue().value.getBytes("UTF-8").length);
                    randomDataBaseFile.write(curPair.getKey().getBytes("UTF-8"));
                    randomDataBaseFile.write(curPair.getValue().value.getBytes("UTF-8"));
                }

            }
            randomDataBaseFile.close();

        	
        }
}

    private void checkString(String str) {
        if ((str == null) || (str.trim().length() == 0)) {
            throw new IllegalArgumentException("Wrong key or value");
        }
    }

    public String put(String keyString, String valueString) {
        checkString(keyString);
        checkString(valueString);

        Node search = getCurrentTable().get(keyString);

        if (search == null) {
            getCurrentTable().put(keyString, new Node(valueString, NEW));
            return null;
        } else {
            String result = null;
            int typeNode = search.type;

            if (typeNode != DELETED) {
                result = search.value;
            }
            getCurrentTable().get(keyString).putValue(valueString);
            return result;
        }


    }


    public String get(String keyString) {
        checkString(keyString);
        Node search = getCurrentTable().get(keyString);
        if (search != null) {
            if (search.type == DELETED) {
                return null;
            } else {
                return search.value;
            }

        } else {
            return null;
        }

    }

    public String remove(String keyString) {
        checkString(keyString);
        String result;
        Node search = getCurrentTable().get(keyString);
        if (search == null) {
            return null;
        } else {
            result = search.value;
            getCurrentTable().get(keyString).remove();
            return result;
        }
    }

    public int countCommits() {
        int count = 0;
        for (Map.Entry<String, Node> curPair : getCurrentTable().entrySet()) {
            if ((curPair.getValue().type == NEW) || (curPair.getValue().type == MODIFIED)
                    || ((curPair.getValue().type == DELETED) && (curPair.getValue().wasInBase))) {
                ++count;
            }
        }
        return count;
    }

    public void commit() throws IOException {
        write();
        getCurrentTable().clear();
        read();
    }

    public void rollback() throws IOException {
        getCurrentTable().clear();
        read();
    }

    public int countSize() {
        int count = 0;
        for (Map.Entry<String, Node> curPair : getCurrentTable().entrySet()) {
            if (curPair.getValue().type != DELETED) {
                ++count;
            }
        }
        return count;
    }


}
