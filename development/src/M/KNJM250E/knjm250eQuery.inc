<?php

require_once('for_php7.php');

class knjm250eQuery extends Query {

    //データ読込
    function ReadQuery($model) {
        $query  = " WITH SEM1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '1' AND ";
        $query .= "         CHAIRCD NOT LIKE '92%' ";
        $query .= " ), SEM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->ObjYear."' AND ";
        $query .= "         SEMESTER    = '2' ";
        if ($model->control_data["学期数"] == 3){
            $query .= " ), SEM3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIRCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".$model->ObjYear."' AND ";
            $query .= "         SEMESTER    = '3' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.REP_SEQ_ALL, ";
        $query .= "     T2.REP_LIMIT, ";
        $query .= "     T2.REP_START_SEQ, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T3.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SEM1 T1 ";
        $query .= "     LEFT JOIN CHAIR_CORRES_DAT T2 ON T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "                                  AND T2.YEAR    = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                              AND T1.CLASSCD         = T2.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T1.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        if ($model->control_data["学期数"] > 1) {
            $query .= " WHERE ";
            $query .= "     T1.CHAIRCD IN ( SELECT ";
            $query .= "                         W1.CHAIRCD ";
            $query .= "                     FROM ";
            if ($model->control_data["学期数"] == 3){
                $query .= "                         SEM2 W1, ";
                $query .= "                         SEM3 W2 ";
                $query .= "                     WHERE ";
                $query .= "                         W1.CHAIRCD = W2.CHAIRCD ";
            } else {
                $query .= "                         SEM2 W1 ";
            }
            $query .= "                    ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //名称
    function GetName($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //回数取得
    function Getperiod($model, $chrsubcd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REP_SEQ_ALL, ";
        $query .= "     REP_START_SEQ ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        }

        return $query;
    }

    //課程学科取得
    function GetCourseMajor($model)
    {
        $query  = " WITH COURSE AS (  ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE  ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COURSE.*, ";
        $query .= "   COURSE.COURSECD || COURSE.MAJORCD || '-' || COURSE.COURSECODE VALUE, ";
        $query .= "   COURSE.COURSECD || COURSE.MAJORCD || '-' || COURSE.COURSECODE || ':' || T1.COURSENAME || T2.MAJORNAME || T3.COURSECODENAME LABEL, ";
        $query .= "   T1.COURSENAME, ";
        $query .= "   T2.MAJORNAME, ";
        $query .= "   T3.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSE ";
        $query .= "   INNER JOIN COURSE_MST T1 ";
        $query .= "     ON T1.COURSECD = COURSE.COURSECD ";
        $query .= "   INNER JOIN MAJOR_MST T2 ";
        $query .= "     ON T2.COURSECD = COURSE.COURSECD ";
        $query .= "     AND T2.MAJORCD = COURSE.MAJORCD ";
        $query .= "   INNER JOIN COURSECODE_MST T3 ";
        $query .= "     ON T3.COURSECODE = COURSE.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "   COURSECD, ";
        $query .= "   MAJORCD, ";
        $query .= "   COURSECODE ";

        return $query;
    }

    //提出基準データ
    function GetRepdata($model, $subcnt) {
        $query  = " SELECT  ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     STANDARD_SEQ, ";
        $query .= "     CHAIRCD, ";
        $query .= "     REPORTDIV, ";
        $query .= "     STANDARD_DATE, ";
        $query .= "     DEADLINE_DATE, ";
        $query .= "     RETURN_DATE, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     year = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "       AND COURSECD || MAJORCD || '-' || COURSECODE = '".$model->coursemajor."' ";
        }

        $query .= "     AND STANDARD_SEQ = ".$subcnt ;

        return $query;
    }

    //DELETE(更新前処理)
    function getDeleteQuery(&$model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "       AND COURSECD || MAJORCD || '-' || COURSECODE = '".$model->coursemajor."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE(更新処理)
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setdata = array();
        if ($model->Properties["useCurriculumcd"] == "1") {
            $subArray = explode("-", substr($model->sub, 7));
        }
        for ($i = $model->repstartcnt; $i < ($model->repstartcnt + $model->repcntall); $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->ObjYear;
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $subArray[0];
                $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
                $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
                $data["SUBCLASSCD"][TEXT]       = $subArray[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = substr($model->sub, 7, 6);
            }
            if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
                $data["COURSECD"][TEXT]    = substr($model->coursemajor,0,1);
                $data["MAJORCD"][TEXT]     = sprintf("%03d",substr($model->coursemajor,1,3));
                $data["COURSECODE"][TEXT]  = sprintf("%04d",substr($model->coursemajor,5,4));
            }
            $data["STANDARD_SEQ"][NUMBER]   = $i;
            $data["CHAIRCD"][TEXT]          = substr($model->sub,0,7);
            $data["REPORTDIV"][TEXT]        = $model->setdata["REPDIV"][$i];
            $data["STANDARD_DATE"][TEXT]    = str_replace("/","-",$model->setdata["BASEDAY"][$i]);
            $data["RETURN_DATE"][TEXT]      = str_replace("/","-",$model->setdata["RETRYDAY"][$i]);
            $data["DEADLINE_DATE"][TEXT]    = str_replace("/","-",$model->setdata["ENDDAY"][$i]);
            $data["REMARK"][TEXT]           = $model->setdata["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
                $query = Query::insertSQL($data, "REP_STANDARDDATE_COURSE_DAT");
            } else {
                $query = Query::insertSQL($data, "REP_STANDARDDATE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
