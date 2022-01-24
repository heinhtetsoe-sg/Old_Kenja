<?php

require_once('for_php7.php');

class knjz211Query extends Query {

    //Z040
    function getZ040() {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 || '：' || T1.NAME1 AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z040' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model) {
        $query  =  " SELECT * FROM ASSESS_COURSE_MST ";
        $query .=  "  WHERE ASSESSCD    = '".$model->sepa."'";
        $query .=  "    AND COURSECD || MAJORCD = '".$model->courseMajor."'";
        $query .=  "    AND COURSECODE  = '".$model->courseCode."'";
        $query .=  " ORDER BY ASSESSLEVEL ";

        return $query;
    }

    //評定段階数
    function getAssessLevelCnt($model) {
        $query  =  " SELECT COUNT(*) AS ASSESSLEVELCNT FROM ASSESS_COURSE_MST ";
        $query .=  "  WHERE ASSESSCD    = '".$model->sepa."'";
        $query .=  "    AND COURSECD || MAJORCD = '".$model->courseMajor."'";
        $query .=  "    AND COURSECODE  = '".$model->courseCode."'";

        return $query;
    }

    //課程学科コンボ
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L3.COURSENAME || T1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1 ";
        $query .= "     LEFT JOIN COURSE_MST L3 ";
        $query .= "         ON  L3.COURSECD = T1.COURSECD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースコンボ
    function getCourseCode($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $coursecd = substr($model->courseMajor, 0, 1);
        $majorcd  = substr($model->courseMajor, 1, 3);

        //削除
        $query  =  " DELETE FROM ASSESS_COURSE_MST ";
        $query .=  "  WHERE ASSESSCD    = '".$model->sepa."'";
        $query .=  "    AND COURSECD    = '".$coursecd."'";
        $query .=  "    AND MAJORCD     = '".$majorcd."'";
        $query .=  "    AND COURSECODE  = '".$model->courseCode."'";
        $db->query($query);

        $sepaMax = $model->sepa == '4' ? 5 : 100;
        //追加
        for ($i = 1; $i <= $model->level; $i++) {
            $data = array();

            $data["ASSESSCD"][TEXT]         = $model->sepa;
            $data["COURSECD"][TEXT]         = $coursecd;
            $data["MAJORCD"][TEXT]          = $majorcd;
            $data["COURSECODE"][TEXT]       = $model->courseCode;
            $data["ASSESSLEVEL"][NUMBER]    = $i;

            $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
            $data["ASSESSLOW"][NUMBER]      = ($i == 1) ? 0 : $model->field["ASSESSLOW".$i];
            if ($i == $model->level) {
                $data["ASSESSHIGH"][NUMBER] = $sepaMax;
            } else if ($model->sepa == 4) {
                $data["ASSESSHIGH"][NUMBER] = ($model->field["ASSESSLOW".($i + 1)] - 0.1);
            } else {
                $data["ASSESSHIGH"][NUMBER] = ($model->field["ASSESSLOW".($i + 1)] - 1);
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_COURSE_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
