package no.nav.dokdistdpo.consumer.dpo.dokumentpakke.sbdh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.ISO6523_AUTHORITY;
import static no.nav.dokdistdpo.consumer.dpo.Organisasjonsnummer.asIso6523;

@Data
@Builder
public class StandardBusinessDocumentHeader {
	private String headerVersion;
	private Set<Partner> sender;
	private Set<Partner> receiver;
	private DocumentIdentification documentIdentification;
	private BusinessScope businessScope;

	public void addSender(Partner partner) {
		if (sender == null) {
			sender = new HashSet<>();
		}
		sender.add(partner);
	}

	public void addReceiver(Partner partner) {
		if (receiver == null) {
			receiver = new HashSet<>();
		}
		receiver.add(partner);
	}

	public Partner createPartner(String orgnummer) {
		PartnerIdentification identification = PartnerIdentification.builder()
				.authority(ISO6523_AUTHORITY)
				.value(asIso6523(orgnummer))
				.build();
		return Partner.builder()
				.identifier(identification)
				.build();
	}

	@JsonIgnore
	public Set<Scope> getScopes() {
		return Optional.ofNullable(this.getBusinessScope()).flatMap(p -> Optional.ofNullable(p.getScope())).orElseGet(Collections::emptySet);
	}

	@JsonIgnore
	public Optional<Scope> getScope(ScopeType scopeType) {
		return this.getScopes().stream().filter(scope -> scopeType.toString().equals(scope.getType()) || scopeType.name().equals(scope.getType())).findAny();
	}

	@JsonIgnore
	public String getDocumentType() {
		return this.getDocumentIdentification().getType();
	}
}
