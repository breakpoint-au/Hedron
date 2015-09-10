package au.com.breakpoint.hedron.core.dao.sample.dao;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import au.com.breakpoint.hedron.core.context.ThreadContext;
import au.com.breakpoint.hedron.core.dao.BaseEntityDao;
import au.com.breakpoint.hedron.core.dao.DaoUtil;
import au.com.breakpoint.hedron.core.dao.FetchSql2;
import au.com.breakpoint.hedron.core.dao.OrderByElement;
import au.com.breakpoint.hedron.core.dao.SetElement;
import au.com.breakpoint.hedron.core.dao.UpdateSql2;
import au.com.breakpoint.hedron.core.dao.WhereElement;

/**
 * Low-level DAO object encapsulating the BLACK_LIST relation. Encapsulates Spring JDBC
 * template database access.
 */
public class BlackListDao extends BaseEntityDao<BlackList, String>
{
    public BlackListDao (final DataSource dataSource)
    {
        super (dataSource);
    }

    /**
     * Deletes the BLACK_LIST table row corresponding to the specified entity.
     *
     * @param id
     *            Type of the primary key.
     * @return whether or not a row was affected by the delete
     */
    @Override
    public boolean delete (final BlackList e)
    {
        return DaoUtil.performDelete (m_dataSource, e, SQL_DELETE) == 1;
    }

    /**
     * Deletes multiple rows from the BLACK_LIST table.
     *
     * @param es
     *            entities corresponding to the rows to be delete
     *
     * @return the numbers of rows affected by the update
     */
    @Override
    public void delete (final List<BlackList> es)
    {
        DaoUtil.performDeleteBatch (m_dataSource, es.toArray (new BlackList[es.size ()]), SQL_DELETE);
    }

    /**
     * Deletes rows from the BLACK_LIST table according to the criteria in whereElements.
     *
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return number of rows affected by the delete
     */
    @Override
    public int delete (final WhereElement[] whereElements)
    {
        return DaoUtil.performDelete (m_dataSource, SQL_FRAGMENT_DELETE, COLUMN_NAMES, whereElements);
    }

    /**
     * Deletes the BLACK_LIST table row corresponding to the specified primary key.
     *
     * @return whether or not a row was affected by the delete
     */
    @Override
    public boolean deleteByPrimaryKey (final String columnAvcId)
    {
        final WhereElement[] whereElements =
            {
                    new WhereElement (BlackList.ColumnAvcId, columnAvcId)
        };
        return delete (whereElements) == 1;
    }

    /**
     * Fetches the rows of the BLACK_LIST relation that satisfy the criteria in the
     * <i>whereElements</i> parameter.
     *
     * @param sql
     *            A convenient readable encapsulation of sql where clauses and order by
     *            statements.
     *
     * @return Collection of BlackList entities
     */
    public List<BlackList> fetch (final FetchSql2<BlackList> sql)
    {
        return fetch (sql.getWhereElements (), sql.getOrderByElements ());
    }

    /**
     * Fetches all rows of the BLACK_LIST relation.
     *
     * @return Collection of BlackList entities
     */
    @Override
    public List<BlackList> fetch (final OrderByElement... orderByElements)
    {
        return DaoUtil.performFetch (m_dataSource, BlackListDao.ROW_MAPPER, SQL_FRAGMENT_SELECT_FROM, COLUMN_NAMES,
            orderByElements);
    }

    /**
     * Fetches the rows of the BLACK_LIST relation that satisfy the criteria in the
     * <i>whereElements</i> parameter.
     *
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return Collection of BlackList entities
     */
    @Override
    public List<BlackList> fetch (final WhereElement[] whereElements, final OrderByElement... orderByElements)
    {
        return DaoUtil.performFetch (m_dataSource, BlackListDao.ROW_MAPPER, SQL_FRAGMENT_SELECT_FROM, COLUMN_NAMES,
            whereElements, orderByElements);
    }

    /**
     * Fetches the row of the BLACK_LIST relation for the specified primary key.
     *
     * @return The specified BlackList entity, or null if it does not exist
     */
    @Override
    public BlackList fetchByPrimaryKey (final String columnAvcId)
    {
        final WhereElement[] whereElements =
            {
                    new WhereElement (BlackList.ColumnAvcId, columnAvcId)
        };
        final List<BlackList> results = fetch (whereElements);

        return results.size () == 1 ? results.get (0) : null;
    }

    /**
     * Inserts a row into the BLACK_LIST table.
     *
     * @param e
     *            entity corresponding to the row to be inserted.
     */
    @Override
    public void insert (final BlackList e)
    {
        final int updateCount = DaoUtil.performInsert (m_dataSource, e, SQL_INSERT);
        ThreadContext.assertError (updateCount == 1, "BlackListDao insert failed");
    }

    /**
     * Inserts multiple rows into the BLACK_LIST table.
     *
     * @param es
     *            entities corresponding to the rows to be inserted
     */
    @Override
    public void insert (final List<BlackList> es)
    {
        DaoUtil.performInsertBatch (m_dataSource, es.toArray (new BlackList[es.size ()]), SQL_INSERT);
    }

    /**
     * Factory method for the BlackList entity.
     *
     * @return instance of the BlackList entity.
     */
    @Override
    public BlackList newEntityInstance ()
    {
        return new BlackList ();
    }

