<?php

require_once('for_php7.php');

class knjl070oQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd1,namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 IN ('".implode($namecd1, "','")."')
                   ORDER BY namecd1,namecd2,name1 ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //仮判定データ(シミュレーション実行有無のチェック)
    function selectQueryJudge_tmp(&$model)
    {
        $query = " SELECT * FROM entexam_judge_tmp t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear     = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     t1.testdiv         = '".$model->testdiv."' AND ";
        $query .= "     t1.judge_exam_type = '".$model->exam_type."' AND ";
        $query .= "     t1.judge_shdiv     = '{$model->shdiv}' AND ";
        $query .= "     EXISTS ( SELECT 'X' FROM ";
        $query .= "                     entexam_applicantbase_dat w1, ";
        $query .= "                     entexam_wishdiv_mst       w2 ";
        $query .= "               WHERE ";
        $query .= "                 w2.entexamyear  = '".$model->year."' AND ";
        $query .= "                 w1.examno       = t1.examno AND ";
        $query .= "                 w2.entexamyear  = w1.entexamyear AND ";
        $query .= "                 w2.desirediv    = w1.desirediv AND ";
        $query .= "                 w2.coursecd || w2.majorcd || w2.examcoursecd = '".$model->course."' ";
        $query .= "            ) ";

        return $query;
    }
    //合格点マスタ
    function selectQueryPassingmark(&$model)
    {
        $query  = " SELECT ";
        $query .= "     entexamyear, ";
        $query .= "     applicantdiv, ";
        $query .= "     testdiv, ";
        $query .= "     exam_type, ";
        $query .= "     shdiv, ";
        $query .= "     coursecd || majorcd || examcoursecd AS course, ";
        $query .= "     border_score, ";
        $query .= "     success_cnt, ";
        $query .= "     back_rate, ";
        $query .= "     capa_cnt ";
        $query .= " FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' AND ";
        $query .= "     exam_type       = '".$model->exam_type."' AND ";
        $query .= "     shdiv           = '{$model->shdiv}' AND ";
        $query .= "     coursecd || majorcd || examcoursecd = '".$model->course."' ";

        return $query;
    }
    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt(&$model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(DISTINCT examno) as success_cnt";
        $query .= " FROM ";
        $query .= "     entexam_judge_tmp t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."'  AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."'  AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";
        $query .= "     t1.judge_shdiv  = '{$model->shdiv}' AND ";
        $query .= "     t1.judgediv     = '1'";

        return $query;
    }
    //合格点マスタ
    function selectQueryPassingmarkAll(&$model)
    {
        $query  = " SELECT ";
        $query .= "     t1.entexamyear, ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t1.examcourse_name, ";
        $query .= "     t1.capacity, ";
        $query .= "     t2.applicantdiv, ";
        $query .= "     t2.testdiv, ";
        $query .= "     N1.NAME     AS TESTDIV_NAME, ";
        $query .= "     N1.CAPACITY AS TESTDIV_CAPACITY, ";
        $query .= "     t2.SHDIV, ";
        $query .= "     L064.NAME1  AS SHDIV_NAME, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '2' THEN t2.border_score ELSE NULL END) AS border_score2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '2' THEN t2.success_cnt  ELSE NULL END) AS success_cnt2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '2' THEN t2.back_rate    ELSE NULL END) AS back_rate2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '2' THEN t2.capa_cnt     ELSE NULL END) AS capa_cnt2 ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst      t1,";
        $query .= "     entexam_passingmark_mst t2 ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST N1 ON N1.ENTEXAMYEAR  = t2.entexamyear ";
        $query .= "                                     AND N1.TESTDIV      = t2.testdiv ";
        $query .= "     LEFT JOIN NAME_MST L064 ON L064.NAMECD1 = 'L064' ";
        $query .= "          AND L064.NAMECD2 = t2.SHDIV ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t2.testdiv      = '".$model->testdiv."' AND ";
        $query .= "     t1.entexamyear  = t2.entexamyear AND ";
        $query .= "     t1.coursecd     = t2.coursecd AND ";
        $query .= "     t1.majorcd      = t2.majorcd AND ";
        $query .= "     t1.examcoursecd = t2.examcoursecd ";
        $query .= " GROUP BY ";
        $query .= "     t1.entexamyear, ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t1.examcourse_name, ";
        $query .= "     t1.capacity, ";
        $query .= "     t2.applicantdiv, ";
        $query .= "     t2.testdiv, ";
        $query .= "     N1.NAME, ";
        $query .= "     N1.CAPACITY, ";
        $query .= "     t2.shdiv, ";
        $query .= "     L064.NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t2.testdiv, ";
        $query .= "     t2.shdiv ";

        return $query;
    }

    //出願コース取得
    function selectQueryCourse(&$model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t3.coursecd, ";
        $query .= "     t3.majorcd, ";
        $query .= "     t3.examcoursecd, ";
        $query .= "     t3.examcourse_name ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         t1.entexamyear, ";
        $query .= "         t1.desirediv ";
        $query .= "     FROM ";
        $query .= "         entexam_applicantbase_dat t1 ";
        $query .= "     WHERE ";
        $query .= "         examno IN (SELECT examno ";
        $query .= "                      FROM entexam_recept_dat ";
        $query .= "                     WHERE ";
        $query .= "                           entexamyear  = '".$model->year."' AND ";
        $query .= "                           applicantdiv = '".$model->applicantdiv."' AND ";
        if ($model->exam_type == "2"){  //４科目で判定
            $query .= "                           exam_type    = '".$model->exam_type."' AND ";
        }
        $query .= "                               testdiv  = '".$model->testdiv."'  ";
        $query .= "                   ) ";
        $query .= "     ) t1, ";
        $query .= "     entexam_wishdiv_mst t2, ";
        $query .= "     entexam_course_mst  t3 ";
        $query .= " WHERE ";
        $query .= "     t2.entexamyear  = t1.entexamyear AND ";
        $query .= "     t2.desirediv    = t1.desirediv AND ";
        $query .= "     t3.entexamyear  = t2.entexamyear AND ";
        $query .= "     t3.coursecd     = t2.coursecd AND ";
        $query .= "     t3.majorcd      = t2.majorcd AND ";
        $query .= "     t3.examcoursecd = t2.examcoursecd and";
        $query .= "     t2.wishno       = '1'";
        $query .= " ORDER BY ";
        $query .= "     t3.coursecd, ";
        $query .= "     t3.majorcd, ";
        $query .= "     t3.examcoursecd ";

        return $query;
    }
    //-----------------------------------
    //シミュレーション処理実行
    //-----------------------------------
    function sim(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);

        $query  = " INSERT INTO entexam_judge_tmp( ";
        $query .= " entexamyear, ";
        $query .= " applicantdiv, ";
        $query .= " testdiv, ";
        $query .= " exam_type, ";
        $query .= " receptno, ";
        $query .= " examno, ";
        $query .= " judgediv, ";
        $query .= " judge_exam_type, ";
        $query .= " judge_shdiv, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "     entexamyear, ";
        $query .= "     applicantdiv, ";
        $query .= "     testdiv, ";
        $query .= "     exam_type, ";
        $query .= "     receptno, ";
        $query .= "     examno, ";
        if ($model->exam_type == '1'){
            $query .= "     CASE WHEN total2 >= ".$model->field["BORDER_SCORE"]." THEN '1' ELSE '2' END AS judgediv, "; //2科目判定
        }else{
            $query .= "     CASE WHEN total4 >= ".$model->field["BORDER_SCORE"]." THEN '1' ELSE '2' END AS judgediv, "; //4科目判定
        }
        $query .= "     '" .$model->exam_type."', ";
        $query .= "     '{$model->shdiv}', ";
        $query .= "     '" .STAFFCD."' ";
        $query .= " FROM ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' AND ";
        if ($model->shdiv != '1'){ //４科目型の受験者
            $query .= "     value(judgediv, '0') != '1' AND ";
        }
        if ($model->exam_type == '2'){ //４科目型の受験者
            $query .= "     exam_type       = '".$model->exam_type."' AND ";
        }
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 entexam_applicantbase_dat w1, ";
        $query .= "                 entexam_wishdiv_mst       w2 ";
        $query .= "             WHERE ";
        $query .= "                 w2.entexamyear  = '".$model->year."' AND ";
        $query .= "                 w1.examno       = t1.examno AND ";
        $query .= "                 w2.entexamyear  = w1.entexamyear AND ";
        $query .= "                 w2.desirediv    = w1.desirediv AND ";
        $query .= "                 w2.coursecd || w2.majorcd || w2.examcoursecd = '".$model->course."' ";
        $query .= "             ) ";
        $query .= " ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
    //特科選抜フラグ 名称マスタ「L004」の予備３
    function getSpecialClassFlg(&$model)
    {
        $query = "SELECT CLASSDIV AS namespare3 FROM ENTEXAM_TESTDIV_MST
                   WHERE ENTEXAMYEAR = '".$model->year."'
                     AND TESTDIV     = '".$model->testdiv."' ";
        return $query;
    }
    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //クラス区分 1:特別進学クラス、2:英数特科クラス、null:特待生選抜入試の正規合格者（入学手続時にクラス確定）
        $classCd = $db->getOne(knjl070oQuery::getSpecialClassFlg($model));

        //シミュレーションから受付データへ反映
        $query  = " UPDATE ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.judge_exam_type,t1.judgediv,t1.judgeline,t1.judgeclass,t1.honordiv) = ";
        $query .= "     (SELECT ";
        $query .= "         st1.judge_exam_type, ";
        $query .= "         st1.judgediv, ";
        $query .= "         st1.judgediv as judgeline, ";
        if ($classCd == "1") {
            //特別進学入試の場合
            $query .= "         case when st1.judgediv = '1' then '".$classCd."' end as judgeclass, ";
            if ($model->shdiv == '1') {
                $query .= "         case when st1.judgediv = '1' then '1' end as honordiv "; //正規合格者は1:特待とする。
            } else {
                $query .= "         cast(null as varchar(1)) as honordiv ";
            }
        } else if ($classCd == "2") {
            //英数特化入試の場合
            $setClassCd = $model->shdiv == '3' ? '1' : '2';
            $query .= "         case when st1.judgediv = '1' then '".$setClassCd."' end as judgeclass, ";
            if ($model->shdiv == '1') {
                $query .= "         case when st1.judgediv = '1' then '1' end as honordiv "; //正規合格者は1:特待とする。
            } else {
                $query .= "         cast(null as varchar(1)) as honordiv ";
            }
        } else {
            //特待生選抜入試の場合
            if ($model->shdiv == '1') {
                //特待Tの場合
                $query .= "         case when st1.judgediv = '1' then '5' end as judgeclass, "; //5:正規合格者
                $query .= "         case when st1.judgediv = '1' then '1' end as honordiv "; //正規合格者は1:特待とする。
            } else {
                //2：T、3：Sの場合
                $setClassCd = $model->shdiv == '3' ? '1' : '2';
                $query .= "         case when st1.judgediv = '1' then '".$setClassCd."' end as judgeclass, ";
                if ($model->shdiv == '1') {
                    $query .= "         case when st1.judgediv = '1' then '1' end as honordiv "; //正規合格者は1:特待とする。
                } else {
                    $query .= "         cast(null as varchar(1)) as honordiv ";
                }
            }
        }
        $query .= "       FROM entexam_judge_tmp st1 ";
        $query .= "      WHERE ";
        $query .= "         st1.entexamyear     = t1.entexamyear AND ";
        $query .= "         st1.applicantdiv    = t1.applicantdiv  AND ";
        $query .= "         st1.testdiv         = t1.testdiv AND ";
        $query .= "         st1.exam_type       = t1.exam_type AND ";
        $query .= "         st1.receptno        = t1.receptno AND ";
        $query .= "         st1.examno          = t1.examno ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."' AND";
        $query .= "     EXISTS (SELECT 'x' FROM entexam_judge_tmp st2 ";
        $query .= "              WHERE ";
        $query .= "                    st2.entexamyear     = t1.entexamyear AND ";
        $query .= "                    st2.applicantdiv    = t1.applicantdiv  AND ";
        $query .= "                    st2.testdiv         = t1.testdiv AND ";
        $query .= "                    st2.exam_type       = t1.exam_type AND ";
        $query .= "                    st2.receptno        = t1.receptno AND ";
        $query .= "                    st2.examno          = t1.examno ";
        $query .= "            ) ";

        $db->query($query);

        //更新処理1（初期値を保持する）
        //「今回入試区分(REMARK1)」と「入試区分コンボで選択された入試区分」が不一致の時、
        //「今回入試区分(REMARK1)」に「入試区分コンボで選択された入試区分」をセット
        //「前回入学クラス(REMARK2)」に「ENTEXAM_APPLICANTBASE_DATのENTCLASS」をセット

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     EXAMNO, ";
        $query .= "     SEQ, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     TMP.ENTEXAMYEAR, ";
        $query .= "     TMP.EXAMNO, ";
        $query .= "     '037' AS SEQ, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP TMP ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND B1.EXAMNO = TMP.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ON BD.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND BD.EXAMNO = TMP.EXAMNO AND BD.SEQ = '037' ";
        $query .= " WHERE ";
        $query .= "     BD.EXAMNO IS NULL ";

        $db->query($query);

        $query  = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT UPD_BD ";
        $query .= " SET ";
        $query .= " ( ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) = ( ";
        $query .= "     SELECT ";
        $query .= "         '{$model->testdiv}' AS REMARK1, ";
        $query .= "         B1.ENTCLASS AS REMARK2, ";
        $query .= "         '".STAFFCD."' AS REGISTERCD, ";
        $query .= "         sysdate() AS UPDATED ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_JUDGE_TMP TMP ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND B1.EXAMNO = TMP.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ON BD.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND BD.EXAMNO = TMP.EXAMNO AND BD.SEQ = '037' ";
        $query .= "     WHERE ";
        $query .= "         VALUE(BD.REMARK1,'') != '{$model->testdiv}' ";
        $query .= "         AND BD.ENTEXAMYEAR = UPD_BD.ENTEXAMYEAR ";
        $query .= "         AND BD.EXAMNO = UPD_BD.EXAMNO ";
        $query .= "         AND BD.SEQ = UPD_BD.SEQ ";
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "     UPD_BD.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND UPD_BD.SEQ = '037' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_JUDGE_TMP TMP ";
        $query .= "             LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND B1.EXAMNO = TMP.EXAMNO ";
        $query .= "             LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ON BD.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND BD.EXAMNO = TMP.EXAMNO AND BD.SEQ = '037' ";
        $query .= "         WHERE ";
        $query .= "             VALUE(BD.REMARK1,'') != '{$model->testdiv}' ";
        $query .= "             AND BD.ENTEXAMYEAR = UPD_BD.ENTEXAMYEAR ";
        $query .= "             AND BD.EXAMNO = UPD_BD.EXAMNO ";
        $query .= "             AND BD.SEQ = UPD_BD.SEQ ";
        $query .= "     ) ";

        $db->query($query);

        //更新処理2（一旦、初期値に戻す）
        //「ENTEXAM_APPLICANTBASE_DATのENTCLASS」に「前回入学クラス(REMARK2)」をセット

        $query  = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT UPD_B1 ";
        $query .= " SET ";
        $query .= " ( ";
        $query .= "     ENTCLASS, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) = ( ";
        $query .= "     SELECT ";
        $query .= "         BD.REMARK2 AS ENTCLASS, ";
        $query .= "         '".STAFFCD."' AS REGISTERCD, ";
        $query .= "         sysdate() AS UPDATED ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_JUDGE_TMP TMP ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND B1.EXAMNO = TMP.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ON BD.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND BD.EXAMNO = TMP.EXAMNO AND BD.SEQ = '037' ";
        $query .= "     WHERE ";
        $query .= "         B1.ENTEXAMYEAR = UPD_B1.ENTEXAMYEAR ";
        $query .= "         AND B1.EXAMNO = UPD_B1.EXAMNO ";
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "     UPD_B1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND UPD_B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_JUDGE_TMP TMP ";
        $query .= "             LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND B1.EXAMNO = TMP.EXAMNO ";
        $query .= "             LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD ON BD.ENTEXAMYEAR = TMP.ENTEXAMYEAR AND BD.EXAMNO = TMP.EXAMNO AND BD.SEQ = '037' ";
        $query .= "         WHERE ";
        $query .= "             B1.ENTEXAMYEAR = UPD_B1.ENTEXAMYEAR ";
        $query .= "             AND B1.EXAMNO = UPD_B1.EXAMNO ";
        $query .= "     ) ";

        $db->query($query);

        //更新処理3（現状の更新処理）

        //志願者基礎の判定更新
        //RECEPTD_DATで一回も合格していない生徒を2:不合格で登録
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,entclass,updated) = (null,null,null,'2',null,sysdate()) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '2' ) AND ";  //シミュレーションで不合格
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear            = t1.entexamyear ";
        $query .= "                                AND COALESCE(t2.judgediv,'0') ='1'";                             //全受付データで１度も合格したことが無い人
        $query .= "                            )";

        $db->query($query);

        //１度でも合格した生徒の合格学科を志望区分マスタより取得して更新
        //特待生選抜入試の場合は、entclassを2で上書きする。
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        if ($classCd == "1") {
            //特別進学入試の場合
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,entclass,updated) = ";
            $query .= "     (SELECT w.coursecd, w.majorcd, w.examcoursecd, '1', '".$classCd."', sysdate() ";
        } else if ($classCd == "2") {
            //英数特化入試の場合
            $setClassCd = $model->shdiv == '3' ? '1' : '2';
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,entclass,updated) = ";
            $query .= "     (SELECT w.coursecd, w.majorcd, w.examcoursecd, '1', '".$setClassCd."', sysdate() ";
        } else {
            //特待生選抜入試の場合
            if ($model->shdiv == '1') {
                //特待Tの場合
                $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,entclass,updated) = ";
                $query .= "     (SELECT w.coursecd, w.majorcd, w.examcoursecd, '1', '2', sysdate() ";
            } else {
                //2：T、3：Sの場合
                $setClassCd = $model->shdiv == '3' ? '1' : '2';
                $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,entclass,updated) = ";
                $query .= "     (SELECT w.coursecd, w.majorcd, w.examcoursecd, '1', '".$setClassCd."', sysdate() ";
            }
        }
        $query .= "        FROM entexam_wishdiv_mst w ";
        $query .= "       WHERE ";
        $query .= "         w.entexamyear = t1.entexamyear AND ";
        $query .= "         w.desirediv   = t1.desirediv AND ";
        $query .= "         w.wishno      = '1' ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv ."' AND ";
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '1' ) ";  //シミュレーションで合格
        $query .= "     AND value(t1.entclass,'0') NOT IN ('2') "; //下位のクラスで、上位の上書きはしない。

        $db->query($query);

        //合格点マスタへシミュレーション結果の保存
        $query  = " DELETE FROM entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv      = '".$model->testdiv."' AND ";
        $query .= "     shdiv        = '{$model->shdiv}' AND ";
        if ($model->exam_type == '2'){
            $query .= "     EXAM_TYPE = '".$model->exam_type."' AND ";
        }
        $query .= "     coursecd || majorcd || examcoursecd  = '".$model->course."' ";

        $db->query($query);

        $query = " INSERT INTO entexam_passingmark_mst( ";
        $query .= "     entexamyear, ";     //年度
        $query .= "     applicantdiv, ";    //試験制度
        $query .= "     testdiv, ";         //出願区分
        $query .= "     exam_type, ";       //判定科目(２or４科目)
        $query .= "     shdiv, ";           //専併区分(未使用)
        $query .= "     coursecd, ";        //課程コード
        $query .= "     majorcd, ";         //学科コード
        $query .= "     examcoursecd, ";    //コースコード
        $query .= "     border_score, ";    //合格点
        $query .= "     success_cnt, ";     //合格者数
        $query .= "     back_rate, ";       //戻り率
        $query .= "     capa_cnt, ";        //収容人数
        $query .= "     registercd ";       //登録者コード
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= "     '" .$model->year ."', ";
        $query .= "     '" .$model->applicantdiv ."', ";
        $query .= "     '" .$model->testdiv ."', ";
        $query .= "     '" .$model->exam_type ."', ";
        $query .= "     '{$model->shdiv}', ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     " .$model->field["BORDER_SCORE"] .", ";
        $query .= "     " .$model->field["SUCCESS_CNT"] .", ";
        $query .= "     " .$model->field["BACK_RATE"] .", ";
        $query .= "     " .$model->field["CAPA_CNT"] .", ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);


        //仮判定データすべて削除
        $query = " DELETE FROM entexam_judge_tmp ";

        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>


