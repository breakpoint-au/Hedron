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

/**
 * For those occasions where you need a mutable holder for an out parameter.
 *
 * @param <T>
 */
public class MutableHolder<T>
{
    public MutableHolder ()
    {
    }

    public MutableHolder (final T value)
    {
        m_value = value;
    }

    public T getValue ()
    {
        return m_value;
    }

    public void setValue (final T value)
    {
        m_value = value;
    }

    public static <T> MutableHolder<T> of ()
    {
        return new MutableHolder<> ();
    }

    public static <T> MutableHolder<T> of (final T value)
    {
        return new MutableHolder<> (value);
    }

    private T m_value;
}
