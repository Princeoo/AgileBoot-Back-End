package com.agileboot.api.customize.service;

/**
 * 微信 code2Session 网关，便于在测试中替换为 stub。
 */
public interface WechatCode2SessionClient {

    WechatSession exchange(String appId, String appSecret, String code);

    /**
     * 微信 code2Session 返回结果的内存模型，sessionKey 不得写入日志或数据库。
     */
    class WechatSession {

        private final String openId;

        private final String unionId;

        private final String sessionKey;

        public WechatSession(String openId, String unionId, String sessionKey) {
            this.openId = openId;
            this.unionId = unionId;
            this.sessionKey = sessionKey;
        }

        public String getOpenId() {
            return openId;
        }

        public String getUnionId() {
            return unionId;
        }

        public String getSessionKey() {
            return sessionKey;
        }
    }
}
