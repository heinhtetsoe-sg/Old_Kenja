# kanji=漢字
# $Id: readme.txt 76246 2020-08-27 08:41:35Z arakaki $

2009/04/02  1.kindaiのKNJC035を元に新規作成

2009/04/23  1.kindaiのKNJC035を元に新規作成

2009/04/27  1.表示する文字の修正

2009/06/19  1.更新方法を「日・校指定」、「期間範囲」から選択できるよう修正

2009/06/23  1.ヘッダ出力（見本）の出力の文字修正
            2.ヘッダ出力（見本）の出力の文字再度修正

2009/07/30  1.日付が'/'区切りのとき取込処理で落ちるのを修正

2010/02/09  1.ラジオボタン、チェックボックスにラベル機能を追加した。

2011/01/05  1.データ取込に処理名コンボボックスを追加した。
              （削除機能の追加）
            2.日・校指定の出欠コードのヘッダを「2：保健室欠課」→「14：保健室欠課」に変更した。
            3.期間範囲の出欠コードのヘッダが「出欠コー」になっていたので修正した。

2011/05/02  1.タイトルの「出力」をカット。

2011/07/07  1.拡張子チェック追加
            2.W_CSVMSG_PRG_DAT作成に伴う修正

2012/03/16  1.備考コード欄を追加した。
            2.備考コードの存在チェックの年度漏れを修正した。

2012/06/06  1.同じ日・校時に複数の講座を受講していたらＤＢエラーになる不具合を修正した。
            2.備考コード欄が空の場合に存在チェックでエラーになる不具合を修正した。

2013/10/29  1.「日・校指定」の勤怠コードに忌引（勤怠コード2）、出停（3）、欠席(6)を追加
            2.「期間範囲」の勤怠コードに欠席(6)を追加

2014/05/28  1.更新/削除等のログ取得機能を追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/08/27  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/11/27  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを設定ファイルの内容で切り替えるように変更

2021/02/23  1.CSVメッセージ統一(SG社)