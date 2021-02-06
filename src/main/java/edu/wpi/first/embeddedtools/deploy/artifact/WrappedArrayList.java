package edu.wpi.first.embeddedtools.deploy.artifact;

import java.util.ArrayList;
import java.util.Collection;

import org.gradle.api.Action;

import groovy.lang.Closure;
import edu.wpi.first.embeddedtools.ActionWrapper;

public class WrappedArrayList<T> extends ArrayList<Action<T>> {
    private static final long serialVersionUID = -7867949793855347981L;

    public WrappedArrayList() {
        super();
    }

    public WrappedArrayList(Collection<Action<T>> c) {
        super(c);
    }

    public WrappedArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public WrappedArrayList<T> leftShift(Closure<T> closure) {
        Action<T> wrapper = new ActionWrapper<T>(closure);
        this.add(wrapper);
        return this;
    }
}
