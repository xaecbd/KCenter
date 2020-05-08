# 2.1.0 (2020-5-207)
### Features / Enhancements
* support jdk1.8
* add ci support
* fix bug
# 2.0.0 (2020-3-25)
### Features / Enhancements
* add ksql feature
* add oauth2 feature
* refactor monitor module
# 1.5.5.2 (2020-2-18)
### Bug Fixes
* **普通用户首次登陆系统，查看team报错**
* **kafkamanager---删除group，绑定的alert仍会发邮件通知**
* **增删cluster时，增加校验**
* **删除Team时，增加安全性校验**
* **topic Queries 操作完成后，数据需要被清除**

# 1.5.5.1 (2020-1-14)
### Bug Fixes
* **disable alert 名称**

# 1.5.5 (2020-1-11)
### Features / Enhancements
* **Alter添加是否禁用发送alerta选项，mailTo修改为可选项**
### Bug Fixes
* **修复group状态判断**

# 1.5.4 (2019-12-28)
### Features / Enhancements
* **task增加审批人信息**
* **添加cookie过期时间**
* **修改consumer图标**
* **cluster页面整合monitor links**
* **增加approve时可以修改partition数和replication数**
### Bug Fixes
* **修复Monitor->Lag排序错误问题**
* **修改ttl精度**
* **修复Queries模块cluster和topic联动显示问题**
* **增加topic申请topic命名规范**
* **增加Add Partition最大限制提示**
* **修复拒绝task以及再次提交task未发邮件**
* **修改连接到consumerlag页面无法返回**
* **Add Partition时增加字段校验**

# 1.5.3 (2019-11-30)
### Bug Fixes
* **修复alerta以及eamil发送警告异常**

# 1.5.2.12 (2019-11-21)
### Features / Enhancements
* **修改alerta发送数据格式**

# 1.5.2.11 (2019-11-15)
### Features / Enhancements
* **css网络资源本地化**
### Bug Fixes
* **修改tablefilter组件字体weight**



# 1.5.2.10 (2019-11-11)
### Bug Fixes
* **修改summary页面值不准确问题**
* **修改summary页面聚合间隔为auto**
* **approve topic时添加遮罩**


# 1.5.2.9 (2019-11-7)
### Bug Fixes
* **修改alterta发送alter数据格式**
* **修复broker下线partition信息不显示问题**
* **修改zookeeper和api partition信息不一致问题**
* **topic consumer lag时间显示到分钟级别**

# 1.5.2.8 (2019-11-6)
### Bug Fixes
* **修改alterta发送alter数据格式**

# 1.5.2.7 (2019-11-5)
### Bug Fixes
* **修改alterta发送alter数据格式**


# 1.5.2.6 (2019-11-2)
### Features / Enhancements
* **添加向alterta发送alter数据**

# 1.5.2.5 (2019-10-26)
### Bug Fixes
* **修复summary trend数据图显示问题**
* **修复manager config修改时不修改default选项值**


# 1.5.2.4 (2019-10-25)
### Bug Fixes
* **修复admin登陆权限问题**
* **修复manager config信息表格不正常显示问题**
* **修复consumer时数据为空时提示错误问题**

# 1.5.2.3 (2019-10-11)
### Bug Fixes
* **修改summary页面统计图问题**

# 1.5.2.2 (2019-10-9)
### Bug Fixes
* **修改页面cluster选择ALL时存放cookie**
* **homepage 页面缓存存放代码优化**

# 1.5.2.1 (2019-10-9)
### Bug Fixes
* **修复页面filter记忆功能**

# 1.5.2 (2019-10-9)
### Features / Enhancements
* **优化邮件模板**
* **consumer alert email add topic link**
* **重构部分前台代码，删除重复的css/js**
* **增加定时任务更新homepage页面统计信息**
* **修改页面初始化拉取全部集群信息改为拉取单个集群信息**
### Bug Fixes
* **修复topic页面Include special开关不起作用问题**

# 1.5.1 (2019-8-31)
### Bug Fixes
* **修复Cluster Summary统计错误的问题**
# 1.5.0 (2019-8-29)
### Features / Enhancements
* **优化Topic创建流程**
* **重构部分代码，删除重复代码**
### Bug Fixes
* **修复Cluster Summary统计错误的问题**
# 1.4.1 (2019-7-31)
### Features / Enhancements
* **完善topic数据修补对比，支持对比partition，replica，ttl**
* **Topic详情页显示集群信息**
### Bug Fixes
* **修复旧版本增加config信息不生效**
* **Team 增加校验问题**


