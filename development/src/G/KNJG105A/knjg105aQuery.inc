<?php

require_once('for_php7.php');

class knjg105aquery extends Query {
    //学期一覧取得
    function getSemesterList() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($semester, $model) {
        $query  = " SELECT ";
        $query .= "     H1.GRADE || H1.HR_CLASS AS VALUE, ";
        $query .= "     H1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= "     JOIN SCHREG_REGD_GDAT G1 ON H1.YEAR  = G1.YEAR ";
        $query .= "                             AND H1.GRADE = G1.GRADE ";
        $query .= " WHERE ";
        $query .= "         H1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND H1.SEMESTER = '".$semester."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (H1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          H1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          H1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          H1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          H1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          H1.SUBTR_CD1  = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期詳細データより日付取得
    function getSemesDate($semester, $detail="") {
        $query  = " SELECT ";
        if(!$detail){
            $query .= "     MIN(SDATE) AS SDATE, ";
            $query .= "     MAX(EDATE) AS EDATE ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if($semester != "9"){
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        if($detail){
            $query .= "     AND SEMESTER_DETAIL = '".$detail."' ";
        }

        return $query;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ";
        $query .= "     MONTH, ";
        $query .= "     MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= " GROUP BY ";
        $query .= "     SEMESTER,MONTH ";
        $query .= " ORDER BY ";
        $query .= "     2,3,1 ";

        return $query;
    }

    //SEM_OFFDAYSを取得
    function getSemoffdays($model) {
        $query  = " SELECT ";
        $query .= "     SEM_OFFDAYS ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND IN (SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                                 YEAR  = '".CTRL_YEAR."' ";
            $query .= "                             AND GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            $query .= "                         ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function selectQuery($model, $attend_seme, $month, $attend_sdate, $semoffdays) {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = " WITH ";
        $query .= "  ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .="          SCHREGNO, ";
        if ($semoffdays == '1') {
            $query .= "         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS, 0)) AS SICK ";
        } else {
            $query .= "         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0)) AS SICK ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER <= '".$model->field["SEMESTER"]."' ";
        $query .= "         AND MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), ATTEND_SEMES_SCHEDULES AS (SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(CASE WHEN ADD.REP_DI_CD IN ('4', '11') THEN 1 ELSE 0 END) + ";
        $query .= "     SUM(CASE WHEN ADD.REP_DI_CD IN ('5', '12') THEN 1 ELSE 0 END) + ";
        $query .= "     SUM(CASE WHEN ADD.REP_DI_CD IN ('6', '13') THEN 1 ELSE 0 END) AS SICK ";
        $query .= "   FROM ";
        $query .= "     ATTEND_DAY_DAT T1 ";
        $query .= "     LEFT JOIN ATTEND_DI_CD_DAT ADD ON T1.YEAR  = ADD.YEAR ";
        $query .= "                                   AND T1.DI_CD = ADD.DI_CD ";
        $query .= "   WHERE T1.ATTENDDATE BETWEEN '".$attend_sdate."' AND '".$date."' ";
        $query .= "   GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ),ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         SUM(SICK) AS SICK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES W1 ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        $query .= "         SUM(SICK) AS SICK ";
        $query .= "     FROM ";
        $query .= "        ATTEND_SEMES_SCHEDULES  W2 ";
        $query .= "     GROUP BY ";
        $query .= "         W2.SCHREGNO ";
        $query .= " ),SICKINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, SUM(VALUE(W1.SICK, 0)) AS SICK ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM W1 ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO ";
        $query .= " )";
//メイン
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         VALUE(S1.SICK, 0) AS SICK, ";
        $query .= "         B1.BASE_REMARK1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SICKINFO S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B1 ON T1.SCHREGNO = B1.SCHREGNO ";
        $query .= "                                                 AND T1.YEAR     = B1.YEAR ";
        $query .= "                                                 AND B1.BASE_SEQ = '005', ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER             = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "         AND T1.SCHREGNO             = T2.SCHREGNO ";
        $query .= "         AND VALUE(S1.SICK, 0)      >= ".$model->field["SICK_CNT"]." ";
        $query .= "     ORDER BY ";
        $query .= "         T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        foreach ($model->checked as $schregno) {

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["BASE_SEQ"][TEXT]         = "005";
            $data["BASE_REMARK1"][TEXT]     = $model->field["BASE_REMARK1{$schregno}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO = '".$schregno."' ";
            $query .= "     AND BASE_SEQ = '005' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "         YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SCHREGNO     = '".$schregno."' ";
                $where .= "     AND BASE_SEQ     = '005' ";
                $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
