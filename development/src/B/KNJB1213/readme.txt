# kanji=漢字
# $Id: readme.txt 69386 2019-08-26 11:34:36Z ishii $

2012/10/25  1.新規作成
            2.対象年度コンボの位置を移動した。
            -- 生徒データ表示欄の年度を対象年度コンボを参照するように変更した。
            -- 新入生の場合、「ログイン年度+1」のみを表示するように変更した。
            3.新入生の場合、FRESHMAN_DATを参照するように変更した。
            4.学籍番号、氏名はパラメータで受け取ったデータを表示するように変更した。
            5.SCHREG_ADDRESS_DATは有効期間開始日がMAXのデータを参照するように変更した。

2012/10/26  1.在籍者の場合、対象年度のSCHREG_REGD_DATがなくても入学年度、住所１、住所２を表示するように変更した。

2012/10/29  1.指定登録日コンボの初期値をMAX日付にし、降順表示に変更した。
            2.SCHREG_TEXTBOOK_FREE_DATに登録されている教科書に「●」を表示した。

2012/10/30  1.「●」印をカット。
            2.指定年度以外に登録された教科書の背景色を変更するように修正した。
            
2012/10/31  1.科目数を見るテーブルを変更 
                - CHAIR_STD_DAT ⇒ SUBCLASS_STD_SELECT_DAT(GROUPCD = '001')

2012/11/02  1.過年度に教科書を登録したら背景色を変更するように修正した。
            -- 背景色を水色→赤に変更した。
            2.登録済み教科書を表示するように修正した。
            3.初期画面（登録日コンボが空）のとき、更新可能な教科書を無償給与対象に表示する。
            4.指定登録日に登録されている教科書は相互移動可に変更した。

2012/11/05  1.登録日を重複して更新したときＤＢエラーとなる不具合を修正した。
            2.無償給与対象外の集計の不具合を修正した。
            3.金額は、カンマ区切りにする。

2015/09/08  1.履修登録の履歴に伴う修正

2015/12/17  1.履修登録の履歴に伴う修正をカットし、元に戻した

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)
