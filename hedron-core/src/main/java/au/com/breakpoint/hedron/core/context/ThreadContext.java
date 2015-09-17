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
package au.com.breakpoint.hedron.core.context;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.log.Logging;

/**
 * Collection of helpers for IScope-style scope implementation. @see IScope.
 *
 * When each ThreadContext.AssertXxxx method detects a condition failure, it writes an
 * entry to the log. ThreadContext.assertFatal and ThreadContext.assertError methods throw
 * an exception. Before throwing the exception, they log a stack trace and the formatted
 * error message.
 *
 * This entire class is inherently thread-safe because of the use of thread-local data.
 *
 * @see IScope
 * @see ExecutionScope
 */
public class ThreadContext
{
    // TODO 0 consider self-JStack https://github.com/takipi/jstack
    public static void addInformation (final String format, final Object... parameters)
    {
        final String s = HcUtil.contextFormatMessage (format, parameters);
        Logging.logInfoString (s);
    }

    public static void addIScope (final IScope iScope, final String name)
    {
        final ContextData contextData = m_contextData.get ();
        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        final String contextId = allocateUniqueId (contextData);

        // Temporarily set the thread name so that significant info can be seen in the debugger
        // or in JStack output etc.
        final Thread t = Thread.currentThread ();
        final String prevThreadName = t.getName ();

        scopeNesting.push (new NestedScopeData (iScope, contextId, name, prevThreadName));

        // Produce a composite thread name from the original plus nested scopes.
        final String threadName = formThreadName (contextData, contextId);

        //        final String threadName = name == null ? String.format ("[%s:%s]", prevThreadName, contextId)
        //            : String.format ("[%s:%s:%s]", prevThreadName, name, contextId);

        //System.out.printf (">> %s%n", threadName);
        t.setName (threadName);
    }

    public static void addWarning (final String format, final Object... parameters)
    {
        final String s = HcUtil.contextFormatMessage (format, parameters);
        Logging.logWarnString (s);
    }

    public static String allocateUniqueId ()
    {
        final ContextData contextData = m_contextData.get ();
        return allocateUniqueId (contextData);
    }

    public static void assertError (final boolean result, final String format, final Object... parameters)
    {
        if (!result)
        {
            final String userReadableMessage = String.format (format, parameters);

            //Include stack trace and ThreadContext context id in log string only.
            final AssertException e = new AssertException (OpResult.Severity.Error, userReadableMessage, true);

            String exceptionDetails;
            if (m_policyErrorIncludeStackTrace)
            {
                exceptionDetails = HcUtil.getExceptionDetails (e);// don't need preamble, the string appears in the stack trace
            }
            else
            {
                // Log the ThreadContext context id if a scope has been active. Prepend the context
                // id to the message.
                // LP: remove redundant context id
                //final String contextId = ThreadContext.getContextId ();
                //exceptionDetails =
                //    contextId != null ? String.format ("%s %s", contextId, userReadableMessage) : userReadableMessage;

                exceptionDetails = userReadableMessage;
            }

            outputErrorExceptionDetails (exceptionDetails);

            throw e;
        }
    }

    public static void assertFault (final boolean result, final String format, final Object... parameters)
    {
        if (!result)
        {
            // Don't return any stack trace etc info to the end user.
            final String userReadableMessage = String.format (format, parameters);

            // Include stack trace in log string only.
            final FaultException e = new FaultException (userReadableMessage, true);

            final String exceptionDetails = HcUtil.getExceptionDetails (e);// don't need preamble, the string appears in the stack trace
            outputFatalExceptionDetails (exceptionDetails);

            throw e;
        }
    }

    public static void assertFaultNotNull (final Object o)
    {
        assertFault (o != null, "ThreadContext.assertNotNull failed: The specified object is null");
    }

    public static void assertFaultNotNull (final Object o, final String format, final Object... parameters)
    {
        final String userReadableMessage = String.format (format, parameters);
        assertFault (o != null, userReadableMessage);
    }

