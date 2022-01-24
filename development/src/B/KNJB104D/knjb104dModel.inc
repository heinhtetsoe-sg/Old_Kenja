<?php

require_once('for_php7.php');

class knjb104dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;

    public function knjb104dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
    
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTCD"           =>  VARS::post("TESTCD"),           //試験
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),      //実施日
                             "PERIODCD"         =>  VARS::post("PERIODCD"),         //校時
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
