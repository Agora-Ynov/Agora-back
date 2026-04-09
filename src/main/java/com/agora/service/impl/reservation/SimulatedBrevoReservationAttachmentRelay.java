package com.agora.service.impl.reservation;

import com.agora.enums.reservation.ReservationDocType;
import com.agora.service.reservation.ReservationAttachmentRelay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulation du canal d’envoi PJ (cahier : Brevo, sans stockage binaire SQL).
 * <ul>
 *   <li>Log applicatif structuré (traçabilité exploitation / soutenance).</li>
 *   <li>Idempotence : une implémentation réelle doit utiliser
 *       {@code reservationDocumentId} comme clé d’idempotence côté APImessagerie.</li>
 *   <li>En cas d’échec réseau futur : lever une exception pour rollback transactionnel
 *       dans {@link ReservationDocumentServiceImpl}.</li>
 * </ul>
 */
@Slf4j
@Component
public class SimulatedBrevoReservationAttachmentRelay implements ReservationAttachmentRelay {

    public static final String CHANNEL = "BREVO_SIMULATED";

    @Override
    public void relayAcceptedDocument(
            UUID reservationDocumentId,
            UUID reservationId,
            String ownerEmail,
            ReservationDocType docType,
            String originalFilename,
            String mimeType,
            byte[] content
    ) {
        int bytes = content == null ? 0 : content.length;
        try {
            log.info(
                    "PJ_RELAY channel={} status=DISPATCH_SIMULATED idempotencyKey={} reservationId={} ownerEmail={} docType={} filename={} mimeType={} byteSize={}",
                    CHANNEL,
                    reservationDocumentId,
                    reservationId,
                    ownerEmail,
                    docType,
                    originalFilename,
                    mimeType,
                    bytes
            );
            log.debug(
                    "PJ_RELAY channel={} reservationDocumentId={} message=En production, appeler ici l API Brevo (piece jointe + meta).",
                    CHANNEL,
                    reservationDocumentId
            );
        } catch (RuntimeException e) {
            log.error(
                    "PJ_RELAY channel={} reservationDocumentId={} reservationId={} phase=RELAY_FAILED",
                    CHANNEL,
                    reservationDocumentId,
                    reservationId,
                    e
            );
            throw e;
        }
    }
}