    public static void assertInformation (final boolean result, final String format, final Object... parameters)
    {
        if (!result)
        {
            addInformation (format, parameters);

            // No exception.
        }
    }

    public static void assertWarning (final boolean result, final String format, final Object... parameters)
    {
        if (!result)
        {
            addWarning (format, parameters);

            // No exception.
        }
    }

    public static void enterLoggingOperation ()
    {
        final ContextData contextData = m_contextData.get ();
        ++contextData.m_loggingOperationDepth;
    }

    public static void enterLoggingSilence ()
    {
        final ContextData contextData = m_contextData.get ();
        ++contextData.m_loggingSilenceDepth;
    }

    /**
     * Gets unique identifier for the request context.
     *
     * @return unique identifier for the request context; null if no IScope has been
     *         established on the current thread.
     */
    public static String getContextId ()
    {
        final ContextData contextData = m_contextData.get ();
        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        return scopeNesting.size () == 0 ? "{}" : scopeNesting.peek ().m_contextId;
    }

    public static boolean getPolicyErrorIncludeStackTrace ()
    {
        return m_policyErrorIncludeStackTrace;
    }

    /**
     * Looks at an exception cause hierarchy and returns the first exception below a
     * wrapper exception, where the wrapper can be a ExecutionException,
     * CompletionException etc.
     *
     * @param e
     *            the exception being analysed
     * @return the wrapped exception
     */
    public static Throwable getReportableException (final Throwable e)
    {
        Throwable wrapped = null;

        if (e instanceof ExecutionException || e instanceof CompletionException)
        {
            // Recurse into the underlying exception from Future.
            wrapped = getReportableException (e.getCause ());
        }
        else
        {
            // Encountered a non-wrapper. Return it.
            wrapped = e;
        }

        return wrapped;
    }

    public static String getScopeName ()
    {
        final ContextData contextData = m_contextData.get ();
        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        String name = null;
        if (scopeNesting.size () > 0)
        {
            final NestedScopeData innermost = scopeNesting.peek ();
            name = innermost.m_name;
        }

        return name;
    }

    public static <T> T getThreadObject (final Object key)
    {
        final ContextData contextData = m_contextData.get ();
        return HcUtil.uncheckedCast (contextData.m_mapThreadObjects.get (key));
    }

    public static <T> T getThreadObject (final Object key, final Function<Object, T> mappingFunction)
    {
        final ContextData contextData = m_contextData.get ();
        return HcUtil.uncheckedCast (contextData.m_mapThreadObjects.computeIfAbsent (key, mappingFunction));
    }

    public static boolean hasBeenLogged (final Throwable e)
    {
        return e instanceof ThreadContextException && ((ThreadContextException) e).isLogged ();
    }

    public static boolean isWithinLoggingOperation ()
    {
        final ContextData contextData = m_contextData.get ();
        return contextData.m_loggingOperationDepth > 0;
    }

    public static boolean isWithinLoggingSilence ()
    {
        final ContextData contextData = m_contextData.get ();
        return contextData.m_loggingSilenceDepth > 0;
    }

    public static void leaveLoggingOperation ()
    {
        final ContextData contextData = m_contextData.get ();
        --contextData.m_loggingOperationDepth;
    }

    public static void leaveLoggingSilence ()
    {
        final ContextData contextData = m_contextData.get ();
        --contextData.m_loggingSilenceDepth;
    }

    public static void logException (final Throwable e)
    {
        // TODO 0 if has been logged could output a reduced message with only the caller file/line (skipping anything in hedron) and context id of the logging entry.... need to store context id when logged
        if (!hasBeenLogged (e))
        {
            // Strip wrapper exception layers to get to the underlying exception.
            final Throwable reportableException = getReportableException (e);

            if (!hasBeenLogged (reportableException))
            {
                // getExceptionDetails also includes the ThreadContext context id.
                final String exceptionDetails =
                    HcUtil.getExceptionDetails (reportableException, "An exception has occurred:");

                //System.out.println (exceptionDetails);
                outputErrorExceptionDetails (exceptionDetails);
            }
        }
    }

