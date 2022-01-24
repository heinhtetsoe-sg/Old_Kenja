<?php

require_once('for_php7.php');

class knjz290_2Query extends Query {
    //名称取得
    function getVNameMst($namecd1, $model) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "Z043" && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAMESPARE2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度取得
    function getStaffYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.STAFFNAME_KANA, ";
        $query .= "     T1.STAFFNAME_ENG, ";
        $query .= "     T1.STAFFNAME_REAL, ";
        $query .= "     T1.STAFFNAME_KANA_REAL, ";
        $query .= "     L1.FIELD1 AS JOBCD, ";
        $query .= "     L2.FIELD1 AS SECTIONCD, ";
        $query .= "     L3.FIELD1 AS DUTYSHARECD, ";
        $query .= "     L4.FIELD1 AS CHARGECLASSCD, ";
        $query .= "     L5.FIELD1 AS POSITIONCD1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 || '-' || L5.FIELD3 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        }
        
        $query .= "     L6.FIELD1 AS POSITIONCD2, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 || '-' || L6.FIELD3 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        }
        
        $query .= "     L7.FIELD1 AS POSITIONCD3, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L6.FIELD1 = '1050' THEN L7.FIELD2 || '-' || L7.FIELD3 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L6.FIELD1 = '1050' THEN L7.FIELD2 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        }
        $query .= "     T2.JOBNAME, ";
        $query .= "     T3.SECTIONABBV, ";
        $query .= "     T4.SHARENAME, ";
        $query .= "     T6.POSITIONNAME AS POSITIONNAME1, ";
        $query .= "     T7.POSITIONNAME AS POSITIONNAME2, ";
        $query .= "     T8.POSITIONNAME AS POSITIONNAME3, ";
        $query .= "     T1.STAFFSEX, ";
        $query .= "     T5.NAME2, ";
        $query .= "     T1.STAFFBIRTHDAY, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     T1.STAFFADDR2, ";
        $query .= "     T1.STAFFTELNO, ";
        $query .= "     T1.STAFFFAXNO, ";
        $query .= "     T1.STAFFE_MAIL, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L1.STAFFCD ";
        $query .= "          AND L1.STAFF_SEQ = '001' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L2.STAFFCD ";
        $query .= "          AND L2.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L3.STAFFCD ";
        $query .= "          AND L3.STAFF_SEQ = '003' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L4.STAFFCD ";
        $query .= "          AND L4.STAFF_SEQ = '004' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L5.STAFFCD ";
        $query .= "          AND L5.STAFF_SEQ = '005' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L6.STAFFCD ";
        $query .= "          AND L6.STAFF_SEQ = '006' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L7.STAFFCD ";
        $query .= "          AND L7.STAFF_SEQ = '007' ";
        $query .= "     LEFT JOIN JOB_MST T2 ON L1.FIELD1 = T2.JOBCD ";
        $query .= "     LEFT JOIN SECTION_MST T3 ON L2.FIELD1 = T3.SECTIONCD ";
        $query .= "     LEFT JOIN DUTYSHARE_MST T4 ON L3.FIELD1 = T4.DUTYSHARECD ";
        $query .= "     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'Z002' AND T1.STAFFSEX = T5.NAMECD2 ";
        $query .= "     LEFT JOIN POSITION_MST T6 ON L5.FIELD1 = T6.POSITIONCD ";
        $query .= "     LEFT JOIN POSITION_MST T7 ON L6.FIELD1 = T7.POSITIONCD ";
        $query .= "     LEFT JOIN POSITION_MST T8 ON L7.FIELD1 = T8.POSITIONCD ";
        if ($model->year !== 'ALL') {
            $query .= " WHERE ";
            $query .= "     T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$detailYear."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD";

        return $query;
    }

