// kanji=漢字
// $Id: readme.txt 69775 2019-09-17 12:28:35Z ishii $

2019/01/15  1.新規作成（参考：KNJL080U）

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/01/22  1.手続者・合格者取得SQLで、母集団をENTEXAM_RECEPT_DATに変更
            2.手続者・合格者取得SQLで、手続完了者の判定についてENTEXAM_RECEPT_DAT.PROCEDUREDIV1を参照するよう変更
            3.更新SQLで、ENTEXAM_RECEPT_DATの更新処理を追加
            4.更新SQLで、ENTEXAM_APPLICANTBASE_DAT.PROCEDUREDATEを手続者の最終手続き日付で更新するよう、処理を追加
            5.手続者・合格者取得SQLで、合否区分 5:受験不可の抽出条件は不要のため、抽出条件を削除
            6.合格者、手続者の番号として、EXAMNO、RECEPTNOの両方を画面に表示するよう、変更
            7.手続者・合格者取得SQLで、RECEPTNOでソートするよう、変更
            8.CSVファイル取込/書出処理を削除
            9.手続者・合格者の横に表示する日付について、ENTEXAM_RECEPT_DAT.PROCEDUREDATE1を表示するよう、変更

2019/02/04  1.更新SQLのENTEXAM_RECEPT_DAT更新時に手続年月日が登録済の場合、手続年月日を更新しないよう、修正

2019/06/06  1.右リストの更新SQL(クリア処理)で、他試験区分で手続済がある人は、下記テーブルのクリア処理をしない
            --手続区分、手続日付、入学区分(ENTEXAM_APPLICANTBASE_DAT.PROCEDUREDIV,PROCEDUREDATE,ENTDIV)
            --入学コース(ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQ=007)

2019/09/17  1.入試制度をログイン校種で制御する。
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2