    /**
     * Updates the BLACK_LIST table row corresponding to the specified entity using its
     * primary key value.
     *
     * @param e
     *            entity corresponding to the row to be updated.
     *
     * @return whether or not a row was affected by the update
     */
    @Override
    public boolean update (final BlackList e)
    {
        return DaoUtil.performUpdate (m_dataSource, e, SQL_UPDATE_ALL_NON_PK) == 1;
    }

    /**
     * Updates multiple rows in the BLACK_LIST table.
     *
     * @param es
     *            entities corresponding to the rows to be inserted
     *
     * @return the numbers of rows affected by the update
     */
    @Override
    public void update (final List<BlackList> es)
    {
        DaoUtil.performUpdateBatch (m_dataSource, es.toArray (new BlackList[es.size ()]), SQL_UPDATE_ALL_NON_PK);
    }

    /**
     * Updates the BLACK_LIST table columns specified by newValues according to the
     * criteria in whereElements.
     *
     * @param newValues
     *            Collection of column/value pairs corresponding to the 'set' part of the
     *            update SQL.
     * @param whereElements
     *            Specified criteria to be added into the where clause. These result in
     *            SQL 'and' clauses. There is currently no way to directly express 'or'
     *            clauses, or more complex expressions. Use a custom query or stored
     *            procedure instead.
     *
     * @return the numbers of rows affected by the update
     */
    @Override
    public int update (final SetElement[] newValues, final WhereElement[] whereElements)
    {
        return DaoUtil.performUpdate (m_dataSource, ENTITY_NAME, COLUMN_NAMES, newValues, whereElements);
    }

    /**
     * Updates the BLACK_LIST table columns specified by newValues according to the
     * criteria in whereElements.
     *
     * @param sql
     *            A convenient readable encapsulation of update set statements and where
     *            clauses.
     *
     * @return the numbers of rows affected by the update
     */
    public int update (final UpdateSql2<BlackList> sql)
    {
        return update (sql.getSetElements (), sql.getWhereElements ());
    }

    public static void main (final String[] args)
    {
        final BlackListDao dao = new BlackListDao (null);

        final FetchSql2<BlackList> fetchSql = //
            new FetchSql2<BlackList> (BlackList.Columns.ReferenceId).greaterThanOrEqual (1) //
                .and (BlackList.Columns.ActionId).lessThan (2) //
                .orderBy (BlackList.Columns.OperatorId);

        @SuppressWarnings ("unused")
        final List<BlackList> es = dao.fetch (fetchSql);

        final UpdateSql2<BlackList> updateSql = //
            new UpdateSql2<BlackList> (BlackList.Columns.OperatorId).set ("qwerqwer") //
                .and (BlackList.Columns.ReferenceId).set ("asdfasdf") //
                .where (BlackList.Columns.ActionId).equal (1) //
                .and (BlackList.Columns.OperatorId).notEqual (1);

        dao.update (updateSql);
    }

    /** The physical name of the database entity */
    private static final String ENTITY_NAME = "BLACK_LIST";

    /** Physical column names corresponding to the BlackList.Columns values */
    public static final String[] COLUMN_NAMES =
        {
                "AVC_ID",
                "DATE_REQUESTED",
                "REASON",
                "OPERATOR_ID",
                "REFERENCE_ID",
                "ACTION_ID"
    };

    /**
     * Function to map BlackList entities from BLACK_LIST result sets.
     */
    public static final RowMapper<BlackList> ROW_MAPPER = (rs, rowNum) ->
    {
        final BlackList e = new BlackList ();

        e.setAvcId (rs.getString (COLUMN_NAMES[BlackList.ColumnAvcId]));
        e.setDateRequested (rs.getTimestamp (COLUMN_NAMES[BlackList.ColumnDateRequested]));
        e.setReason (rs.getString (COLUMN_NAMES[BlackList.ColumnReason]));
        e.setOperatorId (rs.getString (COLUMN_NAMES[BlackList.ColumnOperatorId]));
        final String valueReferenceId = rs.getString (COLUMN_NAMES[BlackList.ColumnReferenceId]);
        if (!rs.wasNull ())
        {
            e.setReferenceId (valueReferenceId);
        }
        e.setActionId (rs.getInt (COLUMN_NAMES[BlackList.ColumnActionId]));

        return e;
    };

    private static final String SQL_FRAGMENT_SELECT_FROM =
        "select AVC_ID, DATE_REQUESTED, REASON, OPERATOR_ID, REFERENCE_ID, ACTION_ID from BLACK_LIST";

    private static final String SQL_INSERT =
        "insert into BLACK_LIST (AVC_ID, DATE_REQUESTED, REASON, OPERATOR_ID, REFERENCE_ID, ACTION_ID) values (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_ALL_NON_PK =
        "update BLACK_LIST set DATE_REQUESTED = ?, REASON = ?, OPERATOR_ID = ?, REFERENCE_ID = ?, ACTION_ID = ? where AVC_ID = ?";

    private static final String SQL_DELETE = "delete from BLACK_LIST where AVC_ID = ?";

    private static final String SQL_FRAGMENT_DELETE = "delete from BLACK_LIST";
}
