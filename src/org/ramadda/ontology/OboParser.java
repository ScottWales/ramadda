/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * Copyright 2010- ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
package org.ramadda.ontology;


import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Thu, Nov 25, '10
 * @author         Enter your name here...    
 */
public class OboParser {

    /** _more_          */
    public static final String TAG_FORMAT_VERSION = "format-version";

    /** _more_          */
    public static final String TAG_DATA_VERSION = "data-version";

    /** _more_          */
    public static final String TAG_DATE = "date";

    /** _more_          */
    public static final String TAG_SUBSETDEF = "subsetdef";

    /** _more_          */
    public static final String TAG_SYNONYMTYPEDEF = "synonymtypedef";

    /** _more_          */
    public static final String TAG_DEFAULT_NAMESPACE = "default-namespace";

    /** _more_          */
    public static final String TAG_REMARK = "remark";

    /** _more_          */
    public static final String TAG_ID = "id";

    /** _more_          */
    public static final String TAG_NAME = "name";

    /** _more_          */
    public static final String TAG_NAMESPACE = "namespace";

    /** _more_          */
    public static final String TAG_DEF = "def";

    /** _more_          */
    public static final String TAG_SYNONYM = "synonym";

    /** _more_          */
    public static final String TAG_IS_A = "is_a";

    /** _more_          */
    public static final String TAG_ALT_ID = "alt_id";

    /** _more_          */
    public static final String TAG_SUBSET = "subset";

    /** _more_          */
    public static final String TAG_XREF = "xref";

    /** _more_          */
    public static final String TAG_COMMENT = "comment";

    /** _more_          */
    public static final String TAG_IS_OBSOLETE = "is_obsolete";

    /** _more_          */
    public static final String TAG_CONSIDER = "consider";

    /** _more_          */
    public static final String TAG_RELATIONSHIP = "relationship";

    /** _more_          */
    public static final String TAG_REPLACED_BY = "replaced_by";

    /** _more_          */
    public static final String TAG_DISJOINT_FROM = "disjoint_from";

    /** _more_          */
    public static final String TAG_IS_TRANSITIVE = "is_transitive";

    /** _more_          */
    public static final String TAG_TRANSITIVE_OVER = "transitive_over";


    /** _more_          */
    static HashSet<String> tagMap = new HashSet<String>();

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public static String[] getPair(String line) {
        return getPair(line, ":");
    }

    /**
     * _more_
     *
     * @param line _more_
     * @param token _more_
     *
     * @return _more_
     */
    public static String[] getPair(String line, String token) {
        List<String> toks = StringUtil.splitUpTo(line, token, 2);
        if ( !tagMap.contains(toks.get(0))) {
            String tag = toks.get(0);
            tagMap.add(tag);
            String var = tag.toUpperCase();
            var = var.replaceAll("-", "_");
            if (var.indexOf(":") < 0) {
                //                System.out.println("public static final String TAG_" + var +"  = \"" + tag +"\";"); 
            }
        }
        if (toks.size() == 1) {
            return new String[] { toks.get(0).trim() };
        }
        return new String[] { toks.get(0).trim(), toks.get(1).trim() };
    }

    /**
     * _more_
     *
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public static void processFile(String file) throws Exception {
        List<String> lines = StringUtil.split(IOUtil.readContents(file,
                                 OboParser.class), "\n", true, true);

        Term                    currentTerm      = null;
        List<Term>              terms            = new ArrayList<Term>();
        Hashtable<String, Term> map = new Hashtable<String, Term>();
        String                  defaultNamespace = "";
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("[Term]")) {
                line = lines.get(++i);
                String[] pair = getPair(line);
                currentTerm = new Term(pair[1]);
                map.put(currentTerm.id, currentTerm);
                terms.add(currentTerm);
                continue;
            }

            if (line.startsWith("[Typedef]")) {
                currentTerm = null;
                continue;
            }
            String[] pair = getPair(line);
            if (pair[0].equals(TAG_DEFAULT_NAMESPACE)) {
                defaultNamespace = pair[1];
                System.err.println("Namespace:" + defaultNamespace);
                continue;
            }

            if (currentTerm != null) {
                currentTerm.values.add(pair);
            }
        }

        for (Term term : terms) {
            String namespace = term.getValue(TAG_NAMESPACE, defaultNamespace);
            System.out.println("term:" + term.getId() + " " + term.getName()
                               + " ns:" + namespace);
            System.out.println("DEF:" + term.getDef());
            for (String tuple : term.getValues(TAG_IS_A)) {
                String id        = getPair(tuple, "!")[0];
                Term   otherTerm = map.get(id);
                if (otherTerm == null) {
                    System.out.println(term.id + "  isa =  NULL " + id);
                    //                    System.exit(0);
                } else {
                    System.out.println("  isa:" + otherTerm.getName());
                }
            }

        }
        System.err.println("# terms:" + terms.size());
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        for (String file : args) {
            processFile(file);
        }
    }


    /**
     * Class description
     *
     *
     * @version        $version$, Thu, Nov 25, '10
     * @author         Enter your name here...    
     */
    public static class Term {

        /** _more_          */
        String id;

        /** _more_          */
        List<String[]> values = new ArrayList<String[]>();

        /**
         * _more_
         *
         * @param id _more_
         */
        public Term(String id) {
            this.id = id;
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getName() {
            return getValue(TAG_NAME, getId());
        }

        /**
         * _more_
         *
         * @return _more_
         */
        public String getId() {
            return id;
        }


        /**
         * _more_
         *
         * @return _more_
         */
        public String getDef() {
            return unquote(getValue(TAG_DEF, ""));
        }

        /**
         * _more_
         *
         * @param s _more_
         *
         * @return _more_
         */
        public String unquote(String s) {
            if (s == null) {
                return null;
            }
            s = s.trim();
            if ( !s.startsWith("\"")) {
                return s;
            }
            s = s.substring(1);
            int idx = s.indexOf("\"");
            if (idx >= 0) {
                s = s.substring(0, idx);
            }
            return s;
        }


        /**
         * _more_
         *
         * @param key _more_
         *
         * @return _more_
         */
        public String getValue(String key) {
            return getValue(key, null);
        }

        /**
         * _more_
         *
         * @param key _more_
         * @param dflt _more_
         *
         * @return _more_
         */
        public String getValue(String key, String dflt) {
            for (String[] tuple : values) {
                if (tuple[0].equals(key)) {
                    return tuple[1];
                }
            }
            return dflt;
        }

        /**
         * _more_
         *
         * @param key _more_
         *
         * @return _more_
         */
        public List<String> getValues(String key) {
            List<String> theValues = new ArrayList<String>();
            for (String[] tuple : values) {
                if (tuple[0].equals(key)) {
                    theValues.add(unquote(tuple[1]));
                }
            }
            return theValues;
        }

    }
}
