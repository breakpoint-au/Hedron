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
package au.com.breakpoint.hedron.core.value;

import java.util.function.Supplier;

/**
 * Immutable. Opposite calculation policy to LazyValue. Exists really only to allow
 * dynamic policy at runtime. If policy is known at compile time, you can use HeldValue
 * instead.
 */
public class EagerValue<T> extends HeldValue<T>
{
    /**
     * Eager evaluation constructor.
     *
     * @param evaluator
     */
    public EagerValue (final Supplier<T> evaluator)
    {
        super (evaluator.get ());
    }

    public static <T> EagerValue<T> of (final Supplier<T> evaluator)
    {
        return new EagerValue<T> (evaluator);
    }
}
