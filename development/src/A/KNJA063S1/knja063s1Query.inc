<?php

require_once('for_php7.php');

class knja063s1Query extends Query {

    //年度・学期
    function getTerm($model) {
        $query  = " SELECT ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        return $query;
    }

    //複式クラスコンボ
    function getGhrCd($model) {
        $query  = " SELECT ";
        $query .= "     GHR_CD || ':' || GHR_NAME AS LABEL, ";
        $query .= "     GHR_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GHR_CD = '{$model->ghr_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //担任名
    function getTeacher($model) {
        $query  = " SELECT ";
        $query .= "     L1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GHR_CD = '{$model->ghr_cd}' ";
        return $query;
    }

    //生徒リスト
    function getStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS SORT_NENKUMIBAN, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T3.NAME, ";
        $query .= "     T3.NAME_KANA AS KANA, ";
        $query .= "     TRANSLATE_KANA(T3.NAME_KANA) as T_KANA, ";
        $query .= "     T3.SEX, ";
        $query .= "     N1.NAME1 AS SEX_NAME, ";
        $query .= "     T4.GHR_ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ";
        //GHR
        $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T5 ON T5.YEAR = T4.YEAR AND T5.SEMESTER = T4.SEMESTER AND T5.GHR_CD = T4.GHR_CD ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR = '{$model->year}' ";
        $query .= "     AND T4.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T4.GHR_CD = '{$model->ghr_cd}' ";

        $order = array("desc", "asc");
        if ($model->sorttype == "0") {
            $sex_order = array("desc", "asc");
        } else {
            $sex_order = array("asc", "desc");
        }
        switch ($model->s_id)
        {
            case "1";
                $query .= " ORDER BY  T3.SEX ".$sex_order[$model->sort[$model->s_id]].", T_KANA ".$order[$model->sort[$model->s_id]]; break;
            case "2":
                $query .= " ORDER BY  T_KANA ".$order[$model->sort[$model->s_id]].", T3.SEX ".$sex_order[$model->sort[$model->s_id]]; break;
            case "3":
                $query .= " ORDER BY  T4.GHR_ATTENDNO ".$order[$model->sort[$model->s_id]]." ,SORT_NENKUMIBAN ".$order[$model->sort[$model->s_id]]; break;
            case "4":
                $query .= " ORDER BY  SORT_NENKUMIBAN ".$order[$model->sort[$model->s_id]]; break;
        }

        return $query;
    }

    //ソート
    function getSort()
    {
        $query  = " SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMESPARE1 IS NULL ";

        return $query;
    }

    //更新 
    function &getUpdateQuery($model) {
        if (get_count($model->schregno) == 0) {
            return false;
        }

        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($model->schregno); $i++) {
            $data["GHR_ATTENDNO"][TEXT] = sprintf("%03d",$model->attendno[$i]);
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND GHR_CD = '{$model->ghr_cd}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno[$i]."' ";

            $query = Query::updateSQL($data, "SCHREG_REGD_GHR_DAT", $where);
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return true;
    }
}
?>
