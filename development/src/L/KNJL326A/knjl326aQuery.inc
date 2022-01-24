<?php

require_once('for_php7.php');

class knjl326aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待区分取得
    public function getEntexamHonordivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     HONORDIV AS VALUE, ";
        $query .= "     HONORDIV || ':' || HONORDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号範囲
    public function getReceptnoFromTo($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(RCPT.RECEPTNO) AS RECEPTNO_FROM, ";
        $query .= "     MAX(RCPT.RECEPTNO) AS RECEPTNO_TO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN V_NAME_MST L013_1 ";
        $query .= "          ON L013_1.YEAR     = RD006.ENTEXAMYEAR ";
        $query .= "         AND L013_1.NAMECD1  = 'L{$model->schoolkind}13' ";
        $query .= "         AND L013_1.NAMECD2  = RD006.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L013_2 ";
        $query .= "          ON L013_2.YEAR     = RD006.ENTEXAMYEAR ";
        $query .= "         AND L013_2.NAMECD1  = 'L{$model->schoolkind}13' ";
        $query .= "         AND L013_2.NAMECD2  = RD006.REMARK9 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '1' ";
        if ($model->shdiv != "ALL") {
            $query .= "     AND RD006.REMARK1       = '{$model->shdiv}' ";
        }
        if ($model->wish_course != "ALL") {
            $query .= "     AND RD006.REMARK2       = '{$model->wish_course}' ";
        }
        if ($model->noticeType == '2') {
            $query .= "     AND (RD006.REMARK8 = '0' OR RD006.REMARK9 = '0') ";
        } else {
            $query .= "     AND (L013_1.NAMESPARE1 = '1' OR L013_2.NAMESPARE1 = '1') ";
        }

        return $query;
    }

    //入学コース選択コンボ（専願合格コース、併願合格コース）
    public function getPassCourse($model)
    {
        //専願合格コース
        $query  = " SELECT ";
        $query .= "     '1' AS PASS_SHDIV, ";
        $query .= "     '1' || '-' || RD006.REMARK8 AS VALUE, ";
        $query .= "     '(専願合格)' || ':' || L013.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR           = RD006.ENTEXAMYEAR ";
        $query .= "         AND L013.NAMECD1        = 'L{$model->schoolkind}13' ";
        $query .= "         AND L013.NAMECD2        = RD006.REMARK8 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '1' ";
        $query .= "     AND RCPT.RECEPTNO       = '{$model->receptnoFrom}' ";
        if ($model->shdiv != "ALL") {
            $query .= "     AND RD006.REMARK1       = '{$model->shdiv}' ";
        }
        if ($model->wish_course != "ALL") {
            $query .= "     AND RD006.REMARK2       = '{$model->wish_course}' ";
        }
        $query .= "     AND L013.NAMESPARE1     = '1' ";
        //併願合格コース
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '2' AS PASS_SHDIV, ";
        $query .= "     '2' || '-' || RD006.REMARK9 AS VALUE, ";
        $query .= "     '(併願合格)' || ':' || L013.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR           = RD006.ENTEXAMYEAR ";
        $query .= "         AND L013.NAMECD1        = 'L{$model->schoolkind}13' ";
        $query .= "         AND L013.NAMECD2        = RD006.REMARK9 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '1' ";
        $query .= "     AND RCPT.RECEPTNO       = '{$model->receptnoFrom}' ";
        if ($model->shdiv != "ALL") {
            $query .= "     AND RD006.REMARK1       = '{$model->shdiv}' ";
        }
        if ($model->wish_course != "ALL") {
            $query .= "     AND RD006.REMARK2       = '{$model->wish_course}' ";
        }
        $query .= "     AND L013.NAMESPARE1     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     PASS_SHDIV ";

        return $query;
    }

    //入学コース登録リスト
    public function getPassCourseList($model)
    {
        if ($model->shdiv == '2') {
            list($passShdiv, $passCourse) = explode("-", $model->passCourse);
        } else {
            $passShdiv = "1";
            $passCourse = "";
        }

        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     '{$passShdiv}' AS PASS_SHDIV, ";
        if ($passShdiv == '2') {
            $query .= "     RD006.REMARK9 AS PASS_COURSE, ";
        } else {
            $query .= "     RD006.REMARK8 AS PASS_COURSE, ";
        }
        $query .= "     BD007.EXAMNO AS BD007_EXAMNO, ";
        $query .= "     BD030.EXAMNO AS BD030_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD007 ";
        $query .= "          ON BD007.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD007.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND BD007.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND BD007.SEQ           = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD030 ";
        $query .= "          ON BD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND BD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND BD030.SEQ           = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "          ON L013.YEAR           = RD006.ENTEXAMYEAR ";
        $query .= "         AND L013.NAMECD1        = 'L{$model->schoolkind}13' ";
        if ($passShdiv == '2') {
            $query .= "         AND L013.NAMECD2        = RD006.REMARK9 ";
        } else {
            $query .= "         AND L013.NAMECD2        = RD006.REMARK8 ";
        }
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->examyear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '1' ";
        if (strlen($model->receptnoFrom) && strlen($model->receptnoTo)) {
            $query .= "     AND RCPT.RECEPTNO BETWEEN '{$model->receptnoFrom}' AND '{$model->receptnoTo}' ";
        } elseif (strlen($model->receptnoFrom)) {
            $query .= "     AND RCPT.RECEPTNO = '{$model->receptnoFrom}' ";
        } elseif (strlen($model->receptnoTo)) {
            $query .= "     AND RCPT.RECEPTNO = '{$model->receptnoTo}' ";
        }
        if ($model->shdiv == '2') {
            $query .= "     AND RCPT.RECEPTNO = '{$model->receptnoFrom}' ";
            if ($passShdiv == '2') {
                $query .= "     AND RD006.REMARK9 = '{$passCourse}' ";
            } else {
                $query .= "     AND RD006.REMARK8 = '{$passCourse}' ";
            }
        }
        if ($model->shdiv != "ALL") {
            $query .= "     AND RD006.REMARK1       = '{$model->shdiv}' ";
        }
        if ($model->wish_course != "ALL") {
            $query .= "     AND RD006.REMARK2       = '{$model->wish_course}' ";
        }
        $query .= "     AND L013.NAMESPARE1     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //課程、学科、コース
        $courseArray = array();
        $result = $db->query(knjl326aQuery::getNameMst($model->examyear, 'L'.$model->schoolkind.'12'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseArray[$row["VALUE"]] = array($row["ABBV3"], $row["NAMESPARE1"], $row["NAMESPARE2"]);
        }
        $result->free();

        //入学コース登録リスト
        $examnoArray = array();
        $result = $db->query(knjl326aQuery::getPassCourseList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examnoArray[] = $row;
        }
        $result->free();

        //更新
        foreach ($examnoArray as $key => $row) {
            $examno = $row["EXAMNO"];
    
            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]       = $row["PASS_COURSE"]; //入学コース
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
            $where  = " WHERE ENTEXAMYEAR   = '".$model->examyear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $array = array();
            $array["007"] = $courseArray[$row["PASS_COURSE"]]; //課程、学科、コース
            $array["030"] = array($row["TESTDIV"], $row["RECEPTNO"], $row["PASS_SHDIV"]); //合格試験区分、合格受験番号、合格専併区分
            foreach ($array as $seq => $val) {
                $data = array();
                $data["REMARK1"][TEXT]      = $val[0];
                $data["REMARK2"][TEXT]      = $val[1];
                $data["REMARK3"][TEXT]      = $val[2];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";
                if (strlen($row["BD{$seq}_EXAMNO"])) {
                    $where  = " WHERE ENTEXAMYEAR   = '".$model->examyear."' ";
                    $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                    $where .= "   AND EXAMNO        = '".$examno."' ";
                    $where .= "   AND SEQ           = '".$seq."' ";
                    $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
                } else {
                    $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
                    $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                    $data["EXAMNO"][TEXT]       = $examno;
                    $data["SEQ"][TEXT]          = $seq;
                    $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
