// kanji=漢字
// $Id$

2020/08/28  1.新規作成

2020/10/19  1.対象受験者全員に対して面接班を割り振れない場合エラーとなる処理の追加
            2.RECEPT作成の対象となる受験者のENTEXAM_RECEPT_DATのDELETE、INSERTの処理を追加
            3.入試制度コンボ、入試区分コンボをVALUE昇順でソートするように修正

2020/12/04  1.リファクタリング
            2.印刷ボタン押下時のRECEPT作成方法を変更(DELETE-INSERT => 存在しなければINSERT)

2020/12/07  1.更新時確認メッセージ追加