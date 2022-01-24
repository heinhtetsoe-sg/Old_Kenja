# kanji=漢字
# $Id: readme.txt 76722 2020-09-10 08:09:24Z arakaki $

2018/03/09  1.KNJL140Bを元に新規作成。三重県専用
            2.権限チェック追加（入試管理者）
            --STAFF_DETAIL_MST(STAFF_SEQ=009)のFIELD1が1以外の時、画面を閉じる
            3.入学コースコンボのSQL修正（不要な条件をカット）

2019/02/12  1.入学者出力の際、学年を追加

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
