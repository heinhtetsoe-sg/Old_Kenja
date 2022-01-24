<?php

require_once('for_php7.php');

class knjd187vModel extends Model
{
    public $field = array();
    public $cmd;

    public function knjd187vModel()
    {
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->field = array("SEMESTER1"       =>  VARS::post("SEMESTER1"),  //学期コード
                             "SEMESTER2"       =>  VARS::post("SEMESTER2"),  //学期コード
                             "GRADE"           =>  VARS::post("GRADE"),      //年組
                             "HR_CLASS"        =>  VARS::post("HR_CLASS"),   //クラス
                             "DISP"            =>  VARS::post("DISP"),       //クラス生徒区分
                             "DIV"             =>  VARS::post("DIV"),        //中間期末区分
                             "DBNAME"          =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->selectdata   = VARS::post("CLSS_OR_STDNTS_SELECTED");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
    }
}
