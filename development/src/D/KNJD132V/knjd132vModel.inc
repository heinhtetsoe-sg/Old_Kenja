<?php

require_once('for_php7.php');

class knjd132vModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $schregno = "";

    /**
     * メンバ変数の初期化
     */
    public function knjd132vModel()
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

        if ($this->Properties["Knjd132v_Teikei_Button_Hyouji"] == "1") {
            //定型文選択
            if (VARS::get("SEM") != "") {
                $this->sem = VARS::get("SEM");
            }
            if (VARS::get("SCHNO") != "") {
                $this->schno = VARS::get("SCHNO");
            }
            if (VARS::get("ROWNO") != "") {
                $this->rowNo = VARS::get("ROWNO");
            }
            $this->subField["GRADE"] = VARS::post("GRADE");
        }

        //年組より校種を取得
        $db = Query::dbCheckOut();
        $grade = !strlen($this->field["GRADE_HR_CLASS"]) ? "" : substr($this->field["GRADE_HR_CLASS"], 0, 2);
        $query = knjd132vQuery::getSchoolKind($grade);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        Query::dbCheckIn($db);

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 50;
            $this->getPro["COMMUNICATION"]["gyou"] = 3;
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

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["COMMUNICATION"][$i], ((int)$this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
                $this->setWarning("MSG915", "(通信欄は{$this->getPro["COMMUNICATION"]["gyou"]}行までです。学籍番号:{$this->data["SCHREGNO"][$i]})");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "COMMUNICATION"."-".$i;
            $this->fields["COMMUNICATION"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd132vQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);