package com.graphaware.common.policy.role;

/**
 * {@link InstanceRolePolicy} with which any {@link InstanceRole} complies. I.e., a module with this policy will run on any machine. Singleton.
 */
public final class AnyRole implements InstanceRolePolicy {

    private static final AnyRole INSTANCE = new AnyRole();

    public static AnyRole getInstance() {
        return INSTANCE;
    }

    private AnyRole() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean comply(InstanceRole role) {
        return true;
    }
}
