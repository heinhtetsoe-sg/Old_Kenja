<?php

require_once('for_php7.php');

class knjl070rQuery extends Query {

    //名称マスタよりデータを取得
    function getName($year, $namecd1, $namecd2 = "")
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

    //合格点マスタ
    function selectQueryPassingmark($model, $course, $desirediv = "", $shdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     border_score, ";
        $query .= "     success_cnt, ";
        $query .= "     success_cnt_special, ";
        $query .= "     success_cnt_special2 ";
        $query .= " FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' AND ";
        if (strlen($desirediv)) { //確定結果一覧
            $query .= "     exam_type       = '".$desirediv."' AND ";
        } else {
            $query .= "     exam_type       = '".$model->desirediv."' AND ";
        }
        if (strlen($shdiv)) { //確定結果一覧
            $query .= "     shdiv           = '".$shdiv."' AND ";
        } else {
            $query .= "     shdiv           = '".$model->shdiv."' AND ";
        }
        $query .= "     coursecd || majorcd || examcoursecd = '".$course."' ";

        return $query;
    }

    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt($model, $wishno = "", $judgediv = "")
    {
        $query  = " SELECT ";
        $query .= "     sum(case when t1.judgediv in ('1','8') then 1 else 0 end) as success_cnt, ";
        $query .= "     sum(case when t1.judgediv in ('2') then 1 else 0 end) as success_cnt_no, ";
        $query .= "     sum(case when t1.judgediv in ('1','8') and b1.sex = '1' then 1 else 0 end) as success_cnt_special, ";
        $query .= "     sum(case when t1.judgediv in ('2') and b1.sex = '1' then 1 else 0 end) as success_cnt_special_no, ";
        $query .= "     sum(case when t1.judgediv in ('1','8') and b1.sex = '2' then 1 else 0 end) as success_cnt_special2, ";
        $query .= "     sum(case when t1.judgediv in ('2') and b1.sex = '2' then 1 else 0 end) as success_cnt_special2_no ";
        $query .= " FROM ";
        $query .= "     entexam_judge_tmp t1 ";
        $query .= "     left join entexam_applicantbase_dat b1 on b1.entexamyear = t1.entexamyear and b1.applicantdiv = t1.applicantdiv and b1.examno = t1.examno ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."'  AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."'  AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";
        $query .= "     t1.exam_type    = '".$model->desirediv."'  AND ";
        if (strlen($wishno)) {
            $query .= "     t1.judge_exam_type = '".$wishno."'  AND ";
        }
        if (strlen($judgediv)) {
            $query .= "     t1.judgediv = '{$judgediv}'  AND ";
        }
        $query .= "     t1.judgediv     in ('1','2','8')";

        return $query;
    }

    //受験区分
    function getDesirediv($model) {
        $query  = " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.EXAMCOURSECD, ";
        $query .= "     T2.COURSECD || T2.MAJORCD || T2.EXAMCOURSECD AS EXAMCOURSE, ";
        $query .= "     T2.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.COURSECD = T1.COURSECD ";
        $query .= "         AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "         AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO ";

        return $query;
    }

    //-----------------------------------
    //シミュレーション処理実行
    //-----------------------------------

