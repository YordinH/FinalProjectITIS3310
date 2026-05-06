package contracts;

import java.io.File;
import java.util.List;

public interface IFileService {
    List<File> scanUnimported();
    File removeBackground(File source);
    void markImported(String filename);
}
