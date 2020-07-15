import * as React from 'react';
import { Icon } from '@alifd/next';
import { Link } from 'ice';
import styles from './index.module.scss';

const SolutionLink = () => (
  <div className={styles.link}>
    <Link to="/solution" title="官方推荐方案">
      <Icon type="smile" />
    </Link>
  </div>
);

export default SolutionLink;
