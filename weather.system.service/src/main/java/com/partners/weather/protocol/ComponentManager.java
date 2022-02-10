package com.partners.weather.protocol;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.weather.common.ResultCode;
import com.partners.weather.event.ComponentStartEvent;
import com.partners.weather.event.listener.ComponentStartListener;
import com.partners.weather.redis.RedisQueue;

public class ComponentManager {
    private static boolean Started = false;
    private static boolean Initialized = false;
    private static volatile ComponentManager Instance;
    private List<GeneralComponent> componentList = new ArrayList<GeneralComponent>();
    private static final Logger logger = LoggerFactory.getLogger(ComponentManager.class);
    private static volatile List<Integer> allports = new ArrayList<>();
    private static volatile RedisQueue<RequestMessage> jedisQueue;

    public static ComponentManager getInstance() {
        if (Started) {
            return Instance;
        }
        if (Objects.isNull(Instance)) {
            synchronized (ComponentManager.class) {
                if (Objects.isNull(Instance)) {
                    Instance = new ComponentManager();
                    Started = true;
                }
            }
        }
        return Instance;
    }

    public static List<Integer> getUsedPorts() {
        return allports;
    }

    public boolean initialize(List<Integer> ports) {
        if (Initialized || ports.size() == 0) {
            return true;
        }
        try {
            GeneralComponent component;
            Set<Integer> portSet = new HashSet<Integer>(ports);
            ComponentManager.allports.addAll(portSet);
            for (Integer port : ComponentManager.allports) {
                component = new ProtocolImp(port);
                this.stopComponent(port);
                this.componentList.add(component);
                this.startComponent(component);
            }

        } catch (Exception ex) {
            logger.error("Component Manager Cannot be Initialized: " + "@" + ex.getMessage());
            return false;
        }
        Initialized = true;
        return true;
    }

    public ResultCode ResetPort(List<Integer> ports) {
        ResultCode resultCode = ResultCode.OK;
        if (ports.size() == 0) {
            return resultCode;
        }
        Set<Integer> portSet = new HashSet<Integer>(ports);
        ports.clear();
        ports.addAll(portSet);
        for (Integer port : ComponentManager.allports) {
            if (ports.contains(port)) {
                continue;
            }
            GeneralComponent component = new ProtocolImp(port);
            resultCode = this.stopComponent(component);
            if (resultCode == ResultCode.OK) {
                ComponentManager.allports.remove(port);
            }
        }
        for (Integer port : ports) {
            GeneralComponent component = new ProtocolImp(port);
            if (ComponentManager.allports.contains(port)) {
                continue;
            }
            this.componentList.add(component);
            resultCode = this.startComponent(component);
            if (resultCode == ResultCode.OK) {
                ComponentManager.allports.add(port);
            }
        }
        return resultCode;
    }

    public ResultCode startComponent(int port) {
        GeneralComponent component = this.getComponent(port);
        return this.startComponent(component);
    }

    public ResultCode startComponent(GeneralComponent component) {
        try {
            if (component == null) {
                return ResultCode.ComponentNotFound;
            }
            if (component.getStatus() == ComponentStatus.Started) {
                return ResultCode.ComponentAlreadyStarted;
            }

            ComponentStartListener listener = new ComponentStartListener();
            ComponentStartEvent.addListner(listener);
            ComponentThread componentThread = new ComponentThread(component);
            Thread thread = new Thread(componentThread);
            thread.start();
            ResultCode result = null;
            // Time Out 20 seconds
            int timeout = 20 * 1000;
            while (true) {
                if (timeout <= 0) {
                    result = ResultCode.ComponentCannotStarted;
                    break;
                }
                Thread.sleep(5);
                timeout -= 5;
                if (listener.getEvent() == null) {
                    continue;
                } else {
                    result = listener.getEvent().getArgs().getResultCode();
                    break;
                }
            }
            if (result != null && result.equals(ResultCode.OK)) {
                component.setStatus(ComponentStatus.Started);
                boolean blnFindPort = false;
                for (GeneralComponent generalComponent : this.componentList) {
                    if (component.getPort() == generalComponent.getPort()) {
                        blnFindPort = true;
                        break;
                    }
                }
                if (!blnFindPort) {
                    this.componentList.add(component);
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("Start Component Failed (Component Not Found): " + "@" + ex.getMessage());
            return ResultCode.ComponentNotFound;
        }
    }

    public ResultCode stopComponent(int port) {
        GeneralComponent component = this.getComponent(port);
        return this.stopComponent(component);
    }

    public ResultCode stopComponent(GeneralComponent component) {

        try {
            if (component == null) {
                return ResultCode.ComponentNotFound;
            }
            if (component.getStatus().equals(ComponentStatus.Stopped)) {
                return ResultCode.ComponentAlreadyStopped;
            }
            ResultCode result = component.stop();
            if (result != null && result.equals(ResultCode.OK)) {
                component.setStatus(ComponentStatus.Stopped);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Stop Component Failed (Component Not Found): " + "@" + ex.getMessage());
            return ResultCode.ComponentNotFound;
        }
    }

    public GeneralComponent getComponent(int port) {
        AtomicReference<GeneralComponent> componentRef = new AtomicReference<>(null);
        try {
            if (this.componentList.size() < 1) {
                return componentRef.get();
            }
            this.componentList.stream().filter(
                    generalComponent -> generalComponent.getPort() == port
            ).findFirst().ifPresent(
                    generalComponent -> componentRef.set(generalComponent)
            );
        } catch (Exception ex) {
            logger.error("Get Component Failed (Component Not Found): " + "@" + ex.getMessage());
        }
        return componentRef.get();
    }

    public RedisQueue<RequestMessage> getJedisQueue() {
        return jedisQueue;
    }

    public void setJedisQueue(RedisQueue<RequestMessage> jedisQueueObj) {
        jedisQueue = jedisQueueObj;
    }
}
