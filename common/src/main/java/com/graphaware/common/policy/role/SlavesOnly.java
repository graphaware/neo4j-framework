package com.graphaware.common.policy.role;

/**
 * {@link InstanceRolePolicy} with which only {@link InstanceRole#SLAVE} complies. I.e., a module with this policy will run only on slaves in HA setup. Singleton.
 */
public final class SlavesOnly implements InstanceRolePolicy {

    private static final SlavesOnly INSTANCE = new SlavesOnly();

    private SlavesOnly() {
    }

    public static SlavesOnly getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean comply(InstanceRole role) {
        return InstanceRole.SLAVE.equals(role);
    }
}
