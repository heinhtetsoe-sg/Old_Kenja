<?php

require_once('for_php7.php');

class knjf175bModel extends Model {

    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjf175bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SCHKIND"  =>  VARS::post("SCHKIND"),  //校種
                             "SDATE"    =>  VARS::post("SDATE"),    //対象日(開始)
                             "EDATE"    =>  VARS::post("EDATE"),    //対象日(終了)
                             "PRINT"    =>  VARS::post("PRINT"),    //欠席者一覧印刷
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean()
    {
    }
}
?>
