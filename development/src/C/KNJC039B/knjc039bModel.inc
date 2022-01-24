<?php

require_once('for_php7.php');

class knjc039bModel extends Model {
    var $field       = array();
    var $cmd;
    var $sepa;
    var $level;
    var $selectdata;

    function knjc039bModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;//年度
        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),         //組
                             "COLLECTION_CD"    =>  VARS::post("COLLECTION_CD")     //集計単位
                             );

        $this->field["GRADE_ALL"]       = "00";
        $this->field["HR_CLASS_ALL"]    = "000";
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    function validateInput() {
        //校種ごとの出欠コードがあるか
        $db = Query::dbCheckOut();
        $cnt1 = $db->getOne(knjc039bQuery::getNamecdCnt($this->year, "C{$this->field["SCHOOL_KIND"]}01"));
        $cnt6 = $db->getOne(knjc039bQuery::getNamecdCnt($this->year, "C{$this->field["SCHOOL_KIND"]}06"));
        $cnt7 = $db->getOne(knjc039bQuery::getNamecdCnt($this->year, "C{$this->field["SCHOOL_KIND"]}07"));
        $this->C001 = ($cnt1 > 0) ? "C{$this->field["SCHOOL_KIND"]}01" : "C001";
        $this->C006 = ($cnt6 > 0) ? "C{$this->field["SCHOOL_KIND"]}06" : "C006";
        $this->C007 = ($cnt7 > 0) ? "C{$this->field["SCHOOL_KIND"]}07" : "C007";

        //備考生成エラー処理　生成対象データが存在するか？
        $data_cnt = $db->getOne(knjc039bQuery::getExistsDataCnt($this));
        Query::dbCheckIn($db);
        if ($data_cnt == 0) {
            $this->setWarning("生成対象データはありません。");
            return false;
        }

        return true;
    }

    /****************/
    /* アップデート */
    /****************/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjc039bQuery::updAttendReasonCollectionDat($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
