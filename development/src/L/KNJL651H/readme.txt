// kanji=漢字
// $Id: 6b80c81438f0d424e5bbf94cffcfc25fe740c7c9 $

2020/10/23  1.新規作成

2020/12/03  1.評価コンボのデフォルト値を「B」にするよう修正。値がNULLの場合はデフォルト値を赤色で表示。

2020/12/09  1.評価コンボのデフォルト値を設定マスタ「L027」の予備2(NAMESPARE2)から取得するよう修正。

2020/12/17  1.試験欠席(JUDGEMENT=3)または、面接欠席(ATTEND_FLG=1)の場合は評価コンボの初期値をブランクで表示するよう修正。
            2.CSV取込時に試験欠席(JUDGEMENT=3)を判定する際の記述誤りの修正。
