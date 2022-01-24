<?php

require_once('for_php7.php');

class knjm380Query extends Query {

    //学籍観点別状況データを取得する
    function selectQuery($model,$flg)
    {
        $query  = "SELECT ";
        $query .= "  SCH_SEQ_ALL, ";
        $query .= "  SCH_SEQ_MIN ";
        $query .= "FROM ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$model->Year."' AND ";
        if ($flg == ''){
            $query .= "  CHAIRCD = '".$model->chaircd."' ";
        }else {
            $query .= "  CHAIRCD = '".$flg."' ";
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
        $query  = " with sem1(CHAIRCD, ";
        $query .= "           CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           CLASSCD, ";
            $query .= "           SCHOOL_KIND, ";
            $query .= "           CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD ";
        $query .= "          ) as ( ";
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
        $query .= "     SEMESTER = '1' ";
        $query .= " ), ";
        $query .= " sem2(CHAIRCD) as (SELECT ";
        $query .= "                         CHAIRCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".$model->Year."' AND ";
        $query .= "                         SEMESTER = '2' ";
        if ($model->control_data["学期数"] == 3){
            $query .= "                     ), ";
            $query .= " sem3(CHAIRCD) as (SELECT ";
            $query .= "                         CHAIRCD ";
            $query .= "                     FROM ";
            $query .= "                         CHAIR_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".$model->Year."' AND ";
            $query .= "                         SEMESTER = '3' ";
        }
        $query .= "                     ) ";
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t1.CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t2.SCH_SEQ_ALL, ";
        $query .= "     t2.SCH_SEQ_MIN ";
        $query .= " FROM ";
        $query .= "     sem1 t1 left join CHAIR_CORRES_DAT t2 on t1.CHAIRCD = t2.CHAIRCD AND t2.year = '".$model->Year."' ";
        $query .= " WHERE ";
        $query .= "     t1.CHAIRCD in (SELECT ";
        $query .= "                        w1.CHAIRCD ";
        $query .= "                    FROM ";
        if ($model->control_data["学期数"] == 3){
            $query .= "                        sem2 w1,sem3 w2 ";
            $query .= "                    WHERE ";
            $query .= "                        w1.CHAIRCD = w2.CHAIRCD ";
        }else {
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
        $query .= "  SCH_SEQ_ALL = " .$model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null){
            $query .= "  SCH_SEQ_MIN = null , ";
        }else {
            $query .= "  SCH_SEQ_MIN = " .$model->field["CHECKCNT"].", ";
        }
        $query .= "  REGISTERCD = '" .STAFFCD."', ";
        $query .= "  UPDATED = SYSDATE() ";
        $query .= "WHERE ";
        $query .= "  YEAR     = '".$model->Year."' AND ";
        $query .= "  CHAIRCD = '". $model->chaircd . "' ";
        $query .= " ";

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
            $query .= " VALUES('".$model->Year."','".$model->chaircd."','".$model->classcd."','".$model->schoolKind."','".$model->curriculumcd."','".$model->subclasscd."', ";
        } else {
            $query .= " VALUES('".$model->Year."','".$model->chaircd."','".$model->subclasscd."', ";
        }
        $query .= " null,null, ";
        $query .=   $model->field["SCHCNT"].", ";
        if ($model->field["CHECKCNT"] == null){
            $query .= " null, ";
        }else {
            $query .=   $model->field["CHECKCNT"].", ";
        }
        $query .= " '".STAFFCD."', ";
        $query .= "  SYSDATE())";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
