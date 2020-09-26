/* eslint-disable class-methods-use-this */
import React, { Component } from 'react';
import { Pagination } from '@alifd/next';


export default class CustomPagination extends Component {
  constructor(props) {
    super(props);
    this.state = {
      dataSource: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      pageList: [10, 50, 100],
    };
  }
  componentDidUpdate(prevProps) {
    if (prevProps.dataSource !== this.props.dataSource) {
      this.initData(this.props.dataSource);
      this.setPageSizeAndPageList();
    } else {
      // 此处逻辑是为了避免引用修改数据造成无法刷新的问题
      // eslint-disable-next-line no-lonely-if
      if (!this.arrayIsEqual(this.state.dataSource, this.props.dataSource)) {
        this.initData(this.props.dataSource);
        this.setPageSizeAndPageList();
      }
    }
  }


  setPageSizeAndPageList(){
    if(this.props.pageList){
      this.setState({
        pageList:this.props.pageList
      });
    }
    if(this.props.pageSize){
      this.setState({
        pageSize:this.props.pageSize
      });
    }
  }

  initData=(value) => {
    const newData = JSON.parse(JSON.stringify(value));
    this.setState({ dataSource: newData, total: newData.length, currentPage: 1 }, () => { this.getPageData(1); });
  }


  arrayIsEqual(arr1, arr2) { // 判断2个数组是否相等
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
  }

  redrawPageData(value) {
    this.props.redrawPageData(value);
  }

  getPageData = (currentPage) => {
    if (Math.ceil(this.state.dataSource.length / this.state.pageSize) < currentPage) {
      currentPage = 1;
      this.setState({ currentPage: 1 });
    }
    const page = (currentPage - 1) * this.state.pageSize;
    const newArray = this.state.dataSource.slice(page, page + this.state.pageSize);

    this.redrawPageData(newArray);
  }

  changePage = (currentPage) => {
    this.setState({ currentPage }, () => {
      this.getPageData(currentPage);
    });
  };

  handlePageSizeChange = (value) => {
    this.setState({ pageSize: value }, () => {
      this.getPageData(this.state.currentPage);
    });
  }

  render() {
    return (
      <div>
        <Pagination
          style={styles.pagination}
          current={this.state.currentPage}
          pageSize={this.state.pageSize}
          total={this.state.total}
          onChange={this.changePage}
          totalRender={total => `Total: ${total}`}
          pageSizeSelector="filter"
          pageSizePosition="end"
          pageSizeList={this.state.pageList}
          onPageSizeChange={this.handlePageSizeChange}
        />
      </div>
    );
  }
}
const styles = {
  pagination: {
    marginTop: '20px',
    textAlign: 'right',
  },
};
