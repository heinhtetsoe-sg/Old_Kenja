# kanji=漢字
# $Id: readme.txt 69649 2019-09-11 18:00:08Z ishii $

2015/10/27  1.KNJX_C035Kを元に新規作成

2016/09/19  1.科目コンボ、データ出力、データ取込時の在籍チェック変更
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2016/09/20  1.年度学期コンボ変更
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2016/12/02  1.年度学期取得SQLから制限付権限用の条件をカット

2017/01/26  1.年度学期取得SQLに「CHAIR_STF_DAT」のSTAFFCDで制限付権限の条件をセット

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2018/05/29  1.プロパティーuse_prg_schoolkind追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/17  1.CSV処理画面終了時、親画面に結果が反映されるように修正

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2021/02/10  1.CSVメッセージ統一(SG社)