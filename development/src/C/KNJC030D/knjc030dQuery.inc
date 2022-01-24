<?php

require_once('for_php7.php');

class knjc030dQuery extends Query {

    //学期取得
    function getSemester($date, $semester="")
    {
        $date = str_replace("/", "-", $date);

        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     '".$date."' BETWEEN SDATE AND EDATE ";
        if($semester) {
        $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model, $semester) {
        if ($model->field["HR_CLASS_TYPE"] == "2") {
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_HDAT";
            } else {
                $tablename = "SCHREG_REGD_GHR_HDAT";
            }
        } else {
            $tablename = "SCHREG_REGD_HDAT";
        }

        $query  = " SELECT ";
        if ($tablename == "SCHREG_REGD_GHR_HDAT") {
            $query .= "     T1.GHR_CD AS VALUE, ";
            $query .= "     T1.GHR_NAME AS LABEL ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        if ($tablename != "SCHREG_REGD_GHR_HDAT") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                    $query .= "          ON G1.YEAR     = T1.YEAR ";
                    $query .= "         AND G1.GRADE    = T1.GRADE ";
                    $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";
        if(AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //異動データ取得
    function getIdouData($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namespare1) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE1  = '".$namespare1."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst2($namecd1, $namespare1="") {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' ";
        if ($namespare1) {
            $query .= " AND NAMESPARE1  = '".$namespare1."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";

        return $query;
    }

    //終了時メッセージ用
    function getCloseMsg($model, $semester) {
        $date = str_replace("/", "-", $model->field["DATE"]);
        if ($model->field["HR_CLASS_TYPE"] == "2") {
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_DAT";
            } else {
                $tablename = "SCHREG_REGD_GHR_DAT";
            }
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }

        $query  = " SELECT ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     {$tablename} REGD ";
        $query .= "     INNER JOIN ATTEND_DAY_DAT DAY_D ON REGD.SCHREGNO = DAY_D.SCHREGNO ";
        $query .= "           AND DAY_D.ATTENDDATE = '{$date}' ";
        $query .= "           AND DAY_D.DI_CD IN ('4', '5', '6') ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '{$semester}' ";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "     AND REGD.GHR_CD   = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "     REGD.GHR_ATTENDNO ";
        } else {
            $query .= "     REGD.ATTENDNO ";
        }

        return $query;
    }

    //出欠データ取得
    function selectAttendQuery($model, $semester, $subl_data)
    {
        $date = str_replace("/", "-", $model->field["DATE"]);
        if ($model->field["HR_CLASS_TYPE"] == "2") {
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_DAT";
            } else {
                $tablename = "SCHREG_REGD_GHR_DAT";
            }
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "         T1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "         T1.ATTENDNO, ";
        }
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .=           $tablename." T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "         T1.GHR_CD   = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.DI_CD, ";
        $query .= "         T3.SUBL_CD, ";
        $query .= "         T4.SUBM_CD ";
        $query .= "     FROM ";
        $query .= "         SCHINFO T1 ";
        $query .= "         LEFT JOIN ATTEND_DAY_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             T2.ATTENDDATE = '".$date."' ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBL_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO AND T2.YEAR = T3.YEAR AND ";
        $query .= "             T2.ATTENDDATE = T3.ATTENDDATE AND T2.DI_CD = T3.DI_CD ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBM_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO AND T2.YEAR = T4.YEAR AND ";
        $query .= "             T2.ATTENDDATE = T4.ATTENDDATE AND T2.DI_CD = T4.DI_CD AND T3.SUBL_CD = T4.SUBL_CD ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CASE DI_CD WHEN '1' THEN '1' ELSE '0' END ABSENT, ";
        $query .= "         CASE DI_CD WHEN '2' THEN '1' ELSE '0' END SUSPEND, ";
        $query .= "         CASE DI_CD WHEN '3' THEN '1' ELSE '0' END MOURNING, ";
        $query .= "         CASE DI_CD WHEN '4' THEN '1' ELSE '0' END SICK, ";
        $query .= "         CASE DI_CD WHEN '5' THEN '1' ELSE '0' END NOTICE, ";
        $query .= "         CASE DI_CD WHEN '6' THEN '1' ELSE '0' END NONOTICE, ";

        foreach ($subl_data as $key) {
            $query .= "         CASE DI_CD WHEN '6' THEN (CASE SUBL_CD WHEN '".$key."' THEN '1' ELSE '0' END) ELSE '0' END CNT".$key.", ";
        }

        $query .= "         CASE DI_CD WHEN '15' THEN '1' ELSE '0' END LATE, ";
        $query .= "         CASE DI_CD WHEN '16' THEN '1' ELSE '0' END EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(INT(ABSENT)) AS ABSENT, ";
        $query .= "         SUM(INT(SUSPEND)) AS SUSPEND, ";
        $query .= "         SUM(INT(MOURNING)) AS MOURNING, ";
        $query .= "         SUM(INT(SICK)) AS SICK, ";
        $query .= "         SUM(INT(NOTICE)) AS NOTICE, ";
        $query .= "         SUM(INT(NONOTICE)) AS NONOTICE, ";

        foreach ($subl_data as $key) {
            $query .= "         SUM(INT(CNT".$key.")) AS CNT".$key.", ";
        }

        $query .= "         SUM(INT(LATE)) AS LATE, ";
        $query .= "         SUM(INT(EARLY)) AS EARLY, ";
        $query .= "         SUM(INT(ABSENT)+INT(SUSPEND)+INT(MOURNING)+INT(SICK)+INT(NOTICE)+INT(NONOTICE)+INT(LATE)+INT(EARLY)) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), ATTTEND_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.REMARK ";
        $query .= "     FROM ";
        $query .= "         SCHINFO T1 ";
        $query .= "         LEFT JOIN ATTEND_ABSENCE_REMARK_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T2.ABSENCE_DATE = '".$date."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.ABSENT, ";
        $query .= "     T2.SUSPEND, ";
        $query .= "     T2.MOURNING, ";
        $query .= "     T2.SICK, ";
        $query .= "     T2.NOTICE, ";
        $query .= "     T2.NONOTICE, ";

        foreach ($subl_data as $key) {
            $query .= "     T2.CNT".$key.", ";
        }

        $query .= "     T2.LATE, ";
        $query .= "     T2.EARLY, ";
        $query .= "     T2.TOTAL, ";
        $query .= "     T3.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN MAIN T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN ATTTEND_REMARK T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //出欠データの更新
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $date = str_replace("/", "-", $model->field["DATE"]);
        //年度データにない名称コードを取得
        $c001Namecd1 = ($model->schoolkind) ? "C".$model->schoolkind."01" : "C001";
        $delete_day     = $db->getCol(knjc030dQuery::getNotExistNameMst($c001Namecd1));
        $delete_subl    = $db->getCol(knjc030dQuery::getNotExistNameMst('C006'));
        $delete_subm    = $db->getCol(knjc030dQuery::getNotExistNameMst('C007'));

        foreach($model->field["SCHREGNO"] as $key => $schregno){
            //不要なデータを削除
            if(get_count($delete_day) > 0) {
                foreach($delete_day as $dayKey) {
                    //ATTEND_DAY_DATの削除
                    $query = knjc030dQuery::deleteAttendDayDat($schregno, $date, $dayKey);
                    $db->query($query);
                    //ATTEND_DAY_SUBL_DATの削除
                    $query = knjc030dQuery::deleteAttendDaySublDat($schregno, $date, $dayKey);
                    $db->query($query);
                    //ATTEND_DAY_SUBM_DATの削除
                    $query = knjc030dQuery::deleteAttendDaySubmDat($schregno, $date, $dayKey);
                    $db->query($query);
                }
            }
            if(get_count($delete_subl) > 0) {
                foreach($delete_subl as $sublKey) {
                    //ATTEND_DAY_SUBL_DATの削除
                    $query = knjc030dQuery::deleteAttendDaySublDat($schregno, $date, "", $sublKey);
                    $db->query($query);
                    //ATTEND_DAY_SUBM_DATの削除
                    $query = knjc030dQuery::deleteAttendDaySubmDat($schregno, $date, "", $sublKey);
                    $db->query($query);
                }
            }
            if(get_count($delete_subm) > 0) {
                foreach($delete_subm as $submKey) {
                    //ATTEND_DAY_SUBM_DATの削除
                    $query = knjc030dQuery::deleteAttendDaySubmDat($schregno, $date, "", "", $submKey);
                    $db->query($query);
                }
            }
            //ATTEND_ABSENCE_REMARK_DATの削除
            $query = knjc030dQuery::deleteAttendAbsenceRemarkDat($schregno, $date);
            $db->query($query);

            $subl_check = 0;
            $nonotice = false;
            //大分類に出欠がある生徒を抽出（欠席以外）
            foreach($model->attendItem as $attKey => $attVal) {
                if(substr($attKey,0,3) == 'CNT') $nonotice = true;
                if(substr($attKey,0,3) != 'CNT' && $attVal[0] != '6') {
                    $subl_check = $subl_check + $db->getOne(knjc030dQuery::checkAttendDaySublDat($model, $schregno, $attVal[0]));
                }
            }

            //欠席の中分類の存在チェック
            $subm_check = $db->getOne(knjc030dQuery::checkAttendDaySubmDat($model, $schregno, '6'));

            if($nonotice && $subm_check == "0") {
                //ATTEND_DAY_SUBL_DATの削除
                $query = knjc030dQuery::deleteAttendDayDat($schregno, $date, '6');
                $db->query($query);

                //ATTEND_DAY_DATの削除
                $query = knjc030dQuery::deleteAttendDaySublDat($schregno, $date, '6');
                $db->query($query);
            }

            foreach($model->attendItem as $attKey => $attVal) {
                //ATTEND_DAY_SUBL_DATの処理
                if(substr($attKey,0,3) == 'CNT') {
                    $subl_cd = substr($attKey,3);

                    //大分類に出欠がある生徒は除く（欠席以外）
                    if($subl_check > "0") continue;

                    //大分類に出欠がある生徒は除く（欠席）
                    $subl_exist_check = $db->getOne(knjc030dQuery::checkAttendDaySublDat($model, $schregno, '6'));
                    if($subl_exist_check > "0") continue;

                    //中分類存在チェック
                    $subm_cnt = get_count($db->getCol(knjc030dQuery::getNameMst('C007', $subl_cd)));
                    if($subm_cnt > "0") continue;

                    if($model->field["SELECTDATA_{$schregno}_{$attKey}"] == "1") {

                        //ATTEND_DAY_SUBL_DATの追加
                        $dataL = array();
                        $dataL["SCHREGNO"][TEXT]    = $schregno;
                        $dataL["ATTENDDATE"][TEXT]  = $date;
                        $dataL["DI_CD"][TEXT]       = '6';
                        $dataL["SUBL_CD"][TEXT]     = $subl_cd;
                        $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataL["REGISTERCD"][TEXT]  = STAFFCD ;
                        $dataL["UPDATED"][FUNC]     = "sysdate()";

                        $query = Query::insertSQL($dataL, "ATTEND_DAY_SUBL_DAT");
                        $db->query($query);

                        //ATTEND_DAY_DATの追加
                        $data = array();
                        $data["SCHREGNO"][TEXT]     = $schregno;
                        $data["ATTENDDATE"][TEXT]   = $date;
                        $data["DI_CD"][TEXT]        = '6';
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["REGISTERCD"][TEXT]   = STAFFCD ;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "ATTEND_DAY_DAT");
                        $db->query($query);

                    }

                //ATTEND_DAY_DATの処理
                } else {

                    //大分類存在チェック
                    $subl_cnt = get_count($db->getCol(knjc030dQuery::getNameMst('C006', $attVal[0])));
                    $subl_data_cnt = $db->getOne(knjc030dQuery::checkAttendDaySublDat($model, $schregno, $attVal[0]));
                    if($subl_cnt > "0" && $subl_data_cnt > "0") continue;

                    //ATTEND_DAY_DATの削除
                    $query = knjc030dQuery::deleteAttendDayDat($schregno, $date, $attVal[0]);
                    $db->query($query);

                    if($model->field["SELECTDATA_{$schregno}_{$attKey}"] == 1) {
                        //ATTEND_DAY_DATの追加
                        $data = array();
                        $data["SCHREGNO"][TEXT]     = $schregno;
                        $data["ATTENDDATE"][TEXT]   = $date;
                        $data["DI_CD"][TEXT]        = $attVal[0];
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["REGISTERCD"][TEXT]   = STAFFCD ;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "ATTEND_DAY_DAT");
                        $db->query($query);
                    }
                }
            }
            if($model->field["REMARK_{$schregno}"] != "") {
                //ATTEND_ABSENCE_REMARK_DATの追加
                $data = array();
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["ABSENCE_DATE"][TEXT]     = $date;
                $data["REMARK"][TEXT]           = $model->field["REMARK_{$schregno}"];
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEMESTER"][TEXT]         = $db->getOne(knjc030dQuery::getSemester($date));
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_ABSENCE_REMARK_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        //更新（出欠入力完了）
        $result = knjc030dQuery::getHrateUpdateQuery($model);

        return true;
    }

