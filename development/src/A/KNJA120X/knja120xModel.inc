<?php

require_once('for_php7.php');

class knja120xModel extends Model {

    var $cmd;
    var $schregNos;
    var $inputValArray = array();
    var $field = array();
    var $setFieldData = array();

    function knja120xModel()
    {
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

        $this->getPropertiesAll(); //プロパティファイル

        $this->inputValArray["10"] = array("LABEL" => "総合的な学習　活動内容", "TABLE_NM" => "HTRAINREMARK_HDAT",  "FIELD_NM" => "TOTALSTUDYACT"    , "MOJI_SU" => "44", "GYOU_SU" => "4");
        $this->inputValArray["11"] = array("LABEL" => "総合的な学習　評価",     "TABLE_NM" => "HTRAINREMARK_HDAT",  "FIELD_NM" => "TOTALSTUDYVAL"    , "MOJI_SU" => "44", "GYOU_SU" => "6");
        if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == '1') {
            $this->inputValArray["20"] = array("LABEL" => "特別活動所見",       "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "SPECIALACTREMARK" , "MOJI_SU" => "22", "GYOU_SU" => "10");
        } else {
            $this->inputValArray["20"] = array("LABEL" => "特別活動所見",       "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "SPECIALACTREMARK" , "MOJI_SU" => "11", "GYOU_SU" => "6");
        }
        if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == '1') {
            $this->inputValArray["30"] = array("LABEL" => "総合所見",           "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "TOTALREMARK"      , "MOJI_SU" => "66", "GYOU_SU" => "8");
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == '1') {
            $this->inputValArray["30"] = array("LABEL" => "総合所見",           "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "TOTALREMARK"      , "MOJI_SU" => "66", "GYOU_SU" => "7");
        } else {
            $this->inputValArray["30"] = array("LABEL" => "総合所見",           "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "TOTALREMARK"      , "MOJI_SU" => "44", "GYOU_SU" => "6");
        }
        $this->inputValArray["40"] = array("LABEL" => "出欠の記録備考",         "TABLE_NM" => "HTRAINREMARK_DAT",   "FIELD_NM" => "ATTENDREC_REMARK" , "MOJI_SU" => "20", "GYOU_SU" => "2");

        $this->cmd = VARS::request("cmd");
        $this->schregNos = VARS::request("schregNos");
    }

    //入力項目チェック
    function validateInput($schregNo, $checkVal, $fieldNm) {
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
            $errorFlg = !$this->validateInput($schregNo, $checkVal, $fieldNm) ? true : $errorFlg;
        }
        if ($errorFlg) {
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ($schArray as $key => $schregNo) {
            $setVal = VARS::post($fieldNm."-".$schregNo);
            $this->setFieldData[$schregNo] = $setVal;
            $query = knja120xQuery::getInsertQuery($db, $this, $schregNo, $setVal, $tableNm, $fieldNm);
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
