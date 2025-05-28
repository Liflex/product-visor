import React from 'react';

const Home = () => {
    return (
        <div className="bg-star-pattern bg-cover bg-center bg-no-repeat min-h-screen flex items-center justify-center">
            <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-4xl">
                <h2 className="text-2xl font-bold text-white mb-6">Главная страница</h2>
                <p className="text-gray-300">Добро пожаловать на главную страницу аудита товаров.</p>
            </div>
        </div>
    );
};

export default Home;