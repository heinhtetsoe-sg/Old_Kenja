// kanji=漢字
// $Id: readme.txt 75593 2020-07-22 00:53:31Z gushiken $

2011/01/11  1.KNJA125Pを元に新規作成

2011/01/13  1.特別活動の記録の観点を(全角17文字X8行)に修正。

2011/01/25  1.特別活動の記録の観点を"HTRAINREMARK_DAT"の"SPECIALACTREMARK"に変更した。
            2.特別活動の記録の観点を17文字X3行に変更した。
            3.ＣＳＶ画面に送るパラメータに特別活動の記録の観点を追加した。

2011/04/08  1.文字数チェックをバイトチェックに変更した。

2011/10/28  1.学校種別が"J"ではない生徒の場合、更新ボタンを無効とした。
            2.データＣＳＶボタンに権限とプログラムＩＤのパラメータを追加した。

2012/02/07  1.通知表所見参照ボタン、出欠の記録参照ボタンを追加した。
            2.行動の記録・特別活動の記録画面に学期データの参照を追加した。

2012/02/16  1.学習活動、観点、評価、特別活動の記録の観点のテキストエリアの文字数を変更した。
            2.活動の状況・様子をカット。
            3.活動の記録、部活動・その他を追加した。
            4.行動の記録でプロパティー「knjdBehaviorsd_UseText = 1」のときの名称マスタ参照をカットした。
            5.行動の記録、特別活動の記録がチェックボタンのときのチェックの不具合を修正した。

2012/02/29  1.出欠の記録備考のサイズをプロパティー「HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J」から参照するようにした。

2012/03/19  1.総合所見及び指導上参考となる諸事項のサイズをプロパティー「HTRAINREMARK_DAT_TOTALREMARK_SIZE_J」から参照するようにした。

2013/02/08  1.行動の記録・特別活動の記録の通知表をプロパティー「sanshouReport = 1」の時、表示するよう修正

2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)

2013/08/14  1.DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            
2014/01/24  1.各項目を入力中に文字数の制限チェックをするために共通関数common.jsのcharCountを追加

2014/02/03  1.各項目に既存入力内容を追加

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正
            2.「総合的な学習の時間の記録」横の既入力内容の参照画面のタイトルを変更

2015/03/23  1.名称マスタ(NAMECD1:Z010 / NAMECD2:00 / NAME1:sapporo)のとき、通知表所見参照ボタンを非表示に修正

2016/03/15  1.通知票取込ボタン追加
            -- プロパティー「KoudouNoKirokuTorikomi = 1」で表示する

2016/04/06  1.プロパティtrain_ref_1_2_3_use_J=1の時、総合所見を３分割で表示する。
            -- 関連プロパティ
            -- train_ref_1_2_3_field_size_J = 21-14-7
            -- train_ref_1_2_3_gyo_size_J = 5

2016/11/30  1.テキストボックス内でEnterキーのsubmit無効

2017/02/15  1.通知票所見参照画面でIE11で所見が折り返し表示されるように修正

2017/03/14  1.部活動、委員会、資格参照登録機能追加
            2.修正

2017/03/24  1.駿台は所見サイズ変更

2017/07/14  1.大会記録備考参照ボタン追加
            --プロパティ「club_kirokubikou」が"1"のとき、表示する。

2017/07/20  1.大会記録備考参照で校種は左画面より受け取ったものを使用

2017/07/25  1.学校マスタ情報取得するとき、SCHOOLCD、SCHOOLKINDを渡すよう修正
            -- AttendAccumulate.php(1.7)に伴う修正

2017/08/08  1.通知表所見参照で、プロパティ"tutihyoSansyoKirikae=1"の時
            --DBのプロパティテーブルにセットした項目を参照する。
            --↓セット例↓
            --  KNJA125J_HREPORTREMARK_DAT__ATTENDREC_REMARK_J = 出欠の記録備考
            --  KNJA125J_HREPORTREMARK_DETAIL_DAT__04_01_REMARK1_J = 所見
            --名付方法「KNJA125J_テーブル名__フィールド名_校種 = 項目名」
            --  ※テーブルは HREPORTREMARK_DATか、HREPORTREMARK_DETAIL_DAT
            --  ※DETAILの場合フィールド名の前に"区分_コード"を加える

2017/08/10  1.rev1.29(2017/08/08)の修正を大幅変更
            -- J_tutihyosanshou_MAX_CNT分、J_tutihyosanshouに設定されてるデータを表示する。
            -- # 指導要録の通知表所見参照画面の出力項目設定
            -- # J_tutihyosanshou:ソート順   値：テーブル名@フィールド名@WHEREの条件:区切り@タイトル
            -- J_tutihyosanshou_MAX_CNT = 5
            -- J_tutihyosanshou:1 = HREPORTREMARK_DETAIL_DAT@REMARK1@01:01@学習活動
            -- J_tutihyosanshou:2 = HREPORTREMARK_DETAIL_DAT@REMARK1@01:02@評価
            -- J_tutihyosanshou:3 = HREPORTREMARK_DETAIL_DAT@REMARK1@02:01@特別活動・資格等
            -- J_tutihyosanshou:4 = HREPORTREMARK_DETAIL_DAT@REMARK1@03:01@部活動等
            -- J_tutihyosanshou:5 = HREPORTREMARK_DETAIL_DAT@REMARK1@04:01@所見

