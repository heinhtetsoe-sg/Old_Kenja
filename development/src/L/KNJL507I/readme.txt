# kanji=漢字
# $Id:  $

2020/12/10  1.KNJL506Iをもとに新規作成

2020/12/22  不具合対応
            1.取消ボタンを押下しても初期値がクリアされない事象を対応。
            2.判定マークコードに半角数字以外（半角アルファベットなど）が入力できない事象を対応。

2021/01/04  1.対応コースコードのコンボボックスが自分自身（GENERAL_DIV='03'）を参照していたため、
              コースコード（GENERAL_DIV='02'）を参照するように修正。

2021/01/20  PHP7対応（require_once('for_php7.php') を追加）
