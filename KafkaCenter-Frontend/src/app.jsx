import * as React from 'react';
import { createApp } from 'ice';
import LocaleProvider from '@/components/LocaleProvider';
import { getLocale } from '@/utils/locale';

const locale = getLocale();
const appConfig = {
  app: {
    rootId: 'ice-container',
    addProvider: ({ children }) => <LocaleProvider locale={locale}>{children}</LocaleProvider>,
    getInitialData: async () => {
      
      const user = JSON.parse(sessionStorage.getItem('user'));
      if(user==null){
        return {
          auth: {
            role: 'member'
          }
        }
      }
      // 约定权限必须返回一个 auth 对象
      // 返回的每个值对应一条权限
      return {
        auth: {
          role:user.role === 'ADMIN'?'admin':'member',
        }
      }
    },
  },
};
createApp(appConfig);
