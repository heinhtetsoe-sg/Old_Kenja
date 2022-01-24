<?php

require_once('for_php7.php');

class knjz406aModel extends Model {
    var $cmd;
    var $field      = array();
    var $setData    = array();

    function knjz406aModel() {}

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

        $this->field["GRADE_HR_CLASS"]  = VARS::request("GRADE_HR_CLASS");
        $this->field["SUBCLASSCD"]      = VARS::post("SUBCLASSCD");
        $this->field["SEQ"]             = VARS::post("SEQ");
        $this->field["CHECK"]           = VARS::post("CHECK");

        $db = Query::dbCheckOut();
        $this->viewList = array();
        $result = $db->query(knjz406aQuery::getViewList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->viewList[] = $row["VALUE"];

            $this->field["VIEWFLG_".$row["VALUE"]]            = VARS::post("VIEWFLG_".$row["VALUE"]);
            $this->field["UNIT_ASSESSHIGH_".$row["VALUE"]]    = VARS::post("UNIT_ASSESSHIGH_".$row["VALUE"]);

        }
        $result->free();
        Query::dbCheckIn($db);

        $this->setData = array();
        if (is_array($this->field["SEQ"])) {
            foreach ($this->field["SEQ"] as $key => $seq) {
                if (is_array($this->field["CHECK"])) {
                    foreach ($this->field["CHECK"] as $ckey => $cval) {
                        if ($seq == $cval) {
                            $this->setData[$seq]["CHECK"] = $cval;
                        }
                    }
                }

                foreach ($this->viewList as $viewcd) {
                    if (is_array($this->field["VIEWFLG_".$viewcd])) {
                        foreach ($this->field["VIEWFLG_".$viewcd] as $vkey => $vval) {
                            if ($seq == $vval) {
                                $this->setData[$seq]["VIEWFLG_".$viewcd] = $vval;
                            }
                        }
                    }
                    $this->setData[$seq]["UNIT_ASSESSHIGH_".$viewcd] = $this->field["UNIT_ASSESSHIGH_".$viewcd][$key];
                }
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->field["SEQ"])) {
            $this->setWarning("MSG304");
            return false;
        }

        $err_flg    = false;
        $check_flg  = false;
        foreach ($this->field["SEQ"] as $key => $seq) {
            if ($this->setData[$seq]["CHECK"]) {
                $check_flg = true;
                foreach ($this->viewList as $viewcd) {
                    if ($this->setData[$seq]["VIEWFLG_".$viewcd] && !strlen($this->setData[$seq]["UNIT_ASSESSHIGH_".$viewcd])) {
                        $err_flg = true;
                    }
                }
            }
        }

        if (!$check_flg) {
            $this->setWarning("MSG304", "更新対象の単元がありません。");
            return false;
        }
        if ($err_flg) {
            $this->setWarning("MSG901", "評価対象の観点に配点を設定してください。");
            return false;
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
        $result = knjz406aQuery::getUpdateQuery($this);
        return $result;
    }

    //コピー処理
    function &getCopyModel() {
        $db = Query::dbCheckOut();

        //前年度データ件数
        $pre_year = CTRL_YEAR - 1;
        $pre_year_cnt1 = $db->getOne(knjz406aQuery::checkUnitTestDat($pre_year));
        $pre_year_cnt2 = $db->getOne(knjz406aQuery::checkJviewnameGradeYdat($pre_year));
        $pre_year_cnt3 = $db->getOne(knjz406aQuery::checkUnitTestInpuseqDat($this, $pre_year));

        //今年度データ件数
        $this_year = CTRL_YEAR;
        $this_year_cnt1 = $db->getOne(knjz406aQuery::checkUnitTestDat($this_year));
        $this_year_cnt2 = $db->getOne(knjz406aQuery::checkJviewnameGradeYdat($this_year));
        $this_year_cnt3 = $db->getOne(knjz406aQuery::checkUnitTestInpuseqDat($this, $this_year));

        if (!$pre_year_cnt1 || !$pre_year_cnt2 || !$pre_year_cnt3) {
            $this->setWarning("前年度のデータがありません。");
        } else if (!$this_year_cnt1 || !$this_year_cnt2) {
            $this->setWarning("今年度の観点、または単元データがありません。");
        } else if ($this_year_cnt3) {
            $this->setWarning("すでに今年度にデータが存在します。");
        } else {
            knjz406aQuery::getCopyQuery($db, $this);
            $this->setMessage("MSG201");
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
