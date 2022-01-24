<?php

require_once('for_php7.php');

class knjl070jQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query = "SELECT namecd1,namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 IN ('".implode($namecd1, "','")."')
                   ORDER BY namecd1,namecd2,name1 ";
        return $query;
    }

    //仮判定データ(シミュレーション実行有無のチェック)
    function selectQueryJudge_tmp(&$model)
    {
        $query = " SELECT * FROM entexam_judge_tmp t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear     = '".$model->year."' AND ";
        $query .= "     t1.testdiv         = '".$model->testdiv."' ";

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
        $query .= "     border_deviation, ";
        $query .= "     success_cnt, ";
        $query .= "     back_rate, ";
        $query .= "     capa_cnt ";
        $query .= " FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     applicantdiv    = '1' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' AND ";
        $query .= "     exam_type       = '1' AND ";
        $query .= "     shdiv           = '1' AND ";
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
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";
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
        $query .= "     t2.shdiv, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '0' THEN t2.border_deviation ELSE NULL END) AS border_deviation1, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '0' THEN t2.success_cnt  ELSE NULL END) AS success_cnt1, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '0' THEN t2.back_rate    ELSE NULL END) AS back_rate1, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '0' THEN t2.capa_cnt     ELSE NULL END) AS capa_cnt1, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '1' THEN t2.border_deviation ELSE NULL END) AS border_deviation2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '1' THEN t2.success_cnt  ELSE NULL END) AS success_cnt2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '1' THEN t2.back_rate    ELSE NULL END) AS back_rate2, ";
        $query .= "     MAX(CASE t2.exam_type WHEN '1' THEN t2.capa_cnt     ELSE NULL END) AS capa_cnt2 ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst      t1,";
        $query .= "     entexam_passingmark_mst t2 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '1' AND ";
//        $query .= "     t1.testdiv      = '".$model->testdiv."' AND ";
        $query .= "     t1.entexamyear  = t2.entexamyear AND ";
        $query .= "     t1.applicantdiv = t2.applicantdiv AND ";
        $query .= "     t1.testdiv      = t2.testdiv AND ";
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
        $query .= "     t2.shdiv ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t2.testdiv ";

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
        $query .= "     entexam_course_mst  t3 ";
        $query .= " WHERE ";
        $query .= "     t3.entexamyear  = '".$model->year."' AND ";
        $query .= "     t3.applicantdiv = '1' AND ";
        $query .= "     t3.testdiv      = '".$model->testdiv."' ";
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
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "     entexamyear, ";
        $query .= "     applicantdiv, ";
        $query .= "     testdiv, ";
        $query .= "     exam_type, ";
        $query .= "     receptno, ";
        $query .= "     examno, ";
        $query .= "     CASE WHEN judge_deviation >= ".$model->field["BORDER_DEVIATION"]." THEN '1' ELSE '2' END AS judgediv, "; //合格・不合格
        $query .= "     exam_type, ";
        $query .= "     '" .STAFFCD."' ";
        $query .= " FROM ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' ";
        $query .= " ) ";

        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }
    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //シミュレーションから受付データへ反映
        $query  = " UPDATE ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.judge_exam_type,t1.judgediv) = ";
        $query .= "     (SELECT ";
        $query .= "         st1.judge_exam_type, ";
        $query .= "         st1.judgediv ";
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

        //志願者基礎の判定更新
        //RECEPTD_DATで一回も合格していない生徒を2:不合格で登録
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,updated) = (null,null,null,'2',sysdate()) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '2' ) AND ";  //シミュレーションで不合格
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear            = t1.entexamyear ";
        $query .= "                                AND COALESCE(t2.judgediv,'0') ='1'";                             //全受付データで１度も合格したことが無い人
        $query .= "                            )";

        $db->query($query);

        //１度でも合格した生徒の合格学科を更新
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,special_measures,updated) = ";
        $query .= "     ( ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     '1', ";
        $query .= "     null, ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '1' ) AND ";  //シミュレーションで合格
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear            = t1.entexamyear ";
        $query .= "                                AND integer(t2.testdiv)       < integer('".$model->testdiv."')"; //過去に
        $query .= "                                AND COALESCE(t2.judgediv,'0') ='1'";                                  //過去に１度も合格したことが無い人
        $query .= "                            )";

        $db->query($query);

        //合格点マスタへシミュレーション結果の保存
        $query  = " DELETE FROM entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
//        $query .= "     applicantdiv = '1' AND ";
        $query .= "     testdiv      = '".$model->testdiv."' AND ";
//        $query .= "     exam_type       = '1' AND ";
//        $query .= "     shdiv        = '1' AND ";
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
        $query .= "     border_deviation, ";//合格点（判定偏差値）
        $query .= "     success_cnt, ";     //合格者数
        $query .= "     back_rate, ";       //戻り率
        $query .= "     capa_cnt, ";        //収容人数
        $query .= "     registercd ";       //登録者コード
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= "     '" .$model->year ."', ";
        $query .= "     '1', ";
        $query .= "     '" .$model->testdiv ."', ";
        $query .= "     '1', ";
        $query .= "     '1', ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     " .$model->field["BORDER_DEVIATION"] .", ";
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


