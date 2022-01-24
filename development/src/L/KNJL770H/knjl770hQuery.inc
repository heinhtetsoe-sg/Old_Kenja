<?php

require_once('for_php7.php');

class knjl770hQuery extends Query
{

    //名称マスタよりデータを取得
    public function getName($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    public function getTestDiv($year, $applicantDiv, $testDiv = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格点マスタ
    public function selectQueryPassingmark($model, $testdiv = "", $sex = "")
    {
        $query  = " SELECT ";
        $query .= "     P1.TESTDIV, ";
        $query .= "     P1.SHDIV AS SEX, ";
        $query .= "     N1.TESTDIV_NAME, ";
        $query .= "     P1.BORDER_SCORE, ";
        $query .= "     P1.SUCCESS_CNT AS GOUKAKU_CNT, ";
        $query .= "     P1.BORDER_SCORE_CANDI, ";
        $query .= "     P1.SUCCESS_CNT_CANDI ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST P1 ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST N1 ON N1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND N1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND N1.TESTDIV = P1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         P1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if (strlen($testdiv)) {
            $query .= "     AND P1.TESTDIV      = '{$testdiv}' ";
        }
        if (strlen($sex)) {
            $query .= "     AND P1.SHDIV      = '{$sex}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     P1.TESTDIV, ";
        $query .= "     P1.SHDIV ";

        return $query;
    }

    //合格者数など取得
    public function getCntPassingmark($model)
    {
        $query  = " WITH T_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '4' THEN 1 ELSE 0 END) AS SUCCESS_CNT_CANDI ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_JUDGE_TMP T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND T1.TESTDIV      = '{$model->testdiv}' ";
        if ($model->sex != "9") {
            $query .= "         AND T2.SEX          = '{$model->sex}' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GOUKAKU_CNT, ";
        $query .= "     FUGOUKAKU_CNT, ";
        $query .= "     SUCCESS_CNT_CANDI ";
        $query .= " FROM ";
        $query .= "     T_CNT ";

        return $query;
    }

    public function getDecisionData($model)
    {
        $query  = "   SELECT ";
        $query .= "       *, ";
        $query .= "       SHDIV AS SEX "; //SHDIVをSEXとして流用
        $query .= "   FROM ";
        $query .= "       ENTEXAM_PASSINGMARK_MST ";
        $query .= "   WHERE ";
        $query .= "             ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND TESTDIV      = '{$model->testdiv}' ";

        return $query;
    }

    //シミュレーションSQL
    public function simSql($model, $div = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        if ($div == "kuriage") {
            $query .= "     CASE WHEN FLOAT(RD009.REMARK6) >= {$model->field["BORDER_SCORE_CANDI"]} THEN '4' ";
            $query .= "          ELSE '2' ";
        } else {
            $query .= "     CASE WHEN FLOAT(RD009.REMARK6) >= {$model->field["BORDER_SCORE"]} THEN '1' ";
            $query .= "          ELSE '2' ";
        }
        $query .= "     END AS JUDGEDIV, ";
        $staffcd = STAFFCD;
        $query .= "     '{$staffcd}' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
        $query .= "         ON RD009.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND RD009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND RD009.TESTDIV = T1.TESTDIV ";
        $query .= "         AND RD009.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND RD009.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND RD009.SEQ = '009' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";
        $query .= "     AND RD009.REMARK6 IS NOT NULL "; //総合点がある場合のみ対象
        if ($model->sex != "9") {
            $query .= "     AND T2.SEX = '{$model->sex}' ";
        }
        if ($div == "kuriage") {
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_JUDGE_TMP TMP ";
            $query .= "         WHERE ";
            $query .= "             TMP.ENTEXAMYEAR       = T1.ENTEXAMYEAR ";
            $query .= "             AND TMP.APPLICANTDIV  = T1.APPLICANTDIV ";
            $query .= "             AND TMP.TESTDIV       = T1.TESTDIV ";
            $query .= "             AND TMP.EXAM_TYPE     = T1.EXAM_TYPE ";
            $query .= "             AND TMP.RECEPTNO      = T1.RECEPTNO ";
            $query .= "             AND TMP.JUDGEDIV      = '1' ";
            $query .= "     ) ";
        }

        return $query;
    }

    //シミュレーション
    public function sim($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);

        //合格判定
        $query = knjl770hQuery::simSql($model);
        $insQuery = knjl770hQuery::getInsertQueryTmp($query);
        $db->query($insQuery);

        if ($model->field["BORDER_SCORE_CANDI"] != "") {
            //繰上合格判定
            $query = knjl770hQuery::simSql($model, "kuriage");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $updQuery = knjl770hQuery::getUpdateKuriageQueryTmp($model, $row);
                $db->query($updQuery);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getInsertQueryTmp($query)
    {
        //仮判定データ追加
        $commonQuery  = " INSERT INTO entexam_judge_tmp( ";
        $commonQuery .= " entexamyear, ";
        $commonQuery .= " applicantdiv, ";
        $commonQuery .= " testdiv, ";
        $commonQuery .= " exam_type, ";
        $commonQuery .= " receptno, ";
        $commonQuery .= " examno, ";
        $commonQuery .= " judgediv, ";
        $commonQuery .= " registercd ";
        $commonQuery .= " ) ";
        $commonQuery .= " ( ";
        $commonQuery .= $query;
        $commonQuery .= " ) ";

        return $commonQuery;
    }

    public function getUpdateKuriageQueryTmp($model, $row)
    {
        $data = array();
        $data["JUDGEDIV"][TEXT]     = $row["JUDGEDIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->year}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $where .= "   AND RECEPTNO       = '{$row["RECEPTNO"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_JUDGE_TMP", $where);

        return $query;
    }

    //合格コース
    public function getCourse($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS COURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV= '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV     = '{$model->testdiv}' ";
        return $query;
    }

    //確定
    public function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受付データ
        //1:合格 2:不合格 4:繰上合格
        $judgeArray = array(1, 2, 4);
        foreach ($judgeArray as $judge) {
            $query  = " UPDATE ";
            $query .= "     entexam_recept_dat t1 ";
            $query .= " SET ";
            $query .= "     (t1.judgediv,updated) = ";
            $query .= "     ( ";
            $query .= "     '{$judge}', ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear      = '{$model->year}' ";
            $query .= "     AND t1.applicantdiv = '{$model->applicantdiv}' ";
            $query .= "     AND t1.testdiv      = '{$model->testdiv}' ";
            $query .= "     AND t1.receptno     IN (SELECT receptno FROM entexam_judge_tmp WHERE judgediv = '{$judge}' ) ";
            $db->query($query);
        }

        //合格コース
        $course = $db->getOne(knjl770hQuery::getCourse($model));

        //志願者基礎データ
        //1:合格 2:不合格 4:繰上合格
        foreach ($judgeArray as $judge) {
            $query  = " UPDATE ";
            $query .= "     entexam_applicantbase_dat t1 ";
            $query .= " SET ";
            $query .= "     (judgement,updated) = ";
            $query .= "     ( ";
            $query .= "     '{$judge}', ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear      = '{$model->year}' ";
            $query .= "     AND t1.applicantdiv = '{$model->applicantdiv}' ";
            $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '{$judge}' ) ";
            $db->query($query);
        }

        //合格点マスタ
        $query  = " DELETE FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND SHDIV        = '{$model->sex}' ";
        $db->query($query);

        //固定ALL0
        $course = "00000000";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]              = $model->year;
        $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
        $data["TESTDIV"][TEXT]                  = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]                = "1";// 1固定
        $data["SHDIV"][TEXT]                    = $model->sex;
        $data["COURSECD"][TEXT]                 = substr($course, 0, 1);
        $data["MAJORCD"][TEXT]                  = substr($course, 1, 3);
        $data["EXAMCOURSECD"][TEXT]             = substr($course, 4, 4);
        $data["BORDER_SCORE"][NUMBER]           = $model->field["BORDER_SCORE"];
        $data["SUCCESS_CNT"][NUMBER]            = $model->field["GOUKAKU_CNT"];
        $data["BORDER_SCORE_CANDI"][NUMBER]     = $model->field["BORDER_SCORE_CANDI"];
        $data["SUCCESS_CNT_CANDI"][NUMBER]      = $model->field["SUCCESS_CNT_CANDI"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
