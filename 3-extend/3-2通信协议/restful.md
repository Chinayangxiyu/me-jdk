# 概述

# restful对查询的定义
https://www.zhihu.com/question/36706936
可以使用post执行查询，并不一定使用get请求，当请求参数过多因为有的http组件不支持往get
请求添加body（http协议本身并没有要求get请求不能使用body）,导致无法调用。