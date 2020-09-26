import numeral from 'numeral';

export function transToHours(miss){
  if(miss>0){
    const mss = parseFloat(miss);
    const hours = mss/(60*60*1000);
    if(!Number.isInteger(hours)){
      return hours.toFixed(2);
    }
    return hours;
    
  }else{
    return 0;
  }
 
}

export function resturctData (data,containsAll) {
  const result = [];
  data.toString().split(',').map((key) => {
    result.push({
      value: key,
      label: key,
    });
  });
  if(containsAll){
    result.push({
      value: 'ALL',
      label: 'ALL',
    });
  }
  return result;
}


export function transToNumer(num) {
  if(num!== null && num!=='null'){
    const val = parseFloat(num.toString());
    if (val < 1000) {
      return numeral(val).format('0a');
    }
    return numeral(val).format('0.00a');
  }else{
    return '-';
  }
  
}

// 格式化字节单位
export function formatSizeUnits(byte, index, record) {
  if (byte === 0) {
    return 0;
  }
  if (record !== '') {
    if (record.metricName === 'MessagesInPerSec' || record.metricName === 'FailedProduceRequestsPerSec' || record.metricName === 'FailedFetchRequestsPerSec') {
      return transToNumer(byte);
    }
    return numeral(byte).format('0.00b');
  }
}

export function bytesToSize(bytes) {
  if (bytes === 0) return '0 B';
  return numeral(bytes).format('0.00b');
}

// 千位分隔
export function thousandSplit(num) {
  return numeral(num).format('0,0');
}

export function sortData(data, type) {
  let dataSource = [];
  dataSource = data.sort((a, b) => {
    a = a[type];
    b = b[type];
    return a.localeCompare(b);
  });
  return dataSource;
}



export function sortDataByOrder(data, type, order) {
  const dataSource = data.sort((a, b) => {
    a = a[type];
    b = b[type];
    if (order === 'asc') {
      return a.localeCompare(b);
    }
    return b.localeCompare(a);
  });
  return dataSource;
}
/**
 * 根据不同数据类型排序,调用请保证数据不存在undefined
 * @param {*} data 
 * @param {*} type 
 * @param {*} order 
 */
export function sort(data, value, order) {
  
  const dataSource = data.sort((a, b) => {
    const x = a[value];
    const y = b[value];
   
    if(typeof x==='number'){
      if (order&&order === 'asc') {
        return x - y;
      }else{
        return y - x;
      }
    }else{
      if (order&&order === 'asc') {
        return x.localeCompare(y);
      }
      return y.localeCompare(x);
    }  
  });
  return dataSource;
}
