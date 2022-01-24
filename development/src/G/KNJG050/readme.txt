// kanji=漢字
// $Id: readme.txt 71787 2020-01-16 04:31:52Z maeshiro $

2006/02/10 o-naka KNJG050,KNJG051を統合

2009/03/02  1.学校マスターの学校区分＝1のときは、3学年以上するようにした。
            2.SQL修正

2010/02/04  1.「卒業見込み出力」チェックボックス追加
            2.「卒業見込み出力」チェックボックスの値を保持するよう修正

2010/02/11  1.ソート修正
            -- クラスをINTにキャストしていたが、近大は文字が入っている為。

2011/02/23  1.パラメータにプロパティーの値(certifNoSyudou)を追加した。

2012/02/08  1.名称マスタ「A023」の登録があるとき、NAMESPARE2からNAMESPARE3の値を学年コンボへ表示するよう修正

2014/05/12  1.学年コンボの参照テーブルをSCHREG_REGD_GDATに変更
            2.年度・学期はログイン年度・学期を参照
            3.ラベル機能追加
            4.リファクタリング
            5.生徒取得の参照テーブルをSCHREG_REGD_DATに変更

2014/08/08  1.ログ取得機能追加

2015/01/27  1.「入学・卒業日付は年月で表示する(卒業証明書のみ)」チェックを追加

2015/01/30  1.文言修正

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/10/07  1.固定文言「生徒」修正
            -- SETTING_DAT参照、なければ固定で「生徒」
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/16  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/10/27  1.プロパティーcertifPrintRealName追加

2018/02/23  1.パラメーターcertif_no_8keta追加

2018/02/26  1.パラメーターuseShuryoShoumeisho追加
            2.useShuryoShoumeishoが1の場合、卒業証明書の文言を終了証明書に変更

2020/01/16  1.パラメーターknjg050PrintStamp、DOCUMENTROOT追加

2021/03/04  1.コード自動整形
            2.プロパティーknjg050PrintStamp=checkboxの場合、指示画面に印影出力するチェックボックスを追加する
