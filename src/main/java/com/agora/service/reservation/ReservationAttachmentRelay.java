package com.agora.service.reservation;

import com.agora.enums.reservation.ReservationDocType;

import java.util.UUID;

/**
 * Relais du fichier PJ après acceptation (validation taille, MIME, droits).
 * <p>
 * Cahier fonctionnel : zéro stockage binaire en base, envoi via un service tiers
 * (ex. Brevo) — voir {@code agora_api_endpoints_version_final.md} §4 DOCUMENTS.
 * <p>
 * <strong>Idempotence :</strong> utiliser {@code reservationDocumentId} comme clé
 * stable (une ligne {@code reservation_documents} par tentative acceptée).
 */
public interface ReservationAttachmentRelay {

    void relayAcceptedDocument(
            UUID reservationDocumentId,
            UUID reservationId,
            String ownerEmail,
            ReservationDocType docType,
            String originalFilename,
            String mimeType,
            byte[] content
    );
}
