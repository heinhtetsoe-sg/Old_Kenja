<?php

require_once('for_php7.php');

class knje066Query extends Query {

    //生徒情報取得
    function getStudentInfoData($model)
    {
        $query  =" SELECT ";
        $query .="     MAIN.SCHREGNO, ";
        $query .="     SC_R.GRADE, ";
        $query .="     SC_G.SCHOOL_KIND, ";
        $query .="     SC_R.GRADE AS CTRL_GRADE, ";
        $query .="     C2.COURSENAME AS COURSE, ";
        $query .="     C2.COURSENAME || M2.MAJORNAME AS MAJOR, ";
        $query .="     CC2.COURSECODENAME, ";
        $query .="     SM.STAFFNAME AS STAFFNAME, ";
        $query .="     SC_R.ANNUAL AS ANNUAL, ";
        $query .="     MAIN.NAME AS NAME, ";
        $query .="     SC_H.CURRICULUM_YEAR AS CURRICULUM_YEAR, ";
        $query .="     MAIN.ENT_DATE, ";
        $query .="     CASE WHEN MONTH(MAIN.ENT_DATE) < 4 ";
        $query .="          THEN YEAR(MAIN.ENT_DATE) - 1 ";
        $query .="          ELSE YEAR(MAIN.ENT_DATE) ";
        $query .="     END AS ENT_YEAR, ";
        $query .="     MAIN.GRD_DATE, ";
        $query .="     ADDR.ADDR1, ";
        $query .="     ADDR.ADDR2, ";
        $query .="     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
        $query .="     MAIN.SATEI_TANNI, ";
        $query .="     MAIN.JIKOUGAI_NYUURYOKU, ";
        $query .="     TOKKATU.NAME1 AS TOKKATU_JISU, ";
        $query .="     SCHOOLING.NAME1 AS SCHOOLING_DIV, ";
        $query .="     MAIN.MUSYOU_KAISU, ";
        $query .="     AREA.NAME1 AS AREA, ";
        $query .="     JOB.NAME1 AS JOB, ";
        $query .="     SC_R.COURSECD, ";
        $query .="     SC_R.MAJORCD, ";
        $query .="     SC_R.COURSECODE ";
        $query .=" FROM ";
        $query .="     V_SCHREG_BASE_MST MAIN ";
        $query .="     LEFT JOIN SCHREG_REGD_DAT SC_R ON SC_R.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_R.YEAR = '".CTRL_YEAR."' ";
        $query .="          AND SC_R.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     LEFT JOIN SCHREG_REGD_DAT SC_CTRL ON SC_CTRL.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_CTRL.YEAR = '".CTRL_YEAR."' ";
        $query .="          AND SC_CTRL.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     LEFT JOIN SCHREG_REGD_GDAT SC_G ON SC_R.YEAR = SC_G.YEAR ";
        $query .="          AND SC_R.GRADE = SC_G.GRADE ";
        $query .="     LEFT JOIN COURSE_MST C2 ON C2.COURSECD = SC_R.COURSECD ";
        $query .="     LEFT JOIN MAJOR_MST M2 ON M2.COURSECD = SC_R.COURSECD ";
        $query .="          AND M2.MAJORCD = SC_R.MAJORCD ";
        $query .="     LEFT JOIN COURSECODE_MST CC2 ON CC2.COURSECODE = SC_R.COURSECODE ";
        $query .="     LEFT JOIN SCHREG_REGD_HDAT SH ON SH.YEAR = SC_R.YEAR ";
        $query .="          AND SH.SEMESTER = SC_R.SEMESTER ";
        $query .="          AND SH.GRADE = SC_R.GRADE ";
        $query .="          AND SH.HR_CLASS = SC_R.HR_CLASS ";
        $query .="     LEFT JOIN STAFF_MST SM ON SM.STAFFCD = SH.TR_CD1 ";
        $query .="     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT SC_H ON MAIN.SCHREGNO = SC_H.SCHREGNO ";
        $query .="          AND SC_H.SCHOOL_KIND = SC_G.SCHOOL_KIND ";
        $query .="     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_YD.YEAR = '".CTRL_YEAR."' ";
        $query .="          AND SC_YD.BASE_SEQ = '001' ";
        $query .="     LEFT JOIN SCHREG_SEND_ADDRESS_DAT SCH_SEND ON SCH_SEND.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SCH_SEND.DIV = '1' ";
        $query .= "    LEFT JOIN NAME_MST AREA ON AREA.NAMECD1 = 'A020' ";
        $query .= "         AND SCH_SEND.SEND_AREACD = AREA.NAMECD2 ";
        $query .= "    LEFT JOIN NAME_MST JOB ON JOB.NAMECD1 = 'H202' ";
        $query .= "         AND SCH_SEND.SEND_JOBCD = JOB.NAMECD2 ";
        $query .= "    LEFT JOIN ( ";
        $query .= "              SELECT ";
        $query .= "                  T1.* ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_ADDRESS_DAT T1, ";
        $query .= "                  (SELECT ";
        $query .= "                       SCHREGNO, ";
        $query .= "                       MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                   FROM ";
        $query .= "                       SCHREG_ADDRESS_DAT ";
        $query .= "                   GROUP BY ";
        $query .= "                       SCHREGNO ";
        $query .= "                  ) T2 ";
        $query .= "              WHERE ";
        $query .= "                  T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                  AND T1.ISSUEDATE = T2.ISSUEDATE) ADDR ";
        $query .= "         ON MAIN.SCHREGNO = ADDR.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST Y_DETAIL ON MAIN.SCHREGNO = Y_DETAIL.SCHREGNO ";
        $query .= "         AND Y_DETAIL.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND Y_DETAIL.BASE_SEQ = '002' ";
        $query .= "    LEFT JOIN NAME_MST TOKKATU ON TOKKATU.NAMECD1 = 'M013' ";
        $query .= "         AND MAIN.TOKKATU_JISU = TOKKATU.NAMECD2 ";
        $query .= "    LEFT JOIN NAME_MST SCHOOLING ON SCHOOLING.NAMECD1 = 'M014' ";
        $query .= "         AND Y_DETAIL.BASE_REMARK1 = SCHOOLING.NAMECD2 ";
        $query .=" WHERE ";
        $query .="     MAIN.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //修得方法
    function getSyutoku()
    {
        $query  = " WITH SYUTOKU(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('前籍校', '1') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SYUTOKU ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //請求データ
    function getStudentClaimData($model)
    {
        $query  =" SELECT ";
        $query .="     SUM(VALUE(TOTAL_CLAIM_MONEY, 0)) AS TOTAL_CLAIM_MONEY ";
        $query .=" FROM ";
        $query .="     CLAIM_DAT ";
        $query .=" WHERE ";
        $query .="     APPLICANTNO = '".$model->applicantno."' ";

        return $query;
    }

    //前籍校
    function getFinSchoolcd($model)
    {
        //前籍校データ
        $query  = " SELECT ";
        $query .= "     T1.FORMER_REG_SCHOOLCD || '：' || T2.FINSCHOOL_NAME AS LABEL, ";
        $query .= "     T1.FORMER_REG_SCHOOLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     ON T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.REGD_S_DATE DESC ";
        $query .= "     ,T1.REGD_E_DATE DESC ";

        return $query;
    }

    //処理年度
    function getExeYear()
    {
        $seq = "";
        for ($i = 6; $i >= 0; $i--) {
            $values .= $seq."(".(CTRL_YEAR - $i).",".(CTRL_YEAR - $i).")";
            $seq = ",";
        }

        $query  = " WITH EXE_YEAR (LABEL, VALUE) AS ( ";
        $query .="     VALUES ".$values." ";
        $query .="     ) ";
        $query .=" SELECT * FROM EXE_YEAR ";

        return $query;
    }

    //教科
    function getClass_mst($model, $div = "", $another = "")
    {
        $query  = " SELECT DISTINCT ";
        if ($div == "ONE") {
            $query .= "     * ";
        } else {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || T1.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        if ($another) {
            $query .= "     ANOTHER_CLASS_MST T1 ";
        } else {
            $query .= "     CLASS_MST T1 ";
        }
        if ($div == "ONE") {
            $query .= " WHERE ";
            if ($another) {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->select_class."' ";
            } else {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->select_rep_class."' ";
            }
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
            }
        } else {
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND = '".$model->schoolKind."' ";
            }
        }
        $query .= " ORDER BY ";
        if ($div == "ALL") {
            $query .= "     VALUE ";
        } else {
            $query .= "     T1.CLASSCD ";
        }

        return $query;
    }

    //教育課程
    function getNameMst($cd, $DIV = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$cd."' ";
        if ($DIV == "2") {
            $query .= "    AND NAMECD2 = '0' ";
        }

        return $query;
    }

    //教育課程初期値
    function getNameDefault($cd, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "") {
            $query .= "     NAMECD2 ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$cd."' ";
        if ($flg == "") {
            $query .= " AND NAMESPARE1 <= '".CTRL_YEAR."' ";
            $query .= " AND NAMESPARE2 >= '".CTRL_YEAR."' ";
        }
        
        return $query;
    }

    //科目コンボ
    function getSubclass_mst($model, $classcd, $curriculum_cd, $another = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || VALUE(T1.SUBCLASSNAME, '') AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        if ($another) {
            $query .= "     ANOTHER_SUBCLASS_MST T1 ";
        } else {
            $query .= "     SUBCLASS_MST T1 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$classcd."' ";
        $query .= "     AND T1.CURRICULUM_CD = '".$curriculum_cd."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育課程
    function getNameValue($cd1, $cd2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '".$cd1."' ";
        $query .= "     AND NAMECD2 = '".$cd2."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //科目値
    function getSubclassValue($model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSCD || ':' || VALUE(SUBCLASSNAME, '') AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         CURRICULUM_CD = '".$model->select_curriculum_cd."' ";
        $query .= "     AND CLASSCD       = '".$model->select_class."' ";
        $query .= "     AND SUBCLASSCD    = '".$model->select_subclass."' ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //明細データ
    function getAnother($db, $model, $div = "")
    {

        $query  =" SELECT ";
        $query .="     STUDYREC.SCHOOLCD, ";
        $query .="     STUDYREC.YEAR, ";
        $query .="     STUDYREC.SCHREGNO, ";
        $query .="     STUDYREC.ANNUAL, ";
        $query .="     STUDYREC.CLASSCD, ";
        $query .="     STUDYREC.SCHOOL_KIND, ";
        $query .="     STUDYREC.CURRICULUM_CD, ";
        $query .="     STUDYREC.SUBCLASSCD, ";
        $query .="     STUDYREC_REPLACE.CLASSCD_SAKI AS REP_CLASSCD, ";
        $query .="     STUDYREC_REPLACE.SCHOOL_KIND_SAKI AS REP_SCHOOL_KIND, ";
        $query .="     STUDYREC_REPLACE.CURRICULUM_CD_SAKI AS REP_CURRICULUM_CD, ";
        $query .="     NM2.NAME1 AS REP_CURRICULUM_CD_NAME, ";
        $query .="     STUDYREC_REPLACE.SUBCLASSCD_SAKI AS REP_SUBCLASSCD, ";
        $query .="     STUDYREC_REPLACE.FORMER_REG_SCHOOLCD, ";
        $query .="     STUDYREC.CLASSABBV, ";
        $query .="     STUDYREC.CLASSNAME_ENG, ";
        $query .="     STUDYREC.CLASSABBV_ENG, ";
        $query .="     STUDYREC.SUBCLASSES, ";
        $query .="     STUDYREC.SUBCLASSNAME_ENG, ";
        $query .="     STUDYREC.SUBCLASSABBV_ENG, ";
        $query .="     STUDYREC.VALUATION, ";
        $query .="     STUDYREC.GET_CREDIT, ";
        $query .="     STUDYREC.ADD_CREDIT, ";
        $query .="     STUDYREC.COMP_CREDIT, ";
        $query .="     STUDYREC.PRINT_FLG, ";

        $query .="     NM.NAME1 AS CURRICULUM_CD_NAME, ";
        $query .="     FSCHOOL.FINSCHOOL_NAME AS FORMER_REG_SCHOOLNAME, ";
        $query .="     STUDYREC_REMARK.REMARK, ";
        $query .="     STUDYREC_REPLACE.VALUATION AS REP_VALUATION, ";
        $query .="     STUDYREC_REPLACE.GET_CREDIT AS REP_GET_CREDIT, ";
        $query .="     STUDYREC_REPLACE.ADD_CREDIT AS REP_ADD_CREDIT, ";
        $query .="     STUDYREC_REPLACE.COMP_CREDIT AS REP_COMP_CREDIT, ";
        $query .="     STUDYREC_REPLACE.GET_DATE, ";
        $query .="     CASE WHEN STUDYREC.CLASSNAME IS NULL ";
        $query .="          THEN CASE WHEN AC_MST.CLASSNAME IS NULL ";
        $query .="                    THEN C_MST.CLASSNAME ";
        $query .="                    ELSE AC_MST.CLASSNAME ";
        $query .="               END ";
        $query .="          ELSE STUDYREC.CLASSNAME ";
        $query .="     END AS CLASSNAME, ";
        $query .="     CASE WHEN STUDYREC.SUBCLASSNAME IS NULL ";
        $query .="          THEN CASE WHEN ASC_MST.SUBCLASSNAME IS NULL ";
        $query .="                    THEN SC_MST.SUBCLASSNAME ";
        $query .="                    ELSE ASC_MST.SUBCLASSNAME ";
        $query .="               END ";
        $query .="          ELSE STUDYREC.SUBCLASSNAME ";
        $query .="     END AS SUBCLASSNAME, ";
        $query .="     CASE WHEN STUDYREC.SUBCLASSNAME IS NULL ";
        $query .="          THEN '1' ";
        $query .="          ELSE '2' ";
        $query .="     END AS MODE_DIV, ";
        $query .="     CASE WHEN STUDYREC.SUBCLASSABBV IS NOT NULL ";
        $query .="          THEN STUDYREC.SUBCLASSABBV ";
        $query .="     END AS SUBCLASSABBV, ";
        $query .="     CASE WHEN SCA_MST1.SUBCLASSNAME IS NOT NULL ";
        $query .="          THEN SCA_MST1.SUBCLASSNAME ";
        $query .="          ELSE SCA_MST2.SUBCLASSNAME ";
        $query .="     END AS REP_SUBCLASSNAME, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .="     FLG.PROV_FLG, ";
        }
        $query .="     CASE WHEN CA_MST1.CLASSNAME IS NOT NULL ";
        $query .="          THEN CA_MST1.CLASSNAME ";
        $query .="          ELSE CA_MST2.CLASSNAME ";
        $query .="     END AS REP_CLASSNAME ";
        $query .=" FROM ";
        $query .="     SCHREG_STUDYREC_DAT STUDYREC ";
        $query .="     LEFT JOIN STUDYRECREMARK_DAT STUDYREC_REMARK ON STUDYREC.YEAR = STUDYREC_REMARK.YEAR ";
        $query .="          AND STUDYREC.SCHREGNO = STUDYREC_REMARK.SCHREGNO ";
        $query .="          AND STUDYREC.CLASSCD = STUDYREC_REMARK.CLASSCD ";
        $query .="          AND STUDYREC.SCHOOL_KIND = STUDYREC_REMARK.SCHOOL_KIND ";
        $query .="          AND STUDYREC.CURRICULUM_CD = STUDYREC_REMARK.CURRICULUM_CD ";
        $query .="          AND STUDYREC.SUBCLASSCD = STUDYREC_REMARK.SUBCLASSCD ";
        $query .="     LEFT JOIN SCHREG_STUDYREC_REPLACE_DAT STUDYREC_REPLACE ON STUDYREC.SCHOOLCD = STUDYREC_REPLACE.SCHOOLCD ";
        $query .="          AND STUDYREC.YEAR = STUDYREC_REPLACE.YEAR ";
        $query .="          AND STUDYREC.SCHREGNO = STUDYREC_REPLACE.SCHREGNO ";
        $query .="          AND STUDYREC.ANNUAL = STUDYREC_REPLACE.ANNUAL ";
        $query .="          AND STUDYREC.CLASSCD = STUDYREC_REPLACE.CLASSCD ";
        $query .="          AND STUDYREC.SCHOOL_KIND = STUDYREC_REPLACE.SCHOOL_KIND ";
        $query .="          AND STUDYREC.CURRICULUM_CD = STUDYREC_REPLACE.CURRICULUM_CD ";
        $query .="          AND STUDYREC.SUBCLASSCD = STUDYREC_REPLACE.SUBCLASSCD ";
        $query .="     LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'Z018' ";
        $query .="          AND STUDYREC.CURRICULUM_CD = NM.NAMECD2 ";
        $query .="     LEFT JOIN NAME_MST NM2 ON NM2.NAMECD1 = 'Z018' ";
        $query .="          AND STUDYREC_REPLACE.CURRICULUM_CD_SAKI = NM2.NAMECD2 ";
        $query .="     LEFT JOIN FINSCHOOL_MST FSCHOOL ON FSCHOOL.FINSCHOOLCD = STUDYREC_REPLACE.FORMER_REG_SCHOOLCD ";
        $query .="     LEFT JOIN CLASS_MST C_MST ON C_MST.CLASSCD = STUDYREC.CLASSCD ";
        $query .="          AND C_MST.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ";
        $query .="     LEFT JOIN ANOTHER_CLASS_MST AC_MST ON AC_MST.CLASSCD = STUDYREC.CLASSCD ";
        $query .="          AND AC_MST.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ";
        $query .="     LEFT JOIN SUBCLASS_MST SC_MST ";
        $query .="          ON  SC_MST.CLASSCD       = STUDYREC.CLASSCD ";
        $query .="          AND SC_MST.SCHOOL_KIND   = STUDYREC.SCHOOL_KIND ";
        $query .="          AND SC_MST.CURRICULUM_CD = STUDYREC.CURRICULUM_CD ";
        $query .="          AND SC_MST.SUBCLASSCD    = STUDYREC.SUBCLASSCD ";
        $query .="     LEFT JOIN ANOTHER_SUBCLASS_MST ASC_MST ";
        $query .="          ON  ASC_MST.CLASSCD       = STUDYREC.CLASSCD ";
        $query .="          AND ASC_MST.SCHOOL_KIND   = STUDYREC.SCHOOL_KIND ";
        $query .="          AND ASC_MST.CURRICULUM_CD = STUDYREC.CURRICULUM_CD ";
        $query .="          AND ASC_MST.SUBCLASSCD    = STUDYREC.SUBCLASSCD ";
        $query .="     LEFT JOIN CLASS_MST CA_MST1 ON CA_MST1.CLASSCD = STUDYREC_REPLACE.CLASSCD_SAKI ";
        $query .="          AND CA_MST1.SCHOOL_KIND = STUDYREC_REPLACE.SCHOOL_KIND ";
        $query .="     LEFT JOIN SUBCLASS_MST SCA_MST1 ";
        $query .="          ON  SCA_MST1.CLASSCD       = STUDYREC_REPLACE.CLASSCD_SAKI ";
        $query .="          AND SCA_MST1.SCHOOL_KIND   = STUDYREC_REPLACE.SCHOOL_KIND_SAKI ";
        $query .="          AND SCA_MST1.CURRICULUM_CD = STUDYREC_REPLACE.CURRICULUM_CD_SAKI ";
        $query .="          AND SCA_MST1.SUBCLASSCD    = STUDYREC_REPLACE.SUBCLASSCD_SAKI ";
        $query .="     LEFT JOIN ANOTHER_CLASS_MST CA_MST2 ON CA_MST2.CLASSCD = STUDYREC_REPLACE.CLASSCD_SAKI ";
        $query .="          AND CA_MST2.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ";
        $query .="     LEFT JOIN ANOTHER_SUBCLASS_MST SCA_MST2 ";
        $query .="          ON  SCA_MST2.CLASSCD       = STUDYREC_REPLACE.CLASSCD_SAKI ";
        $query .="          AND SCA_MST2.SCHOOL_KIND   = STUDYREC_REPLACE.SCHOOL_KIND_SAKI ";
        $query .="          AND SCA_MST2.CURRICULUM_CD = STUDYREC_REPLACE.CURRICULUM_CD_SAKI ";
        $query .="          AND SCA_MST2.SUBCLASSCD    = STUDYREC_REPLACE.SUBCLASSCD_SAKI ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .="     LEFT JOIN STUDYREC_PROV_FLG_DAT FLG ";
            $query .="           ON FLG.SCHOOLCD      = STUDYREC.SCHOOLCD ";
            $query .="          AND FLG.YEAR          = STUDYREC.YEAR ";
            $query .="          AND FLG.SCHREGNO      = STUDYREC.SCHREGNO ";
            $query .="          AND FLG.CLASSCD       = STUDYREC.CLASSCD ";
            $query .="          AND FLG.SCHOOL_KIND   = STUDYREC.SCHOOL_KIND ";
            $query .="          AND FLG.CURRICULUM_CD = STUDYREC.CURRICULUM_CD ";
            $query .="          AND FLG.SUBCLASSCD    = STUDYREC.SUBCLASSCD ";
        }
        $query .=" WHERE ";
        if ($div == "") {
            $query .="     STUDYREC.SCHREGNO = '".$model->schregno."' ";
        } else if ($model->cmd != "reset") {
            $query .="     STUDYREC.SCHREGNO          = '".$model->schregno."' ";
            $query .="     AND STUDYREC.YEAR          = '".$model->select_year."' ";
            $query .="     AND STUDYREC.CLASSCD || '-' || STUDYREC.SCHOOL_KIND || '-' || STUDYREC.CURRICULUM_CD || '-' || STUDYREC.SUBCLASSCD       = '".$model->select_subclass."' ";
        } else {
            $query .="     STUDYREC.SCHREGNO          = '".$model->schregno."' ";
            $query .="     AND STUDYREC.YEAR          = '".$model->up_year."' ";
            $query .="     AND STUDYREC.CLASSCD || '-' || STUDYREC.SCHOOL_KIND || '-' || STUDYREC.CURRICULUM_CD || '-' || STUDYREC.SUBCLASSCD       = '".$model->up_subclass."' ";
        }
        $query .=" ORDER BY ";
        $query .="     STUDYREC.YEAR, ";
        $query .="     STUDYREC.SCHOOLCD, ";
        $query .="     STUDYREC.SCHREGNO, ";
        $query .="     STUDYREC.ANNUAL, ";
        $query .="     STUDYREC.CLASSCD, ";
        $query .="     STUDYREC.SCHOOL_KIND, ";
        $query .="     STUDYREC.CURRICULUM_CD, ";
        $query .="     STUDYREC.SUBCLASSCD ";

        return $query;
    }

    //データチェック
    function getAnotherChk($model)
    {

        $subclassAll = $model->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $model->field["CLASSCD"]."-".$model->field["CURRICULUM_CD"]."-99".$model->field["SUBCLASSCD"];
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                = preg_split("/-/", $subclassAll);

        $query  =" SELECT ";
        $query .="     STUDYREC.* ";
        $query .=" FROM ";
        $query .="     SCHREG_STUDYREC_DAT STUDYREC ";
        $query .=" WHERE ";
        $query .="         STUDYREC.SCHOOLCD      = '".$model->field["SCHOOLCD"]."' ";
        $query .="     AND STUDYREC.YEAR          = '".$model->field["YEAR"]."' ";
        $query .="     AND STUDYREC.SCHREGNO      = '".$model->field["SCHREGNO"]."' ";
        $query .="     AND STUDYREC.ANNUAL        = '00' ";
        $query .="     AND STUDYREC.CLASSCD || '-' || STUDYREC.SCHOOL_KIND || '-' || STUDYREC.CURRICULUM_CD || '-' || STUDYREC.SUBCLASSCD = '".$subclassAll."' ";

        return $query;
    }

    //データチェック
    function getSchregChk($field)
    {
        $query  =" SELECT ";
        $query .="     COUNT(*) AS CNT ";
        $query .=" FROM ";
        $query .="     SCHREG_BASE_MST ";
        $query .=" WHERE ";
        $query .="     SCHREGNO = '".$field["SCHREGNO"]."' ";

        return $query;
    }

    /**
     * 追加
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclassAll = $model->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $model->field["CLASSCD"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                = preg_split("/-/", $subclassAll);

        $data["SCHOOLCD"][TEXT]             = $model->field["SCHOOLCD"];
        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
        $data["ANNUAL"][TEXT]               = "00";
        $data["CLASSCD"][TEXT]              = $classcd;
        $data["SCHOOL_KIND"][TEXT]          = $school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $subclasscd;

        //入力用フィールド
        $data["SUBCLASSNAME"][TEXT]         = $model->field["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $model->field["SUBCLASSABBV"];

        $data["GET_CREDIT"][NUMBER]         = $model->field["GET_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]        = $model->field["COMP_CREDIT"];
        $data["VALUATION"][NUMBER]          = $model->field["VALUATION"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
        $db->query($query);

        $query  = "DELETE FROM ";
        $query .= "    STUDYRECREMARK_DAT ";
        $query .= "WHERE ";
        $query .= "           YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
        $data["CLASSCD"][TEXT]              = $classcd;
        $data["SCHOOL_KIND"][TEXT]          = $school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $subclasscd;

        //入力用フィールド
        $data["REMARK"][TEXT]               = $model->field["REMARK"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STUDYRECREMARK_DAT");
        $db->query($query);

        if ($model->field["MODE"] == "1") {
            if ($model->field["REP_SUBCLASSCD"] != "" && $model->field["REP_CURRICULUM_CD"] != "") {
                list($classcdSaki, $school_kindSaki, $curriculum_cdSaki, $subclasscdSaki)
                        = preg_split("/-/", $model->field["REP_SUBCLASSCD"]);
                $data = array();
                $data["SCHOOLCD"][TEXT]             = $model->field["SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
                $data["ANNUAL"][TEXT]               = "00";
                $data["CLASSCD"][TEXT]              = $classcd;
                $data["SCHOOL_KIND"][TEXT]          = $school_kind;
                $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]           = $subclasscd;
                $data["CLASSCD_SAKI"][TEXT]         = $classcdSaki;
                $data["SCHOOL_KIND_SAKI"][TEXT]     = $school_kindSaki;
                $data["CURRICULUM_CD_SAKI"][TEXT]   = $curriculum_cdSaki;
                $data["SUBCLASSCD_SAKI"][TEXT]      = $subclasscdSaki;
                $data["GET_CREDIT"][NUMBER]         = $model->field["REP_GET_CREDIT"];
                $data["COMP_CREDIT"][NUMBER]        = $model->field["REP_COMP_CREDIT"];
                $data["VALUATION"][NUMBER]          = $model->field["REP_VALUATION"];

                $data["FORMER_REG_SCHOOLCD"][TEXT]  = $model->field["FORMER_REG_SCHOOLCD"];
                $data["GET_DATE"][DATE]             = $model->field["GET_DATE"];

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_STUDYREC_REPLACE_DAT");
                $db->query($query);
            }
        } else {
            $data = array();
            $data["SCHOOLCD"][TEXT]             = $model->field["SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
            $data["ANNUAL"][TEXT]               = "00";
            $data["CLASSCD"][TEXT]              = $classcd;
            $data["SCHOOL_KIND"][TEXT]          = $school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;

            $data["CLASSCD_SAKI"][TEXT]         = "0";
            $data["SCHOOL_KIND_SAKI"][TEXT]     = "0";
            $data["CURRICULUM_CD_SAKI"][TEXT]   = "0";
            $data["SUBCLASSCD_SAKI"][TEXT]      = "0";

            $data["GET_CREDIT"][NUMBER]         = $model->field["REP_GET_CREDIT"];
            $data["COMP_CREDIT"][NUMBER]        = $model->field["REP_COMP_CREDIT"];
            $data["VALUATION"][NUMBER]          = $model->field["REP_VALUATION"];

            $data["FORMER_REG_SCHOOLCD"][TEXT]  = $model->field["FORMER_REG_SCHOOLCD"];
            $data["GET_DATE"][DATE]             = $model->field["GET_DATE"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_STUDYREC_REPLACE_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 更新
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclassAll = $model->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $model->field["CLASSCD"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                = preg_split("/-/", $subclassAll);

        //入力用フィールド
        $data["SUBCLASSNAME"][TEXT]         = $model->field["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $model->field["SUBCLASSABBV"];

        $data["GET_CREDIT"][NUMBER]         = $model->field["GET_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]        = $model->field["COMP_CREDIT"];
        $data["VALUATION"][NUMBER]          = $model->field["VALUATION"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "           SCHOOLCD      = '".$model->field["SCHOOLCD"]."'";
        $where .= "       AND YEAR          = '".$model->field["YEAR"]."' ";
        $where .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."' ";
        $where .= "       AND ANNUAL        = '00' ";
        $where .= "       AND CLASSCD       = '".$classcd."' ";
        $where .= "       AND SCHOOL_KIND   = '".$school_kind."' ";
        $where .= "       AND CURRICULUM_CD = '".$curriculum_cd."' ";
        $where .= "       AND SUBCLASSCD    = '".$subclasscd."' ";

        $query = Query::updateSQL($data, "SCHREG_STUDYREC_DAT", $where);
        $db->query($query);


        $query  = "DELETE FROM ";
        $query .= "    STUDYRECREMARK_DAT ";
        $query .= "WHERE ";
        $query .= "           YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";

        $db->query($query);

        //入力用フィールド
        $data = array();
        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
        $data["CLASSCD"][TEXT]              = $classcd;
        $data["SCHOOL_KIND"][TEXT]          = $school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $subclasscd;
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STUDYRECREMARK_DAT");
        $db->query($query);
        
        //SCHREG_STUDYREC_REPLACE_DATはDELETE INSERTを行なう
        //（査定科目が選択されている場合のみレコードを作成）
        $query  = "DELETE FROM ";
        $query .= "    SCHREG_STUDYREC_REPLACE_DAT ";
        $query .= "WHERE ";
        $query .= "           SCHOOLCD      = '".$model->field["SCHOOLCD"]."'";
        $query .= "       AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND ANNUAL        = '00' ";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";
        $db->query($query);
        if ($model->field["MODE"] == "1") {
            //査定の教科コード、教育課程年度、科目コードが選択されている場合のみレコードを作成
            if ($model->field["REP_SUBCLASSCD"] != "" && $model->field["REP_CURRICULUM_CD"] != "") {
                list($classcdSaki, $school_kindSaki, $curriculum_cdSaki, $subclasscdSaki)
                        = preg_split("/-/", $model->field["REP_SUBCLASSCD"]);
                $data = array();
                $data["SCHOOLCD"][TEXT]             = $model->field["SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
                $data["ANNUAL"][TEXT]               = "00";
                $data["CLASSCD"][TEXT]              = $classcd;
                $data["SCHOOL_KIND"][TEXT]          = $school_kind;
                $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]           = $subclasscd;
                
                $data["CLASSCD_SAKI"][TEXT]         = $classcdSaki;
                $data["SCHOOL_KIND_SAKI"][TEXT]     = $school_kindSaki;
                $data["CURRICULUM_CD_SAKI"][TEXT]   = $curriculum_cdSaki;
                $data["SUBCLASSCD_SAKI"][TEXT]      = $subclasscdSaki;
                
                $data["GET_CREDIT"][NUMBER]         = $model->field["REP_GET_CREDIT"];
                $data["COMP_CREDIT"][NUMBER]        = $model->field["REP_COMP_CREDIT"];
                $data["VALUATION"][NUMBER]          = $model->field["REP_VALUATION"];

                $data["FORMER_REG_SCHOOLCD"][TEXT]  = $model->field["FORMER_REG_SCHOOLCD"];
                $data["GET_DATE"][DATE]             = $model->field["GET_DATE"];

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_STUDYREC_REPLACE_DAT");
                $db->query($query);
            }
        } else {
            $data = array();
            $data["SCHOOLCD"][TEXT]             = $model->field["SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
            $data["ANNUAL"][TEXT]               = "00";
            $data["CLASSCD"][TEXT]              = $classcd;
            $data["SCHOOL_KIND"][TEXT]          = $school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;
            
            $data["CLASSCD_SAKI"][TEXT]         = "0";
            $data["SCHOOL_KIND_SAKI"][TEXT]     = "0";
            $data["CURRICULUM_CD_SAKI"][TEXT]   = "0";
            $data["SUBCLASSCD_SAKI"][TEXT]      = "0";
            
            $data["GET_CREDIT"][NUMBER]         = $model->field["REP_GET_CREDIT"];
            $data["COMP_CREDIT"][NUMBER]        = $model->field["REP_COMP_CREDIT"];
            $data["VALUATION"][NUMBER]          = $model->field["REP_VALUATION"];

            $data["FORMER_REG_SCHOOLCD"][TEXT]  = $model->field["FORMER_REG_SCHOOLCD"];
            $data["GET_DATE"][DATE]             = $model->field["GET_DATE"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_STUDYREC_REPLACE_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 削除
     */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclassAll = $model->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $model->field["CLASSCD"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
        }

        $query  = "DELETE FROM ";
        $query .= "    SCHREG_STUDYREC_DAT ";
        $query .= "WHERE ";
        $query .= "           SCHOOLCD      = '".$model->field["SCHOOLCD"]."'";
        $query .= "       AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND ANNUAL        = '00' ";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";

        $db->query($query);

        $query  = "DELETE FROM ";
        $query .= "    STUDYRECREMARK_DAT ";
        $query .= "WHERE ";
        $query .= "           YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";

        $db->query($query);

        $query  = "DELETE FROM ";
        $query .= "    SCHREG_STUDYREC_REPLACE_DAT ";
        $query .= "WHERE ";
        $query .= "           SCHOOLCD      = '".$model->field["SCHOOLCD"]."'";
        $query .= "       AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "       AND SCHREGNO      = '".$model->field["SCHREGNO"]."'";
        $query .= "       AND ANNUAL        = '00' ";
        $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassAll."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return $query;
    }

    /**
     * 指導要録入力完更新
     */
    function &getUpdateTopQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["EDUCATION_REC_PUT_FLG"][TEXT] = $model->field2["EDUCATION_REC_PUT_FLG"][0];
        $data["SCHREGNO"][TEXT]              = $model->field2["SCHREGNO"];

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE ";
        $where .= "       SCHREGNO = '".$model->field2["SCHREGNO"]."'";

        $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 削除
     */
    function &getDeleteTopQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->delchk as $val) {
            list($year, $schregno, $schoolcd, $subclasscd, $rep_subclassCd, $get_div)
                    = preg_split("/:/", $val);

            $query  = "DELETE FROM ";
            $query .= "    SCHREG_STUDYREC_DAT ";
            $query .= "WHERE ";
            $query .= "           SCHOOLCD      = '".$schoolcd."'";
            $query .= "       AND YEAR          = '".$year."'";
            $query .= "       AND SCHREGNO      = '".$schregno."'";
            $query .= "       AND ANNUAL        = '00' ";
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";

            $db->query($query);

            $query  = "DELETE FROM ";
            $query .= "    STUDYRECREMARK_DAT ";
            $query .= "WHERE ";
            $query .= "           YEAR          = '".$year."'";
            $query .= "       AND SCHREGNO      = '".$schregno."'";
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";

            $db->query($query);

            $query  = "DELETE FROM ";
            $query .= "    SCHREG_STUDYREC_REPLACE_DAT ";
            $query .= "WHERE ";
            $query .= "           SCHOOLCD      = '".$schoolcd."'";
            $query .= "       AND YEAR          = '".$year."'";
            $query .= "       AND SCHREGNO      = '".$schregno."'";
            $query .= "       AND ANNUAL        = '00' ";
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";

            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return true;
    }
}
?>
