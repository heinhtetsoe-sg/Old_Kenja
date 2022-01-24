<?php

require_once('for_php7.php');

class knjb1302Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjb1302Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"      =>  VARS::post("SEMESTER"),        //学期
                             "GRADE"         =>  VARS::post("GRADE"),           //学年
                             "DATE"          =>  VARS::post("DATE"),            //学籍処理日
                             "TAISYOU_MEIBO" =>  VARS::post("TAISYOU_MEIBO"),   //対象名簿
                             "KIJUN1"        =>  VARS::post("KIJUN1"),          //グループ設定をもとにする
                             "RIREKI_CODE"   =>  VARS::post("RIREKI_CODE"),    //履修登録日付
                             "FORM_SELECT"   =>  VARS::post("FORM_SELECT"),    //フォーム選択可変固定
                             "DBNAME"        =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (!$this->field["DATE"]) {
            $this->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
