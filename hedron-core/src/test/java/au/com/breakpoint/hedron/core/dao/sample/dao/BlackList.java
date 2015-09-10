package au.com.breakpoint.hedron.core.dao.sample.dao;

import java.sql.Timestamp;
import au.com.breakpoint.hedron.core.dao.BaseEntity;
import au.com.breakpoint.hedron.core.dao.IColumnIndex;
import au.com.breakpoint.hedron.core.dao.IEntity;

/**
 * Low-level entity object encapsulating a BLACK_LIST row.
 */
public class BlackList extends BaseEntity<String>
{
    /** Default constructor */
    public BlackList ()
    {
    }

    /**
     * Copy constructor.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    public BlackList (final BlackList rhs)
    {
        copyFrom (rhs);
    }

    /**
     * Typesafe implementation of data copy.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    public void copyFrom (final BlackList rhs)
    {
        m_columnAvcId = rhs.m_columnAvcId;
        m_columnDateRequested = au.com.breakpoint.hedron.core.HcUtil.duplicate (rhs.m_columnDateRequested);
        m_columnReason = rhs.m_columnReason;
        m_columnOperatorId = rhs.m_columnOperatorId;
        m_columnReferenceId = rhs.m_columnReferenceId;
        m_columnActionId = rhs.m_columnActionId;
    }

    /**
     * IEntity implementation of data copy.
     *
     * @param rhs
     *            The object being copied (ie the right hand side of the assignment '=').
     */
    @Override
    public void copyFrom (final IEntity<String> rhs)
    {
        copyFrom ((BlackList) rhs);
    }

    /**
     * Column ActionId accessors.
     */
    public int getActionId ()
    {
        return m_columnActionId;
    }

    /**
     * Column AvcId accessors.
     */
    public String getAvcId ()
    {
        return m_columnAvcId;
    }

    /** IEntity implementation of getColumnValues () */
    @Override
    public Object[] getColumnValues (final ColumnType columnType)
    {
        Object[] values = null;

        switch (columnType)
        {
            case All:
            case NonIdentity:
            {
                values = new Object[]
                {
                        getAvcId (),
                        getDateRequested (),
                        getReason (),
                        getOperatorId (),
                        getReferenceId (),
                        getActionId ()
                };
                break;
            }

            case PrimaryKey:
            {
                values = new Object[]
                {
                        getAvcId ()
                };
                break;
            }

            case Updatable:
            {
                values = new Object[]
                {
                        getDateRequested (),
                        getReason (),
                        getOperatorId (),
                        getReferenceId (),
                        getActionId ()
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
     * Column DateRequested accessors.
     */
    public Timestamp getDateRequested ()
    {
        return m_columnDateRequested;
    }

    /**
     * Column OperatorId accessors.
     */
    public String getOperatorId ()
    {
        return m_columnOperatorId;
    }

    /** IEntity implementation of getPrimaryKey () */
    @Override
    public String getPrimaryKey ()
    {
        return m_columnAvcId;
    }

    /**
     * Column Reason accessors.
     */
    public String getReason ()
    {
        return m_columnReason;
    }

    /**
     * Column ReferenceId accessors.
     */
    public String getReferenceId ()
    {
        return m_columnReferenceId;
    }

    public void setActionId (final int columnActionId)
    {
        m_columnActionId = columnActionId;
    }

    public void setAvcId (final String columnAvcId)
    {
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (columnAvcId != null,
            "Column BlackList.AvcId value is classified as 'mandatory'; it cannot be null");
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (
            !shouldEnforceColumnLimits () || columnAvcId.length () <= SizeAvcId,
            "Column BlackList.AvcId value [%s] cannot be longer than %s characters", columnAvcId, SizeAvcId);
        m_columnAvcId = columnAvcId;
    }

    public void setDateRequested (final Timestamp columnDateRequested)
    {
        m_columnDateRequested = columnDateRequested;
    }

    public void setOperatorId (final String columnOperatorId)
    {
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (columnOperatorId != null,
            "Column BlackList.OperatorId value is classified as 'mandatory'; it cannot be null");
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (
            !shouldEnforceColumnLimits () || columnOperatorId.length () <= SizeOperatorId,
            "Column BlackList.OperatorId value [%s] cannot be longer than %s characters", columnOperatorId,
            SizeOperatorId);
        m_columnOperatorId = columnOperatorId;
    }

    public void setReason (final String columnReason)
    {
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (columnReason != null,
            "Column BlackList.Reason value is classified as 'mandatory'; it cannot be null");
        au.com.breakpoint.hedron.core.context.ThreadContext.assertError (
            !shouldEnforceColumnLimits () || columnReason.length () <= SizeReason,
            "Column BlackList.Reason value [%s] cannot be longer than %s characters", columnReason, SizeReason);
        m_columnReason = columnReason;
    }

    public void setReferenceId (final String columnReferenceId)
    {
        if (columnReferenceId != null && shouldEnforceColumnLimits ())
        {
            au.com.breakpoint.hedron.core.context.ThreadContext.assertError (
                columnReferenceId.length () <= SizeReferenceId,
                "Column BlackList.ReferenceId value [%s] cannot be longer than %s characters", columnReferenceId,
                SizeReferenceId);
        }
        m_columnReferenceId = columnReferenceId;
    }

    /** Logical identifiers for the columns, used in WhereElement, SetElement, etc */
    public static enum Columns implements IColumnIndex<BlackList>
    {
        AvcId (ColumnAvcId),
        DateRequested (ColumnDateRequested),
        Reason (ColumnReason),
        OperatorId (ColumnOperatorId),
        ReferenceId (ColumnReferenceId),
        ActionId (ColumnActionId);

        private Columns (final int index)
        {
            m_index = index;
        }

        @Override
        public int getColumnIndex ()
        {
            return m_index;
        }

        private final int m_index;
    }

    private String m_columnAvcId; // primary key

    private Timestamp m_columnDateRequested; // non-nullable

    private String m_columnReason; // non-nullable

    private String m_columnOperatorId; // non-nullable

    private String m_columnReferenceId; // nullable

    private int m_columnActionId; // non-nullable

    public static final int ColumnAvcId = 0;

    public static final int ColumnDateRequested = 1;

    public static final int ColumnReason = 2;

    public static final int ColumnOperatorId = 3;

    public static final int ColumnReferenceId = 4;

    public static final int ColumnActionId = 5;

    /**
     * Column AvcId.
     */
    public static final int SizeAvcId = 16;

    public static final boolean IsNullableAvcId = false;

    /**
     * Column DateRequested.
     */
    public static final boolean IsNullableDateRequested = false;

    /**
     * Column Reason.
     */
    public static final int SizeReason = 125;

    public static final boolean IsNullableReason = false;

    /**
     * Column OperatorId.
     */
    public static final int SizeOperatorId = 30;

    public static final boolean IsNullableOperatorId = false;

    /**
     * Column ReferenceId.
     */
    public static final int SizeReferenceId = 32;

    public static final boolean IsNullableReferenceId = true;

    /**
     * Column ActionId.
     */
    public static final boolean IsNullableActionId = false;

    private static final long serialVersionUID = 4508429214973765867L;
}
