 kanji=漢字

2009/10/20  1.新規作成

2009/10/28  1.追加後前後移動ボタンを追加した。
            2.学校情報、会社情報が表示されない不具合を修正した。
            3.所在地（都道府県）が表示されない不具合を修正した。
            4.追加、更新時の必須項目チェックを追加した。
            5.メイン画面で進路相談のポップアップを閉じる際、メイン画面を読むようにした。
            6.受験報告（進学）の一括更新画面を右フレーム内に表示するように変更した。
              リストtoリストの学籍番号表示をカットした。

2009/12/07  1.テーブル名を変更した。
            　FACULTY_MST　→　COLLEGE_FACULTY_MST
            　DEPARTMENT_MST　→　COLLEGE_DEPARTMENT_MST
            2.大学の住所、電話番号をCOLLEGE_CAMPUS_ADDR_DATから取得に変更した。

2009/12/10  1.学校コード、会社コードを表示するようにした。
            2.学校系統コードと名称取得ＳＱＬを修正した。
            3.更新失敗後に会社情報が消える不具合を修正した。
            4.登録日の年度内チェックの不具合を修正した。
            5.COLLEGE_FACULTY_MSTがない場合、またはCAMPUS_ADDR_CDがNULLの場合、
            　COLLEGE_MSTのCAMPUS_ADDR_CDを参照するように修正した。

2009/12/16  1.「受験報告(就職)」に職業コードのコンボを追加

2009/12/24  1.進路相談の画面サイズ変更
            2.下記の修正をした。
            -- レイアウト変更
            -- 進路相談の画面サイズ変更
            3.レイアウト変更

2010/01/28  1.登録日のデフォルトをログイン日付に変更した。
            2.進路調査に一括更新（進学）ボタンを追加した。
            　進学の一括更新画面を追加した。
            3.受験報告（就職）の受験結果、進路状況を移動した。
            　応募方法をカットした。

2010/04/22  1.一括更新（進学）の進路種別を固定で「1：進路」にした。

2010/09/27  1.生徒選択画面を卒業生検索ボタンのあるパターンに変更。
            2.受験報告（進学）を修正した。
            --所在地の必須入力チェックを削除した。
            --試験時間をカットした。
            --発表日を追加した。
            --卒業生の場合、追加、更新、取り消し、戻るボタンのみ有効とした。
            3.受験報告（進学）一括更新画面を修正した。
            --所在地の必須入力チェックを削除した。
            --試験日、２次試験日、発表日を追加した。

2010/09/30  1.「その他」を追加した。

2011/02/25  1.受験報告（就職）の備考の参照フィールドを"JOBEX_THINK"→"JOB_THINK"に変更した。

2011/03/04  1.進路相談（就職）の第一希望・職業種別、第一希望・就業場所の必須入力チェックを削除した。

2011/03/11  1.受験報告（進路）の「感想」、受験報告（就職）の「備考」を「指導要録表記進路先」に変更した。

2011/03/14  1.受験報告（進路）、受験報告（就職）の「指導要録表記進路先」を「その他を選択した場合の指導要録表記進路先」に変更した。

2011/03/25  1.学校検索画面のレイアウト変更に伴い、画面サイズを広げた。

2012/03/08  1.受験報告(進学、就職)の「その他を選択した場合の指導要録表記進路先」のテキストエリアを検索ボタンの上に移動した

2012/03/19  1.文言変更
              - その他を選択した場合の指導要録表記進路先 ⇒ 指導要録に表記する進路先（直接入力）
              
2012/03/27  1.文言変更の修正漏れを対応（受験報告(進学)）
              - その他を選択した場合の指導要録表記進路先 ⇒ 指導要録に表記する進路先（直接入力）

2012/05/16  1.その他に「その他進路」を追加した。
            2.メイン画面で種別がその他のとき、２に「その他進路」を表示するように変更した。

2012/11/05  1.受験報告（就職）画面にて、PREF_CD（就業場所）の必須入力チェックをカット

2012/11/06  1.「その他進路」の項目の表示は、京都府のみ（名称マスタ「Z010」「00」kyoto）とするように修正

