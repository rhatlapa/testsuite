package org.jboss.hal.testsuite.page.runtime;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.BaseFragment;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentContentRepositoryArea;
import org.jboss.hal.testsuite.fragment.runtime.DeploymentServerGroupArea;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author jcechace
 */
@Location("#domain-deployments")
public class DomainDeploymentPage extends BasePage {

    public DeploymentContentRepositoryArea switchToContentRepository() {
        switchTab("Content Repository");
        return getDeploymentContent(DeploymentContentRepositoryArea.class);
    }

    public DeploymentServerGroupArea switchToServerGroup(String serverGroup) {
        switchTab("Server Groups");
        WebElement backAnchor = getContentRoot().findElement(By.xpath(".//a[text()='Back']"));
        if (backAnchor.isDisplayed()) {
            backAnchor.click();
        }
        getResourceManager().viewByName(serverGroup);
        return getDeploymentContent(DeploymentServerGroupArea.class);
    }

    public <T extends BaseFragment> T getDeploymentContent(Class<T> clazz) {
        WebElement content = getContentRoot().findElement(ByJQuery.selector("." + PropUtils.get("page.content.rhs.class") + ":visible"));
        return Graphene.createPageFragment(clazz, content);
    }
}
