// kanji=漢字
// $Id: readme.txt 75195 2020-07-01 00:52:12Z ishii $

2020/01/08  1.作成中

2020/01/10  1.新規作成

2020/01/11  1.氏名、カナの不具合修正
            2.学校コードの桁数変更。

2020/01/15  1.卒業区分の修正

2020/01/21  1.通学情報が取り込めるよう修正

2020/01/24  1.DELETE-INSERT時に追加するSEQのレコードのみを削除するように修正

2020/02/03  1.志願者基礎、受付データはDelete → Insertせず、Update OR Insertにする。

2020/02/12  1.郵便番号の４桁目に「-」を入れて登録

2020/06/02  1.CSV取り込みの際に、ENTEXAM_APPLICANTCONFRPT_DAT.TOTALSTUDYTIMEに1が登録されるよう変更

2020/06/09  1.ENTEXAM_APPLICANTBASE_DAT.SHDIVは、固定1を登録するよう変更

2020/07/01  1.取込時、志願者毎の試験会場をENTEXAM_RECEPT_DETAILのSEQ014に保持するよう修正
            2.試験会場から試験コードを取得する処理を修正

2020/11/20  1.リファクタリング
            2.学校コードを取り込む際の8桁変換作業をカット。CSVデータ7桁をそのまま取り込むように変更

2020/12/25  1.ENTEXAM_APPLICANTCONFRPT_DAT.TOTALSTUDYTIMEに固定で1が登録されていたところをCSVの44列目(特別活動に対する優遇)を参照し、値をセットするよう修正
              (「01」の場合→1、「01」以外の場合→NULL。ENTEXAM_APPLICANTBASE_DETAIL_DATのSEQ='005'のREMARK1へセットする値と同様)

2021/01/25  1.ENTEXAM_RECEPT_DETAIL_DATはDelete → Insertせず、Update OR Insertにする。
