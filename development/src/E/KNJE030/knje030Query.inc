<?php

require_once('for_php7.php');

class knje030Query extends Query {

    function getCurriculum()
    {
        $query  = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'Z018' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    function getGradeQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     MAX(GRADE_HVAL) AS GRADE_HVAL ";
        $query .= " FROM ";
        $query .= " SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;

    }

    //教科取得
    function getClass($model)
    {
        $query  = " SELECT ";
        $query .= "     CLASSNAME, ";
        $query .= "     CLASSABBV, ";
        $query .= "     CLASSNAME_ENG, ";
        $query .= "     CLASSABBV_ENG, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     CLASSCD || '：' || CLASSNAME AS LABEL, ";
            $query .= "     CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "           SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "           SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //科目取得
    function getSublass($class_chk, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $class_chk);
        }
        $query  = "SELECT ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$class_array[0]."' ";
            $query .= " AND SCHOOL_KIND     = '".$class_array[1]."' ";
            if ($model->field["CURRICULUM_CD"]) {
                $query .= " AND CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' ";
            }
        } else {
            $query .= "     substr(SUBCLASSCD,1,2) = '".$class_chk."' ";
        }
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD ";

        return $query;
    }
    
    //１レコード取得
    function getRow($subclass,$year,$schreg)
    {
        $query  = " SELECT";
        $query .= "     T1.*,";
        $query .= "     T2.REMARK,";
        $query .= "     T3.SUBCLASSCD as CHECKSUBCD,";
        $query .= "     T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
        $query .= "     T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
        $query .= "     T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
        $query .= "     T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M";
        $query .= " FROM";
        $query .= "     SCHREG_STUDYREC_DAT T1";
        $query .= "      LEFT JOIN STUDYRECREMARK_DAT T2 ON T2.YEAR = T1.YEAR";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO";
        $query .= "      AND T2.CLASSCD = T1.CLASSCD";
        $query .= "      AND T2.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T2.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "  AND T2.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "      LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T3.CLASSCD        = T1.CLASSCD ";
            $query .= "                           AND T3.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                           AND T3.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE T1.SCHREGNO = '" .$schreg ."'";
        $query .= " AND T1.SCHOOLCD   = '1'";
        $query .= " AND T1.SUBCLASSCD = '" .$subclass ."'";

        return $query;
    }

    //対象生徒取得
    function getTrans_Student($row_year,$row_semester, $model)
    {
        $query  = " SELECT DISTINCT m.SCHREGNO,m.NAME_SHOW as NAME, ";
        $query .= " d.HR_CLASS,d.ATTENDNO,d.GRADE,d.YEAR, ";
        $query .= " h.HR_NAME, h.HR_NAMEABBV ";
        $query .= " FROM SCHREG_BASE_MST m, ";
        $query .= "      SCHREG_REGD_DAT d  "; 
        $query .= "      LEFT OUTER JOIN SCHREG_REGD_HDAT h ON d.YEAR=h.YEAR AND d.SEMESTER=h.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON h.YEAR = REG_G.YEAR ";
                $query .= "           AND h.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON h.YEAR = REG_G.YEAR ";
            $query .= "           AND h.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE m.SCHREGNO = d.SCHREGNO "; 
        $query .= " AND m.ENT_DIV    IN('4','5','7') ";
        $query .= " AND d.YEAR       = '" .$row_year ."'";
        $query .= " AND d.SEMESTER   = '" .$row_semester ."'";
        $query .= " AND d.GRADE      = h.GRADE ";
        $query .= " AND d.HR_CLASS   = h.HR_CLASS ";
        $query .= " ORDER BY d.GRADE,d.HR_CLASS,m.SCHREGNO ";
        return $query;
    }
    
    //転入生成績
    function getTrans_Class($model, $schregno,$row_year)
    {
        $query  = " SELECT T1.YEAR,T1.SCHREGNO,T1.SUBCLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD,";
        }
        $query .= "        T1.ANNUAL,T1.VALUATION,T1.GET_CREDIT,T1.ADD_CREDIT,T1.COMP_CREDIT,T2.REMARK, ";
        $query .= "        CASE WHEN T3.SUBCLASSCD IS NULL ";
        $query .= "             THEN T1.SUBCLASSNAME ";
        $query .= "             ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME ";
        $query .= " FROM SCHREG_STUDYREC_DAT T1";
        $query .= "      LEFT JOIN STUDYRECREMARK_DAT  T2 ON T2.YEAR = T1.YEAR";
        $query .= "                                      AND T2.SCHREGNO = T1.SCHREGNO";
        $query .= "                                      AND T2.CLASSCD = T1.CLASSCD";
        $query .= "                                      AND T2.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD";
        }
        $query .= "      LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T3.CLASSCD         = T1.CLASSCD";
            $query .= "                           AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND";
            $query .= "                           AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD";
        }
        $query .= " WHERE T1.SCHREGNO = '" .$schregno."'";
        $query .= " AND T1.SCHOOLCD   = '1'";
        $query .= " ORDER BY ";
        $query .= "     T1.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        
        return $query;
    }
    
    //成績期末
    function Record($model,$schregno,$row_year)
    {
    
/*
        $query  = " SELECT DISTINCT t1.SUBCLASSCD,t1.SUBCLASSNAME,t1.SUBCLASSABBV,t2.ANNUAL,t2.VALUATION,t2.GET_CREDIT,t2.ADD_CREDIT  ";
        $query .= "   FROM SUBCLASS_MST t1,RECORDGRADE_DAT t2 ";
        $query .= "  WHERE t2.SCHREGNO       = '" .$schregno."'"; 
        $query .= "    AND t2.YEAR           = '" .$row_year."'"; 
        $query .= "    AND t2.GRADINGCLASSCD = t1.SUBCLASSCD "; 
        $query .= "  ORDER BY t2.ANNUAL,t1.SUBCLASSCD ";
*/
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.subclasscd, ";
        $query .= "     T1.subclassname, ";
        $query .= "     T1.subclassabbv, ";
        $query .= "     T3.annual, ";
        $query .= "     T2.grad_value, ";
        $query .= "     T2.get_credit, ";
        $query .= "     T2.COMP_CREDIT, ";
        $query .= "     T2.add_credit ";
        $query .= " FROM ";
        $query .= "     subclass_mst T1, ";
        $query .= "     record_dat T2, ";
        $query .= "     schreg_regd_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T2.schregno   = '" .$schregno."' AND ";
        $query .= "     T2.year       = '" .$row_year."' AND ";
        $query .= "     T3.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.subclasscd = T1.subclasscd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD       = T1.CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND   = T1.SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }   
        $query .= "     T3.schregno   = T2.schregno AND ";
        $query .= "     T3.year       = T2.year ";
        $query .= " ORDER BY ";
        $query .= "     T3.annual, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.subclasscd ";

        return $query;
    }

    //署名チェック
    function getOpinionsWk($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        return $query;
    }

    //DELETE
    function &getDeleteQuery($fields,$row_year,$grade_range,$cmd,$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd=$fields["CLASSCD"].'-'.substr($fields["CLASSCD"], 0, 2).$fields["SUBCLASSCD"];
        } else {
            $classcd=$fields["CLASSCD"].$fields["SUBCLASSCD"];
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        for ($i = 0; $i <= $grade_range; $i++) {
            $annual = sprintf("%02d", $i);

            //署名チェック
            if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje030Query::getOpinionsWk($fields[$annual]["YEAR"], $fields["SCHREGNO"]);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    continue;
                }
            }

            if($cmd!="update"){
                if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !="" || $fields[$annual]["COMP_CREDIT"] !="");
            }
                $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                $query .= " AND SCHOOLCD           = '1'"; 
                $query .= " AND YEAR               = '" .$fields[$annual]["YEAR"]."'"; 
                $query .= " AND ANNUAL             = '" .$annual."'"; 
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND  CLASSCD          = '".$class_array[0]."' ";
                    $query .= " AND  SCHOOL_KIND      = '".$class_array[1]."' ";
                    $query .= " AND  CURRICULUM_CD    = '".$fields["CURRICULUM_CD"]."' ";
                    $query .= " AND  SUBCLASSCD       = '".$class_array[2]."' ";
                } else {
                    $query .= " AND  SUBCLASSCD       = '" .$classcd."'"; 
                }
                $db->query($query);

                $query  = " DELETE FROM STUDYRECREMARK_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                $query .= " AND YEAR               = '" .$fields[$annual]["YEAR"]."'"; 
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND  CLASSCD          = '".$class_array[0]."' ";
                    $query .= " AND  SCHOOL_KIND      = '".$class_array[1]."' ";
                    $query .= " AND  CURRICULUM_CD    = '".$fields["CURRICULUM_CD"]."' ";
                    $query .= " AND  SUBCLASSCD       = '".$class_array[2]."' ";
                } else {
                    $query .= " AND  CLASSCD          = '" .$fields["CLASSCD"]."'"; 
                    $query .= " AND  SUBCLASSCD       = '" .$classcd."'"; 
                }
                $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        
        return ;
    }

    //UPDATE
    function getDeleteInsertQuery($fields,$row_year,$grade_range,$cmd,$row_semester,$model)
    {
        if ($fields["SCHREGNO"]=="") {
            $schregno = knje030Query::First_No($row_year,$row_semester,$model);
        } else {
            $schregno = $fields["SCHREGNO"];
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd=$fields["CLASSCD"].'-'.substr($fields["CLASSCD"], 0, 2).$fields["SUBCLASSCD"];
        } else {
            $classcd=$fields["CLASSCD"].$fields["SUBCLASSCD"];
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i <= $grade_range; $i++) {
            $annual = sprintf("%02d", $i);

            //署名チェック
            if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje030Query::getOpinionsWk($fields[$annual]["YEAR"], $fields["SCHREGNO"]);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    continue;
                }
            }

            if($cmd!="update"){
                if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !="" || $fields[$annual]["COMP_CREDIT"] !="");
            }
                $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                $query .= " AND SCHOOLCD           = '1'"; 
                $query .= " AND ANNUAL             = '" .$annual."'";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND  CLASSCD          = '".$class_array[0]."' ";
                    $query .= " AND  SCHOOL_KIND      = '".$class_array[1]."' ";
                    $query .= " AND  CURRICULUM_CD    = '".$fields["CURRICULUM_CD"]."' ";
                    $query .= " AND  SUBCLASSCD       = '".$class_array[2]."' ";
                } else {
                    $query .= " AND SUBCLASSCD         = '" .$classcd."'";
                }
                $db->query($query);

                $query  = " DELETE FROM STUDYRECREMARK_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND CLASSCD           = '".$class_array[0]."' ";
                    $query .= " AND SCHOOL_KIND       = '".$class_array[1]."' ";
                    $query .= " AND CURRICULUM_CD     = '".$fields["CURRICULUM_CD"]."' ";
                    $query .= " AND SUBCLASSCD        = '".$class_array[2]."' ";
                } else {
                    $query .= " AND CLASSCD            = '" .$fields["CLASSCD"]."'"; 
                    $query .= " AND SUBCLASSCD         = '" .$classcd."'"; 
                }
                $db->query($query);
        }

        for ($i = 0; $i <= $grade_range; $i++) {
            $annual = sprintf("%02d", $i);

            //署名チェック
            if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje030Query::getOpinionsWk($fields[$annual]["YEAR"], $schregno);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    continue;
                }
            }

            if (($fields[$annual]["YEAR"] !="") && $fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !="" || $fields[$annual]["COMP_CREDIT"] !="") {

                $subclassname     = "";
                $subclassabbv     = "";
                $subclassname_eng = "";
                $subclassabbv_eng = "";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query = "SELECT COUNT(*) AS CNT FROM SUBCLASS_MST WHERE CLASSCD = '".$class_array[0]."' AND SCHOOL_KIND = '".$class_array[1]."' AND CURRICULUM_CD = '".$fields["CURRICULUM_CD"]."'  AND SUBCLASSCD = '".$class_array[2]."' ";
                } else {
                    $query = "SELECT COUNT(*) AS CNT FROM SUBCLASS_MST WHERE SUBCLASSCD = '".$classcd."'";
                }
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row["CNT"] == 0) {
                    $subclassname     = $fields["SUBCLASSNAME"];
                    $subclassabbv     = $fields["SUBCLASSABBV"];
                    $subclassname_eng = $fields["SUBCLASSNAME_ENG"];
                    $subclassabbv_eng = $fields["SUBCLASSABBV_ENG"];
                }

                $data["SCHOOLCD"][TEXT]            = 1;
                $data["SCHREGNO"][TEXT]            = $schregno;
                $data["YEAR"][TEXT]                = $fields[$annual]["YEAR"];
                $data["ANNUAL"][TEXT]              = $annual;
               //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]             = $class_array[0];
                    $data["SCHOOL_KIND"][TEXT]         = $class_array[1];
                    $data["CURRICULUM_CD"][TEXT]       = $fields["CURRICULUM_CD"];
                    $data["SUBCLASSCD"][TEXT]          = $class_array[2];
                } else {
                    $data["CLASSCD"][TEXT]             = $fields["CLASSCD"];
                    $data["SUBCLASSCD"][TEXT]          = $classcd;
                }
                $data["CLASSNAME"][TEXT]           = $fields["CLASSNAME"];
                $data["CLASSABBV"][TEXT]           = $fields["CLASSABBV"];
                $data["CLASSNAME_ENG"][TEXT]       = $fields["CLASSNAME_ENG"];
                $data["CLASSABBV_ENG"][TEXT]       = $fields["CLASSABBV_ENG"];
                
                $data["SUBCLASSNAME"][TEXT]        = $subclassname;
                $data["SUBCLASSABBV"][TEXT]        = $subclassabbv;
                $data["SUBCLASSNAME_ENG"][TEXT]    = $subclassname_eng;
                $data["SUBCLASSABBV_ENG"][TEXT]    = $subclassabbv_eng;
                $data["VALUATION"][NUMBER]         = $fields[$annual]["VALUATION"];
                $data["GET_CREDIT"][NUMBER]        = $fields[$annual]["GET_CREDIT"];
                $get_credit = $fields[$annual]["GET_CREDIT"];
                $com_credit = $fields[$annual]["COMP_CREDIT"];
                if ($com_credit == "" || $com_credit == 0) {
                        //「履修単位」欄がNULLあるいはゼロ  の場合
                        $data["COMP_CREDIT"][NUMBER]   = $fields[$annual]["GET_CREDIT"];
                } else {
                    if ($com_credit < $get_credit) {
                        //「履修単位」欄の値 <「修得単位」欄の値    の場合
                        $data["COMP_CREDIT"][NUMBER]   = $fields[$annual]["GET_CREDIT"];
                    } else {
                        //「履修単位」欄の値 >=「修得単位」欄の値   の場合
                        $data["COMP_CREDIT"][NUMBER]   = $fields[$annual]["COMP_CREDIT"];
                    }
                }
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data, "schreg_studyrec_dat");
            
                $db->query($query);
                if ($fields[$annual]["REMARK"] != "") {
                    $data2["YEAR"][TEXT]                = $fields[$annual]["YEAR"];
                    $data2["SCHREGNO"][TEXT]            = $schregno;
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data2["CLASSCD"][TEXT]            = $class_array[0];
                        $data2["SCHOOL_KIND"][TEXT]        = $class_array[1];
                        $data2["CURRICULUM_CD"][TEXT]      = $fields["CURRICULUM_CD"];
                        $data2["SUBCLASSCD"][TEXT]         = $class_array[2];
                    } else {
                        $data2["CLASSCD"][TEXT]            = $fields["CLASSCD"];
                        $data2["SUBCLASSCD"][TEXT]         = $classcd;
                    }
                    $data2["REMARK"][TEXT]              = $fields[$annual]["REMARK"];
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "STUDYRECREMARK_DAT");
                    $db->query($query);
                }
                $db->commit();
            }
        }

        //$db->commit();
        Query::dbCheckIn($db);

        return ;
    }

    //リストの最初表らわした時(デフォルト)の学籍番号
    function First_No($row_year,$row_semester, $model) 
    {
        $db = Query::dbCheckOut();

        $query  = knje030Query::getTrans_Student($row_year,$row_semester, $model);
        $query .= " fetch first 1 rows only";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["SCHREGNO"];
        
    }
    
    function Studyremark_data($row_year,$row_semester) 
    {
        $db = Query::dbCheckOut();

                $query  = " SELECT  ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD ";
                $query .= " FROM";
                $query .= "    STUDYRECREMARK_DAT ";
                $query .= " WHERE ";
                $query .= "      YEAR      = '" .$row_year."'";                
                $query .= "  AND SCHREGNO  = '" .sprintf("%07d",$row_semester)."'";                
                
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
        
    }
    
}
?>
