package org.jboss.hal.testsuite.page.runtime;

import org.jboss.hal.testsuite.finder.Application;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.MetricsPage;
import org.jboss.hal.testsuite.page.Navigatable;
import org.jboss.hal.testsuite.util.ConfigUtils;

/**
 * Created by mkrajcov on 4/10/15.
 */
public class WebServiceEndpointsPage extends MetricsPage implements Navigatable {
    public MetricsAreaFragment getWebServiceRequestMetricsArea() {
        return getMetricsArea("Web Service Requests");
    }

    public void navigateInDeploymentsMenu() {
        if (ConfigUtils.isDomain()) {
            FinderNavigation deployNavigation = new FinderNavigation(browser, DomainDeploymentPage.class)
                    .step(FinderNames.BROWSE_BY, FinderNames.SERVER_GROUPS)
                    .step(FinderNames.SERVER_GROUP, "main-server-group")
                    .step(FinderNames.DEPLOYMENT, "test.war");
            deployNavigation.selectRow().invoke(FinderNames.VIEW);
            Application.waitUntilVisible();

        } else {
            FinderNavigation deployNavigation = new FinderNavigation(browser, DeploymentPage.class)
                    .step(FinderNames.DEPLOYMENT, "test.war");
            deployNavigation.selectRow().invoke(FinderNames.VIEW);
            Application.waitUntilVisible();
        }
    }

    public void navigate() {
        FinderNavigation navigation;
        if (ConfigUtils.isDomain()) {
            navigation = new FinderNavigation(browser, DomainRuntimeEntryPoint.class)
                    .step(FinderNames.BROWSE_DOMAIN_BY, FinderNames.HOSTS)
                    .step(FinderNames.HOST, "master")
                    .step(FinderNames.SERVER, "server-one");
        } else {
            navigation = new FinderNavigation(browser, StandaloneRuntimeEntryPoint.class)
                    .step(FinderNames.SERVER, FinderNames.STANDALONE_SERVER);
        }
        navigation.step(FinderNames.MONITOR, FinderNames.SUBSYSTEMS)
                .step(FinderNames.SUBSYSTEM, "Webservices")
                .selectRow()
                .invoke(FinderNames.VIEW);
        Application.waitUntilVisible();
    }
}
