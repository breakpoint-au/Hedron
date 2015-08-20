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
package au.com.breakpoint.hedron.core.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

/**
 * Encapsulation of recursive execution of a prepared set of tasks using JDK fork-join.
 *
 * @param <TArg>
 *            Type of input data to the concurrent tasks
 * @param <TReturn>
 *            Type of data returned by the concurrent tasks
 */
public class ConcurrentTask<TArg, TReturn> extends RecursiveTask<List<TReturn>>
{
    public ConcurrentTask (final List<TArg> inputs, final Function<TArg, TReturn> work)
    {
        m_inputs = inputs;
        m_work = work;
    }

    @Override
    protected List<TReturn> compute ()
    {
        List<TReturn> result = null;

        if (m_inputs.size () == 1)
        {
            // Task is small enough to compute in this thread.
            final TArg input = m_inputs.get (0);
            result = Arrays.asList (m_work.apply (input));
        }
        else
        {
            // Keep splitting until we have forked for every input value.
            final int halfwayPoint = m_inputs.size () / 2;

            final List<TArg> firstHalf = m_inputs.subList (0, halfwayPoint);
            final ConcurrentTask<TArg, TReturn> f1 = new ConcurrentTask<> (firstHalf, m_work);
            f1.fork ();

            final List<TArg> secondHalf = m_inputs.subList (halfwayPoint, m_inputs.size ());
            final ConcurrentTask<TArg, TReturn> f2 = new ConcurrentTask<> (secondHalf, m_work);

            result = new ArrayList<> ();
            result.addAll (f2.compute ());// compute this subtask in the current thread
            result.addAll (f1.join ());// join the results of the forked subtask
        }

        return result;
    }

    private final List<TArg> m_inputs;

    private final Function<TArg, TReturn> m_work;

    private static final long serialVersionUID = 5522750171646228206L;
}
