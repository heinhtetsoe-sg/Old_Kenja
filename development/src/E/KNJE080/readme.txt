# kanji=漢字
# $Id: readme.txt 69588 2019-09-09 13:43:16Z maeshiro $

2005.06.09 nakamoto 記載責任者に空白を追加
2005.11.21 nakamoto NO001:記載日付を空白でもＯＫとするよう修正
                    NO002:帳票へ渡すパラメータに、今年度(CTRL_YEAR)を追加

2008/09/03  1.６年用フォーム選択チェックボックスを追加した。単位制のみ表示。

2009/04/01  1.校長名印刷のラジオボタン追加
            2.クラス選択コンボボックスが変わっても出力対象一覧の値を保持よう修正

2009/12/10  1.パラメータ追加。プロパティーファイルのKnje080UseAFormを渡す。

2010/06/22  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2012/06/13  1.パラメータに「useCurriculumcd」を追加した。

2013/04/30  1.パラメータに「seisekishoumeishoTaniPrintRyugaku」を追加した。

2014/01/14  1.パラメータuseGakkaSchoolDivを追加

2014/01/21  1.パラメータuseAddrField2追加

2014/04/10  1.ラベル機能追加
            2.リファクタリング

2014/08/26  1.ログ取得機能追加

2015/03/12  1.パラメータseisekishoumeishoNotPrintAnotherStudyrec追加

2015/05/13  1.リスト間の移動のソート順の不具合を修正

2015/08/27  1.組名称はMAX文字数で調整して表示するよう修正

2016/12/26  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク
            -- 「showMaskStaffCd」が無いときは通常表示

2017/03/20  1.6年用フォーム指定を保持

2017/04/03  1.年組コンボに表示順追加

2017/05/25  1.パラメータDOCUMENTROOT追加

2017/10/27  1.プロパティーcertifPrintRealName追加

2018/08/21  1.プロパティーseisekishoumeishoPrintCoursecodename追加

2019/09/09  1.プロパティーseisekishoumeishoCreditOnlyClasscd追加