    //シミュレーションSQL
    function simSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '{$model->desirediv}' AS EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        /*** JUDGEDIV ***/
        //1:合格 2:不合格
        $query .= "     CASE ";
        //2:内諾なしは、C:面接Cランクは、不合格
        if ($model->shdiv != "1") {
            $query .= "      WHEN T5.INTERVIEW_VALUE = 'C' THEN '2' ";
        }
        //2:内諾なしは、1:全教科受験者でなければ不合格
        if ($model->shdiv != "1") {
            $query .= "      WHEN T1.ATTEND_ALL_FLG != '1' THEN '2' ";
        }
        //通常は、合格点以上で合格
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $course = $examcourseArray["EXAMCOURSE"];
            $query .= "      WHEN T1.TOTAL4 >= {$model->field["BORDER_SCORE-".$course]} THEN '1' ";
        }
        //1:内諾ありは、無条件で合格
        if ($model->shdiv == "1") {
            $query .= "      ELSE '1' ";
        } else {
            $query .= "      ELSE '2' ";
        }
        $query .= "          END AS JUDGEDIV, ";
        /*** JUDGE_EXAM_TYPE ***/
        //合格コース取得のため、JUDGE_EXAM_TYPEに志望順位(1,2,3)をセットしておく
        //不合格の場合は、NULLをセットしておく
        $query .= "     CASE ";
        //2:内諾なしは、C:面接Cランクは、不合格
        if ($model->shdiv != "1") {
            $query .= "      WHEN T5.INTERVIEW_VALUE = 'C' THEN CAST(NULL AS VARCHAR(1)) ";
        }
        //2:内諾なしは、1:全教科受験者でなければ不合格
        if ($model->shdiv != "1") {
            $query .= "      WHEN T1.ATTEND_ALL_FLG != '1' THEN CAST(NULL AS VARCHAR(1)) ";
        }
        //通常は、合格点以上で合格
        $wishnoMax = '0';
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $course = $examcourseArray["EXAMCOURSE"];
            $query .= "      WHEN T1.TOTAL4 >= {$model->field["BORDER_SCORE-".$course]} THEN '{$wishno}' ";
            //wishnoがmaxのコースが内諾コース
            $wishnoMax = $wishno;
        }
        //1:内諾ありは、無条件で合格
        if ($model->shdiv == "1") {
            $query .= "      ELSE '{$wishnoMax}' ";
        } else {
            $query .= "      ELSE CAST(NULL AS VARCHAR(1)) ";
        }
        $query .= "          END AS JUDGE_EXAM_TYPE, ";
        $staffcd = STAFFCD; //変数
        $query .= "     '{$staffcd}' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "         ON  T3.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO       = T2.EXAMNO ";
        $query .= "         AND T3.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT T4 ";
        $query .= "         ON  T4.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T4.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T4.TESTDIV = T2.TESTDIV ";
        $query .= "         AND T4.BEFORE_PAGE = T3.REMARK1 ";
        $query .= "         AND T4.BEFORE_SEQ = T3.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT T5 ";
        $query .= "         ON  T5.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T5.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T5.TESTDIV = T2.TESTDIV ";
        $query .= "         AND T5.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND VALUE(T1.JUDGEDIV,'0') <> '3' "; //3:未受験(欠席)は対象外
        $query .= "     AND T2.DESIREDIV = '{$model->desirediv}' "; //受験パターン(1～7)
        //内諾区分(SS,S) 1:あり,2:なし
        $before_course = "00000000";
        if ($model->shdiv == "1") {
            //wishnoがmaxのコースが内諾コース
            foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
                $before_course = $examcourseArray["EXAMCOURSE"];
            }
        }
        $query .= "     AND value(T4.BEFORE_COURSECD,'0') || value(T4.BEFORE_MAJORCD,'000') || value(T4.BEFORE_EXAMCOURSECD,'0000') = '{$before_course}' ";

        return $query;
    }

    function sim($model)
    {
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
        $query .= " judge_exam_type, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " ( ";
        //シミュレーションSQL
        $query .= knjl070rQuery::simSql($model);
        $query .= " ) ";

        $db->query($query);

        //仮判定データ更新（8:ＧＳ補欠合格で上書き）・・・対象(ＧＡ合格者)
        //処理条件(ＧＳ補欠合格点が入力された時)
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ" && strlen($model->field["BORDER_SCORE-".$model->hoketuCourse])) {
                $result = $db->query(knjl070rQuery::simSqlHoketu($model, $wishno));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data = array();
                    $data["JUDGEDIV"][TEXT]     = $row["JUDGEDIV"];
                    $data["UPDATED"][FUNC]      = "sysdate()";
                    $where  = " WHERE ";
                    $where .= "         ENTEXAMYEAR  = '{$model->year}' ";
                    $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
                    $where .= "     AND TESTDIV      = '{$model->testdiv}' ";
                    $where .= "     AND EXAM_TYPE    = '{$row["EXAM_TYPE"]}' ";
                    $where .= "     AND RECEPTNO     = '{$row["RECEPTNO"]}' ";
                    $query = Query::updateSQL($data, "ENTEXAM_JUDGE_TMP", $where);
                    $db->query($query);
                }
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //シミュレーションSQL（8:ＧＳ補欠合格で上書き）
    function simSqlHoketu($model, $wishno) {
        $query  = " SELECT ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     '8' AS JUDGEDIV "; //8:ＧＳ補欠合格
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND R1.TESTDIV = T1.TESTDIV ";
        $query .= "         AND R1.RECEPTNO = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR      = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV     = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV          = '{$model->testdiv}' ";
        //条件1(ＧＡ合格者)
        $query .= "     AND T1.EXAM_TYPE        = '{$model->desirediv}' ";  //受験パターン(1～9)
        $query .= "     AND T1.JUDGE_EXAM_TYPE  = '{$wishno}' ";            //志望順位(1,2,3,4)
        $query .= "     AND T1.JUDGEDIV         = '1' ";                    //1:合格
        //条件2(ＧＳ補欠合格点以上)
        $query .= "     AND R1.TOTAL4          >= {$model->field["BORDER_SCORE-".$model->hoketuCourse]} ";

        return $query;
    }

    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受付データ
        //2:不合格
        $query  = " UPDATE ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.judgediv,updated) = ";
        $query .= "     ( ";
        $query .= "     '2', ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear      = '".$model->year."' ";
        $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "     AND t1.testdiv      = '".$model->testdiv."' ";
        $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '2' ) ";
        $db->query($query);
        //1:合格
        $query  = " UPDATE ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.judgediv,updated) = ";
        $query .= "     ( ";
        $query .= "     '1', ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear      = '".$model->year."' ";
        $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "     AND t1.testdiv      = '".$model->testdiv."' ";
        $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '1' ) ";
        $db->query($query);
        //8:ＧＳ補欠合格（合格コースはＧＡ）
        $query  = " UPDATE ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.judgediv,updated) = ";
        $query .= "     ( ";
        $query .= "     '8', ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear      = '".$model->year."' ";
        $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "     AND t1.testdiv      = '".$model->testdiv."' ";
        $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '8' ) ";
        $db->query($query);

        //志願者基礎データ
        //2:不合格
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
        $query .= "     ( ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(3)), ";
        $query .= "     CAST(null AS varchar(4)), ";
        $query .= "     '2', ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear      = '".$model->year."' ";
        $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '2' ) ";
        $db->query($query);
        //1:合格
        //8:ＧＳ補欠合格（合格コースはＧＡ）
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $course = $examcourseArray["EXAMCOURSE"];

            //1:合格
            $query  = " UPDATE ";
            $query .= "     entexam_applicantbase_dat t1 ";
            $query .= " SET ";
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
            $query .= "     ( ";
            $query .= "     '" .substr($course, 0, 1) ."', ";
            $query .= "     '" .substr($course, 1, 3) ."', ";
            $query .= "     '" .substr($course, 4, 4) ."', ";
            $query .= "     '1', ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear      = '".$model->year."' ";
            $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '1' ";
            $query .= "                                and exam_type = '{$model->desirediv}' and judge_exam_type = '{$wishno}' ) ";
            $db->query($query);
            //8:ＧＳ補欠合格（合格コースはＧＡ）
            $query  = " UPDATE ";
            $query .= "     entexam_applicantbase_dat t1 ";
            $query .= " SET ";
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
            $query .= "     ( ";
            $query .= "     '" .substr($course, 0, 1) ."', ";
            $query .= "     '" .substr($course, 1, 3) ."', ";
            $query .= "     '" .substr($course, 4, 4) ."', ";
            $query .= "     '8', ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear      = '".$model->year."' ";
            $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
            $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '8' ";
            $query .= "                                and exam_type = '{$model->desirediv}' and judge_exam_type = '{$wishno}' ) ";
            $db->query($query);
        }

        //合格点マスタ
        $query  = " DELETE FROM entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv      = '".$model->testdiv."' AND ";
        $query .= "     exam_type    = '".$model->desirediv."' AND ";
        $query .= "     shdiv        = '".$model->shdiv."' ";
        $db->query($query);

        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $course = $examcourseArray["EXAMCOURSE"];
            if (!strlen($model->field["SUCCESS_CNT-" .$course])) $model->field["SUCCESS_CNT-" .$course] = 0;
            if (!strlen($model->field["SUCCESS_CNT_SPECIAL-" .$course])) $model->field["SUCCESS_CNT_SPECIAL-" .$course] = 0;
            if (!strlen($model->field["SUCCESS_CNT_SPECIAL2-" .$course])) $model->field["SUCCESS_CNT_SPECIAL2-" .$course] = 0;

            $query  = " INSERT INTO entexam_passingmark_mst( ";
            $query .= "     entexamyear, ";     //入試年度
            $query .= "     applicantdiv, ";    //入試制度
            $query .= "     testdiv, ";         //入試区分
            $query .= "     exam_type, ";       //受験区分
            $query .= "     shdiv, ";           //事前相談内諾区分
            $query .= "     coursecd, ";        //課程コード
            $query .= "     majorcd, ";         //学科コード
            $query .= "     examcoursecd, ";    //コースコード
            $query .= "     border_score, ";        //合格点
            $query .= "     success_cnt, ";         //合格者数
            $query .= "     success_cnt_special, "; //合格者数(男)
            $query .= "     success_cnt_special2, ";//合格者数(女)
            $query .= "     registercd ";       //登録者コード
            $query .= " ) ";
            $query .= " VALUES ";
            $query .= " ( ";
            $query .= "     '" .$model->year ."', ";
            $query .= "     '" .$model->applicantdiv ."', ";
            $query .= "     '" .$model->testdiv ."', ";
            $query .= "     '" .$model->desirediv ."', ";
            $query .= "     '" .$model->shdiv ."', ";
            $query .= "     '" .substr($course, 0, 1) ."', ";
            $query .= "     '" .substr($course, 1, 3) ."', ";
            $query .= "     '" .substr($course, 4, 4) ."', ";
            $query .= "     " .$model->field["BORDER_SCORE-".$course] .", ";
            $query .= "     " .$model->field["SUCCESS_CNT-" .$course] .", ";
            $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL-" .$course] .", ";
            $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL2-" .$course] .", ";
            $query .= "     '" .STAFFCD ."' ";
            $query .= " ) ";
            $db->query($query);

            //処理条件(ＧＳ補欠合格点が入力された時)
            if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ" && strlen($model->field["BORDER_SCORE-".$model->hoketuCourse])) {
                $course = $model->hoketuCourse;
                if (!strlen($model->field["SUCCESS_CNT-" .$course])) $model->field["SUCCESS_CNT-" .$course] = 0;
                if (!strlen($model->field["SUCCESS_CNT_SPECIAL-" .$course])) $model->field["SUCCESS_CNT_SPECIAL-" .$course] = 0;
                if (!strlen($model->field["SUCCESS_CNT_SPECIAL2-" .$course])) $model->field["SUCCESS_CNT_SPECIAL2-" .$course] = 0;

                $query  = " INSERT INTO entexam_passingmark_mst( ";
                $query .= "     entexamyear, ";     //入試年度
                $query .= "     applicantdiv, ";    //入試制度
                $query .= "     testdiv, ";         //入試区分
                $query .= "     exam_type, ";       //受験区分
                $query .= "     shdiv, ";           //事前相談内諾区分
                $query .= "     coursecd, ";        //課程コード
                $query .= "     majorcd, ";         //学科コード
                $query .= "     examcoursecd, ";    //コースコード
                $query .= "     border_score, ";        //合格点
                $query .= "     success_cnt, ";         //合格者数
                $query .= "     success_cnt_special, "; //合格者数(男)
                $query .= "     success_cnt_special2, ";//合格者数(女)
                $query .= "     registercd ";       //登録者コード
                $query .= " ) ";
                $query .= " VALUES ";
                $query .= " ( ";
                $query .= "     '" .$model->year ."', ";
                $query .= "     '" .$model->applicantdiv ."', ";
                $query .= "     '" .$model->testdiv ."', ";
                $query .= "     '" .$model->desirediv ."', ";
                $query .= "     '" .$model->shdiv ."', ";
                $query .= "     '" .substr($course, 0, 1) ."', ";
                $query .= "     '" .substr($course, 1, 3) ."', ";
                $query .= "     '" .substr($course, 4, 4) ."', ";
                $query .= "     " .$model->field["BORDER_SCORE-".$course] .", ";
                $query .= "     " .$model->field["SUCCESS_CNT-" .$course] .", ";
                $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL-" .$course] .", ";
                $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL2-" .$course] .", ";
                $query .= "     '" .STAFFCD ."' ";
                $query .= " ) ";
                $db->query($query);
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>
