<?php

require_once('for_php7.php');

class knje020xModel extends Model {

    var $cmd;
    var $schregNos;
    var $inputValArray = array();
    var $field = array();
    var $setFieldData = array();
    var $schoolName; //学校名
    var $schoolCode; //学校コード

    function knje020xModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::request("cmd") == "") {
            unset($this->inputValArray);
        }

        $this->field = array("YEAR_SEME" =>  VARS::post("YEAR_SEME"),
                             "HR_CLASS"  =>  VARS::post("HR_CLASS"),
                             "INPUT_VAL" =>  VARS::post("INPUT_VAL")
                             );

        $this->cmd = VARS::request("cmd");
        $this->schregNos = VARS::request("schregNos");

        $this->getSchoolName(); //学校名取得
        $this->getPropertiesAll(); //プロパティファイル

        if ($this->Properties["tyousasyoTokuBetuFieldSize"] == '1') {
            $this->inputValArray["10"] = array("LABEL" => "特別活動の記録",       "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_REC"          , "MOJI_SU" => "20", "GYOU_SU" => "10");
            $this->inputValArray["11"] = array("LABEL" => "欠席の主な理由",       "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_ABSENCE"      , "MOJI_SU" => "10", "GYOU_SU" => "6");
            $this->inputValArray["13"] = array("LABEL" => "本人の長所・推薦事由", "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_RECOMMEND"    , "MOJI_SU" => "38", "GYOU_SU" => "13");
        } else {
            $this->inputValArray["10"] = array("LABEL" => "特別活動の記録",       "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_REC"          , "MOJI_SU" => "20", "GYOU_SU" => "8");
            $this->inputValArray["11"] = array("LABEL" => "欠席の主な理由",       "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_ABSENCE"      , "MOJI_SU" => "10", "GYOU_SU" => "4");
            $this->inputValArray["13"] = array("LABEL" => "本人の長所・推薦事由", "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_RECOMMEND"    , "MOJI_SU" => "38", "GYOU_SU" => "10");
        }
        $this->inputValArray["12"]     = array("LABEL" => "身体状況　備考",       "TABLE_NM" => "HEXAM_EMPREMARK_DAT", "FIELD_NM" => "JOBHUNT_HEALTHREMARK" , "MOJI_SU" => "14", "GYOU_SU" => "3");
    }

    //学校名取得
    function getSchoolName() {
        $query = knje020xQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput($schregNo, $checkVal)
    {
        $tableNm = $this->inputValArray[$this->field["INPUT_VAL"]]["TABLE_NM"];
        $fieldNm = $this->inputValArray[$this->field["INPUT_VAL"]]["FIELD_NM"];
        $moji_su = $this->inputValArray[$this->field["INPUT_VAL"]]["MOJI_SU"];
        $gyou_su = $this->inputValArray[$this->field["INPUT_VAL"]]["GYOU_SU"];
        $label   = $this->inputValArray[$this->field["INPUT_VAL"]]["LABEL"];

        $errorInfo = $this->schregInfo[$schregNo];
        if ($this->validate_row_cnt($checkVal, ((int)$moji_su * 2)) > (int)$gyou_su) {
            $this->setWarning("MSG915", "{$errorInfo}がエラーです。\\n({$label}は{$gyou_su}行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        $tableNm = $this->inputValArray[$this->field["INPUT_VAL"]]["TABLE_NM"];
        $fieldNm = $this->inputValArray[$this->field["INPUT_VAL"]]["FIELD_NM"];
        $schArray = preg_split("/,/", $this->schregNos);
        //エラーチェック
        $errorFlg = false;
        foreach ($schArray as $key => $schregNo) {
            $checkVal = VARS::post($fieldNm."-".$schregNo);
            $this->setFieldData[$schregNo] = $checkVal;
            $errorFlg = !$this->validateInput($schregNo, $checkVal) ? true : $errorFlg;
        }
        if ($errorFlg) {
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ($schArray as $key => $schregNo) {
            $setVal = VARS::post($fieldNm."-".$schregNo);
            $this->setFieldData[$schregNo] = $setVal;
            $query = knje020xQuery::getInsertQuery($db, $this, $schregNo, $setVal, $tableNm, $fieldNm);
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
