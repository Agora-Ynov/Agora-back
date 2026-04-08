package com.agora.config.seed;

import com.agora.entity.resource.Resource;
import com.agora.enums.resource.AccessibilityTag;
import com.agora.enums.resource.ResourceType;
import com.agora.repository.resource.ResourceRepository;

import java.util.List;

final class SeedResourcesHelper {

    /**
     * Image de démo (salle de réunion) — alignée sur {@code V202604080002__update_resource_image_urls_quebec_cite.sql},
     * utilisée seulement à la création de la ligne.
     */
    private static final String IMG_SEED_DEFAULT =
            "https://meetings.quebec-cite.com/sites/qda/files/styles/gallery_desktop/public/media/image/chateau-laurier-reunion.jpg?itok=KUwS7GbQ";

    private final ResourceRepository resourceRepository;

    SeedResourcesHelper(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    void ensureResources() {
        ensureResource(
                SeedConstants.RESOURCE_GRANDE_SALLE,
                ResourceType.IMMOBILIER,
                250,
                "Grande salle pour événements jusqu'à 250 personnes",
                15000,
                IMG_SEED_DEFAULT,
                List.of(AccessibilityTag.PMR_ACCESS, AccessibilityTag.PARKING, AccessibilityTag.SOUND_SYSTEM)
        );

        ensureResource(
                SeedConstants.RESOURCE_PETITE_SALLE,
                ResourceType.IMMOBILIER,
                80,
                "Salle polyvalente pour réunions et activités associatives",
                8000,
                IMG_SEED_DEFAULT,
                List.of(AccessibilityTag.PMR_ACCESS)
        );

        ensureResource(
                SeedConstants.RESOURCE_VIDEO_PROJECTEUR,
                ResourceType.MOBILIER,
                null,
                "Vidéoprojecteur portable avec câble HDMI",
                5000,
                IMG_SEED_DEFAULT,
                List.of()
        );

        ensureResource(
                SeedConstants.RESOURCE_CHAISES,
                ResourceType.MOBILIER,
                null,
                "Lot de 50 chaises pliantes — retrait en mairie",
                3000,
                IMG_SEED_DEFAULT,
                List.of()
        );
    }

    private void ensureResource(
            String name,
            ResourceType type,
            Integer capacity,
            String description,
            int depositAmountCents,
            String imageUrl,
            List<AccessibilityTag> tags
    ) {
        Resource existing = resourceRepository.findByNameIgnoreCase(name).orElse(null);
        boolean created = existing == null;
        if (existing == null) {
            existing = new Resource();
            existing.setName(name);
        }

        existing.setResourceType(type);
        existing.setCapacity(capacity);
        existing.setDescription(description);
        existing.setDepositAmountCents(depositAmountCents);
        if (created) {
            existing.setImageUrl(imageUrl);
        }
        existing.setAccessibilityTags(tags);
        existing.setActive(true);

        resourceRepository.save(existing);
    }
}

