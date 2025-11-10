# 🚀 Spring AI Learnings

My personal journey exploring Spring AI framework and building intelligent applications with Java and Spring Boot.

## 📖 About This Repository

This repository documents my learning path with Spring AI, Spring's official framework for building AI-powered applications. 
It contains practical examples, experiments, and notes as I explore various AI integration patterns with Spring Boot.

### 🎯 Learning Goals

- Master Spring AI fundamentals and core concepts
- Integrate various AI models (OpenAI, Anthropic Claude, Ollama, etc.)
- Build RAG (Retrieval-Augmented Generation) applications
- Implement AI tools calling
- Explore vector databases and embeddings
- Apply best practices for AI application development

## 🛠️ Technologies & Tools

- **Java**: 17+
- **Spring Boot**: 3.x
- **Spring AI**: Latest version
- **AI Providers**: OpenAI
- **Vector Stores**: PostgreSQL with pgvector
- **Build Tool**: Maven/Gradle

## 📋 Prerequisites

Before running the examples, ensure you have:

- JDK 17 or higher installed
- Maven or Gradle
- API keys for AI providers. Using OpenAI.
- Docker (optional, for running local vector databases)

## 🗂️ Project Structure

```
spring-ai-learnings/
├── src/main/java/
│   ├── advisors/            # Advisors explorations (to be explored)
│   ├── chat/                # Chat model examples
│   ├── rag/                 # RAG patterns and examples
│   ├── models/              # Data models
│   └── observability/       # Observability examples (to be explored)
├── src/main/resources/
│   ├── application.yml      # Application configuration
│   └── data/                # Sample data and vector stores
│       ├── article.pdf      # Sample article for RAG
│       ├── models.json      # Model configurations
│       ├── systemDesign.pdf # System design document
│       └── vectorstore.json # Built-in vector database
└── docs/                    # Additional documentation                  # Additional documentation
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/niharikapatel412-hub/spring-ai-learnings.git
cd spring-ai-learnings
```

### 2. Configure API Keys

Create an `application-local.yml` file in `src/main/resources/`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

Or set environment variables:

```bash
export OPENAI_API_KEY=your_openai_key
```

### 3. Build and Run

```bash
# Using Maven
mvn clean install
mvn spring-boot:run

# Using Gradle
./gradlew build
./gradlew bootRun
```

## 📚 Topics Covered

### ✅ Chat Models
- Basic chat completions
- Streaming responses
- Multi-modal inputs (text + images)
- Chat history management
- Different model providers (OpenAI, Claude, Ollama)

### ✅ Embeddings & Vector Stores
- Generating embeddings
- Storing vectors in databases
- Similarity search
- Integration with Pinecone, Chroma, pgvector

### ✅ RAG (Retrieval-Augmented Generation)
- Document loading and chunking
- Building knowledge bases
- Query-based retrieval
- Context injection into prompts

### ✅ Tools Calling
- Defining functions for AI to call
- Weather APIs, database queries
- Multi-step workflows
- Error handling

### ✅ Prompt Engineering
- Prompt templates
- Few-shot learning
- System prompts
- Output parsers

### ✅ Observability
- Request/response logging
- Token usage tracking
- Performance monitoring

## 📝 Learning Notes

### Key Takeaways
- Spring AI abstracts away provider-specific APIs, making it easy to switch between models
- Vector stores are crucial for building context-aware applications
- Proper chunking strategy significantly impacts RAG performance
- Function calling enables AI to interact with external systems

### Projects Built Using These Learnings
   I've applied the concepts learned in this repository to build real-world applications:

1. ## Agentic TiDB CRUD Example
    - (https://github.com/niharikapatel412-hub/tiDb-Agentic-AI)
2. ## Trade Execution Demo Project 
    - (https://github.com/niharikapatel412-hub/trade-execution-demo-project)

## 🔗 Resources

### Official Documentation
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)

### Tutorials & Guides
- [Getting Started with Spring AI](https://spring.io/guides)
- [Spring AI Examples](https://github.com/spring-projects/spring-ai-examples)
- [Dan Vega's Spring AI Workshop Video](https://www.youtube.com/watch?v=FzLABAppJfM&t=9406s)
- [Dan Vega's Spring AI Workshop GitHub](https://github.com/danvega/spring-ai-workshop)


## 📬 Contact

- **GitHub**: [@niharikapatel412-hub](https://github.com/niharikapatel412-hub)
- **LinkedIn**: [Niharika Patel](www.linkedin.com/in/niharikadpatel)

**Happy Learning! 🎓**