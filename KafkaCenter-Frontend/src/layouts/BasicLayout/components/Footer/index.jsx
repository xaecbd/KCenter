import React from 'react';
import styles from './index.module.scss';

export default function Footer() {
  return (
    <p className={styles.footer}>
      <span className={styles.logo}>Kafka Center</span>
      <br />
      <span className={styles.copyright}> © 2020 {'   '}
        <a
          href="https://github.com/xaecbd/KafkaCenter/graphs/contributors"
          target="_blank"
          className={styles.copyrightlink}
          rel="noopener noreferrer"
        >
              xaecbd Contributors
        </a></span>
    </p>
  );
}
