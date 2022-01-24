/*
 * $Id: 3dc58ed65a3996a8a4f5d7d7fa014f0290902b67 $
 *
 * 作成日: 2015/07/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 禁則処理
 */
public class KNJ_EditKinsoku {

    private static Log log = LogFactory.getLog("KNJ_EditKinsoku.class");
    
    private static final List<String> prevNumChars = Arrays.asList("\\", "$");
    private static final List<String> numCharsHankaku =  charStringList(".0123456789");
    private static final List<String> numCharsZenkaku =  charStringList("．０１２３４５６７８９０");
    private static final List<String> alphabetCharsHankaku = charStringList("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    //private static final List alphabetCharsZenkaku = charStringList("ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ");
    private static final List<String> hyphenationStartChars; // 行頭禁則文字
    private static final List<String> hyphenationEndChars; // 行末禁則文字
    
    private enum GROUP {
        NASI,
        HANKAKU,
        NUM_ZENKAKU,
        ALPHABET_ZENKAKU;
    }
    
    static {
    	hyphenationStartChars = Arrays.asList(
                "。", "｡",
                "、", "､",
                ".", "．",
                ",", "，",
                ")", "]", "}", "」", "）", "］", "｝", "〕", "〉", "＞", "》", "」", "』", "】", "’", "”",
                "・",
                "？",
                "！",
                "-", ":",
                "：",
                "ゝ", "ゞ", "ー",
                "ァ", "ィ", "ゥ", "ェ", "ォ", "ッ", "ャ", "ュ", "ョ", "ヮ", "ヵ", "ヶ",
                "ぁ", "ぃ", "ぅ", "ぇ", "ぉ", "っ", "ゃ", "ゅ", "ょ", "ゎ",
                "々"
        );

        hyphenationEndChars = Arrays.asList("(", "[", "{", "「", "（", "［", "｛", "〔", "〈", "＜", "《", "「", "『", "【");
    }
    
    private static GROUP getCharGroup(final String ch, final Map<String, Map<GROUP, List<String>>> sessionMap) {
        final String mapkey = "KINSOKU_GROUP_CHARS";
        if (null == sessionMap.get(mapkey)) {
            final Map<GROUP, List<String>> groupChars = new HashMap<GROUP, List<String>>();
            sessionMap.put(mapkey, groupChars);
//            if ("1".equals(sessionMap.get("useAlphabetCharsBlock"))) {
                groupChars.put(GROUP.HANKAKU, concat(numCharsHankaku, alphabetCharsHankaku));
                //groupChars.put(new Integer(GRP_ALPHABET_ZENKAKU), alphabetCharsZenkaku);
//            } else {
//                groupChars.put(new Integer(GRP_HANKAKU), numCharsHankaku);
//            }
            groupChars.put(GROUP.HANKAKU, numCharsZenkaku);
            //log.info(" groupChars = " + groupChars);
        }
        final Map<GROUP, List<String>> groupChars = sessionMap.get(mapkey);
        
        for (final Map.Entry<GROUP, List<String>> e : groupChars.entrySet()) {
            final Collection<String> cl = e.getValue();
            if (cl.contains(ch)) {
                final GROUP key = e.getKey();
                return key;
            }
        }
        return GROUP.NASI;
    }
    
    private static List<String> charStringList(final String s) {
        final List<String> rtn = new ArrayList<String>();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            rtn.add(String.valueOf(ch));
        }
        return rtn;
    }
    
    public static boolean _isDebug = false;
    public static int hyphenationTrialMax = 200;
    
    public KNJ_EditKinsoku() {
        log.fatal("$Revision: 64403 $");
    }

    /**
     * @param str
     * @return
     * @deprecated use KNJ_EditEdit.getMS932ByteLength;
     */
    public static int getMS932ByteCount(final String str) {
    	return getMS932ByteLength(str);
    }

    /**
     * 対象文字列が、チェックする文字列のリストから始まるか
     * @param str 対象文字列
     * @param tokenList チェックする文字列のリスト
     * @return 対象文字列が、チェックする文字列のリストのどれかから始まるならtrue、それ以外はfalse
     */
    private static boolean startsWith(final List<String> strList, final List<String> tokenList) {
        if (null == strList || strList.isEmpty()) {
            return false;
        }
        final String stringStart = strList.get(0);
        boolean rtn = false;
        for (final String token : tokenList) {
            if (stringStart.startsWith(token)) {
                rtn = true;
                break;
            }
        }
        return rtn;
    }
    
