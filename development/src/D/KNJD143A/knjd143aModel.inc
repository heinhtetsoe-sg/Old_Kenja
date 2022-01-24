<?php

require_once('for_php7.php');


class knjd143aModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $gradehrclass = "";

    /**
     * メンバ変数の初期化
     */
    function knjd143aModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        //
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        if(VARS::get("SEMESTER")) {
            $this->field["SEMESTER"] = VARS::get("SEMESTER");
        }
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        $this->cmd = VARS::request("cmd");
        //講座コード
        if($this->cmd != "subclasscd" && VARS::post("GRADE_HR_CLASS")){
            $this->field["GRADE_HR_CLASS"] = VARS::post("GRADE_HR_CLASS");
        }
        $this->field["HEADER"] = "on";

        $this->conduct_eval_moji = 28;
        $this->conduct_eval_gyo = 10;

        $db = Query::dbCheckOut();
        $z010 = $db->getOne(knjd143aQuery::getZ010());
        if ($z010 == 'sagaken') {
            $pattern = $db->getOne(knjd143aQuery::getPattern($this));
            if ($this->field["SCHOOL_KIND"] == 'P') {
                if ($pattern == 'E1' || $pattern == 'E2') {
                    $this->conduct_eval_moji = 35;
                    $this->conduct_eval_gyo = 5;
                }
            } else if ($this->field["SCHOOL_KIND"] == 'H') {
                if ($pattern == 'G1' || $pattern == 'G2') {
                    $this->conduct_eval_moji = 18;
                    $this->conduct_eval_gyo = 4;
                }
            }
        }

        Query::dbCheckIn($db);

        if(VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if(VARS::get("GRADE_HR_CLASS")) {
            $this->gradehrclass = VARS::get("GRADE_HR_CLASS");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $conductEval        = "CONDUCT_EVAL-".$i;
            $grad_value         = "GRAD_VALUE-".$i;
            $chk_calc_credit    = "CHK_CALC_CREDIT-".$i;
            $this->fields["CONDUCT_EVAL"][]   = VARS::post($conductEval);
            $this->fields["GRAD_VALUE"][]       = VARS::post($grad_value);
        }
        
    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {

            if ($this->validate_row_cnt($this->fields["CONDUCT_EVAL"][$i], ($this->conduct_eval_moji * $this->conduct_eval_gyo)) > $this->conduct_eval_gyo) {
                $this->setWarning("MSG915","( 実施・評価は{$this->conduct_eval_gyo}行までです )");
                 // Add by PP for textarea_cursor start 2020/01/20
                    $this->message915="CONDUCT_EVAL-".$i; 
                 // Add by PP for textarea_cursor end 2020/01/31
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd143aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
