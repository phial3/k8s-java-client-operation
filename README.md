# k8s-java-client-demo
the demo that how to use kubernetes-client of java

## Kubernetes Flow
[](https://github.com/SingleTigger/k8s-java-client-demo/blob/master/Kubernetes%20Cluster.png)


Pod 的详细的状态说明:

| 状态                                  | 描述                          |      |
| ------------------------------------- | ----------------------------- | ---- |
| CrashLoopBackOff                      | 容器退出，kubelet正在将它重启 |      |
| InvalidImageName                      | 无法解析镜像名称              |      |
| ImageInspectError                     | 无法校验镜像                  |      |
| ErrImageNeverPull                     | 策略禁止拉取镜像              |      |
| ImagePullBackOff                      | 正在重试拉取                  |      |
| RegistryUnavailable                   | 连接不到镜像中心              |      |
| ErrImagePull                          | 通用的拉取镜像出错            |      |
| CreateContainerConfigError            | 不能创建kubelet使用的容器配置 |      |
| CreateContainerError                  | 创建容器失败                  |      |
| m.internalLifecycle.PreStartContainer | 执行hook报错                  |      |
| RunContainerError                     | 启动容器失败                  |      |
| PostStartHookError                    | 执行hook报错                  |      |
| ContainersNotInitialized              | 容器没有初始化完毕            |      |
| ContainersNotRead                     | 容器没有准备完毕              |      |
| ContainerCreating                     | 容器创建中                    |      |
| PodInitializing                       | pod 初始化中                  |      |
| DockerDaemonNotReady                  | docker还没有完全启动          |      |
| NetworkPluginNotReady                 | 网络插件还没有完全启动        |      |
|                                       |                               |      |