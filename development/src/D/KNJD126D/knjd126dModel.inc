<?php

require_once('for_php7.php');

class knjd126dModel extends Model {

    var $cmd;
    var $control = array();        //コントロールのデータを保持
    var $field;

    function knjd126dModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->field = array();
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->field["IBYEAR"] = VARS::post("IBYEAR");
        $this->field["IBGRADE"] = VARS::post("IBGRADE");
        $this->field["IBPRG_COURSE"] = VARS::post("IBPRG_COURSE");
        $this->field["IBSUBCLASSCD"] = VARS::post("IBSUBCLASSCD");
        $this->field["IBEVAL_DIV1"] = VARS::post("IBEVAL_DIV1");
        $this->field["IBEVAL_DIV2"] = VARS::post("IBEVAL_DIV2");
        for ($subCnt = 1; $subCnt <= $this->subclassCnt; $subCnt++) {
            /* 科目 */
            $this->field["SUBCLASSCD".$subCnt] = VARS::post("SUBCLASSCD".$subCnt);
            /* 講座 */
            $this->field["CHAIRCD".$subCnt] = VARS::post("CHAIRCD".$subCnt);
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
                $this->scoreDelete($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $seq, $viewCd);
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
    function &scoreDelete($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $seq, $viewCd) {
        $query = knjd126dQuery::getScoreDeleteSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $seq, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &scoreInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score)
    {
        $query = knjd126dQuery::getScoreInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $seq, $viewCd, $score);
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

        if (is_array($this->semeValueName)) {
            foreach ($this->semeValueName as $key => $val) {
                list($fuyou, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd) = preg_split("/_/", $val);
                $semeValue = VARS::post($val);
                $this->semeValueInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue);
            }
        }
    }

    /* 削除 */
    function &semeValueDelete($db, $schregNo, $ibYear, $ibGrade, $semester, $viewCd) {
        $query = knjd126dQuery::getGradeDeleteSql($this, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &semeValueInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue)
    {
        $query = knjd126dQuery::getGradeInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $semeValue);
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
        $query = knjd126dQuery::getGradeDeleteSql($this, $schregNo, $ibYear, $ibGrade, $semester, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &gradeInsert($db, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade)
    {
        $query = knjd126dQuery::getGradeInsertSql($this, $schregNo, $ibYear, $ibGrade, $div1, $div2, $mark, $semester, $viewCd, $grade);
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
        $query = knjd126dQuery::getChkFinDeleteSql($this, $schregNo, $ibYear, $ibGrade, $seq, $viewCd);
        $db->query($query);
    }

    /* 新規 */
    function &chkFinInsert($db, $ibYear, $ibGrade, $seq, $viewCd, $chkFin)
    {
        $query = knjd126dQuery::getChkFinInsertSql($this, $ibYear, $ibGrade, $seq, $viewCd, $chkFin);
        $db->query($query);
    }

}
?>
