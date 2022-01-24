<?php

require_once('for_php7.php');
class knja111Model extends Model {
    var $cmd;
    var $GradeClass = "";
    var $schoolKind = "";
    var $schregno;
    var $control = array();     //コントロールマスタデータを保持

    function knja111Model() {}

    function init() {

        common::GetControlMaster_Fnc($this->control);

        if (VARS::get("PROGRAMID") != "") {
            $this->schregno = "";
            unset($this->GradeClass);
        }

        $this->cmd = VARS::request("cmd");

        if(VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            $this->GradeClass = VARS::get("GRADE").",".VARS::get("HR_CLASS");
        }

        //DB接続
        $db = Query::dbCheckOut();

        //学校種別
        $this->schoolKind = $db->getOne(knja111Query::getSchoolKind($this));

        //DB切断
        Query::dbCheckIn($db);

        if (VARS::get("init")) {
            $this->schregno = "";
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //削除用モデル
    function &getDeleteModel() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //削除処理
        if ($this->delete()) {
            $this->setMessage("MSG201");
            $this->schregno = "";
        }
        return true;
    }

    //削除処理
    function &delete() {
        $result = knja111Query::getDeleteQuery($this);
        return $result;
    }
}
?>
