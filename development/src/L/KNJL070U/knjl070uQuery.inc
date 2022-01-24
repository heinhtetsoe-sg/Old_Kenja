<?php

require_once('for_php7.php');

class knjl070uQuery extends Query {

    //名称マスタよりデータを取得
    function getName($year, $namecd1, $namecd2 = "") {
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

    //合格点マスタ
    function selectQueryPassingmark($model, $testdiv = "") {
        $query  = " SELECT ";
        $query .= "     C1.CAPACITY, ";
        $query .= "     P1.TESTDIV, ";
        $query .= "     N1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     P1.BORDER_SCORE, ";
        $query .= "     P1.SUCCESS_CNT AS GOUKAKU_CNT, ";
        $query .= "     P1.BACK_RATE AS MODORI_RITU, ";
        $query .= "     P1.CAPA_CNT AS SHUUYOU_CNT, ";
        $query .= "     VALUE(C1.CAPACITY,0) - VALUE(P1.CAPA_CNT,0) AS ZOUGEN_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST P1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR  = P1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV      = P1.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR  = P1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L004' ";
        $query .= "         AND N1.NAMECD2 = P1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         P1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if (strlen($testdiv)) {
            $query .= "     AND P1.TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     P1.TESTDIV ";
        return $query;
    }

    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt($model) {
        $query  = " SELECT ";
        $query .= "     sum(case when t1.judgediv in ('1') then 1 else 0 end) as GOUKAKU_CNT, ";
        $query .= "     sum(case when t1.judgediv in ('2') then 1 else 0 end) as FUGOUKAKU_CNT ";
        $query .= " FROM ";
        $query .= "     entexam_judge_tmp t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."'  AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."'  AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";
        $query .= "     t1.judgediv     in ('1','2')";
        return $query;
    }

    //合格者数など取得
    function getCntPassingmark($model) {
        $query  = " WITH T_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT ";
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
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GOUKAKU_CNT, ";
        $query .= "     FUGOUKAKU_CNT ";
        $query .= " FROM ";
        $query .= "     T_CNT ";
        return $query;
    }

    //シミュレーションSQL
    function simSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        //1:合格 2:不合格 //TOTAL4:総合点
        $query .= "     CASE WHEN T1.JUDGEDIV = '4' THEN '4' ";
        $query .= "          WHEN T1.TOTAL4 >= {$model->field["BORDER_SCORE"]} THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS JUDGEDIV, ";
        $staffcd = STAFFCD;
        $query .= "     '{$staffcd}' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND VALUE(T1.JUDGEDIV,'0') <> '4' "; //4:欠席は対象外
        $query .= "     AND VALUE(T1.JUDGEDIV,'0') <> '5' "; //5:受験不可は対象外

        return $query;
    }

    //シミュレーション
    function sim($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);

        //仮判定データ追加
        $query  = " INSERT INTO entexam_judge_tmp( ";
        $query .= " entexamyear, ";
        $query .= " applicantdiv, ";
        $query .= " testdiv, ";
        $query .= " exam_type, ";
        $query .= " receptno, ";
        $query .= " examno, ";
        $query .= " judgediv, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " ( ";
        //シミュレーションSQL
        $query .= knjl070uQuery::simSql($model);
        $query .= " ) ";
        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //合格コース
    function getCourse($model) {
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
    function decision(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受付データ
        //1:合格 2:不合格
        for ($judge = 1; $judge <= 2; $judge++) {
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
        $course = $db->getOne(knjl070uQuery::getCourse($model));

        //志願者基礎データ
        //1:合格 2:不合格
        for ($judge = 1; $judge <= 2; $judge++) {
            $query  = " UPDATE ";
            $query .= "     entexam_applicantbase_dat t1 ";
            $query .= " SET ";
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
            $query .= "     ( ";
            if ($judge == 1) {
                $query .= "     '".substr($course, 0, 1)."', ";
                $query .= "     '".substr($course, 1, 3)."', ";
                $query .= "     '".substr($course, 4, 4)."', ";
            } else {
                $query .= "     CAST(null AS varchar(1)), ";
                $query .= "     CAST(null AS varchar(3)), ";
                $query .= "     CAST(null AS varchar(4)), ";
            }
            $query .= "     '{$judge}', ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear      = '{$model->year}' ";
            $query .= "     AND t1.applicantdiv = '{$model->applicantdiv}' ";
            $query .= "     AND t1.testdiv      = '{$model->testdiv}' ";
            $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '{$judge}' ) ";
            $db->query($query);
        }

        //合格点マスタ
        $query  = " DELETE FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $db->query($query);

        //固定ALL0
        $course = "00000000";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]              = $model->year;
        $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
        $data["TESTDIV"][TEXT]                  = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]                = "1";
        $data["SHDIV"][TEXT]                    = "1";
        $data["COURSECD"][TEXT]                 = substr($course, 0, 1);
        $data["MAJORCD"][TEXT]                  = substr($course, 1, 3);
        $data["EXAMCOURSECD"][TEXT]             = substr($course, 4, 4);
        $data["BORDER_SCORE"][NUMBER]           = $model->field["BORDER_SCORE"];
        $data["SUCCESS_CNT"][NUMBER]            = $model->field["GOUKAKU_CNT"];
        $data["BACK_RATE"][NUMBER]              = $model->field["MODORI_RITU"];
        $data["CAPA_CNT"][NUMBER]               = $model->field["SHUUYOU_CNT"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>
