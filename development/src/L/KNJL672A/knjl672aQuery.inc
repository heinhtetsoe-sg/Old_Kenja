<?php

require_once('for_php7.php');

class knjl672aQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     VALUE(NAME1, '') AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科
    public function getCourseMajorMst($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE ";
        $query .= "   , COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND COURSECD || MAJORCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             ENTER_COURSECD || ENTER_MAJORCD ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_COURSE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             ENTER_COURSECD || ENTER_MAJORCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学コース
    public function getCourceCode($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECODE || ':' || COURSECODENAME AS LABEL ";
        $query .= "   , COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND COURSECODE IN ( ";
        $query .= "         SELECT ";
        $query .= "             ENTER_COURSECODE ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_COURSE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             ENTER_COURSECODE ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ選択
    public function selectQuery($model)
    {
        $courseCd = substr($model->field['COURSEMAJOR'], 0, 1);
        $majorCd = substr($model->field['COURSEMAJOR'], 1);
        $examCourseCd = $model->field['COURSECODE'];

        $query  = "";
        $query .= " WITH PASS_TBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     R1.ENTEXAMYEAR, ";
        $query .= "     R1.APPLICANTDIV, ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     CASE WHEN RD_007.REMARK1 IS NOT NULL AND RD_007.REMARK7 = '1' ";
        $query .= "          THEN RD_007.REMARK1 ";
        $query .= "          WHEN RD_007.REMARK2 IS NOT NULL AND RD_007.REMARK8 = '1' ";
        $query .= "          THEN RD_007.REMARK2 ";
        $query .= "     END AS PASS_COURSECODE ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ";
        $query .= "        ON RD_007.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "       AND RD_007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "       AND RD_007.TESTDIV = R1.TESTDIV ";
        $query .= "       AND RD_007.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "       AND RD_007.RECEPTNO = R1.RECEPTNO ";
        $query .= "       AND RD_007.SEQ = '007' ";
        $query .= "   WHERE ";
        $query .= "     R1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND R1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND (RD_007.REMARK1 IS NOT NULL AND RD_007.REMARK7 = '1' ";
        $query .= "      OR  RD_007.REMARK2 IS NOT NULL AND RD_007.REMARK8 = '1') ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , PASS.TESTDIV ";
        $query .= "   , T1.EXAMNO ";
        $query .= "   , T1.NAME ";
        $query .= "   , T1.PROCEDUREDIV ";
        $query .= "   , T1.ENTDIV ";

        $query .= "   , T1.SEX ";
        $query .= "   , M4.NAME1 SEXNAME ";
        $query .= "   , T2.REMARK1 COURSECD ";
        $query .= "   , T2.REMARK2 MAJORCD ";
        $query .= "   , T2.REMARK3 COURSECODE ";
        $query .= "   , M2.COURSENAME ";
        $query .= "   , M2.MAJORNAME ";
        $query .= "   , M3.COURSECODENAME ";

        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT T1 ";
        //指定入試区分で登録済み
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "     ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "     AND T2.SEQ = '007' ";
        $query .= "     AND T2.REMARK4 = '{$model->field["TESTDIV"]}' ";
        //別入試区分で登録済み
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "     ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = T3.EXAMNO ";
        $query .= "     AND T3.SEQ = '007' ";
        $query .= "     AND T3.REMARK4 != '{$model->field["TESTDIV"]}' ";

        //指定入試区分・入学コースの合格者
        $query .= "   INNER JOIN PASS_TBL PASS ";
        $query .= "     ON T1.ENTEXAMYEAR = PASS.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = PASS.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = PASS.EXAMNO ";
        $query .= "   INNER JOIN ENTEXAM_COURSE_MST M1 ";
        $query .= "     ON PASS.ENTEXAMYEAR = M1.ENTEXAMYEAR ";
        $query .= "     AND PASS.APPLICANTDIV = M1.APPLICANTDIV ";
        $query .= "     AND PASS.TESTDIV = M1.TESTDIV ";
        $query .= "     AND PASS.PASS_COURSECODE = M1.EXAMCOURSECD ";
        $query .= "     AND M1.ENTER_COURSECD = '{$courseCd}' ";
        $query .= "     AND M1.ENTER_MAJORCD = '{$majorCd}' ";
        $query .= "     AND M1.ENTER_COURSECODE = '{$examCourseCd}' ";

        $query .= "   LEFT JOIN V_COURSE_MAJOR_MST M2 ";
        $query .= "     ON T2.ENTEXAMYEAR = M2.YEAR ";
        $query .= "     AND T2.REMARK1 = M2.COURSECD ";
        $query .= "     AND T2.REMARK2 = M2.MAJORCD ";
        $query .= "   LEFT JOIN V_COURSECODE_MST M3 ";
        $query .= "     ON T2.ENTEXAMYEAR = M2.YEAR ";
        $query .= "     AND T2.REMARK3 = M3.COURSECODE ";
        $query .= "   LEFT JOIN V_NAME_MST M4 ";
        $query .= "     ON T1.ENTEXAMYEAR = M4.YEAR ";
        $query .= "     AND T1.SEX = M4.NAMECD2 ";
        $query .= "     AND M4.NAMECD1 = 'Z002' ";

        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        //複数回合格者の場合、別入試区分で登録済みの時、合格者一覧に表示しない。
        $query .= "   AND T3.REMARK4 IS NULL ";

        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //データ更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i=0; $i < get_count($model->examNoList); $i++) {
            $examNo    = $model->examNoList[$i];

            // 手続区分
            $procedureDiv = "";
            // 入学区分
            $entDiv = "";
            // 課程
            $courseCd = "";
            // 学科
            $majorCd = "";
            // 入学コース
            $sourseCode = "";
            // 入学入試区分
            $enterTestdiv = "";

            // 入学決定者に選択されている受験者の場合、指定項目に値設定
            if ($examNo['SEL'] == "1") {
                $procedureDiv = "1";
                $entDiv = "1";
                $courseCd = substr($model->field["COURSEMAJOR"], 0, 1);
                $majorCd = substr($model->field["COURSEMAJOR"], 1);
                $sourseCode = $model->field["COURSECODE"];
                $enterTestdiv = $model->field["TESTDIV"];
            }

            // 志願者基礎(ENTEXAM_APPLICANTBASE_DAT)
            $data = array();
            $data["PROCEDUREDIV"][TEXT]  = $procedureDiv;
            $data["ENTDIV"][TEXT]        = $entDiv;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO = '{$examNo['EXAMNO']}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            // 志願者基礎詳細(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $examNo['EXAMNO'];
            $data["SEQ"][TEXT]           = "007";
            $data["REMARK1"][TEXT]       = $courseCd;
            $data["REMARK2"][TEXT]       = $majorCd;
            $data["REMARK3"][TEXT]       = $sourseCode;
            $data["REMARK4"][TEXT]       = $enterTestdiv;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";

            $detailCnt = $db->getOne(knjl672aQuery::getAppBaseDetailCnt($model, 'COUNT(*)', $model->field["APPLICANTDIV"], $examNo['EXAMNO'], '007'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "   AND EXAMNO = '{$examNo['EXAMNO']}' ";
                $where .= "   AND SEQ = '007' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    // 志願者基礎詳細データ(登録件数取得)
    public function getAppBaseDetailCnt($model, $field, $applicantdiv, $examNo, $seq)
    {
        $query  = "";
        $query .= " SELECT {$field} ";
        $query .= " FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "   AND EXAMNO = '{$examNo}' ";
        $query .= "   AND SEQ = '{$seq}' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $record = $data_arr[$i];

            // 指定項目に値設定
            $procedureDiv = "1";
            $entDiv = "1";
            $courseCd = $record['COURSECD'];
            $majorCd = $record['MAJORCD'];
            $sourseCode = $record['COURSECODE'];
            $enterTestdiv = $record['TESTDIV'];

            // 志願者基礎(ENTEXAM_APPLICANTBASE_DAT)
            $data = array();
            $data["PROCEDUREDIV"][TEXT]  = $procedureDiv;
            $data["ENTDIV"][TEXT]        = $entDiv;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV = '{$record["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO = '{$record['EXAMNO']}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            // 志願者基礎詳細(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $record["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $record['EXAMNO'];
            $data["SEQ"][TEXT]           = "007";
            $data["REMARK1"][TEXT]       = $courseCd;
            $data["REMARK2"][TEXT]       = $majorCd;
            $data["REMARK3"][TEXT]       = $sourseCode;
            $data["REMARK4"][TEXT]       = $enterTestdiv;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";

            $detailCnt = $db->getOne(knjl672aQuery::getAppBaseDetailCnt($model, 'COUNT(*)', $record["APPLICANTDIV"], $record['EXAMNO'], '007'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$record["APPLICANTDIV"]}' ";
                $where .= "   AND EXAMNO = '{$record['EXAMNO']}' ";
                $where .= "   AND SEQ = '007' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";

        return $query;
    }

    //課程学科マスタ登録コードチェック
    public function getCourseMajorCntSql($model, $courseCd, $majorCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND COURSECD = '{$courseCd}' ";
        $query .= "     AND MAJORCD = '{$majorCd}' ";

        return $query;
    }

    //コースマスタ登録コードチェック
    public function getCourseCodeCntSql($model, $courseCode)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND COURSECODE = '{$courseCode}' ";

        return $query;
    }

    //志願者基礎登録チェック
    public function getEntexamApplicantBaseCntSql($model, $applicantDiv, $testDiv, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "        ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "       AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "       AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ";
        $query .= "        ON RD_007.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "       AND RD_007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "       AND RD_007.TESTDIV = R1.TESTDIV ";
        $query .= "       AND RD_007.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "       AND RD_007.RECEPTNO = R1.RECEPTNO ";
        $query .= "       AND RD_007.SEQ = '007' ";
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND R1.TESTDIV = '{$testDiv}' ";
        $query .= "     AND R1.EXAMNO = '{$examNo}' ";
        $query .= "     AND (RD_007.REMARK1 IS NOT NULL AND RD_007.REMARK7 = '1' ";
        $query .= "      OR  RD_007.REMARK2 IS NOT NULL AND RD_007.REMARK8 = '1') ";

        return $query;
    }
    //登録コース取得
    public function getEntexamApplicantBaseDetailSql($model, $applicantDiv, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 COURSECD ";
        $query .= "   , REMARK2 MAJORCD ";
        $query .= "   , REMARK3 COURSECODE ";
        $query .= "   , REMARK4 TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO = '{$examNo}' ";
        $query .= "     AND SEQ = '007' ";

        return $query;
    }
}
