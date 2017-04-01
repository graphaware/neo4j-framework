package com.graphaware.common.policy.role;

/**
 * {@link InstanceRolePolicy} with which only {@link InstanceRole#MASTER} complies.
 * I.e., a module with this policy will run only on the master in HA setup. Not it will also run in single instance setup.
 * Singleton.
 */
public final class MasterOnly implements InstanceRolePolicy {

    private static final MasterOnly INSTANCE = new MasterOnly();

    private MasterOnly() {
    }

    public static MasterOnly getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean comply(InstanceRole role) {
        return InstanceRole.MASTER.equals(role) || InstanceRole.SINGLE.equals(role);
    }
}
