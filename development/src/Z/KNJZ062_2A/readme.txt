# kanji=漢字
# $Id: readme.txt 74230 2020-05-12 08:55:17Z ishii $

2013/04/22  1.KNJZ060_2を元に新規作成

2013/04/23  1.元画面から権限を取得するよう修正

2013/04/24  1.削除実行時の教育委員会.DBの教科マスタの存在チェックをカット

2013/05/01  1.フィールド名変更
               - SCHOOLCD ⇒ EDBOARD_SCHOOLCD
            2.教育委員会用フラグ追加に伴う修正
               - EDBOARD_FLG

2013/07/04  1.テーブル名変更に伴う修正
                - CLASS_ANOTHER_DAT==>EDOBOARD_CLASS_DAT 

2013/11/26  1.専門の項目にその他を追加し、チェックボックスをコンボに修正

2014/02/02  1.科目マスタに登録されている場合削除不可。

2014/07/10  1.ログ取得機能追加

2014/12/16  1.教科名称の文字範囲の上限を９０バイトに変更

2014/12/18  1.略称名をプロパティで制御するよう修正(90バイト(全角30文字)を超える場合はエラーとする)
                - プロパティー「CLASS_MST_CLASSABBV_SIZE」でコントロール

2020/05/12  1. 削除時にその教科を参照しているテーブルが存在する場合はエラーを出力するよう修正
