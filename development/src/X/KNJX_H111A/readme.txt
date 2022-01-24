# kanji=漢字
# $Id: 9b580bea0e718040c56e350a6ac00a29e6a7c4ba $

2016/06/15  1.新規作成

2017/05/18  1.連番(SEQ)を必須項目に変更

2018/10/02  1.資格証書の列を追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/04/02  1.ログ取得機能追加

2019/08/20  1.対象データをKNJH111Aに合わせた

2019/12/11  1.CSVデータ出力時に「設定区分」が出力されない不具合を修正
            2.「設定区分」を必須項目から除外
             - 「設定区分」の必須チェックを削除
             - 資格マスタの存在チェックSQLの抽出条件から設定区分を削除
             - CSVデータ取込時の「設定区分」の登録値を修正

2019/12/20  1.Properties["useQualifiedManagementFlg"]=1のときはSCHREG_QUALIFIED_TEST_DATを使用

2020/01/23  1.項目「資格点」を追加
            -- QUALIFIED_RANK_DAT、またはQUALIFIED_RESULT_MSTに「NOT_PRINT」が存在すれば「資格点」を表示
            2.項目「スコア」を追加
            3.不要な処理を削除

2020/01/24  1.QUALIFIED_MST.SCORE_FLGの参照を削除
            2.「スコア」を「得点」に修正

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/10/02  1.得点、資格点をプロパティー「useQualifiedScoreLen」参照に変更
            -- 文字数チェックの桁数
            -- ヘッダ例

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/12  1.CSVメッセージ統一(SG社)

2021/04/26  1.CSVメッセージ統一STEP2(SG社)