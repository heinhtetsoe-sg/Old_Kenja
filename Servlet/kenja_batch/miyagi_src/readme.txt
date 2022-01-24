# kanji=漢字
# $Id: readme.txt 56574 2017-10-22 11:21:06Z maeshiro $
# 宮城県　グループウエアとの連携について　修正履歴

2016/04/11

【修正内容】
StaffInfo
★ 職員の抽出条件を変更
-- 変更前：職員コードが９で始まる職員は作成しない。
-- 変更後：職員コードが９で始まる職員は作成する。
Param
★ 実行日付からの学期取得について、学期範囲外の場合、直近の学期をセット。

【プログラム】
・miyagi.jar

2016/05/16

【修正内容】
GradeInfo (1.1)
★ 初版。学年情報テーブル
ClassInfo (1.1)
★ 初版。クラス情報テーブル
StudentInfo (1.1)
★ 初版。生徒情報テーブル
Main (1.2)
★ 連携テーブル追加
-- 学年情報テーブル
-- クラス情報テーブル
-- 生徒情報テーブル

【納品プログラム】
・miyagi.jar

2017/03/24

【修正内容】
StaffInfo.class (1.3)
★ 職員の異動情報追加
-- 教育委員会DBのEDBOARD_STAFF_WORK_HIST_DATを参照
-- 異動区分（TO_DIV）と異動日付（TO_DATE）
-- NAME_MST Z041 NAMESPARE1=「2:転出、3:退職」
Main.class (1.3)
Param.class (1.3)
miyagi.sh (1.3)
★ 教育委員会DBパラメータ追加

【納品プログラム】
・miyagi.jar
・miyagi.sh (1.3)

2017/03/29

【修正内容】
StaffInfo.class (1.4)
★ 職員の異動情報について、対象データの条件を変更
-- 変更前：異動日が「処理日の前日」
-- 変更後：異動日が「処理日」

【納品プログラム】
・miyagi.jar
