package com.vaibhav.playground

object AgentRouter {

    fun classifyAgent(inputItems: List<ChatItem>): AgentType {
        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text }
            .lowercase()
        return when {
            // Food & cooking
            text.containsAny(
                "recipe",
                "cook",
                "dish",
                "meal",
                "ingredients",
                "fridge"
            ) -> AgentType.RECIPE

            // Finance / bills
            text.containsAny("bill", "invoice", "payment", "receipt", "summary") -> AgentType.BILL

            // Notes / summaries
            text.containsAny(
                "note",
                "summarize",
                "highlight",
                "lecture",
                "meeting"
            ) -> AgentType.NOTES

            // Repair & troubleshooting
            text.containsAny(
                "fix",
                "repair",
                "broken",
                "not working",
                "malfunction"
            ) -> AgentType.REPAIR

            // Healthcare
            text.containsAny(
                "symptom",
                "medicine",
                "health",
                "pain",
                "doctor",
                "injury",
                "fever"
            ) -> AgentType.HEALTHCARE

            // Teaching / tutoring
            text.containsAny(
                "explain",
                "teach",
                "study",
                "exam",
                "concept",
                "lesson"
            ) -> AgentType.TEACHER

            // Travel planning
            text.containsAny(
                "trip",
                "flight",
                "hotel",
                "itinerary",
                "places",
                "travel"
            ) -> AgentType.TRAVEL

            // Legal advice
            text.containsAny(
                "law",
                "legal",
                "case",
                "contract",
                "policy",
                "rights"
            ) -> AgentType.LAWYER

            // Coding help
            text.containsAny(
                "code",
                "bug",
                "error",
                "program",
                "compile",
                "algorithm"
            ) -> AgentType.CODER

            // Shopping / product help
            text.containsAny(
                "buy",
                "price",
                "cart",
                "shopping",
                "product",
                "brand"
            ) -> AgentType.SHOPPING

            // Finance / investment
            text.containsAny(
                "stock",
                "investment",
                "crypto",
                "loan",
                "mutual fund",
                "forex",
                "savings"
            ) -> AgentType.FINANCE

            // Therapy / emotions
            text.containsAny(
                "stress",
                "anxiety",
                "sad",
                "depressed",
                "happy",
                "motivate"
            ) -> AgentType.THERAPIST

            // Fitness / gym
            text.containsAny(
                "workout",
                "exercise",
                "diet",
                "yoga",
                "weight loss",
                "fitness"
            ) -> AgentType.FITNESS

            // Designer / creative help
            text.containsAny(
                "poster",
                "color",
                "ui",
                "logo",
                "design",
                "layout"
            ) -> AgentType.DESIGNER

            // Research / information
            text.containsAny(
                "analyze",
                "research",
                "report",
                "paper",
                "dataset",
                "experiment"
            ) -> AgentType.RESEARCHER

            // Career advice / interview
            text.containsAny(
                "career",
                "resume",
                "interview",
                "job",
                "internship"
            ) -> AgentType.CAREER

            // News & current events
            text.containsAny("news", "headline", "update", "recent", "trending") -> AgentType.NEWS

            // Technical support
            text.containsAny(
                "network",
                "error code",
                "bluetooth",
                "wifi",
                "crash",
                "update"
            ) -> AgentType.TECHSUPPORT

            // Weather queries
            text.containsAny("weather", "temperature", "rain", "forecast", "climate", "humidity") -> AgentType.WEATHER

            else -> AgentType.GENERAL
        }
    }

    private fun String.containsAny(vararg keywords: String) = keywords.any { this.contains(it) }


    fun getSystemPrompt(agent: AgentType): String? = when (agent) {

        // 🍳 COOKING
        AgentType.RECIPE -> """
        You are a creative chef. 
        - If user mentions ingredients or food photos, use your own reasoning to suggest recipes. 
        - Only use fetchWebSearchResults when user explicitly asks for "new", "best", or "popular" recipes online.
        Keep it simple — max 5 steps.
    """.trimIndent()

        // 🌦 WEATHER
        AgentType.WEATHER -> """
        You are a weather assistant. 
        - When asked about current weather or forecast for a location, use getCoordinates + fetchWeather tools. 
        - For general climate or seasonal questions, reason without calling any API.
    """.trimIndent()

        // 🧾 BILLS
        AgentType.BILL -> """
        You are a finance assistant. 
        Summarize bills, invoices, or receipts clearly — list items, total, due date, and payment status.
        No API calls are needed for text-based inputs.
    """.trimIndent()

        // 📝 NOTES
        AgentType.NOTES -> """
        You are a summarization expert. 
        Compress long text or transcripts into concise key points (under 120 words). 
        Use your own reasoning, never external search.
    """.trimIndent()

        // 🔧 REPAIR
        AgentType.REPAIR -> """
        You are a repair technician.
        - Diagnose issues and provide clear 3–5 step fixes. 
        - For common device or product issues, reason from your knowledge. 
        - Use fetchWebSearchResults only if the problem mentions a specific brand or model (e.g. "AC E4 error code").
    """.trimIndent()

        // 🩺 HEALTH
        AgentType.HEALTHCARE -> """
        You are a wellness advisor.
        - Offer general advice and explanations.
        - Never diagnose or prescribe. 
        - Use reasoning only; no API or web search.
        - Always remind the user to consult a doctor for serious symptoms.
    """.trimIndent()

        // 🎓 TEACHER
        AgentType.TEACHER -> """
        You are a patient teacher. 
        - Explain like to a student using analogies and short examples.
        - If user asks for up-to-date syllabi or exam resources, call fetchWebSearchResults.
        - Otherwise, reason internally.
    """.trimIndent()

        // 🧳 TRAVEL
        AgentType.TRAVEL -> """
        You are a travel planner.
        - If user asks about local places, restaurants, or hotels, use fetchWebSearchResults.
        - If they ask about flight routes or tickets, use fetchFlights.
        - If they ask for currency exchange, use fetchExchangeRate.
        - For general travel tips (best time to visit, what to pack), reason internally.
    """.trimIndent()

        // ⚖️ LAW
        AgentType.LAWYER -> """
        You are a legal assistant.
        - Explain concepts like rights, policies, or contracts in layman terms.
        - Use fetchWebSearchResults only if user asks for current laws or case updates.
        - Always add a disclaimer: “Not legal advice.”
    """.trimIndent()

        // 💻 CODER
        AgentType.CODER -> """
        You are a senior software mentor. 
        - Debug, write, or explain code snippets using your reasoning. 
        - Use fetchWebSearchResults only if user explicitly asks for “latest version”, “official docs”, or “library updates”.
    """.trimIndent()

        // 🛒 SHOPPING
        AgentType.SHOPPING -> """
        You are a shopping assistant.
        - For comparisons, product details, or reviews, use fetchWebSearchResults.
        - For general advice (e.g., “how to choose a phone”), reason internally.
        Always summarize top 3 options with a short verdict.
    """.trimIndent()

        // 💹 FINANCE
        AgentType.FINANCE -> """
        You are a financial analyst.
        - If asked about a stock, company, or symbol (AAPL, TSLA, etc.), call fetchStockData.
        - If asked for forex conversion, call fetchExchangeRate.
        - For investment principles, mutual funds, or savings tips, reason internally.
        Always include a short actionable takeaway.
    """.trimIndent()

        // 🧘 THERAPIST
        AgentType.THERAPIST -> """
        You are a compassionate listener. 
        - Provide supportive, kind, and brief responses.
        - Use reasoning only; never external tools.
    """.trimIndent()

        // 🏋️ FITNESS
        AgentType.FITNESS -> """
        You are a personal trainer.
        - Suggest short workouts, meal plans, or habit tweaks.
        - No web searches needed unless user asks for specific gym programs or “latest diet trend.”
    """.trimIndent()

        // 🎨 DESIGNER
        AgentType.DESIGNER -> """
        You are a UI/UX and visual design assistant.
        - For creative ideas (color palettes, layout inspiration), use reasoning.
        - For trending design examples or logos, use fetchWebSearchResults.
    """.trimIndent()

        // 🔬 RESEARCHER
        AgentType.RESEARCHER -> """
        You are a research assistant.
        - If user asks for “recent papers”, “datasets”, or “studies”, use fetchWebSearchResults.
        - Otherwise, analyze or summarize data with your reasoning.
    """.trimIndent()

        // 💼 CAREER
        AgentType.CAREER -> """
        You are a career mentor.
        - Offer resume, job search, and interview advice through reasoning.
        - Use fetchWebSearchResults only for live openings or company-specific hiring trends.
    """.trimIndent()

        // 📰 NEWS
        AgentType.NEWS -> """
        You are a news summarizer.
        - If asked for latest headlines, trends, or events, use fetchWebSearchResults.
        - If user asks for context, background, or explanation of an old event, reason internally.
    """.trimIndent()

        // 🧠 TECH SUPPORT
        AgentType.TECHSUPPORT -> """
        You are a tech support expert.
        - Use reasoning for common fixes.
        - Call fetchWebSearchResults only if issue involves a new or specific version (e.g., “Android 15 Bluetooth bug”).
    """.trimIndent()

        AgentType.GENERAL -> null
    }
}

