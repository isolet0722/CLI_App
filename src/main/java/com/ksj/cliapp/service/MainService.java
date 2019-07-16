package com.ksj.cliapp.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ksj.cliapp.jpa.LogEntity;
import com.ksj.cliapp.jpa.LogRepository;
import com.ksj.cliapp.jpa.TargetEntity;
import com.ksj.cliapp.jpa.TargetRepository;

@Service
public class MainService {
	
	@Autowired
	TargetRepository targetRepository;
	
	@Autowired
	LogRepository logRepository;
	
	TargetEntity targetEntity = TargetEntity.builder().build();
	
	LogEntity logEntity = LogEntity.builder().build();
	
	synchronized public StringBuffer scriptService(String fileName, String[] args) {
		String line = "";
		StringBuffer params = new StringBuffer(); 
		StringBuffer result = new StringBuffer();
	    ProcessBuilder builder = new ProcessBuilder();
	    Process process = null;
	
	    // Linux OS 환경이 확인되는 경우에만 bash script 실행
	    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	    if(!isWindows) {
		    try {
		    	if(args != null) for(String temp : args) params.append(temp);
		    	
			    builder.command("bash", "-c", "/mnt/c/scripts/" + fileName + " " + params + ";echo $?"); // 스크립트 파일 경로는 해당 로컬 환경에 맞게 수정되어야 함
		    	process = builder.start();
		    	process.waitFor();
		    			    	
		    	BufferedReader br = new BufferedReader(new InputStreamReader(new SequenceInputStream(process.getInputStream(), process.getErrorStream())));
		    	while((line = br.readLine()) != null) {
		    		// JSON 포맷으로 결과를 반환해야 하는 경우(monitor)와 그렇지 않은 경우를 나누어 스크립트 실행 결과 메시지를 처리
		    		if(fileName.equals("monitor.sh")) {
			    		if(line.equals("0")) line = "\"result\": \"success\" }";
			    		else if(line.equals("1")) line = "\"result\": \"fail\" }";
		    		} else {
			    		if(line.equals("0")) line = "\n[ Command Result : Success ]";
			    		else if(line.equals("1")) line = "\n[ Command Result : Fail ]";
		    		}
		    		
		    		result.append(line);
		    	}
		    	
		    	br.close();
		    } catch(Exception e) {
		    	e.printStackTrace();
		    }
	    } else {
	    	result.append("\n[ It does not seem to be a Linux OS. ]");
	    }
	
		return result;
	}
	
	public void healthCheckService() {
		if(!targetRepository.existsById(1L)) setAddressService("127.0.0.1", "8002"); // DB에 타겟 주소 정보가 없다면, 기본 주소 설정
		
		try {
			String url = "http://" + targetRepository.findById(1L).get().getIp() + ":" + targetRepository.findById(1L).get().getPort() + "/health";
			
			RestTemplate rt = new RestTemplate();
			String response = rt.getForObject(url, String.class);
			
			Map<String, Object> map = new BasicJsonParser().parseMap(response);
			
			if(map.get("status").equals("DOWN")) throw new Exception("Target Server is Down");
		} catch(Exception e) {
			// 가장 최근의 에러 로그와 대조하여, 동일한 에러가 아니거나 300초 이상 경과한 경우에만 새로운 로그를 남김 (중복 로그가 지나치게 많아지는 상황 방지)
			if(logRepository.count() == 0 
					|| !logRepository.findTopByOrderByIdDesc().getErrorMsg().equals(e.getMessage())
					|| ChronoUnit.SECONDS.between(logRepository.findTopByOrderByIdDesc().getTimestamp().toInstant()
							.atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()) >= 300) {
				logEntity.setId(null);
				logEntity.setErrorMsg(e.getMessage());
				logRepository.saveAndFlush(logEntity);
			}
		}
	}
	
	public void setAddressService(String ip, String port) {
		targetEntity.setId(1L);
		targetEntity.setIp(ip);
		targetEntity.setPort(port);
		targetRepository.saveAndFlush(targetEntity);
	}
}
