package com.valorio.auth.ai.service;

import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.CodeReviewRequest;
import com.valorio.auth.ai.model.CodeReviewResponse;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;

public interface AiProviderClient {

    CodeGenerationResponse generateCode(CodeGenerationRequest request);

    TestGenerationResponse generateTests(TestGenerationRequest request);

    CodeReviewResponse reviewCode(CodeReviewRequest request);
}
