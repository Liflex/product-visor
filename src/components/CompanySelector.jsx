import React, { useEffect, useState } from 'react';
import { companyService } from '../services/companyService.js';

export default function CompanySelector({ onSelected }) {
  const [companies, setCompanies] = useState([]);
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await companyService.getAllCompanies();
      setCompanies(data || []);
    } catch (err) {
      setError(err.message || 'Ошибка загрузки компаний');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    try {
      const created = await companyService.createCompany({ name });
      setCompanies(prev => [...prev, created]);
      setName('');
    } catch (err) {
      setError(err.message || 'Ошибка создания компании');
    }
  };

  const handleSelect = async (id) => {
    try {
      await companyService.switchToCompany(id);
      onSelected?.(id);
    } catch (err) {
      setError(err.message || 'Ошибка выбора компании');
    }
  };

  if (loading) return <div className="p-4">Загрузка компаний...</div>;

  return (
    <div className="max-w-xl mx-auto mt-6 p-4 bg-white rounded shadow">
      <h2 className="text-lg font-semibold mb-3">Выбор компании</h2>
      {error && <div className="text-red-600 mb-2">{error}</div>}
      {companies.length === 0 && (
        <div className="mb-3">У вас пока нет компаний. Создайте новую:</div>
      )}
      <div className="flex gap-2 mb-4">
        <input className="flex-1 border p-2" placeholder="Название компании" value={name} onChange={e=>setName(e.target.value)} />
        <button className="bg-green-600 text-white px-3 py-2 rounded" onClick={handleCreate}>Создать</button>
      </div>
      {companies.length > 0 && (
        <ul className="divide-y">
          {companies.map(c => (
            <li key={c.id} className="py-2 flex items-center justify-between">
              <span>{c.name}</span>
              <button className="bg-indigo-600 text-white px-3 py-1 rounded" onClick={() => handleSelect(c.id)}>Выбрать</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}




