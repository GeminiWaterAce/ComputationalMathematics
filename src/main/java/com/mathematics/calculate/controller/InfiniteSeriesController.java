/**
 * 
 */
package com.mathematics.calculate.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mathematics.calculate.constant.ComputerConstant;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author GeminiWaterAce
 *
 */
@RestController
@RequestMapping(value = "infiniteSeries")
public class InfiniteSeriesController {

	// private static final Logger logger =
	// LoggerFactory.getLogger(InfiniteSeriesController.class);

	private static final BigInteger CPU_CORES_BIG_INTEGER_VAL = BigInteger.valueOf(ComputerConstant.CPU_CORES);

	@ApiOperation(value = "调和级数求和")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "n", value = "求和项数", dataType = "BigInteger", required = true, paramType = "query"),
			@ApiImplicitParam(name = "scale", value = "小数位精度", dataType = "int", required = true, paramType = "query") })
	@RequestMapping(value = "/sumHarmonicSeries", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> sumHarmonicSeries(@RequestParam(value = "n") BigInteger n,
			@RequestParam(value = "scale") int scale) {
		LocalDateTime startLdt = LocalDateTime.now();
		List<BigInteger> startElementsTemp = new ArrayList<>(ComputerConstant.CPU_CORES + 1);
		for (int i = 0; i < ComputerConstant.CPU_CORES + 1; i++) {
			BigInteger item = BigInteger.valueOf(i);
			BigInteger startElement = n.multiply(item).divide(CPU_CORES_BIG_INTEGER_VAL);
			startElementsTemp.add(startElement);
		}
		Set<BigInteger> startElementSet = new LinkedHashSet<>(startElementsTemp);
		final List<BigInteger> startElements = new ArrayList<>(startElementSet);
		BigInteger denominatorTemp = BigInteger.ONE;// 求出分母
		BigInteger[] denominators = new BigInteger[startElements.size()];// 分母阶乘拆分为数组
		ForkJoinPool pool = new ForkJoinPool(startElements.size());
		pool.submit(() -> {
			startElements.parallelStream()
					.filter(startElement -> startElements.get(startElements.size() - 1).compareTo(startElement) > 0)
					.forEach(startElement -> {
						int index = startElements.indexOf(startElement);
						if (startElement != startElements.get(index + 1)) {
							BigInteger temp = BigInteger.ONE;
							for (BigInteger i = startElement; i
									.compareTo(startElements.get(index + 1)) < 0; i = BigInteger.ONE.add(i)) {
								BigInteger item = i.add(BigInteger.ONE);
								temp = temp.multiply(item);
							}
							denominators[index] = temp;
						}
					});

		}).join();
		for (int i = 0; i < denominators.length; i++) {
			if (denominators[i] == null) {
				continue;
			}
			denominatorTemp = denominatorTemp.multiply(denominators[i]);
		}
		final BigInteger denominator = denominatorTemp;
		BigInteger numerator = BigInteger.ZERO;// 求出分子
		BigInteger[] numerators = new BigInteger[startElements.size()];// 分子拆分为数组
		pool.submit(() -> {
			startElements.parallelStream()
					.filter(startElement -> startElements.get(startElements.size() - 1).compareTo(startElement) > 0)
					.forEach(startElement -> {
						int index = startElements.indexOf(startElement);
						if (startElement != startElements.get(index + 1)) {
							BigInteger temp = BigInteger.ZERO;
							for (BigInteger i = startElement; i
									.compareTo(startElements.get(index + 1)) < 0; i = BigInteger.ONE.add(i)) {
								BigInteger item = denominator.divide(item = i.add(BigInteger.ONE));
								temp = temp.add(item);
							}
							numerators[index] = temp;
						}
					});

		}).join();
		pool.shutdown();
		for (int i = 0; i < numerators.length; i++) {
			if (numerators[i] == null) {
				continue;
			}
			numerator = numerator.add(numerators[i]);
		}
		BigDecimal sum = new BigDecimal(numerator).divide(new BigDecimal(denominator), scale, BigDecimal.ROUND_CEILING);
		LocalDateTime endLdt = LocalDateTime.now();
		Duration duration = Duration.between(startLdt, endLdt);
		float millis = new Long(duration.toMillis()).floatValue();
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("n", Objects.toString(n));
		resultMap.put("scale", Objects.toString(scale));
		resultMap.put("sum", Objects.toString(sum));
		resultMap.put("time-consuming", Objects.toString(millis / 1000).concat("s"));
		return resultMap;
	}

	@ApiOperation(value = "单线程调和级数求和")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "n", value = "求和项数", dataType = "BigInteger", required = true, paramType = "query"),
			@ApiImplicitParam(name = "scale", value = "小数位精度", dataType = "int", required = true, paramType = "query") })
	@RequestMapping(value = "/sumHarmonicSeriesSingleThread", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> sumHarmonicSeriesSingleThread(@RequestParam(value = "n") BigInteger n,
			@RequestParam(value = "scale") int scale) {
		LocalDateTime startLdt = LocalDateTime.now();
		BigInteger denominator = BigInteger.ONE;// 求出分母
		for (BigInteger i = BigInteger.ZERO; i.compareTo(n) < 0; i = BigInteger.ONE.add(i)) {
			BigInteger item = i.add(BigInteger.ONE);
			denominator = denominator.multiply(item);
		}
		BigInteger numerator = BigInteger.ZERO;// 求出分子
		for (BigInteger i = BigInteger.ZERO; i.compareTo(n) < 0; i = BigInteger.ONE.add(i)) {
			BigInteger item = denominator.divide(i.add(BigInteger.ONE));
			numerator = numerator.add(item);
		}
		BigDecimal sum = new BigDecimal(numerator).divide(new BigDecimal(denominator), scale, BigDecimal.ROUND_CEILING);
		LocalDateTime endLdt = LocalDateTime.now();
		Duration duration = Duration.between(startLdt, endLdt);
		float millis = new Long(duration.toMillis()).floatValue();
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("n", Objects.toString(n));
		resultMap.put("scale", Objects.toString(scale));
		resultMap.put("sum", Objects.toString(sum));
		resultMap.put("time-consuming", Objects.toString(millis / 1000).concat("s"));
		return resultMap;
	}

}