2013/03/29  1.卒業生の場合の修正をした。
            -- 生徒情報（年組番、氏名）をGRD_BASE_MST、GRD_REGD_HDATから出力した。
            -- 追加後前の生徒へ、追加後次の生徒へ、進路相談、一括更新ボタンを使用不可にした。
            
2013/04/24  1.求人検索ボタン追加
            2.PDF参照ボタン追加
            
2013/07/31  1.求人検索ボタン押下後に選択した会社情報のCOMPANY_MSTのデータを画面上にセットされるようHTMLのidを追加
            2.PDF参照ボタン押下後に入力した求人番号の会社情報データ取得し、COMPANY_MSTのデータをセットするよう修正

2013/08/06  1.コンボ選択時に求人番号の会社情報データがセットされたままになるよう修正

2014/04/21  1.IEバージョンによる、ポップアップの表示位置修正

2014/08/25  1.ログ取得機能追加
            2.style指定修正
            3.PDF参照ボタン押下後に求人番号入力有無チェックを追加

2014/09/16  1.メイン画面に「３」を追加
            2.進路相談の相談件名の文字数チェックを追加

2014/09/17  1.メイン画面で「３」を分割して「４～７」を追加

2014/09/19  1.メイン画面の進路情報一覧の枠を広げた

2015/03/02  1.受験報告（進学）を修正
            -- テーブル追加に伴う修正
            -- 募集区分、日程、方式、大分類、小分類コンボを追加
            -- 二次試験日、その他理由をカット
            -- 試験日時、発表日、試験内容、受験理由の項目名変更

2015/03/03  1.参照年度を次年度に変更
            -- COLLEGE_EXAM_CALENDAR
            -- COLLEGE_EXAM_PROGRAM_DAT
            -- COLLEGE_EXAM_LDAT
            -- COLLEGE_EXAM_SDAT

2015/03/04  1.参照年度を処理年度に変更
            -- COLLEGE_EXAM_CALENDAR
            -- COLLEGE_EXAM_PROGRAM_DAT
            -- COLLEGE_EXAM_LDAT
            -- COLLEGE_EXAM_SDAT
            2.募集区分を名称マスタより取得に変更
            3.日程、小分類の名称をCOLLEGE_EXAM_CALENDARから取得に変更

2015/03/05  1.募集区分以下(合格発表まで)の初期値設定処理を追加。

2015/03/09  1.受験報告（就職）に備考1、備考2を追加

2015/12/16  1.ボタン色の記述を変更

2016/01/06  1.進路調査画面を変更
            -- 履歴表示追加
            -- 進学は第六希望まで入力可(COURSE_HOPE_DETAIL_DAT)

2016/04/19  1.評定平均値は、四捨五入した値を表示するように修正。

2016/05/12  1.受験報告（進学）に受験番号を追加

2016/08/01  1.受験報告（進学）一括入力を追加

2016/08/02  1.受験報告（進学）一括入力画面を変更
            -- 登録日のエラーメッセージ
            -- 指導要録に表記する進路先（直接入力）をカット

2016/08/08  1.受験報告（進学）一括入力画面の進路先選択画面を修正
            -- 学校名テキストボックスでEnterを押下すると検索するよう修正
            -- 検索中はテキストボックス、ボタンを使用不可に変更
            -- 戻る際、チェックボックスを外すよう修正（ログのエラー対策）
            2.受験報告（進学）一括入力画面の更新後前後の生徒へボタンの不具合修正

2016/10/06  1.顔写真の表示/非表示機能(useDispUnDispPicture)

2016/10/11  1.固定文言「生徒」修正
            -- SETTING_DAT参照、なければ固定で「生徒」
            2.テキストエリアで折り返さない不具合修正

2016/11/01  1.受験報告（進学）、受験報告（進学）一括入力画面に調査書発行チェックボックス、証明書番号表示追加
            2.受験報告（進学）一括入力画面の受験方式、受験結果、進路状況の位置変更

2016/11/09  1.受験報告（進学）、受験報告（進学）一括入力画面の調査書発行チェックボックスをラジオボタンに変更

