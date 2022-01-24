<?php

require_once('for_php7.php');

class knjf014Query extends Query {

    //学校を判断
    function getNameMstZ010() {
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学期
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //種類
    function getMonth($model) {
        $query  = " WITH T_COMMON (VALUE,LABEL,SORT) AS ( ";
        $query .= "     VALUES('04','4月', 1) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('05','5月', 2) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('06','6月', 3) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('07','7月', 4) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('08','8月', 5) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('09','9月', 6) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('10','10月', 7) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('11','11月', 8) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('12','12月', 9) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('01','1月', 10) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('02','2月', 11) ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('03','3月', 12) ";
        $query .= "  ), SORT_MIN_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(T2.SORT) AS SORT_MIN ";
        $query .= "       , MAX(T3.SORT) AS SORT_MAX ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         , T_COMMON T2 ";
        $query .= "         , T_COMMON T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "         AND T2.VALUE = RIGHT('00' || RTRIM(CAST(MONTH(T1.SDATE) AS CHAR(2))),2) ";
        $query .= "         AND T3.VALUE = RIGHT('00' || RTRIM(CAST(MONTH(T1.EDATE) AS CHAR(2))),2) ";
        $query .= "  ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     T_COMMON T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SORT BETWEEN (SELECT SORT_MIN FROM SORT_MIN_MAX) AND (SELECT SORT_MAX FROM SORT_MIN_MAX) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SORT ";

        return $query;
    }


    //一覧
    function getMedexamMonthList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS REGD_SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     L1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN MEDEXAM_DET_MONTH_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER AND L1.MONTH = '{$model->month}' AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
    //名称マスタ
    function getNameMst($model, $namecd1, $flg = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($flg == "4") {
            $query .= "     NAME1 AS LABEL ";
        } else if ($flg == "3") {
            $query .= "     ABBV1 || '　' || NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || '　' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($flg == "1") {
            $query .= "     AND NAMESPARE3 = '1' ";
        }
        if ($flg == "2") {
            $query .= "     AND NAMECD2 = '98' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //健康診断一般データ存在チェック
    function checkMedexamDetDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_MONTH_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND MONTH = '{$model->month}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //健康診断一般データ
    function getUpdateMedexamDetMonthDat($db, $model, $g) {
        $data = array();
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $data["YEAR"][TEXT]         = $model->year;
        $data["SEMESTER"][TEXT]     = $model->semester;
        $data["MONTH"][TEXT]     = $model->month;
        $data["SCHREGNO"][TEXT]     = $schregno;

        $data["HEIGHT"][NUMBER]              = $model->fields["HEIGHT"][$g];
        $data["WEIGHT"][NUMBER]              = $model->fields["WEIGHT"][$g];
        $data["R_BAREVISION_MARK"][TEXT]     = $model->fields["R_BAREVISION_MARK"][$g];
        $data["L_BAREVISION_MARK"][TEXT]     = $model->fields["L_BAREVISION_MARK"][$g];
        $data["R_VISION_MARK"][TEXT]         = $model->fields["R_VISION_MARK"][$g];
        $data["L_VISION_MARK"][TEXT]         = $model->fields["L_VISION_MARK"][$g];
        if ($model->fields["R_BAREVISION"][$g] != '') $data["R_BAREVISION"][TEXT] = $model->fields["R_BAREVISION"][$g];
        if ($model->fields["R_VISION"][$g] != '')     $data["R_VISION"][TEXT]     = $model->fields["R_VISION"][$g];
        if ($model->fields["L_BAREVISION"][$g] != '') $data["L_BAREVISION"][TEXT] = $model->fields["L_BAREVISION"][$g];
        if ($model->fields["L_VISION"][$g] != '')     $data["L_VISION"][TEXT]     = $model->fields["L_VISION"][$g];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = knjf014Query::checkMedexamDetDat($model, $schregno);
        if ($db->getOne($query) > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND MONTH = '{$model->month}' ";
            $where .= "     AND SCHREGNO = '{$schregno}' ";
            $query = Query::updateSQL($data, "MEDEXAM_DET_MONTH_DAT", $where);
            $db->query($query);
        } else if ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
            $query = Query::insertSQL($data, "MEDEXAM_DET_MONTH_DAT");
            $db->query($query);
        }

    }
    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //健康診断一般データ
            knjf014Query::getUpdateMedexamDetMonthDat($db, $model, $g);
        }//for

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
