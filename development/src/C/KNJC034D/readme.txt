# kanji=漢字

2015/10/01  1.新規作成

2015/10/05  1.背景色を追加
            -- ログイン日付：緑
            -- 5行ごとに切替：白とグレー
            2.行事予定データの休日参照を追加
            -- 出欠コンボを使用不可
            -- 出欠状況チェックボックスを非表示
            -- 一括変更へのリンクなし
            -- 全て休日のとき、更新ボタン使用不可
            3.出欠コンボの表示を太字に変更
            4.一括変更画面の背景色を追加
            -- ログイン日付：緑
            -- 5行ごとに切替：白とグレー
            -- 異動者：黄色

2015/10/07  1.出欠コード取得SQLを修正
            2.出欠状況チェックボックスのidを変更

2015/10/13  1.出欠コンボの「遅刻+早退」を名称マスタ「C008」参照に変更

2015/10/16  1.特別支援学校の処理追加
            -- プロパティー「useSpecial_Support_Hrclass」参照
            -- 休日処理は非対応

2015/11/05  1.出欠の備考を追加

2015/12/09  1.統計学級ラジオボタン追加に伴う修正

2015/12/11  1.統計学級コンボに校種を追加

2016/01/29  1.学年学期対応

2016/02/04  1.統計学級のとき、年組番表示に変更

2016/02/17  1.setAccessLogDetailコメントアウト

2016/04/13  1.プロパティー「attend_Shosai」が1のときの処理を追加
            -- 出欠コンボに出欠詳細を追加
            -- 出欠コンボはNAME2を表示に変更
            -- 更新時に出欠集計処理を追加
            2.一括変更画面の生徒行をクリックしてチェックボックスをON/OFFする処理を追加

2016/05/17  1.プロパティー「useAppointedDayGradeMst」参照追加
            2.出欠コンボの表示をプロパティー「attend_Shosai_kigou」で切替

2016/05/18  1.締め日取得（学年別）の参照テーブルを"V_APPOINTED_DAY_GRADE_MST"に変更

2016/07/01  1.処理速度改善

2016/07/13  1.プロパティー「attend_Shosai」が1のときに勤怠が表示されるように修正

2016/07/26  1.統計学級ラジオボタンをカット
            2.学年混合チェックボックス追加

2016/11/04  1.訪問生かつEVENT_SCHREG_DAT追加に伴い修正
            -- 一括変更画面へのリンク表示
            -- 出欠状況チェックボックス表示（法定クラス）
            -- 出欠コンボの使用不可にEVENT_SCHREG_DAT参照
            -- 訪問生は学年混合、実クラスでもEVENT_SCHREG_DATの休日は出欠コンボを使用不可とする
            -- 一括変更画面で休日の生徒は対象者チェックボックスを使用不可に変更
            -- 一括変更画面で対象者チェックボックスが全チェックoffのとき、変更ボタン使用不可
            -- プロパティー「attend_Shosai」が1のとき、出欠集計処理の授業日数取得で参照

2016/11/15  1.学年混合に休日表示追加（EVENT_MSTのDATA_DIV='1'参照）

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)
            
2017/03/28  1.表示速度改善のため、コンボボックスをつくるたびにDBを読み込まず、一度だけ読んで配列に入れて使いまわすように変更

2017/05/19  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            2.名称マスタ変更。C006、C007、C008、C041先頭から２桁目を校種にする。

2017/06/09  1.更新中は使用不可
            -- 校種・対象月・学級コンボ、クラス方式選択ラジオボタン、学年混合チェックボックス、更新・取消・終了ボタン

2017/09/22  1.前回の修正漏れ

2017/12/15  1.異動処理を変更
            -- SCHREG_ENT_GRD_HIST_COMEBACK_DAT参照追加
            -- 異動中（在籍外）は出欠コンボ使用不可

2018/05/14  1.年組選択での更新時にも出欠済みフラグが更新されるように修正

2018/05/18  1.校種コンボが無い際のjavascriptエラー修正

2018/05/21  1.プロパティー「useAttendRemarkLastMonth」が1のとき、出欠の備考の参照月を学期の最終月にする

2018/07/23  1.プロパティー「useFi_Hrclass」と「useSpecial_Support_Hrclass」が共に"1"のときの不具合修正
            2.前回の修正を修正
            -- プロパティー「useFi_Hrclass」と「useSpecial_Support_Hrclass」の優先度

2018/08/03  1.プロパティー「useSpecial_Support_Hrclass」が"1"かつ実クラスのときの休日処理を追加
            -- 校種がないので公休日マスタメンテ参照

2018/11/28  1.異動日チェックを変更
            -- SCHREG_ENT_GRD_HIST_COMEBACK_DATのENT_DATEとGRD_DATEの範囲内は除く
            2.異動者の出欠集計処理を変更
            -- 全日異動者は集計処理をしない（集計データは削除する）
            -- 一部異動者の授業日数は入力可能な日付のカウントをセット
            3.一括変更画面の異動処理の修正漏れ

2019/04/21  1.updateFrameLockNotMessage → updateFrameLocksを使用するよう修正

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2020/02/20 01.PC-Talkerの機能

2020/06/24  1.訪問生の場合、氏名欄へ「訪問生」を表示
            2.訪問生の場合、「出席」をコンボボックスへ表示
            3.訪問生がいる場合、「終了」ボタンの右に訪問生ありの文言を表示

2020/07/20  1.誤字修正。訪問性 → 訪問生

2021/01/25  1.コード自動整形

2021/01/26  1.欠課遅刻(51)、欠課早退(52)、欠課(53)の追加に伴う修正（集計処理追加）
            -- 集計値：ATTEND_SEMES_DETAIL_DAT(SEQ=051,052,053).CNT

2021/03/11  1.プロパティー「setDefaultMonthCtrlDate = 1」の時、CTRL_DATEの月・学期をデフォルトで使用する。
