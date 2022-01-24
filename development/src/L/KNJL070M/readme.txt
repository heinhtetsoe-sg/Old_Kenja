// kanji=漢字
// $Id: readme.txt,v 1.8 2014/07/22 05:55:39 nakamoto Exp $

KNJL070M
合否判定処理（ライン入力）
-----
KNJL070H を元に作成
-----
学校：武蔵
-----

2009/12/18  1.新規。初版。

2009/12/21  1.判定対象コンボをカットし、固定値：１とする
            2.以下の通り修正
            -- 確定時に以下のフィールドをNULLで更新する
            -- ENTEXAM_APPLICANTBASE_DAT.SUB_ORDER

2009/12/24  1.レイアウト調整
            -- 確定結果一覧で項目欄が２行になっている
            2.入試区分をカット。固定値：１

2010/01/18  1.以下の通り修正
            -- シミュレーションボタンをカット
            -- 戻り率、収容人数、増減をカット
            -- 文字変更。「合格点 ⇒ 表１最低点」「候補点 ⇒ 表２最低点」

2010/01/26  1.文字修正(「表１最低点⇒合格点」「表２最低点⇒候補点」)

2010/02/16  1.判定履歴データ（1:合格、2:補員）の登録処理を追加

2014/07/22  1.データが不足してもWarningは表示しないように修正
            -- Warning: reset(): Passed variable is not an array or object in /usr/local/deve_mutestdb/lib/PHPlib/of_select.inc on line 38
            -- Warning: Variable passed to each() is not an array or object in /usr/local/deve_mutestdb/lib/PHPlib/of_select.inc on line 39
