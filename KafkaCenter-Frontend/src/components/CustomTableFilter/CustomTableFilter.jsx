import React, { Component } from 'react';
import { Grid, Select, Input, Message, Switch, Icon } from '@alifd/next';
import axios from '@utils/axios';
import { setPersonalityCluster, getPersonalityCluster } from '@utils/cookies';

const { Row, Col } = Grid;
export default class CustomTableFilter extends Component {
  constructor(props) {
    super(props);
    if (this.props.onRef !== undefined) {
      this.props.onRef(this);
    }
    const searchValue = sessionStorage.getItem(`${this.props.id}Search`);
    const switchValue = sessionStorage.getItem(`${this.props.id}Switch`);
    this.state = {
      dataSource: this.props.dataSource,
      // select 框的默认数据
      clusterSelectData: [],
      // select 框选中的值
      clusterValue: getPersonalityCluster(this.props.id).cluster,
      // cluster Field
      clusterField: this.props.selectField,

      // filter 输入的值
      searchValue: searchValue === null ? '' : searchValue,
      searchField: this.props.searchField,

      switchField: this.props.switchField,
    
      switchValue: switchValue === null || switchValue === ''? false : switchValue,

    };
  }

  componentWillMount() {
    this.mounted = true;
  //  this.onSwitchChange(false);
  }



  componentDidMount() {
   
    this.getCluster();
    
  }


  //   shouldComponentUpdate(nextProps, nextState){
  //       console.log('props:'+nextProps.dataSource.length);
  //       console.log('state:'+nextState.dataSource.length);
  //       if(!this.arrayIsEqual(nextProps.dataSource,nextState.dataSource)){
  //         return true;
  //       }
  //       this.onSwitchChange();
  //       return false;
  //   }

  componentWillUnmount = () => {
    this.mounted = false;
  }


  componentWillReceiveProps(nextProps) {
    if (this.validateCondition(this.state.dataSource, nextProps.dataSource)) {
      this.setState({
        dataSource: nextProps.dataSource,
      });
      this.initData(nextProps.dataSource);
    }
  }


  validateCondition(oldDataSource, newDataSource) {
    if (oldDataSource == null && newDataSource == null) {
      return false;
    }
    if (typeof (oldDataSource) === 'undefined' && typeof (newDataSource) === 'undefined') {
      return false;
    }
    if (oldDataSource.length === 0 && newDataSource.length === 0) {
      return false;
    }
    // 数组内容相同也不需要重新渲染，避免死循环
    if (this.arrayIsEqual(oldDataSource, newDataSource)) {
      return false;
    }
    // 因为每次重新渲染页面都是重新克隆对象，因此这个条件在数据变更后一直成立
    if (oldDataSource !== newDataSource) {
      return true;
    }
    return false;
  }


  arrayIsEqual = (arr1, arr2) => {
    // 判断2个数组是否相等
    if (arr1 === arr2) {
      // 如果2个数组对应的指针相同，那么肯定相等，同时也对比一下类型
      return true;
    }
    if (arr1.length !== arr2.length) {
      return false;
    }
    for (let i = 0; i < arr1.length; i += 1) {
      const var1 = JSON.stringify(arr1[i]);
      const var2 = JSON.stringify(arr2[i]);
      if (var1.localeCompare(var2) !== 0) {
        return false;
      }
    }
    return true;
  };

  refreshTableData = (value) => {
    this.props.refreshTableData(value);
  };

  getCluster = () => {
    const tempList = [
      {
        value: '-1',
        label: 'ALL',
      },
    ];
    axios
      .get('/cluster')
      .then((response) => {
        if (response.data.code === 200) {
          let minClusterValue = response.data.data[0].id;
          let clusterName = response.data.data[0].name;
          let clusterValue = this.state.clusterValue;
          response.data.data.forEach((cluster) => {
            if (cluster.id < minClusterValue) {
              minClusterValue = cluster.id;
              clusterName = cluster.name;
            }
            tempList.push({ value: cluster.id, label: cluster.name });
          });

          const ids = getPersonalityCluster(this.props.id).id;
          if (ids !== undefined && ids !== null) {
            this.props.refreshDataSource(ids);
          } else {
            this.props.refreshDataSource(minClusterValue);
          }
          if (clusterValue === undefined) {
            if (this.props.id !== 'settingUser' && this.props.id !== 'settingTeam') {
              const clusters = { id: minClusterValue, cluster: clusterName, isAll: false };
              setPersonalityCluster(this.props.id, clusters);
              clusterValue = clusterName;
            }
          }

          this.setState({
            clusterSelectData: tempList,
            clusterValue,
          });
        }
      })
      .catch((error) => {
        console.error('error', error);
        if ( error.response !== undefined && error.response.status !== undefined) {
          if (error.response.status !== '' && error.response.status != null && error.response.status != undefined) {
            if (error.response.status === 401) {
              Message.error({
                content: 'Please login!',
                //   duration: 10000,
                closeable: true,
              });
            } else {
              Message.error({
                content: 'get cluster faily!',
                duration: 10000,
                closeable: true,
              });
            }
          }
        }
      });
  };


