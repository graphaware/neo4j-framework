package com.graphaware.crawler.api;

/**
 * Defines the methods that are invoked when the crawler finds some nodes or relationships or whatever that the user should be
 * notified about.
 */
public interface ThingThatGetsCalledWhenWeFindSomething {

	/**
	 * Does some stuff given the current {@link Context} of the background traversal.
	 *
	 * @param context The {@link Context} that encapsulates information about the stage of the background traversal process
	 */
	void doSomeStuff(Context context);

}
