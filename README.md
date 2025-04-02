# cloudbeaver-docker-build
CloudBeaver-CE未提供驱动管理功能，而存在自定义驱动的需求。

官方给出了CloudBeaver-CE自定义驱动的指南，整体上就是通过Maven新建一个子Module模块，然后重新编译。

为了不污染开发环境，这个仓库主要提供两个Dockerfile文件，构建两个打包镜像，使打包活动在容器中发生。

## 1. 关于自定义插件

在自定义插件方面官方文档给出一个[简单的操作步骤](https://github.com/dbeaver/cloudbeaver/wiki/Adding-new-database-drivers)。同时，在此仓库中我也自定义了一个dameng的插件，因此也可以参考dameng的实现。

但是在实际操作中，可能有与以下方面相关的问题，我将之放到了补充说明中：

1. 按照文档自定义插件，但是CloudBeaver未识别出驱动。
2. 如何确定插件中关于`driver id`如何确定？

## 2. 关于打包镜像

这个仓库的结构有一些奇怪，原因是因为原本我预计在公司内网环境（无法联网、无docker代理）中进行打包。因此我会对项目做出一些说明以便你能够更好的修改其中的不合理之处。

### 为什么会有四个项目？

根目录下的`cloudbeaver`、`dbeaver`、`dbeaver-common`等四个目录对应四个官方的项目。在执行`cloudbeaver/deploy/build.sh`的脚本时,会判断根目录下是否有对应的目录，没有则从Github下载三个项目（除cloudbeaver外），我原计在内网构建，因此我提前下载了，如果你不需要可以删除或替换其他三个目录。

### 为什么会有两个打包镜像？

也是因为内网环境，所以我把最底层的docker镜像依赖达成了一个最基础的包（base镜像），并将之传递到内网中。因此，我分成了两个Dockerfile生成两个镜像。如果你不需要可以考虑合并他们。

### 如何执行

首先在根目录依次执行`base`、`build`两个镜像的打包命令。最后在`build`中重新打包CloudBeaver源码。

第一，进入根目录。

第二，打包基础镜像。

`docker build --no-cache -t cloudbeaver-base:1.0.0-base -f cloudbeaver/custom-plugins-entry/build-env-docker/Dockerfile.base .`如果你之后没有对Dockerfile.base进行修改，这一步不用重复执行。

第三，打包构建镜像。

`docker build --no-cache -t cloudbeaver-build:latest -f cloudbeaver/custom-plugins-entry/build-env-docker/Dockerfile.build .`与base构建相比，这里稍有不同，你如果通过官文的方式新添加了数据库驱动的话，你需要重新执行这个命令。这个命令的作用是把你新构建的驱动module，添加到镜像中，重新生成一个新的包含你修改的镜像。

第四，执行构建镜像。

以下两种方式均可，我更推荐后一种，手动执行构建脚本。
a. 默认启动构建脚本

```shell
docker run -it --rm \
  -v "$(pwd)/cloudbeaver/deploy:/build/cloudbeaver/deploy" \
  -v "$(pwd)/build-logs:/var/log/cloudbeaver" \
  cloudbeaver-build:latest \
  2>&1 | tee build.log
```

b. 进入镜像中自行构建，到cloudbeaver/deploy/自行启动build.sh脚本。因为有时候可能出现build.sh脚本未赋予执行权限的情况。执行完毕后退出镜像。
```shell
docker run -it --rm \
  -v "$(pwd)/cloudbeaver/deploy:/build/cloudbeaver/deploy" \
  -v "$(pwd)/build-logs:/var/log/cloudbeaver" \
  --entrypoint /bin/bash \
  cloudbeaver-build:latest
```

第五，构建cloudbeaver docker镜像。

在第四步之后实际上你的cloudbeaver构建任务已经完成了，你如果不需要一个docker版本的cloudbeaver你不用继续执行。

**如何判断你是否构建插件成功呢？**首先，在未启动时可以检查cloudbeaver/deploy下构建源码后生成的cloudbeaver目录。该目录下的server/plugins下应该有` org.jkiss.dbeaver.ext.`开头的jar包（如`org.jkiss.dbeaver.ext.dameng_1.0.36.202503310723.jar`）。同时cloudbeaver目录下的drivers中应该有新构建驱动所依赖的jar包。其次，在启动后输出日志中会有成功激活的驱动列表，你也可以通过这个判断。

在第四步执行完成，并退出docker镜像后后，在cloudbeaver/deploy/docker中启动 make-docker-container.sh构建docker镜像。

第六，启动cloudbeaver镜像。

在第五步执行完毕后，会生成一个`dbeaver/cloudbeaver:dev`镜像。此镜像名来自于make-docker-container.sh,你可以自行修改。可以通过官方的启动方式启动它,注意tag不是latest而是dev。

`docker run -d --name cloudbeaver -p 8978:8978 -v ./workspace:/opt/cloudbeaver/workspace dbeaver/cloudbeaver:dev`

## 3. 补充说明

1. 按照文档自定义插件，但是CloudBeaver未识别出驱动。

   首先检查目录cloudbeaver/deploy/server/plugins下是否有` org.jkiss.dbeaver.ext.`对应的包。如果没有说明应该时dbeaver生成插件时出现了错误。如果有这个包，那么去检查drivers目录下是否有对应的依赖。如果以上都存在，那么可能是你的driver id不正确，按照下一个问题确定你的driver id。如果以上都不行，你可以考虑通过远程debug的方式检测`org.jkiss.dbeaver.registry.DataSourceProviderRegistry`的`loadExtensions`方法。

2. 如何确定插件中关于`driver id`如何确定？

   可以通过检查`org.jkiss.dbeaver.ext.`下对应的包里面的plugin.xml。driver id的两个字段（冒号分隔的前后两个字段），分别来自于 datasource 标签的id属性以及其内的driver 标签的id属性。
