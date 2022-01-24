<?php

require_once('for_php7.php');

class knjz404jQuery extends Query {

    /**********/
    /* 左画面 */
    /**********/
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."'  ";
        $query .= " AND SCHOOL_KIND IN ('P', 'J') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND DESC, ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象年度
    function selectYearTaisyou($model){
        $query  = " select distinct ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE";
        $query .= " from ";
        $query .= "     SCHOOL_MST; ";

        return $query;
    }

    //参照年度
    function selectYearSansyou($model){
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";

        return $query;
    }

    //科目一覧
    function SelectList($model){
        $query  = " SELECT ";
        $query .= "     JSUB.CLASSCD || '-' || JSUB.SCHOOL_KIND || '-' || JSUB.CURRICULUM_CD || '-' || JSUB.SUBCLASSCD AS VALUE, ";
        $query .= "     JSUB.CLASSCD || '-' || JSUB.SCHOOL_KIND || '-' || JSUB.CURRICULUM_CD || '-' || JSUB.SUBCLASSCD || ':' || value(SUBM.SUBCLASSNAME, '') AS LABEL, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     CASE WHEN JSUB2.JCNT > 0 THEN 'レ' ELSE '' END AS JCNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT YDAT ";
        $query .= " INNER JOIN JVIEWNAME_SUB_MST JSUB ON YDAT.YEAR          = '{$model->taisyouYear}' ";
        $query .= "                                  AND YDAT.CLASSCD       = JSUB.CLASSCD ";
        $query .= "                                  AND YDAT.SCHOOL_KIND   = JSUB.SCHOOL_KIND ";
        $query .= "                                  AND YDAT.CURRICULUM_CD = JSUB.CURRICULUM_CD ";
        $query .= "                                  AND YDAT.SUBCLASSCD    = JSUB.SUBCLASSCD ";
        $query .= "                                  AND YDAT.VIEWCD        = JSUB.VIEWCD ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST SUBM ON JSUB.CLASSCD       = SUBM.CLASSCD ";
        $query .= "                      AND JSUB.SCHOOL_KIND   = SUBM.SCHOOL_KIND ";
        $query .= "                      AND JSUB.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "                      AND JSUB.SUBCLASSCD    = SUBM.SUBCLASSCD ";
        $query .= " LEFT JOIN ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(*) AS JCNT ";
        $query .= "     FROM ";
        $query .= "         JVIEW_SUB_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->taisyouYear}' ";
        if ($model->grade) {
            $query .= "         AND GRADE = '{$model->grade}'  ";
        }
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     ) AS JSUB2 ";
        $query .= "     ON JSUB.CLASSCD        = JSUB2.CLASSCD ";
        $query .= "     AND JSUB.SCHOOL_KIND   = JSUB2.SCHOOL_KIND ";
        $query .= "     AND JSUB.CURRICULUM_CD = JSUB2.CURRICULUM_CD ";
        $query .= "     AND JSUB.SUBCLASSCD    = JSUB2.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     JSUB.CLASSCD, ";
        $query .= "     JSUB.SCHOOL_KIND, ";
        $query .= "     JSUB.CURRICULUM_CD, ";
        $query .= "     JSUB.SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSNAME, ";
        $query .= "     JSUB2.JCNT ";

