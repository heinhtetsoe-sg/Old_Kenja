# kanji=漢字
# $Id: readme.txt 77019 2020-09-23 06:27:19Z yamashiro $
/*** KNJXMENU メニュー処理 readme.txt***/
2006.08.22  管理者でのログイン画面の「年度+学期」選択を過去6年まで選択できるように変更しました。MIYAGI
            2006.09.11  東京に納品

2006/10/31 m-yama   CVS登録。
                    年度変更画面表示の変更をした。
                    ・対象：管理者→全員
                    ・管理者以外は、次年度表示なし
                    管理者以外は、過去でログインをした場合のメニューはKNJA120(権限なし以外)のみとする。
                    変数の取得を修正した。(globalで取得)
2008/04/01 s-yama   "PROGRAMID = 'KNJA120'"を"PROGRAMID LIKE 'KNJA120%'"とした。

2008/04/04 m-yama   出欠未入力対応

2008/09/26  1.DB2のユーザー定義関数「SECURITY_CHK_MNU」の変更（引数「年度」を追加）に伴い修正した。

2008/09/29  1.プロパティファイルの対応。年度学期変更画面表示の対象者。
            2.プロパティファイルの対応。年度学期変更画面表示の過去・未来年数（管理者・管理者以外）。

2008/09/30  1.プロパティファイルの対応。初期メニュー設定。
            2.プロパティファイルの対応。出欠未入力チェック機能のON/OFF。

2008/10/16  1.「指導要録の所見入力」関連の不具合修正。

2008/10/17  1.プロパティファイルの対応。管理者以外が過去の年度でログインした場合、機能を制限し指導要録の所見入力のみを表示する。このような強制的な機能制限を行うプログラムＩＤ。
            2.fopen関数の第一引数のパス指定の不具合を修正。

2009/03/28  1.グループコード7000台も年度指定可能

2009/07/09  1.メニューマスタ変更(TITLE挿入)
            -- lib/Html.incも必要

2009/07/28  1.ダウンロードの指定をプロパティーファイルに追加

2009/10/06  1.今年度今学期前の場合も、過年度としていたが、学期の条件を外した(年度内はOK)

2010/01/18  1.ダウンロードの指定をプロパティーファイルに追加(DownLoadAdbeRdr)

2010/03/11  1.未出欠対象データは、今年度開始～ログイン日付に変更
            -- 近大（担任が出欠を取る学校）は、今まで通り

2010/03/26  1.未出欠ウィンドウ表示位置変更

2010/04/22  1.「availheight」を「availHeight」に修正

2010/05/13  1.未出欠機能での対象日付をNAME_MST(Z026)で制御する。

2010/05/31  1.プロパティファイル項目追加。useSubMenuIdで指定された、SUBMENUIDのみ表示する。
            -- useSubMenuId = C

2010/05/31  1.未出欠対象データの範囲指定を可能にした
            -- NAME_MSTのZ026の予備2が'1'の場合は、ログイン学期の開始日～に変更

2012/01/13  1.未出欠対象データの講座担当の学期も考慮した

2010/05/31  1.プロパティファイル項目追加。NotUseMessageが1の場合、ツールのメッセージ使用しない
            -- NotUseMessage = 1

2015/01/05  1.タブレットからの起動は、MENU_MST.INVALID_FLG = '9'のみ表示とする。

2015/01/15  1.iPadを追加

2015/04/15  1.タブレットの場合メニューを拡大表示する。(宮部さんの修正も反映：setInterval→setTimeout)

2015/04/16  1.以下の修正をした。
            -- タブレットの場合、ツールを表示しない。
            -- タブレットの場合のメニュー拡大率変更。2倍→1.5倍

2015/04/21  1.パスワード桁数変更　10 → 32

2015/05/27  1.テーブル追加。(useMenuUnuseStaffDat=1の時使用)
            -- menu_unuse_staff_dat.sql(rev1.1)

2015/06/02  1.テーブル/ユーザー定義関数追加。(useMenuStaffDat=1の時使用)
            -- menu_staff_mst.sql(rev1.1)
            -- security_chk_mnu2.sql(rev1.1)

2015/08/27  1.教育委員会通達機能追加(useTuutatu = ON)
            2.修正

2015/08/28  1.以下の修正をした。
            -- 未提出は、提出期限赤表示。それ以外は青。
            -- 通知日付ありが対象。
            -- 受信フラグOFFで、掲載日範囲内が対象。
            -- 回答要で、提出期限切れが対象。
            -- 上段の確認済/終了ボタンをカット。
            -- 固定文言の変更。

2015/09/01  1.Z010のABBV1が'1'OR'2'の時のみ$db2を使用する。


2016/07/06  1.KNJXMENU関連を東京→沖縄へ環境をコピー。ただし一部の更新処理がDB2V8で実行するとエラーになってしまう。

