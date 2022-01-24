<?php

require_once('for_php7.php');

class knjb101Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjb101Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"       =>  VARS::post("SEMESTER"),        //学期
                             "GRADE"          =>  VARS::post("GRADE"),           //学年
                             "DATE"           =>  VARS::post("DATE"),            //学籍処理日
                             "TAISYOU_MEIBO"  =>  VARS::post("TAISYOU_MEIBO"),   //対象名簿
                             "KIJUN1"         =>  VARS::post("KIJUN1"),          //グループ設定をもとにする
                             "DBNAME"         =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (!$this->field["DATE"]) {
            $this->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
