<?php

require_once('for_php7.php');

class knjl301dModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;

    function knjl301dModel(){}

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        $this->testdiv      = VARS::post("TESTDIV");
    }

    function &downloadCsvFile() {
        if ($this->testdiv === "" || $this->testdiv === 0){
            $this->setWarning("MSG914","受験種別");
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();

        $chkcnt = $this->chkCsvData($db);
        if ($chkcnt > 0){
            $contents = $this->makeCsvData($db, $this);
            if ($contents !== ""){
                $fsubname = preg_split("/,/", $contents, 2);
                $filename = $fsubname[0] .".csv";
                common::downloadContents($contents, $filename);
            }else{
                $this->setMessage("MSG310");
            }
        }else{
            $this->setMessage("MSG303");
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    function chkCsvData($db){
        $query = knjl301dQuery::getExamerList($this, "COUNT");
        $result = $db->getOne($query);
        
        return $result;
    }
        

    function makeCsvData($db){
        $retval = "";

        //受験種別名称取得
        $query = knjl301dQuery::getTtlNameMst($this->examyear, "L004");
        $result = $db->query($query);

        $testdivstr = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] === $this->testdiv) {
                $testdivstr = $row['LABEL'];
            }
        }
        //取得チェック
        if ($testdivstr === "") {
            return $retval;
        }

        //タイトル,日付
        $eyear = strval($this->examyear);
        $retval .= $eyear . "年度" . $testdivstr . "受験者確認名簿,日付:" . CTRL_DATE . "\r\n";
        //項目名称
        $retval .= "受験番号,氏名,氏名かな,性別,生年月日,志望類型,出身学校,卒業年度,浪人,保護者名,保護者名かな,続柄,郵便番号,住所,電話番号" . "\r\n";
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjl301dQuery::getExamerList($this, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = "";
            $data  = $row['EXAMNO'] . ",";
            $data .= $row['NAME'] . ",";
            $data .= $row['NAME_KANA'] . ",";
            $data .= $row['SEX'] . ",";
            if (!is_null($row['ERACD']) && !is_null($row['BIRTH_Y']) && !is_null($row['BIRTH_M']) && !is_null($row['BIRTH_D'])){
                $data .= $row['ERACD'] . $row['BIRTH_Y'] . "年" . $row['BIRTH_M'] . "月" . $row['BIRTH_D'] . "日" . ",";
            } else {
                $data .= ",";
            }
            $data .= $row['DESIREDIV'] . ",";
            $data .= $row['FINSCHOOL_NAME_ABBV'] . ",";
            $data .= $row['FS_GRDYEAR'] . ",";
            $data .= $row['PAST_STUDENT'] . ",";
            $data .= $row['GNAME'] . ",";
            $data .= $row['GKANA'] . ",";
            $data .= $row['REL_NAME'] . ",";
            $data .= $row['GZIPCD'] . ",";
            $data .= $row['GADDRESS1'] . $row['GADDRESS2'] . ",";
            $data .= $row['GTELNO'];

            $retval .= $data . "\r\n";
        }
        return $retval;
    }
}
?>
