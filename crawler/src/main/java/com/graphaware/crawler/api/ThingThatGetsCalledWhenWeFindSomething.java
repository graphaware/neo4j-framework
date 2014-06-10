package com.graphaware.crawler.api;

/**
 * Defines the methods that are invoked when the crawler finds some nodes or relationships or whatever that the user should be
 * notified about.
 */
public interface ThingThatGetsCalledWhenWeFindSomething {

	/**
	 * Does some stuff given the current {@link Context} of the background traversal.
	 * <p>
	 * If an {@link Exception} is thrown from this method then it will be handled by the framework and should cleanly abort
	 * the crawl, logging the stack trace.  It's therefore important to internally handle any exceptions internally that
	 * shouldn't result in the graph crawl being stopped.
	 * </p>
	 *
	 * @param context The {@link Context} that encapsulates information about the stage of the background traversal process
	 * @throws Exception if there is any
	 */
	void doSomeStuff(Context context) throws Exception;

}