    //年度データにない名称コード取得
    function getNotExistNameMst($namecd1) {

        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        if (substr($namecd1,-2) == '01') {
            $query .= "     NAMECD2 IN ('1','2','3','4','5','6','15','16') AND ";
        }
        $query .= "     NAMECD2 NOT IN (SELECT ";
        $query .= "                         NAMECD2 ";
        $query .= "                     FROM ";
        $query .= "                         V_NAME_MST ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "                         NAMECD1 = '".$namecd1."' ";
        $query .= "                     ) ";

        return $query;
    }

    //ATTEND_DAY_DAT削除
    function deleteAttendDayDat($schregno, $date, $di_cd) {

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' AND ";
        $query .= "     DI_CD       = '".$di_cd."' ";

        return $query;
    }

    //ATTEND_DAY_SUBL_DAT削除
    function deleteAttendDaySublDat($schregno, $date, $di_cd="", $subl_cd="") {

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_SUBL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";
        if($di_cd)   $query .= " AND DI_CD      = '".$di_cd."' ";
        if($subl_cd) $query .= " AND SUBL_CD    = '".$subl_cd."' ";

        return $query;
    }

    //ATTEND_DAY_SUBM_DAT削除
    function deleteAttendDaySubmDat($schregno, $date, $di_cd="", $subl_cd="", $subm_cd="") {

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";
        if($di_cd)   $query .= " AND DI_CD      = '".$di_cd."' ";
        if($subl_cd) $query .= " AND SUBL_CD    = '".$subl_cd."' ";
        if($subm_cd) $query .= " AND SUBM_CD    = '".$subm_cd."' ";

        return $query;
    }

