import React from 'react';

export default () => {
  return (
    <div style={styles.footer}>
      {/* <div style={styles.links}>
        <a href="#" style={styles.link}>
          Help
        </a>
        <a href="#" style={styles.link}>
        Privacy
        </a>
        <a href="#" style={{ ...styles.link, marginRight: '0' }}>
        Provision
        </a>
      </div> */}
      <div style={styles.copyright}>Powered by <a href="https://github.com/xaecbd">xaecbd</a></div>
    </div>
  );
};

const styles = {
  footer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'fixed',
    left: '0',
    right: '0',
    bottom: '20px',
  },
  links: {
    marginBottom: '8px',
  },
  link: {
    fontSize: '13px',
    marginRight: '40px',
    color: '#fff',
  },
  copyright: {
    fontSize: '13px',
    color: '#fff',
    lineHeight: 1.5,
    textAlign: 'right',
  },
};