    public static void putThreadObject (final Object key, final Object value)
    {
        final ContextData contextData = getExistingContextData ();
        contextData.m_mapThreadObjects.put (key, value);
    }

    public static void removeContextObject (final Object key)
    {
        final ContextData contextData = getExistingContextData ();
        contextData.m_mapThreadObjects.remove (key);
    }

    public static void removeIScope (final IScope iScope)
    {
        final ContextData contextData = m_contextData.get ();
        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        // Don't use assertFault for this; bootstrap situation: there is no scope to use.
        if (scopeNesting.size () == 0)
        {
            throw new FaultException (
                "There is no current IScope. Instantiate an ExecutionScope (or sub-class) at the top level program scope.",
                false);
        }

        final NestedScopeData nsd = scopeNesting.peek ();
        final IScope currentIScope = nsd.m_iScope;

        // Don't use assertFault etc here.
        if (currentIScope != iScope)
        {
            // Something is badly wrong with scope nesting. Don't use assertFault for
            // this; bootstrap situation: there is no scope to use.
            throw new FaultException (
                String.format ("removeIScope () is out of sequence; stack size [%s]", scopeNesting.size ()), false);
        }

        // Restore the previous thread name.
        final Thread t = Thread.currentThread ();
        t.setName (nsd.m_prevThreadName);
        //System.out.printf ("<< %s%n", nsd.m_prevThreadName);

        scopeNesting.pop ();

        if (!isAnActiveScope ())
        {
            // Remove remnant thread local storage. Useful in shared thread
            // environments, such as Tomcat.
            m_contextData.remove ();
        }
    }

    public static boolean setPolicyErrorIncludeStackTrace (final boolean policyErrorIncludeStackTrace)
    {
        final boolean prevValue = m_policyErrorIncludeStackTrace;
        m_policyErrorIncludeStackTrace = policyErrorIncludeStackTrace;

        return prevValue;
    }

    public static void throwFault (final Throwable e)
    {
        throw wrapRootThrowable (e);
    }

    /** This was the previous throwFault () before I made it always check */
    public static void throwFaultExplicit (final Throwable e)
    {
        // Just wrap the exception without looking for any underlying cause.
        throw wrapThrowable (e);
    }

    /**
     * Looks at an exception and returns an exception that is a subclass of
     * LogOnceException, ie either FaultException or AssertException, etc. If necessary,
     * the exception is wrapped in a FaultException, but if the passed-in exception wraps
     * a suitable exception, that is returned instead. This prevents multiple level of
     * exception wrapping and duplicated logging, and keeps the resulting exception as
     * close as possible to the wrapped root cause.
     *
     * @param e
     *            the exception being analysed
     * @return the wrapped root exception
     */
    public static ThreadContextException wrapRootThrowable (final Throwable e)
    {
        ThreadContextException wrapper = null;

        if (e instanceof ThreadContextException) // FaultException, AssertException, etc
        {
            // Already the required type of exception. Don't re-wrap.
            wrapper = (ThreadContextException) e;
        }
        else if (e instanceof ExecutionException || e instanceof CompletionException)
        {
            // Recurse into the underlying exception from Future, CompletableFuture etc.
            wrapper = wrapRootThrowable (e.getCause ());
        }
        else
        {
            // Wrap in a fault exception.
            wrapper = wrapThrowable (e);
        }

        return wrapper;
    }

    public static FaultException wrapThrowable (final Throwable e)
    {
        // Retain the actual exception as the nested exception (cause). getExceptionDetails
        // also includes the ThreadContext context id.
        final String exceptionDetails =
            HcUtil.getExceptionDetails (e, "An exception is being rethrown as FaultException:");

        outputFatalExceptionDetails (exceptionDetails);

        return new FaultException (e, true);
    }

