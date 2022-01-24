<?php

require_once('for_php7.php');

class knja210aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knja210aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //権限チェック
        $this->auth = common::SecurityCheck(STAFFCD, 'KNJA233A');   //更新可能
        $this->year = CTRL_YEAR;

        $this->field = array("SEMESTER"      =>  VARS::post("SEMESTER"),     //学期
                             "CLASSCD"       =>  VARS::post("CLASSCD"),      //教科コード
                             "SUBCLASSCD"    =>  VARS::post("SUBCLASSCD"),   //科目コード
                             "TAISYOU_DATE"  =>  VARS::post("TAISYOU_DATE"), //対象日
                             "SCHOOLNAME"    =>  VARS::post("SCHOOLNAME"),   //出身学校
                             "DATE"          =>  VARS::post("DATE"),         //作成日
                             "PRINT_INFO"    =>  VARS::post("PRINT_INFO"),   //出力情報(出身学校/ふりがな)
                             "DBNAME"        =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        if (!$this->field["TAISYOU_DATE"]) {
            $this->field["TAISYOU_DATE"] = str_replace("-", "/", $this->control["学籍処理日"]);
        }

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
