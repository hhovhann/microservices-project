"use client";

import { useRouter } from "next/navigation";

export default function HomePage() {
    const router = useRouter();

    return (
        <div className="h-screen flex items-center justify-center bg-gradient-to-br from-gray-100 to-blue-200">
            <div className="text-center p-8 bg-white rounded-2xl shadow-xl space-y-6 max-w-md w-full">
                <h1 className="text-3xl font-bold text-gray-800">Welcome to Microservices Project ChatBuddy 💬</h1>
                <p className="text-gray-600">A simple chat app with authentication.</p>

                <div className="flex flex-col gap-4">
                    <button
                        onClick={() => router.push("/login")}
                        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
                    >
                        Login
                    </button>
                    <button
                        onClick={() => router.push("/register")}
                        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
                    >
                        Register
                    </button>
                </div>
            </div>
        </div>
    );
}

