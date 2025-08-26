import React from 'react';
import MarketplaceCredentials from './MarketplaceCredentials.jsx';

export default function YandexCredentials() {
  return (
    <MarketplaceCredentials
      marketplace="YANDEX"
      marketplaceName="Yandex"
      marketplaceIcon="🟡"
      credentialsEndpoint="/api/yandex/credentials"
      fields={[
        { key: 'clientId', label: 'Client ID', placeholder: 'Client ID от Yandex' },
        { key: 'apiKey', label: 'API Key', placeholder: 'API Key от Yandex', type: 'password' },
        { key: 'campaignId', label: 'Campaign ID', placeholder: 'Campaign ID от Yandex' }
      ]}
      readOnlyFields={[
        { key: 'syncStatus', label: 'Статус синхронизации' },
        { key: 'lastSyncAt', label: 'Последняя синхронизация' },
        { key: 'isActive', label: 'Активность' },
        { key: 'errorMessage', label: 'Последняя ошибка' }
      ]}
    />
  );
}

