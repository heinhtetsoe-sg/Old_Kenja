# kanji=漢字
# $Id: readme.txt 72234 2020-02-06 01:37:47Z maeshiro $

2011/01/14  1.新規作成

2011/01/24  1.印影印字パラメータ追加

2011/02/18  1.制限付の条件を追加。
            -- 個人選択で、年組コンボに追加
            -- 学年選択で、リストtoリストに追加 

2012/01/27  1.改竄チェック追加

2012/02/16  1.印刷ボタン実行後の選択学生がリセットされる不具合を修正

2012/02/29  1.プロパティーHTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J追加

2012/03/21  1.プロパティーHTRAINREMARK_DAT_TOTALREMARK_SIZE_J追加

2012/03/23  1.カラープリンター使用チェックボックスを追加

2012/04/02  1.改竄チェックが処理されない不具合を修正
            2.印刷ボタンを押すと選択がクリアされる不具合を修正
            3.改竄チェックのハッシュ式を修正

2012/04/25  1.プロパティーseitoSidoYorokuCyugakuKirikaeNendo追加

2012/06/27  1.パラメータuseCurriculumcdを追加

2012/07/01  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/03/29  1.プロパティーseitoSidoYorokuCyugakuKantenNoBlank追加

2013/05/13  1.プロパティーseitoSidoYorokuCyugakuKirikaeNendoForRegdYear追加

2014/01/14  1.パラメータuseAddrField2を追加

2014/04/09  1.更新印刷処理修正。（更新が終わる前に印刷処理をしていた。）

2014/06/27  1.余分なチェック処理をカット

2014/08/05  1.カラープリンター使用チェックボックスをカット(固定で1を帳票にパラメータ渡し)

2014/09/08  1.style指定修正

2014/11/10  1.プロパティーseitoSidoYorokuKinsokuFormJ追加

2014/11/13  1.プロパティーseitoSidoYorokuPrintInei追加

2015/02/18  1.所見用プロパティー追加

2015/02/25  1.一部のhiddenで指定している変数名を修正
                - $ObjForm ⇒ $objForm

2016/04/07  1.プロパティーtrain_ref_1_2_3_use_J、train_ref_1_2_3_field_size_J、train_ref_1_2_3_gyo_size_J追加

2016/11/24  1.「奇数ページの時は空白ページを印刷する」チェックボックス追加

2017/02/08  1.プロパティーnotPrintFinschooltypeName追加

2017/03/09  1.文言「生徒」を「児童」に変更
            2.1.の変更をカット

2017/11/15  1.プロパティーcertifPrintRealName追加

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --生徒・保護者氏名出力
             --現住所の郵便番号出力
             --学校所在地の郵便番号出力

2018/06/26  1.画面の不具合修正

2018/08/08  1.プロパティーknja133jForm1、knja133jForm3、knja133jForm4を追加

2018/08/29  1.宮城県は氏名印字パラメータを固定1でセット

2018/10/02  1.プロパティーseitoSidoYorokuCheckPrintIneiJが1の場合「担任印影出力」チェックボックス追加

2019/04/08  1.プロパティーknja133jUseViewSubclassMstSubclasscd2追加

2019/12/14  1.プロパティーHTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J追加

2020/02/06  1.プロパティーseitoSidoYorokuCheckPrintIneiJが2の場合「校長・担任印影出力」チェックボックス追加

2021/05/04  1.コード自動整形
            2.特例の授業等の記録選択追加

