package au.com.breakpoint.hedron.core.dao;

import au.com.breakpoint.hedron.core.HcUtil;
import au.com.breakpoint.hedron.core.dao.BaseEntity;
import au.com.breakpoint.hedron.core.dao.IEntity;

/**
 * Low-level entity object encapsulating a RCS_V1 row.
 */
public class SampleEntity extends BaseEntity<Void>
{
    /**
     * Default constructor.
     */
    public SampleEntity ()
    {
    }

    /**
     * Copy constructor.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    public SampleEntity (final SampleEntity rhs)
    {
        copyFrom (rhs);
    }

    /**
     * IEntity implementation of data copy.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    @Override
    public void copyFrom (final IEntity<Void> rhs)
    {
        copyFrom ((SampleEntity) rhs);
    }

    /**
     * Typesafe implementation of data copy.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    public void copyFrom (final SampleEntity rhs)
    {
        m_columnResponseTime = au.com.breakpoint.hedron.core.HcUtil.duplicate (rhs.m_columnResponseTime);
        m_columnRcs = rhs.m_columnRcs;
        m_columnIsResponding = rhs.m_columnIsResponding;
    }

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
            final SampleEntity eRhs = (SampleEntity) o;
            isEqual = au.com.breakpoint.hedron.core.HcUtil.safeEquals (m_columnResponseTime,
                eRhs.m_columnResponseTime) && au.com.breakpoint.hedron.core.HcUtil.safeEquals (m_columnRcs,
                    eRhs.m_columnRcs) && m_columnIsResponding == eRhs.m_columnIsResponding;
        }
        return isEqual;
    }

    /**
     * IEntity implementation of getColumnValues ().
     */
    @Override
    public Object[] getColumnValues (final ColumnType columnType)
    {
        Object[] values = null;

        switch (columnType)
        {
            case All:
            case NonIdentity:
            case Updatable:
            {
                values = new Object[]
                {
                        getResponseTime (),
                        getRcs (),
                        getIsResponding ()
                };
                break;
            }

            default:
            {
                break;
            }
        }
        return values;
    }

    /**
     * Column IsResponding accessors.
     */
    public boolean getIsResponding ()
    {
        return m_columnIsResponding;
    }

    /**
     * IEntity implementation of getPrimaryKey ().
     */
    @Override
    public Void getPrimaryKey ()
    {
        return null;
    }

    /**
     * Column Rcs accessors.
     */
    public String getRcs ()
    {
        return m_columnRcs;
    }

    /**
     * Column ResponseTime accessors.
     */
    public java.sql.Timestamp getResponseTime ()
    {
        return m_columnResponseTime;
    }

    public void setIsResponding (final boolean columnIsResponding)
    {
        m_columnIsResponding = columnIsResponding;
    }

    public void setRcs (final String columnRcs)
    {
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (columnRcs != null,
            "Column RcsV1.Rcs value is classified as 'mandatory'; it cannot be null");
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (
            !shouldEnforceColumnLimits () || columnRcs.length () <= SizeRcs,
            "Column RcsV1.Rcs value [%s] cannot be longer than %s characters", columnRcs, SizeRcs);
        m_columnRcs = columnRcs;
    }

    public void setResponseTime (final java.sql.Timestamp columnResponseTime)
    {
        m_columnResponseTime = columnResponseTime;
    }

    @Override
    public String toString ()
    {
        //        return String.format ("RcsV1 = ResponseTime[%s] Rcs[%s] IsResponding[%s]", m_columnResponseTime, m_columnRcs,
        //            m_columnIsResponding);
        return HcUtil.deepToString (m_columnResponseTime, m_columnRcs, m_columnIsResponding);
    }

    /** Logical identifiers for the columns, used in WhereElement, SetElement, etc */
    public static class Columns
    {
        public static final int IsResponding = 2;

        public static final int Rcs = 1;

        public static final int ResponseTime = 0;
    }

    private boolean m_columnIsResponding;// non-nullable

    private String m_columnRcs;// non-nullable

    private java.sql.Timestamp m_columnResponseTime;// non-nullable

    /**
     * Column IsResponding.
     */
    public static final boolean IsNullableIsResponding = false;

    public static final boolean IsNullableRcs = false;

    /**
     * Column ResponseTime.
     */
    public static final boolean IsNullableResponseTime = false;

    /**
     * Column Rcs.
     */
    public static final int SizeRcs = 32;

    private static final long serialVersionUID = 4508429214973765867L;
}
