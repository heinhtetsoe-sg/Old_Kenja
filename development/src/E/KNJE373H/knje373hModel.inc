<?php

require_once('for_php7.php');

class knje373hModel extends Model
{
    public $field = array();
    public $schoolcd;
    public $cmd;

    public function knje373hModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOLDIV"    => VARS::post("SCHOOLDIV"),    //学校種別
                             "GRD_DIV"      => VARS::post("GRD_DIV"),      //在学・卒業
                             "DISTRICTCD"   => VARS::post("DISTRICTCD"),   //地区
                             "ENT_DIV"      => VARS::post("ENT_DIV"),      //1:上段：合格内定進路～ 2:上段：希望受験進路～
                             "DBNAME"       => VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->schoolcd = sprintf("%012d", SCHOOLCD);
        $this->cmd = VARS::request("cmd");
    }
}
