package org.nesc.ec.bigdata.constant;

/**
 * @author lg99
 */
public class TopicConfig {
    public static final String RETENTION_MS = "retention.ms";
    public static final String DELETE_RETENTION_MS = "delete.retention.ms";
    public static final String SKEWED = "Skewed";
    public static final String LEADERSKEWED = "LeaderSkewed";
    public static final String LEADER_COUNT = "leaderCount";
    public static final String EARLIEST = "earliest";

    public static final String LOGSIZE = "logSize";
    public static final String TTL="ttl";
    public static final String PARTITION = "partition";
    public static final String PARTITIONS = "partitions";
    public static final String OLDPARTITIONS = "oldPartition";
    public static final String REPLICATION = "replication";
    public static final String UNDER_REPLICATION = "under_replication";

    public static final String OFFSET = "offset";
    public static final String LAG ="lag";
    public static final String NUMBER_OF_PARTITIONS = "Number of Partitions";
    public static final String SUM_OF_PARTITION_OFFSETS = "Sum of partition offsets";
    public static final String TOTAL_NUMBER_OF_BROKERS = "Total number of Brokers";
    public static final String NUMBER_OF_BROKERS_FOR_TOPIC = "Number of Brokers for Topic";
    public static final String PREFERRED_REPLICAS = "Preferred Replicas %";
    public static final String BROKERS_SKEWED  = "Brokers Skewed %";
    public static final String BROKERS_LEADER_SKEWED = "Brokers Leader Skewed %";
    public static final String BROKERS_SPREAD = "Brokers Spread %";
    public static final String CONSUMMERGROUP ="consummerGroup";

    public static final String LATEST_OFFSET = "Latest_Offset";
    public static final String LEADER = "leader";
    public static final String REPLICAS = "replicas";
    public static final String IN_SYNC_REPLICAS = "InSyncReplicas";
    public static final String PREFERRED_LEADER = "Preferred_Leader";
    public static final String UNDEREPLICATED = "Under_Replicated";
}
