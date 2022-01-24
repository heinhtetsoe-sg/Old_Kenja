<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjg052aModel.inc 59328 2018-03-27 13:26:08Z maeshiro $

class knjg052aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $output;

    function knjg052aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "CMBCLASS"          =>  VARS::post("CMBCLASS"),         //学年・クラス
                            "NOTICEDAY"         =>  VARS::post("NOTICEDAY"),        //記載日付
                            "SEMESTER"          =>  VARS::post("SEMESTER"),         //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->output      = VARS::post("OUTPUT");

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
