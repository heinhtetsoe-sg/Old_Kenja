<?php

require_once('for_php7.php');

class knjd155fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd155fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "TESTKIND_CD"          =>  VARS::post("TESTKIND_CD"),          //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        if ($this->field["SEMESTER"] == "2" || $this->field["SEMESTER"] == '' && CTRL_SEMESTER == '2') {
            $this->testCd = array("010101", // 中間考査
                                  "010201", // 夏期考査
                                  "020101"  // 期末考査
                                  );
        } else {
            $this->testCd = array("010101", // 中間考査
                                  "020101", // 期末考査
                                  "020201"  // 夏期考査
                                  );
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
