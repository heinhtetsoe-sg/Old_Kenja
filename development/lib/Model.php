<?php

require_once('for_php7.php');

/**
 * モデル基底クラス
 */

require_once('PEAR.php');
require_once('Cache/DB.php');
require_once('DB.php');
require_once('Log.php');
require_once('PEAR.php');

class Model extends PEAR
{
    public $log;
    public $Properties = array(); //prgInfo.properties の値

    public function Model()
    {
        $this->initialize();
    }
    /**
     * Clears all datafields of the object and rebuild the internal blocklist
     *
     * @access  public
     * @see     free()
     */
    public function init()
    {
        ;
    }

    public function log($message, $priority = null)
    {
        $this->log->open();
        $this->log->log($message, $priority);
        $this->log->close();
    }

    public function initialize()
    {
        $this->PEAR("Model_Error");
        $this->log =& Log::factory("file", "/tmp/gaku/error.log");

        if ($cmd = VARS::request("cmd")) {
            $this->setCmd($cmd);
        }
        $this->init();
    }

    public function setCmd($cmd)
    {
        $this->cmd = $cmd;
    }

    public function setData($dataSet)
    {
        if (is_array($dataSet)) {
            foreach ($dataSet as $key => $val) {
                $this->data[$key] = $val;
            }
        }
    }

    public function setError(&$value)
    {
        $this->error = $value;
    }

    public static function isError($value = null, $code = null)
    {
        if ($value === null) {
            $value =& $this->error;
        }

        return PEAR::isError($value);
    }

    public function setWarning($code = null, $msg = '')
    {
        $this->warning = $this->errorMessage($code, $msg);
    }

    public function isWarning()
    {
        return (isset($this->warning)) ? $this->warning : "";
    }

    public function setMessage($code = null, $msg = '')
    {
        $this->message = $this->errorMessage($code, $msg);
    }

    public function isMessage()
    {
        return ($this->message) ? $this->message : "";
    }

    public function setAccessLogDetail($access_cd, $programId)
    {
        common::access_log_detail($access_cd, $_POST, $_GET, $programId);
    }

    public function &errorMessage($code, $msg = '')
    {
        $db = Query::dbCheckOut();
        
        $query = "SELECT * FROM MESSAGE_MST"
                ." WHERE MSG_CD  = '" .$code ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
//          Query::dbCheckIn($db);
        $row["MSG_CONTENT"] = str_replace("\r", "\\r", $row["MSG_CONTENT"]);
        $row["MSG_CONTENT"] = str_replace("\n", "\\n", $row["MSG_CONTENT"]);
        return $code ."\\r\\n\\r\\n" .$row["MSG_CONTENT"] ."\\r\\n" .$msg;
    }

    public function &getModel($tttt = '')
    {
        $log = date("Y年m月d日 H時i分s秒")."[".STAFFCD."]:".$_SERVER['PHP_SELF']. " [ " . getmypid() . " ]\n";
        $fp = fopen("/tmp/pid.log", "a");
        fwrite($fp, $log);
        fclose($fp);

        global $app;
        if (! is_object(@$app->data["_ModelClassInstance"])) {
            if ($tttt != '') { 
                $class = $tttt->ModelClassName;//空->error
                if (!class_exists($class)) {
                    return false;
                }
                $num = func_num_args();
                $args_array = func_get_args();
                $sep = $args = "";
                for ($i=0; $i<$num; $i++) {
                    $args .= $sep."\$args_array[$i]";
                    $sep = ",";
                }
                eval("\$app->data[\"_ModelClassInstance\"] = new $class($args);");
                $app->data["_ModelClassInstance"]->initialize();
            } else {
                $class = $this->ModelClassName;
                if (!class_exists($class)) return false;
                $num = func_num_args();
                $args_array = func_get_args();
                $sep = $args = "";
                for ($i=0; $i<$num; $i++) {
                    $args .= $sep."\$args_array[$i]";
                    $sep = ",";
                }
                eval("\$app->data[\"_ModelClassInstance\"] =& new $class($args);");
                $app->data["_ModelClassInstance"]->initialize();
            }
        }
        return $app->data["_ModelClassInstance"];
    }

    public function _Model()
    {
    }

    public function __wakeup()
    {
        $this->initialize();
    }

