/* Copyright (c) 2018 Allette Systems pty. ltd. */
package org.pageseeder.ox.berlioz;

import java.io.File;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.LifecycleListener;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.cleanup.CleanUpManager;
import org.pageseeder.ox.core.StepJob;

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
