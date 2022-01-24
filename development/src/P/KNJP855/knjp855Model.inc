<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp855Model extends Model
{
    public $field      = array();
    public $cmd;
    public $subclassGroup;
    public $control;        //コントロールマスタ
    public $schoolName;
    public $schoolCode;

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
        }

        $this->cmd        = VARS::request("cmd");
        $this->year       = CTRL_YEAR;
        $this->semester   = CTRL_SEMESTER;
        $this->schoolCd   = sprintf("%012d", SCHOOLCD);
        $this->schoolKind = "H"; // 固定

        //出力最大項目数
        $this->outputMaxCnt = 10;

        //CSVの固定部分
        $this->fixedHeader = array();
        $this->fixedHeader["GRADE"]     = "学年";
        $this->fixedHeader["HR_CLASS"]  = "組";
        $this->fixedHeader["ATTENDNO"]  = "出席番号";
        $this->fixedHeader["SCHREGNO"]  = "学籍番号";
        $this->fixedHeader["NAME"]      = "生徒氏名";

        //取込CSVの入金項目列が開始する開始オフセット
        $this->collectStartOffset = get_count($this->fixedHeader);

        $this->field = array("YEAR"               =>  VARS::post("YEAR"),               //年度
                             "GRADE"              =>  VARS::post("GRADE"),              //学年
                             "HR_CLASS"           =>  VARS::post("HR_CLASS"),           //クラス
                             "OUTPUT"             =>  VARS::post("OUTPUT"),             //CSV区分
                             "COLLECT_MONTH"      =>  VARS::post("COLLECT_MONTH"),      //徴収月
                             "COLLECT_PATTERN_CD" =>  VARS::post("COLLECT_PATTERN_CD"), //伝票日付
                             "SLIP_DATE"          =>  VARS::post("SLIP_DATE"),          //伝票日付
                             "HEADER"             =>  VARS::post("HEADER"),             //ヘッダ有
                            );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //出力対象項目
        $this->selectdata = array();
        if (VARS::post("selectdata") != "") {
            $this->selectdata = explode(",", VARS::post("selectdata"));
        }

        //DB接続
        $db = Query::dbCheckOut();

        //グループコード番号帯取得
        $p013Row = $db->getRow(knjp855Query::getNameMstP012($this), DB_FETCHMODE_ASSOC);
        if ($p013Row) {
            $this->grpCdFromNo = $p013Row["NAME2"];
            $this->grpCdToNo   = $p013Row["NAME3"];
        }

        //割振りグループコード設定
        $query = knjp855Query::getMaxCollectGrpCdQuery($this);
        $maxGrpCd = $db->getOne($query);
        $tmp = ($maxGrpCd != "") ?  $maxGrpCd + 1 : $this->grpCdFromNo;
        $this->nextGrpCd = sprintf("%04d", $tmp);

        //パターン情報取得
        $this->patternInfo = $db->getRow(knjp855Query::getCollectPattern($this, $this->field["COLLECT_PATTERN_CD"]), DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /****************/
        /*  ヘッダー例  */
        /****************/

        //出力項目取得
        $outputLMList = array();
        $query = knjp855Query::getKagaihiList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["VALUE"], $this->selectdata)) {
                continue;
            }
            $outputLMList[$row["VALUE"]] = $row["COLLECT_M_NAME"];
        }

        //ヘッダ1行目
        $header = array();
        $header = array_merge($header, $this->fixedHeader);
        foreach ($outputLMList as $lmCd => $mName) {
            $header["LMCD".$lmCd] = $mName;
        }

        //ヘッダ2行目
        $header2 = array();
        foreach ($this->fixedHeader as $field => $label) {
            //固定列にはブランクをセット
            $header2[$field] = "";
        }
        foreach ($outputLMList as $lmCd => $mName) {
            $header2["LMCD".$lmCd] = $lmCd;
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "1") {
            //データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
            $contents .= implode($header2, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $contents .= implode($header_err, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = "";
        if ($this->field["OUTPUT"] == "1") {
            $query = knjp855Query::selectMainQuery($this);
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $query = knjp855Query::selectCsvErrQuery();
        }

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "1") {     //データ
                    //カンマ変換
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));
                    $array = $header;
                } else {
                    $array = $header_err;
                }

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $title = $this->year.'年度_課外費CSV';

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            //データ出力
            if ($this->field["OUTPUT"] == "1") {
                $filename = sprintf("%s.csv", $title);
            //エラー出力
            } else {
                $filename = sprintf("%s.csv", "エラー出力");
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //更新対象の入金項目(ヘッダ2行目で判断)
        $this->updateLMCd = array();

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $cnt = 0;
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");

            if ($errcnt == 0) {
                $cnt = knjp855Query::updateQueryCsv($db, $this, $this->data_arr);
            }

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //チェック対象項目
            $checkdata = array();
            $checkdata["GRADE"]     = array(2, "HISSU", "INT2", "");
            $checkdata["HR_CLASS"]  = array(3, "HISSU", "INT2", "");
            $checkdata["ATTENDNO"]  = array(3, "HISSU", "INT2", "");
            $checkdata["SCHREGNO"]  = array(8, "HISSU", "INT2", "SCHREGNO");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjp855Query::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;          //行数
            $csvColCnt = 0;     //取込CSVの列数
            $header = array();  //CSVヘッダ項目
            $headerChkFlg  = false;
            $header2LineNo = ($this->field["HEADER"] == "on") ? 2 : 1;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$headerChkFlg && $this->field["HEADER"] == "on") {
                        $headerChkFlg = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    //ヘッダ2行目
                    if ($header2LineNo == $line) {
                        //取込入金項目のコードを取得(この行は取込項目を特定するためにヘッダチェックの有無に関わらず、必ず読み込まれる必要がある)
                        for ($header2Col = $this->collectStartOffset; $header2Col < $checkcount; $header2Col++) {
                            $lmCd  = sprintf("%04d", $tmp[$header2Col]);
                            $lmKey = "LMCD-".$lmCd;
                            $mName = knjp855Query::getCollectLMName($db, $this, $lmCd);
                            $this->updateLMCd[$lmKey] = $mName;
                            $checkdata[$lmKey] = array(7, "", "INT", "LMCD");
                        }

                        //取込ヘッダ項目をセット
                        $header = array_merge($this->fixedHeader, $this->updateLMCd);
                        $line++;
                        continue;
                    }

                    $data = array();
                    $dataCnt = 0;
                    foreach ($header as $field => $label) {
                        $tmpVal = $tmp[$dataCnt];
                        if (array_key_exists($field, $checkdata)) {
                            $fieldSize = $checkdata[$field][0];
                            $fieldType = $checkdata[$field][2];
                            if ($fieldType == "INT2") {
                                $tmpVal = sprintf("%0{$fieldSize}d", $tmpVal);
                            }
                        }
                        $data[$field] = $tmpVal;
                        $dataCnt++;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $header, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $header, $data, $checkdata, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";

        foreach ($header as $field => $label) {
            if (in_array($field, array_keys($checkdata))) {
                list($size, $hissu, $div, $type) = $checkdata[$field];

                //必須項目チェック
                if ($hissu == "HISSU" && !strlen(trim($data[$field]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }

                //数値チェック
                if (($div == "INT" || $div == "INT2") && $data[$field] != "" && !is_numeric($data[$field])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $label);
                }

                //存在チェック
                if ($type == "SCHREGNO") {
                    if ($data[$field] != "" &&  0 == $db->getOne(knjp855Query::getSchregExistCnt($this, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } elseif ($type == "LMCD") {
                    //金額が入っている列に対応する入金項目(ヘッダ2行目)を存在チェック
                    $lmCd = explode("-", $field)[1];
                    if ($data[$field] != "" && 0 == $db->getOne(knjp855Query::getCollectLMstExistCnt($this, $lmCd))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "入金項目:{$lmCd}");
                    }
                }

                //文字数チェック
                if ($type != "LMCD" && strlen($data[$field]) > $size) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjp855Query::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //取込CSVの入金項目列に何件入力があるかを返す
    public function getLMUpdCnt ($data_arr, $lmCd) {
        $lmCdUpdCnt = get_count(array_filter(array_column($data_arr, $lmCd), function($value) {
            return strlen(trim($value)) > 0;
        }));

        return $lmCdUpdCnt;
    }

    //納期限日付を取得
    public function getLimitDate($db, $paid_limit_month) {
        //入金パターン引落月
        $directDebitDate = $this->patternInfo["DIRECT_DEBIT_DATE"];

        //銀行休業日取得
        $holidayArray = array();
        $result = $db->query(knjp855Query::getBankHolidayDat($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        $result->free();

        $year = (intval($paid_limit_month) <= 3) ? ($this->year + 1) : $this->year;
        $lastday = date("t", mktime(0, 0, 0, intval($paid_limit_month), 1, $year));
        $day = ($directDebitDate > $lastday) ? $lastday : $directDebitDate;

        $date = $year."-".sprintf("%02d", $paid_limit_month)."-".sprintf("%02d", $day);

        //土日祝日の場合は翌営業日
        $paid_limit_date = $date;
        for ($d = 0; $d < 100; $d++) {
            $paid_limit_date = date("Y-m-d", strtotime("$date $d day"));
            //土(6)・日(0)・祝日以外
            if (!in_array(date('w', strtotime($paid_limit_date)), array(0,6)) && !in_array($paid_limit_date, $holidayArray)) {
                break;
            }
        }

        return $paid_limit_date;
    }

}
