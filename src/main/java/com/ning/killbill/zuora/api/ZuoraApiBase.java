package com.ning.killbill.zuora.api;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.log.LogService;

import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
import com.ning.killbill.zuora.util.Either;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.Converter;
import com.ning.killbill.zuora.zuora.IdentityConverter;
import com.ning.killbill.zuora.zuora.PaymentConverter;
import com.ning.killbill.zuora.zuora.PoolException;
import com.ning.killbill.zuora.zuora.RefundConverter;
import com.ning.killbill.zuora.zuora.ZuoraApi;
import com.ning.killbill.zuora.zuora.ZuoraConnection;
import com.ning.killbill.zuora.zuora.ZuoraErrorConverter;

public class ZuoraApiBase  {


    protected final ZuoraErrorConverter errorConverter = new ZuoraErrorConverter();
    protected final PaymentConverter paymentConverter = new PaymentConverter();
    protected final RefundConverter refundConverter = new RefundConverter();
    protected final IdentityConverter<String> stringConverter = new IdentityConverter<String>();
    protected final ConnectionPool pool;
    protected final ZuoraApi zuoraApi;
    protected final String instanceName;
    protected final LogService logService;
    protected final ZuoraPluginDao zuoraPluginDao;
    protected final DefaultKillbillApi defaultKillbillApi;

    public ZuoraApiBase(final ConnectionPool pool, final ZuoraApi zuoraApi, final LogService logService, final OSGIKillbill osgiKillbill,
                                 final ZuoraPluginDao zuoraPluginDao, final String instanceName) {
        this.pool = pool;
        this.zuoraApi = zuoraApi;
        this.instanceName = instanceName;
        this.logService = logService;
        this.zuoraPluginDao = zuoraPluginDao;
        this.defaultKillbillApi = new DefaultKillbillApi(osgiKillbill, logService);
    }

    protected <T> T withConnection(final ConnectionCallback<T> callback) {
        final ZuoraConnection connection = pool.borrowFromPool();

        try {
            return callback.withConnection(connection);
        } finally {
            if (connection != null) {
                try {
                    pool.returnToPool(connection);
                } catch (PoolException ex) {
                    logService.log(LogService.LOG_INFO, "Error while returning a zuora connection to the pool", ex);
                }
            }
        }
    }


    protected static <S1, S2, T1, T2> Either<T1, T2> convert(final Either<S1, S2> source, final Converter<S1, T1> converter1, final Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        } else {
            return Either.right(converter2 == null ? null : converter2.convert(source.getRight()));
        }
    }

    protected static <S1, S2, T1, T2> Either<T1, List<T2>> convertList(final Either<S1, List<S2>> source, final Converter<S1, T1> converter1, final Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        } else {
            final List<T2> objs = new ArrayList<T2>();
            for (final S2 sourceObj : source.getRight()) {
                objs.add(converter2.convert(sourceObj));
            }
            return Either.right(objs);
        }
    }

    protected static interface ConnectionCallback<T> {

        T withConnection(ZuoraConnection connection);
    }
}
