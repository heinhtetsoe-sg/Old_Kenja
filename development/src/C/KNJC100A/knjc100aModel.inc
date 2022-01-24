<?php

require_once('for_php7.php');

class knjc100aModel extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjc100aModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND"      => VARS::post("SCHOOL_KIND"),     //校種
                             "DISP"             => VARS::post("DISP"),            //表示選択
                             "GRADE_NAME"       => VARS::post("GRADE_NAME"),      //学年名
                             "START_MON_REPORT" => VARS::post("START_MON_REPORT"),//月報(開始日)
                             "END_MON_REPORT"   => VARS::post("END_MON_REPORT"),  //月報(終了日)
                             "START_DW_REPORT"  => VARS::post("START_DW_REPORT"), //日報・週報(開始日)
                             "END_DW_REPORT"    => VARS::post("END_DW_REPORT"),   //日報・週報(終了日)
                             "DBNAME"           => VARS::post("DBNAME")           //DB変更に備えて
        );
        if ($this->field["START_MON_REPORT"] == "") {
            $this->field["START_MON_REPORT"] = substr(CTRL_DATE, 5, 2);
        }
        if ($this->field["END_MON_REPORT"] == "") {
            $this->field["END_MON_REPORT"] = substr(CTRL_DATE, 5, 2);
        }

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
