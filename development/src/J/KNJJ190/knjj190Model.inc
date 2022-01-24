<?php

require_once('for_php7.php');

class knjj190Model extends Model {
    var $field      = array();
    var $selectdata = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjj190Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;
        $this->field = array("CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //対象一覧区分
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "OUTPUT"               =>  VARS::post("OUTPUT"),               //出力ラジオ 1:予定講座 2:講座
                             "RIREKI_CODE"          =>  VARS::post("RIREKI_CODE"),          //履修登録日付
                             "LIMIT_DATE"           =>  VARS::post("LIMIT_DATE"),           //提出期限
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}
}
?>