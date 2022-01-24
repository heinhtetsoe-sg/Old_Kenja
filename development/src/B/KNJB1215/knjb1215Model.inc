<?php

require_once('for_php7.php');

//定数定義
class knjb1215Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $leftCourse;
    var $classcd;
    var $school_kind;
    var $curriculum_cd;
    var $subclasscd;
    var $textbookcd;
    var $control;

    //メンバ変数の初期化
    function knjb1215Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "change") {
           unset($this->classcd);
           unset($this->school_kind);
           unset($this->curriculum_cd);
           unset($this->subclasscd);
        }

        if(VARS::get("PROGRAMID")){
           unset($this->year);
           $this->year = CTRL_YEAR;
           unset($this->leftCourse);
           unset($this->classcd);
           unset($this->school_kind);
           unset($this->curriculum_cd);
           unset($this->subclasscd);
           
        }

        unset($this->field);

        $this->field = array("TEXTBOOKCD" => VARS::post("TEXTBOOKCD"));

        if (VARS::request("YEAR")){
            $this->year = VARS::request("YEAR");
        }
        if (VARS::request("LEFT_COURSE")){
            $this->leftCourse = VARS::request("LEFT_COURSE");
        }
        if (VARS::get("CLASSCD")){
            $this->classcd = VARS::get("CLASSCD");
        }
        if (VARS::get("SCHOOL_KIND")){
            $this->school_kind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CURRICULUM_CD")){
            $this->curriculum_cd = VARS::get("CURRICULUM_CD");
        }
        if(VARS::get("SUBCLASSCD")){
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
    }

    //入力チェック
    function validateInput() {

        //リストから選択しているか
        if ($this->classcd == "" || $this->school_kind == ""  || $this->curriculum_cd == "" || $this->subclasscd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        
        return true;
    }

    //編集完了用のモデルを返します
    function getUpdateModel() {

        //入力チェック
        if ($this->validateInput()) {
            knjb1215Query::getDeleteQuery($this);
            
            $textbookcd = $this->field["TEXTBOOKCD"];
            $notDefault = $this->field["NOT_DEFAULT"];
            if (is_array($textbookcd)) {
                foreach ($textbookcd as $val) {
                    $notDefault = VARS::post("NOT_DEFAULT".$val);
                    knjb1215Query::getInsertQuery($this, $val, $notDefault);
                }
            }

            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }

        return;
    }

    //次年度作成
    function getCopyYearModel() {
        $flg = true;
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $this->nextYear = $this->year + 1;
        $message = '';

        if (!countRecordCount($db, "SUBCLASS_YDAT", $this->nextYear)) {
            $message .= '次年度の科目が設定されていません。\n';
            $flg = false;
        }
        if (!countRecordCount($db, "TEXTBOOK_YDAT", $this->nextYear)) {
            $message .= '次年度の教科書が設定されていません。\n';
            $flg = false;
        }
        if (countRecordCount($db, "SUBCLASS_TEXTBOOK_DAT", $this->nextYear)) {
            $message .= '既に次年度に購入教科書のデータが作成されています。';
            $flg = false;
        }
        if (!countRecordCount($db, "SUBCLASS_TEXTBOOK_DAT", $this->year)) {
            $message .= '作成するデータが存在しません。';
            $flg = false;
        }
        if ($flg) {
            $query = knjb1215Query::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}

//データ件数チェック
function countRecordCount($db, $tableName, $year) {
    $query = knjb1215Query::countRecordCount($tableName, $year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    } else {
        return false;
    }
}
?>
