// kanji=漢字
// $Id: readme.txt 71254 2019-12-16 05:33:56Z yogi $

2010/11/12  1.KNJL050Yを元に新規作成

2010/11/20  1.以下の不具合を修正
            -- 画面が崩れている。
            -- １行目のテキストが入力不可になる。
            -- 複数回受験者の合格種別が更新されない。
            -- スポーツ希望者以外の値チェックを追加。

2010/11/22  1.合格者のみ表示するように修正
            2.高校推薦の時、成績は、評定平均を表示する。
            3.高校推薦以外の時、成績は、TOTAL3を表示する。

2010/11/29  1.成績欄・順位欄を修正。

2010/12/28  1.対象者ラジオボタンを追加した。
            2.以下の通り修正。
            -- 全科目受験フラグ(ATTEND_ALL_FLG)が '1' 以外は、
            -- 下の行に表示し、成績、順位は空白で表示する。

2010/12/29  1.高校・推薦入試は、成績(内申)は表示する。順位は空白とする。

2011/01/12  1.成績順は、下記のデータでソートする。成績欄にも表示する。
            -- 学特・一般は、TOTAL1
            -- 推薦は、内申点

2011/01/20  1.傾斜配点出力ラジオボタンを追加
            -- 傾斜配点出力「する」の場合
            --      「TOTAL3(学特) または TOTAL4(一般)」を成績欄に表示
            -- 傾斜配点出力「しない」の場合
            --      「TOTAL1(学特) または TOTAL2(一般)」を成績欄に表示
            2.志望区分コンボボックスを追加
            -- 最終行に「9:全て」を追加

2013/11/06  1.志望区分コンボにORDER BYを追加

2015/11/09  1.対象者に（◎帰国生除く　○帰国生のみ）ラジオボタン追加
            -- 中学または内部生のみ選択時、グレーアウト

2019/12/16  1.T特奨希望者の列を追加
