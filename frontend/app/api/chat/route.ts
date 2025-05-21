// app/api/chat/route.ts
import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
    console.log("🔁 API route hit");

    const body = await req.json();
    const messages = body.messages;
    const latestMessage = messages[messages.length - 1];

    try {
        const response = await fetch("http://localhost:8083/v1/api/chat/message", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                userId: "hhovhann",
                message: latestMessage.content,
            }),
        });

        const data = await response.json();
        console.log("🧠 Response from backend:", data);

        return NextResponse.json({
            messages: [...messages, { role: "assistant", content: data.content }],
        });
    } catch (error) {
        console.error("❌ Error hitting Spring Boot backend:", error);
        return NextResponse.json({ error: "Failed to fetch from backend" }, { status: 500 });
    }
}

