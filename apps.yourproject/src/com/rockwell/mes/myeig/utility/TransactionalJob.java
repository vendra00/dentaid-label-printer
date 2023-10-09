/**
 *
 */
package com.rockwell.mes.myeig.utility;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.services.TransactionInterceptor;

/**
 * Represents a transactional job where the {@link TransactionInterceptor} from FTPS is used to implement the
 * transactional behavior. Compared to the usage of the {@link TransactionInterceptor} directly this job
 * <ul>
 * <li>translates the checked exception thrown by {@link TransactionInterceptor} into an unchecked
 * {@link MESRuntimeException}
 * <li>offers the convenience method rollback() as a shortcut to the static method of {@link TransactionInterceptor}.
 * </ul>
 * Before using this class think about using FTPS services where you can use annotations for transactional behaviour.
 * <p>
 *
 * @author HPLang
 * @param <T> the return type of the transactional job.
 */
public abstract class TransactionalJob<T> {

    private static final Log LOGGER = LogFactory.getLog(TransactionalJob.class);

    public final T run() {
        try {
            return TransactionInterceptor.callInTransactionImpl(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return execute();
                }
            });
        } catch (Exception exc) {
            LOGGER.error("transaction interceptor threw an exception and therefore did a rollback: "
                    + ExceptionUtils.getStackTrace(exc));
            throw new MESRuntimeException(exc);
        }
    }

    protected void rollback() {
        TransactionInterceptor.setRollback();
    }

    protected abstract T execute();
}
