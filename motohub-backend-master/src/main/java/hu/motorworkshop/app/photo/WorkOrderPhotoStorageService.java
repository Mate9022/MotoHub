package hu.motorworkshop.app.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkOrderPhotoStorageService {

    private final Path baseDirectory;


    public WorkOrderPhotoStorageService(
            @Value(
                    "${app.storage.work-order-photos:./data/work-order-photos}"
            )
            String storagePath
    ) {

        this.baseDirectory =
                Path.of(storagePath)
                        .toAbsolutePath()
                        .normalize();


        try {

            Files.createDirectories(
                    baseDirectory
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Nem sikerült létrehozni a fotótároló mappát.",
                    ex
            );
        }
    }


    public String store(
            UUID workOrderId,
            Path sourceFile,
            String extension
    ) {

        if (
                sourceFile == null ||
                        !Files.isRegularFile(
                                sourceFile
                        )
        ) {

            throw new IllegalArgumentException(
                    "A mentendő képfájl nem található."
            );
        }


        String safeExtension =
                normalizeExtension(
                        extension
                );


        String storedFileName =
                UUID.randomUUID()
                        + safeExtension;


        Path workOrderDirectory =
                workOrderDirectory(
                        workOrderId
                );


        Path target =
                workOrderDirectory
                        .resolve(
                                storedFileName
                        )
                        .normalize();


        Path temporaryTarget =
                workOrderDirectory
                        .resolve(
                                storedFileName + ".tmp"
                        )
                        .normalize();


        ensureInside(
                workOrderDirectory,
                target
        );

        ensureInside(
                workOrderDirectory,
                temporaryTarget
        );


        try {

            Files.createDirectories(
                    workOrderDirectory
            );


            Files.copy(
                    sourceFile,
                    temporaryTarget,
                    StandardCopyOption.REPLACE_EXISTING
            );


            moveIntoPlace(
                    temporaryTarget,
                    target
            );


            return storedFileName;

        } catch (IOException ex) {

            deleteQuietly(
                    temporaryTarget
            );

            deleteQuietly(
                    target
            );


            throw new IllegalStateException(
                    "Nem sikerült elmenteni a fotót.",
                    ex
            );
        }
    }


    public Resource load(
            UUID workOrderId,
            String storedFileName
    ) {

        Path directory =
                workOrderDirectory(
                        workOrderId
                );


        Path file =
                directory
                        .resolve(
                                storedFileName
                        )
                        .normalize();


        ensureInside(
                directory,
                file
        );


        try {

            Resource resource =
                    new UrlResource(
                            file.toUri()
                    );


            if (
                    !resource.exists() ||
                            !resource.isReadable()
            ) {

                throw new IllegalStateException(
                        "A fotófájl nem található."
                );
            }


            return resource;

        } catch (MalformedURLException ex) {

            throw new IllegalStateException(
                    "A fotófájl nem olvasható.",
                    ex
            );
        }
    }


    public void delete(
            UUID workOrderId,
            String storedFileName
    ) {

        Path directory =
                workOrderDirectory(
                        workOrderId
                );


        Path file =
                directory
                        .resolve(
                                storedFileName
                        )
                        .normalize();


        ensureInside(
                directory,
                file
        );


        try {

            Files.deleteIfExists(
                    file
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Nem sikerült törölni a fotófájlt.",
                    ex
            );
        }
    }


    private void moveIntoPlace(
            Path source,
            Path target
    ) throws IOException {

        try {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (AtomicMoveNotSupportedException ex) {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }


    private Path workOrderDirectory(
            UUID workOrderId
    ) {

        Path directory =
                baseDirectory
                        .resolve(
                                workOrderId.toString()
                        )
                        .normalize();


        ensureInside(
                baseDirectory,
                directory
        );


        return directory;
    }


    private void ensureInside(
            Path parent,
            Path child
    ) {

        if (
                !child.startsWith(
                        parent
                )
        ) {

            throw new IllegalStateException(
                    "Érvénytelen fájlútvonal."
            );
        }
    }


    private String normalizeExtension(
            String extension
    ) {

        if (
                extension == null ||
                        !extension.matches(
                                "\\.[A-Za-z0-9]{1,10}"
                        )
        ) {

            throw new IllegalArgumentException(
                    "Érvénytelen fájlkiterjesztés."
            );
        }


        return extension.toLowerCase(
                Locale.ROOT
        );
    }


    private void deleteQuietly(
            Path file
    ) {

        try {

            Files.deleteIfExists(
                    file
            );

        } catch (IOException ignored) {
        }
    }
}
