package gr.cyberstream.workflow.engine.cmis;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.springframework.core.io.InputStreamResource;

public class MultipartFileResource extends InputStreamResource {

	private String filename;
	private Long length;

	public MultipartFileResource(Document document) {
		super(document.getContentStream().getStream());
		String extension = MimeTypes.getExtension(document.getContentStreamMimeType());
		this.filename = document.getContentStream().getFileName() + extension;
		this.length = document.getContentStream().getLength();
	}

	@Override
	public String getFilename() {
		return this.filename;
	}

	@Override
	public long contentLength() {
		return this.length;
	}

}
