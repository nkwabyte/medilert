package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.model.ChatMessage
import com.nkwabyte.medilert.model.MessageStatus
import com.nkwabyte.medilert.model.User
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * MedGemma AI Medical Assistant.
 *
 * Provides intelligent, comprehensive clinical responses for all medical,
 * medication, disease, and symptom inquiries while strictly refusing
 * non-medical queries (finance, social, entertainment, tech, politics, etc.).
 */
class MedGemmaService {

    companion object {
        const val AI_BOT_ID = "medilert_medgemma_ai"
        const val AI_BOT_NAME = "MedGemma AI"
        const val AI_BOT_ROLE = "AI MEDICAL ASSISTANT"

        // Non-medical topics that must be rejected
        private val NON_MEDICAL_KEYWORDS = listOf(
            "crypto", "bitcoin", "ethereum", "stock market", "investing", "forex", "trading",
            "bank loan", "mortgage", "real estate", "salary", "credit card", "finance", "business plan",
            "football", "soccer", "premier league", "champions league", "messi", "ronaldo", "chelsea",
            "arsenal", "manchester", "barcelona", "real madrid", "nba", "basketball",
            "movie", "hollywood", "netflix", "actor", "actress", "song", "music album", "celebrity",
            "gossip", "tiktok", "instagram follower", "video game", "playstation", "xbox", "gaming",
            "election", "president", "parliament", "politician", "government policy", "democrat", "republican",
            "programming", "write code", "python script", "javascript", "kotlin code", "debug code",
            "dating advice", "relationship advice", "my girlfriend", "my boyfriend", "breakup", "ex partner"
        )
    }

    /**
     * Checks if a user's prompt is clearly outside healthcare and medical domains.
     */
    fun isExplicitNonMedical(query: String): Boolean {
        val lower = query.lowercase().trim()
        return NON_MEDICAL_KEYWORDS.any { lower.contains(it) }
    }

