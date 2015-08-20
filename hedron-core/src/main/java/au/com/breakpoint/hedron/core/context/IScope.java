//                       __________________________________
//                ______|      Copyright 2008-2015         |______
//                \     |     Breakpoint Pty Limited       |     /
//                 \    |   http://www.breakpoint.com.au   |    /
//                 /    |__________________________________|    \
//                /_________/                          \_________\
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//	   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing
// permissions and limitations under the License.
//
package au.com.breakpoint.hedron.core.context;

import au.com.breakpoint.hedron.core.ICloseable;

/**
 * IScope is used to delimit a scope of program execution, say the handling of a GET
 * method in an HTTP servlet. On entry to the servlet, the program opens an
 * ExecutionScope. Just prior to exit, the scope is closed. For example, in a simple
 * servlet application:
 *
 * <code>
 *
 * &#64;Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
 *           ServletException, IOException { final IScope scope = new ExecutionScope ();
 *
 *           // Local IScope. try { // ... do work here ... } catch (AssertException e) {
 *           // Failure encountered during execution scope (scope.assertXxx), and control
 *           // is transferred to this point. DisplayApplicationSpecificInfoPage (); }
 *           finally { scope.close (); } } </code>
 *
 *           ThreadContext sees that it is the outermost IScope (ie it is not nested
 *           within another IScope), and allocates a request id that is included on all
 *           logging during the execution of the code in the try block, including nested
 *           IScope blocks.
 *
 *           If an application-level contingency exception occurs,
 *           DisplayApplicationSpecificInfoPage () is used to handle the contingency by
 *           displaying a suitable message to the user.
 *
 *           Note: the doGet () method does not provide a top level fault barrier - that
 *           barrier is provided by the servlet mechanism itself, and error pages are
 *           invoked via the web.xml <error-page/> mechanism. Therefore
 *           ThreadContext.assertFault will result in the
 *           DisplayApplicationSpecificInfoPage () handling.
 *
 *           As another example, a Java console application such as a daemon process
 *           provides its own top level fault barrier. For example, <code>
 *     public class SomeDaemon
 *     {
 *         public static void main (String[] args)
 *         {
 *             // Top level fault barrier -- not ExecutionScope-based.
 *             try
 *             {
 *                 final SomeDaemon SomeDaemon = new SomeDaemon ();
 *                 SomeDaemon.run (args);
 *             }
 *             catch (Throwable e)
 *             {
 *                 // Top level fault barrier. This covers FaultException also. Display error
 *                 // message.
 *                 System.out.println (HcUtil.getExceptionDetails (e,
 *                                     "Program fault occurred"));
 *             }
 *         }
 *
 *         public void run (String[] args)
 *         {
 *             final IScope scope = new ExecutionScope ();
 *
 *             // Local IScope.
 *             try
 *             {
 *                 ThreadContext.assertWarning (args.length == 2,
 *                               "Usage:%n    SomeDaemon <host name> <port name>");
 *
 *                 // ... do work ...
 *             }
 *             catch (AssertException e)
 *             {
 *                 // Failure encountered during execution scope (scope.assertXxxx), and
 *                 // control is transferred to this point.
 *             }
 *             finally
 *             {
 *                 scope.close ();
 *             }
 *
 *             if (ThreadContext.getOpResultList ().size () > 0)
 *             {
 *                 System.out.printf ("Notes:%n");
 *                 for (OpResult r : ThreadContext.getOpResultList ())
 *                 {
 *                     System.out.printf ("  %s: %s%n", r.getSeverity (), r.getDescription ());
 *                 }
 *             }
 *         }
 *     }
 * </code>
 *
 *           In this example, the top level fault barrier is provided in the main ()
 *           method. It simply catches and reports fault-level exceptions that propagate
 *           from the run () method. The run () method is implemented like the servlet
 *           example, with an ExecutionScope around the work performed by the process.
 *
 *           The main motivation for the IScope pattern is to support nested scopes. That
 *           is, one IScope is instantiated inside another, as shown here: <code>
 *     public class SomeDaemon
 *     {
 *         public void run (String[] args)
 *         {
 *             final IScope scope = new ExecutionScope ();
 *
 *             // Local IScope.
 *             try
 *             {
 *                 // ... do work ...
 *
 *                 HelperClass.doSomething ();
 *
 *                 // ... do more work ...
 *             }
 *             finally
 *             {
 *                 scope.close ();
 *             }
 *         }
 *     }
 *
 *     public class HelperClass
 *     {
 *         public void doSomething ()
 *         {
 *             final IScope scope = new ExecutionScope ();
 *
 *             // Local IScope.
 *             try
 *             {
 *                 // ... do work ...
 *             }
 *             finally
 *             {
 *                 scope.close ();
 *             }
 *         }
 *     }
 * </code>
 *
 *           IScope keeps track of the scope nesting, and maintains the same request
 *           context across them until the outer scope is closed, so logging entries will
 *           consistently show the overall request context identifier.
 *
 *           This capability yields dividends in a transactional service environment. That
 *           is, where an outer level software layer such as web tier calls nested
 *           transactional services. See ITransactionScope and its subclasses.
 *
 * @see ExecutionScope
 * @see ITransactionScope
 * @see SimpleJdbcTransactionScope
 * @see ContainerManagedTransactionScope
 * @see BeanManagedTransactionScope
 */
public interface IScope extends ICloseable
{
}
