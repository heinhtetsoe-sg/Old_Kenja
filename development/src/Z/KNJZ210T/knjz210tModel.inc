<?php

require_once('for_php7.php');

class knjz210tModel extends Model
{
    var $cmd;
    var $semester = "";
    var $testcd = "";
    var $record_div = "";
    var $subclasscd = "";
    var $ctrl;
    var $field = array();
    //教育課程対応
    var $classcd = "";
    var $school_kind = "";
    var $curriculum_cd = "";

    function knjz210tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->semester);
            unset($this->testcd);
            unset($this->record_div);
            unset($this->subclasscd);
        }

        if (VARS::post("SEMESTER") != ""){
            $this->semester = VARS::post("SEMESTER");
        }
        if (VARS::post("SEMESTER_2") != ""){
            $this->semester_2 = VARS::post("SEMESTER_2");
        }

        if (VARS::post("TESTCD") != ""){
            $this->testcd = VARS::post("TESTCD");
        }
        if (VARS::post("TESTCD_2") != ""){
            $this->testcd_2 = VARS::post("TESTCD_2");
        }

        if (VARS::post("RECORD_DIV") != ""){
            $this->record_div = VARS::post("RECORD_DIV");
        }

        if (VARS::post("SUBCLASSCD") != ""){
            $this->subclasscd = VARS::post("SUBCLASSCD");
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::post("CLASSCD") != ""){
                $this->classcd = VARS::post("CLASSCD");
            }
            if (VARS::post("SCHOOL_KIND") != ""){
                $this->school_kind = VARS::post("SCHOOL_KIND");
            }
            if (VARS::post("CURRICULUM_CD") != ""){
                $this->curriculum_cd = VARS::post("CURRICULUM_CD");
            }
        }


        $this->field = array("SUBCLASSCD"   => VARS::post("SUBCLASSCD"),
                            //教育課程用
                             "CLASSCD"      => VARS::post("CLASSCD"),
                             "SCHOOL_KIND"  => VARS::post("SCHOOL_KIND"),
                             "CURRICULUM_CD"=> VARS::post("CURRICULUM_CD"),
                             
                             "AAA"          => VARS::post("AAA"),
                             "CHAIRCD"      => VARS::post("selectdata"),
                             "DBNAME"       => VARS::post("DBNAME"));

        if (VARS::request("SUBCLASSCD") != ""){
            $this->subclasscd = VARS::request("SUBCLASSCD");
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("CLASSCD") != ""){
                $this->classcd = VARS::request("CLASSCD");
            }
            if (VARS::request("SCHOOL_KIND") != ""){
                $this->school_kind = VARS::request("SCHOOL_KIND");
            }
            if (VARS::request("CURRICULUM_CD") != ""){
                $this->curriculum_cd = VARS::request("CURRICULUM_CD");
            }
        }

        $this->semester   = ($this->semester) ? $this->semester : CTRL_SEMESTER;
        $this->semester_2 = ($this->semester_2) ? $this->semester_2 : CTRL_SEMESTER;

        $this->cmd = VARS::request("cmd");

        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "update") {
            //必須入力チェック
            if (!$this->semester || !$this->subclasscd || !$this->testcd ) {
                $this->setWarning("MSG304");
                return false;
            }
            if (!$this->record_div) {
                $this->setWarning("MSG304");
                return false;
            }
        }

        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            //必須入力チェック
            if (!$this->semester || !$this->testcd || !$this->record_div) {
                $this->setWarning("MSG304");
                return false;
            }

            //存在チェック（コピー元）
            $cnt_from = get_count($db->getCol(knjz210tQuery::SelectQuery($this, $this->record_div)));
            if($cnt_from == 0){
                $this->setWarning("MSG917","コピー元のデータが存在しません。");
                return false;
            }

            //存在チェック（コピー先）
            $record_div = ($this->record_div == "2") ? "1" : "2";
            $cnt_to = get_count($db->getCol(knjz210tQuery::SelectQuery($this, $record_div)));
            if($cnt_to > 0){
                $this->setWarning("MSG917","コピー先にデータが存在します。");
                return false;
            }
            Query::dbCheckIn($db);
        }

        if ($this->cmd == "copy_2") {
            $db = Query::dbCheckOut();
            //必須入力チェック
            if (!$this->semester || !$this->testcd || !$this->semester_2 || !$this->testcd_2) {
                $this->setWarning("MSG304");
                return false;
            }

            //存在チェック（コピー元）
            $cnt_from = $db->getOne(knjz210tQuery::copy_check($this->semester, $this->testcd));
            if($cnt_from == 0){
                $this->setWarning("MSG917","コピー元のデータが存在しません。");
                return false;
            }

            //存在チェック（コピー先）
            $cnt_to = $db->getOne(knjz210tQuery::copy_check($this->semester_2, $this->testcd_2));
            if($cnt_to > 0){
                $this->setWarning("MSG917","コピー先にデータが存在します。");
                return false;
            }
            Query::dbCheckIn($db);
        }

        return true;
    }

    //更新
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjz210tQuery::UpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー
    function &getCopyModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $result    = $db->query(knjz210tQuery::getCopyData($this));  //コピー元データ取得
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knjz210tQuery::copyInsert($row, $this));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー学期
    function &getCopyModel_2()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $result    = $db->query(knjz210tQuery::getCopyData_2($this));  //コピー元データ取得
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knjz210tQuery::copyInsert_2($row, $this));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