  /**
   * cluster filter
   */
  handleClusterFilterChange = (clusterValue, item) => {
    this.props.refreshDataSource(item.value);
    const dataSource = this.state.dataSource;
    clusterValue = item.label.toString();
    const clusters = { id: item.value, cluster: item.label, isAll: false };
    if (item.value === '-1') {
      clusters.isAll = true;
    }
    setPersonalityCluster(this.props.id, clusters);
    const filterData = this.filterByPara(
      dataSource,
      this.state.clusterField,
      clusterValue,
      this.state.searchField,
      this.state.searchValue,
      this.state.switchField,
      this.state.switchValue
    );
    this.setState({
      clusterValue,
    });
    this.refreshTableData(filterData);
  };

  /**
   * filter
   */
  handleFilterChange = (searchValue) => {
    searchValue = searchValue.toString();
    const dataSource = this.state.dataSource;
    const filterData = this.filterByPara(
      dataSource,
      this.state.clusterField,
      this.state.clusterValue,
      this.state.searchField,
      searchValue,
      this.state.switchField,
      this.state.switchValue
    );
    this.setState({
      searchValue,
    });
    this.refreshTableData(filterData);
    this.setSesion(`${this.props.id}Search`, searchValue);
  };

  /**
   * switch
   */
  onSwitchChange = (switchValue) => {
    const dataSource = this.state.dataSource;
    const filterData = this.filterByPara(
      dataSource,
      this.state.clusterField,
      this.state.clusterValue,
      this.state.searchField,
      this.state.searchValue,
      this.state.switchField,
      switchValue
    );
    this.setState({
      switchValue,
    });
    this.refreshTableData(filterData);
    this.setSesion(`${this.props.id}Switch`, switchValue);
  };

  setSesion = (key, value) => {
    if (key !== undefined) {
      sessionStorage.setItem(key, value);
    }
  };

  /**
   * 初始化数据
   */
  initData = (dataSource) => {
    // const dataSource = this.state.dataSource;
    const newData = JSON.parse(JSON.stringify(dataSource));

    const filterData = this.filterByPara(
      newData,
      this.state.clusterField,
      this.state.clusterValue,
      this.state.searchField,
      this.state.searchValue,
      this.state.switchField,
      this.state.switchValue
    );
    this.setState(
      {
        dataSource: newData,
      }
    );
    this.refreshTableData(filterData);
  };

  /**
   * cluster value change or filter value change
   */
  filterByPara = (
    dataSource,
    clusterField,
    clusterValue,
    searchField,
    searchValue,
    switchField,
    switchValue
  ) => {
    let result = JSON.parse(JSON.stringify(dataSource));
    if (!this.isNullOrEmptyStr(switchField)) {
      if (!switchValue || switchValue === 'false') {
        result = result.filter(v => !v[switchField].startsWith('_'));
      }
    }
    
    if(!this.isNullOrEmptyStr(clusterField)){
      if (!this.isNullOrEmptyStr(clusterValue) && clusterValue !== 'ALL' ) {
        result = this.searchdata(result, clusterField, clusterValue);
      }
    }
    
    if (!this.isNullOrEmptyStr(searchValue)) {
      result = this.searchdata(result, searchField, searchValue);
    }
    return result;
  };

