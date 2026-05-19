import React from "react";
import { Link } from "react-router";
import { motion } from "framer-motion";
import { Dog, Shield, Wind, Utensils, Heart, ChevronRight } from "lucide-react";

export function Landing() {
    return (
        <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
            {/* Navbar */}
            <header className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center">
                            <Dog className="w-5 h-5 text-white" />
                        </div>
                        <span className="font-bold text-xl text-gray-900 tracking-tight">펫하우스</span>
                    </div>
                    <div className="flex items-center gap-3">
                        <Link to="/auth" className="text-sm font-medium text-gray-600 hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors">
                            로그인
                        </Link>
                        <Link to="/auth" className="text-sm font-medium bg-gray-900 text-white px-4 py-2 rounded-xl hover:bg-gray-800 transition-colors">
                            회원가입
                        </Link>
                    </div>
                </div>
            </header>

            {/* Hero Section */}
            <section className="flex-1 flex flex-col justify-center py-20 lg:py-32 relative overflow-hidden">
                {/* Background blobs */}
                <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[600px] opacity-30 pointer-events-none">
                    <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-400 rounded-full blur-3xl mix-blend-multiply opacity-50" />
                </div>

                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 w-full">
                    <div className="grid lg:grid-cols-2 gap-12 items-center">
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.6 }}
                            className="max-w-2xl"
                        >
                            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-50 border border-blue-100 text-blue-600 text-sm font-semibold mb-6">
                                <span className="relative flex h-2 w-2">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500"></span>
                                </span>
                                스마트 IoT 펫케어 솔루션
                            </div>
                            <h1 className="text-5xl lg:text-6xl font-bold text-gray-900 leading-tight mb-6 tracking-tight">
                                반려동물의 매일을<br />
                                <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">
                                    더 건강하고 행복하게
                                </span>
                            </h1>
                            <p className="text-lg text-gray-600 mb-8 leading-relaxed">
                                실시간 환경 모니터링부터 자동 급식, 스마트 환기, 그리고 음성 분석까지.
                                집을 비워도 안심할 수 있는 완벽한 스마트 펫하우스 환경을 구축하세요.
                            </p>
                            <div className="flex items-center gap-4">
                                {/* For demonstration purposes, this goes to /dashboard */}
                                <Link
                                    to="/auth"
                                    className="inline-flex items-center gap-2 bg-blue-600 text-white px-6 py-3.5 rounded-2xl text-base font-semibold hover:bg-blue-700 hover:shadow-lg hover:shadow-blue-500/30 transition-all"
                                >
                                    무료로 시작하기
                                    <ChevronRight className="w-5 h-5" />
                                </Link>
                                <button className="px-6 py-3.5 rounded-2xl text-base font-semibold text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 transition-colors">
                                    기능 둘러보기
                                </button>
                            </div>
                        </motion.div>

                        <motion.div
                            initial={{ opacity: 0, scale: 0.9 }}
                            animate={{ opacity: 1, scale: 1 }}
                            transition={{ duration: 0.8, delay: 0.2 }}
                            className="relative"
                        >
                            <div className="aspect-[4/3] rounded-3xl overflow-hidden shadow-2xl shadow-blue-900/10 border-8 border-white bg-white">
                                <img
                                    src="https://images.unsplash.com/photo-1712746438867-0bdba91a4bf6?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydCUyMHBldCUyMGhvdXNlfGVufDF8fHx8MTc3Nzg1MjYyNHww&ixlib=rb-4.1.0&q=80&w=1080"
                                    alt="Smart Pet House"
                                    className="w-full h-full object-cover"
                                />
                            </div>

                            {/* Floating feature card */}
                            <motion.div
                                animate={{ y: [0, -10, 0] }}
                                transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
                                className="absolute -bottom-6 -left-6 bg-white p-4 rounded-2xl shadow-xl border border-gray-100 flex items-center gap-4"
                            >
                                <div className="w-12 h-12 bg-green-50 rounded-xl flex items-center justify-center">
                                    <Wind className="w-6 h-6 text-green-500" />
                                </div>
                                <div>
                                    <div className="text-sm text-gray-500 font-medium">현재 온도 / 습도</div>
                                    <div className="text-lg font-bold text-gray-900">24°C / 45%</div>
                                </div>
                            </motion.div>
                        </motion.div>
                    </div>
                </div>
            </section>

            {/* Features Grid Section */}
            <section className="py-24 bg-white">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="text-center max-w-2xl mx-auto mb-16">
                        <h2 className="text-3xl font-bold text-gray-900 mb-4">
                            모든 것을 앱 하나로 관리하세요
                        </h2>
                        <p className="text-gray-600 text-lg">
                            펫하우스는 반려동물의 생활 환경을 모니터링하고 제어하는데 필요한 모든 기능을 제공합니다.
                        </p>
                    </div>

                    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                        <FeatureCard
                            icon={<Shield className="w-6 h-6 text-blue-500" />}
                            title="실시간 상태 모니터링"
                            description="언제 어디서든 온도, 습도, 펫하우스 내부 상태를 확인하고 안전하게 보호하세요."
                            color="blue"
                        />
                        <FeatureCard
                            icon={<Utensils className="w-6 h-6 text-orange-500" />}
                            title="스마트 급여 / 급수"
                            description="스케줄에 맞춘 자동 급식과 신선한 물 공급으로 건강한 식습관을 유지합니다."
                            color="orange"
                        />
                        <FeatureCard
                            icon={<Wind className="w-6 h-6 text-teal-500" />}
                            title="지능형 환기 시스템"
                            description="설정한 기준 온도에 따라 자동으로 환풍기 강도를 조절하여 쾌적한 환경을 제공합니다."
                            color="teal"
                        />
                        <FeatureCard
                            icon={<Heart className="w-6 h-6 text-rose-500" />}
                            title="음성 분석 & 케어"
                            description="반려동물의 짖음이나 소리를 분석하여 스트레스나 불안 상태를 파악합니다."
                            color="rose"
                        />
                        <FeatureCard
                            icon={<svg className="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>}
                            title="내 근처 동물병원"
                            description="응급 상황 시 현재 위치를 기반으로 가장 가까운 동물병원을 빠르게 검색하세요."
                            color="indigo"
                        />
                        <FeatureCard
                            icon={<Dog className="w-6 h-6 text-purple-500" />}
                            title="다중 하우스 관리"
                            description="여러 마리의 반려동물을 위한 각각의 펫하우스를 하나의 계정으로 통합 관리합니다."
                            color="purple"
                        />
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="bg-gray-50 py-12 border-t border-gray-200 mt-auto">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
                    <div className="flex items-center gap-2">
                        <Dog className="w-5 h-5 text-gray-400" />
                        <span className="font-semibold text-gray-600">펫하우스</span>
                    </div>
                    <div className="text-sm text-gray-500">
                        © 2026 PetHouse Inc. All rights reserved.
                    </div>
                </div>
            </footer>
        </div>
    );
}

function FeatureCard({ icon, title, description, color }: { icon: React.ReactNode, title: string, description: string, color: string }) {
    const colorMap: Record<string, string> = {
        blue: "bg-blue-50 border-blue-100",
        orange: "bg-orange-50 border-orange-100",
        teal: "bg-teal-50 border-teal-100",
        rose: "bg-rose-50 border-rose-100",
        indigo: "bg-indigo-50 border-indigo-100",
        purple: "bg-purple-50 border-purple-100",
    };

    return (
        <div className="p-6 rounded-2xl bg-white border border-gray-100 hover:shadow-xl hover:shadow-gray-200/50 transition-all duration-300 group cursor-default">
            <div className={`w-14 h-14 rounded-xl flex items-center justify-center mb-6 transition-transform group-hover:scale-110 ${colorMap[color]}`}>
                {icon}
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-3">{title}</h3>
            <p className="text-gray-600 leading-relaxed">
                {description}
            </p>
        </div>
    );
}