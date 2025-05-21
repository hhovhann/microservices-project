"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [form, setForm] = useState({ username: "", password: "" });
    const [message, setMessage] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleLogin = async () => {
        try {
            const res = await fetch("http://localhost:8081/v1/api/user/authenticate", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ...form, email: "placeholder@email.com" }), // REQUIRED BY BACKEND
            });

            const text = await res.text();
            if (res.ok) {
                localStorage.setItem("token", text);
                router.push("/chat");
            } else {
                setMessage("❌ Failed: " + text);
            }
        } catch (e) {
            setMessage("❌ Network error");
        }
    };

    return (
        <div className="p-4 space-y-2">
            <h2 className="text-xl font-semibold">Login</h2>
            <input name="username" placeholder="Username" onChange={handleChange} className="border p-2 w-full" />
            <input name="password" placeholder="Password" type="password" onChange={handleChange} className="border p-2 w-full" />
            <button onClick={handleLogin} className="bg-green-600 text-white p-2 rounded">Login</button>
            {message && <div className="mt-2 text-sm text-red-600">{message}</div>}
        </div>
    );
}