  /**
   * 根据数组中对象的某个属性值进行搜索
   */
  searchdata = (data, filterField, filterValue) => {
    const filterFields = filterField.split(',');
    return data.filter((v) => {
      let satisfyCondition = false;
      for (let i = 0, len = filterFields.length; i < len; i += 1) {
        const fields = filterFields[i].split('.');
        let fieldValue = v;
        for (let j = 0, lenj = fields.length; j < lenj; j += 1) {
          fieldValue = fieldValue[fields[j]];
        }
        if (
          fieldValue
            .toLocaleLowerCase()
            .search(filterValue.toLocaleLowerCase()) !== -1
        ) {
          satisfyCondition = true;
          break;
        }
      }
      return satisfyCondition;
      // v[filterField].toLocaleLowerCase().search(filterValue.toLocaleLowerCase()) !== -1
    });
  };

  isNullOrEmptyStr = (value) => {
    if (value === null || value === undefined || value === '') {
      return true;
    }
    return false;
  };

  selectView = (selectTitle) => {
    return (
      <Col align="center">
        <span style={{ fontWeight: '600' }}>{selectTitle}:&nbsp;&nbsp;&nbsp;</span>
        <Select
          showSearch
          dataSource={this.state.clusterSelectData}
          placeholder="please select cluster"
          style={{ width: '300px' }}
          onChange={(value, type, item) => {
            this.handleClusterFilterChange(value, item);
          }}
          value={this.state.clusterValue}
        />
      </Col>
    );
  };

  filterView = (filterTitle, filterPlaceholder) => {
    return (
      // <Col align="center" offset="10" style={{ marginLeft: '30%' }}>
      <Col align="center">
        <span style={{ fontWeight: '600' }}>{filterTitle}:&nbsp;&nbsp;&nbsp;</span>
        <Input
          placeholder={
            this.isNullOrEmptyStr(filterPlaceholder)
              ? 'Input filter value'
              : filterPlaceholder
          }
          hasClear
          onChange={this.handleFilterChange}
          style={{ width: '300px' }}
          value={this.state.searchValue}
        />
      </Col>
    );
  };

  parseBoolean = (value) => {
    if (value === 'true' || value === 'TRUE' || value === 'True') {
      return true;
    }
    if (value === 'false' || value === 'FALSE' || value === 'False') {
      return false;
    }
  };

  getFilterData = (dataSource) => {
    const newData = JSON.parse(JSON.stringify(dataSource));
    const filterData = this.filterByPara(newData, this.state.clusterField, this.state.clusterValue, this.state.searchField, this.state.searchValue, this.state.switchField, this.state.switchValue);
    return filterData;
  }

  switchView = () => {
    let value = this.state.switchValue;
    if (typeof this.state.switchValue === 'string') {
      value = this.parseBoolean(this.state.switchValue.trim());
    }
    return (
      <Col align="center">
        <div style={{ display: 'flex' }}>
          <span style={styles.special}>Include special topic:&nbsp;&nbsp;</span>
          <Switch
            onChange={this.onSwitchChange}
            checked={value}
            defaultChecked={value}
            checkedChildren={<Icon type="select" size="xs" />} // true
            unCheckedChildren={<Icon type="close" size="xs" />} // false
          />
        </div>
      </Col>
    );
  };

  render() {
    const selectTitle = this.props.selectTitle;
    const filterTitle = this.props.searchTitle;
    const filterPlaceholder = this.props.searchPlaceholder;
    const selectView = this.isNullOrEmptyStr(selectTitle)
      ? ''
      : this.selectView(selectTitle);
    const filterView = this.isNullOrEmptyStr(filterTitle)
      ? ''
      : this.filterView(filterTitle, filterPlaceholder);
    const otherComponent = this.isNullOrEmptyStr(this.props.otherComponent)
      ? ''
      : this.props.otherComponent;
    const switchView = this.isNullOrEmptyStr(this.props.switchField)
      ? ''
      : this.switchView();
    return (
      <Row style={styles.row}>
        {selectView}
        {filterView}
        {otherComponent}
        {switchView}
      </Row>
    );
  }
}

const styles = {
  row: {
    margin: '20px 4px 20px',
  },
  special: {
    fontFamily:
      'Roboto, "Helvetica Neue", Helvetica, Tahoma, Arial, "PingFang SC", "Microsoft YaHei"',
    fontSize: '12px',
    lineHeight: '1.28571',
    color: '#333333',
    alignSelf: 'center',
    fontWeight: '600',
  },
};
