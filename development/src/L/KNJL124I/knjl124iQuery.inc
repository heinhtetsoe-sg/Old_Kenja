<?php
class knjl124iQuery extends Query {

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
            $query .= "     AND NAMECD2 = '2' "; //入試制度 2:高校 固定
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得 
    function getEntexamSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SETTING_CD   = '".$namecd1."' ";
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

    //合格点の取得
    function getBorderScore($model) {
        $query .= " SELECT ";
        $query .= "     BORDER_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND SHDIV        = '".$model->field["SEX"]."' ";
        $query .= "   AND COURSECD     = '0' ";
        $query .= "   AND MAJORCD      = '000' ";
        $query .= "   AND EXAMCOURSECD = '0000' ";
        return $query;
    }

    //合格者数の取得
    function getPassCount($model, $score = "") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS PASS_COUNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "            AND T2.SEX          = '".$model->field["SEX"]."' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        if ($score) {
            $query .= "   AND T1.TOTAL1 >= ".$score." "; //合格点を超えている人数
        } else {
            $query .= "   AND T1.JUDGEDIV     = '1' "; //合格者数
        }
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
        $query .= "   VALUE(T3.PASS_COUNT, 0) AS PASS_COUNT, ";
        //リンクにて参照
        $query .= "   T1.APPLICANTDIV, ";
        $query .= "   T1.TESTDIV, ";
        $query .= "   T1.EXAM_TYPE, ";
        $query .= "   T1.SHDIV ";
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

    //満点の取得
    function getPerfectScore($model) {
        $query .= " SELECT ";
        $query .= "     SUM(T1.PERFECT) AS SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_PERFECT_EXAMTYPE_MST T2 ";
        $query .= "             ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "            AND T2.TESTDIV        = T1.TESTDIV ";
        $query .= "            AND T2.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "            AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND T1.COURSECD     = '0' ";
        $query .= "   AND T1.MAJORCD      = '000' ";
        $query .= "   AND T1.EXAMCOURSECD = '0000' ";
        $query .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        return $query;
    }

    //A方式の合格判定に使用
    function getSubTotalAvg($model, $testSubCd) {
        $query  = "     SELECT ";
        $query .= "         AVARAGE_TOTAL ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "         AND TESTSUBCLASSCD = '{$testSubCd}' ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //件数の取得
        $query .= " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND SHDIV        = '".$model->field["SEX"]."' ";
        $query .= "   AND COURSECD        = '0' ";
        $query .= "   AND MAJORCD         = '000' ";
        $query .= "   AND EXAMCOURSECD    = '0000' ";
        $getDataCount = $db->getOne($query);

        $data = array();
        $data["BORDER_SCORE"][TEXT] = $model->field["BORDER_SCORE"];
        $data["SUCCESS_CNT"][TEXT]  = $db->getOne(knjl124iQuery::getPassCount($model, $model->field["BORDER_SCORE"]));
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($getDataCount == 0) {
            //登録
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]    = $model->field["EXAM_TYPE"];
            $data["SHDIV"][TEXT]        = $model->field["SEX"];
            $data["COURSECD"][TEXT]     = '0';
            $data["MAJORCD"][TEXT]      = '000';
            $data["EXAMCOURSECD"][TEXT] = '0000';
            $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        } else {
            //更新
            $where .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
            $where .= "   AND SHDIV        = '".$model->field["SEX"]."' ";
            $where .= "   AND COURSECD     = '0' ";
            $where .= "   AND MAJORCD      = '000' ";
            $where .= "   AND EXAMCOURSECD = '0000' ";
            $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
        }
        $db->query($query);

        //更新(ENTEXAM_RECEPT_DAT, ENTEXAM_APPLICANTBASE_DAT)
        knjl124iQuery::getReceptUpdateQuery($db, $model);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //更新(ENTEXAM_RECEPT_DAT, ENTEXAM_APPLICANTBASE_DAT)
    function getReceptUpdateQuery($db, &$model)
    {
        $engSubCd = "5";

        //更新対象の取得
        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T2.JUDGE_KIND, ";
        $query .= "   T3.SCORE AS ENG_SCORE, ";
        $query .= "   T4.REMARK11 AS TOTAL_PLUS, ";
        $query .= "   T4.REMARK12 AS TOTAL_MINUS, ";
        $query .= "   T5.SCORE1 AS INTERVIEW_PLUS, ";
        $query .= "   T5.SCORE2 AS INTERVIEW_MINUS ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "           ON T2.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "          AND T2.EXAMNO          = T1.EXAMNO ";
        $query .= "          AND T2.SEX             = '".$model->field["SEX"]."' ";
        $query .= "   LEFT JOIN ENTEXAM_SCORE_DAT T3 "; //A方式判定に使用
        $query .= "           ON T3.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "          AND T3.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "          AND T3.TESTDIV         = T1.TESTDIV ";
        $query .= "          AND T3.EXAM_TYPE       = T1.EXAM_TYPE ";
        $query .= "          AND T3.RECEPTNO        = T1.RECEPTNO ";
        $query .= "          AND T3.TESTSUBCLASSCD  = '{$engSubCd}' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4 "; //B方式判定に使用
        $query .= "           ON T4.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "          AND T4.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "          AND T4.EXAMNO          = T1.EXAMNO ";
        $query .= "          AND T4.SEQ             = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_INTERVIEW_DAT T5 "; //B方式判定に使用
        $query .= "           ON T5.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "          AND T5.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "          AND T5.TESTDIV         = T1.TESTDIV ";
        $query .= "          AND T5.EXAMNO          = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND T1.EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "   AND VALUE(T1.JUDGEDIV, '') <> '4' ";
        $result = $db->query($query);

        //「5:英語」の全体平均点を取得
        $query = knjl124iQuery::getSubTotalAvg($model, $engSubCd);
        $engTotalAvg = $db->getOne($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $passFlg = false;

            //1.内定済みの場合
            if ($model->naiteiDivArr[$row["JUDGE_KIND"]]) {
                $passFlg = true;
            //2.合格点以上の場合
            } else {
                if ($model->field["TESTDIV"] == 1) { //A方式
                    $clearFlg1 = ($row["TOTAL1"] >= $model->field["BORDER_SCORE"]) ? true : false;
                    $clearFlg2 = ($row["ENG_SCORE"] < $engTotalAvg * 0.3) ? false : true; //個人の英語の得点が、科目全体の平均の30%未満は不合格
                    $passFlg = ($clearFlg1 && $clearFlg2) ? true : false;
                } else if ($model->field["TESTDIV"] == 2) { //B方式
                    $tyousasyoScore = $row["TOTAL_PLUS"] - $row["TOTAL_MINUS"];
                    $interviewScore = $row["INTERVIEW_PLUS"] - $row["INTERVIEW_MINUS"];
                    $passFlg = ($tyousasyoScore + $interviewScore >= $model->field["BORDER_SCORE"]) ? true : false;
                } else { //それ以外は無条件に合格
                    $passFlg = true;
                }                
            }

            //合否の設定 1.合格  それ以外の場合：2.不合格
            $judge = ($passFlg) ? "1" : "2";

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["JUDGEDIV"][TEXT]     = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
            $where .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
            $where .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]     = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
            $where .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO       = '".$row["EXAMNO"]."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }
    }

}
?>