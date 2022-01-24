<?php

require_once('for_php7.php');

class knjm240Query extends Query {

    //学籍観点別状況データを取得する
    function selectQuery($model)
    {
        $query  = "SELECT ";
        $query .= "  REP_SEQ_ALL, ";
        $query .= "  REP_LIMIT ";
        $query .= "FROM ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CHAIRCD || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->chaircd."' ";
        } else {
            $query .= "  CHAIRCD || SUBCLASSCD = '".$model->chaircd."' ";
        }
        return $query;
    }
    //年
    function getSub_ClasyearQuery()
    {
        $query  = " SELECT DISTINCT YEAR ";
        $query .= " FROM CHAIR_DAT ";

        return $query;
    }

    //科目
    function ReadQuery($model)
    {
        $query  = " with sem1 as ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->Year."' AND ";
        $query .= "     SEMESTER = '1' AND ";
        $query .= "     CHAIRCD not like '92%' ";
        $query .= " ), sem2 as (SELECT ";
        $query .= "             CHAIRCD ";
        $query .= "          FROM ";
        $query .= "             CHAIR_DAT ";
        $query .= "          WHERE ";
        $query .= "             YEAR = '".$model->Year."' AND ";
        $query .= "             SEMESTER = '2' ";
        if ($model->control_data["学期数"] == 3) {
            $query .= " ), sem3(CHAIRCD) as (SELECT ";
            $query .= "                       CHAIRCD ";
            $query .= "                   FROM ";
            $query .= "                       CHAIR_DAT ";
            $query .= "                   WHERE ";
            $query .= "                       YEAR = '".$model->Year."' AND ";
            $query .= "                       SEMESTER = '3' ";
        }
        $query .= "                     ) ";
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t1.CHAIRNAME, ";
        $query .= "     t2.REP_SEQ_ALL, ";
        $query .= "     t2.REP_LIMIT, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     t3.SUBCLASSCD, ";
        }
        $query .= "     t3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     sem1 t1 ";
        $query .= "     left join CHAIR_CORRES_DAT t2 on t1.CHAIRCD = t2.CHAIRCD ";
        $query .= "          AND t2.year = '".$model->Year."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
            $query .= "          AND t1.SUBCLASSCD = t2.SUBCLASSCD ";
        }
        $query .= "     left join SUBCLASS_MST t3 on t1.SUBCLASSCD = t3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND t1.CLASSCD = t3.CLASSCD ";
            $query .= "          AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
            $query .= "          AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     t1.CHAIRCD in (SELECT ";
        $query .= "                        w1.CHAIRCD ";
        $query .= "                    FROM ";
        if ($model->control_data["学期数"] == 3) {
            $query .= "                        sem2 w1,sem3 w2 ";
            $query .= "                    WHERE ";
            $query .= "                        w1.CHAIRCD = w2.CHAIRCD ";
        } else {
            $query .= "                        sem2 w1 ";
        }
        $query .= "                    ) ";
        $query .= " ORDER BY t1.CHAIRCD ";

        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "SET ";
        $query .= "  REP_SEQ_ALL = " .$model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= "  REP_LIMIT = null , ";
        } else {
            $query .= "  REP_LIMIT = " .$model->field["CHECKCNT"].", ";
        }
        $query .= "  REGISTERCD = '" .STAFFCD."', ";
        $query .= "  UPDATED = SYSDATE() ";
        $query .= "WHERE ";
        $query .= "  YEAR     = '".$model->Year."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "  CHAIRCD || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->chaircd."' ";
        } else {
            $query .= "  CHAIRCD || SUBCLASSCD = '". $model->chaircd . "' ";
        }

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "INSERT INTO ";
        $query .= "  CHAIR_CORRES_DAT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $subArray = explode("-", $model->subclasscd);
            $query .= " VALUES('".$model->Year."','".substr($model->chaircd,0,7)."','".$subArray[0]."','".$subArray[1]."','".$subArray[2]."','".$subArray[3]."', ";
        } else {
            $query .= " VALUES('".$model->Year."','".substr($model->chaircd,0,7)."','".$model->subclasscd."', ";
        }
        $query .=   $model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null) {
            $query .= " null, ";
        } else {
            $query .=   $model->field["CHECKCNT"].", ";
        }
        $query .= " null,null, ";
        $query .= " '".STAFFCD."', ";
        $query .= "  SYSDATE())";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
