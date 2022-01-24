# kanji=漢字
# $Id: readme.txt 76327 2020-08-28 08:26:02Z arakaki $

2013/07/09  1.近大のバックアップから2009/08/13の更新日付に戻した。
                - KNJH160専用のプログラムとする

2014/05/14  1.ＤＢエラーを修正

2014/08/20  1.ログ取得機能追加
            2.ラベル機能追加

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/08/28  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/02  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/09  1.CSVメッセージ統一(SG社)