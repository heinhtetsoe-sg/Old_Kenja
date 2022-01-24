# $Id: readme.txt 64737 2019-01-18 04:38:11Z matsushima $
/*** KNJA131K 賢者版「中学指導要録」 readme.txt***/


2006/04/14 yamauchi 新規作成（KNJD131Jをコピー）
2006/04/26 yamauchi NO001 個人、クラス選択のラジオボタンを追加。画面が切り替わるように変更。

2009/03/27  1.prgInfo.propertiesのuseSchregRegdHdatが1の時にのみパラメータ"1"(それ以外は0)を送信するように修正

2009/03/29  1.prgInfo.propertiesがないときにWarningが出てしまうのを修正

2009/09/09  1.印影出力チェックボックスをカット。

2010/06/09  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2012/01/27  1.教育課程の追加、追加に伴う修正
              - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/04/25  1.プロパティーseitoSidoYorokuCyugakuKirikaeNendo追加

2012/07/10  1.プロパティーuseCurriculumcd追加

2013/01/09  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/05/13  1.プロパティーseitoSidoYorokuCyugakuKirikaeNendoForRegdYear追加

2014/01/14  1.パラメータuseAddrField2を追加

2014/09/08  1.style指定修正

2017/08/01  1.ＣＳＶボタンカット

2017/10/13  1.ＣＳＶボタン復活、プロパティ「unUseCsvBtn_YorokuTyousa」が"1"の時、ＣＳＶボタン非表示

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --生徒・保護者氏名出力

2019/01/18  1.CSV出力の文字化け修正(Edge対応)