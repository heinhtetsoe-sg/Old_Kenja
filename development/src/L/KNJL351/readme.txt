# kanji=漢字
# $Id: readme.txt 65098 2019-01-21 01:18:30Z matsushima $

2006/01/06 o-naka NO001 備考欄に「辞退」「特待」「繰上」「特別」を出力する。「特待」は、試験区分の降順にデータをみる
2006/01/06 o-naka NO002 ＳＱＬにて、「GOUHI1」「GOUHI2」「GOUHI3」「GOUHI」を帳票と同じように修正
2006/01/15 o-naka NO003 帳票と同様に、各回毎の欠席者は▲表示にする。


2007/06/12  CVS登録。
            CSV処理を修正した。

2008/12/19  CSV処理に4回目、5回目を追加した。

2009/02/03  1.不具合修正。欠席マーク：▲　が第4回、第5回試験で表記されている。

2009/02/05  1.各回の合否欄に現在受験中の生徒にマークをつける。（”＊”：アスタリスク）
            2.受験欄の不具合修正。第4回、第5回試験を受験した生徒に欠席マーク：▲が表記されている。

2012/11/22  1.成績(２科平均順)ラジオボタンの表示をカット

2012/12/10  1.２科計表記削除
            2.入試区分０の追加に伴う修正
            -- ６回目表記追加
            3.前回(1.9)の修正漏れ
            4.合否結果マークの修正
            -- [修正前]
            --     ◎・・・合格
            -- [修正後]
            --     ◎・・・英数特科クラス合格
            --     ○・・・特別進学クラス合格

2013/01/12  1.合否マークの修正
            -- ①　”↑”　＝＝＞　”◎”　（英数特科クラス合格を表します）
            -- ②　”↓”　＝＝＞　”○”　（特別進学クラス合格を表します）
            -- ③　特待合格の場合は　”☆”　で表記する。

2013/01/21  1.以下の通り修正
            -- ①”延期”→”特別アップ”にタイトル変更
            -- ②特別アップ合格者は”○”をつける。

2013/05/22  1.入試区分の表示順を修正
            -- 左詰めに表示

2013/11/25  1.特待生選抜入試の対応
            -- 合否マーク
            -- ☆特待合格・・・正規合格者
            -- ○特別進学クラス合格・・・非正規合格者

2014/11/26  1.加点追加に伴い修正
            ・タイトル変更　’４科計’→’合計’
            ・合計欄には加点を含める
            ・受験欄右に加点欄追加（５，１０を表記）
            -- rep-entexam_recept_dat.sql (1.2)

2015/10/26  1.７回目を追加

2016/02/04  1.ＣＳＶ出力で氏名の後ろに「氏名かな」の項目を追加

2016/03/03  1.ＣＳＶ出力項目「地区コード」追加。※出身校の左側

2019/01/18  1.CSV出力の文字化け修正(Edge対応)