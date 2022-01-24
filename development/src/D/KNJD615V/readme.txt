# kanji=漢字
# $Id: readme.txt 76401 2020-09-03 10:45:01Z maeshiro $

2013/07/22  1.新規作成

2013/07/29  1.同一クラスでのチェックボックスをカット
            2.テスト種別コードをADMIN_CONTROL_SDIV_DATより取得

2013/07/30  1.テスト種別コードにSCORE_DIV追加。学期末・学年末を表示しない条件を削除。

2013/08/08  1.プログラムIDをKNJD615GからKNJD615Vへ変更

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/01/22  1.考査種別コンボのラベル表示を修正
            -- 名称マスタ「D053」の参照をカットした。

2014/02/01  1.帳票パターンの「結果」時数→「欠課」時数に変更
            2.レイアウト変更

2014/02/18  1.帳票タイトル修正
            2.帳票パターン修正
            3.帳票タイトル修正

2014/05/30  1.更新/削除等のログ取得機能を追加

2014/06/17  1.欠点に追指導を含むチェックボックスを追加

2014/06/18  1.総合順位出力を学年、学科、コースに変更

2014/06/19  1.権限が制限付のときの条件に副担任を追加

2014/07/05  1.コメント修正

2014/09/23  1.前/後期科目の選択を追加（科目変動型のみ使用）

2015/03/30  1.欠点者数に欠査者を含めないチェックボックスを追加

2015/07/06  1.備考欄出力（出欠備考を出力）チェックボックスを追加

2015/07/28  1.テストコンボを学校種別参照に変更

2016/01/08  1.CSV出力ボタン追加（科目変動型は使用不可）

2016/02/09  1.備考欄出力選択（全て/学期から/年間まとめ）を追加

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/08/12  1.学年コンボの参照先をSCHREG_REGD_GDATに変更
            2.総合順位出力ラジオボタンのラベルの不具合修正

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/10/12 1.プロパティーknjd615vPrintNullRemark追加

2017/04/18 1.集計開始日付追加
           2.欠課時数を0表記するチェックボックス追加

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/05/10  1.宮城県のADMIN_CONTROL_SDIV_DAT対応
            2.帳票名と並び順変更
            3.不具合修正

2017/07/27  1.文言修正

2017/11/22  1.プロパティーknjd615vSelectForm、knjd615vDefaultOutputRank、knjd615vDefaultOutputKijun追加

2017/11/30  1.パラメータDOCUMENTROOT追加

2018/02/08  1.出力順指定追加
            2.プロパティーknjd615vPrintPerfect追加

2018/02/18  1.欠課時数を分数表記するチェックボックス追加

2018/04/06  1.全クラスの成績表を出力するチェックボックス追加

2018/06/16  1.空行を詰めて印字チェックボックス追加

2018/06/22  1.帳票パターン１（科目固定型）の時のみ分数表記チェックは有効

2019/07/22  1.プロパティーuseAssessCourseMst追加

2019/07/29  1.プロパティーknjd615vNotNewPageByCourseとコースごと改ページのチェックボックスを追加

2019/08/13  1.プロパティーprintSubclassLastChairStd追加

2019/10/10  1.文言修正

2019/10/17  1.プロパティーuseAttendSemesHrRemark、hibiNyuuryokuNasi追加

2019/11/01  1.プロパティーuseSchoolMstSemesAssesscd追加

2019/12/18  1.プロパティーknjd615vDefaultOutputRank、knjd615vDefaultOutputKijunを
              校種別プロパティーに分けて、そちらを優先してチェックするよう、変更。
              -- knjd615vDefaultOutputRank_J、knjd615vDefaultOutputRank_H、knjd615vDefaultOutputKijun_J、knjd615vDefaultOutputKijun_Hプロパティー追加
            2.学年選択で、対象学年の校種が変化する際は、総合順位出力、順位の基準点の値を
              初期値(上記プロパティ値)で設定しなおすよう、変更

2020/02/19  1.knjd615vSelectBikoTermTypeプロパティが立っている時、画面下部の備考欄の表示が切り替わるよう、変更
            2.knjd615vSelectBikoTermTypeプロパティが帳票に渡るよう、変更
              -- knjd615vSelectBikoTermTypeプロパティ追加
            3.knjd615vSelectBikoTermTypeプロパティが立っている時、出欠集計単位毎の設定マスタから
              出欠集計単位名称を新規コンボボックスに設定するよう、変更

2020/03/25  1.名称マスタ「Z010」が「koma」の時、画面下部のレイアウトを変更するよう修正

2020/04/23  1.プロパティーknjd615vGroupDivが1の場合、出力対象範囲（クラス毎、学年毎、コース毎）の選択を表示する

2020/05/12  1.プロパティーknjd615vShowRankOutputRangeが1の場合、出力順位範囲入力を表示する
            2.プロパティーknjd615vSelectOutputValueが1の場合、出力内容選択を表示する

2020/05/15  1.プロパティーknjd615vGroupDivが1で出力対象範囲を変更した場合、総合順位出力を合わせる
            2.プロパティーknjd615vGroupDivが1,2,3以外の場合、出力対象範囲を表示しない
            3.プロパティーknjd615vSelectOutputValueが1の場合、出力内容選択を表示する

2020/07/01  1.名称マスタ「Z010」が'koma'の場合の処理を削除

2020/08/04  1.以下のプロパティーを追加
            --# KNJD615V
            --# KNJD615V_NameKirikae　1:文字数によってフォントサイズ切替を行う
            --KNJD615V_NameKirikae = 1

2020/09/03  1.プロパティーknjd615vJugyoJisuLessonを追加

2021/01/14  1.コード自動整形
            2.プロパティーuseSubclassWeightingCourseDatをパラメータに追加

2021/05/11  1.プロパティーknjd615vDefaultFormでデフォルトの帳票パターンを指定する
            -- 帳票パターンの指定値はknjd615vSelectFormと同様以下のとおり
            -- 1:科目固定型 4:科目固定型（成績の記録） 5:科目固定型（出欠の記録） 3:科目固定型（仮評定付） 2:科目変動型

