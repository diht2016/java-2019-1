import java.io.*;
import java.nio.charset.StandardCharsets;

public class EncryptedClassLoader extends ClassLoader {
    private final String key;
    private final File dir;

    public EncryptedClassLoader(String key, File dir, ClassLoader parent) {
        super(parent);
        this.key = key;
        this.dir = dir;
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = getDecryptedFileContent(dir + name);
            return defineClass(name, b, 0, b.length);
        } catch (IOException err) {
            throw new ClassNotFoundException();
        }
    }
 
    private byte[] getDecryptedFileContent(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] fileData = inputStream.readAllBytes();
        byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < fileData.length; i++) {
            fileData[i] = (byte) (fileData[i] ^ keyData[i % keyData.length]);
        }
        return fileData;
    }
}