    public function validate_row_cnt()
    {
        if (func_num_args() != 2) {
            echo '共通関数validate_row_cnt()<br>引数を2つだけ渡してください。';
            die();
        }
        if (!is_string(func_get_arg(0)) && !is_null(func_get_arg(0))) {
            echo '共通関数validate_row_cnt()<br>第1引数は文字列でなければなりません。';
            die();
        }
        if (!is_int(func_get_arg(1))) {
            echo '共通関数validate_row_cnt()<br>第2引数は整数でなければなりません。';
            die();
        }

        $string = func_get_arg(0); //入力された文字
        $itigyou_no_mojiLen = func_get_arg(1);

        //改行コードで区切って配列に入れていく
        $string = str_replace("\r\n", "\n", $string);
        $string = str_replace("\r", "\n", $string);
        $stringArray = preg_split("/\n/", $string);

        $hankakuKana = array("ｱ", "ｲ", "ｳ", "ｴ", "ｵ",
                             "ｶ", "ｷ", "ｸ", "ｹ", "ｺ",
                             "ｻ", "ｼ", "ｽ", "ｾ", "ｿ",
                             "ﾀ", "ﾁ", "ﾂ", "ﾃ", "ﾄ",
                             "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ",
                             "ﾊ", "ﾋ", "ﾌ", "ﾍ", "ﾎ",
                             "ﾏ", "ﾐ", "ﾑ", "ﾒ", "ﾓ",
                             "ﾔ", "ﾕ", "ﾖ",
                             "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ",
                             "ﾜ", "ｦ", "ﾝ",
                             "ｧ", "ｨ", "ｩ", "ｪ", "ｫ",
                             "ｬ", "ｭ", "ｮ", "ｯ", "ｰ",
                             "ﾞ", "ﾟ", "｡", "｢", "｣", "､", "･");
        $row_cnt = 0;

        if ($itigyou_no_mojiLen) {
            //改行コードが現れるまでに何行消費するか数える
            foreach ($stringArray as $value) {
                $mojiLen = 0;
                $mojisu = mb_strlen($value);
                for ($i = 0; $i < $mojisu; $i++) {
                    $hitoMoji = mb_substr($value, $i, 1);
                    if (in_array($hitoMoji, $hankakuKana)) {
                        $mojiLen += 1;
                    } else {
                        $mojiLen += strlen($hitoMoji) > 1 ? 2 : 1;
                    }
                }
                $amari = $mojiLen % (int)$itigyou_no_mojiLen;
                $gyousu = ($mojiLen - $amari) / (int)$itigyou_no_mojiLen;
                if ($amari > 0) {
                    $gyousu++;
                }
                if ($gyousu) {
                    $row_cnt += $gyousu;
                } else {
                    $row_cnt++;
                }
            }
        }
        return $row_cnt;
    }

    //プロパティーファイル取得
    public function getPropertiesAll()
    {
        $this->Properties = array();

        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        if ($fp) { //ファイルがあればファイルから読込む
            while ($line = fgets($fp, 1024)) {
                mb_language("Japanese");
                $line = mb_convert_encoding($line, "UTF-8", "auto");
                $line = preg_replace('/\r\n/', "\n", $line);
                $line = preg_replace('/\r/', "\n", $line);

                if (preg_match('/^(#|\n)|^ *$/', $line)) {
                    continue;
                }
                if (!trim($line)) {
                    continue;
                }
                preg_match('/^.*?=/', $line, $matches_key);
                preg_match('/=.*$/', $line, $matches_val);
                $key = preg_replace('/=/', '', $matches_key[0]);
                $key = trim($key);
                $val = preg_replace('/=/', '', $matches_val[0]);
                $val = trim($val);
                $this->Properties[$key] = $val;
            }
            fclose($fp);
        }

        $db = Query::dbCheckOut();
        $existsSql = " SELECT COUNT(*) FROM SYSCAT.TABLES WHERE TABNAME = 'PRGINFO_PROPERTIES_MST' ";
        if (0 < $db->getOne($existsSql)) {
            $query  = " SELECT ";
            $query .= " * ";
            $query .= " FROM ";
            $query .= "    PRGINFO_PROPERTIES_MST ";
            $query .= " ORDER BY ";
            $query .= "    SORT ";

            $db->expectError(); //エラーが出ても処理を止めない
            $result = $db->query($query);
            if (!PEAR::isError($result)) {
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $this->Properties[$row["NAME"]] = $row["VALUE"];
                }
            }
        }

