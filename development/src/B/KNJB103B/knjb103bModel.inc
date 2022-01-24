<?php

require_once('for_php7.php');

class knjb103bModel extends Model
{
    public $field  = array();
    public $cmd;

    public function knjb103bModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"),//学期
                             "GRADE"    =>  VARS::post("GRADE"),   //学年
                             "TESTCD"   =>  VARS::post("TESTCD"),  //考査
                             "CHECK"    =>  VARS::post("CHECK"),   //出力条件
                             "DBNAME"   =>  VARS::post("DBNAME")   //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
