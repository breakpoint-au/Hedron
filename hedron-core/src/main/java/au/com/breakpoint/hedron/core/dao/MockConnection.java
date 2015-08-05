package au.com.breakpoint.hedron.core.dao;

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
        Logging.logInfo ("MockConnection.abort ()");
    }

    @Override
    public void clearWarnings () throws SQLException
    {
        Logging.logInfo ("MockConnection.clearWarnings ()");
    }

    @Override
    public void close () throws SQLException
    {
        Logging.logInfo ("MockConnection.close ()");
    }

    @Override
    public void commit () throws SQLException
    {
        Logging.logInfo ("MockConnection.commit ()");
    }

    @Override
    public Array createArrayOf (final String typeName, final Object[] elements) throws SQLException
    {
        Logging.logInfo ("MockConnection.createArrayOf ()");
        return null;
    }

    @Override
    public Blob createBlob () throws SQLException
    {
        Logging.logInfo ("MockConnection.createBlob ()");
        return null;
    }

    @Override
    public Clob createClob () throws SQLException
    {
        Logging.logInfo ("MockConnection.createClob ()");
        return null;
    }

    @Override
    public NClob createNClob () throws SQLException
    {
        Logging.logInfo ("MockConnection.createNClob ()");
        return null;
    }

    @Override
    public SQLXML createSQLXML () throws SQLException
    {
        Logging.logInfo ("MockConnection.createSQLXML ()");
        return null;
    }

    @Override
    public Statement createStatement () throws SQLException
    {
        Logging.logInfo ("MockConnection.createStatement ()");
        return null;
    }

    @Override
    public Statement createStatement (final int resultSetType, final int resultSetConcurrency) throws SQLException
    {
        Logging.logInfo ("MockConnection.createStatement ()");
        return null;
    }

    @Override
    public Statement createStatement (final int resultSetType, final int resultSetConcurrency,
        final int resultSetHoldability) throws SQLException
    {
        Logging.logInfo ("MockConnection.createStatement ()");
        return null;
    }

    @Override
    public Struct createStruct (final String typeName, final Object[] attributes) throws SQLException
    {
        Logging.logInfo ("MockConnection.createStruct ()");
        return null;
    }

    @Override
    public boolean getAutoCommit () throws SQLException
    {
        Logging.logInfo ("MockConnection.getAutoCommit ()");
        return false;
    }

    @Override
    public String getCatalog () throws SQLException
    {
        Logging.logInfo ("MockConnection.getCatalog ()");
        return null;
    }

    @Override
    public Properties getClientInfo () throws SQLException
    {
        Logging.logInfo ("MockConnection.getClientInfo ()");
        return null;
    }

    @Override
    public String getClientInfo (final String name) throws SQLException
    {
        Logging.logInfo ("MockConnection.getClientInfo ()");
        return null;
    }

    @Override
    public int getHoldability () throws SQLException
    {
        Logging.logInfo ("MockConnection.getHoldability ()");
        return 0;
    }

    @Override
    public DatabaseMetaData getMetaData () throws SQLException
    {
        Logging.logInfo ("MockConnection.getMetaData ()");
        return null;
    }

    @Override
    public int getNetworkTimeout () throws SQLException
    {
        Logging.logInfo ("MockConnection.getNetworkTimeout ()");
        return 0;
    }

    @Override
    public String getSchema () throws SQLException
    {
        Logging.logInfo ("MockConnection.getSchema ()");
        return null;
    }

    @Override
    public int getTransactionIsolation () throws SQLException
    {
        Logging.logInfo ("MockConnection.getTransactionIsolation ()");
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap () throws SQLException
    {
        Logging.logInfo ("MockConnection.getTypeMap ()");
        return null;
    }

    @Override
    public SQLWarning getWarnings () throws SQLException
    {
        Logging.logInfo ("MockConnection.getWarnings ()");
        return null;
    }

    @Override
    public boolean isClosed () throws SQLException
    {
        Logging.logInfo ("MockConnection.isClosed ()");
        return false;
    }

    @Override
    public boolean isReadOnly () throws SQLException
    {
        Logging.logInfo ("MockConnection.isReadOnly ()");
        return false;
    }

    @Override
    public boolean isValid (final int timeout) throws SQLException
    {
        Logging.logInfo ("MockConnection.isValid ()");
        return false;
    }

    @Override
    public boolean isWrapperFor (final Class<?> iface) throws SQLException
    {
        Logging.logInfo ("MockConnection.isWrapperFor ()");
        return false;
    }

    @Override
    public String nativeSQL (final String sql) throws SQLException
    {
        Logging.logInfo ("MockConnection.nativeSQL ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency)
        throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency,
        final int resultSetHoldability) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareCall ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int autoGeneratedKeys) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int resultSetType,
        final int resultSetConcurrency) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int resultSetType,
        final int resultSetConcurrency, final int resultSetHoldability) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final int[] columnIndexes) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public PreparedStatement prepareStatement (final String sql, final String[] columnNames) throws SQLException
    {
        Logging.logInfo ("MockConnection.prepareStatement ()");
        return null;
    }

    @Override
    public void releaseSavepoint (final Savepoint savepoint) throws SQLException
    {
        Logging.logInfo ("MockConnection.releaseSavepoint ()");
    }

    @Override
    public void rollback () throws SQLException
    {
        Logging.logInfo ("MockConnection.rollback ()");
    }

    @Override
    public void rollback (final Savepoint savepoint) throws SQLException
    {
        Logging.logInfo ("MockConnection.rollback ()");
    }

    @Override
    public void setAutoCommit (final boolean autoCommit) throws SQLException
    {
        Logging.logInfo ("MockConnection.setAutoCommit ()");
    }

    @Override
    public void setCatalog (final String catalog) throws SQLException
    {
        Logging.logInfo ("MockConnection.setCatalog ()");
    }

    @Override
    public void setClientInfo (final Properties properties) throws SQLClientInfoException
    {
        Logging.logInfo ("MockConnection.setClientInfo ()");
    }

    @Override
    public void setClientInfo (final String name, final String value) throws SQLClientInfoException
    {
        Logging.logInfo ("MockConnection.setClientInfo ()");
    }

    @Override
    public void setHoldability (final int holdability) throws SQLException
    {
        Logging.logInfo ("MockConnection.setHoldability ()");
    }

    @Override
    public void setNetworkTimeout (final Executor executor, final int milliseconds) throws SQLException
    {
        Logging.logInfo ("MockConnection.setNetworkTimeout ()");
    }

    @Override
    public void setReadOnly (final boolean readOnly) throws SQLException
    {
        Logging.logInfo ("MockConnection.setReadOnly ()");
    }

    @Override
    public Savepoint setSavepoint () throws SQLException
    {
        Logging.logInfo ("MockConnection.setSavepoint ()");
        return null;
    }

    @Override
    public Savepoint setSavepoint (final String name) throws SQLException
    {
        Logging.logInfo ("MockConnection.setSavepoint ()");
        return null;
    }

    @Override
    public void setSchema (final String schema) throws SQLException
    {
        Logging.logInfo ("MockConnection.setSchema ()");
    }

    @Override
    public void setTransactionIsolation (final int level) throws SQLException
    {
        Logging.logInfo ("MockConnection.setTransactionIsolation ()");
    }

    @Override
    public void setTypeMap (final Map<String, Class<?>> map) throws SQLException
    {
        Logging.logInfo ("MockConnection.setTypeMap ()");
    }

    @Override
    public <T> T unwrap (final Class<T> iface) throws SQLException
    {
        Logging.logInfo ("MockConnection.unwrap ()");
        return null;
    }
}
