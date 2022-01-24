<?php

require_once('for_php7.php');

class knjj144aModel extends Model {

    var $field = array();
    var $cmd;

    function knjj144aModel() {}

    function init() {
        $this->field = array(
            "NUMBER_OF_TIMES" => VARS::post("NUMBER_OF_TIMES"),
            "EVENT_NAME"      => VARS::post("EVENT_NAME"),
            "EVENT_DATE"      => VARS::post("EVENT_DATE"),
            "MAN_METERS"      => VARS::post("MAN_METERS"),
            "WOMEN_METERS"    => VARS::post("WOMEN_METERS")
        );

        $this->cmd = VARS::request("cmd");
    }


    function validateInput()
    {
        //文字数チェック(実施回数)
        if (strlen($this->field["NUMBER_OF_TIMES"]) > 15) {
            $this->setWarning("( 実施回数 ) 全角5文字まで");
            return false;
        }

        //文字数チェック(名称)
        if (strlen($this->field["EVENT_NAME"]) > 30) {
            $this->setWarning("( 名称 ) 全角10文字まで");
            return false;
        }

        //日付型チェック(実施日)
        if($this->field["EVENT_DATE"]){
            if (!preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $this->field["EVENT_DATE"])) {
                return false;
            }
        }

        //距離(男子)
        $man_meters = explode(".",$this->field["MAN_METERS"]);
        if (!isset($man_meters[1])) {
            //小数点がない場合
            if(strlen($man_meters[0]) > 2){
                $this->setWarning("( 距離(男子) ) 整数2桁まで");
                return false;
            }
        } else {
            //小数点がある場合
            $man_meters = explode(".",$this->field["MAN_METERS"]);
            //整数
            if(strlen($man_meters[0]) > 2){
                $this->setWarning("( 距離(男子) ) 整数2桁まで");
                return false;
            }
            //小数点
            if(strlen($man_meters[1]) > 3){
                $this->setWarning("( 距離(男子) ) 小数3桁まで");
                return false;
            }
        }

        //距離(女子)
        $women_meters = explode(".",$this->field["WOMEN_METERS"]);
        if (!isset($women_meters[1])) {
            //小数点がない場合
            if(strlen($women_meters[0]) > 2){
                $this->setWarning("( 距離(女子) ) 整数2桁まで");
                return false;
            }
        } else {
            //小数点がある場合
            //整数
            if(strlen($women_meters[0]) > 2){
                $this->setWarning("( 距離(女子) ) 整数2桁まで");
                return false;
            }
            //小数点
            if(strlen($women_meters[1]) > 3){
                $this->setWarning("( 距離(女子) ) 小数3桁まで");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjj144aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>