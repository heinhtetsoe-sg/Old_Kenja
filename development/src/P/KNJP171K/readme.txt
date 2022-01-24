# kanji=漢字
# $Id: readme.txt 56580 2017-10-22 12:35:29Z maeshiro $

2010/09/02  1.新規作成

2010/10/05  1.認定番号がNULLは更新対象外とする。
            2.所得が0の場合NULLで更新されていたので修正

2010/10/12  1.以下の項目を追加。
            -- 開始年月
            --     REDUCTION_AUTHORIZE_DATのBEGIN_YEARMONTH
            -- 残月数
            --     REDUCTION_AUTHORIZE_DATのREMAIN_SUP_LIMIT_MONTH
            -- 参照データ
            --     KNJP170Kと同じ。
            -- ※スクリプト（reduction_authorize_dat_rev1.1.sql）

2010/10/13  1.異動情報の表示に「退学、転学、転入学、編入学」を追加。

2010/12/20  1.認定番号は、表示のみに変更
            2.認定番号が無い場合は、基準額表示なし

2010/12/22  1.在籍データは、MAX学期を使用する。

2011/01/11  1.加算チェックがONのデータは所得額を使用不可にする。

2011/03/28  1.テーブル変更に伴う修正
            -- rep-reduction_country_dat_rev1.1.sql

2011/04/13  1.rep-reduction_country_dat_rev1.1.sql対応での修正漏れ

2011/05/09  1.認定番号なしも処理する。

2011/08/03  1.府県、国の備考欄を共通化
            -- 府県、国両方の備考欄の更新を行う。

2012/11/06  1.サブミットする時は、ボタン使用不可にする。

2014/09/19  1.学資負担欄カット
            2.互換ではない場合のエラー修正

2014/09/25  1.基準額を２つにする。

2014/09/26  1.特殊フラグ追加他(修正中)
            -- reduction_country_add_mst.sql(rev1.1)
            -- rep-reduction_country_dat.sql(rev1.2)

2014/10/01  1.大幅修正
            -- rep-reduction_country_dat.sql(rev1.3)

2014/10/02  1.修正

2014/10/09  1.2014/09/19以降の修正をキャンセル。
