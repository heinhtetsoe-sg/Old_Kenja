# kanji=漢字
# $Id: readme.txt 76425 2020-09-04 08:09:48Z arakaki $

2008/11/26  1.新規作成(処理：新規　レイアウト：新規)

2009/02/13  1.SQLの修正

2009/03/23  1.評定印字指定機能追加

2009/06/10  1.絞り込み機能仕様変更

2009/09/28  1.レポート提出の、条件３：不合格・未提出の仕様変更
            2.人数表示追加
            3.レポート提出の、条件３：不合格・未提出の修正

2009/09/29  1.評価/評定の条件に、考査の条件を使用していたので修正
            2.評価/評定は、前期の場合SEM1_VALUE。後期の場合GRAD_VALUEを使用する。
            3.CSV出力ボタンと、名簿ボタン追加
            4.CSV出力機能追加

2009/09/30  1.パラメータ追加
            2.パラメータ追加
            3.住所住所タックシールの、保護者と生徒のラジオ修正

2010/02/17  1.得点、評価/評定は、NULLを０として扱う

2010/10/21  1.スクーリングでの０とは、レコードが一件も無い人を対象とする。

2011/07/07  1.PDF印字する場合のコメント追加

2011/07/08  1.PDF印字する場合のコメント出力を帳票種類の生徒住所タックシールを選択時に修正
            2.コメント出力の位置を修正

2012/06/15  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2014/12/12  1.style指定修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
