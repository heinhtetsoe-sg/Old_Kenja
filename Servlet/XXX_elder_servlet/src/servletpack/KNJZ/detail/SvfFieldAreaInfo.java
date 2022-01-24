package servletpack.KNJZ.detail;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import nao_package.svf.Vrw32alp;

public class SvfFieldAreaInfo {

	private static final Log log = LogFactory.getLog(SvfFieldAreaInfo.class);

	private static final String revision = "$Revision: 69117 $ $Date: 2019-08-06 18:30:40 +0900 (火, 06 8 2019) $"; // CVSキーワードの取り扱いに注意
	
	public final Param _param = new Param(); 
	
	public static class Param {
		public boolean _setKinsoku;
		public boolean _isOutputDebug;
		public boolean _isOutputDebugKinsoku;
		private Map _formFieldInfoMap = new HashMap();
		private Map _sessionCache = new HashMap();
		
        public Map formFieldInfoMap(final String form, final String fieldInfoKey) {
            final String formFieldKey = form + "." + fieldInfoKey;
            return Util.getMappedHashMap(_formFieldInfoMap, formFieldKey);
        }
	}
    
    private static String getString(final String field, final Map<String, String> row) {
        if (null == row || row.isEmpty()) {
            return null;
        }
        if (!row.containsKey(field)) {
            throw new IllegalStateException("no such field : " + field + " / " + row);
        }
        return row.get(field);
    }
    
    private static Double getDouble(final String field, final Map<String, String> row) {
        final String s = getString(field, row);
        if (null == s) {
            return null;
        }
        if (NumberUtils.isNumber(s)) {
            return Double.valueOf(s);
        }
        log.error("not number : " + s + "/" + field + " / " + row);
        return null;
    }
    
    private static Integer getInteger(final String field, final Map<String, String> row) {
        final String s = getString(field, row);
        if (null == s) {
            return null;
        }
        if (NumberUtils.isNumber(s)) {
            return Integer.valueOf(s);
        }
        log.error("not digits : " + s + "/" + field + " / " + row);
        return null;
    }
    
    private static int getInt(final String field, final Map<String, String> row) {
        final Integer s = getInteger(field, row);
        if (null == s) {
            return 0;
        }
        return s.intValue();
    }

//    private static String defstr(final Object s) {
//        return null == s ? "" : StringUtils.defaultString(s.toString());
//    }
//    
//    private static String defstr(final Object s1, final String s2) {
//        return null == s1 ? s2 : StringUtils.defaultString(s1.toString(), s2);
//    }
    
    public Map getModifyFieldInfoMap(final Map formFieldInfoMap, final String form, final String fieldname, final int repeatCount, final int preferKeta, final String data) {
    	final ModifyParam modifyParam = new ModifyParam();
    	modifyParam._repeatCount = repeatCount;
    	modifyParam._usePreferPoint = false;
    	modifyParam._preferKeta = preferKeta;
        return getModifyFieldInfoMap(formFieldInfoMap, form, fieldname, modifyParam, data);
    }
	
	public static class ModifyParam {
		public int _repeatCount;
		public boolean _usePreferPoint;
		public boolean _usePreferPointThanKeta;
		/** <code>_usePreferPointThanKeta = true</code>の場合の文字ポイント */
		public double _preferPointThanKeta;
		/** <code>_usePreferPoint = true</code>の場合の文字ポイント */
		public double _preferPoint;
		/** <code>_usePreferPointThanKeta = false && _usePreferPoint = false</code>の場合の桁数 */
		public int _preferKeta;
		/** 最大文字ポイント */
		public double _preferPointMax = 12.0;
		public final Map<String, Map<String, String>> _otherParam = new HashMap(); 
		
		public boolean isRepeat() {
	        return _repeatCount > 1;
		}
	}

