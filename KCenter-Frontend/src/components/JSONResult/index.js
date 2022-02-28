import React from 'react';
import PropTypes from 'prop-types';
import styles from './index.less';
import JSONTree from 'react-json-tree';

function TestResult({ result }) {
  return (
    <div className={styles.testResult}>
      <div className={styles.inner}>
        {result ? (
          <JSONTree
            data={result}
            shouldExpandNode={() => {
              return true;
            }}
          />
        ) : (
          ''
        )}
      </div>
    </div>
  );
}
TestResult.propTypes = {
  result: PropTypes.object
};
export default TestResult;