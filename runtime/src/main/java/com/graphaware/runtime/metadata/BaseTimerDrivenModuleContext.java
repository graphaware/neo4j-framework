package com.graphaware.runtime.metadata;

/**
 * Base-class for {@link TimerDrivenModuleContext} implementations.
 *
 * @param <T> type of the position representation.
 */
public abstract class BaseTimerDrivenModuleContext<T> implements TimerDrivenModuleContext<T> {

    private final long earliestNextCall;

    /**
     * Create a new context indicating the module should be called ASAP.
     */
    public BaseTimerDrivenModuleContext() {
        this(ASAP);
    }

    /**
     * Create a new context indicating when is the earliest time the module should be called.
     *
     * @param earliestNextCall in ms since 1/1/1970
     */
    public BaseTimerDrivenModuleContext(long earliestNextCall) {
        this.earliestNextCall = earliestNextCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long earliestNextCall() {
        return earliestNextCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTimerDrivenModuleContext that = (BaseTimerDrivenModuleContext) o;

        if (earliestNextCall != that.earliestNextCall) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (earliestNextCall ^ (earliestNextCall >>> 32));
    }
}
