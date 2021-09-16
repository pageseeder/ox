/*
 * Copyright 2021 Allette Systems (Australia)
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
package org.pageseeder.ox.berlioz;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.LifecycleListener;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.cleanup.CleanUpManager;
import org.pageseeder.ox.core.StepJob;

import java.io.File;

/**
 * The life cycle listener for OX.
 *
 * - Start the Clean Up Manager: Responsible to delete the files creadted by OX
 *
 * @author Carlos Cabral
 * @since 29 October 2018
 */
public final class OXLifecycle implements LifecycleListener {

  @Override
  public boolean start() {
    File packagesRootFolder = OXConfig.getOXTempFolder();
    long maxInactiveTimeAllowed = Long.parseLong(GlobalSettings.get("ox2.max-inactive-time-ms", String.valueOf(StepJob.DEFAULT_MAX_INACTIVE_TIME_MS)));
    CleanUpManager cleanUpManager = CleanUpManager.getInstance(maxInactiveTimeAllowed, CleanUpManager.DEFAULT_DELAY, packagesRootFolder);
    cleanUpManager.addFileToIgnore(OXConfig.getOXTempUploadFolder());
    cleanUpManager.start();

    // clearCache();
    return true;
  }

  @Override
  public boolean stop() {
    CleanUpManager cleanUpManager = CleanUpManager.getInstance();
    if (cleanUpManager != null) cleanUpManager.stop();

    return true;
  }

}
