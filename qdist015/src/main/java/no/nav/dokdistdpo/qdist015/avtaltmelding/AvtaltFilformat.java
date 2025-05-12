package no.nav.dokdistdpo.qdist015.avtaltmelding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AvtaltFilformat {

	JPEG("jpeg", "jpeg"),
	PNG("png", "png"),
	PDF("pdf", "pdf"),
	TIFF("tiff", "tiff"),
	XLSX("xlsx", "xlsx");

	private final String format;
	private final String filendelse;
}
