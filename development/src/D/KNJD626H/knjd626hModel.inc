<?php

require_once('for_php7.php');

class knjd626hModel extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjd626hModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->field = array("SEMESTER"   =>  VARS::post("SEMESTER"),   //学期
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
