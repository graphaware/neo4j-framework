package com.graphaware.runtime.metadata;

/**
 * Default production implementation of {@link com.graphaware.runtime.metadata.TimerDrivenModuleMetadata}.
 */
public class DefaultTimerDrivenModuleMetadata implements TimerDrivenModuleMetadata {

    private final TimerDrivenModuleContext context;

    /**
     * Construct new metadata.
     *
     * @param context module context held by the metadata. Can be null in case it is unknown (first run).
     */
    public DefaultTimerDrivenModuleMetadata(TimerDrivenModuleContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleContext<?> lastContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultTimerDrivenModuleMetadata that = (DefaultTimerDrivenModuleMetadata) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return context != null ? context.hashCode() : 0;
    }
}
