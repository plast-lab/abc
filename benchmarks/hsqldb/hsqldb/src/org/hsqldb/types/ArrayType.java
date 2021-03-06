/* Copyright (c) 2001-2010, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.types;

import org.hsqldb.OpTypes;
import org.hsqldb.Session;
import org.hsqldb.SessionInterface;
import org.hsqldb.Tokens;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.store.ValuePool;

/**
 * Class for ARRAY type objects.<p>
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 2.0.0
 * @since 2.0.0
 */
public class ArrayType extends Type {

    final Type dataType;
    final int  maxCardinality;

    public ArrayType(Type dataType, int cardinality) {

        super(Types.SQL_ARRAY, Types.SQL_ARRAY, 0, 0);

        this.dataType       = dataType;
        this.maxCardinality = cardinality;
    }

    public int displaySize() {
        return 7 + (dataType.displaySize() + 1) * maxCardinality ;
    }

    public int getJDBCTypeCode() {
        return Types.ARRAY;
    }

    public Class getJDBCClass() {
        return java.sql.Array.class;
    }

    public String getJDBCClassName() {
        return "java.sql.Array";
    }

    public Integer getJDBCScale() {
        return ValuePool.INTEGER_0;
    }

    public int getJDBCPrecision() {
        return ValuePool.INTEGER_0;
    }

    public int getSQLGenericTypeCode() {
        return 0;
    }

    public String getNameString() {

        StringBuffer sb = new StringBuffer();

        sb.append(dataType.getNameString()).append(' ');
        sb.append(Tokens.T_ARRAY);

        if (maxCardinality != defaultArrayCardinality) {
            sb.append('[').append(maxCardinality).append(']');
        }

        return sb.toString();
    }

    String getDefinition() {

        StringBuffer sb = new StringBuffer();

        sb.append(dataType.getDefinition()).append(' ');
        sb.append(Tokens.T_ARRAY);

        if (maxCardinality != defaultArrayCardinality) {
            sb.append('[').append(maxCardinality).append(']');
        }

        return sb.toString();
    }

    public int compare(Session session, Object a, Object b) {

        if (a == b) {
            return 0;
        }

        if (a == null) {
            return -1;
        }

        if (b == null) {
            return 1;
        }

        Object[] arra   = (Object[]) a;
        Object[] arrb   = (Object[]) b;
        int      length = arra.length;

        if (arrb.length < length) {
            length = arrb.length;
        }

        for (int i = 0; i < length; i++) {
            int result = dataType.compare(session, arra[i], arrb[i]);

            if (result != 0) {
                return result;
            }
        }

        if (arra.length > arrb.length) {
            return 1;
        } else if (arra.length < arrb.length) {
            return -1;
        }

        return 0;
    }

    public Object convertToTypeLimits(SessionInterface session, Object a) {

        if (a == null) {
            return null;
        }

        Object[] arra = (Object[]) a;

        if (arra.length > maxCardinality) {
            throw Error.error(ErrorCode.X_2202F);
        }

        Object[] arrb = new Object[arra.length];

        for (int i = 0; i < arra.length; i++) {
            arrb[i] = dataType.convertToTypeLimits(session, arra[i]);
        }

        return arrb;
    }

    public Object convertToType(SessionInterface session, Object a,
                                Type otherType) {

        if (a == null) {
            return null;
        }

        if (otherType == null) {
            return a;
        }

        if (!otherType.isArrayType()) {
            throw Error.error(ErrorCode.X_42562);
        }

        Object[] arra = (Object[]) a;

        if (arra.length > maxCardinality) {
            throw Error.error(ErrorCode.X_2202F);
        }

        Type otherComponent = otherType.collectionBaseType();

        if (dataType.equals(otherComponent)) {
            return a;
        }

        Object[] arrb = new Object[arra.length];

        for (int i = 0; i < arra.length; i++) {
            arrb[i] = dataType.convertToType(session, arra[i], otherComponent);
        }

        return arrb;
    }

    public Object convertToDefaultType(SessionInterface sessionInterface,
                                       Object o) {
        return o;
    }

    public String convertToString(Object a) {

        if (a == null) {
            return null;
        }

        return convertToSQLString(a);
    }

    public String convertToSQLString(Object a) {

        if (a == null) {
            return Tokens.T_NULL;
        }

        Object[]     arra = (Object[]) a;
        StringBuffer sb   = new StringBuffer();

        sb.append(Tokens.T_ARRAY);
        sb.append('[');

        for (int i = 0; i < arra.length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append(dataType.convertToSQLString(arra[i]));
        }

        sb.append(']');

        return sb.toString();
    }

    public boolean canConvertFrom(Type otherType) {

        if (otherType == null) {
            return true;
        }

        if (!otherType.isArrayType()) {
            return false;
        }

        Type otherComponent = otherType.collectionBaseType();

        return dataType.canConvertFrom(otherComponent);
    }

    public boolean canBeAssignedFrom(Type otherType) {

        if (otherType == null) {
            return true;
        }

        Type otherComponent = otherType.collectionBaseType();

        return otherComponent != null
               && dataType.canBeAssignedFrom(otherComponent);
    }

    public Type collectionBaseType() {
        return dataType;
    }

    public int arrayLimitCardinality() {
        return maxCardinality;
    }

    public boolean isArrayType() {
        return true;
    }

    public Type getAggregateType(Type otherType) {

        if (otherType == null) {
            return this;
        }

        if (!otherType.isArrayType()) {
            throw Error.error(ErrorCode.X_42562);
        }

        Type otherComponent = otherType.collectionBaseType();

        if (dataType.equals(otherComponent)) {
            return this;
        }

        Type newType = dataType.getAggregateType(otherComponent);

        return new ArrayType(newType, maxCardinality);
    }

    public Type getCombinedType(Type otherType, int operation) {

        if (operation != OpTypes.CONCAT) {
            return getAggregateType(otherType);
        }

        if (otherType == null) {
            return this;
        }

        if (!otherType.isArrayType()) {
            throw Error.error(ErrorCode.X_42562);
        }

        Type otherComponent = otherType.collectionBaseType();
        Type combinedType   = dataType.getAggregateType(otherComponent);

        return new ArrayType(combinedType,
                             maxCardinality
                             + otherType.arrayLimitCardinality());
    }

    public int cardinality(Session session, Object a) {

        if (a == null) {
            return 0;
        }

        return ((Object[]) a).length;
    }

    public Object concat(Session session, Object a, Object b) {

        if (a == null || b == null) {
            return null;
        }

        int      size  = ((Object[]) a).length + ((Object[]) b).length;
        Object[] array = new Object[size];

        System.arraycopy(a, 0, array, 0, ((Object[]) a).length);
        System.arraycopy(b, 0, array, ((Object[]) a).length,
                         ((Object[]) b).length);

        return array;
    }
}
