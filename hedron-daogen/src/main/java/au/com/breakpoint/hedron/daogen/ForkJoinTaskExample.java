package au.com.breakpoint.hedron.daogen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import au.com.breakpoint.hedron.core.HcUtil;

public class ForkJoinTaskExample
{
    public static void main (final String[] args)
    {
        final Function<Double, String> work = input -> input.toString ();

        // Calculate four inputs
        final List<Double> inputs = Arrays.asList (1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

        final List<String> result = HcUtil.executeConcurrently (inputs, work);
        System.out.println (result);
    }
}
