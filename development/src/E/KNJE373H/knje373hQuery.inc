<?php

require_once('for_php7.php');
class knje373hQuery extends Query
{
    //地区取得
    public function getDistrict($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        if ($model->field["SCHOOLDIV"] == "1") {
            // 出身学校の場合
            $query .= "     NAMECD1 = 'Z003' ";
        } else {
            // 塾の場合
            $query .= "     NAMECD1 = 'Z060' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校 or 出身塾一覧取得
    public function getSchool($model)
    {
        $lastYear = CTRL_YEAR - 1;
        $query  = " WITH DISTRICT AS ( ";
        if ($model->field["SCHOOLDIV"] == "1") {
            // 出身学校の場合
            $query .= " SELECT ";
            $query .= "     FIN.FINSCHOOLCD AS SCHOOLCD, ";
            $query .= "     FIN.DISTRICTCD, ";
            $query .= "     FIN.FINSCHOOLCD AS VALUE, ";
            $query .= "     VALUE(Z003.NAMECD2 || '：' || Z003.NAME1 || ' ｜ ' || FIN.FINSCHOOL_NAME, FIN.FINSCHOOL_NAME) AS LABEL ";
            $query .= " FROM ";
            $query .= "     FINSCHOOL_MST FIN ";
            $query .= "     LEFT JOIN V_NAME_MST Z003 ON ";
            $query .= "                Z003.NAMECD1 = 'Z003' ";
            $query .= "            AND Z003.NAMECD2 = FIN.DISTRICTCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            // 塾の場合
            $query .= " SELECT ";
            $query .= "     PRI.PRISCHOOLCD AS SCHOOLCD, ";
            $query .= "     PRI.DISTRICTCD, ";
            $query .= "     PRI.PRISCHOOLCD AS VALUE, ";
            $query .= "     VALUE(Z060.NAMECD2 || '：' || Z060.NAME1 || ' ｜ ' || PRI.PRISCHOOL_NAME, PRI.PRISCHOOL_NAME) AS LABEL ";
            $query .= " FROM ";
            $query .= "     PRISCHOOL_MST PRI ";
            $query .= "     LEFT JOIN NAME_MST Z060 ON ";
            $query .= "                Z060.NAMECD1 = 'Z060' ";
            $query .= "            AND Z060.NAMECD2 = PRI.DISTRICTCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     DISTRICT.VALUE, ";
        $query .= "     DISTRICT.LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT DAT ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON ";
        $query .= "                GDAT.YEAR        = DAT.YEAR ";
        $query .= "            AND GDAT.GRADE       = DAT.GRADE ";
        $query .= "            AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "                BASE.SCHREGNO = DAT.SCHREGNO ";
        $query .= "     INNER JOIN DISTRICT ON ";
        if ($model->field["SCHOOLDIV"] == "1") {
            $query .= "                BASE.FINSCHOOLCD = DISTRICT.SCHOOLCD ";
        } else {
            $query .= "                BASE.PRISCHOOLCD = DISTRICT.SCHOOLCD ";
        }
        if ($model->field["DISTRICTCD"] != "ALL") {
            if ($model->field["DISTRICTCD"] != "EXCEPT") {
                $query .= "            AND DISTRICT.DISTRICTCD = '".$model->field["DISTRICTCD"]."' ";
            } else {
                $query .= "            AND DISTRICT.DISTRICTCD IS NULL "; //未登録コード
            }
        }
        $query .= "     INNER JOIN (SELECT ";
        $query .= "                     MAX(YEAR) AS GRAD_YEAR, ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     AFT_GRAD_COURSE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR IN ('".CTRL_YEAR."', '".$lastYear."') ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                ) GRAD ON ";
        $query .= "                GRAD.SCHREGNO = BASE.SCHREGNO ";

        $query .= " WHERE ";
        if ($model->field["GRD_DIV"] == "1") {
            // 全ての場合
            // 在学生　＋　卒業生
            $query .= "                ( ";
            // 在校生
            $query .= "                    ( ";
            $query .= "                         DAT.YEAR     = '".CTRL_YEAR."' "; // 今年
            $query .= "                     AND DAT.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                    ) ";
            // 1:卒業
            $query .= "                OR  ( ";
            $query .= "                         DAT.YEAR       = '".$lastYear."' "; // 去年
            $query .= "                     AND DAT.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".$lastYear."' AND SEMESTER <> '9') ";
            $query .= "                     AND DAT.GRADE = (SELECT NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'A023' AND NAME1 = '".SCHOOLKIND."') ";
            $query .= "                     AND BASE.GRD_DIV   = '1' ";
            $query .= "                    ) ";
            $query .= "                ) ";
        } elseif ($model->field["GRD_DIV"] == "2") {
            // 在学生の場合
            // 今年の進路情報（AFT_GRAD_COURSE_DAT）が登録されており、
            $query .= "                DAT.YEAR    = '".CTRL_YEAR."' "; // 今年
            $query .= "            AND DAT.SEMESTER = '".CTRL_SEMESTER."' ";
            //$query .= "            AND DAT.GRADE = (SELECT NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'A023' AND NAME1 = '".SCHOOLKIND."') ";
        } elseif ($model->field["GRD_DIV"] == "3") {
            // 卒業生の場合
            // 今年の進路情報（AFT_GRAD_COURSE_DAT）が登録されており、
            // 昨年（ログイン年度の前年度）に卒業した人
            $lastYear = CTRL_YEAR - 1;
            $query .= "                DAT.YEAR    = '".$lastYear."' "; // 去年
            $query .= "            AND DAT.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".$lastYear."' AND SEMESTER <> '9') ";
            $query .= "            AND DAT.GRADE = (SELECT NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'A023' AND NAME1 = '".SCHOOLKIND."') ";
            $query .= "            AND BASE.GRD_DIV = '1' "; // 1:卒業
        }

        $query .= " GROUP BY ";
        $query .= "     DISTRICT.DISTRICTCD, ";
        $query .= "     DISTRICT.VALUE, ";
        $query .= "     DISTRICT.LABEL ";

        $query .= " ORDER BY ";
        $query .= "     DISTRICT.DISTRICTCD, ";
        $query .= "     DISTRICT.VALUE, ";
        $query .= "     DISTRICT.LABEL ";

        return $query;
    }
}
