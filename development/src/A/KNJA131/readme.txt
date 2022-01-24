# kanji=漢字
# $Id: readme.txt 64733 2019-01-18 04:36:30Z matsushima $

/*** KNJA131 生徒指導要録中高印刷（千代田区） readme.txt***/


2006/03/09　　学習の記録を前期、後期課程に分ける（gakushu1,gakushu2）
2006/03/20 o-naka NO001 （校長印および担任印の出力のため）パラメータ DOCUMENTROOT を追加

2009/09/09  1.tokio:/usr/local/development/src/A/KNJA131からコピーした。
            2.印影出力チェックボックスをカット。

2012/01/27  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2012/06/14  1.パラメータuseCurriculumcdを追加

2013/01/08  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2014/09/08  1.style指定修正

2018/06/11  1.宮城県の場合 以下のチェックボックスをカット
             --生徒・保護者氏名出力

2019/01/18  1.CSV出力の文字化け修正(Edge対応)