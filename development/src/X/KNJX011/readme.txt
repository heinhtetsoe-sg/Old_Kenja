// kanji=漢字
// $Id: readme.txt 76583 2020-09-08 08:36:18Z arakaki $

2016/04/06  1.新規作成

2016/10/11  1.年度チェック追加

2018/04/17  1.削除のとき、対象テーブルにSTAFF_YDAT、USER_MST、USER_PWD_HIST_MST追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/03/14  1.E-Mailの最大バイト数を25バイトから50バイトに変更

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/23  1.CSVメッセージ統一(SG社)