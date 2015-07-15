package au.com.breakpoint.hedron.core.concurrent;

import java.util.concurrent.ThreadFactory;

public class CustomisingThreadFactory implements ThreadFactory
{
    public CustomisingThreadFactory (final String name)
    {
        this (name, false);
    }

    public CustomisingThreadFactory (final String name, final boolean isDaemon)
    {
        m_name = name;
        m_isDaemon = isDaemon;
    }

    @Override
    public Thread newThread (final Runnable r)
    {
        final Thread thread = new Thread (r, m_name);
        thread.setDaemon (m_isDaemon);

        return thread;
    }

    @Override
    public String toString ()
    {
        return String.format ("CustomisingThreadFactory [%s]", m_name);
    }

    private final boolean m_isDaemon;

    private final String m_name;
}