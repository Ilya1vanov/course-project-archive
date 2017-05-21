package com.ilya.ivanov.security.registration;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Sample {@link CredentialsPolicy} implementation which read policy items from
 * properties.file (In conjunction with Spring's {@link PropertySource}).
 */
@Component
@Configurable
@Scope("prototype")
public class PropertiesFileCredentialsPolicy implements CredentialsPolicy {
	@Value("${com.ilya.ivanov.credential.alwaysGenerateOnRegistration}")
	private boolean alwaysGenerateOnRegistration;
	
	@Value("${com.ilya.ivanov.credential.defaultPasswordGeneratorName}")
	private Class<PasswordGenerator> passwordGeneratorType;
	
	@Override
	public boolean alwaysGenerateOnRegistration() {
		return alwaysGenerateOnRegistration;
	}

	@Override
	public Class<PasswordGenerator> defaultPasswordGeneratorType() {
		return passwordGeneratorType;
	}
}
