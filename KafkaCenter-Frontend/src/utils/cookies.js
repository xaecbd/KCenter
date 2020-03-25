/* eslint-disable import/prefer-default-export */
const Cookies = require('js-cookie');
const dayjs = require('dayjs');
// cluster:{id:1,cluster:E11Kafka}
export function setPersonalityCluster(moduleName, cluster) {
  let cookie = getPersonalityCookie();

//   if (getPersonalityCluster(moduleName).cluster === cluster.cluster) {
//     return;
//   }

  if (cookie == null || typeof (cookie) === 'undefined') {
    cookie = {};
  }

  cookie[moduleName] = cluster;
  const data = dayjs().add(1, 'day').toDate();
  Cookies.set('personalityQuery', cookie,{
    path: '/',
    expires: data,   
  });
}

export function getPersonalityCluster(moduleName) {
  const cookie = getPersonalityCookie();
 // let cluster = {cluster:'ALL',id:-1};.
 let cluster = {};
  if (cookie == null || typeof (cookie) === 'undefined') {
    return cluster;
  }
  if (cookie[moduleName] == null || typeof (cookie[moduleName]) === 'undefined') {
    return cluster;
  }
  try {
    return cookie[moduleName];
  } catch (error) {
    console.error(error);
    return cluster;
  }
}

export function setUrl(name,url) {
    try {
     const data = dayjs().add(3, 'minute').toDate();
     Cookies.set(name, url,{
        path: '/',
        expires: data,
      });
    } catch (error) {
      console.error(error);
      return '';
    }
  }

  export function removeUrl(name) {
    try {
     // return cookie;
      Cookies.remove(name);
    } catch (error) {
      console.error(error);
      return '';
    }
  }

  export function getUrlCookie(name) {
    try {
      const json = Cookies.get(name);
      if(json ===  '' || json===undefined){
          json = '/#/home/page';
          Cookies.set(name, json,{
            path: '/',
            maxAge: 60*1
          });
      }      
      return json;
    } catch (error) {
      return null;
    }
  }

export function getPersonalityCookie() {
  try {
    const json = JSON.parse(Cookies.get('personalityQuery'));
    return json;
  } catch (error) {
    return null;
  }
}
