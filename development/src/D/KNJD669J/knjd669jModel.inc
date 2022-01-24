<?php

require_once('for_php7.php');

class knjd669jModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjd669jModel() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "TESTKIND_ITEMCD"  =>  VARS::post("TESTKIND_ITEMCD"),  //テスト種別
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),         //コースグループ
                             "IDOU_DATE"        =>  VARS::post("IDOU_DATE"),        //異動対象日付
                             "DATE"             =>  VARS::post("DATE"),             //印刷日付
                             "PRINT_NAME"       =>  VARS::post("PRINT_NAME"),       //生徒氏名・順位を表示する
                             "NOT_PRINT_COUNT0" =>  VARS::post("NOT_PRINT_COUNT0"), //人数0を表示しない
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean() {}
}
?>
