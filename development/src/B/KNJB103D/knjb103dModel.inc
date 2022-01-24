<?php

require_once('for_php7.php');

class knjb103dModel extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjb103dModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->field = array("SEMESTER"   =>  VARS::post("SEMESTER"),   //学期
                             "GRADE"      =>  VARS::post("GRADE"),      //学年
                             "TESTCD"     =>  VARS::post("TESTCD"),     //考査
                             "CLASSCD"    =>  VARS::post("CLASSCD"),    //教科
                             "SUBCLASSCD" =>  VARS::post("SUBCLASSCD"), //科目
                             "DBNAME"     =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
