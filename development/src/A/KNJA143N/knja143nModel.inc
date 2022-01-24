<?php

require_once('for_php7.php');

class knja143nModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knja143nModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );
        $this->SELECTED_DATA = VARS::post("SELECTED_DATA");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }

    function &downloadCsvFile() {
        if ($this->SELECTED_DATA === ""){
            $this->setWarning("MSG914","出力対象一覧");
            return false;
        }
        
        //DB接続
        $db = Query::dbCheckOut();

        $chkcnt = $this->chkCsvData($db);
        if ($chkcnt > 0){
            $contents = $this->makeCsvData($db, $this);
            if ($contents !== ""){
                $fsubname = preg_preg_split("/,/", $contents, 2);
                $filename = $fsubname[0] .".csv";
                common::downloadContents($contents, $filename, "SJIS-win");
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
        $query = knja143nQuery::getStudentInfo($this, "COUNT");
        $result = $db->getOne($query);
        
        return $result;
    }
        

    function makeCsvData($db){
        $retval = "";
        //タイトル,日付
        $eyear = explode("-", CTRL_DATE);
        $retval .= $eyear[0]."年"."身分証明書,日付:".CTRL_DATE."\r\n";
        //項目名称
        $retval .= "生徒番号,中高,年,組,生徒名,住所,生年月日,路線,駅発,駅着,路線2,駅発2,駅着2,路線3,駅発3,駅着3,旧組" . "\r\n";
        //DB接続
        $db = Query::dbCheckOut();

        $query = knja143nQuery::getStudentInfo($this, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = "";
            $data  = $row['SCHREGNO'] . ",";
            $data .= $row['SCHOOL_KIND'] . ",";
            $data .= $row['GRADE'] . ",";
            $data .= $row['HR_CLASS_NAME'] . ",";
            $data .= $row['NAME'] . ",";
            $data .= $row['ADDR'] . ",";
            $data .= $row['BIRTHDAY'] . ",";
            $data .= $row['ROSEN_1'] . ",";
            $data .= $row['JOSYA_1'] . ",";
            $data .= $row['GESYA_1'] . ",";
            $data .= $row['ROSEN_2'] . ",";
            $data .= $row['JOSYA_2'] . ",";
            $data .= $row['GESYA_2'] . ",";
            $data .= $row['ROSEN_3'] . ",";
            $data .= $row['JOSYA_3'] . ",";
            $data .= $row['GESYA_3'] . ",";
            $data .= $row['G_OLD_HR_CLASS_NAME1'];

            $retval .= $data."\r\n";
        }
        return $retval;
    }
}
?>
