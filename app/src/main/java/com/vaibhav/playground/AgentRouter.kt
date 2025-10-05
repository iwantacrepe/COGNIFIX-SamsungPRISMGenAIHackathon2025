package com.vaibhav.playground

object AgentRouter {

    fun classifyAgent(inputItems: List<ChatItem>): AgentType {
        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text }
            .lowercase()

        return when {
            // Cooking
            listOf("recipe", "cook", "dish", "fridge", "ingredients").any { it in text } -> AgentType.RECIPE

            // Bills / finance docs
            listOf("bill", "invoice", "receipt", "payment").any { it in text } -> AgentType.BILL

            // Notes
            listOf("note", "summary", "summarize", "highlight").any { it in text } -> AgentType.NOTES

            // Repair
            listOf("fix", "repair", "broken", "not working").any { it in text } -> AgentType.REPAIR

            // Healthcare
            listOf("symptom", "medicine", "doctor", "health", "pain", "fever").any { it in text } -> AgentType.HEALTHCARE

            // Teacher
            listOf("explain", "teach", "study", "lesson", "homework", "exam").any { it in text } -> AgentType.TEACHER

            // Travel
            listOf("trip", "flight", "hotel", "vacation", "places", "travel").any { it in text } -> AgentType.TRAVEL

            // Legal
            listOf("law", "legal", "contract", "case", "court").any { it in text } -> AgentType.LAWYER

            // Coding
            listOf("code", "bug", "program", "debug", "compile", "script").any { it in text } -> AgentType.CODER

            // Shopping
            listOf("buy", "price", "shopping", "cart", "amazon", "meesho").any { it in text } -> AgentType.SHOPPING

            // Finance
            listOf("stock", "investment", "crypto", "loan", "mutual fund").any { it in text } -> AgentType.FINANCE

            // Therapy
            listOf("stress", "anxiety", "sad", "happy", "feelings").any { it in text } -> AgentType.THERAPIST

            else -> AgentType.GENERAL
        }
    }

    fun getSystemPrompt(agent: AgentType): String? {
        return when (agent) {
            AgentType.RECIPE -> " You are a master chef. Suggest creative recipes based on text or fridge images."
            AgentType.BILL -> " You are a finance assistant. Summarize bills and invoices with clear totals."
            AgentType.NOTES -> " You are a study helper. Summarize and highlight main points from notes or PDFs."
            AgentType.REPAIR -> " You are a repair expert. Give step-by-step fixes with safety tips."
            AgentType.HEALTHCARE -> " You are a health advisor. Provide general wellness info, but remind user to consult a doctor."
            AgentType.TEACHER -> " You are a patient teacher. Explain concepts in simple words with examples."
            AgentType.TRAVEL -> " You are a travel guide. Suggest itineraries, hotels, and cultural tips."
            AgentType.LAWYER -> " You are a legal assistant. Provide general legal information (not legal advice)."
            AgentType.CODER -> " You are a coding tutor. Help debug, explain, and write code in any language."
            AgentType.SHOPPING -> " You are a shopping assistant. Compare products, suggest best deals, and recommend brands."
            AgentType.FINANCE -> " You are a finance planner. Provide stock, crypto, and investment strategies."
            AgentType.THERAPIST -> " You are a friendly therapist. Listen empathetically and give coping strategies."
            AgentType.GENERAL -> null
        }
    }
}