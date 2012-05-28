/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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
//jeffmc: change the package name to a ramadda package
//package ldap;
package org.ramadda.repository.auth.ldap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


/**
 * Class description
 *
 *
 * @version        $version$, Sun, Nov 20, '11
 * @author         Enter your name here...    
 */
public class LDAPManager {

    /** The OU (organizational unit) to add users to */
    private static String USERS_OU = new String();

    /** The OU (organizational unit) to add groups to */
    private static String GROUPS_OU = new String();

    /** The OU (organizational unit) to add permissions to */
    private static final String PERMISSIONS_OU =
        "ou=Permissions,o=forethought.com";

    /** The default LDAP port */
    private static final int DEFAULT_PORT = 389;

    /** The LDAPManager instance object */
    private static Map instances = new HashMap();

    /** The connection, through a <code>DirContext</code>, to LDAP */
    private DirContext context;

    /** The hostname connected to */
    private String hostname;

    /** The port connected to */
    private int port;

    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     * @param users_ou _more_
     * @param groups_ou _more_
     * @param username _more_
     * @param password _more_
     *
     * @throws NamingException _more_
     */
    protected LDAPManager(String hostname, int port, String users_ou,
                          String groups_ou, String username, String password)
            throws NamingException {

        context = getInitialContext(hostname, port, username, password);

        // Only save data if we got connected
        this.hostname  = hostname;
        this.port      = port;
        this.USERS_OU  = users_ou;
        this.GROUPS_OU = groups_ou;
    }

    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     * @param users_ou _more_
     * @param groups_ou _more_
     * @param username _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public static LDAPManager getInstance(String hostname, int port,
                                          String users_ou, String groups_ou,
                                          String username, String password)
            throws NamingException {

        // Construct the key for the supplied information
        String key = new StringBuffer().append(hostname).append(":").append(
                         port).append("|").append(((username == null)
                ? ""
                : username)).append("|").append(((password == null)
                ? ""
                : password)).toString();

        if ( !instances.containsKey(key)) {
            synchronized (LDAPManager.class) {
                if ( !instances.containsKey(key)) {
                    LDAPManager instance = new LDAPManager(hostname, port,
                                               users_ou, groups_ou, username,
                                               password);
                    instances.put(key, instance);
                    return instance;
                }
            }
        }

        return (LDAPManager) instances.get(key);
    }

    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public static LDAPManager getInstance(String hostname, int port)
            throws NamingException {
        return getInstance(hostname, port, null, null, null, null);
    }

    /**
     * _more_
     *
     * @param hostname _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public static LDAPManager getInstance(String hostname)
            throws NamingException {
        return getInstance(hostname, DEFAULT_PORT, null, null, null, null);
    }

    /**
     * _more_
     *
     * @param username _more_
     * @param password _more_
     *
     * @return _more_
     */
    public boolean isValidUser(String username, String password) {
        try {
            DirContext context = getInitialContext(hostname, port,
                                     getUserDN(username), password);
            return true;
        } catch (javax.naming.NameNotFoundException e) {
            return false;
        } catch (NamingException e) {
            // Any other error indicates couldn't log user in
            return false;
        }
    }

    /**
     * _more_
     *
     * @param username _more_
     * @param groupName _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public boolean userInGroup(String username, String groupName)
            throws NamingException {

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = "uniqueMember";

        Attributes attributes = context.getAttributes(getGroupDN(groupName),
                                    searchAttributes);
        if (attributes != null) {
            Attribute memberAtts = attributes.get("uniqueMember");
            if (memberAtts != null) {
                for (NamingEnumeration vals = memberAtts.getAll();
                        vals.hasMoreElements(); ) {
                    if (username.equalsIgnoreCase(
                            getUserUID((String) vals.nextElement()))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
     * Get the attributes defined by the variable userAttributes and their values
     *
     * @param username          Name of the user
     *
     * @return userAttributes   Hashtable keys are the attributes names and
     *                          hashtable values are the attribute values
     *
     */

