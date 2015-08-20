//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
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
