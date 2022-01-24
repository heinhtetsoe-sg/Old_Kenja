# kanji=漢字
# $Id: readme.txt 74448 2020-05-21 05:50:30Z maeshiro $

2005.06.09 nakamoto 記載責任者に空白を追加
2005.06.09 nakamoto 印刷パラメータ(入力時ＯＳ選択)を追加・・・hiddenで渡す=1:XP
2005.11.18 nakamoto NO001:記載日付を空白でもＯＫとするよう修正
                    NO002:帳票へ渡すパラメータに、今年度(CTRL_YEAR)を追加
2006/09/20 m-yama   ネスト、コメントの修正をした。
                    オブジェクト作成をメソッドにした。
                    未履修科目出力ラジオを追加した。
2006/09/22 m-yama   ６年用フォーム選択チェックボックスを追加した。

2009/04/01  1.熊本は３年用フォームと表示、熊本以外は６年用フォームと表示

2009/04/13  1.「評定読替」項目を画面上から削除。
            2.「履修のみ科目出力　する／しない」のラジオボタンを追加。
            3.「未履修科目出力」が「する」になっている状態で、「履修のみ科目出力」で「しない」が選択された場合、メッセージ表示し「する」が選択されている状態にする。

2009/09/24  1.指導上参考となる諸事欄、３分割フォームチェックボックス追加

2009/12/09  1.熊本と鳥取だけ３年用フォームと表示するよう修正

2009/12/12  1.学習成績概評チェックボックスの初期値を"on"に変更した。
            2.未履修科目出力ラジオボタンの初期値を「しない」に変更した。

2010/03/25  1.パラメータの追加tyousasyoAttendrecRemarkFieldSize

2010/06/11  1.「評定読替」チェックボックスの復活

2010/06/23  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/07/08  1.パラメーター追加(gaihyouGakkaBetu)

2010/07/14  1.「３年用フォーム」と「６年用フォーム」の表示をプロパティーの値で切り替える

2010/07/20  1.パラメーター追加(train_ref_1_2_3_field_size、3_or_6_nenYoForm、tyousasyoSougouHyoukaNentani)

2010/07/21  1.「指導上参考となる諸事欄」チェックボックスのカット、「○年用フォーム」の値修正

2010/07/30  1.調査書入力で使っている値をパラメータに追加、三分割のチェックボックス追加

2010/08/17  1.「評定読替」はプロパティーが「hyoteiYomikae = 1」の時に表示するよう修正

2010/08/26  1.近大の場合、「未履修科目出力」、「履修のみ科目出力」を非表示にした。

2010/09/13  1.以下の２つの帳票パラメータを追加。hiddenで渡す。
            -- 総合的な学習の時間の単位を０表示
            -- 留学の単位を０表示

2010/09/24  1.パラメータの追加「tyousasyoKinsokuForm」

2011/08/04  1.何年用フォームを使うかの判定に名称マスタ「Z001」のNAMESPARE2を追加した。

2012/01/12  1.評定読替のチェックボックスの初期値をチェック有りに修正

2012/06/13  1.パラメータに「useCurriculumcd」を追加した。

2012/08/01  1.パラメータの追加tyousasyoEMPTokuBetuFieldSize

2013/03/04  1.パラメータの追加tyousasyoNotPrintAnotherStudyrec
            2.チェックボックスの追加（出欠の前籍校を含まない）

2013/06/27  1.パラメータ「useClassDetailDat」追加

2013/12/26  1.パラメータuseAddrField2、useProvFlgを追加

2014/01/14  1.パラメータuseGakkaSchoolDivを追加

2014/05/02  1.ラベル機能追加
            2.リファクタリング

2014/05/13  1.パラメータtrain_ref_1_2_3_gyo_size追加

2014/08/21  1.パラメータtyousasyoNotPrintEnterGrade追加

2015/07/14  1.useAssessCourseMstのパラメータを追加

2016/05/10  1.パラメータ「useMaruA_avg」を追加

2016/08/22  1.パラメータ「tyousasyoSyusyokuPrintGappeiTougou」を追加

2016/09/27  1.９．備考の文字、桁数のプロパティーtyousasyoRemarkFieldSize追加

2016/12/26  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク
            -- 「showMaskStaffCd」が無いときは通常表示

2017/07/25  1.パラメータ「tyousasyoUseEditKinsoku」追加

2017/09/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            2.プロパティーtyousasyoNotCheckGaihyoCommentが1の場合、画面を開いた際に、学習成績概評「学年人数を印刷する。 」をチェックしない

2017/10/27  1.プロパティーcertifPrintRealName追加

2018/02/20  1.プロパティーtyousasyoHankiNinteiが1の場合、進学用（半期認定）、就職用（半期認定）の選択を追加する

2018/04/30  1.パラメータuseTotalstudySlashFlg、useAttendrecRemarkSlashFlg、DOCUMENTROOT追加

2018/05/22  1.プロパティーtyousasyoCheckCertifDateが1の場合、記載（証明）日付をデフォルトセットせず、印刷時に空の場合エラーとする
            2.プロパティーtyousasyoPrintHomeRoomStaffが1の場合、記載責任者選択はブランク。帳票は記載責任者欄は生徒の卒業時の担任を印字する。

2018/07/30  1.プロパティーtyousasyoPrintCoursecodename追加

2018/10/10  1.プロパティーtyousasyoPrintChairSubclassSemester2追加

2018/10/22  1.プロパティーtyousasyoHanasuClasscdに設定された教科コードは総合的な学習の時間の上に出力する

2018/11/09  1.「総合的な学習の時間の単位を０表示」選択、「留学の単位を０表示」選択ラジオボタンを追加

2019/03/21  1.プロパティーtyousasyoJiritsuKatsudouRemark追加

2019/06/14  1.プロパティーtyousasyoNoSelectUseSyojikou3、tyousasyoNoSelectNenYoForm追加

2019/06/24  1.プロパティーknja110aShowAbroadPrintDropRegdが1の場合、指導要録に仮在籍を印字するチェックボックスを表示

2020/05/21  1.プロパティーhyoteiYomikaeが'1_off'の場合、「評定が1の場合は2で処理する。」チェックボックスは表示のみでデフォルトONにしない

2021/01/28  1.コード自動整形

2021/04/19  1.Chromeで左画面が表示されるよう修正
