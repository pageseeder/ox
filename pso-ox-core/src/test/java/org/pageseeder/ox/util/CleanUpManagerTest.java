/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.util;

import static org.mockito.Matchers.any;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.process.PipelineJobQueue;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Carlos Cabral
 * @since 26 October 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PipelineJobQueue.class})
public class CleanUpManagerTest {

  @Test
  public void testSimpleFiles() {
    try {
      File base = new File (OXConfig.getOXTempFolder(), "test");
      //Clean folder if already exist
      if (base.exists()) FileUtils.delete(base);
      Assert.assertTrue("Attempt to Create base directory", base.mkdirs());
      CleanUpManager manager = CleanUpManager.getInstance(1, CleanUpManager.DEFAULT_DELAY, base);    


      File temp1 = new File(base, "test1.txt");
      Assert.assertTrue(temp1.createNewFile());
      File temp2 = new File(base, "test2.txt");
      Assert.assertTrue(temp2.createNewFile());
      
      //Test if file are there
      Assert.assertEquals(2, base.list().length);
      manager.start();
            
      //Waiting the job to perform its first iteraction
      while (manager.status() != CleanUpStatus.WAITING_NEXT_ITERACTION) Thread.sleep(500);
      
      Assert.assertEquals(0, base.list().length);
      manager.stop();
    } catch (IOException | InterruptedException ex) {
      Assert.fail(ex.getMessage());
    }
  }
  
  @Test
  public void testJobFile() {
    try {
      final long delay = 100l;
      File base = new File (OXConfig.getOXTempFolder(), "test");
      //Clean folder if already exist
      if (base.exists()) FileUtils.delete(base);
      Assert.assertTrue("Attempt to Create base directory", base.mkdirs());
      
      
      
      //Instantiate Clean up manager
      CleanUpManager manager = CleanUpManager.getInstance(1, delay, base);
            
      File packageFolder = new File(base, "TEST-1245-123");
      Assert.assertTrue("Attempt to Create Package Directory.", packageFolder.mkdir());
      File temp1 = new File(packageFolder, "test1.txt");
      Assert.assertTrue(temp1.createNewFile());
      File temp2 = new File(packageFolder, "test2.txt");
      Assert.assertTrue(temp2.createNewFile());
      
      //Test if package file is there
      Assert.assertEquals(1, base.list().length);
      
      //Simulate Package folder in job list
      PowerMockito.mockStatic(PipelineJobQueue.class);
      PowerMockito.when(PipelineJobQueue.getJobId(any())).thenReturn("12345679");
      
      //Start Thread
      manager.start();
      
      //Waiting the job to perform its first interaction
      while (manager.status() != CleanUpStatus.WAITING_NEXT_ITERACTION) Thread.sleep(delay/2);
      
      //Test if package file is there
      Assert.assertEquals(1, base.list().length);
      
      //Remove Package folder from job List
      PowerMockito.when(PipelineJobQueue.getJobId(any())).thenReturn("");

      //Allow the the thread to run 2 time
      if (manager.status() == CleanUpStatus.WAITING_NEXT_ITERACTION) Thread.sleep(300l);
      
      //Wait to finish the last running
      while (manager.status() != CleanUpStatus.WAITING_NEXT_ITERACTION) Thread.sleep(45l);
     
      //Should not have nay file there
      Assert.assertEquals(0, base.list().length);
      manager.stop();
      Assert.assertEquals(CleanUpStatus.STOPPED, manager.status());
    } catch (Exception ex) {
      ex.printStackTrace();
      Assert.fail(ex.getMessage());
    }
  }
  
  @Test
  public void testSingleton() {
    try {
      File base = new File (OXConfig.getOXTempFolder(), "test");
      //Clean folder if already exist
      if (base.exists()) FileUtils.delete(base);
      Assert.assertTrue("Attempt to Create base directory", base.mkdirs());
      CleanUpManager manager1 = CleanUpManager.getInstance(1, 1l, base);    
      CleanUpManager manager2 = CleanUpManager.getInstance(1, 1l, base);
      
      manager1.start();
      while (manager1.status() == CleanUpStatus.NOT_STARTED) Thread.sleep(100);
      Assert.assertNotEquals(CleanUpStatus.NOT_STARTED, manager2.status());
      
      manager2.stop();
      Assert.assertEquals(CleanUpStatus.STOPPED, manager1.status());
      
      //Try to start again
      manager1.start();
      Assert.assertEquals(CleanUpStatus.STOPPED, manager2.status());
      
    } catch (IOException | InterruptedException ex) {
      Assert.fail(ex.getMessage());
    }
  }
}