    /** Exposed for unit testing */
    static String formThreadName (final ContextData contextData, final String contextId)
    {
        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        // Get scope names from deque. They are ordered from inner to outer.
        final List<String> names = scopeNesting.stream ()//
            .map (s -> s.m_name)//
            .filter (n -> HcUtil.safeGetLength (n) > 0)//
            .collect (Collectors.toList ());
        Collections.reverse (names);

        // Reverse order from outer to inner and concatenate.
        final String partialThreadName = names.stream ()//
            .collect (Collectors.joining (":"));

        // Prepend with original thread name and append context id.
        final String threadName =
            partialThreadName.length () > 0 ? contextData.m_originalThreadName + ":" + partialThreadName + contextId
                : contextData.m_originalThreadName + contextId;

        return threadName;
    }

    private static String allocateUniqueId (final ContextData contextData)
    {
        // Qualify by thread id.
        final long tid = Thread.currentThread ().getId ();

        // Qualify by epoch to distinguish between context ids after processor restart.
        // System.nanoTime method can only be used to measure elapsed time and is not
        // related to any other notion of system or wall-clock time, so it is only useful as
        // a general discriminator and it is possible that the same context id could be
        // generated twice (after OS reboot for example). As it it not unique anyway, just
        // use the most varying part as discriminator.
        final long epoch = System.nanoTime () % 0xFFFFL;

        final Deque<NestedScopeData> scopeNesting = contextData.m_scopeNesting;

        String id;
        if (scopeNesting.size () == 0)
        {
            // Use the global thread context sequence to allocate the outermost scope's context id.
            final long seq = m_idSequenceOuter.incrementAndGet ();
            id = String.format ("{%s:%X:%X:%X}", HcUtil.getThisHost (), tid, epoch, seq);
        }
        else
        {
            final NestedScopeData innermost = scopeNesting.peek ();

            final String contextIdParent = innermost.m_contextId;
            final String contextIdParentMinusBrace = contextIdParent.substring (0, contextIdParent.length () - 1);

            // Use the nested sequence to allocate the outermost scope's context id.
            final long seq = innermost.m_idSequence++;// threadlocal: threadsafe
            id = String.format ("%s:%X}", contextIdParentMinusBrace, seq);
        }

        return id;
    }

    private static ContextData getExistingContextData ()
    {
        return m_contextData.get ();
    }

    private static boolean isAnActiveScope ()
    {
        final ContextData contextData = m_contextData.get ();
        return contextData.m_scopeNesting.size () > 0;
    }

    private static void outputErrorExceptionDetails (final String exceptionDetails)
    {
        // Prevent logging of exceptions encountered within logging operations, since
        // this can cause an infinite loop.
        if (!isWithinLoggingOperation ())
        {
            Logging.logErrorString (exceptionDetails);
        }
        else
        {
            // Got an exception while doing some logging... don't use log system to report.
            System.err.printf ("Logging panic ERROR %s%n", exceptionDetails);
        }
    }

    private static void outputFatalExceptionDetails (final String exceptionDetails)
    {
        // Prevent logging of exceptions encountered within logging operations, since
        // this can cause an infinite loop.
        if (!isWithinLoggingOperation ())
        {
            Logging.logFatalString (exceptionDetails);
        }
        else
        {
            // Got an exception while doing some logging... don't use log system to report.
            System.err.printf ("Logging panic FATAL %s%n", exceptionDetails);
        }
    }

    /** Make the ContextData thread-local */
    private static ThreadLocal<ContextData> m_contextData = ThreadLocal.withInitial (ContextData::new);

    /** Sequence counter used to allocate outmost context id */
    private static AtomicLong m_idSequenceOuter = new AtomicLong (-1L);

    private static boolean m_policyErrorIncludeStackTrace;
}
