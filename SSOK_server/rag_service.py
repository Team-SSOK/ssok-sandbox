import os
from dotenv import load_dotenv
from langchain_qdrant import QdrantVectorStore
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain.chains import RetrievalQA
from qdrant_client import QdrantClient
from logging_utils import logger

# 환경 변수 로드
load_dotenv()

# 필수 환경 변수 로딩
OPENAI_API_KEY = str(os.getenv("OPENAI_API_KEY"))

# 필수 값 검증
if not OPENAI_API_KEY:
    raise ValueError("OPENAI_API_KEY가 .env에 설정되지 않음")

# 모델/설정 관련 환경 변수 로딩
EMBEDDING_MODEL = str(os.getenv("EMBEDDING_MODEL", "text-embedding-3-small"))
LLM_MODEL = str(os.getenv("LLM_MODEL", "gpt-4o-mini"))
LLM_TEMPERATURE = float(os.getenv("LLM_TEMPERATURE", "0.2"))
RETRIEVER_TOP_K = int(os.getenv("RETRIEVER_TOP_K", "3"))
COLLECTION_NAME = str(os.getenv("COLLECTION_NAME", "java-files"))

# Qdrant client 연결
client = QdrantClient(host="qdrant", port=6333)

if not client.collection_exists(COLLECTION_NAME):
    from qdrant_client.http.models import VectorParams, Distance
    client.create_collection(
        collection_name=COLLECTION_NAME,
        vectors_config=VectorParams(size=1536, distance=Distance.COSINE),
    )
    logger.info(f"{COLLECTION_NAME} 컬렉션 자동 생성됨")

# 임베딩 모델
embedding_model = OpenAIEmbeddings(
    model=EMBEDDING_MODEL
)

# Qdrant VectorStore
vectorstore = QdrantVectorStore(
    client=client,
    collection_name=COLLECTION_NAME,
    embedding=embedding_model,
)

# Retriever
retriever = vectorstore.as_retriever(search_kwargs={"k": RETRIEVER_TOP_K})

# LLM
llm = ChatOpenAI(
    model=LLM_MODEL,
    temperature=LLM_TEMPERATURE
)

# Retrieval QA 체인
qa_chain = RetrievalQA.from_chain_type(
    llm=llm,
    retriever=retriever
)

# 서버에서 가져다 쓸 수 있게 export
def get_qa_chain():
    return qa_chain
