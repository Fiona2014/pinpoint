package com.navercorp.pinpoint.web.alarm.checker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

public abstract class AgentChecker extends AlarmChecker {
    
    protected Map<String, Long> detectedAgents = new HashMap<String, Long>();

    protected AgentChecker(Rule rule, String unit, DataCollector dataCollector) {
        super(rule, unit, dataCollector);
    }
    
    @Override
    public void check() {
        dataCollector.collect();

        Map<String, Long> agents = getAgentValues();
        
        for(Entry<String, Long> agent : agents.entrySet()) {
            if (decideResult(agent.getValue())) {
                detected = true;
                detectedAgents.put(agent.getKey(), agent.getValue());
            }
            
            logger.info("{} result is {} for agent({}). value is {}. (threshold : {}).", this.getClass().getSimpleName(), detected, agent.getKey(), agent.getValue(), rule.getThreshold());
        }
    }
    
    @Override
    protected long getDetectedValue() {
        throw new UnsupportedOperationException(this.getClass() + "is not support getDetectedValue function. you should use getAgentValues");
    }

    public List<String> getSmsMessage() {
        List<String> messages = new LinkedList<String>();
        
        for (Entry<String, Long> detected : detectedAgents.entrySet()) {
            messages.add(String.format("[PINPOINT Alarm - %s] %s is %s%s (Threshold : %s%s)", detected.getKey(), rule.getCheckerName(), detected.getValue(), unit, rule.getThreshold(), unit));
        }
        
        return messages;
    };
    
    @Override
    public String getEmailMessage() {
        StringBuilder message = new StringBuilder();
        
        for (Entry<String, Long> detected : detectedAgents.entrySet()) {
            message.append(String.format(" Value of agent(%s) is %s%s during the past 5 mins.(Threshold : %s%s)", detected.getKey(), detected.getValue(), unit, rule.getThreshold(), unit));
            message.append("<br>");
        }
        
        return message.toString();
    };
    
    protected abstract Map<String, Long> getAgentValues();
    
}