package org.nesc.ec.bigdata.constant;

/**
 * @author lg99
 */
public class Constants {

	/** Generate the constants needed for JSON */
	public static class JsonObject{
		public static final String NAME = "name";
		public static final String VALUE = "value";
		public static final String LABEL = "label";
		public static final String SUMMARY = "summary";
		public static final String ID = "id";
		public static final String TYPE = "type";
		public static final String KEY = "key";
		public static final String CONFIG = "config";
		public static final String TIME = "time";

	}

	/** Generate the constants for Time Number */
	public static class Time{
		public static final int THOUSAND = 1000;
		public static final int FOUR = 4;
		public static final int FIVE = 5;
		public static final int EIGHT = 8;
		public static final int TEN = 10;
		public static final int SIXTEEN = 16;
		public static final int SIXTY = 60;
		public static final int HUNDRED= 100;
		public static final int THIRTY = 30;


	}


	/** Generate the constants for Role */
	public static class Role{
		public static final String MEMBER  = "member";
		public static final String MASTER  = "master";
		public static final String ADMIN  = "admin";
		public static final String ADMIN_UPP  = "ADMIN";
		public static final String MEMBER_UPP  = "MEMBER";
		public static final String ROLE = "role";

	}

	/** Generate the constants for consumer status */
	public  static class Status{
		public static final String STATUS  = "status";
		public static final String WARN = "warn";
		public static final String OK = "ok";
		public static  final String BAD  = "bad";
		public static final String ACTIVE = "active";
		public static final String DEAD = "dead";

	}

	/** Generate the constants for sendType,like email or alerta */
	public static class SendType{
		public static final String ALL = "all";
		public static final String EMAIL = "email";
		public static final String ALERTA = "alerta";
	}

	/** Generate the constants needed for User */
	public static class User{
		public static final String PASSWORD = "password";
		public static final String CREATETIME = "createTime";
		public static final String NAME = "name";
	}

	/** Constants key*/
	public static class KeyStr{
		public static final String API = "api";
		public static final String HOME = "home";
		public static final String MANAGER = "manager";
		public static final String COUNT = "Count";
		public static final String SINGLE = "Single";
		public static final String CLUSTER ="cluster";
		public static final String CLUSTERID = "clusterID";
		public static final String LOWER_CLUSTER_ID = "clusterId";
		public static final String CLUSTER_NAME = "clusterName";
		public static final String CLUSTER_ID = "cluster_id";
		public static final String CLIENTID = "clientId";
		public static final String TOPICNAME = "topicName";
		public static final String TOPIC_NAME = "topic_name";

		public static final String TOPIC = "topic";
		public static final String MAX_FILE = "max_file";

		public static final String APPROVAL_COMMENTS = "approvalComments";

		public static final String START = "start";
		public static final String INTERVAL = "interval";
		public static final String END = "end";

		public static final String ENTRY = "entry";
		public static final String USER_NAME = "name";
		public static final String PASSWORD = "password";
		public static final String MONITOR_TOPIC = "monitor_topic";
		public static final String COLLECTION = "collection";
		public static final String USER_ID = "user_id";
		public static final String REMOTE = "remote";
		public static final String MONITOR = "monitor";
		public static final String WAIT_TIME = "waitTime";
		public static final String GROUP_ID = "groupID";
		public static final String RECORD_NUM = "recordNum";
		public static final String ISBY_PARTITION = "isByPartition";
		public static final String IS_COMMIT = "isCommit";
		public static final String ALL = "all";
		public static final String DATE = "date";
		public static final String TIMESTAMP = "timestamp";
		public static final String COMSUMBER_TYPE = "consumerType";
		public static final String COMSUMBER_API = "consumerAPI";
		public static final String MESSAGE = "message";
		public static final String CONTENT_TYPE = "Content-Type";
		public static final String APPLICATION_JSON = "application/json";
		public static final String KSQL_APPLICATION_JSON = "application/vnd.ksql.v1+json; charset=utf-8";
		public static final String REMOTELOCATIONS = "remotelocations";
		public static final String LOCATION = "location";
		public static final String DATAGRAME = "datagrame";
		public static final String METRIC = "metric";
		public static final String AVG_RATE = "avg_rate";
		public static final String DIFF_DATA = "diff_data";
		public static final String TIME = "time";
		public static final String DATA = "data";
		public static final String CONNECTION_URL = "connection_url";

		public static final String TOKEN = "Token";
		public static final String STATE = "state";
		public static final String DEAD = "Dead";
		public static final String NULL = "null";
		public static final String OWNER_EMAIL = "owner-email";
		public static final String OWNER_GROUPS = "owner-groups";
		public static final String MORE_INFO = "moreInfo";
		public static final String THRESHOLDINFO = "thresholdInfo";

		public static final String MAX_DATA = "max_data";
		public static final String MIN_DATA = "min_data";


	}

	/** constants kafka consumer type*/
	public static class ConsumerType{
		public static final String ZK = "zk";
		public static final String BROKER = "broker";
	}

	/**Generate the constants needed for Time Interval*/
	public static class Interval{
		public static final String FIVE_MINUTES = "5m";
		public static final String TEN_MINUTES = "10m";
		public static final String THREETY_MINUTES = "30m";
		public static final String ONE_HOURS = "1h";
		public static final String FOUR_HOURS = "4h";
		public static final String EIGHT_HOURS = "8h";
		public static final String FORTHY_HOURS = "16h";
		public static final String ONE_DAY = "1d";
	}

	/**Generate the constants needed for ElasticSearch*/
	public static class EleaticSearch{
		public static final String AGGS = "aggs";
		public static final String BUCKETS = "buckets";
		public static final String AGGREGATIONS = "aggregations";
		public static final String HITS = "hits";
		public static final String SOURCE_ = "_source";

		public static final String DOC_COUNT = "doc_count";

		public static final String KEY_AS_STRING = "key_as_string";

		public static final String KEY = "key";

		public static final String VALUE = "value";
	}

	/** Symbol constants*/
	public static class Symbol{
		public static final String COMMA = ",";
		public static final String SEMICOLON = ";";
		public static final String COLON = ":";
		public static final String SLASH = "/";
		public static final String DOUBLE_SLASH = "//";
		public static final String DOUBLE_THE_SLASH = "\\";
		public static final String VERTICAL_STR = "|";
		public static final String LEFT_PARENTHESES = "[";
		public static final String RIGHT_PARENTHESES = "]";
		public static final String LEFT = "{";
		public static final String RIGHT = "}";
		public static final String PERCENT = "%";
		public static final String STARSTR = "*";
		public static final String EMPTY_STR = "";
		public static final String SPACE_STR = " ";
	}

	/** number constants*/
	public static class Number{
		public static final String TWO = "2";
		public static final String ONE = "1";
		public static final String ONE_HUNANDER = "100";

	}

	public static final String SUCCESS = "success";

	public static final String TRUE = "true";

	public static class Verify{
		public static final String SESSIONID = "sessionId";
		public static final String EMAIL = "email";
		public static final String NAME = "name";
	}

}
