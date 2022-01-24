<?php

require_once('for_php7.php');

class knjd626iModel extends Model
{
    public $field  = array();
    public $cmd;
    public $selectleft;
    public $selectleftval;

    public function knjd626iModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->field = array("SEMESTER"   =>  VARS::post("SEMESTER"),   //学期
                             "GRADE"      =>  VARS::post("GRADE"),      //学年
                             "DISP"       =>  VARS::post("DISP"),       //指定ラジオボタン
                             "CLASSCD"    =>  VARS::post("CLASSCD"),    //教科
                             "SUBCLASSCD" =>  VARS::post("SUBCLASSCD"), //科目
                             "SEX"        =>  VARS::post("SEX"),        //男女
                             "TANTOU"     =>  VARS::post("TANTOU"),     //担当者
                             "S_CLASS"    =>  VARS::post("S_CLASS"),    //スモールクラス
                             "DBNAME"     =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd == 'change') {
            $this->selectleft = "";
        }
    }
}
