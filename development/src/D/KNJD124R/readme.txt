// kanji=漢字
// $Id: readme.txt 76257 2020-08-27 08:51:43Z arakaki $

KNJD124S
成績入力（３学期制）
１学期・・・中間・期末
２学期・・・中間・期末
３学期・・・期末
-----
参考：KNJD124Tをコピー。成績テーブルをRECORD_SCORE_DATに変更。
-----
学校：熊本県立

2008/05/08  新規。初版。
2008/05/09  コードを修正した。
2008/05/19  以下の通り修正した。
            ・３学期評価欄を削除。
            ・学年成績欄を追加。
            ・３学期期末評価の更新時、同じ値を３学期評価にもセットし更新。

2008/06/03  1.成績が全てNULLの場合、レコードを削除する。

2008/06/04  1.機能追加。Enterキーでのカーソル移動。

2008/07/30  1.仕様変更。生徒を抽出する条件の日付。学籍処理日が学期範囲外の場合、学期終了日を使用する。

2008/08/08  1.機能追加。学年・成績が入力され、学年・評定欄が空欄の場合は評定マスタの換算表を参照して評定を自動計算する。

2008/10/10  1.出欠累計の欠時数に保健室欠課(nouseoff)を加算追加。
            2.出欠累計にNULLデータが存在する場合でも累計できるようにした。

2009/01/15  1.評定マスタの抽出条件変更。評定が”１”以上という条件を追加。
            2.評定が”空欄（NULL)”の場合、履修、修得単位数もNULLとする。

2009/03/27  1.合計行を追加。

2009/05/12  1.学校マスタのフラグ（LESSON_OFFDAYS）を参照し「休学時の欠課をカウントする」処理を追加。

2009/05/19  1.学校マスタのテーブル変更に伴う修正。「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。

2009/05/27  1.エクセルからの貼付け機能を追加

2009/06/02  1.一覧表の行を交互に色をつける修正

2009/06/02  1.固定文字の表示を以下の通り修正。
            ・学期マスタの学期名を表示。
            ・テスト項目マスタのテスト項目名の頭５文字を表示。
            ※「prgInfo.properties」の「useTestCountflg」を参照。

2009/06/04  1.考査満点マスタを参照するよう修正

2009/06/24  1.以下の修正をした。
            -- 一覧の表示は5行ごとに色を変えるよう修正
            -- 評定・評価の「成績入力完了チェックボックス」追加(RECORD_CHKFIN_DATを更新)

2009/07/13  1.成績入力完了にチェックが入っていて入力欄にnullがあれば確認を求めるよう修正

2009/07/14  1.共通関数の修正に伴う修正

2009/07/24  1.成績入力完了のチェックの表示文字の修正

2009/08/03  1.以下の通り修正。
            -- 欠課数上限値が「NULL」の場合は「９９」に読み替えて処理する。

2009/08/04  1.名称マスタの値によって、合併元科目の「評定＝１（履修のみ）」の扱いを変えるよう修正。

2009/08/05  1.実・法定欠課時数上限の修正。

2009/08/06  1.実・法定欠課時数上限の修正。

2009/09/07  1.メンバ変数の初期化を init() で処理するように変更。

2010/03/02  1.画面表示が崩れる不具合の修正
            -- 「１学期中間」「１学期期末」などのテスト項目名について
            -- フォントサイズを小さいサイズに変更

2010/03/12  1.以下の通り修正。
            -- 「成績が全てNULLの場合、レコードを削除する」処理について、
            -- 評定が空欄で、履修単位・修得単位にのみ入力した場合、
            -- レコードが削除されてしまう不具合を修正。

2010/05/31  1.プロパティー追加に伴い修正
            -- useRecordChkfinDat = 1
            -- 成績完了チェックフラグ更新処理
            -- 1の場合：RECORD_CHKFIN_DATのRECORD_DIV='1'と'2'を更新
            -- 1以外　：RECORD_CHKFIN_DATのRECORD_DIV='2'と、SCH_CHR_TESTを更新

2010/06/21  1.プロパティーファイルの参照方法を共通関数を使うよう修正


2011/10/27  1.「参照可能」「参照可能・制限付」の時、更新ボタンを押したら、メッセージを表示する。
            -- 例：この処理は許可されていません。(MSG300)

2012/01/30  1.読み込み中は、更新ボタンをグレー（押せないよう）にする。

2012/02/14  1.スペースチェックを追加
            -- スペース文字があればエラーメッセージを表示する。
            -- 例：入力された値は不正です。「スペース」が混ざっています。

2012/03/13  1.スペースチェックをカット。
            -- スペース文字があればスペース文字を削除する。
            
2012/05/23  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/03/15  1.プロパティ追加に伴う修正
            -- ・useAssessSubclassMst
            -- 1の場合、科目別評定マスタを参照し、なければ評定マスタを参照する。
            -- 1以外の場合、評定マスタを参照する。

2013/05/24  1.得点欄の変更があった場合、終了時にメッセージを表示する。

2014/02/27  1.更新時、サブミットする項目使用不可　※本番機直接修正のため、使い捨て

2014/03/11  1.３学期期末評価欄(3-0201-VALUE)を削除し、３学期評価欄(3-9900-VALUE)を追加した。
            -- ※本番機直接修正のため、使い捨て。KNJD124RにPRGID変更

2017/09/20  1.DI_CD(29-32)追加

2017/10/02  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2020/08/27  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/11/30  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを設定ファイルの内容で切り替えるように変更