    public Map getModifyFieldInfoMap(final Map formFieldInfoMap, final String form, final String fieldname, final ModifyParam modifyParam, final String data) {
    	final String FIELD_AREA_CANDIDATE_INFO = "FIELD_AREA_CANDIDATE_INFO";

        final Map<String, Map<Tuple<BigDecimal, Integer>, List<SvfField>> > fieldInfoMap = _param.formFieldInfoMap(form, fieldname);
        if (null == fieldInfoMap.get(FIELD_AREA_CANDIDATE_INFO)) {
            final Map areaInfo = getAreaInfo(formFieldInfoMap, form, fieldname, modifyParam);
            fieldInfoMap.put(FIELD_AREA_CANDIDATE_INFO, getFieldAreaCandidateInfo(modifyParam, areaInfo));
            if (_param._isOutputDebug) {
                for (final Iterator it = Util.getMappedMap(fieldInfoMap, FIELD_AREA_CANDIDATE_INFO).entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final List l = (List) e.getValue();
                    log.info(" " + fieldname + " candidate " + e.getKey() + "\n" + Util.listString(l, 0));
                }
            }
        }
        final Map<Tuple<BigDecimal, Integer>, List<SvfField>> candidates = Util.getMappedMap(fieldInfoMap, FIELD_AREA_CANDIDATE_INFO);
        
        final TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>> validCandidates = filterValidCandidates(modifyParam, data, candidates);
        
        final Map modifyFieldInfoMap = new HashMap();
        if (!validCandidates.isEmpty()) {
            final TreeMap<BigDecimal, Tuple<BigDecimal, Integer>> preferTupleMap;
            if (modifyParam._usePreferPointThanKeta) {
                preferTupleMap = getPreferPointTupleMap(validCandidates, modifyParam._preferPointThanKeta);
            } else if (modifyParam._usePreferPoint) {
                preferTupleMap = getPreferPointTupleMap(validCandidates, modifyParam._preferPoint);
            } else {
                preferTupleMap = getPreferKetaTupleMap(validCandidates, modifyParam._preferKeta);
            }
            
            if (_param._isOutputDebug) {
                log.info(" ############## " + fieldname + " prefer = " + preferTupleMap);
            }
            
            final Tuple<BigDecimal, Integer> fontSizeKeta = preferTupleMap.get(preferTupleMap.firstKey());
            
            final BigDecimal fontSizeBd = fontSizeKeta._first;
            final int candKeta = fontSizeKeta._second.intValue();
            final List<SvfField> fields = validCandidates.get(fontSizeKeta);
            if (modifyParam.isRepeat()) {
                modifyFieldInfoMap.put("FIELD_NAME", fieldname);
                modifyFieldInfoMap.put("FIELD_KETA", String.valueOf(candKeta));
                modifyFieldInfoMap.put("FIELD_LINE", String.valueOf(fields.size()));

                for (int ri = 0; ri < fields.size(); ri++) {
                    final Map attrMap = new HashMap();
                    final SvfField field = fields.get(ri);
                    attrMap.put("FIELD_ATTR", "Y=" + String.valueOf(field.y()) + ",Size=" + fontSizeBd + ",Keta=" + candKeta);
                    Util.getMappedList(modifyFieldInfoMap, "REPEAT").add(attrMap);
                }

            } else {
                // 未実装
            }
        }
        return modifyFieldInfoMap;
    }

    private TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>> filterValidCandidates(final ModifyParam modifyParam, final String data, final Map<Tuple<BigDecimal, Integer>, List<SvfField>> candidates) {
        final TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>> validCandidates = new TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>>();
        final List<Map.Entry<Tuple<BigDecimal, Integer>, List<SvfField>> > candEnts = Util.reverse(candidates.entrySet());
        for (int ei = 0; ei < candEnts.size(); ei++) {
            boolean isLast = ei == candEnts.size() - 1;
            final Map.Entry<Tuple<BigDecimal, Integer>, List<SvfField>> e = candEnts.get(ei);
            final Tuple<BigDecimal, Integer> fontSizeKeta = e.getKey();
            int candKeta = fontSizeKeta._second.intValue();
            if (candKeta % 2 == 1) {
                candKeta -= 1;
            }
            final int candMoji = candKeta / 2;
            final List<SvfField> fields = e.getValue();
            final int candGyo = fields.size();
            
            final List<String> tokens = Util.getTokenList(_param, data, candMoji * 2);

			if (modifyParam._usePreferPointThanKeta) {
                if (tokens.size() <= candGyo || isLast) {
                    validCandidates.put(e.getKey(), fields);
                }
			} else if (modifyParam._usePreferPoint) {
            	validCandidates.put(e.getKey(), fields);
            } else {
                if (tokens.size() <= candGyo || isLast) {
                    validCandidates.put(e.getKey(), fields);
                }
            }
        }
        return validCandidates;
    }
    
    private TreeMap<BigDecimal, Tuple<BigDecimal, Integer>> getPreferPointTupleMap(final TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>> candidates, final double point) {
        final TreeMap preferPointMap = new TreeMap();
        for (final Tuple<BigDecimal, Integer> fontSizePointPreferSearch : candidates.keySet()) {
            final BigDecimal pointPrefer = fontSizePointPreferSearch._first;
            double res = pointPrefer.doubleValue() - point;
            preferPointMap.put(new BigDecimal(Math.abs(res)).setScale(2, BigDecimal.ROUND_HALF_UP), fontSizePointPreferSearch);
        }
        return preferPointMap;
    }
    
    private TreeMap<BigDecimal, Tuple<BigDecimal, Integer>> getPreferKetaTupleMap(final TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>> candidates, final int keta) {
        final TreeMap<BigDecimal, Tuple<BigDecimal, Integer>> preferKetaMap = new TreeMap();
        for (final Tuple<BigDecimal, Integer> fontSizeKetaPreferSearch : candidates.keySet()) {
            final Integer ketaPrefer = fontSizeKetaPreferSearch._second;
            int res = ketaPrefer.intValue() - keta;
            if (res >= 0) { // 指定桁未満を含まない
                preferKetaMap.put(new BigDecimal(Math.abs(res)), fontSizeKetaPreferSearch);
            }
        }
        if (preferKetaMap.isEmpty()) {
            // 候補が空の場合、指定桁未満のフィールドも含む
            for (final Tuple<BigDecimal, Integer> fontSizeKetaPreferSearch : candidates.keySet()) {
                final Integer ketaPrefer = fontSizeKetaPreferSearch._second;
                int res = ketaPrefer.intValue() - keta;
                preferKetaMap.put(new BigDecimal(Math.abs(res)), fontSizeKetaPreferSearch);
            }
        }
        return preferKetaMap;
    }
    
