<?php

require_once('for_php7.php');

class knje354Model extends Model
{
    public $cmd;
    public $file;
    public $data_arr = array();
    public $controls = array();
    public $field = array();
    public $lastColumn;//CSVの最後の列の値

    public function knje354Model()
    {
        $this->new_year = CTRL_YEAR + 1;
        common::GetControlMaster_Fnc($this->controls);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTPUT"       =>  VARS::post("OUTPUT"),       //出力取込種別
                             "SHORI_MEI"    =>  VARS::post("SHORI_MEI"),    //処理名
                             "YEAR"         =>  VARS::post("YEAR")          //年度
                             );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
        }
        $this->headercheck = VARS::post("HEADERCHECK");

        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイル
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        if ($this->warning) {
            return false;
        }

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knje354Query::insertQuery($this);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = $this->getCheckData();

            $umejidata = array("SCHOOL_CD",
                               "FACULTYCD",
                               "DEPARTMENTCD",
                               "PROGRAM_CD",
                               "PREF_CD",
                               "ADVERTISE_DIV");

            //DB接続
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $csvTable = array();
            $query = knje354Query::getCsvTableData();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $csvTable[] = array("FIELDNAME"     => $row["NAME"],
                                    "TYPE"          => $row["TYPENAME"],
                                    "NOTNULL_FIELD" => $row["NULLS"],
                                    "LEN"           => $row["LENGTH"]);
            }
            $result->free();

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);


            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('COLLEGE_EXAM_CALENDAR');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array();

                    for ($tmpCnt = 0; $tmpCnt < get_count($tmp); $tmpCnt++) {
                        $tmp[$tmpCnt] = str_replace("'", "", str_replace(" ", "", $tmp[$tmpCnt]));
                        if (in_array($csvTable[$tmpCnt]["FIELDNAME"], $umejidata)) {
                            $tmp[$tmpCnt] = (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%0".$csvTable[$tmpCnt]["LEN"]."d", $tmp[$tmpCnt]) : "";
                        }
                        if ($tmp[$tmpCnt] == $this->lastColumn) { //LASTCOLUMNは最後なので取り込まない
                            break;
                        }
                        if ($csvTable[$tmpCnt]["FIELDNAME"] != "") {
                            $data[$csvTable[$tmpCnt]["FIELDNAME"]] = $tmp[$tmpCnt];
                        }
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $csvTable, $duplicateCsv1, $primaryKey1)) {
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
        $this->csvTable = $csvTable;
        return $temp_arr;
    }

    public function getCheckData()
    {
        $checkdata = array("SCHOOL_CD"              => "大学コード",
                               "FACULTYCD"              => "学部コード",
                               "DEPARTMENTCD"           => "学科コード",
                               "PROGRAM_CD"             => "日程コード",
                               "FORM_CD"                => "方式コード",
                               "S_CD"                   => "系統小分類コード",
                               "SCHOOL_NAME"            => "大学名",
                               "FACULTYNAME"            => "学部名",
                               "DEPARTMENTNAME"         => "学科名",
                               "PROGRAM_NAME"           => "日程名",
                               "FORM_NAME"              => "方式名",
                               "S_NAME"                 => "系統小分類名称",
                               "ADVERTISE_FLG"          => "募集単位フラグ",
                               "BACHELOR_DIV"           => "文理区分",
                               "PREF_CD"                => "県コード",
                               "CENTER_PARTICIPATE"     => "センター採用区分",
                               "WANTED_STUDENT_CNT"     => "募集人員",
                               "JUDGE_DATE"             => "判定基準日",
                               "CENTER_JUDGE_B"         => "センター判定B",
                               "L_CD1"                  => "大分類Ⅰ",
                               "L_CD2"                  => "大分類Ⅱ",
                               "L_CD3"                  => "大分類Ⅲ",
                               "L_CD4"                  => "大分類Ⅳ",
                               "L_CD5"                  => "大分類Ⅴ",
                               "S_CD1"                  => "小分類Ⅰ",
                               "S_CD2"                  => "小分類Ⅱ",
                               "S_CD3"                  => "小分類Ⅲ",
                               "S_CD4"                  => "小分類Ⅳ",
                               "S_CD5"                  => "小分類Ⅴ",
                               "S_CD6"                  => "小分類Ⅵ",
                               "S_CD7"                  => "小分類Ⅶ",
                               "S_CD8"                  => "小分類Ⅷ",
                               "S_CD9"                  => "小分類Ⅸ",
                               "S_CD10"                 => "小分類Ⅹ",
                               "ADVERTISE_DIV"          => "募集区分",
                               "LIMIT_DATE_WEB"         => "入試締切日(WEB)",
                               "LIMIT_DATE_WINDOW"      => "入試締切日(窓口)",
                               "LIMIT_DATE_MAIL"        => "入試締切日(郵便)",
                               "LIMIT_MAIL_DIV"         => "入試締切郵便区分",
                               "EXAM_DATE"              => "入試日",
                               "EXAM_PASS_DATE"         => "合格発表日",
                               "PROCEDURE_LIMIT_DATE"   => "手続き締切日",
                               "ENT_MONEY"              => "納付金入学",
                               "PROCEDURE_MONEY"        => "納付金手続",
                               "TOTAL_MONEY"            => "納付金合計",
                               "ACCEPTANCE_CRITERION_A" => "判定基準A",
                               "ACCEPTANCE_CRITERION_B" => "判定基準B",
                               "ACCEPTANCE_CRITERION_C" => "判定基準C",
                               "ACCEPTANCE_CRITERION_D" => "判定基準D",
                               "DOCKING_CRITERION_A"    => "ドッキング判定基準A",
                               "DOCKING_CRITERION_B"    => "ドッキング判定基準B",
                               "DOCKING_CRITERION_C"    => "ドッキング判定基準C",
                               "DOCKING_CRITERION_D"    => "ドッキング判定基準D",
                               "CENTER_CRITERION_A"     => "センター判定基準A",
                               "CENTER_CRITERION_B"     => "センター判定基準B",
                               "CENTER_CRITERION_C"     => "センター判定基準C",
                               "CENTER_CRITERION_D"     => "センター判定基準D");

        return $checkdata;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //5千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    public function errCheck($db, $data, $data_each, $line, $csvTable, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($csvTable as $key => $val) {
            //必須項目チェック
            if ($val["NOTNULL_FIELD"] == "N") {
                if (!strlen(trim($data[$val["FIELDNAME"]]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $data_each[$val["FIELDNAME"]]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //数値チェック
                if ($val["TYPE"] == "SMALLINT" || $val["TYPE"] == "INTEGER") {
                    if ($data[$val["FIELDNAME"]] != "" && !is_numeric($data[$val["FIELDNAME"]])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $data_each[$val["FIELDNAME"]]);
                    }
                }

                //文字数チェック
                if ($val["TYPE"] == "VARCHAR") {
                    if (strlen($data[$val["FIELDNAME"]]) > $val["LEN"]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $data_each[$val["FIELDNAME"]]);
                    }
                }
            } else {
                //削除処理は、ない。
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje354Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $csvTable = array();
        $query = knje354Query::getCsvTableData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $csvTable[] = array("FIELDNAME"     => $row["NAME"],
                                "TYPE"          => $row["TYPENAME"],
                                "NOTNULL_FIELD" => $row["NULLS"],
                                "LEN"           => $row["LENGTH"]);
        }
        $result->free();

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $checkdata = $this->getCheckData();
            $contents = ($this->headercheck == 1) ? implode($checkdata, ","). "\n" : "";
            $sep = "";
            foreach ($csvTable as $key => $val) {
                //数値
                if ($val["TYPE"] == "SMALLINT" || $val["TYPE"] == "INTEGER") {
                    $contents .= $sep."数値";
                }
                //文字
                if ($val["TYPE"] == "VARCHAR") {
                    $contents .= $sep.$val["LEN"]."バイト";
                }
                $sep = ",";
            }
            $contents.= $sep.$this->lastColumn;
        } elseif ($this->field["OUTPUT"] == "3") {
            $duplicateHeader = array("YEAR"                  => "年度",
                                    "SCHOOL_CD"              => "大学コード",
                                    "FACULTYCD"              => "学部コード",
                                    "DEPARTMENTCD"           => "学科コード",
                                    "PROGRAM_CD"             => "日程コード",
                                    "FORM_CD"                => "方式コード",
                                    "S_CD"                   => "系統小分類コード",
                                    "ADVERTISE_DIV"          => "募集区分"
                                    );
            //エラー
            $tables = array();
            $tables = array("1" => "COLLEGE_EXAM_CALENDAR");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents .= implode($headerErr, ",")."\n";
            }

            $contents .= implode($this->getHeadErorr(), ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] == "3") {
            $query = $this->selectCsvErrQuery(PROGRAMID);

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "入試カレンダー";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //エラーヘッダ
    public function getHeadErorr()
    {
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        return $header_err;
    }
}
