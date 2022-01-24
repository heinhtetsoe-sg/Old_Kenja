<?php

require_once('for_php7.php');

class knjc039cModel extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;
    var $schregno = "";

    /**
     * メンバ変数の初期化
     */
    function knjc039cModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->schregno);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "COLLECTION_CD"    =>  VARS::post("COLLECTION_CD"),    //集計結果
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //KNJC039Bからコールされた時
        if (VARS::get("SEND_PRGID")) {
            $this->field["SCHOOL_KIND"]     = VARS::get("SEND_SCHOOL_KIND");
            $this->field["COLLECTION_CD"]   = VARS::get("SEND_COLLECTION_CD");
            $this->field["GRADE"]           = VARS::get("SEND_GRADE");
            $this->field["GRADE_HR_CLASS"]  = VARS::get("SEND_HR_CLASS");
        }

        $this->getPro["ATTEND_REMARK"]["moji"] = 50;
        $this->getPro["ATTEND_REMARK"]["gyou"] = 1;

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if ($this->field["COLLECTION_CD"] == "") {
            $this->setWarning("MSG304", "( 集計結果 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->data["ATTEND_REMARK"][$i], ($this->getPro["ATTEND_REMARK"]["moji"] * 2)) > $this->getPro["ATTEND_REMARK"]["gyou"]) {
                $this->setWarning("MSG915","(出欠の備考は{$this->getPro["ATTEND_REMARK"]["moji"]}文字までです。学籍番号:{$this->data["SCHREGNO"][$i]})");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        //初期化
        $this->data["ATTEND_REMARK"] = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $this->data["ATTEND_REMARK"][] = VARS::post("ATTEND_REMARK-".$i);
        }
        if ($this->validateInput()) {
            knjc039cQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
