<?php

require_once('for_php7.php');

class knje063Query extends Query
{

    //ABC評定科目
    public function getABCHyoutei($fields)
    {
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
    public function getD001($hyoutei)
    {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D001' ";
        $query .= "     AND NAMECD2 = '{$hyoutei}' ";

        return $query;
    }

    public function getCurriculum()
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

    public function getGenkyuu()
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

    public function getSchoolMst()
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
    public function selectQuery($model, $sortDiv = "", $checkColorDiv = "")
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
        if ($model->Properties["knje063UseAnotherSubclassMst"] == '1') {
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANCL.CLASSNAME, T2.CLASSNAME) ELSE T2.CLASSNAME END AS CLASSNAME, ";
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANSUBCL.SUBCLASSCD, T3.SUBCLASSCD) ELSE T3.SUBCLASSCD END as CHECKSUBCD,";
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANSUBCL.SUBCLASSNAME, T3.SUBCLASSNAME) ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME_M,";
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANSUBCL.SUBCLASSABBV, T3.SUBCLASSABBV) ELSE T3.SUBCLASSABBV END AS SUBCLASSABBV_M,";
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANSUBCL.SUBCLASSNAME_ENG, T3.SUBCLASSNAME_ENG) ELSE T3.SUBCLASSNAME_ENG END AS SUBCLASSNAME_ENG_M,";
            $query .= "   CASE WHEN T1.SCHOOLCD = '1' THEN VALUE(ANSUBCL.SUBCLASSABBV_ENG, T3.SUBCLASSABBV_ENG) ELSE T3.SUBCLASSABBV_ENG END AS SUBCLASSABBV_ENG_M,";
        } else {
            $query .= "   T2.CLASSNAME, ";
            $query .= "   T3.SUBCLASSCD as CHECKSUBCD,";
            $query .= "   T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
            $query .= "   T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
            $query .= "   T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
            $query .= "   T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M,";
        }
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
        if ($model->Properties["knje063UseAnotherSubclassMst"] == '1') {
            $query .= "   LEFT JOIN ANOTHER_CLASS_MST ANCL ON T1.CLASSCD = ANCL.CLASSCD ";
            $query .= "                                   AND T1.SCHOOL_KIND = ANCL.SCHOOL_KIND ";
            $query .= "   LEFT JOIN ANOTHER_SUBCLASS_MST ANSUBCL ON T1.CLASSCD = ANSUBCL.CLASSCD ";
            $query .= "                                   AND T1.SCHOOL_KIND = ANSUBCL.SCHOOL_KIND ";
            $query .= "                                   AND T1.CURRICULUM_CD = ANSUBCL.CURRICULUM_CD ";
            $query .= "                                   AND T1.SUBCLASSCD = ANSUBCL.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        if ($model->cmd == "edit") {
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
        if ($sortDiv == "HENSYU") {
            $query .= " ORDER BY ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.YEAR ";
        } elseif (is_array($model->sort)) {
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order) {
                switch ($field) {
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

    public function getMaxRegdYear($model)
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
    public function selectEnt($schregno)
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
    public function selectSubclassQuery($year)
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
    public function selectSchoolMstQuery($year)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";

        return $query;
    }

    public function getStudyRecCnt($row, $schoolcd, $model)
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
    public function &getInsertQuery($model, $fields, $grade_range, $i, $db)
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
            } elseif ($com_credit == "" || $com_credit == 0) {
                    //「履修単位」欄がNULLあるいはゼロ  の場合
                    $data["COMP_CREDIT"][NUMBER]   = (int)$fields[$i]["GET_CREDIT"] + (int)$fields[$i]["ADD_CREDIT"];
            } else {
                if ($com_credit < ((int)$get_credit + (int)$add_credit)) {
                    //「履修単位」欄の値 <「修得単位」欄の値 ＋「増加単位」欄の値    の場合
                    $data["COMP_CREDIT"][NUMBER]   = (int)$fields[$i]["GET_CREDIT"] + (int)$fields[$i]["ADD_CREDIT"];
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
    public function &getInsertDetailQuery($model, $fields, $grade_range, $i, $db)
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
    public function &getInsertProvFlgQuery($model, $fields, $grade_range, $i, $db)
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
    public function &getDeleteQuery($model, $schoolcd = "")
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
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked2, "','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked2, "','") ."') AND ";
            }
        } elseif ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked, "','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked, "','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";

        return $query;
    }

    // DELETE
    public function &getDeleteDetailQuery($model, $schoolcd = "")
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
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked2, "','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked2, "','") ."') AND ";
            }
        } elseif ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked, "','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked, "','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND SEQ = '002' ";

        return $query;
    }

    // DELETE
    public function &getDeleteProvFlgQuery($model, $schoolcd = "")
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
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            }
        } elseif ($model->cmd == "delete") {
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
    public function &getInsertQueryRemark($model)
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
    public function &getDeleteQueryRemark($model)
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

    //履歴一覧・・・参考SQLはselectQuery()
    public function getListRireki($model)
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
        $query .= "   L1.STAFFNAME, ";
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
        $query .= "   RM1.REMARK AS REMARK1, ";
        $query .= "   RM2.REMARK AS REMARK2 ";
        $query .= " FROM ";
        $query .= "   CLASS_MST T2, ";
        $query .= "   SCHREG_STUDYREC_EXEC_HIST_DAT T1 ";
        $query .= "   LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.REGISTERCD ";
        $query .= "   LEFT JOIN REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "        AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN NOT_RYUNEN ON REGD.GRADE = NOT_RYUNEN.GRADE ";
        $query .= "        AND T1.SCHREGNO = NOT_RYUNEN.SCHREGNO ";
        $query .= "        AND T1.YEAR = NOT_RYUNEN.YEAR ";
        $query .= "   LEFT JOIN SCHREG_STUDYREC_DETAIL_EXEC_HIST_DAT DETAIL ";
        $query .= "                    ON T1.CALC_DATE = DETAIL.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = DETAIL.CALC_TIME ";
        $query .= "                   AND T1.BEF_AFT_DIV = DETAIL.BEF_AFT_DIV ";
        $query .= "                   AND T1.SCHOOLCD = DETAIL.SCHOOLCD ";
        $query .= "                   AND T1.YEAR = DETAIL.YEAR ";
        $query .= "                   AND T1.SCHREGNO = DETAIL.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = DETAIL.CLASSCD ";
        if($model->Properties["useSchool_KindField"] == "1"){
            $query .= "                   AND T1.SCHOOL_KIND = DETAIL.SCHOOL_KIND ";
        }
        $query .= "                   AND T1.CURRICULUM_CD = DETAIL.CURRICULUM_CD ";
        $query .= "                   AND T1.SUBCLASSCD = DETAIL.SUBCLASSCD ";
        $query .= "                   AND DETAIL.SEQ = '002' ";
        $query .= "   LEFT JOIN STUDYRECREMARK_EXEC_HIST_DAT RM1 ";
        $query .= "                    ON T1.CALC_DATE = RM1.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = RM1.CALC_TIME ";
        $query .= "                   AND RM1.BEF_AFT_DIV = '1' ";
        $query .= "                   AND T1.YEAR = RM1.YEAR ";
        $query .= "                   AND T1.SCHREGNO = RM1.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = RM1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = RM1.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = RM1.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = RM1.SUBCLASSCD ";
        $query .= "   LEFT JOIN STUDYRECREMARK_EXEC_HIST_DAT RM2 ";
        $query .= "                    ON T1.CALC_DATE = RM2.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = RM2.CALC_TIME ";
        $query .= "                   AND RM2.BEF_AFT_DIV = '2' ";
        $query .= "                   AND T1.YEAR = RM2.YEAR ";
        $query .= "                   AND T1.SCHREGNO = RM2.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = RM2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = RM2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = RM2.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = RM2.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD = T3.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   LEFT JOIN STUDYREC_PROV_FLG_EXEC_HIST_DAT T5 ";
            $query .= "                    ON T1.CALC_DATE = T5.CALC_DATE ";
            $query .= "                   AND T1.CALC_TIME = T5.CALC_TIME ";
            $query .= "                   AND T1.BEF_AFT_DIV = T5.BEF_AFT_DIV ";
            $query .= "                   AND T1.SCHOOLCD = T5.SCHOOLCD ";
            $query .= "                   AND T1.YEAR = T5.YEAR ";
            $query .= "                   AND T1.SCHREGNO = T5.SCHREGNO ";
            $query .= "                   AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "               AND T1.CLASSCD = T5.CLASSCD ";
                $query .= "               AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
                $query .= "               AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
            }
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.CALC_DATE DESC, ";
        $query .= "   T1.CALC_TIME DESC, ";
        $query .= "   T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.CLASSCD || '-' || ";
            $query .= "   T1.SCHOOL_KIND || '-' || ";
            $query .= "   T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SCHOOLCD "; ////データ種別

