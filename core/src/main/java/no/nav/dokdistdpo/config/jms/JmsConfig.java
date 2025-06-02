package no.nav.dokdistdpo.config.jms;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.net.ssl.SSLSocketFactory;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Profile("nais")
@Configuration
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";

	@Bean
	public Queue qdist009(@Value("${dokdistsentralprint_qdist009_dist_s_print.queuename}") String qdist009Queue) throws JMSException {
		return new MQQueue(qdist009Queue);
	}

	@Bean
	public Queue qdist015(@Value("${dokdistdpo_qdist015_dist_til_dpo.queuename}") String qdist015Queue) throws JMSException {
		return new MQQueue(qdist015Queue);
	}

	@Bean
	public Queue qdist015FunksjonellFeil(@Value("${dokdistdpo_qdist015_funk_feil.queuename}") String qdist015FunkFeilQueue) throws JMSException {
		return new MQQueue(qdist015FunkFeilQueue);
	}

	@Bean
	public ConnectionFactory connectionFactory(DokdistdpoProperties dokdistdpoProperties) throws JMSException {
		return createConnectionFactory(dokdistdpoProperties);
	}

	private JmsPoolConnectionFactory createConnectionFactory(DokdistdpoProperties dokdistdpoProperties) throws JMSException {
		MQConnectionFactory mqConnectionFactory = getMqConnectionFactory(dokdistdpoProperties);

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(mqConnectionFactory);
		adapter.createConnection(dokdistdpoProperties.serviceuser().username(), dokdistdpoProperties.serviceuser().password());

		JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
		poolConnectionFactory.setConnectionFactory(mqConnectionFactory);
		poolConnectionFactory.setMaxConnections(10);
		poolConnectionFactory.setMaxSessionsPerConnection(10);

		return poolConnectionFactory;
	}

	private MQConnectionFactory getMqConnectionFactory(DokdistdpoProperties dokdistdpoProperties) throws JMSException {
		MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
		mqConnectionFactory.setHostName(dokdistdpoProperties.mqGateway().hostname());
		mqConnectionFactory.setChannel(dokdistdpoProperties.mqGateway().channelName());
		mqConnectionFactory.setPort(dokdistdpoProperties.mqGateway().port());
		mqConnectionFactory.setQueueManager(dokdistdpoProperties.mqGateway().managerName());
		mqConnectionFactory.setTransportType(WMQ_CM_CLIENT);
		mqConnectionFactory.setCCSID(UTF_8_WITH_PUA);
		mqConnectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
		mqConnectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		mqConnectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);

		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		mqConnectionFactory.setSSLSocketFactory(sslSocketFactory);
		return mqConnectionFactory;
	}
}