    /**
     * 対象文字列が、チェックする文字列のリストから始まるか
     * @param str 対象文字列
     * @param tokenList チェックする文字列のリスト
     * @return 対象文字列が、チェックする文字列のリストのどれかから始まるならtrue、それ以外はfalse
     */
    private static boolean endsWith(final List<String> strList, final List<String> tokenList) {
        if (null == strList || strList.isEmpty()) {
            return false;
        }
        final String stringEnd = strList.get(strList.size() - 1);
        boolean rtn = false;
        for (final String token : tokenList) {
            if (stringEnd.endsWith(token)) {
                rtn = true;
                break;
            }
        }
        return rtn;
    }

    /**
     * 対象文字列でチェックする文字列リストに含まれている連続文字の最初のインデクスを得る
     * @param strList 対象文字列
     * @param tokenList チェックする文字列リスト
     * @return 対象文字列でチェックする文字列リストに含まれている連続文字の最初のインデクス
     */
    private static int getSeqFirstIndexOfContained(final List<String> strList, final List<String> tokenList) {
        if (strList == null || strList.isEmpty() || tokenList == null) {
            return -1;
        }
        int idx = -1;
        for (int li = strList.size() - 1; li >= 0; li -= 1) {
            final String ch = strList.get(li);
            if (tokenList.contains(ch)) {
                idx = li;
            } else {
                break;
            }
        }
        return idx;
    }

    /**
     * 対象文字列でチェックする文字列リストに含まれていない文字の最後のインデクスを得る
     * @param strList 対象文字列
     * @param tokenList チェックする文字列リスト
     * @return 対象文字列でチェックする文字列リストに含まれていない文字の最後のインデクス
     */
    private static int getLastIndexOfNotContained(final List<String> strList, final List<String> tokenList) {
        if (strList == null || strList.isEmpty() || tokenList == null) {
            return -1;
        }
        int idx = -1;
        for (int li = strList.size() - 1; li >= 0; li -= 1) {
            final String ch = strList.get(li);
            if (!tokenList.contains(ch) && !ch.equals("\n")) {
                idx = li;
                break;
            }
        }
        return idx;
    }

    private static void debugStringList(final String comment, final String head, final List<List<String>> list) {
        if (!_isDebug) {
            return;
        }
        log.info(head + " " + comment);
        for (int i = 0; i < list.size(); i++) {
            final List line = list.get(i);
            log.info(head + "[" + i + "] = " + line);
        }
    }
    
    /**
     * 元文字列をLineBlockリストで返す
     * @param text 元文字列(not null)
     * @param keta 桁数
     * @param sessionMap 設定値マップ
     * @return LineBlockリスト
     */
    private static List<LineBlock> getLineBlockList(final String text, final int keta, final Map sessionMap) {
        final List<String> groupList = new ArrayList<String>(); // 文字のグループのリスト
        GROUP charGroupBefore = GROUP.NASI;
        StringBuffer currentNumCharsSeq = null;
        for (int idx = 0; idx < text.length(); idx += 1) {
            final String ch = text.substring(idx, idx + 1);
            final GROUP charGroup = getCharGroup(ch, sessionMap); // 連続したnumCharsにふくまれる文字をひとつのグループとする
            if (charGroup != GROUP.NASI) {
                if (charGroupBefore == GROUP.NASI || charGroupBefore != charGroup) {
                    if (charGroupBefore != GROUP.NASI) {
                        groupList.add(currentNumCharsSeq.toString());
                    }
                    currentNumCharsSeq = new StringBuffer();
                    if (charGroup == GROUP.HANKAKU) {
                        for (final ListIterator<String> it = groupList.listIterator(groupList.size()); it.hasPrevious();) {
                            final String prev = it.previous();
                            if (prevNumChars.contains(prev)) { // \, $等、頭に金額を意味する文字列は途中改行しない
                                currentNumCharsSeq.insert(0, prev);
                                it.remove();
                            } else {
                                break;
                            }
                        }
                    }
                }
                currentNumCharsSeq.append(ch);
            } else {
                if (charGroupBefore != GROUP.NASI) {
                    groupList.add(currentNumCharsSeq.toString());
//                    log.info(" groupList add = " + groupList.get(groupList.size() - 1));
                    currentNumCharsSeq = null;
                }
                if ("\t".equals(ch)) {
                    groupList.add("　　　　");
                } else {
                    groupList.add(ch);
                }
//                log.info(" groupList add = " + groupList.get(groupList.size() - 1));
            }
            charGroupBefore = charGroup;
        }
        if (charGroupBefore != GROUP.NASI) {
            groupList.add(currentNumCharsSeq.toString());
//            log.info(" groupList add = " + groupList.get(groupList.size() - 1));
            currentNumCharsSeq = null;
        }

//        if (_debug) {
//            log.info(" groupList = " + groupList);
//        }

        final List<LineBlock> blockList = new ArrayList<LineBlock>();
        LineBlock currentBlock; // 1行の文字グループのリスト List<List<String>>

        currentBlock = new LineBlock();
        blockList.add(currentBlock);
        for (int idx = 0; idx < groupList.size(); idx += 1) {
            final String ch = groupList.get(idx);
            if ("\n".equals(ch)) {
                currentBlock = new LineBlock();
                blockList.add(currentBlock);
            } else {
                final int bytelen = getMS932ByteLength(stringListToString(currentBlock.currentLine()) + ch);
                if (!currentBlock.currentLine().isEmpty() && bytelen > keta) {
                    currentBlock._lineList.add(new ArrayList<String>());
                }
                currentBlock.currentLine().add(ch);
            }
        }
        return blockList;
    }
    
