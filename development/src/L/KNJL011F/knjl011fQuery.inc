<?php

require_once('for_php7.php');

class knjl011fQuery extends Query
{

    //MAX受験番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '2' ";//2:高校

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = "0001";
        }

        Query::dbCheckIn($db);
        return sprintf("%04d", $maxExam);
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
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
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数(2:高校のみ)
    public function getTestdiv0($year, $testdiv)
    {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
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

    //受験科目　3001:スポーツ科学　1:Ａ推薦・推薦、4:帰国生Ａ　の時、選択
    public function getSelectSubclassDiv($year, $cd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME2 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        $query .= "     AND NAMECD2 IN ('2','5','9') ";//2:数学 5:英語 9:プレゼンテーション
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

    //メインデータ取得
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T5.REMARK8 || '-' || T5.REMARK9 || '-' || T5.REMARK10 AS EXAMCOURSE, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        //受験料
        $query .= "     M1.EXAM_PAY_DIV, ";
        $query .= "     M1.EXAM_PAY_DATE, ";
        $query .= "     M1.EXAM_PAY_CHAK_DATE, ";
        //学力診断テスト対象者
        $query .= "     T1.GENERAL_FLG AS GAKU_TEST_FLG, ";
        //事前番号
        $query .= "     BASE_D_002.REMARK1 AS RECRUIT_NO, ";
        //姉妹
        //母
        $query .= "     BASE_D_015.REMARK1 AS MOTHER_NAME, ";
        $query .= "     BASE_D_015.REMARK2 AS MOTHER_NENDO, ";
        $query .= "     BASE_D_015.REMARK3 AS MOTHER_HR_CLASS, ";
        $query .= "     BASE_D_015.REMARK4 AS MOTHER_BIRTHDAY, ";
        $query .= "     BASE_D_015.REMARK5 AS KOUYUU_DIV, ";
        $query .= "     BASE_D_015.REMARK6 AS KOUYUU_SCHOOL_KIND, ";
        $query .= "     BASE_D_015.REMARK7 AS KOUYUU_COLLEGE, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.SHIFT_DESIRE_FLG, ";
        $query .= "     T1.SLIDE_FLG, ";
        $query .= "     T1.SELECT_SUBCLASS_DIV, ";
        //併願校名、併願校合格発表日
        $query .= "     BASE_D_016.REMARK1 AS SH_SCHOOLNAME, ";
        $query .= "     BASE_D_016.REMARK2 AS SH_JUDGEMENT_DATE, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV1 AS GANSHO_YUUSOU, ";
        $query .= "     T1.SPECIAL_REASON_DIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
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
        $query .= "     L1.TOTAL3, ";
        $query .= "     L1.TOTAL5, ";
        $query .= "     L1.TOTAL_ALL, ";
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
        //姉妹
        //母
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_015 ";
        $query .= "      ON BASE_D_015.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_015.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_015.SEQ = '015' ";
        //併願校名、併願校合格発表日
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_016 ";
        $query .= "      ON BASE_D_016.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_016.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_016.SEQ = '016' ";
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
        $query .= "                  AND L013.NAMECD2 = '4' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '2' AND ";//2:高校

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".sprintf("%04d", $model->examno)."'";
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

    //受験番号範囲取得
    public function getEntexamNoDatCnt($model)
    {
        $course_array = explode("-", $model->field["EXAMCOURSE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND TESTDIV0        = '{$model->field["TESTDIV0"]}' ";
        $query .= "     AND EXAMCOURSECD    = '".$course_array[2]."' ";
        $query .= "     AND '{$model->examno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        //入力画面で追加するのは、窓口・郵送のみとする。
        if ($model->cmd == "add") {
            $query .= "     AND REMARK_DIV      = '2' ";//1:Web出願　2:窓口・郵送
        }

        return $query;
    }

    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011fQuery::getCalendarno($model->year));
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
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["TESTDIV0"][TEXT]      = $model->field["TESTDIV0"];//入試回数
        $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
        $data["SHIFT_DESIRE_FLG"][TEXT]     = $model->field["SHIFT_DESIRE_FLG"];
        $data["SLIDE_FLG"][TEXT]            = $model->field["SLIDE_FLG"];
        $data["SELECT_SUBCLASS_DIV"][TEXT]  = $model->field["SELECT_SUBCLASS_DIV"];
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

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '016' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "016";
        $data["REMARK1"][TEXT]          = $model->field["SH_SCHOOLNAME"];//併願校名
        $data["REMARK2"][TEXT]          = str_replace("/", "-", $model->field["SH_JUDGEMENT_DATE"]);//併願校合格発表日
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
        $query = knjl011fQuery::getPayMoney($model, $model->examno);
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

        $result = $db->query(knjl011fQuery::getCalendarno($model->year));
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
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["TESTDIV0"][TEXT]      = $model->field["TESTDIV0"];//入試回数
        $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
        $data["SHIFT_DESIRE_FLG"][TEXT]     = $model->field["SHIFT_DESIRE_FLG"];
        $data["SLIDE_FLG"][TEXT]            = $model->field["SLIDE_FLG"];
        $data["SELECT_SUBCLASS_DIV"][TEXT]  = $model->field["SELECT_SUBCLASS_DIV"];
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

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
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

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ = '016' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = "016";
        $data["REMARK1"][TEXT]          = $model->field["SH_SCHOOLNAME"];//併願校名
        $data["REMARK2"][TEXT]          = str_replace("/", "-", $model->field["SH_JUDGEMENT_DATE"]);//併願校合格発表日
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
        $query = knjl011fQuery::getPayMoney($model, $model->examno);
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

        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
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
