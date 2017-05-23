package gr.cyberstream.workflow.engine.customservicetasks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import gr.cyberstream.workflow.engine.service.InvalidRequestException;

public class FileAppend implements JavaDelegate {

	private Expression filepath;
	private Expression value;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		String fpath = (String) filepath.getValue(execution);
		String params = (String) value.getValue(execution);

		if (params == null || params.isEmpty()) {
			System.out.println("No values have been specified");
			throw new InvalidRequestException("No values have been specified");
		}

		List<String> data = new ArrayList<String>();
		data.add(params);

		try {
			Files.write(Paths.get(fpath), data, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
			
		} catch (IOException ioe) {
			System.out.println("Error creating the file");
			throw new InvalidRequestException("Error creating the file: " + ioe.getMessage());
			
		} catch (UnsupportedOperationException uoe) {
			System.out.println("Unsupported operation");
			throw new InvalidRequestException("Unsupported operation: " + uoe.getMessage());
			
		} catch (SecurityException se) {
			System.out.println("Security exception");
			throw new InvalidRequestException("Security exception: " + se.getMessage());
		}

	}

}
