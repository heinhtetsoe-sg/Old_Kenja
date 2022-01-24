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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.AbstractMappedMap;
import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.HashMappedMap;

/**
 * <p>Abstract superclass for type-safe enums.</p>
 *
 * <p>One feature of the C programming language lacking in Java is enumerations. The
 * C implementation based on ints was poor and open to abuse. The original Java
 * recommendation and most of the JDK also uses int constants. It has been recognised
 * however that a more robust type-safe class-based solution can be designed. This
 * class follows the basic Java type-safe enumeration pattern.</p>
 *
 * <p><em>NOTE:</em>Due to the way in which Java ClassLoaders work, comparing
 * MyEnum objects should always be done using <code>equals()</code>, not <code>==</code>.
 * The equals() method will try == first so in most cases the effect is the same.</p>
 * 
 * <p>Of course, if you actually want (or don't mind) Enums in different class
 * loaders being non-equal, then you can use <code>==</code>.</p>
 * 
 * @author Apache Avalon project
 * @author Stephen Colebourne
 * @author Chris Webb
 * @author Mike Bowler
 * @since 1.0
 * @version $Id: MyEnum.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public abstract class MyEnum<K extends Comparable<K>, V> implements Comparable<MyEnum<K, V>> {

//    private static final long serialVersionUID = -2655992748023751226L;

    /** log */
    /*pkg*/static final Log log = LogFactory.getLog(MyEnum.class);

    // After discussion, the default size for HashMaps is used, as the
    // sizing algorithm changes across the JDK versions
    /**
     * An empty <code>Map</code>, as JDK1.2 didn't have an empty map.
     */
//    private static final Map EMPTY_MAP = Collections.unmodifiableMap(new HashMap(0));
    
    /**
     * <code>Map</code>, key of class key, value of <code>Entry</code>.
     */
    private static final AbstractMappedMap<Category, Class, Entry> cEnumClasses = new HashMappedMap<Category, Class, Entry>();
    
    /**
     * The string representation of the MyEnum.
     */
    private final K iKey;
    
    /**
     * The hashcode representation of the MyEnum.
     */
    private transient final int iHashCode;
    
    /**
     * The toString representation of the MyEnum.
     * @since 2.0
     */
    protected transient String iToString = null;

    private final Category _category;
    private boolean _isAlive;
    private final boolean _keepAlive;
    private final int _index;

    /**
     * <p>Enable the iterator to retain the source code order.</p>
     */
    private static class Entry<MK, ME> {
        /**
         * Map of MyEnum key to MyEnum.
         */
        final Map<MK, ME> map = new HashMap<MK, ME>();
        /**
         * Map of MyEnum key to MyEnum.
         */
        final Map<MK, ME> unmodifiableMap = Collections.unmodifiableMap(map);
        /**
         * List of Enums in source code order.
         */
        final List<ME> list = new ArrayList<ME>(25);
        /**
         * Map of MyEnum key to MyEnum.
         */
        final List<ME> unmodifiableList = Collections.unmodifiableList(list);

        /**
         * <p>Restrictive constructor.</p>
         */
        private Entry() {
        }
    }

    public static class Category {
        public static final Category NULL = null;
        private static int initNumber;
        private static synchronized int nextNum() {
            return initNumber++;
        }
        private final String _name;
        public Category() {
            _name = "Category:" + nextNum();
            log.trace("new " + _name);
        }
        public String toString() { return _name; }
    }

