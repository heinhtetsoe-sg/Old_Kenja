<?php
require_once('for_php7.php');

class knje380aQuery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID       = 'KNJE380A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学科一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     YDAT.MAJORCD, ";
        $query .= "     MST.MAJORNAME ";
        $query .= " FROM ";
        $query .= "             MAJOR_YDAT YDAT ";
        $query .= " INNER JOIN  MAJOR_MST MST    ON YDAT.MAJORCD = MST.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     YDAT.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $SENKOU_SCHOOL = "0";  //先行区分 : 進学
        $SENKOU_WORK   = "1";  //先行区分 : 就職
        $SHIMANE_CD    = 32;   //「32:島根県」
        $INSIDE        = "県内";
        $OUTSIDE       = "県外";
        $GRADE_UP      = "進学";
        $WORK          = "就職";
        $UNDECIDED     = "未定";

        $query  = " WITH TMP AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(YEAR) AS LAST_YEAR ";
        $query .= " FROM ";
        $query .= "     GRD_REGD_DAT ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        //No.
        $query .= "     SCH_MAIN.SCHREGNO, ";
        //出身
        $query .= "     CASE FINSCHOOL.FINSCHOOL_PREF_CD ";
        $query .= "          WHEN ".$SHIMANE_CD." THEN '".$INSIDE."' ";
        $query .= "          ELSE '".$OUTSIDE."' ";
        $query .= "     END FINSCHOOLCD, ";
        //性別
        $query .= "     NAME1.NAME2 SEX, ";
        //進路状況
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "               CASE ";
        $query .= "                   WHEN AGCD_BASE.SENKOU_KIND = ".$SENKOU_SCHOOL." THEN '".$GRADE_UP."'";
        $query .= "                   WHEN AGCD_BASE.SENKOU_KIND = ".$SENKOU_WORK."   THEN '".$WORK."' ";
        $query .= "               END ";
        $query .= "          ELSE '".$UNDECIDED."' ";
        $query .= "     END SENKOU_KIND, ";
        //県内外別
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "              CASE ";
        $query .= "                  WHEN AGCD_BASE.SENKOU_KIND = '".$SENKOU_SCHOOL."' THEN "; //進学
        $query .= "                           CASE COLLEGE.AREA_NAME ";
        $query .= "                               WHEN '島根県' THEN '".$INSIDE."' ";
        $query .= "                               ELSE '".$OUTSIDE."' ";
        $query .= "                           END ";
        $query .= "                  WHEN AGCD_BASE.SENKOU_KIND = '".$SENKOU_WORK."' THEN "; //就職
        $query .= "                           CASE AGCD_BASE.PREF_CD ";
        $query .= "                               WHEN ".$SHIMANE_CD." THEN '".$INSIDE."' ";
        $query .= "                               ELSE '".$OUTSIDE."' ";
        $query .= "                           END ";
        $query .= "              END ";
        $query .= "         ELSE '".$UNDECIDED."' ";
        $query .= "     END STAT_PREF_CD, ";
        //進学先・就職先　名称
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "          CASE AGCD_BASE.SENKOU_KIND ";
        $query .= "               WHEN '".$SENKOU_SCHOOL."' THEN COLLEGE.SCHOOL_NAME ";  //学校名
        $query .= "               WHEN '".$SENKOU_WORK."' THEN COMPANY.COMPANY_NAME ";   //会社名
        $query .= "          END ";
        $query .= "     END STAT_CD, ";
        //学部
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "          CASE AGCD_BASE.SENKOU_KIND ";
        $query .= "               WHEN '".$SENKOU_SCHOOL."' THEN FACULTY.FACULTYNAME ";
        $query .= "          END  ";
        $query .= "     END FACULTYCD, ";
        //学科・コース・専攻
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "          CASE AGCD_BASE.SENKOU_KIND ";
        $query .= "               WHEN '".$SENKOU_SCHOOL."' THEN DEPARTMENT.DEPARTMENTNAME ";
        $query .= "               END ";
        $query .= "     END DEPARTMENTCD, ";
        //進学先種別
        $query .= "     CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "          CASE AGCD_BASE.SENKOU_KIND ";
        $query .= "               WHEN '".$SENKOU_SCHOOL."' THEN NAME2.NAME1 ";
        $query .= "               END ";
        $query .= "     END SCHOOL_SORT, ";
        //求人票No.（業種）
        $query .= "    CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "         CASE AGCD_BASE.SENKOU_KIND ";
        $query .= "              WHEN '".$SENKOU_WORK."' THEN OFFER.INDUSTRY_SCD ";
        $query .= "         END ";
        $query .= "     END SENKOU_NO, ";
        //業種
        $query .= "     '' STAT, ";    //列のみ出力
        //求人票No.（職種）
        $query .= "    CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "         CASE ";
        $query .= "             WHEN AGCD_BASE.SENKOU_KIND = '".$SENKOU_WORK."' ";
        $query .= "              AND AGCD_BASE.SENKOU_NO IS NOT NULL THEN AGCD_BASE.JOBTYPE_SCD || '-' || AGCD_BASE.JOBTYPE_SSCD ";
        $query .= "         END ";
        $query .= "    END JOBTYPE_SCD, ";
        //職種
        $query .= "    '' PREF, ";    //列のみ出力
        //求人票に依らない場合の業種
        $query .= "    CASE WHEN AGCD_BASE.PLANSTAT = '1' THEN ";  //進学先が「決定」
        $query .= "         CASE ";
        $query .= "             WHEN AGCD_BASE.SENKOU_KIND = '".$SENKOU_WORK."' ";
        $query .= "              AND AGCD_BASE.SENKOU_NO IS NULL ";
        $query .= "                  THEN INDUSTRY.INDUSTRY_LCD || '：' || INDUSTRY.INDUSTRY_LNAME ";
        $query .= "         END ";
        $query .= "    END INDUSTRY, ";
        //求人票に依らない場合の職種
        $query .= "     CASE ";
        $query .= "         WHEN AGCD_BASE.SENKOU_KIND = '".$SENKOU_WORK."' ";
        $query .= "          AND AGCD_BASE.SENKOU_NO IS NULL ";
        $query .= "              THEN JOBTYPE.JOBTYPE_LCD || '：' || JOBTYPE.JOBTYPE_LNAME ";
        $query .= "     END JOBTYPE, ";
        //志望した理由第一
        $query .= "     NAME3.NAME1 AS REASON1, ";
        //志望した理由第二
        $query .= "     NAME4.NAME1 AS REASON2, ";
        //夢実現進学チャレンジセミナー
        $query .= "     CASE AGCDD.REMARK1 ";
        $query .= "         WHEN '1' THEN '〇' ";
        $query .= "         WHEN '2' THEN '‐' ";
        $query .= "     END CHALLENGE_SEMI1, ";
        //学びの力向上セミナー
        $query .= "     CASE AGCDD.REMARK2 ";
        $query .= "         WHEN '1' THEN '〇' ";
        $query .= "         WHEN '2' THEN '‐' ";
        $query .= "     END CHALLENGE_SEMI2 ";
        $query .= " FROM ";
        $query .= "     (";
        $query .= "         SELECT SBM.SCHREGNO ";
        $query .= "              , SRD.YEAR ";
        $query .= "              , SRD.MAJORCD ";
        $query .= "           FROM SCHREG_BASE_MST AS SBM ";
        $query .= "           JOIN SCHREG_REGD_DAT AS SRD ";
        $query .= "             ON ( ";
        $query .= "                     SRD.SCHREGNO   = SBM.SCHREGNO ";
        $query .= "                 AND SRD.YEAR       = '".CTRL_YEAR."'";     //ログイン年度
        $query .= "                 AND SRD.SEMESTER   = '".CTRL_SEMESTER."'"; //学期
        $query .= "                ) ";
        $query .= "           JOIN SCHOOL_MST AS SM ";
        $query .= "             ON ( ";
        $query .= "                     SM.SCHOOLCD    = '000000000000' ";
        $query .= "                 AND SM.YEAR        = '".CTRL_YEAR."'";      //ログイン年度
        $query .= "                 AND SM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "                ) ";
        $query .= "           JOIN SCHREG_REGD_GDAT AS G ";
        $query .= "             ON ( ";
        $query .= "                     G.YEAR        = '".CTRL_YEAR."'";       //ログイン年度
        $query .= "                 AND G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "                 AND G.GRADE_CD    = SM.GRADE_HVAL ";
        $query .= "                 AND G.GRADE       = SRD.GRADE ";
        $query .= "                ) ";
        $query .= "          WHERE ";
        $query .= "                SBM.GRD_DIV = '1' ";    //「異動/卒業」が1:卒業
        $query .= "             OR SBM.GRD_DIV IS NULL ";  //「異動/卒業」が未設定

        if ($model->field["GRD"] == "on") {
            //既卒生含む
            $query .= "          UNION ";
            $query .= "         SELECT GBM.SCHREGNO ";
            $query .= "              , AGCD.YEAR ";
            $query .= "              , GRD.MAJORCD ";
            $query .= "           FROM GRD_BASE_MST AS GBM ";
            $query .= "           JOIN AFT_GRAD_COURSE_SS_DAT AS AGCD ";
            $query .= "             ON ( ";
            $query .= "                     AGCD.YEAR     = '".CTRL_YEAR."'";    //ログイン年度
            $query .= "                 AND AGCD.PLANSTAT = '1'";                //進路決定
            $query .= "                 AND AGCD.SCHREGNO = GBM.SCHREGNO ";
            $query .= "                ) ";
            $query .= "           LEFT JOIN GRD_REGD_DAT GRD ";
            $query .= "             ON GBM.SCHREGNO       = GRD.SCHREGNO ";
            $query .= "           JOIN TMP ";
            $query .= "             ON ( ";
            $query .= "                      GRD.SCHREGNO = TMP.SCHREGNO ";
            $query .= "                 AND  GRD.YEAR     = TMP.LAST_YEAR ";
            $query .= "                ) ";
        }
        $query .= "     ) SCH_MAIN";
        $query .= "     LEFT JOIN ( ";
        $query .= "             SELECT YEAR ";
        $query .= "                  , SCHREGNO ";
        $query .= "                  , MAX(UPDATED) AS MAX_UPDATED ";
        $query .= "               FROM AFT_GRAD_COURSE_SS_DAT ";
        $query .= "              WHERE SENKOU_KIND IN ('0', '1') ";    //「0:進学」「1:就職」
        $query .= "           GROUP BY YEAR ";
        $query .= "                  , SCHREGNO ";
        $query .= "          ) AS AGCD_MAX ";
        $query .= "       ON ( ";
        $query .= "               AGCD_MAX.YEAR              = SCH_MAIN.YEAR ";
        $query .= "           AND AGCD_MAX.SCHREGNO          = SCH_MAIN.SCHREGNO ";
        $query .= "          ) ";
        $query .= "     LEFT JOIN      AFT_GRAD_COURSE_SS_DAT AGCD_BASE ";
        $query .= "            ON SCH_MAIN.SCHREGNO          = AGCD_BASE.SCHREGNO ";
        $query .= "           AND AGCD_BASE.UPDATED          = AGCD_MAX.MAX_UPDATED";
        $query .= "           AND AGCD_BASE.YEAR             = AGCD_MAX.YEAR";
        $query .= "     LEFT JOIN      SCHREG_BASE_MST BASE ";
        $query .= "            ON SCH_MAIN.SCHREGNO          = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ";
        $query .= "            ON BASE.FINSCHOOLCD           = FINSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST NAME1 ";
        $query .= "            ON BASE.SEX                   = NAME1.NAMECD2 ";
        $query .= "           AND NAME1.NAMECD1              = 'Z002' ";
        $query .= "     LEFT JOIN COLLEGE_MST COLLEGE ";
        $query .= "            ON AGCD_BASE.STAT_CD          = COLLEGE.SCHOOL_CD ";
        $query .= "     LEFT JOIN COMPANY_MST COMPANY ";
        $query .= "            ON AGCD_BASE.STAT_CD          = COMPANY.COMPANY_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST FACULTY ";
        $query .= "            ON AGCD_BASE.STAT_CD          = FACULTY.SCHOOL_CD ";
        $query .= "           AND AGCD_BASE.FACULTYCD        = FACULTY.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST DEPARTMENT ";
        $query .= "            ON AGCD_BASE.STAT_CD          = DEPARTMENT.SCHOOL_CD ";
        $query .= "           AND AGCD_BASE.FACULTYCD        = DEPARTMENT.FACULTYCD ";
        $query .= "           AND AGCD_BASE.DEPARTMENTCD     = DEPARTMENT.DEPARTMENTCD ";
        $query .= "     LEFT JOIN NAME_MST NAME2 ";
        $query .= "            ON COLLEGE.SCHOOL_SORT        = NAME2.NAMECD2 ";
        $query .= "           AND NAME2.NAMECD1              = 'E001' ";
        $query .= "     LEFT JOIN JOB_OFFER_SS_DAT OFFER ";
        $query .= "            ON AGCD_BASE.SENKOU_NO        = OFFER.SENKOU_NO ";
        $query .= "           AND AGCD_BASE.YEAR             = OFFER.YEAR ";
        $query .= "     LEFT JOIN INDUSTRY_L_MST INDUSTRY ";
        $query .= "            ON COMPANY.INDUSTRY_LCD = INDUSTRY.INDUSTRY_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST JOBTYPE ";
        $query .= "            ON AGCD_BASE.JOBTYPE_LCD      = JOBTYPE.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AGCDD ";
        $query .= "            ON AGCDD.YEAR                 = AGCD_BASE.YEAR ";
        $query .= "           AND AGCDD.SEQ                  = AGCD_BASE.SEQ ";
        $query .= "           AND AGCDD.DETAIL_SEQ           = 7 ";
        $query .= "     LEFT JOIN NAME_MST NAME3 ";
        $query .= "            ON AGCDD.REMARK3              = NAME3.NAMECD2 ";
        $query .= "           AND NAME3.NAMECD1              = 'E074' ";
        $query .= "     LEFT JOIN NAME_MST NAME4 ";
        $query .= "            ON AGCDD.REMARK4              = NAME4.NAMECD2 ";
        $query .= "           AND NAME4.NAMECD1              = 'E074' ";
        $query .= " WHERE ";
        $query .= "       SCH_MAIN.YEAR                      = '".CTRL_YEAR."' ";
        $query .= "   AND SCH_MAIN.MAJORCD                       = '".$model->field["MAJORCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SCH_MAIN.SCHREGNO ";

        return $query;
    }
}
