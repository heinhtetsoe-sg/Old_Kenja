// kanji=漢字
// $Id: readme.txt 71920 2020-01-23 01:58:00Z maeshiro $

2013/10/11  1.新規作成
            2.ボタン文言修正（新規←→追加）

2013/10/15  1.以下の修正をした
            -- 入試制度空白なし
            -- 事前番号表示
            -- 生年月日の初期値(4：平成)
            -- エンターキーをTABキーと同様に

2013/10/17  1.画面レイアウト修正

2013/10/17  1.事前番号の取得方法変更

2013/10/18  1.画面レイアウト修正
            2.事前番号より事前データの入試区分と受験コースを表示

2013/10/21  1.内心点の９０点換算値の保存処理追加

2013/10/22  1.出身学校登録方法変更

2013/10/28  1.新規ボタン押下時に、中学校CDも残す
            2.生徒氏名欄を入力したら、氏名かなも同時に入力する。

2013/10/29  1.志望区分パターンのチェック処理修正
            2.画面レイアウト修正
            3.画面レイアウト修正

2013/11/26  1.志望区分の横に受験区分表示(数値)、内申欄に換算値表示

2013/11/29  1.「かな」→「カナ」に変更

2013/12/09  1.画面レイアウト修正
            2.入試区分の初期値修正
            3.氏名カナに半角カナが使われている場合、更新時に全角変換し、更新する
            4.削除実行後も入試制度、入試区分、志望区分、出身学校は残したままにする

2013/12/11  1.氏名、かな、性別、生年月日、出身学校、内申の背景色をピンクに変更

2013/12/13  1.新規ボタン押し下げ後、氏名欄にカーソルを移動する

2014/01/16  1.自動表示される文字変更「ひらがな」→「カタカナ」
            -- 氏名漢字を入力したら、氏名カナ欄に自動表示される機能

2014/01/23  1.自動表示される文字変更(保護者)「ひらがな」→「カタカナ」
            -- 氏名漢字を入力したら、氏名カナ欄に自動表示される機能
            
2014/02/06  1.志望区分の新規追加時のチェックを修正
            2.エラーメッセージ、ボタン名修正

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正

2014/12/18  1.検索条件追加に伴い、学校検索の呼び出し時のウインドウサイズを変更

2018/12/07  1.キー["APPLICANTDIV"]追加に伴う修正

2019/01/10  1.入試区分マスタの取得SQLに入試制度の条件を追加

2019/04/15  1.卒業年月の元号対応
            -- 初期値が固定で「4:平成」となっているのを修正

2019/04/15  入試制度をログイン校種で制御する。
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2

2020/01/22  1.削除処理を修正

2020/01/23  1.受験番号附番変更
            -- 推薦 　・難関コース＆特進コース：Ｓ１００１～ 　・選抜進学コース：Ｓ２００１～ 　・総合進学コース：Ｓ３００１～ 　※Ｓ４００１～は使用しません 
            -- 一般 　・難関コース＆特進コース：１０００１～ 　・選抜進学コース：２０００１～ 　・総合進学コース：３０００１～ 　※４０００１～は使用しません 
