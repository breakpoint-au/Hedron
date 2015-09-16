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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Spliterators;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.w3c.dom.Node;
import au.com.breakpoint.hedron.core.Tuple.E2;
import au.com.breakpoint.hedron.core.Tuple.E3;
import au.com.breakpoint.hedron.core.concurrent.Concurrency;
import au.com.breakpoint.hedron.core.concurrent.ConcurrentTask;
import au.com.breakpoint.hedron.core.context.ExecutionScopes;
import au.com.breakpoint.hedron.core.context.FaultException;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.log.Logging;
import au.com.breakpoint.hedron.core.value.AbstractValue;
import au.com.breakpoint.hedron.core.value.IValue;
import au.com.breakpoint.hedron.core.value.SafeLazyValue;

/**
 * Collection of diverse but useful methods.
 */
public class HcUtil
{
    /**
     * Lazy duration formatting support.
     */
    public static class FormattedNanoseconds extends AbstractValue<String>
    {
        public FormattedNanoseconds (final long ns)
        {
            m_nanoseconds = ns;
        }

        @Override
        public String get ()
        {
            // Calculate the value if not already done. Does nothing if already calculated.
            if (m_value == null)
            {
                m_value = formatNanoseconds (m_nanoseconds);
            }

            return m_value;
        }

        private final long m_nanoseconds;

        private String m_value;
    }

    public static String abbreviate (final String s, final int maxLength)
    {
        String a = s;

        if (HcUtil.safeGetLength (s) > maxLength)
        {
            final int halfSize = (maxLength - 3 + 1) / 2;
            if (halfSize > 1)
            {
                final String firstBit = s.substring (0, halfSize);
                final String lastBit = s.substring (s.length () - halfSize);

                a = String.format ("%s...%s", firstBit, lastBit);
            }
        }

        return a;
    }

    /**
     * @param task
     *            Task to run on shutdown.
     * @param priority
     *            Default priority is 100. Use smaller values to run earlier, larger to
     *            run later.
     * @return
     */
    public static ResourceScope<?> addShutdownTask (final ResourceScope<?> a, final int priorityValue)
    {
        synchronized (_m_shutdownTasks)
        {
            _m_shutdownTasks.put (priorityValue, a);
        }

        return a;
    }

    public static ResourceScope<?> addShutdownTask (final ResourceScope<?> a, final ShutdownPriority priority)
    {
        return addShutdownTask (a, priority.getValue ());
    }

    /**
     * @param task
     *            Task to run on shutdown.
     * @param priority
     *            Default priority is 100. Use smaller values to run earlier, larger to
     *            run later.
     * @return
     */
    public static ResourceScope<?> addShutdownTask (final Runnable task, final int priorityValue, final String details)
    {
        final ResourceScope<Runnable> a = ResourceScope.of (task, Runnable::run, details);
        return addShutdownTask (a, priorityValue);
    }

    public static ResourceScope<?> addShutdownTask (final Runnable task, final ShutdownPriority priority,
        final String details)
    {
        return addShutdownTask (task, priority.getValue (), details);
    }

    /**
     * Generic routine to take a list of entities representing the desired state of the
     * collection, and determines the required inserts, updates, and deletes to transform
     * the collection into that desired state.
     *
     * @param <T>
     *            Type of the collection data entity
     * @param before
     *            Collection of entities representing the current state of the collection
     * @param after
     *            Collection of entities representing the required state of the collection
     * @return Tuple containing the collections of actions to be performed. Element 0
     *         holds the entities to be inserted. Element 1 holds the entities to be
     *         updated (ie the new entity values). Element 2 holds the entities to be
     *         deleted.
     */
    public static <K, T extends IIdentifiable<K>> E3<List<T>, List<T>, List<T>> analyseDifferences (
        final List<T> before, final List<T> after)
    {
        // For quicker analysis, transfer the before and after lists to maps, keyed by
        // the entity primary key.
        final Map<K, T> mapBefore = toMap (before);
        final Map<K, T> mapAfter = toMap (after);

        // The records to be inserted are those that are in after but not in before.
        final List<T> listInsert = filter (after, e -> mapBefore.get (e.getPrimaryKey ()) == null);

        // The records to be updated exist in both but are different.
        final Predicate<? super T> updateComparator = e ->
        {
            final T eFromMap = mapBefore.get (e.getPrimaryKey ());
            return eFromMap != null && !eFromMap.equals (e);
        };
        final List<T> listUpdate = filter (after, updateComparator);

        // The records to be deleted are those that are in before but not in after.
        final List<T> listDelete = filter (before, e -> mapAfter.get (e.getPrimaryKey ()) == null);

        return E3.of (listInsert, listUpdate, listDelete);
    }

    public static <TOutput> Supplier<TOutput> asSupplier (final Callable<? extends TOutput> c)
    {
        return () ->
        {
            TOutput output = null;

            try
            {
                output = c.call ();
            }
            catch (final Throwable e)
            {
                // Propagate exception as unchecked fault up to the fault barrier. Use
                // throwRootFault to handle the caught exception according to its type.
                ThreadContext.throwFault (e);
            }

            return output;
        };
    }

    public static Supplier<Void> asSupplier (final Runnable r)
    {
        return () ->
        {
            r.run ();
            return null;
        };
    }

    public static void awaitShutdownExecutorService (final ExecutorService executorService, final String name,
        final long timeout, final TimeUnit unit)
    {
        try
        {
            executorService.awaitTermination (timeout, unit);
        }
        catch (final InterruptedException e)
        {
            // No logging after here since threadpool is shutdown.
        }

        Logging.logDebug ("....shutdown ExecutorService [%s] finished", name);
    }

    public static final String buildString (final char c, final int length)
    {
        String s = "";

        if (length > 0)
        {
            final StringBuilder b = new StringBuilder (length);
            b.setLength (length);

            for (int i = 0; i < length; ++i)
            {
                b.setCharAt (i, c);
            }

            s = b.toString ();
        }

        return s;
    }

    public static long calculateAverage (final long total, final long count)
    {
        return count <= 0 ? 0 : total / count;
    }

    public static double calculateAverageMsec (final long totalNs, final long count)
    {
        return count <= 0 ? 0.0 : HcUtil.nsToMsecDouble ((double) totalNs / (double) count);
    }

    public static double calculatePercentage (final long count, final long total)
    {
        return count <= 0 ? 0.0 : (double) count / (double) total * 100.0;
    }

    public static String caseBreakToUnderScoreBreak (final String logicalName)
    {
        final StringBuilder sb = new StringBuilder ();

        final char[] chars = logicalName.toCharArray ();

        boolean prevWasLowerCase = false;
        for (int i = 0; i < chars.length; ++i)
        {
            final char c = chars[i];
            final char uc = Character.toUpperCase (c);

            if (Character.isLowerCase (c))
            {
                sb.append (uc);
                prevWasLowerCase = true;
            }
            else
            {
                // Handle ABCBlah as ABC_BLAH
                if (Character.isUpperCase (
                    c) && prevWasLowerCase || Character.isUpperCase (c) && i > 0 && i < chars.length - 1 && Character
                        .isLowerCase (chars[i + 1]))
                {
                    sb.append ('_');
                    sb.append (uc);
                }
                else
                {
                    sb.append (uc);
                }

                prevWasLowerCase = false;
            }
        }

        return sb.toString ();
    }

    /**
     * Lazy-evaluating Java version of SQL COALESCE.
     *
     * @param ts
     *            value supplier functions, evaluated as needed
     * @return the first non-null value or null if none.
     */
    @SafeVarargs
    public static <T> T coalesce (final Supplier<T>... ts)
    {
        return asList (ts)// collection
            .stream ()// stream
            .map (Supplier::get)// get the data from supplier
            .filter (Objects::nonNull)// avoid null entries
            .findFirst ()// stop at the first
            .orElse (null);
    }

    /**
     * Java version of SQL COALESCE.
     *
     * @param ts
     *            values to be considered
     * @return the first non-null value or null if none.
     */
    @SafeVarargs
    public static <T> T coalesce (final T... ts)
    {
        return asList (ts)// collection
            .stream ()// stream
            .filter (Objects::nonNull)// avoid null entries
            .findFirst ()// stop at first
            .orElse (null);
    }

