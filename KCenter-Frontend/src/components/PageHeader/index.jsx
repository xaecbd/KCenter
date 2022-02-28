import React from 'react';
import { Breadcrumb, Box, Typography } from '@alifd/next';
import styles from './index.module.scss';

const PageHeader = props => {
  const { breadcrumbs, title, description, ...others } = props;
  return (
    <Box spacing={8} className={styles.PageHeader} {...others}>
      {breadcrumbs && breadcrumbs.length > 0 ? (
        <Breadcrumb className={styles.Breadcrumbs} separator=" / ">
          {breadcrumbs.map(item => (
            <Breadcrumb.Item link={item.path}>{item.name}</Breadcrumb.Item>
          ))}
        </Breadcrumb>
      ) : null}

      {title && <Typography.Text className={styles.Title}>{title}</Typography.Text>}

      {description && (
        <Typography.Text className={styles.Description}>{description}</Typography.Text>
      )}
    </Box>
  );
};

export default PageHeader;
