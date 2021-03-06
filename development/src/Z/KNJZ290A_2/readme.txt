# kanji=漢字
# $Id: readme.txt 76440 2020-09-04 08:20:32Z arakaki $

2014/06/11  1.新規作成(KNJZ291を元に)

2015/01/29  1.学年主任、教務主任を追加
            2.レイアウト修正
                - 職名、所属、校務分掌部、授業受け持ち、学年主任、教科主任の帯の色を変更
            3.職名、所属、校務分掌部、授業受け持ち、学年主任、教科主任は年度がすべての時はSTAFF_DETAIL_MSTのログイン年度から表示
            
2015/02/01  1.CSV取り込み出力機能追加

2015/02/04  1.文言修正

2015/02/09  1.肩書き1,2を追加し、学年主任の時は学年コンボ、教科主任の時は教科コンボを表示するよう修正
            2.CSV取り込み出力機能修整

2015/02/12  1.画面初期状態で年度がNULLのときは、ログイン年度をセットするよう修正

2015/06/08  1.肩書き3を追加し、教科主任を2つまで登録できるよう修正

2015/06/18  1.名称マスタZ010が'sapporo'のとき、肩書き1～3の’1050：教務主任’の教科コンボはIB教科を参照するよう修正

2015/07/14  1.画面を開いた時に年度がNULLの場合はログイン年度をセットするよう修正

2015/03/25  1.教育委員会以外でも使用できるよう修正。

2016/03/26  1.職名等前年度コピー機能を追加
                - 対象年度の職員ごとに未設定箇所のみをコピーする

2016/05/02  1.学校所属登録ボタン → 赴任履歴修正ボタンに変更

2017/04/03  1.項目：担当保健室を追加
            --Properties["useNurseoffRestrict"]=1の時、担当保健室を表示

2017/04/04  1.プロパティーuseSchool_KindField='1'の場合、保健室(名称マスタ「Z043」)は校種の条件を追加

2018/04/17  1.削除のとき、対象テーブルにUSER_MST、USER_PWD_HIST_MST追加

2018/10/03  1.use_staff_detail_ext_mst=1の時、教科主任は複数教科選択可能
            -- use_staff_detail_ext_mst
            2.修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/03/14  1.E-Mailの最大バイト数を25バイトから50バイトに変更

2019/07/01  1.顔写真取込処理を追加

2019/07/17  1.左側の画面で、年度に「－全て－」を選択した場合、「職名等前年度からコピー」ボタンを選択できないよう、変更
            2.「職名等前年度からコピー」ボタンの処理で、前年度/当年度共に登録されているデータのみ対象とするよう、変更

2019/07/26  1.校務分掌部にコンボボックスを追加
            2.追加した校務分掌部をCSV出力/取込で扱うよう、変更

2019/08/16  1.担当校種チェックボックスを追加
            2.更新エラー時にチェックボックスの値を保持するように修正

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2020/12/10  1.LASTCOLUMN対策漏れの修正

2021/03/03  1.CSVメッセージ統一(SG社)

2021/03/05  1.リファクタリング
            2.青山学院の場合、「年齢年月日」「学院就任年月日」「高等部就任年月日」「学院勤続」「高等部勤続」の項目を追加する
            -- 青山学院の判定:名称マスタ「Z010」=「aoyama」
            -- 学院就任年月日:STAFF_DETAIL_MST.FIELD1 (STAFF_SEQ=011)、高等部就任年月日:STAFF_DETAIL_MST.FIELD2 (STAFF_SEQ=011)
            3.不要なスペースの記述を削除
