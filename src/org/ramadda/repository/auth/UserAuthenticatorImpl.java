/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


import java.util.ArrayList;
import java.util.List;


/**
 * Base implementation of the UserAuthenticator interface
 *
 *
 * @author RAMADDA Development Team
 */
public abstract class UserAuthenticatorImpl implements UserAuthenticator {

    /** _more_ */
    private Repository repository;

    /**
     * _more_
     */
    public UserAuthenticatorImpl() {}


    /**
     * _more_
     *
     * @param repository _more_
     */
    public UserAuthenticatorImpl(Repository repository) {
        this.repository = repository;
    }


    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void initUsers() throws Exception {}


    /**
     * _more_
     *
     * @param repository _more_
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return this.repository;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public UserManager getUserManager() {
        return this.repository.getUserManager();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param defaultValue _more_
     *
     * @return _more_
     */
    public String getProperty(String name, String defaultValue) {
        return repository.getProperty(name, defaultValue);
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public void debug(String msg) {
        System.err.println("UserAuthenticator: " + msg);
    }

    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logError(String message, Exception exc) {
        if (repository != null) {
            repository.getLogManager().logError(message, exc);
        } else {
            System.err.println("ERROR:" + message);
            exc.printStackTrace();
        }
    }

    /**
     * this gets called when we want to just get a User object from the ID.
     * return null if user is unknown
     *
     * @param repository _more_
     * @param userId _more_
     *
     * @return _more_
     */
    public User findUser(Repository repository, String userId) {
        return null;
    }

    /**
     * this gets called when we want to autheticate the given user/password
     * return null if user/password is unknown or incorrect
     *
     * @param repository _more_
     * @param request _more_
     * @param loginFormExtra _more_
     * @param userId _more_
     * @param password _more_
     *
     * @return _more_
     */
    public abstract User authenticateUser(Repository repository,
                                          Request request,
                                          StringBuffer loginFormExtra,
                                          String userId, String password);


    /**
     * This is used to list out the roles for display in the access pages
     *
     * @return _more_
     */
    public List<String> getAllRoles() {
        return new ArrayList<String>();
    }


    /**
     * this can be used to list out all of the users and display them
     * in RAMADDA
     * It is not used by RAMADDA right now
     *
     * @return _more_
     */
    public List<User> getAllUsers() {
        return new ArrayList<User>();
    }



    /**
     * This will be used to allow this authenticator to add options
     * to the admin config form
     * Its not used right now
     *
     * @param repository _more_
     * @param sb _more_
     */
    public void addToConfigurationForm(Repository repository,
                                       StringBuffer sb) {}

    /**
     * This will be used to allow this authenticator to set the options from the config form
     * to the admin config form
     * Its not used right now
     *
     * @param repository _more_
     * @param request _more_
     */
    public void applyConfigurationForm(Repository repository,
                                       Request request) {}

}
