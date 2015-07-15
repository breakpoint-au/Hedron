package au.com.breakpoint.hedron.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.value.IValue;

public class TimestampLevelFormatter implements BiFunction<Level, IValue<String>, String>
{
    @Override
    public String apply (final Level level, final IValue<String> v)
    {
        final String levelString = Level.getString (level);
        final String timestamp = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (new Date ());

        return timestamp + " " + levelString + " " + v.get ();
    }
}