//    protected MyEnum(final boolean keepAlive, final Comparable key) {
//        this(null, keepAlive, key);
//    }
//
//    protected MyEnum(final Comparable key) {
//        this(null, false, key);
//    }

    /**
     * <p>Constructor to add a new key item to the enumeration.</p>
     *
     * @param key  the key of the enum object,
     *  must not be empty or <code>null</code>
     * @param keepAlive trueなら、clear()の対象外
     * @throws IllegalArgumentException if the key is <code>null</code>
     * @throws IllegalArgumentException if the getEnumClass() method returns
     *  a null or invalid Class
     */
    protected MyEnum(final Category category, final boolean keepAlive, final K key) {
        super();
        _category = category;
        _keepAlive = keepAlive;
        _isAlive = true; // init()呼び出しより前にaliveにする
        _index = init(category, key);
        iKey = key;
        iHashCode = 7 + getEnumClass().hashCode() + 3 * key.hashCode();
        // cannot create toString here as subclasses may want to include other data
    }

    /**
     * <p>Constructor to add a new key item to the enumeration.</p>
     *
     * @param key  the key of the enum object,
     *  must not be empty or <code>null</code>
     * @throws IllegalArgumentException if the key is <code>null</code>
     * @throws IllegalArgumentException if the getEnumClass() method returns
     *  a null or invalid Class
     */
    protected MyEnum(final Category category, final K key) {
        this(category, false, key);
    }

    public final Category getCategory() {
        return _category;
    }

    /**
     * Initializes the enumeration.
     * 
     * @param key  the enum key
     * @return 追加の順番(最初の要素の場合はゼロ)
     * @throws IllegalArgumentException if the key is null or duplicate
     * @throws IllegalArgumentException if the enumClass is null or invalid
     */
    private int init(final Category category, final K key) {
        if (null == key) {
            throw new IllegalArgumentException("The MyEnum key must not be null");
        }
        
        Class enumClass = getEnumClass();
        if (enumClass == null) {
            throw new IllegalArgumentException("getEnumClass() must not be null");
        }
        Class cls = getClass();
        boolean ok = false;
        while (cls != null && cls != MyEnum.class && cls != MyValuedEnum.class) {
            if (cls == enumClass) {
                ok = true;
                break;
            }
            cls = cls.getSuperclass();
        }
        if (ok == false) {
            throw new IllegalArgumentException("getEnumClass() must return a superclass of this class");
        }
        
        // create entry
        Entry<K, MyEnum<K, V>> entry = cEnumClasses.get(category, enumClass);
        if (entry == null) {
            entry = createEntry(category, enumClass);
            if (Category.NULL != category) {
                copyEntry(category, enumClass, entry);
            }
            cEnumClasses.put(category, enumClass, entry);
        }
        if (entry.map.containsKey(key)) {
            throw new IllegalArgumentException("The MyEnum key must be unique, '" + key + "' has already been added");
        }
        final int index = entry.list.size();
        entry.map.put(key, this);
        entry.list.add(this);
        return index;
    }

//    /**
//     * <p>Handle the deserialization of the class to ensure that multiple
//     * copies are not wastefully created, or illegal enum types created.</p>
//     *
//     * @return the resolved object
//     */
//    protected Object readResolve() {
//        Entry entry = (Entry) cEnumClasses.get(getEnumClass());
//        if (entry == null) {
//            return null;
//        }
//        return (MyEnum) entry.map.get(getKey());
//    }
    
    //--------------------------------------------------------------------------------

    private void copyEntry(final Category category, final Class<MyEnum<K, V>> enumClass, final Entry<K, MyEnum<K, V>> entry) {
        final Entry<K, MyEnum<K, V>> from = getEntry(Category.NULL, enumClass);
        if (null == from) {
            return;
        }
        for (final Iterator<K> it = from.map.keySet().iterator(); it.hasNext();) {
            final K key = it.next();
            final MyEnum<K, V> value = from.map.get(key);
            final MyEnum<K, V> that = value;
            if (that._keepAlive) {
                entry.map.put(key, value);
                entry.list.add(value);
                if (log.isInfoEnabled()) {
                    final String cls = ClassUtils.getShortClassName(enumClass);
                    log.info("add :" + category + ", " + cls + "=" + key + ":" + value);
                }
            } else {
                log.warn(value + " is not KeepAlive.");
            }
        }
    }

    /**
     * <p>Gets an <code>MyEnum</code> object by class and key.</p>
     * 
     * @param enumClass  the class of the MyEnum to get, must not
     *  be <code>null</code>
     * @param key  the key of the <code>MyEnum</code> to get,
     *  may be <code>null</code>
     * @return the enum object, or null if the enum does not exist
     * @throws IllegalArgumentException if the enum class
     *  is <code>null</code>
     */
    protected static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> T getEnum(final Category category, final Class<T> enumClass, final MK key) {
        Entry<MK, T> entry = getEntry(category, enumClass);
        if (entry == null) {
            return null;
        }
        T obj = entry.map.get(key);
        if (null == obj && Category.NULL != category) {
            return getEnum(Category.NULL, enumClass, key);
        }
        return obj;
    }

    /**
     * <p>Gets the <code>Map</code> of <code>MyEnum</code> objects by
     * key using the <code>MyEnum</code> class.</p>
     *
     * <p>If the requested class has no enum objects an empty
     * <code>Map</code> is returned.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get,
     *  must not be <code>null</code>
     * @return the enum object Map
     * @throws IllegalArgumentException if the enum class is <code>null</code>
     * @throws IllegalArgumentException if the enum class is not a subclass of MyEnum
     */
    protected static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> Map<MK, T> getEnumMap(final Category category, final Class<T> enumClass) {
        Entry<MK, T> entry = getEntry(category, enumClass);
        if (entry == null) {
            return Collections.emptyMap();
        }
        return entry.unmodifiableMap;
    }

    /**
     * <p>Gets the <code>List</code> of <code>MyEnum</code> objects using the
     * <code>MyEnum</code> class.</p>
     *
     * <p>The list is in the order that the objects were created (source code order).
     * If the requested class has no enum objects an empty <code>List</code> is
     * returned.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get,
     *  must not be <code>null</code>
     * @return the enum object Map
     * @throws IllegalArgumentException if the enum class is <code>null</code>
     * @throws IllegalArgumentException if the enum class is not a subclass of MyEnum
     */
    protected static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> List<T> getEnumList(final Category category, final Class<T> enumClass) {
        Entry<MK, T> entry = getEntry(category, enumClass);
        if (entry == null) {
            return Collections.emptyList();
        }
        return entry.unmodifiableList;
    }

    /**
     * <p>Gets an <code>Iterator</code> over the <code>MyEnum</code> objects in
     * an <code>MyEnum</code> class.</p>
     *
     * <p>The <code>Iterator</code> is in the order that the objects were
     * created (source code order). If the requested class has no enum
     * objects an empty <code>Iterator</code> is returned.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get,
     *  must not be <code>null</code>
     * @return an iterator of the MyEnum objects
     * @throws IllegalArgumentException if the enum class is <code>null</code>
     * @throws IllegalArgumentException if the enum class is not a subclass of MyEnum
     */
    protected static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> Iterator<T> iterator(final Category category, final Class<T> enumClass) {
        return MyEnum.getEnumList(category, enumClass).iterator();
    }

