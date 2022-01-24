<?php

require_once('for_php7.php');

class knjh714Model extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjh714Model()
    {
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND" => VARS::post("SCHOOL_KIND"), //校種
                             "SEMESTER"    => VARS::post("SEMESTER"),    //学期
                             "TESTNAME"    => VARS::post("TESTNAME"),    //学力テスト
                             "PERIODID"    => VARS::post("PERIODID"),    //時限
                             "DBNAME"      => VARS::post("DBNAME")       //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
