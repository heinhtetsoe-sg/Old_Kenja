<?php

class knjh890Model extends Model {

    var $cmd;
    var $cmdSub;
    var $schregno;
    var $delchk;
    var $dividecd;
    var $auth = array();
    var $taitleSort = array();
    var $sort;
    var $staffsort;
    var $datesort;
    var $titlesort;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjh890Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル

        //DB接続
        $db = Query::dbCheckOut();

        $this->cmd      = VARS::request("cmd");
        $this->gakuseki = VARS::request("GAKUSEKI") != "" ? VARS::request("GAKUSEKI") : $this->gakuseki;
        
        $this->check = VARS::request("CHECK");

        $this->field = array(
                             "KUBUN"            =>  VARS::post("KUBUN"),
                             
                             "G_HR"             =>  VARS::post("G_HR"),
                             
                             "GHR_CHOICE"       =>  VARS::post("GHR_CHOICE"),
                             
                             );
        //学年かクラスかを切り替えたとき
        if($this->cmd == "change"){
            $this->field["GHR_CHOICE"] = "";
        }
        
        //ファイル
        $this->file = VARS::file("FILE");
        if($this->cmd == ""){
            $this->csvImport = array();
            $this->csvData = array();
            
            //エラーデータの保持用
            $this->errSchregno = array();
            $this->errKnjid = array();
            $this->errLoginid = array();
        }
        
