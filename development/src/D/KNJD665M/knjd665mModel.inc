<?php

require_once('for_php7.php');

class knjd665mModel extends Model
{
    public $selectdata;
    public $selectdata2;
    public $cmd;
    public $control;        //コントロールマスタ

    public function knjd665mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($selectdata);
            unset($selectdata2);
        }

        $this->field = array("YEAR"                        =>  CTRL_YEAR,
                             "SCHOOL_KIND"                 =>  VARS::post("SCHOOL_KIND"),                  //校種
                             "GRADE"                       =>  VARS::post("GRADE"),                        //学年
                             "CLASSCD"                     =>  VARS::post("CLASSCD")                       //教科
                             );

        $this->selectdata = array();
        $this->selectdata2 = array();

        if (VARS::post("selectdata")) {
            $this->selectdata = explode(",", VARS::post("selectdata"));
        }
        if (VARS::post("selectdata2")) {
            $this->selectdata2 = explode(",", VARS::post("selectdata2"));
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
