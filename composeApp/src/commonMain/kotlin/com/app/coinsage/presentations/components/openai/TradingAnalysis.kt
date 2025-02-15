package com.app.coinsage.presentations.components.openai

import kotlinx.serialization.Serializable

@Serializable
data class TradingAnalysis(
    val results: String? = "",      // "buy" or "sell"
    val entry: Double? = 0.0,        // Entry price for the trade
    val takeProfits: Double? = 0.0,  // Target price for taking profits
    val stopLoss: Double? = 0.0,     // Stop loss level
    val risk: Double? = 0.0,         // Risk percentage per trade
    val win: Double? = 0.0           // Win percentage of the AI strategy
)

val prompt = "Analyze the provided trading chart image and generate a profitable trading recommendation. Your response should follow this JSON format:\n" +
        "\n" +
        "{\n" +
        "  \"results\": \"buy\" or \"sell\",  // Based on AI's trading decision\n" +
        "  \"entry\": (number),           // Suggested entry price\n" +
        "  \"takeProfits\": (number),     // Target price to take profits\n" +
        "  \"stopLoss\": (number),        // Price level to cut losses\n" +
        "  \"risk\": (number),            // Suggested risk percentage per trade\n" +
        "  \"win\": (number)              // Confidence in this trade as a percentage\n" +
        "}\n" +
        "\n" +
        "**Analysis Process:**\n" +
        "1. **Trend Analysis:** Identify if the market is in an uptrend, downtrend, or ranging.\n" +
        "2. **Support & Resistance:** Detect key price levels.\n" +
        "3. **Indicators Used:**\n" +
        "   - **Moving Averages (SMA/EMA):** Check for crossovers.\n" +
        "   - **MACD:** Look for bullish/bearish signals.\n" +
        "   - **RSI:** Identify overbought/oversold conditions.\n" +
        "4. **Risk Management:** Suggest stop loss and take profit levels.\n" +
        "5. **Final Recommendation:** Provide a buy or sell decision with a confidence percentage.\n" +
        "\n" +
        "**Example Response:**\n" +
        "{\n" +
        "  \"results\": \"buy\",\n" +
        "  \"entry\": 150.75,\n" +
        "  \"takeProfits\": 160.00,\n" +
        "  \"stopLoss\": 148.00,\n" +
        "  \"risk\": 2.0,\n" +
        "  \"win\": 80.0\n" +
        "}\n" +
        "\n" +
        "If no clear trade opportunity is found, respond with:\n" +
        "{\n" +
        "  \"results\": \"N/A\",\n" +
        "  \"entry\": null,\n" +
        "  \"takeProfits\": null,\n" +
        "  \"stopLoss\": null,\n" +
        "  \"risk\": null,\n" +
        "  \"win\": null\n" +
        "}\n"