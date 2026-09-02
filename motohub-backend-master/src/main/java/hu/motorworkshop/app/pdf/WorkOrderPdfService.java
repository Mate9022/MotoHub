package hu.motorworkshop.app.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import hu.motorworkshop.app.photo.WorkOrderPhoto;
import hu.motorworkshop.app.photo.WorkOrderPhotoRepository;
import hu.motorworkshop.app.photo.WorkOrderPhotoStorageService;
import hu.motorworkshop.app.settings.WorkshopSettingsDto;
import hu.motorworkshop.app.settings.WorkshopSettingsService;
import hu.motorworkshop.app.workorder.LaborItem;
import hu.motorworkshop.app.workorder.PartItem;
import hu.motorworkshop.app.workorder.WorkOrder;
import hu.motorworkshop.app.workorder.WorkOrderStatus;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkOrderPdfService {

    private static final Locale HU =
            Locale.forLanguageTag("hu-HU");


    private static final ZoneId BUDAPEST =
            ZoneId.of("Europe/Budapest");


    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter
                    .ofPattern("yyyy.MM.dd. HH:mm")
                    .withLocale(HU)
                    .withZone(BUDAPEST);


    /*
     * PDF-be nem rakjuk bele a teljes
     * 12-48 megapixeles mobilfotót.
     *
     * Generáláskor maximum ekkorára
     * méretezzük át.
     */
    private static final int PDF_IMAGE_MAX_SIZE =
            1400;


    private static final float PDF_JPEG_QUALITY =
            0.82f;


    private final WorkshopSettingsService settingsService;

    private final WorkOrderPhotoRepository photoRepository;

    private final WorkOrderPhotoStorageService photoStorage;


    public WorkOrderPdfService(
            WorkshopSettingsService settingsService,
            WorkOrderPhotoRepository photoRepository,
            WorkOrderPhotoStorageService photoStorage
    ) {

        this.settingsService =
                settingsService;

        this.photoRepository =
                photoRepository;

        this.photoStorage =
                photoStorage;
    }


    // =====================================================
    // PDF GENERÁLÁS
    // =====================================================

    public byte[] generate(
            WorkOrder workOrder
    ) {

        WorkshopSettingsDto settings =
                settingsService.get();


        validateSettings(
                settings
        );


        List<WorkOrderPhoto> photos =
                photoRepository
                        .findAllByWorkOrder_IdOrderByCreatedAtDesc(
                                workOrder.getId()
                        );


        String html =
                buildHtml(
                        workOrder,
                        settings,
                        photos
                );


        try (
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();


            registerHungarianFontIfAvailable(
                    builder
            );


            builder.useFastMode();


            builder.withHtmlContent(
                    html,
                    null
            );


            builder.toStream(
                    output
            );


            builder.run();


            return output.toByteArray();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "A PDF generálása sikertelen.",
                    ex
            );
        }
    }


    // =====================================================
    // MŰHELYBEÁLLÍTÁSOK ELLENŐRZÉSE
    // =====================================================

    private void validateSettings(
            WorkshopSettingsDto settings
    ) {

        if (
                settings.workshopName() == null ||
                        settings.workshopName().isBlank()
        ) {

            throw new IllegalStateException(
                    "A PDF kiállításához add meg a műhely nevét a Beállításokban."
            );
        }


        /*
         * A részletes szervizdokumentációban
         * legyen egyértelműen azonosítható
         * a dokumentumot kiállító műhely.
         */
        if (
                settings.address() == null ||
                        settings.address().isBlank()
        ) {

            throw new IllegalStateException(
                    "A PDF kiállításához add meg a műhely címét a Beállításokban."
            );
        }
    }


    // =====================================================
    // BETŰTÍPUS
    // =====================================================

    private void registerHungarianFontIfAvailable(
            PdfRendererBuilder builder
    ) {

        List<String> candidates =
                List.of(
                        "C:/Windows/Fonts/arial.ttf",
                        "C:/Windows/Fonts/calibri.ttf",
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/dejavu/DejaVuSans.ttf"
                );


        for (String path : candidates) {

            File font =
                    new File(path);


            if (font.isFile()) {

                builder.useFont(
                        font,
                        "WorkshopFont"
                );

                return;
            }
        }
    }


    // =====================================================
    // HTML
    // =====================================================

    private String buildHtml(
            WorkOrder workOrder,
            WorkshopSettingsDto settings,
            List<WorkOrderPhoto> photos
    ) {

        var motorcycle =
                workOrder.getMotorcycle();


        var customer =
                motorcycle.getCustomer();


        /*
         * A dokumentum generálásának tényleges időpontja.
         */
        Instant issuedAt =
                Instant.now();


        /*
         * Régi munkalapok kompatibilitása:
         *
         * ha handedOverAt még nincs,
         * de closedAt igen, akkor azt használjuk.
         */
        Instant handedOverAt =
                workOrder.getHandedOverAt() != null
                        ? workOrder.getHandedOverAt()
                        : workOrder.getClosedAt();


        BigDecimal laborTotal =
                workOrder
                        .getLaborItems()
                        .stream()
                        .map(
                                LaborItem::calculateTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal partsTotal =
                workOrder
                        .getPartItems()
                        .stream()
                        .map(
                                PartItem::calculateTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal grandTotal =
                laborTotal.add(
                        partsTotal
                );


        String laborRows =
                buildLaborRows(
                        workOrder
                );


        String partRows =
                buildPartRows(
                        workOrder
                );


        String workshopContact =
                buildWorkshopContact(
                        settings
                );


        String photoSection =
                buildPhotoSection(
                        workOrder.getId(),
                        photos
                );


        String stateNotice =
                buildStateNotice(
                        workOrder
                );


        return """
                <!DOCTYPE html>

                <html lang="hu">

                <head>

                    <meta charset="UTF-8"/>

                    <style>

                        @page {
                            size: A4;
                            margin: 15mm 17mm 18mm 17mm;
                        }


                        body {
                            font-family:
                                WorkshopFont,
                                Arial,
                                sans-serif;

                            color: #151515;

                            font-size: 10pt;
                            line-height: 1.4;
                        }


                        h1 {
                            margin: 0 0 2mm;

                            font-size: 20pt;
                        }


                        h2 {
                            margin: 7mm 0 2.5mm;

                            padding-bottom: 1.5mm;

                            border-bottom:
                                1px solid #bbbbbb;

                            font-size: 12.5pt;
                        }


                        .muted {
                            color: #666666;
                        }


                        /* ====================================== */
                        /* MŰHELY */
                        /* ====================================== */

                        .workshop-header {
                            width: 100%%;

                            margin-bottom: 7mm;

                            padding-bottom: 4mm;

                            border-bottom:
                                2px solid #222222;
                        }


                        .workshop-name {
                            margin-bottom: 1.5mm;

                            font-size: 18pt;
                            font-weight: bold;
                        }


                        .workshop-contact {
                            color: #555555;

                            font-size: 9pt;
                            line-height: 1.5;
                        }


                        /* ====================================== */
                        /* DOKUMENTUM FEJLÉC */
                        /* ====================================== */

                        .document-header {
                            margin-bottom: 5mm;
                        }


                        .work-order-number {
                            margin-top: 1mm;

                            font-size: 11pt;
                            font-weight: bold;
                        }


                        .issued-at {
                            margin-top: 1mm;

                            color: #666666;

                            font-size: 9pt;
                        }


                        .document-notice {
                            margin: 4mm 0 6mm;

                            padding: 3.5mm;

                            border:
                                1px solid #bbbbbb;

                            background: #f7f7f7;

                            font-size: 8.8pt;
                            line-height: 1.5;
                        }


                        /* ====================================== */
                        /* MUNKALAP ÁLLAPOT */
                        /* ====================================== */

                        .state-notice {
                            margin-bottom: 4mm;

                            padding: 2.5mm;

                            border:
                                1px solid #c4c4c4;

                            background: #fafafa;

                            font-size: 9pt;
                            font-weight: bold;

                            text-align: center;
                        }


                        /* ====================================== */
                        /* ALAPADATOK */
                        /* ====================================== */

                        .grid {
                            width: 100%%;

                            border-collapse:
                                collapse;
                        }


                        .grid td {
                            width: 50%%;

                            padding:
                                1.5mm 4mm
                                1.5mm 0;

                            vertical-align: top;
                        }


                        .vehicle-box {
                            margin-top: 2mm;

                            padding: 3mm;

                            border:
                                1px solid #dddddd;

                            background: #fafafa;
                        }


                        /* ====================================== */
                        /* TÉTEL TÁBLÁK */
                        /* ====================================== */

                        .items {
                            width: 100%%;

                            border-collapse:
                                collapse;
                        }


                        .items th,
                        .items td {
                            padding:
                                2mm 1.5mm;

                            border-bottom:
                                1px solid #dddddd;

                            text-align: left;
                        }


                        .items th {
                            background: #f3f3f3;

                            font-weight: bold;
                        }


                        .num {
                            text-align:
                                right !important;

                            white-space:
                                nowrap;
                        }


                        /* ====================================== */
                        /* SZÖVEGES BLOKK */
                        /* ====================================== */

                        .note {
                            white-space:
                                pre-wrap;

                            line-height: 1.45;
                        }


                        /* ====================================== */
                        /* ÖSSZESÍTÉS */
                        /* ====================================== */

                        .total {
                            width: 55%%;

                            margin-top: 7mm;
                            margin-left: auto;

                            border-collapse:
                                collapse;

                            page-break-inside:
                                avoid;
                        }


                        .total td {
                            padding: 1.3mm;
                        }


                        .grand {
                            border-top:
                                2px solid #222222;

                            font-size: 13pt;
                            font-weight: bold;
                        }


                        /* ====================================== */
                        /* FOTÓK */
                        /* ====================================== */

                        .photo-layout {
                            width: 100%%;

                            border-collapse:
                                separate;

                            border-spacing:
                                3mm;
                        }


                        .photo-layout td {
                            width: 50%%;

                            vertical-align: top;
                        }


                        .photo-card {
                            padding: 2mm;

                            border:
                                1px solid #dddddd;

                            page-break-inside:
                                avoid;
                        }


                        .photo-image {
                            display: block;

                            width: 100%%;
                            height: auto;

                            max-height: 85mm;
                        }


                        .photo-caption {
                            margin-top: 2mm;

                            color: #555555;

                            font-size: 8pt;
                        }


                        .photo-unavailable {
                            padding:
                                15mm 3mm;

                            background: #f2f2f2;

                            color: #777777;

                            text-align: center;

                            font-size: 8pt;
                        }


                        /* ====================================== */
                        /* ALÁÍRÁS */
                        /* ====================================== */

                        .signatures {
                            width: 100%%;

                            margin-top: 16mm;

                            border-collapse:
                                collapse;

                            page-break-inside:
                                avoid;
                        }


                        .signatures td {
                            width: 50%%;

                            padding: 0 8mm;

                            text-align: center;
                            vertical-align: bottom;
                        }


                        .signature-line {
                            padding-top: 12mm;

                            border-bottom:
                                1px solid #555555;
                        }


                        .signature-label {
                            padding-top: 2mm;

                            color: #666666;

                            font-size: 9pt;
                        }


                        /* ====================================== */
                        /* FOOTER */
                        /* ====================================== */

                        .footer-info {
                            margin-top: 10mm;

                            color: #777777;

                            font-size: 8pt;
                            text-align: center;
                        }

                    </style>

                </head>


                <body>


                    <!-- ========================================== -->
                    <!-- MŰHELY -->
                    <!-- ========================================== -->

                    <div class="workshop-header">

                        <div class="workshop-name">
                            %s
                        </div>


                        <div class="workshop-contact">
                            %s
                        </div>

                    </div>


                    <!-- ========================================== -->
                    <!-- DOKUMENTUM FEJLÉC -->
                    <!-- ========================================== -->

                    <div class="document-header">

                        <h1>
                            Szerviz munkalap / szervizdokumentáció
                        </h1>


                        <div class="work-order-number">

                            Munkalapszám:
                            %s

                        </div>


                        <div class="issued-at">

                            Dokumentum kiállítása:
                            %s

                        </div>

                    </div>


                    <!-- ========================================== -->
                    <!-- ÁLLAPOT -->
                    <!-- ========================================== -->

                    %s


                    <!-- ========================================== -->
                    <!-- DOKUMENTUM JELLEGE -->
                    <!-- ========================================== -->

                    <div class="document-notice">

                        <strong>
                            Tájékoztatás:
                        </strong>

                        ez a dokumentum a motorkerékpáron
                        elvégzett szerviz- és javítási munkák
                        részletes dokumentációja.

                        Nem számla, nem nyugta és az
                        ellenérték megfizetését nem igazolja.

                        A dokumentumban szereplő összegek
                        a munkalapon rögzített szolgáltatások,
                        munkadíjak, alkatrészek és anyagok
                        tájékoztató összesítését jelentik.

                    </div>


                    <!-- ========================================== -->
                    <!-- ÜGYFÉL -->
                    <!-- ========================================== -->

                    <h2>
                        Ügyfél
                    </h2>


                    <table class="grid">

                        <tr>

                            <td>

                                <strong>
                                    Név:
                                </strong>

                                %s

                            </td>


                            <td>

                                <strong>
                                    Telefon:
                                </strong>

                                %s

                            </td>

                        </tr>


                        <tr>

                            <td>

                                <strong>
                                    E-mail:
                                </strong>

                                %s

                            </td>


                            <td>
                                &#160;
                            </td>

                        </tr>

                    </table>


                    <!-- ========================================== -->
                    <!-- MOTORKERÉKPÁR -->
                    <!-- ========================================== -->

                    <h2>
                        Motorkerékpár
                    </h2>


                    <div class="vehicle-box">

                        <table class="grid">

                            <tr>

                                <td>

                                    <strong>
                                        Márka:
                                    </strong>

                                    %s

                                </td>


                                <td>

                                    <strong>
                                        Modell:
                                    </strong>

                                    %s

                                </td>

                            </tr>


                            <tr>

                                <td>

                                    <strong>
                                        Évjárat:
                                    </strong>

                                    %s

                                </td>


                                <td>

                                    <strong>
                                        Rendszám:
                                    </strong>

                                    %s

                                </td>

                            </tr>


                            <tr>

                                <td colspan="2">

                                    <strong>
                                        Alvázszám / VIN:
                                    </strong>

                                    %s

                                </td>

                            </tr>

                        </table>

                    </div>


                    <!-- ========================================== -->
                    <!-- SZERVIZ IDŐPONTOK -->
                    <!-- ========================================== -->

                    <h2>
                        Szerviz időpontok
                    </h2>


                    <table class="grid">

                        <tr>

                            <td>

                                <strong>
                                    Beérkezés:
                                </strong>

                                %s

                            </td>


                            <td>

                                <strong>
                                    Beérkezési kilométeróra:
                                </strong>

                                %s km

                            </td>

                        </tr>


                        <tr>

                            <td>

                                <strong>
                                    Javítás elkészült:
                                </strong>

                                %s

                            </td>


                            <td>

                                <strong>
                                    Átadás az ügyfélnek:
                                </strong>

                                %s

                            </td>

                        </tr>


                        <tr>

                            <td>

                                <strong>
                                    Státusz:
                                </strong>

                                %s

                            </td>


                            <td>

                                <strong>
                                    Átadáskori kilométeróra:
                                </strong>

                                %s km

                            </td>

                        </tr>

                    </table>


                    <!-- ========================================== -->
                    <!-- PANASZ -->
                    <!-- ========================================== -->

                    <h2>
                        Ügyfél által jelzett probléma / kérés
                    </h2>


                    <div class="note">
                        %s
                    </div>


                    <!-- ========================================== -->
                    <!-- MEGÁLLAPÍTÁSOK -->
                    <!-- ========================================== -->

                    <h2>
                        Megállapítások
                    </h2>


                    <div class="note">
                        %s
                    </div>


                    <!-- ========================================== -->
                    <!-- MUNKÁK -->
                    <!-- ========================================== -->

                    <h2>
                        Elvégzett munkák
                    </h2>


                    <table class="items">

                        <thead>

                            <tr>

                                <th>
                                    Munka
                                </th>


                                <th class="num">
                                    Idő
                                </th>


                                <th class="num">
                                    Óradíj
                                </th>


                                <th class="num">
                                    Összeg
                                </th>

                            </tr>

                        </thead>


                        <tbody>
                            %s
                        </tbody>

                    </table>


                    <!-- ========================================== -->
                    <!-- ALKATRÉSZEK -->
                    <!-- ========================================== -->

                    <h2>
                        Felhasznált alkatrészek és anyagok
                    </h2>


                    <table class="items">

                        <thead>

                            <tr>

                                <th>
                                    Tétel
                                </th>


                                <th>
                                    Cikkszám
                                </th>


                                <th class="num">
                                    Menny.
                                </th>


                                <th class="num">
                                    Egységár
                                </th>


                                <th class="num">
                                    Összeg
                                </th>

                            </tr>

                        </thead>


                        <tbody>
                            %s
                        </tbody>

                    </table>


                    <!-- ========================================== -->
                    <!-- AJÁNLÁS -->
                    <!-- ========================================== -->

                    <h2>
                        Ajánlások / következő karbantartás
                    </h2>


                    <div class="note">
                        %s
                    </div>


                    <!-- ========================================== -->
                    <!-- ÖSSZESÍTÉS -->
                    <!-- ========================================== -->

                    <h2>
                        Összesítés
                    </h2>


                    <table class="total">

                        <tr>

                            <td>
                                Munkadíj:
                            </td>


                            <td class="num">
                                %s
                            </td>

                        </tr>


                        <tr>

                            <td>
                                Alkatrészek / anyagok:
                            </td>


                            <td class="num">
                                %s
                            </td>

                        </tr>


                        <tr class="grand">

                            <td>
                                Összesített érték:
                            </td>


                            <td class="num">
                                %s
                            </td>

                        </tr>

                    </table>


                    <!-- ========================================== -->
                    <!-- FOTÓDOKUMENTÁCIÓ -->
                    <!-- ========================================== -->

                    %s


                    <!-- ========================================== -->
                    <!-- ALÁÍRÁS -->
                    <!-- ========================================== -->

                    <table class="signatures">

                        <tr>

                            <td>

                                <div class="signature-line">
                                    &#160;
                                </div>


                                <div class="signature-label">
                                    Ügyfél aláírása
                                </div>

                            </td>


                            <td>

                                <div class="signature-line">
                                    &#160;
                                </div>


                                <div class="signature-label">
                                    Műhely
                                </div>

                            </td>

                        </tr>

                    </table>


                    <!-- ========================================== -->
                    <!-- FOOTER -->
                    <!-- ========================================== -->

                    <div class="footer-info">

                        A dokumentum a(z)
                        %s
                        elektronikus
                        szerviznyilvántartásából készült.

                        <br/>

                        Munkalapszám:
                        %s

                    </div>


                </body>

                </html>
                """.formatted(

                /*
                 * MŰHELY
                 */

                e(
                        settings.workshopName()
                ),

                workshopContact,


                /*
                 * DOKUMENTUM
                 */

                e(
                        workOrder.getWorkOrderNumber()
                ),

                date(
                        issuedAt
                ),

                stateNotice,


                /*
                 * ÜGYFÉL
                 */

                e(
                        customer.getName()
                ),

                e(
                        customer.getPhone()
                ),

                e(
                        customer.getEmail()
                ),


                /*
                 * MOTOR
                 */

                e(
                        motorcycle.getBrand()
                ),

                e(
                        motorcycle.getModel()
                ),

                integer(
                        motorcycle.getModelYear()
                ),

                e(
                        motorcycle.getLicensePlate()
                ),

                e(
                        motorcycle.getVin()
                ),


                /*
                 * SZERVIZ IDŐPONTOK
                 */

                date(
                        workOrder.getReceivedAt()
                ),

                integer(
                        workOrder.getOdometerKm()
                ),

                date(
                        workOrder.getReadyAt()
                ),

                date(
                        handedOverAt
                ),

                statusLabel(
                        workOrder.getStatus()
                ),

                integer(
                        workOrder.getOdometerKm()
                ),


                /*
                 * PANASZ
                 */

                e(
                        workOrder.getComplaint()
                ),


                /*
                 * MEGÁLLAPÍTÁSOK
                 */

                e(
                        workOrder.getFindings()
                ),


                /*
                 * MUNKÁK
                 */

                laborRows,


                /*
                 * ALKATRÉSZEK
                 */

                partRows,


                /*
                 * AJÁNLÁS
                 */

                e(
                        workOrder.getRecommendations()
                ),


                /*
                 * ÖSSZESÍTÉS
                 */

                money(
                        laborTotal
                ),

                money(
                        partsTotal
                ),

                money(
                        grandTotal
                ),


                /*
                 * FOTÓK
                 */

                photoSection,


                /*
                 * FOOTER
                 */

                e(
                        settings.workshopName()
                ),

                e(
                        workOrder.getWorkOrderNumber()
                )
        );
    }


    // =====================================================
    // MUNKALAP ÁLLAPOT JELZÉS
    // =====================================================

    private String buildStateNotice(
            WorkOrder workOrder
    ) {

        if (
                workOrder.getStatus()
                        == WorkOrderStatus.READY_FOR_PICKUP
        ) {

            return """
                    <div class="state-notice">

                        JAVÍTÁS ELKÉSZÜLT –
                        A MOTORKERÉKPÁR ÁTADÁSRA VÁR

                    </div>
                    """;
        }


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            return "";
        }


        return """
                <div class="state-notice">

                    FOLYAMATBAN LÉVŐ MUNKALAP –
                    TÁJÉKOZTATÓ DOKUMENTUM

                </div>
                """;
    }


    // =====================================================
    // FOTÓDOKUMENTÁCIÓ
    // =====================================================

    private String buildPhotoSection(
            UUID workOrderId,
            List<WorkOrderPhoto> photos
    ) {

        if (
                photos == null ||
                        photos.isEmpty()
        ) {

            return "";
        }


        StringBuilder html =
                new StringBuilder();


        html.append(
                """
                <h2>
                    Fotódokumentáció
                </h2>


                <div class="muted">

                    A képek a munkalaphoz rögzített
                    állapot-, hiba- és javítási dokumentáció
                    részét képezik.

                </div>


                <table class="photo-layout">
                """
        );


        for (
                int i = 0;
                i < photos.size();
                i += 2
        ) {

            html.append(
                    "<tr>"
            );


            html.append(
                    buildPhotoCell(
                            workOrderId,
                            photos.get(i)
                    )
            );


            if (
                    i + 1 < photos.size()
            ) {

                html.append(
                        buildPhotoCell(
                                workOrderId,
                                photos.get(
                                        i + 1
                                )
                        )
                );

            } else {

                html.append(
                        "<td></td>"
                );
            }


            html.append(
                    "</tr>"
            );
        }


        html.append(
                "</table>"
        );


        return html.toString();
    }


    private String buildPhotoCell(
            UUID workOrderId,
            WorkOrderPhoto photo
    ) {

        String imageData =
                createPdfImageDataUri(
                        workOrderId,
                        photo
                );


        String imageHtml;


        if (imageData == null) {

            imageHtml =
                    """
                    <div class="photo-unavailable">

                        A kép előnézete ebben a PDF-ben
                        nem jeleníthető meg.

                    </div>
                    """;

        } else {

            imageHtml =
                    """
                    <img
                        class="photo-image"
                        src="%s"
                        alt="%s"
                    />
                    """.formatted(

                            imageData,

                            e(
                                    photo.getOriginalFileName()
                            )
                    );
        }


        return """
                <td>

                    <div class="photo-card">

                        %s


                        <div class="photo-caption">

                            %s

                            <br/>

                            Rögzítve:
                            %s

                        </div>

                    </div>

                </td>
                """.formatted(

                imageHtml,

                e(
                        photo.getOriginalFileName()
                ),

                date(
                        photo.getCreatedAt()
                )
        );
    }


    // =====================================================
    // FOTÓ -> PDF JPEG
    // =====================================================

    private String createPdfImageDataUri(
            UUID workOrderId,
            WorkOrderPhoto photo
    ) {

        try {

            Resource resource =
                    photoStorage.load(
                            workOrderId,
                            photo.getStoredFileName()
                    );


            try (
                    InputStream input =
                            resource.getInputStream()
            ) {

                BufferedImage source =
                        ImageIO.read(
                                input
                        );


                /*
                 * Ha az ImageIO nem tudja értelmezni
                 * az adott formátumot, nem engedjük,
                 * hogy emiatt az egész PDF elbukjon.
                 */
                if (source == null) {

                    return null;
                }


                BufferedImage resized =
                        resizeForPdf(
                                source
                        );


                byte[] jpeg =
                        writeJpeg(
                                resized
                        );


                return "data:image/jpeg;base64,"
                        + Base64
                        .getEncoder()
                        .encodeToString(
                                jpeg
                        );
            }

        } catch (Exception ex) {

            /*
             * Egy sérült vagy hiányzó kép miatt
             * a teljes PDF továbbra is elkészül.
             */
            return null;
        }
    }


    private BufferedImage resizeForPdf(
            BufferedImage source
    ) {

        int sourceWidth =
                source.getWidth();


        int sourceHeight =
                source.getHeight();


        double scale =
                Math.min(
                        1.0,

                        (double) PDF_IMAGE_MAX_SIZE
                                /
                                Math.max(
                                        sourceWidth,
                                        sourceHeight
                                )
                );


        int width =
                Math.max(
                        1,

                        (int) Math.round(
                                sourceWidth
                                        * scale
                        )
                );


        int height =
                Math.max(
                        1,

                        (int) Math.round(
                                sourceHeight
                                        * scale
                        )
                );


        BufferedImage target =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );


        Graphics2D graphics =
                target.createGraphics();


        try {

            graphics.setColor(
                    Color.WHITE
            );


            graphics.fillRect(
                    0,
                    0,
                    width,
                    height
            );


            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );


            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );


            graphics.drawImage(
                    source,
                    0,
                    0,
                    width,
                    height,
                    null
            );

        } finally {

            graphics.dispose();
        }


        return target;
    }


    private byte[] writeJpeg(
            BufferedImage image
    ) throws Exception {

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName(
                        "jpeg"
                );


        if (!writers.hasNext()) {

            throw new IllegalStateException(
                    "Nem található JPEG ImageWriter."
            );
        }


        ImageWriter writer =
                writers.next();


        try (
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                ImageOutputStream imageOutput =
                        ImageIO.createImageOutputStream(
                                output
                        )
        ) {

            writer.setOutput(
                    imageOutput
            );


            ImageWriteParam param =
                    writer.getDefaultWriteParam();


            if (
                    param.canWriteCompressed()
            ) {

                param.setCompressionMode(
                        ImageWriteParam.MODE_EXPLICIT
                );


                param.setCompressionQuality(
                        PDF_JPEG_QUALITY
                );
            }


            writer.write(
                    null,

                    new IIOImage(
                            image,
                            null,
                            null
                    ),

                    param
            );


            imageOutput.flush();


            return output.toByteArray();

        } finally {

            writer.dispose();
        }
    }


    // =====================================================
    // MŰHELY ELÉRHETŐSÉGEK
    // =====================================================

    private String buildWorkshopContact(
            WorkshopSettingsDto settings
    ) {

        StringBuilder result =
                new StringBuilder();


        appendContactLine(
                result,
                settings.address()
        );


        appendContactLine(
                result,
                settings.phone()
        );


        appendContactLine(
                result,
                settings.email()
        );


        if (
                settings.taxNumber() != null &&
                        !settings.taxNumber().isBlank()
        ) {

            appendContactLine(
                    result,

                    "Adószám: "
                            + settings.taxNumber()
            );
        }


        return result.toString();
    }


    private void appendContactLine(
            StringBuilder builder,
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return;
        }


        if (!builder.isEmpty()) {

            builder.append(
                    "<br/>"
            );
        }


        builder.append(
                e(value)
        );
    }


    // =====================================================
    // MUNKADÍJ
    // =====================================================

    private String buildLaborRows(
            WorkOrder workOrder
    ) {

        if (
                workOrder
                        .getLaborItems()
                        .isEmpty()
        ) {

            return """
                    <tr>

                        <td colspan="4">
                            Nincs rögzített munkadíj.
                        </td>

                    </tr>
                    """;
        }


        StringBuilder rows =
                new StringBuilder();


        for (
                LaborItem item
                : workOrder.getLaborItems()
        ) {

            rows.append(
                    """
                    <tr>

                        <td>
                            %s
                        </td>


                        <td class="num">
                            %s óra
                        </td>


                        <td class="num">
                            %s
                        </td>


                        <td class="num">
                            %s
                        </td>

                    </tr>
                    """.formatted(

                            e(
                                    item.getDescription()
                            ),

                            number(
                                    item.getHours()
                            ),

                            money(
                                    item.getHourlyRate()
                            ),

                            money(
                                    item.calculateTotal()
                            )
                    )
            );
        }


        return rows.toString();
    }


    // =====================================================
    // ALKATRÉSZ
    // =====================================================

    private String buildPartRows(
            WorkOrder workOrder
    ) {

        if (
                workOrder
                        .getPartItems()
                        .isEmpty()
        ) {

            return """
                    <tr>

                        <td colspan="5">
                            Nincs rögzített alkatrész.
                        </td>

                    </tr>
                    """;
        }


        StringBuilder rows =
                new StringBuilder();


        for (
                PartItem item
                : workOrder.getPartItems()
        ) {

            rows.append(
                    """
                    <tr>

                        <td>
                            %s
                        </td>


                        <td>
                            %s
                        </td>


                        <td class="num">
                            %s
                        </td>


                        <td class="num">
                            %s
                        </td>


                        <td class="num">
                            %s
                        </td>

                    </tr>
                    """.formatted(

                            e(
                                    item.getDescription()
                            ),

                            e(
                                    item.getSku()
                            ),

                            number(
                                    item.getQuantity()
                            ),

                            money(
                                    item.getUnitPrice()
                            ),

                            money(
                                    item.calculateTotal()
                            )
                    )
            );
        }


        return rows.toString();
    }


    // =====================================================
    // STÁTUSZ
    // =====================================================

    private String statusLabel(
            WorkOrderStatus status
    ) {

        if (status == null) {

            return "—";
        }


        return switch (status) {

            case OPEN ->
                    "Nyitott";

            case IN_PROGRESS ->
                    "Javítás alatt";

            case WAITING_PARTS ->
                    "Alkatrészre vár";

            case READY_FOR_PICKUP ->
                    "Átadásra kész";

            case COMPLETED ->
                    "Átadva / lezárva";
        };
    }


    // =====================================================
    // DÁTUM
    // =====================================================

    private String date(
            Instant instant
    ) {

        if (instant == null) {

            return "—";
        }


        return DATE_TIME.format(
                instant
        );
    }


    // =====================================================
    // INTEGER
    // =====================================================

    private String integer(
            Integer value
    ) {

        if (value == null) {

            return "—";
        }


        NumberFormat format =
                NumberFormat.getIntegerInstance(
                        HU
                );


        return format.format(
                value
        );
    }


    // =====================================================
    // DECIMÁLIS SZÁM
    // =====================================================

    private String number(
            BigDecimal value
    ) {

        if (value == null) {

            return "—";
        }


        return value
                .stripTrailingZeros()
                .toPlainString();
    }


    // =====================================================
    // PÉNZ
    // =====================================================

    private String money(
            BigDecimal value
    ) {

        if (value == null) {

            return "—";
        }


        NumberFormat format =
                NumberFormat.getIntegerInstance(
                        HU
                );


        BigDecimal rounded =
                value.setScale(
                        0,
                        RoundingMode.HALF_UP
                );


        return format.format(
                rounded
        ) + " Ft";
    }


    // =====================================================
    // HTML ESCAPE
    // =====================================================

    private String e(
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return "—";
        }


        return value
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}