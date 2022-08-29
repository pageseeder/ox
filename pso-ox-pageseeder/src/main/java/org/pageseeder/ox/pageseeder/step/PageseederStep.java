/*
 * Copyright 2022 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.pageseeder.step;

import net.pageseeder.app.simple.vault.*;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.bridge.PSConfig;
import org.pageseeder.bridge.berlioz.auth.AuthException;
import org.pageseeder.bridge.berlioz.auth.PSAuthenticator;
import org.pageseeder.bridge.berlioz.auth.PSUser;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.util.StepUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Pageseeder step.
 *
 * @author ccabral
 * @since 29 August 2022
 */
public abstract class PageseederStep implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(PageseederStep.class);

  /**
   * Get the parameter 'psconfig' from step definition, if it is not found then gets from the request parameter.
   * Otherwise, returns the defaults.
   *
   * @param data PackageData
   * @param info StepInfo
   * @return ps config parameter
   */
  protected String getPSConfigParameter(PackageData data, StepInfo info) {
    return StepUtils.getParameter(data, info, "psconfig", VaultUtils.getDefaultPSOAuthConfigName());
  }

  /**
   * Gets tokens vault item.
   *
   * @param data the data
   * @param info the info
   * @return the tokens vault item
   */
  protected TokensVaultItem getTokensVaultItem(PackageData data, StepInfo info) {
    String psconfigName = this.getPSConfigParameter(data, info);
    return this.getTokensVaultItem(psconfigName);
  }

  /**
   * Gets tokens vault item.
   *
   * @param psconfigName the psconfig name
   * @return the tokens vault item
   */
  protected TokensVaultItem getTokensVaultItem(String psconfigName) {
    return TokensVaultManager.get(psconfigName);
  }

  /**
   * Gets pso auth config.
   *
   * @param data the data
   * @param info the info
   * @return the pso auth config
   */
  protected PSOAuthConfig getPSOAuthConfig(PackageData data, StepInfo info) {
    String psconfigName = this.getPSConfigParameter(data, info);
    return getPSOAuthConfig(psconfigName);
  }

  /**
   * Gets pso auth config.
   *
   * @param psconfigName the psconfig name
   * @return the pso auth config
   */
  protected PSOAuthConfig getPSOAuthConfig(String psconfigName) {
    return PSOAuthConfigManager.get(psconfigName);
  }

  /**
   * Gets ps authenticator.
   *
   * @param psConfig the ps config
   * @return the ps authenticator
   */
  protected PSAuthenticator getPSAuthenticator (PSConfig psConfig) {
    PSAuthenticator psAuthenticator = new PSAuthenticator();
    //If the group filter is not set to null, then it loads the membership and this step does need it.
    psAuthenticator.setGroupFilter(null);
    //Set which PS config it will use.
    psAuthenticator.setConfig(psConfig);
    return psAuthenticator;
  }


  /**
   * Gets Bridge admin user.
   * It must be set in the config-{mode}.xml
   *
   * @param psconfigName    the psconfig name
   * @param psAuthenticator the ps authenticator
   * @return the bridge admin user
   * @throws AuthException the auth exception
   */
  protected PSUser getAdminUser(String psconfigName, PSAuthenticator psAuthenticator) throws AuthException {
    PSUser user = null;
    StringBuffer authProperties = new StringBuffer("bridge.");
    //If it is not the default, then append its name to the property
    if (!VaultUtils.getDefaultPSOAuthConfigName().equalsIgnoreCase(psconfigName)) {
      authProperties.append(psconfigName);
      authProperties.append(".");
    }
    authProperties.append("admin");

    //GET username and password the GlobalSettings
    String property = authProperties.toString();
    String username = GlobalSettings.get(property + ".username");
    String password = GlobalSettings.get(property + ".password");

    user = psAuthenticator.login(username, password);
    if (user == null) {
      LOGGER.error("User config property '{}' not setup - results in null user", property);
      throw new SecurityException("Unable to retrieve the user to call the publish script.");
    }
    return user;
  }


  /**
   * Logout Bridge admin user.
   *
   * @param user            the bridge admin user
   * @param psAuthenticator the ps authenticator
   * @throws AuthException the auth exception
   */
  protected void logout(PSUser user, PSAuthenticator psAuthenticator) throws AuthException {
    psAuthenticator.logoutUser(user);
  }
}
