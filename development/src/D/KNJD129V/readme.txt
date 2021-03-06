// kanji=漢字
// $Id: readme.txt 76389 2020-09-03 08:16:48Z arakaki $

-----
学校：宮城県・常盤高校
-----

2013/06/24  1.新規。初版。
            -- 単位認定入力（パーツタイプ）
            2.テスト名は略称１を表示
            3.科目別設定の修正
            4.プログラムIDの変更

2013/07/31  1.仮評定フラグ追加に伴う修正
            -- 学年評定を更新する場合、評定フラグは本評定(NULL)とする

2013/08/22  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)
            2.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正漏れ対応
            
2013/12/27  1.仮評定の追加対応するプロパティ追加
              - useProvFlg = '1'の時のみ対応する
            2.Enterキーによるカーソル移動機能を追加

2014/01/15  1.単位の自動計算を追加
            2.プロパティnotOpenCredit追加
            -- プロパティnotOpenCreditが'1'の時は、履修単位／修得単位の窓をあけない。
            -- また、単位の自動計算は、常に上書きする。

2014/01/28  1.レイアウト幅調整
            -- 全体の画面を少し縮める(欠課欄が空きすぎのため)
            -- フッターのボタンを２個分左に移動

2014/02/01  1.TESTITEM_MST_COUNTFLG_NEW_SDIVのSCORE_DIV’01’の時のみ、満点マスタを参照

2014/02/25  1.単位認定入力の学年評価欄にも追指導のリンク設定を表示する。
            2.追指導（記号、点数）が登録されている場合、背景色をピンク色で表示する。
            -- さらに、追指導（点数を換算後の評定が１、記号が×）の場合、文字色を赤色で表示する。

2014/02/26  1.更新時、サブミットする項目使用不可

2014/02/27  1.更新時、サブミットする項目使用不可の修正漏れ

2014/02/28  1.単位の自動計算を修正
            -- 学年評定０の場合、履修単位／修得単位は０とする。
            2.更新時、サブミットする項目使用不可。ソース整理

2014/03/06  1.更新時のロック機能(レイヤ)を追加

2014/04/21  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効
            2.Enterキーによるカーソル移動機能修正
            
2014/04/23  1.貼り付け機能の修正
                - 関数名を修正 show ⇒ showPaste

2014/05/28  1.相対評定マスタの設定がある場合、相対評定マスタを参照。設定がない場合、評定マスタを参照。

2014/05/29  1.更新/削除等のログ取得機能を追加

2014/07/02  1.成績が1件も入力されていない場合、RECORD_SCORE_DATのレコードは作成しない（考査ごと）

2014/08/15  1.合併先科目の単位認定について、
            -- 評定コピー／クリア（KNJD214V）の合併先科目の単位認定と同じ仕様に修正

2014/09/25  1.異動者に黄色表示されない不具合修正

2014/10/02  1.科目または講座が選択されていない時、各ボタン使用不可。終了ボタン以外使用不可。

2015/04/08  1.合併先科目は、科目コンボの内容で、科目コードの前に、黒丸（●）を表示する。

2015/06/03  1.累積情報に授業時数欄を追加
            2.累積情報に授業時数欄を修正
            -- 実授業の時は、出欠集計データの授業時数でok
            -- 法定授業の時は、授業週数×単位数=値　を表示する。

2015/06/04  1.累積情報の”授業時数”を”出席すべき時数”に文言変更。
            2.仮評定フラグを一括で設定できるチェックボックスを追加

2015/07/30  1.講座名簿に登録されている生徒より学校校種を取得し、基本設定のコードをセットするよう修正
            2.学校校種は講座名簿に登録されている生徒ではなく、科目コンボの科目より取得してセットするよう修正
                - ただし、科目コンボがNULLの場合は名称マスタA023のNAME1のMINより取得

2015/09/24  1.更新対象のテスト種別について、テスト種別が9990009は成績以外に履修単位か修得単位が入力されている際も対象に含める。

2016/03/29  1.成績が別講座の生徒に上書される不具合修正
            【現象】
            講座1-1選択後すぐに講座1-2を選択。
            講座1-2を更新すると講座1-1の生徒に上書される。
            【原因】
            生徒を表示する時に、学籍番号を保持しています。
            更新は、保持した学籍番号で更新しています。
            上記ケースの場合、本来、保持した学籍番号は、
            講座1-2の生徒になるはずなのに、講座1-1の生徒になっている。
            【対応】
            生徒を表示する時に、学籍番号を保持しておくのではなく、
            更新時に、学籍番号を取得し更新するように修正。

2016/05/31  1.処理科目ラジオボタン追加
            -- 1:前期(1学期)　2:後期(2学期)　3:3学期　4:通年
            -- 5:全て・・・初期値
            -- 6:合併先
            -- SUBCLASS_DETAIL_DATのSUBCLASS_SEQが'012'を参照
            ※ 三重県から要望

2016/06/01  1.CSV入出力機能追加
            ※ 三重県から要望
            2.プロパティKNJD129V_SUBCLASS_RADIO=1の時、科目ラジオボタンを表示する。

2016/06/02  1.リファクタ。出席すべき時数欄。
            -- 授業時数欄を追加する前準備のため、変数名を変更。
            2.授業時数欄を追加。三重県から要望
            3.氏名欄に学籍番号表記を追加。三重県から要望

2016/06/06  1.以下の処理をカット
            -- 法定授業の時は、授業週数×単位数=値　を表示する。

2016/06/22  1.修正漏れ。機能していなかった。
            -- 成績入力完了チェックの入れ忘れ防止対策

2016/06/24  1.宮城県の時、成績入力完了チェックのメッセージを変更する。

2016/09/19  1.科目コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/09/20  1.前回の修正漏れ
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/11/01  1.プロパティーuse_SchregNo_hyoji='1'の時、学籍番号表記

2016/11/28  1.プロパティー「use_school_detail_gcm_dat」が"1"の時の処理追加
            -- 新テーブルの管理者コントロールとテスト項目マスタを参照
            -- 課程学科コンボ追加

2017/02/27  1.処理速度改善のため、科目コンボ・講座コンボのＳＱＬ修正

2017/03/01  1.プロパティ「use_school_detail_gcm_dat = 1」かつ「課程学科が２件以上」の場合、
            -- 指定された課程学科で使用していない科目、講座を除外する。

2017/04/18  1.異動日の翌日以降は、転学、退学者は入力不可にする。
            ※ 宮城県要望
            2.異動日の翌日以降は、転学、退学者は入力不可にする。
            -- 但し、管理者（権限が更新可能）は、入力可

2017/04/20  1.管理者コントロールの基本のSCHOOL_KINDについて、混在は、MAXを取得する。
            -- 宮城県は'00'がある(古いデータ)
            2.入力窓の条件訂正

2017/04/27  1.速度改善

2017/05/16  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/05/17  1.前回修正の訂正
            2.前回の修正漏れ(校種)
            3.リファクタ。不要な校種条件をカット

2017/05/17  1.項目が多いのでsetAccessLogDetailはコールしない。

2017/12/18  1.'*'の貼り付け入力ができない不具合修正

2018/07/13  1.名称マスタ「D065」登録科目の場合、名称マスタ「D001」を参照し入力値チェックする。
            --また、単位の自動計算は入力値0以上とする。
            ※埼玉栄から要望

2020/02/20  1.PC-Talker

2020/07/14  1.出欠の累積情報について、合併先科目の場合、合併元科目の合算値を表示する。
            2.学校マスタの校種対応

2020/09/03  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
