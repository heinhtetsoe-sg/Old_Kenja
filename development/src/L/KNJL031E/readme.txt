// kanji=漢字
// $Id: readme.txt 65369 2019-01-28 07:36:43Z maeshiro $

2018/11/12  1.新規作成

2018/11/29  1.一括設定をデータ更新から画面上の値のみセットに変更

2018/11/30  1.受験コースを表示する

2018/12/13  1.氏名の次に出身学校を画面に表示するよう、変更

2018/12/20  1.入試制度、入試区分、会場変更時の確認メッセージキャンセル後、
              選択状態が元に戻らない不具合に対応

2018/12/28  1.入試区分 = 21：音楽A 22：音楽Bの場合下記に仕様変更
            -- 受験コース(普通科併願)と表示する。
            -- ソート順、普通科併願情報なし＞普通科併願情報あり
            2.普通科併願情報は、L061.ABBV1。条件にNAMESPARE1 = '1'を追加
            3.普通推薦、面接会場を指定した場合のソート仕様変更。
            -- 受験コース＞受験区分＞受験番号
            4.普通推薦、面接会場を指定した場合のソート仕様変更。
            -- 受験区分＞受験番号

2019/01/07  1.Entrer縦移動、貼り付け機能追加

2019/01/28  1.ACCESS_LOG_DETAIL出力カット(DBのサイズエラー対応)
