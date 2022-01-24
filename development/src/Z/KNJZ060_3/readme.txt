# kanji=漢字
# $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

2013/07/04  1.新規作成

2013/07/10  1.SEQ = 007 を追加、その他修正

2013/07/11  1.オリジナルコード(名称マスタのコード)の更新を追加

2013/07/30  1.表示する教科リストの仕様変更
              - CLASS_YDAT ⇒ CLASS_MSTより出力
            2.処理年度のコンボの仕様変更
              - 選択した処理年度のCLASS_YDATに存在するデータでSORT（CLASS_YDATのデータ有が上にくる）
            3.前年度コピーの仕様変更
              - CLASS_YDATは見ないで、CLASS_DETAIL_DATのみを見るように修正

2013/07/31  1.画面レイアウトを修正（SEQを表示）

2014/04/11  1.ラベル機能追加

2014/11/14  1.ログ取得機能追加

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/06  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
