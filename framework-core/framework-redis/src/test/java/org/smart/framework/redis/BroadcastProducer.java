//package org.jerry.framework.redis;
//
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.common.message.Message;
//import org.apache.rocketmq.remoting.common.RemotingHelper;
//
//public class BroadcastProducer {
//    public static void main(String[] args) throws Exception {
//        DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName");
//        producer.setNamesrvAddr("10.168.3.202:9876");
//        producer.start();
//
//        for (int i = 0; i < 2; i++){
//            Message msg = new Message("TopicTest",
//                "TagC",
//                "OrderID188",
//                "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
//            SendResult sendResult = producer.send(msg);
//            System.out.printf("%s%n", sendResult);
//        }
//        producer.shutdown();
//    }
//}
