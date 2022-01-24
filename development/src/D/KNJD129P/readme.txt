// kanji=漢字
// $Id: readme.txt 76387 2020-09-03 08:15:52Z arakaki $

-----
学校：九段
-----

2017/10/30  1.KBJD129Vを元に新規作成
            -- 先科目選択時、元科目の出欠情報も表示
            -- 処理科目ラジオは常に表示（プロパティは参照しない）
            -- 縦横スクロール表示

2020/09/03  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
