package hu.motorworkshop.app.photo;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderPhotoDto(

        UUID id,

        String originalFileName,

        String contentType,

        Long fileSize,

        Instant createdAt

) {
}