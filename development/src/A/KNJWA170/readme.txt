# kanji=漢字
# $Id: readme.txt,v 1.19 2009/09/25 04:23:49 m-yama Exp $

2007/12/12 新規作成(処理：新規　レイアウト：新規)

2007/12/27 検索条件で志願者の受験区分が3:転入 or 4:編入の条件を省いた
           印刷指定をラジオボタンに変更

2007/12/29 入学辞退、移動者は除く

2008/01/11 1.生徒証もデータベースに更新
           2.証明書種類の変更 通学証明書:306 運賃割引証:307
           3.生徒証の種別:9、種類コード:308に設定 
           
2008/01/21 権限の設定

2008/01/24 CERTIF_ISSUE_DATのCERTIF_NOはTYPEに応じてCERTIF_NO+1

2008/01/28 パラメータにイメージファイルのパスを設定

2008/02/04 1.検索条件に、在籍年度を追加
           2.生徒証を選択したとき、入学年月日、卒業予定日、備考を表示する
           3.通学証明書・旅客運賃割引証を選択したとき、駅から、駅まで、経由を表示
           
2008/02/06 1.所属マスタ表示項目をSCHOOLNAME1>>>SCHOOLNAME3に変更
           2.初期表示では、データが表示されないように修正

2008/02/25  1.下記の修正をした。
            -- 左検索の年度をブランクなしにした
            -- 左検索の年度をパラメータとして渡す

2008/02/27  1.年度は全て、CTRL_YEARではなく$model->search["EXE_YEAR"]を使用する。
            2.パラメータにmodel追加

2008/04/02  1.以下の修正をした。
            -- 通学証明書：$model->search["EXE_YEAR"]
            -- 通学証明書以外：CTRL_YEAR

2008/08/01  1.通学証明書選択の場合、有効期間開始日を元に、終了日に一ヶ月後、引く一日をセットする。

2008/12/03  1.開始日より終了日が小さい場合（年+１）2008/12/02 → 2009/01/01

2009/04/04  1.日付データ仕様変更

2009/09/25  1.以下の修正をした
            -- 有効期間の修正
            -- 日付のチェック
