<?php

require_once('for_php7.php');

class knje030kQuery extends Query {

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

    function getGradeQuery()
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= " GRADE_HVAL";
        $query .= " FROM ";
        $query .= " SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;

    }

    //教科取得
    function getClass($model)
    {
        $query  = "SELECT ";
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
        $query .= "FROM ";
        $query .= "     CLASS_MST ";
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
        $query  = " SELECT * FROM SCHREG_STUDYREC_DAT"; 
        $query .= " WHERE SCHREGNO = '" .$schreg ."'";
        $query .= " AND SCHOOLCD   = '1'";
        $query .= " AND YEAR       = '" .$year ."'";
        $query .= " AND SUBCLASSCD = '" .$subclass ."'";

        return $query;
    }

    //転入生
    function getTrans_Student($row_year,$row_semester)
    {
        $query  = " SELECT DISTINCT m.SCHREGNO,m.NAME_SHOW as NAME, ";
        $query .= " d.HR_CLASS,d.ATTENDNO,d.GRADE,d.YEAR, ";
        $query .= " h.HR_NAME, h.HR_NAMEABBV ";
        $query .= " FROM SCHREG_BASE_MST m, ";
        $query .= "      SCHREG_REGD_DAT d  ";
        $query .= "      LEFT OUTER JOIN SCHREG_REGD_HDAT h ON d.YEAR=h.YEAR AND d.SEMESTER=h.SEMESTER ";
        $query .= " WHERE m.SCHREGNO = d.SCHREGNO "; 
#        $query .= " AND m.ENT_DIV    = '4' ";  # 2006/02/17
        $query .= " AND m.ENT_DIV    IN( '4','5') ";
        $query .= " AND d.YEAR       = '" .$row_year ."'";
        $query .= " AND d.SEMESTER   = '" .$row_semester ."'";
        $query .= " AND d.GRADE      = h.GRADE ";
        $query .= " AND d.HR_CLASS   = h.HR_CLASS ";
        $query .= " ORDER BY d.GRADE,d.HR_CLASS,m.SCHREGNO ";
        # echo $query;
        return $query;
    }
    //転入生成績
    function getTrans_Class($schregno,$row_year,$model)
    {
        $query  = " SELECT SCHREGNO,SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CLASSCD, ";
            $query .= " SCHOOL_KIND, ";
            $query .= " CURRICULUM_CD,";
        }
        $query .= " SUBCLASSNAME,ANNUAL,VALUATION,GET_CREDIT,ADD_CREDIT ";
        $query .= " FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHREGNO = '" .$schregno."'";
        $query .= " AND SCHOOLCD   = '1'";
        $query .= " AND YEAR = '" .$row_year."'";  
        $query .= " ORDER BY ";
        $query .= "     ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD ";
        return $query;
    }
    //成績期末
    function Record($model, $schregno)
    {
         # 2005/04/26 近大-作業依頼書20041020-01 成績テーブル変更に伴う修正
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.SUBCLASSABBV, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     T3.ASSESS AS VALUATION, ";
        $query .= "     CASE WHEN DEC(T3.ASSESS) > 1 THEN T3.CREDITS ELSE CAST(NULL AS SMALLINT) END AS GET_CREDIT, ";
        $query .= "     CAST(NULL AS SMALLINT) AS ADD_CREDIT ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1, ";
        $query .= "     KIN_RECORD_DAT T2 ";
        $query .= "     LEFT OUTER JOIN ( ";
        $query .= "                     SELECT ";
        $query .= "                          ST1.YEAR, ";
        $query .= "                          ST1.SEMESTER, ";
        $query .= "                          ST1.SCHREGNO, ";
        $query .= "                          ST1.ANNUAL, ";
        $query .= "                          ST2.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          ST2.CLASSCD, ";
            $query .= "                          ST2.SCHOOL_KIND, ";
            $query .= "                          ST2.CURRICULUM_CD, ";
        }
        $query .= "                          CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS  ";
        $query .= "                               WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS  ";
        $query .= "                               WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS  ";
        $query .= "                          END AS ASSESS, ";
        $query .= "                          ST3.CREDITS ";
        $query .= "                      FROM ";
        $query .= "                          SCHREG_REGD_DAT AS ST1, ";
        $query .= "                          KIN_RECORD_DAT AS ST2, ";
        $query .= "                          CREDIT_MST AS ST3 ";
        $query .= "                      WHERE ";
        $query .= "                          ST2.YEAR = ST1.YEAR AND ";
        $query .= "                          ST2.SCHREGNO = ST1.SCHREGNO AND ";
        $query .= "                          ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          ST2.CLASSCD = ST3.CLASSCD AND ";
            $query .= "                          ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "                          ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "                          ST3.YEAR = ST1.YEAR AND ";
        $query .= "                          ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "                          ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "                          ST3.GRADE = ST1.GRADE AND ";
        $query .= "                          ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "                          ST3.CLASSCD = SUBSTR(ST3.SUBCLASSCD,1,2) ";
        $query .= "                     ) AS T3 ON  T3.YEAR=T2.YEAR AND T3.SEMESTER='".CTRL_SEMESTER."' AND T3.SCHREGNO=T2.SCHREGNO AND T3.SUBCLASSCD=T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T3.CLASSCD=T2.CLASSCD ";
            $query .= "                                 AND T3.SCHOOL_KIND=T2.SCHOOL_KIND ";
            $query .= "                                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }   
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     T1.SUBCLASSCD ";

        #echo $query;
        return $query;
    }

    //INSERT
    function &getInsertQuery($fields,$row_year,$row_semester,$grade_range,$model)
    {
        if ($fields["SCHREGNO"]=="") {
            $schregno = knje030kQuery::First_No($row_year,$row_semester);
        } else {
            $schregno = $fields["SCHREGNO"];
        }
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
        for ($i=1;$i<=$grade_range;$i++) {
            $annual = sprintf("%02d", $i);

            if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !="" || $fields[$annual]["ADD_CREDIT"] !="") {

                $data["SCHOOLCD"][TEXT]            = 1;
                $data["SCHREGNO"][TEXT]            = $schregno;
                $data["YEAR"][TEXT]                = $row_year;
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
                $data["SUBCLASSNAME"][TEXT]        = $fields["SUBCLASSNAME"];
                $data["SUBCLASSABBV"][TEXT]        = $fields["SUBCLASSABBV"];
                $data["SUBCLASSNAME_ENG"][TEXT]    = $fields["SUBCLASSNAME_ENG"];
                $data["SUBCLASSABBV_ENG"][TEXT]    = $fields["SUBCLASSABBV_ENG"];
                $data["VALUATION"][NUMBER]         = $fields[$annual]["VALUATION"];
                $data["GET_CREDIT"][NUMBER]        = $fields[$annual]["GET_CREDIT"];
                $data["ADD_CREDIT"][NUMBER]        = $fields[$annual]["ADD_CREDIT"];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data, "schreg_studyrec_dat");
                $db->query($query);
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);
        
        return;
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
        for ($i=1;$i<=$grade_range;$i++) {
            $annual = sprintf("%02d", $i);

            if($cmd!="update"){
                if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !=""|| $fields[$annual]["ADD_CREDIT"] !="");
            }
                $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                $query .= " AND SCHOOLCD           = '1'"; 
                $query .= " AND YEAR               = '" .$row_year."'"; 
                $query .= " AND ANNUAL             = '" .$annual."'"; 
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND  CLASSCD          = '".$class_array[0]."' ";
                    $query .= " AND  SCHOOL_KIND      = '".$class_array[1]."' ";
                    $query .= " AND  CURRICULUM_CD    = '".$fields["CURRICULUM_CD"]."' ";
                    $query .= " AND  SUBCLASSCD       = '".$class_array[2]."' ";
                } else {
                    $query .= " AND SUBCLASSCD       = '" .$classcd."'"; 
                }

                $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        
        return ;
    }

    //UPDATE
    function getDeleteInsertQuery($fields,$row_year,$grade_range,$cmd,$row_semester,$grade_range,$model)
    {
        if ($fields["SCHREGNO"]=="") {
            $schregno = knje030kQuery::First_No($row_year,$row_semester);
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

        for ($i=1;$i<=$grade_range;$i++) {
            $annual = sprintf("%02d", $i);

            if($cmd!="update"){
                if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !=""|| $fields[$annual]["ADD_CREDIT"] !="");
            }
                $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= " WHERE   SCHREGNO       = '" .$fields["SCHREGNO"]."'"; 
                $query .= " AND SCHOOLCD           = '1'"; 
                $query .= " AND YEAR               = '" .$row_year."'"; 
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
        }

        for ($i=1;$i<=$grade_range;$i++) {
            $annual = sprintf("%02d", $i);

            if ($fields[$annual]["VALUATION"] !="" || $fields[$annual]["GET_CREDIT"] !=""|| $fields[$annual]["ADD_CREDIT"] !="") {

                $data["SCHOOLCD"][TEXT]            = 1;
                $data["SCHREGNO"][TEXT]            = $schregno;
                $data["YEAR"][TEXT]                = $row_year;
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
                $data["SUBCLASSNAME"][TEXT]        = $fields["SUBCLASSNAME"];
                $data["SUBCLASSABBV"][TEXT]        = $fields["SUBCLASSABBV"];
                $data["SUBCLASSNAME_ENG"][TEXT]    = $fields["SUBCLASSNAME_ENG"];
                $data["SUBCLASSABBV_ENG"][TEXT]    = $fields["SUBCLASSABBV_ENG"];
                $data["VALUATION"][NUMBER]         = $fields[$annual]["VALUATION"];
                $data["GET_CREDIT"][NUMBER]        = $fields[$annual]["GET_CREDIT"];
                $data["ADD_CREDIT"][NUMBER]        = $fields[$annual]["ADD_CREDIT"];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data, "schreg_studyrec_dat");

                $db->query($query);
                $db->commit();
            }
        }

        //$db->commit();
        Query::dbCheckIn($db);

        return ;
    }

    //リストの最初表らわした時(デフォルト)の学籍番号
    function First_No($row_year,$row_semester) 
    {
        $db = Query::dbCheckOut();

        $query  = knje030kQuery::getTrans_Student($row_year,$row_semester);
        $query .= " fetch first 1 rows only";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["SCHREGNO"];
        
    }
    
    //追加時、データは既に存在しているかを調べる
    function isNew($fields,$schregno,$subclass,$row_year,$row_semester,$model)
    {
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

        if ($schregno=="") {
            $schregno = knje030kQuery::First_No($row_year,$row_semester);
        }
        
        $db = Query::dbCheckOut();
        $query  = " SELECT COUNT(*) FROM schreg_studyrec_dat"; 
        $query .= " WHERE SCHREGNO = '" .$schregno ."'";
        $query .= " AND SCHOOLCD   = '1'";
        $query .= " AND YEAR       = '" .$row_year."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND  CLASSCD          = '".$class_array[0]."' ";
            $query .= " AND  SCHOOL_KIND      = '".$class_array[1]."' ";
            $query .= " AND  CURRICULUM_CD    = '".$fields["CURRICULUM_CD"]."' ";
            $query .= " AND  SUBCLASSCD       = '".$class_array[2]."' ";
        } else {
            $query .= " AND SUBCLASSCD = '" .$subclass ."'";
        }

        $db->query($query);
        
        $row = $db->getone($query,null,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row;
    }
        
}
?>
