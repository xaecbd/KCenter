import React from 'react';
import { useAuth } from 'ice';

function Auth({ children, rolename }) {
  const [auth] = useAuth();
  // 有权限时直接渲染内容
  if (rolename===auth.role) {
    return children;
  } else {
    // 无权限时显示指定 UI
    return(<div/>);
  }
};

export default Auth;