2016/07/22  1.KNJXMENUに利用者がIMEかATOKの使用判断ボタンを追加。
              IME,ATOKの判断に利用しているのは　STAFF_DETAIL_SEQ_MST　STAFF_SEQ='001'　FIELD3='1'：IME　FIELD3='2'：ATOK　データなし初期値はIME。
              更新は以下。
            -- knjxmenuQuery.inc
            -- knjxmenuModel.inc
            -- knjxmenuForm2.html
            -- knjxmenuForm2.php
            
2016/07/26  1.左メニューを開いた後もう一度クリックしたら閉じるように変更。
            -- knjxmenuForm1.html
            -- knjxmenuForm1.php
            -- knjxmenuForm1.js

2016/07/27  1.DB2 V8で色、文字サイズ、入力区分を更新する時にエラーにならないように修正。
            -- knjxmenuModel.inc
            -- knjxmenuQuery.inc

2016/08/04  1.ツールのパスワード変更を以下の通り修正
            ★ パスワードの桁数、文字チェック機能追加
            -- 8桁以上、英数字&大文字小文字の混在
            ★ パスワードの履歴管理とチェック機能を追加
            -- メッセージ「過去に使用したパスワードは入力できません。」
            -- user_pwd_hist_mst.sql (1.1)
            ★ パスワード更新のＤＢエラー修正。SCHOOKCD → SCHOOLCD
            ★ PHP関数変更。ereg() → preg_match()
            -- knjxmenuModel.inc
            -- knjxmenuQuery.inc

2016/08/05  1.賢者ログイン機能のセキュリティ強化
            ★ パスワード有効期限を過ぎると使用不可
            -- メッセージ「パスワードが不正です。期限が切れています。」
            -- 自動でパスワード変更画面へ
            ★ 1週間前から期限切れ警告をログイン時に出力
            -- メッセージ「ご使用のパスワードの有効期限は【残り4日】です。」
            ★ パスワードの使用日数は、SYSDATE()とパスワード履歴のMAX更新日付より算出
            -- knjxmenuModel.inc
            -- knjxmenuQuery.inc

2016/08/12  1.IME,ATOK切替の表示/非表示設定(menuInfo.properties useImeOrAtok = 1)
            2.CSSの色指定が未保存の場合、menuInfo.properties cssIROを使用する。
            
            
2016/09/16  n.kanamaru
            1.TOPのみにつけていた学期変更の機能をどのメニューを開いている時でも表示されるように変更。（古宿さんの指示）
            2.複数タブを開いている時に賢者のタブのみをログアウトせず×で閉じた際にログアウトされるように変更。
            -- knjxmenuForm2.php
            -- knjxmenuModel.inc
            
2017/01/30  1.(金丸さんに依頼)テーブルの学校コード、校種はmenuInfo.propertiesのuseSchool_KindMenuが'1'の場合使用する
            2.(金丸さんに依頼)修正漏れ対応
            
2017/01/30  n.kanamaru
            1.プロパティファイルmenuInfo.propertiesにuseSchool_KindMenu = 1を追加。
            ★ プロパティなし、もしくは1以外の場合はMENU_MST等に校種がないバージョンのDBに対応。(SECURITY_CHK_MNUのパラメータが３つ)
               1の場合は校種が必要。（SECURITY_CHK_MNUのパラメータが５つ)
            
2017/02/03  1.(金丸さんに依頼)修正漏れ対応


2017/03/03  1.パフォーマンス向上のため、左メニューのデータの取得方法を変更
            --knjxmenuForm1.php
            --knjxmenuForm2.php
            --knjxmenuForm1.js
            --knjxmenuQuery.inc
            --knjxmenuModel.inc
            2.ログイン画面でfile does not existのエラーが出てしまうので対応。
                /usr/local/development/src/common/crcloginfrom.ihtml
            3.新メニュー用のcssファイルが古かったので一緒に更新。
                /usr/local/development/src/common/gkcss/*.*
*/
2017/03/03  n.kanamaru
            1.左メニューのMENU_MST取得のSQLの改善。（INDEXの追加あり）IDX_MENU_MST
            
2017/03/28  n.kanamaru
            1.機能別・目的別メニューの最後に使用した方をユーザーごとに保存して、次回ログイン時の初期値とする。（最初の初期値は機能別）
            2.右メニューから画面を開くときのパラメータにメニューID（MN_ID）を追加。（各画面でrequest可能）

2017/04/08  kimura
            1.右メニュー取得Query(selectQuery)を修正。
            2.メニュー取得用Queryに管理者でない人が過去年度でログインしたときのメニューを絞る部分を追加。(左右メニューどちらも)

2017/04/17  kimura
            1.Form2.phpの39～60行目、355～357行目を復活。

2017/04/18  kimura
            1.右側メニューのリンクにURL_SCHOOLCDとURL_SCHOOLKINDを追加。(Form2.php)

2017/05/22  kimura
            1.左側ツールのダウンロードするURLに"target=_blank"を追加。(Form1.php)
            2.右側のマニュアルのファイルのURLにView.phpのecho_filedateを使って常にdateを取得して、最新のファイルをダウンロードできるように修正。(Form2.php)

