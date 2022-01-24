<?php

require_once('for_php7.php');

class knjl090eQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタよりデータを取得(L062)
    function get_name_cdL062($year, $passCourse) {
        $query  = " SELECT DISTINCT ";
        $query .= "     N1.NAME1 || '-' || N1.NAME2 || '-' || N1.NAME3 || '：' ";
        $query .= "     || value(V1.COURSENAME, '') || '' || value(V1.MAJORNAME, '') || '　' || value(V2.COURSECODENAME, '') AS LABEL, ";
        $query .= "     N1.NAME1 || '-' || N1.NAME2 || '-' || N1.NAME3 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST N1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST V1 ON V1.YEAR     = N1.YEAR ";
        $query .= "                                    AND V1.COURSECD = N1.NAME1 ";
        $query .= "                                    AND V1.MAJORCD  = N1.NAME2 ";
        $query .= "     LEFT JOIN V_COURSECODE_MST V2 ON V2.YEAR       = N1.YEAR ";
        $query .= "                                  AND V2.COURSECODE = N1.NAME3 ";
        $query .= " WHERE ";
        $query .= "         N1.YEAR       = '{$year}' ";
        $query .= "     AND N1.NAMECD1    = 'L062' ";
        $query .= "     AND N1.NAMESPARE1 = '{$passCourse}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    function getNameMstList($year, $namecd1In) {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND NAMECD1 IN {$namecd1In} ";

        return $query;
    }

    //入試科目
    function getSubclass($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SEQ002.REMARK1 AS LOGIN_ID, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD as FINSCHOOLCD, ";
        $query .= "     BASE.FS_DAY, ";
        $query .= "     BASE.FS_GRDDIV, ";
        $query .= "     SEQ030.REMARK4 AS PASS_DIV, ";
        $query .= "     SEQ030.REMARK5 AS PASS_COURSE, ";
        $query .= "     SEQ007.REMARK1 || '-' || SEQ007.REMARK2 || '-' || SEQ007.REMARK3 AS ENT_COURSE, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     SEQ022.REMARK1 AS PROCEDUREDIV1, ";
        $query .= "     SEQ022.REMARK2 AS PROCEDUREDATE1, ";
        $query .= "     SEQ022.REMARK3 AS PAY_MONEY1, ";
        $query .= "     BASE.PROCEDUREDIV, ";
        $query .= "     BASE.PROCEDUREDATE, ";
        $query .= "     BASE.PAY_MONEY, ";
        $query .= "     BASE.ENTDIV, ";
        $query .= "     SEQ033.REMARK1 AS HOPE1, ";
        $query .= "     SEQ033.REMARK2 AS HOPE2, ";
        $query .= "     SEQ030.REMARK6 AS HOPE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ";
        $query .= "          ON SEQ002.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ002.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ002.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ002.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ007 ";
        $query .= "          ON SEQ007.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ007.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ007.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ007.SEQ          = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ";
        $query .= "          ON SEQ022.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ022.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ022.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ022.SEQ          = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ030 ";
        $query .= "          ON SEQ030.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ030.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ030.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ030.SEQ          = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND BASE.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND BASE.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "     AND BASE.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //併願状況取得
    function getHeiganData($model, $loginId) {
        $query  = " SELECT ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     SEQ030.REMARK4 AS PASS_DIV, ";
        $query .= "     SEQ030.REMARK5 AS PASS_COURSE, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     L013.NAMESPARE1 as PASS_FLG, ";
        $query .= "     SEQ007.REMARK1 || '-' || SEQ007.REMARK2 || '-' || SEQ007.REMARK3 AS ENT_COURSE, ";
        $query .= "     L004.NAMESPARE3, ";
        $query .= "     SEQ033.REMARK1 AS HOPE1, ";
        $query .= "     SEQ033.REMARK2 AS HOPE2, ";
        $query .= "     SEQ030.REMARK6 AS HOPE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ";
        $query .= "          ON SEQ002.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ002.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ002.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ002.SEQ          = '002' ";
        $query .= "         AND SEQ002.REMARK1      = '{$loginId}' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ007 ";
        $query .= "          ON SEQ007.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ007.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ007.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ007.SEQ          = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ030 ";
        $query .= "          ON SEQ030.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ030.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ030.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ030.SEQ          = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";
        $query .= "     LEFT JOIN V_NAME_MST L004 ";
        $query .= "          ON L004.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "         AND L004.NAMECD1 = 'L004' ";
        $query .= "         AND L004.NAMECD2 = BASE.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "         AND L013.NAMECD1 = 'L013' ";
        $query .= "         AND L013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //成績データ取得
    function getScoredata($model, $loginId) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SCORE.RECEPTNO, ";
        $query .= "     SCORE.TESTSUBCLASSCD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     RECPT.TOTAL2, ";
        $query .= "     RECPT.TOTAL3, ";
        $query .= "     RECPT.TOTAL_RANK3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT SCORE ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECPT ON RECPT.ENTEXAMYEAR  = SCORE.ENTEXAMYEAR ";
        $query .= "                                       AND RECPT.APPLICANTDIV = SCORE.APPLICANTDIV ";
        $query .= "                                       AND RECPT.TESTDIV      = SCORE.TESTDIV ";
        $query .= "                                       AND RECPT.EXAM_TYPE    = SCORE.EXAM_TYPE ";
        $query .= "                                       AND RECPT.RECEPTNO     = SCORE.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         SCORE.ENTEXAMYEAR    = '".$model->year."' ";
        $query .= "     AND SCORE.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SCORE.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND SCORE.RECEPTNO      IN ( ";
        $query .= "                                 SELECT ";
        $query .= "                                    EXAMNO ";
        $query .= "                                 FROM ";
        $query .= "                                    ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                       ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "                                   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "                                   AND SEQ          = '002' ";
        $query .= "                                   AND REMARK1      = '{$loginId}' ";
        $query .= "                                ) ";
        $query .= "     AND SCORE.SCORE         IS NOT NULL ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

/***** 追加更新処理 *****/
    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";

        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //併願毎に更新
        $shCnt = '1';
        $query = knjl090eQuery::getHeiganData($model, $model->field["LOGIN_ID"]);
        $result = $db->query($query);
        while ($shRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            if ($model->field["H_ENT_COURSE_".$shCnt] != '') {
                $data["PROCEDUREDIV"][TEXT]     = '1';
                $data["ENTDIV"][TEXT]           = '1';
            } else {
                $data["PROCEDUREDIV"][TEXT]     = '';
                $data["ENTDIV"][TEXT]           = '';
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO       = '".$shRow['EXAMNO']."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT
            //cnt
            $getCount007 = $db->getOne(knjl090eQuery::getCountDetail($model, $shRow['EXAMNO'], "007"));
            //where
            $getWhere007 = knjl090eQuery::getWhereDetail($model, $shRow['EXAMNO'], "007");

            list($courseCd, $majorCd, $courseCode) = explode('-', $model->field["H_ENT_COURSE_".$shCnt]); // 入学コース
            $data = array();
            $data["REMARK1"][TEXT]              = $courseCd;
            $data["REMARK2"][TEXT]              = $majorCd;
            $data["REMARK3"][TEXT]              = $courseCode;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if ($getCount007 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $shRow['EXAMNO'];
                $data["SEQ"][TEXT]              = "007";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere007);
            }
            $db->query($query);

            $shCnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //カウントDetail
    function getCountDetail($model, $examno, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //WHERE 条件セット Detail
    function getWhereDetail($model, $examno, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }
}
?>