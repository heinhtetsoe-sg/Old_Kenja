# kanji=漢字
# $Id: readme.txt 74230 2020-05-12 08:55:17Z ishii $

/*** KNJZ060_2 教科マスタメンテ readme.txt***/

2007/03/06 m-yama   CVS登録。
                    CLASS_MSTテーブル変更に伴う修正をした。
                    オブジェクト作成をメソッドにした。

2007/03/08 m-yama   固定文字を修正した。
                    -- 科目名→教科名

2007/06/11 s-yama   CLASS_MSTテーブル変更に伴い調査書用表示順を追加した。
                    固定文字「教科名その他１」→「調査書用教科名」に変更した。

2007/08/15 m-yama   CLASS_MSTテーブル変更に伴い通知表用表示順を追加した。

2009/03/31  1.追加、更新した際にスクロールバーを対象となった行へ移動させる処理を追加
            2.前回の修正の不要な部分のカット

2009/10/07  1.SPECIALDIVフィールド追加に伴う修正
            -- rep-class_mst.sql-1.4
            -- v_class_mst.sql-1.4

2010/05/20  1.「SHOWORDER4」追加に伴う修正

2011/08/11  1.エラー文言の修正

2011/12/19  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
                
2012/04/11  1.教育課程対応の未修正部分を修正

2012/07/10  1.教育課程の追加に伴う修正漏れ対応

2012/07/11  1.教育課程の学校校種、教育課程コードを修正

2013/01/11  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/04/19  1.リストTOリストの並びを学校校種 + 教科コードの並びに修正

2013/11/22  1.専門の項目にその他を追加し、チェックボックスをコンボに修正

2014/02/02  1.科目マスタに登録されている場合削除不可。

2014/10/16  1.useSpecial_Support_Schoolの場合、教科区分の設定を可能にする。
            -- class_detail_mst.sql(rev1.1)

2014/10/20  1.プロパティ名を変更
            -- useSpecial_Support_School
            -- ↓
            -- useSpecial_Support_Hrclass

2014/11/14  1.ログ取得機能追加

2014/12/16  1.教科名称の文字範囲の上限を９０バイトに変更

2014/12/18  1.略称名をプロパティで制御するよう修正(90バイト(全角30文字)を超える場合はエラーとする)
                - プロパティー「CLASS_MST_CLASSABBV_SIZE」でコントロール

2015/03/17  1.教科名英字、教科略称英字の英数字チェックをカット

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/03/09  1.useCurriculumcd='1'以外で削除ボタンを押した際の科目マスタチェックのDBエラー修正

2017/08/30  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2018/05/10  1.教科区分について、名称マスタ'Z052'を優先して利用する処理を追加

2018/12/28  1.テーブルの項目名指定を修正「SUBSUBCLASSCD」→「SUBCLASSCD」

2020/05/12  1. 削除時にその教科を参照しているテーブルが存在する場合はエラーを出力するよう修正

2021/03/18  1.リファクタリング
            2.プロパティ「useSpecial_Support_Hrclass」=1の場合、「状態区分」の項目を追加
