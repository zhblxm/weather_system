package com.partners.weather.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.partners.entity.ParameterAttribute;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.weather.common.CommonResources;
import com.partners.weather.exception.FileFormatException;

public class ParseXML {
	private static final Logger logger = LoggerFactory.getLogger(ParseXML.class);
	private static final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
	private static final XPath xPath = XPathFactory.newInstance().newXPath();

	public static Terminalparameters parseXMLFile(final File xmlFile) throws FileFormatException, ParserConfigurationException {
		return parseXMLFile(xmlFile, documentFactory.newDocumentBuilder());
	}

	public static Terminalparameters parseXMLFile(final String filePath) throws FileFormatException, ParserConfigurationException {
		return parseXMLFile(new File(filePath), documentFactory.newDocumentBuilder());
	}

	private static Terminalparameters parseXMLFile(final File sourceFile, final DocumentBuilder documentBuilder) throws FileFormatException {
		Terminalparameters terminalparameters = null;
		try {
			if (StringUtils.isBlank(sourceFile.getName())) {
				throw new NullPointerException("文件名不能为空，请合理使用文件名！");
			}
			Document document = documentBuilder.parse(sourceFile.getAbsolutePath());
			String root = "/Parameters";
			Element element = (Element) xPath.evaluate(root, document, XPathConstants.NODE);
			NodeList childNodes = null;
			NamedNodeMap parameterMap;
			if (element == null) {
				throw new FileFormatException("xml文件格式不符合系统要求");
			}
			NodeList nodes = element.getChildNodes();
			if (nodes.getLength() == 0) {
				throw new FileFormatException("未发现可用元素，xml文件格式不符合系统要求");
			}
			if (StringUtils.isBlank(element.getAttribute("name").trim())) {
				throw new FileFormatException("根节点属性name不能为空，xml文件格式不符合系统要求");
			}
			terminalparameters = new Terminalparameters(element.getAttribute("name").trim(), sourceFile.getName().replaceAll("\\s*", "").replaceAll("[.][^.]+$", ""));
			List<TerminalParametersAttrs> terminalParametersAttrs = new ArrayList<>();
			List<ParameterAttribute> parameterAttributes = null;
			TerminalParametersAttrs terminalParametersAttr;
			ParameterAttribute parameterAttribute;
			String eleValue = "";
			int lasterOrder = 0;
			boolean hasDataConver=false;
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if (!nodes.item(i).hasAttributes()) {
						throw new FileFormatException("参数至少有一个属性！文件格式不符合系统要求。");
					}
					parameterMap = nodes.item(i).getAttributes();
					if (parameterMap.getNamedItem("name") == null || StringUtils.isBlank(parameterMap.getNamedItem("name").getNodeValue())) {
						throw new FileFormatException("参数缺少name！文件格式不符合系统要求。");
					}
					terminalParametersAttr = new TerminalParametersAttrs(parameterMap.getNamedItem("name").getNodeValue().trim());
					if (parameterMap.getNamedItem("order") == null || StringUtils.isBlank(parameterMap.getNamedItem("order").getNodeValue())) {
						throw new FileFormatException("参数缺少order！文件格式不符合系统要求。");
					}
					terminalParametersAttr.setOrder(Integer.valueOf(parameterMap.getNamedItem("order").getNodeValue().trim()));
					lasterOrder=lasterOrder<terminalParametersAttr.getOrder()?terminalParametersAttr.getOrder():lasterOrder;
					if (parameterMap.getNamedItem("description") != null) {
						terminalParametersAttr.setDescription(parameterMap.getNamedItem("description").getNodeValue().trim());
					}
					if (parameterMap.getNamedItem("showInPage") != null) {
						terminalParametersAttr.setShowInPage(parameterMap.getNamedItem("showInPage").getNodeValue().trim());
					}
					if (parameterMap.getNamedItem("orderDesc") != null) {
						terminalParametersAttr.setOrderDesc(parameterMap.getNamedItem("orderDesc").getNodeValue().trim());
					}
					childNodes = nodes.item(i).getChildNodes();
					if (childNodes.getLength() > 0) {
						parameterAttributes = new ArrayList<>();
					}
					hasDataConver=false;
					for (int j = 0; j < childNodes.getLength(); j++) {
						if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
							parameterAttribute = new ParameterAttribute();
							if (!childNodes.item(j).hasAttributes()) {
								throw new FileFormatException("属性配置缺少name或description！文件格式不符合系统要求。");
							}
							parameterMap = childNodes.item(j).getAttributes();
							if (parameterMap.getNamedItem("name") != null || StringUtils.isBlank(parameterMap.getNamedItem("name").getNodeValue())) {
								parameterAttribute.setName(parameterMap.getNamedItem("name").getNodeValue().trim());
							}
							if (parameterMap.getNamedItem("description") != null) {
								parameterAttribute.setDescription(parameterMap.getNamedItem("description").getNodeValue().trim());
							}
							if (childNodes.item(j).getChildNodes() != null && childNodes.item(j).getChildNodes().getLength() > 0) {
								eleValue = childNodes.item(j).getChildNodes().item(0).getNodeValue().trim();
								if ("datatype".equalsIgnoreCase(parameterAttribute.getName())) {
									if (!Arrays.asList(CommonResources.DATETYPE).contains(eleValue.toLowerCase())) {
										throw new FileFormatException("数据类型不符合要求，请检查文件配置");
									}
								}
								if ("datatypeformat".equalsIgnoreCase(parameterAttribute.getName())) {
									if (!Arrays.asList(CommonResources.DATEFORMATE).contains(eleValue)) {
										throw new FileFormatException("日期或时间格式不符合要求，请检查文件配置");
									}
								}
								if (("minvalue".equalsIgnoreCase(parameterAttribute.getName()) || "maxvalue".equalsIgnoreCase(parameterAttribute.getName()))) {
									if (StringUtils.isBlank(eleValue)) {
										eleValue = "minvalue".equalsIgnoreCase(parameterAttribute.getName()) ? String.valueOf(Integer.MIN_VALUE) : String.valueOf(Integer.MAX_VALUE);
									} else {
										Double.valueOf(eleValue);
									}
								}
								if ("dataconvert".equalsIgnoreCase(parameterAttribute.getName())) {
									hasDataConver=true;
									Integer.valueOf(eleValue.replace('乘', '0').replace('除', '0'));
								}
								parameterAttribute.setValue(eleValue);
							}
							parameterAttributes.add(parameterAttribute);
						}
					}
					if(!hasDataConver)
					{
						parameterAttribute = new ParameterAttribute();
						parameterAttribute.setName("dataconvert");
						parameterAttribute.setValue("");
						parameterAttributes.add(parameterAttribute);
					}
					terminalParametersAttr.setParameterAttributes(parameterAttributes);
					terminalParametersAttrs.add(terminalParametersAttr);
				}
			}

			if (terminalParametersAttrs.size() > 0) {
				terminalParametersAttr=gererateParameter("fullCollectDate", "收集日期", CommonResources.DATETYPE[4], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("systemDate", "系统日期", CommonResources.DATETYPE[4], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("stationId", "站点标识", CommonResources.DATETYPE[5], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("weatherhistoryId", "系统收到消息唯一标识", CommonResources.DATETYPE[1], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("delayed", "延时数据", CommonResources.DATETYPE[5], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("IsOnlyHour", "系统短日期", CommonResources.DATETYPE[5], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("terminalParamterCategoryId", "所用要素或设备", CommonResources.DATETYPE[5], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalParametersAttr=gererateParameter("terminalModel", "设备", CommonResources.DATETYPE[0], ++lasterOrder);
				terminalParametersAttrs.add(terminalParametersAttr);
				terminalparameters.setTerminalParametersAttrs(terminalParametersAttrs);
				Collections.sort(terminalParametersAttrs, new Comparator() {
					@Override
					public int compare(Object v1, Object v2) {
						TerminalParametersAttrs t1 = (TerminalParametersAttrs) v1;
						TerminalParametersAttrs t2 = (TerminalParametersAttrs) v2;
						return Integer.compare(t1.getOrder(), t2.getOrder());
					}
				});
			}

		} catch (Exception e) {
			logger.error("Error in {0}", e);
			throw new FileFormatException(e.getMessage());
		}
		if (terminalparameters == null) {
			throw new FileFormatException("文件解析失败！请通知管理员查看日志文件。");
		}
		return terminalparameters;

	}
	public static Terminalparameters gererateInvalidMappingParameters(final Terminalparameters terminalparameter){
		Terminalparameters terminalparamete_new=new Terminalparameters(terminalparameter.getName(),terminalparameter.getFileName());
		List<TerminalParametersAttrs> terminalParametersAttrs = new ArrayList<>();
		int lasterOrder=0;		
		TerminalParametersAttrs terminalParametersAttr=gererateParameter("invalidfield", "无效数据字段", CommonResources.DATETYPE[0], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("invalidfielddesc", "无效数据字段描述信息", CommonResources.DATETYPE[0], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("maxvalue", "最大值", CommonResources.DATETYPE[1], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("minvalue", "最小值", CommonResources.DATETYPE[1], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("value", "接收到的值", CommonResources.DATETYPE[1], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("convert", "转换公式", CommonResources.DATETYPE[0], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);		
		 terminalParametersAttr=gererateParameter("fullCollectDate", "收集日期", CommonResources.DATETYPE[4], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("systemDate", "系统日期", CommonResources.DATETYPE[4], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("stationId", "站点标识", CommonResources.DATETYPE[5], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("weatherhistoryId", "系统收到消息唯一标识", CommonResources.DATETYPE[1], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("IsOnlyHour", "是否为系统整点数据", CommonResources.DATETYPE[5], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);	
		terminalParametersAttr=gererateParameter("terminalParamterCategoryId", "所用要素或设备", CommonResources.DATETYPE[5], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalParametersAttr=gererateParameter("terminalModel", "设备", CommonResources.DATETYPE[0], ++lasterOrder);
		terminalParametersAttrs.add(terminalParametersAttr);
		terminalparamete_new.setTerminalParametersAttrs(terminalParametersAttrs);
		return terminalparamete_new;
	}
	private static TerminalParametersAttrs gererateParameter(String name,String description,String datatype,int order) {
		TerminalParametersAttrs terminalParametersAttr = new TerminalParametersAttrs(name,true);
		terminalParametersAttr.setDescription(description);
		terminalParametersAttr.setOrder(order);
		terminalParametersAttr.setOrderDesc("");
		terminalParametersAttr.setShowInPage("N");
		List<ParameterAttribute> parameterAttributes = new ArrayList<>();
		ParameterAttribute parameterAttribute = new ParameterAttribute();
		parameterAttribute.setName("datatype");
		parameterAttribute.setValue(datatype);
		parameterAttribute.setDescription("数据类型");
		parameterAttributes.add(parameterAttribute);
		if("datetime".equalsIgnoreCase(datatype))
		{
			parameterAttribute = new ParameterAttribute();
			parameterAttribute.setName("datatypeformat");
			parameterAttribute.setValue("yyyy-MM-dd HH:mm:ss");
			parameterAttribute.setDescription("数据格式");
			parameterAttributes.add(parameterAttribute);
		}
		terminalParametersAttr.setParameterAttributes(parameterAttributes);
		return terminalParametersAttr;
	}
}