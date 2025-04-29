import traceback
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from embedding_service import embed_documents
from rag_service import get_qa_chain
from logging_utils import log_relevant_docs

# FastAPI 앱 생성
app = FastAPI()

# RAG QA 체인 가져오기
qa_chain = get_qa_chain()

# request 스키마
class QuestionRequest(BaseModel):
    query: str

class EmbeddingRequest(BaseModel):
    github_url: str

# response 스키마
class QuestionResponse(BaseModel):
    query: str
    message: str

class EmbeddingResponse(BaseModel):
    message: str


# 질문 API
@app.post("/api/question", response_model=QuestionResponse)
async def ask_question(request: QuestionRequest):
    try:
        retriever = qa_chain.retriever
        relevant_docs = retriever.invoke(request.query)

        log_relevant_docs(relevant_docs)

        response = qa_chain.invoke(request.query)

        return QuestionResponse(
            query=request.query,
            message=response["result"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# 임베딩 API
@app.post("/api/embedding-code", response_model=EmbeddingResponse)
async def embed(request: EmbeddingRequest):
    try:
        embed_documents(request.github_url)
        return EmbeddingResponse(
            message=f"{request.github_url} 주소 코드의 임베딩을 완료했습니다."
        )
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))
