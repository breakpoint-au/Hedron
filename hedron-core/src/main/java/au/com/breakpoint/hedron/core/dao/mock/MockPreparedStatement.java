package au.com.breakpoint.hedron.core.dao.mock;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import au.com.breakpoint.hedron.core.log.Logging;

public class MockPreparedStatement implements PreparedStatement
{
    @Override
    public void addBatch () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.addBatch");
    }

    @Override
    public void addBatch (final String sql) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.addBatch");
    }

    @Override
    public void cancel () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.cancel");
    }

    @Override
    public void clearBatch () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.clearBatch");
    }

    @Override
    public void clearParameters () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.clearParameters");
    }

    @Override
    public void clearWarnings () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.clearWarnings");
    }

    @Override
    public void close () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.close");
    }

    @Override
    public void closeOnCompletion () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.closeOnCompletion");
    }

    @Override
    public boolean execute () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.execute");
        return true;
    }

    @Override
    public boolean execute (final String sql) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.execute");
        return true;
    }

    @Override
    public boolean execute (final String sql, final int autoGeneratedKeys) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.execute");
        return true;
    }

    @Override
    public boolean execute (final String sql, final int[] columnIndexes) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.execute");
        return true;
    }

    @Override
    public boolean execute (final String sql, final String[] columnNames) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.execute");
        return true;
    }

    @Override
    public int[] executeBatch () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeBatch");
        return null;
    }

    @Override
    public ResultSet executeQuery () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeQuery");
        return null;
    }

    @Override
    public ResultSet executeQuery (final String sql) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeQuery");
        return null;
    }

    @Override
    public int executeUpdate () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeUpdate");
        return 1;
    }

    @Override
    public int executeUpdate (final String sql) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeUpdate");
        return 1;
    }

    @Override
    public int executeUpdate (final String sql, final int autoGeneratedKeys) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeUpdate");
        return 1;
    }

    @Override
    public int executeUpdate (final String sql, final int[] columnIndexes) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeUpdate");
        return 1;
    }

    @Override
    public int executeUpdate (final String sql, final String[] columnNames) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.executeUpdate");
        return 1;
    }

    @Override
    public Connection getConnection () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getConnection");
        return null;
    }

    @Override
    public int getFetchDirection () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getFetchDirection");
        return 0;
    }

    @Override
    public int getFetchSize () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getFetchSize");
        return 0;
    }

    @Override
    public ResultSet getGeneratedKeys () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getGeneratedKeys");
        return null;
    }

    @Override
    public int getMaxFieldSize () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getMaxFieldSize");
        return 0;
    }

    @Override
    public int getMaxRows () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getMaxRows");
        return 0;
    }

    @Override
    public ResultSetMetaData getMetaData () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getMetaData");
        return null;
    }

    @Override
    public boolean getMoreResults () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getMoreResults");
        return false;
    }

    @Override
    public boolean getMoreResults (final int current) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getMoreResults");
        return false;
    }

    @Override
    public ParameterMetaData getParameterMetaData () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getParameterMetaData");
        return null;
    }

    @Override
    public int getQueryTimeout () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getQueryTimeout");
        return 0;
    }

    @Override
    public ResultSet getResultSet () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getResultSet");
        return null;
    }

    @Override
    public int getResultSetConcurrency () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getResultSetConcurrency");
        return 0;
    }

    @Override
    public int getResultSetHoldability () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getResultSetHoldability");
        return 0;
    }

    @Override
    public int getResultSetType () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getResultSetType");
        return 0;
    }

    @Override
    public int getUpdateCount () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getUpdateCount");
        return 1;
    }

    @Override
    public SQLWarning getWarnings () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.getWarnings");
        return null;
    }

    @Override
    public boolean isClosed () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.isClosed");
        return false;
    }

    @Override
    public boolean isCloseOnCompletion () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.isCloseOnCompletion");
        return false;
    }

    @Override
    public boolean isPoolable () throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.isPoolable");
        return false;
    }

    @Override
    public boolean isWrapperFor (final Class<?> iface) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.isWrapperFor");
        return false;
    }

    @Override
    public void setArray (final int parameterIndex, final Array x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setArray");
    }

    @Override
    public void setAsciiStream (final int parameterIndex, final InputStream x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setAsciiStream");
    }

    @Override
    public void setAsciiStream (final int parameterIndex, final InputStream x, final int length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setAsciiStream");
    }

    @Override
    public void setAsciiStream (final int parameterIndex, final InputStream x, final long length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setAsciiStream");
    }

    @Override
    public void setBigDecimal (final int parameterIndex, final BigDecimal x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBigDecimal");
    }

    @Override
    public void setBinaryStream (final int parameterIndex, final InputStream x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBinaryStream");
    }

    @Override
    public void setBinaryStream (final int parameterIndex, final InputStream x, final int length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBinaryStream");
    }

    @Override
    public void setBinaryStream (final int parameterIndex, final InputStream x, final long length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBinaryStream");
    }

    @Override
    public void setBlob (final int parameterIndex, final Blob x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBlob");
    }

    @Override
    public void setBlob (final int parameterIndex, final InputStream inputStream) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBlob");
    }

    @Override
    public void setBlob (final int parameterIndex, final InputStream inputStream, final long length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBlob");
    }

    @Override
    public void setBoolean (final int parameterIndex, final boolean x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBoolean");
    }

    @Override
    public void setByte (final int parameterIndex, final byte x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setByte");
    }

    @Override
    public void setBytes (final int parameterIndex, final byte[] x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setBytes");
    }

    @Override
    public void setCharacterStream (final int parameterIndex, final Reader reader) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setCharacterStream");
    }

    @Override
    public void setCharacterStream (final int parameterIndex, final Reader reader, final int length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setCharacterStream");
    }

    @Override
    public void setCharacterStream (final int parameterIndex, final Reader reader, final long length)
        throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setCharacterStream");
    }

    @Override
    public void setClob (final int parameterIndex, final Clob x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setClob");
    }

    @Override
    public void setClob (final int parameterIndex, final Reader reader) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setClob");
    }

    @Override
    public void setClob (final int parameterIndex, final Reader reader, final long length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setClob");
    }

    @Override
    public void setCursorName (final String name) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setCursorName");
    }

    @Override
    public void setDate (final int parameterIndex, final Date x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setDate");
    }

    @Override
    public void setDate (final int parameterIndex, final Date x, final Calendar cal) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setDate");
    }

    @Override
    public void setDouble (final int parameterIndex, final double x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setDouble");
    }

    @Override
    public void setEscapeProcessing (final boolean enable) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setEscapeProcessing");
    }

    @Override
    public void setFetchDirection (final int direction) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setFetchDirection");
    }

    @Override
    public void setFetchSize (final int rows) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setFetchSize");
    }

    @Override
    public void setFloat (final int parameterIndex, final float x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setFloat");
    }

    @Override
    public void setInt (final int parameterIndex, final int x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setInt");
    }

    @Override
    public void setLong (final int parameterIndex, final long x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setLong");
    }

    @Override
    public void setMaxFieldSize (final int max) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setMaxFieldSize");
    }

    @Override
    public void setMaxRows (final int max) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setMaxRows");
    }

    @Override
    public void setNCharacterStream (final int parameterIndex, final Reader value) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNCharacterStream");
    }

    @Override
    public void setNCharacterStream (final int parameterIndex, final Reader value, final long length)
        throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNCharacterStream");
    }

    @Override
    public void setNClob (final int parameterIndex, final NClob value) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNClob");
    }

    @Override
    public void setNClob (final int parameterIndex, final Reader reader) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNClob");
    }

    @Override
    public void setNClob (final int parameterIndex, final Reader reader, final long length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNClob");
    }

    @Override
    public void setNString (final int parameterIndex, final String value) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNString");
    }

    @Override
    public void setNull (final int parameterIndex, final int sqlType) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNull");
    }

    @Override
    public void setNull (final int parameterIndex, final int sqlType, final String typeName) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setNull");
    }

    @Override
    public void setObject (final int parameterIndex, final Object x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setObject");
    }

    @Override
    public void setObject (final int parameterIndex, final Object x, final int targetSqlType) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setObject");
    }

    @Override
    public void setObject (final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength)
        throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setObject");
    }

    @Override
    public void setPoolable (final boolean poolable) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setPoolable");
    }

    @Override
    public void setQueryTimeout (final int seconds) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setQueryTimeout");
    }

    @Override
    public void setRef (final int parameterIndex, final Ref x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setRef");
    }

    @Override
    public void setRowId (final int parameterIndex, final RowId x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setRowId");
    }

    @Override
    public void setShort (final int parameterIndex, final short x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setShort");
    }

    @Override
    public void setSQLXML (final int parameterIndex, final SQLXML xmlObject) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setSQLXML");
    }

    @Override
    public void setString (final int parameterIndex, final String x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setString");
    }

    @Override
    public void setTime (final int parameterIndex, final Time x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setTime");
    }

    @Override
    public void setTime (final int parameterIndex, final Time x, final Calendar cal) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setTime");
    }

    @Override
    public void setTimestamp (final int parameterIndex, final Timestamp x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setTimestamp");
    }

    @Override
    public void setTimestamp (final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setTimestamp");
    }

    @Override
    public void setUnicodeStream (final int parameterIndex, final InputStream x, final int length) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setUnicodeStream");
    }

    @Override
    public void setURL (final int parameterIndex, final URL x) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.setURL");
    }

    @Override
    public <T> T unwrap (final Class<T> iface) throws SQLException
    {
        Logging.logDebug ("MockPreparedStatement.unwrap");
        return null;
    }
}
