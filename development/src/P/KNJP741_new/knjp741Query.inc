<?php

require_once('for_php7.php');

class knjp741Query extends Query {

    //ABC評定科目
    function getABCHyoutei($fields) {
        $henkanSub = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        list($curri, $classCd, $kind, $subclassCd) = preg_split("/-/", $henkanSub);
        $setSub = $classCd."-".$kind."-".$curri."-".$subclassCd;

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D065' ";
        $query .= "     AND NAME1 = '{$setSub}' ";


        return $query;
    }

    //評定
    function getD001($hyoutei) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D001' ";
        $query .= "     AND NAMECD2 = '{$hyoutei}' ";

        return $query;
    }

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

    function getGenkyuu()
    {
        $query  = " SELECT ";
        $query .= "   VALUE(REMARK6, '0') AS REMARK6 ";
        $query .= " FROM ";
        $query .= "   CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND CERTIF_KINDCD = '107' ";
        return $query;
    }

    function getSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "   VALUE(SCHOOLDIV, '0') AS SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //指導要録データよりデータを取得
    function selectQuery($model, $sortDiv = "", $checkColorDiv = "")
    {
        $query  = " WITH REGD AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(GRADE) AS GRADE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO ";
        $query .= " ), NOT_RYUNEN AS ( ";
        $query .= "   SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(YEAR) AS YEAR";
        $query .= "   FROM ";
        $query .= "     (SELECT DISTINCT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT ";
        $query .= "      WHERE ";
        $query .= "      SCHREGNO = '" .$model->schregno ."' ";
        $query .= "     ) T1 ";
        $query .= "   GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   REGD.GRADE, ";
        $query .= "   CASE WHEN NOT_RYUNEN.YEAR IS NULL THEN '1' END AS RYUNEN_FLG, ";
        $query .= "   DETAIL.REMARK1 AS YUUKOU_FLG, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T3.SUBCLASSCD as CHECKSUBCD,";
        $query .= "   T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
        $query .= "   T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
        $query .= "   T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
        $query .= "   T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M,";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   T5.PROV_FLG, ";
        }
        $query .= "   T4.REMARK ";
        $query .= " FROM ";
        $query .= "   CLASS_MST T2, ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= "   LEFT JOIN REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "        AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN NOT_RYUNEN ON REGD.GRADE = NOT_RYUNEN.GRADE ";
        $query .= "        AND T1.SCHREGNO = NOT_RYUNEN.SCHREGNO ";
        $query .= "        AND T1.YEAR = NOT_RYUNEN.YEAR ";
        $query .= "   LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT DETAIL ON T1.SCHOOLCD = DETAIL.SCHOOLCD ";
        $query .= "                   AND T1.YEAR = DETAIL.YEAR ";
        $query .= "                   AND T1.SCHREGNO = DETAIL.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = DETAIL.CLASSCD ";
        $query .= "                   AND T1.SCHOOL_KIND = DETAIL.SCHOOL_KIND ";
        $query .= "                   AND T1.CURRICULUM_CD = DETAIL.CURRICULUM_CD ";
        $query .= "                   AND T1.SUBCLASSCD = DETAIL.SUBCLASSCD ";
        $query .= "                   AND DETAIL.SEQ = '002' ";
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T4 ON T1.YEAR = T4.YEAR ";
        $query .= "                   AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = T4.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = T4.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD = T3.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   LEFT JOIN STUDYREC_PROV_FLG_DAT T5 ON T1.SCHOOLCD = T5.SCHOOLCD ";
            $query .= "                                     AND T1.YEAR     = T5.YEAR ";
            $query .= "                                     AND T1.SCHREGNO  = T5.SCHREGNO ";
            $query .= "                                     AND T1.SUBCLASSCD  = T5.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "               AND T1.CLASSCD = T5.CLASSCD ";
                $query .= "               AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
                $query .= "               AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
            }
        }
        $query .= " WHERE ";
        if ($model->cmd == "edit"){
            $query .= "   T1.YEAR   = '" .$model->editYear ."' ";
            $query .= "   AND T1.ANNUAL   = '" .$model->annual ."' ";
            $query .= "   AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   T1.CLASSCD || '-' || ";
                $query .= "   T1.SCHOOL_KIND || '-' || ";
                $query .= "   T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "       T1.SUBCLASSCD   = '" .$model->subclasscd ."' ";
            $query .= "   AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        if ($checkColorDiv == "CHECK_COLOR") {
            $query .= "   AND T1.SCHOOLCD <> '1' ";
        }
        if ($sortDiv == "HENSYU"){
            $query .= " ORDER BY ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.YEAR ";
        } else if (is_array($model->sort)) {
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "YEAR":    //年度順
                        $query .= " T1.YEAR";
                        $query .= ($order > 0)? " ASC, " : " DESC, ";
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $query .= "   T1.CLASSCD || '-' || ";
                            $query .= "   T1.SCHOOL_KIND || '-' || ";
                            $query .= "   T1.CURRICULUM_CD || '-' || ";
                        }
                        $query .= " T1.SUBCLASSCD";
                        break 2;
                    case "SUBCLASSCD":    //科目順
                        $query .= " T1.YEAR, ";
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $query .= "   T1.CLASSCD || '-' || ";
                            $query .= "   T1.SCHOOL_KIND || '-' || ";
                            $query .= "   T1.CURRICULUM_CD || '-' || ";
                        }
                        $query .= " T1.SUBCLASSCD";
                        $query .= ($order > 0)? " ASC " : " DESC ";
                        break 2;
                }
            }
        } else {
            $query .= "   T1.YEAR, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   T1.CLASSCD || '-' || ";
                $query .= "   T1.SCHOOL_KIND || '-' || ";
                $query .= "   T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "   T1.SUBCLASSCD ";
        }
        
        return $query;
    }

    function getMaxRegdYear($model)
    {
        $query  = " WITH REGD AS ( ";
        $query .= " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(GRADE) AS GRADE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$model->editYear ."' ";
        $query .= "   AND SCHREGNO = '" .$model->schregno ."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   MAX(YEAR) AS YEAR";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   REGD ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   AND T1.GRADE = REGD.GRADE ";
        return $query;
    }
    
    //対応者を取得
    function selectEnt($schregno)
    {
        $query = " SELECT ";
        $query .= "   NAME1 ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST ";
        $query .= "   LEFT JOIN NAME_MST ON NAMECD1 = 'A002' AND NAMECD2 = ENT_DIV";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$schregno."' ";
        $query .= "   AND ENT_DIV IN ('4','5') ";
        return $query;
    }
    
    
    //対応者を取得・・・未使用？
    function selectSubclassQuery($year)
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$year."' ";
        $query .= " ORDER BY ";
        $query .= "   SUBCLASSCD ";
        return $query;
    }

    //学校マスタ取得
    function selectSchoolMstQuery($year)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";

        return $query;
    }

    function getStudyRecCnt($row, $schoolcd, $model)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '{$schoolcd}' ";
        $query .= "   AND YEAR = '{$row["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$row["SCHREGNO"]}' ";
        $query .= "   AND ANNUAL = '{$row["ANNUAL"]}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclassCd = $row["CLASSCD"].$row["SUBCLASSCD"];
            $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclassCd}' ";
        } else {
            $query .= "   AND CLASSCD = '{$row["CLASSCD"]}' ";
            $subclassCd = $row["CLASSCD"].$row["SUBCLASSCD"];
            $query .= "   AND SUBCLASSCD = '{$subclassCd}' ";
        }
        return $query;
    }

    // INSERT
    function &getInsertQuery($model, $fields, $grade_range, $i, $db)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        } else {
            $subclasscd = $fields["CLASSCD"].$fields["SUBCLASSCD"];
        }
        //教育課程対応
        $arr = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arr = explode("-", $subclasscd);
        }
        if ($i != "2" || ($i == "2"  && ($fields[$i]["VALUATION"] != "" || $fields[$i]["GET_CREDIT"] != "" || $fields[$i]["ADD_CREDIT"] != "" || $fields[$i]["COMP_CREDIT"] != ""))) {

            $subclassname     = "";
            $subclassabbv     = "";
            $subclassname_eng = "";
            $subclassabbv_eng = "";
            $query = "SELECT COUNT(*) AS CNT FROM SUBCLASS_MST WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " CURRICULUM_CD || '-' || ";
                $query .= " CLASSCD || '-' || ";
                $query .= " SCHOOL_KIND || '-' || ";
            }
            $query .= "     SUBCLASSCD = '".$subclasscd."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["CNT"] == 0) {
                $subclassname     = $fields["SUBCLASSNAME"];
                $subclassabbv     = $fields["SUBCLASSABBV"];
                $subclassname_eng = $fields["SUBCLASSNAME_ENG"];
                $subclassabbv_eng = $fields["SUBCLASSABBV_ENG"];
            }

            $data["SCHOOLCD"][TEXT]            = $i;
            $data["SCHREGNO"][TEXT]            = $model->schregno;
            $data["YEAR"][TEXT]                = $fields["YEAR"];
            $data["ANNUAL"][TEXT]              = sprintf("%02d", $fields["ANNUAL"]);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]         = $arr[1];
                $data["SCHOOL_KIND"][TEXT]     = $arr[2];
                $data["CURRICULUM_CD"][TEXT]   = $arr[0];
                $data["SUBCLASSCD"][TEXT]      = $arr[3];
            } else {
                $data["CLASSCD"][TEXT]         = $fields["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]      = $subclasscd;
            }
            $data["CLASSNAME"][TEXT]           = $fields["CLASSNAME"];
            $data["CLASSABBV"][TEXT]           = $fields["CLASSABBV"];
            $data["CLASSNAME_ENG"][TEXT]       = $fields["CLASSNAME_ENG"];
            $data["CLASSABBV_ENG"][TEXT]       = $fields["CLASSABBV_ENG"];
            $data["SUBCLASSNAME"][TEXT]        = $subclassname;
            $data["SUBCLASSABBV"][TEXT]        = $subclassabbv;
            $data["SUBCLASSNAME_ENG"][TEXT]    = $subclassname_eng;
            $data["SUBCLASSABBV_ENG"][TEXT]    = $subclassabbv_eng;
            $data["VALUATION"][NUMBER]         = $fields[$i]["VALUATION"];
            $data["GET_CREDIT"][NUMBER]        = $fields[$i]["GET_CREDIT"];
            $data["ADD_CREDIT"][NUMBER]        = $fields[$i]["ADD_CREDIT"];
            $get_credit = $fields[$i]["GET_CREDIT"];
            $add_credit = $fields[$i]["ADD_CREDIT"];
            $com_credit = $fields[$i]["COMP_CREDIT"];
            if ($get_credit == "" && $add_credit == "") {
                    //修得単位も増加単位も、画面上でNULL  の場合
                    $data["COMP_CREDIT"][NUMBER]   = $fields[$i]["COMP_CREDIT"];
            } else if ($com_credit == "" || $com_credit == 0) {
                    //「履修単位」欄がNULLあるいはゼロ  の場合
                    $data["COMP_CREDIT"][NUMBER]   = $fields[$i]["GET_CREDIT"] + $fields[$i]["ADD_CREDIT"];
            } else {
                if ($com_credit < ($get_credit + $add_credit)) {
                    //「履修単位」欄の値 <「修得単位」欄の値 ＋「増加単位」欄の値    の場合
                    $data["COMP_CREDIT"][NUMBER]   = $fields[$i]["GET_CREDIT"] + $fields[$i]["ADD_CREDIT"];
                } else {
                    //「履修単位」欄の値 >=「修得単位」欄の値 ＋「増加単位」欄の値   の場合
                    $data["COMP_CREDIT"][NUMBER]   = $fields[$i]["COMP_CREDIT"];
                }
            }
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data, "schreg_studyrec_dat");
        }
        return $query;
    }

    // INSERT
    function &getInsertDetailQuery($model, $fields, $grade_range, $i, $db)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        } else {
            $subclasscd = $fields["CLASSCD"].$fields["SUBCLASSCD"];
        }
        //教育課程対応
        $arr = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arr = explode("-", $subclasscd);
        }
        if ($i != "2" || ($i == "2"  && ($fields[$i]["VALUATION"] != "" || $fields[$i]["GET_CREDIT"] != "" || $fields[$i]["ADD_CREDIT"] != "" || $fields[$i]["COMP_CREDIT"] != ""))) {

            $data["SCHOOLCD"][TEXT]            = $i;
            $data["YEAR"][TEXT]                = $fields["YEAR"];
            $data["SCHREGNO"][TEXT]            = $model->schregno;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]         = $arr[1];
                $data["SCHOOL_KIND"][TEXT]     = $arr[2];
                $data["CURRICULUM_CD"][TEXT]   = $arr[0];
                $data["SUBCLASSCD"][TEXT]      = $arr[3];
            } else {
                $data["CLASSCD"][TEXT]         = $fields["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]      = $subclasscd;
            }
            $data["SEQ"][TEXT]                 = "002";
            $data["REMARK1"][TEXT]             = $fields["YUUKOU_FLG"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_STUDYREC_DETAIL_DAT");
        }
        return $query;
    }

    // INSERT(STUDYREC_PROV_FLG_DAT)
    function &getInsertProvFlgQuery($model, $fields, $grade_range, $i, $db)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        } else {
            $subclasscd = $fields["CLASSCD"].$fields["SUBCLASSCD"];
        }
        //教育課程対応
        $arr = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arr = explode("-", $subclasscd);
        }
        if ($i != "2" || ($i == "2"  && ($fields[$i]["VALUATION"] != "" || $fields[$i]["GET_CREDIT"] != "" || $fields[$i]["ADD_CREDIT"] != "" || $fields[$i]["COMP_CREDIT"] != ""))) {
            //SCHREG_PROV_FLG_DATの更新
            $data2["SCHOOLCD"][TEXT]            = $i;
            $data2["SCHREGNO"][TEXT]            = $model->schregno;
            $data2["YEAR"][TEXT]                = $fields["YEAR"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data2["CLASSCD"][TEXT]         = $arr[1];
                $data2["SCHOOL_KIND"][TEXT]     = $arr[2];
                $data2["CURRICULUM_CD"][TEXT]   = $arr[0];
                $data2["SUBCLASSCD"][TEXT]      = $arr[3];
            } else {
                $data2["CLASSCD"][TEXT]         = $fields["CLASSCD"];
                $data2["SUBCLASSCD"][TEXT]      = $subclasscd;
            }
            $data2["PROV_FLG"][TEXT]         = $fields[$i]["PROV_FLG"];

            $query = Query::insertSQL($data2, "STUDYREC_PROV_FLG_DAT");
        }
        return $query;
    }

    // DELETE
    function &getDeleteQuery($model, $schoolcd = "")
    {
        //教育課程対応
        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }
        $query = " DELETE FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   ANNUAL = '" .sprintf("%02d", $model->field["ANNUAL"]) ."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        }else if ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked2,"','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked2,"','") ."') AND ";
            }
        }else if ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked,"','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked,"','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";

        return $query;
    }

    // DELETE
    function &getDeleteDetailQuery($model, $schoolcd = "")
    {

        if ($model->cmd == "delete") {
            foreach ($model->checked as $key => $val) {
                $checkedArray = preg_split("/,/", $val);
                $sep = "";
                for ($check = 0; $check < get_count($checkedArray); $check++) {
                    if ($check != 1) {
                        $checked[$key] .= $sep.$checkedArray[$check];
                        $sep = ",";
                    }
                }
            }
        }

        if ($model->cmd == "delete2") {
            foreach ($model->checked2 as $key => $val) {
                $checked2Array = preg_split("/,/", $val);
                $sep = "";
                for ($check = 0; $check < get_count($checked2Array); $check++) {
                    if ($check != 2) {
                        $checked2[$key] .= $sep.$checked2Array[$check];
                        $sep = ",";
                    }
                }
            }
        }

        //教育課程対応
        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }
        $query = " DELETE FROM ";
        $query .= "   SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        }else if ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked2,"','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked2,"','") ."') AND ";
            }
        }else if ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked,"','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked,"','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND SEQ = '002' ";

        return $query;
    }

    // DELETE
    function &getDeleteProvFlgQuery($model, $schoolcd = "")
    {
        //教育課程対応
        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }
        $query = " DELETE FROM ";
        $query .= "   STUDYREC_PROV_FLG_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        }else if ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            }
        }else if ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .$model->checked[0].",".$model->checked[2]. "') AND ";
            } else {
                $query .= "   YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .$model->checked[0].",".$model->checked[2]. "') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        return $query;
    }
    
    //備考登録
    function &getInsertQueryRemark($model)
    {
        $query = " INSERT INTO STUDYRECREMARK_DAT( ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " SCHOOL_KIND, ";
            $query .= " CURRICULUM_CD, ";
        }
        $query .= " SUBCLASSCD, ";
        $query .= " REMARK, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= "  ( ";
        $query .= " '".$model->field["YEAR"]."', ";
        $query .= " '".$model->schregno."', ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arr = explode("-", $model->subclasscd);
            $query .= " '".$arr[0]."', ";//CLASSCD
            $query .= " '".$arr[1]."', ";//SCHOOL_KIND
            $query .= " '".$model->field["CURRICULUM_CD"]."', ";//CURRICULUM_CD
            $query .= " '".$arr[2]."', ";//SUBCLASSCD
        } else {
            $query .= " '".$model->field["CLASSCD"]."', ";
            $query .= " '".$model->subclasscd."', ";
        }
        $query .= " '".Query::addquote($model->field["REMARK"])."', ";
        $query .= " '".STAFFCD."' ";
        $query .= " ) ";

        return $query;
    }
    function &getDeleteQueryRemark($model)
    {
        $query = " DELETE FROM ";
        $query .= "   STUDYRECREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "   SCHREGNO = '".$model->schregno."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CURRICULUM_CD || '-' || ";
            $query .= " CLASSCD || '-' || ";
            $query .= " SCHOOL_KIND || '-' || ";
            $query .= "   SUBCLASSCD = '".$model->field["CURRICULUM_CD"]."-".$model->subclasscd."' ";
        } else {
            $query .= "   SUBCLASSCD = '".$model->subclasscd."' ";
        }
        return $query;
    }
    //年度取得
    function &selectQueryYear($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "   YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '".$model->schregno."' ";
        return $query;
    }
    
    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    function SelectAnnualQuery($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year       = '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual    <> '".$model->field["ANNUAL"]."'";
        return $query;
    }
    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    function SelectAnnualQuery2($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year      <> '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual     = '".$model->field["ANNUAL"]."'";
        return $query;
    }

    //レコードチェック
    function recordCheckQuery($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat ";
        $query .= "  WHERE ";
        $query .= "        YEAR       = '".$model->field["YEAR"]."'";
        $query .= "    AND SCHREGNO   = '".$model->schregno."'";
        $query .= "    AND ANNUAL     = '".$model->field["ANNUAL"]."'";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    AND CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."'";
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".$model->field["CLASSCD"].$model->field["SUBCLASSCD"]."'";
        } else {
            $query .= "    AND CLASSCD    = '".$model->field["CLASSCD"]."'";
            $query .= "    AND SUBCLASSCD = '".$model->field["CLASSCD"].$model->field["SUBCLASSCD"]."'";
        }
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
}
?>
