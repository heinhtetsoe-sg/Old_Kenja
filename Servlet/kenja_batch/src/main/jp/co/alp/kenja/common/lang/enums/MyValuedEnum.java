/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package jp.co.alp.kenja.common.lang.enums;

import java.util.List;

import org.apache.commons.lang.ClassUtils;

/**
 * <p>Abstract superclass for type-safe enums with integer values suitable
 * for use in <code>switch</code> statements.</p>
 *
 * <p><em>NOTE:</em>Due to the way in which Java ClassLoaders work, comparing
 * <code>MyEnum</code> objects should always be done using the equals() method,
 * not <code>==</code>. The equals() method will try <code>==</code> first so
 * in most cases the effect is the same.</p>
 *
 * <p>As shown, each enum has a key and a value. These can be accessed using
 * <code>getKey</code> and <code>getValue</code>.</p>
 *
 * <p>The <code>getEnum</code> and <code>iterator</code> methods are recommended.
 * Unfortunately, Java restrictions require these to be coded as shown in each subclass.
 * An alternative choice is to use the {@link MyEnumUtils} class.</p>
 *
 * @author Apache Avalon project
 * @author Stephen Colebourne
 * @since 1.0
 * @version $Id: MyValuedEnum.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class MyValuedEnum<K extends Comparable<K>, V extends Comparable<V>> extends MyEnum<K, V> {
//    private static final long serialVersionUID = -8194963322818291163L;

    /**
     * The value contained in enum.
     */
    private final V iValue;

    /**
     * Constructor for enum item.
     *
     * @param key  the key of enum item
     * @param value  the value of enum item
     */
    protected MyValuedEnum(final Category category, K key, V value) {
        super(category, key);
        iValue = value;
    }

    /**
     * <p>Gets an <code>MyEnum</code> object by class and value.</p>
     *
     * <p>This method loops through the list of <code>MyEnum</code>,
     * thus if there are many <code>MyEnum</code>s this will be
     * slow.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get
     * @param value  the value of the <code>MyEnum</code> to get
     * @return the enum object, or null if the enum does not exist
     * @throws IllegalArgumentException if the enum class is <code>null</code>
     */
    protected static <K extends Comparable<K>, V extends Comparable<V>, MVE extends MyValuedEnum<K, V>> MyEnum<K, V> getEnumByValue(final Category category, Class<MVE> enumClass, V value) {
        if (enumClass == null) {
            throw new IllegalArgumentException("The MyEnum Class must not be null");
        }
        List<MVE> list = MyEnum.getEnumList(category, enumClass);
        for (final MVE en : list) {
            if (en.getValue().compareTo(value) == 0) {
                return en;
            }
        }
        return null;
    }

    /**
     * <p>Get value of enum item.</p>
     *
     * @return the enum item's value.
     */
    public final V getValue() {
        checkAlive();
        return iValue;
    }

    /**
     * <p>Tests for order.</p>
     *
     * <p>The default ordering is numeric by value, but this
     * can be overridden by subclasses.</p>
     * 
     * @see java.lang.Comparable#compareTo(Object)
     * @param other  the other object to compare to
     * @return -ve if this is less than the other object, +ve if greater than,
     *  <code>0</code> of equal
     * @throws ClassCastException if other is not an <code>MyEnum</code>
     * @throws NullPointerException if other is <code>null</code>
     */
    public int compareTo(final MyEnum<K, V> other) {
        checkAlive();
        MyValuedEnum<K, V> that = (MyValuedEnum<K, V>) other;
        return iValue.compareTo(that.iValue);
    }

    /**
     * <p>Human readable description of this <code>MyEnum</code> item.</p>
     *
     * @return String in the form <code>type[key=value]</code>, for example:
     *  <code>JavaVersion[Java 1.0=100]</code>. Note that the package name is
     *  stripped from the type name.
     */
    public String toString() {
        checkAlive();
        if (iToString == null) {
            String shortName = ClassUtils.getShortClassName(getEnumClass());
            iToString = shortName + "[" + getKey() + "=" + getValue() + "]";
        }
        return iToString;
    }
}
