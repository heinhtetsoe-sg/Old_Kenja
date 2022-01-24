<?php
class knjo151Query extends Query {

    //生徒情報取得
    function getStudentInfoData($model)
    {
        $query  =" SELECT ";
        $query .="     MAIN.SCHREGNO, ";
        $query .="     SC_R.GRADE, ";
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


    //教科
    function getClass_mst($model, $div = "", $another = "")
    {
        $query  = " SELECT DISTINCT ";
        if ($div == "ONE") {
            $query .= "     * ";
        } else {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        if ($another) {
            $query .= "     ANOTHER_CLASS_MST ";
        } else {
            $query .= "     CLASS_MST ";
        }
        if ($div == "ONE") {
            $query .= " WHERE ";
            if ($another) {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$model->select_class."' ";
            } else {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$model->select_rep_class."' ";
            }
        }
        $query .= " ORDER BY ";
        if ($div == "ALL") {
            $query .= "     VALUE ";
        } else {
            $query .= "     CLASSCD ";
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
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || VALUE(SUBCLASSNAME, '') AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        if ($another) {
            $query .= "     ANOTHER_SUBCLASS_MST ";
        } else {
            $query .= "     SUBCLASS_MST ";
        }
        $query .= " WHERE ";
        $query .= "         CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        $query .= "     AND CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


////////////////////////////////////////////////////20161128
    function getAnother($db, $model, $tblNo = "", $rowNo = "")
    {
        $query  = " SELECT ";
        $query .= "     t1.TYPE, ";
        $query .= "     t1.DATA_ROW, ";
        $query .= "     t1.CLASS_NAME, ";
        $query .= "     t1.SUBCLASS_NAME, ";
        $query .= "     t1.SUBCLASS_TANNI as GET_CREDIT, ";
        $query .= "     t1.SCHOOL_CLASS_NAME, ";
        $query .= "     t1.SCHOOL_SUBCLASS_NAME, ";
        $query .= "     t1.SCHOOL_SUBCLASS_TANNI, ";
        $query .= "     t2.CODENAME as APP_CLASSNAME, ";
        $query .= "     t3.CODENAME as APP_SUBCLASSNAME, ";
        $query .= "     t1.CLASSCD || '-' || t4.SCHOOL_KIND as REP_CLASSCD, ";
        $query .= "     t1.CLASSCD || '-' || t4.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD as REP_SUBCLASSCD, ";
        $query .= "     t1.CURRICULUM_CD as REP_CURRICULUM_CD, ";
        $query .= "     t4.CLASSNAME, ";
        $query .= "     t5.SUBCLASSNAME, ";
        $query .= "     t6.NAME1 as CURRICULUM_CD_NAME, ";
        $query .= "     t9.NAME1 as APP_CURRICULUM_NAME ";
        $query .= " FROM ";
        $query .= "     (  ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             '1' as TYPE, ";
        $query .= "             DATA_ROW, ";
        $query .= "             CLASS_NAME, ";
        $query .= "             SUBCLASS_NAME, ";
        $query .= "             SUBCLASS_TANNI, ";
        $query .= "             SCHOOL_CLASS_NAME, ";
        $query .= "             SCHOOL_SUBCLASS_NAME, ";
        $query .= "             SCHOOL_SUBCLASS_TANNI, ";
        $query .= "             CLASSCD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             CURRICULUM_CD ";
        $query .= "         FROM ";
        $query .= "             H_APP_Y_COMMON_SUBJ_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '".$model->schregno."' ";
        $query .= "         ORDER BY ";
        $query .= "             DATA_ROW  ";
        $query .= "         ) ";
        $query .= "     UNION  ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             '2' as TYPE, ";
        $query .= "             DATA_ROW, ";
        $query .= "             CLASS_NAME, ";
        $query .= "             SUBCLASS_NAME, ";
        $query .= "             SUBCLASS_TANNI, ";
        $query .= "             SCHOOL_CLASS_NAME, ";
        $query .= "             SCHOOL_SUBCLASS_NAME, ";
        $query .= "             SCHOOL_SUBCLASS_TANNI, ";
        $query .= "             CLASSCD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             CURRICULUM_CD ";
        $query .= "         FROM ";
        $query .= "             H_APP_Y_EXPERT_SUBJ_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO = '".$model->schregno."' ";
        $query .= "         ORDER BY ";
        $query .= "             DATA_ROW  ";
        $query .= "         )  ";
        $query .= "     ) t1 ";
        $query .= "     left join H_APP_CLASS_MST t2 on t1.CLASS_NAME = t2.DICTIONARY_CODE  ";
        $query .= "     left join (SELECT DISTINCT ";
        $query .= "                     DICTIONARY_CODE, ";
        $query .= "                     CURRICULUM_CD, ";
        $query .= "                     CODENAME ";
        $query .= "                 FROM ";
        $query .= "                     H_APP_SUBCLASS_MST ";
        $query .= "                 ) t3 on t1.SUBCLASS_NAME = t3.DICTIONARY_CODE  ";
        $query .= "     left join ANOTHER_CLASS_MST t4 on t1.CLASSCD = t4.CLASSCD  ";
        $query .= "     left join ANOTHER_SUBCLASS_MST t5 on t1.SUBCLASSCD = t5.SUBCLASSCD and t1.CURRICULUM_CD = t5.CURRICULUM_CD  ";
        $query .= "     left join NAME_MST t6 ON t6.NAMECD1 = 'Z018' AND t1.CURRICULUM_CD = t6.NAMECD2 ";
        $query .= "     left join H_APP_YOUROKU_SYUBETU_MST t7 on t1.SCHREGNO = t7.SCHREGNO ";
        $query .= "     left join H_APP_NAME_MST t8 on t7.YOUROKU_SYUBETU = t8.DICTIONARY_CODE and t8.DICTIONARY_KIND = '02' and t8.DICTIONARY_NO = '01' ";
        $query .= "     left join NAME_MST t9 on t8.KENJA_CODE = t9.NAMECD2 and t9.NAMECD1 = 'Z018' ";
        if($tblNo != "" && $rowNo != ""){
            $query .= "     WHERE ";
            $query .= "         t1.TYPE = '".$tblNo."' ";
            $query .= "     AND ";
            $query .= "         t1.DATA_ROW = '".$rowNo."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBCLASS_NAME ";

        return $query;
    }

    //教科名取得
    function getClassName($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_CODE = '".$model->select_classcd."' ";
        
        return $query;
    }
    //科目名取得
    function getSubclassName($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     H_APP_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     DICTIONARY_CODE = '".$model->select_subclasscd."' ";

        return $query;
    }
    //教育課程取得
    function getCurriculumName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";
        $query .= " AND ";
        $query .= "     NAMECD2 = '3' ";

        return $query;
    }
    
    //SCHREG_STUDYREC_DATのデータ確認
    function getStudyRecCnt($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SCHOOLCD = '1' ";
        
        return $query;
    }
    
    //SCHREG_STUDYREC_DAT削除
    function delStudyRec($schregno)
    {
        $query .= " DELETE FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SCHOOLCD = '1' ";
        
        return $query;
    }
    
    //H_APP_Y_COMMON_SUBJ_DATとH_APP_Y_EXPERT_SUBJ_DATからデータ取得
    function getHappData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.CLASSCD, ";
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.CURRICULUM_CD, ";
        $query .= "     t1.SUBCLASS_TANNI, ";
        $query .= "     t1.SCHOOL_SUBCLASS_TANNI ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_COMMON_SUBJ_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     UNION   ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         H_APP_Y_EXPERT_SUBJ_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ) t1  ";
        
        return $query;
    }
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //データチェック
    function getAnotherChk($model)
    {

        $subclassAll = $model->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $model->field["CLASSCD"]."-".$model->field["CURRICULUM_CD"]."-99".$model->field["SUBCLASSCD"];
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                = split("-", $subclassAll);

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

}
?>
