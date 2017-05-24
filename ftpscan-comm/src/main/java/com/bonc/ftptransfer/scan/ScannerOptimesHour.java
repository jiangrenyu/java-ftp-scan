package com.bonc.ftptransfer.scan;

import com.bonc.ftptransfer.scan.service.FilePathService;
import com.bonc.ftptransfer.scan.service.impl.FilePathServiceImpl;
import com.bonc.ftptransfer.scan.task.DirectoryScanTaskByStatusV6;
import com.bonc.ftputil.bean.KafkaProducer;
import com.bonc.ftputil.util.DateUtil;
import com.bonc.ftputil.util.JdbcUtils;
import com.bonc.ftputil.util.OpTimeDirectoryAdapter;
import com.bonc.ftputil.util.PropertyConfig;
import com.bonc.ftputil.vo.FtpPath;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScannerOptimesHour {

	private static Logger LOG = LoggerFactory.getLogger(Scanner.class);

	private static PropertyConfig config = null;

	private static JdbcUtils jdbcUtil = null;

	private static String groupIdStr = null;

	private static String[] groupIds = null;

	private static String hourStr = null;

	private static String[] hours = null;

	private static boolean isThreadPoolLimit = false;

	private static KafkaProducer producer = null;

	private static int maxTaskTime;

	private static int scanInternal;

	private static int loginTimeout = 30000;

	/**
	 * 线程池默认大小为1
	 */
	private static int maxPoolSize = 1;

	private static ExecutorService pool = null;

	private static boolean isSFTP = false;

	private static int queryBatchSize = 100;// 批查询大小，默认为100

	public static void main(String[] args) {

		String usage = "Usage:FTPScanner -groupId groupId1,groupId2 -configPath <configPath> -dbPath <dbPath> -hours <hours>";

		if (args.length < 6) {
			System.err.println("缺少参数");
			System.out.println(usage);
			System.exit(-1);
		}

		for (int i = 0; i < args.length; i++) {
			if ("-groupId".equals(args[i])) {

				groupIdStr = args[++i];

				if (groupIdStr == null || groupIdStr.length() == 0 || groupIdStr.split(",").length == 0) {
					System.err.println("please set your groupIds ");
					System.exit(-1);
				} else {
					groupIds = groupIdStr.split(",", -1);
				}
			} else if ("-hours".equals(args[i])) {

				hourStr = args[++i];

				if (hourStr != null && hourStr.length() != 0) {
					hours = hourStr.split(",", -1);
				}
			} else if ("-configPath".equals(args[i])) {
				String configPath = args[++i];
				try {
					config = new PropertyConfig(configPath);
				} catch (Exception e) {
					System.err.println("can't found configPath" + configPath);
					System.exit(-1);
				}
			} else if ("-dbPath".equals(args[i])) {
				String dbPath = args[++i];
				try {

					jdbcUtil = new JdbcUtils(dbPath);
				} catch (Exception e) {
					System.err.println("create dataSource error ,please check dbPath:" + dbPath);
					System.exit(-1);
				}
			} else {
				throw new IllegalArgumentException("arg " + args[i] + " not recognized");
			}

		}

		String logDirPath = config.getValue("logDir");

		if (logDirPath == null || logDirPath.indexOf("log4j.properties") == -1) {
			System.out.println("请在配置文件中设置 log4j.properties的路径：logDir");
			System.exit(-1);
		} else {
			System.setProperty("logId", "" + groupIdStr + "_" + DateUtil.formatDate(new Date(), "yyyyMMddHHmmss"));

			PropertyConfigurator.configure(logDirPath);
		}

		try {
			isSFTP = Boolean.parseBoolean(config.getValue("isSFTP"));
		} catch (Exception e) {
			LOG.info("请在配置文件中设置 FTP 服务器的类型：isSFTP");
			System.exit(-1);
		}

		try {
			loginTimeout = Integer.parseInt(config.getValue("loginTimeout")) * 1000;
			if (loginTimeout < 10000) {
				LOG.info("登录超时时间至少为 10 s,请重新设置");
				System.exit(-1);
			}
		} catch (Exception e) {
			LOG.info("请在配置文件中设置 FTP 登录超时时间：loginTimeout");
			System.exit(-1);
		}

		try {
			queryBatchSize = Integer.parseInt(config.getValue("queryBatchSize"));
			if (queryBatchSize < 0) {
				LOG.info("请在配置文件中设置数据库批查询大小：queryBatchSize");
				System.exit(-1);
			}
		} catch (Exception e) {
			LOG.info("请在配置文件中设置数据库批查询大小：queryBatchSize");
			System.exit(-1);
		}

		try {
			isThreadPoolLimit = Boolean.parseBoolean(config.getValue("isThreadPoolLimit"));
		} catch (Exception e) {
			LOG.info("请在配置文件中设置是否限制线程池的大小：isThreadPoolLimit");
			System.exit(-1);
		}

		if (isThreadPoolLimit) {
			try {
				maxPoolSize = Integer.parseInt(config.getValue("maxPoolSize"));
				if (maxPoolSize < 1) {
					LOG.info("线程池的大小不能小于 1，请检查配置中的设置");
					System.exit(-1);
				}
			} catch (NumberFormatException e) {
				LOG.info("请在配置文件中设置线程池的大小：maxPoolSize");
				System.exit(-1);
			}
		}

		try {
			maxTaskTime = Integer.parseInt(config.getValue("maxTaskTime"));
			if (maxTaskTime < 30) {
				LOG.info("扫描一次任务的时间至少大于30s,请重新设置");
				System.exit(-1);
			}
		} catch (NumberFormatException e1) {
			LOG.info("请在配置文件中设置扫描一次任务所需要的时间：maxTaskTime");
			System.exit(-1);
		}

		try {
			scanInternal = Integer.parseInt(config.getValue("scanInternal"));
			if (scanInternal < 0) {
				LOG.info("扫描间隔必须大于0s,请重新设置");
				System.exit(-1);
			}
		} catch (NumberFormatException e1) {
			LOG.info("请在配置文件中设置任务扫描间隔时间：scanInternal");
			System.exit(-1);
		}

		String brokers = null;

		try {
			brokers = config.getValue("metadata.broker.list");
			if (brokers == null || brokers.length() == 0 || brokers.split(",").length == 0) {
				LOG.info("请在配置文件中设置kafka接收消息的broker地址列表");
				System.exit(-1);
			}
		} catch (Exception e) {
			LOG.info("请在配置文件中设置kafka接收消息的broker地址列表");
			System.exit(-1);
		}

		String format = null;

		try {
			format = config.getValue("scan.dir.nowOptimeFormat");
			if (format == null || "".equals(format)) {
				LOG.info("账期小时格式不能为空,请在配置文件中设置账期小时格式");
				System.exit(-1);
			}
		} catch (Exception e) {
			LOG.info("账期小时格式不能为空,请在配置文件中设置账期小时格式");
			System.exit(-1);
		}

		Map conf = new HashMap<>();
		conf.putAll(config.getProperties());
		producer = new KafkaProducer(conf);

		FilePathService filePathService = new FilePathServiceImpl(jdbcUtil);

		HashMap<String, List<FtpPath>> pathMap = filePathService.queryFtpPath(groupIds);

		if (pathMap == null || pathMap.size() == 0) {
			LOG.info("未查询到组 " + groupIds + " 下存在需要扫描的目录");
			System.exit(-1);
		}

		OpTimeDirectoryAdapter adapter = new OpTimeDirectoryAdapter(pathMap);
		// 当前及前一小时账期
		String[] nowOptime = null;
		// 指定账期
		String[] optimes = null;
		if (hours != null && hours.length == 1) {
			optimes = DateUtil.getDistanceHours(hours[0], format);
		} else if (hours != null && hours.length > 1) {
			optimes = hours;
		}
		// 当前及前一小时账期对应的FtpPath列表
		HashMap<String, List<FtpPath>> nowOptimeFtpPath = null;
		// 指定账期对应的FtpPath列表
		HashMap<String, List<FtpPath>> optimesFtpPath = adapter.getOpTimeDirectory(optimes);

		boolean isOptimes = true;
		while (true) {
			LOG.info("开始新一轮的扫描任务...");

			if (isThreadPoolLimit) {
				pool = Executors.newFixedThreadPool(maxPoolSize);
			} else {
				pool = Executors.newCachedThreadPool();
			}

			if (isOptimes && optimesFtpPath != null && optimesFtpPath.size() != 0) {
				for (Map.Entry<String, List<FtpPath>> entry : optimesFtpPath.entrySet()) {
					String key = entry.getKey();
					pool.execute(new DirectoryScanTaskByStatusV6(isSFTP, loginTimeout, queryBatchSize,
							optimesFtpPath.get(key), jdbcUtil, producer));
				}
				isOptimes = false;
			}

			nowOptime = DateUtil.getBeforeTwoDayHour(format);
			nowOptimeFtpPath = adapter.getOpTimeDirectory(nowOptime);
			for (Map.Entry<String, List<FtpPath>> entry : nowOptimeFtpPath.entrySet()) {
				String key = entry.getKey();
				pool.execute(new DirectoryScanTaskByStatusV6(isSFTP, loginTimeout, queryBatchSize,
						nowOptimeFtpPath.get(key), jdbcUtil, producer));
			}

			try {
				// 通知关闭
				pool.shutdown();
				if (!pool.awaitTermination(maxTaskTime, TimeUnit.SECONDS)) {
					LOG.info("线程池中的任务执行时间已经超过设定的最大值:" + maxTaskTime + " ,强制停止任务执行...");
					pool.shutdownNow();
				}
			} catch (InterruptedException e) {
				// awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
				LOG.info("等待线程池中的任务执行完成时出现中断异常,强制停止任务执行...", e);
				pool.shutdownNow();
			}

			try {
				LOG.info("进入睡眠,时间为：" + scanInternal + " 秒");
				Thread.sleep(scanInternal * 1000);
			} catch (InterruptedException e) {
				LOG.info("等待下一轮任务执行时出现中断异常,直接开始执行下一轮任务...", e);
			}

		}

	}

}
