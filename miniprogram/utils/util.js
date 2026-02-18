const formatDate = (date) => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
};

const sportIcons = {
  running: '🏃',
  walking: '🚶',
  cycling: '🚴',
  swimming: '🏊',
  yoga: '🧘',
  basketball: '🏀',
  badminton: '🏸',
  fitness: '💪'
};

const getSportIcon = (icon) => sportIcons[icon] || '🏅';

module.exports = { formatDate, getGreeting, sportIcons, getSportIcon };
