<?php

require_once('for_php7.php');

class knja143oModel extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knja143oModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "ISSUE_DATE"       =>  VARS::post("ISSUE_DATE"),       //発行日
                             "LIMIT_DATE_FROM"  =>  VARS::post("LIMIT_DATE_FROM"),  //有効期限(開始日)
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),       //有効期限
                             "PRINT_PAGE"       =>  VARS::post("PRINT_PAGE"),       //表/裏
                             "IMAGE_COLOR"      =>  VARS::post("IMAGE_COLOR"),      //画像色
                             "PAGE2_DIV"        =>  VARS::post("PAGE2_DIV"),        //裏面選択
                             "POROW"            =>  VARS::post("POROW"),            //行
                             "POCOL"            =>  VARS::post("POCOL"),            //列
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
