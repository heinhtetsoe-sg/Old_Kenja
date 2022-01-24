<?php

require_once('for_php7.php');

class knjm250Query extends Query {

    //回数
    function Getperiod($model, $chrsubcd)
    {
        $query  = " SELECT REP_SEQ_ALL ";
        $query .= " FROM CHAIR_CORRES_DAT ";
        $query .= " WHERE YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($chrsubcd, 7)."' ";
        }
        return $query;
    }

    //提出基準データ
    function GetRepdata($model,$subcnt)
    {
        $query  = " SELECT  ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     STANDARD_SEQ, ";
        $query .= "     CHAIRCD, ";
        $query .= "     REPORTDIV, ";
        $query .= "     STANDARD_DATE, ";
        $query .= "     DEADLINE_DATE, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     year = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        $query .= "     AND STANDARD_SEQ = ".$subcnt ;

        return $query;
    }

    //名称
    function GetName($model,$namecd1)
    {
        $query  = " SELECT NAME1,NAMECD2 FROM v_name_mst ";
        $query .= "  WHERE year    = '".$model->ObjYear."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";

        return $query;
    }

    //DELETE(更新前処理)
    function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM REP_STANDARDDATE_DAT ";
        $query .= " WHERE YEAR = '".$model->ObjYear."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".substr($model->sub, 7)."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".substr($model->sub, 7)."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE(更新処理)
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setdata = array();
        if ($model->Properties["useCurriculumcd"] == "1") {
            $subArray = explode("-", substr($model->sub, 7));
        }
        for ($i = 0; $i < get_count($model->setdata["REPDIV"]); $i++) {
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
            $data["STANDARD_SEQ"][NUMBER]   = $model->repcntall - ($model->repcntall - ($i+1) );
            $data["CHAIRCD"][TEXT]          = substr($model->sub,0,7);
            $data["REPORTDIV"][TEXT]        = $model->setdata["REPDIV"][$i];
            $data["STANDARD_DATE"][TEXT]    = str_replace("/","-",$model->setdata["BASEDAY"][$i]);
            $data["DEADLINE_DATE"][TEXT]    = str_replace("/","-",$model->setdata["ENDDAY"][$i]);
            $data["REMARK"][TEXT]           = $model->setdata["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "REP_STANDARDDATE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
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
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     SEMESTER = '1' AND ";
        $query .= "     CHAIRCD not like '92%' ";
        $query .= " ), sem2 as ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND SEMESTER = '2' ";
        if ($model->control_data["学期数"] == 3){
            $query .= " ), sem3 as ( ";
            $query .= " SELECT ";
            $query .= "     CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$model->ObjYear."' ";
            $query .= "     AND SEMESTER = '3' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t1.CHAIRNAME, ";
        $query .= "     t1.SUBCLASSCD, ";
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
        $query .= "     AND t2.year = '".$model->ObjYear."' ";
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

}
?>
