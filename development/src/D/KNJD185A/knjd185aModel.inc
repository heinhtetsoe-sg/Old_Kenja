<?php

require_once('for_php7.php');

class knjd185aModel extends Model
{
    public $field = array();
    public $cmd;

    public function knjd185aModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "TESTCD"           =>  VARS::post("TESTCD"),           //テストCD
                             "SUMMARY_DATE"     =>  VARS::post("SUMMARY_DATE"),     //異動対象日付及び出欠集計日付
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //発行日付
                             "TESTSUBCLASSONLY" =>  VARS::post("TESTSUBCLASSONLY"), //試験科目のみ出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