    private Map getAreaInfo(final Map formInfoFieldMap, final String form, final String fieldname, final ModifyParam modifyParam) {
    	int maxKeta = 0;
        final List<SvfField> resultList = Util.getMappedList(getSearchFieldResult(_param, formInfoFieldMap, form, fieldname, "Regex", "SvfField"), "resultList"); // 総合所見
        for (final SvfField field : resultList) {
            maxKeta = Math.max(field._fieldLength, maxKeta);
        }
        final List<SvfField> fieldList = new ArrayList();
        for (final SvfField field : resultList) {
            if (maxKeta == field._fieldLength) {
                fieldList.add(field);
            }
        }
        int x1Max = Integer.MIN_VALUE;
        double yMin = Integer.MAX_VALUE;
        double yMax = Integer.MIN_VALUE;
        double fontSizeMin = Double.MAX_VALUE;
        for (final SvfField field : fieldList) {
//            log.debug(" " + fieldname + " area info field = " + field);
            x1Max = Math.max(field.x(), x1Max);
//            yMin = Math.min(field.y() / NAZONO_RATE, yMin);
//            yMax = Math.max(field.y() / NAZONO_RATE, yMax);
            yMin = Math.min(field.y(), yMin);
            yMax = Math.max(field.y(), yMax);
            fontSizeMin = Math.min(field.size(), fontSizeMin);
        }
        final double charSize = fontSizeMin;
        final int keta = maxKeta;
        
        double width = KNJSvfFieldModify.fieldWidth("getAreaInfo " + fieldname, charSize, 0, keta);
        int areaHeight;
        int y2 = 0;
        if (modifyParam.isRepeat()) {
            areaHeight = (int) Math.floor(KNJSvfFieldModify.charHeightPixel("getAreaInfo " + fieldname, fontSizeMin) * modifyParam._repeatCount);
            y2 = (int) (yMin + areaHeight);
        } else {
            areaHeight = (int) (yMax + Math.floor(KNJSvfFieldModify.charHeightPixel("getAreaInfo " + fieldname, fontSizeMin)));
            y2 = (int) (yMax + (int) KNJSvfFieldModify.charHeightPixel("getAreaInfo " + fieldname, fontSizeMin));
        }
        double x2 = x1Max + width;
        final Tuple<Integer, Integer> rightXLowerY = getRightXLowerY(form, fieldname);
        if (null != rightXLowerY) {
    		if (_param._isOutputDebug) {
    			log.info("  svfForm " + form + "." + fieldname + " = " + rightXLowerY + " (calcByFontVal x = " + x2 + ", y = " + y2 + ")");
    		}
        	x2 = rightXLowerY._first - 10;
    		width = x2 - x1Max;
        	y2 = rightXLowerY._second;
    		areaHeight = (int) (y2 - yMax);
        } else {
        	SvfField x2y2 = (SvfField) Util.getMappedMap(formInfoFieldMap, form).get(fieldname + "_X2Y2");
        	if (null != x2y2) {
        		if (_param._isOutputDebug) {
        			log.info("  x2 diff = " + (x2y2.x() - x2) + " (x2 = " + x2 + ") , y2diff = " + (x2y2.y() - y2) + " (y2 = " + y2 + ")");
        		}
        		x2 = x2y2.x();
        		y2 = x2y2.y();
        		width = x2 - x1Max;
        		areaHeight = (int) (y2 - yMax);
        	} else if (modifyParam._otherParam.containsKey(form + "." + fieldname)) {
        		final Map fieldInfoMap = (Map) modifyParam._otherParam.get(form + "." + fieldname);
        		if (_param._isOutputDebug) {
        			log.info("  set parameter X2Y2 " + fieldInfoMap);
        		}
        		final String x2Str = (String) fieldInfoMap.get("X2");
        		final String y2Str = (String) fieldInfoMap.get("Y2");
        		if (NumberUtils.isNumber(x2Str)) {
        			x2 = Double.parseDouble(x2Str);
        			width = x2 - x1Max;
        		}
        		if (NumberUtils.isNumber(y2Str)) {
        			y2 = (int) Double.parseDouble(y2Str);
        			areaHeight = (int) (y2 - yMax);
        		}
        	}
        }
        if (_param._isOutputDebug) {
            log.info(" fieldarea : " + fieldname + " = x1Max = " + x1Max + ", width = " + width + ", x2 = " + x2 + ", yMin = " + yMin + ", fontSizeMin = " + fontSizeMin + " keta = " + maxKeta + ", repeatCount = " + modifyParam._repeatCount + ", areaHeight = " + areaHeight + ", y2 = " + y2);
        }
        
        final Map areaInfo = new HashMap();
//        areaInfo.put("X1", String.valueOf(x1Max));
//        areaInfo.put("X2", String.valueOf(x2));
//        areaInfo.put("Y2", String.valueOf((int) y2));
        areaInfo.put("WIDTH", String.valueOf(width));
        areaInfo.put("Y1", String.valueOf((int) yMin));
        areaInfo.put("FONTSIZE", String.valueOf(fontSizeMin));
        areaInfo.put("KETA", String.valueOf(maxKeta));
        areaInfo.put("HEIGHT", String.valueOf((int) areaHeight));
        areaInfo.put("REPEATCOUNT", String.valueOf(modifyParam._repeatCount));
        areaInfo.put("FIELDLIST", fieldList);
        areaInfo.put("FIELDAREA_NAME", fieldname);
        return areaInfo;
    }
    
