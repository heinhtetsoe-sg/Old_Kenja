// kanji=漢字

2016/10/18  1.新規作成

2016/10/20  1.中学の受験型3,4,5のTOTAL2は、NULLではなく、TOTAL4と同じ合計をセットする。

2016/11/14  1.以下の処理を、受付確定処理から成績確定処理へ移行。
            -- 中学の第５回の時、特別入試対象者は、合格とする。

2016/11/25  1.「特別措置者のみ」チェックボックス追加

2017/02/02  1.中学の受験型3のTOTAL2にセットする合計を変更
            -- 変更前：4科の合計（1:国、2:算、3:理、4:社）
            -- 変更後：2科の合計（1:国、2:算）

2017/03/06  1.入試回数に第３回・第４回を追加
            -- 第３回・第４回の試験日は名称マスタ「L044」を参照

2017/12/21  1.中学の入試区分「コース別思考力」追加に伴う修正
            -- 受験型「6:グローバル」科目「7:思考力総合問題」
            -- 受験型「7:サイエンス」科目「8:レポート作成」
            -- 受験型「8:スポーツ」科目「8:レポート作成」

2017/12/22  1.前回の修正。科目コード変更
            -- 受験型「6:グローバル」科目「G:思考力総合問題」
            -- 受験型「7:サイエンス」科目「H:レポート作成」
            -- 受験型「8:スポーツ」科目「H:レポート作成」

2018/01/25  1.選択問題の最高1,最高2の更新処理を追加
            --ENTEXAM_RECEPT_DETAIL_DAT(SEQ=004)のREMARK3、REMARK4
            2.英検取得者への対応（欠席者以外）
            --（選択問題25点分換算）5級は、　　最高1:25点、最高2:最もよい点　をセット
            --（選択問題50点分換算）4級は、　　最高1:25点、最高2:25点　　　　をセット
            --（選択問題50点分換算）3級以上は、最高1:25点、最高2:25点　　　　をセット

2018/01/31  1.中学の科目2:算数は、7:算数1と8:算数2を合計して算出し、その後にENTEXAM_RECEPT_DATを作成する

2018/02/01  1.英検取得級「4:チャレンジイングリュシュ」追加（5級と同じ扱い）
            --（選択問題25点分換算）最高1:25点、最高2:最もよい点　をセット

2018/02/05  1.中学第5回入試について、理科は理科1と2、社会は社会1と2の合計を計算し、その後にENTEXAM_RECEPT_DATを作成する
            -- 「3:理科」は「A:理科1」と「B:理科2」の合計
            -- 「4:社会」は「C:社会1」と「D:社会2」の合計

2018/03/08  1.入試回数に2次募集第２回を追加
            -- 2次募集第２回の試験日は名称マスタ「L059」を参照

2018/11/06  1.ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQが"010"～"013"のとき、参照テーブルを変更
            -- ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT
            2.入試区分を数値型でソートに変更

2018/11/22  1.参照テーブルを変更
            --ENTEXAM_RECEPT_DAT → V_ENTEXAM_RECEPT_DAT

2019/11/21  1.中学の得意型文京方式、得意型２科入試の場合、国語または算数の高い得点を2倍にし合計を算出する

2020/01/30  1.チャレンジイングリッシュのコードを4から6に変更

2020/02/01  1.チャレンジイングリッシュは選択問題25点分換算からカット
            2.チャレンジイングリッシュの更新処理修正
            3.チャレンジイングリッシュは選択の試験を英検からセットしない

2021/01/04  1.コード自動整形
            2.中学の受験型「9:インタラクティブ」の場合、科目「I:英活」で合計を算出する
            3.中学の受験型「A:適性検査」の場合、科目「J:適性検査Ⅰ」「K:適性検査Ⅱ」で合計を算出する
            4.中学の以下の処理は、教科型入試「TESTDIV=1,2,3,5,16,17」の時のみとする。
            -- 科目「2:算数」のレコードを「7:算数1、8:算数2」の合計で作成

2021/01/28  1.高校の入試区分「7:一般特別」の受験科目「9:プレゼンテーション」追加に伴う修正