    /**
     * 桁あふれする文字列のインデクスを返す
     * @param stringList 文字列のリスト
     * @param keta 桁
     * @param debugList
     * @return 桁あふれする文字列のインデクス。桁あふれしない場合は-1
     */
    private static int getKetaAfureIndex(final List<String> stringList, final int keta, final List<String> debugList) {
        final int strKeta = getMS932ByteLength(stringListToString(stringList));
        //log.info(" keta = " + strKeta + " (" + stringListToString(stringList) + "), 指定keta = " + keta);
        if (strKeta <= keta) {
            return -1;
        }
        int idx = -1;
        int accumBytes = 0;
        for (int i = 0; i < stringList.size(); i++) {
            final String s = stringList.get(i);
            final int mojiketa = getMS932ByteLength(s);
            if (accumBytes + mojiketa > keta) {
                idx = i;
                break;
            }
            accumBytes += mojiketa;
        }
        if (_isDebug) {
            debugList.add(" keta afure index " + keta + " / " + stringList + " = " + idx);
        }
        return idx;
    }
    
    /**
     * 文字列のリストを連結した文字列で返す
     * @param stringList 文字列のリスト
     * @return 文字列のリストを連結した文字列
     */
    private static String stringListToString(final List<String> stringList) {
        final StringBuffer stb = new StringBuffer();
        for (final String s : stringList) {
            stb.append(s);
        }
        return stb.toString();
    }

    private static String stringListToLogDebugString(final List<String> stringList) {
        final StringBuffer stb = new StringBuffer("\n");
        for (final String s : stringList) {
            stb.append(s).append("\n");
        }
        return stb.toString();
    }

