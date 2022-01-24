<?php

require_once('for_php7.php');

class knjz218bModel extends Model {
    var $leftSemester;
    var $leftGrade;
    var $leftGroupDiv;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $selectSublass = array();
    var $cmd;

    function knjz218bModel()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");
        $this->sendFlg = VARS::get("SEND_FLG");

        if ($this->sendFlg) {
            $this->sendField = array("GROUPCD"          =>  VARS::get("GROUPCD")
                                     );
        }

        $this->field = array("GROUPCD"                  =>  VARS::post("GROUPCD"),
                             "GROUPNAME"                =>  VARS::post("GROUPNAME"),
                             "GROUPABBV"                =>  VARS::post("GROUPABBV"),
                             );

        $this->selectMock = array();
        $this->selectMock = explode(",", VARS::post("selectMock"));

        if (VARS::get("PROGRAMID")) {
            $this->clean();
        }
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
    }

    function &getCopyModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //去年度データ取得
        $lastYearCnt = knjz218bQuery::getCopyCheck($this, "LAST");

        //今年度データ取得
        $nowYearCnt = knjz218bQuery::getCopyCheck($this);

        //DB切断
        Query::dbCheckIn($db);

        //データ有無
        if ($lastYearCnt < 1){
            $this->setWarning("前年度データが、ありません。");
            return false;
        }

        //データ有無
        if ($nowYearCnt > 0){
            $this->setWarning("今年度データが、存在しています。");
            return false;
        }

        $this->copy();
        $this->setMessage("MSG201");

        return;
    }

    function &copy()
    {
        $result = knjz218bQuery::getCopyQuery($this);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {
        if (($this->cmd=="update" || $this->cmd=="delete") && $this->sendField["GROUPCD"] != $this->field["GROUPCD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //必須
        if (strlen(trim($this->field["GROUPCD"])) == 0) {
            $this->setWarning("MSG301","(グループCD)");
            return false;
        }
        if (strlen(trim($this->field["GROUPNAME"])) == 0) {
            $this->setWarning("MSG301","(グループ名)");
            return false;
        }

        if ($this->cmd=="add" || $this->cmd=="update") {

            //重複チェック
            $cnt = knjz218bQuery::getMockExistsCnt($this->field);
            if ($this->cmd == "add" && $cnt > 0) {
                $this->setMessage("MSG918", "(グループCD)");
                return false;
            }

            if (strlen($this->field["GROUPCD"]) > 4 ) {
                $this->setWarning("MSG915","(グループCDは4バイトまでです)");
                return false;
            } elseif (strlen($this->field["GROUPNAME"]) > 60){
                $this->setWarning("MSG915","(グループ名は60バイトまでです)");
                return false;
            } elseif (strlen($this->field["GROUPABBV"]) >30){
                $this->setWarning("MSG915","(グループ略称は30バイトまでです)");
                return false;
            }
        }

        if ($this->cmd=="delete") {
            $cnt = knjz218bQuery::getMockExistsCnt($this->field);
            if ($this->cmd == "delete" && $cnt < 1) {
                $this->setMessage("MSG918", "(グループCD)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();

            $this->clean();
            $this->setMessage("MSG201");
        }
        return;
    }

    function &insert()
    {

        $result = knjz218bQuery::getInsertQuery($this->field, $this->selectMock);
        return $result;
    }

    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->validateInput()){
            knjz218bQuery::getDeleteQuery($db, $this->field, $this->selectMock);
        }
        $this->clean();
        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
        $this->sendField = array();
        if ($this->cmd == "delete") {
            $this->field = array();
            $this->selectMock = array();
        }
    }
}
?>
