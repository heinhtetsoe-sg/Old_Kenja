// kanji=漢字
// $Id: readme.txt 56580 2017-10-22 12:35:29Z maeshiro $
***KNJL115K通知発行No.採番処理

2005/10/27 新規作成
        ・合否判定処理より分岐、機能拡張
2005/11/08 
        1:附属推薦,2:中高一貫は、合格通知採番処理対象外
2006/01/05 仮納品
2006/01/06 
    ・合格者一般ラジオボタン追加
    ・合格者追加繰上ラジオボタン追加
    ・合格者追加繰上回数コンボ表示位置変更
    ・最終番号表示　削除(結果一覧リストで表示するので不要)
    ・結果一覧リスト追加
    ・番号クリア処理追加
    ・追加繰越者採番、旧番号項目対応
2006/01/10 
    ・一覧表示内容、採番者のみ表示より、未採番データも表示の仕様へもどす。
    ・採番･クリア処理前、"MSG101 処理を開始します。よろしですか？ YES OR NO"メッセージ追加。
2006/01/11 knjl115Form1.jsファイルのBOM削除
2006/01/11 一覧表示データ取得時の追加繰上合格者抽出条件修正
2006/01/12
    ・旧番号を重複チェック対象とする。（合格通知番号、旧番号、不合格番号で存在すればエラーとする。）
    ・一覧表示内容変更、繰上者で繰上採番処理未処理の番号は、未採番Nullとして扱い表示する。
    ・採番処理前に対象者が採番済の場合、エラーとする。
    ・登録者コード、処理日付更新追加。
2006/01/13 
    ・5:追加合格者は、旧番号との関連なしへ変更
2006/01/13
    ・採番処理時の番号クリア処理、不要のため処理削除
    ・追加繰上 旧番号保管処理6:繰上合格者のみ対象とする。
2006/01/13
    対象採番済チェックの追加繰上チェック時のJUDGEMENT_GROUP_NOの条件もれ対応。
2006/01/20
    対象データ採番済チェックの追加繰上チェック時のTESTDIV条件もれ対応。
2006/02/09 合格者、一般　採番処理時にスポーツ推薦者(APPLICANTDIV='3' AND  JUDGEMENT='4')のデータも採番対象とする。	

2009/02/11  1.採番実行時、受験番号1240から割振られる現象発生
              番号更新時のSQL文に ORDER BY を追加。row_number() over(ORDER BY EXAMNO)。

2010/01/28  1.採番処理の合格者に中高一貫を指定出来るようにした。

2010/02/01  1.下の一覧に「合格者　中高一貫」を表示するようにした。
            2.インフルエンザ対応

2010/02/02  1.インフルエンザ対応を元に戻した。
            -- 再修正するため一旦修正前の状態に戻す。
            2.リファクタリング
            3.中高一貫について、番号クリア処理できるようにした。
            -- 前回の修正漏れ。
            4.インフルエンザ対応

2010/02/03  1.インフルエンザ対応
            -- 追加繰上合格者存在チェック

2012/03/20  1.JUDGEMENTのコード変更に伴う修正
            -- 9：保留 → 0：保留
            -- 9：保留 → 9：第４志望合格(スライド)