//    protected synchronized static void clear(final Class enumClass) {
//        final Entry entry = getEntry(enumClass);
//        if (entry == null) {
//            log.warn("entry is null");
//            return;
//        }
//
//        final List l = new ArrayList(entry.list);
//
//        entry.list.clear();
//        entry.map.clear();
//
//        final int size = l.size();
//        Object o;
//        for (int i = 0; i < size; i++) {
//            o = l.get(i);
//            if (!(o instanceof MyEnum)) {
//                log.error("'" + o + "' is not MyEnum");
//                continue;
//            }
//
//            ((MyEnum) o)._isAlive = false;
//        }
//
//        l.clear();
//    }

    protected synchronized static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> void clear(final Category category, final Class<T> enumClass) {
        final Entry<MK, T> entry = getEntry(category, enumClass);
        if (entry == null) {
            return;
        }

        MyEnum<MK, ME> me;
        for (final Iterator<T> it = entry.list.iterator(); it.hasNext();) {
            me = it.next();

            if (me instanceof Clearable) {
                ((Clearable) me).clear();
            }

            if (! me._keepAlive) {
                me._isAlive = false;
                it.remove();
                entry.map.remove(me.iKey);
            }
        }
    }

    /**
     * そのクラスの列挙の数を得る。
     * @return 列挙の数
     */
    protected static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> int size(final Category category, final Class<T> enumClass) {
        final Entry<MK, T> entry = getEntry(category, enumClass);
        if (entry == null) {
            return 0;
        }
//        assert null != entry.list;

        return entry.list.size();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Gets an <code>Entry</code> from the map of Enums.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get
     * @return the enum entry
     */
    private static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> Entry<MK, T> getEntry(final Category category, final Class<T> enumClass) {
        if (enumClass == null) {
            throw new IllegalArgumentException("The MyEnum Class must not be null");
        }
        if (MyEnum.class.isAssignableFrom(enumClass) == false) {
            throw new IllegalArgumentException("The Class must be a subclass of MyEnum");
        }
        Entry<MK, T> entry = cEnumClasses.get(category, enumClass);
        return entry;
    }
    
    /**
     * <p>Creates an <code>Entry</code> for storing the Enums.</p>
     *
     * <p>This accounts for subclassed Enums.</p>
     * 
     * @param enumClass  the class of the <code>MyEnum</code> to get
     * @return the enum entry
     */
    private static <MK extends Comparable<MK>, ME, T extends MyEnum<MK, ME>> Entry<MK, T> createEntry(final Category category, final Class<T> enumClass) {
        Entry<MK, T> entry = new Entry<MK, T>();
        Class cls = enumClass.getSuperclass();
        while (cls != null && cls != MyEnum.class && cls != MyValuedEnum.class) {
            Entry loopEntry = cEnumClasses.get(category, cls);
            if (loopEntry != null) {
                entry.list.addAll(loopEntry.list);
                entry.map.putAll(loopEntry.map);
                break;  // stop here, as this will already have had superclasses added
            }
            cls = cls.getSuperclass();
        }
        return entry;
    }

    //-----------------------------------------------------------------------
//    protected final MyEnum setKeepAlive() {
//        _keepAlive = true;
//        if (log.isInfoEnabled()) {
//            final String shortName = ClassUtils.getShortClassName(getEnumClass());
//            log.info("'" + shortName + "[" + getKey() + "]' to keepAlive.");
//        }
//        return this;
//    }

    public final boolean isAlive() {
        return _isAlive;
    }

    public final void checkAlive() {
        if (!_isAlive) {
            throw new IllegalStateException(iKey + " is not alive.");
        }
    }

    /**
     * 列挙のインデックス（追加の順序,最初の要素の場合はゼロ）を得る。
     * @return 列挙のインデックス
     */
    public final int getIndex() {
        checkAlive();
        return _index;
    }

    /**
     * <p>Retrieve the key of this MyEnum item, set in the constructor.</p>
     * 
     * @return the <code>String</code> key of this MyEnum item
     */
    public final K getKey() {
        checkAlive();
        return iKey;
    }

    /**
     * <p>Retrieves the Class of this MyEnum item, set in the constructor.</p>
     * 
     * <p>This is normally the same as <code>getClass()</code>, but for
     * advanced Enums may be different. If overridden, it must return a
     * constant value.</p>
     * 
     * @return the <code>Class</code> of the enum
     * @since 2.0
     */
    public Class<? extends MyEnum> getEnumClass() {
        checkAlive();
        return getClass();
    }

    /**
     * <p>Tests for equality.</p>
     *
     * <p>Two MyEnum objects are considered equal
     * if they have the same class key and the same key.
     * Identity is tested for first, so this method usually runs fast.</p>
     *
     * @param other  the other object to compare for equality
     * @return <code>true</code> if the Enums are equal
     */
    public final boolean equals(Object other) {
        checkAlive();

        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other.getClass() == this.getClass()) {
            // shouldn't happen, but...
            return iKey.equals(((MyEnum) other).iKey);
        } else if (!(other instanceof MyEnum)) {
            return false;
        } else if (((MyEnum) other).getEnumClass().getName().equals(getEnumClass().getName())) {
            // different classloaders
            try {
                // try to avoid reflection
                return iKey.equals(((MyEnum) other).iKey);

            } catch (ClassCastException ex) {
                // use reflection
                try {
                    Method mth = other.getClass().getMethod("getKey", null);
                    String key = (String) mth.invoke(other, null);
                    return iKey.equals(key);
                } catch (NoSuchMethodException ex2) {
                    // ignore - should never happen
                } catch (IllegalAccessException ex2) {
                    // ignore - should never happen
                } catch (InvocationTargetException ex2) {
                    // ignore - should never happen
                }
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * <p>Returns a suitable hashCode for the enumeration.</p>
     *
     * @return a hashcode based on the key
     */
    public final int hashCode() {
        checkAlive();
        return iHashCode;
    }

    /**
     * <p>Tests for order.</p>
     *
     * <p>The default ordering is alphabetic by key, but this
     * can be overridden by subclasses.</p>
     * 
     * @see java.lang.Comparable#compareTo(Object)
     * @param other  the other object to compare to
     * @return -ve if this is less than the other object, +ve if greater
     *  than, <code>0</code> of equal
     * @throws ClassCastException if other is not an MyEnum
     * @throws NullPointerException if other is <code>null</code>
     */
    public int compareTo(final MyEnum<K, V> other) {
        checkAlive();
        if (other == this) {
            return 0;
        }
        return iKey.compareTo(other.iKey);
    }

    /**
     * <p>Human readable description of this MyEnum item.</p>
     * 
     * @return String in the form <code>type[key]</code>, for example:
     * <code>Color[Red]</code>. Note that the package name is stripped from
     * the type name.
     */
    public String toString() {
        checkAlive();
        if (iToString == null) {
            String shortName = ClassUtils.getShortClassName(getEnumClass());
            iToString = shortName + "[" + getKey() + "]";
        }
        return iToString;
    }
}
