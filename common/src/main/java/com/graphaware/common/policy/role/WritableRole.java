package com.graphaware.common.policy.role;

/**
 * {@link InstanceRolePolicy} with which any {@link InstanceRole} that is writable (i.e., {@link InstanceRole#isWritable()} returns <code>true</code>)
 * complies. Singleton.
 * <p>
 * A module with this policy will run only on:
 * - {@link InstanceRole#MASTER} in case of HA.
 * - {@link InstanceRole#LEADER} in case of CC.
 * - {@link InstanceRole#SINGLE} in case of no clustering.
 * <p>
 * Note that slaves in HA are technically writable but not for the purposes of this class and related framework mechanisms.
 */
public final class WritableRole implements InstanceRolePolicy {

    private static final WritableRole INSTANCE = new WritableRole();

    public static WritableRole getInstance() {
        return INSTANCE;
    }

    private WritableRole() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean comply(InstanceRole role) {
        return role.isWritable();
    }
}
