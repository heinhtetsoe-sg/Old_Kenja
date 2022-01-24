<?php

require_once('for_php7.php');

class knjl070yQuery extends Query {

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
        $query  = " SELECT COUNT(*) FROM entexam_judge_tmp t1 ";
        $query .= "     inner join v_entexam_applicantdesire_dat t2 ";
        $query .= "          ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "          AND t2.applicantdiv = t1.applicantdiv ";
        $query .= "          AND t2.testdiv      = t1.testdiv ";
        $query .= "          AND t2.examno       = t1.examno ";
//      $query .= "          AND t2.wishno       = '1' "; //第１志望
        $query .= "          AND t2.coursecd || t2.majorcd || t2.examcoursecd = '{$model->course}' ";
        $query .= "          AND t2.shdiv        = '{$model->shdiv}' ";
        $query .= "     inner join entexam_applicantbase_dat t3 ";
        $query .= "          ON  t3.entexamyear  = t1.entexamyear ";
        $query .= "          AND t3.applicantdiv = t1.applicantdiv ";
        $query .= "          AND t3.examno       = t1.examno ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear     = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     t1.testdiv         = '".$model->testdiv."' AND ";
        $query .= "     t1.judge_exam_type = '".$model->exam_type."' ";
        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        if ($model->kikoku == "2") {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0')  = '1' ";
        } else {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0') != '1' ";
        }

