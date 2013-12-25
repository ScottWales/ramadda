/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository;


import org.ramadda.repository.database.*;


import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;


import org.ramadda.util.TTLCache;
import org.ramadda.util.TTLObject;

import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * This class does most of the work of managing repository content
 */
public class EntryUtil extends RepositoryManager {

    //Cache for 1 hour

    /** _more_ */
    private TTLObject<Hashtable<String, Integer>> typeCache =
        new TTLObject<Hashtable<String, Integer>>(60 * 60 * 1000);



    /**
     * _more_
     *
     * @param repository _more_
     */
    public EntryUtil(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     */
    public void clearCache() {
        typeCache = new TTLObject<Hashtable<String, Integer>>(60 * 60 * 1000);

    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    public List<Entry> sortEntriesOnName(List<Entry> entries,
                                         final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1     = (Entry) o1;
                Entry e2     = (Entry) o2;
                int   result = e1.getName().compareToIgnoreCase(e2.getName());
                if (descending) {
                    if (result >= 1) {
                        return -1;
                    } else if (result <= -1) {
                        return 1;
                    }

                    return 0;
                }

                return result;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    public List<Entry> doGroupAndNameSort(List<Entry> entries,
                                          final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1     = (Entry) o1;
                Entry e2     = (Entry) o2;
                int   result = 0;
                if (e1.isGroup()) {
                    if (e2.isGroup()) {
                        result = e1.getFullName().compareTo(e2.getFullName());
                    } else {
                        result = -1;
                    }
                } else if (e2.isGroup()) {
                    result = 1;
                } else {
                    result = e1.getFullName().compareTo(e2.getFullName());
                }
                if (descending) {
                    return -result;
                }

                return result;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    public List<Entry> sortEntriesOnDate(List<Entry> entries,
                                         final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                if (e1.getStartDate() < e2.getStartDate()) {
                    return (descending
                            ? 1
                            : -1);
                }
                if (e1.getStartDate() > e2.getStartDate()) {
                    return (descending
                            ? -1
                            : 1);
                }

                return 0;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    public List<Entry> sortEntriesOnCreateDate(List<Entry> entries,
            final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                if (e1.getCreateDate() < e2.getCreateDate()) {
                    return (descending
                            ? 1
                            : -1);
                }
                if (e1.getStartDate() > e2.getStartDate()) {
                    return (descending
                            ? -1
                            : 1);
                }

                return 0;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }


    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     *
     * @return _more_
     */
    public List<Entry> sortEntriesOnChangeDate(List<Entry> entries,
            final boolean descending) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                if (e1.getChangeDate() < e2.getChangeDate()) {
                    return (descending
                            ? 1
                            : -1);
                }
                if (e1.getChangeDate() > e2.getChangeDate()) {
                    return (descending
                            ? -1
                            : 1);
                }

                return 0;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }




    /**
     * _more_
     *
     * @param entries _more_
     * @param descending _more_
     * @param type _more_
     * @param sortOrderFieldIndex _more_
     *
     * @return _more_
     */
    public List<Entry> sortEntriesOnField(List<Entry> entries,
                                          final boolean descending,
                                          final String type,
                                          final int sortOrderFieldIndex) {
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Entry   e1 = (Entry) o1;
                Entry   e2 = (Entry) o2;
                int     result;
                boolean isType1 = e1.isType(type);
                boolean isType2 = e2.isType(type);
                if (isType1 && isType2) {
                    Integer i1 =
                        (Integer) e1.getTypeHandler().getEntryValue(e1,
                            sortOrderFieldIndex);
                    Integer i2 =
                        (Integer) e2.getTypeHandler().getEntryValue(e2,
                            sortOrderFieldIndex);
                    result = i1.compareTo(i2);
                } else if (isType1) {
                    result = -1;
                } else if (isType2) {
                    result = 1;
                } else {
                    result = e1.getName().compareToIgnoreCase(e2.getName());
                }
                if (descending) {
                    if (result >= 1) {
                        return -1;
                    } else if (result <= -1) {
                        return 1;
                    }

                    return 0;
                }

                return result;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
        };
        Object[] array = entries.toArray();
        Arrays.sort(array, comp);

        return (List<Entry>) Misc.toList(array);
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getTimezone(Entry entry) {
        try {
            List<Metadata> metadataList =
                getMetadataManager().findMetadata(null, entry,
                    ContentMetadataHandler.TYPE_TIMEZONE, true);
            if ((metadataList != null) && (metadataList.size() > 0)) {
                Metadata metadata = metadataList.get(0);

                return metadata.getAttr1();
            }
        } catch (Exception exc) {
            logError("getting timezone", exc);
        }

        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String formatDate(Request request, Entry entry) {
        return getPageHandler().formatDate(request, entry.getStartDate(),
                                           getTimezone(entry));
    }



    /**
     * _more_
     *
     * @param entries _more_
     * @param type _more_
     *
     * @return _more_
     */
    public List<Entry> getEntriesWithType(List<Entry> entries, String type) {
        List<Entry> results = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.getTypeHandler().isType(type)) {
                results.add(entry);
            }
        }

        return results;
    }


    /**
     * _more_
     *
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public int getEntryCount(TypeHandler typeHandler) throws Exception {
        Hashtable<String, Integer> typesWeHave = typeCache.get();
        if (typesWeHave == null) {
            typesWeHave = new Hashtable<String, Integer>();
            for (String type :
                    getRepository().getDatabaseManager().selectDistinct(
                        Tables.ENTRIES.NAME, Tables.ENTRIES.COL_TYPE, null)) {
                int cnt = getDatabaseManager().getCount(Tables.ENTRIES.NAME,
                              Clause.eq(Tables.ENTRIES.COL_TYPE, type));

                typesWeHave.put(type, new Integer(cnt));
            }
            typeCache.put(typesWeHave);
        }
        Integer cnt = typesWeHave.get(typeHandler.getType());
        if (cnt == null) {
            return 0;
        }

        return cnt.intValue();
    }

    /**
     * _more_
     *
     * @param entries _more_
     * @param type _more_
     *
     * @return _more_
     */
    public List<Entry> getEntriesOfType(List<Entry> entries, String type) {
        List<Entry> result = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (entry.isType(type)) {
                result.add(entry);
            }
        }

        return result;
    }



}
