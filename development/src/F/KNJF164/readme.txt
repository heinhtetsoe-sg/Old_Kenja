# kanji=漢字
# $Id: readme.txt 76326 2020-08-28 08:25:18Z arakaki $

2011/07/28  1.新規作成

2011/07/29  1.SQLの不具合を修正した。
            2.取り込んだファイルの拡張子チェックを追加した。

2011/08/03  1.その他病気のヘッダを修正した。
            2.病気けが（入学前）、病気けが（入学後）、家族情報を修正した。
            --出力：連番を数値でソートした。
            --取込：MAX連番取得が文字での取得になっていた不具合を修正した。

2011/08/08  1.家族情報を修正した。
            --親族番号で"1"と"01"を区別するようにした。
            --HEALTH_RELA_DATの追加、更新の条件を変更した。

2011/08/09  1.家族情報のHEALTH_RELA_DATの追加、更新の判定の不具合を修正した。

2012/01/06  1.健康調査の質問の出力順を変更した。

2012/02/16  1.健康調査の質問の答えの出力順を質問と対応するように修正
            2.健康調査のCSV出力処理修正
            3.健康調査の質問の答えのソート処理修正

2016/07/13  1.【入学後の病気】・・「スポーツ振興センター」をカット

2016/10/06  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/22  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/15  1.CSV出力の文字化け修正(Edge対応)

2020/08/28  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/02  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/15  1.CSVメッセージ統一(SG社)