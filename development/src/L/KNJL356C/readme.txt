// kanji=漢字
// $Id: readme.txt 64627 2019-01-15 12:10:55Z nakamoto $

2008/11/21  新規作成(処理：智辯KNJL351C　レイアウト：智辯KNJL351C 参考)

2010/01/12  1.入試制度「1：中学」、入試区分「6：内部生」のとき、
            　受験者全員、受験者指定ラジオボタンは使用不可に変更した。

2010/01/13  1.以下の通り修正
            -- 入試制度・入試区分コンボのＳＱＬ抽出条件のテーブルを変更
            -- 変更前：ENTEXAM_RECEPT_DAT
            -- 変更後：ENTEXAM_APPLICANTBASE_DAT

2011/11/28  1.五条のときの入試制度は、高校のみを対象となるように修正
            -- 併願のみ対象となるコメントを追加

2011/12/15  1.名称マスタ「Z010」の参照テーブルを変更
            -- 変更前：V_NAME_MST
            -- 変更後：NAME_MST

2011/12/21  1.「※併願のみ対象です。」のコメントをカットする。
            2.入試制度を高校のみ表示を変更・・・中学も出力が必要になった。
            -- 通常と同様に名称マスタ「L003」を表示する。

2012/02/08  1.合格者チェックボックスを追加（初期値チェック有）

2018/12/18  1.和歌山の場合、追加合格者全員ラジオボタン追加

2019/01/15  1.志願者全員ラジオボタン追加
            -- 和歌山から要望
