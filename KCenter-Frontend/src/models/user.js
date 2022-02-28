import { request } from 'ice';

export default {
  state: {
    name: 'default',
    department: '',
    avatar: '',
    userid: null,
  },
  effects: dispatch => ({
    async fetchUserProfile() {
      const res = await request('/api/profile');

      if (res.status === 'SUCCESS') {
        dispatch.user.update(res.data);
      }
    },
  }),
  reducers: {
    update(prevState, payload) {
      return { ...prevState, ...payload };
    },
  },
};
