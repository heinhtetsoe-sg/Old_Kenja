# kanji=漢字
# $Id: readme.txt 64741 2019-01-18 04:40:35Z matsushima $

2009/11/06  1.tokio:/usr/local/development/src/A/KNJA170からコピーした。

2009/11/13  1.チェックボックスにラベル機能を追加した。

2011/04/13  1.リファクタリング
            2.以下の修正をした
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)
            -- 権限渡し
            -- エクセル出力ボタン表示有無

2011/04/14  1.セキュリティーチェックに該当した場合の処理を以下の通り変更。
            -- 変更前：画面を閉じる。
            -- 変更後：「エクセル出力」ボタンを表示しない。
            -- 補足：「プレビュー／印刷」ボタンがあるプログラムの仕様。
            2.単独で呼び出された場合閉じずに、ボタン非表示
            3.修正

2014/07/14  1.学期コンボの取得に共通関数を使用しないように修正

2015/01/28  1.新規フォーム追加に伴い、帳票パターン選択ラジオボタンを追加

2015/01/30  1.CSV出力を修正
            -- DUMMYの出力カット
            -- Bパターンの担任名出力カット
            -- Bパターンに保護者電話番号を追加
            2.電話番号を出力する指定は、保護者電話番号も指定通りに出力する。

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/04/27  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)