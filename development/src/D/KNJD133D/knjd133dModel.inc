<?php

require_once('for_php7.php');

class knjd133dModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $replace_data = array();    //置換で選択された生徒一覧
    var $chaircd = "";
    var $file;
    var $itemMst = array();

    function knjd133dModel() {}

    function init() {

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->semester);
            unset($this->datadiv);
            common::GetControlMaster_Fnc($this->control);
        }

        //学期
        if (VARS::request("SEMESTER")) {
            $this->semester = VARS::request("SEMESTER");
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if($this->cmd != "subclasscd" && VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //コメント
        $this->moji = "55";
        $this->gyou = "3";

        //初期化
        $this->fields = array();
    }

    //入力項目チェック
    function validateInput() {
        foreach ($this->data["SCHREGNO"] as $schregno) {
            $remarkKey      = "REMARK_".$schregno;
            $attendNameKey  = "ATTENDO_NAME_".$schregno;
            $chkPatternKey  = "PATTERN_CD_".$schregno;
            $this->fields[$remarkKey]       = VARS::post($remarkKey);
            $this->fields[$attendNameKey]   = VARS::post($attendNameKey);
            $this->fields[$chkPatternKey]   = VARS::post($chkPatternKey);

            if ($this->validate_row_cnt($this->fields[$remarkKey], ($this->moji * 2)) > $this->gyou) {
                $this->setWarning("MSG915","「定型文」は{$this->gyou}行までです。 ({$this->fields[$attendNameKey]})");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjd133dQuery::update($this);
            $this->setMessage("MSG201");
        }
    }

}
?>
