package de.xwic.etlgine.loader.database;

import java.util.List;

import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.loader.database.DatabaseLoader.Mode;
import de.xwic.etlgine.util.Validate;

/**
 * Utility class to perform various validations.
 * 
 * @author mbogdan
 *
 */
public class DatabaseLoaderValidators {

	/**
	 * Throws an ETLException if the loader parameters are not according to the operating mode.
	 */
	public static void validateParameters(final Mode mode, final List<String> pkColumns, final IIdentityManager identityManager,
			final String tablename) throws ETLException {

		Validate.notNull(tablename, "Regardless of the mode, the 'tablename' is mandatory.");

		if (mode == Mode.INSERT_OR_UPDATE) {
			Validate.notEmpty(
					pkColumns,
					"When running in INSERT_OR_UPDATE mode, at least one column name inside 'pkColumns' is needed to determine if the operation will be INSERT or UPDATE.");
			Validate.notNull(identityManager,
					"When running in INSERT_OR_UPDATE mode, the 'identityManager' is needed to determine if the operation will be INSERT or UPDATE.");
		}
		//TODO Bogdan - the 2 other modes...
	}
}
