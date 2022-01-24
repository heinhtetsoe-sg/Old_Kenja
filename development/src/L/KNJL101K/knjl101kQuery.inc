<?php

require_once('for_php7.php');

class knjl101kQuery extends Query {

    //中高判定 2005/08/28 ALP
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //名称
    function GetName($namecd1, $year)
    {
        $query = "SELECT namecd1,namecd2, name1, name2 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 IN ('".implode($namecd1, "','")."')
                   ORDER BY namecd1,namecd2,name1 ";
        return $query;
    }

    //仮判定データ(シミュレーション実行有無のチェック)
    function selectQueryJudge_tmp(&$model)
    {
        $query  = " SELECT * FROM entexam_judge_tmp t1 ";
        $query .= "  WHERE (";
        $query .= "         t1.entexamyear, t1.testdiv, t1.examno, ";
        $query .= "         t1.coursecd, t1.majorcd, t1.examcoursecd ";
        $query .= "        ) IN";
        $query .= "        (SELECT ";
        $query .= "          t1.entexamyear, t1.testdiv, t1.examno, ";
        $query .= "          t2.coursecd, t2.majorcd, t2.examcoursecd ";
        $query .= "           FROM ";
        $query .= "                entexam_applicantbase_dat   t1, ";
        $query .= "                entexam_wishdiv_mst         t2 ";
        $query .= "          WHERE ";
        $query .= "                t1.entexamyear  = '".$model->year."' AND ";
        $query .= "                t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                t1.testdiv      = '".$model->testdiv."' AND ";
        $query .= "                t1.shdiv        = '".$model->shdiv."' AND ";
        $query .= "                t2.coursecd || t2.majorcd || t2.examcoursecd = '".$model->course."' AND ";

        //附属推薦、中高一貫、未受験は、除く        2005/08/11 arakaki
        $query .= "                t1.examno NOT IN (SELECT examno ";
        $query .= "                                    FROM entexam_applicantbase_dat ";
        $query .= "                                   WHERE entexamyear     = t1.entexamyear AND ";
        $query .= "                                         testdiv         = t1.testdiv     AND ";
#        $query .= "                                         (applicantdiv    IN('1','2','3')  OR  ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦  #2005/09/12 arakaki
        $query .= "                                         (applicantdiv    IN('1','2')  OR  ";         // 1:附属推薦、2:中高一貫                   #2005/11/15 arakaki
        $query .= "                                         judgement       = '8'            )) AND ";   // 8:未受験

        //今から処理するコースより若い志望区分のコースの志願者は除く！！
        $query .= "                      NOT EXISTS (SELECT 'X' ";
        $query .= "                                    FROM ";
        $query .= "                                         entexam_applicantbase_dat   st1, ";
        $query .= "                                         entexam_wishdiv_mst         st2 ";
        $query .= "                                   WHERE ";
        $query .= "                                         st1.entexamyear     = t1.entexamyear AND ";
        $query .= "                                         st1.entexamyear     = st2.entexamyear AND ";
        $query .= "                                         st1.testdiv         = t1.testdiv AND ";
        $query .= "                                         st1.testdiv         = st2.testdiv AND ";
        $query .= "                                         st1.desirediv       = st2.desirediv AND ";
        $query .= "                                         st1.suc_coursecd    = st2.coursecd AND ";
        $query .= "                                         st1.suc_majorcd     = st2.majorcd AND ";
        $query .= "                                         st1.suc_coursecode  = st2.examcoursecd AND ";
        $query .= "                                         st1.examno          = t1.examno AND ";
        $query .= "                                         st2.wishno          < t2.wishno ";
        $query .= "                                 ) ";
        $query .= "        ) ";

        #echo $query;
        return $query;
    }

    //合格点マスタ
    function selectQueryPassingmark(&$model)
    {

       $query .= " SELECT ";
       $query .= "     entexamyear, ";
       $query .= "     testdiv, ";
       $query .= "     shdiv, ";
       $query .= "     majorcd, ";
       $query .= "     examcoursecd, ";
       $query .= "     coursecd || majorcd || examcoursecd as course, ";
       $query .= "     judge_col, ";       //判定配点(Ａb配点)
       $query .= "     border_score, ";    //基本合格点
       $query .= "     a_border_score, ";  //判定Ａ合格点
       $query .= "     b_border_score, ";  //判定Ｂ合格点
       $query .= "     success_cnt   , ";  //基本合格者数
       $query .= "     success_cnt   , ";  //基本合格者数
       $query .= "     a_success_cnt , ";  //判定Ａ合格者数
       $query .= "     b_success_cnt , ";  //判定Ｂ合格者数
       $query .= "     s_success_cnt, ";   //スポ推薦合格者数

       $query .= "     success_sex1_cnt, ";     #(正規)合格者男数   2005/12/27
       $query .= "     success_sex2_cnt, ";     #(正規)合格者女数   2005/12/27
       $query .= "     a_success_sex1_cnt, ";   #Ａ合格者男数       2005/12/27
       $query .= "     a_success_sex2_cnt, ";   #Ａ合格者女数       2005/12/27
       $query .= "     b_success_sex1_cnt, ";   #Ｂ合格者男数       2005/12/27
       $query .= "     b_success_sex2_cnt, ";   #Ｂ合格者女数       2005/12/27

       $query .= "     back_rate, ";       //戻り率
       $query .= "     capa_cnt ";         //収容人数
       $query .= " FROM ";
       $query .= "     entexam_passingmark_mst";
       $query .= " WHERE ";
       $query .= "     entexamyear     = '".$model->year."' AND ";
       $query .= "     testdiv         = '".$model->testdiv."' AND ";
       $query .= "     shdiv           = '".$model->shdiv."' AND ";
       $query .= "     coursecd || majorcd || examcoursecd = '".$model->course."' ";
       #echo $query;
       return $query;

    }

    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt(&$model)
    {
      $query  = " SELECT COUNT(*)                                                                       AS   SUCCESS_CNT, ";
      /*** UPDATE 2005/11/04 by ameku ***/
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '1' THEN 1 ELSE 0 END)                  AS R_SUCCESS_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '2' THEN 1 ELSE 0 END)                  AS A_SUCCESS_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '3' THEN 1 ELSE 0 END)                  AS B_SUCCESS_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '1' AND T2.SEX = '1' THEN 1 ELSE 0 END) AS R_SUCCESS_SEX1_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '1' AND T2.SEX = '2' THEN 1 ELSE 0 END) AS R_SUCCESS_SEX2_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '2' AND T2.SEX = '1' THEN 1 ELSE 0 END) AS A_SUCCESS_SEX1_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '2' AND T2.SEX = '2' THEN 1 ELSE 0 END) AS A_SUCCESS_SEX2_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '3' AND T2.SEX = '1' THEN 1 ELSE 0 END) AS B_SUCCESS_SEX1_CNT, ";
      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '3' AND T2.SEX = '2' THEN 1 ELSE 0 END) AS B_SUCCESS_SEX2_CNT ";
      /*** UPDATE 2005/11/04 by ameku ***/
      $query .= "   FROM ENTEXAM_JUDGE_TMP T1, ENTEXAM_APPLICANTBASE_DAT T2 ";
      $query .= "  WHERE T1.ENTEXAMYEAR = '".$model->year."'  ";
      $query .= "    AND T1.TESTDIV = '".$model->testdiv."' ";
      $query .= "    AND T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD = '".$model->course."'  ";
      $query .= "    AND T1.REGULARSUCCESS_FLG IN('1','2','3') ";
      $query .= "    AND T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
      $query .= "    AND T1.TESTDIV     = T2.TESTDIV ";
      $query .= "    AND T1.EXAMNO      = T2.EXAMNO ";
      $query .= "    AND T2.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
      # echo $query;
      return $query;

