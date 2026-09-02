package hu.motorworkshop.app.photo;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Service
public class WorkOrderPhotoImageOptimizer {

    private static final int MAX_OUTPUT_DIMENSION =
            2560;

    private static final float JPEG_QUALITY =
            0.84f;

    /*
     * Védelem extrém nagy / hibás képek ellen.
     * 60 MP még a legtöbb mai telefon teljes felbontású
     * fotóját beengedi, de nem engedünk korlátlan dekódolást.
     */
    private static final long MAX_SOURCE_PIXELS =
            60_000_000L;

    private static final int MAX_SOURCE_DIMENSION =
            12_000;

    private static final String OUTPUT_CONTENT_TYPE =
            "image/jpeg";

    private static final String OUTPUT_EXTENSION =
            ".jpg";

    private static final Set<String> ALLOWED_FORMATS =
            Set.of(
                    "jpeg",
                    "jpg",
                    "png",
                    "webp"
            );


    public OptimizedPhoto optimize(
            MultipartFile file
    ) {

        ImageInfo imageInfo =
                readImageInfo(
                        file
                );


        Path temporaryFile;

        try {

            temporaryFile =
                    Files.createTempFile(
                            "motorworkshop-photo-",
                            OUTPUT_EXTENSION
                    );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Nem sikerült létrehozni az ideiglenes képfájlt.",
                    ex
            );
        }


