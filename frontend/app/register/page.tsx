"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function RegisterPage() {
    const router = useRouter();
    const [form, setForm] = useState({ username: "", email: "", password: "" });
    const [message, setMessage] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleRegister = async () => {
        try {
            const res = await fetch("http://localhost:8081/v1/api/user/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form),
            });

            if (res.ok) {
                setMessage("✅ Registered successfully");
                setTimeout(() => router.push("/login"), 1000);
            } else {
                const text = await res.text();
                setMessage("❌ Failed to register: " + text);
            }
        } catch (e) {
            setMessage("❌ Network error");
        }
    };

    return (
        <div className="p-4 space-y-2">
            <h2 className="text-xl font-semibold">User Registration</h2>
            <input name="username" placeholder="Username" onChange={handleChange} className="border p-2 w-full" />
            <input name="email" placeholder="Email" onChange={handleChange} className="border p-2 w-full" />
            <input name="password" placeholder="Password" type="password" onChange={handleChange} className="border p-2 w-full" />
            <button onClick={handleRegister} className="bg-blue-600 text-white p-2 rounded">Register User</button>
            {message && <div className="mt-2 text-sm text-red-600">{message}</div>}
        </div>
    );
}
