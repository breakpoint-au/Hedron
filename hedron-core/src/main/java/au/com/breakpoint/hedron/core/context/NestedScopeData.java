package au.com.breakpoint.hedron.core.context;

/** A set of data associated with the thread's context */
class NestedScopeData
{
    NestedScopeData (final IScope iScope, final String contextId, final String name, final String prevThreadName)
    {
        m_iScope = iScope;
        m_contextId = contextId;
        m_name = name;
        m_prevThreadName = prevThreadName;
    }

    /** Unique identifier for the request context */
    final String m_contextId;

    /** Sequence counter used to allocate each nested context id */
    long m_idSequence;

    /** The IScope object associated with this nested scope */
    final IScope m_iScope;

    /** A scope name, used for debugging etc */
    final String m_name;

    /** For restoring the previous thread name */
    final String m_prevThreadName;
}
