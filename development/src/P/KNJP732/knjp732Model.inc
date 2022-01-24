<?php

require_once('for_php7.php');

class knjp732Model extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //処理種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //請求期の読替
        $this->rep_month = array();
        for ($i = 1; $i <= 12; $i++) {
            $this->rep_month[$i] = $i + 3;
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel()
    {
        //事前チェック（CSV取込項目設定）
        $db = Query::dbCheckOut();
        $errcnt = $db->getOne(knjp732Query::preCheckCsvM($this));
        Query::dbCheckIn($db);

        if ($errcnt > 0) {
            $this->setWarning("MSG305", "（CSV取込項目設定）");
            return;
        }

        //ファイルの文字列の取得
        $this->data_arr = $this->data_arr2 = array();
        list ($this->data_arr, $this->data_arr2) = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();

            //生徒、月ごとの請求額格納
            $planMoney = array();
            $query = knjp732Query::getPlanMoney($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["ROW_NO"]) {
                    $planMoney[$row["SCHREGNO"].":".$row["PAID_LIMIT_MONTH"]]["ROW_NO".$row["ROW_NO"]] = $row["PLAN_MONEY"];
                } else {
                    $planMoney[$row["SCHREGNO"].":".$row["PAID_LIMIT_MONTH"]]["PLAN_PAID_MONEY"]       = $row["PLAN_MONEY"];
                }
            }
            $result->free();

            //生徒、月ごとの入金額格納
            $paidMoney = array();
            $query = knjp732Query::getPaidMoney($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $paidMoney[$row["SCHREGNO"].":".$row["PAID_LIMIT_MONTH"]] = $row["PLAN_PAID_MONEY"];
            }
            $result->free();

            //エラー対象の学籍番号取得
            $schnoArray = $schnoArray2 = array();
            $counter = $counter2 = 0;
            foreach ($this->data_arr2 as $key => $val) {
                foreach ($val as $field => $money) {
                    if ($field == "PLAN_PAID_MONEY_DATE") {
                        continue;
                    }
                    if ($money != $planMoney[$key][$field]) {
                        list ($schno, $m) = explode(":", $key);
                        if (!in_array($schno, $schnoArray)) {
                            if ($counter < 50) {
                                $schnoArray[] = $schno;
                            }
                            $counter++;
                        }
                    }
                }
                if ($paidMoney[$key] > 0) {
                    list ($schno, $m) = explode(":", $key);
                    if ($counter2 < 50) {
                        $schnoArray2[] = $schno;
                    }
                    $counter2++;
                }
            }
            //エラー件数取得
            Query::dbCheckIn($db);
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if (get_count($schnoArray) > 0) {
                    $schnoList = implode(",", $schnoArray);
                    if ($counter >= 50) {
                        $schnoList .= "...";
                        $this->setWarning("MSG203", "請求額と一致しません。\\n学籍番号：".$schnoList);
                        return;
                    }
                } elseif (get_count($schnoArray2) > 0) {
                    $schnoList = implode(",", $schnoArray2);
                    if ($counter2 >= 50) {
                        $schnoList .= "...";
                        $this->setWarning("MSG203", "入金データが存在します。\\n学籍番号：".$schnoList);
                        return;
                    }
                }
                //更新
                knjp732Query::updateQueryCsv($this, $this->data_arr2);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr2)."件");
            }
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString()
    {
        $temp_arr = $temp_arr2 = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //CSV取込項目格納
            $csvRowArray = array();
            $query = knjp732Query::getCollectCsvInfoDat($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $csvRowArray[$row["ROW_NO"]] = $row["ROW_NO_NAME"];
            }
            $result->free();

            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["YEAR"]                  = array(4,    "HISSU",  "INT",   "請求年度");
            $checkdata["PLAN_PAID_MONEY_DATE"]  = array(10,   "HISSU",  "DATE",  "入金日");     //西暦年(4) + 月(2) + 日(2)
            $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT",   "学籍番号");
            $checkdata["PAID_LIMIT_TERM"]       = array(2,    "HISSU",  "INT",   "請求期");
            $checkdata["PLAN_PAID_MONEY"]       = array(8,    "HISSU",  "INT",   "入金額計");
            foreach ($csvRowArray as $rowno => $name) {
                $checkdata["ROW_NO".$rowno]         = array(8,    "",  "INT",   $rowno.':'.$name);
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = array("SCHREGNO", "PAID_LIMIT_TERM");
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }
                    $addupField = array("PLAN_PAID_MONEY");

                    //チェック用 CSV取込項目
                    $data = array();
                    $data["YEAR"]                   = $tmp[0];
                    $data["PLAN_PAID_MONEY_DATE"]   = $tmp[17];
                    $data["SCHREGNO"]               = $tmp[18];
                    $data["PAID_LIMIT_TERM"]        = $tmp[21];
                    $data["PLAN_PAID_MONEY"]        = $tmp[24];
                    foreach ($csvRowArray as $rowno => $name) {
                        $no = $rowno-1;
                        $data["ROW_NO".$rowno]          = $tmp[$no];
                        $addupField[] = "ROW_NO".$rowno;
                    }

                    //学籍番号と請求期でグループ化
                    foreach ($addupField as $field) {
                        $paid_limit_month = ((int)$data["PAID_LIMIT_TERM"] >= 10) ? (int)$data["PAID_LIMIT_TERM"] - 9 : (int)$data["PAID_LIMIT_TERM"] + 3;
                        $temp_arr2[sprintf("%08s", $data["SCHREGNO"]).":".sprintf("%02s", $paid_limit_month)][$field] += (int)$data[$field];
                    }
                    $temp_arr2[sprintf("%08s", $data["SCHREGNO"]).":".sprintf("%02s", $paid_limit_month)]["PLAN_PAID_MONEY_DATE"] = $this->date_rep($data["PLAN_PAID_MONEY_DATE"]);

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $temp_arr2, $line, $duplicateCsv1, $primaryKey1)) {
                        foreach ($checkdata as $key => $val) {
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                $data[$key] = $this->date_rep($data[$key]);
                            }
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}s", $data[$key]);
                            }
                        }
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return array($temp_arr, $temp_arr2);
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    function errCheck($db, $data, $checkdata, $data2, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $duplicateDbErrFlg  = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $duplicateDbErrMsg  = "";

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";
        $duplicateCsvSep    = "";
        $duplicateDbSep     = "";

        $cnt = knjp732Query::getDBDuplicatePaid($this, $data2);
        if ($cnt > 0) {
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複2");
        }

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //存在チェック
            $array = array("YEAR","SCHREGNO");
            if (in_array($key, $array)) {
                if ($key == "YEAR" && $this->field["YEAR"] != $data[$key]) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                if ($key == 'SCHREGNO' && strlen($data[$key]) && 0 == $db->getOne(knjp732Query::schregnoCntSql($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateDbErrMsg.$duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp732Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    function date_rep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel()
    {
        $headerDuplicate = array("SCHOOLCD"     => "学校コード",
                                 "SCHOOL_KIND"  => "学校校種",
                                 "YEAR"         => "請求年度",
                                 "SLIP_NO"      => "伝票番号",
                                 "COLLECT_L_CD" => "会計科目コード",
                                 "COLLECT_M_CD" => "会計項目コード",
                                 "PLAN_YEAR"    => "入金計画年",
                                 "PLAN_MONTH"   => "入金計画月",
                                 "SEQ"          => "SEQ");
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        // エラー出力
        $tables = array();
        $tables = array("1" => "COLLECT_SLIP_PLAN_PAID_M_DAT",
                        "2" => "COLLECT_SLIP_PLAN_PAID_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }

        $contents .= implode($header_err, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //エラー
        $query = $this->selectCsvErrQuery(PROGRAMID);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header_err as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            $filename = sprintf("%s.csv", "エラー出力");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
