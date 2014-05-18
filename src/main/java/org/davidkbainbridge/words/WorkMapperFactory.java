package org.davidkbainbridge.words;

/**
 * Simply factory class that can be used to create instances of mappers based on
 * a system property.
 */
public class WorkMapperFactory {
	static public final String WORK_MAPPER_PROPERTY = "work.mapper";
	static public final String DEFAULT_WORK_MAPPER = WordListStepWorkMapper.class
			.getName();

	public <V> WorkMapper newWorkMapper() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		String mapperClassName = System.getProperty(WORK_MAPPER_PROPERTY,
				DEFAULT_WORK_MAPPER);
		Class<?> found = Class.forName(mapperClassName);
		if (WorkMapper.class.isAssignableFrom(found)) {
			return (WorkMapper) found.newInstance();
		}

		throw new ClassCastException(String.format(
				"Cannot cast instance of '%s' to WorkMapper", found.getClass()
						.getName()));
	}

}
