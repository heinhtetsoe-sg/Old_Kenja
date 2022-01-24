# kanji=漢字
# $Id: readme.txt 62818 2018-10-15 02:43:18Z maeshiro $

-----
学校：京都府／洛北・園部高等学校付属中学校
-----
◆仕様 2014/07/03更新
・成績入力（考査種別選択）
・成績入力コントロール
  TESTITEM_MST_COUNTFLG_NEWを参照し、ADMIN_CONTROL_SDIV_DATに登録
・出欠入力コントロール
  名称マスタ「Z005」を参照し、ADMIN_CONTROL_DAT(CONTROL_FLG='2')に登録
・観点入力コントロール
  名称マスタ「Z009」を参照し、ADMIN_CONTROL_DAT(CONTROL_FLG='3')に登録
・実力テスト入力コントロール
  PROFICIENCY_MSTを参照し、ADMIN_CONTROL_PROFICIENCY_DATに登録
-----

2014/07/03  1.KNJZ350V、KNJZ350Jを元に作成

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.Z009の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2018/10/15  1.プロパティーuse_prg_schoolkindが1の場合、校種のコンボボックスを表示する
