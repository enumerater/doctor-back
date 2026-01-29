package com.enumerate.disease_detection.ModelInterfaces;

import dev.langchain4j.service.*;

public interface MainAgent {


        @dev.langchain4j.agentic.Agent
        String brain(@MemoryId String memoryId, @V("request") String request);

}