    //学年取得
    function getGrade($model, $positioncdManager="") {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        if ($positioncdManager) {
            $query .= "     GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$detailYear."' ";
        if ($positioncdManager) {
            $query .= " AND GRADE = '".$positioncdManager."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //教科取得
    function getClass($model, $positioncdManager="") {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        if ($positioncdManager) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL ";
            } else {
                $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL ";
            }
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL ";
            } else {
                $query .= "     CLASSCD AS VALUE, ";
                $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL ";
            }
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$detailYear."' ";
        if ($positioncdManager) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$positioncdManager."' ";
            } else {
                $query .= " AND CLASSCD = '".$positioncdManager."' ";
            }
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //１レコード取得
    function getRow($model, $staffcd) {

        $db = Query::dbCheckOut();
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.STAFFNAME_KANA, ";
        $query .= "     T1.STAFFNAME_ENG, ";
        $query .= "     T1.STAFFNAME_REAL, ";
        $query .= "     T1.STAFFNAME_KANA_REAL, ";
        $query .= "     L1.FIELD1 AS JOBCD, ";
        $query .= "     L2.FIELD1 AS SECTIONCD, ";
        $query .= "     L3.FIELD1 AS DUTYSHARECD, ";
        $query .= "     L3.FIELD2 AS DUTYSHARECD2, ";
        $query .= "     L4.FIELD1 AS CHARGECLASSCD, ";
        $query .= "     L5.FIELD1 AS POSITIONCD1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 || '-' || L5.FIELD3 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        }
        
        $query .= "     L6.FIELD1 AS POSITIONCD2, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 || '-' || L6.FIELD3 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        }
        
        $query .= "     L7.FIELD1 AS POSITIONCD3, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L7.FIELD1 = '1050' THEN L7.FIELD2 || '-' || L7.FIELD3 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L7.FIELD1 = '1050' THEN L7.FIELD2 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        }
        $query .= "     L8.FIELD1 AS CHARGENURSEOFF, ";
        $query .= "     T1.STAFFSEX, ";
        $query .= "     T1.STAFFBIRTHDAY, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     T1.STAFFADDR2, ";
        $query .= "     T1.STAFFTELNO, ";
        $query .= "     T1.STAFFFAXNO, ";
        $query .= "     T1.STAFFE_MAIL, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L1.STAFFCD ";
        $query .= "          AND L1.STAFF_SEQ = '001' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L2.STAFFCD ";
        $query .= "          AND L2.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L3.STAFFCD ";
        $query .= "          AND L3.STAFF_SEQ = '003' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L4.STAFFCD ";
        $query .= "          AND L4.STAFF_SEQ = '004' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L5.STAFFCD ";
        $query .= "          AND L5.STAFF_SEQ = '005' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L6.STAFFCD ";
        $query .= "          AND L6.STAFF_SEQ = '006' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L7.STAFFCD ";
        $query .= "          AND L7.STAFF_SEQ = '007' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L8 ON L8.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L8.STAFFCD ";
        $query .= "          AND L8.STAFF_SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //１レコード取得(マスタ管理用)
    function getRow2($staffcd,$classcd,$sdate,$model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM  ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '" .$staffcd ."'";
        $query .= " AND CLASSCD = '" .$classcd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND   = '" .$model->school_kind."'";
        }
        $query .= " AND SDATE = '" .str_replace("/", "-", $sdate) ."'";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
            $query .= "      CLASSCD ";
        } else {
            $query .= "      CLASSCD ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }
    
    //教育課程重複エラーチェック１レコード取得(マスタ管理用)
    function getRow2check($staffcd,$classcd,$sdate,$model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM STAFF_CLASS_MST ";
        $query .= " WHERE STAFFCD       = '" .$staffcd ."'";
        $query .= " AND CLASSCD         = '" .$class_array[0]."'";
        $query .= " AND SCHOOL_KIND     = '" .$class_array[1]."'";
        $query .= " AND SDATE = '" .str_replace("/", "-", $sdate) ."'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }
    
    
    //職名
    function getJOBNAME()
    {
        return "SELECT DISTINCT JOBCD,JOBNAME FROM V_JOB_MST ORDER BY JOBCD";
    }
    
    //所属
    function getSECTION()
    {
        return "SELECT DISTINCT SECTIONCD,SECTIONABBV FROM V_SECTION_MST ORDER BY SECTIONCD";
    }

    //校務分掌部
    function getDUTYSHARE()
    {
        return "SELECT DISTINCT DUTYSHARECD,SHARENAME FROM V_DUTYSHARE_MST ORDER BY DUTYSHARECD";
    }

    //職員性別
    function getSTAFFSEX()
    {
        return "SELECT DISTINCT NAMECD1,NAMECD2,NAME2 FROM V_NAME_MST WHERE NAMECD1 = 'Z002' ORDER BY NAMECD1";
    }

    //資格教科
    function getStaffClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$model->staffcd."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }
        return $query;
    }
    
    //肩書き
    function getPositionCd($model)
    {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     T1.POSITIONCD || ' ' || L1.POSITIONNAME AS LABEL, ";
        $query .= "     T1.POSITIONCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT T1 ";
        $query .= "     LEFT JOIN POSITION_MST L1 ON L1.POSITIONCD = T1.POSITIONCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$detailYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //年度データの確認
    function IsExisting($staffcd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM STAFF_YDAT WHERE STAFFCD = '".$staffcd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);
        
        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }
    
    //追加　教科名取得--------------------------マスタメンテ用
    function getName($model)
    {
        $query .= "  SELECT ";
        $query .= "      T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "      T1.CLASSNAME ";
        $query .= "  FROM ";
        $query .= "      CLASS_MST T1 ";
        $query .= "  ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }

        return $query;
    }
    
    //追加　教科データの取得---------------------マスタメンテ用
    function getClassSdateEdate($staffcd, $model)
    {
        $query  = " SELECT ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$staffcd}' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }

        return $query;
    }

    //マスタメンテの重複確認用関数
    function get_cnt_Classcd($staffcd,$classcd,$model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD  = '{$classcd}' ";
        }

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFFNAME"][TEXT]            = $fields["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $fields["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $fields["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $fields["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $fields["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $fields["STAFFNAME_KANA_REAL"];
        $data["JOBCD"][TEXT]                = $fields["JOBNAME"];
        $data["SECTIONCD"][TEXT]            = $fields["SECTIONABBV"];
        $data["DUTYSHARECD"][TEXT]          = $fields["SHARENAME"];
        $data["CHARGECLASSCD"][TEXT]        = $fields["CHARGECLASS"];
        $data["STAFFSEX"][TEXT]             = $fields["NAME2"];
        $data["STAFFBIRTHDAY"][TEXT]        = str_replace("/","-",$fields["STAFFBIRTHDAY"]);
        $data["STAFFZIPCD"][TEXT]           = $fields["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]           = $fields["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]           = $fields["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]           = $fields["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]           = $fields["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]          = $fields["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        
        $query = Query::insertSQL($data, "STAFF_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $data = array();
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];        
        $data["STAFFNAME"][TEXT]            = $fields["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $fields["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $fields["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $fields["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $fields["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $fields["STAFFNAME_KANA_REAL"];
        $data["JOBCD"][TEXT]                = $fields["JOBNAME"];
        $data["SECTIONCD"][TEXT]            = $fields["SECTIONABBV"];
        $data["DUTYSHARECD"][TEXT]          = $fields["SHARENAME"];
        $data["CHARGECLASSCD"][TEXT]        = $fields["CHARGECLASS"];
        $data["STAFFSEX"][TEXT]             = $fields["NAME2"];
        $data["STAFFBIRTHDAY"][TEXT]        = str_replace("/","-",$fields["STAFFBIRTHDAY"]);
        $data["STAFFZIPCD"][TEXT]           = $fields["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]           = $fields["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]           = $fields["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]           = $fields["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]           = $fields["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]          = $fields["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = "WHERE STAFFCD             = '" .$fields["STAFFCD"] ."'";
        $query = Query::updateSQL($data, "STAFF_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //STAFF_DETAIL_MSTチェック
    function &getCountDetailMst($model, $fields, $seq)
    {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$detailYear ."' ";
        $query .= " AND STAFFCD = '" .$fields["STAFFCD"] ."' ";
        $query .= " AND STAFF_SEQ = '".$seq."' ";

        return $query;
    }

    //--- UPDATE, INSERT (STAFF_DETAIL_MST)
    function &getUpdateDetailQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        //職名
        $getCount1 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "001"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["JOBNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount1 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "001";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '001'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //所属
        $getCount2 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "002"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["SECTIONABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount2 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "002";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '002'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //校務分掌部
        $getCount3 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "003"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["SHARENAME"];
        $data["FIELD2"][TEXT]               = $fields["SHARENAME2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount3 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "003";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '003'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //授業受け持ち
        $getCount4 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "004"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["CHARGECLASS"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount4 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "004";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '004'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //肩書き１
        $getCount5 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "005"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["POSITIONCD1"];
        if ($fields["POSITIONCD1"] === '0200') {
            $data["FIELD2"][TEXT]               = $fields["POSITIONCD1_MANAGER"];
            $data["FIELD3"][TEXT]               = "";
        } else if ($fields["POSITIONCD1"] === '1050') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind) = explode("-", $fields["POSITIONCD1_MANAGER"]);
                $data["FIELD2"][TEXT]               = $classcd;
                $data["FIELD3"][TEXT]               = $school_kind;
            } else {
                $data["FIELD2"][TEXT]               = $fields["POSITIONCD1_MANAGER"];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount5 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "005";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '005'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }
        //肩書き２
        $getCount6 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "006"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["POSITIONCD2"];
        if ($fields["POSITIONCD2"] === '0200') {
            $data["FIELD2"][TEXT]               = $fields["POSITIONCD2_MANAGER"];
            $data["FIELD3"][TEXT]               = "";
        } else if ($fields["POSITIONCD2"] === '1050') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind) = explode("-", $fields["POSITIONCD2_MANAGER"]);
                $data["FIELD2"][TEXT]               = $classcd;
                $data["FIELD3"][TEXT]               = $school_kind;
            } else {
                $data["FIELD2"][TEXT]               = $fields["POSITIONCD2_MANAGER"];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount6 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "006";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '006'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);

            $db->query($query);
        }
        //肩書き３
        $getCount7 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "007"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["POSITIONCD3"];
        if ($fields["POSITIONCD3"] === '0200') {
            $data["FIELD2"][TEXT]               = $fields["POSITIONCD3_MANAGER"];
            $data["FIELD3"][TEXT]               = "";
        } else if ($fields["POSITIONCD3"] === '1050') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind) = explode("-", $fields["POSITIONCD3_MANAGER"]);
                $data["FIELD2"][TEXT]               = $classcd;
                $data["FIELD3"][TEXT]               = $school_kind;
            } else {
                $data["FIELD2"][TEXT]               = $fields["POSITIONCD3_MANAGER"];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount7 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "007";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '007'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);

            $db->query($query);
        }
        //担当保健室
        $getCount8 = $db->getOne(knjz290_2Query::getCountDetailMst($model, $fields, "008"));
        $data = array();
        $data["FIELD1"][TEXT]               = $fields["CHARGENURSEOFF"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount8 == 0) {
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = "008";
            $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
            $db->query($query);
        } else {
            $where  = "WHERE YEAR    = '" .$detailYear ."'";
            $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
            $where .= "  AND STAFF_SEQ = '008'";
            $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //--- 詳細データの最新でUPDATE 
    function &newDataUpdate($model, $fields)
    {        
        $db = Query::dbCheckOut();

        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     MAX(YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$fields["STAFFCD"]."' ";
        $query .= " GROUP BY ";
        $query .= "     STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 MAX_T E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = E1.YEAR ";
        $query .= "                 AND T1.STAFFCD = E1.STAFFCD ";
        $query .= "            ) ";

        $result = $db->query($query);
        $data = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["STAFF_SEQ"] == "001") {
                $data["JOBCD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "002") {
                $data["SECTIONCD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "003") {
                $data["DUTYSHARECD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "004") {
                $data["CHARGECLASSCD"][TEXT] = $row["FIELD1"];
            }
        }
        $result->free();

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where = "WHERE STAFFCD = '" .$fields["STAFFCD"] ."'";

        $query = Query::updateSQL($data, "STAFF_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //マスタメンテ用 Insert-----------
    function &getInsert2Query($fields, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $fields["CLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $class_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $class_array[1];
        } else {
            $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        }
        $data["SDATE"][TEXT]                = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]                = str_replace("/","-",$fields["EDATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_CLASS_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //マスタメンテ用UpDate--------------
    function &getUpdate2Query($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->subField2["CLASSCD"]);
        }
        $db = Query::dbCheckOut();

        //職員資格科目マスタの削除
        $query  = " DELETE FROM STAFF_CLASS_MST ";
        $query .= " WHERE STAFFCD     = '" .$model->staffcd. "'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND   CLASSCD       = '" .$class_array[0]. "'";
            $query .= " AND   SCHOOL_KIND   = '" .$class_array[1]. "'";
        } else {
            $query .= " AND   CLASSCD     = '" .$model->subField2["CLASSCD"]. "'";
        }
        $db->query($query);

        $data["STAFFCD"][TEXT]              = $model->staffcd;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $class_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $class_array[1];
        } else {
            $data["CLASSCD"][TEXT]          = $model->subField2["CLASSCD"];
        }
        $data["SDATE"][TEXT]                = str_replace("/","-",$model->subField2["SDATE"]);
        $data["EDATE"][TEXT]                = str_replace("/","-",$model->subField2["EDATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_CLASS_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //職員マスタの削除
        $query  = " DELETE FROM STAFF_MST ";
        $query .= " WHERE STAFFCD = '".$fields["STAFFCD"]."' ";
        $db->query($query);

        //学年主任、教科主任情報削除
        $query  = " DELETE FROM STAFF_DETAIL_MST ";
        $query .= " WHERE  ";
        $query .= "     STAFFCD   = '".$fields["STAFFCD"]."' ";
        $query .= " AND STAFF_SEQ IN ('001', '002', '003', '004', '005', '006', '007', '008') ";
        $db->query($query);

        //職員資格科目マスタの削除
        $query = " DELETE FROM STAFF_CLASS_MST WHERE STAFFCD = '".$fields["STAFFCD"]."' ";
        $db->query($query);

        //利用者マスタの削除
        $query = " DELETE FROM USER_MST WHERE STAFFCD = '".$fields["STAFFCD"]."' ";
        $db->query($query);

        //利用者パスワード履歴マスタの削除
        $query = " DELETE FROM USER_PWD_HIST_MST WHERE STAFFCD = '".$fields["STAFFCD"]."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return true;
    }

    function &getDelete2Query($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->subField2["CLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //職員資格科目マスタの削除
        $query  = " DELETE FROM STAFF_CLASS_MST ";
        $query .= " WHERE STAFFCD     = '" .$model->staffcd. "'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND   CLASSCD       = '" .$class_array[0]. "'";
            $query .= " AND   SCHOOL_KIND   = '" .$class_array[1]. "'";
        } else {
            $query .= " AND   CLASSCD     = '" .$model->subField2["CLASSCD"]. "'";
        }
        $query .= " AND   SDATE       = '" .str_replace("/","-",$model->subField2["SDATE"]). "'";
        $query .= " AND   EDATE       = '" .str_replace("/","-",$model->subField2["EDATE"]). "'";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return true;
    }
    
    //職員情報 
    function getStfInfo($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T2.CLASSCD, ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= "     LEFT JOIN STAFF_CLASS_MST T2 ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.CLASSCD ";

        return $query;
    }

    //職員情報SubForm用
    function getStfInfo2($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= "     LEFT JOIN STAFF_CLASS_MST T2 ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.CLASSCD ";

        return $query;
    }

    //職員取得---//いらない？
    function getStaffList($model, $flg="")
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ' ' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        if($model->selectStaff[0]){
            if($flg == "select"){
                $query .= "     STAFFCD IN ('".implode("','", $model->selectStaff)."') ";
            } else {
                $query .= "     STAFFCD NOT IN ('".implode("','", $model->selectStaff)."') ";
            }
        } else {
            if($flg == "select"){
                $query .= "     STAFFCD = '".$model->staffcd."' ";
            } else {
                $query .= "     STAFFCD <> '".$model->staffcd."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格科目⇒資格教科(subclascd ⇒ classcd)
    
    function getStfSubclass($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || ' ' || T2.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //科目⇒教科
    
    function getSubclass($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD AS VALUE, ";
        $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD NOT IN ( SELECT ";
        $query .= "                             CLASSCD ";
        $query .= "                         FROM ";
        $query .= "                             STAFF_CLASS_MST  ";
        $query .= "                         WHERE ";
        $query .= "                             STAFFCD = '".$staffcd."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    

    //--- UPDATE 
    function &getUpdateSubQuery1($model)
    {
        $subclass = explode(",",implode(",",$model->selectSubclass));
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //削除
        $query = " DELETE FROM STAFF_CLASS_MST WHERE STAFFCD = '".$model->staffcd."' ";
        $db->query($query);

        if($subclass[0] != ""){
            //追加
            for($i=0; $i < get_count($model->selectSubclass) ; $i++){
                $data["STAFFCD"][TEXT]      = $model->staffcd;        
                $data["CLASSCD"][TEXT]      = substr($subclass[$i],0,2);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "STAFF_CLASS_MST");
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateSubQuery2($model)
    {
        $staff = explode(",",implode(",",$model->selectStaff));
        $subclass = explode(",",implode(",",$model->selectSubclass));
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if($staff[0] != ""){
            for($i=0; $i < get_count($model->selectStaff) ; $i++){
                //削除
                $query = " DELETE FROM STAFF_CLASS_MST WHERE STAFFCD = '".$staff[$i]."' ";
                $db->query($query);

                //追加
                if($subclass[0] != ""){
                    for($j=0; $j < get_count($model->selectSubclass) ; $j++){
                        $data["STAFFCD"][TEXT]      = $staff[$i];
                        $data["CLASSCD"][TEXT]      = substr($subclass[$j],0,2);
                        //教育課程対応
                        /*if ($model->Properties["useCurriculumcd"] == '1') {
                            $data["SCHOOL_KIND"][TEXT]    = substr($subclasscd, 3, 1);
                            $data["CURRICULUM_CD"][TEXT]  = substr($subclasscd, 5, 1);
                        }*/
                        $data["SUBCLASSCD"][TEXT]   = $subclass[$j];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $query = Query::insertSQL($data, "STAFF_CLASS_MST");
                        $db->query($query);
                    }
                }
            }
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    /**
     * 前年度コピー(職員詳細データ)処理
    */
    function &getCopyDetail($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 1; $i <= 8; $i++) {
            //対象のSTAFF_SEQをセット
            $setSeq = sprintf ("%03d", $i);
            //対象の前年度データを取得
            $result = $db->query(knjz290_2Query::getCopyList($model, $setSeq));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkCountdata = $db->getOne(knjz290_2Query::getCheckCountData($model, $setSeq, $row["STAFFCD"]));
                if ($checkCountdata > 0) {
                    $checkUpdata = $db->getOne(knjz290_2Query::getCheckCountData($model, $setSeq, $row["STAFFCD"], "UPDATE"));
                //Insert処理
                } else {
                    knjz290_2Query::getInsertCopyQuery($db, $row, $model);
                }
                //Update処理
                if ($checkUpdata > 0 && $checkUpdata == "1") {
                    knjz290_2Query::getUpdateCopyQuery($db, $row, $model);
                }
            }
            $result->free();
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return true;
    }
    
    //コピー元対象データ取得
    function getCopyList($model, $setSeq) {
        $setOldYear = $model->year - 1;
        
        $query  = " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFF_SEQ, ";
        $query .= "     T1.FIELD1, ";
        $query .= "     T1.FIELD2, ";
        $query .= "     T1.FIELD3, ";
        $query .= "     T1.FIELD4, ";
        $query .= "     T1.FIELD5 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setOldYear."' ";
        $query .= " AND T1.STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND (T1.FIELD1 IS NOT NULL ";
        $query .= " OR   T1.FIELD2 IS NOT NULL ";
        $query .= " OR   T1.FIELD3 IS NOT NULL ";
        $query .= " OR   T1.FIELD4 IS NOT NULL ";
        $query .= " OR   T1.FIELD5 IS NOT NULL) ";
        return $query;
    }

    //今年度データをカウント
    function getCheckCountData($model, $setSeq, $staffcd, $upflg="") {
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";
        if ($upflg != "") {
            $query .= " AND FIELD1 IS NULL ";
            $query .= " AND FIELD2 IS NULL ";
            $query .= " AND FIELD3 IS NULL ";
            $query .= " AND FIELD4 IS NULL ";
            $query .= " AND FIELD5 IS NULL ";
        }

        return $query;
    }
    
    //詳細データ Insert(コピー処理)
    function &getInsertCopyQuery($db, $row, $model)
    {
        $data = array();
        $data["YEAR"][TEXT]          = $model->year;
        $data["STAFFCD"][TEXT]       = $row["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]     = $row["STAFF_SEQ"];
        $data["FIELD1"][TEXT]        = $row["FIELD1"];
        $data["FIELD2"][TEXT]        = $row["FIELD2"];
        $data["FIELD3"][TEXT]        = $row["FIELD3"];
        $data["FIELD4"][TEXT]        = $row["FIELD4"];
        $data["FIELD5"][TEXT]        = $row["FIELD5"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        return;
    }

    //詳細データ Update(コピー処理)
    function &getUpdateCopyQuery($db, $row, $model)
    {
        $data = array();
        $data["FIELD1"][TEXT]        = $row["FIELD1"];
        $data["FIELD2"][TEXT]        = $row["FIELD2"];
        $data["FIELD3"][TEXT]        = $row["FIELD3"];
        $data["FIELD4"][TEXT]        = $row["FIELD4"];
        $data["FIELD5"][TEXT]        = $row["FIELD5"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        
        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' ";
        $where .= " AND STAFF_SEQ = '".$row["STAFF_SEQ"]."' ";
        $where .= " AND STAFFCD = '".$row["STAFFCD"]."' ";
        
        $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
        $db->query($query);

        return;
    }
}
?>
