# kanji=漢字
# $Id: readme.txt 69350 2019-08-23 08:44:42Z ishii $

2011/10/26  1.KNJE011Aを元に新規作成
            -- rep-hexam_entremark_dat_rev1.9.sql
            -- rep-grd_hexam_entremark_dat_rev1.7.sql

2011/10/27  1.「参考となる諸事項等の記録」を修正。
            -- 修正前：全角31文字X6行
            -- 修正後：全角31文字X12行
            -- rep-hexam_entremark_hdat_rev1.3.sql
            -- rep-grd_hexam_entremark_hdat_rev1.2.sql

2011/12/06  1.通知表所見参照画面（KNJA121A参照）を追加した。

2011/12/08  1.観点、評価を"HREPORTREMARK_DETAIL_DAT"から出力するように変更した。

2013/06/21  1.出欠備考参照ボタンの参照先を切替処理追加
                - Properties["useAttendSemesRemarkDat"]の値により切替を行う
                
2013/06/25  1.出欠備考参照ボタンの表示名を修正
                - Properties["useAttendSemesRemarkDat"] = 1の時、まとめ出欠備考参照
                - それ以外、日々出欠備考参照

2014/03/06  1.更新時のロック機能(レイヤ)を追加

2014/03/11  1.更新時のロック機能(レイヤ)修正漏れ対応

2014/03/28  1.更新時のロック機能(レイヤ)修正

2014/04/25  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効

2014/12/01  1.参考となる諸事項等の記録のサイズ、レイアウトをログイン年度で切替する

2015/08/28  1.所見のテーブルを中学用に変更（HEXAM_ENTREMARK_J_HDAT、GRD_HEXAM_ENTREMARK_J_HDAT）

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)
