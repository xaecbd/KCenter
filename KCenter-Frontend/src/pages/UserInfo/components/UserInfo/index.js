import React, { Component } from 'react';
import { Avatar, Card, Form, Input, Message, ResponsiveGrid, Box, Switch } from '@alifd/next';
import axios from '@utils/axios';
import styles from './index.module.scss';
import { withRouter } from 'react-router-dom';


const { Cell } = ResponsiveGrid;
const FormItem = Form.Item;
@withRouter
export default class UserInfo extends Component {
  state={
    formValue: {},
    user: JSON.parse(sessionStorage.getItem('user')),
    enable: sessionStorage.getItem('oauthEnable'),
    updatePassword: false,
  }

 formChange = (values) => {
   this.setState({
     formValue: values,
   });
 }


 componentDidMount() {
   this.fetchData();
 }


 fetchData() {
   const user = this.state.user;
   if (user && user.id) {
     user.pwd = user.password;
     user.password = '';
     this.setState({
       formValue: user,
     });
   } else {
     Message.warning('please login');
   }
 }

    checkPasswd2=(rule, values, callback, stateValues) => {
      if (!values) {
        callback('please input your password');
      } else if (values && values !== this.state.formValue.password) {
        callback('The two input passwords are inconsistent');
      } else {
        callback();
      }
    }

   onSubmit=(values, errors, field) => {
     if (errors) {
       return;
     }
     const value = this.state.formValue;
     axios.put('/users/modifty', value).then((res) => {
       if (res.data.code === 200) {
         sessionStorage.clear();
         Message.success(`${res.data.message},please login again`);
         window.location.href = '/login/logout';
       } else {
         Message.error(res.data.message);
       }
     }).catch((e) => {
       console.log(e);
     });
   }

   onCancel = () => {
     this.props.history.push('/home/page');
   }

   onChange=(checked) => {
     this.setState({
       updatePassword: checked,
     });
   }

   render() {
     const flag = this.state.enable && this.state.formValue.pwd != null && this.state.formValue.pwd != '';
     return (

       <div style={stylese.container}>
         <Card free>
           <Card.Content>
             <Box className={styles.baseSettingContainer}>
               <Form
                 className={styles.baseSetting}
                 value={this.state.formValue}
                 labelAlign="top"
                 onChange={this.formChange}
                 responsive
               >
                 <FormItem colSpan={12}>
                   <ResponsiveGrid gap={10}>
                     <Cell colSpan={2}><Avatar shape="circle" size={64} icon="account" src={this.state.formValue.picture ? this.state.formValue.picture : require('../../images/avatar.png')} /></Cell>
                     <Cell colSpan={10} className={styles.changeLogo} />
                     <Box spacing={12}>
                       <FormItem />
                       <Box>
                         <p className={styles.label}>{this.state.formValue.name}</p>
                       </Box>
                     </Box>
                   </ResponsiveGrid>
                 </FormItem>
                 <FormItem label="Email:" colSpan={12} format="email">
                   <Input placeholder="Please enter your email" id="realName" name="email" />
                 </FormItem>

                 <FormItem label="Real Name:" colSpan={12}>
                   <Input placeholder="Please enter your real name" id="realName" name="realName" />
                 </FormItem>

                 {flag ? <FormItem label="Update Password:" colSpan={12}>
                   <Switch name="updatePwd" checked={this.state.updatePassword} id="updatePwd" onChange={this.onChange} />
                         </FormItem> : null}
                 { this.state.updatePassword ? <FormItem required label="New Password:" colSpan={12} requiredMessage="new password is requried">
                   <Input htmlType="password" placeholder="Please enter your password" id="password" name="password" />
                 </FormItem> : null}
                 {this.state.updatePassword ? <FormItem required label="New Password:" validator={this.checkPasswd2} colSpan={12} requiredMessage="new password is requried">
                   <Input htmlType="password" placeholder="Please enter your password again" id="rePass" name="new password" />
                 </FormItem> : null}

                 <FormItem colSpan={12}>
                   <Box spacing={10} direction="row">
                     <Form.Submit
                       validate
                       type="primary"
                       onClick={this.onSubmit}

                     >Submit
                     </Form.Submit>

                     <Form.Submit
                       type="normal"
                       onClick={this.onCancel}
                       validate
                     >Cancel
                     </Form.Submit>
                   </Box>

                 </FormItem>
               </Form>
             </Box>
           </Card.Content>
         </Card>
       </div>
     );
   }
}

const stylese = {
  container: {
    margin: '20px',
  },
};
