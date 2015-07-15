package au.com.breakpoint.hedron.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TreeCollection<T>
{
    public TreeCollection (final T value, final List<TreeCollection<T>> children)
    {
        m_value = value;
        m_children.addAll (children);
    }

    @SafeVarargs
    public TreeCollection (final T value, final TreeCollection<T>... children)
    {
        this (value, Arrays.asList (children));
    }

    public Stream<TreeCollection<T>> flattened ()
    {
        final Stream<TreeCollection<T>> flatChildrenStream =
            m_children.stream ().flatMap (TreeCollection<T>::flattened);
        return Stream.concat (Stream.of (this), flatChildrenStream);
    }

    public List<TreeCollection<T>> getChildren ()
    {
        return Collections.unmodifiableList (m_children);
    }

    public T getValue ()
    {
        return m_value;
    }

    public void recurse (final Consumer<TreeCollection<T>> action, final boolean topDown)
    {
        if (topDown)
        {
            action.accept (this);
            recurseTopDown (m_children, action);
        }
        else
        {
            recurseBottomUp (m_children, action);
            action.accept (this);
        }
    }

    public static <T> TreeCollection<T> of (final T value, final List<TreeCollection<T>> children)
    {
        return new TreeCollection<T> (value, children);
    }

    @SafeVarargs
    public static <T> TreeCollection<T> of (final T value, final TreeCollection<T>... children)
    {
        return new TreeCollection<T> (value, children);
    }

    private static <T> void recurseBottomUp (final List<TreeCollection<T>> childNodes,
        final Consumer<TreeCollection<T>> action)
    {
        for (final TreeCollection<T> n : childNodes)
        {
            recurseBottomUp (n.m_children, action);
            action.accept (n);
        }
    }

    private static <T> void recurseTopDown (final List<TreeCollection<T>> childNodes,
        final Consumer<TreeCollection<T>> action)
    {
        for (final TreeCollection<T> n : childNodes)
        {
            action.accept (n);
            recurseTopDown (n.m_children, action);
        }
    }

    private final List<TreeCollection<T>> m_children = new ArrayList<> ();

    private final T m_value;
}