        //$file_nameには「KNJD120A」とかが入る
        $file_name = $_SERVER['REQUEST_URI'];
        $file_name = preg_replace('/\/[^\/]+\.php.*$/', '', $file_name);
        $file_name = preg_replace('/^.*\//', '', $file_name);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PRGINFO_PROPERTIES ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$file_name}' ";
        $query .= "     AND VALUE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        $db->expectError(); //エラーが出ても処理を止めない
        $result = $db->query($query);
        if (!PEAR::isError($result)) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->Properties[$row["NAME"]] = $row["VALUE"];
            }
        }

        $staffQuery = " SELECT FIELD1, FIELD2, FIELD3 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);
        $this->Properties["FEP"] = $staffRow["FIELD3"];

        Query::dbCheckIn($db);
    }

    //対象校種取得
    public function getSelectSchoolKind()
    {
        if (VARS::get("PROGRAMID") != "") {
            $this->programid = VARS::get("PROGRAMID");
        }
        if (VARS::get("URL_SCHOOLKIND") != "") {
            $this->urlSchoolKind = VARS::get("URL_SCHOOLKIND");
            $this->urlSchoolCd = VARS::get("URL_SCHOOLCD");
            $this->mnId = VARS::get("MN_ID");
        }
        $this->selectSchoolKind = "";
        $query  = " SELECT ";
        $query .= "     SELECT_SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND = '{$this->urlSchoolKind}' ";
        $query .= "     AND PROGRAMID = '{$this->programid}' ";

        $db = Query::dbCheckOut();
        $db->expectError(); //エラーが出ても処理を止めない
        $result = $db->query($query);
        $sep = "";
        if (!PEAR::isError($result)) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->selectSchoolKind .= $sep.$row["SELECT_SCHOOL_KIND"];
                $sep = ":";
            }
        }
        Query::dbCheckIn($db);
    }

    //デフォルト値取得
    public function getPrgDefaultVal($prgId, $schoolCd, $schoolKind = "H")
    {
        $this->PrgDefaultVal = array();
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PRG_DEFAULT_VALUE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", $schoolCd))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND PROGRAMID = '{$prgId}' ";

        $db = Query::dbCheckOut();
        $db->expectError(); //エラーが出ても処理を止めない
        $result = $db->query($query);
        $sep = "";
        if (!PEAR::isError($result)) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->PrgDefaultVal[$row["OBJ_NAME"]] = $row["DEFAULT_VAL"];
            }
        }
        Query::dbCheckIn($db);
    }

    //デフォルト値設定
    public function getSetDefaultVal($value, $default, $prgDefault, $cmd)
    {
        $retVal = $value;
        if ($cmd != "") {
            return $retVal;
        }
        if (strlen($prgDefault) > 0) {
            $retVal = $value ? $value : $prgDefault;
        } else {
            $retVal = $value ? $value : $default;
        }
        return $retVal;
    }

    //デフォルト値セット
    public function updPrgDefaultVal($prgId, $schoolCd, $schoolKind = "H")
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PRG_DEFAULT_VALUE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", $schoolCd))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND PROGRAMID = '{$prgId}' ";

        $db->query($query);

        $updData = VARS::post("UPD_DEFAULT_VALUE");
        $updDataArray = explode("|", $updData);
        foreach ($updDataArray as $kye => $val) {
            list($objName, $setVal) = explode(":", $val);
            $data = array();
            $data["SCHOOLCD"]["TEXT"]       = sprintf("%012d", $schoolCd);
            $data["SCHOOL_KIND"]["TEXT"]    = $schoolKind;
            $data["PROGRAMID"]["TEXT"]      = $prgId;
            $data["OBJ_NAME"]["TEXT"]       = $objName;
            $data["DEFAULT_VAL"]["TEXT"]    = $setVal;
            $data["PROGRAMID"]["TEXT"]      = $prgId;
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "PRG_DEFAULT_VALUE_DAT");
            $db->query($query);
        }

        $this->setMessage("初期値の登録完了");

        Query::dbCheckIn($db);
    }

    public function getPrimaryKey($tableName)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   NAME ";
        $query .= " FROM ";
        $query .= "   SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "   TBNAME = '".$tableName."' ";
        $query .= "   AND KEYSEQ > 0 ";
        $query .= "   ORDER BY KEYSEQ ASC ";
        $result = $db->query($query);
        $primaryKey = array();
        if (!PEAR::isError($result)) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $primaryKey[] = $row["NAME"];
            }
        }
        return $primaryKey;
    }

    public function getDuplicateErrorHeader($programId, $tableName)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "   PROGRAMID = '".$programId."' ";
        $query .= "   AND MSGREMARK LIKE '%重複%' ";
        $count = $db->getOne($query);
        $primaryKeyHeader = array();
        $primaryKey = array();
        if ($count > 0) {
            foreach ($tableName as $key => $val) {
                $primaryKey = $this->getPrimaryKey($val);
                $sep = "";
                foreach ($primaryKey as $pkey => $pval) {
                    $primaryKeyHeader[$key] .= $sep.$pval;
                    $sep = ",";
                }
            }
        }
        return $primaryKeyHeader;
    }

    public function getDuplicateErrorHeaderNoSave($programId, $tableName, $errData)
    {
        $result = false;
        foreach ($errData as $val) {
            if (stripos($val["MSG"], "重複") !== false) {
                $result = true;
            }
        }
        $primaryKeyHeader = array();
        $primaryKey = array();
        if ($result) {
            foreach ($tableName as $key => $val) {
                $primaryKey = $this->getPrimaryKey($val);
                $sep = "";
                foreach ($primaryKey as $pkey => $pval) {
                    $primaryKeyHeader[$key] .= $sep.$pval;
                    $sep = ",";
                }
            }
        }
        return $primaryKeyHeader;
    }

    public function checkCsvErrQuery($programId)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "   PROGRAMID = '".$programId."' ";
        $errCnt = $db->getOne($query);
        Query::dbCheckIn($db);

        if ($errCnt > 0) {
            $this->setWarning("MSG204", "エラーCSVを出力して、ご確認下さい。");
            return false;
        } else {
            return true;
        }
    }

    public function selectCsvErrQuery($programId)
    {
        $query  = " SELECT ";
        $query .= "   MSGROW ";
        $query .= "   , MSGREMARK ";
        $query .= " FROM ";
        $query .= "   W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "   PROGRAMID = '".$programId."' ";

        return $query;
    }

    //エラーデータの削除
    public function deleteQueryErr($programId)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "   W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "   PROGRAMID = '".$programId."' ";

        return $query;
    }

    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        if ($firstMsg === "※重複(CSV):" || $firstMsg === "※重複(登録済):") {
            $errMsg .= $sep.$val;
            $sep = "，";
        } else {
            $errMsg .= "[".$val."]";
        }
        return false;
    }

    public function duplicateCsvCheck($duplicateCsv, $primaryKey, $data)
    {
        $dupFlg = 0;
        $dupLine = 0;
        foreach ($duplicateCsv as $dkey => $dval) {
            $dup = 1;
            foreach ($primaryKey as $pkey => $pval) {
                if ($dval[$pval] != $data[$pval]) {
                    $dup = 0;
                }
            }
            if ($dup == 1) {
                $dupFlg = 1;
                $dupLine = $dkey;
                break;
            }
        }
        return array($dupFlg, $dupLine);
    }

    public function duplicateCsvHeader($dupTable, $header)
    {
        $priDupHeader = "";
        $space = "";
        foreach ($dupTable as $key => $val) {
            $index = array();
            $index = explode(",", $val);
            $priDupHeader .= $space."〇重複". $key .":";
            foreach ($index as $ikey => $ival) {
                $priDupHeader .= "[".$header[$ival]."]";
            }
            $space = "　";
        }
        $headerErr = array("MSGROW"            => "",
        "MSGREMARK"         => $priDupHeader);

        return $headerErr;
    }

    public function checkErrLength($checkErr)
    {
        if (strlen($checkErr) > 120) {
            $checkErr = mb_strcut($checkErr, 0, 115);
            $checkErr .= "..他";
        }

        return $checkErr;
    }
}

class Model_Error extends PEAR_Error
{
    public function Model_Error(
        $code = null,
        $mode = PEAR_ERROR_RETURN,
        $level = E_USER_NOTICE,
        $debuginfo = null
    ) {
        $this->PEAR_Error(MODEL::errorMessage($code), $code, $mode, $level, $debuginfo);
    }
}

class Model_Warning extends PEAR_Error
{
    public function DB_Warning(
        $code = null,
        $mode = PEAR_ERROR_RETURN,
        $level = E_USER_NOTICE,
        $debuginfo = null
    ) {
        $this->PEAR_Error(MODEL::mbsubstr($code), $code, $mode, $level, $debuginfo);
    }
}
