package com.ksj.cliapp.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.ksj.cliapp.service.MainService;

@RestController
@EnableScheduling
public class MainController {
	
	@Autowired
	private MainService service;
	
	boolean isHealthCheckActivated = false;
	
	@RequestMapping(value = "/")
	public ModelAndView index() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("index");

		return mv;
	}
	
	@CrossOrigin
	@PostMapping(value = "/hello")
	public StringBuffer script(@RequestParam(value = "args", required = false) String[] args) {
		return service.scriptService("hello.sh", args);
	}
	
	@CrossOrigin
	@GetMapping(value = "/monitor")
	public StringBuffer monitor() {
		return service.scriptService("monitor.sh", null);
	}
	
	@PostMapping(value = "/healthcheck")
	public String toggleHealthCheck(@RequestParam(value = "ip", required = false) String ip, @RequestParam(value = "port", required = false) String port) {
		if(!ip.isEmpty() && !port.isEmpty()) service.setAddressService(ip, port);
		
		isHealthCheckActivated = !isHealthCheckActivated;
		
		if(isHealthCheckActivated) return "[ Health check activated ]";
		else return "[ Health check deactivated ]";
	}
	
	@GetMapping("/stream")
	public SseEmitter stream() {
		SseEmitter emitter = new SseEmitter();
		// 필요에 따라 스레드를 생성하고, 가능한 경우 이전에 구축된 스레드를 재사용함. 유휴 상태의 스레드는 자동으로 폐기하고, 캐시에서 제거하여 리소스를 소비하지 않음.
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(() -> {
			try {
				while(true) {
					SseEventBuilder event = SseEmitter.event()
						.data(service.scriptService("monitor.sh", null).toString());
					emitter.send(event);
					TimeUnit.MILLISECONDS.sleep(1500);
				}
			} catch(Exception e) {
				emitter.completeWithError(e); // 이벤트 중 예외가 발생하면 프로세스가 완료됨.
			}
		});
		
		return emitter;
	}
	
	@Scheduled(fixedDelay = 1000)
	public void healthCheck() {
		if(isHealthCheckActivated) service.healthCheckService();
	}
}
