<?php

require_once('for_php7.php');

class knjc036Query extends Query {

    /* 学期名称取得 */
    function selectSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* 学級名称取得 */
    function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER = '".$model->semester."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, HR_CLASS ";

        return $query;
    }

    /* 出欠月別累積データ・出欠科目別累積データ・遅刻欠課訂正データ */
    function selectAttendQuery($model,$knjSchoolMst)
    {
        $query  = " WITH ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER, ";
        $query .= "         sum(KEKKA) as KEKKA ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY  ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        $query .= "     ) ";
        $query .= " , ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER, ";
        $query .= "         sum(LATE) as LATE, ";
        $query .= "         sum(value(SICK,0)+value(NOTICE,0)+value(NONOTICE,0)+value(NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(VIRUS,0)";
        }
        $query .= "         ) as JISUU ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY  ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        $query .= "     ) ";
        $query .= " , ATTEND_CORRECTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER, ";
        $query .= "         sum(LATEDETAIL) as LATEDETAIL, ";
        $query .= "         sum(KEKKADETAIL) as KEKKADETAIL ";
        $query .= "     FROM ";
        $query .= "         ATTEND_CORRECTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY  ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     value(L3.LATE,0) as LATE, ";
        $query .= "     value(L4.LATEDETAIL,0) as LATE_COR, ";
        $query .= "     value(L3.LATE,0) + value(L4.LATEDETAIL,0) as LATE_FIX, ";
        $query .= "     value(L3.JISUU,0) as JISUU, ";
        $query .= "     value(L2.KEKKA,0) as KEKKA, ";
        $query .= "     value(L4.KEKKADETAIL,0) as KEKKA_COR, ";
        $query .= "     value(L2.KEKKA,0) + value(L4.KEKKADETAIL,0) as KEKKA_FIX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2  ";
        $query .= "             ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND T2.YEAR = T1.YEAR ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1  ";
        $query .= "             ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "      LEFT JOIN ATTEND_SEMES L2  ";
        $query .= "             ON L2.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND L2.SEMESTER = T2.SEMESTER ";
        $query .= "      LEFT JOIN ATTEND_SUBCLASS L3  ";
        $query .= "             ON L3.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND L3.SEMESTER = T2.SEMESTER ";
        $query .= "      LEFT JOIN ATTEND_CORRECTION L4  ";
        $query .= "             ON L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND L4.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* 遅刻欠課訂正データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach($model->field["SCHREGNO"] as $key => $schregno){
            /* 対象データの削除 */
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     ATTEND_CORRECTION_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".$model->semester."' AND ";
            $query .= "     SCHREGNO = '".$schregno."' ";
            $db->query($query);
            /* 対象データをセット */
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["LATEDETAIL"][NUMBER]     = ($model->field["LATE_COR"][$key]) ?   $model->field["LATE_COR"][$key] :  0; //遅刻訂正
            $data["KEKKADETAIL"][NUMBER]    = ($model->field["KEKKA_COR"][$key]) ?  $model->field["KEKKA_COR"][$key] : 0; //欠課訂正
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            /* 対象データ：訂正がある生徒のみ - INSERT */
            if ($model->field["LATE_COR"][$key] || $model->field["KEKKA_COR"][$key]) {
                $query = Query::insertSQL($data, "ATTEND_CORRECTION_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
