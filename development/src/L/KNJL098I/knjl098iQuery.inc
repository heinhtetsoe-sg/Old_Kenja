<?php
class knjl098iQuery extends Query {

    //名称マスタより取得 
    function getNameMst($namecd1, $year) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if($namecd1 == "L003"){
            $query .= "     AND NAMECD2 = '1' "; //入試制度 1:中学 固定
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分の取得
    function getTestDiv($model) {
        $query .= " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " WITH RECEPT AS( ";
        $query .= "   SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T2.SEX, ";
        $query .= "     COUNT(*) AS PASS_COUNT ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "     AND T1.JUDGEDIV     = '1' ";
        $query .= "   GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T2.SEX ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T2.TESTDIV_NAME, ";
        $query .= "   CASE WHEN T1.SHDIV = '1' ";
        $query .= "        THEN '男子' ";
        $query .= "        ELSE CASE WHEN T1.SHDIV = '2' ";
        $query .= "                  THEN '女子' ";
        $query .= "                  ELSE '' ";
        $query .= "             END ";
        $query .= "   END AS SEX, ";
        $query .= "   T1.BORDER_SCORE, ";
        $query .= "   VALUE(T3.PASS_COUNT, 0) AS PASS_COUNT ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_PASSINGMARK_MST T1 ";
        $query .= "   LEFT JOIN ENTEXAM_TESTDIV_MST T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "   LEFT JOIN RECEPT T3 ";
        $query .= "          ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T3.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T3.SEX          = T1.SHDIV ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND T1.COURSECD     = '0' ";
        $query .= "   AND T1.MAJORCD      = '000' ";
        $query .= "   AND T1.EXAMCOURSECD = '0000' ";
        $query .= " ORDER BY ";
        $query .= "   T1.TESTDIV, ";
        $query .= "   T1.SHDIV ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //全体順位の取得
        $rank = 1;
        $query = knjl098iQuery::getReceptDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //件数の取得
            $query  = " SELECT ";
            $query .= "   COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "   ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "       ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
            $query .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
            $query .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
            $query .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $query .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
            $query .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $query .= "   AND SEQ          = '015' ";
            $getDataCount = $db->getOne($query);

            $data = array();
            $data["REMARK1"][TEXT]      = $rank;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($getDataCount == 0) {
                //登録
                $data["ENTEXAMYEAR"][TEXT]      = $row["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
                $data["RECEPTNO"][TEXT]         = $row["RECEPTNO"];
                $data["SEQ"][TEXT]              = "015";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                //更新
                $where  = " WHERE ";
                $where .= "       ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
                $where .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
                $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
                $where .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
                $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
                $where .= "   AND SEQ          = '015' ";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            }
            $db->query($query);
            $rank++;
        }

        //性別毎の順位の取得(1:男子)
        $sex = "1";
        $rank = 1;
        $query = knjl098iQuery::getReceptDat($model, $sex);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新
            $data = array();
            $data["REMARK2"][TEXT]      = $rank;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "       ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
            $where .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND SEQ          = '015' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            $db->query($query);
            $rank++;
        }
        
        //性別毎の順位の取得(2:女子)
        $sex = "2";
        $rank = 1;
        $query = knjl098iQuery::getReceptDat($model, $sex);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新
            $data = array();
            $data["REMARK2"][TEXT]      = $rank;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "       ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
            $where .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND SEQ          = '015' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            $db->query($query);
            $rank++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //更新対象の取得
    function getReceptDat($model, $sex = "")
    {
        $query  = " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.EXAMNO       = T1.EXAMNO ";
        if ($sex) {
            $query .= "      AND T2.SEX          = '".$sex."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND T1.JUDGEDIV     = '1' ";
        $query .= " ORDER BY TOTAL1 DESC ";
        return $query;
    }
}
?>