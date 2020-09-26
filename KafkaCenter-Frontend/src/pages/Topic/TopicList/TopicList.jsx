import React from 'react';
import IceContainer from '@icedesign/container';
import CustomBreadcrumb from '@components/CustomBreadcrumb';
import TopicTable from './components/TopicTable';

const styles = {
  container: {
    margin: '20px',
    padding: '10px 20px 20px',
    minHeight: '600px',
  },
};
const TopicList=()=>{
  const breadcrumb = [
    {
      link: '',
      text: 'Topic',
    },
    {
      link: '',
      text: 'Topic List',
    },
  ];
  return (
    <div>
      <CustomBreadcrumb items={breadcrumb} title="Topic List" />
      <IceContainer style={styles.container} >
        <TopicTable />
      </IceContainer>
    </div>
  );
}

export default TopicList;
