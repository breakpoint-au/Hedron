package au.com.breakpoint.hedron.core.context;

import java.util.ArrayDeque;
import java.util.Deque;

/** A set of data associated with the thread's context */
class ContextData
{
    /**
     * Counter used to track nested logging operations in order to prevent logging of
     * exceptions encountered within logging operations, since this can cause an infinite
     * loop.
     */
    int m_loggingOperationDepth;

    /**
     * Counter used to track nested logging operations in order to prevent logging of
     * exceptions encountered within logging operations, since this can cause an infinite
     * loop.
     */
    int m_loggingSilenceDepth;

    /** Support thread naming for JStack visibility. Mutable for unit testing */
    String m_originalThreadName = Thread.currentThread ().getName ();

    /** A stack representing the current set of nested scopes */
    final Deque<NestedScopeData> m_scopeNesting = new ArrayDeque<> ();
}
