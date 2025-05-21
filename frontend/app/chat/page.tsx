"use client";

import {useState, useRef, useEffect} from "react";
import {useRouter} from "next/navigation";

export default function ChatPage() {
    const [input, setInput] = useState("");
    const [messages, setMessages] = useState<{ role: string; content: string }[]>([]);
    const [loading, setLoading] = useState(false);
    const [authorized, setAuthorized] = useState<boolean | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const router = useRouter();

    // Scroll chat to bottom on new message
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"});
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        const fetchMe = async () => {
            const token = localStorage.getItem("token");  // get token
            if (!token) {
                window.location.href = "/login";
                return;
            }

            const res = await fetch("http://localhost:8081/v1/api/user/me", {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,  // send token in header
                },
            });

            if (res.ok) {
                const user = await res.json();
                console.log("👤 Current user:", user);
                setAuthorized(true);
            } else {
                console.log("❌ Not logged in or token expired");
                window.location.href = "/login";
            }
        };

        fetchMe();
    }, [router]);

    if (authorized === null) return <p className="p-4">🔐 Checking auth...</p>;

    async function sendMessage() {
        if (!input.trim()) return;

        const newMessage = {role: "user", content: input};
        const updatedMessages = [...messages, newMessage];

        setMessages(updatedMessages);
        setInput("");
        setLoading(true);

        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include", // Send the token cookie
                body: JSON.stringify({messages: updatedMessages}),
            });

            if (response.ok) {
                const data = await response.json();
                if (data.messages) {
                    setMessages(data.messages);
                }
            } else {
                console.error("❌ Backend error:", await response.text());
            }
        } catch (error) {
            console.error("❌ Failed to fetch from backend:", error);

        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="flex flex-col h-screen bg-[#f5f5f5]">
            <div className="flex-1 overflow-y-auto p-6 space-y-4">
                {messages.map((m, i) => (
                    <div
                        key={i}
                        className={`max-w-xl px-4 py-3 rounded-lg ${
                            m.role === "user"
                                ? "bg-blue-100 self-end text-right"
                                : "bg-gray-200 self-start"
                        }`}
                    >
                        <p className="text-sm text-gray-800">{m.content}</p>
                    </div>
                ))}
                <div ref={messagesEndRef}/>
            </div>
            <div className="p-4 border-t bg-black">
                <div className="flex gap-2">
                    <input
                        type="text"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                        className="flex-1 border rounded px-4 py-2 shadow-sm focus:outline-none"
                        placeholder="Ask anything..."
                    />
                    <button
                        onClick={sendMessage}
                        disabled={loading}
                        className="bg-blue-600 text-black px-4 py-2 rounded hover:bg-blue-700 transition disabled:opacity-50"
                    >
                        {loading ? "..." : "Send"}
                    </button>
                </div>
            </div>
        </div>
    );
}
