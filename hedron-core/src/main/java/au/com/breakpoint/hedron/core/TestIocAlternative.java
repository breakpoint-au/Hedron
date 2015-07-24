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
package au.com.breakpoint.hedron.core;

class TestIocAlternative
{
    interface IReader
    {
        int read ();
    }

    interface IWriter
    {
        void write (Integer v);
    }

    static class Reader implements IReader
    {
        public Reader (final int i)
        {
            m_i = i;
        }

        @Override
        public int read ()
        {
            return m_i;
        }

        private final int m_i;
    }

    static class Writer implements IWriter
    {
        @Override
        public void write (final Integer v)
        {
            System.out.printf ("data [%s]%n", v);
        }
    }

    /**
     * ------------------- With context -------------------
     */
    static class Z2
    {
        static class Augmenter implements IAugmenter
        {
            public Augmenter (final int i)
            {
                m_i = i;
            }

            @Override
            public String augmenter (final String input)
            {
                return input + m_i;
            }

            private final int m_i;
        }

        interface IAugmenter
        {
            String augmenter (String input);
        }

        interface IContext extends Persister.IContext, Poller.IContext
        {
        }

        interface IPersister
        {
            void persist (Integer data);
        }

        interface IPoller
        {
            String getData ();
        }

        interface IProcessor
        {
            void process ();
        }

        interface ITransformer
        {
            Integer transform (String input);
        }

        static class Persister implements IPersister
        {
            public Persister (final IContext context)
            {
                m_context = context;
            }

            @Override
            public void persist (final Integer data)
            {
                m_context.getWriter ().write (data);
            }

            public interface IContext
            {
                IWriter getWriter ();
            }

            private final IContext m_context;
        }

        static class Poller implements IPoller
        {
            public Poller (final IContext context, final IAugmenter augmenter)
            {
                m_context = context;
                m_augmenter = augmenter;
            }

            @Override
            public String getData ()
            {
                final int arg = m_context.getReader ().read ();

                final String valueOf = String.valueOf (arg);
                return m_augmenter.augmenter (valueOf);
            }

            public interface IContext
            {
                IReader getReader ();
            }

            private final IAugmenter m_augmenter;

            private final IContext m_context;
        }

        static class Processor implements IProcessor
        {
            Processor (final IPoller poller, final ITransformer transformer, final IPersister persister)
            {
                m_poller = poller;
                m_transformer = transformer;
                m_persister = persister;
            }

            @Override
            public void process ()
            {
                final String data = m_poller.getData ();
                final Integer outputData = m_transformer.transform (data);
                m_persister.persist (outputData);
            }

            private final IPersister m_persister;

            private final IPoller m_poller;

            private final ITransformer m_transformer;
        }

        static class Transformer implements ITransformer
        {
            @Override
            public Integer transform (final String input)
            {
                return Integer.valueOf (input) + 10;
            }
        }

        public static void test ()
        {
            // Context.
            final IContext context = getContext ();

            // Composition.
            final IAugmenter augmenter = new Augmenter (3);
            final IPoller poller = new Poller (context, augmenter);
            final ITransformer transformer = new Transformer ();
            final IPersister persister = new Persister (context);

            final IProcessor p = new Processor (poller, transformer, persister);
            p.process ();
        }

        /** Implemented here as closure rather than standalone class */
        private static IContext getContext ()
        {
            final IReader reader = new Reader (5);
            final IWriter writer = new Writer ();
            final IContext context = new IContext ()
            {
                @Override
                public IReader getReader ()
                {
                    return reader;
                }

                @Override
                public IWriter getWriter ()
                {
                    return writer;
                }
            };
            return context;
        }
    }

    public static void main (final String[] args)
    {
        Z2.test ();
    }
}
