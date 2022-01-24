<?php

require_once('for_php7.php');

class knjh111cModel extends Model {
    var $field      = array();
    var $cmd;
    var $sikakuCd = "";        //primary
    var $resultCd = "";        //primary
    var $selectdata;
    var $calenderFlg = "";

    function knjh111cModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->sikakuCd);
            unset($this->grade);
            unset($this->hrClass);
            unset($this->topTestDate);
            unset($this->topTestCd);
            unset($this->calenderFlg);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "" && $this->calenderFlg == 1) {
            $this->cmd = "edit";
        }

        if (VARS::request("GRADE") != "") {
            $this->grade = VARS::request("GRADE");
        }
        if (VARS::request("HR_CLASS") != "") {
            $this->hrClass = VARS::request("HR_CLASS");
        }
        if (VARS::request("SIKAKUCD") != "") {
            $this->sikakuCd = VARS::request("SIKAKUCD");
        }
        if (VARS::request("TOP_TEST_DATE") != "") {
            $this->topTestDate = VARS::request("TOP_TEST_DATE");
        }
        if (VARS::request("TOP_TEST_CD") != "") {
            $this->topTestCd = VARS::request("TOP_TEST_CD");
        }
        $db = Query::dbCheckOut(); //DB接続
        $setGrade    = $db->getOne(knjh111cQuery::getGrade($this));
        $this->grade = ($this->grade == "") ? $setGrade: $this->grade;

        $setHrClass  = $db->getOne(knjh111cQuery::GetHr_Class($this));
        $setSikakuCd = $db->getOne(knjh111cQuery::getQualifiedMst());
        $setTestCd   = $db->getOne(knjh111cQuery::getTestCd($this));
        $this->hrClass   = ($this->hrClass   == "") ? $setHrClass: $this->hrClass;
        $this->sikakuCd  = ($this->sikakuCd  == "") ? $setSikakuCd: $this->sikakuCd;
        $this->topTestCd = ($this->topTestCd == "") ? $setTestCd: $this->topTestCd;

        //学期開始、終了日付
        $this->sDate = $db->getOne(knjh111cQuery::getSEdate("SDATE"));
        $this->eDate = $db->getOne(knjh111cQuery::getSEdate("EDATE"));

        Query::dbCheckIn($db);     //DB切断

        $this->field = array("RIGHT_TEST_DATE"      =>  VARS::post("RIGHT_TEST_DATE"),        //試験日
                             "RIGHT_TEST_CD"        =>  VARS::post("RIGHT_TEST_CD")           //受験級
                             );

        $temp = VARS::get("RESULT_CD");
        if (isset($temp)) {
            $this->resultCd = VARS::get("RESULT_CD");
        }

        $temp = VARS::get("chFlg");
        if (isset($temp)) {
            $this->chFlg = VARS::get("chFlg");
        } else {
            $this->chFlg = 0;
        }

        //左リスト
        $this->selectdata = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (strlen($this->field["RIGHT_TEST_DATE"]) == 0 && $this->cmd == "update") {
            $this->setWarning("MSG301","(試験日)");
            return false;
        }
        if (strlen($this->field["RIGHT_TEST_CD"]) == 0 && $this->cmd == "update") {
            $this->setWarning("MSG301","(受験級)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->topTestDate = str_replace('/', '-', $this->field["RIGHT_TEST_DATE"]);
            $this->topTestCd   = $this->field["RIGHT_TEST_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        if ($this->selectdata) {
            $result = knjh111cQuery::getDeleteInsertQuery($this, $this->field);
        } else {
            $result = knjh111cQuery::getDeleteQuery($this);
        }
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
