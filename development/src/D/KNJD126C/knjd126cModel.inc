<?php

require_once('for_php7.php');

class knjd126cModel extends Model {

    var $cmd;
    var $control = array();        //コントロールのデータを保持
    var $field;

    function knjd126cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->field = array();
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->field["IBYEAR"] = CTRL_YEAR;
        $this->field["IBGRADE"] = VARS::post("IBGRADE");
        $this->field["IBPRG_COURSE"] = VARS::post("IBPRG_COURSE");
        $this->field["IBSUBCLASSCD"] = VARS::post("IBSUBCLASSCD");
        $this->field["IBEVAL_DIV1"] = "0";
        $this->field["IBEVAL_DIV2"] = "0";
        $this->subclassCnt = VARS::post("subclassCnt");

        for ($subCnt = 1; $subCnt <= $this->subclassCnt; $subCnt++) {
            /* 科目 */
            $this->field["SUBCLASSCD".$subCnt] = VARS::post("SUBCLASSCD".$subCnt);
            /* 講座 */
            $this->field["CHAIRCD".$subCnt] = VARS::post("CHAIRCD".$subCnt);
        }

        $postScoreName = VARS::post("ScoreName");
        $this->scoreName = array();
        if ($postScoreName) {
            $getScoreName = preg_split("/:/", $postScoreName);
            foreach ($getScoreName as $key => $val) {
                list($sKey, $sVal) = preg_split("/@/", $val);
                $this->scoreName[$sKey] = $sVal;
            }
        }

        $postSemeValueName = VARS::post("SemeValueName");
        $this->semeValueName = array();
        if ($postSemeValueName) {
            $getSemeValueName = preg_split("/:/", $postSemeValueName);
            foreach ($getSemeValueName as $key => $val) {
                list($sKey, $sVal) = preg_split("/@/", $val);
                $this->semeValueName[$sKey] = $sVal;
            }
        }

        $postGradeName = VARS::post("GradeName");
        $this->gradeName = array();
        if ($postGradeName) {
            $getGradeName = preg_split("/:/", $postGradeName);
            foreach ($getGradeName as $key => $val) {
                list($sKey, $sVal) = preg_split("/@/", $val);
                $this->gradeName[$sKey] = $sVal;
            }
        }

        $postChkFinName = VARS::post("ChkFinName");
        $this->chkFinName = array();
        if ($postChkFinName) {
            $getChkFinName = preg_split("/:/", $postChkFinName);
            foreach ($getChkFinName as $key => $val) {
                list($sKey, $sVal) = preg_split("/@/", $val);
                $this->chkFinName[$sKey] = $sVal;
            }
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $this->scoreUpdate($db);
            $this->semeValueUpdate($db);
            $this->gradeUpdate($db);
            $this->chkFinUpdate($db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    /* 得点更新 */
    function &scoreUpdate($db) {
        if (is_array($this->scoreName)) {
            foreach ($this->scoreName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd) = preg_split("/_/", $val);
                $this->scoreDelete($db, $schregNo, $ibYear, $ibGrade, $seq, $viewCd);
            }
        }

        if (is_array($this->scoreName)) {
            foreach ($this->scoreName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd) = preg_split("/_/", $val);
                $score = VARS::post($val);
                $this->scoreInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score);
            }
        }
    }

    /* 削除 */
    function &scoreDelete($db, $schregNo, $ibYear, $ibGrade, $seq, $viewCd) {
        $query = knjd126cQuery::getScoreDeleteSql($this, $schregNo, $ibYear, $ibGrade, $seq, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &scoreInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score)
    {
        $query = knjd126cQuery::getScoreInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score);
        $db->query($query);
    }

    /* SemeValue更新 */
    function &semeValueUpdate($db) {
        if (is_array($this->semeValueName)) {
            foreach ($this->semeValueName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) = preg_split("/_/", $val);
                $this->semeValueDelete($db, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
            }
        }

        $this->schregIbSemeVal = array();
        if (is_array($this->semeValueName)) {
            foreach ($this->semeValueName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) = preg_split("/_/", $val);
                $semeValue = VARS::post($val);
                if ($semester == "8") {
                    $this->schregIbSemeVal[$schregNo] += $semeValue;
                }
                $this->semeValueInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue);
            }
        }
    }

    /* 削除 */
    function &semeValueDelete($db, $schregNo, $ibYear, $ibGrade, $semester, $viewCd) {
        $query = knjd126cQuery::getGradeDeleteSql($this, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &semeValueInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue)
    {
        $query = knjd126cQuery::getGradeInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue);
        $db->query($query);
    }

    /* Grade更新 */
    function &gradeUpdate($db) {
        if (is_array($this->gradeName)) {
            foreach ($this->gradeName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) = preg_split("/_/", $val);
                $this->gradeDelete($db, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
            }
        }

        if (is_array($this->gradeName)) {
            foreach ($this->gradeName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) = preg_split("/_/", $val);
                $grade = VARS::post($val);
                $this->gradeInsert($db, $schregNo, $ibYear, $ibGrade, "0", "0", "00", $semester, $viewCd, $grade);
            }
        }
    }

    /* 削除 */
    function &gradeDelete($db, $schregNo, $ibYear, $ibGrade, $semester, $viewCd) {
        $query = knjd126cQuery::getGradeDeleteSql($this, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &gradeInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade)
    {
        $query = knjd126cQuery::getGradeLevel($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade);
        $setGrade = $db->getOne($query);
        $grade = $grade ? $grade : $setGrade;
        $query = knjd126cQuery::getGradeInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade);
        $db->query($query);
    }

    /* 入力完了更新 */
    function &chkFinUpdate($db) {
        if (is_array($this->chkFinName)) {
            foreach ($this->chkFinName as $key => $val) {
                list($fuyou, $ibYear, $ibGrade, $seq, $viewCd) = preg_split("/_/", $val);
                $this->chkFinDelete($db, $ibYear, $ibGrade, $seq, $viewCd);
            }
        }

        if (is_array($this->chkFinName)) {
            foreach ($this->chkFinName as $key => $val) {
                list($fuyou, $ibYear, $ibGrade, $seq, $viewCd) = preg_split("/_/", $val);
                $chkFin = VARS::post($val);
                if ($chkFin) {
                    $this->chkFinInsert($db, $ibYear, $ibGrade, $seq, $viewCd, $chkFin);
                }
            }
        }
    }

    /* 削除 */
    function &chkFinDelete($db, $ibYear, $ibGrade, $seq, $viewCd) {
        $query = knjd126cQuery::getChkFinDeleteSql($this, $schregNo, $ibYear, $ibGrade, $seq, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &chkFinInsert($db, $ibYear, $ibGrade, $seq, $viewCd, $chkFin)
    {
        $query = knjd126cQuery::getChkFinInsertSql($this, $ibYear, $ibGrade, $seq, $viewCd, $chkFin);
        $db->query($query);
    }

}
?>
