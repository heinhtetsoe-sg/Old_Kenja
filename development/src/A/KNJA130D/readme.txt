$Id: readme.txt 69553 2019-09-05 07:49:23Z maeshiro $


2009/10/28  1.KNJA130C(1.3)を元に新規作成

2009/11/02  1.帳票に渡すパラメーター(seitoSidoYorokuFieldSize)の追加

2009/11/05  1.帳票に渡すパラメーター(seitoSidoYorokuSougouFieldSize)の追加

2010/02/02  1.帳票に渡すパラメーターの(seitoSidoYorokuSpecialactremarkFieldSize)追加

2010/04/02  1.「戸籍氏名出力」を追加

2010/04/22  1.「戸籍名出力」をカット
            2.javascript エラー修正

2010/04/26  1.帳票に渡すパラメーターにログイン日付追加

2010/06/09  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/08/10  1.「現住所の方書き(アパート名)出力」を画面上カット

2010/10/04  1.帳票に渡すパラメーター(seitoSidoYorokuKinsokuForm)の追加

2011/02/18  1.印影パラメータを追加した。
            2.不要な行を削除した。
            3.印影パラメータでＣＳＶボタンを表示を制限した。

2011/03/08  1.学年、クラスコンボの条件に「SCHREG_REGD_GDATの学校種別が'H'の学年」を追加した。

2011/03/28  1.ＣＳＶ出力で総合的な学習の時間の記録（学習活動、評価）をHTRAINREMARK_DATから出力するよう変更した。

2011/10/03  1.下記のパラメータの条件を変更した。
            --seitoSidoYorokuSougouFieldSize（"1"、または"0"（1以外））
            --seitoSidoYorokuSpecialactremarkFieldSize（"1"、または"0"（1以外））
            2.下記のパラメータを追加した。
            --seitoSidoYoroku_dat_TotalstudyactSize
            --seitoSidoYoroku_dat_TotalstudyvalSize
            3.署名の改ざんチェックを追加した。

2011/10/04  1.記述ミスを修正した。

2011/10/06  1.プロパティーseitoSidoYorokuZaisekiMae、seitoSidoYorokuKoumokuMei追加

2012/01/27  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/01/27  1.権限チェック削除

2012/04/06  1.プロパティーseitoSidoYorokuHyotei0ToBlank追加

2012/05/07  1.プロパティーseitoSidoYorokuYoshiki2PrintOrder追加

2012/05/10  1.プロパティーuseCurriculumcd追加

2013/01/08  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/03/04  1.パラメータseitoSidoYorokuNotPrintAnotherStudyrec、seitoSidoYorokuNotPrintAnotherAttendrecを追加

2013/04/03  1.ハッシュ値作成時の教育課程対応時の修正漏れ対応

2013/12/25  1.パラメータuseAddrField2を追加
            2.パラメータuseProvFlgを追加

2014/01/06  1.未履修科目出力、履修のみ科目出力のラジオボタンを追加（デフォルト値は名称マスタ「A027」設定）

2014/01/10  1.履修登録のみ科目出力のラジオボタンを追加（デフォルトは名称マスタ「A027」）

2014/01/14  1.パラメータuseGakkaSchoolDivを追加

2014/04/09  1.更新印刷処理修正。（更新が終わる前に印刷処理をしていた。）

2014/09/08  1.style指定修正

2015/02/26  1.個人を選択する際、担任署名済み・校長署名済み・改ざん無しの生徒は「署」を表示する

2015/03/23  1.前回の修正漏れを修正（生徒移動時のソート）

2015/03/25  1.「前籍校の出欠を含めない」チェックボックスを追加。（初期値はseitoSidoYorokuNotPrintAnotherAttendrec='1'の場合、ON）

2015/08/19  1.京都の場合、KNJD133SからコールされていなくてもATTEST_OPINIONS_UNMATCH更新処理をする。

2016/03/14  1.プロパティーseitoSidoYoroku_Totalstudyact2_val2_UseTextFlg追加

2016/07/05  1.所見サイズプロパティー追加

2017/02/08  1.プロパティーnotPrintFinschooltypeName追加

2017/03/13  1.HRコンボに表示順指定追加 

2017/08/01  1.ＣＳＶボタンカット

2017/08/04  1.パラメータseitoSidoYorokuHozonkikanを追加

2017/10/13  1.ＣＳＶボタン復活、プロパティ「unUseCsvBtn_YorokuTyousa」が"1"の時、ＣＳＶボタン非表示

2017/11/02  1.千代田九段（「Z010」名称1がchiyoda）は３．出欠の記録の指定を追加
            2.パラメータseitoSidoYorokuTotalStudyCombineHtrainremarkDatを追加

2018/01/05  1.パラメータDBNAME2追加

2018/02/15  1.プロパティーseitoSidoYorokuNotUseSubclassSubstitution追加

2018/02/16  1.プロパティーseitoSidoYorokuFinschoolFinishDateYearOnly追加

2018/02/28  1.プロパティーseitoSidoYorokuUseEditKinsokuH追加
            2.プロパティーseitoSidoYorokuHanasuClasscd追加

2018/04/09  1.プロパティーseitoSidoYorokuSogoShoken3Bunkatsu追加

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --生徒・保護者氏名出力
             --現住所の郵便番号出力
             --学校所在地の郵便番号出力

2018/06/14  1.プロパティーseitoSidoYoroku_train_ref_1_2_3_field_size、train_ref_1_2_3_field_size、seitoSidoYoroku_train_ref_1_2_3_gyo_size、train_ref_1_2_3_gyo_size追加

2018/06/16  1.チェックボックスの不具合修正

2018/07/26  1.出力ラジオボタンがクラスのとき、ＣＳＶ出力ボタンは使用不可

2018/09/27  1.プロパティーseitoSidoYorokuTaniPrintAbroad、seitoSidoYorokuTaniPrintSogaku追加

2018/10/02  1.プロパティーseitoSidoYorokuCheckPrintIneiHが1の場合「担任印影出力」チェックボックス追加

2018/10/25  1.プロパティーseitoSidoYorokuYoshiki1UraBunkatsuRishu追加

2019/01/10  1.プロパティーseitoSidoYorokuPrintCoursecodename追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/02/04  1.プロパティー追加 (KNJA130Cにあわせた)

2019/02/13  1.プロパティーseitoSidoYorokuPrintDropRecInTenTaiYear追加

2019/03/27  1.プロパティーseitoSidoYorokuPrintGappeimaeSchoolname追加

2019/08/23  1.プロパティーseitoSidoYorokuTaniPrintTotal追加

2019/09/03  1.プロパティーseitoSidoYorokuCheckPrintIneiHが2の場合「校長・担任印影出力」チェックボックス追加

2019/09/05  1.プロパティーseitoSidoYorokuPrintHosoku追加

2021/04/19  1.リファクタリング
            2.プロパティー「seitoSidoYorokuPrintOrder」追加

2021/05/04  1.特例の授業等の記録選択追加
            2.特例の授業等の記録のチェックボックスをデフォルトオフに修正