#2005/12/27
#      $query  = " SELECT COUNT(*)                                                                              AS   SUCCESS_CNT, ";
#      /*** UPDATE 2005/11/04 by ameku ***/
#      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '1' THEN 1 ELSE 0 END)                         AS R_SUCCESS_CNT, ";
#      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '2' THEN 1 ELSE 0 END) AS A_SUCCESS_CNT, ";
#      $query .= "        SUM(CASE WHEN T1.REGULARSUCCESS_FLG  = '3' THEN 1 ELSE 0 END) AS B_SUCCESS_CNT ";
#      /*** UPDATE 2005/11/04 by ameku ***/
#      $query .= "   FROM ENTEXAM_JUDGE_TMP T1 ";
#      $query .= "  WHERE T1.ENTEXAMYEAR = '".$model->year."'  ";
#      $query .= "    AND T1.TESTDIV = '".$model->testdiv."' ";
#      $query .= "    AND T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD = '".$model->course."'  ";
#      $query .= "    AND T1.REGULARSUCCESS_FLG IN('1','2','3') ";
#      # echo $query;
#      return $query;

    }

    //合格点マスタ
    function selectQueryPassingmarkAll(&$model)
    {
        $query = " SELECT ";
        $query .= "     t1.entexamyear, ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t1.examcourse_name, ";
        $query .= "     t1.capacity, ";
        $query .= "     t2.testdiv, ";
        $query .= "     SUM(t2.s_success_cnt) AS s_success_cnt, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.reprocess_flg    ELSE null END) AS reprocess_flg1, "; // add 2005/02/07
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.reprocess_flg    ELSE null END) AS reprocess_flg2, "; // add 2005/02/07
        //専願
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.border_score     ELSE null END) AS border_score1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.a_border_score   ELSE null END) AS a_border_score1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.b_border_score   ELSE null END) AS b_border_score1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt      ELSE null END) AS success_cnt1, "; #2005/11/15
#        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.success_cnt      ELSE null END) + ";
#        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.s_success_cnt    ELSE null END) AS success_cnt1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.a_success_cnt    ELSE null END) AS a_success_cnt1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.b_success_cnt    ELSE null END) AS b_success_cnt1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.back_rate        ELSE null END) AS back_rate1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.capa_cnt         ELSE null END) AS capa_cnt1, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '1' THEN t2.judge_col        ELSE null END) AS judge_col1, ";
        //併願
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.border_score     ELSE null END) AS border_score2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.a_border_score   ELSE null END) AS a_border_score2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.b_border_score   ELSE null END) AS b_border_score2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt      ELSE null END) AS success_cnt2, "; #2005/11/15
#        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.success_cnt      ELSE null END) + ";
#        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.s_success_cnt    ELSE null END) AS success_cnt2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.a_success_cnt    ELSE null END) AS a_success_cnt2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.b_success_cnt    ELSE null END) AS b_success_cnt2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.back_rate        ELSE null END) AS back_rate2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.capa_cnt         ELSE null END) AS capa_cnt2, ";
        $query .= "     MAX(CASE t2.shdiv WHEN '2' THEN t2.judge_col        ELSE null END) AS judge_col2 ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst      t1, ";
        $query .= "     entexam_passingmark_mst t2 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t2.entexamyear  = t1.entexamyear AND ";
        $query .= "     t2.testdiv      = '".$model->testdiv."' AND ";
        $query .= "     t2.coursecd     = t1.coursecd AND ";
        $query .= "     t2.majorcd      = t1.majorcd AND ";
        $query .= "     t2.examcoursecd = t1.examcoursecd ";
        $query .= " GROUP BY ";
        $query .= "     t1.entexamyear, ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t1.examcourse_name, ";
        $query .= "     t1.capacity, ";
        $query .= "     t2.testdiv ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd||t1.majorcd||t1.examcoursecd desc, ";   //2005.09.04---ALP
        $query .= "     t2.testdiv ";


        # echo $query;
        return $query;
    }

    //出願コース取得
    function selectQueryCourse(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     t3.coursecd, ";
        $query .= "     t3.majorcd, ";
        $query .= "     t3.examcoursecd, ";
        $query .= "     t3.examcourse_name ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat   t1, ";
        $query .= "     entexam_wishdiv_mst         t2, ";
        $query .= "     entexam_course_mst          t3 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."' AND ";

        //附属推薦、中高一貫、未受験は、除く
        $query .= "     t1.examno NOT IN (SELECT examno ";
        $query .= "                         FROM entexam_applicantbase_dat ";
        $query .= "                        WHERE entexamyear     = t1.entexamyear AND ";
        $query .= "                              testdiv         = t1.testdiv     AND ";
#        $query .= "                             (applicantdiv    IN('1','2','3')  OR  ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦  #2005/09/12 arakaki
        $query .= "                             (applicantdiv    IN('1','2')      OR  ";      // 1:附属推薦、2:中高一貫                  #2005/11/15 arakaki
        $query .= "                              judgement       = '8'            )) AND ";   // 8:未受験

        $query .= "     t2.entexamyear  = t1.entexamyear AND ";
        $query .= "     t2.testdiv      = t1.testdiv AND ";
        $query .= "     t2.desirediv    = t1.desirediv AND ";
        $query .= "     t3.entexamyear  = t2.entexamyear AND ";
        $query .= "     t3.coursecd     = t2.coursecd AND ";
        $query .= "     t3.majorcd      = t2.majorcd AND ";
        $query .= "     t3.examcoursecd = t2.examcoursecd ";
        $query .= " ORDER BY ";
        $query .= "     t3.examcoursecd DESC ";	//alp m-yama 2005/09/03

        # echo $query;
        return $query;
    }

    //--------------------------------------------
    //シミュレーション処理実行
    //--------------------------------------------
    #2005/11/14 3:スポーツ推薦は、シュミレーション対象、合否判定は、対象外
    function sim(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        set_time_limit(0);

        //仮判定データすべて削除
        $query = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);


        $total = ($model->judge_col == "1")? "a_total":"b_total";   //判定項目の選定(配点Ａの合計または配点Ｂの合計で行うか？)

       $query = " INSERT INTO entexam_judge_tmp( ";
       $query .= "     entexamyear, ";         //年度
       $query .= "     testdiv, ";             //試験区分
       $query .= "     examno, ";              //受験番号
       $query .= "     coursecd, ";            //課程コード
       $query .= "     majorcd, ";             //学科コード
       $query .= "     examcoursecd, ";        //コースコード
       $query .= "     judgement, ";           //判定
       $query .= "     regularsuccess_flg, ";  //基本合格フラグ
       $query .= "     registercd ";           //登録者コード
       $query .= " ) ";
       $query .= " (SELECT ";
       $query .= "     t1.entexamyear, ";
       $query .= "     t1.testdiv, ";
       $query .= "     t1.examno, ";
       $query .= "     t2.coursecd, ";
       $query .= "     t2.majorcd, ";
       $query .= "     t2.examcoursecd, ";
       //合否判定
       $query .= "     CASE WHEN ( t1.applicantdiv = '3') THEN t1.judgement ";                                       //合否判定対象外(3:スポーツ推薦)
       $query .= "          ELSE ";
       $query .= "              CASE WHEN ( $total >= ".$model->field["BORDER_SCORE"].")  OR ";                          //基本点合格
       $query .= "                        ( $total >= ".$model->field["A_BORDER_SCORE"]." AND t3.judgement = '1') OR ";  //事前相談Ａ判定合格
       $query .= "                        ( $total >= ".$model->field["B_BORDER_SCORE"]." AND t3.judgement = '2')    ";  //事前相談Ｂ判定合格
       $query .= "                  THEN  ";
       $query .= "                          t2.wishno ";                                                                 //1:第1, 2:第2, 3:第3, 合格！   #2005/09/12 arakaki
       $query .= "                 ELSE '7'  ";                                                                          //7:不合格
       $query .= "              END ";
       $query .= "     END, ";
       //正規合格フラグ
       $query .= "     CASE WHEN $total >= ".$model->field["BORDER_SCORE"] ."                          THEN '1' ";  //基本点合格
       $query .= "          WHEN $total >= ".$model->field["A_BORDER_SCORE"] ." AND t3.judgement = '1' THEN '2' ";  //事前相談Ａ判定合格
       $query .= "          WHEN $total >= ".$model->field["B_BORDER_SCORE"] ." AND t3.judgement = '2' THEN '3' ";  //事前相談Ｂ判定合格
       $query .= "          ELSE Null  ";
       $query .= "     END, ";

       $query .= "     '" .STAFFCD ."' ";
       $query .= " FROM ";
       $query .= "     entexam_applicantbase_dat   t1 ";
       $query .= "     INNER JOIN entexam_wishdiv_mst  t2 ";
       $query .= "             ON t2.entexamyear  = t1.entexamyear AND ";
       $query .= "                t2.testdiv      = t1.testdiv AND ";
       $query .= "                t2.desirediv    = t1.desirediv AND ";
       $query .= "                t2.coursecd || t2.majorcd || t2.examcoursecd = '".$model->course."' ";
       $query .= "     LEFT OUTER JOIN entexam_applicantcons_dat   t3 ";
       $query .= "                  ON t3.entexamyear  = t1.entexamyear AND ";
       $query .= "                     t3.testdiv      = t1.testdiv AND ";
       $query .= "                     t3.shdiv        = t1.shdiv AND ";
       $query .= "                     t3.examno       = t1.examno AND ";
       $query .= "                     t3.coursecd     = t2.coursecd AND ";
       $query .= "                     t3.majorcd      = t2.majorcd AND ";
       $query .= "                     t3.examcoursecd = t2.examcoursecd ";
       $query .= " WHERE ";
       $query .= "     t1.entexamyear  = '".$model->year."' AND ";
       $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
       $query .= "     t1.testdiv      = '".$model->testdiv."' AND ";
       $query .= "     t1.shdiv        = '".$model->shdiv."' AND ";

       //附属推薦、中高一貫、未受験は、除く        2005/08/11 arakaki
       $query .= "     t1.examno NOT IN (SELECT examno ";
       $query .= "                         FROM entexam_applicantbase_dat ";
       $query .= "                        WHERE entexamyear     = t1.entexamyear AND ";
       $query .= "                              testdiv         = t1.testdiv     AND ";
       $query .= "                             (applicantdiv    IN('1','2')  OR  ";         // 1:附属推薦、2:中高一貫 対象外 #2005/11/14 arakaki
       $query .= "                              judgement       = '8'            )) AND ";  // 8:未受験

       //今から処理するコースより若い志望区分のコースで合格している志願者は除く！！
       $query .= "                 NOT EXISTS (SELECT 'X' ";
       $query .= "                               FROM ";
       $query .= "                                    entexam_applicantbase_dat   st1, ";
       $query .= "                                    entexam_wishdiv_mst         st2 ";
       $query .= "                              WHERE ";
       $query .= "                                    st1.entexamyear     = t1.entexamyear AND ";
       $query .= "                                    st1.entexamyear     = st2.entexamyear AND ";
       $query .= "                                    st1.testdiv         = t1.testdiv AND ";
       $query .= "                                    st1.testdiv         = st2.testdiv AND ";
       $query .= "                                    st1.desirediv       = st2.desirediv AND ";
       $query .= "                                    st1.suc_coursecd    = st2.coursecd AND ";
       $query .= "                                    st1.suc_majorcd     = st2.majorcd AND ";
       $query .= "                                    st1.suc_coursecode  = st2.examcoursecd AND ";
       $query .= "                                    st1.examno          = t1.examno AND ";
       $query .= "                                    st2.wishno          < t2.wishno AND ";
       $query .= "                                    st1.judgement       IN ('1','2','3','4','5','6','9') ";   //1:第1, 2:第2, 3:第3, 4:推薦, 5:追加, 6:コース変更, 9:第4 合格 ※ここでは8:未受験, 0:保留は使用しない
       $query .= "                            ) ";

       $query .= " ) ";

        # echo $query;
        $db->query($query);

        $query  = " SELECT COUNT(*) FROM entexam_judge_tmp ";
        $dataCount = $db->getOne($query);

        $db->commit(); 
        Query::dbCheckIn($db);

        return $dataCount;
    }

    //--------------------------------------------
    //確定処理実行
    //--------------------------------------------
    function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        set_time_limit(0);

        //判定が不合格はNullをセットそれ以外は各情報(課程、学科、コース、正規合格フラグ)をセット
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.suc_coursecd,t1.suc_majorcd,t1.suc_coursecode,t1.judgement,t1.regularsuccess_flg,t1.interview_attend_flg,t1.procedurediv,t1.entdiv,t1.judgement_group_no) = (SELECT ";  //2005.08.29 minei
        $query .= "         CASE WHEN st1.judgement != '7' THEN st1.coursecd            ELSE null END, ";   //課程
        $query .= "         CASE WHEN st1.judgement != '7' THEN st1.majorcd             ELSE null END, ";   //学科
        $query .= "         CASE WHEN st1.judgement != '7' THEN st1.examcoursecd        ELSE null END, ";   //コース
        $query .= "         st1.judgement, ";                                                               //判定
        $query .= "         CASE WHEN st1.judgement != '7' THEN st1.regularsuccess_flg  ELSE null END, ";   //正規合格フラグ
        $query .= "         CASE WHEN st1.judgement != '7' AND st2.shdiv='1' THEN '1'   ELSE null END, ";   //面接出欠区分(1:出席　2:欠席)  専願のみ
        $query .= "         CASE WHEN st1.judgement != '7' AND st2.shdiv='1' THEN '2'   ELSE null END, ";   //手続区分(1:未　　2:済)        専願のみ
        $query .= "         CASE WHEN st1.judgement != '7' AND st2.shdiv='1' THEN '2'   ELSE null END, ";   //入学区分(1:辞退　2:有り)      専願のみ 2005.08.29 minei
        $query .= "         CAST(NULL AS VARCHAR(2)) "; //2005.08.09 minei  グループ番号を初期化(合格、不合格は未使用のため)
        $query .= "    FROM ";
        $query .= "         entexam_judge_tmp         st1 ";
        $query .= "        ,entexam_applicantbase_dat st2 ";
        $query .= "   WHERE ";
        $query .= "         st1.entexamyear = t1.entexamyear AND ";
        $query .= "         st1.testdiv     = t1.testdiv AND ";
        $query .= "         st1.examno      = t1.examno AND ";
        $query .= "         st1.coursecd || st1.majorcd || st1.examcoursecd  = '".$model->course."' AND ";
        $query .= "         st2.entexamyear = st1.entexamyear AND ";
        $query .= "         st2.testdiv     = st1.testdiv AND ";
        $query .= "         st2.examno      = st1.examno ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";

        //附属推薦、中高一貫、未受験は、除く        2005/08/11 arakaki
        $query .= "     t1.examno NOT IN (SELECT examno ";
        $query .= "                         FROM entexam_applicantbase_dat ";
        $query .= "                        WHERE entexamyear     = t1.entexamyear AND ";
        $query .= "                              testdiv         = t1.testdiv     AND ";
        $query .= "                             (applicantdiv    IN('1','2','3')  OR  ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦  #2005/09/12 arakaki
        $query .= "                              judgement       = '8'            )) AND ";  // 8:未受験

        $query .= "     t1.examno IN (SELECT w.examno ";
        $query .= "                     FROM entexam_judge_tmp w ";
        $query .= "                    WHERE ";
        $query .= "                          w.entexamyear = t1.entexamyear AND ";
        $query .= "                          w.testdiv     = t1.testdiv AND ";
        $query .= "                          w.coursecd || w.majorcd || w.examcoursecd = '".$model->course."' ";
        $query .= "                  ) ";

        $db->query($query);


        //3:スポーツ推薦、正規合格フラグ更新処理 2005/11/14
        $query  = " UPDATE ";
        $query .= "     entexam_applicantbase_dat t1 ";
        $query .= " SET ";
        $query .= "     (t1.REGULARSUCCESS_FLG) = (SELECT ";
        $query .= "         st1.regularsuccess_flg  ";   //正規合格フラグ
        $query .= "    FROM ";
        $query .= "         entexam_judge_tmp         st1 ";
        $query .= "        ,entexam_applicantbase_dat st2 ";
        $query .= "   WHERE ";
        $query .= "         st1.entexamyear = t1.entexamyear AND ";
        $query .= "         st1.testdiv     = t1.testdiv AND ";
        $query .= "         st1.examno      = t1.examno AND ";
        $query .= "         st1.coursecd || st1.majorcd || st1.examcoursecd  = '".$model->course."' AND ";
        $query .= "         st2.entexamyear = st1.entexamyear AND ";
        $query .= "         st2.testdiv     = st1.testdiv AND ";
        $query .= "         st2.examno      = st1.examno ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' AND ";
        $query .= "     t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     t1.testdiv      = '".$model->testdiv."'  AND ";

        $query .= "     t1.examno IN (SELECT examno ";
        $query .= "                         FROM entexam_applicantbase_dat ";
        $query .= "                        WHERE entexamyear     = t1.entexamyear AND ";
        $query .= "                              testdiv         = t1.testdiv     AND ";
        $query .= "                              applicantdiv    = '3'          ) AND ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦  #2005/09/12 arakaki

        $query .= "     t1.examno IN (SELECT w.examno ";
        $query .= "                     FROM entexam_judge_tmp w ";
        $query .= "                    WHERE ";
        $query .= "                          w.entexamyear = t1.entexamyear AND ";
        $query .= "                          w.testdiv     = t1.testdiv AND ";
        $query .= "                          w.coursecd || w.majorcd || w.examcoursecd = '".$model->course."' ";
        $query .= "                  ) ";

        $db->query($query);


        //合格点マスタ
        # 2005/02/07 下位コースのレコードは消さないで、下位コースのREPROCESS_FLGに１をセットするように変更
        $query  = "UPDATE entexam_passingmark_mst ";
        $query .= "   SET reprocess_flg = '1' ";
        $query .= " WHERE entexamyear = '".$model->year."'";
        $query .= "   AND testdiv     = '".$model->testdiv."'";
        $query .= "   AND shdiv       = '".$model->shdiv."'";
        $query .= "   AND (coursecd || majorcd || examcoursecd )";
        $query .= "    IN ";
        $query .= "     (SELECT DISTINCT t2.coursecd || t2.majorcd || t2.examcoursecd ";
        $query .= "        FROM (SELECT * FROM entexam_wishdiv_mst ";
        $query .= "               WHERE entexamyear = '".$model->year."' ";
        $query .= "                 AND testdiv     = '$model->testdiv'  ";
        $query .= "                 AND coursecd || majorcd || examcoursecd = '".$model->course."' ";
        $query .= "             ) t1 INNER JOIN entexam_wishdiv_mst t2  ";
        $query .= "                          ON t1.entexamyear = t2.entexamyear  ";
        $query .= "                         AND t1.testdiv     = t2.testdiv  ";
        $query .= "                         AND t1.desirediv   = t2.desirediv  ";
        $query .= "                         AND ( t1.wishno < t2.wishno OR t1.coursecd || t1.majorcd || t1.examcoursecd = t2.coursecd || t2.majorcd || t2.examcoursecd ) ";
        $query .= "       WHERE T2.coursecd || T2.majorcd || T2.examcoursecd <> '".$model->course."') ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     entexam_passingmark_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."' AND ";
        $query .= "     testdiv     = '".$model->testdiv."' AND ";
        $query .= "     shdiv       = '".$model->shdiv."' AND ";
        $query .= "     coursecd || majorcd || examcoursecd  IN";
        $query .= "    ( SELECT DISTINCT";
        $query .= "             coursecd || majorcd || examcoursecd";
        $query .= "        FROM entexam_wishdiv_mst";
        $query .= "       WHERE entexamyear = '".$model->year."' AND ";
        $query .= "             testdiv     = '".$model->testdiv."' AND ";
        $query .= "             coursecd || majorcd || examcoursecd  = '".$model->course."' ";
        $query .= "    )";
        $db->query($query);

        //2005/09/13 arakaki 処理対象外合格者数カウント(1:付属推薦、2:中高一貫、3:スポーツ推薦)
        $other_cnt = $db->getRow(knjl101kQuery::selectQueryOtherCnt($model), DB_FETCHMODE_ASSOC);

        $query  = " INSERT INTO entexam_passingmark_mst ";
        $query .= "     ( ";
        $query .= "         entexamyear ";
        $query .= "        ,testdiv ";
        $query .= "        ,shdiv ";
        $query .= "        ,coursecd ";
        $query .= "        ,majorcd ";
        $query .= "        ,examcoursecd ";
        $query .= "        ,reprocess_flg ";                 // add 2005/02/07
        $query .= "        ,judge_col ";
        $query .= "        ,border_score ";
        $query .= "        ,a_border_score ";
        $query .= "        ,b_border_score ";
        $query .= "        ,success_cnt ";
        $query .= "        ,a_success_cnt ";
        $query .= "        ,b_success_cnt ";
        $query .= "        ,s_success_cnt ";

        $query .= "        ,success_sex1_cnt ";     #2005/12/27
        $query .= "        ,success_sex2_cnt ";     #2005/12/27
        $query .= "        ,a_success_sex1_cnt ";   #2005/12/27
        $query .= "        ,a_success_sex2_cnt ";   #2005/12/27
        $query .= "        ,b_success_sex1_cnt ";   #2005/12/27
        $query .= "        ,b_success_sex2_cnt ";   #2005/12/27

        $query .= "        ,back_rate ";
        $query .= "        ,capa_cnt ";
        $query .= "        ,registercd ";
        $query .= "     ) ";
        $query .= "     ( ";

        $query .= "      SELECT ";
        $query .= "         '".$model->year."' ";                   //年度
        $query .= "        ,'".$model->testdiv."' ";                //試験区分
        $query .= "        ,'".$model->shdiv."' ";                  //専併区分
        $query .= "        ,'".SUBSTR($model->course, 0, 1)."' ";   //課程コード
        $query .= "        ,'".SUBSTR($model->course, 1, 3)."' ";   //学科コード
        $query .= "        ,'".SUBSTR($model->course, 4, 4)."' ";   //コースコード
        $query .= "        ,'0' ";                                  //再計算フラグ(0は計算済)
        $query .= "        ,'".$model->judge_col."' ";              //判定項目(AorB配点)
        $query .= "        , ".$model->field["BORDER_SCORE"]." ";   //基本合格点
        $query .= "        , ".$model->field["A_BORDER_SCORE"]." "; //Ａ判定合格点
        $query .= "        , ".$model->field["B_BORDER_SCORE"]." "; //Ｂ判定合格点
        $query .= "        ,success_cnt   + " .$other_cnt["K_SCNT"] ;                          //基本合格者数
        $query .= "        ,a_success_cnt + " .$other_cnt["A_SCNT"] ;                        //Ａ判定合格者数
        $query .= "        ,b_success_cnt + " .$other_cnt["B_SCNT"] ;                        //Ｂ判定合格者数
        $query .= "        ,".$other_cnt["S_SUCCESS_CNT"]." ";      //スポーツ推薦合格者数       2005/11/14 arakaki

        $query .= "        ,success_sex1_cnt ";
        $query .= "        ,success_sex2_cnt ";
        $query .= "        ,a_success_sex1_cnt ";
        $query .= "        ,a_success_sex2_cnt ";
        $query .= "        ,b_success_sex1_cnt ";
        $query .= "        ,b_success_sex2_cnt ";

        $query .= "        ," .$model->field["BACK_RATE"]."";       //戻り率
        $query .= "        ," .$model->field["CAPA_CNT"]." ";        //収容人数
        $query .= "        ,'".STAFFCD."' ";
        $query .= "     FROM ";
        $query .= "          (SELECT ";
        /*** UPDATE 2005/11/04 by ameku ***/
         
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '1' THEN 1 ELSE 0 END)                        AS success_cnt, ";    //合格者でシミュレーションで正規合格フラグが１の志願者
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '2' AND t3.judgement = '1' THEN 1 ELSE 0 END) AS a_success_cnt, ";  //合格者でシミュレーションで正規合格フラグが１以外で志願者事前相談データでＡ判定の志願者
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '3' AND t3.judgement = '2' THEN 1 ELSE 0 END) AS b_success_cnt, ";   //合格者でシミュレーションで正規合格フラグが１以外で志願者事前相談データでＢ判定の志願者

        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '1' AND t1.SEX = '1' THEN 1 ELSE 0 END)       AS success_sex1_cnt, ";
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '1' AND t1.SEX = '2' THEN 1 ELSE 0 END)       AS success_sex2_cnt, ";
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '2' AND t3.judgement = '1' AND t1.SEX = '1' THEN 1 ELSE 0 END) AS a_success_sex1_cnt, ";
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '2' AND t3.judgement = '1' AND t1.SEX = '2' THEN 1 ELSE 0 END) AS a_success_sex2_cnt, ";
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '3' AND t3.judgement = '2' AND t1.SEX = '1' THEN 1 ELSE 0 END) AS b_success_sex1_cnt, ";
        $query .= "              SUM(CASE WHEN t2.judgement != '7' AND t2.regularsuccess_flg  = '3' AND t3.judgement = '2' AND t1.SEX = '2' THEN 1 ELSE 0 END) AS b_success_sex2_cnt ";

        $query .= "            FROM ";   
        $query .= "              entexam_applicantbase_dat   t1 ";
        $query .= "              INNER JOIN entexam_judge_tmp    t2 ";
        $query .= "                      ON t1.entexamyear  = t2.entexamyear AND ";
        $query .= "                         t1.testdiv      = t2.testdiv AND ";
        $query .= "                         t1.examno       = t2.examno ";
        $query .= "              LEFT OUTER JOIN entexam_applicantcons_dat   t3 ";
        $query .= "                          ON t1.entexamyear  = t3.entexamyear AND ";
        $query .= "                             t1.testdiv      = t3.testdiv AND ";
        $query .= "                             t1.shdiv        = t3.shdiv AND ";
        $query .= "                             t1.examno       = t3.examno AND ";
        $query .= "                             t2.coursecd     = t3.coursecd AND ";
        $query .= "                             t2.majorcd      = t3.majorcd AND ";
        $query .= "                             t2.examcoursecd = t3.examcoursecd AND ";
        $query .= "                             t3.coursecd || t3.majorcd || t3.examcoursecd = '".$model->course."'";
        $query .= "           WHERE ";
        $query .= "               t1.entexamyear  = '".$model->year."'    AND ";
        $query .= "               t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "               t1.testdiv      = '".$model->testdiv."' AND ";
        $query .= "               t1.shdiv        = '".$model->shdiv."'   AND";

        //附属推薦、中高一貫、未受験は、除く        2005/08/11 arakaki
        $query .= "                t1.examno NOT IN (SELECT examno ";
        $query .= "                                    FROM entexam_applicantbase_dat ";
        $query .= "                                   WHERE entexamyear     = t1.entexamyear AND ";
        $query .= "                                         testdiv         = t1.testdiv     AND ";
        $query .= "                                        (applicantdiv    IN('1','2','3')  OR  ";     // 1:附属推薦、2:中高一貫、3:スポーツ推薦  #2005/09/12 arakaki
        $query .= "                                         judgement       = '8'            ))  ";     // 8:未受験

        $query .= "          ) t ";   
        $query .= " ) ";

        #echo $query;
        $db->query($query);

        //合格コース履歴データ削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_COURSE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND EXAMNO IN (SELECT ";
        $query .= "                        EXAMNO ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_JUDGE_TMP ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR = '".$model->year."' ";
        $query .= "                        AND TESTDIV = '".$model->testdiv."') ";

        $db->query($query);

        //合格コース履歴データ追加
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_COURSE_HIST_DAT ";
        $query .= " (SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     '".$model->testdiv."', ";
        $query .= "     t1.EXAMNO, ";
        $query .= "     0, ";
        $query .= "     t1.JUDGEMENT, ";
        $query .= "     t1.JUDGEMENT_GROUP_NO, ";
        $query .= "     t1.SUC_COURSECD, ";
        $query .= "     t1.SUC_MAJORCD, ";
        $query .= "     t1.SUC_COURSECODE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT t1 ";
        $query .= "  WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND t1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND t1.EXAMNO IN (SELECT t2.EXAMNO ";
        $query .= "                       FROM ENTEXAM_JUDGE_TMP t2 ";
        $query .= "                       WHERE ";
        $query .= "                           t2.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "                           AND t2.TESTDIV = '".$model->testdiv."' ";
        $query .= "                      ) ";
        $query .= " ) ";

        $db->query($query);

        //仮判定データすべて削除
        $query = " DELETE FROM entexam_judge_tmp ";

        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //再計算フラグチェック
    function CheckReprocess_flg($year)
    {
        $query  = "SELECT MEISYOU_GET(T1.testdiv, 'L003', 1) AS testdiv ";
        $query .= "      ,MEISYOU_GET(T1.shdiv,   'L006', 1) AS shdiv ";
        $query .= "      ,T2.examcourse_name ";
        $query .= "  FROM entexam_passingmark_mst T1 ";
        $query .= "      ,entexam_course_mst T2 ";
        $query .= " WHERE T1.entexamyear   = T2.entexamyear ";
        $query .= "   AND T1.coursecd      = T2.coursecd ";
        $query .= "   AND T1.majorcd       = T2.majorcd ";
        $query .= "   AND T1.examcoursecd  = T2.examcoursecd ";
        $query .= "   AND T1.reprocess_flg = '1' ";
        $query .= "   AND T1.entexamyear   = '".$year."'";
        $query .= " ORDER BY T1.testdiv, T1.shdiv, T1.coursecd || T1.majorcd || T1.examcoursecd";
        return $query;
    }

    //上位志望区分確定データ確認 2005/08/31
    function selectQueryRankhFixChk(&$model)
    {
        $query  = "WITH ";
        $query .= "T_OWN AS( ";                             #志望区分マスタデータ抽出
        $query .= "    SELECT ENTEXAMYEAR, ";
        $query .= "           TESTDIV, ";
        $query .= "           DESIREDIV, ";
        $query .= "           WISHNO  ";
        $query .= "      FROM ENTEXAM_WISHDIV_MST ";
        $query .= "     WHERE ENTEXAMYEAR      = '".$model->year."' ";
        $query .= "       AND TESTDIV          = '".$model->testdiv."' ";
        $query .= "       AND COURSECD || MAJORCD || EXAMCOURSECD = '".$model->course."' ";
        $query .= "), ";
        $query .= "T_RANK_H AS ( ";                         #上位志望データ抽出
        $query .= "    SELECT T01.ENTEXAMYEAR, ";
        $query .= "           T01.TESTDIV, ";
        $query .= "           T01.DESIREDIV, ";
        $query .= "           T01.WISHNO, ";
        $query .= "           T01.COURSECD || T01.MAJORCD || T01.EXAMCOURSECD AS CME ";
        $query .= "      FROM ENTEXAM_WISHDIV_MST AS T01, ";
        $query .= "           T_OWN ";
        $query .= "     WHERE T01.ENTEXAMYEAR  = T_OWN.ENTEXAMYEAR ";
        $query .= "       AND T01.TESTDIV      = T_OWN.TESTDIV ";
        $query .= "       AND T01.DESIREDIV    = T_OWN.DESIREDIV ";
        $query .= "       AND T01.WISHNO       < T_OWN.WISHNO ";
        $query .= ") ";
        $query .= " SELECT * ";                             #上位志望未確定データ抽出
        $query .= " FROM T_RANK_H ";
        $query .= " WHERE (ENTEXAMYEAR,TESTDIV,CME) ";
        $query .= "  NOT IN (SELECT ENTEXAMYEAR,TESTDIV,COURSECD || MAJORCD || EXAMCOURSECD ";
        $query .= "            FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= "           WHERE ENTEXAMYEAR  = T_RANK_H.ENTEXAMYEAR ";
        $query .= "             AND TESTDIV      = T_RANK_H.TESTDIV ";
        $query .= "             AND SHDIV        = '".$model->shdiv."' ";
        $query .= "         ) ";

        #echo $query;
        return $query;
    }

    //スポーツ推薦者数算出
    function selectQueryOtherCnt(&$model)
    {
        $query .= " SELECT COUNT(*)                                             AS S_SUCCESS_CNT, ";
        $query .= "        COUNT(CASE WHEN REGULARSUCCESS_FLG = '1' THEN 1 END) AS K_SCNT, ";
        $query .= "        COUNT(CASE WHEN REGULARSUCCESS_FLG = '2' THEN 1 END) AS A_SCNT, ";
        $query .= "        COUNT(CASE WHEN REGULARSUCCESS_FLG = '3' THEN 1 END) AS B_SCNT ";
        $query .= "   FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "  WHERE ENTEXAMYEAR  = '".$model->year."'  ";
        $query .= "    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "    AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "    AND SHDIV        = '".$model->shdiv."'   ";
        $query .= "    AND APPLICANTDIV ='3' ";
        $query .= "    AND REGULARSUCCESS_FLG IN('1','2','3') ";        #(1:基本点合格,2:事前相談Ａ判定合格,3:事前相談Ｂ判定合格)
        $query .= "    AND SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE = '".$model->course."'  ";
        #echo $query;
        return $query;

#        $query .= " SELECT COALESCE(SUM(CASE WHEN APPLICANTDIV = '3'       THEN 1 ELSE 0 END),0)          AS S_SUCCESS_CNT ";
#        $query .= "   FROM ENTEXAM_APPLICANTBASE_DAT ";
#        $query .= "  WHERE ENTEXAMYEAR  = '".$model->year."'  ";
#        $query .= "    AND TESTDIV      = '".$model->testdiv."' ";
#        $query .= "    AND SHDIV        = '".$model->shdiv."'   ";  #2005/11/14
#        $query .= "    AND JUDGEMENT   != '7' ";
#        $query .= "    AND SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE = '".$model->course."'  ";
#        #echo $query;
#        return $query;

    }

    //--------------------------------------------
    //通知Ｎｏ発行処理実行
    //--------------------------------------------
