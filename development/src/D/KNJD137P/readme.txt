// kanji=漢字
// $Id: readme.txt 69336 2019-08-22 11:07:09Z ishii $

2011/01/13  1.KNJA125Pを元に新規作成

2011/01/26  1.出欠の記録備考を追加した。

2011/04/19  1.文字数チェックをバイトチェックに変更した。

2011/05/20  1.レイアウトを変更した。
            2.テーブルをHREPORTREMARK_DATに変更した。
            3.生活・特別活動のようすをボタンに変更し、共通プログラムを呼び出すようにした。
            4.テキスト入力範囲の文字数、行数はプロパティーを参照するようにした。
            5.データCSVボタンのプログラム参照先を変更した。
            6.データの更新処理を「delete & insert」→「insert or update」に変更した。

2011/06/07  1.行動の記録に渡すパラメータを追加(138Jや137P毎でKNJD_BEHAVIOR_SDの設定可能なように)
            -- send_knjdBehaviorsd_UseText
            
2011/06/10  1.１行のテキストエリア内のEnterキー押下後の動作修正（画面遷移しないように修正）

2011/07/01  1.所見入力画面の項目名を変更
            2.ＣＳＶ処理画面に渡すパラメータにSCHOOL_KINDを追加した。

2014/03/11  1.更新時のロック機能(レイヤ)を追加

2014/03/28  1.更新時のロック機能(レイヤ)修正

2014/04/30  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)
