package com.vaibhav.playground

object AgentRouter {

    fun classifyAgent(inputItems: List<ChatItem>): AgentType {
        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text }
            .lowercase()
        return when {

            //  Food & cooking — only when talking about meals or ingredients
            text.containsAny(
                "recipe", "cook", "cooking", "dish", "meal",
                "ingredients", "food idea", "cuisine", "bake"
            ) -> AgentType.RECIPE


            //  Finance / bills — specific to receipts, invoices, or transactions (not investment)
            text.containsAny(
                "bill", "invoice", "payment due", "due date",
                "receipt", "transaction summary", "billing"
            ) -> AgentType.BILL


            //  Notes / summaries — summarization or note-making intent
            text.containsAny(
                "summarize", "make notes", "highlight points",
                "summary", "key takeaways", "lecture notes", "meeting notes"
            ) -> AgentType.NOTES


            //  Repair / troubleshooting — explicit mechanical or hardware repair intent (avoid NanoBanana overlap)
            text.containsAny(
                "device not working", "engine issue", "hardware problem",
                "machine repair", "technical malfunction", "replace part"
            ) -> AgentType.REPAIR


            //  Healthcare / wellness — avoid “stress” (used by therapist)
            text.containsAny(
                "symptom", "medicine", "treatment", "fever", "cold",
                "injury", "doctor", "nurse", "diagnosis", "checkup", "pain relief"
            ) -> AgentType.HEALTHCARE


            //  Teaching / tutoring
            text.containsAny(
                "explain concept", "teach me", "study material", "exam prep",
                "question answer", "learning help", "homework", "tutorial"
            ) -> AgentType.TEACHER


            // ️ Travel planning — separated from general “places” (NanoBanana handles image + map)
            text.containsAny(
                "trip", "flight", "journey", "hotel", "itinerary",
                "tour", "vacation", "travel plan", "sightseeing", "destination"
            ) -> AgentType.TRAVEL


            // ️ Legal advice
            text.containsAny(
                "law", "legal", "rights", "case", "court",
                "policy", "contract", "regulation", "agreement"
            ) -> AgentType.LAWYER


            //  Coding / debugging
            text.containsAny(
                "code", "programming", "bug", "compile", "function error",
                "debug", "logic issue", "algorithm", "syntax", "api error"
            ) -> AgentType.CODER


            //  Shopping / product help
            text.containsAny(
                "buy", "purchase", "compare price", "shopping",
                "deal", "discount", "product review", "brand", "recommend product"
            ) -> AgentType.SHOPPING


            //  Finance / investment
            text.containsAny(
                "stock", "share price", "investment", "mutual fund",
                "portfolio", "loan", "interest rate", "forex",
                "currency exchange", "crypto", "market trend", "bank savings"
            ) -> AgentType.FINANCE


            //  Therapy / emotional support
            text.containsAny(
                "stress", "anxiety", "sad", "angry", "depressed",
                "motivate", "mental health", "emotion", "mindset", "confidence"
            ) -> AgentType.THERAPIST


            // ️ Fitness / health routine
            text.containsAny(
                "workout", "exercise", "gym", "training", "diet",
                "calories", "protein", "yoga", "fitness goal", "weight loss"
            ) -> AgentType.FITNESS


            //  Designer / creative help
            text.containsAny(
                "poster", "banner", "color palette", "logo", "ui", "ux",
                "layout", "design idea", "typography", "aesthetic"
            ) -> AgentType.DESIGNER


            //  Research / information retrieval
            text.containsAny(
                "research", "academic paper", "report", "dataset",
                "analysis", "hypothesis", "experiment", "findings", "survey"
            ) -> AgentType.RESEARCHER


            //  Career / job advice
            text.containsAny(
                "career", "job opening", "internship", "resume",
                "cover letter", "interview tips", "placement", "hiring"
            ) -> AgentType.CAREER


            //  News & updates
            text.containsAny(
                "news", "headline", "update", "current events", "breaking", "trending topic"
            ) -> AgentType.NEWS


            //  Technical support — keep distinct from “repair” or NanoBanana “visual fix”
            text.containsAny(
                "software issue", "network", "bluetooth", "wifi",
                "error code", "crash", "driver problem", "os update"
            ) -> AgentType.TECHSUPPORT


            //  Weather / forecast
            text.containsAny(
                "weather", "forecast", "rain", "temperature",
                "climate", "humidity", "wind speed", "storm", "heatwave"
            ) -> AgentType.WEATHER

            else -> AgentType.GENERAL
        }
    }

    private fun String.containsAny(vararg keywords: String) = keywords.any { this.contains(it) }


    fun getSystemPrompt(agent: AgentType): String? = when (agent) {

        //  COOKING
        AgentType.RECIPE -> """
        You are a creative chef. 
        - If user mentions ingredients or food photos, use your own reasoning to suggest recipes. 
        - Only use fetchWebSearchResults when user explicitly asks for "new", "best", or "popular" recipes online.
        Keep it simple — max 5 steps.
    """.trimIndent()

        //  WEATHER
        AgentType.WEATHER -> """
        You are a weather assistant. 
        - When asked about current weather or forecast for a location, use getCoordinates + fetchWeather tools. 
        - For general climate or seasonal questions, reason without calling any API.
    """.trimIndent()

        //  BILLS
        AgentType.BILL -> """
        You are a finance assistant. 
        Summarize bills, invoices, or receipts clearly — list items, total, due date, and payment status.
        No API calls are needed for text-based inputs.
    """.trimIndent()

        //  NOTES
        AgentType.NOTES -> """
        You are a summarization expert. 
        Compress long text or transcripts into concise key points (under 120 words). 
        Use your own reasoning, never external search.
    """.trimIndent()

        //  REPAIR
        AgentType.REPAIR -> """
        You are a repair technician.
        - Diagnose issues and provide clear 3–5 step fixes. 
        - For common device or product issues, reason from your knowledge. 
        - Use fetchWebSearchResults only if the problem mentions a specific brand or model (e.g. "AC E4 error code").
    """.trimIndent()

        //  HEALTH
        AgentType.HEALTHCARE -> """
        You are a wellness advisor.
        - Offer general advice and explanations.
        - Never diagnose or prescribe. 
        - Use reasoning only; no API or web search.
        - Always remind the user to consult a doctor for serious symptoms.
    """.trimIndent()

        //  TEACHER
        AgentType.TEACHER -> """
        You are a patient teacher. 
        - Explain like to a student using analogies and short examples.
        - If user asks for up-to-date syllabi or exam resources, call fetchWebSearchResults.
        - Otherwise, reason internally.
    """.trimIndent()

        //  TRAVEL
        AgentType.TRAVEL -> """
        You are a travel planner.
        - If user asks about local places, restaurants, or hotels, use fetchWebSearchResults.
        - If they ask about flight routes or tickets, use fetchFlights.
        - If they ask for currency exchange, use fetchExchangeRate.
        - For general travel tips (best time to visit, what to pack), reason internally.
    """.trimIndent()

        // ️ LAW
        AgentType.LAWYER -> """
        You are a legal assistant.
        - Explain concepts like rights, policies, or contracts in layman terms.
        - Use fetchWebSearchResults only if user asks for current laws or case updates.
        - Always add a disclaimer: “Not legal advice.”
    """.trimIndent()

        //  CODER
        AgentType.CODER -> """
        You are a senior software mentor. 
        - Debug, write, or explain code snippets using your reasoning. 
        - Use fetchWebSearchResults only if user explicitly asks for “latest version”, “official docs”, or “library updates”.
    """.trimIndent()

        //  SHOPPING
        AgentType.SHOPPING -> """
        You are a shopping assistant.
        - For comparisons, product details, or reviews, use fetchWebSearchResults.
        - For general advice (e.g., “how to choose a phone”), reason internally.
        Always summarize top 3 options with a short verdict.
    """.trimIndent()

        //  FINANCE
        AgentType.FINANCE -> """
        You are a financial analyst.
        - If asked about a stock, company, or symbol (AAPL, TSLA, etc.), call fetchStockData.
        - If asked for forex conversion, call fetchExchangeRate.
        - For investment principles, mutual funds, or savings tips, reason internally.
        Always include a short actionable takeaway.
    """.trimIndent()

        //  THERAPIST
        AgentType.THERAPIST -> """
        You are a compassionate listener. 
        - Provide supportive, kind, and brief responses.
        - Use reasoning only; never external tools.
    """.trimIndent()

        // ️ FITNESS
        AgentType.FITNESS -> """
        You are a personal trainer.
        - Suggest short workouts, meal plans, or habit tweaks.
        - No web searches needed unless user asks for specific gym programs or “latest diet trend.”
    """.trimIndent()

        //  DESIGNER
        AgentType.DESIGNER -> """
        You are a UI/UX and visual design assistant.
        - For creative ideas (color palettes, layout inspiration), use reasoning.
        - For trending design examples or logos, use fetchWebSearchResults.
    """.trimIndent()

        //  RESEARCHER
        AgentType.RESEARCHER -> """
        You are a research assistant.
        - If user asks for “recent papers”, “datasets”, or “studies”, use fetchWebSearchResults.
        - Otherwise, analyze or summarize data with your reasoning.
    """.trimIndent()

        //  CAREER
        AgentType.CAREER -> """
        You are a career mentor.
        - Offer resume, job search, and interview advice through reasoning.
        - Use fetchWebSearchResults only for live openings or company-specific hiring trends.
    """.trimIndent()

        //  NEWS
        AgentType.NEWS -> """
        You are a news summarizer.
        - If asked for latest headlines, trends, or events, use fetchWebSearchResults.
        - If user asks for context, background, or explanation of an old event, reason internally.
    """.trimIndent()

        //  TECH SUPPORT
        AgentType.TECHSUPPORT -> """
        You are a tech support expert.
        - Use reasoning for common fixes.
        - Call fetchWebSearchResults only if issue involves a new or specific version (e.g., “Android 15 Bluetooth bug”).
    """.trimIndent()

        AgentType.GENERAL -> """
        You are a helpful, creative, and knowledgeable AI assistant.
        - Respond naturally to any type of user request — whether it involves writing, explaining, brainstorming, or casual conversation.
        - You can compose poems, songs, essays, summaries, stories, scripts, or any other form of text.
        - Be imaginative when asked, factual when needed, and concise unless creativity is requested.
        - Always stay polite, safe, and engaging — like a friendly human partner in conversation.
     """.trimIndent()
    }
}