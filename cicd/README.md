# 使用帮助

## Jenkinsfile文件:
  jenkinsfile脚本
## Dockerfile-java.v1-league文件:

```
dockerfile模板，支持按产品线分和自定义项目类型
       Dockerfile-java.v1-${PROJECT_ID}-${DEPLOYMENT}
  如： Dockerfile-java.v1-league-league-guide-application
```

## pipeline文件:
  统一项目模板，调用jenkinsfile脚本


```
  动态传参:
  
    ----必须参数-----
    XBUILD_TEAM: 产品线,即项目组名
    XBUILD_REPO: git仓库地址
    XBUILD_DEPLOY: 项目名,即项目实际部署的deployment名
    XBUILD_BRANCHE: git分支
    XBUILD_LABEL: jenkins 节点标签
    XBUILD_VERSION: 镜像的tag
    -----分割线------
    XBUILD_SCHEME: 固定参数,指定执行的函数
    XBUILD_OWNER: 固定参数,jenkins凭据。gitlab账户信息
```
