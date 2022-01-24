<?php

require_once('for_php7.php');

class knjosearchQuery extends Query {
    //�Z��擾
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
            if ($model->setSchoolKind != "") {
                $query .= "     AND NAME1 = '".$model->setSchoolKind."' ";
            } else if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    function getGradeHrClass($model) {
        $query  = "SELECT ";
        $query .= "     REG_H.GRADE || REG_H.HR_CLASS AS VALUE, ";
        $query .= "     REG_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REG_H ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR        = REG_G.YEAR ";
            $query .= "                                       AND REG_H.GRADE       = REG_G.GRADE ";
            $query .= "                                       AND REG_G.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
            $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REG_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REG_H.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function GetYear()
    {
        $query  = "select distinct datecnv(transfer_sdate, 1) as graduate_year";
        $query .= "  from schreg_transfer_dat";
        $query .= " where transfercd <> '9'";
        $query .= " order by graduate_year desc";
        return $query;
    }
    function SearchStudent($fields, $model)
    {
        $query  = "select t2.grade";
        $query .= "      ,t2.hr_class";
        $query .= "      ,t2.attendno";
        $query .= "      ,t1.name_show as name";
        $query .= "      ,substr(CHAR(t1.ent_date),1,4) as graduateyear";
        $query .= "      ,t1.name_kana as kana";
        $query .= "      ,t1.sex as sexnum";
        $query .= "      ,CASE t1.sex WHEN t4.namecd2 THEN t4.name2 ELSE t1.sex END as sex";
        $query .= "      ,t1.birthday";
        $query .= "      ,t1.schregno";
        $query .= "      ,t3.hr_name";
        $query .= "  from schreg_base_mst t1 LEFT OUTER JOIN ";
        $query .= "      (select name2, namecd2 from name_mst where namecd1 = 'Z002') t4 on t1.sex = t4.namecd2";
        $query .= "      ,schreg_regd_dat t2";
        $query .= "      ,schreg_regd_hdat t3";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON t3.YEAR = REG_G.YEAR ";
            $query .= "           AND t3.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON t3.YEAR = REG_G.YEAR ";
            $query .= "           AND t3.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " where ";
        $query .= "       t1.schregno = t2.schregno";
        $query .= "   and t2.year = '" .$fields["year"]."'";
        $query .= "   and t2.semester = '" .$fields["semester"]."'";
        $query .= "   and t3.year = '" .$fields["year"]."'";
        $query .= "   and t3.semester = '" .$fields["semester"]."'";
        $query .= "   and t2.grade = t3.grade";
        $query .= "   and t2.hr_class = t3.hr_class";
        if (strlen($fields["grade"]))
            $query .= "   and t2.grade = '".$fields["grade"]."'";
        if (strlen($fields["class"]))
            $query .= "   and t2.hr_class = '".$fields["class"]."'";
        if (strlen($fields["kanji"]))
            $query .= "   and t1.name_show like '%".$fields["kanji"]."%'";
        if (strlen($fields["kana"]))
            $query .= "   and t1.name_kana like '%".$fields["kana"]."%'";
        $query .= " ORDER BY t2.grade,t2.hr_class,t2.attendno";

        return $query;
    }
}
?>