        return $query;
    }
    //専願合格者数を取得
    function getSuccessCnt1(&$model)
    {
        $query  = " SELECT ";
        $query .= "     success_cnt ";
        $query .= " FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '{$model->year}' AND ";
        $query .= "     applicantdiv    = '{$model->applicantdiv}' AND ";
        $query .= "     testdiv         = '{$model->testdiv}' AND ";
        $query .= "     exam_type       = '{$model->exam_type}' AND ";
        $query .= "     shdiv           = '1' AND ";//1:専願
        $query .= "     coursecd || majorcd || examcoursecd = '{$model->course}' ";

        return $query;
    }
    //合格点マスタ
    function selectQueryPassingmark(&$model)
    {
        $query  = " SELECT ";
        $query .= "     entexamyear, ";
        $query .= "     applicantdiv, ";
        $query .= "     testdiv, ";
        $query .= "     exam_type AS KIKOKU, ";
        $query .= "     shdiv, ";
        $query .= "     coursecd || majorcd || examcoursecd AS course, ";
        $query .= "     border_score, ";
        $query .= "     success_cnt, ";//
        $query .= "     back_rate, ";
        $query .= "     capa_cnt, ";//
        $query .= "     border_score_candi, ";
        $query .= "     success_cnt_candi, ";//
        $query .= "     smallint(border_deviation) as border_deviation, ";
        $query .= "     success_cnt_special AS SUCCESS_CNT_CHALLENGE, ";
        $query .= "     success_cnt_special, ";
        $query .= "     success_cnt_special2 ";
        $query .= " FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     applicantdiv    = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv         = '".$model->testdiv."' AND ";
        $query .= "     exam_type       = '".$model->kikoku."' AND ";
        $query .= "     shdiv           = '{$model->shdiv}' AND ";
        $query .= "     coursecd || majorcd || examcoursecd = '".$model->course."' ";

        return $query;
    }
    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt(&$model)
    {
        $query  = " SELECT ";
        $query .= "     sum(case when t1.judgediv in ('1','3','5','9','6') then 1 else 0 end) as success_cnt";
        $query .= "    ,sum(case when t1.judgediv='3' then 1 else 0 end) as success_cnt_candi";
        $query .= "    ,sum(case when t1.judgediv in ('5','9') then 1 else 0 end) as success_cnt_special";
        $query .= "    ,sum(case when t1.judgediv='9' then 1 else 0 end) as success_cnt_special2";
        $query .= "    ,sum(case when t1.judgediv='6' then 1 else 0 end) as SUCCESS_CNT_CHALLENGE";
        $query .= " FROM ";
        $query .= "     entexam_judge_tmp t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."'  AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."'  AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";
        $query .= "     t1.judgediv     in ('1','3','5','9','6')";

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
        $query .= "     t2.applicantdiv, ";
        $query .= "     t2.testdiv, ";
        $query .= "     t2.exam_type AS KIKOKU, ";
        $query .= "     SUM(CASE WHEN t2.testdiv = '3' THEN t2.success_cnt_special ELSE 0 END) AS SUCCESS_CNT_CHALLENGE, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.shdiv ELSE NULL END) AS shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.back_rate ELSE NULL END) AS back_rate_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.border_score ELSE NULL END) AS border_score2_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt  ELSE NULL END) AS success_cnt2_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt_candi  ELSE NULL END) AS success_cnt_candi2_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN smallint(t2.border_deviation)  ELSE NULL END) AS border_deviation_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt_special  ELSE NULL END) AS success_cnt_special_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt_special2  ELSE NULL END) AS success_cnt_special2_shdiv1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.shdiv ELSE NULL END) AS shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.back_rate ELSE NULL END) AS back_rate_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.border_score ELSE NULL END) AS border_score2_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt  ELSE NULL END) AS success_cnt2_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt_candi  ELSE NULL END) AS success_cnt_candi2_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN smallint(t2.border_deviation)  ELSE NULL END) AS border_deviation_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt_special  ELSE NULL END) AS success_cnt_special_shdiv2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt_special2  ELSE NULL END) AS success_cnt_special2_shdiv2 ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst      t1,";
        $query .= "     entexam_passingmark_mst t2 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."' AND ";
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
        $query .= "     t2.applicantdiv, ";
        $query .= "     t2.testdiv, ";
        $query .= "     t2.exam_type ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t2.testdiv, ";
        $query .= "     t2.exam_type ";

        return $query;
    }

    //出願コース取得
    function selectQueryCourse(&$model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t3.coursecd, ";
        $query .= "     t3.majorcd, ";
        $query .= "     t3.examcoursecd, ";
        $query .= "     t3.examcourse_name, ";
        $query .= "     t3.capacity ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst  t3 ";
        $query .= " WHERE ";
        $query .= "     t3.entexamyear  = '".$model->year."' AND ";
        $query .= "     t3.applicantdiv = '".$model->applicantdiv."' AND ";
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

        //判定対象の成績
        $total = "";
        $courseMajorcd = substr($model->course, 0, 4);
        //1:学特
        if ($model->testdiv == "1") {
            //1001:普通科
            if ($courseMajorcd == "1001") {
                $total = "total1"; //満点500点(加点)
            //1002:英語科
            } else if ($courseMajorcd == "1002") {
                $total = "total3"; //満点550点(加点)
            }
        //3:一般
        } else if ($model->testdiv == "3") {
            //1001:普通科
            if ($courseMajorcd == "1001") {
                $total = "total2"; //満点500点
            //1002:英語科
            } else if ($courseMajorcd == "1002") {
                $total = "total4"; //満点550点
            }
        }
        $staffcd = STAFFCD;

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
        //判定対象(1:合格, 2:不合格)・・・第１志望コースの判定
        $query .= "  SELECT ";
        $query .= "     t1.entexamyear, ";
        $query .= "     t1.applicantdiv, ";
        $query .= "     t1.testdiv, ";
        $query .= "     t1.exam_type, ";
        $query .= "     t1.receptno, ";
        $query .= "     t1.examno, ";
        $query .= "     CASE WHEN t1.examno LIKE '6%' THEN '1' "; //一般入試での内部生は、合格とする。
        //3:一般入試において特進チャレンジ者で何名が合格になるか。
        //(10010009:普通科特進コース・10020007:英語科特進コース)を実行した場合のみ
        if ($model->testdiv == "3" && ($model->course == "10010009" || $model->course == "10020007")) {
            $query .= "      WHEN t3.select_subclass_div = '1' THEN ";
            $query .= "          CASE WHEN t1.attend_all_flg != '1' THEN '7' "; //7:(ダミーコード)特進チャレンジ者で不合格
            $query .= "               WHEN t1.{$total} >= ".$model->field["BORDER_SCORE"]." THEN '6' "; //6:(ダミーコード)特進チャレンジ者で合格
            $query .= "          ELSE '7' END ";
        }
        $query .= "          WHEN t1.attend_all_flg != '1' THEN '2' "; //全科目受験フラグが '1' 以外の者は、不合格とする。
        $query .= "          WHEN t1.{$total} >= ".$model->field["BORDER_SCORE"]." THEN '1' ";
        //特別判定合格は、評定合計が入力された場合のみ行う
        if (strlen($model->field["BORDER_DEVIATION"]) && $model->testdiv == "1") {
            $query .= "      WHEN t3.shift_desire_flg = '1' and t4.AVERAGE_ALL >= ".$model->field["BORDER_DEVIATION"]." THEN '5' "; //5:特別判定合格
        }
        $query .= "          ELSE '2' END AS judgediv, ";
        $query .= "     '{$model->exam_type}', ";
        $query .= "     '{$staffcd}' ";
        $query .= " FROM ";
        $query .= "     entexam_recept_dat t1 ";
        $query .= "     inner join v_entexam_applicantdesire_dat t2 ";
        $query .= "          ON  t2.entexamyear  = t1.entexamyear ";
        $query .= "          AND t2.applicantdiv = t1.applicantdiv ";
        $query .= "          AND t2.testdiv      = t1.testdiv ";
        $query .= "          AND t2.examno       = t1.examno ";
        $query .= "          AND t2.wishno       = '1' "; //第１志望
        $query .= "          AND t2.coursecd || t2.majorcd || t2.examcoursecd = '{$model->course}' ";
        $query .= "          AND t2.shdiv        = '{$model->shdiv}' ";
        //(10010010:学特普通科進学コース・10020008:学特英語科進学コース)を実行した場合のみ
        //if ($model->testdiv == "1" && ($model->course == "10010010" || $model->course == "10020008")) {
            $query .= "     left join entexam_applicantbase_dat t3 ";
            $query .= "          ON  t3.entexamyear  = t1.entexamyear ";
            $query .= "          AND t3.applicantdiv = t1.applicantdiv ";
            $query .= "          AND t3.examno       = t1.examno ";
            $query .= "     left join entexam_applicantconfrpt_dat t4 ";
            $query .= "          ON  t4.entexamyear  = t1.entexamyear ";
            $query .= "          AND t4.examno       = t1.examno ";
        //}
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '{$model->year}' AND ";
        $query .= "     t1.applicantdiv = '{$model->applicantdiv}' AND ";
        $query .= "     t1.testdiv      = '{$model->testdiv}' AND ";
        $query .= "     value(t1.judgediv,'0') <> '4' "; //4:欠席は対象外
        if ($model->testdiv == "1") $query .= "   AND t1.examno NOT LIKE '6%' "; //学特入試での内部生は、対象外
        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        if ($model->kikoku == "2") {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0')  = '1' ";
        } else {
            $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0') != '1' ";
        }
        //判定対象(3:スライド合格, 2:不合格)・・・第２志望コースの判定
        //(10010010:学特普通科進学コース・10020008:学特英語科進学コース)を実行した場合のみ
        if (($model->testdiv == "1" || $model->testdiv == "3") && ($model->course == "10010010" || $model->course == "10020008")) {
            $query .= "  UNION ";
            $query .= "  SELECT ";
            $query .= "     t1.entexamyear, ";
            $query .= "     t1.applicantdiv, ";
            $query .= "     t1.testdiv, ";
            $query .= "     t1.exam_type, ";
            $query .= "     t1.receptno, ";
            $query .= "     t1.examno, ";
            $query .= "     CASE WHEN t1.attend_all_flg != '1' THEN '2' "; //全科目受験フラグが '1' 以外の者は、不合格とする。
            $query .= "          WHEN t1.{$total} >= ".$model->field["BORDER_SCORE"]." THEN '3' ";
            //特別判定合格は、評定合計が入力された場合のみ行う
            if (strlen($model->field["BORDER_DEVIATION"]) && $model->testdiv == "1") {
                $query .= "      WHEN t3.shift_desire_flg = '1' and t4.AVERAGE_ALL >= ".$model->field["BORDER_DEVIATION"]." THEN '9' "; //9:(ダミーコード)特別判定合格(第２志望)
            }
            $query .= "          ELSE '2' END AS judgediv, ";
            $query .= "     '{$model->exam_type}', ";
            $query .= "     '{$staffcd}' ";
            $query .= " FROM ";
            $query .= "     entexam_recept_dat t1 ";
            $query .= "     inner join v_entexam_applicantdesire_dat t2 ";
            $query .= "          ON  t2.entexamyear  = t1.entexamyear ";
            $query .= "          AND t2.applicantdiv = t1.applicantdiv ";
            $query .= "          AND t2.testdiv      = t1.testdiv ";
            $query .= "          AND t2.examno       = t1.examno ";
            $query .= "          AND t2.wishno       = '2' "; //第２志望
            $query .= "          AND t2.coursecd || t2.majorcd || t2.examcoursecd = '{$model->course}' ";
            $query .= "          AND t2.shdiv        = '{$model->shdiv}' ";
            $query .= "     inner join entexam_applicantbase_dat t3 ";
            $query .= "          ON  t3.entexamyear  = t1.entexamyear ";
            $query .= "          AND t3.applicantdiv = t1.applicantdiv ";
            $query .= "          AND t3.testdiv      = t1.testdiv "; //出願区分 = 入試区分
            $query .= "          AND t3.examno       = t1.examno ";
            $query .= "          AND t3.slide_flg    = '1' "; //スライド希望
            //$query .= "          AND value(t3.select_subclass_div,'0') <> '1' "; //1:特進チャレンジ受験者は対象外
            $query .= "     left join entexam_applicantconfrpt_dat t4 ";
            $query .= "          ON  t4.entexamyear  = t1.entexamyear ";
            $query .= "          AND t4.examno       = t1.examno ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear  = '{$model->year}' AND ";
            $query .= "     t1.applicantdiv = '{$model->applicantdiv}' AND ";
            $query .= "     t1.testdiv      = '{$model->testdiv}' AND ";
            $query .= "     t1.judgediv     = '2' "; //2:不合格
            if ($model->testdiv == "1" || $model->testdiv == "3") $query .= "   AND t1.examno NOT LIKE '6%' "; //内部生は、対象外
            //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
            if ($model->kikoku == "2") {
                $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0')  = '1' ";
            } else {
                $query .= "   AND VALUE(T3.INTERVIEW_ATTEND_FLG, '0') != '1' ";
            }
        }
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
                            //9:(ダミーコード)特別判定合格(第２志望)を5:特別判定合格に変換
        $query .= "         case when st1.judgediv = '9' then '5' ";
                            //6:(ダミーコード)特進チャレンジ者で合格を1:合格に変換
        $query .= "              when st1.judgediv = '6' then '1' ";
                            //7:(ダミーコード)特進チャレンジ者で不合格を2:不合格に変換
        $query .= "              when st1.judgediv = '7' then '2' ";

        $query .= "              else st1.judgediv end as judgediv ";
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

        //志願者基礎の判定更新
        //RECEPTD_DATで一回も合格していない生徒を2:不合格で登録
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
        $query .= "     ( ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(3)), ";
        $query .= "     CAST(null AS varchar(4)), ";
        $query .= "     '2', "; //2:不合格
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear      = '".$model->year."' ";
        $query .= "     AND t1.applicantdiv = '".$model->applicantdiv."' ";
                        //シミュレーションで2:不合格
        $query .= "     AND t1.examno       IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv in ('2') ) ";
                        //全受付データで１度も1:合格,3:スライド合格,5:特別判定合格したことが無い人
        $query .= "     AND t1.examno   NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear         = '".$model->year."' ";
        $query .= "                                AND t2.applicantdiv        = '".$model->applicantdiv."' ";
        $query .= "                                AND value(t2.judgediv,'0') in ('1','3','5') ";
        $query .= "                            )";
        $db->query($query);

        //１度でも合格した生徒の合格学科を更新
        //1:合格
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
        $query .= "     ( ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     '1', "; //1:合格
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv ."' AND ";
                        //シミュレーションで1:合格
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '1' ) AND ";
                        //過去に１度も1:合格,3:スライド合格,5:特別判定合格したことが無い人
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear         = '".$model->year."' ";
        $query .= "                                AND t2.applicantdiv        = '".$model->applicantdiv."' ";
        $query .= "                                AND integer(t2.testdiv)    < integer('".$model->testdiv."') ";
        $query .= "                                AND value(t2.judgediv,'0') in ('1','3','5') ";
        $query .= "                            )";
        $db->query($query);
        //3:スライド合格
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
        $query .= "     ( ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     '3', "; //3:スライド合格
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv ."' AND ";
                        //シミュレーションで3:スライド合格
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '3' ) AND ";
                        //過去に１度も1:合格,3:スライド合格,5:特別判定合格したことが無い人
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear         = '".$model->year."' ";
        $query .= "                                AND t2.applicantdiv        = '".$model->applicantdiv."' ";
        $query .= "                                AND integer(t2.testdiv)    < integer('".$model->testdiv."') ";
        $query .= "                                AND value(t2.judgediv,'0') in ('1','3','5') ";
        $query .= "                            )";
        $db->query($query);
        //5:特別判定合格
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
        $query .= "     ( ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     '5', "; //5:特別判定合格
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     CAST(null AS varchar(1)), ";
        $query .= "     sysdate() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.applicantdiv = '".$model->applicantdiv ."' AND ";
                        //シミュレーションで5:特別判定合格,9:(ダミーコード)特別判定合格(第２志望)
        $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv in ('5','9') ) AND ";
                        //過去に１度も1:合格,3:スライド合格,5:特別判定合格したことが無い人
        $query .= "     t1.examno       NOT IN (SELECT t2.examno FROM entexam_recept_dat t2 ";
        $query .= "                              WHERE t2.entexamyear         = '".$model->year."' ";
        $query .= "                                AND t2.applicantdiv        = '".$model->applicantdiv."' ";
        $query .= "                                AND integer(t2.testdiv)    < integer('".$model->testdiv."') ";
        $query .= "                                AND value(t2.judgediv,'0') in ('1','3','5') ";
        $query .= "                            )";
        $db->query($query);

        //特進チャレンジ者
        if ($model->testdiv == "3" && ($model->course == "10010009" || $model->course == "10020007")) {
            //特進チャレンジ者で合格
            $query  = " UPDATE ";
            $query .= "     entexam_applicantbase_dat t1 ";
            $query .= " SET ";
            $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode,judgement,procedurediv,entdiv,updated) = ";
            $query .= "     ( ";
            $query .= "     '" .substr($model->course, 0, 1) ."', ";
            $query .= "     '" .substr($model->course, 1, 3) ."', ";
            $query .= "     '" .substr($model->course, 4, 4) ."', ";
            $query .= "     '1', ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     CAST(null AS varchar(1)), ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "     t1.entexamyear  = '".$model->year."' AND ";
            $query .= "     t1.applicantdiv = '".$model->applicantdiv ."' AND ";
            $query .= "     t1.examno           IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '6' ) "; //シミュレーションで6:合格
            $db->query($query);

            //特進チャレンジ者で不合格
            //3:一般入試において、特進チャレンジ者が不合格の場合、基礎データは、1:学特入試での進学コース合格に戻す。
            //理由：特進チャレンジ者は、1:学特入試で進学コース合格して、合格を保有したまま3:一般入試の特進コースを再受験しています。
            $result    = $db->query(knjl070yQuery::getJudgeCourse($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       EXAMNO        = '".$row["EXAMNO"]."'";
                $data = array();
                $data["JUDGEMENT"][TEXT]        = $row["JUDGEDIV"];
                $data["SUC_COURSECD"][TEXT]     = $row["COURSECD"];
                $data["SUC_MAJORCD"][TEXT]      = $row["MAJORCD"];
                $data["SUC_COURSECODE"][TEXT]   = $row["EXAMCOURSECD"];
                $data["PROCEDUREDIV"][TEXT]     = "";
                $data["ENTDIV"][TEXT]           = "";
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }

        //合格点マスタへシミュレーション結果の保存
        $query  = " DELETE FROM entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     testdiv      = '".$model->testdiv."' AND ";
        $query .= "     exam_type    = '{$model->kikoku}' AND ";
        $query .= "     shdiv        = '{$model->shdiv}' AND ";
        $query .= "     coursecd || majorcd || examcoursecd  = '".$model->course."' ";
        $db->query($query);

        $query = " INSERT INTO entexam_passingmark_mst( ";
        $query .= "     entexamyear, ";     //年度
        $query .= "     applicantdiv, ";    //試験制度
        $query .= "     testdiv, ";         //出願区分
        $query .= "     exam_type, ";       //判定科目(２or４科目)
        $query .= "     shdiv, ";           //専併区分
        $query .= "     coursecd, ";        //課程コード
        $query .= "     majorcd, ";         //学科コード
        $query .= "     examcoursecd, ";    //コースコード
        $query .= "     border_score, ";    //合格点
        $query .= "     success_cnt, ";     //合格者数
        $query .= "     back_rate, ";       //戻り率
        $query .= "     capa_cnt, ";        //収容人数
        $query .= "     success_cnt_candi, ";       //スライド合格者数
        //特別判定合格は、評定合計が入力された場合のみ行う
        if (strlen($model->field["BORDER_DEVIATION"])) {
            $query .= "     border_deviation, ";        //評定合計
        }
        $query .= "     success_cnt_special, ";     //特別判定合格者数 または 特進チャレンジ合格者数
        $query .= "     success_cnt_special2, ";    //特別判定合格(第２志望)合格者数
        $query .= "     registercd ";       //登録者コード
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= "     '" .$model->year ."', ";
        $query .= "     '" .$model->applicantdiv ."', ";
        $query .= "     '" .$model->testdiv ."', ";
        $query .= "     '" .$model->kikoku ."', ";
        $query .= "     '{$model->shdiv}', ";
        $query .= "     '" .substr($model->course, 0, 1) ."', ";
        $query .= "     '" .substr($model->course, 1, 3) ."', ";
        $query .= "     '" .substr($model->course, 4, 4) ."', ";
        $query .= "     " .$model->field["BORDER_SCORE"] .", ";
        $query .= "     " .$model->field["SUCCESS_CNT"] .", ";
        $query .= "     " .$model->field["BACK_RATE"] .", ";
        $query .= "     " .$model->field["CAPA_CNT"] .", ";
        $query .= "     " .$model->field["SUCCESS_CNT_CANDI"] .", ";
        //特別判定合格は、評定合計が入力された場合のみ行う
        if (strlen($model->field["BORDER_DEVIATION"])) {
            $query .= "     " .$model->field["BORDER_DEVIATION"] .", ";
        }
        //一般入試の場合、特進チャレンジ合格者数
        if ($model->testdiv == "3") {
            $query .= "     " .$model->field["SUCCESS_CNT_CHALLENGE"] .", ";
        } else {
            $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL"] .", ";
        }
        $query .= "     " .$model->field["SUCCESS_CNT_SPECIAL2"] .", ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= " ) ";
        $db->query($query);

        //仮判定データすべて削除