        try {

            BufferedImage resizedImage =
                    resizeAndOrient(
                            file,
                            imageInfo
                    );


            try {

                writeJpeg(
                        resizedImage,
                        temporaryFile
                );

            } finally {

                resizedImage.flush();
            }


            long fileSize =
                    Files.size(
                            temporaryFile
                    );


            if (fileSize <= 0) {

                throw new IllegalStateException(
                        "A kép optimalizálása üres fájlt eredményezett."
                );
            }


            return new OptimizedPhoto(
                    temporaryFile,
                    OUTPUT_CONTENT_TYPE,
                    fileSize,
                    OUTPUT_EXTENSION
            );

        } catch (IOException ex) {

            deleteQuietly(
                    temporaryFile
            );


            throw new IllegalStateException(
                    "Nem sikerült optimalizálni a képet.",
                    ex
            );

        } catch (RuntimeException ex) {

            deleteQuietly(
                    temporaryFile
            );


            throw ex;
        }
    }


    private ImageInfo readImageInfo(
            MultipartFile file
    ) {

        try (
                InputStream inputStream =
                        file.getInputStream();

                ImageInputStream imageInputStream =
                        ImageIO.createImageInputStream(
                                inputStream
                        )
        ) {

            if (imageInputStream == null) {

                throw new IllegalArgumentException(
                        "A feltöltött fájl nem olvasható képként."
                );
            }


            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(
                            imageInputStream
                    );


            if (!readers.hasNext()) {

                throw new IllegalArgumentException(
                        "A feltöltött fájl nem támogatott képformátum."
                );
            }


            ImageReader reader =
                    readers.next();


            try {

                reader.setInput(
                        imageInputStream,
                        true,
                        true
                );


                String format =
                        reader.getFormatName()
                                .toLowerCase(
                                        Locale.ROOT
                                );


                if (
                        !ALLOWED_FORMATS.contains(
                                format
                        )
                ) {

                    throw new IllegalArgumentException(
                            "Csak JPG, PNG vagy WEBP kép tölthető fel."
                    );
                }


                int width =
                        reader.getWidth(
                                0
                        );

                int height =
                        reader.getHeight(
                                0
                        );


                validateDimensions(
                        width,
                        height
                );


                return new ImageInfo(
                        width,
                        height,
                        format
                );

            } finally {

                reader.dispose();
            }

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "A feltöltött kép sérült vagy nem olvasható.",
                    ex
            );
        }
    }


    private void validateDimensions(
            int width,
            int height
    ) {

        if (
                width <= 0 ||
                        height <= 0
        ) {

            throw new IllegalArgumentException(
                    "A kép mérete érvénytelen."
            );
        }


        if (
                width > MAX_SOURCE_DIMENSION ||
                        height > MAX_SOURCE_DIMENSION
        ) {

            throw new IllegalArgumentException(
                    "A kép felbontása túl nagy."
            );
        }


        long pixelCount =
                (long) width *
                        (long) height;


        if (
                pixelCount > MAX_SOURCE_PIXELS
        ) {

            throw new IllegalArgumentException(
                    "A kép felbontása legfeljebb 60 megapixel lehet."
            );
        }
    }


    private BufferedImage resizeAndOrient(
            MultipartFile file,
            ImageInfo imageInfo
    ) throws IOException {

        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            if (
                    imageInfo.width() > MAX_OUTPUT_DIMENSION ||
                            imageInfo.height() > MAX_OUTPUT_DIMENSION
            ) {

                return Thumbnails
                        .of(
                                inputStream
                        )
                        .size(
                                MAX_OUTPUT_DIMENSION,
                                MAX_OUTPUT_DIMENSION
                        )
                        .keepAspectRatio(
                                true
                        )
                        .useExifOrientation(
                                true
                        )
                        .asBufferedImage();
            }


            /*
             * Kisebb képet nem nagyítunk fel,
             * csak EXIF szerint helyes irányba forgatjuk
             * és újrakódoljuk.
             */
            return Thumbnails
                    .of(
                            inputStream
                    )
                    .scale(
                            1.0
                    )
                    .useExifOrientation(
                            true
                    )
                    .asBufferedImage();
        }
    }


    private void writeJpeg(
            BufferedImage source,
            Path target
    ) throws IOException {

        BufferedImage rgbImage =
                new BufferedImage(
                        source.getWidth(),
                        source.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );


        Graphics2D graphics =
                rgbImage.createGraphics();


        try {

            /*
             * PNG / WEBP átlátszóság esetén ne fekete legyen
             * a háttér JPEG konverzió után.
             */
            graphics.setColor(
                    Color.WHITE
            );

            graphics.fillRect(
                    0,
                    0,
                    rgbImage.getWidth(),
                    rgbImage.getHeight()
            );

            graphics.drawImage(
                    source,
                    0,
                    0,
                    null
            );

        } finally {

            graphics.dispose();
        }


        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName(
                        "jpeg"
                );


        if (!writers.hasNext()) {

            rgbImage.flush();

            throw new IllegalStateException(
                    "A JPEG képkódoló nem érhető el."
            );
        }


        ImageWriter writer =
                writers.next();


        try (
                ImageOutputStream outputStream =
                        ImageIO.createImageOutputStream(
                                target.toFile()
                        )
        ) {

            if (outputStream == null) {

                throw new IOException(
                        "Nem sikerült megnyitni a kimeneti képfájlt."
                );
            }


            writer.setOutput(
                    outputStream
            );


            ImageWriteParam writeParam =
                    writer.getDefaultWriteParam();


            if (
                    writeParam.canWriteCompressed()
            ) {

                writeParam.setCompressionMode(
                        ImageWriteParam.MODE_EXPLICIT
                );

                writeParam.setCompressionQuality(
                        JPEG_QUALITY
                );
            }


            if (
                    writeParam.canWriteProgressive()
            ) {

                writeParam.setProgressiveMode(
                        ImageWriteParam.MODE_DEFAULT
                );
            }


            writer.write(
                    null,
                    new IIOImage(
                            rgbImage,
                            null,
                            null
                    ),
                    writeParam
            );

            outputStream.flush();

        } finally {

            writer.dispose();
            rgbImage.flush();
        }
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


    private record ImageInfo(
            int width,
            int height,
            String format
    ) {
    }


    public record OptimizedPhoto(
            Path path,
            String contentType,
            long fileSize,
            String extension
    ) implements AutoCloseable {

        @Override
        public void close() {

            try {

                Files.deleteIfExists(
                        path
                );

            } catch (IOException ignored) {
            }
        }
    }
}
