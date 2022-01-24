<?php

require_once('for_php7.php');

class knjd428lQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //校種・学年取得
    function getSchoolKindGrade($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= "   , T2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->exp_year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->semester."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."'";
        return $query;
    }

    //HREPORT_CONDITION_DAT 取得
    function getReportCondition($model, $schoolKind, $seq) {
        $query  = " SELECT ";
        $query .= "   T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "   HREPORT_CONDITION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "   AND T1.GRADE = '00' ";
        $query .= "   AND T1.COURSECD = '0' ";
        $query .= "   AND T1.MAJORCD = '000' ";
        $query .= "   AND T1.COURSECODE = '0000' ";
        $query .= "   AND T1.SEQ = '".$seq."' ";

        return $query;
    }

    //１レコード取得 -- HREPORTREMARK_DAT
    function getRow($model) {
        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , D01.REMARK1 REMARK1_01 ";
        $query .= "   , T2.FOREIGNLANGACT ";
        $query .= "   , T2.TOTALSTUDYTIME ";
        $query .= "   , T2.SPECIALACTREMARK ";
        $query .= "   , T2.ATTENDREC_REMARK ";
        $query .= "   , T2.COMMUNICATION ";
        $query .= "   , D02.REMARK1 REMARK1_02 ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN HREPORTREMARK_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D01 ";
        $query .= "     ON D01.YEAR = T1.YEAR ";
        $query .= "     AND D01.SEMESTER = T1.SEMESTER ";
        $query .= "     AND D01.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND D01.DIV = '01' ";
        $query .= "     AND D01.CODE = '01' ";
        $query .= "   LEFT JOIN HREPORTREMARK_DETAIL_DAT D02 ";
        $query .= "     ON D02.YEAR = T1.YEAR ";
        $query .= "     AND D02.SEMESTER = T1.SEMESTER ";
        $query .= "     AND D02.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND D02.DIV = '01' ";
        $query .= "     AND D02.CODE = '02' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->exp_year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->field["SEMESTER"]."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //データチェック
    function getHreportRemarkCnt($model, $tblname, $semester, $div="", $code="") {
        //テーブル取得
        $table = "";
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '".$semester."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        if ($div != "") {
            $query .= " AND DIV = '".$div."' ";
        }
        if ($code != "") {
            $query .= " AND CODE = '".$code."' ";
        }

        return $query;
    }

    //UPDATE処理
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();
        // $db->autoCommit(false);

        //生徒の校種・学年を取得
        $query = knjd428lQuery::getSchoolKindGrade($model);
        $gradeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $section = array();
        if ($model->useSection[$gradeRow["SCHOOL_KIND"]][$gradeRow["GRADE_CD"]]) {
            $section = $model->useSection[$gradeRow["SCHOOL_KIND"]][$gradeRow["GRADE_CD"]];
        }
        if (get_count($section) <= 0) {
            //学年(GRADE_CD)で取得出来なかった場合は "00" を取得
            $section = $model->useSection[$gradeRow["SCHOOL_KIND"]]["00"];
        }

        $data = array();
        //外国語活動
        if (in_array("2", $section)) {
            $data["FOREIGNLANGACT"][TEXT]   = $model->field["FOREIGNLANGACT"];
        }
        //総合的な学習の時間
        if (in_array("3", $section)) {
            $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
        }
        //特別活動
        if (in_array("4", $section)) {
            $data["SPECIALACTREMARK"][TEXT] = $model->field["SPECIALACTREMARK"];
        }
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";
        $cnt = $db->getOne(knjd428lQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DAT", $model->field["SEMESTER"]));
        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data["YEAR"][TEXT] = $model->exp_year;
            $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT] = $model->schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            $db->query($query);
        }

        //特別の教科 道徳
        if (in_array("1", $section)) {
            $data = array();
            $data["REMARK1"][TEXT] = $model->field["REMARK1_01"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428lQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", $model->field["SEMESTER"], "01", "01"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND DIV      = '01' ";
                $where .= " AND CODE     = '01' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = "01";
                $data["CODE"][TEXT]     = "01";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                $db->query($query);
            }

        }

        //自立活動
        if (in_array("5", $section)) {
            $data = array();
            $data["REMARK1"][TEXT] = $model->field["REMARK1_02"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428lQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", $model->field["SEMESTER"], "01", "02"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND DIV      = '01' ";
                $where .= " AND CODE     = '02' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = "01";
                $data["CODE"][TEXT]     = "02";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