    private static List<String> toStringList(final String str) {
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length(); i++) {
            list.add(String.valueOf(str.charAt(i)));
        }
        return list;
    }
    
    private static <E> List<E> singletonList(final E o) {
        final List<E> l = new ArrayList<E>();
        l.add(o);
        return l;
    }
    
    private static List<String> getLine(final List<List<String>> lineList, final int idx) {
        if (idx < lineList.size()) {
            return lineList.get(idx);
        }
        return Collections.emptyList();
    }
    
    private static void setLine(final List<List<String>> lineList, final int idx, final List<String> line) {
        if (lineList.size() <= idx) {
            for (int i = lineList.size(); i <= idx; i++) {
                lineList.add(null);
            }
        }
        lineList.set(idx, line);
    }
    
    private static <E> List<E> concat(final List<E> lineList1, final List<E> lineList2) {
        final List<E> rtn = new ArrayList<E>();
        rtn.addAll(lineList1);
        rtn.addAll(lineList2);
        return rtn;
    }
    
    private static class LineBlock {
        // 文字列グループのリスト(=1行)のリスト List<List<String>>
        final List<List<String>> _lineList;
        public LineBlock() {
            _lineList = new ArrayList<List<String>>();
            _lineList.add(new ArrayList<String>());
        }
        public List<String> currentLine() {
            return _lineList.get(_lineList.size() - 1);
        }
        public String toString() {
            return "Block(" + _lineList + ")";
        }
    }
    
    /**
     * 文字列を1行あたりの桁数で禁則処理した行のリストを返す
     * @param text 文字列
     * @param keta 1行あたりの桁数
     * @param gyo 行数
     * @return 文字列を1行あたりの桁数で禁則処理した行のリスト。要素数の最大数は行数。文字列がnullもしくはブランクの場合、空リスト
     */
    public static List<String> getTokenList(final String text, final int keta, final int gyo) {
        return getTokenList(text, keta, gyo, null);
    }

    /**
     * 文字列を1行あたりの桁数で禁則処理した行のリストを返す
     * @param text 文字列
     * @param keta 1行あたりの桁数
     * @param gyo 行数
     * @return 文字列を1行あたりの桁数で禁則処理した行のリスト。要素数の最大数は行数。文字列がnullもしくはブランクの場合、空リスト
     */
    public static List<String> getTokenList(final String text, final int keta, final int gyo, final Map sessionMap) {
        if (gyo <= 0) {
            return new ArrayList<String>();
        }
        List<String> tokenList = getTokenList(text, keta, sessionMap);
        if (tokenList.size() > gyo) {
            tokenList = tokenList.subList(0, gyo);
        }
        return tokenList;
    }

    /**
     * 文字列を1行あたりの桁数で禁則処理した行のリストを返す
     * @param text 文字列
     * @param keta 1行あたりの桁数
     * @return 文字列を1行あたりの桁数で禁則処理した行のリスト。文字列がnullもしくはブランクの場合、空リスト
     */
    public static List<String> getTokenList(final String text, final int keta) {
        return getTokenList(text, keta, null);
    }

    /**
     * 対象文言を移動した場合にスペースがやたら（とりあえず半角1文字分以外すべて）空くか
     * @param line 行
     * @param charBlockIdx 指定文字のインデクス
     * @param keta 行の文字桁
     * @return やたら空くtrue
     */
	private static boolean isLineFilled(final List<String> line, final int charBlockIdx, final int keta) {
		if (charBlockIdx == 1 && charBlockIdx < line.size()) {
			int blockKeta = 0;
			for (int i = 0; i < charBlockIdx; i++) {
				final String chr = line.get(i);
				blockKeta += getMS932ByteLength(chr);
			}
			blockKeta += getMS932ByteLength(line.get(charBlockIdx));
			return keta - blockKeta <= 1; 
		}
		return false;
	}

    /**
     * 文字列を1行あたりの桁数で禁則処理した行のリストを返す
     * @param text 文字列
     * @param keta 1行あたりの桁数
     * @param sessionMap 追加オプション
     * @return 文字列を1行あたりの桁数で禁則処理した行のリスト。文字列がnullもしくはブランクの場合、空リスト
     */
    public static List<String> getTokenList(final String text, final int keta, Map sessionMap) {
        if (text == null || text.length() == 0) {
            return new ArrayList<String>();
        }
        sessionMap = null == sessionMap ? new HashMap() : sessionMap;
        final String text1 = StringUtils.replace(StringUtils.replace(StringUtils.replace(text, "\u000b", ""), "\r\n", "\n"), "\r", "\n");
        if (getMS932ByteLength(text1) < keta && -1 == text1.indexOf('\n')) {
            return singletonList(text1);
        }
        final boolean isOutputDebug = "1".equals(sessionMap.get("outputDebug"));
        boolean setDebug = false;
        if (isOutputDebug && _isDebug == false) {
            _isDebug = true;
            setDebug = true;
        }
        if (_isDebug) {
            log.info(" hyphen-process string: " + text1);
            log.info(" hyphen-process keta: " + keta);
        }
        if (keta <= 1) {
        	String errorMessage = null;
        	if (keta == 1) {
				for (int i = 0; i < text.length(); i++) {
					final char ch = text.charAt(i);
					if (getMS932ByteLength(String.valueOf(ch)) > keta) {
						errorMessage = "keta = " + keta + "(" + ch + ")";
						break;
					}
				}
        	} else {
        		errorMessage = "keta = " + keta;
        	}
        	if (null != errorMessage) {
				try {
					throw new IllegalArgumentException(errorMessage); // スタックトレースを表示したいだけ
				} catch (Exception e) {
					log.fatal("exception!", e);
				}
				return new ArrayList();
        	}
        }

        final List<LineBlock> lineBlockList = getLineBlockList(text1, keta, sessionMap);
        
        int modifiedCount = 0;
        boolean modifiedOnce = false;
        
        for (int bidx = 0; bidx < lineBlockList.size(); bidx++) {
            final LineBlock g = lineBlockList.get(bidx);
            final List<List<String>> lineList = g._lineList;

            //debugStringList("string list", ">", lineList);
            boolean modified = true;

            lineloop:
            for (int lineidx = 0; lineidx < lineList.size();) {
                final int lineidx0 = lineidx;

                LinkedList<String> debugList = new LinkedList<String>();
                List<String> line;
                line = getLine(lineList, lineidx);
                if (_isDebug) {
                    if (modified) {
                        for (int i = 0; i < lineList.size(); i++) {
                            debugList.add(" lineidx = " + lineidx + " | line  [block " + bidx + "] [line " + i + "] = " + getLine(lineList, i));
                        }
                    }
                }
                if (lineList.size() > 200 || Thread.interrupted()) {
                    log.warn("couldn't be hyphen-processed correctly2 : keta = " + keta + ", text = " + text);
                    for (int i = 0; i < lineList.size(); i++) {
                        debugList.add(" lineidx = " + lineidx + " | line  [block " + bidx + "] [line " + i + "] = " + getLine(lineList, i));
                    }
                	break lineloop;
                }
                modified = false;

                // 行頭禁則 ... 行頭に行頭禁則文字が配置される場合、1行前の行から行頭禁則文字以外の文字を押し出す
                if (0 != lineidx && line.size() > 0 && startsWith(line, hyphenationStartChars)) {
                	final String check = "(" + lineidx + ")  行頭禁則対象：" + line;
            		if (_isDebug) {
            			debugList.add(check);
            		}
            		// 1行前の文字列から禁則文字以外の文字を探す
            		final List<String> befLine = getLine(lineList, lineidx - 1);
            		final int idxNotKinsokuChar = getLastIndexOfNotContained(befLine, hyphenationStartChars);
            		if (idxNotKinsokuChar == -1) {
            			// [禁則文字以外の文字が見つからない。]なら処理をしない
            			if (_isDebug) {
            				debugList.add("  直前の行に禁則文字以外の文字が見つからないため処理しない:" + befLine);
            			}
            		} else if (idxNotKinsokuChar == 0) {
            			// [禁則文字以外の文字が見つかったがインデクスが0]なら処理をしない
            			if (_isDebug) {
            				debugList.add("  直前の行の禁則文字以外の文字インデクスが0のため処理しない:" + befLine);
            			}
            		} else if (isLineFilled(befLine, idxNotKinsokuChar, keta)) {
            			// [禁則文字以外の文字が見つかりインデクスが0以上だがはまってる]なら処理をしない
            			if (_isDebug) {
            				debugList.add("  直前の行の禁則文字以外の文字インデクスが0以上だがはまってるため処理しない:" + befLine);
            			}
            		} else {
            			if (_isDebug) {
            				debugList.add("  行頭 bef [line " + (lineidx - 1) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx - 1));
            				debugList.add("  行頭 bef [line " + (lineidx - 0) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx));
            			}
            			setLine(lineList, lineidx - 1, befLine.subList(0, idxNotKinsokuChar));
            			setLine(lineList, lineidx - 0, concat(befLine.subList(idxNotKinsokuChar, befLine.size()), line));
            			modified = true;
            			modifiedOnce = true;
            			if (_isDebug) {
            				debugList.add("  行頭 aft [line " + (lineidx - 1) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx - 1));
            				debugList.add("  行頭 aft [line " + (lineidx - 0) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx));
            			}
            			lineidx -= 1;
            		}
                }

                line = getLine(lineList, lineidx);
                // 行末禁則 ... 行末に行末禁則文字が配置される場合、1行後の行に押し出す
                if (lineidx < lineList.size() - 1 && endsWith(line, hyphenationEndChars)) {
                	final String check = "(" + lineidx + ") 行末禁則対象：" + line;
                    debugList.add(check);
                    // 対象の文字列から禁則文字を探す
                    final int idxKinsokuChar = getSeqFirstIndexOfContained(line, hyphenationEndChars);
                    if (idxKinsokuChar == -1) {
                        // [禁則文字が見つからない。]なら処理をしない
                        log.warn(" 行に禁則文字が見つからないため処理しない:" + line);
                    } else if (idxKinsokuChar == 0) {
                        // [禁則文字が先頭]なら処理をしない
                        log.warn(" 行の禁則文字が先頭のため処理しない:" + line);
                    } else {
                        if (_isDebug) {
                            debugList.add("  行末 bef [line " + (lineidx + 0) + "](mod" + modifiedCount + ") " + getLine(lineList, lineidx));
                            debugList.add("  行末 bef [line " + (lineidx + 1) + "](mod" + modifiedCount + ") " + getLine(lineList, lineidx + 1));
                        }
                        setLine(lineList, lineidx + 0, line.subList(0, idxKinsokuChar));
                        setLine(lineList, lineidx + 1, concat(line.subList(idxKinsokuChar, line.size()), getLine(lineList, lineidx + 1)));
                        modified = true;
                        modifiedOnce = true;
                        if (_isDebug) {
                            debugList.add("  行末 aft [line " + (lineidx + 0) + "](mod" + modifiedCount + ") " + getLine(lineList, lineidx));
                            debugList.add("  行末 aft [line " + (lineidx + 1) + "](mod" + modifiedCount + ") " + getLine(lineList, lineidx + 1));
                        }
                    }
                }


                line = getLine(lineList, lineidx);
                // 桁あふれ対応 ... 桁あふれする場合、1行後の行に押し出す
                final int ketaAfureIndex = getKetaAfureIndex(line, keta, debugList);
                if (-1 != ketaAfureIndex) {
                    if (_isDebug) {
                        debugList.add("  桁あふれ bef [line " + (lineidx + 0) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx));
                        debugList.add("  桁あふれ bef [line " + (lineidx + 1) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx + 1));
                        debugList.add("  桁あふれ index = " + ketaAfureIndex);
                    }
                    if (0 == ketaAfureIndex) {
                        // 数値列等、1グループ目が行に収まらないため改行する
                        final String str = line.get(0);
                        final int splitIdx = getKetaAfureIndex(toStringList(str), keta, debugList); // 改行のインデクス
                        if (_isDebug) {
                            debugList.add("  桁あふれ split index = " + splitIdx);
                        }
                        setLine(lineList, lineidx + 0, singletonList(str.substring(0, splitIdx)));
                        setLine(lineList, lineidx + 1, concat(concat(singletonList(str.substring(splitIdx)), line.subList(1, line.size())), getLine(lineList, lineidx + 1)));
                        modified = true;
                        modifiedOnce = true;
                    } else {
                        setLine(lineList, lineidx + 0, line.subList(0, ketaAfureIndex));
                        setLine(lineList, lineidx + 1, concat(line.subList(ketaAfureIndex, line.size()), getLine(lineList, lineidx + 1)));
                        modified = true;
                        modifiedOnce = true;
                    }
                    if (_isDebug) {
                        debugList.add("  桁あふれ aft [line " + (lineidx + 0) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx));
                        debugList.add("  桁あふれ aft [line " + (lineidx + 1) + "](mod " + modifiedCount + ") " + getLine(lineList, lineidx + 1));
                    }
                }
                if (_isDebug) {
                    if (debugList.size() > 0) {
                        debugList.addFirst(" --------------------------- check lineidx = " + lineidx0 + "---------------------------");
                        log.info(stringListToLogDebugString(debugList));
                    }
                }
                if (modified) {
                    // line、lineListを修正したら次ループで再度チェックする
                    modifiedCount += 1;
                    if (modifiedCount > hyphenationTrialMax || Thread.interrupted()) {
                        log.warn("couldn't be hyphen-processed correctly : keta = " + keta + ", text = " + text);
                        break;
                    }
                } else {
                    lineidx += 1; // 処理を次の行に移す
                    modifiedCount = 0;
                }
            }
        }
        
        // 文字列のリストに変換
        final List<String> tokenList = new ArrayList<String>();
        for (int bidx = 0; bidx < lineBlockList.size(); bidx++) {
            final LineBlock g = lineBlockList.get(bidx);
            for (int i = 0; i < g._lineList.size(); i++) {
                tokenList.add(stringListToString(getLine(g._lineList, i)));
            }
        }
        if (modifiedOnce && _isDebug) {
            log.info("modified : " + stringListToLogDebugString(tokenList));
        }
        if (setDebug) {
            _isDebug = false;
        }
        return tokenList;
    }
}

// eof
