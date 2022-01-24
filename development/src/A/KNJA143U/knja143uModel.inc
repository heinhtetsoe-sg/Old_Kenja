<?php

require_once('for_php7.php');

class knja143uModel extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knja143uModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"            =>  VARS::post("SEMESTER"),             //学期
                             "SCHOOL_KIND"         =>  VARS::post("SCHOOL_KIND"),          //校種
                             "DISP"                =>  VARS::post("DISP"),                 //表示選択
                             "GRADE"               =>  VARS::post("GRADE"),                //学年選択
                             "GRADE_HR_CLASS"      =>  VARS::post("GRADE_HR_CLASS"),       //年組
                             "FROM_KIND"           =>  VARS::post("FROM_KIND"),            //radio(1:生徒証 2:通学路のみ)
                             "NEXT_YEAR_GRADE_FLG" =>  VARS::post("NEXT_YEAR_GRADE_FLG"),  //次年度チェックボックス
                             "ISSUE_DATE"          =>  VARS::post("ISSUE_DATE"),           //発行日
                             "LIMIT_DATE"          =>  VARS::post("LIMIT_DATE"),           //有効期限
                             "DBNAME"              =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
