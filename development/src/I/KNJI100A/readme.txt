$Id: readme.txt 73951 2020-04-23 06:23:46Z ishii $

2010/04/08  1.KNJI100を元に新規作成

2010/04/12  1.SCHREG_REGD_DETAIL を使っていたのを修正
            2.以下の不具合に修正
            --一部の生徒が出力されない
            --「卒業時出席番号」を「出席番号」に修正
            --項目のリストtoリストのエラー修正
            --出力対象者が多いとSQLエラーになるのを修正

2010/11/11  1.塾の位置を血液型の下に移動した。
            2.出身学校～卒業期を名称マスタ「A023」の学校種別分出力した。

2011/03/25  1.異動対象日付を追加した。
            2.異動生徒には●を表示した。

2011/05/12  1.出身学校、出身校卒業日付の項目名の学校種別表示を変更した。

2012/10/02  1.出身学校マスタに登録されていないコードがある場合に列がずれるのを修正

2013/05/13  1.課程入学年度を追加
            2.課程入学年度の取得方法、順番修正

2013/12/26  1.プロパティー「useAddrField2」を追加した。
            -- '1'がセットされている場合は追加分の住所2を使用する。

2014/08/22  1.ログ取得機能追加

2014/11/10  1.書出し項目保存機能追加
                - テーブル名：KNJI100A_KAKIDASHI_LIST

2014/11/11  1.書出し項目保存についてチェックがオフの場合、保存された項目をDBから削除するように変更

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/03/31  1.ラジオボタン追加（学年、個人）

2017/04/27  1.項目一覧に "その他（身体状態）" 項目追加

2018/04/16  1.プロパティーuse_prg_schoolkind校種対応

2020/04/23  1.CSV項目に「備忘録」を追加(SCHREG_BASE_DETAIL_MSTのSEQ=009のREMARK2に対応)
            --フィールド追加:rep-knji100a_kakidashi_list.sql(rev.73946)