    /**
     * _more_
     *
     * @param username _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public Hashtable getUserAttributes(String username)
            throws NamingException {

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = "uniqueMember";

        Hashtable  userAttributes  = new Hashtable();
        List       attributeValues = new ArrayList();

        Attributes attributes = context.getAttributes(getUserDN(username));
        if (attributes != null) {
            for (NamingEnumeration ae = attributes.getAll(); ae.hasMore(); ) {
                Attribute attr = (Attribute) ae.next();
                System.out.println("attribute: " + attr.getID());
                if (attr.getID().equals("givenName")
                        || attr.getID().equals("sn")) {
                    /* Print each value */
                    for (NamingEnumeration e = attr.getAll(); e.hasMore(); ) {
                        String value = new String((String) e.next());
                        attributeValues.add(value);
                        System.out.println("value: " + value);
                    }
                    userAttributes.put((String) attr.getID(),
                                       attributeValues);
                    attributeValues = new ArrayList();
                }
            }
            return userAttributes;
        }
        return null;
    }

    /**
     * _more_
     *
     * @param groupName _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public List getMembers(String groupName) throws NamingException {
        List members = new LinkedList();

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = "uniqueMember";

        Attributes attributes = context.getAttributes(getGroupDN(groupName),
                                    searchAttributes);
        if (attributes != null) {
            Attribute memberAtts = attributes.get("uniqueMember");
            if (memberAtts != null) {
                for (NamingEnumeration vals = memberAtts.getAll();
                        vals.hasMoreElements();
                        members.add(getUserUID((String) vals.nextElement())));
            }
        }

        return members;
    }

    /**
     * _more_
     *
     * @param username _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public List getGroups(String username) throws NamingException {
        List groups = new LinkedList();

        // Set up criteria to search on
        String filter = new StringBuffer().append("(&").append(
                            "(objectClass=groupOfForethoughtNames)").append(
                            "(uniqueMember=").append(
                            getUserDN(username)).append(")").append(
                            ")").toString();

        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.ONELEVEL_SCOPE);

        NamingEnumeration results = context.search(GROUPS_OU, filter, cons);

        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            groups.add(getGroupCN(result.getName()));
        }

        return groups;
    }

    /**
     * _more_
     *
     * @param groupName _more_
     * @param permissionName _more_
     *
     * @throws NamingException _more_
     */
    public void revokePermission(String groupName, String permissionName)
            throws NamingException {

        try {
            ModificationItem[] mods = new ModificationItem[1];

            Attribute mod = new BasicAttribute("uniquePermission",
                                getPermissionDN(permissionName));
            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);
            context.modifyAttributes(getGroupDN(groupName), mods);
        } catch (NoSuchAttributeException e) {
            // Ignore errors if the attribute doesn't exist
        }
    }

    /**
     * _more_
     *
     * @param groupName _more_
     * @param permissionName _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public boolean hasPermission(String groupName, String permissionName)
            throws NamingException {

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = "uniquePermission";

        Attributes attributes = context.getAttributes(getGroupDN(groupName),
                                    searchAttributes);
        if (attributes != null) {
            Attribute permAtts = attributes.get("uniquePermission");
            if (permAtts != null) {
                for (NamingEnumeration vals = permAtts.getAll();
                        vals.hasMoreElements(); ) {
                    if (permissionName.equalsIgnoreCase(
                            getPermissionCN((String) vals.nextElement()))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * _more_
     *
     * @param groupName _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public List getPermissions(String groupName) throws NamingException {
        List permissions = new LinkedList();

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = "uniquePermission";

        Attributes attributes = context.getAttributes(getGroupDN(groupName),
                                    searchAttributes);
        if (attributes != null) {
            Attribute permAtts = attributes.get("uniquePermission");
            if (permAtts != null) {
                for (NamingEnumeration vals = permAtts.getAll();
                        vals.hasMoreElements();
                        permissions.add(
                            getPermissionCN((String) vals.nextElement())));
            }
        }

        return permissions;
    }

    /**
     * _more_
     *
     * @param username _more_
     *
     * @return _more_
     */
    private String getUserDN(String username) {
        return new StringBuffer().append("uid=").append(username).append(
            ",").append(USERS_OU).toString();
    }

    /**
     * _more_
     *
     * @param userDN _more_
     *
     * @return _more_
     */
    private String getUserUID(String userDN) {
        int start = userDN.indexOf("=");
        int end   = userDN.indexOf(",");

        if (end == -1) {
            end = userDN.length();
        }

        return userDN.substring(start + 1, end);
    }

    /**
     * _more_
     *
     * @param username _more_
     *
     * @return _more_
     */
    private String getUserCN(String username) {
        return new StringBuffer().append("cn=").append(username).append(
            ",").append(USERS_OU).toString();
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    private String getGroupDN(String name) {
        return new StringBuffer().append("cn=").append(name).append(
            ",").append(GROUPS_OU).toString();
    }

    /**
     * _more_
     *
     * @param groupDN _more_
     *
     * @return _more_
     */
    private String getGroupCN(String groupDN) {
        int start = groupDN.indexOf("=");
        int end   = groupDN.indexOf(",");

        if (end == -1) {
            end = groupDN.length();
        }

        return groupDN.substring(start + 1, end);
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    private String getPermissionDN(String name) {
        return new StringBuffer().append("cn=").append(name).append(
            ",").append(PERMISSIONS_OU).toString();
    }

    /**
     * _more_
     *
     * @param permissionDN _more_
     *
     * @return _more_
     */
    private String getPermissionCN(String permissionDN) {
        int start = permissionDN.indexOf("=");
        int end   = permissionDN.indexOf(",");

        if (end == -1) {
            end = permissionDN.length();
        }

        return permissionDN.substring(start + 1, end);
    }

    /**
     * _more_
     *
     * @param hostname _more_
     * @param port _more_
     * @param username _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    private DirContext getInitialContext(String hostname, int port,
                                         String username, String password)
            throws NamingException {

        String providerURL = new StringBuffer("ldap://").append(
                                 hostname).append(":").append(
                                 port).toString();

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                  "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, providerURL);

        if ((username != null) && ( !username.equals(""))) {
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, ((password == null)
                    ? ""
                    : password));
        }

        return new InitialDirContext(props);
    }
}
