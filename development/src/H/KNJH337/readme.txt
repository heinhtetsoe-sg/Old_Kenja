# kanji=漢字
# $Id: readme.txt 75499 2020-07-17 08:13:47Z maeshiro $

/*** KNJH337模試科目マスタメンテ readme.txt***/

2007/06/26  新規作成(KNJH330参照)

2007/06/27  科目略称を必須項目にした
            科目略称サイズ変更 9 → 15

2009/02/27  1.教科コード、科目コードを追加しました。

2011/11/17  1.県下統一模試科目コードを追加

2011/11/21  1.県下統一模試科目名登録ボタンを追加し、子画面（KNJH341）を呼び出すように修正
            2.画面レイアウト修正

2011/11/22  1.テーブル名変更による修正
               - PREF_SUBCLASS_MST ⇒ MOCK_PREF_SUBCLASS_MST
               
2011/12/15  1.統一というコメント削除

2012/01/17  1.教科コードを必須項目に修正

2012/01/20  1.県下模試科目登録ボタンをprgInfo.properties["usePerfSubclasscd_Touroku"]の値で表示・非表示をコントロールできるように修正
            2.県下模試科目登録ボタンをprgInfo.properties["usePerfSubclasscd_Touroku"]の値で県下模試科目を表示・非表示をコントロールできるように修正

2012/06/25  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。
            
2013/01/12  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2016/04/26  1.教科コードは、プロパティーusePerfSubclasscd_Touroku＝'1'の時のみ必須とする。

2016/07/22  1.MOCK_SUBCLASS_MST.SUBCLASS_DIV追加に伴う修正
            -- rep-mock_subclass_mst.sql(rev1.5)

2016/09/21  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/10/03  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/03/14  1.取消ボタンがきかない不具合修正。Model.inc

2020/07/17  1.プロパティーuse_prg_schoolkind対応