//        echo $query;
        return $query;
    }
    //(実行履歴)実行日付・時間を取得
    public function getCalcDateTime()
    {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //(実行履歴)変更前・変更後データ追加・・・SQL条件はgetDeleteQuery()と同じにすること
    public function &getInsertRireki($calcRow, $befAftDiv, $model, $schoolcd = "")
    {
        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }

        $query  = " INSERT INTO SCHREG_STUDYREC_EXEC_HIST_DAT( ";
        $query .= "     CALC_DATE, ";   //実行日付
        $query .= "     CALC_TIME, ";   //実行時間
        $query .= "     BEF_AFT_DIV, "; //1:変更前データ 2:変更後データ
        $query .= "     SCHOOLCD, ";    //データ種別
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     CLASSNAME, ";
        $query .= "     CLASSABBV, ";
        $query .= "     CLASSNAME_ENG, ";
        $query .= "     CLASSABBV_ENG, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     SUBCLASSABBV, ";
        $query .= "     SUBCLASSNAME_ENG, ";
        $query .= "     SUBCLASSABBV_ENG, ";
        $query .= "     VALUATION, ";
        $query .= "     GET_CREDIT, ";
        $query .= "     ADD_CREDIT, ";
        $query .= "     COMP_CREDIT, ";
        $query .= "     CMD_DIV, ";     //変更区分　1:更新ボタン 2:削除ボタン
        $query .= "     REGISTERCD, ";  //更新者
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$calcRow["CALC_DATE"]."' AS CALC_DATE, ";
        $query .= "     '".$calcRow["CALC_TIME"]."' AS CALC_TIME, ";
        if ($befAftDiv == "BEF") {
            $query .= "     '1' AS BEF_AFT_DIV, ";
        } elseif ($befAftDiv == "AFT") {
            $query .= "     '2' AS BEF_AFT_DIV, ";
        }
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     CLASSNAME, ";
        $query .= "     CLASSABBV, ";
        $query .= "     CLASSNAME_ENG, ";
        $query .= "     CLASSABBV_ENG, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     SUBCLASSABBV, ";
        $query .= "     SUBCLASSNAME_ENG, ";
        $query .= "     SUBCLASSABBV_ENG, ";
        $query .= "     VALUATION, ";
        $query .= "     GET_CREDIT, ";
        $query .= "     ADD_CREDIT, ";
        $query .= "     COMP_CREDIT, ";
        if ($model->cmd == "update") {
            $query .= "     '1' AS CMD_DIV, ";
        } elseif ($model->cmd == "delete2" || $model->cmd == "delete") {
            $query .= "     '2' AS CMD_DIV, ";
        }
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   ANNUAL = '" .sprintf("%02d", $model->field["ANNUAL"]) ."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked2, "','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked2, "','") ."') AND ";
            }
        } elseif ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($model->checked, "','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || ANNUAL || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($model->checked, "','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";

        return $query;
    }
    //(実行履歴)変更前・変更後データ追加・・・SQL条件はgetDeleteDetailQuery()と同じにすること
    public function &getInsertRirekiDetail($calcRow, $befAftDiv, $model, $schoolcd = "")
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

        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }

        $query  = " INSERT INTO SCHREG_STUDYREC_DETAIL_EXEC_HIST_DAT( ";
        $query .= "     CALC_DATE, ";   //実行日付
        $query .= "     CALC_TIME, ";   //実行時間
        $query .= "     BEF_AFT_DIV, "; //1:変更前データ 2:変更後データ
        $query .= "     SCHOOLCD, ";    //データ種別
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";     //有効フラグ
        $query .= "     REMARK2, ";
        $query .= "     CMD_DIV, ";     //変更区分　1:更新ボタン 2:削除ボタン
        $query .= "     REGISTERCD, ";  //更新者
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$calcRow["CALC_DATE"]."' AS CALC_DATE, ";
        $query .= "     '".$calcRow["CALC_TIME"]."' AS CALC_TIME, ";
        if ($befAftDiv == "BEF") {
            $query .= "     '1' AS BEF_AFT_DIV, ";
        } elseif ($befAftDiv == "AFT") {
            $query .= "     '2' AS BEF_AFT_DIV, ";
        }
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        if ($model->cmd == "update") {
            $query .= "     '1' AS CMD_DIV, ";
        } elseif ($model->cmd == "delete2" || $model->cmd == "delete") {
            $query .= "     '2' AS CMD_DIV, ";
        }
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked2, "','") ."') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked2, "','") ."') AND ";
            }
        } elseif ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .implode($checked, "','") ."') AND ";
            } else {
                $query .= "   YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .implode($checked, "','") ."') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND SEQ = '002' ";

        return $query;
    }
    //(実行履歴)変更前・変更後データ追加・・・SQL条件はgetDeleteProvFlgQuery()と同じにすること
    public function &getInsertRirekiProvFlg($calcRow, $befAftDiv, $model, $schoolcd = "")
    {
        $curriculumCd = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $curriculumCd = " CLASSCD || '-' || SCHOOL_KIND || '-' || ";
        }

        $query  = " INSERT INTO STUDYREC_PROV_FLG_EXEC_HIST_DAT( ";
        $query .= "     CALC_DATE, ";   //実行日付
        $query .= "     CALC_TIME, ";   //実行時間
        $query .= "     BEF_AFT_DIV, "; //1:変更前データ 2:変更後データ
        $query .= "     SCHOOLCD, ";    //データ種別
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     PROV_FLG, ";    //仮評定
        $query .= "     PROV_SEMESTER, ";
        $query .= "     PROV_TESTKINDCD, ";
        $query .= "     PROV_TESTITEMCD, ";
        $query .= "     PROV_SCORE_DIV, ";
        $query .= "     CMD_DIV, ";     //変更区分　1:更新ボタン 2:削除ボタン
        $query .= "     REGISTERCD, ";  //更新者
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$calcRow["CALC_DATE"]."' AS CALC_DATE, ";
        $query .= "     '".$calcRow["CALC_TIME"]."' AS CALC_TIME, ";
        if ($befAftDiv == "BEF") {
            $query .= "     '1' AS BEF_AFT_DIV, ";
        } elseif ($befAftDiv == "AFT") {
            $query .= "     '2' AS BEF_AFT_DIV, ";
        }
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     PROV_FLG, ";
        $query .= "     PROV_SEMESTER, ";
        $query .= "     PROV_TESTKINDCD, ";
        $query .= "     PROV_TESTITEMCD, ";
        $query .= "     PROV_SCORE_DIV, ";
        if ($model->cmd == "update") {
            $query .= "     '1' AS CMD_DIV, ";
        } elseif ($model->cmd == "delete2" || $model->cmd == "delete") {
            $query .= "     '2' AS CMD_DIV, ";
        }
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     STUDYREC_PROV_FLG_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update") {
            $query .= "   SCHOOLCD = '".$schoolcd."' AND ";
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   {$curriculumCd}SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."' AND ";
            }
        } elseif ($model->cmd == "delete2") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            } else {
                $query .= "   SCHOOLCD || ',' || YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .$model->checked2[0].",".$model->checked2[1].",".$model->checked2[3]. "') AND ";
            }
        } elseif ($model->cmd == "delete") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   YEAR || ',' || {$curriculumCd} CURRICULUM_CD || '-' || SUBCLASSCD IN ('" .$model->checked[0].",".$model->checked[2]. "') AND ";
            } else {
                $query .= "   YEAR || ',' || {$curriculumCd}SUBCLASSCD IN ('" .$model->checked[0].",".$model->checked[2]. "') AND ";
            }
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";

        return $query;
    }
    //(実行履歴)変更前・変更後データ追加・・・SQL条件はgetDeleteQueryRemark()と同じにすること
    public function &getInsertRirekiRemark($calcRow, $befAftDiv, $model)
    {
        $query  = " INSERT INTO STUDYRECREMARK_EXEC_HIST_DAT( ";
        $query .= "     CALC_DATE, ";   //実行日付
        $query .= "     CALC_TIME, ";   //実行時間
        $query .= "     BEF_AFT_DIV, "; //1:変更前データ 2:変更後データ
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     REMARK, ";      //備考
        $query .= "     CMD_DIV, ";     //変更区分　1:更新ボタン 2:削除ボタン
        $query .= "     REGISTERCD, ";  //更新者
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$calcRow["CALC_DATE"]."' AS CALC_DATE, ";
        $query .= "     '".$calcRow["CALC_TIME"]."' AS CALC_TIME, ";
        if ($befAftDiv == "BEF") {
            $query .= "     '1' AS BEF_AFT_DIV, ";
        } elseif ($befAftDiv == "AFT") {
            $query .= "     '2' AS BEF_AFT_DIV, ";
        }
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     REMARK, ";
        if ($model->cmd == "update") {
            $query .= "     '1' AS CMD_DIV, ";
        } elseif ($model->cmd == "delete2" || $model->cmd == "delete") {
            $query .= "     '2' AS CMD_DIV, ";
        }
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     STUDYRECREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CURRICULUM_CD || '-' || ";
            $query .= " CLASSCD || '-' || ";
            $query .= " SCHOOL_KIND || '-' || ";
            $query .= " SUBCLASSCD = '".$model->field["CURRICULUM_CD"]."-".$model->subclasscd."' ";
        } else {
            $query .= " SUBCLASSCD = '".$model->subclasscd."' ";
        }

        return $query;
    }

    //年度取得
    public function &selectQueryYear($model)
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
    public function SelectAnnualQuery($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year       = '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual    <> '".$model->field["ANNUAL"]."'";
        return $query;
    }
    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    public function SelectAnnualQuery2($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year      <> '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual     = '".$model->field["ANNUAL"]."'";
        return $query;
    }

    //レコードチェック
    public function recordCheckQuery($model)
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
    public function getOpinionsWk($year, $schregno)
    {
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
