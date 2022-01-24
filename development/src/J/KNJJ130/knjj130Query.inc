<?php

require_once('for_php7.php');

class knjj130Query extends Query {
    //校種取得
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
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //クラブ取得
    function getClubNameList($model)
    {
        $query  = "SELECT T1.CLUBCD AS VALUE,T2.CLUBNAME AS LABEL ";
        $query .= "FROM   CLUB_YDAT T1 ";
        $query .= "   INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ";
        //参照・更新可（制限付き）---2005.06.02
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "   INNER JOIN ( SELECT CLUBCD ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " , SCHOOLCD, SCHOOL_KIND ";
            }
            $query .= "                FROM CLUB_ADVISER_DAT ";
            $query .= "                WHERE  YEAR = '".CTRL_YEAR."' AND ADVISER = '".STAFFCD."' ";
            $query .= "              ) T3 ON T3.CLUBCD = T2.CLUBCD ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T3.SCHOOLCD = T1.SCHOOLCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            }
        }
        $query .= "WHERE  T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY T1.CLUBCD ";

        return $query;
    }

    //ソート一覧取得
    function getSortList()
    {
        $query .= " SELECT ";
        $query .= "     '開催地域' AS LABEL, ";
        $query .= "     '01' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '日付' AS LABEL, ";
        $query .= "     '02' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '種目' AS LABEL, ";
        $query .= "     '03' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '成績' AS LABEL, ";
        $query .= "     '04' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '年組番号' AS LABEL, ";
        $query .= "     '05' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力
    function getCsvQuery($model) {
        $club_select = "('";
        if ($model->selectdata) {
            $comma = "";
            foreach (explode(",", $model->selectdata) as $val) {
                $club_select .= $comma.$val;
                $comma = "','";
            }
        }
        $club_select .= "')";
        
        $sort_select = "";
        if ($model->selectdata2) {
            $comma = "";
            foreach (explode(",", $model->selectdata2) as $val) {
                if ($val == '01') $val = "T1.HOSTCD";
                if ($val == '02') $val = "T1.DETAIL_DATE";
                if ($val == '03') $val = "T1.ITEMCD, T1.KINDCD";
                if ($val == '04') $val = "T1.RECORDCD";
                if ($val == '05') $val = "L6.GRADE DESC, L6.HR_CLASS, L6.ATTENDNO";
                $sort_select .= $comma.$val;
                $comma = ",";
            }
        }

        $query  = "  WITH MAX_REGD AS (  ";
        $query .= "  SELECT  ";
        $query .= "      T1.YEAR,  ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      MAX(T1.SEMESTER) AS SEMESTER  ";
        $query .= "  FROM  ";
        $query .= "      SCHREG_REGD_DAT  T1 ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T1.YEAR ";
        $query .= "                                  AND GDAT.GRADE = T1.GRADE ";
        if ($model->Properties["useClubMultiSchoolKind"] != "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
                if (isset($model->field["SCHKIND"])) {
                    $query .= "     AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."'  ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= "       AND GDAT.SCHOOL_KIND = '"+ SCHOOLKIND +"'  ";
            }
        }
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "  GROUP BY  ";
        $query .= "      T1.YEAR,  ";
        $query .= "      T1.SCHREGNO  ";
        $query .= "  ) ";
        $query .= "  SELECT  ";
        $query .= "      T1.CLUBCD,  ";
        $query .= "      L0.CLUBNAME,  ";
        $query .= "      T1.HOSTCD,  ";
        $query .= "      L1.HOSTNAME,  ";
        $query .= "      T1.MEET_NAME,  ";
        $query .= "      T1.DETAIL_DATE,  ";
        $query .= "      L2.SEX,  ";
        $query .= "      L3.NAME1 AS SEX_NAME,  ";
        $query .= "      T1.ITEMCD,  ";
        $query .= "      T1.KINDCD,  ";
        $query .= "      L4.KINDNAME,  ";
        $query .= "      T1.DIV,  ";
        $query .= "      CASE WHEN T1.DIV = '1'  ";
        $query .= "           THEN '個人'  ";
        $query .= "           ELSE '団体'  ";
        $query .= "      END AS DIV_NAME,  ";
        $query .= "      T1.RECORDCD,  ";
        $query .= "      L5.RECORDNAME,  ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      L7.HR_NAME,  ";
        $query .= "      L6.ATTENDNO,  ";
        $query .= "      L2.NAME,  ";
        $query .= "      L2.FINSCHOOLCD,  ";
        $query .= "      L8.FINSCHOOL_NAME,  ";
        $query .= "      CASE WHEN T1.DOCUMENT IS NOT NULL AND T1.DETAIL_REMARK IS NOT NULL  ";
        $query .= "           THEN T1.DOCUMENT || '/' || T1.DETAIL_REMARK  ";
        $query .= "           ELSE CASE WHEN T1.DOCUMENT IS NOT NULL  ";
        $query .= "                     THEN T1.DOCUMENT  ";
        $query .= "                     ELSE T1.DETAIL_REMARK  ";
        $query .= "                END  ";
        $query .= "      END AS REMARK  ";
        $query .= "  FROM  ";
        $query .= "      SCHREG_CLUB_HDETAIL_DAT T1  ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "      LEFT JOIN CLUB_DETAIL_DAT CDET001 ON CDET001.YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND CDET001.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "       AND CDET001.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "       AND CDET001.CLUBCD = T1.CLUBCD ";
            $query .= "       AND CDET001.SEQ = '001' ";
        }
        $query .= "      LEFT JOIN CLUB_MST L0 ON T1.CLUBCD = L0.CLUBCD  ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND L0.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L0.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L0.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND L0.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L0.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L0.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L0.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L0.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L0.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      LEFT JOIN CLUB_HOST_MST L1 ON T1.HOSTCD = L1.HOSTCD  ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L1.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
            $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO  ";
        $query .= "      LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002'  ";
        $query .= "           AND L2.SEX = L3.NAMECD2  ";
        $query .= "      LEFT JOIN CLUB_ITEM_KIND_MST L4 ON T1.ITEMCD = L4.ITEMCD  ";
        $query .= "           AND T1.KINDCD = L4.KINDCD  ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND L4.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L4.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
            $query .= " AND L4.SCHOOLCD = '".SCHOOLCD."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND L4.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L4.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L4.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L4.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L4.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L4.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      LEFT JOIN CLUB_RECORD_MST L5 ON T1.RECORDCD = L5.RECORDCD  ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND L5.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L5.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
            $query .= " AND L5.SCHOOLCD = '".SCHOOLCD."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND L5.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L5.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L5.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L5.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND L5.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND L5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      INNER JOIN (SELECT  ";
        $query .= "                     R1.*  ";
        $query .= "                 FROM  ";
        $query .= "                     SCHREG_REGD_DAT R1,  ";
        $query .= "                     MAX_REGD R2  ";
        $query .= "                 WHERE  ";
        $query .= "                     R1.YEAR = R2.YEAR  ";
        $query .= "                     AND R1.SEMESTER = R2.SEMESTER  ";
        $query .= "                     AND R1.SCHREGNO = R2.SCHREGNO  ";
        $query .= "      ) L6 ON T1.SCHREGNO = L6.SCHREGNO  ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT REGD ON L6.YEAR = REGD.YEAR  ";
        $query .= "           AND L6.GRADE = REGD.GRADE  ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT L7 ON L6.YEAR = L7.YEAR  ";
        $query .= "           AND L6.SEMESTER = L7.SEMESTER  ";
        $query .= "           AND L6.GRADE = L7.GRADE  ";
        $query .= "           AND L6.HR_CLASS = L7.HR_CLASS  ";
        $query .= "      LEFT JOIN FINSCHOOL_MST L8 ON L2.FINSCHOOLCD = L8.FINSCHOOLCD  ";
        $query .= "  WHERE  ";
        $query .= "      T1.CLUBCD IN ".$club_select." ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T1.DETAIL_SCHOOL_KIND = REGD.SCHOOL_KIND ";
            $query .= " AND CDET001.REMARK1 LIKE CONCAT(CONCAT('%', T1.DETAIL_SCHOOL_KIND), '%') ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND T1.DETAIL_DATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR+1)."-03-31' ";
        $query .= "  ORDER BY  ";
        $query .= "      T1.CLUBCD,  ";
        if ($sort_select) {
            $query .= "      ".$sort_select." ";
        } else {
            $query .= "      T1.HOSTCD,  ";
            $query .= "      T1.DETAIL_DATE,  ";
            $query .= "      T1.ITEMCD,  ";
            $query .= "      T1.KINDCD,  ";
            $query .= "      T1.RECORDCD,  ";
            $query .= "      L5.RECORDNAME,  ";
            $query .= "      L6.GRADE DESC,  ";
            $query .= "      L6.HR_CLASS,  ";
            $query .= "      L6.ATTENDNO  ";
        }

        return $query;
    }

}
?>
