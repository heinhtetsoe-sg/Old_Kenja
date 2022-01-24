# kanji=漢字
# $Id: readme.txt 72512 2020-02-20 06:33:16Z furusawa $

2011/06/06  1.新規作成

2011/07/19  1.パラメータ(CTRL_YEAR,CTRL_SEMESTER,CTRL_DATE)を追加。

2012/07/10  1.パラメータuseCurriculumcdを追加

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/08/22  1.style指定修正

2015/01/17  1.学年取得の11未満の条件をカット
            -- 智辯の条件の時も同様

2015/09/16  1.学年コンボの表示を SCHREG_REGD_GDAT の GRADE_NAME1 に変更

2016/03/30  1.学年コンボの表示を SCHREG_REGD_GDAT の GRADE_NAME1 に変更

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/01  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/11/22  1.パラメータDOCUMENTROOT追加

2018/04/03  1.学期選択が「学年末」であればuseJviewStatus_NotHyoji_D028プロパティ値、それ以外はuseJviewStatus_NotHyoji_D029のプロパティ値を引き渡す処理を追加。

2018/07/11  1.パラメータuseTestCountflg追加

2019/10/23  1.「CSV出力」ボタンを追加

2020/02/20  1.KIN_RECORD_DAT対応
            -- rep-kin_record_dat.sql(rev.72507)
