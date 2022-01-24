// kanji=漢字
// $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2011/01/21  1.新規作成

2011/01/25  1.特別活動の記録の観点を"HTRAINREMARK_DAT"の"SPECIALACTREMARK"に変更した。
            2.特別活動の記録の観点を17文字X3行に変更した。
            3.ハッシュ値取得に学習記録データ、出欠記録データを追加した。

2011/02/03  1.ハッシュ値取得の不具合（学習記録データ）を修正した。

2011/03/16  1.修正

2011/04/08  1.文字数チェックをバイトチェックに変更した。

2011/10/28  1.データＣＳＶボタンを追加した。
            2.学校種別が"J"ではない生徒の場合、更新ボタンを無効とした。

2012/02/08  1.通知表所見参照ボタン、出欠の記録参照ボタンを追加した。
            2.行動の記録・特別活動の記録画面に学期データの参照を追加した。

2012/02/16  1.学習活動、観点、評価、特別活動の記録の観点のテキストエリアの文字数を変更した。
            2.活動の状況・様子をカット。
            3.活動の記録、部活動・その他を追加した。
            4.行動の記録でプロパティー「knjdBehaviorsd_UseText = 1」のときの名称マスタ参照をカットした。
            5.行動の記録、特別活動の記録がチェックボタンのときのチェックの不具合を修正した。

2012/02/29  1.出欠の記録備考のサイズをプロパティー「HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J」から参照するようにした。

2012/03/19  1.総合所見及び指導上参考となる諸事項のサイズをプロパティー「HTRAINREMARK_DAT_TOTALREMARK_SIZE_J」から参照するようにした。

2012/07/25  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
            2.ハッシュ値取得修正

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

2017/07/25  1.学校マスタ情報取得するとき、SCHOOLCD、SCHOOLKINDを渡すよう修正
            -- AttendAccumulate.php(1.7)に伴う修正

2017/10/20  1.総合的な学習の時間の各項目は以下のプロパティを参照
            --「HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J」
            --「HTRAINREMARK_DAT_VIEWREMARK_SIZE_J」
            --「HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J」
