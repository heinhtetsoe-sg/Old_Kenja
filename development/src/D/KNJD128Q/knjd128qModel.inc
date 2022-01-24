<?php

require_once('for_php7.php');


class knjd128qModel extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $hr_class;
    var $data_cnt;
    var $control;
    var $z010name1;

    function knjd128qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->semester = VARS::post("SEMESTER");
        $this->courseMajor = VARS::post("COURSE_MAJOR");
        $this->testCd = VARS::post("TESTCD");
        $this->grade = VARS::post("GRADE");
        $this->hr_class = VARS::post("HR_CLASS");

        if (VARS::request("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }
        unset($this->sendSchregNo);
        unset($this->sendGrpCd);

        $this->cmd   = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->updateKey as $counter => $updInfo) {
            $setSuppScore = VARS::post("SUPP_SCORE_{$updInfo["SCHREGNO"]}_{$updInfo["SUBCLASSCD_ALL"]}");
            $updateData = array();
            $updateData["CLASSCD"]        = $updInfo["CLASSCD"];
            $updateData["SCHOOL_KIND"]    = $updInfo["SCHOOL_KIND"];
            $updateData["CURRICULUM_CD"]  = $updInfo["CURRICULUM_CD"];
            $updateData["SUBCLASSCD"]     = $updInfo["SUBCLASSCD"];
            $updateData["SCHREGNO"]       = $updInfo["SCHREGNO"];
            if ($setSuppScore == "*") {
                $updateData["SCORE"]          = "";
                $updateData["SCORE_PASS"]     = "";
                $updateData["SCORE_PASS_FLG"] = "";
                $updateData["SCORE_FLG"]      = "1";
                $updateData["SCORE_DI"]       = $setSuppScore;
            } else {
                $updateData["SCORE"]          = $setSuppScore;
                $updateData["SCORE_PASS"]     = $updInfo["PASS_SCORE"] <= $setSuppScore ? $updInfo["PASS_SCORE"] : "";
                $updateData["SCORE_PASS_FLG"] = $updInfo["PASS_SCORE"] <= $setSuppScore ? "1" : "2";
                $updateData["SCORE_FLG"]      = "1";
                $updateData["SCORE_DI"]       = "";
            }
            knjd128qQuery::getUpdateQuery($db, $this, $updateData);
        }

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

}
?>
