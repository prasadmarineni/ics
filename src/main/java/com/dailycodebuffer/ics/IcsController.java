package com.dailycodebuffer.ics;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class IcsController {

	private final ChatModel chatModel;
	private final VectorStore vectorStore;

	private String prompt = """
			Your task is to answer the questions about Indian Constitution. Use the information from the DOCUMENTS
			section to provide accurate answers. If unsure or if the answer isn't found in the DOCUMENTS section,
			simply state that you don't know the answer.

			QUESTION:
			{input}

			DOCUMENTS:
			{documents}

			""";

	public IcsController(ChatModel chatModel, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
	}

	@GetMapping("/chat")
	public Map<String, String> simplify(@RequestParam(value = "question", required = false) String question) {

		String answer = "Ask something";

		if (question != null && !question.isBlank()) {
			PromptTemplate template = new PromptTemplate(prompt);
			Map<String, Object> promptsParameters = new HashMap<>();
			promptsParameters.put("input", question);
			promptsParameters.put("documents", findSimilarData(question));

			answer = chatModel.call(template.create(promptsParameters)).getResult().getOutput().getContent();

		}

		return Map.of("answer", answer, "question", question);
	}

	private String findSimilarData(String question) {
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(5));

		return documents.stream().map(document -> document.getContent().toString()).collect(Collectors.joining());

	}
}
