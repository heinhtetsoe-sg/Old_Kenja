<?php

require_once('for_php7.php');

class knjd428dQuery extends Query {

    //校種取得
    function getSchKindFromGrade($model) {
        $query  = " SELECT ";
        $query .= "   T2.SCHOOL_KIND ";
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

    //学期取得
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "   T2.GRADE_CD ";
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

    //教師コンボデータ取得
    function getStaff($model) {
        $query   = "  SELECT ";
        $query  .= "    STAFFCD AS VALUE, ";
        $query  .= "    STAFFNAME AS LABEL ";
        $query  .= "  FROM ";
        $query  .= "    V_STAFF_MST ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        $query  .= "  ORDER BY ";
        $query  .= "    STAFFCD ";

        return $query;
    }

    //１レコード取得 -- HREPORTREMARK_DAT
    function getRow($model) {
        $query  .= "  SELECT ";
        $query  .= "    HD0101.REMARK1 AS MORALEDUCATION, ";
        $query  .= "    H1.FOREIGNLANGACT, ";
        $query  .= "    H1.TOTALSTUDYTIME, ";
        $query  .= "    H1.SPECIALACTREMARK, ";
        $query  .= "    HD0102.REMARK1 AS SELFRELIANCEACT, ";
        $query  .= "    HD0103.REMARK1 AS TOTALREMARK, ";
        $query  .= "    H1.ATTENDREC_REMARK, ";
        $query  .= "    H1.COMMUNICATION ";
        $query  .= "  FROM ";
        $query  .= "    SCHREG_REGD_DAT REGD ";
        $query  .= "  LEFT JOIN HREPORTREMARK_DAT H1 ";
        $query  .= "    ON H1.YEAR = REGD.YEAR ";
        $query  .= "    AND H1.SEMESTER = REGD.SEMESTER ";
        $query  .= "    AND H1.SCHREGNO = REGD.SCHREGNO ";
        $query  .= "  LEFT JOIN HREPORTREMARK_DETAIL_DAT HD0101 ";
        $query  .= "    ON HD0101.YEAR = REGD.YEAR ";
        $query  .= "    AND HD0101.SEMESTER = REGD.SEMESTER ";
        $query  .= "    AND HD0101.SCHREGNO = REGD.SCHREGNO ";
        $query  .= "    AND HD0101.DIV = '01' ";
        $query  .= "    AND HD0101.CODE = '01' ";
        $query  .= "  LEFT JOIN HREPORTREMARK_DETAIL_DAT HD0102 ";
        $query  .= "    ON HD0102.YEAR = REGD.YEAR ";
        $query  .= "    AND HD0102.SEMESTER = REGD.SEMESTER ";
        $query  .= "    AND HD0102.SCHREGNO = REGD.SCHREGNO ";
        $query  .= "    AND HD0102.DIV = '01' ";
        $query  .= "    AND HD0102.CODE = '02' ";
        $query  .= "  LEFT JOIN HREPORTREMARK_DETAIL_DAT HD0103 ";
        $query  .= "    ON HD0103.YEAR = REGD.YEAR ";
        $query  .= "    AND HD0103.SEMESTER = REGD.SEMESTER ";
        $query  .= "    AND HD0103.SCHREGNO = REGD.SCHREGNO ";
        $query  .= "    AND HD0103.DIV = '01' ";
        $query  .= "    AND HD0103.CODE = '03' ";
        $query  .= "  WHERE ";
        $query  .= "    REGD.YEAR = '".$model->exp_year."' ";
        $query  .= "    AND REGD.SEMESTER = '".$model->semester."'";
        $query  .= "    AND REGD.SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //総合所見表示名取得
    function getTotalRemarkName($schkind) {
        $query   = "  SELECT ";
        $query  .= "    REMARK10 AS TOTALREMARK_NAME ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_CONDITION_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        $query  .= "    AND SCHOOLCD = '".SCHOOLCD."' ";
        $query  .= "    AND SCHOOL_KIND = '{$schkind}' ";
        $query  .= "    AND GRADE = '00' ";
        $query  .= "    AND COURSECD = '0' ";
        $query  .= "    AND MAJORCD = '000' ";
        $query  .= "    AND COURSECODE = '0000' ";
        $query  .= "    AND SEQ = '210' ";

        return $query;
    }

    //総合所見表示フラグ取得
    function getDispFlg($model, $schkind) {
        $query   = "  SELECT ";
        $query  .= "    SEQ, ";
        $query  .= "    REMARK1 AS DISP_FLG ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_CONDITION_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        $query  .= "    AND SCHOOLCD = '".SCHOOLCD."' ";
        $query  .= "    AND SCHOOL_KIND = '{$schkind}' ";
        $query  .= "    AND GRADE = '00' ";
        $query  .= "    AND COURSECD = '0' ";
        $query  .= "    AND MAJORCD = '000' ";
        $query  .= "    AND COURSECODE = '0000' ";
        $query  .= "    AND SEQ IN ('208', '210', '211') ";
        $query  .= "  ORDER BY ";
        $query  .= "    SEQ ";

        return $query;
    }

    //教師取得
    function getHreportStaff($model) {
        $query   = "  SELECT ";
        $query  .= "    SEQ, ";
        $query  .= "    STAFFCD ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_STAFF_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        $query  .= "    AND SEMESTER = '9' ";
        $query  .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //教師取得2(HREPORT_STAFF_DATに登録がされてない場合)
    function getRegdhStaff($model) {
        $query   = "  SELECT ";
        $query  .= "    TR_CD1 AS STAFFCD1, ";
        $query  .= "    TR_CD2 AS STAFFCD2, ";
        $query  .= "    TR_CD3 AS STAFFCD3, ";
        $query  .= "    SUBTR_CD1 AS STAFFCD4, ";
        $query  .= "    SUBTR_CD2 AS STAFFCD5, ";
        $query  .= "    SUBTR_CD3 AS STAFFCD6";
        $query  .= "  FROM ";
        $query  .= "    SCHREG_REGD_HDAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        if ($model->semester != "9") {
            $query  .= "    AND SEMESTER = '".$model->semester."' ";
        } else {
            $query  .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query  .= "    AND GRADE = '".$model->grade."' ";
        $query  .= "    AND HR_CLASS = '".$model->hrClass."' ";

        return $query;
    }

    //校種 取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND GDAT.GRADE    = REGD.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '{$model->exp_year}' ";
        $query .= " AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= " AND REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND HDAT.GRADE    = '{$model->grade}' ";
        $query .= " AND HDAT.HR_CLASS = '{$model->hrClass}' ";

        return $query;
    }

    //HREPORT_CONDITION_DAT 取得
    function getReportCondition($model,$schoolKind,$seq) {
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

    //データチェック
    function getHreportRemarkCnt($model, $tblname, $semester, $div="", $code="")
    {
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
        $db->autoCommit(false);
        //(今年度今学期の)選択生徒の校種を割り出す。
        $schkind = $db->getOne(knjd428dQuery::getSchKindFromGrade($model));

        //HREPORT_STAFF_DAT更新
        $query   = "  DELETE ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_STAFF_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR            = '".$model->exp_year."' ";
        $query  .= "    AND SEMESTER    = '9' ";
        $query  .= "    AND SCHREGNO    = '".$model->schregno."' ";
        $query  .= "    AND SEQ IN ('1', '2', '3', '4', '5', '6') ";
        $db->query($query);

        for($i = 1; $i <= 6; $i++) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = "9";
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["SEQ"][TEXT]          = $i;
            $data["STAFFCD"][TEXT]      = $model->staffField[$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "HREPORT_STAFF_DAT");
            $db->query($query);
        }

        //HREPORTREMARK_DAT更新
        $data = array();
        for($i = 1; $i <= 9; $i++) {
            if (!in_array($i, array("2", "3", "4", "8", "9"))) continue;    //HREPORTREMARK_DATにデータを登録する項目の番号のみ処理
            if ($model->displayItemNo[$i] != "1") continue;

            $updRemark = $model->field["REMARK{$i}"];
            if ($i == "2") {
                $updColName = "FOREIGNLANGACT";
            }
            if ($i == "3") {
                $updColName = "TOTALSTUDYTIME";
            }
            if ($i == "4") {
                $updColName = "SPECIALACTREMARK";
            }
            if ($i == "8") {
                $updColName = "ATTENDREC_REMARK";
            }
            if ($i == "9") {
                $updColName = "COMMUNICATION";
            }
            $data[$updColName][TEXT]      = $model->field["REMARK{$i}"];
        }
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";

        $cnt = $db->getOne(knjd428dQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DAT", $model->semester));
        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->semester."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data["YEAR"][TEXT] = $model->exp_year;
            $data["SEMESTER"][TEXT] = $model->semester;
            $data["SCHREGNO"][TEXT] = $model->schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            $db->query($query);
        }

        //HREPORTREMARK_DAT更新
        $query   = "  DELETE ";
        $query  .= "  FROM ";
        $query  .= "    HREPORTREMARK_DETAIL_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$model->exp_year."' ";
        $query  .= "    AND SEMESTER = '".$model->semester."' ";
        $query  .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query  .= "    AND DIV      = '01' ";
        $query  .= "    AND CODE IN ('01', '02', '03') ";
        $db->query($query);

        $data = array();
        for($i = 1; $i <= 9; $i++) {
            if (!in_array($i, array("1", "5", "6"))) continue;    //HREPORTREMARK_DETAIL_DATにデータを登録する項目の番号のみ処理
            if ($model->displayItemNo[$i] != "1") continue;

            if ($i == "1") {
                $div    = "01";
                $code   = "01";
            }
            if ($i == "5") {
                $div    = "01";
                $code   = "02";
            }
            if ($i == "6") {
                $div    = "01";
                $code   = "03";
            }

            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = $model->semester;
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["DIV"][TEXT]           = $div;
            $data["CODE"][TEXT]          = $code;
            $data["REMARK1"][TEXT]       = $model->field["REMARK{$i}"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
