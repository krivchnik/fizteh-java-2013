package ru.fizteh.fivt.students.musin.filemap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileMap {
    File location;
    HashMap<String, String> map;

    public FileMap(File location) {
        this.location = location;
        map = new HashMap<String, String>();
    }

    public void loadFromDisk() throws FileNotFoundException {
        map.clear();
        FileInputStream inputStream = new FileInputStream(location);
        byte[] buffer;
        ByteBuffer cast;
        try {
            while (true) {
                buffer = new byte[4];
                int bytesRead = inputStream.read(buffer, 0, 4);
                if (bytesRead == -1) {
                    break;
                }
                if (bytesRead != 4) {
                    System.err.println("Database loading failed: Wrong data format");
                    break;
                }
                cast = ByteBuffer.wrap(buffer);
                int keyLength = cast.getInt();
                bytesRead = inputStream.read(buffer, 0, 4);
                if (bytesRead != 4) {
                    System.err.println("Database loading failed: Wrong data format");
                    break;
                }
                cast = ByteBuffer.wrap(buffer);
                int valueLength = cast.getInt();
                buffer = new byte[keyLength];
                bytesRead = inputStream.read(buffer, 0, keyLength);
                if (bytesRead != bytesRead) {
                    System.err.println("Database loading failed: Wrong data format");
                    break;
                }
                String key = new String(buffer, "UTF-8");
                buffer = new byte[valueLength];
                bytesRead = inputStream.read(buffer, 0, valueLength);
                if (bytesRead != valueLength) {
                    System.err.println("Database loading failed: Wrong data format");
                    break;
                }
                String value = new String(buffer, "UTF-8");
                map.put(key, value);
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public void writeToDisk() throws Exception {
        if (location.exists() && location.isDirectory()) {
            System.err.println("Database can't be written to the specified location");
            return;
        }
        if (!location.exists()) {
            if (!location.createNewFile()) {
                System.err.println("Database can't be written to the specified location");
                return;
            }
        }
        FileOutputStream outputStream = new FileOutputStream(location);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            byte[] key = entry.getKey().getBytes("UTF-8");
            byte[] value = entry.getValue().getBytes("UTF-8");
            outputStream.write(ByteBuffer.allocate(4).putInt(key.length).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(value.length).array());
            outputStream.write(key);
            outputStream.write(value);
        }
    }

    public String put(String key, String value) {
        return map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public boolean remove(String key) {
        return (map.remove(key) != null);
    }

    private ShellCommand[] commands = new ShellCommand[]{
            new ShellCommand("put", new ShellExecutable() {
                @Override
                public int execute(Shell shell, ArrayList<String> args) {
                    if (args.size() > 2) {
                        System.err.println("put: Too many arguments");
                        return -1;
                    }
                    if (args.size() < 2) {
                        System.err.println("put: Too few arguments");
                        return -1;
                    }
                    String value = put(args.get(0), args.get(1));
                    if (value == null) {
                        System.out.println("new");
                    } else {
                        System.out.printf("overwrite\n%s\n", value);
                    }
                    return 0;
                }
            }),
            new ShellCommand("get", new ShellExecutable() {
                @Override
                public int execute(Shell shell, ArrayList<String> args) {
                    if (args.size() > 1) {
                        System.err.println("put: Too many arguments");
                        return -1;
                    }
                    if (args.size() < 1) {
                        System.err.println("put: Too few arguments");
                        return -1;
                    }
                    String value = get(args.get(0));
                    if (value == null) {
                        System.out.println("not found");
                    } else {
                        System.out.printf("found\n%s\n", value);
                    }
                    return 0;
                }
            }),
            new ShellCommand("remove", new ShellExecutable() {
                @Override
                public int execute(Shell shell, ArrayList<String> args) {
                    if (args.size() > 1) {
                        System.err.println("put: Too many arguments");
                        return -1;
                    }
                    if (args.size() < 1) {
                        System.err.println("put: Too few arguments");
                        return -1;
                    }
                    if (remove(args.get(0))) {
                        System.out.println("removed");
                    } else {
                        System.out.println("not found");
                    }
                    return 0;
                }
            })
    };

    public void integrate(Shell shell) {
        for (int i = 0; i < commands.length; i++) {
            shell.addCommand(commands[i]);
        }
    }
}
