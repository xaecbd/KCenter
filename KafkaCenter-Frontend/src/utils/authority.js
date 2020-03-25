// use localStorage to store the authority info, which might be sent from server in actual project.
export function getAuthority() {
  return localStorage.getItem('ice-pro-authority') || 'member';
}

export function setAuthority(authority) {
  return localStorage.setItem('ice-pro-authority', authority);
}

export function removeAuthority() {
  return localStorage.removeItem('ice-pro-authority');
}