# 1.4.0 (2019-7-20)
### Features / Enhancements
* **Kafka manager支持topic管理，group管理等**
* **完善审批流程，支持驳回修改功能**
* **新增支持指定offset消费的功能**
* **前端代码自动化构建**
* **moment.js改为dayjs,提升页面响应性能**
* **查询条件个性化支持**
### Bug Fixes
* **页面排序bug**
* **创建task是会出现cluster id 为空，如：(,2)**

# 1.3.7 (2019-7-10)
### Features / Enhancements
* **增加ttl默认配置项**
* **增加KafkaManager中topic功能**
* **通知邮件增加详细信息**
### Bug Fixes
* **页面排序bug**
* **fix zkserver 格式校验问题**
* **team模块添加返回按钮,fix productconsumerlag图表与y轴重叠**
* **修改cluster delete样式，fix team 模块筛选框提示信息不对**
* **修改homepage cluster图表以及cousmerlag聚合时间**

# 1.3.6.3 (2019-6-15)
### Features / Enhancements
* **Cluster 删除提示信息改进**
### Bug Fixes
* **Kafaka 集群指标监控图bug 修复**
* **Team模块下filter 提示内容修正**

# 1.3.6.2 (2019-6-10)
### Bug Fixes
* **修复Monitor>Topic数据过滤问题**

# 1.3.6.1 (2019-6-10)
### Features / Enhancements
* **修改集群单选组件为下拉组件**

### Bug Fixes
* **修复Topic List页面数据重复问题**
* **修复Topic list页面前端渲染慢问题**

# 1.3.6 (2019-6-4)
### Features / Enhancements
* **Topic List定时补充数据**
* **topic detail and group consumer lag页面添加刷新**
* **页面统一pagesize，增加分页器选择**

### Bug Fixes
* **kafka consumer lag chart数据修改为精确查询**
* **修复topic list页面管理员更新topic的owner/team出错时未提示问题**

# 1.3.5 (2019-5-28)

### Features / Enhancements
* **增加从zookeeper指定目录获取kafka集群信息**
* **增加 Consumer Tool**
* **增加超级管理员收藏topic以及添加alert**
* **增加Home页面cluster summary功能汇总metric信息**

### Bug Fixes
* **kafka consumer console data not clear**
* **alert模块group查询超时问题**
* **cluster连接不上所关联的topic模块数据显示问题**
* **cluster被删除，历史数据显示问题**

# 1.3.4 (2019-5-21)

### Features / Enhancements
* **调整Monitor>Group菜单位置**  
* **调整表格默认字体从12px修改为14px**
* **添加收藏按钮被触发的样式**

### Bug Fixes
* **修复consumer lag 收集程序停止工作** 
* **修复Monitor/Group Partition重复问题**
* **fix 查询e4慢问题、优化group显示问题**


# 1.3.3 (2019-5-17)
### Features / Enhancements
* **调整My Favorite菜单位置**  
* **Monitor模块增加按Group粒度查询topic/lag** 
* **系统操作日志去除读操作记录**

### Bug Fixes
* **修复Home页面加载太慢问题**
* **修复Monitor/Topics页面加载太慢问题**


# 1.3.2 (2019-5-16)
### Features / Enhancements
* **ConsumerLag页面添加聚合时间选择功能**
* **Consumer消费图坐标格式化**
* **Alert模块增加创建时间以及创建人员**
* **Monitor模块添加my favorite功能**
* **集成KafkaManager**
* **添加gitlab CD自动部署**
* **邮件模板优化**
### Bug Fixes
* **修复home page页面broker统计不全错误**
* **修复项目中的Fiter功能没有记忆效果**



# 1.3.1 (2019-05-11)
### Features / Enhancements
* **告警邮件按partition排序**
* **邮件中超链接可配置**
* **重构前端路由与后端API接口**：

### Bug Fixes
* **Monitor模块下查询remote localtion无数据**

# 1.3.0 (2019-05-10)
### Features / Enhancements
* **性能优化**: 收集集群所有topic消费延迟lag性能优化
* **添加Loading页面**: 新增Loading页面，提升用户体验
* **菜单字体与logo优化**: 修改菜单字体，修改版权所有
* **跨级群流程实现方式优化**: 
* **Topic 创建添加邮件提醒**

### Bug Fixes
* **HomePage Detail数据格式化错误**: 修复页面数据格式化错误
* **Alert模块排序报错**