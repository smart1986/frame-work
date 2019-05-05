//package org.smart.framework.net.ext;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import GameInit;
//import InitIndex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public abstract class MessageMapper<K,V> implements GameInit {
//	protected Logger logger = LoggerFactory.getLogger(this.getClass());
//    private static  Map<Object,Object> messageMap = new HashMap<>();
//
//    public void registMessage(K key, V value){
//        if (messageMap.containsKey(key)){
//            throw new RuntimeException("duplicate message key:" + key);
//        }
//        messageMap.put(key, value);
//        logger.debug("message mapped,key:{},value:{}",key,value.getClass().getName());
//    }
//
//    @SuppressWarnings("unchecked")
//	public static <K,V> V findMessage(K key){
//        return (V) messageMap.get(key);
//    }
//
//    public abstract void init();
//    
//    @Override
//    public void gameInit() {
//        init();
//    }
//
//    @Override
//    public InitIndex index() {
//        return InitIndex.INTI_FIRST;
//    }
//
//}