    public static List<E3<String, Object, Object>> compareProperties (final Object lhs, final Object rhs,
        final boolean strict)
    {
        final List<E3<String, Object, Object>> diffs = GenericFactory.newArrayList ();

        PropertyDescriptor[] sourceProperties = null;
        PropertyDescriptor[] targetProperties = null;
        try
        {
            sourceProperties = Introspector.getBeanInfo (lhs.getClass ()).getPropertyDescriptors ();
            targetProperties = Introspector.getBeanInfo (rhs.getClass ()).getPropertyDescriptors ();
        }
        catch (final IntrospectionException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        for (final PropertyDescriptor sp : sourceProperties)
        {
            final String name = sp.getName ();
            final PropertyDescriptor pd = findProperty (name, targetProperties);
            if (pd == null)
            {
                ThreadContext.assertFault (!strict, "Cannot find property [%s] in class [%s]", name,
                    rhs.getClass ().getCanonicalName ());
            }
            else if (!name.equals ("class"))
            {
                final Method readLeft = sp.getReadMethod ();
                final Method readRight = pd.getReadMethod ();
                if (readLeft != null && readRight != null)
                {
                    // Get from source and set in target.
                    try
                    {
                        final Object valueLeft = readLeft.invoke (lhs);
                        final Object valueRight = readRight.invoke (rhs);
                        if (!objectEquals (valueLeft, valueRight))
                        {
                            // Different. Record the details.
                            diffs.add (E3.of (name, valueLeft, valueRight));
                        }
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        // Propagate exception as unchecked fault up to the fault barrier.
                        ThreadContext.throwFault (e);
                    }
                }
            }
        }

        return diffs;
    }

    public static boolean contains (final byte[] a, final byte o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final double[] a, final double o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final float[] a, final float o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final int[] a, final int o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final long[] a, final long o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final Object[] a, final Object o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean contains (final short[] a, final short o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEquals (a[i], o);
        }

        return does;
    }

    public static boolean containsIgnoreCase (final String[] a, final String o)
    {
        boolean does = false;

        for (int i = 0; !does && i < a.length; ++i)
        {
            does = safeEqualsIgnoreCase (a[i], o);
        }

        return does;
    }

    public static boolean containsWildcard (final String[] regexps, final String o)
    {
        boolean does = false;

        for (int i = 0; !does && i < regexps.length; ++i)
        {
            does = HcUtil.wildcardMatches (regexps[i], o);
        }

        return does;
    }

    public static String contextFormatMessage (final String format, final Object... parameters)
    {
        return String.format (format, parameters);
    }

    /**
     * In a Java service run under the Java Service Wrapper, this will run once on
     * System.exit (), which the JSW uses to stop the service. In a standalone program
     * running ExecutionScopes.executeProgram () or indirectly through
     * Instrumentation.executeProgram (), it will be called twice, both explicitly and via
     * System.exit () but there is nothing left for it to do since the stored CloseAction
     * entities tolerate multiple calls to close (), and _m_shutdownTasks is cleared
     * anyway.
     */
    public static void coordinateShutdown ()
    {
        synchronized (_m_shutdownTasks)
        {
            // Perform application level shutdown actions in priority order (low
            // to high priority number).
            for (final Entry<Integer, List<ResourceScope<?>>> entry : _m_shutdownTasks.getMap ().entrySet ())
            {
                for (final ResourceScope<?> r : entry.getValue ())
                {
                    final Integer priority = entry.getKey ();
                    final Object resource = r.get ();

                    Logging.logDebug ("..shutdown task [%s] [%s] [%s] starting", priority,
                        resource.getClass ().getSimpleName (), r.getDetails ());

                    ExecutionScopes.executeFaultBarrier (r::close);

                    Logging.logDebug ("..shutdown task [%s] [%s] [%s] finished", priority,
                        resource.getClass ().getSimpleName (), r.getDetails ());
                }
            }

            _m_shutdownTasks.clear ();

            Logging.logDebug ("..shutdown tasks complete");
        }
    }

    public static int countLeadingSpaces (final String s)
    {
        final char[] a = s.toCharArray ();

        int i;
        for (i = 0; i < a.length && a[i] == ' '; ++i)
        {
        }

        return i;
    }

    /** Varargs wrapper */
    public static int deepHashCode (final Object... objects)
    {
        return Arrays.deepHashCode (objects);
    }

    /** Varargs wrapper */
    public static String deepToString (final Object... objects)
    {
        return Arrays.deepToString (objects);
    }

    public static E2<long[], long[]> deinterleave (final long[] counts)
    {
        final int size = counts.length / 2;
        final long[] cs = new long[size];
        final long[] vs = new long[size];

        for (int i = 0, j = 0; i < counts.length; i += 2, ++j)
        {
            cs[j] = counts[i];
            vs[j] = counts[i + 1];
        }

        return E2.of (cs, vs);
    }

    /**
     * Simple comparator for sorting etc in descending order. Generic type T must
     * implement Comparable<T> so that the compareTo method can be used for sorting.
     */
    public static <T extends Comparable<T>> Comparator<T> descendingComparator ()
    {
        final Comparator<T> comparator = (lhs, rhs) -> rhs.compareTo (lhs);

        return comparator;
    }

    /**
     * Simple comparator for sorting etc in descending order based on data extracted from
     * the objects being sorted / compared. The data extracted must be of type TField.
     * TField must implement Comparable<T> so that the compareTo method can be used for
     * sorting based on the extracted values.
     */
    @SafeVarargs
    public static <T> Comparator<T> descendingComparator (final Function<T, ? extends Comparable<?>>... extractors)
    {
        @SuppressWarnings ("unchecked")
        final Comparator<T> comparator = (lhs, rhs) ->
        {
            int result = 0;// equal

            // Keep going while equal to subsort by the extractors in order.
            for (int i = 0; result == 0 && i < extractors.length; ++i)
            {
                final Function<T, ? extends Comparable<?>> ex = extractors[i];

                @SuppressWarnings ("rawtypes")
                final Comparable fieldLhs = ex.apply (lhs);

                @SuppressWarnings ("rawtypes")
                final Comparable fieldRhs = ex.apply (rhs);

                result = fieldRhs.compareTo (fieldLhs);
            }

            return result;
        };

        return comparator;
    }

    /**
     * Copy-constructs (clones) a byte array.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     * @return The duplicate.
     */
    public static byte[] duplicate (final byte[] rhs)
    {
        // byte[] lhs = null;
        // if (rhs != null)
        // {
        // lhs = new byte[rhs.length];
        // System.arraycopy (rhs, 0, lhs, 0, rhs.length);
        // }
        //
        // return lhs;

        return rhs.clone ();
    }

    /**
     * Copy-constructs (clones) a Timestamp, which doesn't fully support Object.clone ().
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     * @return The duplicate.
     */
    public static Timestamp duplicate (final Timestamp rhs)
    {
        Timestamp lhs = null;
        if (rhs != null)
        {
            lhs = new Timestamp (rhs.getTime ());
            lhs.setNanos (rhs.getNanos ());
        }

        return lhs;
    }

    /**
     * Execute a command under Windows.
     *
     * <code>
     * final String[] cmd = new String[]
     *   {
     *           HG_EXE, "tag", "-R", m_repositoryPath, "--force", tagName
     *   };
     *   final String output = HcUtil.executeCommand (txn, cmd, WORKING_DIR);
     * </code>
     *
     * @param commandAndArgs
     * @param pathWorkingDirectory
     * @return
     */
    public static E2<String, String> executeCommand (final String[] commandAndArgs, final String pathWorkingDirectory)
    {
        String sysout = null;
        String syserr = null;

        final File dir = new File (pathWorkingDirectory);

        try
        {
            final Process process = Runtime.getRuntime ().exec (commandAndArgs, null, dir);

            sysout = readStringFromStream (process.getInputStream ());
            syserr = readStringFromStream (process.getErrorStream ());

            // No more output was available from the process, so...
            // Ensure that the process completes
            process.waitFor ();
        }
        catch (final IOException | InterruptedException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        Logging.logDebug ("Command %s result [%s]", Arrays.toString (commandAndArgs), sysout);

        return E2.of (sysout, syserr);
    }

    /**
     * A simple encapsulation of execution of a prepared set of tasks using the specified
     * executor, waiting for completion.
     *
     * @param executor
     *            Executor used to execute the tasks
     * @param tasks
     *            Tasks to be executed
     * @param inCompletionOrder
     *            If true, returns the results in the order they complete (using a
     *            CompletionService). If false, returns the results in the order of the
     *            tasks.
     *
     * @return The results returned by the tasks
     */
    public static <T> List<T> executeConcurrently (final ExecutorService executor,
        final List<? extends Callable<T>> tasks, final boolean inCompletionOrder)
    {
        final List<T> results = GenericFactory.newArrayList ();

        if (inCompletionOrder)
        {
            // Keep results in the order of completion... this is good for finding exceptions early.
            final int count = tasks.size ();

            final CompletionService<T> completionService = new ExecutorCompletionService<T> (executor);
            for (final Callable<T> task : tasks)
            {
                // Note: this is not a fault barrier; any exceptions will flow back via FutureTask.get ()
                // but ExecutionScopes.execute () translates to FaultException.
                completionService.submit ( () -> HcUtil.handleCall (task));
            }

            for (int i = 0; i < count; ++i)
            {
                try
                {
                    final Future<T> f = completionService.take ();
                    results.add (waitForFuture (f));
                }
                catch (final InterruptedException e)
                {
                    // Propagate exception as unchecked fault up to the fault barrier.
                    ThreadContext.throwFault (e);
                }
            }
        }
        else
        {
            // Keep results in the order of tasks.
            final List<Future<T>> futures = GenericFactory.newArrayList ();
            for (final Callable<T> task : tasks)
            {
                // Note: this is not a fault barrier; any exceptions will flow back via FutureTask.get ()
                // but ExecutionScopes.execute () translates to FaultException.
                futures.add (executor.submit ( () -> HcUtil.handleCall (task)));
            }

            for (final Future<T> f : futures)
            {
                results.add (waitForFuture (f));
            }
        }

        return results;
    }

    public static <T> List<T> executeConcurrently (final int countTasks, final Function<Integer, T> action,
        final int nrThreads, final boolean inCompletionOrder)
    {
        // Equivalent but specious.
        //
        //        final Function<? super Integer, ? extends Callable<T>> mapper = i ->
        //        {
        //            final Callable<T> e = () -> action.apply (i);
        //            return e;
        //        };
        //
        //        final List<Callable<T>> tasks =
        //            IntStream.range (0, countTasks)
        //                .boxed ()
        //                .map (mapper)
        //                .collect (toList ());

        final List<Callable<T>> tasks = GenericFactory.newArrayList ();
        for (int i = 0; i < countTasks; ++i)
        {
            final int v = i;
            tasks.add ( () -> action.apply (v));
        }

        return HcUtil.executeConcurrently (tasks, nrThreads, inCompletionOrder);
    }

    /**
     * A simple encapsulation of execution of a prepared set of tasks using a thread pool,
     * waiting for completion, and then shutting down the threadpool.
     *
     * @param tasks
     *            Tasks to be executed
     * @param nrThreads
     *            Number of threads to be created in the thread pool
     * @param inCompletionOrder
     *            If true, returns the results in the order they complete (using a
     *            CompletionService). If false, returns the results in the order of the
     *            tasks.
     *
     * @return The results returned by the tasks
     */
    public static <T> List<T> executeConcurrently (final List<? extends Callable<T>> tasks, final int nrThreads,
        final boolean inCompletionOrder)
    {
        List<T> results = null;

        try (final ResourceScope<ExecutorService> ch =
            Concurrency.createFixedThreadPool (nrThreads, "HcUtil.executeConcurrently", false, false))
        {
            results = executeConcurrently (ch.get (), tasks, inCompletionOrder);
        }

        return results;
    }

    /**
     * A simple encapsulation of execution of a prepared set of tasks using JDK 7
     * fork-join.
     *
     * @param inputs
     * @param work
     * @return
     */
    public static <TArg, TReturn> List<TReturn> executeConcurrently (final List<TArg> inputs,
        final Function<TArg, TReturn> work)
    {
        List<TReturn> results = null;

        final ConcurrentTask<TArg, TReturn> task = new ConcurrentTask<> (inputs, work);

        try (final ResourceScope<ForkJoinPool> ch =
            Concurrency.createForkJoinPool ("HcUtil.executeConcurrentlyFj", false))
        {
            final ForkJoinPool pool = ch.get ();
            results = pool.invoke (task);
        }

        return results;

        //    final ConcurrentTask<TArg, TReturn> task = new ConcurrentTask<> (inputs, work);
        //    final ForkJoinPool pool = new ForkJoinPool ();
        //    return pool.invoke (task);
    }

    /**
     * Concurrently run a task. Useful for unit testing concurrent code, if nothing else.
     *
     * @param task
     * @param nrThreads
     * @param nrTasks
     */
    public static void executeManyConcurrently (final Runnable runnable, final int nrThreads, final int nrTasks)
    {
        // Lots of working threads. The Concurrency.createXxxx methods handled scheduler shutdown.
        try (final ResourceScope<ExecutorService> ch =
            Concurrency.createFixedThreadPool (nrThreads, "HcUtil.executeManyConcurrently", false, false))
        {
            final Callable<Void> task = () ->
            {
                ((Runnable) runnable::run).run ();
                return null;
            };

            final List<Callable<Void>> tasks = GenericFactory.newArrayList ();
            for (int i = 0; i < nrTasks; ++i)
            {
                tasks.add (task);// same task concurrently
            }

            HcUtil.waitForTasks (ch.get (), tasks);
        }
    }

    /**
     * Concurrently run a task. Useful for unit testing concurrent code, if nothing else.
     *
     * @param runnable
     *            The task
     * @param threadCount
     *            Number of threads to hit it with
     * @param msecDuration
     *            How long to run for
     */
    public static void executeManyConcurrentlyFor (final Runnable runnable, final int threadCount,
        final int msecDuration, final int msecPause)
    {
        final long stopTimeNs = System.nanoTime () + msecToNs (msecDuration);

        // Lots of polling threads. Handle scheduler shutdown via try-with-resources.
        try (final ResourceScope<ExecutorService> ch =
            Concurrency.createFixedThreadPool (threadCount, "HcUtil.executeManyConcurrentlyFor", false, false))
        {
            // Collection of futures to wait for producer and consumer to finish.
            final List<Future<Void>> results = GenericFactory.newArrayList ();

            // Kick off consumers.
            for (long i = 0; i < threadCount; ++i)
            {
                final Callable<Void> task = () ->
                {
                    do
                    {
                        pause (msecPause);
                        runnable.run ();
                    }
                    while (nsIsBefore (System.nanoTime (), stopTimeNs));

                    return null;
                };

                final ExecutorService executorService = ch.get ();
                final Future<Void> future = executorService.submit (task);
                results.add (future);
            }

            // Wait for the threads to finish.
            waitForFutures (results);
        }
    }

    /**
     * @param task
     *            Work to happen asynchronously
     */
    public static void executeSerialAsync (final Runnable task)
    {
        // Just log exceptions and keep going so other async tasks are not killed.
        m_serialAsyncExecutor.get ().execute ( () -> ExecutionScopes.executeFaultBarrier (task));
    }

    /**
     * Returns <code>true</code> if the predicate holds for at least one of the elements
     * of this array, <code>false</code> otherwise (<code>false</code> for the empty
     * array).
     *
     * @param f
     *            the predicate function to test on the elements of this array.
     * @return <code>true</code> if the predicate holds for at least one of the elements
     *         of this array.
     */
    public static <T> boolean exists (final List<T> as, final Predicate<? super T> criterion)
    {
        return find (as, criterion) != null;
    }

    public static <T> boolean exists (final List<T> as, final T a)
    {
        return as.indexOf (a) != -1;
    }

    public static boolean existsIgnoreCase (final List<String> values, final String value)
    {
        final Predicate<String> criterion = v -> v.equalsIgnoreCase (value);// un-inlined to help javac
        return exists (values, criterion);
    }

    /**
     * Filters a standard java.util.List.
     *
     * @param <T>
     *            Type of objects in the list
     * @param list
     *            The list
     * @param criterion
     *            Function object that recognises the required object
     * @return The filtered list.
     */
    public static <T> List<T> filter (final List<T> list, final Predicate<? super T> criterion)
    {
        return list// collection
            .stream ()// stream
            .filter (criterion)// apply filter
            .collect (toList ());
    }

    /**
     * Finds an object in a List.
     *
     * @param <T>
     *            Type of objects in the list
     * @param list
     *            The list
     * @param criterion
     *            Function object that recognises the required object
     * @return Required object or null.
     */
    public static <T> T find (final List<T> list, final Predicate<? super T> criterion)
    {
        return list.stream ()// stream
            .filter (criterion)// apply filter
            .findFirst ()// stop at first
            .orElse (null);
    }

    public static String findAncestralDirectory (final String startingDir, final String requiredDirName)
    {
        String s = null;

        final String slashed = toSlashPath (startingDir) + "/";
        final int i = slashed.toLowerCase ().lastIndexOf ("/" + requiredDirName.toLowerCase () + "/");

        if (i != -1)
        {
            s = slashed.substring (0, i + requiredDirName.length () + 1);
        }

        return s;
    }

    public static PropertyDescriptor findProperty (final String name, final PropertyDescriptor[] ps)
    {
        final PropertyDescriptor result = find (Arrays.asList (ps), v -> v.getName ().equals (name));

        return result;
    }

    public static String formatBytes (final long bytes)
    {
        String s = null;

        if (bytes < 1000L)
        {
            s = String.format ("%s bytes", bytes);
        }
        else if (bytes < 1000000L)
        {
            final double truncated = (bytes / 100L) * 100L;
            final double v = truncated / 1000.0;
            s = String.format ("%.1f KB", v);
        }
        else if (bytes < 1000000000L)
        {
            final double truncated = (bytes / 100000L) * 100000L;
            final double v = truncated / 1000000.0;
            s = String.format ("%.1f MB", v);
        }
        else if (bytes < 1000000000000L)
        {
            final double truncated = (bytes / 100000000L) * 100000000L;
            final double v = truncated / 1000000000.0;
            s = String.format ("%.1f GB", v);
        }
        else
        // if (bytes < 1000000000000000L)
        {
            final double truncated = (bytes / 100000000000L) * 100000000000L;
            final double v = truncated / 1000000000000.0;
            s = String.format ("%.1f TB", v);
        }

        return s;
    }

    public static String formatIntegralDate (final Date date)
    {
        final Calendar calendar = new GregorianCalendar ();
        calendar.setTime (date);

        // Get the components of the date
        final int year = calendar.get (Calendar.YEAR);// e.g. 2002
        final int month = calendar.get (Calendar.MONTH) + 1;// 0=Jan, 1=Feb, ...
        final int day = calendar.get (Calendar.DAY_OF_MONTH);// 1...

        return String.format ("%04d%02d%02d", year, month, day);
    }

    public static String formatMilliseconds (final long ms)
    {
        return formatNanoseconds (ms * 1_000_000L);
    }

    public static String formatNanoseconds (final long ns)
    {
        String s;

        final long NS_ONE_MICROSECOND = 1_000L;
        final long NS_ONE_MILLISECOND = 1_000L * NS_ONE_MICROSECOND;
        final long NS_ONE_SECOND = 1_000 * NS_ONE_MILLISECOND;
        final long NS_ONE_MINUTE = 60L * NS_ONE_SECOND;
        final long NS_ONE_HOUR = 60L * NS_ONE_MINUTE;
        final long NS_ONE_DAY = 24L * NS_ONE_HOUR;

        if (ns < NS_ONE_MICROSECOND)
        {
            s = String.format ("%dns", ns);
        }
        else if (ns < NS_ONE_MILLISECOND)
        {
            s = String.format ("%dus", ns / NS_ONE_MICROSECOND);
        }
        else if (ns < NS_ONE_SECOND)
        {
            s = String.format ("%dms", ns / NS_ONE_MILLISECOND);
        }
        else if (ns < NS_ONE_MINUTE)
        {
            s = String.format ("%.1fs", (double) ns / (double) NS_ONE_SECOND);
        }
        else if (ns < NS_ONE_HOUR)
        {
            s = String.format ("%.1fm", (double) ns / (double) NS_ONE_MINUTE);
        }
        else if (ns < NS_ONE_DAY)
        {
            s = String.format ("%.1fh", (double) ns / (double) NS_ONE_HOUR);
        }
        else
        {
            s = String.format ("%.1fd", (double) ns / (double) NS_ONE_DAY);
        }

        return s;
    }

    public static String formatObjects (final String headerLine, final List<?> results)
    {
        final StringBuilder sb = new StringBuilder ();
        sb.append (headerLine);

        MaxCounter.appendLines (sb, results);

        return sb.toString ();
    }

    public static String formatXsdDate (final Date date)
    {
        return getXsdDateFormat ().format (date);
    }

    public static String formatXsdDateMs (final Date date)
    {
        return getXsdDateFormatMs ().format (date);
    }

    public static String formatXsdDateSpace (final Date date)
    {
        return getXsdDateFormatSpace ().format (date);
    }

    public static String formatXsdDateSpaceMs (final Date date)
    {
        return getXsdDateFormatSpaceMs ().format (date);
    }

    public static String formFilepath (final boolean toSlash, final String directoryPath, final String... pathElements)
    {
        String fp = directoryPath;

        for (final String f : pathElements)
        {
            fp = String.format ("%s/%s", removeTrailingPathSeparator (fp), f);
        }

        return toSlash ? toSlashPath (fp) : fp;
    }

    public static String formFilepath (final String directoryPath, final String... pathElements)
    {
        return formFilepath (true, directoryPath, pathElements);
    }

    public static String getApplicationName ()
    {
        return m_applicationName;
    }

    public static <T> T getApplicationObject (final Object key)
    {
        return HcUtil.uncheckedCast (m_mapApplicationObjects.get (key));
    }

    public static <T> T getApplicationObjectAssert (final Object key)
    {
        final T o = getApplicationObject (key);
        ThreadContext.assertFault (o != null,
            "There is no stored object with key [%s]. You need to call HcUtil.putApplicationObject ([%s], [value]) first",
            key, key);
        return o;
    }

    public static String getApplicationVersion ()
    {
        return m_applicationVersion;
    }

    public static byte getByteValue (final Object obj)
    {
        final long v = getLongValue (obj);
        ThreadContext.assertError (v >= Byte.MIN_VALUE, "Byte value cannot be less than [%s]", Byte.MIN_VALUE);
        ThreadContext.assertError (v <= Byte.MAX_VALUE, "Byte value cannot be greater than [%s]", Byte.MAX_VALUE);
        return (byte) v;
    }

    public static String getCanonicalFilepath (final File file)
    {
        String fp = null;
        try
        {
            fp = file.getCanonicalPath ();
        }
        catch (final IOException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return fp;
    }

    public static String getCanonicalFilepath (final String filepath)
    {
        return getCanonicalFilepath (new File (filepath));
    }

    /**
     * Looks through the cause chain of an exception for an causing instance of a
     * specified exception.
     *
     * @param e
     *            the top leve exception to be examined
     * @param targetClass
     *            class of the exception being checked for
     * @return the instance of the specified exception, or null
     */
    public static <T extends Throwable> T getCause (Throwable e, final Class<? extends T> targetClass)
    {
        Throwable target = null;

        // Traverse the exception hierarchy.
        for (; target == null && e != null; e = e.getCause ())
        {
            final Class<? extends Throwable> eClass = e.getClass ();
            if (eClass == targetClass)
            {
                target = e;
            }
        }

        return uncheckedCast (target);
    }

    /**
     * Looks through the cause chain of an exception for an causing instance of a
     * specified exception or its subclasses.
     *
     * @param e
     *            the top leve exception to be examined
     * @param targetClass
     *            class of the exception being checked for
     * @return the instance of the specified exception, or null
     */
    public static <T extends Throwable> T getCauseAllowSubclass (Throwable e, final Class<? extends T> targetClass)
    {
        Throwable target = null;

        // Traverse the exception hierarchy.
        for (; target == null && e != null; e = e.getCause ())
        {
            final Class<? extends Throwable> eClass = e.getClass ();
            if (targetClass.isAssignableFrom (eClass)) // targetClass same class or super
            {
                target = e;
            }
        }

        return uncheckedCast (target);
    }

    public static <T> Class<T> getClassObject (final String className)
    {
        Class<T> serviceClass = null;
        try
        {
            serviceClass = uncheckedCast (Class.forName (className));
        }
        catch (final ClassNotFoundException e)
        {
            // Propagate transaction exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return serviceClass;
    }

    public static String getCommandLine ()
    {
        //            final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean ();
        //            final List<String> jvmArgs = bean.getInputArguments ();
        //
        //            for (int i = 0; i < jvmArgs.size (); i++)
        //            {
        //                System.out.println (jvmArgs.get (i));
        //            }

        final String commandLine =
            " -classpath " + System.getProperty ("java.class.path") + " " + System.getProperty ("sun.java.command");
        return commandLine;
    }

    public static String getCurrentDirectory ()
    {
        return new File (".").getAbsolutePath ();
    }

    /**
     * Compares two lists and returns the differences as a tuple, consisting of a list of
     * the left-only items then a list of the shared items then a list of the right-only
     * items.
     *
     * @param <T>
     *            Type of the data item.
     * @param lhs
     *            The 'left' list being compared.
     * @param rhs
     *            The 'right' list being compared.
     * @return The comparison tuple.
     */
    public static <T> E3<List<T>, List<T>, List<T>> getDifferences (final List<T> lhs, final List<T> rhs)
    {
        // Naive implementation. 638 times too slow.
        //
        //TimedScope  msecMax
        //HcUtilGetDifferencesTest.getDifferencesCustom   18.350371   1
        //HcUtilGetDifferencesTest.getDifferencesListBased    12546.2199  683.7038825
        //HcUtilGetDifferencesTest.getDifferencesSetBased 9648.398139 525.7876333
        //
        //
        //final List<T> leftOnly = GenericFactory.newArrayList (lhs);
        //leftOnly.removeAll (rhs);
        //
        //final List<T> shared = GenericFactory.newArrayList (lhs);
        //shared.removeAll (leftOnly);
        //
        //final List<T> rightOnly = GenericFactory.newArrayList (rhs);
        //rightOnly.removeAll (lhs);
        //
        //return E3.newInstance (leftOnly, shared, rightOnly);

        final List<T> leftOnlyList = GenericFactory.newArrayList ();
        final List<T> sharedList = GenericFactory.newArrayList ();

        final Set<T> rhsSet = GenericFactory.newHashSet (rhs);
        for (final T t : lhs)
        {
            if (rhsSet.remove (t))
            {
                // It was in the rhs, hence shared.
                sharedList.add (t);
            }
            else
            {
                // It was not in the rhs, hence left only.
                leftOnlyList.add (t);
            }
        }

        // Anything left in the rhsSet is rhs-only.
        final List<T> rightOnlyList = GenericFactory.newArrayList (rhsSet);

        return E3.of (leftOnlyList, sharedList, rightOnlyList);
    }

    public static String getDirectory (final String filepath)
    {
        final Path p = Paths.get (filepath);
        final Path parent = p.getParent ();

        return parent == null ? null : parent.toString ();
    }

    public static String getExceptionDetails (final Throwable e)
    {
        return getExceptionDetails (e, "");
    }

    public static String getExceptionDetails (final Throwable e, final String preamble)
    {
        return getExceptionDetails (e, preamble, true);
    }

    public static String getExceptionDetails (final Throwable e, final String preamble, final boolean includeContextId)
    {
        final StringBuilder result = new StringBuilder ();
        final String stackTrace = getStackTrace (e);

        final String formatSpecifier = "%s%n%s";
        final String s = includeContextId ? contextFormatMessage (formatSpecifier, preamble, stackTrace)
            : String.format (formatSpecifier, preamble, stackTrace);
        result.append (s);

        // Remove trailing newline.
        return result.toString ().trim ();
    }

    /**
     * @return name of file *or* directory.
     */
    public static String getFilename (final String filepath)
    {
        final Path p = Paths.get (filepath);
        final Path filename = p.getFileName ();

        return filename == null ? null : filename.toString ();
    }

    public static double getFractionalComponent (final double v)
    {
        return v - getIntegerComponent (v);
    }

    public static double getFractionalComponent (final float v)
    {
        return v - getIntegerComponent (v);
    }

    /**
     * Form a filepath from filename assuming current working directory.
     */
    public static String getFullCurrentFilepath (String filename)
    {
        if (!hasPath (filename))
        {
            filename = formFilepath (getCurrentDirectory (), filename);
        }

        return filename;
    }

    /**
     * Take into account that the nestedFilepath may be a relative path to the file it is
     * being included from.
     */
    public static String getFullNestedFilepath (final String parentFilepath, final String nestedFilepath)
    {
        String fullNestedFilepath = nestedFilepath;

        if (!hasPath (nestedFilepath))
        {
            final String thisParentDirectory = getDirectory (parentFilepath);
            fullNestedFilepath = formFilepath (thisParentDirectory, nestedFilepath);
        }
        return fullNestedFilepath;
    }

    public static String getHcUtilProjectDirectoryName ()
    {
        return formFilepath (getProjectsDirectoryName (), "hedron-core");
    }

    public static String getHostName ()
    {
        String hostName = null;

        try
        {
            hostName = java.net.InetAddress.getLocalHost ().getHostName ();
        }
        catch (final UnknownHostException e)
        {
            hostName = "(unknown host)";
        }

        return hostName;
    }

    public static long getIntegerComponent (final double v)
    {
        return (long) v;
    }

    public static long getIntegerComponent (final float v)
    {
        return (long) v;
    }

    public static int getIntValue (final Object obj)
    {
        final long v = getLongValue (obj);
        ThreadContext.assertError (v >= Integer.MIN_VALUE, "Int value cannot be less than [%s]", Integer.MIN_VALUE);
        ThreadContext.assertError (v <= Integer.MAX_VALUE, "Int value cannot be greater than [%s]", Integer.MAX_VALUE);
        return (int) v;
    }

    public static <K, V> List<KeyValuePair<K, V>> getKeyValuePairs (final Map<K, V> map)
    {
        final List<KeyValuePair<K, V>> kvps = GenericFactory.newArrayList ();

        for (final Entry<K, V> e : map.entrySet ())
        {
            kvps.add (KeyValuePair.of (e.getKey (), e.getValue ()));
        }

        return kvps;
    }

    public static final String getLeftPaddedString (final String source, final int width)
    {
        return getLeftPaddedString (source, width, ' ');
    }

    public static final String getLeftPaddedString (final String source, final int width, final char padding)
    {
        return source == null ? null : buildString (padding, width - source.length ()) + source;
    }

    public static <T> BinaryOperator<List<T>> getListCombiner ()
    {
        return (l, r) ->
        {
            final List<T> combined = GenericFactory.newArrayList (l);
            combined.addAll (r);

            return combined;
        };
    }

    public static long getLongValue (final Object obj)
    {
        long value = 0L;

        if (obj instanceof Byte)
        {
            final Byte o = (Byte) obj;
            value = o.byteValue ();
        }
        else if (obj instanceof Short)
        {
            final Short o = (Short) obj;
            value = o.shortValue ();
        }
        else if (obj instanceof Integer)
        {
            final Integer o = (Integer) obj;
            value = o.intValue ();
        }
        else if (obj instanceof Long)
        {
            final Long o = (Long) obj;
            value = o.longValue ();
        }
        else if (obj instanceof BigDecimal)
        {
            final BigDecimal o = (BigDecimal) obj;

            // -- unreliable precision information coming back from Oracle JDBC driver --
            // final int precision = o.precision ();
            // ThreadContext.assertError (precision == 0,
            // "Illegal attempt to getLongValue from BigDecimal of precision [%s]",
            // precision);
            final double fv = o.doubleValue ();
            ThreadContext.assertError (getFractionalComponent (fv) == 0.0,
                "Illegal attempt to getLongValue from BigDecimal [%s]", fv);
            value = o.longValue ();
        }
        else if (obj instanceof Float)
        {
            final Float o = (Float) obj;
            final float v = o.floatValue ();
            ThreadContext.assertError (getFractionalComponent (v) == 0.0,
                "Illegal attempt to getLongValue from Float [%s]", v);
            value = (long) v;
        }
        else if (obj instanceof Double)
        {
            final Double o = (Double) obj;
            final double v = o.doubleValue ();
            ThreadContext.assertError (getFractionalComponent (v) == 0.0,
                "Illegal attempt to getLongValue from Double [%s]", v);
            value = (long) o.doubleValue ();
        }

        return value;
    }

    public static long getMemoryUsedBytes ()
    {
        final Runtime runtime = Runtime.getRuntime ();
        return runtime.totalMemory () - runtime.freeMemory ();
    }

    public static byte[] getNullTerminatedBytes (final String s)
    {
        final byte[] bytes = s.getBytes ();

        final byte[] b = new byte[bytes.length + 1];
        System.arraycopy (bytes, 0, b, 0, bytes.length);
        b[bytes.length] = 0;

        return b;
    }

    public static String getNullTerminatedString (final byte[] buffer)
    {
        int lastIdx = -1;
        for (int i = 0; i < buffer.length && buffer[i] != 0; ++i)
        {
            lastIdx = i;
        }

        return new String (buffer, 0, lastIdx + 1);
    }

    public static String getProjectsDirectoryName ()
    {
        return m_valueProjectsDirectoryName.get ();
    }

    public static int getRandomIndex (final int maxValue, final int indexToAvoid)
    {
        final Random r = new Random ();

        int index = 0;
        boolean found = false;
        for (int i = 0; !found && i < maxValue * 1000; ++i)
        {
            index = r.nextInt (maxValue);
            found = index != indexToAvoid;
        }

        return index;
    }

    public static final String getRightPaddedString (final String source, final int width)
    {
        return getRightPaddedString (source, width, ' ');
    }

    public static final String getRightPaddedString (final String source, final int width, final char padding)
    {
        return source == null ? null : source + buildString (padding, width - source.length ());
    }

    /**
     * Descends through the cause chain of an exception and gets the exception at the
     * bottom of the chain.
     *
     * @param e
     *            the top leve exception to be examined
     * @return the root exception, which is e itself if the exception has no cause
     */
    public static Throwable getRootCause (Throwable e)
    {
        // Traverse the exception hierarchy.
        // Note: in Java 8 at least, Throwable.getCause returns (cause==this ? null : cause)
        // so it is not necessary to check for self to prevent infinite iteration.

        //for (Throwable cause = e.getCause (); cause != null && cause != e; cause = cause.getCause ())

        for (Throwable cause = e.getCause (); cause != null; cause = cause.getCause ())
        {
            e = cause;
        }

        return e;
    }

    public static short getShortValue (final Object obj)
    {
        final long v = getLongValue (obj);
        ThreadContext.assertError (v >= Short.MIN_VALUE, "Short value cannot be less than [%s]", Short.MIN_VALUE);
        ThreadContext.assertError (v <= Short.MAX_VALUE, "Short value cannot be greater than [%s]", Short.MAX_VALUE);
        return (short) v;
    }

    public static <T> List<T> getSortedDistinct (final List<T> values)
    {
        // Store in a sorted set to eliminate duplicates.
        final Set<T> uniqueSet = GenericFactory.newTreeSet (values);

        final List<T> l = GenericFactory.newArrayList ();
        l.addAll (uniqueSet);

        return l;
    }

    public static String getStackTrace (final Throwable e)
    {
        final Writer sw = new StringWriter ();
        final PrintWriter pw = new PrintWriter (sw);

        e.printStackTrace (pw);

        return sw.toString ();
    }

    public static List<E2<String, String>> getStandardSubstitutions ()
    {
        return m_standardSubstitutions.get ();
    }

    public static String getSummaryData (final boolean excludeUnused)
    {
        final StringBuilder sb = new StringBuilder ();

        // Take a copy to iterate.
        final List<Function<Boolean, String>> summaryDataProviders =
            GenericFactory.newArrayList (m_summaryDataProviders);

        boolean first = true;
        for (final Function<Boolean, String> p : summaryDataProviders)
        {
            if (!first)
            {
                sb.append (NewLine);
            }

            sb.append (p.apply (excludeUnused));
            first = false;
        }

        return sb.toString ();
    }

    public static String getTempDirectoryName ()
    {
        //        final String property = "java.io.tmpdir";
        //
        //        // Get the temporary directory and print it.
        //        final String tempDir = System.getProperty (property);

        return m_valueTempDirectoryName.get ();
    }

    public static String getTemporarySubdirectory (final String dir)
    {
        final String subdir = removeUnsafeFilenameChars (ThreadContext.getContextId ());

        return formFilepath (dir, subdir);
    }

    public static String getThisHost ()
    {
        return _m_thisHost;
    }

    public static String getThisOperatingSystem ()
    {
        return ThisOperatingSystem;
    }

    public static String getThisuser ()
    {
        return ThisUser;
    }

    public static String getTrimmedText (final Node node)
    {
        return node.getTextContent ().trim ();
    }

    public static Class<?> getUnderlyingClass (final Object o)
    {
        Class<?> c = null;

        if (o != null)
        {
            if (o instanceof Object[])
            {
                // Get class name of the array element type if possible, otherwise
                // use the array itself.
                final Object[] a = (Object[]) o;
                c = a.length > 0 ? a[0].getClass () : a.getClass ();
            }
            else
            {
                // Not an array.
                c = o.getClass ();
            }
        }

        return c;
    }

    public static String getUniqueIdentifier ()
    {
        // Java's equivalent of the GUID.
        return new java.rmi.dgc.VMID ().toString ();
    }

    public static <T> BiConsumer<T, T> getUnsupportedCombiner ()
    {
        return (m1, m2) ->
        {
            // Not necessary for purely sequential operations; for parallel execution.
            ThreadContext.assertFault (false, "Parallel operation is not supported - no combiner implementation");
        };
    }

    public static String getUserName ()
    {
        final String userName = System.getProperty ("user.name");
        return userName == null ? "(unknown user)" : userName;
    }

    public static <TOutput> TOutput handleCall (final Callable<? extends TOutput> service)
    {
        TOutput output = null;

        // Execute the business service method.
        try
        {
            output = service.call ();
        }
        catch (final Throwable e)
        {
            // Propagate exception as unchecked fault up to the fault barrier. Use
            // throwRootFault to handle the caught exception according to its type.
            ThreadContext.throwFault (e);
        }
        return output;
    }

    public static boolean hasPath (final String filepath)
    {
        return filepath.contains ("/") || filepath.contains ("\\");
    }

    public static byte hexToNibble (final char hex)
    {
        int nibble = 0;

        if (hex >= '0' && hex <= '9')
        {
            nibble = hex - '0';
        }
        else if (hex >= 'A' && hex <= 'F')
        {
            nibble = hex - 'A' + 10;
        }

        return (byte) nibble;
    }

    public static boolean inDateTimeRange (final Date dt, final Date fromDateTime, final Date toDateTime)
    {
        boolean between = false;

        if (dt.after (fromDateTime))
        {
            between = dt.before (toDateTime);
        }

        return between;
    }

    public static boolean inDateTimeRange (final GregorianCalendar gcDt, final GregorianCalendar gcFrom,
        final GregorianCalendar gcTo)
    {
        final Date dt = gcDt.getTime ();
        final Date fromDateTime = gcFrom.getTime ();
        final Date toDateTime = gcTo.getTime ();

        return inDateTimeRange (dt, fromDateTime, toDateTime);
    }

    /**
     * Instantiate an object via reflection.
     *
     * @param theClass
     *            class object used to instantiate an object on request
     * @return the created object instance
     */
    public static <T> T instantiate (final Class<? extends T> theClass)
    {
        Object o = null;
        try
        {
            o = theClass.newInstance ();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return uncheckedCast (o);
    }

    /**
     * Instantiate an object via reflection.
     *
     * @param className
     *            class name used to instantiate an object on request
     * @return the created object instance
     */
    public static <T> T instantiate (final String className)
    {
        final Class<T> classObject = getClassObject (className);
        final T o = instantiate (classObject);

        return o;
    }

    public static Object instantiateConstructor (final Class<?> theClass, final Class<?>[] parameterTypes,
        final Object[] parameters)
    {
        Object o = null;
        Constructor<?> c;
        try
        {
            c = theClass.getConstructor (parameterTypes);
            o = c.newInstance (parameters);
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return o;
    }

    public static boolean isNear (final double v, final double target, final double margin)
    {
        return v >= target - margin && v <= target + margin;
    }

    public static boolean isOperatingSystemWindows ()
    {
        // includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
        return System.getProperty ("os.name").startsWith ("Windows");
    }

    public static String[] joinArrays (final String[]... arrays)
    {
        int length = 0;

        for (final String[] a : arrays)
        {
            length += safeGetLength (a);
        }

        final String[] joined = new String[length];

        int j = 0;
        for (final String[] a : arrays)
        {
            if (a != null)
            {
                for (final String s : a)
                {
                    joined[j++] = s;
                }
            }
        }

        return joined;
    }

    public static <K> List<K> keysToList (final Map<K, ?> map)
    {
        final List<K> list = GenericFactory.newArrayList ();
        for (final K e : map.keySet ())
        {
            list.add (e);
        }

        return list;
    }

    public static <T> List<T> limitSize (final List<T> theList, final int maxSize)
    {
        List<T> l;

        if (theList.size () <= maxSize)
        {
            l = theList;
        }
        else
        {
            l = GenericFactory.newArrayList ();

            int i = 0;
            for (final Iterator<T> it = theList.iterator (); i < maxSize && it.hasNext (); ++i)
            {
                l.add (it.next ());
            }
        }

        return l;
    }

    public static String listToString (final List<?> l)
    {
        return Arrays.toString (l.toArray ());
    }

    public static int log2 (final long v)
    {
        // Log (0) is undefined, but treat it as 0 here.
        return v == 0 ? 0 : countBits (v) - 1;
    }

    public static String lowerCaseAcronyms (final String s)
    {
        String result = s;

        if (safeGetLength (s) > 0)
        {
            final char[] chars = s.toCharArray ();

            boolean lastWasUpperCase = Character.isUpperCase (chars[0]);

            for (int i = 1; i < chars.length; ++i)
            {
                // ABCTeleVISION -> AbcTeleVision
                // BRANCH4 -> Branch4
                final char c = chars[i];
                final boolean isUpper = Character.isUpperCase (c);
                if (isUpper && lastWasUpperCase && (i == chars.length - 1 || Character
                    .isUpperCase (chars[i + 1]) || Character.isDigit (chars[i + 1])))
                {
                    chars[i] = Character.toLowerCase (c);
                }

                lastWasUpperCase = isUpper;
            }

            result = new String (chars);
        }

        return result;
    }

    public static void main (final String[] args)
    {
        final Function<Integer, Function<Integer, Integer>> f = x -> y -> x + y;
        final Function<Integer, Integer> threeAdder = f.apply (3);
        System.out.printf ("threeAdder.apply (2) %s%n", threeAdder.apply (2));

        final int[] ii =
            {
                    0,
                    1
        };
        System.out.printf ("ii %s %s%n", Objects.hash (new Object[]
        {
                ii
        }), Arrays.deepHashCode (new Object[]
        {
                ii
        }));

        final Integer[] iii =
            {
                    0,
                    1
        };
        System.out.printf ("iii %s %s%n", Objects.hash (new Object[]
        {
                iii
        }), Arrays.deepHashCode (new Object[]
        {
                iii
        }));

        final String[] ss =
            {
                    "0",
                    "1"
        };
        System.out.printf ("ss %s %s%n", Objects.hash (new Object[]
        {
                ss
        }), Arrays.deepHashCode (new Object[]
        {
                ss
        }));

        System.out.printf ("TimeZone.getDefault ():%n  %s%n", TimeZone.getDefault ());
        System.out.printf ("GregorianCalendar.getTimeZone ():%n  %s%n", new GregorianCalendar ().getTimeZone ());
    }

    public static <T> List<T> mergeLists (final List<List<T>> lists)
    {
        final List<T> merged = GenericFactory.newArrayList ();
        for (final List<T> list : lists)
        {
            merged.addAll (list);
        }

        return merged;
    }

    public static long msecToNs (final long nsElapsed)
    {
        return nsElapsed * 1000000L;
    }

    /**
     * Simple comparator for sorting etc in ascending order based on data extracted from
     * the objects being sorted / compared. The data extracted must be of type TField.
     * TField must implement Comparable<T> so that the compareTo method can be used for
     * sorting based on the extracted values.
     *
     * NB: for one extractor argument, this is superseded in Java 8 by
     * Comparator.comparing (T::getKey), eg Comparator.comparing (KeyValuePair::getKey)
     */
    @SafeVarargs
    public static <T> Comparator<T> multilevelComparator (final Function<T, ? extends Comparable<?>>... extractors)
    {
        @SuppressWarnings ("unchecked")
        final Comparator<T> comparator = (lhs, rhs) ->
        {
            int result = 0;// equal

            // Keep going while equal to subsort by the extractors in order.
            for (int i = 0; result == 0 && i < extractors.length; ++i)
            {
                final Function<T, ? extends Comparable<?>> ex = extractors[i];

                @SuppressWarnings ("rawtypes")
                final Comparable fieldLhs = ex.apply (lhs);

                @SuppressWarnings ("rawtypes")
                final Comparable fieldRhs = ex.apply (rhs);

                result = fieldLhs.compareTo (fieldRhs);
            }

            return result;
        };

        return comparator;
    }

    public static char nibbleToHex (final byte nibble)
    {
        return HEX_DIGITS[nibble & 0x0F];
    }

    public static boolean nsIsBefore (final long ns, final long nsLimit)
    {
        // Because of the possibility of numerical overflow, to compare two nanoTime
        // values, use t1 - t2 < 0, not t1 < t2.

        // return ns < nsLimit;
        return ns - nsLimit < 0;
    }

    public static long nsToMsec (final long nsElapsed)
    {
        return nsElapsed / 1000000L;
    }

    public static double nsToMsecDouble (final double ns)
    {
        return ns / 1000000.0;
    }

    public static double nsToMsecDouble (final long ns)
    {
        return nsToMsecDouble ((double) ns);
    }

    public static void nullifyStrings (final Object o)
    {
        PropertyDescriptor[] properties = null;
        try
        {
            properties = Introspector.getBeanInfo (o.getClass ()).getPropertyDescriptors ();
        }
        catch (final IntrospectionException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        for (final PropertyDescriptor p : properties)
        {
            final Class<?> propertyClass = p.getPropertyType ();
            if (propertyClass == String.class)
            {
                final Method read = p.getReadMethod ();
                final Method write = p.getWriteMethod ();
                if (read != null && write != null)
                {
                    try
                    {
                        final String value = (String) read.invoke (o);
                        if (value != null)
                        {
                            // Only nullify if the value consists of 0 or more spaces.
                            final String trimmed = value.trim ();
                            if (trimmed.length () == 0)
                            {
                                write.invoke (o, new Object[]
                                {
                                        null
                                });
                            }
                        }
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        // Propagate exception as unchecked fault up to the fault barrier.
                        ThreadContext.throwFault (e);
                    }
                }
            }
        }
    }

    public static boolean objectEquals (final Object lhs, final Object rhs)
    {
        boolean isEqual = false;

        if (lhs == null)
        {
            isEqual = rhs == null;
        }
        else if (rhs == null)
        {
            isEqual = false;
        }
        else if (lhs == rhs)
        {
            isEqual = true;
        }
        // else if (lhs.getClass () == rhs.getClass ())
        // {
        // if (lhs instanceof Byte)
        // {
        // isEqual = safeEquals ((Byte) lhs, (Byte) rhs);
        // }
        // else if (lhs instanceof Short)
        // {
        // isEqual = safeEquals ((Short) lhs, (Short) rhs);
        // }
        // else if (lhs instanceof Integer)
        // {
        // isEqual = safeEquals ((Integer) lhs, (Integer) rhs);
        // }
        // else if (lhs instanceof Long)
        // {
        // isEqual = safeEquals ((Long) lhs, (Long) rhs);
        // }
        // else if (lhs instanceof Double)
        // {
        // isEqual = safeEquals ((Double) lhs, (Double) rhs);
        // }
        // else
        // {
        // isEqual = lhs.equals (rhs);
        // }
        // }
        else
        {
            isEqual = lhs.equals (rhs);
        }

        return isEqual;
    }

    public static Date parseXsdDate (final String s)
    {
        return parseDate (s, getXsdDateFormat ());
    }

    public static Date parseXsdDateMs (final String s)
    {
        return parseDate (s, getXsdDateFormatMs ());
    }

    public static Date parseXsdDateSpace (final String s)
    {
        return parseDate (s, getXsdDateFormatSpace ());
    }

    public static Date parseXsdDateSpaceMs (final String s)
    {
        return parseDate (s, getXsdDateFormatSpaceMs ());
    }

    /**
     * A basic sleep where you don't care about the interrupted exception.
     *
     * @param msec
     *            Milliseconds to seleep for
     */
    public static void pause (final int msec)
    {
        if (msec > 0)
        {
            try
            {
                Thread.sleep (msec);
            }
            catch (final InterruptedException e)
            {
            }
        }
    }

    public static void putApplicationObject (final Object key, final Object value)
    {
        m_mapApplicationObjects.put (key, value);
    }

    public static String qualifyName (final Class<?> theClass, final String name)
    {
        return qualifyName (theClass.getSimpleName (), name);
    }

    public static String qualifyName (final String part1, final Class<?> usingClass, final String name)
    {
        final String part2 = HcUtil.qualifyName (usingClass, name);
        return HcUtil.qualifyName (part1, part2);
    }

    public static String qualifyName (final String part1, final String part2)
    {
        return part1 + "." + part2;
    }

    public static void registerSummaryData (final Function<Boolean, String> source)
    {
        m_summaryDataProviders.add (source);
    }

    public static void removeContextObject (final Object key)
    {
        m_mapApplicationObjects.remove (key);
    }

    public static void removeLeadingAndTrailingBlankLines (final List<String> lines)
    {
        for (boolean gotNonBlank = false; !gotNonBlank && lines.size () > 0;)
        {
            final int i = 0;
            if (lines.get (i).trim ().length () == 0)
            {
                // Blank. Delete it.
                lines.remove (i);
            }
            else
            {
                gotNonBlank = true;
            }
        }

        for (boolean gotNonBlank = false; !gotNonBlank && lines.size () > 0;)
        {
            final int i = lines.size () - 1;
            if (lines.get (i).trim ().length () == 0)
            {
                // Blank. Delete it.
                lines.remove (i);
            }
            else
            {
                gotNonBlank = true;
            }
        }
    }

    public static String removeLeadingSpaces (final String line, int count)
    {
        // Safety check the count.
        final int leading = countLeadingSpaces (line);
        if (count > leading)
        {
            count = leading;
        }

        return line.substring (count);
    }

    public static String removeTrailingPathSeparator (final String fp)
    {
        return removeTrailing (fp, '/', '\\');
    }

    public static String removeUnsafeFilenameChars (final String filepath)
    {
        return filepath.replaceAll ("[\\\\/\\:]", "");
    }

    public static <T1, T2> T2 safeApply (final T1 target, final Function<T1, T2> f)
    {
        return target == null ? null : f.apply (target);
    }

    public static <T1, T2, T3> T3 safeApply (final T1 target, final Function<T1, T2> f1, final Function<T2, T3> f2)
    {
        return safeApply (safeApply (target, f1), f2);
    }

    public static <T1, T2, T3, T4> T4 safeApply (final T1 target, final Function<T1, T2> f1, final Function<T2, T3> f2,
        final Function<T3, T4> f3)
    {
        return safeApply (safeApply (target, f1, f2), f3);
    }

    public static <T1, T2, T3, T4, T5> T5 safeApply (final T1 target, final Function<T1, T2> f1,
        final Function<T2, T3> f2, final Function<T3, T4> f3, final Function<T4, T5> f4)
    {
        return safeApply (safeApply (target, f1, f2, f3), f4);
    }

    @SafeVarargs
    public static <T> List<T> safeAsList (final T... a)
    {
        // Don't use Arrays.asList (a) since doesn't support all operations that
        // might subsequently be required.
        return a == null ? GenericFactory.<T> newArrayList () : GenericFactory.<T> newArrayList (a);
    }

    public static boolean safeBoolean (final Boolean b)
    {
        return b != null && b.booleanValue ();
    }

    public static void safeClose (final ICloseable c)
    {
        if (c != null)
        {
            c.close ();
        }
    }

    public static boolean safeEquals (final boolean lhs, final boolean rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final boolean[] lhs, final boolean[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final byte lhs, final byte rhs)
    {
        return lhs == rhs;
    }

    /**
     * Special handling for arrays.
     *
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final byte[] lhs, final byte[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final double lhs, final double rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final double[] lhs, final double[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final float lhs, final float rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final float[] lhs, final float[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final int lhs, final int rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final int[] lhs, final int[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final long lhs, final long rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final long[] lhs, final long[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEquals (final Object lhs, final Object rhs)
    {
        //return lhs == rhs || lhs != null && rhs != null && lhs.equals (rhs);

        // Came along in Java 7.
        return Objects.equals (lhs, rhs);
    }

    public static boolean safeEquals (final short lhs, final short rhs)
    {
        return lhs == rhs;
    }

    /**
     * @return true when arguments are equal or both null; returns false when not equal or
     *         one argument is null.
     */
    public static boolean safeEquals (final short[] lhs, final short[] rhs)
    {
        return Arrays.equals (lhs, rhs);
    }

    public static boolean safeEqualsIgnoreCase (final String lhs, final String rhs)
    {
        return lhs == rhs || lhs != null && rhs != null && lhs.equalsIgnoreCase (rhs);
    }

    public static <T> T safeGetElement (final T[] a, final int i)
    {
        return a != null && i >= 0 && i < a.length ? a[i] : null;
    }

    public static int safeGetLength (final String s)
    {
        return s == null ? 0 : s.length ();
    }

    public static <T> int safeGetLength (final T[] ts)
    {
        return ts == null ? 0 : ts.length;
    }

    public static int safeGetLengthTrim (final String s)
    {
        return s == null ? 0 : s.trim ().length ();
    }

    public static int safeGetSize (final List<?> l)
    {
        return l != null ? l.size () : 0;
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final byte[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final byte element : a)
            {
                result = 31 * result + element;
            }
        }

        return result;
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final double[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final double value : a)
            {
                final long bits = Double.doubleToLongBits (value);
                result = 31 * result + (int) (bits ^ (bits >>> 32));// implementation taken from java.lang.Double to avoid instantiation
            }
        }

        return result;
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final float[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final float element : a)
            {
                result = 31 * result + Float.floatToIntBits (element);// implementation taken from java.lang.Float to avoid instantiation
            }
        }

        return result;
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final int[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final int element : a)
            {
                result = 31 * result + element;
            }
        }

        return result;
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final long[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final long value : a)
            {
                result = 31 * result + (int) (value ^ (value >>> 32));// implementation taken from java.lang.Long
            }
        }

        return result;
    }

    public static int safeHashCode (final Object o)
    {
        // If the field is an object reference and this class's equals method compares the
        // field by recursively invoking equals, recursively invoke hashCode on the field.
        // If a more complex comparison is required, compute a "canonical representation"
        // for this field and invoke hashCode on the canonical representation. If the
        // value of the field is null, return 0 (or some other constant, but 0 is traditional).
        //      return o != null ? o.hashCode () : 17;

        // Came along in Java 7.
        return Objects.hashCode (o);
    }

    /** Special handling for primitive arrays */
    public static int safeHashCode (final short[] a)
    {
        int result = 17;

        if (a != null)
        {
            for (final short element : a)
            {
                result = 31 * result + element;
            }
        }

        return result;
    }

    public static String safeTrim (final String s)
    {
        return s == null ? null : s.trim ();
    }

    /**
     * Schedule the one-shot execution of a task using the shared, general use thread
     * scheduler. Because the scheduler is shared, there is no latency guarantee.
     *
     * @param service
     *            The work to be executed
     * @param periodMsec
     *            Period in milliseconds
     * @return a handle that can be used to wait on / cancel the scheduling
     */
    public static ScheduledFuture<?> scheduleOnce (final Runnable service, final int periodMsec)
    {
        return ExecutionScopes.scheduleOnce (m_sharedScheduler.get (), service, periodMsec);
    }

    /**
     * Schedule the periodic execution of a task using the shared, general use thread
     * scheduler. Because the scheduler is shared, there is no latency guarantee.
     *
     * @param service
     *            The work to be executed
     * @param periodMsec
     *            Period in milliseconds
     * @return a handle that can be used to wait on / cancel the scheduling
     */
    public static ScheduledFuture<?> schedulePeriodically (final Runnable service, final long periodMsec)
    {
        return ExecutionScopes.schedulePeriodically (m_sharedScheduler.get (), service, periodMsec, true);
    }

    public static void setApplicationName (final String applicationName, final String applicationVersion)
    {
        m_applicationName = applicationName == null ? _UNKNOWN : applicationName;
        m_applicationVersion = applicationVersion == null ? _UNKNOWN : extractVersionString (applicationVersion);
    }

    /** For unit testing */
    public static void setThisHost (final String thisHost)
    {
        _m_thisHost = thisHost;
    }

    public static void shutdownExecutorService (final ExecutorService executorService, final String name,
        final int seconds)
    {
        startShutdownExecutorService (executorService, name);
        awaitShutdownExecutorService (executorService, name, seconds, TimeUnit.SECONDS);
    }

    public static void simulateWork (final long millis)
    {
        // Tie up the cpu.
        final long ns = msecToNs (millis);
        final long nsUntil = System.nanoTime () + ns;
        do
        {
        }
        while (nsIsBefore (System.nanoTime (), nsUntil));
    }

    public static List<String> splitStringIntoLines (String s)
    {
        s = s.replace ("\r\n", "\n");// prevent double blank lines
        return GenericFactory.newArrayList (s.split ("[\r\n]"));
    }

    public static void startShutdownExecutorService (final ExecutorService executorService, final String name)
    {
        Logging.logDebug ("....shutdown ExecutorService [%s] starting", name);
        executorService.shutdown ();
    }

    /** Wrap an iterable in a stream */
    public static <T> Stream<T> streamOf (final Iterable<? extends T> iterable)
    {
        return streamOf (iterable.iterator ());
    }

    /** Wrap an iterator in a stream */
    public static <T> Stream<T> streamOf (final Iterator<? extends T> iterator)
    {
        return StreamSupport.stream (Spliterators.spliteratorUnknownSize (iterator, 0), false);
    }

    public static String substituteSymbols (final String s, final List<E2<String, String>> substitutions)
    {
        String ss = s;

        if (substitutions != null)
        {
            for (final E2<String, String> pair : substitutions)
            {
                ss = ss.replace (pair.getE0 (), pair.getE1 ());
            }
        }

        return ss;
    }

    public synchronized static void systemExit (final int status)
    {
        if (!m_exiting)
        {
            m_exiting = true;
            Logging.logInfo ("System exit...");

            // Formally shuts down other threads, eg Logging etc, through the shutdown tasks.
            System.exit (status);
        }
    }

    public static String toBackslashPath (final String filepath)
    {
        return filepath.replace ('/', '\\');
    }

    public static String toFirstCharUpperCase (final String s)
    {
        return s.substring (0, 1).toUpperCase () + s.substring (1);
    }

    /**
     * Stores all the entries of an entity list in a map, using the entity primary key as
     * key for the map.
     *
     * @param <T>
     *            Type of the entity
     * @param list
     *            List of entities to be stored in the map.
     * @return The map of entities.
     */
    public static <TPrimaryKey, T extends IIdentifiable<TPrimaryKey>> Map<TPrimaryKey, T> toMap (
        final List<? extends T> list)
    {
        return toMap (list, IIdentifiable::getPrimaryKey);
    }

    /**
     * Stores all the entries of an object list in a map, using the supplied function
     * object to extract the key for the map.
     *
     * @param <T>
     *            Type of the object
     * @param list
     *            List of objects to be stored in the map.
     * @param keyExtractor
     *            Function object that extracts the values from the object to use as key
     *            into the map.
     * @return The map of objects.
     */
    public static <K, T> Map<K, T> toMap (final List<? extends T> list, final Function<? super T, K> keyExtractor)
    {
        final Map<K, T> m = list// collection
            .stream ()// stream
            .collect (Collectors.toMap (keyExtractor, Function.identity ()));

        return m;
    }

    public static Properties toProperties (final ResourceBundle aBundle)
    {
        final Properties properties = new Properties ();
        final Enumeration<String> keyEnum = aBundle.getKeys ();

        while (keyEnum.hasMoreElements ())
        {
            final String keyVal = keyEnum.nextElement ();
            final String val = aBundle.getString (keyVal);
            properties.setProperty (keyVal, val);
        }

        return properties;
    }

    public static String toSlashPath (final String filepath)
    {
        return filepath.replace ('\\', '/');
    }

    //    // Enhanced from Arrays.toString ()
    //    public static String toString (final boolean[][] a)
    //    {
    //        if (a == null)
    //        {
    //            return "null";
    //        }
    //
    //        final int iMax = a.length - 1;
    //        if (iMax == -1)
    //        {
    //            return "[]";
    //        }
    //
    //        final StringBuilder b = new StringBuilder ();
    //        b.append ('[');
    //        for (int i = 0;; i++)
    //        {
    //            final boolean[] element = a[i];
    //
    //            b.append (Arrays.toString (element));
    //            if (i == iMax)
    //            {
    //                return b.append (']').toString ();
    //            }
    //            b.append (", ");
    //        }
    //    }

    /** Varargs wrapper */
    public static String toString (final Object... objects)
    {
        return Arrays.toString (objects);
    }

    public static void transferProperties (final Object source, final Object target, final boolean strict)
    {
        PropertyDescriptor[] sourceProperties = null;
        PropertyDescriptor[] targetProperties = null;
        try
        {
            sourceProperties = Introspector.getBeanInfo (source.getClass ()).getPropertyDescriptors ();
            targetProperties = Introspector.getBeanInfo (target.getClass ()).getPropertyDescriptors ();
        }
        catch (final IntrospectionException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        for (final PropertyDescriptor sp : sourceProperties)
        {
            final String name = sp.getName ();
            final PropertyDescriptor pd = findProperty (name, targetProperties);
            if (pd == null)
            {
                ThreadContext.assertFault (!strict, "Cannot find property [%s] in class [%s]", name,
                    target.getClass ().getCanonicalName ());
            }
            else
            {
                final Method read = sp.getReadMethod ();
                final Method write = pd.getWriteMethod ();
                if (read != null && write != null)
                {
                    // Get from source and set in target.
                    try
                    {
                        final Object value = read.invoke (source);
                        if (value != null)
                        {
                            write.invoke (target, value);
                        }
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        // Propagate exception as unchecked fault up to the fault barrier.
                        ThreadContext.throwFault (e);
                    }
                }
            }
        }
    }

    public static String trimLeft (final String s)
    {
        // Hacked from String.trim ()
        String trimmed = s;

        final int len = s.length ();
        if (len > 0)
        {
            final char[] val = s.toCharArray ();

            int st = 0;
            while ((st < len) && (val[st] <= ' '))
            {
                ++st;
            }

            trimmed = st > 0 ? s.substring (st, len) : s;
        }

        return trimmed;
    }

    public static String trimRight (final String s)
    {
        // Hacked from String.trim ()
        String trimmed = s;

        int len = s.length ();
        if (len > 0)
        {
            final char[] val = s.toCharArray ();

            while (len > 0 && val[len - 1] <= ' ')
            {
                len--;
            }

            trimmed = len < s.length () ? s.substring (0, len) : s;
        }

        return trimmed;
    }

    public static <T> T uncheckedCast (final Object o)
    {
        @SuppressWarnings ("unchecked")
        final T t = (T) o;
        return t;
    }

    public static String underScoreBreakToCaseBreak (final String s)
    {
        // Logical name is the physical name converted to mixed case, eg ACTUAL_DATA to
        // ActualData.
        final StringBuilder sb = new StringBuilder ();

        final char[] chars = s.toCharArray ();

        boolean nextShouldBeUpperCase = true;
        for (final char c : chars)
        {
            if (c == '_')
            {
                // Separator. Cause a case break.
                nextShouldBeUpperCase = true;
            }
            else if (c == '$' || !Character.isJavaIdentifierPart (c))
            {
                // Translate illegal computer language characters to '_' and cause a case
                // break.
                sb.append ('_');
                nextShouldBeUpperCase = true;
            }
            else if (nextShouldBeUpperCase)
            {
                sb.append (Character.toUpperCase (c));
                nextShouldBeUpperCase = false;
            }
            else
            {
                sb.append (Character.toLowerCase (c));
            }
        }

        return sb.toString ();
    }

    public static <T> List<T> valuesToList (final Map<?, ? extends T> map)
    {
        return map.values ()// collection
            .stream ()// stream
            .collect (toList ());// collect
    }

    public static <T> T waitForFuture (final Future<T> future)
    {
        T result = null;

        try
        {
            result = future.get ();// wait for future
        }
        catch (final ExecutionException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier. Use
            // throwRootFault to handle the caught exception according to its type.
            ThreadContext.throwFault (e);
        }
        catch (final InterruptedException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return result;
    }

    public static <T> List<T> waitForFutures (final List<Future<T>> futures)
    {
        final List<T> results = GenericFactory.newArrayList ();

        try
        {
            for (final Future<T> future : futures)
            {
                final T result = future.get ();// wait for future
                results.add (result);// pass result back
            }
        }
        catch (final ExecutionException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier. Use
            // throwRootFault to handle the caught exception according to its type.
            ThreadContext.throwFault (e);
        }
        catch (final InterruptedException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }

        return results;
    }

    public static <T> List<T> waitForTasks (final ExecutorService executor, final List<Callable<T>> tasks)
    {
        final List<Future<T>> results = GenericFactory.newArrayList ();

        for (final Callable<T> task : tasks)
        {
            final Future<T> future = executor.submit (task);
            results.add (future);
        }

        // Wait for the test to finish.
        return HcUtil.waitForFutures (results);
    }

    public static boolean wildcardMatches (final String regex, final String s)
    {
        final Matcher m = Pattern.compile (regex).matcher (s);

        return m.matches ();
    }

    /** Visible for unit testing */
    static String extractVersionString (final String applicationVersion)
    {
        String s = null;

        //applicationVersion = "SomeApp 9.0#35 (20/05/2013 17:50) by Breakpoint Pty Limited";
        final String bs = applicationVersion;
        final int i = bs.indexOf ('#');
        if (i != -1)
        {
            // Up to the space past the hash.
            final int j = bs.indexOf (' ', i + 1);
            if (j != -1)
            {
                s = bs.substring (0, j);
            }
        }

        return s == null ? _UNKNOWN : s;
    }

    private static int countBits (long v)
    {
        int count = 0;

        for (; v > 0; v = v / 2)
        {
            ++count;
        }

        return count;
    }

    private static String getAvailableDirectoryName (final String[] paths)
    {
        String path = null;

        for (int i = 0; path == null && i < paths.length; ++i)
        {
            final String p = paths[i];

            if (HcUtilFile.doesDirectoryExist (p))
            {
                path = p;
            }
        }

        if (path == null)
        {
            // Used at initialisation so don't use ThreadContext.assertFault
            // since the logging environment is not properly set up yet.
            throw new FaultException (
                String.format ("None of the following paths is available [%s]", Arrays.toString (paths)), false);
        }

        return path;
    }

    private static String getAvailableProjectsDirectoryName ()
    {
        final String currentDir = getCurrentDirectory ();
        String s = findAncestralDirectory (currentDir, "hedron");

        if (s == null)
        {
            s = getAvailableDirectoryName (new String[]
            {
                    "B:/Projects",
                    "/Volumes/Internal-2TB/dev/root/tools/Projects"
            });
        }

        return s;
    }

    private static String getOperatingSystem ()
    {
        return System.getProperty ("os.name");
    }

    private static SimpleDateFormat getXsdDateFormat ()
    {
        // NB don't share instances - not threadsafe.
        return new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
    }

    private static SimpleDateFormat getXsdDateFormatMs ()
    {
        // NB don't share instances - not threadsafe.
        return new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private static SimpleDateFormat getXsdDateFormatSpace ()
    {
        // NB don't share instances - not threadsafe.
        return new SimpleDateFormat ("yyyy-MM-dd' 'HH:mm:ss");
    }

    private static SimpleDateFormat getXsdDateFormatSpaceMs ()
    {
        // NB don't share instances - not threadsafe.
        return new SimpleDateFormat ("yyyy-MM-dd' 'HH:mm:ss.SSS");
    }

    private static Date parseDate (final String s, final SimpleDateFormat format)
    {
        Date date = null;
        try
        {
            date = format.parse (s);
        }
        catch (final ParseException e)
        {
            // Propagate exception as unchecked fault up to the fault barrier.
            ThreadContext.throwFault (e);
        }
        return date;
    }

    private static String readStringFromStream (final InputStream is) throws IOException
    {
        String sysout;
        final StringBuilder sb = new StringBuilder ();
        final byte buffer[] = new byte[1024];

        int cbRead;
        while ((cbRead = is.read (buffer)) != -1)
        {
            for (int i = 0; i < cbRead; ++i)
            {
                final byte[] bytes = new byte[]
                {
                        buffer[i]
                };
                final String chars = new String (bytes);
                sb.append (chars);
            }
        }

        sysout = sb.toString ();
        return sysout;
    }

    private static String removeTrailing (String s, final char... cs)
    {
        final int length = s.length ();
        if (length > 0)
        {
            final char last = s.charAt (length - 1);
            boolean matches = false;
            for (int i = 0; !matches && i < cs.length; ++i)
            {
                matches = cs[i] == last;
            }

            if (matches)
            {
                s = s.substring (0, length - 1);
            }
        }

        return s;
    }

    public static final String _UNKNOWN = "(unknown)";

    public static final int DEFAULT_THREAD_SHUTDOWN_SECONDS = 5;

    public static final String NewLine = System.getProperty ("line.separator");

    public static final BinaryOperator<String> StringCombiner = (l, r) ->
    {
        return safeGetLength (l) == 0 ? r : l + "," + r;
    };

    /**
     * [[[ NB: extra underscore used here in the variable name so that Eclipse
     * alphabetically-sorted Java class members mean that m_allShutdownTasks is intialised
     * first. ]]]
     */
    private static final ListTreeMap<Integer, ResourceScope<?>> _m_shutdownTasks = ListTreeMap.of ();

    private static volatile String _m_thisHost = getHostName ();

    private static final char[] HEX_DIGITS =
        {
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
    };

    private static volatile String m_applicationName = _UNKNOWN;

    private static volatile String m_applicationVersion = _UNKNOWN;

    private static boolean m_exiting;// protected by class lock

    /** Map of additional objects */
    private static final Map<Object, Object> m_mapApplicationObjects = GenericFactory.newHashMap ();

    /**
     * Executor used for executeSerialAsync (). Note: the
     * Concurrency.createFixedThreadPool method handles scheduler shutdown. Created on
     * demand.
     *
     * NOTE: for debugging only, this makes async sync: private static final
     * ExecutorService m_serialAsyncExecutor = new CallingThreadMockExecutorService ();
     */
    private static final IValue<ExecutorService> m_serialAsyncExecutor =
        SafeLazyValue.of ( () -> Concurrency.createFixedThreadPool (1, "HcUtil.m_serialAsyncExecutor", false));

    /**
     * Scheduler used for scheduleOnce () and schedulePeriodically (). Note: the
     * Concurrency.createSingleThreadScheduledExecutor method handles scheduler shutdown.
     */
    private static final IValue<ScheduledExecutorService> m_sharedScheduler =
        SafeLazyValue.of ( () -> Concurrency.createSingleThreadScheduledExecutor ("HcUtil.m_sharedScheduler", false));

    private static final IValue<List<E2<String, String>>> m_standardSubstitutions =
        new SafeLazyValue<List<E2<String, String>>> ( () ->
        {
            // Use Ant-style symbols.
            final String currentDrive = HcUtilFile.getCurrentDrive ();
            ThreadContext.assertFault (currentDrive.endsWith ("/"), "Unexpected currentDrive [%s]", currentDrive);

            final E2<String, String> hostPair = E2.of ("${hostName}", _m_thisHost);

            final E2<String, String> drivePair =
                E2.of ("${driveName}", currentDrive.substring (0, currentDrive.length () - 1));

            final List<E2<String, String>> standardSubstitutions = GenericFactory.newArrayList (hostPair, drivePair);

            return standardSubstitutions;
        });

    /**
     * A register of callbacks that provide execution summary information for logging etc
     */
    private static final List<Function<Boolean, String>> m_summaryDataProviders = GenericFactory.newArrayList ();

    /** Lazy singleton for Projects directory */
    private static final IValue<String> m_valueProjectsDirectoryName =
        SafeLazyValue.of ( () -> getAvailableProjectsDirectoryName ());

    /** Lazy singleton for temp directory */
    private static final IValue<String> m_valueTempDirectoryName =
        SafeLazyValue.of ( () -> getAvailableDirectoryName (new String[]
    {
            "C:/temp",
            System.getProperty ("java.io.tmpdir")
    }));

    private static final String ThisOperatingSystem = getOperatingSystem ();

    private static final String ThisUser = getUserName ();

    static
    {
        // Register the shutdown hook for cleanup on JVM shutdown.
        final Thread shutdownHook = new Thread ("HcUtil.shutdownHook")
        {
            @Override
            public void run ()
            {
                try
                {
                    // Run the shutdown hook logic inside a fault barrier.
                    ExecutionScopes.executeFaultBarrier (HcUtil::coordinateShutdown);
                }
                catch (final Throwable e)
                {
                }
            }
        };
        Runtime.getRuntime ().addShutdownHook (shutdownHook);
    }
}
