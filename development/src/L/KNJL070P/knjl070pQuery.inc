<?php

require_once('for_php7.php');

class knjl070pQuery extends Query {

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

    //面接点(固定)
    function getMensetsudiv() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:面接なし') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:面接あり') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //合格点マスタ
    function selectQueryPassingmark($model) {
        $query  = " SELECT ";
        $query .= "     CASE WHEN SHDIV = '9' THEN '0' ELSE SHDIV END AS SORT, ";
        $query .= "     CASE WHEN EXAM_TYPE = '1' THEN '面接なし' ";
        $query .= "          WHEN EXAM_TYPE = '2' THEN '面接あり' ";
        $query .= "     END AS MENSETSUDIV_NAME, ";
        $query .= "     SHDIV AS SEX, ";
        $query .= "     BORDER_SCORE, ";
        $query .= "     BORDER_DEVIATION, ";
        $query .= "     SUCCESS_CNT_SPECIAL     AS SHUTUGAN_CNT, ";
        $query .= "     SUCCESS_CNT_SPECIAL2    AS KESSEKI_CNT, ";
        $query .= "     SUCCESS_CNT             AS GOUKAKU_CNT, ";
        $query .= "     SUCCESS_CNT_CANDI       AS FUGOUKAKU_CNT, ";
        $query .= "     SUCCESS_RATE            AS JITUGOUKAKU_RITU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV= '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV     = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE   = '{$model->mensetsudiv}' ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";
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
        $query .= "     t1.EXAM_TYPE    = '".$model->mensetsudiv."'  AND ";
        $query .= "     t1.judgediv     in ('1','2')";
        return $query;
    }

    //シミュレーションSQL
    function simSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '{$model->mensetsudiv}' AS EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        //1:合格 2:不合格 4:欠席 //TOTAL4:合計面接なし
        $query .= "     CASE WHEN T1.JUDGEDIV = '4' THEN '4' ";
        if ($model->mensetsudiv == "1") { //1:面接なし 2:面接あり
            if ($model->field["BORDER_SCORE"] && $model->field["BORDER_DEVIATION"]) {
                $query .= "          WHEN T1.TOTAL4 >= {$model->field["BORDER_SCORE"]} AND T1.JUDGE_DEVIATION >= {$model->field["BORDER_DEVIATION"]} THEN '1' ";
            } else if ($model->field["BORDER_SCORE"]) {
                $query .= "          WHEN T1.TOTAL4 >= {$model->field["BORDER_SCORE"]} THEN '1' ";
            } else if ($model->field["BORDER_DEVIATION"]) {
                $query .= "          WHEN T1.JUDGE_DEVIATION >= {$model->field["BORDER_DEVIATION"]} THEN '1' ";
            }
        } else {
            if ($model->field["BORDER_SCORE"] && $model->field["BORDER_DEVIATION"]) {
                $query .= "          WHEN T1.TOTAL2 >= {$model->field["BORDER_SCORE"]} AND T1.LINK_JUDGE_DEVIATION >= {$model->field["BORDER_DEVIATION"]} THEN '1' ";
            } else if ($model->field["BORDER_SCORE"]) {
                $query .= "          WHEN T1.TOTAL2 >= {$model->field["BORDER_SCORE"]} THEN '1' ";
            } else if ($model->field["BORDER_DEVIATION"]) {
                $query .= "          WHEN T1.LINK_JUDGE_DEVIATION >= {$model->field["BORDER_DEVIATION"]} THEN '1' ";
            }
        }
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
//        $query .= "     AND VALUE(T1.JUDGEDIV,'0') <> '4' "; //4:欠席は対象外

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
        $query .= knjl070pQuery::simSql($model);
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
            $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '{$judge}' ) ";
            $db->query($query);
        }

        //合格コース
        $course = $db->getOne(knjl070pQuery::getCourse($model));

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
        $query  = " DELETE FROM entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "         entexamyear  = '{$model->year}' ";
        $query .= "     AND applicantdiv = '{$model->applicantdiv}' ";
        $query .= "     AND testdiv      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->mensetsudiv}' ";
        $db->query($query);

        //固定ALL0
        $course = "00000000";
        for ($s = 0; $s <= 2; $s++) {
            $sex = ($s == 0) ? 9 : $s;
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]              = $model->year;
            $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
            $data["TESTDIV"][TEXT]                  = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]                = $model->mensetsudiv;
            $data["SHDIV"][TEXT]                    = $sex;
            $data["COURSECD"][TEXT]                 = substr($course, 0, 1);
            $data["MAJORCD"][TEXT]                  = substr($course, 1, 3);
            $data["EXAMCOURSECD"][TEXT]             = substr($course, 4, 4);
            $data["BORDER_SCORE"][NUMBER]           = $model->field["BORDER_SCORE"];
            $data["BORDER_DEVIATION"][NUMBER]       = $model->field["BORDER_DEVIATION"];
            $data["SUCCESS_CNT_SPECIAL"][NUMBER]    = $model->field["SHUTUGAN_CNT".$sex];
            $data["SUCCESS_CNT_SPECIAL2"][NUMBER]   = $model->field["KESSEKI_CNT".$sex];
            $data["SUCCESS_CNT"][NUMBER]            = $model->field["GOUKAKU_CNT".$sex];
            $data["SUCCESS_CNT_CANDI"][NUMBER]      = $model->field["FUGOUKAKU_CNT".$sex];
            $data["SUCCESS_RATE"][NUMBER]           = $model->field["JITUGOUKAKU_RITU".$sex];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
            $db->query($query);

        }//for

        if ($model->field["BORDER_SCORE"] == '') {
            $model->field["BORDER_SCORE"] = $db->getOne(knjl070pQuery::katahouSql($model, "BORDER_SCORE"));

            $data = array();
            $data["BORDER_SCORE"][NUMBER] = $model->field["BORDER_SCORE"];

            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
            $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $where .= "       TESTDIV       = '".$model->testdiv."' AND ";
            $where .= "       EXAM_TYPE     = '".$model->mensetsudiv."' AND ";
            $where .= "       SHDIV         = '9' AND ";
            $where .= "       COURSECD      = '".substr($course, 0, 1)."' AND ";
            $where .= "       MAJORCD       = '".substr($course, 1, 3)."' AND ";
            $where .= "       EXAMCOURSECD  = '".substr($course, 4, 4)."' ";
            $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
            $db->query($query);

        } else if ($model->field["BORDER_DEVIATION"] == '') {
            $model->field["BORDER_DEVIATION"] = $db->getOne(knjl070pQuery::katahouSql($model, "BORDER_DEVIATION"));

            $data = array();
            $data["BORDER_DEVIATION"][NUMBER] = $model->field["BORDER_DEVIATION"];

            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
            $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $where .= "       TESTDIV       = '".$model->testdiv."' AND ";
            $where .= "       EXAM_TYPE     = '".$model->mensetsudiv."' AND ";
            $where .= "       SHDIV         = '9' AND ";
            $where .= "       COURSECD      = '".substr($course, 0, 1)."' AND ";
            $where .= "       MAJORCD       = '".substr($course, 1, 3)."' AND ";
            $where .= "       EXAMCOURSECD  = '".substr($course, 4, 4)."' ";
            $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //合格ライン、合計偏差値ラインの指定されなかった一方を合格者から算出
    function katahouSql($model, $katahouFlg) {
        $query  = " SELECT ";
        //1:合格 2:不合格 4:欠席 //TOTAL4:合計面接なし
        if ($model->mensetsudiv == "1") { //1:面接なし 2:面接あり
            if ($katahouFlg == "BORDER_SCORE") {
                $query .= "          MIN(T1.TOTAL4) AS MIN_TOTAL ";
            } else if ($katahouFlg == "BORDER_DEVIATION") {
                $query .= "          MIN(T1.JUDGE_DEVIATION) AS MIN_DEVIATION ";
            }
        } else {
            if ($katahouFlg == "BORDER_SCORE") {
                $query .= "          MIN(T1.TOTAL2) AS MIN_TOTAL ";
            } else if ($katahouFlg == "BORDER_DEVIATION") {
                $query .= "          MIN(T1.LINK_JUDGE_DEVIATION) AS MIN_DEVIATION ";
            }
        }
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
        $query .= "     AND T1.JUDGEDIV     = '1' "; // 合格者

        return $query;
    }

    //出願者数など取得
    function getCntPassingmark($model) {
        $query  = " WITH T_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SUM(CASE WHEN T2.SEX = '1' THEN 1 ELSE 0 END) AS SHUTUGAN_CNT1, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '1' AND T1.JUDGEDIV = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT1, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '1' AND T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '1' AND T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT1, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '2' THEN 1 ELSE 0 END) AS SHUTUGAN_CNT2, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '2' AND T1.JUDGEDIV = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT2, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '2' AND T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT2, ";
        $query .= "         SUM(CASE WHEN T2.SEX = '2' AND T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT2, ";
        $query .= "         COUNT(*) AS SHUTUGAN_CNT9, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '4' THEN 1 ELSE 0 END) AS KESSEKI_CNT9, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT9, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT9 ";
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
        $query .= "     SHUTUGAN_CNT1, ";
        $query .= "     KESSEKI_CNT1, ";
        $query .= "     GOUKAKU_CNT1, ";
        $query .= "     FUGOUKAKU_CNT1, ";
        $query .= "     CASE WHEN 0 < (SHUTUGAN_CNT1-KESSEKI_CNT1) ";
        $query .= "          THEN DECIMAL(ROUND((FLOAT(GOUKAKU_CNT1)/(SHUTUGAN_CNT1-KESSEKI_CNT1)*100)*10,0)/10,5,1) ";
        $query .= "          ELSE 0.0 ";
        $query .= "     END AS JITUGOUKAKU_RITU1, ";
        $query .= "     SHUTUGAN_CNT2, ";
        $query .= "     KESSEKI_CNT2, ";
        $query .= "     GOUKAKU_CNT2, ";
        $query .= "     FUGOUKAKU_CNT2, ";
        $query .= "     CASE WHEN 0 < (SHUTUGAN_CNT2-KESSEKI_CNT2) ";
        $query .= "          THEN DECIMAL(ROUND((FLOAT(GOUKAKU_CNT2)/(SHUTUGAN_CNT2-KESSEKI_CNT2)*100)*10,0)/10,5,1) ";
        $query .= "          ELSE 0.0 ";
        $query .= "     END AS JITUGOUKAKU_RITU2, ";
        $query .= "     SHUTUGAN_CNT9, ";
        $query .= "     KESSEKI_CNT9, ";
        $query .= "     GOUKAKU_CNT9, ";
        $query .= "     FUGOUKAKU_CNT9, ";
        $query .= "     CASE WHEN 0 < (SHUTUGAN_CNT9-KESSEKI_CNT9) ";
        $query .= "          THEN DECIMAL(ROUND((FLOAT(GOUKAKU_CNT9)/(SHUTUGAN_CNT9-KESSEKI_CNT9)*100)*10,0)/10,5,1) ";
        $query .= "          ELSE 0.0 ";
        $query .= "     END AS JITUGOUKAKU_RITU9 ";
        $query .= " FROM ";
        $query .= "     T_CNT ";
        return $query;
    }
}
?>
