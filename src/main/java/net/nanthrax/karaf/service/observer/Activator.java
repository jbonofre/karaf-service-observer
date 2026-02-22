package net.nanthrax.karaf.service.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ServiceTracker<DataSource, DataSource> dataSourceTracker = null;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        logger.info("Starting Karaf DataSource service observer ...");
        
        ServiceTracker<DataSource, DataSource> dataSourceTracker = new ServiceTracker<DataSource, DataSource>(bundleContext, DataSource.class, null) {
            @Override
            public DataSource addingService(ServiceReference<DataSource> reference) {
                logger.info("New DataSource service detected {}", reference.toString());
                return bundleContext.getService(reference);
            }
            @Override
            public void modifiedService(ServiceReference<DataSource> reference, DataSource service) {
                logger.info("Datasource {} modified", reference.toString());

                if (reference.getUsingBundles() != null && reference.getUsingBundles().length > 0) {
                    for (Bundle bundle : reference.getUsingBundles()) {
                        logger.info("\tBundle {} ({}) is using the datasource", bundle.getSymbolicName(), bundle.getBundleId());
                        logger.info("\t\trefreshing bundle ...");
                        try {
                            bundle.update();
                            logger.info("\t\tbundle refreshed");
                        } catch (BundleException e) {
                            logger.error("Error refreshing bundle {} ({}): ", bundle.getSymbolicName(), bundle.getBundleId(), e);
                        }
                    }
                }
                
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'modifiedService'");
            }
            @Override
            public void removedService(ServiceReference<DataSource> reference, DataSource service) {
                logger.info("Datasource {} removed", reference.toString());
            }
        };
        dataSourceTracker.open();

    }
    
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (dataSourceTracker != null) {
            logger.info("Stopping Karaf DataSource service observer ...");
            dataSourceTracker.close();
        }
    }
}