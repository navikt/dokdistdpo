package no.nav.dokdistdpo.consumer.dpo;

public interface Eformidling {

	void send(NavDokumentpakke navDokumentpakke, String arkivmelding);

	void bekreft(String filreferanse);
}