#    function notice(&$model)
#    {
#        $db = Query::dbCheckOut();
#        $db->autoCommit(false);
# 
#        //通知Ｎｏ初期化
#        $query = " UPDATE ";
#        $query .= "     entexam_applicantbase_dat ";
#        $query .= " SET ";
#        $query .= "     (success_noticeno,failure_noticeno) = (null,null) ";
#        $query .= " WHERE ";
#        $query .= "     entexamyear = '" .$model->year ."' AND ";
#        $query .= "     testdiv     = '" .$model->testdiv ."' ";
# 
#        $db->query($query);
#        
#        //合格通知Ｎｏ更新
#        $query  = " UPDATE ";
#        $query .= "     entexam_applicantbase_dat t1 ";
#        $query .= " SET ";
#        $query .= "     t1.success_noticeno = (SELECT ";
#        $query .= "         CHAR(st1.ke+6000) ";    //6001から開始
#        $query .= "     FROM ";
#        $query .= "         (SELECT ";
#        $query .= "             ROW_NUMBER() OVER() AS ke, ";
#        $query .= "             sst1.entexamyear, ";
#        $query .= "             sst1.testdiv, ";
#        $query .= "             sst1.examno ";
#        $query .= "         FROM ";
#        $query .= "             (SELECT ";
#        $query .= "                 * ";
#        $query .= "             FROM ";
#        $query .= "                 entexam_applicantbase_dat ";
#        $query .= "             WHERE ";
#        $query .= "                 entexamyear = '" .$model->year ."' AND ";
#        $query .= "                 testdiv     = '" .$model->testdiv ."' AND ";
#        $query .= "                 judgement BETWEEN '1' AND '6' "; //1:第1, 2:第2, 3:第3, 4:推薦, 5:追加, 6:コース変更 合格
#        $query .= "             ) sst1 ";
#        $query .= "         ) AS st1 ";
#        $query .= "     WHERE ";
#        $query .= "         st1.examno = t1.examno ";
#        $query .= "     ORDER BY ";
#        $query .= "         st1.examno ";
#        $query .= "     ) ";
#        $query .= " WHERE ";
#        $query .= "     t1.entexamyear  = '" .$model->year ."' AND ";
#        $query .= "     t1.testdiv      = '" .$model->testdiv ."' AND ";
#        $query .= "     t1.judgement BETWEEN '1' AND '6' "; //1:第1, 2:第2, 3:第3, 4:推薦, 5:追加, 6:コース変更 合格
# 
#        $db->query($query);
# 
#        //不合格通知Ｎｏ更新
#        $query  = " UPDATE ";
#        $query .= "     entexam_applicantbase_dat t1 ";
#        $query .= " SET ";
#        $query .= "     t1.failure_noticeno = (select ";
#        $query .= "         CHAR(st1.ke+8000) "; //8001から開始
#        $query .= "     FROM ";
#        $query .= "         (SELECT ";
#        $query .= "             ROW_NUMBER() OVER() AS ke, ";
#        $query .= "             sst1.entexamyear, ";
#        $query .= "             sst1.testdiv, ";
#        $query .= "             sst1.examno ";
#        $query .= "         FROM ";
#        $query .= "             (SELECT ";
#        $query .= "                 * ";
#        $query .= "             FROM ";
#        $query .= "                 entexam_applicantbase_dat ";
#        $query .= "             WHERE ";
#        $query .= "                 entexamyear = '" .$model->year ."' AND ";
#        $query .= "                 testdiv     = '" .$model->testdiv ."' AND ";
#        $query .= "                 judgement   = '7' ";    //7:不合格(ちなみに8:未受験、9:保留)
#        $query .= "             ) sst1 ";
#        $query .= "         ) AS st1 ";
#        $query .= "     WHERE ";
#        $query .= "         st1.examno = t1.examno ";
#        $query .= "     ORDER BY ";
#        $query .= "         st1.examno ";
#        $query .= "     ) ";
#        $query .= " WHERE ";
#        $query .= "     t1.entexamyear  = '" .$model->year ."' AND ";
#        $query .= "     t1.testdiv      = '" .$model->testdiv ."' AND ";
#        $query .= "     t1.judgement    = '7' ";    //7:不合格(ちなみに8:未受験、9:保留)
# 
#        $db->query($query);
# 
#        $db->commit(); 
#        Query::dbCheckIn($db);
#    }

}
?>