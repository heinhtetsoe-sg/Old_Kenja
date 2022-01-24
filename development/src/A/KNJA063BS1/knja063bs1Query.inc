<?php

require_once('for_php7.php');

class knja063bs1Query extends Query {

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
        $query .= "     LC_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || LC_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE || '-' || LC_CLASS = '{$model->ghr_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //担任名
    function getTeacher($model) {
        $query  = " SELECT ";
        $query .= "     STAFF.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_HDAT LCH ";
        $query .= "     LEFT JOIN STAFF_MST STAFF ON STAFF.STAFFCD = LCH.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "     LCH.YEAR = '{$model->year}' ";
        $query .= "     AND LCH.SEMESTER = '{$model->semester}' ";
        $query .= "     AND LCH.GRADE || '-' || LCH.LC_CLASS = '{$model->ghr_cd}' ";
        return $query;
    }

    //生徒リスト
    function getStudents($model) {
        $query  = " SELECT ";
        $query .= "     LCD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO AS SORT_NENKUMIBAN, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA AS KANA, ";
        $query .= "     TRANSLATE_KANA(BASE.NAME_KANA) as T_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     Z002.NAME1 AS SEX_NAME, ";
        $query .= "     LCD.ATTENDNO AS GHR_ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_DAT LCD ";
        $query .= "     INNER JOIN SCHREG_REGD_LC_HDAT LCH ON LCD.YEAR = LCH.YEAR ";
        $query .= "           AND LCD.SEMESTER = LCH.SEMESTER ";
        $query .= "           AND LCD.GRADE = LCH.GRADE ";
        $query .= "           AND LCD.LC_CLASS = LCH.LC_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = LCD.SCHREGNO ";
        $query .= "           AND REGD.YEAR = LCD.YEAR ";
        $query .= "           AND REGD.SEMESTER = LCD.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ";
        $query .= "           AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND REGDH.GRADE = REGD.GRADE ";
        $query .= "           AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = LCD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ";
        $query .= " WHERE ";
        $query .= "     LCD.YEAR = '{$model->year}' ";
        $query .= "     AND LCD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND LCD.GRADE || '-' || LCD.LC_CLASS = '{$model->ghr_cd}' ";

        $order = array("desc", "asc");
        if ($model->sorttype == "0") {
            $sex_order = array("desc", "asc");
        } else {
            $sex_order = array("asc", "desc");
        }
        switch ($model->s_id) {
            case "1";
                $query .= " ORDER BY  BASE.SEX ".$sex_order[$model->sort[$model->s_id]].", T_KANA ".$order[$model->sort[$model->s_id]]; break;
            case "2":
                $query .= " ORDER BY  T_KANA ".$order[$model->sort[$model->s_id]].", BASE.SEX ".$sex_order[$model->sort[$model->s_id]]; break;
            case "3":
                $query .= " ORDER BY  LCD.ATTENDNO ".$order[$model->sort[$model->s_id]]." ,SORT_NENKUMIBAN ".$order[$model->sort[$model->s_id]]; break;
            case "4":
                $query .= " ORDER BY  SORT_NENKUMIBAN ".$order[$model->sort[$model->s_id]]; break;
        }

        return $query;
    }

    //ソート
    function getSort() {
        $query  = " SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMESPARE1 IS NULL ";

        return $query;
    }

    //更新 
    function &getUpdateQuery($model) {
        if (get_count($model->schregno) == 0) {
            return false;
        }
        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);

        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($model->schregno); $i++) {
            $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->attendno[$i]);
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND GRADE = '{$grade}' ";
            $where .= "     AND LC_CLASS = '{$hrClass}' ";
            $where .= "     AND SCHREGNO = '".$model->schregno[$i]."' ";

            $query = Query::updateSQL($data, "SCHREG_REGD_LC_DAT", $where);
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return true;
    }
}
?>
