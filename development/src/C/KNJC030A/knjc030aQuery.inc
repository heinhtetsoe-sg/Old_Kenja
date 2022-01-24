<?php

require_once('for_php7.php');

class knjc030aQuery extends Query
{

    //学期取得
    public function getTerm($year, $day)
    {
        return " VALUES Y2T(DATE('".$day."'),'N') ";
    }

    //見出し行データの取得
    public function getPeriodName()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 AS PERIODNAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'B001' ";

        return $query;
    }

    //制限処理月のチェック
    public function chControlMonth($model)
    {
        $query  = " SELECT 'x' FROM ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND IN ( ";
            $query .= "      SELECT ";
            $query .= "        SCHOOL_KIND ";
            $query .= "      FROM ";
            $query .= "        SCHREG_REGD_GDAT ";
            $query .= "      WHERE ";
            $query .= "        YEAR = '".CTRL_YEAR."' ";
            $query .= "        AND GRADE = '".$model->GRADE."' ";
            $query .= "     ) AND ";
        }
        $query .= "     CONTROL_FLG = '2' AND ";
        $query .= "     CONTROL_CODE = '".substr($model->cntl_dt_key, 5, 2)."' ";

        return $query;
    }

    //クラスの基本データを取得
    public function getHrClass($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     GRADE || HR_CLASS AS GR_CL, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT  ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".$model->cntl_dt_year."' ";
        $query .= "     AND SEMESTER ='".$model->termIs."' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= " AND  (    TR_CD1    = '".STAFFCD."' 
                               OR TR_CD2    = '".STAFFCD."' 
                               OR TR_CD3    = '".STAFFCD."' 
                               OR SUBTR_CD1 = '".STAFFCD."' 
                               OR SUBTR_CD2 = '".STAFFCD."' 
                               OR SUBTR_CD3 = '".STAFFCD."'
                               OR '".STAFFCD."' IN (SELECT STAFFCD FROM STAFF_DETAIL_MST WHERE YEAR = '".CTRL_YEAR."' AND STAFF_SEQ IN ('005', '006', '007') AND FIELD1 = '0200' AND FIELD2 = GRADE)
                               ) ";
        } else {
            $query .="      AND GRADE = '".$model->GRADE."'  ";
        }

        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        return $query;
    }

    public function getUpdateSelectData($model, $div = "")
    {
        $query  =" SELECT ";
        $query .="     t2.SCHREGNO, ";
        $query .="     t4.PERIODCD ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT t1, ";
        $query .="     SCHREG_REGD_DAT t2, ";
        $query .="     CHAIR_STD_DAT t3, ";
        $query .="     SCH_CHR_DAT t4, ";
        $query .="     ATTEND_DAT t5 ";
        $query .=" WHERE ";
        $query .="     t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .="     t1.SEMESTER = '".$model->termIs."' AND ";
        if ($div == "UPD") {
            $query .="     t1.GRADE || t1.HR_CLASS IN ".$model->instate." AND ";
        } else {
            $query .= "   (   T1.TR_CD1    = '".STAFFCD."' 
                           OR T1.TR_CD2    = '".STAFFCD."' 
                           OR T1.TR_CD3    = '".STAFFCD."' 
                           OR T1.SUBTR_CD1 = '".STAFFCD."' 
                           OR T1.SUBTR_CD2 = '".STAFFCD."' 
                           OR T1.SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        $query .="     t2.YEAR = t1.YEAR AND ";
        $query .="     t2.SEMESTER = t1.SEMESTER AND ";
        $query .="     t2.GRADE = t1.GRADE AND ";
        $query .="     t2.HR_CLASS = t1.HR_CLASS AND ";
        $query .="     t3.YEAR = t1.YEAR AND ";
        $query .="     t3.SEMESTER = t1.SEMESTER AND ";
        $query .="     t3.SCHREGNO = t2.SCHREGNO AND ";
        $query .="     t5.SCHREGNO = t2.SCHREGNO AND ";
        $query .="     '".str_replace("/", "-", $model->cntl_dt_key)."' between t3.APPDATE AND ";
        $query .="     t3.APPENDDATE AND ";
        $query .="     t4.EXECUTEDATE = '".str_replace("/", "-", $model->cntl_dt_key)."' AND ";
        $query .="     t5.ATTENDDATE=t4.EXECUTEDATE  AND ";
        $query .="     t5.PERIODCD=t4.PERIODCD  AND ";
        $query .="     t4.CHAIRCD = t3.CHAIRCD AND ";
        $query .="     NOT t4.PERIODCD = '0' ";

        return $query;
    }

    //学年データの取得
    public function gradeCombo($model)
    {
        $query  =" SELECT ";
        $query .="     HDAT.GRADE AS VALUE, ";
        $query .="     GDAT.GRADE_NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT HDAT ";
        $query .="     INNER JOIN SCHREG_REGD_GDAT GDAT ON HDAT.YEAR = GDAT.YEAR ";
        $query .="           AND HDAT.GRADE = GDAT.GRADE ";
        $query .=" WHERE ";
        $query .="     HDAT.YEAR ='".CTRL_YEAR."'  ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND  (    HDAT.TR_CD1    = '".STAFFCD."' 
                                OR HDAT.TR_CD2    = '".STAFFCD."' 
                                OR HDAT.TR_CD3    = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD1 = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD2 = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD3 = '".STAFFCD."'
                                OR '".STAFFCD."' IN (SELECT STAFFCD FROM STAFF_DETAIL_MST WHERE YEAR = '".CTRL_YEAR."' AND STAFF_SEQ IN ('005', '006', '007') AND FIELD1 = '0200' AND FIELD2 = HDAT.GRADE)
                                ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .=" GROUP BY ";
        $query .="     HDAT.GRADE, ";
        $query .="     GDAT.GRADE_NAME1 ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";
        return $query;
    }

    //表示用データを取得
    public function readQuery(&$model)
    {
        //先に一度適応日付に当てはまる生徒がいる年組と講座を取得しておく
        $query  = " WITH T_CHAIR_STD (grade, hr_class, chaircd) ";
        $query .= "               AS (SELECT T2.grade, T2.hr_class, T1.chaircd ";
        $query .= "                     FROM chair_std_dat T1, schreg_regd_dat T2 ";
        $query .= "                    WHERE T1.schregno = T2.schregno ";
        $query .= "                      AND T1.year     = T2.year ";
        $query .= "                      AND T1.semester = T2.semester ";
        $query .= "                      AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "                      AND T1.semester = '".$model->termIs."' ";
        $query .= "                      AND '".$model->cntl_dt_key."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "                      AND T2.grade    = '".$model->GRADE."' ";
        $query .= "                    GROUP BY T2.grade, T2.hr_class, T1.chaircd) ";
        $query .= ", SUBMAIN AS ( ";
        $query .= "SELECT w1.GRADE, ";
        $query .= "       w1.HR_CLASS, ";
        $query .= "       w1.GRADE || w1.HR_CLASS AS TARGETCLASS, ";
        $query .= "       w1.HR_NAME, ";
        $query .= "       w1.CHAIRCD, ";
        $query .= "       w1.TR_CD1, ";
        $query .= "       w4.SUBCLASSABBV, ";
        $query .= "       SUBSTR(w1.CHAIRNAME,1,9) AS CHAIRNAME, ";
        $query .= "       w2.STAFFCD, ";
        $query .= "       w2.STAFFNAME, ";
        $query .= "       w1.PERIODCD, ";
        $query .= "       w1.SCD_EXECUTED, ";   //講座別実施確認フラグ(1：出欠済み)
        $query .= "       w3.EXECUTED, ";        //ＨＲ別実施確認フラグ(1：出欠済み)
        $query .= "       CASE WHEN VALUE(w1.SCD_EXECUTED,'0') = '0' ";
        $query .= "            THEN VALUE(w3.EXECUTED, '0') ";
        $query .= "            ELSE w1.SCD_EXECUTED END AS EXECUTED_CHK, ";
        $query .= "       w1.EXECUTEDIV AS SCD_EXECUTEDIV ";        //講座別実施区分(名称マスタC009)

        $query .= "  FROM (SELECT T1.YEAR, ";
        $query .= "               T1.SEMESTER, ";
        $query .= "               T1.GRADE, ";
        $query .= "               T1.HR_CLASS, ";
        $query .= "               T1.HR_NAME, ";
        $query .= "               T1.TR_CD1, ";
        $query .= "               T2.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "               T4.CLASSCD,";
            $query .= "               T4.SCHOOL_KIND,";
            $query .= "               T4.CURRICULUM_CD,";
        }
        $query .= "               T4.SUBCLASSCD, ";
        $query .= "               T4.CHAIRNAME, ";
        $query .= "               T3.EXECUTEDATE, ";
        $query .= "               T3.EXECUTED as SCD_EXECUTED, ";
        $query .= "               T3.EXECUTEDIV, ";
        $query .= "               T3.PERIODCD ";
        $query .= "          FROM SCHREG_REGD_HDAT T1, ";
        $query .= "               T_CHAIR_STD      T2, ";
        $query .= "               SCH_CHR_DAT      T3, ";
        $query .= "               CHAIR_DAT        T4 ";
        $query .= "         WHERE T1.grade       = T2.grade ";
        $query .= "           AND T1.hr_class    = T2.hr_class ";
        $query .= "           AND T1.year        = T4.year ";
        $query .= "           AND T1.semester    = T4.semester ";
        $query .= "           AND T2.chaircd     = T3.chaircd ";
        $query .= "           AND T2.chaircd     = T4.chaircd ";
        $query .= "           AND T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "           AND T1.SEMESTER    = '".$model->termIs."' ";
        $query .= "           AND T1.GRADE       = '".$model->GRADE."' ";
        $query .= "           AND T3.EXECUTEDATE = DATE('".$model->cntl_dt_key."') ";
        if ($model->Properties["knjc030aShowPeriod0"] == "1") {
            // PERIODCD='0'を表示する
        } else {
            $query .= "           AND T3.PERIODCD <> '0'  ";
        }
        //更新可制限付のスタッフを指定
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "           AND   (    T1.TR_CD1    = '".STAFFCD."' 
                                          OR T1.TR_CD2    = '".STAFFCD."' 
                                          OR T1.TR_CD3    = '".STAFFCD."' 
                                          OR T1.SUBTR_CD1 = '".STAFFCD."' 
                                          OR T1.SUBTR_CD2 = '".STAFFCD."' 
                                          OR T1.SUBTR_CD3 = '".STAFFCD."'
                                          OR '".STAFFCD."' IN (SELECT STAFFCD FROM STAFF_DETAIL_MST WHERE YEAR = '".CTRL_YEAR."' AND STAFF_SEQ IN ('005', '006', '007') AND FIELD1 = '0200' AND FIELD2 = T1.GRADE)
                                          ) ";
        }
        $query .= "       ) w1 LEFT OUTER JOIN (SELECT t1.YEAR, ";
        $query .= "                                    t1.SEMESTER, ";
        $query .= "                                    t1.CHAIRCD, ";
        $query .= "                                    t1.STAFFCD, ";
        $query .= "                                    t2.STAFFNAME ";
        $query .= "                               FROM CHAIR_STF_DAT t1, ";
        $query .= "                                    STAFF_MST t2 ";
        $query .= "                              WHERE t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                AND t1.SEMESTER = '".$model->termIs."' ";
        $query .= "                                AND t1.STAFFCD = t2.STAFFCD ";
        $query .= "                             ) w2 ON w1.YEAR = w2.YEAR  ";
        $query .= "                                 AND w1.SEMESTER = w2.SEMESTER  ";
        $query .= "                                 AND w1.CHAIRCD = w2.CHAIRCD  ";
        $query .= "             LEFT OUTER JOIN SCH_CHR_HRATE_DAT w3  ";
        $query .= "                          ON w1.EXECUTEDATE = w3.EXECUTEDATE  ";
        $query .= "                         AND w1.PERIODCD    = w3.PERIODCD  ";
        $query .= "                         AND w1.CHAIRCD     = w3.CHAIRCD  ";
        $query .= "                         AND w1.GRADE       = w3.GRADE  ";
        $query .= "                         AND w1.HR_CLASS    = w3.HR_CLASS  ";
        $query .= "             LEFT OUTER JOIN SUBCLASS_MST w4  ";
        $query .= "                          ON w4.SUBCLASSCD = w1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "              AND w4.CLASSCD = w1.CLASSCD ";
            $query .= "              AND w4.SCHOOL_KIND = w1.SCHOOL_KIND ";
            $query .= "              AND w4.CURRICULUM_CD = w1.CURRICULUM_CD ";
        }
        $query .= "), MAIN AS ( ";
        $query .= "SELECT ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.TARGETCLASS, ";
        $query .= "    T1.HR_NAME, ";
        $query .= "    T1.CHAIRCD, ";
        $query .= "    T1.TR_CD1, ";
        $query .= "    T1.SUBCLASSABBV, ";
        $query .= "    T1.CHAIRNAME, ";
        $query .= "    CASE WHEN A1.STAFFCD IS NOT NULL THEN A1.STAFFCD ELSE T1.STAFFCD END AS STAFFCD, ";
        $query .= "    CASE WHEN A1.STAFFCD IS NOT NULL THEN A2.STAFFNAME ELSE T1.STAFFNAME END AS STAFFNAME, ";
        $query .= "    T1.PERIODCD, ";
        $query .= "    T1.SCD_EXECUTED, ";
        $query .= "    T1.EXECUTED , ";
        $query .= "    T1.EXECUTED_CHK, ";
        $query .= "    T1.SCD_EXECUTEDIV , ";
        $query .= "    NMC009.NAME1 AS SCD_EXECUTEDIV_NAME ";
        $query .= "FROM ";
        $query .= "    SUBMAIN T1 ";
        $query .= "  LEFT JOIN SCH_STF_DAT A1 ON T1.PERIODCD || T1.CHAIRCD = A1.PERIODCD || A1.CHAIRCD ";
        $query .= "    AND A1.executedate = DATE('".$model->cntl_dt_key."') ";
        $query .= "  LEFT JOIN V_STAFF_MST A2 ON A2.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND A2.STAFFCD = A1.STAFFCD ";
        $query .= "  LEFT JOIN V_NAME_MST NMC009 ON NMC009.NAMECD1 = 'C009' ";
        $query .= "    AND NMC009.NAMECD2 = T1.SCD_EXECUTEDIV ";
        $query .= "    AND NMC009.NAMESPARE3 = '1' "; // 印字フラグ
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    TARGETCLASS, ";
        $query .= "    HR_NAME, ";
        $query .= "    CHAIRCD, ";
        $query .= "    TR_CD1, ";
        $query .= "    SUBCLASSABBV, ";
        $query .= "    CHAIRNAME, ";
        $query .= "    STAFFCD, ";
        $query .= "    STAFFNAME, ";
        $query .= "    PERIODCD, ";
        $query .= "    SCD_EXECUTED, ";
        $query .= "    EXECUTED , ";
        $query .= "    EXECUTED_CHK, ";
        $query .= "    SCD_EXECUTEDIV , ";
        $query .= "    SCD_EXECUTEDIV_NAME ";
        $query .= "FROM ";
        $query .= "    MAIN  ";
        $query .= "GROUP BY ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    TARGETCLASS, ";
        $query .= "    HR_NAME, ";
        $query .= "    CHAIRCD, ";
        $query .= "    TR_CD1, ";
        $query .= "    SUBCLASSABBV, ";
        $query .= "    CHAIRNAME, ";
        $query .= "    STAFFCD, ";
        $query .= "    STAFFNAME, ";
        $query .= "    PERIODCD, ";
        $query .= "    SCD_EXECUTED, ";
        $query .= "    EXECUTED , ";
        $query .= "    EXECUTED_CHK, ";
        $query .= "    SCD_EXECUTEDIV , ";
        $query .= "    SCD_EXECUTEDIV_NAME ";
        $query .= "ORDER BY HR_NAME, PERIODCD ";

        return $query;
    }

    //データを移動
    public function keyMoverQuery($model, $move_key = "")
    {
        $handle_end = "";

        switch ($move_key) {
            case "next":
                $max_min = " MIN";
                $handle = " > ";
                break;
            case "before":
                $max_min = " MAX";
                $handle = " < ";
                break;
            default:
                $max_min = " MIN";
                $handle = " BETWEEN ";
                $handle_end = " AND DATE('".(CTRL_YEAR + 1)."-3-31') ";
                break;
        }

        if (AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT ";
            $query  .= $max_min."(EXECUTEDATE) AS EXECUTEDATE ";
            $query  .= "FROM ";
            $query  .= " SCH_CHR_DAT ";
            $query  .= "WHERE ";
            $query  .= " EXECUTEDATE ".$handle." DATE('".$model->cntl_dt_key."') ";
            $query  .= $handle_end ;
        } else {
            $query  =" SELECT ";
            $query .= $max_min."(T4.EXECUTEDATE) AS EXECUTEDATE";
            $query .=" FROM ( ";
            $query .="         SELECT YEAR,SEMESTER,GRADE,HR_CLASS,TR_CD1 ";
            $query .="           FROM SCHREG_REGD_HDAT ";
            $query .="          WHERE YEAR='".CTRL_YEAR."' ";
            $query .="            AND (   TR_CD1    = '".STAFFCD."' 
                                       OR TR_CD2    = '".STAFFCD."' 
                                       OR TR_CD3    = '".STAFFCD."' 
                                       OR SUBTR_CD1 = '".STAFFCD."' 
                                       OR SUBTR_CD2 = '".STAFFCD."' 
                                       OR SUBTR_CD3 = '".STAFFCD."'
                                       OR '".STAFFCD."' IN (SELECT STAFFCD FROM STAFF_DETAIL_MST WHERE YEAR = '".CTRL_YEAR."' AND STAFF_SEQ IN ('005', '006', '007') AND FIELD1 = '0200' AND FIELD2 = GRADE)
                                       ) ";
            $query .="     ) AS T1,(";
            $query .="         SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS ";
            $query .="           FROM SCHREG_REGD_DAT ";
            $query .="          WHERE YEAR='".CTRL_YEAR."' ";
            $query .="                AND GRADE = '".$model->GRADE."' ";
            $query .="     ) AS T2, (";
            $query .="         SELECT YEAR,SEMESTER,CHAIRCD,SCHREGNO,APPDATE,APPENDDATE ";
            $query .="           FROM CHAIR_STD_DAT ";
            $query .="          WHERE YEAR='".CTRL_YEAR."' ";
            $query .="     ) AS T3,(";
            $query .="         SELECT ".$max_min."(EXECUTEDATE) as EXECUTEDATE,PERIODCD,CHAIRCD,YEAR,SEMESTER ";
            $query .="           FROM SCH_CHR_DAT ";
            $query .="          WHERE PERIODCD IN ('1','2','3','4','5','6','7','8','9') ";
            $query .="                AND EXECUTEDATE ".$handle." DATE('".$model->cntl_dt_key."') ";
            $query .= $handle_end ;
            $query .="         GROUP BY ";
            $query .="         PERIODCD,CHAIRCD,YEAR,SEMESTER ";
            $query .="     ) AS T4 ";
            $query .=" WHERE T1.YEAR     = T2.YEAR ";
            $query .="   AND T1.SEMESTER = T2.SEMESTER ";
            $query .="   AND T1.YEAR     = T3.YEAR ";
            $query .="   AND T1.SEMESTER = T3.SEMESTER ";
            $query .="   AND T1.GRADE    = T2.GRADE ";
            $query .="   AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .="   AND T2.SCHREGNO = T3.SCHREGNO ";
            $query .="   AND T4.EXECUTEDATE BETWEEN T3.APPDATE AND T3.APPENDDATE ";
            $query .="   AND T3.CHAIRCD  = T4.CHAIRCD ";
        }

        $db  = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["EXECUTEDATE"];
    }

    //年組取得
    public function getChrClass($model, $executedate, $periodcd, $chaircd)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '{$model->cntl_dt_year}' ";
        $query .= "           AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '{$executedate}' ";
        $query .= "     AND T1.PERIODCD = '{$periodcd}' ";
        $query .= "     AND T1.CHAIRCD = '{$chaircd}' ";

        return $query;
    }
    //アップデート
    public function &getUpdateQuery(&$model)
    {
        $ary = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //選択した日付でATTEND_DATのデータを削除する
        $query  = knjc030aQuery::getUpdateSelectData($model, "UPD");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  =" DELETE ";
            $query .=" FROM ";
            $query .=" ATTEND_DAT ";
            $query .="  where SCHREGNO = '". $row["SCHREGNO"]."' ";
            $query .="   and ATTENDDATE = '".str_replace("/", "-", $model->cntl_dt_key)."' ";
            $query .="   and PERIODCD = '". $row["PERIODCD"]."' ";
            $db->query($query);
        }

        //SCH_CHR_HRATE_DATを削除し、実施区分(EXECUTED)を1にしてインサート
        for ($i = 0; $i < get_count($model->data["CHAIRCD"]); $i++) {
            $targetCheck = $model->data["GRADE"][$i].$model->data["HR_CLASS"][$i];
            if (in_array($targetCheck, $model->target_check)) {
                $query  =" DELETE ";
                $query .=" FROM ";
                $query .=" SCH_CHR_HRATE_DAT ";
                $query .="  WHERE EXECUTEDATE = '". str_replace("/", "-", $model->cntl_dt_key)."' ";    //実施日付
                $query .="  AND PERIODCD      = '". $model->data["PERIODCD"][$i]."' ";                //校時コード
                $query .="  AND CHAIRCD       = '". $model->data["CHAIRCD"][$i]."' ";                 //講座コード
                $db->query($query);

                $result = $db->query(knjc030aQuery::getChrClass($model, str_replace("/", "-", $model->cntl_dt_key), $model->data["PERIODCD"][$i], $model->data["CHAIRCD"][$i]));
                while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data["EXECUTEDATE"][TEXT] = str_replace("/", "-", $model->cntl_dt_key) ;               //実施日付
                    $data["PERIODCD"][TEXT]    = $model->data["PERIODCD"][$i] ;                           //校時コード
                    $data["CHAIRCD"][TEXT]     = $model->data["CHAIRCD"][$i];                             //講座コード
                    $data["GRADE"][TEXT]       = $row2["GRADE"];                                          //年
                    $data["HR_CLASS"][TEXT]    = $row2["HR_CLASS"];                                       //組
                    $data["EXECUTED"][TEXT]    = '1' ;                                                    //実施区分
                    $data["ATTESTOR"][TEXT]    = STAFFCD ;                                                //出欠確認者コード
                    $data["REGISTERCD"][TEXT]  = STAFFCD ;                                                //登録者コード
                    $data["UPDATED"][FUNC]     = "sysdate()";                                             //更新日付
                    $query = Query::insertSQL($data, "SCH_CHR_HRATE_DAT");
                    $db->query($query);
                }

                //SCH_CHR_DAT 時間割講座データ
                $query  =" UPDATE SCH_CHR_DAT SET";
                $query .=" EXECUTEDIV = '1', " ;                                                      //実施
                $query .=" ATTESTOR   = '". STAFFCD ."'," ;                                           //出欠確認者コード
                $query .=" REGISTERCD = '". STAFFCD ."'," ;                                           //登録者コード
                $query .=" UPDATED = "."sysdate()"."" ;                                               //更新日付
                $query .="  WHERE EXECUTEDATE = '". str_replace("/", "-", $model->cntl_dt_key)."' ";    //実施日付
                $query .="  AND PERIODCD      = '". $model->data["PERIODCD"][$i]."' ";                //校時コード
                $query .="  AND CHAIRCD       = '". $model->data["CHAIRCD"][$i]."' ";                 //講座コード
                $db->query($query);

                if (knjc030aQuery::isSchChrHrateAllExecuted($db, str_replace("/", "-", $model->cntl_dt_key), $model->data["PERIODCD"][$i], $model->data["CHAIRCD"][$i])) {
                    //SCH_CHR_DAT 時間割講座データ
                    $query  =" UPDATE SCH_CHR_DAT SET";
                    $query .=" EXECUTED   = '1', " ;                                                      //実施区分
                    $query .=" ATTESTOR   = '". STAFFCD ."'," ;                                           //出欠確認者コード
                    $query .=" REGISTERCD = '". STAFFCD ."'," ;                                           //登録者コード
                    $query .=" UPDATED = "."sysdate()"."" ;                                               //更新日付
                    $query .="  WHERE EXECUTEDATE = '". str_replace("/", "-", $model->cntl_dt_key)."' ";    //実施日付
                    $query .="  AND PERIODCD      = '". $model->data["PERIODCD"][$i]."' ";                //校時コード
                    $query .="  AND CHAIRCD       = '". $model->data["CHAIRCD"][$i]."' ";                 //講座コード
                    $db->query($query);
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //時間割のすべての年組が出欠済みか
    public function isSchChrHrateAllExecuted($db, $executedate, $periodcd, $chaircd)
    {
        $query  = knjc030aQuery::getSchChrHrateAllExecutedSql($executedate, $periodcd, $chaircd);
        $result = $db->query($query);
        $notAllExecuted = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["EXECUTED"] != '1') {
                $notAllExecuted = true;
            } // 未出欠の年組がある
        }
        $result->free();
        if ($notAllExecuted) {
            return false;
        }
        return true;
    }

    //時間割のすべての年組の出欠済みフラグ
    public function getSchChrHrateAllExecutedSql($executedate, $periodcd, $chaircd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GRADE, T3.HR_CLASS, T4.EXECUTED ";
        $query .= " FROM SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_DAT T0 ON T0.YEAR = T1.YEAR ";
        $query .= "     AND T0.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T0.CHAIRCD = T1.CHAIRCD ";
        $query .= " INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     AND '".$executedate."' BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T3.YEAR = T2.YEAR ";
        $query .= "     AND T3.SEMESTER = T2.SEMESTER ";
        $query .= " LEFT JOIN SCH_CHR_HRATE_DAT T4 ON T4.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "     AND T4.PERIODCD = T1.PERIODCD ";
        $query .= "     AND T4.CHAIRCD = T1.CHAIRCD ";
        $query .= "     AND T4.GRADE = T3.GRADE ";
        $query .= "     AND T4.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '".$executedate."' ";
        $query .= "     AND T1.PERIODCD = '".$periodcd."' ";
        $query .= "     AND T1.CHAIRCD = '".$chaircd."' ";
        return $query;
    }

    //表示項目の取得
    public function getDispCol()
    {
        $query  = " SELECT COALESCE(MAX(NAMECD2),'1') AS DISP_COL  ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = 'C000' ";
        return $query;
    }
}