        return $query;
    }

    /**********/
    /* 右画面 */
    /**********/
    //段階取得
    function getDankai() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE1 "; //(玉川聖)10 (成城)5
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E064' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return strlen($row["NAMESPARE1"]) ? (int)$row["NAMESPARE1"] : 10;
    }

    //科目名
    function getSubclassName($model){
        $query  = " SELECT ";
        $query .= "     JSUB.CLASSCD || '-' || JSUB.SCHOOL_KIND || '-' || JSUB.CURRICULUM_CD || '-' || JSUB.SUBCLASSCD AS VALUE, ";
        $query .= "     JSUB.CLASSCD || '-' || JSUB.SCHOOL_KIND || '-' || JSUB.CURRICULUM_CD || '-' || JSUB.SUBCLASSCD || ':' || SUBM.SUBCLASSNAME AS LABEL, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_MST JSUB ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST SUBM ON JSUB.CLASSCD       = SUBM.CLASSCD ";
        $query .= "                      AND JSUB.SCHOOL_KIND   = SUBM.SCHOOL_KIND ";
        $query .= "                      AND JSUB.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "                      AND JSUB.SUBCLASSCD    = SUBM.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     JSUB.CLASSCD || '-' || JSUB.SCHOOL_KIND || '-' || JSUB.CURRICULUM_CD || '-' || JSUB.SUBCLASSCD = '{$model->subclassCd}' ";
        $query .= " GROUP BY ";
        $query .= "     JSUB.CLASSCD, ";
        $query .= "     JSUB.SCHOOL_KIND, ";
        $query .= "     JSUB.CURRICULUM_CD, ";
        $query .= "     JSUB.SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSNAME ";

        return $query;
    }

    //観点取得
    function selectViewcdQuery($model){
        $substr_classcd       = substr($model->subclassCd, 0, 2);
        $substr_school_kind   = substr($model->subclassCd, 3, 1);
        $substr_curriculum_cd = substr($model->subclassCd, 5, 1);
        $substr_subclasscd    = substr($model->subclassCd, 7, 6);

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR               = '{$model->taisyouYear}' ";
        $query .= "     AND T1.CLASSCD            = '{$substr_classcd}' ";
        $query .= "     AND T1.SCHOOL_KIND        = '{$substr_school_kind}' ";
        $query .= "     AND T1.CURRICULUM_CD      = '{$substr_curriculum_cd}' ";
        $query .= "     AND T1.SUBCLASSCD         = '{$substr_subclasscd}' ";
        $query .= "     AND T1.CLASSCD            = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND        = T2.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD      = T2.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD         = T2.SUBCLASSCD ";
        $query .= "     AND T1.VIEWCD             = T2.VIEWCD ";
        $query .= "     AND SUBSTR(T1.VIEWCD,1,2) = '{$substr_classcd}' ";
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    function getRightList($model){
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '{$model->taisyouYear}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='{$model->subclassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     SCORE DESC ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    function deleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->taisyouYear}'  ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='{$model->subclassCd}'  ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function insertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->subclassCd);

        $data["YEAR"][TEXT]         = $model->taisyouYear;
        $data["GRADE"][TEXT]        = $model->grade;
        $data["CLASSCD"][TEXT]      = $classcd;
        $data["SCHOOL_KIND"][TEXT]  = $school_kind;
        $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        for ($i = 1; $i <= $model->dankai; $i++) {
            $data['SCORE'][NUMBER]    = $i;
            for ($j = 1; $j <= 10; $j++) {
                $data['JVIEW'.$j][TEXT]    = $model->data['JVIEW_'.$i.'_'.$j];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "JVIEW_SUB_MST");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    function copySansyouCount($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->sansyouYear}' ";

        $ret = $db->getOne($query);
        $db->commit();
        Query::dbCheckIn($db);

        return $ret;
    }

    function copyTaisyouCount($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->taisyouYear}' ";

        $ret = $db->getOne($query);
        $db->commit();
        Query::dbCheckIn($db);

        return $ret;
    }

    function copyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->sansyouYear}' ";

        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data["YEAR"][TEXT]          = $model->taisyouYear;
            $data["GRADE"][TEXT]         = $row['GRADE'];
            $data["CLASSCD"][TEXT]       = $row['CLASSCD'];
            $data["SCHOOL_KIND"][TEXT]   = $row['SCHOOL_KIND'];
            $data["CURRICULUM_CD"][TEXT] = $row['CURRICULUM_CD'];
            $data["SUBCLASSCD"][TEXT]    = $row['SUBCLASSCD'];
            $data['SCORE'][NUMBER]       = $row['SCORE'];
            $data['JVIEW1'][TEXT]        = $row['JVIEW1'];
            $data['JVIEW2'][TEXT]        = $row['JVIEW2'];
            $data['JVIEW3'][TEXT]        = $row['JVIEW3'];
            $data['JVIEW4'][TEXT]        = $row['JVIEW4'];
            $data['JVIEW5'][TEXT]        = $row['JVIEW5'];
            $data['JVIEW6'][TEXT]        = $row['JVIEW6'];
            $data['JVIEW7'][TEXT]        = $row['JVIEW7'];
            $data['JVIEW8'][TEXT]        = $row['JVIEW8'];
            $data['JVIEW9'][TEXT]        = $row['JVIEW9'];
            $data['JVIEW10'][TEXT]       = $row['JVIEW10'];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $query = Query::insertSQL($data, "JVIEW_SUB_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
