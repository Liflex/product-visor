import React from 'react';

const OzonOrders = () => {
  const [orders, setOrders] = React.useState([]);
  const [from, setFrom] = React.useState('');
  const [to, setTo] = React.useState('');
  const [loading, setLoading] = React.useState(false);

  const load = async () => {
    const r = await fetch('/api/orders?page=0&size=50');
    const j = await r.json();
    setOrders(j.content || []);
  };

  const backfill = async () => {
    setLoading(true);
    try {
      await fetch(`/api/ozon/orders/fbo/backfill?pageSize=100`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ from, to })
      });
      await load();
    } finally { setLoading(false); }
  };

  React.useEffect(() => { load(); }, []);

  return (
    <div className="p-4">
      <h2 className="text-xl text-white mb-4">Ozon Orders</h2>
      <div className="flex items-center space-x-2 mb-4">
        <input type="datetime-local" onChange={e=>setFrom(new Date(e.target.value).toISOString())} />
        <input type="datetime-local" onChange={e=>setTo(new Date(e.target.value).toISOString())} />
        <button onClick={backfill} className="bg-indigo-600 text-white px-3 py-1 rounded" disabled={loading}>
          {loading ? 'Loading...' : 'Backfill'}
        </button>
      </div>
      <table className="min-w-full text-sm">
        <thead>
          <tr className="text-gray-300">
            <th className="px-2 py-1 text-left">Posting</th>
            <th className="px-2 py-1 text-left">Status</th>
            <th className="px-2 py-1 text-left">Created</th>
            <th className="px-2 py-1 text-left">Items</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(o => (
            <tr key={o.postingNumber} className="border-t border-gray-700 text-gray-200">
              <td className="px-2 py-1">{o.postingNumber}</td>
              <td className="px-2 py-1">{o.status}</td>
              <td className="px-2 py-1">{o.createdAt}</td>
              <td className="px-2 py-1">{(o.items||[]).map(i=>i.offerId).join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default OzonOrders;


