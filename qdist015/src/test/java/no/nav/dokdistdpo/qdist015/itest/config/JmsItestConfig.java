package no.nav.dokdistdpo.qdist015.itest.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("itest")
@Configuration
public class JmsItestConfig {

	@Bean
	public Queue qdist015(@Value("${dokdistdpo_qdist015_dist_til_dpo.queuename}") String qdist015Queue) {
		return new ActiveMQQueue(qdist015Queue);
	}

	@Bean
	public Queue qdist015FunksjonellFeil(@Value("${dokdistdpo_qdist015_funk_feil.queuename}") String qdist015FunkFeilQueue) {
		return new ActiveMQQueue(qdist015FunkFeilQueue);
	}

	@Bean
	public Queue qdist009(@Value("${dokdistsentralprint_qdist009_dist_s_print.queuename}") String qdist009Queue) {
		return new ActiveMQQueue(qdist009Queue);
	}

	@Bean
	public Queue backoutQueue(@Value("${dokdistdpo_qdist015_backout.queuename}") String qdist014Bq) {
		return new ActiveMQQueue(qdist014Bq);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ embeddedActiveMQ() {
		EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
		embeddedActiveMQ.setConfigResourcePath("artemis-server.xml");
		return embeddedActiveMQ;
	}

	/**
	 * Opprett ConnectionFactory for test
	 * @param embeddedActiveMQ depender på embeddedActiceMQ så serveren er klar før vi oppretter connectionFactory
	 */
	@Bean
	public ConnectionFactory activeMQConnectionFactory(EmbeddedActiveMQ embeddedActiveMQ) {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0");
		JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
		poolConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
		poolConnectionFactory.setMaxConnections(1);
		return poolConnectionFactory;
	}
}
