# kanji=漢字
# $Id: readme.txt 69386 2019-08-26 11:34:36Z ishii $

/*** knjp150k生活行事費払戻処理readme.txt***/
2005/05/19 納品
2005/05/31 KNJXEXPK_生徒情報参照画面(校納金用共通部品)呼出変更
2005/05/31 PGID変更
2005/12/13 費目中分類の返金データ更新時、返金区分の更新は更新は行わない
2006/03/29 NO001 alp m-yama リスト表示の条件に男女も選択可能にする。

2006/08/23 m-yama   テーブル変更に伴う修正(EXPENSE_S_MST/MONEY_DUE_S_DAT)。

2009/04/08  1.リファクタリング
            -- Tab文字変換
            -- 可読性
            -- オブジェクト作成をメソッドにした。
            2.更新対象データのデフォルトを、その他にした。
            3.返金済みフラグのみの更新を可能にした。

2010/08/23  1.全ての項目がブランクの場合に入金削除を行っていたが、ボタン追加し押下により実行するように仕様変更した。

2010/11/10  1.返金を複数指定出来るよう修正
            -- money_repay_s_dat_rev1.1.sql

2010/11/11  1.一括更新で、入金レコードが複数出来ないよう修正した。

2010/11/12  1.以下の修正をした。
            -- 返金指定で、項目全て未入力は直近の返金を削除する。
            -- 小分類の返金データの 区分/備考は、直近の返金データを入れる。
            2.一括画面の返金済みフラグをカット
            3.個人指定画面での返金項目全て未入力は、返金を削除する。
            4.一括更新での対象者が個人指定で選ばれた生徒となっていたので修正

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)