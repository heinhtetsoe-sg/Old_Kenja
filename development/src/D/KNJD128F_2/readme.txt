// kanji=漢字
// $Id: readme.txt 76780 2020-09-11 05:33:01Z arakaki $

2015/06/29  1.KNJD128_2(1.13)を元に新規作成。
            -- 京都中学パーツタイプ

2016/03/30  1.成績が別講座の生徒に上書される不具合修正
            -- 生徒を表示する時に、学籍番号を保持しておくのではなく、
            -- 更新時に、学籍番号を取得し更新するように修正。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/21  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2020/09/11  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
