# kanji=漢字
# $Id: readme.txt 68507 2019-07-05 02:27:01Z maeshiro $
KNJE065
概評人数登録処理
----
学校：賢者版・・・近大以外

2007/08/24 nakamoto
★ 異動基準日を追加した。
-- 再計算で使用する条件である。
-- 退学・転学した生徒は人数には含まないので、
-- 異動基準日を指定して判断する。
-- ※退学・転学者は、異動基準日までは在学者である。
★ 選択科目を除くチェックボックスを追加した。
-- 再計算で使用する条件である。
-- schreg_studyrec_datから選択科目を除く

2007/09/04 nakamoto
★ 2007/08/24 に修正した内容を元に戻した。

2008/06/25 nakamoto
★ 事前処理チェックの処理を移動した。
-- 修正前：画面を最初に開いた時にチェック
-- 修正後：「再計算」「更新」ボタンを押した時にチェック

2009/12/29  1.集計方法の修正STUDYRECの対象データは、各学年のMAX年度とする。

2010/07/08  1.プロパティーファイルで学科別、コース別を選択できるよう修正
            2.SQL修正
            3.SQL修正

2010/07/10 1.プロパティーの「gaihyouGakkaBetu」の意味を逆に変更、'1'の時コース別とする

2010/07/20  1.「評定1の場合は2で処理する」チェックボックスを追加
            -- prgInfo.propertiesで、チェックボックスの画面表示をコントロールする。
            --      # 評定読替するかしないかのフラグ 1:表示 1以外:非表示
            --      hyoteiYomikae = 1
            -- 再計算では、チェックがはいったら、評定1を2として、集計する。

2010/07/22 1.教科コード01～89を対象とする(法政、自修館の処理は未となってます)

2010/07/23 1.教科コード01～89を対象とする
           -- 法政：教科コード01～89 OR 科目コード = '941001'
           -- 自修館：教科コード01～85

2010/08/25  1.「再計算」処理の修正。
            -- 「不具合内容」
            -- 　　「調査書印刷」の「全体評定平均値」で数えた場合と、概評人数が合わない不具合の修正
            -- 「修正内容」
            -- 　　「調査書印刷」と同様に「科目マスタで、同一グループ科目設定されている場合」の対応

2011/07/27  1.「再計算」処理の修正。（評定平均の母集団となる成績データの条件を修正）
              修正前：在籍データを学年でグループ化したMAX年度の成績データを母集団とする。
              修正後：成績データを年次でグループ化したMAX年度の成績データを母集団とする。

2011/08/31  1.中高一貫の場合、DBエラーになる不具合を修正（前回の修正不足への対応。）

2012/07/13  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/12/20  1.集計対象からGRD_DIV='6'の除籍者を除くよう修正

2014/03/10  1.更新時のロック機能(レイヤ)を追加

2014/03/28  1.更新時のロック機能(レイヤ)修正

2014/04/25  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効

2016/07/27  1.7:転籍は除く

2019/07/05  1.再計算、更新ボタン押下時の「ログイン年度のSCHREG_STUDYREC_DATがなければエラー」の処理をカット
