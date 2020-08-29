/*
 * Copyright (c) 2020 MAHun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Based on the code by Mark A. Hunter at
 * https://github.com/fhirfactory/pegacorn-communicate-iris/tree/master/src/main/java/net/fhirfactory/pegacorn/communicate/iris/utilities
 * and updated for Infinispan 10.x
 * 
 * This class creates the clustered cache manager and configures the shared cache.
 * 
 */
package net.fhirfactory.pegacorn.petasos.core.moa.resilience.cluster.utility;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.enterprise.context.ApplicationScoped;
import org.infinispan.manager.DefaultCacheManager;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;

@ApplicationScoped
public class PetasosCacheManager {

	private DefaultCacheManager petasosCacheManager;

	@Produces
	public DefaultCacheManager getCacheManager() {
		if (petasosCacheManager == null) {
			// configure a named clustered cache configuration using Infinispan defined
			// defaults
			GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder().clusteredDefault();

			// complete the config with a cluster name, jgroups config, and enable JMX
			// statistics
			GlobalConfiguration global = builder.transport().clusterName("petasos-cluster")
					.addProperty("configurationFile", "jgroups-petasos.xml").jmx().enable().build();

			// define a local configuration for setting finer level properties including
			// individual cache statistics and methods required for configuring the cache
			// as clustered
			//            Configuration local = new ConfigurationBuilder().statistics().enable().clustering()
			//                    .cacheMode(CacheMode.DIST_SYNC).build();
			// note the doco for each of the persistence methods is poor so I have just
			// copied the
			// persistence config from
			// https://infinispan.org/docs/stable/titles/configuring/configuring.html#configuring_cache_stores-persistence
			// not sure about preload effect when starting a new pod - could out of date
			// info clobber newer info, see
			// https://docs.jboss.org/infinispan/10.1/apidocs/org/infinispan/configuration/cache/AbstractStoreConfigurationBuilder.html#preload(boolean)
			/* Configuration local = new ConfigurationBuilder().statistics().enable().clustering()
					.cacheMode(CacheMode.DIST_SYNC).persistence().passivation(true) // only write the cache overflow to
																					// disk
					.addSingleFileStore() // the disk cache
					.preload(true).shared(false).fetchPersistentState(true).ignoreModifications(false)
					.purgeOnStartup(false).location(petasosProperties.getCacheOverflowDirectory()).async().enabled(true)
					.threadPoolSize(5).memory()
//               .storageType(StorageType.BINARY) // let Infinispan decide?
					.evictionType(EvictionType.MEMORY).size(petasosProperties.getCacheSizeInBytes()) // cache size in
																										// bytes, need
																										// to make
																										// configurable
					.build(); */

			// create a cache manager based on the gloabl configuration
			/* petasosCacheManager = new DefaultCacheManager(global);
			// define a set of caches based on the local configuration
			petasosCacheManager.defineConfiguration("petasos-parcel-cache", local);
			petasosCacheManager.defineConfiguration("petasos-watchdog-cache", "petasos-parcel-cache", local);
			petasosCacheManager.defineConfiguration("petasos-uow-to-wup-map", "petasos-parcel-cache", local);
			petasosCacheManager.defineConfiguration("capability-map", "petasos-parcel-cache", local); */
		}
		return petasosCacheManager;
	}

	@PreDestroy
	public void cleanUp() {
		petasosCacheManager.stop();
		petasosCacheManager = null;
	}
}