<?php

require_once('for_php7.php');

class knjd135pModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $schregno = "";

    /**
     * メンバ変数の初期化
     */
    public function knjd135pModel()
    {
    }

    public function init()
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

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //年組より校種を取得
        $db = Query::dbCheckOut();
        $grade = !strlen($this->field["GRADE_HR_CLASS"]) ? "" : substr($this->field["GRADE_HR_CLASS"], 0, 2);
        $query = knjd135pQuery::getSchoolKind($grade);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        Query::dbCheckIn($db);

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]) {
            list($moji, $gyou) = split('\*', $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 20;
            $this->getPro["COMMUNICATION"]["gyou"] = 20;
        }
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$school_kind]) {
            list($moji, $gyou) = split('\*', $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$school_kind]);
            $this->getPro["ATTEND_STR"]["moji"] = (int)trim($moji);
            $this->getPro["ATTEND_STR"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTEND_STR"]["moji"] = 10;
            $this->getPro["ATTEND_STR"]["gyou"] = 1;
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        if (count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($cnt = 0; $cnt < count($this->data["SCHREGNO"]); $cnt++) {
            if ($this->validate_row_cnt($this->fields["COMMUNICATION"][$cnt], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
                $this->setWarning("MSG915", "(特記事項・その他は{$this->getPro["COMMUNICATION"]["gyou"]}行までです。学籍番号:{$this->data["SCHREGNO"][$cnt]})");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["ATTENDREC_REMARK"][$cnt], ($this->getPro["ATTEND_STR"]["moji"] * 2)) > $this->getPro["ATTEND_STR"]["gyou"]) {
                $this->setWarning("MSG915", "(出欠の特記事項は{$this->getPro["ATTEND_STR"]["gyou"]}行までです。学籍番号:{$this->data["SCHREGNO"][$cnt]})");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($cnt = 0; $cnt < count($this->data["SCHREGNO"]); $cnt++) {
            $term_name = "COMMUNICATION"."-".$cnt;
            $this->fields["COMMUNICATION"][] = VARS::post($term_name);
            $term_name = "ATTENDREC_REMARK"."-".$cnt;
            $this->fields["ATTENDREC_REMARK"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd135pQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
