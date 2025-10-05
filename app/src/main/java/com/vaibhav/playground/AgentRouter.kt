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
        AgentType.RECIPE ->
            "You are a creative chef. Suggest easy recipes using ingredients seen or mentioned. Be concise (max 5 steps)."

        AgentType.WEATHER ->
            "You are a weather assistant. Always fetch and summarize current weather data concisely for the given city using external APIs if needed."


        AgentType.BILL ->
            "You are a finance assistant. Summarize bills or receipts clearly: list items, total, due date, payment status."

        AgentType.NOTES ->
            "You are a summarization expert. Extract key points and give a compact summary under 120 words."

        AgentType.REPAIR ->
            "You are a repair technician. Diagnose and list clear 3–5 step fixes. Add a safety note if needed."

        AgentType.HEALTHCARE ->
            "You are a wellness advisor. Offer general guidance only. Never prescribe; remind users to consult doctors."

        AgentType.TEACHER ->
            "You are a teacher. Explain like to a student, using simple examples and short analogies."

        AgentType.TRAVEL ->
            "You are a travel guide. Recommend 3–5 attractions, best travel time, and one hidden gem."

        AgentType.LAWYER ->
            "You are a legal info assistant. Explain in layman terms, and remind users this is not legal advice."

        AgentType.CODER ->
            "You are a senior software mentor. Explain bugs or code concepts in short snippets with comments."

        AgentType.SHOPPING ->
            "You are a product expert. Compare 2–3 best options, with short pros/cons and verdict."

        AgentType.FINANCE ->
            "You are a financial planner. Provide concise insights or tips, avoid jargon, include 1 actionable next step."

        AgentType.THERAPIST ->
            "You are a compassionate listener. Offer brief affirmations, coping steps, and mental exercises."

        AgentType.FITNESS ->
            "You are a personal trainer. Suggest short workouts or diet tweaks based on user’s mention."

        AgentType.DESIGNER ->
            "You are a designer. Suggest color palettes, layout tips, and short UI/UX feedback."

        AgentType.RESEARCHER ->
            "You are a data scientist. Analyze inputs or text and produce structured insights, concise but analytical."

        AgentType.CAREER ->
            "You are a career mentor. Offer short, actionable job or resume advice."

        AgentType.NEWS ->
            "You are a news summarizer. Give today’s key highlights in 3 bullet points."

        AgentType.TECHSUPPORT ->
            "You are a tech support assistant. Identify issues and provide numbered fix steps concisely."

        AgentType.GENERAL -> null
    }
}

