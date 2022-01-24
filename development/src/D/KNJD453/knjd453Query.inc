<?php

require_once('for_php7.php');

class knjd453Query extends Query {

    //受講科目一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     L1.SCHREGNO AS SET_DATA ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                            AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                            AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T3 ON T3.YEAR = T1.YEAR    ";
        $query .= "                                 AND T3.CLASSCD = T2.CLASSCD ";
        $query .= "                                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                                 AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                                        AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                        AND L1.CLASSCD = T2.CLASSCD ";
        $query .= "                                                        AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                                                        AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                                                        AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '".$model->exp_semester."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";        

        return $query;
    }
    
    /* 更新処理 */
    function update(&$db, $model) {
    
        //対象データ一旦削除
        $query  = " DELETE FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT  ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR     = '{$model->exp_year}' ";
        $query .= " AND SEMESTER = '{$model->exp_semester}' ";
        $db->query($query);

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->exp_semester;
            $data["CLASSCD"][TEXT]              = substr($selectdata[$i],0,2);
            $data["SCHOOL_KIND"][TEXT]          = substr($selectdata[$i],3,1);
            $data["CURRICULUM_CD"][TEXT]        = substr($selectdata[$i],5,1);
            $data["SUBCLASSCD"][TEXT]           = substr($selectdata[$i],7,13);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";
            
            $query = Query::insertSQL($data, "EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT");
            $db->query($query);
        }
        return;
    }
}
?>
