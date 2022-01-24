# kanji=漢字
# $Id: readme.txt 74442 2020-05-21 01:14:49Z maeshiro $

2011/01/07  新規作成

2011/01/14  1.クラス選択コンボボックスを変更しました。
            --SCHREG_REGD_GDATの「SCHOOL_KIND='P'」の学年を対象とした。
            --学年の場合、学年名称１を表示する。

2011/01/24  1.印影印字パラメータ追加

2011/02/18  1.制限付の条件を追加。
            -- 個人選択で、年組コンボに追加
            -- 学年選択で、リストtoリストに追加 

2012/01/27  1.改竄チェック追加

2012/02/16  1.印刷ボタン実行後の選択学生がリセットされる不具合を修正

2012/02/29  1.プロパティーHTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P追加

2012/03/23  1.カラープリンター使用チェックボックス、文言印刷チェックボックスを追加

2012/04/02  1.改竄チェックが処理されない不具合を修正
            2.印刷ボタンを押すと選択がクリアされる不具合を修正
            3.改竄チェックのハッシュ式を修正

2012/06/27  1.パラメータuseCurriculumcdを追加

2012/07/01  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
                
2012/07/18  1.Warning:～の表示の対応修正

2014/01/14  1.パラメータuseAddrField2を追加

2014/04/09  1.更新印刷処理修正。（更新が終わる前に印刷処理をしていた。）

2014/04/11  1.2012/02/16での不具合を修正

2014/06/27  1.余分なチェック処理をカット

2014/08/12  1.カラープリンター使用チェックボックスをカット(固定で1を帳票にパラメータ渡し)

2014/09/08  1.style指定修正

2014/11/10  1.プロパティーseitoSidoYorokuKinsokuFormP追加

2017/01/06  1.プロパティーuse_finSchool_teNyuryoku_P追加

2017/02/08  1.プロパティーnotPrintFinschooltypeName追加

2017/03/09  1.文言「生徒」を「児童」に変更

2017/03/12  1.プロパティーseitoSidoYorokuNotPrintKantenBlankIfPageOver追加

2018/03/29  1.プロパティーseitoSidoYorokuUseEditKinsokuP、seitoSidoYorokuUseSvfFieldAreaP追加

2018/07/11  1.プロパティーHTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P追加

2018/07/13  1.プロパティーHTRAINREMARK_DAT_TOTALREMARK_SIZE_P追加

2018/10/12  1.ハッシュにFOREIGNLANGACT4を追加

2019/03/15  1.プロパティーknja133pUseSlashNameMst追加

2019/03/29  1.プロパティーHTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P、HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P、HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P、HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P、HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P追加

2019/04/27  1.プロパティーknja133pUseViewSubclassMstSubclasscd2追加

2019/02/27  1.プロパティーseitoSidoYorokuCheckPrintIneiPが1の場合「担任印影出力」、2の場合「校長・担任印影出力」チェックボックス追加

2020/05/21  1.プロパティーseitoSidoYorokuCheckPrintIneiPが3の場合「担任印影出力」、4の場合「校長・担任印影出力」チェックボックスをデフォルトonに変更

2021/05/04  1.コード自動整形
            2.特例の授業等の記録選択追加

