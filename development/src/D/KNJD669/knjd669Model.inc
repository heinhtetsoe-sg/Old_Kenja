<?php

require_once('for_php7.php');

class knjd669Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjd669Model() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKIND_ITEMCD"  =>  VARS::post("TESTKIND_ITEMCD"),  //テスト種別
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),         //コースグループ
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //印刷日付
                             "IDOU_DATE"        =>  VARS::post("IDOU_DATE"),        //異動対象日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean() {}
}
?>