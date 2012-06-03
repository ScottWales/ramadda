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
package org.ramadda.plugins.ldap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.InvalidNameException;
import javax.naming.NameClassPair;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


/**
 * Does the work of communicating with the ldap server
 *
 */
public class LDAPManager {

    /** The LDAPManager instance object */
    private static Map instances = new HashMap();

    private String ldapUrl;

    private String username;

    private String password;


    private String userPath;

    private String groupsPath;



    /** The connection, through a <code>DirContext</code>, to LDAP */
    private DirContext theContext;



    /**
     * _more_
     *
     * @param username _more_
     * @param password _more_
     *
     * @throws NamingException _more_
     */
    protected LDAPManager(String ldapUrl, String userPath,
                          String groupsPath, String username, String password)
            throws NamingException {
        this.ldapUrl = ldapUrl;
        this.userPath  = userPath;
        this.groupsPath = groupsPath;
        this.username = username;
        this.password = password;
        //Try it
        getContext();
    }

    private DirContext getContext() throws NamingException {
        DirContext  localContext = theContext;
        
        //Create the first time
        if(localContext == null) {
            localContext = getInitialContext(ldapUrl, username, password);
        }

        //Try to connect with a dummy path
        try {
            localContext.getAttributes("dummypath");
        } catch(InvalidNameException ignoreThis) {
            //            System.err.println ("Connection OK with dummy path");
        } catch(Exception badConnection) {
            //            System.err.println ("bad connection:" + badConnection);
            //Maybe the connection got dropped so we'll try again
            localContext = null;
        } 

        if(localContext == null) {
            //            System.err.println ("Trying again");
            localContext = getInitialContext(ldapUrl, username, password);
            //            System.err.println ("OK");
        }
        theContext = localContext;
        return localContext;
    }


    /**
     * _more_
     *
     * @param username _more_
     * @param password _more_
     *
     * @return _more_
     *
     * @throws NamingException _more_
     */
    public static LDAPManager getInstance(String ldapUrl,
                                          String userPath, String groupPath,
                                          String username, String password)
            throws NamingException {

        // Construct the key for the supplied information
        String key = new StringBuffer().append(ldapUrl).append("|").append(((username == null)
                ? ""
                : username)).append("|").append(((password == null)
                ? ""
                : password)).toString();

        if ( !instances.containsKey(key)) {
            synchronized (LDAPManager.class) {
                if ( !instances.containsKey(key)) {
                    LDAPManager instance = new LDAPManager(ldapUrl,
                                               userPath, groupPath, username,
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
     * @param username _more_
     * @param password _more_
     *
     * @return _more_
     */
    public boolean isValidUser(String username, String password) {
        try {
            DirContext tmpContext = getInitialContext(ldapUrl,
                                                   getUserDN(username), password);
            return true;
        } catch (javax.naming.NameNotFoundException e) {
            debug("Name or password not found:" + username);
            return false;
        } catch (NamingException e) {
            log ("Error validating user:" + username +" " + e);
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
        Attributes attributes = getContext().getAttributes(getGroupDN(groupName),
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
    public Hashtable<String,List<String>> getUserAttributes(String username)
            throws NamingException {
        Hashtable<String,List<String>>  userAttributes  = new Hashtable<String,List<String>>();
        Attributes attributes = getContext().getAttributes(getUserDN(username));
        if (attributes == null) {
            return userAttributes;
        }
        for (NamingEnumeration ae = attributes.getAll(); ae.hasMore(); ) {
            Attribute attr = (Attribute) ae.next();
            List<String> attributeValues = new ArrayList<String>();
            for (NamingEnumeration e = attr.getAll(); e.hasMore(); ) {
                Object o = e.next();
                if(o instanceof String) {
                    attributeValues.add((String)o);
                }
            }
            if(attributeValues.size()>0) {
                userAttributes.put((String) attr.getID(),
                                   attributeValues);
            }
        }
        return userAttributes;
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
    public List<String> getGroups(String username, String groupMemberAttribute) throws NamingException {
        List<String> groups = new LinkedList<String>();
        DirContext context = getContext();
        NamingEnumeration<NameClassPair> enums = context.list(groupsPath);
        String[] searchAttributes = new String[]{groupMemberAttribute};
        while(enums.hasMoreElements()) {
            NameClassPair key = enums.nextElement();
            String id = key.getName();
            String groupId = getGroupCN(id);
            Attributes attributes = context.getAttributes(getGroupDN(groupId),
                                                          searchAttributes);
            if (attributes == null) {
                continue;
            }
            Attribute memberAtts = attributes.get(groupMemberAttribute);
            if (memberAtts == null) {
                continue;
            }
            for (NamingEnumeration vals = memberAtts.getAll();
                 vals.hasMoreElements(); ) {
                if (username.equalsIgnoreCase(
                                              getUserUID((String) vals.nextElement()))) {
                    groups.add(groupId);
                    break;
                }
            }
        }
        return groups;
    }



    public List OLDgetGroups(String username) throws NamingException {
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

        NamingEnumeration results = getContext().search(groupsPath, filter, cons);

        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            groups.add(getGroupCN(result.getName()));
        }

        return groups;
    }


    /**
     * _more_
     *
     * @param username _more_
     *
     * @return _more_
     */
    private String getUserDN(String username) {
        return userPath.replace("${id}", username);
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
     * @param name _more_
     *
     * @return _more_
     */
    private String getGroupDN(String name) {
        return new StringBuffer().append("cn=").append(name).append(
            ",").append(groupsPath).toString();
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


    private static void debug(String msg) {
        //        System.err.println("LDAP:" + msg);
    }

    private static void log(String msg) {
        System.err.println("LDAP:" + msg);
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
    private static DirContext getInitialContext(String ldapUrl,
                                                String username, String password)
            throws NamingException {


        log("Connecting to:" + ldapUrl);
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                  "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapUrl);

        if ((username != null) && ( !username.equals(""))) {
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, ((password == null)
                    ? ""
                    : password));
        }

        DirContext context =  new InitialDirContext(props);
        //        String suffix  = "dc=ldap,dc=int,dc=unavco,dc=org";
        //        walk(context, suffix, "");
        /*
        NamingEnumeration<NameClassPair> enums = context.list(suffix);
        while(enums.hasMoreElements()) {
            NameClassPair key = enums.nextElement();
            String id = key.getName();
            System.err.println("id:" + id);
        }
        */
        return context;
    }



    private void walk(DirContext context, String path, String tab) 
            throws NamingException {
        Attributes attributes = context.getAttributes(path);
        if (attributes != null) {
            for (NamingEnumeration ae = attributes.getAll(); ae.hasMore(); ) {
                Attribute attr = (Attribute) ae.next();
                System.out.print(tab +"attr: " + attr.getID());
                for (NamingEnumeration e = attr.getAll(); e.hasMore(); ) {
                    Object o = e.next();
                    if(o instanceof String) {
                        String value = (String) o;
                        System.out.print("   value: " + o);
                    } else {
                        System.out.print("   ?value: " + o.getClass().getName());
                    }
                }
                System.out.println("");
            }
        }

        NamingEnumeration<NameClassPair> enums = context.list(path);
        while(enums.hasMoreElements()) {
            NameClassPair key = enums.nextElement();
            String id = key.getName();
            System.out.println(tab + "id:" + id);
            walk(context, id+"," + path, tab+"  ");
        }
    }


}