    private Tuple<Integer, Integer> getRightXLowerY(final String formname, final String fieldname) {
    	int x = -1;
    	int y = -1;
    	try {
    		if (!_param._sessionCache.containsKey(formname)) {
        		Vrw32alp svf = new Vrw32alp();
        		final String path = svf.getPath(formname);
        		final File formFile = new File(path);
        		
        		SvfForm svfForm = new SvfForm(formFile);
        		svfForm._debug = _param._isOutputDebug;
        		if (svfForm.readFile()) {
        			_param._sessionCache.put(formname, svfForm);
        		} else {
        			_param._sessionCache.put(formname, null);
        		}
    		}
    		
    		SvfForm svfForm = (SvfForm) _param._sessionCache.get(formname);
    		if (null != svfForm) {
				SvfForm.Field field = svfForm.getField(fieldname);
				if (null != field) {
					SvfForm.Line rightLine = svfForm.getNearestRightLine(field._position);
					if (null == rightLine) {
						log.info(" no right line in " + field._position);
					} else {
						x = rightLine._end._x;
					}
					SvfForm.Line lowerLine = svfForm.getNearestLowerLine(field._position);
					if (null == rightLine) {
						log.info(" no right line in " + field._position);
					} else {
						y = lowerLine._end._y;
					}
				}
    		}
    	} catch (Throwable e) {
    		if (_param._isOutputDebug) {
    			log.error("exception!", e);
    		}
    	}
    	if (-1 == x || -1 == y) {
    		return null;
    	}
    	return Tuple.of(x, y);
    }
    
