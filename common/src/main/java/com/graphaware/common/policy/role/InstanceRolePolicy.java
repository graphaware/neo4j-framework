package com.graphaware.common.policy.role;

/**
 * Specifies {@link InstanceRole} a machine must have in order to run the module with this configuration.
 */
public interface InstanceRolePolicy {

    /**
     * Does the given role comply with this policy? I.e., should the module run?
     *
     * @param role role of this instance.
     * @return true iff the given role complies with the policy.
     */
    boolean comply(InstanceRole role);
}
