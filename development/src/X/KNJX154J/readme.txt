# kanji=漢字
# $Id: readme.txt 76637 2020-09-09 07:31:05Z arakaki $

2013/07/22  1.KNJX154EをPROGRAMIDを変更して新規作成（宮城全日制用 ）

2014/02/03  1.出力情報を修正
               - SPECIALACTREMARK ⇒ COMMUNICATION
            2.表画面からCSV情報をパラメータを渡し、取得するよう修正
            
2014/05/29  1.更新/削除等のログ取得機能を追加

2016/09/20  1.年度学期コンボ、年組コンボ、データ出力、データ取込時の在籍チェック変更
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2018/06/01  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/09  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/10  1.CSVメッセージ統一(SG社)

2021/04/26  1.CSVメッセージ統一STEP2(SG社)