        //DB切断
        Query::dbCheckIn($db);

    }
    
    //UPDATE
    function getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $this->insert = array();
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        if($this->Kubun == "0"){
            $table = "PV_SCHREG_MST";
            $fieldName = "SCHREGNO";
        }else{
            $table = "PV_STAFF_MST";
            $fieldName = "STAFFCD";
        }
        
        foreach($this->csvImport as $val){
            $this->insert[$fieldName] = $val;
            $this->insert["KNJID"] = $this->csvData[$val]["KNJID"];
            $this->insert["LOGINID"] = $this->csvData[$val]["LOGINID"];
            
            common::_update($table, "", "", $this->insert, $db, "2");
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("更新しました。");
        
        //取り込んだデータを空にする
        $this->csvImport = array();
        $this->csvData = array();

        return true;
    }
    
    //CSV出力
    function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $header_data = false;
        $contents = "学籍/職員番号,年,組,番,氏名,会員番号,ログインID\n";
        
        if($this->field["KUBUN"] == "1"){
            //職員のとき
            $query = knjh890Query::getStaffData($this->field);
        }else{
            if($this->field["G_HR"] != "" && $this->field["GHR_CHOICE"] != ""){
                //生徒のとき
                $query = knjh890Query::getSchregData($this->field);
            }
        }
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if($row["KNJID"] == ""){
                if($this->field["KUBUN"] == "1"){
                    //職員のとき
                    $contents .= $row["STAFFCD"].",,,,";
                    $contents .= $row["STAFFNAME"].",";
                }else{
                    //生徒のとき
                    $contents .= $row["SCHREGNO"].",";
                    $contents .= $row["GRADE"].",";
                    $contents .= $row["HR_CLASS"].",";
                    $contents .= $row["ATTENDNO"].",";
                    $contents .= $row["NAME"].",";
                }
                
                $contents .= ",";
                $contents .= "\n";
                
                $header_data = true;
            }
        }
        
        //DB切断
        Query::dbCheckIn($db);
        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","賢者+V会員割り当て");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //CSV取込
    public function getCsvImportModel()
    {
        $this->warning = "";
        $this->errData = array();
        $this->csvImport = array();
        $this->csvData = array();
        $data = array();

        if (is_uploaded_file($this->file["tmp_name"])) {
            $checkdata = array();
            $checkdata["SCHREGNO"] = array(8, "学籍/職員番号");
            $checkdata["KNJID"]   = array(10, "会員番号");
            $checkdata["LOGINID"] = array(10, "ログインID");
            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //保持用
            $schregno = array();
            $knjid = array();
            $loginid = array();

            //エラーデータの保持用
            $this->errSchregno = array();
            $this->errKnjid = array();
            $this->errLoginid = array();

            //桁数
            $keta = 8;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $primaryKey3   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1   = array('KNJID');
            $primaryKey2   = array('SCHREGNO');
            $primaryKey3   = array('LOGINID');
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    /*headerありにするなら*/
                    if ($line == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $buffer = str_replace("\"", "", $buffer);
                    $buffer = str_replace("↓", "\n", $buffer);
                    $tmp = explode(",", $buffer);


                    $data = array("SCHREGNO"        =>  sprintf("%0".$keta."d", $tmp[0]),
                                  "GRADE"           =>  $tmp[1],
                                  "HR_CLASS"        =>  $tmp[2],
                                  "ATTENDNO"        =>  $tmp[3],
                                  "NAME"            =>  $tmp[4],
                                  "KNJID"           =>  $tmp[5],
                                  "LOGINID"         =>  $tmp[6]);

                    if ($data["KNJID"] != "" || $data["LOGINID"] != "") {
                        $this->csvImport[] = $data["SCHREGNO"];
                        $this->csvData[$data["SCHREGNO"]]["KNJID"] = $data["KNJID"];
                        $this->csvData[$data["SCHREGNO"]]["LOGINID"] = $data["LOGINID"];
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
            fclose($fp);

            if (empty($this->csvData)) {
                $this->warning = "取込ファイルにデータが入っていません。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        $erraDatas = "";
        $headerDuplicate = array("KNJID"    => "会員番号",
                                 "STAFFCD"  => "職員番号",
                                 "SCHREGNO" => "学籍番号");
        $tables = array();
        $headerErr = array();
        if ($this->field["KUBUN"] == "1") {
            $tables = array("1" => "PV_STAFF_MST");
        } else {
            $tables = array("1" => "PV_SCHREG_MST");
        }
        $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
        }
        if (sizeof($this->errData) > 0) {
            $erraDatas .= "行番号　メッセージ"."\\n";
        }
        foreach ($this->errData as $errKey => $errVal) {
            $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
        }
        if ($erraDatas != "") {
            $this->warning = "MSG204". "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas;
            return false;
        } else {
            $this->warning = "MSG202". "\\n"."更新件数".get_count($temp_arr)."件";
        }
    }

    //エラーチェック
    public function errCheck($db, $data, $dataEach, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複CSV
        $existenceErrFlg    = false;
        $duplicateDbErrFlg  = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複CSV
        $existenceErrMsg    = "";
        $duplicateDbErrMsg  = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $existenceSep    = "";
        $duplicateDbSep  = "";

        $checkFlg = 0;
        for ($dateCnt = 1; $dateCnt <= 3; $dateCnt++) {
            $dupFlg1 = $this->duplicateCsvCheck(${"duplicateCsv$dateCnt"}, ${"primaryKey$dateCnt"}, $data);
            if ($dupFlg1[0] == 1) {
                if ($checkFlg == 0) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
                    $checkFlg = 1;
                }
                if ($dateCnt == 1) {
                    $this->errKnjid[] = $data["KNJID"];
                } elseif ($dateCnt == 2) {
                    $this->errSchregno[] = $data["SCHREGNO"];
                } else {
                    $this->errLoginid[] = $data["LOGINID"];
                }
            }
        }

        foreach ($dataEach as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("KNJID", "LOGINID"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("KNJID", "LOGINID"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //表示するデータのSCHREGNOまたはSTAFFCD
        if ($this->field["KUBUN"] == "1") {
            //職員のとき
            $result = $db->query(knjh890Query::getStaffData("", $data["SCHREGNO"]));
        } else {
            //生徒のとき
            $result = $db->query(knjh890Query::getSchregData("", $data["SCHREGNO"]));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->field["KUBUN"] == "1") {
                $row["SCHREG_STAFFCD"] = $row["STAFFCD"];
                $row["GRADE"] = "";
                $row["NAME"] = $row["STAFFNAME"];
            } else {
                $row["SCHREG_STAFFCD"] = $row["SCHREGNO"];
                if ($row["GRADE"] != "") {
                    $row["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
                } else {
                    $row["GRADE"] = "";
                }
            }
            if (!empty($this->csvData[$row["SCHREG_STAFFCD"]])) {
                //取り込んだデータから会員情報を取得する
                $query = knjh890Query::getMemberData($this->field["KUBUN"], $row["SCHREG_STAFFCD"], $this->csvData[$row["SCHREG_STAFFCD"]]);
                $mData = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if ($mData["KNJID"] == "") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "指定会員番号/ログインID");
                }

                if ($mData["SCHREGCNT"] > 0 || $mData["KNJCNT"] > 0 || $mData["LOGINCNT"] > 0) {
                    $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
                }
                if ($mData["USER_TYPE"] != $this->field["KUBUN"]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "会員種別");
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }
}
?>
