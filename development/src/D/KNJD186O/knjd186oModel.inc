<?php

require_once('for_php7.php');

class knjd186oModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd186oModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("DATE_DIV"         =>  VARS::post("DATE_DIV"),          //出欠集計範囲 1:累計 2:学期
                             "SDATE"            =>  VARS::post("SDATE"),             //出欠集計範囲 From
                             "EDATE"            =>  VARS::post("EDATE"),             //出欠集計範囲 To
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),             //年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),         //グループ区分
                             "ITEM_DIV"         =>  VARS::post("ITEM_DIV"),          //出力内容
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),          //順位の基準点
                             "PRINT_RANK"       =>  VARS::post("PRINT_RANK"),        //順位表記
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>