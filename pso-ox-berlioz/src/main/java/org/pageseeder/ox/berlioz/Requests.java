/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.ox.berlioz;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A utility class to ensure that requests are handled uniformly.
 *
 * @author Christophe Lauret
 * @version 25 October 2013
 */
public final class Requests {

  /**
   *  Utility.
   */
  private Requests() {}

  /**
   * Returns the package data form the "id" parameter.
   *
   * <p>If the returned value is <code>null</code> the generator should return immediately.
   *
   * @param req The content request.
   * @param xml The XML to write errors only.
   *
   * @return the package data or <code>null</code>.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  public static PackageData getPackageData(ContentRequest req, XMLWriter xml) throws IOException {
    String id = req.getParameter("id");
    PackageData data = PackageData.getPackageData(id);
    if (data == null) {
      Errors.invalidParameter(req, xml, "id");
    }
    return data;
  }

  /**
   * Returns the specified model, if cannot find from request parameter [model], default one will be selected [the first one].
   *
   * <p>If the returned value is <code>null</code> the generator should return immediately.
   *
   * @param req The content request.
   * @param xml The XML to write errors only.
   *
   * @return the model <code>null</code>.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  public static Model getModel(ContentRequest req, XMLWriter xml) throws IOException {
    ensureConfigured();
    boolean isReload = "true".equals(req.getParameter("ox-reload", "false")) ? true : false;

    String name = req.getParameter("model");
    // Try model specified in request
    if (name != null && !name.isEmpty()) {
      if (Model.isDefined(name)) {
        return new Model(name);
      } else {
        Errors.invalidParameter(req, xml, "model");
        return null;
      }
    }
    // Revert to default model
    Model model = Model.getDefault();
    if (model == null) {
      Errors.invalidParameter(req, xml, "model");
      return null;
    } else {
      if (isReload) {
        model.reload();
      }
    }
    return model;
  }

  /**
   * Return the list of models.
   *
   * @param req The content request.
   * @param xml the xml
   * @return the list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List<Model> listModel(ContentRequest req, XMLWriter xml) throws IOException {
    ensureConfigured();
    boolean isReload = "true".equals(req.getParameter("ox-reload", "false")) ? true : false;
    List<Model> models = Model.listModels(isReload);
    return models;

  }

  /**
   * Ensure the configuration file is set.
   */
  public static void ensureConfigured() {
    OXConfig config = OXConfig.get();
    File dir = config.getModelsDirectory();
    if (dir == null) {
      config.setModelsDirectory(new File(GlobalSettings.getAppData(), "model"));
    }
  }

}
