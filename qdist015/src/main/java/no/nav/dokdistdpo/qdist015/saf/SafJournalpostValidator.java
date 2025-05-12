package no.nav.dokdistdpo.qdist015.saf;

import no.nav.dokdistdpo.consumer.saf.SafJournalpost;
import no.nav.dokdistdpo.exception.functional.SafJournalpostValidationException;
import no.nav.dokdistdpo.utils.SafJournalpostUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_ARKIV;
import static no.nav.dokdistdpo.constant.DokdistdpoConstant.VARIANTFORMAT_SLADDET;
import static no.nav.dokdistdpo.consumer.saf.SafJournalpost.Datotype.DATO_JOURNALFOERT;
import static no.nav.dokdistdpo.utils.SafJournalpostUtils.assertCollectionNotNull;
import static no.nav.dokdistdpo.utils.SafJournalpostUtils.assertNotEmpty;
import static no.nav.dokdistdpo.utils.SafJournalpostUtils.assertNotNull;

@Component
public class SafJournalpostValidator {

	public void asssertJournalpost(SafJournalpost safJournalpost, String journalpostId) {
		SafJournalpostUtils.assertNotEmpty("journalpost.journalpostId", safJournalpost.journalpostId(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.opprettetAvNavn", safJournalpost.opprettetAvNavn(), journalpostId);
		assertNotNull("journalpost.datoOpprettet", safJournalpost.datoOpprettet(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.tittel", safJournalpost.tittel(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.journalfoerendeEnhet", safJournalpost.journalfoerendeEnhet(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.temanavn", safJournalpost.temanavn(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.tema", safJournalpost.tema(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.journalposttype", safJournalpost.journalposttype(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.journalfortAvNavn", safJournalpost.journalfortAvNavn(), journalpostId);
		assertRelevanteDatoContainsDatoJournalfoert(safJournalpost, journalpostId);
		assertBruker(safJournalpost, journalpostId);
		assertSak(safJournalpost, journalpostId);
		assertDokumenter(safJournalpost.dokumenter(), journalpostId);
	}

	private void assertBruker(SafJournalpost safJournalpost, String journalpostId) {
		assertNotNull("journalpost.bruker", safJournalpost.bruker(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.bruker.id", safJournalpost.bruker().id(), journalpostId);
		SafJournalpostUtils.assertNotEmpty("journalpost.bruker.type", safJournalpost.bruker().type(), journalpostId);
	}

	private void assertSak(SafJournalpost safJournalpost, String journalpostId) {
		assertNotNull("journalpost.sak", safJournalpost.sak(), journalpostId);
		assertNotNull("journer.sak.arkivsaksnummer", safJournalpost.sak().arkivsaksnummer(), journalpostId);
	}

	private void assertRelevanteDatoContainsDatoJournalfoert(SafJournalpost safJournalpost, String journalpostId) {
		assertNotNull("journalpost.relevanteDatoer", safJournalpost.relevanteDatoer(), journalpostId);
		safJournalpost.relevanteDatoer().stream()
				.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.datotype()))
				.findAny()
				.orElseThrow(() -> new SafJournalpostValidationException(
						format("Feltet journalpost.relevanteDatoer må inneholde DATO_JOURNALFOERT. journalpostId=%s", journalpostId)
				));
	}

	private void assertDokumenter(List<SafJournalpost.DokumentInfo> dokumenter, String journalpostId) {

		assertNotNull("Journalpost.Dokumenter", dokumenter, journalpostId);

		dokumenter.forEach(dokument -> {
			SafJournalpostUtils.assertNotEmpty("dokumentInfo.dokumentInfoId", dokument.dokumentInfoId(), journalpostId);
			SafJournalpostUtils.assertNotEmpty("dokumentInfo.tittel", dokument.tittel(), journalpostId);
			assertCollectionNotNull("dokumentInfo.dokumentvarianter", dokument.dokumentvarianter(), journalpostId);

			if (!isDokumentvariantSladdetOrArkiv(dokument)) {
				throw new SafJournalpostValidationException(format("DokumentInfo med dokumentInfoId=%s har " +
						"ikke tilknyttede dokumentvarianter ARKIV eller SLADDET. JournalpostId=%s", dokument.dokumentInfoId(), journalpostId));
			}

			assertDokumentvariantFiltype(dokument);
		});
	}

	private boolean isDokumentvariantSladdetOrArkiv(SafJournalpost.DokumentInfo dokumentInfo) {
		return dokumentInfo.dokumentvarianter().stream()
				.anyMatch(dokumentvariant ->
						VARIANTFORMAT_SLADDET.equals(dokumentvariant.variantformat()) ||
								VARIANTFORMAT_ARKIV.equals(dokumentvariant.variantformat()));
	}

	private void assertDokumentvariantFiltype(SafJournalpost.DokumentInfo dokumentInfo) {
		dokumentInfo.dokumentvarianter().forEach(
				dokumentvariant ->
						assertNotEmpty("dokumentInfo.dokumentvarianter.filtype", dokumentvariant.filtype(), dokumentInfo.dokumentInfoId(), dokumentInfo.originalJournalpostId())
		);
	}
}
