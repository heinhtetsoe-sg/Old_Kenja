# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

2012/02/13  新規作成(KNJH337参照)

2012/03/05  レイアウト修正

2012/04/03  1.科目コードをカットした。

2012/05/30  1.県下模試科目登録ボタンの遷移先変更
               - KNJH341 → KNJH541
            2.県下実力科目の参照先変更
               - MOCK_PREF_SUBCLASS_MST → PROFICIENCY_PREF_SUBCLASS_MST

2012/07/24  1.教育課程の追加、追加に伴う修正
            - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/01/12  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2014/08/29  1.ログ取得機能追加

2015/04/22  1.Properties["useCurriculumcd"]=1のとき、教育課程コードを左画面のリストに表示するよう修正

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