//        $query = " DELETE FROM entexam_judge_tmp ";
//        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //特進チャレンジ者の1:学特入試での進学コース合格を取得
    function getJudgeCourse($model) {
        $query  = " WITH MAX_WISHDIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV, ";
        $query .= "         MAX(WISHNO) AS WISHNO "; //進学コース
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV ";
        $query .= "     ) ";
        $query .= " , T_WISHDIV_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.ENTEXAMYEAR, ";
        $query .= "         W1.APPLICANTDIV, ";
        $query .= "         W1.TESTDIV, ";
        $query .= "         W1.DESIREDIV, ";
        $query .= "         W1.WISHNO, ";
        $query .= "         W1.COURSECD, ";
        $query .= "         W1.MAJORCD, ";
        $query .= "         W1.EXAMCOURSECD ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST W1 ";
        $query .= "         INNER JOIN MAX_WISHDIV W2 ";
        $query .= "             ON  W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
        $query .= "             AND W2.APPLICANTDIV = W1.APPLICANTDIV ";
        $query .= "             AND W2.TESTDIV = W1.TESTDIV ";
        $query .= "             AND W2.DESIREDIV = W1.DESIREDIV ";
        $query .= "             AND W2.WISHNO = W1.WISHNO ";
        $query .= "     ) ";
        $query .= "SELECT T2.RECEPTNO ";
        $query .= "      ,T2.EXAMNO ";
        $query .= "      ,T2.JUDGEDIV ";
        $query .= "      ,W1.COURSECD ";
        $query .= "      ,W1.MAJORCD ";
        $query .= "      ,W1.EXAMCOURSECD ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV "; //出願区分(1:学特入試)
        $query .= "             AND T2.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "       INNER JOIN T_WISHDIV_MST W1 ";
        $query .= "             ON  W1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND W1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND W1.DESIREDIV    = T1.DESIREDIV ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->year}'";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.EXAMNO         IN (SELECT examno FROM entexam_judge_tmp WHERE judgediv = '7' ) "; //シミュレーションで7:不合格
        return $query;
    }
}
?>
