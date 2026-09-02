package hu.motorworkshop.app.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderPhotoRepository
        extends JpaRepository<WorkOrderPhoto, UUID> {

    List<WorkOrderPhoto>
    findAllByWorkOrder_IdOrderByCreatedAtDesc(
            UUID workOrderId
    );


    Optional<WorkOrderPhoto>
    findByIdAndWorkOrder_Id(
            UUID id,
            UUID workOrderId
    );
}