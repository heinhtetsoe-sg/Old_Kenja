# kanji=漢字
# $Id: readme.txt,v 1.65 2013/07/18 01:57:10 m-yama Exp $

2009/10/06  1.knjd232xを元に新規作成

2009/10/17  1.皆勤者を選択して、0を入力するとDBエラーになっていたのを修正

2009/10/20  1.ファイル名の変更、DBエラーの修正、CSVレイアウトの修正

2009/10/28  1.SCHREG_ABSENCE_HIGH_DATは、DIV='2’(随時)が対象

2009/11/20  1.「成績不振者」の評価(評定)をテキストボックスに変更
            -- その際「学期」コンボが「学年末」の場合、入力範囲を１～５とする。
            -- 「学期」コンボが「学年末」以外の場合、「評定」⇒「評価」と修正
            2.「学期」コンボの初期値を「学年末」に修正

2009/11/23  1.以下の通り修正
            -- 学期コンボが「学年末」以外の場合、
            -- 成績優良者が出力されない不具合を修正
            2.以下の通り修正
            -- 成績不振者にて、指定した条件と一致
            -- しない科目も出力されている不具合を修正
            3.成績不振者に授業時数を追加
            4.評定平均は小数点第１位まで表示（小数点第２位を四捨五入）

2009/11/27  1.ラジオボタンで「総合的な時間」を追加、「教科・科目」の「教科以外」を「特別活動」に変更

2009/12/28  1.出欠状況のテキスト指定がない場合、固定500としていたが99999にした。

2010/01/11  1.「皆勤者/皆勤者」を選択した時のクエリのバグ修正

2010/02/01  1.画面レイアウトの調整、CSVの項目名(単位数累計)に「前年度の」を追加、「SCHREG_STUDYREC_DAT」は今年度を含めないよう修正
            2.画面下の「終了」ボタンは残す、「成績優良者」も同様に修正

2010/02/10  1.評価・評定で、0又はNULLの生徒も抽出できるよう修正
            2.成績不振者の値は学期コンボ変更後、初期値になるよう修正。ファイル名、ファイル中のタイトル修正。
            3.「学年」に「全て」を追加、「1行/1人 or 複数行/1人」の選択を追加

2010/02/12  1.「欠課数換算」のパターン5の処理を追加
            2.成績不振者の特別活動の不具合修正、成績不振者に「欠時数」、「遅刻・早退」の追加
            3.成績不振者の特別活動の「欠課時数」、「授業時数」を五捨六入に変更
            4.五捨六入の処理に一旦四捨五入の処理追加(FLOATを使っているため)
            5.上記の四捨五入する位置の修正
            6.「総合的な時間」を選択したときのjavascriptのエラー修正
            7.「特別活動」の欠課数換算方法を名称マスタを参照して、3種類に分ける処理追加

2010/02/13  1.「特別活動」の出力をグループコード別に出力できるよう修正
            2.「特別活動グループ名」は固定で4つセットするよう修正

2010/02/15  1.「特別活動」のSQL修正(合計値で特別上限値と比較するよう修正)
            2.「成績不振者」の選択方法をチェックボックスに変更

2010/02/16  1.「授業時数」を「出席すべき時数」に修正

2010/02/18  1.「成績不振者」でチェックボックスを複数選択した時、「AND」ではなく「OR」に変更

2010/03/02  1.「成績不振者 (過去の不認定科目) 」の項目追加
            2.SQLエラー修正
            3.「成績不振者 (過去の不認定科目) 」のCSVに「年度」追加

2010/03/03  1.「出欠状況」「皆勤者」での処理の振り分け修正
            2.「教科・科目」「総合的な時間」の条件を修正
            3.ファイルのタイトルの条件修正

2010/03/16  1.生徒の抽出条件に下記を追加
            --「成績優良者」以外の時 SCHREG_BASE_MST の GRD_DATE が入力日付'以下'の人は対象外
            --「成績優良者」の時 SCHREG_BASE_MST の GRD_DIV が'1'の人も対象外

2010/07/14  1.「成績優良者」の条件で四捨五入した後の値と比べるよう修正

2010/08/18  1.出欠コード「23＝遅刻２、24＝遅刻３」の追加に伴い修正。

2010/09/02  1.特別活動を計算する分母の1時間当たりの授業時分の参照フィールドを変更
            -- v_school_mst_rev1.13.sql

