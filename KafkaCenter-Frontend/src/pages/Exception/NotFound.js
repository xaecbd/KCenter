import React from 'react';
import Exception from '../../components/Exception';

const NotFound = () => {
  return (
    <Exception
      statusCode="400"
      image="https://img.alicdn.com/tfs/TB1BJ_3GxTpK1RjSZFKXXa2wXXa-260-260.png"
      description="Sorry, the page you visited is empty"
      backText="Back Login"
    />
  );
};

export default NotFound;
