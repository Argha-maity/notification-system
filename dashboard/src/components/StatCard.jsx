import React from 'react';

export default function StatCard({ title, value, icon: Icon, color, textColor, iconColor, change }) {
  return (
    <div className={`${color} border rounded-lg p-6 backdrop-blur transition-all hover:shadow-md hover:-translate-y-1 cursor-pointer`}>
      <div className="flex justify-between items-start">
        <div>
          <p className="text-gray-600 text-sm mb-2 font-medium">{title}</p>
          <p className={`text-3xl font-bold ${textColor}`}>{value}</p>
          <p className="text-gray-600 text-xs mt-2">{change}</p>
        </div>
        <Icon className={`${iconColor} opacity-20`} size={32} />
      </div>
    </div>
  );
}
