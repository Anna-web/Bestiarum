import java.io.File;
import java.io.IOException;

public abstract class FileImporter {
    protected FileImporter next;

    public FileImporter() {
    }

    public void setNext(FileImporter next) {
        this.next = next;
    }

    public abstract void importFile(File var1, CreatureStorages var2) throws IOException;
}
