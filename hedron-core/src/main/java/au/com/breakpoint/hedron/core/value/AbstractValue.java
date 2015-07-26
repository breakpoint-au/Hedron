//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.core.value;

import au.com.breakpoint.hedron.core.HcUtil;

public abstract class AbstractValue<T> implements IValue<T>
{
    @Override
    public boolean equals (final Object o)
    {
        boolean isEqual = false;
        if (this == o)
        {
            isEqual = true;
        }
        else if (o != null && getClass () == o.getClass ())
        {
            final AbstractValue<T> eRhs = HcUtil.uncheckedCast (o);

            isEqual = get ().equals (eRhs.get ());
        }

        return isEqual;
    }

    @Override
    public int hashCode ()
    {
        return get ().hashCode ();
    }

    @Override
    public String toString ()
    {
        return get ().toString ();
    }
}