    private static BigDecimal pointBd(final double point) {
    	return new BigDecimal(point).setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private Map<Tuple<BigDecimal, Integer>, List<SvfField>> getFieldAreaCandidateInfo(final ModifyParam modifyParam, final Map areaInfo) {
    	final double width = getDouble("WIDTH", areaInfo).doubleValue();
        final int yMin = getInt("Y1", areaInfo);
        final double fontSizeMin = getDouble("FONTSIZE", areaInfo).doubleValue();
        final int maxKeta = getInt("KETA", areaInfo);
        final int areaHeight = getInt("HEIGHT", areaInfo);
        final int repeatCount = getInt("REPEATCOUNT", areaInfo);
        final List<SvfField> fieldList = Util.getMappedList(areaInfo, "FIELDLIST");
        final String fieldAreaName = "fieldArea " + getString("FIELDAREA_NAME", areaInfo);
        
        final Map<Tuple<BigDecimal, Integer>, List<SvfField>> areaCandidateInfo = new TreeMap<Tuple<BigDecimal, Integer>, List<SvfField>>();
        final BigDecimal fontSizeMinBd = pointBd(fontSizeMin);
        final List<SvfField> setFieldList;
		if (modifyParam.isRepeat()) {
            final double fontSizeVar = fontSizeMin;
            final BigDecimal fontSizeBd = pointBd(fontSizeVar);
            final double charSize = fontSizeVar;
            
            final double keta1w = KNJSvfFieldModify.fieldWidth("", charSize, 0, 1);
            final Integer newKeta = new Integer((int) Math.floor(width / keta1w));
            if (_param._isOutputDebug) {
                log.info(" font size " + fontSizeVar + " in width = " + width + ", keta1w = " + keta1w + ", newKeta = " + newKeta);
            }
            final double fontHeight0 = KNJSvfFieldModify.charHeightPixel("", fontSizeVar);
            final double subt = fontSizeVar > fontSizeMin ? 0 : (((KNJSvfFieldModify.charHeightPixel(fieldAreaName + " charpoint1", fontSizeVar) - KNJSvfFieldModify.charHeightPixel(fieldAreaName + " fontSizeMin", fontSizeMin))) / 2);
            final double fontHeight = fontHeight0 - subt;
            
            final List<SvfField> newFieldList = new ArrayList<SvfField>();
            if (modifyParam.isRepeat()) {
                double blkY = 0;
                final SvfField field = fieldList.get(0);
                for (int i = 1; i <= repeatCount; i++) {
                    if (blkY + fontHeight > areaHeight) {
                        if (_param._isOutputDebug) {
                        	if (i < repeatCount) {
                        		log.info(" " + fieldAreaName + " has repeatCount " + repeatCount + " but use max " + i + "(areaHeight = " + areaHeight + ", printed hegiht = " + (blkY + fontHeight));
                        	}
                        }
                        break;
                    }
                    final Map attrs = Util.keyValueArrayToMap(new Object[] {SvfField.AttributeY, String.valueOf(pointBd(yMin + blkY)), SvfField.AttributeSize, fontSizeBd.toString(), SvfField.AttributeKeta, newKeta.toString()});
                    final SvfField newField = field.set(attrs);
                    newFieldList.add(newField);
                    blkY += fontHeight;
                }
            }
            setFieldList = newFieldList;

        } else {
        	setFieldList = fieldList;
        }
        final List<Double> fontCandidate;
		if (modifyParam._usePreferPoint) {
			final double preferPointRange = 0.3;
			if (modifyParam._preferPoint - preferPointRange <= fontSizeMinBd.doubleValue() && fontSizeMinBd.doubleValue() <= modifyParam._preferPoint) {
				areaCandidateInfo.put(Tuple.of(fontSizeMinBd, new Integer(maxKeta)), setFieldList);
			}
			fontCandidate = KNJSvfFieldModify.charPointCandidate(modifyParam._preferPoint - preferPointRange, modifyParam._preferPoint);
		} else {
			areaCandidateInfo.put(Tuple.of(fontSizeMinBd, new Integer(maxKeta)), setFieldList);
			fontCandidate = KNJSvfFieldModify.charPointCandidate(fontSizeMin, modifyParam._preferPointMax);
		}

        if (_param._isOutputDebug) {
        	log.info(" font candidate = " + fontCandidate);
        }
        for (final double fontSizeVar : fontCandidate) {
            final BigDecimal fontSizeBd = pointBd(fontSizeVar);
            
            final double keta1w = KNJSvfFieldModify.fieldWidth(fieldAreaName, fontSizeVar, 0, 1); // ?
            final Integer newKeta = new Integer((int) Math.floor(width / keta1w));
            final double charPoint = fontSizeVar;
            final double fontHeight0 = KNJSvfFieldModify.charHeightPixel(fieldAreaName, charPoint);
            final double charPoint1 = fontSizeVar;
            final double subt = fontSizeVar > fontSizeMin ? 0 : (((KNJSvfFieldModify.charHeightPixel(fieldAreaName + " charpoint1_", charPoint1) - KNJSvfFieldModify.charHeightPixel(fieldAreaName + " fontSizeMin_", fontSizeMin))) / 2);
            final double fontHeight = fontHeight0 - subt;
            if (_param._isOutputDebug) {
            	log.info(" font size " + fontSizeVar + " in width = " + width + ", keta1w = " + keta1w + ", newKeta = " + newKeta + ", fontHeight = " + fontHeight + " (" + fontHeight0 + ")");
            }
            final int gap = 2; // 2ポイント余裕をとる
            final List<SvfField> newFieldList = new ArrayList<SvfField>();
            if (modifyParam.isRepeat()) {
                double blkY = 0;
                final SvfField field = fieldList.get(0);
                for (int i = 1; i <= repeatCount; i++) {
                    if (blkY + fontHeight + gap > areaHeight) {
                        if (_param._isOutputDebug) {
                        	log.info(" " + fieldAreaName + " has repeatCount " + repeatCount + " but use max " + (i - 1) + " (area.y2 = " + (yMin + areaHeight) + ", printed y2 = " + (yMin + blkY) + ", over = " + (yMin + blkY + fontHeight));
                        }
                        break;
                    }
                    final Map attrs = Util.keyValueArrayToMap(new Object[] {SvfField.AttributeY, String.valueOf(yMin + blkY), SvfField.AttributeSize, fontSizeBd.toString(), SvfField.AttributeKeta, newKeta.toString()});
                    final SvfField newField = field.set(attrs);
                    newFieldList.add(newField);
                    blkY += fontHeight;
//                    if (_param._isOutputDebug) {
//                        log.info(" " + fieldAreaName + " " + i + ": y = " + blkY);
//                    }
                }

            } else {
                int blkY = 0;
                for (final SvfField field : fieldList) {
                    if (blkY + fontHeight > areaHeight) {
                        break;
                    }
                    final Map attrs = Util.keyValueArrayToMap(new Object[] {SvfField.AttributeY, String.valueOf(yMin + blkY), SvfField.AttributeSize, fontSizeBd.toString(), SvfField.AttributeKeta, newKeta.toString()});
                    final SvfField newField = field.set(attrs);
                    newFieldList.add(newField);
                    blkY += fontHeight;
                }
            }
            areaCandidateInfo.put(Tuple.of(fontSizeBd, newKeta), newFieldList);
        }
        return areaCandidateInfo;
    }
    
    public static Map getSearchFieldResult(final Param param, final Map formFieldInfoMap, final String form, final String fieldPart, final String searchMethod, final String target) {
    	if (null == form || null == formFieldInfoMap.get(form)) {
            log.warn(" not set currentForm : " + form);
            return new HashMap();
        }
        final List resultList = new ArrayList();
        final String cacheKey = "search:" + target + ":" + searchMethod + ":" + form + "." + fieldPart;
        final Map cacheMap = Util.getMappedMap(formFieldInfoMap, "Cache");
        if (null != cacheMap.get(cacheKey)) {
            return Util.keyValueArrayToMap(new Object[] {"cached", "true", "resultList", cacheMap.get(cacheKey)});
        }
        if ("IndexOf".equals(searchMethod)) {
            final Map<String, SvfField> fieldInfo = (Map) formFieldInfoMap.get(form);
            for (final Map.Entry<String, SvfField> e : fieldInfo.entrySet()) {
                final SvfField field = e.getValue();
                if (null == field._name) {
                    continue;
                }
                if (-1 < field._name.toUpperCase().indexOf(fieldPart.toUpperCase())) {
                    if ("SvfField".equals(target)) {
                        resultList.add(field);
                    } else if ("Name".equals(target)) {
                        resultList.add(field._name);
                    } else {
                        throw new IllegalArgumentException("target:" + target);
                    }
                }
            }
            Collections.sort(resultList);
            cacheMap.put(cacheKey, resultList);
        } else if ("Regex".equals(searchMethod)) {
            try {
                final Pattern pat = new Perl5Compiler().compile(fieldPart, Perl5Compiler.CASE_INSENSITIVE_MASK);
                
                final Map<String, SvfField> fieldInfo = (Map) formFieldInfoMap.get(form);
                for (final Map.Entry<String, SvfField> e : fieldInfo.entrySet()) {
                    final SvfField field = e.getValue();
                    if (null == field._name) {
                        continue;
                    }
                    final Perl5Matcher matcher = new Perl5Matcher();
                    if (matcher.matches(field._name, pat)) {
                        final MatchResult result = matcher.getMatch();
                        if ("SvfField".equals(target)) {
                            resultList.add(field);
                        } else if ("Group1".equals(target) && result.groups() > 0) {
                            if (null == result.group(1)) {
                                log.fatal("none of group1 : " + result.group(0));
                            } else {
                                resultList.add(result.group(1));
                            }
                        } else if ("Name".equals(target)) {
                            resultList.add(field._name);
                        } else {
                            throw new IllegalArgumentException("target:" + target);
                        }
                    }
                }
                Collections.sort(resultList);
                cacheMap.put(cacheKey, resultList);
            } catch (Exception e) {
                log.warn("exception!", e);
            }
        } else {
            throw new IllegalArgumentException("searchMethod:" + searchMethod);
        }
        
        // キャッシュされていなければデバッグ表示
        if (resultList.size() == 0) {
            log.warn(" " + cacheKey + " result empty.");
        } else {
            if (param._isOutputDebug) {
                for (final Iterator it = resultList.iterator(); it.hasNext();) {
                    final Object v = it.next();
                    log.debug(" " + cacheKey + " result = " + v);
                }
            }
        }

        return Util.keyValueArrayToMap(new Object[] {"cached", null, "resultList", cacheMap.get(cacheKey)});
    }
    

    private static class Tuple<L extends Comparable<L>, R extends Comparable<R>> implements Comparable<Tuple<L, R>> {
        final L _first;
        final R _second;
        private Tuple(final L first, final R second) {
            _first = first;
            _second = second;
        }
        public static <L1 extends Comparable<L1>, R1 extends Comparable<R1>> Tuple<L1, R1> of(final L1 first, final R1 second) {
            return new Tuple(first, second);
        }
        public int compareTo(final Tuple<L, R> to) {
            int cmp;
            if (null == _first) {
                return 1;
            } else if (null == to._first) {
                return -1;
            }
            cmp = _first.compareTo(to._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second) {
                return 1;
            } else if (null == to._second) {
                return -1;
            }
            cmp = _second.compareTo(to._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    
    public static class KNJSvfFieldModify {

//        private final String _fieldname; // フィールド名
//        private final int _width;   //フィールドの幅(ドット)
//        private final int _height;  //フィールドの高さ(ドット)
//        private final int _ystart;  //開始位置(ドット)
//        private final int _minnum;  //最小設定文字数
//        private final int _maxnum;  //最大設定文字数
        
        private static final double dpi = 400.0;
        private static final double pointPerInch = 72;

        // 標準4ポイントから変更
        public static final double[][] charPointTable = {
                {14.80,15.00, 7.469},
                {14.41,14.69, 7.287},
                {14.10,14.32, 7.059},
                {13.71,13.93, 6.897},
                {13.41,13.56, 6.729},
                {13.00,13.36, 6.534},
                {12.60,12.93, 6.360},
                {12.30,12.49, 6.186},
                {11.91,12.20, 5.980},
                {11.60,11.80, 5.806},
                {11.21,11.43, 5.625},
                {10.80,11.08, 5.446},
                {10.50,10.75, 5.279},
                {10.10,10.34, 5.070},
                { 9.80,10.05, 4.898},
                { 9.41, 9.67, 4.715},
                { 9.00, 9.35, 4.534},
                { 8.71, 8.96, 4.353},
                { 8.30, 8.61, 4.176},
                { 8.00, 8.21, 3.991},
                { 7.61, 7.84, 3.822},
                { 7.21, 7.53, 3.633},
                { 6.90, 7.13, 3.455},
                { 6.50, 6.75, 3.279},
                { 6.21, 6.40, 3.090},
                { 5.80, 6.05, 2.927},
                { 5.40, 5.69, 2.719},
                { 5.11, 5.31, 2.544},
                { 4.71, 4.96, 2.361},
                { 4.40, 4.60, 2.183},
                { 4.00, 4.24, 2.002},
                { 3.61, 3.89, 1.823},
                { 3.31, 3.53, 1.639},
                { 2.90, 3.17, 1.463},
                { 2.61, 2.81, 1.286},
                { 2.20, 2.45, 1.098},
                { 1.81, 2.09, 1.098},
                { 1.50, 1.73, 0.741},
                { 1.10, 1.37, 0.563},
        };
        
        // 標準10ポイントから変更
//        public static final double[][] charPointTable = {
//                {14.80,15.00, 7.500},
//                {14.41,14.69, 7.347},
//                {14.10,14.32, 7.200},
//                {13.71,13.93, 7.059},
//                {13.41,13.56, 6.792},
//                {13.00,13.36, 6.667},
//                {12.60,12.93, 6.429},
//                {12.30,12.49, 6.316},
//                {11.91,12.20, 6.102},
//                {11.60,11.80, 5.902},
//                {11.21,11.43, 5.714},
//                {10.80,11.08, 5.538},
//                {10.50,10.75, 5.373},
//                {10.10,10.34, 5.217},
//                { 9.80,10.05, 5.000},
//                { 9.41, 9.67, 4.865},
//                { 9.00, 9.35, 4.675},
//                { 8.71, 8.96, 4.500},
//                { 8.30, 8.61, 4.286},
//                { 8.00, 8.21, 4.138},
//                { 7.61, 7.84, 3.934},
//                { 7.21, 7.53, 3.750},
//                { 6.90, 7.13, 3.564},
//                { 6.50, 6.75, 3.396},
//                { 6.21, 6.40, 3.214},
//                { 5.80, 6.05, 3.025},
//                { 5.40, 5.69, 2.857},
//                { 5.11, 5.31, 2.667},
//                { 4.71, 4.96, 2.483},
//                { 4.40, 4.60, 2.308},
//                { 4.00, 4.24, 2.130},
//                { 3.61, 3.89, 1.946},
//                { 3.31, 3.53, 1.765},
//                { 2.90, 3.17, 1.586},
//                { 2.61, 2.81, 1.406},
//                { 2.20, 2.45, 1.224},
//                { 1.81, 2.09, 1.043},
//                { 1.50, 1.73, 0.863},
//                { 1.10, 1.37, 0.683},
//        };
        
        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final String debugString, final double charPoint, final int upperOrLower) {
            return charPointEnabled(debugString, charPoint, upperOrLower) * dpi / pointPerInch;
        }
        
        public static List<Double> charPointCandidate(final double min, final double max) {
            final List<Double> candidateList = new ArrayList<Double>();
            final double[][] table = charPointTable();
            for (int i = table.length - 1; i >= 0; i--) {
                if (table[i][0] > max) {
                    break;
                }
                if (table[i][0] < min) {
                    continue;
                }
                candidateList.add(new Double(table[i][0]));
            }
            return candidateList;
        }
        
        public static double[][] charPointTable() {
        	return charPointTable;
        }
        
        public static double charPointEnabled(final String debugString, final double charPoint, final int upperOrLower) {
            final double[][] table = charPointTable();
            final int arrayIdx = charPointArrayIdx(debugString, charPoint, upperOrLower);
            double charPointEnabled = -1.0;
            if (-1 == arrayIdx) {
            	charPointEnabled = 0.1;
            } else {
            	charPointEnabled = table[arrayIdx][1];
            }
            return charPointEnabled;
        }
        
        public static double charWidthPointEnabled(final String debugString, final double charPoint) {
            final double[][] table = charPointTable();
            final int arrayIdx = charPointArrayIdx(debugString, charPoint, 0);
            double charWidthtEnabled = -1.0;
            if (-1 == arrayIdx) {
            	charWidthtEnabled = 0.1;
            } else {
            	charWidthtEnabled = table[arrayIdx][2];
            }
            return charWidthtEnabled;
        }
        
        private static int charPointArrayIdx(final String debugString, final double charPoint, final int upperOrLower) {
            final double[][] table = charPointTable();
            int idx = -1;
            if (charPoint < 1.10) {
            	try {
            		throw new IllegalArgumentException(" font too small:" + charPoint + " (" + debugString + ")"); // スタックトレースを表示したいだけ...
            	} catch (Exception e) {
            		log.warn(e.getMessage(), e);
            	}
            } else {
                if (upperOrLower == -1) {
                    for (int i = 1; i < table.length; i++) {
                        if (table[i][0] <= charPoint) {
                        	idx = i - 1;
                        	break;
                        }
                    }
                } else if (upperOrLower == 0) {
                    for (int i = 0; i < table.length; i++) {
                        if (table[i][0] <= charPoint) {
                        	idx = i;
                        	break;
                        }
                    }
                } else if (upperOrLower == 1) {
                    // 1段下
                    for (int i = 0; i < table.length - 1; i++) {
                        if (table[i][0] <= charPoint) {
                        	idx = i + 1;
                        	break;
                        }
                    }
                }
            }
//            log.warn(" charpoint = " + charPoint + " => " + charPointEnabled);
            return idx;
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final String debugString, final double charSize, final int upperOrLower, final int keta) {
            return charWidthPointEnabled(debugString, charSize) * dpi / pointPerInch * keta;
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharPoint(final int pixel) {
            return pixel * pointPerInch / dpi;
        }

//        /**
//         *  Ｙ軸の設定
//         *  引数について  int hnum   : 出力位置(行)
//         */
//        public float getYjiku(int hnum, double charSize) {
//            float jiku = 0;
//            try {
//                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
//            } catch (Exception ex) {
//                log.error("setRetvalue error!", ex);
//                log.debug(" jiku = " + jiku);
//            }
//            return jiku;
//        }

        /**
         *  文字サイズを設定
         */
        public static double retFieldPoint(int dotWidth, int num) {
            return (float) Math.round((double) dotWidth / (num / 2) * pointPerInch / dpi * 10) / 10;
//            final double rtn = new BigDecimal((double) width / (num / 2) * pointPerInch / dpi).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
//            log.info(" width = " + width + ", num = " + num + " => charwidth point = " + rtn);
//            log.info(" *** " + fieldWidth(rtn, 1, num));
//            return charPointEnabled(rtn, 1);
        }
        
        public static double charHeightPixel(final String debugString, final double charSize) {
            return charPointToPixel(debugString, charSize, 0);
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        public static float retFieldY(int height, double charSize) {
            return (float) Math.round(((double) height - charHeightPixel("retFieldY", charSize)) / 2);
        }
        
//        public String toString() {
//            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
//        }
    }

    private static class Util {
        
        private static List<String> getTokenList(final Param param, final String strx, final int keta) {
            if (param._setKinsoku) {
            	if (param._isOutputDebugKinsoku) {
            		KNJ_EditKinsoku._isDebug = true;
            	}
                List<String> tokenList = KNJ_EditKinsoku.getTokenList(strx, keta);
            	if (param._isOutputDebugKinsoku) {
            		KNJ_EditKinsoku._isDebug = false;
            	}
				return tokenList;
            }
            return getTokenList(strx, keta);
        }
        
        private static List<String> getTokenList(final String source0, final int bytePerLine) {
            if (source0 == null || source0.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final String source = StringUtils.replace(StringUtils.replace(source0, "\r\n", "\n"), "\r", "\n");

            final List<String> tokenList = new ArrayList();        //分割後の文字列の配列
            int startIndex = 0;                         //文字列の分割開始位置
            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
            for (int idx = 0; idx < source.length(); idx += 1) {
                if (source.charAt(idx) == '\n') {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = 0;
                    startIndex = idx + 1;
                } else {
                    final int sbytelen = KNJ_EditEdit.getMS932ByteLength(source.substring(idx, idx + 1));
                    byteLengthInLine += sbytelen;
                    if (byteLengthInLine > bytePerLine) {
                        tokenList.add(source.substring(startIndex, idx));
                        byteLengthInLine = sbytelen;
                        startIndex = idx;
                    }
                }
            }
            if (byteLengthInLine > 0) {
                tokenList.add(source.substring(startIndex));
            }

            return tokenList;
        } //String get_token()の括り

        private static <E> List<E> reverse(final Collection<E> col) {
            final LinkedList<E> rtn = new LinkedList<E>();
            for (final ListIterator<E> it = new ArrayList(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }
        
        private static <E, F> List<F> getMappedList(final Map<E, List<F>> map, final E key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList<F>());
            }
            return map.get(key1);
        }

        private static <E, F, G> Map<F, G> getMappedMap(final Map<E, Map<F, G>> map, final E key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap<F, G>());
            }
            return map.get(key1);
        }

        private static <E, F, G> Map<F, G> getMappedHashMap(final Map<E, Map<F, G>> map, final E key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap<F, G>());
            }
            return map.get(key1);
        }
        
        private static <K, V> TreeSet<V> getMappedTreeSet(final Map<K, TreeSet<V>> map, final K key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeSet<V>());
            }
            return map.get(key1);
        }

        private static Map keyValueArrayToMap(final Object[] keyValue) {
            if (null == keyValue || keyValue.length % 2 != 0) {
                throw new IllegalArgumentException("keyValue = " + keyValue);
            }
            final Map rtn = new HashMap();
            for (int i = 0; i < keyValue.length; i += 2) {
                rtn.put(keyValue[i], keyValue[i + 1]);
            }
            return rtn;
        }
        
        private static Integer parseInteger(final String s) {
            if (NumberUtils.isNumber(s) && -1 == s.indexOf(".")) {
                return Integer.valueOf(s);
            }
            if (!StringUtils.isBlank(s)) {
                log.warn("try to parseInteger not-integer-value:" + s);
            }
            return null;
        }
        
        private static int parseIntSafe(final String s, final int def) {
            if (NumberUtils.isNumber(s) && -1 == s.indexOf(".")) {
                return Integer.parseInt(s);
            }
            if (!StringUtils.isBlank(s)) {
                log.warn("try to parseInt not-int-value:" + s);
            }
            return def;
        }
        
        private static String listString(final Collection coll, final int depth) {
            if (null == coll) {
                return "null";
            } else if (coll.size() == 0) {
                return "[]";
            }
            final String space = StringUtils.repeat("  ", depth);
            final StringBuffer stb = new StringBuffer();
            stb.append(space).append("[").append("\n");
            String comma = "  ";
            final List list = new ArrayList(coll);
            for (int i = 0; i < list.size(); i++) {
                final Object o = list.get(i);
                stb.append(space).append(comma);
                if (o instanceof List) {
                    stb.append(listString((List) o, depth + 1));
                } else if (null == o) {
                    stb.append("null");
                } else if (o instanceof Map.Entry) {
                    final Map.Entry e = (Map.Entry) o;
                    final Object v = e.getValue();
                    stb.append(space).append(e.getKey()).append("=").append(v instanceof Collection ? listString((Collection) v, depth + 1) : v);
                } else if (list.size() == 0) {
                    stb.append("[]");
                } else {
                    stb.append(o.toString());
                }
                stb.append("\n");
                comma = ", ";
            }
            stb.append(space).append("]").append("\n");
            return stb.toString();
        }
    }
}
