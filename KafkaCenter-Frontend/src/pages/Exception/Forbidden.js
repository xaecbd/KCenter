import React from 'react';
import Exception from '../../components/Exception';

const Forbidden = () => {
  return (
    <Exception
      statusCode="403"
      image="https://img.alicdn.com/tfs/TB174TvGCzqK1RjSZPcXXbTepXa-260-260.png"
      description="Sorry, you don't have access to this page"
      backText="Back Login"
    />
  );
};

export default Forbidden;
