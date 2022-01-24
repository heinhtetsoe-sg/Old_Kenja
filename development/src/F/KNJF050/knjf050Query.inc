<?php

require_once('for_php7.php');

class knjf050query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //都道府県名取得
    function getPrefName($model) {
        $query  = " SELECT ";
        $query .= "     T2.PREF_NAME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1, ";
        $query .= "     PREF_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.PREF_CD  = T2.PREF_CD  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->fields["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->fields["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //本校集計
    function ThisSchool_Total($grade) {
        $query  = " SELECT";
        $query .= "     T2.GRADE,";
        $query .= "     T2.SEX,";
        $query .= "     ROUND(AVG(HEIGHT)   ,1) AS AVG_HEIGHT,";
        $query .= "     ROUND(AVG(WEIGHT)   ,1) AS AVG_WEIGHT,";
        $query .= "     ROUND(AVG(SITHEIGHT),1) AS AVG_SITHEIGHT";
        $query .= " FROM";
        $query .= "     MEDEXAM_DET_DAT T1,";
        $query .= "     (SELECT";
        $query .= "         W1.YEAR,";
        $query .= "         W1.SCHREGNO,";
        $query .= "         W1.GRADE,";
        $query .= "         W2.SEX";
        $query .= "     FROM";
        $query .= "         SCHREG_REGD_DAT W1,";
        $query .= "         SCHREG_BASE_MST W2";
        $query .= "     WHERE";
        $query .= "         W1.YEAR     = '".CTRL_YEAR."' AND";
        $query .= "         W1.SEMESTER = '".CTRL_SEMESTER."' AND";
        $query .= "         W1.GRADE    = '".$grade."' AND";
        $query .= "         W1.SCHREGNO = W2.SCHREGNO AND";
        $query .= "         (W2.SEX = '1' OR W2.SEX = '2')";
        $query .= "     ) T2";
        $query .= " WHERE";
        $query .= "     T1.YEAR     = T2.YEAR AND";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO";
        $query .= " GROUP BY";
        $query .= "     T2.GRADE,";
        $query .= "     T2.SEX";

        return $query;
    }

    //身体測定平均値データ
    function &getBodyMeasured_Avg_Dat($grade) {
        $query  = " SELECT";
        $query .= "     DISTRICT,";
        $query .= "     GRADE,";
        $query .= "     SUM(CASE WHEN SEX='1' AND MEASUREMENT ='1' THEN MEASUREMENT_AVG ELSE NULL END) HEIGHT1,";
        $query .= "     SUM(CASE WHEN SEX='2' AND MEASUREMENT ='1' THEN MEASUREMENT_AVG ELSE NULL END) HEIGHT2,";
        $query .= "     SUM(CASE WHEN SEX='1' AND MEASUREMENT ='2' THEN MEASUREMENT_AVG ELSE NULL END) WEIGHT1,";
        $query .= "     SUM(CASE WHEN SEX='2' AND MEASUREMENT ='2' THEN MEASUREMENT_AVG ELSE NULL END) WEIGHT2,";
        $query .= "     SUM(CASE WHEN SEX='1' AND MEASUREMENT ='3' THEN MEASUREMENT_AVG ELSE NULL END) SITHEIGHT1,";
        $query .= "     SUM(CASE WHEN SEX='2' AND MEASUREMENT ='3' THEN MEASUREMENT_AVG ELSE NULL END) SITHEIGHT2";
        $query .= " FROM";
        $query .= "     BODYMEASURED_AVG_DAT";
        $query .= " WHERE";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND";
        $query .= "     GRADE = '".$grade."'";
        $query .= " GROUP BY";
        $query .= "     DISTRICT,";
        $query .= "     GRADE";

        return $query;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM BODYMEASURED_AVG_DAT ";
        $query .= " WHERE YEAR ='".CTRL_YEAR."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->fields["SCHKIND"]."' ";
            $query .= "                 ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for($x=1; $x<=2; $x++)
        {
            $result = $db->query(knjf050Query::getGrade($model));
            while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $grade = ltrim($Row["GRADE"],"0");
                for($i=1; $i<=3; $i++)
                {
                    for($y=1; $y<=3; $y++)
                    {
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["DISTRICT"][TEXT]     = $i;
                        $data["SEX"][TEXT]          = $x;
                        $data["GRADE"][TEXT]        = sprintf("%02d", $grade);
                        if($y == 1){
                            $data["MEASUREMENT"][TEXT] = $y;
                            $data["MEASUREMENT_AVG"][NUMBER] = $fields["HEIGHT".$x][$grade][$i];
                        } elseif($y == 2){
                            $data["MEASUREMENT"][TEXT] = $y;
                            $data["MEASUREMENT_AVG"][NUMBER] = $fields["WEIGHT".$x][$grade][$i];
                        } elseif($y == 3){
                            $data["MEASUREMENT"][TEXT] = $y;
                            $data["MEASUREMENT_AVG"][NUMBER] = $fields["SITHEIGHT".$x][$grade][$i];
                        }
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "BODYMEASURED_AVG_DAT");
                        $db->query($query);
                    }
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>