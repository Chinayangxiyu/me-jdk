[参考](https://www.zhihu.com/collection/525744206)
## 2.1 概念
什么是jwt：JSON Web Token，开放标准(RFC 7519)；是一种Web令牌，定义了一种紧凑的JSON结构信息，经过加密后在各系统之间传递。

## 2.2 结构：JWT包含3个部分：头部Header，数据Payload，签名Signature。
###  Header属性
alg：算法
type：令牌类型，统一写JWT
### payload属性：iss (issuer)：签发人
exp (expiration time)：过期时间
sub (subject)：主题
aud (audience)：受众
nbf (Not Before)：生效时间
iat (Issued At)：签发时间
jti (JWT ID)：编号

## 2.3 工作原理：
背景概述：有一个统一的用户管理中心，所以的前端页面系统和这个用户中心交互拿到用户信息；后端服务并不直接和用户管理中心交互。  
1、登陆：用户在前端页面登陆，调用用户管理中心进行校验；  
2、生成token：用户管理中心校验通过以后，填充header和payload信息，然后对header、payload加密生成签名信息；  
**所以header、payload的信息都是透明的，不应该保存敏感信息**  
3、访问后端服务：前端拿到token后，根据这个token访问后端服务；  
4、解析：后端服务拿到token后进行切割，使用自己的密钥进行**签名验证**，验证通过后可以获取自己的需要的信息。

## 2.4 JWT优劣势（和传统sessionID比较）
优势：实现简单，后端服务和用户管理中心解偶

劣势：  
&emsp;&emsp;  （1）加密解密需要消耗时间和资源；
&emsp;&emsp;  （2）交互的过程中token数据比sessionID大，占用更多宽带；
&emsp;&emsp;  （3）虽然JWT自身带有失效时间，但是不能主动使其失效；既不能主动注销用户的访问。

优势：
&emsp;&emsp;  （1）无状态：后端不需要像session一样存储信息，但是无状态也导致系统无法强制使JWT失效；
使用JWT访问服务的任何一个节点都可以，但是使用session的话还需要验证session是否生效等。
&emsp;&emsp;  （2）不依赖cookie：sessionID的传递依赖cookie，token不依赖；
&emsp;&emsp;  （3）跨语言：因为token是json结构，可以跨语言通用。


## 总结
**JWT生成的token信息header和payload部分只是使用base64编码，是透明信息，无法保证信息安全。**