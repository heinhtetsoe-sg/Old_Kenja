<?php

class knjx_e372fModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_e372fModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array();
        // サブフォームGET
        if (VARS::get("SEND_GRADE_HR_CLASS")) {
            $this->field["HR_CLASS"] = VARS::get("SEND_GRADE_HR_CLASS");
        }

        if (VARS::post("OUTPUT")) {
            $this->field["OUTPUT"] = VARS::post("OUTPUT");
        }
        if (VARS::post("SHORI_MEI")) {
            $this->field["SHORI_MEI"] = VARS::post("SHORI_MEI");
        }
        if (VARS::post("HEADER")) {
            $this->field["HEADER"] = VARS::post("HEADER");
        }
        if (VARS::post("HR_CLASS")) {
            $this->field["HR_CLASS"] = VARS::post("HR_CLASS");
        }

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }


    /**
     * CSV取込処理
    */
    public function &getCsvExecModel()
    {
        if ($this->field["OUTPUT"] == "2") {
            //データ取込
            return $this->getCsvInputModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    /**
     * CSV出力処理
    */
    public function &getDownloadModel()
    {
        if ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "4") {
            //ヘッダ出力(見本)
            //データ出力
            return $this->getCsvDownloadModel();
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            return $this->getErrorCsvDownloadModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    /**
     * CSV取込
    */
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            //データ更新
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $this->convertRank = array();
                foreach ($this->data_arr as $schregNo => $value) {
                    $this->convertRank[$schregNo] = $value["CONVERT_RANK"];
                }
                knjx_e372fQuery::update($this);

                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                if (count($this->data_arr) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array(
                  "SCHREGNO"                 => array(8,"学籍番号")
                // , "GRADE"                    => array(2,"学年")
                // , "HR_CLASS"                 => array(3,"組")
                // , "ATTENDNO"                 => array(3,"出席番号")
                // , "NAME"                     => array(6,"氏名")
                // , "ATTEND_ADJUSTMENT_SCORE"  => array(6,"出欠調整点")
                // , "ADJUSTMENT_SCORE"         => array(6,"調整点入力")
                // , "ADJUST_TOTAL_SCORE"       => array(6,"調整点合計")
                // , "CONVERT_SCORE"            => array(6,"換算値")
                // , "CONVERT_TOTAL"            => array(6,"換算値合計")
                , "CONVERT_RANK"             => array(3,"順位")
            );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('AFT_SCHREG_CONVERT_SCORE_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    $data = array(
                          "SCHREGNO" => (strlen(trim($tmp[0]))) ? sprintf("%08d", $tmp[0]) : ""
                        // , "GRADE"                   => $tmp[1]
                        // , "HR_CLASS"                => $tmp[2]
                        // , "ATTENDNO"                => $tmp[3]
                        // , "NAME"                    => $tmp[4]
                        // , "ATTEND_ADJUSTMENT_SCORE" => $tmp[5]
                        // , "ADJUSTMENT_SCORE"        => $tmp[6]
                        // , "ADJUST_TOTAL_SCORE"      => $tmp[7]
                        // , "CONVERT_SCORE"           => $tmp[8]
                        // , "CONVERT_TOTAL"           => $tmp[9]
                        , "CONVERT_RANK"            => $tmp[10]
                        , "YEAR"                    => CTRL_YEAR
                    );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$data["SCHREGNO"]] = $data;
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

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO", "CONVERT_RANK"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("SCHREGNO",  "CONVERT_RANK"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $query = knjx_e372fQuery::selectConvertScoreDat($data[$key]);
                $schregRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!$schregRow) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $data[$key]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e372fQuery::insertCsvMsgPrgDat($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getCsvDownloadModel()
    {
        //項目
        $header = array(
              "SCHREGNO"                 => "学籍番号"
            , "GRADE"                    => "学年"
            , "HR_CLASS"                 => "組"
            , "ATTENDNO"                 => "出席番号"
            , "NAME"                     => "氏名"
            , "ATTEND_ADJUSTMENT_SCORE"  => "出欠調整点"
            , "ADJUSTMENT_SCORE"         => "調整点入力"
            , "ADJUST_TOTAL_SCORE"       => "調整点合計"
            , "CONVERT_SCORE"            => "換算値"
            , "CONVERT_TOTAL"            => "換算値合計"
            , "CONVERT_RANK"             => "順位"
            , $this->lastColumn          => $this->lastColumn
        );

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $sample = "";
        $isData = false;
        //データ取得
        $query = knjx_e372fQuery::selectQuery($this);
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力(見本)
            $query = knjx_e372fQuery::selectSampleQuery($this);
            $sample = "（見本）";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $row[$this->lastColumn] = $this->lastColumn;
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($isData) {
            $fileName = sprintf("換算値順位%s.csv", $sample);
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //エラー出力
    public function getErrorCsvDownloadModel()
    {
        //項目
        $header = array(
              "MSGROW"    => "行番号"
            , "MSGREMARK" => "メッセージ"
        );

        $contents  = "";
        $headerDuplicate = array("YEAR"       => "年度",
                                 "SCHREGNO"   => "学籍番号");

        $tables = array();
        $tables = array("1" => "AFT_SCHREG_CONVERT_SCORE_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($isData) {
            $fileName = "エラー出力.csv";
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
