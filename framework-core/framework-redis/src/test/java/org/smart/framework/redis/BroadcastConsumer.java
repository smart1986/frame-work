//package org.smart.framework.redis;
//
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class BroadcastConsumer {
//
//	private static Logger logger = LoggerFactory.getLogger(BroadcastConsumer.class);
//    public static void main(String[] args) throws Exception {
//
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("example_group_name");
//        consumer.setNamesrvAddr("10.168.3.202:9876");
//        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
//
//        //set to broadcast mode
//        consumer.setMessageModel(MessageModel.BROADCASTING);
//
//        consumer.setConsumeConcurrentlyMaxSpan(1);
//
//        consumer.subscribe("TopicTest", "TagA || TagC || TagD");
//
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
//                ConsumeConcurrentlyContext context) {
////                System.out.printf(Thread.currentThread().getName() + " Receive New Messages: " + msgs + "%n");
//            	System.out.println(msgs.size());
//                for (MessageExt me : msgs) {
//					System.out.println(new String(me.getBody()));
//				}
//
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//
//        consumer.start();
//        logger.error("Broadcast Consumer Started.");
//
//        TimeUnit.SECONDS.sleep(10);
//        consumer.shutdown();
//    }
//}