2016/11/18  1.受験報告（進学）を修正
            -- 学校、学部、学科テキストボックス、確定ボタン追加
            -- 一括更新に電話欄追加
            2.受験報告（進学）、受験報告（進学）一括入力画面の調査書発行ラジオボタン修正
            -- 更新前なら確認ダイアログで選択のon/offが可能

2016/11/21  1.受験報告（進学）、受験報告（進学）一括入力画面の証明書番号表示カット

2016/11/24  1.証明書番号表示は、CERTIF_SCHOOL_DATのCERTIF_NOの設定により切り替える
            2.CERTIF_SCHOOL_DATのCERTIF_NOが表示する設定の場合、文言「証明書番号： 」は表示する。

2016/11/24  1.証明書番号が表示されるように修正

2016/11/28  1.受験番号と備考１を進路状況の後ろに移動
            2.レイアウト一部修正(枠の余白)

2016/12/01  1.受験報告（進学）一括更新画面を修正
            -- 年組コンボ追加
            -- 生徒リストtoリストの表示内容変更

2016/12/07  1.以下の修正をした。(山口さんからの依頼)
            -- 受験結果、調査書発行の初期値をプロパティーで設定
            -- 調査書発行ONにする時のメッセージをカット
               メッセージのカットをしたので、ラジオボタン対応が出来なくなった
               調査書発行のラジオボタン(１つのラジオ)→チェックボックスに変更
            2.受験報告(進学)一括入力も2016/12/07-1の修正内容で修正した
            3.以下の修正をした。(山口さんからの依頼)
            -- 学校コードテキストでエンター → 学部コードにフォーカス移動
            -- 学部コードテキストでエンター → 学科コードにフォーカス移動
            -- 学科コードテキストでエンター → 確定ボタンにフォーカス移動
            4.受験報告（進学）一括更新画面の進路先選択の学校名検索を修正
            -- 検索先をCOLLEGE_MSTの"SCHOOL_NAME","SCHOOL_NAME_SHOW1","SCHOOL_NAME_SHOW2"に変更
            -- COLLEGE_MSTのSCHOOL_CDと一致するデータをCOLLEGE_EXAM_CALENDARから取得

2016/12/08  1.CERTIF_DETAIL_EACHTYPE_DATの帳票パラメータを追加

2016/12/09  1.受験報告（進学）一括更新画面を修正
            -- 縦横スクロール追加
            -- 学校名、学部名、学科名は略称表示に変更

2016/12/15  1.進路調査の就職で入力した項目が画面に反映されるように修正
            2.受験報告（進学）一括更新画面のレイアウト調整

2016/12/16  1.2016/12/01の修正漏れ
            -- 進路調査の一括更新（進学）にも同様の処理追加

2017/01/30  1.大学検索時に「大学名＋全角スペース」で実行すると全校表示されないように修正

2017/01/31  1.大学検索画面で選択更新時にCOLLEGE_EXAM_CALENDARの情報（入試日、合格発表など）を取得し、セットするよう修正

2017/02/07  1.2017/01/31の修正漏れ
            -- 大学検索時、選択済みの大学欄に黄色網掛けされるように修正
            2.前回の修正漏れ

2017/02/22  1.進路情報削除の際、CERTIF_DETAIL_EACHTYPE_DAT.REMARK13をクリア

2017/05/01  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/06/06  1.メイン画面修正
            -- 出力データ選択ラジオボタン追加
            -- 受験報告（進学）に日程、方式の表示追加
            2.受験報告（進学）一括入力画面修正
            -- 更新等ボタンの位置変更
            -- 日程、方式の位置変更
            3.メイン画面の出力データ選択ラジオボタンの初期値を「受験報告（進学)」に変更

2017/06/07  1.受験報告（進学）一括入力画面の枠の高さ調整

2017/06/20  1.追加ボタン押下後に画面クリアする処理を追加
            -- 受験報告（進学）、受験報告（就職）
            2.調査書発行チェックボックスをプロパティー「KNJE360_DEF_ISSUE」が1のとき表示に変更
            -- 受験報告（進学）、受験報告（進学）一括更新、受験報告（進学）一括入力
            3.受験報告（進学）一括入力画面の所在地をカット

