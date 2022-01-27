package com.partners.weather.serial.transfer;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class SerialPortManager {

	/**
	 * 查找所有可用端口
	 */
	@SuppressWarnings("unchecked")
	public static final List<String> findPort() {
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		List<String> portNameList = new ArrayList<>();
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}

	/**
	 * 打开串口
	 */
	public static final SerialPort openPort(String portName, int baudrate) throws Exception {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			CommPort commPort = portIdentifier.open(portName, 2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				try {
					serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				} catch (UnsupportedCommOperationException e) {
					throw new Exception("设置串口参数失败！");
				}
				return serialPort;
			} else {
				throw new Exception("端口指向设备不是串口类型！");
			}
		} catch (NoSuchPortException e1) {
			throw new Exception("没有找到与该端口名匹配的串口设备！");
		} catch (PortInUseException e2) {
			throw new Exception("端口已被占用！");
		}
	}

	/**
	 * 关闭串口
	 * 
	 */
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * 从串口读取数据
	 * @throws IOException 
	 */
	public static byte[] readFromPort(SerialPort serialPort) throws IOException {
		InputStream in = null;
		byte[] bytes = null;
		try {
			in = serialPort.getInputStream();
			int bufflenth = in.available();
			while (bufflenth != 0) {
				bytes = new byte[bufflenth];
				in.read(bytes);
				bufflenth = in.available();
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return bytes;
	}

	/**
	 * 监听
	 * @throws TooManyListenersException 
	 * 
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) throws TooManyListenersException {
		try {
			port.addEventListener(listener);
			port.notifyOnDataAvailable(true);
			port.notifyOnBreakInterrupt(true);
		} catch (TooManyListenersException e) {
			throw e;
		}
	}
}
