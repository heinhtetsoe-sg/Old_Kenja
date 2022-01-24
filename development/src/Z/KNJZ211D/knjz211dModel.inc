<?php

require_once('for_php7.php');
class knjz211dModel extends model {
    var $field = array();
    var $cmd;
    var $grade;
    var $subclass;
    var $schoolkind;
    var $school_kind;
    var $semester;

    function knjz211dModel() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->schoolkind);
            unset($this->grade);
            unset($this->subclass);
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        $this->grade        = VARS::post("GRADE");
        $this->subclass     = VARS::post("SUBCLASS");
        $this->hid_ViewCd   = VARS::post("HID_VIEWCD");

        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("SUBCLASS")) {
            $this->subclass = VARS::get("SUBCLASS");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校種取得
        $grade = (!$this->grade) ? $db->getOne(knjz211dQuery::getGrade($this)) : $this->grade;
        if (VARS::request("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::post("SCHOOL_KIND");
        }

        //学期取得
        $this->semester = array();
        $result = $db->query(knjz211dQuery::getSemester());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->semester[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //初期化
        $this->setViewCd = array();
        $this->arrPatternCd = $this->arrViewFlg = array();

        //再配置
        $this->setViewCd = explode(",", $this->hid_ViewCd);
        foreach ($this->setViewCd as $key => $viewcd) {
            $this->arrPatternCd[$viewcd] = VARS::post("PATTERN_CD-".$viewcd);
            foreach ($this->semester as $semester => $semestername) {
                $this->arrViewFlg[$viewcd][$semester] = VARS::post("VIEWFLG".$semester."-".$viewcd);
            }
        }

        //必須チェック
        if (!strlen($this->grade) || !strlen($this->subclass)) {
            $this->setWarning("MSG310");
            return false;
        }

        //更新
        knjz211dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //前年度コピー
    function &getCopyModel() {
        //存在チェック
        $db = Query::dbCheckOut();
        $preCnt     = $db->getOne(knjz211dQuery::checkExistsSubclassPattern((CTRL_YEAR - 1), $this->schoolkind));
        Query::dbCheckIn($db);
        if ($preCnt == 0) {
            $this->setWarning("MSG203","前年度にデータがありません。");
            return false;
        }

        //今年度のデータ削除
        knjz211dQuery::getDeleteQuery($this, $this->schoolkind);

        //コピー
        knjz211dQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();
        return;
    }

}
?>
