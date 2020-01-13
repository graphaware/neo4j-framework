package com.graphaware.runtime.metadata;

import com.graphaware.runtime.module.RuntimeModule;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DummyModuleMetadataRepository implements ModuleMetadataRepository {

    private final ConcurrentMap<String, ModuleMetadata> metadata;

    public DummyModuleMetadataRepository() {
        this.metadata = new ConcurrentHashMap<>();
    }

    @Override
    public <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule module) {
        return (M) metadata.get(module.getId());
    }

    @Override
    public <M extends ModuleMetadata> M getModuleMetadata(String moduleId) {
        return (M) metadata.get(moduleId);
    }

    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule module, M metadata) {
        this.metadata.put(module.getId(), metadata);
    }

    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(String moduleId, M metadata) {
        this.metadata.put(moduleId, metadata);
    }

    @Override
    public Set<String> getAllModuleIds() {
        return metadata.keySet();
    }

    @Override
    public void removeModuleMetadata(String moduleId) {
        metadata.remove(moduleId);
    }
}
