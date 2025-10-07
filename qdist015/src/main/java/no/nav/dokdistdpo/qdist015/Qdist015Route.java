package no.nav.dokdistdpo.qdist015;

import jakarta.jms.Queue;
import no.nav.dokdistdpo.config.properties.DokdistdpoProperties;
import no.nav.dokdistdpo.exception.functional.DokdistdpoFunctionalException;
import no.nav.dokdistdpo.qdist015.utils.MdcHeaderProcessor;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import static jakarta.xml.bind.JAXBContext.newInstance;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_FORSENDELSE_ID;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.PROPERTY_KONVERSASJON_ID;
import static no.nav.dokdistdpo.qdist015.dokdistforsendelse.SendTilPrintService.SEND_TIL_PRINT;
import static org.apache.camel.ExchangePattern.InOnly;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class Qdist015Route extends RouteBuilder {

	public static final String QDIST015_ROUTE_ID = "qdist015";
	public static final String QDIST009_ROUTE_ID = "qdist009";

	private final Queue qdist015;
	private final Queue qdist015FunksjonellFeil;
	private final Queue qdist009;
	private final Qdist015Service qdist015Service;
	private final DokdistdpoProperties dokdistdpoProperties;

	public Qdist015Route(Queue qdist015, Queue qdist015FunksjonellFeil,
						 Queue qdist009,
						 Qdist015Service qdist015Service,
						 DokdistdpoProperties dokdistdpoProperties) {
		this.qdist015 = qdist015;
		this.qdist015FunksjonellFeil = qdist015FunksjonellFeil;
		this.qdist009 = qdist009;
		this.qdist015Service = qdist015Service;
		this.dokdistdpoProperties = dokdistdpoProperties;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off

		log.info("Registrert QDIST015 route på kø: {}", qdist015.getQueueName());

		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(DokdistdpoFunctionalException.class, ValidationException.class, IllegalArgumentException.class, IllegalStateException.class)
				.handled(true)
				.useOriginalMessage()
				.logExhaustedMessageBody(false)
				.log(WARN, log, "${exception};" + getIdsForLogging())
				.to("jms:" + qdist015FunksjonellFeil.getQueueName());

		from("jms:" + qdist015.getQueueName() + "?transacted=true")
				.autoStartup(dokdistdpoProperties.qdist015().autostartup())
				.routeId(QDIST015_ROUTE_ID)
				.setExchangePattern(InOnly)
				.process(new MdcHeaderProcessor())
				.log(INFO, log, "Qdist015 har mottatt forsendesle med " + logForsendelseId())
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(newInstance(DistribuerTilKanal.class)))
				.bean(qdist015Service)
				.process(exchange -> {
					String forsendelseId = exchange.getProperty(PROPERTY_FORSENDELSE_ID, String.class);
					exchange.getIn().setBody(forsendelseId);
				})
				.log("Qdist015: Forsendelsen ble oppdatert med forsendelseStatus OVERSENDT og behandlingen av" + getIdsForLogging() + " er avsluttet.")
				.end();

		from(SEND_TIL_PRINT)
				.routeId(QDIST009_ROUTE_ID)
				.process(new MdcHeaderProcessor())
				.marshal(new JaxbDataFormat(newInstance(DistribuerTilKanal.class)))
				.convertBodyTo(String.class)
				.to("jms:" + qdist009.getQueueName())
				.log(INFO, log,"Qdist015 har lagt forsendelse med " + getIdsForLogging() + " i køen til qdist009 for distribusjon til print")
				.end();
		//@formatter:on
	}

	public static String getIdsForLogging() {
		return "konversasjonId=${exchangeProperty." + PROPERTY_KONVERSASJON_ID + "}, " +
				"forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}

	public static String logForsendelseId() {
		return "forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}
}
