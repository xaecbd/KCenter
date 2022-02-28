import React, { useState } from 'react';
import { Icon, Badge, Overlay, Avatar, Message, List } from '@alifd/next';
import styles from './index.module.scss';

const { Popup } = Overlay;
const defaultNoticeList = [
  {
    id: 1,
    name: 'Aric',
    avatar: 'https://img.alicdn.com/tfs/TB1.ZBecq67gK0jSZFHXXa9jVXa-904-826.png',
    message: '新标识怎么去掉？',
  },
  {
    id: 2,
    name: 'Mark',
    avatar: 'https://img.alicdn.com/tfs/TB1.ZBecq67gK0jSZFHXXa9jVXa-904-826.png',
    message: '如何查看新增页面？',
  },
];

const Notice = ({ noticeList }) => {
  const [badgeCount, setBageCount] = useState(2);
  const [readList, setReadList] = useState([]);

  function markAsRead(id) {
    setReadList([...readList, id]);
    setBageCount(badgeCount - 1);
  }

  function clearNotice() {
    const noticeIds = noticeList.map(item => item.id);
    setBageCount(0);
    setReadList(noticeIds);
  }

  function viewMore() {
    Message.success('点击了查看更多操作');
  }

  const renderList = noticeList.filter(item => readList.indexOf(item.id) === -1);
  return (
    <Popup
      trigger={
        <div className={styles.noticeIcon}>
          <Badge count={badgeCount}>
            <Icon type="email" />
          </Badge>
        </div>
      }
      triggerType="click"
    >
      <List
        size="small"
        divider={false}
        className={styles.noticeContainer}
        header={
          <div className={styles.title}>
            <h4>通知</h4>
            <span className={styles.clear} onClick={clearNotice}>
              清空通知
            </span>
          </div>
        }
        footer={
          <div className={styles.footer}>
            <a onClick={viewMore}>查看更多</a>
          </div>
        }
      >
        {renderList.map(noticeItem => {
          const { id, name, avatar, message } = noticeItem;
          return (
            <List.Item
              className={styles.noticeItem}
              key={id}
              title={name}
              media={<Avatar size={32} src={avatar} alt="avatar" />}
              extra={
                <span className={styles.close} onClick={() => markAsRead(id)}>
                  <Icon type="close" size="xs" />
                </span>
              }
            >
              {message}
            </List.Item>
          );
        })}
        {renderList.length === 0 && (
          <List.Item className={styles.empty}>你已查看所有通知</List.Item>
        )}
      </List>
    </Popup>
  );
};

Notice.defaultProps = {
  noticeList: defaultNoticeList,
};
export default Notice;
