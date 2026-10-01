export const ROLE_DESTINATIONS = {
  ADMIN: '/admin',
  MODERATOR: '/moderation',
  SELLER: '/seller',
  BUYER: '/buyer',
};

export const ROLE_LABELS = {
  ADMIN: 'Administrator',
  MODERATOR: 'Moderator',
  SELLER: 'Seller',
  BUYER: 'Buyer',
};

const ROLE_PRIORITY = ['ADMIN', 'MODERATOR', 'SELLER', 'BUYER'];

export function getPrimaryRole(account) {
  const roles = Array.isArray(account?.roles) ? account.roles : [];
  return ROLE_PRIORITY.find((role) => roles.includes(role)) || null;
}

export function getDefaultRoute(account) {
  return ROLE_DESTINATIONS[getPrimaryRole(account)] || '/sign-in';
}

export function hasAnyRole(account, allowedRoles = []) {
  if (allowedRoles.length === 0) return Boolean(account);
  const roles = Array.isArray(account?.roles) ? account.roles : [];
  return allowedRoles.some((role) => roles.includes(role));
}

export function getPersonaLabels(account) {
  return (account?.personas || []).map((item) => item.persona);
}
