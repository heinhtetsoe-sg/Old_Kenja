<?php

require_once('for_php7.php');

class knjd297Query extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
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
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE IN (SELECT ";
        $query .= "                      REGD_G.GRADE ";
        $query .= "                   FROM ";
        $query .= "                      SCHREG_REGD_GDAT REGD_G ";
        $query .= "                   WHERE ";
        $query .= "                      REGD_G.YEAR = '{$model->year}' ";
        $query .= "                      AND REGD_G.SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"] ."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //生徒一覧取得
    function selectSchQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO || '番' || '　' || T4.NAME LABEL, ";
        $query .= "    T1.SCHREGNO VALUE ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.GRADE = T2.GRADE ";
        $query .= "    INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "      ON T1.YEAR = T3.YEAR ";
        $query .= "      AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "      AND T1.GRADE = T3.GRADE ";
        $query .= "      AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST T4 ";
        $query .= "      ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    function selectMainCsvQuery($model) {
        $query  = "  WITH RANK_CALC AS ( ";
        $query .= "    SELECT ";
        $query .= "      YEAR ";
        $query .= "      , SCHREGNO ";
        $query .= "      , SUM(CASE SCORE WHEN 5 THEN 1 ELSE 0 END) AS HYOUTEI5 ";
        $query .= "      , SUM(CASE SCORE WHEN 4 THEN 1 ELSE 0 END) AS HYOUTEI4 ";
        $query .= "      , SUM(CASE SCORE WHEN 3 THEN 1 ELSE 0 END) AS HYOUTEI3 ";
        $query .= "      , SUM(CASE SCORE WHEN 2 THEN 1 ELSE 0 END) AS HYOUTEI2 ";
        $query .= "      , SUM(CASE SCORE WHEN 1 THEN 1 ELSE 0 END) AS HYOUTEI1 ";
        $query .= "      , SUM(CASE WHEN SCORE IN (5, 4, 3, 2, 1) THEN SCORE ELSE 0 END) AS SCORE_SUM ";
        $query .= "    FROM ";
        $query .= "      RECORD_RANK_SDIV_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER       = '9' ";
        $query .= "      AND TESTKINDCD = '99' ";
        $query .= "      AND TESTITEMCD = '00' ";
        $query .= "      AND SCORE_DIV  = '09' ";
        $query .= "    GROUP BY ";
        $query .= "      YEAR ";
        $query .= "      , SCHREGNO ";
        $query .= "  ) ";
        $query .= "  , ATTEND_CALC AS ( ";
        $query .= "    SELECT ";
        $query .= "      YEAR ";
        $query .= "      , SCHREGNO ";
        $query .= "      , SUM(SICK + NOTICE + NONOTICE) AS ABSCENCE ";
        $query .= "      , SUM(LATE) AS LATE ";
        $query .= "      , SUM(EARLY) AS EARLY ";
        $query .= "    FROM ";
        $query .= "      ATTEND_SEMES_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "      YEAR ";
        $query .= "      , SCHREGNO ";
        $query .= "  ) ";
        $query .= "  , CLASS_SCH AS ( ";
        $query .= "    SELECT ";
        $query .= "      * ";
        $query .= "    FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "      AND SCHREGNO IN ('".implode("','", explode(",", $model->selectdata))."') ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "    T1.SCHREGNO ";
        $query .= "    , T1.GRADE ";
        $query .= "    , T1.HR_CLASS ";
        $query .= "    , T1.ATTENDNO ";
        $query .= "    , BASE.NAME ";
        $query .= "    , T2.HYOUTEI5 ";
        $query .= "    , T2.HYOUTEI4 ";
        $query .= "    , T2.HYOUTEI3 ";
        $query .= "    , T2.HYOUTEI2 ";
        $query .= "    , T2.HYOUTEI1 ";
        $query .= "    , T2.SCORE_SUM ";
        $query .= "    , VALUE( CAST( ROUND(CAST(T2.SCORE_SUM AS FLOAT) / NULLIF( (T2.HYOUTEI1 + T2.HYOUTEI2 + T2.HYOUTEI3 + T2.HYOUTEI4 + T2.HYOUTEI5), 0), 1) AS DECIMAL(3, 1) ), 0 ) AS SCORE_AVG ";
        $query .= "    , T3.ABSCENCE ";
        $query .= "    , T3.LATE ";
        $query .= "    , T3.EARLY ";
        $query .= "  FROM ";
        $query .= "    CLASS_SCH T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "      ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "    LEFT JOIN RANK_CALC T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "    LEFT JOIN ATTEND_CALC T3 ";
        $query .= "      ON T1.YEAR = T3.YEAR ";
        $query .= "      AND T1.SCHREGNO = T3.SCHREGNO ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //学科コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM major_ydat WHERE year = '" .$model->year ."'";
        $db->query($query);

        $query = "INSERT INTO major_ydat(year,coursecd,majorcd,registercd,updated)";
        $query.= "VALUES";
        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .substr($array[$i],0,1)."'";                      //課程コード
                $query.= ",'" .substr($array[$i],1)."'";                        //学科コード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
