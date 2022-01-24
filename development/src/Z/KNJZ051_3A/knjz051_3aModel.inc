<?php

require_once('for_php7.php');

class knjz051_3aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $send_year;
    var $majorcd = "";
    var $coursecd = "";

    function knjz051_3aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SCHOOLDIV"    =>  VARS::post("SCHOOLDIV")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->year);
            unset($this->send_year);
        }
        $temp = VARS::get("SEND_YEAR");
        if (isset($temp)){
            $this->send_year = VARS::get("SEND_YEAR");
        }

        //年度が変更されたとき
        if (VARS::get("mode") == "2"){
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->year);
        }

        //左のフレームから取得
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }
        $temp = VARS::get("YEAR");
        if (isset($temp)){
            $this->year = VARS::get("YEAR");
        }
    }

    //更新処理
    function &getUpdateModel() {
        $this->update();
        $this->field["YEAR"] = $this->year;
        $this->setMessage("MSG201");
    }

    //更新
    function &update() {
        $result = knjz051_3aQuery::getUpdateQuery($this, $this->field);
    }
}
?>
