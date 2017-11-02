package com.zrs.turbine;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.model.Check;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 管理类
 * @author zhuruisong
 */
@RestController
@Slf4j
public class AdminController {

	@Autowired
	private ConsulClient consulClient;

	/**
	 * 清理掉无效服务实例
	 * @param response
	 */
//	@ApiOperation(value="剔除所有无效的服务实例", notes="剔除所有无效的服务实例")
	@RequestMapping(value="/clear/failing",method=RequestMethod.GET)
	public void getAllServicer(HttpServletResponse response){
		log.info("***********************consul上无效服务清理开始*******************************************");
		StringBuilder stringBuilder = new StringBuilder();
		//获取所有的members的信息
		List<Member> members = consulClient.getAgentMembers().getValue();
		for (int i=0; i<members.size(); i++){
			//获取每个member的IP地址
			String address = members.get(i).getAddress();
			log.info("member的IP地址为:{}",address);
			//根据role变量获取每个member的角色  role：consul---代表服务端   role：node---代表客户端
			String role =  members.get(i).getTags().get("role");
			log.info("{}机器的role为：{}=====注释*role为consul代表服务端   role为node代表客户端",address,role);
			//判断是否为client, 'consul'服务端刚注册进来的实例为failing状态需要点时间才变成passing
			if (role.equals("consul")){//TODO client启动后改成node
				//将IP地址传给ConsulClient的构造方法，获取对象
				ConsulClient  clearClient =new ConsulClient(address);
				//根据clearClient，获取当前IP下所有的服务 使用迭代方式 获取map对象的值
				Iterator<Map.Entry<String,Service>> it =clearClient.getAgentServices().getValue().entrySet().iterator();
				while (it.hasNext()){
					//迭代数据
					Map.Entry<String,Service> serviceMap =  it.next();
					//获得Service对象
					Service service = serviceMap.getValue();
					//获取服务名称
					String serviceName = service.getService();
					//获取服务ID
					String serviceId = service.getId();
					log.info("在{}客户端上的服务名称 :{}**服务ID:{}",address,serviceName,serviceId);
					//根据服务名称获取服务的健康检查信息
					Response<List<Check>> checkList =consulClient.getHealthChecksForService(serviceName,null);
					List<Check> checks = checkList.getValue();
					//获取健康状态值  PASSING：正常  WARNING  CRITICAL  UNKNOWN：不正常
					Check.CheckStatus checkStatus = checks.get(0).getStatus();
					log.info("在{}客户端上的服务 :{}的健康状态值：{}",address,serviceName,checkStatus);
					if (checkStatus != Check.CheckStatus.PASSING){
						log.info("在{}客户端上的服务 :{}为无效服务，准备清理...................",address,serviceName);
						clearClient.agentServiceDeregister(serviceId);
						stringBuilder.append("在").append(address).append("客户端上的服务 :").append(serviceName).append("为无效服务，已经清理\n");
					}
				}
			}
		}
		try {
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(stringBuilder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
