# kanji=漢字
# $Id: readme.txt 73448 2020-04-02 02:14:49Z maeshiro $

2007/04/03  新規作成(処理：東京都KNJA262　レイアウト：東京都KNJA262参考)

2008/03/27 成績テーブルの参照を制御。

2010/04/06  1.コンボの学年が２桁表示されない不具合を修正

2011/04/05  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2012/06/11  1.教育課程の修正に伴い、評定平均のSQLを修正した。

2012/06/28  1.プロパティuseCurriculumcdを追加

2014/05/26  1.更新/削除等のログ取得機能を追加

2015/04/03  1.useTestCountflg = TESTITEM_MST_COUNTFLG_NEW_SDIVの時、RECORD_RANK_SDIV_DATを使用する

2016/04/26  1.学年コンボの参照先にSCHREG_REGD_GDATを追加

2016/08/23  1.ファイル名の学年表記を「新+SCHREG_REGD_GDAT.GRADE_NAME1」に変更

2016/09/20  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/04/24  1.単位制かつプロパティー「useKeepGrade」が"1"のとき、ファイル名の進級学年名称をカット

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2018/03/13  1.CSVに学期,留年フラグ,旧学籍番号,旧学年,旧組,旧出席番号追加

2018/03/13  1.年を学年に、番を出席番号に文言変更

2020/04/02  1.最終学期は進行前校種の最上級生の学年を表示対象に追加
            2.CSV出力で最終学期はGRADE, HR_CLASS, ATTENDNO位置にOLD_GRADE、OLD_HR_CLASS, OLD_ATTENDNOを出力しない