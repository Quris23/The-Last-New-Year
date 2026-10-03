package dev.qur1s.spellclasses.compat;

import java.util.Map;
import java.util.UUID;

/** Extra access to a Rift Gate block entity, implemented by {@code RiftGateBlockEntityCrossMixin}. */
public interface CrossGateAccess {
    void spellclasses$setGateId(UUID id);

    Map<UUID, Integer> spellclasses$settling();

    boolean spellclasses$isNetherGate();
}