2010/09/15  1.集計フラグを参照するテーブルを以下の通りに修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2010/10/27  1.合併元科目を表示する、合併先科目を表示するチェックボタンを追加した。
            -- 学期関係無しに合併元・先科目の表示選択ができるよう修正。

2010/11/17  1.「成績不振者」合併先科目の出欠関連項目は合併元科目の各項目の合計値に変更した。

2010/12/09  1.出欠の状況の遅刻・早退に欠課数換算後の余りを出力するように修正した。
            2.成績不振者、出欠の状況に"chikokuHyoujiFlg"での遅刻・早退欄の表示切替処理を追加した。

2011/02/24  1.「成績不振者（教科・科目）」に科目コード、科目担当者、公欠数、忌引数、出停数を追加した。
            2.「成績不振者（過去の不認定科目）」に評定～出席すべき時数を追加した。

2011/03/07  1.「成績不振者（過去の不認定科目）」が出力されない不具合を修正。
            -- 対象の生徒を取得する条件にて、固定値で2006年度未満となっていたのをログイン年度未満と修正。

2011/06/29  1.テスト種別コンボを追加した。（成績優良者、成績不振者で使用）
            -- プロパティー「KNJD232Y_TESTKIND」で出力内容のコントロール
            2.皆勤者の抽出条件に授業遅刻を追加した。
            -- 出力項目にも授業遅刻を追加した。
            3.成績優良者で名称マスタ「D008」の教科は除くように変更した。
            4.成績優良者で名称マスタ「D008」の教科は除く処理カットした。
            5.成績優良者で名称マスタ「D017」で学期に応じたABBV1～NAMESPARE1に'1'のある科目を除くようにした。

2011/07/01  1.プロパティー「KNJD232Y_TESTKIND」をテスト種別コンボの表示コントロールに変更した。

2011/07/04  1.テスト種別が"9900"の場合はRECORD_RANK_DAT、それ以外はRECORD_RANK_V_DATを参照するように変更した。
            2.名称マスタ「Z010」が鳥取で学年末を選択した場合、テスト種別は固定で学年評価、学年評定を表示した。
            -- 学年評価の評価はRECORD_SCORE_DATのSCOREから、評価平均はRECORD_RANK_V_DATのAVGから出力するようにした。

2011/10/05  1.「特別活動の上限値をこえた」の判定に学校マスタの「特活欠課数換算」を参照するように変更した。
            -- 固定で「1：二捨三入」で処理していた。

2011/10/07  1.成績優良者、成績不振者（教科・科目）について以下のとおり変更した。
            -- 合併先科目の見込み単位数欄は、生徒のRECORD_SCORE_DATの合併元科目の単位マスタの単位数の合計を表示する。
            -- （修正前は先科目の単位マスタの単位数を表示していた。）
            2.1.の修正について以下のとおり修正した。
            -- 合併先科目の見込み単位数欄は、生徒の講座名簿の合併元科目の単位マスタの単位数の合計を表示する。

2011/10/21  1.在籍期間全てチェックボックスを追加した。
            -- 入学年度からの出欠データを加算した累計を出力する。
            -- 留年生を除く。
            2.ラベル機能を追加した。

2011/12/06  1.「成績不振者(特別活動の上限値をこえた)」のＳＱＬを修正
            -- 参照する上限値を変更(GET_ABSENCE_HIGH ===> COMP_ABSENCE_HIGH)
            -- 設定されてない時の上限値を変更(0 ===> 999999)
            2.「特別活動」の欠課数換算方法を名称マスタを参照して、3種類に分ける処理を修正
            -- NAMESPARE11が'1'の場合、欠時 + 遅刻 + 早退 ===> 欠時 + 遅刻

2011/12/12  1.出欠状況の教科・科目、教科以外の出力項目に欠時数を追加した。

2012/03/15  1.出欠集計範囲ラジオボタンを追加した。
            -- 学期を選択したとき、出欠集計は学期開始日付とする。
            -- 学期を選択したとき、下記を使用不可とする。
            -- ・「欠課時数が修得...」チェックボックス
            -- ・「欠課時数が履修...」チェックボックス
            -- ・「特別活動...」CSV出力ボタン
            -- ・「在籍期間全て」チェックボックス

2012/07/13  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/07/17  1.学年に「全て」を選択した場合にデータがあっても「データは存在していません。」と表示される不具合を修正

2013/01/11  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/06/28  1.名称マスタのマスタ化に伴う修正

2013/07/02  1.SUBCLASS_SEQを"004"に変更した。

2013/07/18  1.講座担任名の取得方法を変更

2013/07/24  1.testtesttest
