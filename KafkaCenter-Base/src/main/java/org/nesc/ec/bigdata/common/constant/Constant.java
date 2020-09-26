package org.nesc.ec.bigdata.common.constant;

/**
 * @author lg99
 */
public class Constant {

    public class Elasticsearch {
        public static final String HITS ="hits";
        public static final String AGGREGATIONS = "aggregations";
        public static final String SCROLL_ID="_scroll_id";
    }

    public class Search {
        public static final String SEARCH_SCROLL="/_search/scroll";
        public static final String SEARCH_SCROLL_1M = "/_search?scroll=1m";
        public static final String SEARCH_ ="/_search";
    }

    public class Encryption {
        public static final String MD5 = "MD5";
    }
    public class Num {
        public static final String ZERO = "0";
    }
}
