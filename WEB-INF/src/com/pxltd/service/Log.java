package com.pxltd.service;

import com.microstrategy.utils.log.Logger;
import com.microstrategy.utils.log.LoggerConfigurator;

class Log extends LoggerConfigurator {
    public static final Logger logger = new Log().createLogger();
}
