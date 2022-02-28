package org.nesc.ec.bigdata.monitor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nesc.ec.bigdata.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.nesc.ec.bigdata.Application;

/**
 * @author Reason.H.Duan
 * @version 1.0
 * @date 3/29/2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={Application.class})
public class ElasticsearchServiceTest {

    @Autowired
    ElasticsearchService elasticsearchService;

    /**
     * Test query.
     */
    @Test
    public void testQueryOffset() {
        elasticsearchService.queryOffset("1","ReasonTest","reason-group01","zk","","");
    }


    @Test
    public void testQuery(){
        String topic = "ReasonTest";
        String group = "reason-group01";
        String query = "{" +
                "    \"size\": 0," +
                "    \"aggs\": {" +
                "        \"aggs\": {" +
                "            \"date_histogram\": {" +
                "                \"field\": \"timestamp\"," +
                "                \"interval\": \"30m\"" +
                "            }," +
                "            \"aggs\": {" +
                "                \"group\": {" +
                "                    \"terms\": {" +
                "                        \"field\": \"group\"," +
                "                        \"order\": {" +
                "                            \"_key\": \"desc\"" +
                "                        }" +
                "                    }," +
                "                    \"aggs\": {" +
                "                        \"topic\": {" +
                "                            \"terms\": {" +
                "                                \"field\": \"topic\"," +
                "                                \"order\": {" +
                "                                    \"_key\": \"desc\"" +
                "                                }" +
                "                            }," +
                "                            \"aggs\": {" +
                "                                \"data\": {" +
                "                                    \"terms\": {" +
                "                                        \"field\": \"partition\"," +
                "                                        \"order\": {" +
                "                                            \"_key\": \"desc\"" +
                "                                        }" +
                "                                    }," +
                "                                    \"aggs\": {" +
                "                                        \"offset\": {" +
                "                                            \"max\": {" +
                "                                                \"field\": \"offset\"" +
                "                                            }" +
                "                                        }," +
                "                                        \"lag\": {" +
                "                                            \"max\": {" +
                "                                                \"field\": \"lag\"" +
                "                                            }" +
                "                                        }" +
                "                                    }" +
                "                                }," +
                "                                \"offset\": {" +
                "                                    \"sum_bucket\": {" +
                "                                        \"buckets_path\": \"data>offset\"" +
                "                                    }" +
                "                                }," +
                "                                \"lag\": {" +
                "                                    \"sum_bucket\": {" +
                "                                        \"buckets_path\": \"data>lag\"" +
                "                                    }" +
                "                                }" +
                "                            }" +
                "                        }" +
                "                    }" +
                "                }" +
                "            }" +
                "        }" +
                "    }," +
                "    \"query\": {" +
                "        \"bool\": {" +
                "            \"must\": [";


        if (group != null && group.length() > 0) {
            query += "                {" +
                    "                    \"match\": {" +
                    "                        \"group\": {" +
                    "                            \"query\": \"" + group + "\"" +
                    //   "                            \"type\": \"phrase\"" +
                    "                        }" +
                    "                    }" +
                    "                },";
        }
        if (topic != null && topic.length() > 0) {
            query += "                {" +
                    "                    \"match\": {" +
                    "                        \"topic\": {" +
                    "                            \"query\": \"" + topic + "\"" +
                    //   "                            \"type\": \"phrase\"" +
                    "                        }" +
                    "                    }" +
                    "                }";
        }

        query +=

                "            ]," +
                        "            \"filter\": [" +
                        "                {" +
                        "                    \"range\": {" +
                        "                        \"timestamp\": {" +
                        "                            \"gte\": 1555637400000," +
                        "                            \"lte\": 1555738200000," +
                        "                            \"format\": \"epoch_millis\"" +
                        "                        }" +
                        "                    }" +
                        "                }," +
                        "                {" +
                        "                    \"query_string\": {" +
                        "                        \"analyze_wildcard\": true," +
                        "                        \"query\": \"*\"" +
                        "                    }" +
                        "                }" +
                        "            ]" +
                        "        }" +
                        "    }" +
                        "}";
        System.out.println(query);

    }
}
