<?php

require_once('for_php7.php');

class knjg045aQuery extends Query {

    //校種
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //天気取得
    function getWeather($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //日誌取得
    function getDiary($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.REMARK1  AS IMPORTANT_MATTER, ";
        $query .= "     L1.REMARK2  AS GUEST, ";
        $query .= "     L1.REMARK3  AS REPORT, ";
        $query .= "     L1.REMARK4  AS RECEIVE_OFFICIAL_DOCUMENTS, ";
        $query .= "     L1.REMARK5  AS SENDING_OFFICIAL_DOCUMENTS, ";
        $query .= "     L1.REMARK6  AS BUSINESS_TRIP, ";
        $query .= "     L1.REMARK7  AS VACATION, ";
        $query .= "     L1.REMARK8  AS SPECIAL_LEAVE, ";
        $query .= "     L1.REMARK9  AS ABSENCE, ";
        $query .= "     L1.REMARK10 AS LATE_EARLY ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= "     LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L1 ON T1.SCHOOLCD = L1.SCHOOLCD ";
        $query .= "          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T1.DIARY_DATE = L1.DIARY_DATE ";
        $query .= " WHERE ";
        $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    //読込
    function getPermRequest($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     DATE(T1.SDATE) AS SDATE, ";
        $query .= "     DATE(T1.EDATE) AS EDATE, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     PERMREQUEST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     '".str_replace('/', '-', $model->diaryDate)."' BETWEEN DATE(T1.SDATE) AND DATE(T1.EDATE) ";
        $query .= "     AND T1.APPLYCD IN (SELECT ";
        $query .= "                            I1.NAMECD2 ";
        $query .= "                        FROM ";
        $query .= "                            NAME_MST I1 ";
        $query .= "                        WHERE ";
        $query .= "                            I1.NAMECD1 = 'G100' AND ";
        $query .= "                            I1.NAMESPARE1 = '{$cd}' ";
        $query .= "                         ) ";
        $query .= "     AND T1.PERM_CD = '1' ";

        return $query;
    }

    //MAX日付
    function getMaxDate($model)
    {
        $query  = " VALUES DAY(last_day(DATE('".str_replace('/', '-', $model->diaryDate)."'))) ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $dateArray = preg_split("/\//", $model->diaryDate);
        $setYear = $dateArray[1] <= "03" ? $dateArray[0] - 1 : $dateArray[0];
        $query .= " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     DAY(last_day(DATE(EDATE))) AS DAY_MAX ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$setYear}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER DESC ";
        return $query;
    }

    //EVENT_DATより学年ごとの休日数を取得（対象学年の全てのクラスが休日の時をカウントする）
    function getHoliCnt($model, $grade, $fDay, $eDay) {
        $dateArray = preg_split("/\//", $model->diaryDate);
        $setYear = $dateArray[1] <= "03" ? $dateArray[0] - 1 : $dateArray[0];
        $query  = " WITH REGD_HDAT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     COUNT(*) AS GRADE_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$setYear}' ";
        $query .= " AND SEMESTER = '{$model->kyouseiSeme}' ";
        $query .= " AND GRADE = '{$grade}' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ) ";
        $query .= " ,HOLIDAY_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     COUNT(*) AS GRADE_CNT ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.GRADE = T1.GRADE ";
        $query .= "                                  AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$setYear}' ";
        $query .= " AND L1.SEMESTER = '{$model->kyouseiSeme}' ";
        $query .= " AND L1.GRADE = '{$grade}' ";
        $query .= " AND T1.EXECUTEDATE BETWEEN '{$fDay}' AND '{$eDay}' ";
        $query .= " AND T1.HOLIDAY_FLG = '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.EXECUTEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS HOLI_CNT ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_DATA T1 ";
        $query .= "     INNER JOIN REGD_HDAT_DATA L1 ON L1.GRADE = T1.GRADE ";
        $query .= "                                 AND L1.GRADE_CNT = T1.GRADE_CNT ";

        return $query;
    }

    //学年取得
    function getGrade($model, $year) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出欠データ取得
    function getNurseoffAttendDat($model, $date) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE = '".$date."' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data = array();
        $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]                  = $model->schoolKind;
        $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
        $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
        $db->query($query);

        $data = array();
        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
        $data["DIARY_DATE"][DATE]   = $model->diaryDate;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK1"][TEXT]      = $model->field["IMPORTANT_MATTER"];
        $data["REMARK2"][TEXT]      = $model->field["GUEST"];
        $data["REMARK3"][TEXT]      = $model->field["REPORT"];
        $data["REMARK4"][TEXT]      = $model->field["RECEIVE_OFFICIAL_DOCUMENTS"];
        $data["REMARK5"][TEXT]      = $model->field["SENDING_OFFICIAL_DOCUMENTS"];
        $data["REMARK6"][TEXT]      = $model->field["BUSINESS_TRIP"];
        $data["REMARK7"][TEXT]      = $model->field["VACATION"];
        $data["REMARK8"][TEXT]      = $model->field["SPECIAL_LEAVE"];
        $data["REMARK9"][TEXT]      = $model->field["ABSENCE"];
        $data["REMARK10"][TEXT]     = $model->field["LATE_EARLY"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT");
        $db->query($query);

        //NURSEOFF_ATTEND_DAT
        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況更新
            foreach ($model->di_cd as $dkey => $dval) {
                foreach ($model->grade as $gkey => $gval) {
                    $data = array();
                    $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
                    $data["DATE"][DATE]         = $model->diaryDate;
                    $data["GRADE"][TEXT]        = $gkey;
                    $data["DI_CD"][TEXT]        = $dkey;
                    $data["CNT"][NUMBER]        = $model->att_field[$dkey][$gkey];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "NURSEOFF_ATTEND_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";

        $db->query($query);

        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND SEQ = '001' ";

        $db->query($query);

        //NURSEOFF_ATTEND_DAT
        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況
            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     DATE = '".str_replace("/", "-", $model->diaryDate)."' ";
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>