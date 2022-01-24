<?php

require_once('for_php7.php');


class knje372fModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $replace_data   = array();    //置換で選択された生徒一覧
    var $chaircd;
    var $file;

    var $isKomazawa;    // 駒沢

    /**
     * メンバ変数の初期化
     */
    function knje372fModel()
    {
    }

    function init()
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

        $this->field["HR_CLASS"] = VARS::post("HR_CLASS");
        $this->field["SORT_ITEM"] = VARS::post("SORT_ITEM");

        //学籍番号
        $this->schregNoList = array();
        if(VARS::post("SCHREGNO_LIST")) {
            $this->schregNoList = explode(",", VARS::post("SCHREGNO_LIST"));
        }
        //換算値順位
        $this->convertRank = array();
        for ($i = 0; $i < get_count($this->schregNoList); $i++) {
            $schregNo = $this->schregNoList[$i];
            $this->convertRank[$schregNo] = VARS::post("CONVERT_RANK_".$schregNo);
        }

        $this->subField = array();
        // サブフォームGET
        if(VARS::get("HR_CLASS")) {
            $this->subField["HR_CLASS"] = VARS::get("HR_CLASS");
        }

        if (VARS::post("OUTPUT")) {
            $this->subField["OUTPUT"] = VARS::post("OUTPUT");
        }
        if (VARS::post("SHORI_MEI")) {
            $this->subField["SHORI_MEI"] = VARS::post("SHORI_MEI");
        }
        if (VARS::post("HEADER")) {
            $this->subField["HEADER"] = VARS::post("HEADER");
        }
        if(VARS::post("HR_CLASS")) {
            $this->subField["HR_CLASS"] = VARS::post("HR_CLASS");
        }

        $this->file = VARS::file("FILE");

    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->schregNoList); $i++) {
            $schregNo = $this->schregNoList[$i];
            if (strlen($this->convertRank[$schregNo]) <= 0) {
                $this->setWarning("MSG301","(順位を入力してください。)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knje372fQuery::update($this);
            $this->setCmd("edit");
            $this->setMessage("MSG201");
        } else {
            $this->setCmd("check");
        }
    }


    /**
     * CSV取込処理
    */
    function &getCsvExecModel() {

        if ($this->subField["OUTPUT"] == "2") {
            //データ取込
            return $this->getCsvInputModel();
        }
        $this->setWarning("MSG901","処理区分");
        return false;
    }

    /**
     * CSV出力処理
    */
    function &getDownloadModel() {

        if ($this->subField["OUTPUT"] == "1" || $this->subField["OUTPUT"] == "4") {
            //ヘッダ出力(見本)
            //データ出力
            return $this->getCsvDownloadModel();
        } else if ($this->subField["OUTPUT"] == "3") {
            //エラー出力
            return $this->getErrorCsvDownloadModel();
        }
        $this->setWarning("MSG901","処理区分");
        return false;
    }

    /**
     * CSV取込
    */
    function &getCsvInputModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                //データ更新
                $this->convertRank = array();
                foreach ($this->data_arr as $schregNo => $value) {
                    $this->convertRank[$schregNo] = $value["CONVERT_RANK"];
                }
                knje372fQuery::update($this);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                if (get_count($this->data_arr) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    //取込
    function GetFileString() {
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
                // , "DUMMY"                    => array(2,"DUMMY")
            );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('AFT_SCHREG_CONVERT_SCORE_DAT');

            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->subField["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    $data = array(
                          "SCHREGNO" => (strlen(trim($tmp[0]))) ? sprintf("%08d",$tmp[0]) : ""
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
                        // , "DUMMY"                   => $tmp[11]
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1) {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $masterErrFlg       = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $masterErrMsg       = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $masterSep          = "";
        $duplicateCsvSep    = "";

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
                $query = knje372fQuery::selectConvertScoreDat($data[$key]);
                $schregRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!$schregRow) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $data[$key]);
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->err_data[$line]["MSG"] = $checkError;
            knje372fQuery::insertCsvMsgPrgDat($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //ＣＳＶ出力
    function getCsvDownloadModel() {
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
            , "DUMMY"                    => "DUMMY"
        );

        $contents  = "";
        if ($this->subField["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $sample = "";
        $isData = false;
        //データ取得
        $query = knje372fQuery::selectQuery($this);
        if ($this->subField["OUTPUT"] == "1") {
            //ヘッダ出力(見本)
            $query = knje372fQuery::selectSmpleQuery($this);
            $sample = "（見本）";
        }
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data = array();
            $row["DUMMY"] = "DUMMY";
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
    function getErrorCsvDownloadModel() {
        //項目
        $header = array(
              "MSGROW"    => "行番号"
            , "MSGREMARK" => "メッセージ"
        );
        $duplicateHeader = array( "SCHREGNO"                 => "学籍番号"
                                , "YEAR"                     => "年度"
        );
        $contents  = "";
        if ($this->subField["HEADER"] == "on") {
            $tables = array();
            $tables = array("1" => "AFT_SCHREG_CONVERT_SCORE_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data = array();
            // $row["DUMMY"] = "DUMMY";
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
?>
