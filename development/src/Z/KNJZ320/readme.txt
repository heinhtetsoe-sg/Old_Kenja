# kanji=漢字
# $Id: readme.txt 62687 2018-10-06 11:28:53Z yamashiro $

2011/07/04  1.CVS登録
            2.使用可能が無い場合の処理変更
            -- メニュー/サブメニューの権限を９ → メニュー/サブメニューを削除

2011/08/18  1.左側のコンボの初期値表示を変更
            -- 修正前：固定で、"C0000"
            -- 修正後：menuInfo.propertiesの下記を参照して、『頭1桁 + '0000'』を初期値とする。
            -- 　　　　useRootMenu = C1000

2014/07/28  1.ログ取得機能追加

2016/07/15  1.テーブルのキー追加に伴う修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで切替

2018/03/07  1.一括変更ラジオボタン追加

2018/03/30  1.useSchoolWare=1の時、職員一覧はSTAFF_WORK_HIST_DATから出力する。

2018/03/31  1.学校CDはSCHOOL_DETAIL_DATではなく、Z010のNAME2を使う

2018/10/06  1.STAFF_WORK_HIST_DAT.USE_KNJ = '1'のみ対象
