package au.com.breakpoint.hedron.core;

import java.util.function.BiFunction;
import au.com.breakpoint.hedron.core.log.Level;
import au.com.breakpoint.hedron.core.value.IValue;

public class NullFormatter implements BiFunction<Level, IValue<String>, String>
{
    @Override
    public String apply (final Level level, final IValue<String> v)
    {
        return v.get ();
    }
}