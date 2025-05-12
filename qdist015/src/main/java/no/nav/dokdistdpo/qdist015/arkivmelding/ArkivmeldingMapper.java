package no.nav.dokdistdpo.qdist015.arkivmelding;

import jakarta.xml.bind.JAXBElement;
import no.arkivverket.standarder.noark5.arkivmelding.Arkivmelding;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentbeskrivelse;
import no.arkivverket.standarder.noark5.arkivmelding.Dokumentobjekt;
import no.arkivverket.standarder.noark5.arkivmelding.EnhetsidentifikatorType;
import no.arkivverket.standarder.noark5.arkivmelding.Journalpost;
import no.arkivverket.standarder.noark5.arkivmelding.Korrespondansepart;
import no.arkivverket.standarder.noark5.arkivmelding.ObjectFactory;
import no.arkivverket.standarder.noark5.arkivmelding.Part;
import no.arkivverket.standarder.noark5.arkivmelding.Saksmappe;
import no.nav.dokdistdpo.consumer.ereg.EregConsumer;
import no.nav.dokdistdpo.consumer.pdl.HentPersonInfo;
import no.nav.dokdistdpo.consumer.pdl.PdlConsumer;
import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.CompactSafJournalpost;
import no.nav.dokdistdpo.consumer.saf.journalpost.SafJournalpostService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.NAV_ORGNUMMER;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.ARKIVFORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.AVSENDER;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENTASJON;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENTET_ER_FERDIGSTILT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.EKSPEDERT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.FERDIGSTILT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.FILFORMAT_JPEG;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.FILFORMAT_PNG;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.HOVEDDOKUMENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.INNGAAENDE;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.MOTTAKER;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.NAV;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.PRODUKSJONSFORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.REFERANSE_DOKUMENTFIL_FORMAT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.SAKSPART_ROLLE_AMP;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.SAKSPART_ROLLE_DAP;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UKJENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UNDER_BEHANDLING;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UTGAAENDE;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.UTGAAENDE_DOKUMENT;
import static no.nav.dokdistdpo.qdist015.Qdist015Constant.VEDLEGG;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.convertLocalDateTimeToXmlGregorianCalendar;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.getNow;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.isBrukerTypeAktoerId;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.isBrukerTypeFnr;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.isBrukerTypeOrgNr;
import static no.nav.dokdistdpo.qdist015.Qdist015Utils.isHoveddokument;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformatMapper.getVariantformatArkiv;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformatMapper.getVariantformatSladdet;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformatMapper.isDokumentContainsSladdetVariant;
import static no.nav.dokdistdpo.qdist015.avtaltmelding.AvtaltFilformatMapper.map;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class ArkivmeldingMapper {

	private final PdlConsumer pdlConsumer;
	private final EregConsumer eregConsumer;
	private final SafJournalpostService<CompactSafJournalpost> compactSafJournalpostService;

	public ArkivmeldingMapper(PdlConsumer pdlConsumer, EregConsumer eregConsumer,
							  @Qualifier("CompactSafJournalpostQueryService") SafJournalpostService<CompactSafJournalpost> compactSafJournalpostService) {
		this.pdlConsumer = pdlConsumer;
		this.eregConsumer = eregConsumer;
		this.compactSafJournalpostService = compactSafJournalpostService;
	}

	public JAXBElement<Arkivmelding> createAvtaltmelding(SafJournalpost safJournalpost, String bestillingsId) {
		ObjectFactory factory = new ObjectFactory();
		XMLGregorianCalendar datoAvtaltmeldingOpprettet = getNow();

		Arkivmelding arkivmelding = factory.createArkivmelding();

		arkivmelding.useMeldingId(bestillingsId).useTidspunkt(datoAvtaltmeldingOpprettet)
				.useAntallFiler((int) safJournalpost.dokumenter().stream()
						.filter(dok -> isFerdigstilt(dok.dokumentstatus())).count());
		arkivmelding.getMappe().add(createAndPopulateSaksmappe(safJournalpost, datoAvtaltmeldingOpprettet, factory));

		return factory.createArkivmelding(arkivmelding);
	}


	private Saksmappe createAndPopulateSaksmappe(SafJournalpost safJournalpost,
												 XMLGregorianCalendar datoAvtaltmeldingOpprettet,
												 ObjectFactory objectFactory) {
		Saksmappe saksmappe = objectFactory.createSaksmappe();

		no.arkivverket.standarder.noark5.arkivmelding.Journalpost journalpost = mapJournalpost(safJournalpost, datoAvtaltmeldingOpprettet, objectFactory);
		XMLGregorianCalendar opprettetDato = mapOpprettetDato(safJournalpost, journalpost);

		saksmappe.useTittel(safJournalpost.temanavn())
				.useOpprettetDato(opprettetDato)
				.useOpprettetAv(safJournalpost.opprettetAvNavn())
				.useVirksomhetsspesifikkeMetadata(safJournalpost.sak().arkivsaksnummer())
				.useSaksdato(opprettetDato)
				.useAdministrativEnhet(NAV)
				.useSaksansvarlig(safJournalpost.opprettetAvNavn())
				.useJournalenhet(safJournalpost.journalfoerendeEnhet())
				.useSaksstatus(UNDER_BEHANDLING);

		saksmappe.getPart().add(createAndPopulatePartAMP(safJournalpost, objectFactory));
		saksmappe.getPart().add(createAndPopulatePartDAP(safJournalpost, objectFactory));
		saksmappe.getRegistrering().add(journalpost);

		return saksmappe;
	}

	private XMLGregorianCalendar mapOpprettetDato(SafJournalpost safJournalpost, Journalpost journalpost) {
		LocalDateTime datoOpprettet = safJournalpost.sak().datoOpprettet();

		if (datoOpprettet == null) {
			return finnEldsteVedleggSortertEtterDokumentbeskrivelseOpprettetDato(journalpost);
		}
		return convertLocalDateTimeToXmlGregorianCalendar(datoOpprettet);
	}

	private Journalpost mapJournalpost(SafJournalpost safJournalpost,
									   XMLGregorianCalendar datoAvtaltmeldingOpprettet,
									   ObjectFactory objectFactory) {
		Journalpost journalpost = objectFactory.createJournalpost();

		journalpost.useOpprettetDato(convertLocalDateTimeToXmlGregorianCalendar(safJournalpost.datoOpprettet()))
				.useOpprettetAv(safJournalpost.opprettetAvNavn())
				.useTittel(safJournalpost.tittel())
				.useJournalposttype(UTGAAENDE_DOKUMENT)
				.useJournalstatus(EKSPEDERT)
				.useJournaldato(convertLocalDateTimeToXmlGregorianCalendar(safJournalpost.getJournalfoertDato()));

		journalpost.getKorrespondansepart().add(createAndPopulateKorrespondansepartAvsender(objectFactory));
		journalpost.getKorrespondansepart().add(createAndPopulateKorrespondansepartMottaker(safJournalpost, objectFactory));

		addDokumentBeskrivelserToJournalpost(journalpost, safJournalpost, datoAvtaltmeldingOpprettet, objectFactory);

		return journalpost;
	}

	private void addDokumentBeskrivelserToJournalpost(Journalpost journalpost,
													  SafJournalpost safJournalpost,
													  XMLGregorianCalendar datoAvtaltmeldingOpprettet,
													  ObjectFactory objectFactory) {
		List<Dokumentbeskrivelse> dokumentbeskrivelses = journalpost.getDokumentbeskrivelse();
		safJournalpost.dokumenter()
				.forEach(dokumentInfo -> {
					if (isFerdigstilt(dokumentInfo.dokumentstatus())) {
						dokumentbeskrivelses.add(createAndPopulateDokumentbeskrivelse(safJournalpost, dokumentInfo,
								dokumentbeskrivelses.size() + 1, datoAvtaltmeldingOpprettet, objectFactory));
					}
				});

	}

	private boolean isFerdigstilt(String dokumentStatus) {
		return isBlank(dokumentStatus) || FERDIGSTILT.equals(dokumentStatus);
	}

	private Dokumentbeskrivelse createAndPopulateDokumentbeskrivelse(SafJournalpost safJournalpost,
																	 SafJournalpost.DokumentInfo dokumentInfo,
																	 int rekkefolge,
																	 XMLGregorianCalendar datoAvtaltmeldingOpprettet,
																	 ObjectFactory objectFactory) {

		CompactSafJournalpost compactSafJournalpost = compactSafJournalpostService.hentJournalpost(dokumentInfo.originalJournalpostId());
		Dokumentbeskrivelse dokumentbeskrivelse = objectFactory.createDokumentbeskrivelse();
		dokumentbeskrivelse.useDokumenttype(DOKUMENTASJON)
				.useDokumentstatus(DOKUMENTET_ER_FERDIGSTILT)
				.useTittel(getDokumentbeskrivelseTittel(dokumentInfo, isHoveddokument(rekkefolge), compactSafJournalpost))
				.useOpprettetDato(getDokumentJournalfoertDato(isHoveddokument(rekkefolge), safJournalpost, dokumentInfo, compactSafJournalpost))
				.useOpprettetAv(getDokumentJournalfoertAvNavn(isHoveddokument(rekkefolge), safJournalpost, dokumentInfo, compactSafJournalpost))
				.useTilknyttetRegistreringSom(isHoveddokument(rekkefolge) ? HOVEDDOKUMENT : VEDLEGG)
				.useDokumentnummer(BigInteger.valueOf(rekkefolge))
				.useTilknyttetDato(datoAvtaltmeldingOpprettet)
				.useTilknyttetAv(safJournalpost.journalfortAvNavn());
		dokumentbeskrivelse.getDokumentobjekt().add(
				createAndPopulateDokumentObjekt(safJournalpost, dokumentInfo, isHoveddokument(rekkefolge), compactSafJournalpost, objectFactory));

		return dokumentbeskrivelse;
	}


	private String getDokumentbeskrivelseTittel(SafJournalpost.DokumentInfo dokumentInfo,
												boolean isHoveddokument,
												CompactSafJournalpost compactSafJournalpost) {
		if (!isHoveddokument && isNotBlank(dokumentInfo.originalJournalpostId())) {
			if (INNGAAENDE.equals(compactSafJournalpost.journalposttype())) {
				return format("%s, Fra %s", dokumentInfo.tittel(), compactSafJournalpost.avsenderMottakerNavn());
			} else if (UTGAAENDE.equals(compactSafJournalpost.journalposttype())) {
				return format("%s, Til %s", dokumentInfo.tittel(), compactSafJournalpost.avsenderMottakerNavn());
			} else {
				return dokumentInfo.tittel();
			}
		}
		return dokumentInfo.tittel();
	}

	private Dokumentobjekt createAndPopulateDokumentObjekt(SafJournalpost safJournalpost,
														   SafJournalpost.DokumentInfo dokumentInfo,
														   boolean isHoveddokument,
														   CompactSafJournalpost compactSafJournalpost,
														   ObjectFactory objectFactory) {
		Dokumentobjekt dokumentobjekt = objectFactory.createDokumentobjekt();
		return dokumentobjekt.useVersjonsnummer(ONE)
				.useVariantformat(getVariantformat(dokumentInfo))
				.useFormat(map(dokumentInfo).getFormat())
				.useOpprettetDato(getDokumentJournalfoertDato(isHoveddokument, safJournalpost, dokumentInfo, compactSafJournalpost))
				.useOpprettetAv(getDokumentJournalfoertAvNavn(isHoveddokument, safJournalpost, dokumentInfo, compactSafJournalpost))
				.useReferanseDokumentfil(getReferanseDokumentfil(safJournalpost.journalpostId(), dokumentInfo));
	}

	private XMLGregorianCalendar getDokumentJournalfoertDato(boolean isHoveddokument,
															 SafJournalpost safJournalpost,
															 SafJournalpost.DokumentInfo dokumentInfo,
															 CompactSafJournalpost compactSafJournalpost) {
		if (!isHoveddokument && isNotBlank(dokumentInfo.originalJournalpostId())) {
			if (!isJournalDatoNull(compactSafJournalpost)) {
				return convertLocalDateTimeToXmlGregorianCalendar(compactSafJournalpost.datoJournalfoert());
			}
		}
		return convertLocalDateTimeToXmlGregorianCalendar(safJournalpost.getJournalfoertDato());
	}

	private String getDokumentJournalfoertAvNavn(boolean isHoveddokument,
												 SafJournalpost safJournalpost,
												 SafJournalpost.DokumentInfo dokumentInfo,
												 CompactSafJournalpost compactSafJournalpost) {
		if (!isHoveddokument && isNotBlank(dokumentInfo.originalJournalpostId())) {
			if (!isJournalfoertAvNavnNull(compactSafJournalpost)) {
				return compactSafJournalpost.journalfortAvNavn();
			}
			return UKJENT;
		}
		return safJournalpost.journalfortAvNavn();
	}

	private boolean isJournalDatoNull(CompactSafJournalpost compactSafJournalpost) {
		return compactSafJournalpost == null || compactSafJournalpost.datoJournalfoert() == null;
	}

	private boolean isJournalfoertAvNavnNull(CompactSafJournalpost compactSafJournalpost) {
		return compactSafJournalpost == null || compactSafJournalpost.journalfortAvNavn() == null;
	}

	private String getReferanseDokumentfil(String journalpostId, SafJournalpost.DokumentInfo dokumentInfo) {
		return format(REFERANSE_DOKUMENTFIL_FORMAT, journalpostId, dokumentInfo.dokumentInfoId(), getVariantformat(dokumentInfo), getFiltype(dokumentInfo));
	}

	private String getFiltype(SafJournalpost.DokumentInfo dokumentInfo) {
		if (isDokumentContainsSladdetVariant(dokumentInfo)) {
			return getVariantformatSladdet(dokumentInfo);
		}

		return getVariantformatArkiv(dokumentInfo);
	}

	private String getVariantformat(SafJournalpost.DokumentInfo dokumentInfo) {
		if (isDokumentContainsSladdetVariant(dokumentInfo)) {
			return DOKUMENT_HVOR_DELER_AV_INNHOLDET_ER_SKJERMET;
		}
		String filtype = getVariantformatArkiv(dokumentInfo);

		return isFiltypePNGorJPEG(filtype) ? ARKIVFORMAT : PRODUKSJONSFORMAT;
	}

	private boolean isFiltypePNGorJPEG(String filtype) {
		return FILFORMAT_JPEG.equals(filtype) || FILFORMAT_PNG.equals(filtype);
	}

	private Korrespondansepart createAndPopulateKorrespondansepartAvsender(ObjectFactory objectFactory) {
		Korrespondansepart avsender = objectFactory.createKorrespondansepart();

		return avsender.useKorrespondanseparttype(AVSENDER)
				.useOrganisasjonsnummer(new EnhetsidentifikatorType()
						.useOrganisasjonsnummer(NAV_ORGNUMMER))
				.useKorrespondansepartNavn(NAV);
	}

	private Korrespondansepart createAndPopulateKorrespondansepartMottaker(SafJournalpost safJournalpost, ObjectFactory objectFactory) {
		Korrespondansepart mottaker = objectFactory.createKorrespondansepart();
		return mottaker.useKorrespondanseparttype(MOTTAKER)
				.useOrganisasjonsnummer(new EnhetsidentifikatorType()
						.useOrganisasjonsnummer(hentOrgNummerDAP(safJournalpost)))
				.useKorrespondanseparttype(getSakspartNavnDAP(safJournalpost));

	}

	private Part createAndPopulatePartDAP(SafJournalpost safJournalpost, ObjectFactory objectFactory) {
		Part partDAP = objectFactory.createPart();
		partDAP.usePartNavn(getSakspartNavnDAP(safJournalpost))
				.usePartRolle(SAKSPART_ROLLE_DAP);

		if (isBrukerTypeOrgNr(safJournalpost.bruker())) {
			partDAP.setPartID(hentOrgNummerDAP(safJournalpost));
		} else {
			partDAP.setPartID(getFoedselsnummer(safJournalpost));
		}
		return partDAP;
	}

	private Part createAndPopulatePartAMP(SafJournalpost safJournalpost, ObjectFactory objectFactory) {
		Part partAMP = objectFactory.createPart();
		return partAMP.usePartNavn(NAV)
				.usePartRolle(SAKSPART_ROLLE_AMP)
				.usePartID(NAV_ORGNUMMER)
				.useKontaktperson(safJournalpost.opprettetAvNavn());

	}

	private String getFoedselsnummer(SafJournalpost journalpost) {
		if (isBrukerTypeAktoerId(journalpost.bruker())) {
			return hentPersonInfo(journalpost.bruker().id()).ident();
		} else if (isBrukerTypeFnr(journalpost.bruker())) {
			return journalpost.bruker().id();
		} else {
			return null;
		}
	}

	private String hentOrgNummerDAP(SafJournalpost safJournalpost) {
		return isBrukerTypeOrgNr(safJournalpost.bruker()) ? safJournalpost.bruker().id() : null;
	}

	private String getSakspartNavnDAP(SafJournalpost safJournalpost) {
		String brukerId = safJournalpost.bruker().id();

		if (isBrukerTypeOrgNr(safJournalpost.bruker())) {
			return eregConsumer.hentOrganisasjonsnavn(brukerId);
		}
		return hentPersonInfo(brukerId).fullnavn();
	}

	private HentPersonInfo hentPersonInfo(String brukerId) {
		return pdlConsumer.hentPersonInfo(brukerId);
	}

	private XMLGregorianCalendar finnEldsteVedleggSortertEtterDokumentbeskrivelseOpprettetDato(Journalpost journalpost) {
		XMLGregorianCalendar eldstedato = null;

		for (Dokumentbeskrivelse dokumentbeskrivelse : journalpost.getDokumentbeskrivelse()) {
			if (VEDLEGG.equals(dokumentbeskrivelse.getTilknyttetRegistreringSom())) {
				XMLGregorianCalendar opprettetDato = dokumentbeskrivelse.getOpprettetDato();
				if (opprettetDato != null || opprettetDato.compare(eldstedato) == DatatypeConstants.LESSER) {
					eldstedato = opprettetDato;
				}
			}
		}
		return eldstedato;
	}
}
