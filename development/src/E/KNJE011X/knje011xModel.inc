<?php

require_once('for_php7.php');

class knje011xModel extends Model {

    var $cmd;
    var $schregNos;
    var $inputValArray = array();
    var $field = array();
    var $setFieldData = array();
    var $schoolName; //学校名
    var $schoolCode; //学校コード

    function knje011xModel() {
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

        if ($this->schoolName == 'tottori') {
            $this->inputValArray["10"]     = array("LABEL" => "総合的な学習　活動内容",  "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "TOTALSTUDYACT"   , "MOJI_SU" => "41", "GYOU_SU" => "4");
            $this->inputValArray["11"]     = array("LABEL" => "総合的な学習　評価",      "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "TOTALSTUDYVAL"   , "MOJI_SU" => "41", "GYOU_SU" => "5");
            $this->inputValArray["12"]     = array("LABEL" => "総合的な学習　備考",      "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "REMARK"          , "MOJI_SU" => "41", "GYOU_SU" => "4");
        } else {
            $this->inputValArray["10"]     = array("LABEL" => "総合的な学習　活動内容",  "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "TOTALSTUDYACT"   , "MOJI_SU" => "41", "GYOU_SU" => "2");
            $this->inputValArray["11"]     = array("LABEL" => "総合的な学習　評価",      "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "TOTALSTUDYVAL"   , "MOJI_SU" => "41", "GYOU_SU" => "3");
            $this->inputValArray["12"]     = array("LABEL" => "総合的な学習　備考",      "TABLE_NM" => "HEXAM_ENTREMARK_HDAT", "FIELD_NM" => "REMARK"          , "MOJI_SU" => "41", "GYOU_SU" => "5");
        }
        $this->inputValArray["20"]         = array("LABEL" => "出欠の記録備考",          "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "ATTENDREC_REMARK", "MOJI_SU" => "5", "GYOU_SU" => "6");
        if ($this->Properties["tyousasyoTokuBetuFieldSize"] == '1') {
            $this->inputValArray["21"]     = array("LABEL" => "特別活動の記録",          "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "SPECIALACTREC"   , "MOJI_SU" => "15", "GYOU_SU" => "10");
        } else {
            $this->inputValArray["21"]     = array("LABEL" => "特別活動の記録",          "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "SPECIALACTREC"   , "MOJI_SU" => "11", "GYOU_SU" => "8");
        }
        if ($this->Properties["useSyojikou3"] == '1') {
            $this->inputValArray["22"]     = array("LABEL" => "指導上参考となる諸事項1", "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "TRAIN_REF1"      , "MOJI_SU" => "14", "GYOU_SU" => "5");
            $this->inputValArray["23"]     = array("LABEL" => "指導上参考となる諸事項2", "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "TRAIN_REF2"      , "MOJI_SU" => "14", "GYOU_SU" => "5");
            $this->inputValArray["24"]     = array("LABEL" => "指導上参考となる諸事項3", "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "TRAIN_REF3"      , "MOJI_SU" => "14", "GYOU_SU" => "5");
        } else {
            if ($this->Properties["tyousasyoTokuBetuFieldSize"] == '1') {
                $this->inputValArray["22"] = array("LABEL" => "指導上参考となる諸事項",  "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "TRAIN_REF"       , "MOJI_SU" => "58", "GYOU_SU" => "7");
            } else {
                $this->inputValArray["22"] = array("LABEL" => "指導上参考となる諸事項",  "TABLE_NM" => "HEXAM_ENTREMARK_DAT",  "FIELD_NM" => "TRAIN_REF"       , "MOJI_SU" => "41", "GYOU_SU" => "5");
            }
        }
    }

    //学校名取得
    function getSchoolName() {
        $query = knje011xQuery::getSchoolName();
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
            $query = knje011xQuery::getInsertQuery($db, $this, $schregNo, $setVal, $tableNm, $fieldNm);
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
