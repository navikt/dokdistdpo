package no.nav.dokdistdpo.sdist008;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

public record ForsendelseStatusEndringer(
		Set<Long> bekreftet,
		Set<Long> ekspedert,
		Set<Long> feilet) {

	public ForsendelseStatusEndringer() {
		this(new HashSet<>(), new HashSet<>(), new HashSet<>());
	}

	@Override
	public String toString() {
		return format("antall_bekreftet=%d, bekreftet=%s. antall_ekspedert=%d, ekspedert=%s. antall_feilet=%d, feilet=%s.",
				bekreftet.size(), bekreftet(), ekspedert().size(), ekspedert(), feilet().size(), feilet());
	}
}
