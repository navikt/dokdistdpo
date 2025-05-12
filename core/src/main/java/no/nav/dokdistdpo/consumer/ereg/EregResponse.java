package no.nav.dokdistdpo.consumer.ereg;

public record EregResponse(Navn navn) {

	public record Navn(String sammensattnavn) {}
}
