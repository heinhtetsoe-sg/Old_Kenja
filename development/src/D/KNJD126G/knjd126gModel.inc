<?php

require_once('for_php7.php');

class knjd126gModel extends Model {
    var $cmd;
    var $field      = array();
    var $setData    = array();

    function knjd126gModel() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            $this->field    = array();
            $this->setData  = array();
        }

        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        $this->field["GRADE_HR_CLASS"]  = VARS::post("GRADE_HR_CLASS");
        $this->field["SUBCLASSCD"]      = VARS::post("SUBCLASSCD");
        $this->field["SEQ"]             = VARS::post("SEQ");
        $this->field["UNIT_TEST_DATE"]  = VARS::post("UNIT_TEST_DATE");
        $this->field["SCHREGNO"]        = VARS::post("SCHREGNO");

        $db = Query::dbCheckOut();
        $this->viewList = array();
        $result = $db->query(knjd126gQuery::getViewList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->viewList[] = $row["VIEWCD"];
            $this->field["SCORE_".$row["VIEWCD"]] = VARS::post("SCORE_".$row["VIEWCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $this->setData = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                foreach ($this->viewList as $viewcd) {
                    $this->setData[$schregno]["SCORE_".$viewcd] = $this->field["SCORE_".$viewcd][$key];
                }
            }
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->field["SCHREGNO"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (get_count($this->viewList) == 0) {
            $this->setWarning("MSG305", "観点を設定してください。");
            return false;
        }

        $score_flg = false;
        foreach ($this->field["SCHREGNO"] as $key => $schregno) {
            foreach ($this->viewList as $viewcd) {
                if (strlen($this->setData[$schregno]["SCORE_".$viewcd])) {
                    $score_flg = true;
                }
            }
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    function &update() {
        $result = knjd126gQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
