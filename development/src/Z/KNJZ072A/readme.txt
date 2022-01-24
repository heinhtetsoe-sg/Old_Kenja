# kanji=漢字
# $Id: readme.txt 74216 2020-05-12 05:46:40Z ishii $

2013/04/24  1.KNJZ070を元に新規作成

2014/07/10  1.ログ取得機能追加

2014/08/08  1.詳細登録ボタンをプロパティuseClassDetailDat = 1の時のみ、表示するよう修正

2014/08/13  1.詳細登録ボタンに権限のパラメータを追加
            2.詳細登録ボタンの表示プロパティ作成により下記を変更
              - useClassDetailDat ⇒ hyoujiClassDetailDat

2014/08/15  1.詳細登録ボタンの表示にプロパティuseCurriculumcdも参照するよう修正

2018/05/11  1.useCurriculumcd、useClassDetailDat、hyoujiClassDetailDatが1で
              年度を追加したときSUBCLASS_DETAIL_DATも年度コピーする

2020/05/12  1.YDATから科目を除く更新時にその科目を参照しているテーブルが存在する場合はエラーを出力するよう修正

2021/03/18  1.リファクタリング、php7対応

2021/03/19  1.テーブルにデータが存在するかのチェックに年度条件を追加
            2.Insertの前に、SUBCLASS_DETAIL_DATの削除処理が漏れていた

2021/03/24  1.SUBCLASS_DETAIL_DATの削除処理の位置を変更
