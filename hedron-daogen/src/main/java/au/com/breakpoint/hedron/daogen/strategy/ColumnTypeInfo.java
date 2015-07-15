//                       __________________________________
//                ______|         Copyright 2008           |______
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
package au.com.breakpoint.hedron.daogen.strategy;

public class ColumnTypeInfo
{
    public enum CopyStyle
    {
        Duplicate, ShallowCopy; // Duplicate uses HcUtil.duplicate
    }

    public CopyStyle m_copyStyle;

    public String m_equalityExpression;

    public String m_hashCodeExpression;

    public String m_javaCastExpression; // eg (int) (Integer) xxx for integer type

    public String m_javaConvenienceType; // long for BigDecimal, etc

    public String m_javaConversionMethod; // eg au.com.breakpoint.util.HgUtil.getLongValue

    public String m_javaObjectType; // String, Integer, etc

    public String m_javaType; // String, int, Integer, etc

    public String m_javaTypeConstantSuffix; // eg L for long values

    public String m_javaTypeOfLimitValue; // byte, int, etc, used for generating constants

    public String m_jdbcJavaSqlType; // eg INTEGER for Types.INTEGER, etc

    public String m_jdbcResultSetAccessor; // the java type to be used when retrieving from StoredProcedureResultSet etc

    public String m_jdbcType; // the java type to be used when retrieving from StoredProcedureResultSet etc

    public long m_maxValue = Long.MIN_VALUE; // used when storing in an intrinsic type rather than java.math.BigDecimal

    public long m_minValue = Long.MAX_VALUE; // used when storing in an intrinsic type rather than java.math.BigDecimal

    public boolean m_nonPrimitiveTypeJavaLangType; // eg uses String/Integer etc

    public String m_precision; // for BigDecimal

    public String m_rowMapperType;

    public String m_scale; // for BigDecimal

    public int m_size = -1; // for String (CHAR/VARCHAR)
}