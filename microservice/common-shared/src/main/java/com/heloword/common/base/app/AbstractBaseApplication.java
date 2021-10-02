package com.heloword.common.base.app;

import org.springframework.context.annotation.ComponentScan;

// add this component scan so that it will scan the shared common packages
@ComponentScan({"com.heloword"})
public abstract class AbstractBaseApplication {
}
