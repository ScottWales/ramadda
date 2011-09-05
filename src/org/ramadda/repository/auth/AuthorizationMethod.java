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

package org.ramadda.repository.auth;


import org.ramadda.repository.*;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class AuthorizationMethod {

    /** _more_ */
    private static final String TYPE_HTTPAUTH = "httpauth";

    /** _more_ */
    private static final String TYPE_HTML = "html";

    /** _more_ */
    private String type = TYPE_HTML;

    /** _more_ */
    public static final AuthorizationMethod AUTH_HTTP =
        new AuthorizationMethod(TYPE_HTTPAUTH);

    /** _more_ */
    public static final AuthorizationMethod AUTH_HTML =
        new AuthorizationMethod(TYPE_HTML);

    /**
     * _more_
     *
     *
     * @param type _more_
     */
    private AuthorizationMethod(String type) {
        this.type = type;
    }


    /**
     * _more_
     *
     * @param method _more_
     *
     * @return _more_
     */
    public static AuthorizationMethod getMethod(String method) {
        if (method.equals(TYPE_HTTPAUTH)) {
            return AUTH_HTTP;
        }
        return AUTH_HTML;
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !(o instanceof AuthorizationMethod)) {
            return false;
        }
        AuthorizationMethod that = (AuthorizationMethod) o;
        return this.type.equals(that.type);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return type;
    }

}
