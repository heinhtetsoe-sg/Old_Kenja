package jp.co.alp.kenja.common.util;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionWrapper<E> {
    Collection<E> _coll;
    public CollectionWrapper(final Collection<E> coll) {
        set(coll);
    }
    public CollectionWrapper() {
        set(new ArrayList<E>());
    }
    public void set(final Collection<E> coll) {
        _coll = coll;
    }
    public Collection<E> forAdd() {
        return _coll;
    }
    public void add(final E obj) {
        forAdd().add(obj);
    }
    public void addAll(final CollectionWrapper<E> wrapper) {
        forAdd().addAll(wrapper._coll);
    }
    public int size() {
        return _coll.size();
    }
}