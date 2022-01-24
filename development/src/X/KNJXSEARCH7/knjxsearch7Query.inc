<?php

require_once('for_php7.php');

class knjxsearch7Query extends Query {

    //年度取得
    function GetYear($year, $semester, $model)
    {
        $query  = "select ";
        $query .= " T1.year as graduate_year ";
        $query .= "from ";
        $query .= " (SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   SUBSTR(WORK,1,4) AS YEAR, ";
        $query .= "   SUBSTR(WORK,6,2) AS GRADE, ";
        $query .= "   SUBSTR(WORK,5,1) AS SEMESTER, ";
        $query .= "   SUBSTR(WORK,8,3) AS HR_CLASS ";
        $query .= "  FROM ";
        $query .= "   (SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     MAX(REGD.YEAR || REGD.SEMESTER || REGD.GRADE || REGD.HR_CLASS) AS WORK ";
        $query .= "    FROM ";
        $query .= "     GRD_REGD_DAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "    WHERE ";
        $query .= "    (REGD.YEAR = '" .$year ."' AND ";
        $query .= "     REGD.SEMESTER = '" .$semester ."') OR ";
        $query .= "     REGD.YEAR < '" .$year ."' ";
        $query .= "    GROUP BY ";
        $query .= "     REGD.SCHREGNO ";
        $query .= "   ) AS W1 ";
        $query .= " ) AS T1 ";
        $query .= "group by T1.year ";
        $query .= "order by graduate_year desc";

        return $query;
    }

    //卒業年組取得
    function getHrclass($g_year, $model)
    {
        $query  = " SELECT ";
        $query .= "     T2.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         G1.SCHREGNO, ";
        $query .= "         G1.YEAR, ";
        $query .= "         G1.GRADE, ";
        $query .= "         G1.SEMESTER, ";
        $query .= "         G1.HR_CLASS ";
        $query .= "     FROM GRD_REGD_DAT G1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON G1.YEAR = REG_G.YEAR ";
                $query .= "           AND G1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON G1.YEAR = REG_G.YEAR ";
            $query .= "           AND G1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN (";
        $query .= "         SELECT S1.SCHREGNO, S1.YEAR, MAX(S1.SEMESTER) AS SEMESTER ";
        $query .= "         FROM GRD_REGD_DAT S1 ";
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                 I1.SCHREGNO, ";
        $query .= "                 MAX(I1.YEAR) AS YEAR ";
        $query .= "              FROM ";
        $query .= "                 GRD_REGD_DAT I1 ";
        $query .= "                 LEFT JOIN (SELECT SCHREGNO FROM GRD_REGD_DAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."') IL2 ON IL2.SCHREGNO = I1.SCHREGNO ";
        $query .= "              WHERE ";
        $query .= "                 I1.YEAR < '".CTRL_YEAR."' ";
        $query .= "                 AND IL2.SCHREGNO IS NULL ";
        $query .= "              GROUP BY ";
        $query .= "                 I1.SCHREGNO ";
        $query .= "             ) S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.YEAR = S1.YEAR ";
        $query .= "         GROUP BY S1.SCHREGNO, S1.YEAR ";
        $query .= "         UNION ALL ";
        $query .= "         SELECT IT1.SCHREGNO, IT1.YEAR, IT1.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             GRD_REGD_DAT IT1 ";
        $query .= "         WHERE ";
        $query .= "             IT1.YEAR = '".CTRL_YEAR."' AND IT1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         ) IT1 ON IT1.SCHREGNO = G1.SCHREGNO AND IT1.YEAR = G1.YEAR AND IT1.SEMESTER = G1.SEMESTER ";
        $query .= "     ) AS T1, ";
        $query .= "     GRD_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$g_year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T2.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";

        return $query;
    }

    function SearchStudent($year,$semester,$fields,$model)
    {
        $query  = "WITH T_REGD_KEY as ( ";
        $query .= "    SELECT ";
        $query .= "            G1.SCHREGNO, ";
        $query .= "            G1.YEAR, G1.SEMESTER, G1.GRADE, G1.HR_CLASS, G1.ATTENDNO, ";
        $query .= "            (G1.YEAR || G1.SEMESTER || G1.GRADE || G1.HR_CLASS || G1.ATTENDNO) AS regd_key ";
        $query .= "    FROM ";
        $query .= "            GRD_REGD_DAT G1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON G1.YEAR = REG_G.YEAR ";
                $query .= "           AND G1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON G1.YEAR = REG_G.YEAR ";
            $query .= "           AND G1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         WHERE ";
        $query .= "         (G1.YEAR = '" .$year ."' AND ";
        $query .= "          G1.SEMESTER = '" .$semester ."') OR ";
        $query .= "          G1.YEAR < '" .$year ."' ";
        $query .= "), T2 as ( ";
        $query .= "    SELECT ";
        $query .= "        G1.SCHREGNO, ";
        $query .= "        G1.YEAR , G1.SEMESTER , G1.GRADE , G1.HR_CLASS , G1.ATTENDNO ";
        $query .= "    FROM ";
        $query .= "        T_REGD_KEY G1  ";
        $query .= "    INNER JOIN (SELECT SCHREGNO, MAX(REGD_KEY) AS REGD_KEY FROM T_REGD_KEY ";
        $query .= "                GROUP BY SCHREGNO ";
        $query .= "               ) G2 ON G2.SCHREGNO = G1.SCHREGNO AND G2.REGD_KEY = G1.REGD_KEY ";
        $query .= " )";
        $query .= "select t2.grade";
        $query .= "      ,t2.hr_class";
        $query .= "      ,t2.attendno";
        $query .= "      ,t1.name_show as name";
        $query .= "      ,t2.year as graduateyear";
        $query .= "      ,t1.name_kana as kana";
        $query .= "      ,t1.sex as sexnum";
        $query .= "      ,CASE t1.sex WHEN t4.namecd2 THEN t4.name2 ELSE t1.sex END as sex";
        $query .= "      ,t1.birthday";
        $query .= "      ,t1.schregno";
        $query .= "      ,t3.hr_name";
        $query .= "  from grd_base_mst t1 ";
        $query .= "  LEFT JOIN name_mst t4 on t4.namecd1 = 'Z002' and t1.sex = t4.namecd2 ";
        $query .= "      inner join T2 on t1.schregno = t2.schregno  ";
        $query .= "      inner join grd_regd_hdat t3 on t3.year = t2.year ";
        $query .= "          and t3.semester = t2.semester ";
        $query .= "          and t2.grade = t3.grade";
        $query .= "          and t2.hr_class = t3.hr_class";
        $query .= " where ";
        $query .= "   t1.schregno = t2.schregno ";
        if (strlen($fields["graduate_year"]))
            $query .= "   and t2.year = '".$fields["graduate_year"]."'";
        if (strlen($fields["graduate_class"]))
            $query .= "   and t2.grade || t2.hr_class = '".$fields["graduate_class"]."'";
        if (strlen($fields["kanji"]))
            $query .= "   and t1.name_show like '%".$fields["kanji"]."%'";
        if (strlen($fields["kana"]))
            $query .= "   and t1.name_kana like '%".$fields["kana"]."%'";
        $query .= " ORDER BY 1,2,3";

        return $query;
    }
}
?>