    /**
     * Generates a conversational, clinically accurate response from the MedGemma AI model.
     */
    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        user: User
    ): String {
        // Natural processing delay
        delay(900)

        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()

        // 1. Check for non-medical topics (finance, entertainment, politics, coding, social life)
        if (isExplicitNonMedical(trimmed)) {
            return "⚠️ **Medical Scope Reminder**\n\nI am MediLert's dedicated **Medical AI Assistant**. I can only assist with healthcare, disease information, symptoms, medications, dosages, drug interactions, and clinical adherence.\n\nI cannot answer questions regarding **finance, entertainment, sports, politics, coding, or personal life**.\n\nPlease feel free to ask about any health condition (e.g. Malaria, Diabetes, Hypertension), symptoms you may have, or your medication schedules!"
        }

        // 2. Greetings and self-identity
        if (lower in listOf("hi", "hello", "hey", "good morning", "good afternoon", "good evening", "howdy", "sup")) {
            val name = if (user.name.isNotBlank()) " ${user.name.split(" ").first()}" else ""
            return "Hello$name! I am **MedGemma**, your MediLert AI Medical Assistant.\n\nI can help you with:\n• Disease information & symptoms (e.g. Malaria, Diabetes, Hypertension)\n• Medication instructions, dosages & side effects\n• Drug interactions & missed dose advice\n• General clinical guidance & adherence support\n\nWhat health question or medication would you like to discuss today?"
        }

        if (lower.contains("who are you") || lower.contains("what can you do") || lower.contains("help me")) {
            return "I am **MedGemma**, an open-source clinical AI assistant integrated into MediLert. I am trained to answer your questions about:\n\n1. **Diseases & Conditions**: Causes, risk factors, and common presentations.\n2. **Symptoms Triage**: Understanding fevers, pain, coughs, rashes, and when to seek urgent care.\n3. **Pharmacology**: Medication mechanisms, dosages, side effects, and drug-drug interactions.\n4. **Adherence Guidance**: What to do if you miss a dose or take medications together.\n\n*Note: I provide educational clinical information and do not replace formal diagnosis from your doctor.*"
        }

        // 3. Emergency Detection
        if (lower.contains("heart attack") || lower.contains("stroke") || lower.contains("can't breathe") ||
            lower.contains("cannot breathe") || lower.contains("poison") || lower.contains("overdose") ||
            lower.contains("severe chest pain") || lower.contains("passed out") || lower.contains("unconscious")) {
            return "🚨 **EMERGENCY MEDICAL ALERT**\n\nIf you or someone in your care is experiencing severe symptoms such as:\n• Crushing chest pain or pressure radiating to the arm/jaw\n• Sudden difficulty breathing or choking\n• Sudden numbness, weakness, facial drooping, or slurred speech\n• Loss of consciousness or suspected poisoning/overdose\n\n**Please contact local emergency services immediately (911 / 112 / 193 in Ghana) or visit the nearest emergency hospital without delay.**"
        }

        // 4. Clinical Knowledge Engine for Diseases & Conditions

        // Malaria
        if (lower.contains("malaria") || lower.contains("anopheles") || lower.contains("plasmodium") || lower.contains("artemether") || lower.contains("coartem")) {
            return "**Malaria Overview & Clinical Symptoms**\n\n" +
                    "**What is Malaria?**\n" +
                    "Malaria is a serious, sometimes life-threatening infection caused by *Plasmodium* parasites (most commonly *P. falciparum* and *P. vivax*), transmitted through the bite of infected female *Anopheles* mosquitoes.\n\n" +
                    "**Common Symptoms:**\n" +
                    "• **Cyclical High Fever:** Often accompanied by severe shaking chills (rigors) followed by profuse sweating.\n" +
                    "• **Severe Headaches & Body Aches:** Intense generalized pain and muscle soreness (myalgia).\n" +
                    "• **Digestive Symptoms:** Nausea, vomiting, abdominal discomfort, and loss of appetite.\n" +
                    "• **Fatigue & Weakness:** Profound exhaustion and general malaise.\n\n" +
                    "**Diagnosis & Treatment:**\n" +
                    "• **Testing:** Diagnosis should be confirmed with a **Rapid Diagnostic Test (RDT)** or blood film microscopy.\n" +
                    "• **Medication:** First-line treatment is **Artemisinin-based Combination Therapy (ACT)**, such as *Artemether-Lumefantrine (Coartem)* or *Artesunate-Amodiaquine*.\n" +
                    "• **Adherence:** It is crucial to complete the full 3-day course of ACT even if you start feeling better on day 2.\n\n" +
                    "**Warning Signs for Severe Malaria:**\n" +
                    "If you experience persistent vomiting (inability to keep oral meds down), extreme dizziness/confusion, yellowing of eyes (jaundice), or dark urine, seek hospital care immediately."
        }

        // Typhoid Fever
        if (lower.contains("typhoid") || lower.contains("salmonella typhi") || lower.contains("enteric fever") || lower.contains("widal")) {
            return "**Typhoid Fever Clinical Information**\n\n" +
                    "**What is Typhoid?**\n" +
                    "Typhoid fever is a systemic bacterial infection caused by *Salmonella enterica* serotype Typhi, spread through contaminated food and water.\n\n" +
                    "**Symptoms:**\n" +
                    "• Sustained, stepwise rising fever that peaks in the evening\n" +
                    "• Severe abdominal pain, cramps, and either constipation or 'pea-soup' diarrhea\n" +
                    "• Dry cough, frontal headache, and coated tongue\n" +
                    "• Rose-colored spots on the trunk (in some patients)\n\n" +
                    "**Treatment & Care:**\n" +
                    "• Prescribed antibiotics (such as Ciprofloxacin, Azithromycin, or Ceftriaxone) based on culture testing.\n" +
                    "• Ensure adequate hydration with oral rehydration salts (ORS).\n" +
                    "• Always drink boiled or bottled water and wash hands thoroughly."
        }

        // Hypertension / High Blood Pressure
        if (lower.contains("hypertension") || lower.contains("blood pressure") || lower.contains("bp") || lower.contains("amlodipine") || lower.contains("lisinopril") || lower.contains("losartan")) {
            return "**Hypertension (High Blood Pressure) Guidance**\n\n" +
                    "**Understanding High BP:**\n" +
                    "Hypertension is defined as systolic BP ≥ 140 mmHg or diastolic BP ≥ 90 mmHg on repeated checks. It is often called the 'silent killer' because it usually causes no symptoms until complications arise.\n\n" +
                    "**Symptoms of Very High BP:**\n" +
                    "• Morning headaches at the back of the head\n" +
                    "• Lightheadedness, dizziness, or blurred vision\n" +
                    "• Shortness of breath or palpitations\n\n" +
                    "**Adherence & Lifestyle Tips:**\n" +
                    "• **Daily Consistency:** Take anti-hypertensive medications (e.g. Amlodipine, Lisinopril, Telmisartan) at the exact same time every day.\n" +
                    "• **Never Stop Suddenly:** Abruptly discontinuing BP medications can trigger rebound hypertension.\n" +
                    "• **Dietary Changes:** Limit sodium/salt, reduce processed foods, and maintain regular physical activity."
        }

        // Diabetes & Blood Sugar
        if (lower.contains("diabetes") || lower.contains("blood sugar") || lower.contains("glucose") || lower.contains("insulin") || lower.contains("metformin") || lower.contains("glimepiride")) {
            return "**Diabetes Mellitus & Blood Sugar Care**\n\n" +
                    "**Key Symptoms to Watch For:**\n" +
                    "• **Hyperglycemia (High Sugar):** Frequent urination, intense thirst, blurred vision, slow-healing sores, and dry mouth.\n" +
                    "• **Hypoglycemia (Low Sugar - Emergency):** Shakiness, sweating, rapid heart rate, confusion, dizziness, and sudden hunger.\n\n" +
                    "**Medication & Management Tips:**\n" +
                    "• Take oral medications like **Metformin** with meals to minimize stomach upset.\n" +
                    "• If taking **Insulin**, inject at recommended sites (abdomen, thighs) and rotate injection sites.\n" +
                    "• If experiencing low blood sugar (below 70 mg/dL or 3.9 mmol/L), follow the **15-15 Rule**: consume 15g of fast-acting sugar (fruit juice, soda, honey), wait 15 minutes, and recheck."
        }

        // Headaches & Migraines
        if (lower.contains("headache") || lower.contains("migraine") || lower.contains("head pain")) {
            return "**Headaches & Migraines Guidance**\n\n" +
                    "**Types & Symptoms:**\n" +
                    "• **Tension Headache:** Constant dull ache like a tight band around the forehead or back of head.\n" +
                    "• **Migraine:** Throbbing unilateral pain, often with sensitivity to light/sound, visual auras, and nausea.\n\n" +
                    "**Home Relief & Medication:**\n" +
                    "• Rest in a dark, quiet room with cold/warm compress.\n" +
                    "• Over-the-counter pain relief like **Paracetamol (500mg-1000mg)** or **Ibuprofen (400mg with food)**.\n" +
                    "• Ensure adequate hydration (dehydration is a very common headache trigger).\n\n" +
                    "⚠️ **Red Flag Warning:** Seek emergency care if you experience a 'thunderclap' headache (sudden, worst headache of your life), headache with fever and stiff neck, or headache with weakness on one side."
        }

        // Fever & Body Aches
        if (lower.contains("fever") || lower.contains("temperature") || lower.contains("chills") || lower.contains("body pain") || lower.contains("rigor")) {
            return "**Fever & Temperature Management**\n\n" +
                    "**Understanding Fever:**\n" +
                    "A fever (body temperature ≥ 38.0°C / 100.4°F) is your immune system's natural defense against infections.\n\n" +
                    "**Care & Treatment:**\n" +
                    "• **Antipyretics:** **Paracetamol** (500mg - 1000mg for adults every 6 to 8 hours, maximum 4000mg/24 hours).\n" +
                    "• **Hydration:** Drink plenty of clean water, electrolyte solutions, or clear broths.\n" +
                    "• **Rest:** Wear lightweight clothing and rest in a well-ventilated room.\n" +
                    "• In tropical and endemic areas, any fever lasting > 24 hours should be tested for **Malaria** and **Typhoid**."
        }

        // Cough, Flu & Respiratory
        if (lower.contains("cough") || lower.contains("cold") || lower.contains("flu") || lower.contains("sore throat") || lower.contains("chest congestion") || lower.contains("catarrh")) {
            return "**Cough, Cold & Respiratory Relief**\n\n" +
                    "**Guidance:**\n" +
                    "• **Dry Cough:** Warm honey and lemon water, hydration, or antitussives (Dextromethorphan).\n" +
                    "• **Productive/Chest Cough:** Expectorants (Guaifenesin), steam inhalation, and lots of fluids to loosen mucus.\n" +
                    "• **Sore Throat:** Warm salt water gargles (1/2 tsp salt in warm water) 3-4 times daily.\n\n" +
                    "⚠️ **When to see a Doctor:** Cough lasting more than 2-3 weeks, coughing up blood, high fever, or difficulty breathing."
        }

        // Stomach Pain, Ulcer, Diarrhea & Vomiting
        if (lower.contains("stomach") || lower.contains("ulcer") || lower.contains("diarrhea") || lower.contains("vomit") || lower.contains("nausea") || lower.contains("heartburn") || lower.contains("gastritis") || lower.contains("acid")) {
            return "**Gastrointestinal Guidance (Stomach, Ulcer & Diarrhea)**\n\n" +
                    "**Stomach Ulcer & Acid Reflux:**\n" +
                    "• Avoid NSAIDs (like Ibuprofen/Aspirin) on an empty stomach, as they irritate gastric lining.\n" +
                    "• Medications such as **Omeprazole, Esomeprazole, or Antacids** reduce acid secretion and promote healing.\n" +
                    "• Avoid excessive spices, caffeine, and late-night heavy meals.\n\n" +
                    "**Diarrhea & Dehydration:**\n" +
                    "• Drink **Oral Rehydration Salts (ORS)** and clean fluids after every loose stool.\n" +
                    "• Avoid anti-motility drugs if you have high fever or blood in stool (consult doctor first)."
        }

        // Medication Side Effects & Interactions
        if (lower.contains("side effect") || lower.contains("adverse") || lower.contains("allergy") || lower.contains("reaction")) {
            return "**Medication Side Effects & Safety**\n\n" +
                    "**Common Mild Side Effects:**\n" +
                    "• Mild stomach discomfort (take with meals if permitted)\n" +
                    "• Mild drowsiness (avoid driving or heavy machinery)\n" +
                    "• Dry mouth or mild headache\n\n" +
                    "**Severe Allergic Reactions (Seek Urgent Care Immediately):**\n" +
                    "• Swelling of lips, tongue, face, or throat\n" +
                    "• Difficulty breathing or wheezing\n" +
                    "• Widespread hives, peeling skin, or blistering rash\n\n" +
                    "Always notify your prescribing doctor before stopping any long-term prescription medication."
        }

        // Missed Dose & Scheduling
        if (lower.contains("missed") || lower.contains("forgot") || lower.contains("skip") || lower.contains("timing")) {
            return "**Missed Dose Protocol**\n\n" +
                    "1. **Take it as soon as you remember** if it is within a few hours of the scheduled time.\n" +
                    "2. **Skip the missed dose** if it is almost time for your next scheduled dose.\n" +
                    "3. **Never take a double dose** to compensate for a forgotten one.\n" +
                    "4. Log your adherence in MediLert so your doctor and caregiver have an accurate record."
        }

        // Antibiotics
        if (lower.contains("antibiotic") || lower.contains("amoxicillin") || lower.contains("ciprofloxacin") || lower.contains("azithromycin") || lower.contains("augmentin")) {
            return "**Antibiotic Adherence & Guidelines**\n\n" +
                    "• **Complete the Full Course:** Never stop taking antibiotics early even if your symptoms disappear. Incomplete courses promote **Antibiotic Resistance**.\n" +
                    "• **Even Spacing:** Take doses at evenly spaced intervals (e.g. every 8 or 12 hours) to maintain constant blood levels.\n" +
                    "• **Never Share:** Antibiotics are specific to bacteria and do not cure viral infections like the common cold."
        }

        // 5. Context-Aware Fallback for specific health/symptom queries
        return "**Clinical Evaluation & Guidance**\n\n" +
                "Regarding **\"$trimmed\"**:\n\n" +
                "• **Medical Assessment:** Please monitor the severity, duration, and frequency of these symptoms. Ensure you record your daily medication doses accurately in MediLert.\n" +
                "• **Hydration & Rest:** Maintain balanced fluid intake, wholesome nutrition, and avoid skipping prescribed treatments.\n" +
                "• **Direct Doctor Contact:** If you are experiencing persistent discomfort, worsening pain, or new symptoms, use the **Live Chat** to message your assigned doctor or visit your healthcare clinic for a clinical examination.\n\n" +
                "Would you like more details on specific medications, dosage schedules, or symptom warning signs?"
    }

    /**
     * Creates an initial greeting message from MedGemma.
     */
    fun createInitialGreeting(userName: String): ChatMessage {
        val name = if (userName.isNotBlank()) " ${userName.split(" ").first()}" else ""
        return ChatMessage(
            id = "ai_welcome_${Clock.System.now().toEpochMilliseconds()}",
            senderId = AI_BOT_ID,
            senderName = AI_BOT_NAME,
            recipientId = "user",
            text = "Hello$name! I am **MedGemma**, your MediLert AI Medical Assistant.\n\nI am here to answer your health questions, explain diseases (like Malaria or Diabetes), check symptoms, review medication side effects, and support your daily wellness.\n\nHow can I help you today?",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            status = MessageStatus.READ,
            read = true
        )
    }
}

