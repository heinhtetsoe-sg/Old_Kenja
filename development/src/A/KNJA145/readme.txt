// kanji=漢字
// $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

KNJA145
身分証明書
-----
近大の KNJA141 を元に作成
-----
学校：法政
-----
仕様をreadme.txtに記述
====================== ここから =====================================

◆Ａ４用紙(新入生)
//年度・学期・・・ログイン年度・学期の次学期
//有効期限・・・・学期開始日付～学期開始月末日
//生徒リスト・・・下記テーブルより取得
CLASS_FORMATION_DAT-- SCHREGNO, GRADE, HR_CLASS, ATTENDNO, REMAINGRADE_FLG
SCHREG_BASE_MST-- NAME
FRESHMAN_DAT-- NAME
SCHREG_REGD_HDAT-- HR_NAME
※１学年で、REMAINGRADE_FLGがゼロまたはNULLの場合、FRESHMAN_DATの氏名。それ以外の場合、SCHREG_BASE_MSTの氏名。
CASE WHEN GRADE = '01' AND VALUE(REMAINGRADE_FLG,'0') = '0'
     THEN FRESHMAN_DAT.NAME
     ELSE SCHREG_BASE_MST.NAME

◆Ａ４用紙(在籍)
//年度・学期・・・ログイン年度・学期
//有効期限・・・・学期開始日付～学期開始月末日
//生徒リスト・・・下記テーブルより取得
SCHREG_REGD_DAT-- SCHREGNO, GRADE, HR_CLASS, ATTENDNO
SCHREG_BASE_MST-- NAME
SCHREG_REGD_HDAT-- HR_NAME

◆カード(在籍)
//年度・学期・・・ログイン年度・学期
//有効期限・・・・学期コード='9'の学期開始日付～学期終了日付
//生徒リスト・・・下記テーブルより取得
SCHREG_REGD_DAT-- SCHREGNO, GRADE, HR_CLASS, ATTENDNO
SCHREG_BASE_MST-- NAME
SCHREG_REGD_HDAT-- HR_NAME

//印刷パラメータ
OUTPUT・・・・・・・フォーム種別(1:Ａ４用紙(新入生), 2:カード(在籍), 3:Ａ４用紙(在籍))
DISP・・・・・・・・1:個人,2:クラス
category_selected ・学籍番号または学年＋組
YEAR・・・・・・・・年度
GAKKI ・・・・・・・学期
TERM_SDATE・・・・・有効期限(開始)
TERM_EDATE・・・・・有効期限(終了)
DOCUMENTROOT・・・・パス '/usr/local/deve_ktest/src'
GRADE_HR_CLASS・・・学年＋組

====================== ここまで =====================================

2007/05/22  出力対象を溜め込む処理を修正した。
2007/09/11  出力対象一覧に空白行が作られる不具合を修正した。

2009/09/09  1.ラジオボタンにラベルを追加した。

2014/01/21  1.パラメータuseAddrField2追加

2014/09/08  1.style指定修正
