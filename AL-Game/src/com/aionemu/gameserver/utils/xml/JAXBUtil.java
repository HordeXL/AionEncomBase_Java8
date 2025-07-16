package com.aionemu.gameserver.utils.xml;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author , Neon
 */
public class JAXBUtil {

	private static final Map<Class<?>, Future<JAXBContext>> CONTEXTS = new ConcurrentHashMap<>();

	public static void preLoadContextAsync(Class<?> clazz) {
		CONTEXTS.put(clazz, ForkJoinPool.commonPool().submit(() -> JAXBContext.newInstance(clazz)));
	}

	public static String serialize(Object obj) {
		return serialize(obj, (String) null); 
	}

	public static String serialize(Object obj, String schemaFile) {
		try {
			JAXBContext jc = JAXBContext.newInstance(obj.getClass());
			Marshaller m = jc.createMarshaller();
			if (schemaFile != null)
				m.setSchema(XmlUtil.getSchema(schemaFile));
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter sw = new StringWriter();
			m.marshal(obj, sw);
			return sw.toString();
		}
		catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static void serialize(Object obj, File file) {
		serialize(obj, file, null);
	}

	public static void serialize(Object obj, File file, String schemaFile) {
		try {
			JAXBContext jc = JAXBContext.newInstance(obj.getClass());
			Marshaller m = jc.createMarshaller();
			if (schemaFile != null)
				m.setSchema(XmlUtil.getSchema(schemaFile)); // Depende de XmlUtil
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(obj, file);
		}
		catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T deserialize(String xml, Class<T> clazz) {
		return deserialize((Object) xml, clazz);
	}

	public static <T> T deserialize(File xml, Class<T> clazz) {
		return deserialize((Object) xml, clazz);
	}

	public static <T> T deserialize(Reader xml, Class<T> clazz) {
		return deserialize((Object) xml, clazz);
	}

	public static <T> T deserialize(Document xml, Class<T> clazz) {
		return deserialize((Object) xml, clazz);
	}

	public static <T> T deserialize(Node xml, Class<T> clazz) {
		return deserialize((Object) xml, clazz);
	}

	public static <T> T deserialize(Object xml, Class<T> clazz) {
		Unmarshaller u = newUnmarshaller(clazz);
		try {
            if (xml instanceof Reader) {
                return (T) u.unmarshal((Reader) xml);
            } else if (xml instanceof File) {
                return (T) u.unmarshal((File) xml);
            } else if (xml instanceof Node) {
                return u.unmarshal((Node) xml, clazz).getValue();
            } else {
                return (T) u.unmarshal(new StringReader(xml.toString()));
            }
		}
		catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> deserializeCollection(Collection<File> files, Class<T> clazz) {
		Unmarshaller u = newUnmarshaller(clazz);
		List<T> objects = new ArrayList<>();
		for (File file : files) {
			try {
				objects.add((T) u.unmarshal(file));
			} catch (Exception e) {
				throw new RuntimeException("Failed to unmarshal class " + clazz.getName() + " from file:\n" + file, e);
			}
		}
		return objects;
	}

	public static Unmarshaller newUnmarshaller(Class<?> clazz) { 
		Future<JAXBContext> contextTask = CONTEXTS.remove(clazz);
		try {
			JAXBContext jc = contextTask == null ? JAXBContext.newInstance(clazz) : contextTask.get();
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new XmlValidationHandler());
			return u;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e instanceof ExecutionException ? e.getCause() : e);
		} catch (JAXBException e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try (PrintStream ps = new PrintStream(os)) {
				e.printStackTrace(ps); 
				throw new RuntimeException("Failed to initialize unmarshaller for class " + clazz.getName() + "\n" + os);
			}
		}
	}

	public static Unmarshaller newUnmarshaller(Class<?> clazz, Schema schema) {
		Future<JAXBContext> contextTask = CONTEXTS.remove(clazz);
		try {
			JAXBContext jc = contextTask == null ? JAXBContext.newInstance(clazz) : contextTask.get();
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new XmlValidationHandler());
			u.setSchema(schema);
			return u;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e instanceof ExecutionException ? e.getCause() : e);
		} catch (JAXBException e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try (PrintStream ps = new PrintStream(os)) {
				e.printStackTrace(ps); 
				throw new RuntimeException("Failed to initialize unmarshaller for class " + clazz.getName() + "\n" + os);
			}
		}
	}


	public static String generateSchema(Class<?>... classes) {
		try {
			JAXBContext jc = JAXBContext.newInstance(classes);
			StringSchemaOutputResolver ssor = new StringSchemaOutputResolver();
			jc.generateSchema(ssor);
			return ssor.getSchemaString();
		}
		catch (IOException | JAXBException e) {
			throw new RuntimeException(e);
		}
	}
}