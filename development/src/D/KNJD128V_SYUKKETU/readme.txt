# kanji=漢字
# $Id: readme.txt 70364 2019-10-25 08:22:51Z yamauchi $

2014/07/04  1.新規作成
            2.印刷とCSVボタンカット
            3.修正

2014/10/02  1.指定学期の範囲月が年を超える場合に表示されるように修正
            2.対象月コンボの学期指定をカット
            3.名簿は対象月に所属している生徒に変更

2015/06/30  1.レイアウト修正。ヘッダー項目はスクロールしないようにした。
            -- 参考:KNJC035E

2015/07/03  1.ACCESS_LOG_DETAILの更新処理をコメントアウト(宮城のDBエラーからの対策)

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/06/28  1.ボタンの表示を「保存」→「更新」に変更
            2.成績入力画面からコールされたときの戻るボタンの不具合修正
            3.ヘッダ部分の授業時数テキストボックス内を右寄せに変更

2018/04/23  1.戻るボタンを押した時、警告メッセージMSG108を出力する

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2019/10/25  1.未入力の判定方法変更
