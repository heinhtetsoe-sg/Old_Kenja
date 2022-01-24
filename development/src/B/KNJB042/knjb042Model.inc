<?php

require_once('for_php7.php');

class knjb042Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb042Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("JIKANWARI_SYUBETU"  =>  VARS::post("JIKANWARI_SYUBETU"),  //時間割種別
                             "TITLE"              =>  VARS::post("TITLE"),              //基本時間割
                             "SDATE"              =>  VARS::post("SDATE"),              //開始日付(通常時間割)
                             "EDATE"              =>  VARS::post("EDATE"),              //終了日付(通常時間割)
                             "GRADE"              =>  VARS::post("GRADE"),              //学年
                             "SUBCLASS_CHAIR_DIV" =>  VARS::post("SUBCLASS_CHAIR_DIV"), //出力項目(科目名・講座名)
                             "SYUTURYOKU_KOUMOKU" =>  VARS::post("SYUTURYOKU_KOUMOKU"), //出力項目
                             "DBNAME"             =>  VARS::post("DBNAME"),             //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