2017/09/20  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/26  1.通知表所見参照ボタン非表示、プロパティー「unUseSyokenSansyoButton」が"1"の時
            2.前回の修正、プロパティーを「unUseSyokenSansyoButton_J」に変更

2017/10/05  1.表彰参照ボタン追加、プロパティー「useHyosyoSansyoButton_J」が"1"の時表示

2017/10/06  1.前回の修正漏れ

2017/10/20  1.総合的な学習の時間の各項目は以下のプロパティを参照
            --「HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J」
            --「HTRAINREMARK_DAT_VIEWREMARK_SIZE_J」
            --「HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J」

2017/12/25  1.行動の記録参照ボタン追加

2018/02/14  定型文入力ボタン追加

2018/02/16  1.定型入力の入力後にウインドウを閉じて色変えを行う

2018/04/04  1.出欠の記録参照ボタンで表示されるウィンドウの内容変更

2018/04/04  1.賞選択ボタンで表示されるウィンドウの内容変更

2018/09/25  1.データCSVへのパラメータ（学習活動、観点、評価のフィールドサイズ）を修正
            --固定ではなく、コメントと同様のプロパティーを参照するように変更
            ※データCSV取込でのエラーチェックの不具合対応

2018/10/16  1.部活動複数校種設定対応
            -- プロパティー「useClubMultiSchoolKind」参照

2018/12/13  1.委員会参照にプロパティー「useSchool_KindField」仕様追加

2018/12/26  1.ボタンカット
            -- 部活動参照、大会記録備考参照、委員会参照、資格参照
            2.ボタン追加（共通プログラム）
            -- 委員会選択、部活動選択、記録備考選択、検定選択
            3.共通プログラムに変更
            -- 賞選択

2019/01/28  1.既入力内容参照に指定年度未満の条件追加

2019/03/05  1.定型文が1行しかない場合、テキストエリアに反映されない不具合修正

2019/03/13  1.道徳テキストエリアを追加
            2.通知票取込ボタンを追加
            -- プロパティー「tutihyoYoriYomikomiHyoujiFlg_J 」が"1"の時表示
            -- プロパティー追加：「tutihyoYoriYomikomiHyoujiFlg_J 」

2019/03/18  1.通知票取込処理で、評価の項目に観点の値が入っていたので、観点の値は観点の項目に設定するよう、修正。
            2.通知票取込処理で、評価の項目を設定する処理を追加。
            3.通知票取込処理で、tutisyoSougoushokengakkiプロパティが'1-2'となっている場合、
              2学期の教科コード'90'で登録されている学習活動、観点、評価の値を取得するよう、変更。
            4.評価の項目にある定型文選択ボタン処理で、選択した文字列が観点に反映されるため、評価に反映されるよう、修正。
            5.評価の入力で、定型文選択で入力した際はテキストボックスの背景色を変更する処理を追加。
            6.評価の値の出力位置が誤っていたので、修正。

2019/03/19  1.総合的な学習の時間の観点と評価のデータ表示位置を入れ替え
            2.評価の項目にある定型文選択ボタン処理で、選択した文字列が観点に反映されるため、評価に反映されるよう、修正。

2019/03/22  1.出欠の記録参照で通知表所見が無くても出欠データが表示されるよう修正

2019/04/12  1.プロパティー名変更
            --「HTRAINREMARK_DAT_REMARK1_SIZE_P」→「HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J」

2019/04/17  1.近大中学(名称マスタ:Z010[KINJUNIOR])の場合、道徳の箇所に「通知票の参照」ボタンを追加

2019/07/04  1.「年間出欠備考選択」ボタンを追加
            -- プロパティー「useReasonCollectionBtn」が'1'のとき表示

2019/09/24  1.「年間出欠備考選択」のパラメーター修正

2019/10/29  1.「賞選択」ボタンの位置を変更

2019/12/14  1.九段は道徳の通知表取込みボタンを追加する

2019/12/20  1.前回の九段道徳通知票取込み不具合対応

2020/01/31  1.道徳の定型文マスタのコードを10に変更
            2.「知能検査偏差値」テキストを追加
            -- プロパティー「knje125j_Chinokensa_show」が'1'のとき表示

2020/02/03  1.道徳の定型文マスタのコードを11に変更

2020/02/10  1.名称マスタ「Z010」が'rakunan'の場合、道徳の通知表取込ボタンを追加

2020/02/13  1.通知票参照ボタンを追加

2020/02/14  1.通知票参照ボタンの表示を修正
            -- プロパティー「knja125j_Sougoushoken_TutisyoShoken_Button_Hyouji」が'1'のとき表示

