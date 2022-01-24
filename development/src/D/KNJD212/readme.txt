// kanji=漢字
// $Id: readme.txt 56580 2017-10-22 12:35:29Z maeshiro $

KNJD212
類型グループ平均計算処理（類型グループ毎）
-----
近大高校の KNJD210K を元に作成
-----
学校：近大高校
-----
2007/03/01 仕様をreadme.txtに記述
====================== ここから =====================================

以下は仕様
■主なテーブル
1.TYPE_GROUP_HR_DAT
2.TYPE_GROUP_MST
3.KIN_RECORD_DAT
4.SCHREG_REGD_DAT
5.SCHREG_BASE_MST
6.SCHREG_TRANSFER_DAT
7.TYPE_ASSES_HDAT

■類型平均算出（再計算）
・下記1～4の再計算を順番に行う。
1.TYPE_GROUP_HR_DAT の XXX_SUM, XXX_CNT の再計算
-- NULL で更新
-- テーブル(1～6)より XXX_SUM, XXX_CNT を算出
-- 算出した XXX_SUM, XXX_CNT で更新
2.TYPE_GROUP_MST の XXX_SUM, XXX_CNT の再計算
-- NULL で更新
-- テーブル(1)より XXX_SUM, XXX_CNT を算出
-- 算出した XXX_SUM, XXX_CNT で更新
3.TYPE_GROUP_MST の XXX_TYPE_ASSES_CD, XXX_DATE の再計算
-- NULL で更新
-- テーブル(2,7)より(7)の TYPE_ASSES_CD を算出
-- 算出した TYPE_ASSES_CD と SYSDATE() で更新
-- テーブル(2)の TYPE_ASSES_CD を算出
-- 算出した TYPE_ASSES_CD と SYSDATE() で更新
4.KIN_RECORD_DAT の JUDGE_PATTERN の再計算
-- NULL で更新
-- テーブル(1,2)より(2)の XXX_TYPE_ASSES_CD を算出
-- 算出した XXX_TYPE_ASSES_CD で更新

====================== ここまで =====================================

2007/06/20 s-yama  処理済リストの類型グループ名取得に年度も見るよう変更した。

2007/09/20 alp o-naka
★ 仮評定の処理を追加した。
-- 「科目読替処理」と「類型平均算出」において、
-- 第３学年で、１・２学期平均を実行する場合、学年平均も実行する
2007/10/19 alp o-naka
★ 下記の「仮評定の処理」を削除した。
-- 「科目読替処理」と「類型平均算出」において、
-- 第３学年で、１・２学期平均を実行する場合、学年平均も実行する
-- ※不要な処理と判断されたため

2012/06/29  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/12/04  1.欠課時数によりA～Cパターンを０で更新する。

2013/12/05  1.欠課時数による更新に、読替科目の処理を追加

2013/12/10  1.欠課時数によりA～Cパターンを０で更新する。処理をカット

2014/03/06  1.KK,KS等のデータは対象外とする。(カウントに入れない、合算は０)

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)
