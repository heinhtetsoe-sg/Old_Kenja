<?php

require_once('for_php7.php');

class knjp913Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $schregno = "";

    /**
     * メンバ変数の初期化
     */
    function knjp913Model() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->schregno);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "HR_CLASS"     =>  VARS::post("HR_CLASS"),     //年組
                             "SYORI_DIV"    =>  VARS::post("SYORI_DIV"),    //処理区分（1:繰越キャンセル、2:キャンセル取消）
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //学年より校種を取得
        $db = Query::dbCheckOut();
        $query = knjp913Query::getSchoolKind($this->field["GRADE"]);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        $this->schoolKind = $school_kind;
        Query::dbCheckIn($db);

        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregno = $this->data["SCHREGNO"][$i];
            $this->field["CHECK_".$schregno] = VARS::post("CHECK_".$schregno);
        }

    }

    //入力項目チェック
    function validateInput() {

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->updFields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregno = $this->data["SCHREGNO"][$i];
            foreach ($this->carryArr as $lmCd => $mName) {
                $term_name = "CARRY_OVER_MONEY"."-".$lmCd."-".$schregno;
                $this->updFields[$schregno][$lmCd] = VARS::post($term_name);
            }
        }

        if ($this->validateInput()) {
            knjp913Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
