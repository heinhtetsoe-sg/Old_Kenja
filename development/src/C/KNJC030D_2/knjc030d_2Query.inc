<?php

require_once('for_php7.php');

class knjc030d_2Query extends Query {

    //クラス名取得
    function getHrName($model)
    {
        if ($model->hr_class_type == "2") {
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
            $query .= "     GHR_NAME ";
        } else {
            $query .= "     HR_NAME ";
        }
        $query .= " FROM ";
        $query .=       $tablename;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        if ($tablename == "SCHREG_REGD_GHR_HDAT") {
            $query .= "     GHR_CD      = '".$model->grade_hr_class."' ";
        } else {
            $query .= "     GRADE || HR_CLASS = '".$model->grade_hr_class."' ";
        }

        return $query;
    }

    //欠課種別取得
    function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C001' ";
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
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst2($namecd1, $namecd2) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMECD2     = '".$namecd2."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst3($namecd1, $namecd2) {

        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMECD2     = '".$namecd2."' ";

        return $query;
    }

    //出欠データ取得
    function selectAttendQuery($model, $sub_data, $namespare1)
    {
        $date = str_replace("/", "-", $model->date);
        if ($model->hr_class_type == "2") {
            if ($model->Properties["useFi_Hrclass"] == "1") {
                $tablename = "SCHREG_REGD_FI_DAT";
            } else {
                $tablename = "SCHREG_REGD_GHR_DAT";
            }
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "     T1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "     T1.ATTENDNO, ";
        }
        $query .= "     T2.NAME_SHOW ";

        foreach ($sub_data as $key) {
            $query .= " ,SUB".$key.".CNT".$key." ";
        }

        $query .= " FROM ";
        $query .=       $tablename." T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";

        if (substr($model->title,0,3) != 'CNT') {
            foreach ($sub_data as $key) {
                $query .= " LEFT JOIN ( SELECT ";
                $query .= "                 SCHREGNO, ";
                $query .= "                 COUNT(*) AS CNT".$key." ";
                $query .= "             FROM ";
                $query .= "                 ATTEND_DAY_SUBL_DAT ";
                $query .= "             WHERE ";
                $query .= "                 ATTENDDATE  = '".$date."' AND ";
                $query .= "                 DI_CD       = '".$namespare1."' AND ";
                $query .= "                 SUBL_CD     = '".$key."'  ";
                $query .= "             GROUP BY ";
                $query .= "                 SCHREGNO ";
                $query .= "           ) AS SUB".$key." ON SUB".$key.".SCHREGNO = T1.SCHREGNO ";
            }
        } else {
            foreach ($sub_data as $key) {
                $query .= " LEFT JOIN ( SELECT ";
                $query .= "                 SCHREGNO, ";
                $query .= "                 COUNT(*) AS CNT".$key." ";
                $query .= "             FROM ";
                $query .= "                 ATTEND_DAY_SUBM_DAT ";
                $query .= "             WHERE ";
                $query .= "                 ATTENDDATE = '".$date."' AND ";
                $query .= "                 DI_CD = (SELECT ";
                $query .= "                             NAMESPARE1 ";
                $query .= "                          FROM ";
                $query .= "                             V_NAME_MST ";
                $query .= "                          WHERE ";
                $query .= "                             YEAR    = '".CTRL_YEAR."' AND ";
                $query .= "                             NAMECD1 = 'C006' AND ";
                $query .= "                             NAMECD2 = '".$namespare1."') AND ";
                $query .= "                 SUBL_CD = '".$namespare1."' AND ";
                $query .= "                 SUBM_CD = '".$key."'  ";
                $query .= "             GROUP BY ";
                $query .= "                 SCHREGNO ";
                $query .= "           ) AS SUB".$key." ON SUB".$key.".SCHREGNO = T1.SCHREGNO ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND";
        if ($tablename == "SCHREG_REGD_GHR_DAT") {
            $query .= "     T1.GHR_CD   = '".$model->grade_hr_class."' ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->grade_hr_class."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    /* 出欠データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $date = str_replace("/", "-", $model->date);

        foreach($model->field["SCHREGNO"] as $key => $schregno){
            $data = $dataL = $dataM = array();

            if(substr($model->title,0,3) == 'CNT') {

                $subl_cd = substr($model->title,3);
                $subm_cd = $db->getCol(knjc030d_2Query::getNameMst('C007', $subl_cd));

                $chg_data = false;
                foreach($subm_cd as $submKey) {
                    $data = $dataL = $dataM = array();

                    // 更新対象データが存在するかをチェック--ATTEND_DAY_SUBM_DAT
                    $chk_Allsubm_data = $db->getOne(knjc030d_2Query::checkAttendDaySubmDat($model, $schregno, "", "", "", "All", "data"));

                    /* ATTEND_DAY_SUBM_DAT */
                    if($chk_Allsubm_data == $submKey){
                        //DELETE
                        $query  = " DELETE FROM ";
                        $query .= "     ATTEND_DAY_SUBM_DAT ";
                        $query .= " WHERE ";
                        $query .= "     SCHREGNO    = '".$schregno."' AND ";
                        $query .= "     ATTENDDATE  = '".$date."' ";

                        $db->query($query);
                    }

                    if($model->field["SUBDATA_{$schregno}_CNT{$submKey}"] == "1") {
                        //INSERT
                        $dataM["SCHREGNO"][TEXT]    = $schregno;
                        $dataM["ATTENDDATE"][TEXT]  = $date;
                        $dataM["DI_CD"][TEXT]       = $model->field["DI_CD"];
                        $dataM["SUBL_CD"][TEXT]     = $subl_cd;
                        $dataM["SUBM_CD"][TEXT]     = $submKey;
                        $dataM["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataM["REGISTERCD"][TEXT]  = STAFFCD ;
                        $dataM["UPDATED"][FUNC]     = "sysdate()";

                        $query = Query::insertSQL($dataM, "ATTEND_DAY_SUBM_DAT");
                        $db->query($query);
                        $chg_data = true;
                    }
                }

                // 更新対象データが存在するかをチェック--ATTEND_DAY_SUBL_DAT
                $chk_Allsubl_data = $db->getOne(knjc030d_2Query::checkAttendDaySublDat($model, $schregno, "", "", "All", "data"));

                /* ATTEND_DAY_SUBL_DAT */
                if($chk_Allsubl_data == $subl_cd){
                    //DELETE
                    $query  = " DELETE FROM ";
                    $query .= "     ATTEND_DAY_SUBL_DAT ";
                    $query .= " WHERE ";
                    $query .= "     SCHREGNO    = '".$schregno."' AND ";
                    $query .= "     ATTENDDATE  = '".$date."' ";

                    $db->query($query);
                }

                if($chg_data){
                    //INSERT
                    $dataL["SCHREGNO"][TEXT]    = $schregno;
                    $dataL["ATTENDDATE"][TEXT]  = $date;
                    $dataL["DI_CD"][TEXT]       = $model->field["DI_CD"];
                    $dataL["SUBL_CD"][TEXT]     = $subl_cd;
                    $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                    $dataL["REGISTERCD"][TEXT]  = STAFFCD ;
                    $dataL["UPDATED"][FUNC]     = "sysdate()";

                    $query = Query::insertSQL($dataL, "ATTEND_DAY_SUBL_DAT");
                    $db->query($query);
                }

            } else {

                $subl_cd = $db->getCol(knjc030d_2Query::getNameMst('C006', $model->field["DI_CD"]));

                $chg_data = false;
                foreach($subl_cd as $sublKey) {
                    $data = $dataL = $dataM = array();

                    // 更新対象データが存在するかをチェック--ATTEND_DAY_SUBL_DAT
                    $chk_Allsubl_data = $db->getOne(knjc030d_2Query::checkAttendDaySublDat($model, $schregno, "", "", "All", "data"));
                    $subm_check = get_count($db->getCol(knjc030d_2Query::getNameMst('C007', $sublKey)));

                    /* ATTEND_DAY_SUBL_DAT */
                    if($chk_Allsubl_data == $sublKey && $subm_check == 0) {
                        //DELETE
                        $query  = " DELETE FROM ";
                        $query .= "     ATTEND_DAY_SUBL_DAT ";
                        $query .= " WHERE ";
                        $query .= "     SCHREGNO    = '".$schregno."' AND ";
                        $query .= "     ATTENDDATE  = '".$date."' ";

                        $db->query($query);
                    }

                    if($model->field["SUBDATA_{$schregno}_CNT{$sublKey}"] == "1") {
                        //INSERT
                        $dataL["SCHREGNO"][TEXT]    = $schregno;
                        $dataL["ATTENDDATE"][TEXT]  = $date;
                        $dataL["DI_CD"][TEXT]       = $model->field["DI_CD"];
                        $dataL["SUBL_CD"][TEXT]     = $sublKey;
                        $dataL["YEAR"][TEXT]        = CTRL_YEAR;
                        $dataL["REGISTERCD"][TEXT]  = STAFFCD ;
                        $dataL["UPDATED"][FUNC]     = "sysdate()";

                        $query = Query::insertSQL($dataL, "ATTEND_DAY_SUBL_DAT");
                        $db->query($query);
                        $chg_data = true;
                    }
                }
            }

            /* ATTEND_DAY_DAT */
            $chk_Allsubl_data = $db->getOne(knjc030d_2Query::checkAttendDaySublDat($model, $schregno, "", "", "All"));
            $chk_Allsubm_data = $db->getOne(knjc030d_2Query::checkAttendDaySubmDat($model, $schregno, "", "", "", "All"));
            if (($chk_Allsubl_data == 0 && $chk_Allsubm_data == 0) || $chg_data) {
                //DELETE
                $query  = " DELETE FROM ";
                $query .= "     ATTEND_DAY_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO    = '".$schregno."' AND ";
                $query .= "     ATTENDDATE  = '".$date."' AND ";
                $query .= "     DI_CD       = '".$model->field["DI_CD"]."' ";

                $db->query($query);
            }

            if($chg_data) {
                //INSERT
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["ATTENDDATE"][TEXT]   = $date;
                $data["DI_CD"][TEXT]        = $model->field["DI_CD"];
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_DAY_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //ATTEND_DAY_DATチェック
    function checkAttendDayDat($model, $schregno, $All="", $data="") {

        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        if($data) {
            $query .= "     DI_CD ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";
        if($All == "") {
            $query .= "     AND ";
            $query .= "     DI_CD = '".$model->field["DI_CD"]."' ";
        }

        return $query;
    }

    //ATTEND_DAY_SUBL_DATチェック
    function checkAttendDaySublDat($model, $schregno, $di_cd, $subl_cd, $All="", $data="") {

        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        if($data) {
            $query .= "     SUBL_CD AS SUB_CD ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_SUBL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";
        if($All == "") {
            if($di_cd)      $query .= " AND DI_CD       = '".$di_cd."' ";
            if($subl_cd)    $query .= " AND SUBL_CD     = '".$subl_cd."' ";
        }

        return $query;
    }

    //ATTEND_DAY_SUBM_DATチェック
    function checkAttendDaySubmDat($model, $schregno, $di_cd, $subl_cd, $subm_cd, $All="", $data="") {

        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        if($data) {
            $query .= "     SUBM_CD AS SUB_CD ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_DAY_SUBM_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ATTENDDATE  = '".$date."' ";
        if($All == "") {
            if($di_cd)    $query .= " AND DI_CD       = '".$di_cd."' ";
            if($subl_cd)  $query .= " AND SUBL_CD     = '".$subl_cd."' ";
            if($subm_cd)  $query .= " AND SUBM_CD     = '".$subm_cd."' ";
        }

        return $query;
    }
}
?>
