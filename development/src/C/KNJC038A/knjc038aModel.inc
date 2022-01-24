<?php
class knjc038aModel extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $field      = array();
    var $data       = array();
    var $control;  //コントロールマスタ
    var $staffcd;  //登録者コード
    var $properties = array();

    function knjc038aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //登録者コード
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        $this->field = array(
                              "OUTPUT"      => VARS::post("OUTPUT")     //出力取込種別
                            , "START_DATE"  => VARS::post("START_DATE") //開始日付
                            , "END_DATE"    => VARS::post("END_DATE")   //終了日付
                            , "YEAR"        => VARS::post("YEAR")       //年度
                        );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function &getMainModel() {}

    //更新(データ取込)
    function getExecModel() {

        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }
        //アップロードされたファイルかチェック
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーデータの削除
        $query = knjc038aQuery::deleteQueryErr();
        $result = $db->query($query);

        // 指定範囲のデータは、最初に全て削除する。
        // 学校側が、空のCSVを取り込むことでクリアされる様な仕様が望ましいとの事。
        $query = knjc038aQuery::allDeleteAttendDayDat($this);
        $db->query($query);
        $query = knjc038aQuery::allDeleteAttendDat($this);
        $db->query($query);

        $record_no = 0; //行番号(初期値)
        $error_no = 0;  //エラー件数(初期値)

        $fp = fopen ($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
        //ファイルポインタがファイル終端に達しているかどうか調べる
        while (!feof($fp)) {
            //ファイルポインタから1行取得する
            $buffer = fgets($fp, 4096);
            // 行番号カウント
            $record_no++;

            // 取得した行が空白の場合は次の行を取得
            if (trim($buffer) == '') {
                continue;
            }
            //１行目はヘッダなので無視
            if ($record_no < 1) {
                continue;
            }
            //１万件を超えたらループから抜ける。
            if ($record_no > 10000) break;

            //文字列を指定コードに変換する
            $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");
            $tmp = explode(",", $buffer);

            $data = array(
                "HR_CLASS"      => trim($tmp[0])    // クラス
                , "ATTENDNO"    => trim($tmp[1])    // 並び順
                , "NAME"        => trim($tmp[2])    // 名前
                , "ATTENDDATE"  => trim($tmp[3])    // 対象日
                , "DI"          => trim($tmp[4])    // 届出種類
                , "ADSENCE1"    => trim($tmp[5])    // 欠席種別１
                , "ADSENCE2"    => trim($tmp[6])    // 欠席種別２
                , "TARDINESS"   => trim($tmp[7])    // 遅刻相対内容
                , "REASON"      => trim($tmp[8])    // 理由
                , "SENDER"      => trim($tmp[9])    // 送信者
                , "UPDATED"     => trim($tmp[10])   // 更新日時
                , "UPDSTAFF"    => trim($tmp[11])   // 更新者
                , "ADTYEP"      => trim($tmp[12])   // 欠席種別
                , "PERIOD1"     => trim($tmp[13])   // １時限
                , "PERIOD2"     => trim($tmp[14])   // ２時限
                , "PERIOD3"     => trim($tmp[15])   // ３時限
                , "PERIOD4"     => trim($tmp[16])   // ４時限
                , "PERIOD5"     => trim($tmp[17])   // ５時限
                , "PERIOD6"     => trim($tmp[18])   // ６時限
                , "PERIOD7"     => trim($tmp[19])   // ７時限
            );

            //エラー出力用
            $koumoku_error = "";
            $koumoku_error = $this->koumoku_check($this, $data);

            // クラスの変換
            $data['HR_CLASS'] = substr($data['HR_CLASS'], 0, 1).'-'.substr($data['HR_CLASS'], 1, 1);
            // 並び順の変換
            $data['ATTENDNO'] = sprintf("%03d", trim($data['ATTENDNO']));
            // 対象日の変換(和暦 X月X日 → 西暦 XXXX/XX/XX)
            $pattern = "/([0-9]{1,2})月([0-9]{1,2})日/";
            preg_match($pattern, $data['ATTENDDATE'], $matches, PREG_OFFSET_CAPTURE);
            //３月以下は年度＋１
            $checkyear = $this->year;
            if (intval($matches[1][0]) <= 3) {
                $checkyear = $this->year + 1;
            }
            $attendDate = $checkyear.'-'.sprintf("%02d", $matches[1][0]).'-'.sprintf("%02d", $matches[2][0]);
            $data['ATTENDDATE'] = $attendDate;
            // 更新日時(XXXX/XX/XX XX:XX)
            $pattern = "/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2}) ([0-9]{1,2}):([0-9]{1,2})/";
            preg_match($pattern, $data['UPDATED'], $matches, PREG_OFFSET_CAPTURE);
            $updDate = sprintf("%04d",$matches[1][0]).'-'.sprintf("%02d", $matches[2][0]).'-'.sprintf("%02d",$matches[3][0]);
            $updDate .= ' '.sprintf("%02d", $matches[4][0]).':'.sprintf("%02d", $matches[5][0]);
            $data['UPDATED'] = $updDate;

            /********************************************************************/
            /*** ↓↓ 学籍番号チェック ↓↓
            /********************************************************************/
            $query = knjc038aQuery::getSchregNo($this, $data);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row) {
                $koumoku_error .= "存在しない生徒データ。";
            } else {
                $data['SCHREGNO'] = $row['SCHREGNO'];
            }

            // 登録した更新日時の方が大きい(未来)の場合は登録しない
            if ($insDataList[$data['SCHREGNO']][$data['ATTENDDATE']]) {
                $insData =  strtotime($insDataList[$data['SCHREGNO']][$data['ATTENDDATE']]);
                $updDate =  strtotime($data['UPDATED']);
                if ($updDate < $insData) {
                    // エラーには出力せずにレコードをスキップ
                    // $koumoku_error .= "更新日時が古い為、スキップ。";
                    continue;
                }
            }

            if ($koumoku_error != "") {
                $check_error = $koumoku_error;
                //フィールドサイズを超えてると入らないので120バイトにする
                if (strlen($check_error) > 120) {
                    $check_error = mb_strcut($check_error, 0, 115);
                    $check_error .= "..他";
                }
                knjc038aQuery::insertQueryErr($db, $record_no, $check_error);
                //エラー件数(カウント)
                $error_no++;

                continue;
            }

            // 校時変換用テーブル
            $periodList = array(
                "PERIOD1" => "2"
              , "PERIOD2" => "3"
              , "PERIOD3" => "4"
              , "PERIOD4" => "5"
              , "PERIOD5" => "6"
              , "PERIOD6" => "7"
              , "PERIOD7" => "8"
            );
            // DI_CD 判定
            $data['DAY_DI_CD'] = '';
            $data['DI_CD'] = '';
            if (strpos($data['DI'], '遅刻') !== false) {
                $data['DAY_DI_CD'] = '15';
                $data['DI_CD'] = '6';
            } else if (strpos($data['DI'], '早退') !== false) {
                $data['DAY_DI_CD'] = '16';
                $data['DI_CD'] = '6';
            } else {
                // $data['DI'] = '欠席' の場合の処理
                if (strpos($data['ADSENCE1'], '欠席') !== false) {
                    $data['DAY_DI_CD'] = '6';
                    $data['DI_CD'] = '6';
                    // 全校時 更新
                    foreach ($periodList as $key => $period) $data[$key] = '1';
                }
                if (strpos($data['ADSENCE1'], '出席扱い') !== false) {
                    $data['DAY_DI_CD'] = '1';
                    $data['DI_CD'] = '1';
                    // 全校時 更新
                    foreach ($periodList as $key => $period) $data[$key] = '1';
                }
                if (strpos($data['ADSENCE1'], '出席停止・忌引') !== false) {
                    $data['DAY_DI_CD'] = '2';
                    $data['DI_CD'] = '2';
                    // 全校時 更新
                    foreach ($periodList as $key => $period) $data[$key] = '1';
                }
                if (strpos($data['ADSENCE1'], '欠課(学校使用欄)') !== false) {
                    $data['DAY_DI_CD'] = '';
                    $data['DI_CD'] = '6';
                }
            }

            // チェックＯＫの処理
            // 出欠日別データ(ATTEND_DAY_DAT), 出欠データ(ATTEND_DAT) 削除
            $query = knjc038aQuery::deleteAttendDayDat($this, $data);
            $db->query($query);
            $query = knjc038aQuery::deleteAttendDat($this, $data);
            $db->query($query);

            if ($data['DAY_DI_CD'] != '') {
                // 出欠日別データ(ATTEND_DAY_DAT) 登録
                $insdata = array();
                $insdata["SCHREGNO"][TEXT] = $data['SCHREGNO'];
                $insdata["ATTENDDATE"][DATE]  = $data['ATTENDDATE'];
                $insdata["DI_CD"][TEXT] = $data['DAY_DI_CD'];
                $insdata["YEAR"][TEXT] = $this->year;
                $insdata["REGISTERCD"][TEXT]   = STAFFCD;
                $insdata["UPDATED"][NUMBER]    = "sysdate()";

                $query = Query::insertSQL($insdata, "ATTEND_DAY_DAT");
                $db->query($query);
            }

            // 登録キーを保持
            // ※更新日時が最新のデータを保持
            $insDataList[$data['SCHREGNO']][$data['ATTENDDATE']] = $data['UPDATED'];

            // 校時のフラグが立っているデータを登録
            foreach ($periodList as $key => $period) {

                if ($data[$key] == '1') {
                    // 講座CD取得
                    $query = knjc038aQuery::getChaircd($this, $data, $period);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($row) {
                        $data['CHAIRCD'] = $row['CHAIRCD'];
                    }

                    // 出欠データ(ATTEND_DAT) 登録
                    $insdata = array();
                    $insdata["SCHREGNO"][TEXT] = $data['SCHREGNO'];
                    $insdata["ATTENDDATE"][DATE]  = $data['ATTENDDATE'];
                    $insdata["PERIODCD"][TEXT] = $period;
                    $insdata["CHAIRCD"][TEXT] = $data['CHAIRCD'];
                    $insdata["DI_CD"][TEXT] = $data['DI_CD'];
                    $insdata["YEAR"][TEXT] = $this->year;
                    $insdata["REGISTERCD"][TEXT]   = STAFFCD;
                    $insdata["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($insdata, "ATTEND_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        fclose($fp);
        $this->message = "CSV登録を完了しました。\\nエラー件数：".$error_no."件";
        //１万件を超えたら強制終了する。
        if( $record_no > 10000 ) $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";

    }

    //ＣＳＶへエラー出力
    function getDownloadModel() {

        $header = array("MSGROW"    => "行番号",
                        "MSGREMARK" => "メッセージ");

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjc038aQuery::selectCsvQuery();
        $result = $db->query($query);
        $err_data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データ項目のチェック
    function koumoku_check($db, $data) {

        //項目エラー用
        $koumoku_error = "";

        // 桁数チェック
        if (strlen($data["HR_CLASS"]) != 2
        || strlen($data["ATTENDNO"])  > 3
        || strlen($data["NAME"])      > 120) {
            $koumoku_error .= "桁数エラー。";
        }

        // 必須チェック
        if ($data["HR_CLASS"] == "" || $data["ATTENDNO"] == "" || $data["NAME"] == ""
        || $data["ATTENDDATE"] == "" || $data["DI"] == ""
        || $data["SENDER"] == "" || $data["UPDATED"] == "") {
            $koumoku_error .= "必須項目エラー。";
        }

        // 対象日チェック(日付チェック)
        $pattern = "/([0-9]{1,2})月([0-9]{1,2})日/";
        if (preg_match($pattern, $data['ATTENDDATE'], $matches, PREG_OFFSET_CAPTURE) === false) {
            $koumoku_error .= "対象日エラー。";
        }

        if (!checkdate(intval($matches[1][0]), intval($matches[2][0]), intval($this->year))) {
            $koumoku_error .= "対象日エラー。";
        }

        // 対象日チェック(対象外)
        // 対象日チェック(対象外)
        $checkyear = $this->year;
        if (intval($matches[1][0]) <= 3) {
            $checkyear = $this->year + 1;
        }
        $attendDate = strtotime($checkyear.'/'.$matches[1][0].'/'.$matches[2][0]);
        $startDate =  strtotime($this->field['START_DATE']);
        $endDate =  strtotime($this->field['END_DATE']);
        if ($attendDate < $startDate || $endDate < $attendDate) {
            $koumoku_error .= "対象日範囲外エラー。";
        }

        // 届出種類
        if (strpos($data['DI'], '遅刻') === false 
        && strpos($data['DI'], '早退') === false 
        && strpos($data['DI'], '欠席') === false) {
            $koumoku_error .= "届出種類エラー。";
        }

        // 欠席種別１
        if (strpos($data['DI'], '欠席') !== false) {
            // 「届出種類」が「欠席」の場合、以下が登録されていない場合はエラー
            if (strpos($data['ADSENCE1'], '欠席') === false 
            && strpos($data['ADSENCE1'], '出席扱い') === false 
            && strpos($data['ADSENCE1'], '出席停止・忌引') === false 
            && strpos($data['ADSENCE1'], '欠課(学校使用欄)') === false) {
                $koumoku_error .= "欠席種別エラー。";
            }
        }

        // 更新日時(日付チェック)
        $pattern = "/([0-9]{4})\/([0-9]{1,2})\/([0-9]{1,2}) ([0-9]{1,2}):([0-9]{1,2})/";
        if (preg_match($pattern, $data['UPDATED'], $matches, PREG_OFFSET_CAPTURE) === false) {
            $koumoku_error .= "更新日時エラー。";
        }
        if (!checkdate(intval($matches[2][0]), intval($matches[3][0]), intval($matches[1][0]))) {
            $koumoku_error .= "更新日時エラー。";
        }

        // 送信者
        if (strpos($data['SENDER'], '先生') === false
        && strpos($data['SENDER'], '保護者') === false) {
            $koumoku_error .= "送信者エラー。";
        }
        // 更新者
        if (strlen($data['UPDSTAFF']) > 0) {
            if (strpos($data['UPDSTAFF'], '先生') === false
            && strpos($data['UPDSTAFF'], '保護者') === false) {
                $koumoku_error .= "更新者エラー。";
            }
        }
        // 送信者 or 更新者 に「先生」がない場合は登録不可
        if (strpos($data['SENDER'], '先生') === false && strpos($data['UPDSTAFF'], '先生') === false) {
            $koumoku_error .= "更新者・送信者エラー。";
        }

        //項目エラー用
        return $koumoku_error;
    }

}
?>
