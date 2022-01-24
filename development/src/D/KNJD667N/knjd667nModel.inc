<?php

require_once('for_php7.php');

class knjd667nModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $backSemester;

    function knjd667nModel() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKIND_ITEMCD"  =>  VARS::post("TESTKIND_ITEMCD"),  //テスト種別
                             "COURSECODE"       =>  VARS::post("COURSECODE"),       //コース
                             "DATE_FROM"        =>  VARS::post("DATE_FROM"),        //出欠集計開始日付
                             "DATE_TO"          =>  VARS::post("DATE_TO"),          //出欠集計終了日付
                             "BORDERVAL"        =>  VARS::post("BORDERVAL"),        //出力条件
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->backSemester = VARS::post("HID_SEMESTER");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
