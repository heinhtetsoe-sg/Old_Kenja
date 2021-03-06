// kanji=漢字
// $Id: readme.txt,v 1.3 2012/06/05 10:03:10 hamasaki Exp $

KNJD121T
成績入力処理(前期３回、後期２回)
-----
KNJD122T を元に作成
-----
SEM1_TERM2_CHAIRCD
SEM1_TERM2_SCORE（管理者コード・・・0131：前期－第3回－素点）
SEM1_TERM2_VALUE（管理者コード・・・0132：前期－第3回－評価）
SEM1_TERM2_VALUE_DI

2006/12/27 alp o-naka 単位入力欄の入力可／不可の区別を変更した。
・学年評定欄の管理者コントロールの指定に依存する。
・よって、管理者コントロールのコード「0884：履修単位」「0885：修得単位」は廃止。
・成績処理改訂版。
2007/01/12 alp o-naka 単位の自動計算する処理を変更した。（成績処理改訂版）
・基本前提
  単位入力欄が入力可で、学年評定が入力されていて、履修単位／修得単位が null の時、自動計算する。
  それ以外は、自動計算しない。（値そのまま）
・合併先科目の場合、
  科目合併設定データの単位固定／加算フラグを参照し、
  「1:単位固定」なら、単位マスタから合併先科目の単位数を取得して登録する。
  「2:単位加算」なら、生徒が選択した合併元科目の単位数を加算した値を登録する。
・教科コード'90'以上の場合、
  学年評定１以上なら履修単位／修得単位に登録する。欠課時数はチェックしない。
・教科コード'90'未満の場合、
  現在の自動計算と同じ処理をして登録する。（評定・欠課時数を元に単位を取得して登録）

2007/01/19 alp o-naka 単位の自動計算する処理を修正した。（成績処理改訂版）
・合併先科目の場合、
  学年評定１なら修得単位にゼロを登録する。

2007/03/07 単位の自動計算する処理を修正した。（成績処理改訂版）
-- ”教科コード'90'以上”の処理を削除した。
-- ”教科コード'90'未満”の条件を”合併先科目でない”に変更した。
-- よって、単位の自動計算する処理の場合分けは、
-- ”合併先科目でない”と”合併先科目である”の２つである。

2008/06/04  1.機能追加。Enterキーでのカーソル移動。

2008/10/10  1.出欠累計の欠時数に保健室欠課(nouseoff)を加算追加。
            2.出欠累計にNULLデータが存在する場合でも累計できるようにした。

2008/10/14  1.生徒を抽出する条件の日付。学籍処理日が学期範囲外の場合、学期終了日を使用する。

2009/02/03  1.名称マスタの値によって、合併元科目の「評定＝１（履修のみ）」の扱いを変えるよう修正。

2009/02/04  1.名称マスタは「V_NAME_MST」を参照するようにした。
            2.在籍データに存在しない学籍番号が最終行に表示される不具合を修正。

2009/02/12  1.固定文字の表示を以下の通り修正。
            ・学期マスタの学期名を表示。
            ・テスト項目マスタのテスト項目名の頭３文字を表示。
            ※「prgInfo.properties」の「useTestCountflg」を参照。

2009/03/11  1.帳票へ渡すパラメータを修正。累積現在日の「年度」を「年（西暦）」に変更。

2009/04/09  1.学校マスタのフラグ（LESSON_OFFDAYS）を参照し「休学時の欠課をカウントする」処理を追加。

2009/05/19  1.学校マスタのテーブル変更に伴う修正。「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。

2009/05/28  1.エクセルからの貼付け機能を追加

2009/06/02  1.一覧表の行を交互に色をつける修正

2009/06/03  1.考査満点マスタを参照するよう修正
            2.考査満点マスタのコードを修正

2009/06/23  1.以下の修正をした。
            -- 一覧の表示は5行ごとに色を変えるよう修正
            -- 評定・評価の「成績入力完了チェックボックス」追加(RECORD_CHKFIN_DATを更新)

2009/07/09  1.以下の修正をした。
            -- 「総合的な学習の時間」等の「評定を設定しない科目」に対して
            -- 科目合併が設定された場合の扱い。

2009/07/10  1.以下の状態を修正。レイアウト調整。
            -- ・画面下のボタン類が、スクロールしないと見えない状態。
            -- ・画面下のボタン類が、少しだけ見えない状態。
            -- ※動作環境・・・OS：XP／IE7

2009/07/13  1.成績入力完了にチェックが入っていて入力欄にnullがあれば確認を求めるよう修正
            2.変数名修正

2009/07/14  1.共通関数の修正に伴う修正

2009/07/24  1.成績入力完了のチェックの表示文字の修正

2009/08/03  1.以下の通り修正。
            -- 欠課数上限値が「NULL」の場合は「９９」に読み替えて処理する。

2009/08/05  1.実・法定欠課時数上限の修正。

2009/08/06  1.実・法定欠課時数上限の修正。

2009/09/07  1.メンバ変数の初期化を init() で処理するように変更。

2009/09/30  1.以下の不具合を修正。
            -- 成績入力完了にチェックし、講座コンボを変更した時、チェックされたままになっている。

2010/10/08  1.入力完了チェックをしても更新するとOFFになる不具合を修正。
            -- RECORD_CHKFIN_DATの更新にて、配列定義が間違っていた。
            -- 修正前：「前期２回・後期３回」の内容
            -- 修正後：「前期３回・後期２回」の内容

/******************* 桐ヶ丘から取得 *******************/

2011/04/14  1.CVS登録（桐ヶ丘で使っている旧バージョン）
            -- KNJD121T(1.32)を元にPRGIDを変更しただけ
            -- 但し、帳票PRGIDは変更しない
            2.CSV入出力の機能をカット。

2012/06/05  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応