# kanji=漢字
# $Id: readme.txt 69586 2019-09-09 13:31:00Z maeshiro $

2005.06.09 nakamoto 記載責任者に空白を追加
2005.11.21 nakamoto NO001:記載日付を空白でもＯＫとするよう修正
                    NO002:帳票へ渡すパラメータに、今年度(CTRL_YEAR)を追加

2008/09/03  1.６年用フォーム選択チェックボックスを追加した。単位制のみ表示。

2009/12/10  1.リファクタリング
            -- Tabをスペースに
            -- ネストの修正
            2.パラメータ追加。プロパティーファイルのKnje080UseAFormを渡す。

2010/06/23  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2012/06/13  1.パラメータに「useCurriculumcd」を追加した。

2013/04/08  1.Propertiesの綴りを修正。

2013/04/30  1.パラメータに「seisekishoumeishoTaniPrintRyugaku」を追加した。

2014/01/14  1.パラメータuseGakkaSchoolDivを追加

2014/05/07  1.ラベル機能追加
            2.リファクタリング

2016/12/26  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク
            -- 「showMaskStaffCd」が無いときは通常表示

2017/05/25  1.パラメータDOCUMENTROOT追加

2017/09/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/10/27  1.プロパティーcertifPrintRealName追加

2018/08/21  1.プロパティーseisekishoumeishoPrintCoursecodename追加

2019/09/02  1.左フレームが表示されない不具合修正

2019/09/09  1.プロパティーseisekishoumeishoCreditOnlyClasscd追加
