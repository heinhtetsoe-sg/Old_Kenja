<?php

require_once('for_php7.php');

class knjl571aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namespare="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namespare) {
            $query .= " AND NAMESPARE".$namespare." = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分の取得
    function getHopeCourse($year) {
        $query  = " SELECT ";
        $query .= "   T1.HOPE_COURSECODE AS VALUE, ";                       // 志望区分コード
        $query .= "   T1.HOPE_COURSECODE || ':' || T2.HOPE_NAME AS LABEL "; // 志望区分名称
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ";
        $query .= "     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
        

        return $query;
    }

    //合否の取得
    function getHopeCoursePass($year) {
        $query  = " SELECT DISTINCT";
        $query .= "   T1.HOPE_COURSECODE AS VALUE, ";
        $query .= "   T1.PASS_NAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST  T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //一覧表示
    function SelectQuery($model) {
        $sortDiv = $model->sort[$model->getSort] == 1 ? "" : " DESC ";

        $query  = " WITH ENTEXAM_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAM_SCHOOLCD, ";
        $query .= "         T2.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCHOOL_MST T1 ";
        $query .= "         INNER JOIN  FINSCHOOL_MST T2 ";
        $query .= "                ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= " ), HOPE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.HOPE_COURSECODE, ";
        $query .= "   T1.HOPE_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_MST T1 ";
        $query .= "   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ";
        $query .= "           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= "          AND T2.ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     R1.TOTAL4 AS POINT_TOTAL, ";
        $query .= "     AC1.TOTAL5 AS NAISIN5_TOTAL, ";
        $query .= "     AC1.TOTAL_ALL AS NAISIN9_TOTAL, ";
        $query .= "     H1.HOPE_NAME AS HOPE_COURSE, ";
        $query .= "     BASEDTL01.REMARK10, ";
        $query .= "     B1.SUC_COURSECODE AS PASS_UNPASS, ";
        $query .= "      CASE WHEN B1.SUC_COURSECODE IS NOT NULL THEN ";
        $query .= "          CASE WHEN BASEDTL01.REMARK10 =  B1.SUC_COURSECODE THEN L013.NAME1 ";
        $query .= "               WHEN BASEDTL01.REMARK10 <> B1.SUC_COURSECODE THEN HOPEMST_SUC.NOT_PASS_NAME ";
        $query .= "          ELSE '' END ";
        $query .= "     ELSE L013.NAME1 ";
        $query .= "     END AS PU_LABEL, ";
        $query .= "     D1.REMARK1 AS SCHOLARSHIP_STUDENT, ";
        $query .= "     D1.REMARK2 AS SP_SOLICIT, ";
        $query .= "     CASE WHEN D2.REMARK8 = '1' THEN '○' ELSE '' END AS PROMISED, ";
        $query .= "     B1.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "             ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "            AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "            AND B1.TESTDIV      = R1.TESTDIV ";
        $query .= "            AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "             ON BASEDTL01.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND BASEDTL01.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND BASEDTL01.EXAMNO       = B1.EXAMNO ";
        $query .= "            AND BASEDTL01.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ";
        $query .= "             ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_REAL ";
        $query .= "             ON HOPEMST_REAL.HOPE_COURSECODE = B1.SUC_COURSECODE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT AC1 ";
        $query .= "             ON AC1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND AC1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND AC1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN HOPE H1 ";
        $query .= "          ON H1.HOPE_COURSECODE  = BASEDTL01.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ";
        $query .= "          ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND D1.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D2 ";
        $query .= "          ON D2.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND D2.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND D2.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND D2.SEQ          = '004' ";
        $query .= "     LEFT JOIN NAME_MST L013 ";
        $query .= "          ON L013.NAMECD1 = 'L013' ";
        $query .= "         AND L013.NAMECD2 = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND R1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        if ($model->field["SIBORI"] == "1") {
            //得点絞り込み
            if(strlen($model->field["POINT_FROM"]) || strlen($model->field["POINT_TO"])){
                $from = (strlen($model->field["POINT_FROM"])) ? $model->field["POINT_FROM"] : "0";
                $to   = (strlen($model->field["POINT_TO"]))   ? $model->field["POINT_TO"]   : "999999999";
                $query .= "     AND R1.TOTAL4 BETWEEN '".$from."' ";
                $query .= "                       AND '".$to."' ";
            }
        } else if ($model->field["SIBORI"] == "2") {
            //内申絞り込み
            if ($model->field["SEL_TOTAL"] == "1") {
                //5科合計
                if(strlen($model->field["TOTAL5_FROM"]) || strlen($model->field["TOTAL5_TO"])){
                    $from = (strlen($model->field["TOTAL5_FROM"])) ? $model->field["TOTAL5_FROM"] : "0";
                    $to   = (strlen($model->field["TOTAL5_TO"]))   ? $model->field["TOTAL5_TO"]   : "999999999";
                    $query .= "     AND AC1.TOTAL5 BETWEEN '".$from."' ";
                    $query .= "                       AND '".$to."' ";
                }
            } else if ($model->field["SEL_TOTAL"] == "2") {
                //9科合計
                if(strlen($model->field["TOTAL9_FROM"]) || strlen($model->field["TOTAL9_TO"])){
                    $from = (strlen($model->field["TOTAL9_FROM"])) ? $model->field["TOTAL9_FROM"] : "0";
                    $to   = (strlen($model->field["TOTAL9_TO"]))   ? $model->field["TOTAL9_TO"]   : "999999999";
                    $query .= "     AND AC1.TOTAL_ALL BETWEEN '".$from."' ";
                    $query .= "                           AND '".$to."' ";
                }
            }
        }
        if (($model->field["SIBORI"] == "1" && $model->field["IGNORE_GOUHI_LEFT"] == "1" ) || ($model->field["SIBORI"] == "2" && $model->field["IGNORE_GOUHI_RIGHT"] == "1" )) {
            $query .= "     AND B1.JUDGEMENT IS NULL ";
        }
        if (strlen($model->field["DESIREDIV"])) {
            $query .= "     AND BASEDTL01.REMARK10     = '".$model->field["DESIREDIV"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->getSort == "SRT_POINT") {
            if ($model->sort[$model->getSort] == 1) {
                $query .= "     POINT_TOTAL, ";
            } else {
                $query .= "     CASE WHEN POINT_TOTAL IS NULL THEN 0 ELSE POINT_TOTAL END DESC, ";
            }
        }
        if ($model->getSort == "SRT_NAISIN5") {
            if ($model->sort[$model->getSort] == 1) {
                $query .= "     NAISIN5_TOTAL, ";
            } else {
                $query .= "     CASE WHEN NAISIN5_TOTAL IS NULL THEN 0 ELSE NAISIN5_TOTAL END DESC, ";
            }
        }
        if ($model->getSort == "SRT_NAISIN9") {
            if ($model->sort[$model->getSort] == 1) {
                $query .= "     NAISIN9_TOTAL, ";
            } else {
                $query .= "     CASE WHEN NAISIN9_TOTAL IS NULL THEN 0 ELSE NAISIN9_TOTAL END DESC, ";
            }
        }
        if ($model->getSort == "SRT_PROMISED") {
            $query .= "     CASE WHEN PROMISED = '' THEN 0 ELSE 1 END{$sortDiv}, ";
        }
        $query .= "     EXAMNO ";
        $query .= " FETCH FIRST 50 ROWS ONLY ";

        return $query;
    }

    function getPULabel($model, $examno, $selval) {
        $convval = $model->data["PASS_UNPASS"][$examno] != "UNPASS" ? $selval : "";
        $jdgval = $model->data["PASS_UNPASS"][$examno] == "" || $model->data["PASS_UNPASS"][$examno] == "UNPASS" ? "2" : "1";

        $query = " SELECT ";
        $query .= "     CASE WHEN '".$convval."' <> '' THEN ";
        $query .= "               CASE WHEN BASEDTL01.REMARK10 =  '".$convval."' THEN L013.NAME1 ";
        $query .= "                    WHEN BASEDTL01.REMARK10 <> '".$convval."' THEN HOPEMST_SUC.NOT_PASS_NAME ";
        $query .= "                    ELSE '' END ";
        $query .= "          ELSE L013.NAME1 ";
        $query .= "          END AS PU_LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "             ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "            AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "            AND B1.TESTDIV      = R1.TESTDIV ";
        $query .= "            AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ";
        $query .= "             ON BASEDTL01.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND BASEDTL01.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND BASEDTL01.EXAMNO       = B1.EXAMNO ";
        $query .= "            AND BASEDTL01.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ";
        $query .= "             ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT AC1 ";
        $query .= "             ON AC1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND AC1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND AC1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L013 ";
        $query .= "          ON L013.NAMECD1 = 'L013' ";
        $query .= "         AND L013.NAMECD2 = '".$jdgval."' ";
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND R1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        $query .= "     AND R1.EXAMNO       = '".$examno."' ";

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
    function checkEntexamApplicantbaseDetailDat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     SEQ             = '".$seq."' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $D030      = $db->getCol(knjl571aQuery::checkEntexamApplicantbaseDetailDat($model, "030"));
        $D007      = $db->getCol(knjl571aQuery::checkEntexamApplicantbaseDetailDat($model, "007"));

        //更新
        $array = explode(",", $model->hid_examno);
        foreach ($array as $key => $examno) {
            if($model->data["CHECKED"][$examno] == "1"){
                // ENTEXAM_APPLICANTBASE_DAT
                $data = array();
                $sucCoursecode = !strlen($model->data["PASS_UNPASS"][$examno]) ? "" : $model->data["PASS_UNPASS"][$examno];
                $sucCoursecd = "";
                $sucMajorcd  = "";
                $judgement   = "";
                if($sucCoursecode != ""){
                    $sucCoursecd   = ($sucCoursecode == "UNPASS") ? "" : "1";
                    $sucMajorcd    = ($sucCoursecode == "UNPASS") ? "" : "101";
                    $judgement     = ($sucCoursecode == "UNPASS") ? "2" : "1"; //1:合格 2:不合格
                    $sucCoursecode = ($sucCoursecode == "UNPASS") ? "" : $sucCoursecode;
                }

                $data["SUC_COURSECD"][TEXT]     = $sucCoursecd;
                $data["SUC_MAJORCD"][TEXT]      = $sucMajorcd;
                $data["SUC_COURSECODE"][TEXT]   = $sucCoursecode;
                $data["JUDGEMENT"][TEXT]        = $judgement;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                $where .= "     EXAMNO          = '".$examno."' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);


                // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:030
                $data = array();
                if (!in_array($examno, $D030)) {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                    $data["EXAMNO"][TEXT]           = $examno;
                    $data["SEQ"][TEXT]              = '030';
                }
                $data["REMARK1"][TEXT]          = !strlen($model->data["SCHOLARSHIP_STUDENT"][$examno]) ? "" : $model->data["SCHOLARSHIP_STUDENT"][$examno];
                $data["REMARK2"][TEXT]          = !strlen($model->data["SP_SOLICIT"][$examno]) ? "" : $model->data["SP_SOLICIT"][$examno];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                if (!in_array($examno, $D030)) {
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                    $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                    $where .= "     EXAMNO          = '".$examno."' AND ";
                    $where .= "     SEQ             = '030' ";

                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);


                // KNJL140B用をここでセット
                // 合格コースコードで入試用コースマスタを参照して学籍コースコードを取得しセット
                // ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ:007
                $data = array();
                if (!in_array($examno, $D007)) {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                    $data["EXAMNO"][TEXT]           = $examno;
                    $data["SEQ"][TEXT]              = '007';
                }
                $data["REMARK1"][TEXT]          = $sucCoursecd;
                $data["REMARK2"][TEXT]          = $sucMajorcd;
                $data["REMARK3"][TEXT]          = !strlen($sucCoursecode) ? "" : $db->getOne(" SELECT COURSECODE FROM ENTEXAM_HOPE_COURSE_MST WHERE HOPE_COURSECODE = '{$sucCoursecode}' ");
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                if (!in_array($examno, $D007)) {
                    //追加
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                    $where .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
                    $where .= "     EXAMNO          = '".$examno."' AND ";
                    $where .= "     SEQ             = '007' ";

                    //更新
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>