/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.*;

public class DatanucleusJdoServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setContentType("text/plain");

    Map<String, String> properties = new HashMap();
    if (SystemProperty.environment.value() ==
          SystemProperty.Environment.Value.Production) {
      properties.put("javax.jdo.option.ConnectionDriverName",
          "com.mysql.jdbc.GoogleDriver");
      properties.put("javax.jdo.option.ConnectionURL",
          System.getProperty("cloudsql.url"));
    } else {
      properties.put("javax.jdo.option.ConnectionDriverName",
          "com.mysql.jdbc.Driver");
      properties.put("javax.jdo.option.ConnectionURL",
          System.getProperty("cloudsql.url.dev"));
    }

    PersistenceManagerFactory pmf =
        JDOHelper.getPersistenceManagerFactory(properties, "Demo");

    // Insert a few rows.
    PersistenceManager pm = pmf.getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();
      pm.makePersistent(new Greeting("user", new Date(), "Hello!"));
      pm.makePersistent(new Greeting("user", new Date(), "Hi!"));
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }

    // List all the rows.
    pm = pmf.getPersistenceManager();
    tx = pm.currentTransaction();
    try {
      tx.begin();
      Extent e = pm.getExtent(Greeting.class, true);
      Iterator iter = e.iterator();
      while (iter.hasNext()) {
        Greeting g = (Greeting) iter.next();
        res.getWriter().println(
            g.getId() + " " +
            g.getAuthor() + "(" + g.getDate() + "): " +
            g.getContent());
      }
      tx.commit();
    } catch (Exception e) {
      res.getWriter().println("Exception: " + e.getMessage());
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }

    res.getWriter().println("---");

    // Another wayt to list all the rows.
    pm = pmf.getPersistenceManager();
    tx = pm.currentTransaction();
    try {
      tx.begin();
      Query q = pm.newQuery("SELECT FROM " + Greeting.class.getName());
      List<Greeting> result = (List<Greeting>) q.execute();
      for (Greeting g : result) {
        res.getWriter().println(
            g.getId() + " " +
            g.getAuthor() + "(" + g.getDate() + "): " +
            g.getContent());
      }
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }

  }
}
