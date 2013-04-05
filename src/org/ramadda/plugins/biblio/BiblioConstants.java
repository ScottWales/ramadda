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

package org.ramadda.plugins.biblio;



/**
 */
public interface BiblioConstants  {


    public static final String TAG_BIBLIO_TYPE = "%0";
    public static final String TAG_BIBLIO_AUTHOR = "%A";
    public static final String TAG_BIBLIO_INSTITUTION = "%I";
    public static final String TAG_BIBLIO_DATE = "%D";
    public static final String TAG_BIBLIO_TAG = "%K";
    public static final String TAG_BIBLIO_TITLE = "%T";
    public static final String TAG_BIBLIO_PUBLICATION = "%J";
    public static final String TAG_BIBLIO_KEYWORD = "%K";
    public static final String TAG_BIBLIO_VOLUME = "%V";
    public static final String TAG_BIBLIO_ISSUE = "%N";
    public static final String TAG_BIBLIO_PAGE = "%P";
    public static final String TAG_BIBLIO_DOI = "%R";
    public static final String TAG_BIBLIO_DESCRIPTION = "%X";
    public static final String TAG_BIBLIO_URL = "%U";

    public static final int IDX_PRIMARY_AUTHOR = 0;
    public static final int IDX_TYPE = 1;
    public static final int IDX_INSTITUTION = 2;
    public static final int IDX_OTHER_AUTHORS = 3;
    public static final int IDX_PUBLICATION = 4;
    public static final int IDX_VOLUME = 5;
    public static final int IDX_ISSUE = 6;
    public static final int IDX_PAGE = 7;
    public static final int IDX_DOI = 8;
    public static final int IDX_LINK = 9;


    public static final String[] TAGS = {TAG_BIBLIO_INSTITUTION,
                                TAG_BIBLIO_PUBLICATION,
                                TAG_BIBLIO_VOLUME,
                                TAG_BIBLIO_ISSUE,
                                TAG_BIBLIO_PAGE,
                                TAG_BIBLIO_DOI,
                                TAG_BIBLIO_URL,
    };

    public static final int[] INDICES = {
        IDX_INSTITUTION,
                                IDX_PUBLICATION,
                                IDX_VOLUME,
                                IDX_ISSUE,
                                IDX_PAGE,
                                IDX_DOI,
                                IDX_LINK,
    };




}
