<?php

require_once('for_php7.php');

class knjl010fQuery extends Query
{


    //MAX管理番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $setTop = "";
        //1:帰国生入試 2:一般入試
        $setTop = ($model->field["TEST_L_DIV"] == "1") ? "1" : "2";

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";//1:中学
//        $query .= "     AND TESTDIV = '".$model->field["TEST_L_DIV"]."' ";
//        $query .= "     AND EXAMNO LIKE '{$setTop}%' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
//            $maxExam = $setTop.substr($maxExam, 1, 4);
            $maxExam++;
        } else {
//            $maxExam = $setTop."0001";
            $maxExam = "00001";
        }

        Query::dbCheckIn($db);
        return sprintf("%05d", $maxExam);
    }

    //入試区分配列取得
    public function getTestdivArray($model)
    {
        $db = Query::dbCheckOut();

        $testdivArray = array();
        $query = knjl010fQuery::getNameCd($model->year, "L024");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = array("TESTDIV" => $row["NAMECD2"], "TESTDIV_NAME" => $row["NAME1"], "TESTDIV_DATE" => $row["NAMESPARE1"], "KIKOKU_FLG" => $row["ABBV2"]);
        }
        $result->free();

        Query::dbCheckIn($db);
        return $testdivArray;
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     ABBV1, ";
        $query .= "     ABBV2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        if ($cd == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試区分大分類コンボ　1:帰国生入試 2:一般入試
    public function getTestLDiv($year, $cd)
    {
        $query  = " WITH T_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV2, ";
        $query .= "         NAMESPARE2 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "         AND NAMECD1 = '".$cd."' ";
        $query .= "     ) ";
        $query .= " , T_TEST_L_DIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS VALUE, ";
        $query .= "         '1:帰国生入試' AS LABEL ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         ABBV2 IS NOT NULL ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         '2' AS VALUE, ";
        $query .= "         '2:一般入試' AS LABEL ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         ABBV2 IS NULL ";
        $query .= "     ) ";
        $query .= " , T_DEF AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN ABBV2 IS NOT NULL THEN '1' ELSE '2' END AS VALUE, ";
        $query .= "         NAMESPARE2 ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         NAMECD2 IN ( ";
        $query .= "             SELECT ";
        $query .= "                 MIN(NAMECD2) AS MIN_NAMECD2 ";
        $query .= "             FROM ";
        $query .= "                 T_COMMON ";
        $query .= "             WHERE ";
        $query .= "                 NAMESPARE2 IS NOT NULL ";
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.LABEL, ";
        $query .= "     L1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     T_TEST_L_DIV T1 ";
        $query .= "     LEFT JOIN T_DEF L1 ON L1.VALUE = T1.VALUE ";
        $query .= " ORDER BY ";
        $query .= "     T1.VALUE ";

        return $query;
    }

    //名称マスタ(受験型)
    public function getExamTypeALL($cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //(姉妹)中高コンボ
    public function getJHName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 IN ('J','H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //(姉妹)年組コンボ
    public function getHrName()
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //(校友会)年組コンボ
    public function getMotherHrName()
    {
        $query  = " WITH MAX_HDAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         max(YEAR||SEMESTER) as YEAR_SEMESTER, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_HDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR <= '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN MAX_HDAT T2 ON T2.YEAR_SEMESTER = T1.YEAR || T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分
    public function getExamcourse($year, $applicantdiv, $testdiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";//固定
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV AS TEST_L_DIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T5.REMARK8 || '-' || T5.REMARK9 || '-' || T5.REMARK10 AS EXAMCOURSE, ";
        //受験料
        $query .= "     M1.EXAM_PAY_DIV, ";
        $query .= "     M1.EXAM_PAY_DATE, ";
        $query .= "     M1.EXAM_PAY_CHAK_DATE, ";
        //特別入試対象者
        $query .= "     T1.GENERAL_FLG AS TOKU_TEST_FLG, ";
        //事前番号
        $query .= "     BASE_D_002.REMARK1 AS RECRUIT_NO, ";
        //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            $query .= "     BASE_D_010.REMARK{$testdiv} AS TESTDIV{$testdiv}, ";
            $query .= "     BASE_D_011.REMARK{$testdiv} AS EXAM_TYPE{$testdiv}, ";
            $query .= "     BASE_D_012.REMARK{$testdiv} AS RECEPTNO{$testdiv}, ";
            $query .= "     BASE_D_013.REMARK{$testdiv} AS TESTSUBCLASSCD{$testdiv}, ";
        }
        //姉妹
        //母
        $query .= "     BASE_D_014.REMARK9 AS EIKEN_SHUTOKU_KYUU, ";
        $query .= "     BASE_D_015.REMARK1 AS MOTHER_NAME, ";
        $query .= "     BASE_D_015.REMARK2 AS MOTHER_NENDO, ";
        $query .= "     BASE_D_015.REMARK3 AS MOTHER_HR_CLASS, ";
        $query .= "     BASE_D_015.REMARK4 AS MOTHER_BIRTHDAY, ";
        $query .= "     BASE_D_015.REMARK5 AS KOUYUU_DIV, ";
        $query .= "     BASE_D_015.REMARK6 AS KOUYUU_SCHOOL_KIND, ";
        $query .= "     BASE_D_015.REMARK7 AS KOUYUU_COLLEGE, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV1 AS GANSHO_YUUSOU, ";
        $query .= "     T1.SPECIAL_REASON_DIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T3.NAME1, ";
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2, ";
        $query .= "     T4.REMARK3, ";
        $query .= "     T4.REMARK4, ";
        $query .= "     T4.REMARK5, ";
        $query .= "     T4.REMARK6, ";
        $query .= "     T4.REMARK7 AS SHOUGAKU1, ";
        $query .= "     T4.REMARK8 AS SHOUGAKU5, ";
        $query .= "     T4.REMARK9 AS SOUDAN, ";
        $query .= "     L1.CONFIDENTIAL_RPT01, ";
        $query .= "     L1.CONFIDENTIAL_RPT02, ";
        $query .= "     L1.CONFIDENTIAL_RPT03, ";
        $query .= "     L1.CONFIDENTIAL_RPT04, ";
        $query .= "     L1.CONFIDENTIAL_RPT05, ";
        $query .= "     L1.CONFIDENTIAL_RPT06, ";
        $query .= "     L1.CONFIDENTIAL_RPT07, ";
        $query .= "     L1.CONFIDENTIAL_RPT08, ";
        $query .= "     L1.CONFIDENTIAL_RPT09, ";
        $query .= "     L1.CONFIDENTIAL_RPT10, ";
        $query .= "     L1.TOTAL_ALL, ";
        $query .= "     L1.TOTAL5, ";
        $query .= "     L1.KASANTEN_ALL, ";
        $query .= "     L1.ABSENCE_DAYS, ";
        $query .= "     L1.ABSENCE_DAYS2, ";
        $query .= "     L1.ABSENCE_DAYS3, ";
        $query .= "     L1.ABSENCE_REMARK, ";
        $query .= "     L1.ABSENCE_REMARK2, ";
        $query .= "     L1.ABSENCE_REMARK3, ";
        $query .= "     L1.REMARK1 AS CONFRPT_REMARK1, ";
        $query .= "     D4.REMARK1 AS DETAIL4_REMARK1, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_INFO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T2.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.ERACD = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L007' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T4.EXAMNO AND ";
        $query .= "     T4.SEQ = '009' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T5.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T5.EXAMNO AND ";
        $query .= "     T5.SEQ = '001' ";
        //入試区分(TESTDIV)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_010 ";
        $query .= "      ON BASE_D_010.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_010.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_010.SEQ = '010' ";
        //受験型(EXAM_TYPE)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_011 ";
        $query .= "      ON BASE_D_011.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_011.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_011.SEQ = '011' ";
        //受験番号(RECEPTNO)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_012 ";
        $query .= "      ON BASE_D_012.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_012.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_012.SEQ = '012' ";
        //帰国生Ｂ方式選択科目(TESTSUBCLASSCD)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_013 ";
        $query .= "      ON BASE_D_013.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_013.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_013.SEQ = '013' ";
        //姉妹
        //母
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_014 ";
        $query .= "      ON BASE_D_014.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_014.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_014.SEQ = '014' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_015 ";
        $query .= "      ON BASE_D_015.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_015.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_015.SEQ = '015' ";
        //事前番号
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_002 ";
        $query .= "      ON BASE_D_002.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_002.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_002.SEQ = '002' ";
        //受験料
        $query .= " LEFT JOIN ENTEXAM_MONEY_DAT M1 ";
        $query .= "      ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND M1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND M1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT D4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = D4.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = D4.EXAMNO AND ";
        $query .= "     D4.SEQ = '004' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     FINSCHOOL_MST FIN ";
        $query .= " ON ";
        $query .= "     T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "                  ON  L013.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                  AND L013.NAMECD1 = 'L013' ";
        $query .= "                  AND L013.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                  AND L013.NAMECD2 IN ('4','5') ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '1' AND ";//1:中学

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".sprintf("%05d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME, ";
        $query .= "         case when L4.NAME1 is not null then '(' || L4.NAME1 || ')' else '' end FINSCHOOL_TYPE_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "         LEFT JOIN NAME_MST L4 ON  L4.NAMECD1 = 'L019' ";
        $query .= "                               AND L4.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //存在チェック
    public function existData($model)
    {
        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        return $query;
    }

    //受験番号範囲チェック
    public function getEntexamNoDatCnt($model, $testdiv, $exam_type, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$testdiv}' ";
        $query .= "     AND MEMO_KISO       = '{$exam_type}' ";
        $query .= "     AND '{$receptno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        //入力画面で追加するのは、窓口・郵送のみとする。
        if ($model->cmd == "add") {
            $query .= "     AND REMARK_DIV      = '2' ";//1:Web出願　2:窓口・郵送
        }

        return $query;
    }

    //受験番号重複チェック
    public function checkNumber($model, $testdiv, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND EXAMNO <> '".$model->examno."' ";
        $query .= "     AND SEQ = '012' ";
        $query .= "     AND REMARK{$testdiv} = '{$receptno}' ";
        return $query;
    }

    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用情報取得(和暦→西暦)
        $result = $db->query(knjl010fQuery::getCalendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d", $model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TEST_L_DIV"];
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["TESTDIV1"][TEXT]      = $model->field["GANSHO_YUUSOU"];
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = "2";
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(NAME_KANA) FROM ENTEXAM_APPLICANTBASE_DAT " .$where);
        //UPDATE
        $data = array();
        $data["NAME_KANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(GKANA) FROM ENTEXAM_APPLICANTADDR_DAT " .$where);
        //UPDATE
        $data = array();
        $data["GKANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT 010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ IN ('010','011','012','013') ";
        $db->query($query);
        //INSERT
        $testCnt = 0;
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            //入試区分チェックありのみ処理
            if ($model->field["TESTDIV".$testdiv] == "") {
                continue;
            }
            //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
            for ($i = 10; $i <= 13; $i++) {
                $seq = sprintf("%03d", $i);
                $name = "";
                if ($seq == "010") {
                    $name = "TESTDIV";
                }
                if ($seq == "011") {
                    $name = "EXAM_TYPE";
                }
                if ($seq == "012") {
                    $name = "RECEPTNO";
                }
                if ($seq == "013") {
                    $name = "TESTSUBCLASSCD";
                }
                if ($seq == "013") {
                    //1:帰国生,5:Ｂ方式の時、選択科目を登録する。
                    if ($codeArray["KIKOKU_FLG"] != "1" || $model->field["EXAM_TYPE".$testdiv] != "5") {
                        continue;
                    }
                }
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $model->field[$name.$testdiv];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                if ($testCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
                    $where .= "   AND EXAMNO = '".$model->examno."' ";
                    $where .= "   AND SEQ = '{$seq}' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);
            }
            $testCnt++;
        }

        /***/
        //受験コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);
        /***/

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 014:姉妹
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '014' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "014";
        $data["REMARK9"][TEXT]          = $model->field["EIKEN_SHUTOKU_KYUU"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 015:母
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '015' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "015";
        $data["REMARK1"][TEXT]          = $model->field["MOTHER_NAME"];         //校友会氏名
        $data["REMARK2"][TEXT]          = $model->field["MOTHER_NENDO"];        //校友会卒業年度
        $data["REMARK3"][TEXT]          = $model->field["MOTHER_HR_CLASS"];     //校友会卒業クラス
        $data["REMARK4"][TEXT]          = $model->field["MOTHER_BIRTHDAY"];     //校友会生年月日
        $data["REMARK5"][TEXT]          = $model->field["KOUYUU_DIV"];          //校友会ご関係
        $data["REMARK6"][TEXT]          = $model->field["KOUYUU_SCHOOL_KIND"];  //校友会卒業中高
        $data["REMARK7"][TEXT]          = $model->field["KOUYUU_COLLEGE"];      //校友会卒業大学・学部
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT　事前番号
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $where .= "   AND SEQ = '002' ";
        //データチェック
        $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where);
        //UPDATE
        $data = array();
        $data["REMARK1"][TEXT]          = $model->field["RECRUIT_NO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "002";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_MONEY_DAT　受験料
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        //データチェック
        $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_MONEY_DAT " .$where);
        //UPDATE
        $data = array();
        $data["EXAM_PAY_DIV"][TEXT]     = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : $model->field["EXAM_PAY_DIV"];
        $data["EXAM_PAY_DATE"][TEXT]    = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["EXAM_PAY_DATE"]);
        $data["EXAM_PAY_CHAK_DATE"][TEXT]   = !strlen($model->field["EXAM_PAY_CHAK_DATE"]) ? "" : str_replace("/", "-", $model->field["EXAM_PAY_CHAK_DATE"]);
        //受験料を取得
        $query = knjl010fQuery::getPayMoney($model, $model->examno);
        $data["EXAM_PAY_MONEY"][NUMBER] = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : $db->getOne($query);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
        }
        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用情報取得(和暦→西暦)
        $result = $db->query(knjl010fQuery::getCalendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d", $model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TEST_L_DIV"];
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["TESTDIV1"][TEXT]      = $model->field["GANSHO_YUUSOU"];
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = "2";
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(NAME_KANA) FROM ENTEXAM_APPLICANTBASE_DAT " .$where);
        //UPDATE
        $data = array();
        $data["NAME_KANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(GKANA) FROM ENTEXAM_APPLICANTADDR_DAT " .$where);
        //UPDATE
        $data = array();
        $data["GKANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT 010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ IN ('010','011','012','013') ";
        $db->query($query);
        //INSERT
        $testCnt = 0;
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            //入試区分チェックありのみ処理
            if ($model->field["TESTDIV".$testdiv] == "") {
                continue;
            }
            //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
            for ($i = 10; $i <= 13; $i++) {
                $seq = sprintf("%03d", $i);
                $name = "";
                if ($seq == "010") {
                    $name = "TESTDIV";
                }
                if ($seq == "011") {
                    $name = "EXAM_TYPE";
                }
                if ($seq == "012") {
                    $name = "RECEPTNO";
                }
                if ($seq == "013") {
                    $name = "TESTSUBCLASSCD";
                }
                if ($seq == "013") {
                    //1:帰国生,5:Ｂ方式の時、選択科目を登録する。
                    if ($codeArray["KIKOKU_FLG"] != "1" || $model->field["EXAM_TYPE".$testdiv] != "5") {
                        continue;
                    }
                }
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $model->field[$name.$testdiv];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                if ($testCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
                    $where .= "   AND EXAMNO = '".$model->examno."' ";
                    $where .= "   AND SEQ = '{$seq}' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);
            }
            $testCnt++;
        }

        /***/
        //受験コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "       AND EXAMNO = '".$model->examno."'";
        $query.= "       AND SEQ = '001'";
        $db->query($query);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);
        /***/

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 014:姉妹
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '014' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "014";
        $data["REMARK9"][TEXT]          = $model->field["EIKEN_SHUTOKU_KYUU"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 015:鏡友会・校友会 卒業時
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '015' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "015";
        $data["REMARK1"][TEXT]          = $model->field["MOTHER_NAME"];         //校友会氏名
        $data["REMARK2"][TEXT]          = $model->field["MOTHER_NENDO"];        //校友会卒業年度
        $data["REMARK3"][TEXT]          = $model->field["MOTHER_HR_CLASS"];     //校友会卒業クラス
        $data["REMARK4"][TEXT]          = $model->field["MOTHER_BIRTHDAY"];     //校友会生年月日
        $data["REMARK5"][TEXT]          = $model->field["KOUYUU_DIV"];          //校友会ご関係
        $data["REMARK6"][TEXT]          = $model->field["KOUYUU_SCHOOL_KIND"];  //校友会卒業中高
        $data["REMARK7"][TEXT]          = $model->field["KOUYUU_COLLEGE"];      //校友会卒業大学・学部
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT　事前番号
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $where .= "   AND SEQ = '002' ";
        //データチェック
        $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where);
        //UPDATE
        $data = array();
        $data["REMARK1"][TEXT]          = $model->field["RECRUIT_NO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "002";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_MONEY_DAT　受験料
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        //データチェック
        $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_MONEY_DAT " .$where);
        //UPDATE
        $data = array();
        $data["EXAM_PAY_DIV"][TEXT]     = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : $model->field["EXAM_PAY_DIV"];
        $data["EXAM_PAY_DATE"][TEXT]    = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["EXAM_PAY_DATE"]);
        $data["EXAM_PAY_CHAK_DATE"][TEXT]   = !strlen($model->field["EXAM_PAY_CHAK_DATE"]) ? "" : str_replace("/", "-", $model->field["EXAM_PAY_CHAK_DATE"]);
        //受験料を取得
        $query = knjl010fQuery::getPayMoney($model, $model->examno);
        $data["EXAM_PAY_MONEY"][NUMBER] = !strlen($model->field["EXAM_PAY_DATE"]) ? "" : $db->getOne($query);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
        }
        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getPayMoney($model, $examno)
    {
        $query  = "";

        //免除額コードを取得(BASE)
        $query .= " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND EXAMNO = '{$examno}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND DIV = '0' ";
        $query .= "         AND REMARK4 = '1' ";//受験料
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ITEM_MONEY, 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";

        return $query;
    }
}
