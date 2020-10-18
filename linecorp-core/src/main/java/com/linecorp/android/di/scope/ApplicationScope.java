
package com.linecorp.android.di.scope;

import java.lang.annotation.Documented;

import javax.inject.Scope;

/**
 * Dagger scope for dependencies that should only have a single instance created for the entire
 * application.
 */
@Scope
@Documented
public @interface ApplicationScope {}
