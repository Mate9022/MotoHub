package hu.motorworkshop.app.pdf;

import hu.motorworkshop.app.workorder.WorkOrder;
import hu.motorworkshop.app.workorder.WorkOrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderPdfController {

    private final WorkOrderService workOrderService;

    private final WorkOrderPdfService pdfService;


    public WorkOrderPdfController(
            WorkOrderService workOrderService,
            WorkOrderPdfService pdfService
    ) {

        this.workOrderService =
                workOrderService;

        this.pdfService =
                pdfService;
    }


    @GetMapping(
            value = "/{id}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable UUID id
    ) {

        WorkOrder workOrder =
                workOrderService.getEntity(id);


        byte[] pdf =
                pdfService.generate(
                        workOrder
                );


        String workOrderNumber =
                workOrder.getWorkOrderNumber();


        String filename =
                (
                        workOrderNumber != null &&
                                !workOrderNumber.isBlank()
                )
                        ? workOrderNumber + ".pdf"
                        : "munkalap-" + id + ".pdf";


        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition(filename)
                )

                .contentType(
                        MediaType.APPLICATION_PDF
                )

                .contentLength(
                        pdf.length
                )

                .body(
                        pdf
                );
    }


    private String contentDisposition(
            String filename
    ) {

        return "inline; filename=\""
                + filename
                + "\"; filename*=UTF-8''"
                + encodeFilename(filename);
    }


    private String encodeFilename(
            String filename
    ) {

        StringBuilder result =
                new StringBuilder();


        byte[] bytes =
                filename.getBytes(
                        StandardCharsets.UTF_8
                );


        for (byte value : bytes) {

            int b =
                    value & 0xff;


            if (
                    (b >= 'a' && b <= 'z') ||
                            (b >= 'A' && b <= 'Z') ||
                            (b >= '0' && b <= '9') ||
                            b == '-' ||
                            b == '_' ||
                            b == '.'
            ) {

                result.append(
                        (char) b
                );

            } else {

                result.append(
                        '%'
                );


                String hex =
                        Integer
                                .toHexString(b)
                                .toUpperCase();


                if (hex.length() == 1) {

                    result.append(
                            '0'
                    );
                }


                result.append(
                        hex
                );
            }
        }


        return result.toString();
    }
}