    //ATTEND_ABSENCE_REMARK_DAT削除
    function deleteAttendAbsenceRemarkDat($schregno, $date) {

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_ABSENCE_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ABSENCE_DATE  = '".$date."' ";

        return $query;
    }

    //ATTEND_DAY_DATチェック
    function checkAttendDayDat($model, $schregno) {

        $date = str_replace("/", "-", $model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";

        return $query;
    }

    //ATTEND_DAY_SUBL_DATチェック
    function checkAttendDaySublDat($model, $schregno, $di_cd, $flg="") {

        $date = str_replace("/", "-", $model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1, ";
        $query .= "     ATTEND_DAY_SUBL_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1      = 'C006' AND ";
        $query .= "     T2.SCHREGNO     = '".$schregno."' AND ";
        $query .= "     T2.ATTENDDATE   = '".$date."' AND ";
        $query .= "     T2.DI_CD        = T1.NAMESPARE1 AND ";
        $query .= "     T2.SUBL_CD      = T1.NAMECD2 ";
        if(!$flg) {
            $query .= "     AND T2.DI_CD        = '".$di_cd."' ";
        } else {
            $query .= "     AND T2.DI_CD        <> '6' ";
        }

        return $query;
    }

    //ATTEND_DAY_SUBM_DATチェック
    function checkAttendDaySubmDat($model, $schregno, $di_cd, $flg="") {

        $date = str_replace("/", "-", $model->field["DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$schregno."' AND ";
        $query .= "     ATTENDDATE   = '".$date."' AND ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        if(!$flg) {
            $query .= "     AND DI_CD        = '".$di_cd."' ";
        }

        return $query;
    }

    //更新（出欠入力完了・出欠完了取消）
    function getHrateUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $hr_class = substr($model->field["GRADE_HR_CLASS"], 2);

        //ATTEND_DAY_HRATE_DAT存在チェック
        $getData = $db->getOne(knjc030dQuery::checkAttendDayHrateDat($grade, $hr_class, $model));

        //出欠済みフラグ
        $executed = ($model->cmd == "cancelHrAte") ? "" : 1;

        $data = array();
        if ($cnt == 0) {
            $data["ATTENDDATE"][TEXT]   = str_replace("/", "-", $model->field["DATE"]);
            $data["GRADE"][TEXT]        = $grade;
            $data["HR_CLASS"][TEXT]     = $hr_class;
            $data["ATTESTOR"][TEXT]     = STAFFCD;
        }
        $data["EXECUTED"][TEXT]     = $executed;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if (strlen($getData) == 0) {
            //追加
            $query = Query::insertSQL($data, "ATTEND_DAY_HRATE_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     ATTENDDATE  = '".str_replace("/","-",$model->field["DATE"])."' AND ";
            $where .= "     GRADE       = '{$grade}' AND ";
            $where .= "     HR_CLASS    = '{$hr_class}' ";

            //更新
            $query = Query::updateSQL($data, "ATTEND_DAY_HRATE_DAT", $where);
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //ATTEND_DAY_HRATE_DAT存在チェック
    function checkAttendDayHrateDat($grade, $hr_class, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE  = '".str_replace("/", "-", $model->field["DATE"])."' AND ";
        $query .= "     GRADE       = '{$grade}' AND ";
        $query .= "     HR_CLASS    = '{$hr_class}' ";

        return $query;
    }
}
?>