2020/02/21  1.以下のプロパティを参照し「まとめ出欠備考参照」「まとめ出欠備考取込」ボタンを表示するよう修正
              useAttendSemesRemarkDat_J が "0" の場合 「日々出欠備考参照」
              useAttendSemesRemarkDat_J が "1" の場合 「まとめ出欠備考参照」
              useAttendSemesRemarkDat_J が "1" かつ useTorikomiAttendSemesRemarkDat_J が "1" の場合 「まとめ出欠備考取込」

2020/03/10  1.以下のプロパティを参照し定型選択ボタンを表示するよう修正
            -- プロパティー「Knja125j_HTRAINREMARK_TEMP_DAT」追加。'0'以外の時、値に対応する定型文を取得    ：中学校指導要録道徳の文言評価
            -- プロパティー「TotalRemark_HTRAINREMARK_TEMP_DAT」追加。が'0'以外の時、値に対応する定型文を取得 ：指導要録、調査書の総合所見1・2
            -- プロパティー「SpecialAct_HTRAINREMARK_TEMP_DAT」追加。'0'以外の時、値に対応する定型文を取得  ：指導要録、調査書の特別活動1・2

2020/03/11  1.以下のプロパティを参照し総合所見の定型選択ボタンを表示するよう修正
            -- プロパティー「seitoSidoYorokuSougou_Teikei_Button_Hyouji」'1'の時、定型文選択ボタン表示
               ※ただしプロパティー「TotalRemark_HTRAINREMARK_TEMP_DAT='1'」が設定されている場合は「TotalRemark_HTRAINREMARK_TEMP_DAT」が優先される
            2.「TotalRemark_HTRAINREMARK_TEMP_DAT」が'1'の時、定型文ボタンを表示するよう修正(定型文は「DATA_DIV=12」「DATA_DIV=13」を取得)
            3.「SpecialAct_HTRAINREMARK_TEMP_DAT」が'1'の時、定型文ボタンを表示するよう修正(定型文は「DATA_DIV=14」「DATA_DIV=15」を取得)
            4.「Knja125j_HTRAINREMARK_TEMP_DAT」の参照を削除

2020/03/26  1.「tutihyoDoutokuYomikomiHyoujiFlg_J」が "1" の場合道徳の帯に「通知票取込」ボタンを表示
            2.以下のプロパティを追加
              # 通知票より読込ボタンの表示・非表示(中学：KNJA125J)
              # 「道徳」 1:表示、1以外:非表示
              tutihyoDoutokuYomikomiHyoujiFlg_J = 1

              # 「道徳」通知票より読込ボタンの取込み項目設定(中学：KNJA125J)
              # tutihyoDoutokuYomikomiField_J = テーブル名@フィールド名@WHEREの条件:区切り@タイトル
              #   テーブル名：HREPORTREMARK_DAT or HREPORTREMARK_DETAIL_DAT
              #   @WHEREの条件：HREPORTREMARK_DETAIL_DAT(DIV:CODE)
              #tutihyoDoutokuYomikomiField_J = HREPORTREMARK_DETAIL_DAT@REMARK1@08:01@道徳

2020/04/15  1.以下の条件で表示される道徳欄の通知表取込におけるSQLを修正。
            --「tutihyoDoutokuYomikomiHyoujiFlg_J」が "1" の場合 かつ「tutihyoDoutokuYomikomiField_J」による設定が無い場合

2020/07/22  1.「中学で履修済み備考」を追加
            2.「罰選択」ボタンを追加

2021/01/14  1.コード自動整形、PHP7対応
            2.Z010がteihachiの場合の更新エラー対応
            3.プロパティーの参照エラー対応

2021/01/29  1.「Totalremark_2disp_J」が1の時、総合所見及び指導上参考となる諸事項を2分割するように修正
            2.名称マスタZ010が「kwansei」の時、「総合所見及び指導上参考となる諸事項」と「出欠の記録備考」がそれぞれ空の場合に初期値を表示するように修正
            3.各入力枠の文字数制限の文字数の設定ミスを修正
            4.プロパティSpecialactremark_3disp_J=1の場合、特別活動の記録の観点を３つの入力欄に分ける

2021/02/15  1.「中学で履修済み備考」を年間毎ではなく通年での登録に変更
            2.プロパティ「HTRAINREMARK_TEMP_SCORE_MST_J」 = 1の時、道徳と総合的な学習の時間の記録の3枠の初期値を定型文得点から取得してセットするように修正
            3.プロパティ「HTRAINREMARK_TEMP_SCORE_MST_J」 = 1の時、定型文選択と通知票取込ボタンを非表示に修正

2021/02/17  1.「全取込」ボタン、「調査書特別活動参照」ボタン作成

2021/03/12  1.近大中学用に総合的な探究の時間と道徳の通知票取込処理を追加

2021/03/18  1.「Totalremark_2disp_J」= 0の時に各種ボタンからの取り込みができないバグの修正