2017/05/30  1.2017/5/29 Warningメッセージ対応(Form2.php)の修正に東京分を上書きしたので、再度修正を入れた。


2017/06/14  n.kanamaru
            1.ACCESS_LOGが書き出されなかったので修正。右メニューのリンクにプログラムIDを追加した際の変更の影響が原因と思われる。（Form2.php）
            
2017/07/24  n.kanamaru
            1.右画面の上のメニュー名を取得する部分で
            $model->properties["useSchool_KindMenu"] == "1"の記述が
            $properties["useSchool_KindMenu"] == "1"になっていたので修正。
            2.その中のSQL部分でANDがなかったので追加。

2017/09/04  n.kanamaru
            1.右メニューの学校名をNAME_MSTのX002,00があればそれを優先するように変更。なければ今まで通り。（Form2.php,Query.inc)

2017/11/30  n.kanamaru
            1.kyotuManualのファイル名の文字コードがUTF-8でもSJISでも大丈夫なように文字コードチェックを追加して変換・無変換を条件分岐させる。（Form2.php）

2018/08/22  1.タブレットからの起動処理を追加

2018/09/10  1.英語対応

2018/09/13  1.英語/日本語切替ボタン追加。(プロパティー追加)
            -- useLanguageChange

2018/09/26  1.英語対応(職員名)・・高倉さん対応
            2.言語以外の変更(色変更等)をした際に日本語に戻ってしまう不具合を修正

2018/10/05  1.初回ログイン時、強制パスワード変更機能

2018/10/07  1.お知らせがない際にもお知らせ入力のボタン文言を表示

2018/10/13  1.プロパティーpasswordCheckOptionを追加。(桁数の指定、数字・大文字英字・小文字英字を使用するべき文字に含めるかを指定)
              プロパティーoldPasswordEnableChangeCountを追加。(過去のパスワードを指定回数を超えた際に使用可能とする)

2018/10/17  1.次回強制PWD変更機能追加
            -- rep-user_mst.sql(rev.62877)

2019/06/26  1.タブレットモードで動かしたい端末を可変に
            -- プロパティー：userAgent

2019/07/01  1.未出欠機能使用時のリンク先の切替機能
            -- useCheckAttendInputPrg = KNJC010A

2019/12/10  1.PHPバージョンアップに伴う修正

2020/02/21  1.Tablet端末の判定用パラメーターの追加。(crcloginform.ihtmlから取得)

2020/06/22  1.学校専用マニュアルの「操作説明」の表示文字が文字化けするのを修正

2020/08/14  1.TITLEの表示を変更(左寄せ、フォントサイズ、先頭Imageの表示)
            2.お知らせ内容、非表示設定機能追加
            -- oshirase_not_disp.sql(rev.76016)
            3.表示/非表示の文言を変更

2020/08/17  1.お知らせ機能に、添付ファイル機能追加

2020/08/18  1.キー追加に伴う修正(releaseNote機能追加)。添付ファイルはPDF以外読み飛ばす。
            -- rep-oshirase_grp.sql(rev.76050)
            -- rep-oshirase_ind.sql(rev.76050)
            -- rep-oshirase_tbl.sql(rev.76050)
            -- oshirase_not_disp.sql(rev.76050)
            2.表示ファイルの判定処理修正。

2020/08/21  1.OSHIRASE_TBL。フィールド名変更に伴う修正(DATA_NO → OSHIRASE_NO)
            -- rep-oshirase_tbl.sql(rev.76146)

2020/08/26  1.topレイアウト変更(東京修正)
            2.以下の修正をした。
            -- 英語お知らせ内容追加
            -- rep-oshirase_tbl.sql(rev.76215)
            -- 送信者の英語切替
            -- 学期名を1st,2nd,3rd,4th
            -- 日付のフォーマット。9999-99-99 → 9999/99/99
            -- 送信者を正式名称
            -- リリースノートのお知らせ出力内容変更。ANNOUNCE → START_DATE＋固定文言
            3.改行表示がされない不具合を修正

2020/08/27  1.学期の英語表記を変更。SEMESTER：1st → 1st Semester

2020/08/28  1.以下の修正をした。
            -- リリースノートの送信者は固定。日本語「システム」、英語「System」
            -- 年度と学期の間のスペース調整
            2.ANNOUNCE_ENGがない場合は、ANNOUNCEを出力する。

2020/09/08  1.操作手順(DATA_DIV=98)の追加。98/99は全員表示。

2020/09/10  1.リリースノート/操作マニュアルのお知らせ内容修正。

2020/09/17  1.98/99は、表示期間を非表示

2020/09/23  1.PROGRAMIDがなければ、右画面のリンクはなし。

2021/01/13  1.php7対応
            2.SWからのプログラム起動。

2021/03/02  1.未出欠ウィンドウのSTAFFCDの指定修正
            -- アプレットで権限エラーになる不具合対応
            
2021/03/10  1.京都PHPバージョンアップ対応
