# kanji=漢字
# $Id: readme.txt 69358 2019-08-23 13:45:24Z maeshiro $

2011/02/08  1.KNJA130Bをもとに新規作成
            2.フォーム選択ラジオボタンを削除した。

2011/03/08  1.クラスコンボの条件に「SCHREG_REGD_GDATの学校種別が'H'の学年」を追加した。

2012/01/27  1.権限チェック削除

2012/03/05  1.ハッシュ値作成に成績データと出欠データを追加した。

2012/06/27  1.パラメータuseCurriculumcdを追加

2012/07/01  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/06/27  1.パラメータseitoSidoYorokuNotPrintAnotherStudyrec、seitoSidoYorokuNotPrintAnotherAttendrecを追加

2013/03/20  1.東京都の場合出欠の記録のチェックボックスを追加

2014/01/14  1.パラメータuseAddrField2を追加

2014/03/27  1.未履修科目出力、履修のみ科目出力、履修登録のみ科目出力のラジオボタンを追加（デフォルト値は名称マスタ「A027」）

2014/04/09  1.更新印刷処理修正。（更新が終わる前に印刷処理をしていた。）

2014/09/08  1.style指定修正

2014/09/30  1.パラメータuseStudyrecRemarkQualifiedDat追加

2014/10/09  1.カラープリンター使用チェックボックスをカット（パラメータは固定1）
            2.東京都の場合出欠の記録のみでも出力するように修正

2017/01/07  1.「１年間休学の場合、学習の記録ページを表記しない」チェックボックスを追加
            2.パラメータ追加

2017/03/13  1.年組表示順修正

2018/02/16  1.プロパティーseitoSidoYorokuFinschoolFinishDateYearOnly追加

2018/04/29  1.宮城県は「生徒・保護者氏名出力」を表示しない

2018/05/18  1.宮城県はSIMEIパラメータ(hidden)追加

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --現住所の郵便番号出力
             --学校所在地の郵便番号出力

2018/06/16  1.チェックボックスの不具合修正

2018/07/15  1.プロパティーHR_ATTEND_DAT_NotSansyou追加

2018/10/30  1.宮城県の現住所、学校所在地チェックボックス不具合修正

2019/01/10  1.プロパティーnotPrintFinschooltypeName追加
            2.プロパティーseitoSidoYorokuPrintCoursecodename追加

2019/04/23  1.パラメータseitoSidoYorokuNotPrintZaisekiSubekiKikan追加

2019/06/24  1.プロパティーseitoSidoYorokuTaniPrintAbroad、seitoSidoYorokuTaniPrintSogaku追加

2019/08/23  1.プロパティーseitoSidoYorokuTaniPrintTotal追加
