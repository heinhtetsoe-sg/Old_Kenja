# kanji=漢字
# $Id: readme.txt 67056 2019-04-17 08:12:25Z yamauchi $

2012/06/11  1.新規作成

2012/06/13  1.下記を修正
            -- テキスト右詰
            -- 指定月の最終日以下かチェック
            -- 同一年月のコピー不可
            -- 対象月コンボのデフォルトをログイン日から
            2.対象月のデータ取得処理修正
            3.データ表示枠修正
            
2014/04/15  1.IE8(環境：windows xp)でのレイアウト修正
            2.レイアウト作り直し
            3.年組情報をSCHREG_REGD_HDATより情報を取得し、表示

2014/08/01  1.ログ取得機能追加

2015/03/02  1.呼び出し元の権限を取得するよう修正

2015/12/01  1.自動計算対象チェックボックス作成
            2.useFi_Hrclass='1'の場合FIクラスを参照する。

2016/06/03  1.プロパティ「useFi_Hrclass」= '1'の時、クラス選択ラジオボタンを表示
            -- それ以外は法定クラスとする
            -- プロパティ「useFi_Hrclass」での切替をクラス選択ラジオボタンに変更

2016/06/09  1.行事予定日数取得にクラス区分参照追加
            2.行事から日数計算の不具合を修正

2016/09/21  1.コピー元コンボ、データ一覧、更新処理、コピー処理修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/24  1.行事から日数計算での対象月データ取得処理修正

2018/09/13  1.テキスト修正
            -- 3桁まで入力可
            -- 指定月の最終日以下かチェックをカット

2019/01/29  1.プロパティー「useSchool_KindField」の時、渡ってきた校種またはSCHOOLKIND参照に変更

2019/04/17  1.コピー元を前年度データに選択したときの不具合修正