2017/06/22  1.メイン画面で出力データ選択ラジオボタンによる項目の表示切替を追加
            2.進路先選択画面の一覧表の高さを変更

2017/07/03  1.メイン画面の評定平均の算出を変更
            -- VALUATIONがNULLのデータを除く
            -- CLASSCDは"90"未満
            -- 中高一貫の条件をカット
            -- 処理年度の校種（卒業生は卒業年度）と同じ校種のデータが対象

2017/08/16  1.確定ボタン追加、押下後に入力した求人番号の会社情報データを画面上にセット
            --受験報告(就職)

2017/10/10  1.大学検索で、学校コード検索を追加

2017/11/16  1.受験報告(進学)一括入力の進路先選択画面のボタン表示文言変更
            -- 「選択」→「登録」
            -- 「戻る」→「受験報告（進学）一括入力へ」

2017/12/14  1.部活動表示の修正
            --現在有効の部活（所属中の部活）を繋げて表示するよう修正
            2.前回の修正漏れ

2017/12/20  1.委員会の校種指定追加
            2.駿台甲府は委員会の対象をログイン年度のみとする

2018/02/14  1.受験方式の隣に専併区分コンボを追加

2018/10/12  1.プロパティ「useTokyotoShinroTyousasyo」が"1"の時、
            --学校種別、大分類、中分類コンボを追加

2018/10/16  1.以下の修正をした。
            -- プロパティー「useCollegeSearch10Keta」が"1"の時、10桁の学校検索機能を表示
               AFT_GRAD_COURSE_DETAIL_DAT.DETAIL_SEQ = '4'.REMARK1に保存
            -- IEでもEdgeでも使えるよう修正した。(ActiveXObject/XMLHttpRequestの切替)
            2.以下の修正をした。
            -- 固定文言追加
            -- 駿台コード検索で、学部/学科はオール0の場合ブランクとする。
            3.COLLEGE_FACULTY_SYSTEM_MSTの更新時、学部CDがNULLであれば'000'で更新する。
            4.バグ修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)
            2.合格短冊匿名希望チェックボックス追加

2019/01/21  1.CSV出力の文字化け対応の修正(setDownloadHeaderの不要な引数削除)

2019/07/18  1.学校検索ウィンドウの位置を修正(loadwindowのY軸を0に設定)

2019/08/08  1.プロパティ「KNJE360_SENKOUKOUMOKU」が"1"の時、受験報告(進学)に下記項目を追加。
            -- 選考分類コンボ、選考結果コンボ、志望順位
            2.上記をそれぞれAFT_GRAD_COURSE_DETAIL_DAT.DETAIL_SEQ = '6'のREMARK1, REMARK2, REMARK3に保存

2019/10/03  1.【更新】の際、useAutoSetCollegeNameToThinkExamプロパティが1かつ
              進路状況が’1：決定’かつ登録済みの大学コードと画面の大学コードが不一致の時
              ”指導要録に表記する進路先（直接入力）”のテキストに大学名を設定する処理を追加

2019/11/22  1.合格短冊匿名希望チェックの表示を修正
            --プロパティー「knje360ShowTokumeiCheck」が'1'の場合、表示

2020/01/06  1.プロパティー「useCollegeExamCalendar」を追加。
            2.入試カレンダーフラグを参照し、入試カレンダーを使用しないように修正

2020/02/07  1.受験種別ラジオの初期値を修正
            --プロパティー「knje360ShubetDefault」 1:全て 3:受験報告(就職) それ以外:受験報告(進学)

2020/07/10  1.プロパティー「Show_Recommendation」を追加
            2.進路調査で進路種別「1：進学」を選択時、プロパティー「Show_Recommendation」が"1"ならば内部推薦希望先登録にする

2021/02/18  1.リファクタリング
            2.明治のとき、駿台と同様に委員会は処理年度のデータを出力するように変更

2021/03/04  1.求人番号に数値のみの制限追加

2021/03/10  1.SCHOOL_KINDif避け 京都対応

2021/04/08  1.削除処理の条件をカット（AFT_GRAD_COURSE_DETAIL_DATにゴミデータが残る）
            2.学校検索の条件を修正
