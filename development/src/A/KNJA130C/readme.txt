$Id: readme.txt 72951 2020-03-12 14:07:58Z maeshiro $


2009/10/13  1.KNJA130(1.10)を元に新規作成

2009/10/17  1.「prgInfo.properties」の「formType」の値を帳票に送るよう修正
            2.「formType」を「seitoSidoYorokuFormType」に変更

2009/11/02  1.帳票に渡すパラメーター(seitoSidoYorokuFieldSize)の追加

2009/11/05  1.帳票に渡すパラメーター(seitoSidoYorokuSougouFieldSize)の追加

2010/02/02  1.帳票に渡すパラメーターの(seitoSidoYorokuSpecialactremarkFieldSize)追加

2010/04/02  1.「戸籍氏名出力」を追加

2010/04/22  1.「戸籍名出力」をカット
            2.javascript エラー修正

2010/04/26  1.帳票に渡すパラメーターにログイン日付追加

2010/06/09  1.プロパティーファイルの参照方法を共通関数を使うよう修正
            2.初期値の設定追加

2010/08/10  1.「現住所の方書き(アパート名)出力」を画面上カット

2010/10/04  1.帳票に渡すパラメーター(seitoSidoYorokuKinsokuForm)の追加

2011/03/08  1.学年、クラスコンボの条件に「SCHREG_REGD_GDATの学校種別が'H'の学年」を追加した。

2011/10/06  1.プロパティーseitoSidoYorokuZaisekiMae、seitoSidoYorokuKoumokuMei追加

2012/01/27  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/04/06  1.プロパティーseitoSidoYorokuHyotei0ToBlank追加

2012/05/07  1.プロパティーseitoSidoYorokuYoshiki2PrintOrder追加

2012/05/10  1.プロパティーuseCurriculumcd追加

2013/01/08  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/03/04  1.パラメータseitoSidoYorokuNotPrintAnotherStudyrec、seitoSidoYorokuNotPrintAnotherAttendrecを追加

2013/08/14  1.パラメータtrain_ref_1_2_3_field_sizeを追加

2013/12/25  1.パラメータuseAddrField2を追加
            2.パラメータuseProvFlgを追加

2014/01/06  1.未履修科目出力、履修のみ科目出力のラジオボタンを追加（デフォルト値は名称マスタ「A027」設定）

2014/01/10  1.履修登録のみ科目出力のラジオボタンを追加（デフォルトは名称マスタ「A027」）

2014/01/14  1.パラメータuseGakkaSchoolDivを追加

2014/05/26  1.更新/削除等のログ取得機能を追加

2014/05/27  1.ログ取得機能修正 (画面表示データを取得するよう修正)

2014/09/08  1.style指定修正

2015/07/13  1.プロパティーuseAssessCourseMst="1"の時、評定マスタを切り替えるよう修正
                - ASSESS_MST ⇒ ASSESS_COURSE_MST

2016/03/14  1.プロパティーseitoSidoYoroku_Totalstudyact2_val2_UseTextFlg追加

2016/07/05  1.所見サイズプロパティー追加

2017/02/08  1.プロパティーnotPrintFinschooltypeName追加

2017/03/13  1.年組表示順修正

2017/08/01  1.ＣＳＶボタンカット

2017/08/04  1.パラメータseitoSidoYorokuHozonkikanを追加

2017/09/14  1.パラメータseitoSidoYoroku_train_ref_1_2_3_field_size,seitoSidoYoroku_train_ref_1_2_3_gyo_sizeを追加

2017/10/13  1.ＣＳＶボタン復活、プロパティ「unUseCsvBtn_YorokuTyousa」が"1"の時、ＣＳＶボタン非表示

2017/11/02  1.千代田九段（「Z010」名称1がchiyoda）は３．出欠の記録の指定を追加
            2.パラメータseitoSidoYorokuTotalStudyCombineHtrainremarkDatを追加

2017/11/09  1.千代田九段以外の印刷指示選択不具合修正
            2.パラメータcertifPrintRealNameを追加

2018/01/05  1.パラメータDBNAME2追加

2018/02/15  1.プロパティーseitoSidoYorokuNotUseSubclassSubstitution追加

2018/02/16  1.プロパティーseitoSidoYorokuFinschoolFinishDateYearOnly追加

2018/02/25  1.プロパティーseitoSidoYorokuHankiNinteiが1なら半期認定のチェックボックスと追加する

2018/02/28  1.プロパティーseitoSidoYorokuUseEditKinsokuH追加
            2.プロパティーseitoSidoYorokuHanasuClasscd追加

2018/03/14  1.プロパティーseitoSidoYorokuZaisekiSubekiKikanMaxMonth追加

2018/03/16  1.プロパティーseitoSidoYorokuNotPrintZaisekiSubekiKikan追加

2018/04/09  1.プロパティーseitoSidoYorokuSogoShoken3Bunkatsu追加

2018/05/02  1.CSV出力の単位数に加算単位数を加算

2018/05/22  1.茗溪は「修得単位の記録」に「IBコース」チェックボックスを追加表示する

2018/06/07  1.プロパティーseitoSidoYorokuSougouHyoukaNentani追加
            2.プロパティーseitoSidoYorokuHoushiNentani追加
            3.プロパティーseitoSidoYorokuUsePrevSchoolKindGrdDivNameAsFinschoolGrdName追加
            4.プロパティーseitoSidoYorokuNotPrintFinschoolGrdDivDefaultName追加

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --生徒・保護者氏名出力
             --現住所の郵便番号出力
             --学校所在地の郵便番号出力

2018/06/16  1.チェックボックス不具合修正

2018/07/26  1.出力ラジオボタンがクラスのとき、ＣＳＶ出力ボタンは使用不可

2018/08/02  1.文言修正（半期認定対応様式で出力）

2018/09/27  1.プロパティーseitoSidoYorokuTaniPrintAbroad、seitoSidoYorokuTaniPrintSogaku追加

2018/10/25  1.プロパティーseitoSidoYorokuYoshiki1UraBunkatsuRishu追加

2019/01/10  1.プロパティーseitoSidoYorokuPrintCoursecodename追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/02/13  1.プロパティーseitoSidoYorokuPrintDropRecInTenTaiYear追加

2019/03/27  1.プロパティーseitoSidoYorokuPrintGappeimaeSchoolname追加

2019/08/23  1.プロパティーseitoSidoYorokuTaniPrintTotal追加

2019/09/03  1.プロパティーseitoSidoYorokuCheckPrintIneiHが2の場合「校長・担任印影出力」チェックボックス追加

2019/09/05  1.プロパティーseitoSidoYorokuPrintHosoku追加

2020/02/06  1.印影出力がサブミット時にクリアされないよう修正

2020/03/12  1.CSV出力の総合的な探究の時間対応

2021/01/28  1.コード自動整形
            2.プロパティhyoteiYomikaeRadio=1の場合、評定読替・切替選択を表示する

2021/03/28  1.青山学院はCSVをServletで出力する

2021/04/19  1.プロパティー「seitoSidoYorokuPrintOrder」追加

2021/05/04  1.特例の授業等の記録選択追加
            2.特例の授業等の記録のチェックボックスをデフォルトオフに修正
