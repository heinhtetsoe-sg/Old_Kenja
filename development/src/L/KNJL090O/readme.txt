// kanji=漢字
// $Id: readme.txt 56582 2017-10-22 12:39:47Z maeshiro $

KNJL090O_合否判定メンテナンス処理

2008/12/21  1.出願区分欄の表示を変更。出願コースの表示位置を変更。
            2.以下の通り修正。
            ・合格クラスを追加。
            ・受験科目欄を最大５→４行表示に変更。
            ・入学クラスを追加。
            ・合否学科をコース名のみ表示に変更。
            ・措置が入力され更新した時、入学クラスの必須チェックを追加。

2008/12/22  1.合否区分が「2:不合格」の場合、合格クラスは空白とする。

2012/12/13  1.入試区分０の追加に伴う修正
            -- 出願区分欄に表示できるように修正

2012/12/17  1.判定の右に”特別アップ合格”の表示追加に伴う修正
            -- 例）判定：合格　特別アップ合格
            -- 入学クラス、手続区分の表示位置変更

2013/05/20  1.入試区分マスタ化に伴い修正

2014/01/30  1.措置コンボの使用条件を変更
            -- 変更前：合格者については、選択不可とする。
            -- 変更後：合格者についても、選択可とする。

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正

2014/11/21  1.加点追加に伴い修正
            ・合計の上に「加点」欄追加（合計、平均、全体席次、出願席次は１つ下に詰める）
            ・２科目欄の廃止（合計、平均、全体席次、出願席次）
            ・４科目欄は左に詰め、右欄は空欄とする
            ・タイトル変更　’４科目’→’総合成績
            -- rep-entexam_recept_dat.sql (1.2)

2015/10/26  1.出願区分に第７回が表記されない
