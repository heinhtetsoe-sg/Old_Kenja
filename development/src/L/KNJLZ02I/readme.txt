# kanji=漢字
# $Id: f8a1035088cefedea9c6aeab046ed244af9de780 $

2020/09/02  1.新規作成

2020/09/08  1.名称マスタL009の名称がNULLでないもののみをリストに表示するよう修正

2020/09/09  1.名称マスタL009で中学はNAME1,高校はNAME2を参照するよう修正

2020/09/24  1.KNJL3020→KNJLZ02Iにリネーム
            2.名称マスタ使用している処理の修正
              -- V_NAME_MST→ENTEXAM_SETTING_MSTに変更

2020/09/28  1.以下の修正をした
            --マスタが空の時に年度コンボが出ない不具合修正
            --全ての科目を右に移動させた状態で更新するとDBエラーとなる不具合修正
            --更新後に入試年度がクリアされないよう修正

2020/10/07  1.入試制度が高校の場合に、入試設定マスタL009の科目名が表示されない不具合修正

2020/10/08  1.以下の修正をした
            --年度コンボのソート順を降順に修正
            --入試制度コンボの初期値を名称マスタから取得するよう修正

2020/10/20  1.入試区分コンボをVALUE昇順でソートするように修正
