package au.com.breakpoint.hedron.core.dao.sample.dao;

/**
 * Class containing database metrics as enums.
 */
public class Enums
{
    public static enum AvcAction
    {
        EnforceDeprioritisationLongTerm (0, Definitions.ENUM_DESCRIPTIONS_AvcAction[0]),
        EnforceSpeedReduction (1, Definitions.ENUM_DESCRIPTIONS_AvcAction[1]),
        EnforceDeprioritisationShortTerm (2, Definitions.ENUM_DESCRIPTIONS_AvcAction[2]),
        RemoveDeprioritisationLongTerm (3, Definitions.ENUM_DESCRIPTIONS_AvcAction[3]),
        RemoveSpeedReduction (4, Definitions.ENUM_DESCRIPTIONS_AvcAction[4]),
        RemoveDeprioritisationShortTerm (5, Definitions.ENUM_DESCRIPTIONS_AvcAction[5]);

        private AvcAction (final int value, final String description)
        {
            m_value = value;
            m_description = description;
        }

        public String getDescription ()
        {
            return m_description;
        }

        public int getValue ()
        {
            return m_value;
        }

        public static AvcAction of (final int value)
        {
            AvcAction e = null;

            switch (value)
            {
                case 0:
                {
                    e = EnforceDeprioritisationLongTerm;
                    break;
                }

                case 1:
                {
                    e = EnforceSpeedReduction;
                    break;
                }

                case 2:
                {
                    e = EnforceDeprioritisationShortTerm;
                    break;
                }

                case 3:
                {
                    e = RemoveDeprioritisationLongTerm;
                    break;
                }

                case 4:
                {
                    e = RemoveSpeedReduction;
                    break;
                }

                case 5:
                {
                    e = RemoveDeprioritisationShortTerm;
                    break;
                }

                default:
                {
                    break;
                }
            }

            return e;
        }

        private final int m_value;

        private final String m_description;
    }
}
