# kanji=漢字
# $Id: readme.txt 56589 2017-10-22 12:59:34Z maeshiro $

2017/07/20  1.新規作成

2017/08/03  1.県内扱いの僻地の文字を削除。(From1.html)
            2.評価の使用を年度によって変更できるように修正。
            　NAME_YDATに対象の年度データを作成。
              NAME_MSTの取得をYDATからの取得に変更。(Query)
              点数を入力するTEXTBOXのnameを単純なcountからNAMECD2に変更。
              それに伴って、更新時にもNAME_YDATを取得してから更新を行う。(NAME_YDATに入っていない判定のデータは更新されない)
              また、TEXTBOXのnameが変わったのでjsでのエラーチェック用にhiddenを作成し、エラーチェックで使用している。
              (Form1.php, Form1.js, Model, Query)
