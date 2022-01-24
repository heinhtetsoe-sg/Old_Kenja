<?php

require_once('for_php7.php');

class knjd184kModel extends Model {
    var $field = array();
    var $cmd;

    function knjd184kModel() {}

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUMMARY_DATE"     =>  VARS::post("SUMMARY_DATE"),     //異動対象日付及び出欠集計日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),        //記載日付
                             "DESC_DATE_PRINT"  =>  VARS::post("DESC_DATE_PRINT"),  //記載日付を印刷する
                             "PRINT_FOOTER"     =>  VARS::post("PRINT_FOOTER"),     //課程修了を表示する
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                             "minHrClass"       =>  VARS::post("minHrClass")        //指定した学年の中で最小の年組を選ぶ
                     );

        $this->cmd = VARS::request("cmd");
        $predisp = VARS::post("predisp");

        //クラス・個人指定を変更した場合のフラグ
        $this->changeDispFlg = $predisp != $this->field["DISP"];
    }
}
?>
