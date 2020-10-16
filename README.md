# meizu-version-tool-idea-plugin
魅族版本工具
此工具能够让极大的方便我们拉取新版本或者打RC版本，能否自动同步项目之间依赖版本，特别在同时进行多项目开发时节省大量时间 
功能如下:
项目显示当前版本号
全局同步版本，自动同步当前工作区打开的有依赖的maven项目版本，比如，A项目依赖B项目1.0版本，B项目升级为2.0，A项目可以使用此功能自动更改依赖版本号为2.0
全局修改RC版本，自动根据当前版本和master版本进行计算新版本 新版本计算规则如下: 1.如果当前分支为RC版本，则只进行RC版本递增，比如：RC01 -> RC02 2.否则根据master分支进行版本计算，小版本递增，比如: 1.2.3-RC01 -> 1.2.4-RC01 
单独进行某个项目RC版本，自动根据当前版本和master版本进行计算新版本
单独进行某个项目SNAPSHOT版本，自动根据当前版本和master版本进行计算新版本
Git项目push到Gerrit平台时自动将目标分支加上refs/for作为前缀，免去每次push修改分支操作
Git项目commit会初始化默认提交信息
