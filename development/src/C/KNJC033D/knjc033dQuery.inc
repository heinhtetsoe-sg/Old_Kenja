<?php

require_once('for_php7.php');

class knjc033dQuery extends Query {

    //ADMIN_CONTROL_DATチェック
    function checkAdminControlDat($model, $grade) {
        list ($year, $month, $day) = explode('-', str_replace("/","-",$model->cntl_dt_key));

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
            $query .= "          ON T1.YEAR         = REG_G.YEAR ";
            $query .= "         AND T1.SCHOOL_KIND  = REG_G.SCHOOL_KIND ";
            $query .= "         AND REG_G.GRADE     = '".$grade."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CONTROL_FLG  = '2' AND ";
        $query .= "     T1.CONTROL_CODE = '".$month."' ";

        return $query;
    }

    //SEMESTER_MST事前チェック
    function checkSemesterMst($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     '".str_replace("/","-",$model->cntl_dt_key)."' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年取得
    function getGrade($school_kind, $model) {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($grade, $hr_class="", $model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     CASE WHEN (T1.TR_CD1 = '".STAFFCD."' OR T1.TR_CD2 = '".STAFFCD."' OR T1.TR_CD3 = '".STAFFCD."') THEN '1' ";
        $query .= "          WHEN (T1.SUBTR_CD1 = '".STAFFCD."' OR T1.SUBTR_CD2 = '".STAFFCD."' OR T1.SUBTR_CD3 = '".STAFFCD."') THEN '1' ";
        $query .= "          ELSE '0' END AS HR_STAFF,  ";
        $query .= "     T2.EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN ATTEND_DAY_HRATE_DAT T2 ON T1.GRADE       = T2.GRADE ";
        $query .= "                                      AND T1.HR_CLASS    = T2.HR_CLASS ";
        $query .= "                                      AND T2.ATTENDDATE  = '".str_replace("/","-",$model->cntl_dt_key)."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE       = '".$grade."' ";
        if ($hr_class) {
            $query .= " AND T1.HR_CLASS    = '".$hr_class."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //ATTEND_DAY_HRATE_DAT存在チェック
    function checkAttendDayHrateDat($grade, $hr_class, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE  = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
        $query .= "     GRADE       = '".$grade."' AND ";
        $query .= "     HR_CLASS    = '".$hr_class."' ";

        return $query;
    }

    //更新（全員出席）
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        list ($grade, $hr_class) = explode('-', $model->grade_hr_class);
        list ($year, $month, $day) = explode('-', str_replace("/","-",$model->cntl_dt_key));

        //ATTEND_SEMES_DAT -- 削除
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     S1.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = S1.YEAR AND ";
        $query .= "                     T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                     S1.GRADE    = '".$grade."' AND ";
        $query .= "                     S1.HR_CLASS = '".$hr_class."') ";
        $db->query($query);

        //授業日数取得
        $lessonD = $db->getOne(knjc033dQuery::checkAttendSemesLessonDat($month, $grade, $hr_class, $model));
        $lessonM = $db->getOne(knjc033dQuery::checkAttendLessonMst($month, $grade));

        //初期値
        $default = ($model->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";

        //出欠集計データ取得
        $query = knjc033dQuery::getAttendSum($model, $month, $grade, $hr_class);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //授業日数
            $lesson = "";
            if ($lessonM != "") $lesson = $lessonM;
            if ($row["LESSON"] != "") $lesson = $row["LESSON"];
            if ($lessonD != "") $lesson = $lessonD;

            //ATTEND_SEMES_DAT -- 追加
            $data = array();
            $data["COPYCD"][TEXT]           = 0;
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["MONTH"][TEXT]            = $month;
            $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["APPOINTED_DAY"][TEXT]    = $row["APPOINTED_DAY"];
            $data["LESSON"][NUMBER]         = ($lesson) ? $lesson : 0;
            $data["OFFDAYS"][NUMBER]        = $default;
            $data["ABSENT"][NUMBER]         = ($row["ABSENT"] != "") ? $row["ABSENT"] : $default;
            $data["SUSPEND"][NUMBER]        = ($row["SUSPEND"] != "") ? $row["SUSPEND"] : $default;
            $data["MOURNING"][NUMBER]       = ($row["MOURNING"] != "") ? $row["MOURNING"] : $default;
            $data["ABROAD"][NUMBER]         = $default;
            $data["SICK"][NUMBER]           = $default;
            $data["NOTICE"][NUMBER]         = $default;
            $data["NONOTICE"][NUMBER]       = ($row["NONOTICE"] != "") ? $row["NONOTICE"] : $default;
            $data["LATE"][NUMBER]           = ($row["LATE"] != "") ? $row["LATE"] : $default;
            $data["EARLY"][NUMBER]          = ($row["EARLY"] != "") ? $row["EARLY"] : $default;
            $data["KEKKA_JISU"][NUMBER]     = $default;
            $data["KEKKA"][NUMBER]          = $default;
            $data["LATEDETAIL"][NUMBER]     = $default;
            $data["VIRUS"][NUMBER]          = $default;
            $data["KOUDOME"][NUMBER]        = $default;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
            $db->query($query);
        }
        $result->free();

        //ATTEND_DAY_HRATE_DAT存在チェック
        $cnt = $db->getOne(knjc033dQuery::checkAttendDayHrateDat($grade, $hr_class, $model));

        $data = array();
        if ($cnt == 0) {
            $data["ATTENDDATE"][TEXT]   = str_replace("/","-",$model->cntl_dt_key);
            $data["GRADE"][TEXT]        = $grade;
            $data["HR_CLASS"][TEXT]     = $hr_class;
            $data["ATTESTOR"][TEXT]     = STAFFCD;
        }
        $data["EXECUTED"][TEXT]     = 1;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($cnt == 0) {
            //追加
            $query = Query::insertSQL($data, "ATTEND_DAY_HRATE_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     ATTENDDATE  = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
            $where .= "     GRADE       = '".$grade."' AND ";
            $where .= "     HR_CLASS    = '".$hr_class."' ";

            //更新
            $query = Query::updateSQL($data, "ATTEND_DAY_HRATE_DAT", $where);
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //ATTEND_SEMES_LESSON_DAT事前チェック
    function checkAttendSemesLessonDat($month, $grade, $hr_class, $model) {
        $query  = " SELECT ";
        $query .= "     LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_LESSON_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$grade."' AND ";
        $query .= "     HR_CLASS    = '".$hr_class."' ";

        return $query;
    }

    //ATTEND_LESSON_MST事前チェック
    function checkAttendLessonMst($month, $grade) {
        $query  = " SELECT ";
        $query .= "     LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$grade."' AND ";
        $query .= "     COURSECD    = '0' AND ";
        $query .= "     MAJORCD     = '000' ";

        return $query;
    }

    //出欠コード一覧取得
    function getC001($flg, $di_cd, $model) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 IN ('".implode($di_cd,"','")."') ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     NAMESPARE2, ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //生徒取得
    function getStudent($model, $flg) {
        $date = str_replace("/","-",$model->cntl_dt_key);

        if ($flg == "cnt") {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
        } else {
            $query  = " WITH IDOU AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_BASE_MST T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "         T1.GRADE    = '".$model->grade."' AND ";
            $query .= "         T1.HR_CLASS = '".$model->hr_class."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         ((T2.GRD_DIV IN ('1','2','3') AND T2.GRD_DATE < '".$date."') OR ";
            $query .= "          (T2.ENT_DIV IN('4','5') AND T2.ENT_DATE > '".$date."')) ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_TRANSFER_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "         T1.GRADE    = '".$model->grade."' AND ";
            $query .= "         T1.HR_CLASS = '".$model->hr_class."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.TRANSFERCD IN ('1','2') AND ";
            $query .= "         '".$date."' BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T2.NAME_SHOW, ";
            $query .= "     CASE WHEN T3.SCHREGNO IS NULL THEN '' ELSE '1' END AS IDOU ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        if ($flg == "list") {
            $query .= "     LEFT JOIN IDOU T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = '".$model->grade."' AND ";
        $query .= "     T1.HR_CLASS = '".$model->hr_class."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     T1.ATTENDNO ";
        }

        return $query;
    }

    //出欠コード取得
    function getDI_CD($schregno, $di_cd, $model) {
        $query  = " SELECT ";
        $query .= "     DI_CD ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$schregno."' AND ";
        $query .= "     ATTENDDATE   = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
        $query .= "     YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     DI_CD IN ('".implode($di_cd,"','")."') ";

        return $query;
    }

    //更新（確定）
    function getUpdateQuery2($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $attend = array("ATTEND1", "ATTEND2", "ATTEND3");

        if ($model->schregno) {
            $schregno = explode(',', $model->schregno);
            for ($i=0; $i < get_count($schregno); $i++) {
                //ATTEND_DAY_DAT -- 削除
                $query  = " DELETE FROM ";
                $query .= "     ATTEND_DAY_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO     = '".$schregno[$i]."' AND ";
                $query .= "     ATTENDDATE   = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
                $query .= "     YEAR         = '".CTRL_YEAR."' ";
                $db->query($query);

                //ATTEND_DAY_DAT -- 追加
                for ($j=0; $j < get_count($attend); $j++) {
                    if ($model->field[$schregno[$i]][$attend[$j]]) {
                        $data = array();
                        $data["SCHREGNO"][TEXT]     = $schregno[$i];
                        $data["ATTENDDATE"][TEXT]   = str_replace("/","-",$model->cntl_dt_key);
                        $data["DI_CD"][TEXT]        = $model->field[$schregno[$i]][$attend[$j]];
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "ATTEND_DAY_DAT");
                        $db->query($query);
                    }
                }
            }

            list ($year, $month, $day) = explode('-', str_replace("/","-",$model->cntl_dt_key));

            //ATTEND_SEMES_DAT -- 削除
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH       = '".$month."' AND ";
            $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
            $query .= "     SCHREGNO IN ('".implode("','", explode(',', $model->schregno))."') ";
            $db->query($query);

            //授業日数取得
            $lessonD = $db->getOne(knjc033dQuery::checkAttendSemesLessonDat($month, $model->grade, $model->hr_class, $model));
            $lessonM = $db->getOne(knjc033dQuery::checkAttendLessonMst($month, $model->grade));

            //初期値
            $default = ($model->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";

            //出欠集計データ取得
            $query = knjc033dQuery::getAttendSum($model, $month);

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //授業日数
                $lesson = "";
                if ($lessonM != "") $lesson = $lessonM;
                if ($row["LESSON"] != "") $lesson = $row["LESSON"];
                if ($lessonD != "") $lesson = $lessonD;

                //ATTEND_SEMES_DAT -- 追加
                $data = array();
                $data["COPYCD"][TEXT]           = 0;
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["MONTH"][TEXT]            = $month;
                $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["APPOINTED_DAY"][TEXT]    = $row["APPOINTED_DAY"];
                $data["LESSON"][NUMBER]         = ($lesson) ? $lesson : 0;
                $data["OFFDAYS"][NUMBER]        = $default;
                $data["ABSENT"][NUMBER]         = ($row["ABSENT"] != "") ? $row["ABSENT"] : $default;
                $data["SUSPEND"][NUMBER]        = ($row["SUSPEND"] != "") ? $row["SUSPEND"] : $default;
                $data["MOURNING"][NUMBER]       = ($row["MOURNING"] != "") ? $row["MOURNING"] : $default;
                $data["ABROAD"][NUMBER]         = $default;
                $data["SICK"][NUMBER]           = $default;
                $data["NOTICE"][NUMBER]         = $default;
                $data["NONOTICE"][NUMBER]       = ($row["NONOTICE"] != "") ? $row["NONOTICE"] : $default;
                $data["LATE"][NUMBER]           = ($row["LATE"] != "") ? $row["LATE"] : $default;
                $data["EARLY"][NUMBER]          = ($row["EARLY"] != "") ? $row["EARLY"] : $default;
                $data["KEKKA_JISU"][NUMBER]     = $default;
                $data["KEKKA"][NUMBER]          = $default;
                $data["LATEDETAIL"][NUMBER]     = $default;
                $data["VIRUS"][NUMBER]          = $default;
                $data["KOUDOME"][NUMBER]        = $default;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                $db->query($query);
            }
            $result->free();

            //ATTEND_DAY_HRATE_DAT -- 存在チェック
            $cnt = $db->getOne(knjc033dQuery::checkAttendDayHrateDat($model->grade, $model->hr_class, $model));

            $data = array();
            if ($cnt == 0) {
                $data["ATTENDDATE"][TEXT]   = str_replace("/","-",$model->cntl_dt_key);
                $data["GRADE"][TEXT]        = $model->grade;
                $data["HR_CLASS"][TEXT]     = $model->hr_class;
                $data["ATTESTOR"][TEXT]     = STAFFCD;
            }
            $data["EXECUTED"][TEXT]     = 1;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($cnt == 0) {
                //ATTEND_DAY_HRATE_DAT -- 追加
                $query = Query::insertSQL($data, "ATTEND_DAY_HRATE_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     ATTENDDATE  = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
                $where .= "     GRADE       = '".$model->grade."' AND ";
                $where .= "     HR_CLASS    = '".$model->hr_class."' ";

                //ATTEND_DAY_HRATE_DAT --  更新
                $query = Query::updateSQL($data, "ATTEND_DAY_HRATE_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //出欠集計データ取得
    function getAttendSum($model, $month, $grade="", $hr_class="") {
        $query  = " WITH APP_DAY AS ( ";
        $query .= "     SELECT ";
        $query .= "         MONTH, ";
        $query .= "         APPOINTED_DAY ";
        $query .= "     FROM ";
        $query .= "         APPOINTED_DAY_MST AM ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN ";
            $query .= "         SCHREG_REGD_GDAT SGD ";
            $query .= "         ON  ";
            $query .= "         AM.YEAR = SGD.YEAR AND ";
            $query .= "         AM.SCHOOL_KIND = SGD.SCHOOL_KIND AND ";
            $query .= "         SGD.GRADE      = '".$model->grade."' ";
        }
        $query .= "     WHERE ";
        $query .= "         AM.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         AM.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " ), SEME AS ( ";
        $query .= "     SELECT ";
        $query .= "         MONTH(EDATE) AS LAST_MONTH, ";
        $query .= "         RTRIM(CHAR(DAY(EDATE))) AS DAY ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        foreach($model->use_di_cd as $key => $val) {
            $query .= "         ,SUM(CASE T1.DI_CD WHEN '".$key."' THEN 1 ELSE 0 END) AS ".$val[0]." ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_DAY_DAT T1, ";
        $query .= "         SEMESTER_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         MONTH(T1.ATTENDDATE) = INT('".$month."') AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        foreach($model->use_di_cd as $key => $val) {
            $query .= "     L1.".$val[0].", ";
        }
        $query .= "     CASE WHEN L2.APPOINTED_DAY IS NOT NULL THEN L2.APPOINTED_DAY ";
        $query .= "          WHEN L3.DAY IS NOT NULL THEN L3.DAY ";
        $query .= "          ELSE RTRIM(CHAR(DAY(LAST_DAY(DATE('".str_replace("/","-",$model->cntl_dt_key)."'))))) END AS APPOINTED_DAY, ";
        $query .= "     L4.LESSON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN MAIN L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN APP_DAY L2 ON L2.MONTH = '".$month."' ";
        $query .= "     LEFT JOIN SEME L3 ON L3.LAST_MONTH = INT('".$month."') ";
        $query .= "     LEFT JOIN ATTEND_LESSON_MST L4 ON L4.YEAR       = T1.YEAR ";
        $query .= "                                   AND L4.MONTH      = '".$month."' ";
        $query .= "                                   AND L4.SEMESTER   = T1.SEMESTER ";
        $query .= "                                   AND L4.GRADE      = T1.GRADE ";
        $query .= "                                   AND L4.COURSECD   = T1.COURSECD ";
        $query .= "                                   AND L4.MAJORCD    = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($grade) {
            $query .= "     T1.SCHREGNO IN (SELECT ";
            $query .= "                         S1.SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1 ";
            $query .= "                     WHERE ";
            $query .= "                         T1.YEAR     = S1.YEAR AND ";
            $query .= "                         T1.SEMESTER = S1.SEMESTER AND ";
            $query .= "                         S1.GRADE    = '".$grade."' AND ";
            $query .= "                         S1.HR_CLASS = '".$hr_class."') ";
        } else {
            $query .= "     T1.SCHREGNO IN ('".implode("','", explode(',', $model->schregno))."') ";
        }

        return $query;
    }

    //削除（全てクリア）
    function getDeleteQuery2($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->schregno) {
            $schregno = explode(',', $model->schregno);
            for ($i=0; $i < get_count($schregno); $i++) {
                //ATTEND_DAY_DAT -- 削除
                $query  = " DELETE FROM ";
                $query .= "     ATTEND_DAY_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO     = '".$schregno[$i]."' AND ";
                $query .= "     ATTENDDATE   = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
                $query .= "     YEAR         = '".CTRL_YEAR."' ";
                $db->query($query);
            }

            list ($year, $month, $day) = explode('-', str_replace("/","-",$model->cntl_dt_key));

            //ATTEND_SEMES_DAT -- 削除
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH       = '".$month."' AND ";
            $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
            $query .= "     SCHREGNO IN ('".implode("','", explode(',', $model->schregno))."') ";
            $db->query($query);

            //授業日数取得
            $lessonD = $db->getOne(knjc033dQuery::checkAttendSemesLessonDat($month,  $model->grade, $model->hr_class, $model));
            $lessonM = $db->getOne(knjc033dQuery::checkAttendLessonMst($month, $model->grade));

            //初期値
            $default = ($model->Properties["use_Attend_zero_hyoji"] == "1") ? "" : "0";

            //出欠集計データ取得
            $query = knjc033dQuery::getAttendSum($model, $month);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //授業日数
                $lesson = "";
                if ($lessonM != "") $lesson = $lessonM;
                if ($row["LESSON"] != "") $lesson = $row["LESSON"];
                if ($lessonD != "") $lesson = $lessonD;

                //ATTEND_SEMES_DAT -- 追加
                $data = array();
                $data["COPYCD"][TEXT]           = 0;
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["MONTH"][TEXT]            = $month;
                $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["APPOINTED_DAY"][TEXT]    = $row["APPOINTED_DAY"];
                $data["LESSON"][NUMBER]         = ($lesson) ? $lesson : 0;
                $data["OFFDAYS"][NUMBER]        = $default;
                $data["ABSENT"][NUMBER]         = ($row["ABSENT"] != "") ? $row["ABSENT"] : $default;
                $data["SUSPEND"][NUMBER]        = ($row["SUSPEND"] != "") ? $row["SUSPEND"] : $default;
                $data["MOURNING"][NUMBER]       = ($row["MOURNING"] != "") ? $row["MOURNING"] : $default;
                $data["ABROAD"][NUMBER]         = $default;
                $data["SICK"][NUMBER]           = $default;
                $data["NOTICE"][NUMBER]         = $default;
                $data["NONOTICE"][NUMBER]       = ($row["NONOTICE"] != "") ? $row["NONOTICE"] : $default;
                $data["LATE"][NUMBER]           = ($row["LATE"] != "") ? $row["LATE"] : $default;
                $data["EARLY"][NUMBER]          = ($row["EARLY"] != "") ? $row["EARLY"] : $default;
                $data["KEKKA_JISU"][NUMBER]     = $default;
                $data["KEKKA"][NUMBER]          = $default;
                $data["LATEDETAIL"][NUMBER]     = $default;
                $data["VIRUS"][NUMBER]          = $default;
                $data["KOUDOME"][NUMBER]        = $default;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                $db->query($query);
            }
            $result->free();

            //ATTEND_DAY_HRATE_DAT -- 削除
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_DAY_HRATE_DAT ";
            $query .= " WHERE ";
            $query .= "     ATTENDDATE  = '".str_replace("/","-",$model->cntl_dt_key)."' AND ";
            $query .= "     GRADE       = '".$model->grade."' AND ";
            $query .= "     HR_CLASS    = '".$model->hr_class."' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
