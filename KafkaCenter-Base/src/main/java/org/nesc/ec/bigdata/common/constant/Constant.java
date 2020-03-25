package org.nesc.ec.bigdata.common.constant;

/**
 * @author lg99
 */
public class Constant {

    public class ELASTICSEARCH{
        public static final String HITS ="hits";
        public static final String AGGREGATIONS = "aggregations";
        public static final String SCROLL_ID="_scroll_id";
    }

    public class SEARCH{
        public static final String SEARCH_SCROLL="/_search/scroll";
        public static final String SEARCH_SCROLL_1M = "/_search?scroll=1m";
        public static final String _SEARCH="/_search";
    }

    public class ENCRYPTION{
        public static final String MD5 = "MD5";
    }
    public class NUM{
        public static final String ZERO = "0";
    }
}
