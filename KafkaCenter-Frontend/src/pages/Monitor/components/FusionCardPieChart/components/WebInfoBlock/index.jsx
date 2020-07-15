import React from 'react';
import { Box, Card } from '@alifd/next';
import styles from './index.module.scss';

const WebInfoBlock = props => {
  const { cardConfig } = props;
  const { value, name, des, rate, isRise } = cardConfig;
  return (
    <Card free className={styles.box}>
      <Card.Content
        style={{
          height: '100%',
        }}
      >
        <Box
          justify="center"
          align="center"
          style={{
            height: '100%',
          }}
        >
          <Box className={styles.content} justify="space-between" align="center">
            <div className={styles.value}>{value}</div>
            <div className={styles.name}>{name}</div>
            <div className={styles.des}>
              {des}
              {isRise ? (
                <span className={styles.rise}>{rate}↑</span>
              ) : (
                <span className={styles.fall}>{rate}↓</span>
              )}
            </div>
          </Box>
        </Box>
      </Card.Content>
    </Card>
  );
};

export default WebInfoBlock;
