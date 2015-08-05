package au.com.breakpoint.hedron.core.dao.mock;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import au.com.breakpoint.hedron.core.log.Logging;

public class MockConnection implements Connection
{
    @Override
    public void abort (final Executor executor) throws SQLException
    {
        Logging.logDebug ("MockConnection.abort ()");
    }

    @Override
    public void clearWarnings () throws SQLException
    {
        Logging.logDebug ("MockConnection.clearWarnings ()");
    }

    @Override
    public void close () throws SQLException
    {
        Logging.logDebug ("MockConnection.close ()");
    }

    @Override
    public void commit () throws SQLException
    {
        Logging.logDebug ("MockConnection.commit ()");
    }

    @Override
    public Array createArrayOf (final String typeName, final Object[] elements) throws SQLException
    {
        Logging.logDebug ("MockConnection.createArrayOf ()");
        return null;
    }

    @Override
    public Blob createBlob () throws SQLException
    {
        Logging.logDebug ("MockConnection.createBlob ()");
        return null;
    }

    @Override
    public Clob createClob () throws SQLException
    {
        Logging.logDebug ("MockConnection.createClob ()");
        return null;
    }

    @Override
    public NClob createNClob () throws SQLException
    {
        Logging.logDebug ("MockConnection.createNClob ()");
        return null;
    }

    @Override
    public SQLXML createSQLXML () throws SQLException
    {
        Logging.logDebug ("MockConnection.createSQLXML ()");
        return null;
    }

    @Override
    public Statement createStatement () throws SQLException
    {
        Logging.logDebug ("MockConnection.createStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public Statement createStatement (final int resultSetType, final int resultSetConcurrency) throws SQLException
    {
        Logging.logDebug ("MockConnection.createStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public Statement createStatement (final int resultSetType, final int resultSetConcurrency,
        final int resultSetHoldability) throws SQLException
    {
        Logging.logDebug ("MockConnection.createStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public Struct createStruct (final String typeName, final Object[] attributes) throws SQLException
    {
        Logging.logDebug ("MockConnection.createStruct ()");
        return null;
    }

    @Override
    public boolean getAutoCommit () throws SQLException
    {
        Logging.logDebug ("MockConnection.getAutoCommit ()");
        return false;
    }

    @Override
    public String getCatalog () throws SQLException
    {
        Logging.logDebug ("MockConnection.getCatalog ()");
        return null;
    }

    @Override
    public Properties getClientInfo () throws SQLException
    {
        Logging.logDebug ("MockConnection.getClientInfo ()");
        return null;
    }

    @Override
    public String getClientInfo (final String name) throws SQLException
    {
        Logging.logDebug ("MockConnection.getClientInfo ()");
        return null;
    }

    @Override
    public int getHoldability () throws SQLException
    {
        Logging.logDebug ("MockConnection.getHoldability ()");
        return 0;
    }

    @Override
    public DatabaseMetaData getMetaData () throws SQLException
    {
        Logging.logDebug ("MockConnection.getMetaData ()");
        return null;
    }

    @Override
    public int getNetworkTimeout () throws SQLException
    {
        Logging.logDebug ("MockConnection.getNetworkTimeout ()");
        return 0;
    }

    @Override
    public String getSchema () throws SQLException
    {
        Logging.logDebug ("MockConnection.getSchema ()");
        return null;
    }

    @Override
    public int getTransactionIsolation () throws SQLException
    {
        Logging.logDebug ("MockConnection.getTransactionIsolation ()");
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap () throws SQLException
    {
        Logging.logDebug ("MockConnection.getTypeMap ()");
        return null;
    }

    @Override
    public SQLWarning getWarnings () throws SQLException
    {
        Logging.logDebug ("MockConnection.getWarnings ()");
        return null;
    }

    @Override
    public boolean isClosed () throws SQLException
    {
        Logging.logDebug ("MockConnection.isClosed ()");
        return false;
    }

    @Override
    public boolean isReadOnly () throws SQLException
    {
        Logging.logDebug ("MockConnection.isReadOnly ()");
        return false;
    }

    @Override
    public boolean isValid (final int timeout) throws SQLException
    {
        Logging.logDebug ("MockConnection.isValid ()");
        return false;
    }

    @Override
    public boolean isWrapperFor (final Class<?> iface) throws SQLException
    {
        Logging.logDebug ("MockConnection.isWrapperFor ()");
        return false;
    }

    @Override
    public String nativeSQL (final String sql) throws SQLException
    {
        Logging.logDebug ("MockConnection.nativeSQL ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency)
        throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency,
        final int resultSetHoldability) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int autoGeneratedKeys) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int resultSetType,
        final int resultSetConcurrency) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int resultSetType,
        final int resultSetConcurrency, final int resultSetHoldability) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int[] columnIndexes) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final String[] columnNames) throws SQLException
    {
        Logging.logDebug ("MockConnection.prepareStatement ()");
        return new MockPreparedStatement ();
    }

    @Override
    public void releaseSavepoint (final Savepoint savepoint) throws SQLException
    {
        Logging.logDebug ("MockConnection.releaseSavepoint ()");
    }

    @Override
    public void rollback () throws SQLException
    {
        Logging.logDebug ("MockConnection.rollback ()");
    }

    @Override
    public void rollback (final Savepoint savepoint) throws SQLException
    {
        Logging.logDebug ("MockConnection.rollback ()");
    }

    @Override
    public void setAutoCommit (final boolean autoCommit) throws SQLException
    {
        Logging.logDebug ("MockConnection.setAutoCommit ()");
    }

    @Override
    public void setCatalog (final String catalog) throws SQLException
    {
        Logging.logDebug ("MockConnection.setCatalog ()");
    }

    @Override
    public void setClientInfo (final Properties properties) throws SQLClientInfoException
    {
        Logging.logDebug ("MockConnection.setClientInfo ()");
    }

    @Override
    public void setClientInfo (final String name, final String value) throws SQLClientInfoException
    {
        Logging.logDebug ("MockConnection.setClientInfo ()");
    }

    @Override
    public void setHoldability (final int holdability) throws SQLException
    {
        Logging.logDebug ("MockConnection.setHoldability ()");
    }

    @Override
    public void setNetworkTimeout (final Executor executor, final int milliseconds) throws SQLException
    {
        Logging.logDebug ("MockConnection.setNetworkTimeout ()");
    }

    @Override
    public void setReadOnly (final boolean readOnly) throws SQLException
    {
        Logging.logDebug ("MockConnection.setReadOnly ()");
    }

    @Override
    public Savepoint setSavepoint () throws SQLException
    {
        Logging.logDebug ("MockConnection.setSavepoint ()");
        return null;
    }

    @Override
    public Savepoint setSavepoint (final String name) throws SQLException
    {
        Logging.logDebug ("MockConnection.setSavepoint ()");
        return null;
    }

    @Override
    public void setSchema (final String schema) throws SQLException
    {
        Logging.logDebug ("MockConnection.setSchema ()");
    }

    @Override
    public void setTransactionIsolation (final int level) throws SQLException
    {
        Logging.logDebug ("MockConnection.setTransactionIsolation ()");
    }

    @Override
    public void setTypeMap (final Map<String, Class<?>> map) throws SQLException
    {
        Logging.logDebug ("MockConnection.setTypeMap ()");
    }

    @Override
    public <T> T unwrap (final Class<T> iface) throws SQLException
    {
        Logging.logDebug ("MockConnection.unwrap ()");
        return null;
    }
}
