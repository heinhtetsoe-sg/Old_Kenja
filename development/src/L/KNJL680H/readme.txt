// kanji=漢字
// $Id: 2e41f28f5b88f84b5929e1d6a170a3fcdcb63134 $

2020/10/26  1.KNJL780Hをもとに新規作成

2020/10/27  1.KNJL680HのFormではなくKNJL580HのFormを呼び出していた箇所を修正
            2.不要な処理を削除

2020/10/28  1.入試年度、入試日程、手続年月日の表示欄の背景色を見出しの色から白へ変更

2020/12/11  1.ヘッダ有をチェックしない場合、ヘッダなしのcsvが出力されるよう修正
            2.csv取込でファイル名不正のエラーの場合にデータ更新される不具合の修正

2020/12/20  1.PROCEDUREDIV=1の時、ENTDIV=1、PROCEDUREDIV=1以外の場合は、ENTDIV=Nで更新するよう修正

2021/02/04  1.CSV取込み後の処理済み件数が合格者を含めた更新処理済みの件数になっていたため、CSVファイルの処理済みの件数を表示するよう修正
