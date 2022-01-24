<?php

require_once('for_php7.php');

class knje063pQuery extends Query {

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

    //指導要録データよりデータを取得
    function selectQuery($model, $view_key = array())
    {
        $query  = " WITH VIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,MAX(CASE WHEN T1.VIEWCD = '".$col."' THEN T1.STATUS ELSE NULL END) AS STATUS".$code;
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_SUB_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "        T1.YEAR = '".$model->editYear."' AND ";
        $query .= "        T1.SEMESTER = '9' AND ";
        $query .= "        T1.SCHREGNO = '".$model->schregno."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.CLASSCD || '-' || ";
            $query .= "   T1.SCHOOL_KIND || '-' || ";
            $query .= "   T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "        T1.SUBCLASSCD = '".$model->subclasscd."' AND ";
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T1.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        $query .= "        SUBSTR(T1.VIEWCD, 1, 2) = '".substr($model->field["CLASSCD"], 0, 2)."' ";
        $query .= "    GROUP BY ";
        $query .= "        T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T3.SUBCLASSCD as CHECKSUBCD,";
        $query .= "   T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
        $query .= "   T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
        $query .= "   T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
        $query .= "   T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M,";
        $query .= "   T4.REMARK ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col,2) != "99") {
                $query .= ",VIEWSTAT.STATUS".$code;
            }
        }
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
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
        $query .= "   LEFT JOIN VIEWSTAT ON VIEWSTAT.SCHREGNO = T1.SCHREGNO, ";
        $query .= "   CLASS_MST T2 ";
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
            $query .= "   T1.SUBCLASSCD   = '" .$model->subclasscd ."' ";
            $query .= "   AND ";
        }
        if ($model->cmd == "add_year" || $model->cmd == "class"){
            $query .= "   T1.YEAR   = '" .$model->field["YEAR"] ."' ";
            $query .= "   AND T1.ANNUAL   = '" .$model->field["ANNUAL"] ."' ";
            $query .= "   AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   T1.CLASSCD || '-' || ";
                $query .= "   T1.SCHOOL_KIND || '-' || ";
                $query .= "   T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "   T1.SUBCLASSCD   = '" .$model->field["CLASSCD"].$model->field["SUBCLASSCD"] ."' ";
            $query .= "   AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        if (is_array($model->sort)){
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
                        $query .= "T1.SUBCLASSCD";
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
        }else{
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

    //対応者を取得
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

    //編集可能項目の判別
    function selectViewcdQuery($model){
        $year = $model->field["YEAR"] ? $model->field["YEAR"] : $model->editYear;
        $subclassCd = $model->field["CLASSCD"].$model->field["SUBCLASSCD"] ? $model->field["CLASSCD"].$model->field["SUBCLASSCD"] : $model->subclasscd;
        $classCd = $model->field["CLASSCD"] ? $model->field["CLASSCD"] : $model->classcd;

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".$year."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclassCd."' ";
            $query .= "    AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        } else {
            $query .= "    AND T1.SUBCLASSCD = '".$subclassCd."' ";
            $query .= "    AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        }
        $query .= "    AND T1.VIEWCD = T2.VIEWCD ";
        $query .= "    AND SUBSTR(T1.VIEWCD,1,2) = '".substr($classCd, 0, 2)."' ";
        $query .= "    AND T2.SCHOOL_KIND IN(SELECT ";
        $query .= "                              IN1.SCHOOL_KIND ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_GDAT IN1, ";
        $query .= "                              (SELECT ";
        $query .= "                                   MAX(GRADE) AS GRADE ";
        $query .= "                               FROM ";
        $query .= "                                   SCHREG_REGD_DAT ";
        $query .= "                               WHERE ";
        $query .= "                                   YEAR = '".$year."' ";
        $query .= "                                   AND SCHREGNO = '".$model->schregno."' ";
        $query .= "                              ) IN2 ";
        $query .= "                          WHERE ";
        $query .= "                             IN1.YEAR = '".$year."' ";
        $query .= "                             AND IN1.GRADE = IN2.GRADE ";
        $query .= "                          ) ";
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //名称マスタ「D065」設定科目か？
    function getCntNameMstD065($model) {
        //教育課程対応
        $subClasscd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $cd = $model->field["CURRICULUM_CD"]."-".$model->field["CLASSCD"].$model->field["SUBCLASSCD"];
            $arr = array();
            $arr = explode("-", $cd);
            $subClasscd = $arr[1]."-".$arr[2]."-".$arr[0]."-".$arr[3];
        } else {
            $subClasscd = $model->field["CLASSCD"].$model->field["SUBCLASSCD"];
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D065' ";
        $query .= "     AND NAME1 = '".$subClasscd."' ";

        return $query;
    }

    //選択教科か？
    function getElectdivClassMst($model) {
        //教育課程対応
        $classcd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $cd = $model->field["CURRICULUM_CD"]."-".$model->field["CLASSCD"].$model->field["SUBCLASSCD"];
            $arr = array();
            $arr = explode("-", $cd);
            $classcd = $arr[1]."-".$arr[2];
        } else {
            $classcd = $model->field["CLASSCD"];
        }

        $query  = " SELECT ";
        $query .= "     VALUE(ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        } else {
            $query .= "     CLASSCD = '".$classcd."' ";
        }

        return $query;
    }

    //教科取得
    function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.CLASSCD AS VALUE, ";
        $query .= "     T2.CLASSCD || ' ' || T2.CLASSNAME AS LABEL, ";
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD || '-' || T2.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
        } else {
            $query .= "     AND T2.CLASSCD = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "             FROM ";
        $query .= "                 JVIEWNAME_SUB_MST T3, ";
        $query .= "                 SCHREG_REGD_GDAT T4, ";
        $query .= "                 (SELECT ";
        $query .= "                      MAX(GRADE) AS GRADE ";
        $query .= "                  FROM ";
        $query .= "                      SCHREG_REGD_DAT ";
        $query .= "                  WHERE ";
        $query .= "                      YEAR = '".$model->field["YEAR"]."' ";
        $query .= "                      AND SCHREGNO = '".$model->schregno."' ";
        $query .= "                 ) IN2 ";
        $query .= "             WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.CLASSCD = T3.CLASSCD AND ";
            $query .= "                 T1.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
        $query .= "                 T1.VIEWCD = T3.VIEWCD AND ";
        $query .= "                 T1.YEAR = T4.YEAR AND ";
        $query .= "                 T3.SCHOOL_KIND = T4.SCHOOL_KIND AND ";
        $query .= "                 T4.GRADE = IN2.GRADE ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // INSERT
    function &getInsertQuery($model, $fields, $grade_range, $i, $db)
    {
        $subclasscd = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        //教育課程対応
        $arr = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arr = explode("-", $subclasscd);
        }

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
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data, "schreg_studyrec_dat");

        return $query;
    }

    //更新
    function jViewUpdate($model, $db)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subClasscd = $model->field["CURRICULUM_CD"]."-".$model->field["CLASSCD"].$model->field["SUBCLASSCD"];
        } else {
            $subClasscd = $model->field["CLASSCD"].$model->field["SUBCLASSCD"];
        }
        //教育課程対応
        $curriculumCd = "";
        $arr = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CURRICULUM_CD || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || ";
            $arr = explode("-", $subClasscd);
        }

        //削除
        $query  = "DELETE FROM ";
        $query .= "    JVIEWSTAT_SUB_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "    SEMESTER = '9' AND ";
        $query .= "    SCHREGNO = '".$model->schregno."' AND ";
        $query .= "    {$curriculumCd}SUBCLASSCD = '".$subClasscd."' ";
        $db->query($query);
        foreach ($model->view_key as $code => $col) {

            //評定は更新しない
            if (substr($col,2) == "99") continue;

            //追加
            $data = array();
            $data["YEAR"][TEXT]         = $model->field["YEAR"];
            $data["SEMESTER"][TEXT]     = "9";
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]         = $arr[1];
                $data["SCHOOL_KIND"][TEXT]     = $arr[2];
                $data["CURRICULUM_CD"][TEXT]   = $arr[0];
                $data["SUBCLASSCD"][TEXT]      = $arr[3];
            } else {
                $data["SUBCLASSCD"][TEXT]      = $subClasscd;
            }
            $data["VIEWCD"][TEXT]       = $col;
            $data["STATUS"][TEXT]       = $model->field["STATUS".$code];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWSTAT_SUB_DAT");
            $db->query($query);
        }
    }

    // DELETE
    function &getDeleteQuery($model)
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
            $query .= "   YEAR   = '".$model->field["YEAR"]."' AND ";
            $query .= "   ANNUAL   = '" .sprintf("%02d", $model->field["ANNUAL"]) ."' AND ";
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

    function &getDeleteQueryJview($model, $db)
    {

        if ($model->cmd == "delete2") {
            foreach ($model->checked2 as $key => $val) {
                $delKey = explode(",", $val);
                $query  = "DELETE FROM ";
                $query .= "    JVIEWSTAT_SUB_DAT ";
                $query .= "WHERE ";
                $query .= "    YEAR     = '".$model->field["YEAR"]."' AND ";
                $query .= "    SEMESTER = '9' AND ";
                $query .= "    SCHREGNO = '".$model->schregno."' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " CLASSCD || '-' || ";
                    $query .= " SCHOOL_KIND || '-' || ";
                    $query .= " CURRICULUM_CD || '-' || ";
                }
                $query .= "    SUBCLASSCD = '".$delKey[3]."' ";
                $db->query($query);
            }
        } else if ($model->cmd == "delete") {
            foreach ($model->checked as $key => $val) {
                $delKey = explode(",", $val);
                $query  = "DELETE FROM ";
                $query .= "    JVIEWSTAT_SUB_DAT ";
                $query .= "WHERE ";
                $query .= "    YEAR     = '".$model->field["YEAR"]."' AND ";
                $query .= "    SEMESTER = '9' AND ";
                $query .= "    SCHREGNO = '".$model->schregno."' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " CLASSCD || '-' || ";
                    $query .= " SCHOOL_KIND || '-' || ";
                    $query .= " CURRICULUM_CD || '-' || ";
                }
                $query .= "    SUBCLASSCD = '".$delKey[2]."' ";
                $db->query($query);
            }
        }
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
