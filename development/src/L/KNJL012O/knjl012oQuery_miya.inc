<?php

require_once('for_php7.php');

class knjl012oquery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_desire_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";            //入試年度
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";       //入試制度
        $query .= "   AND testdiv      = '".$model->testdiv."'";            //出願区分
        $db->query($query);

// 基礎データのAPPLICANTDIVが複数になることはありえないので結合条件にしない //

#-- minei -----------------------------

        $query  = "INSERT INTO entexam_desire_dat ";
        /* ｎ回目の受付データに登録されている志願者は受験有り(2)で追加する! */
        $query .= "WITH now_receptd (examno,receptno,testdiv) AS ";
        $query .= "    (SELECT examno,receptno,testdiv";
        $query .= "       FROM entexam_recept_dat";
        $query .= "      WHERE entexamyear  ='".$model->ObjYear."' ";
        $query .= "        AND testdiv      ='".$model->testdiv."' ";
        $query .= "    ) ";
        $query .= "SELECT '".$model->ObjYear."'";
        $query .= "      ,'".$model->applicantdiv."'";
        $query .= "      ,'".$model->testdiv."'";
        $query .= "      ,T1.examno ";
        $query .= "      ,'1' AS applicant_div ";         //志願　1:有り/2:無し
        $query .= "      ,T1.examnee_div";                //受験　1:有り/2:欠席
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE()";
        $query .= " FROM (";
        /* ｎ回目の基礎データに登録されている志願者 */
        $query .= "SELECT T1.examno,CASE COALESCE(W1.receptno,'') WHEN '' THEN '2' ELSE '1' END AS examnee_div";
        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "       LEFT OUTER JOIN now_receptd W1 ON W1.examno = T1.examno ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."' ";
        $query .= " UNION ";
        /* ｎ回以前で合格していない志願者 */
        $query .= "SELECT T1.examno,CASE COALESCE(W1.receptno,'') WHEN '' THEN '2' ELSE '1' END AS examnee_div";
        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "       LEFT OUTER JOIN now_receptd W1 ON W1.examno = T1.examno ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv      < '".$model->testdiv."' ";                //今回以前
        $query .= "   AND T1.judgement    NOT IN ('1','2','3') ";                   //合格、繰上、特別以外！
        $query .= " UNION ";
        /* ｎ回目に受付している者 */
        $query .= "SELECT T1.examno,'1' AS examnee_div";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= "      ,entexam_recept_dat        T2 ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T2.entexamyear  = T1.entexamyear ";
        $query .= "   AND T2.examno       = T1.examno ";
        $query .= "   AND T2.testdiv      = '".$model->testdiv."' ";                //今回受付
#--  mieni ---------------------------------


        /* ｎ-１回目合格者でかつｎ回目に受付している者 */
#        $query .= "SELECT T1.examno,'1' AS examnee_div";
#        $query .= "  FROM entexam_applicantbase_dat T1 ";
#        $query .= "      ,entexam_recept_dat        T2 ";
#        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
#        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
#        $query .= "   AND T1.testdiv      = '".($model->testdiv - 1)."'";           //前回
#        $query .= "   AND T1.judgement IN ('1','2','3') ";                          //合格、繰上合格、特別合格
#        $query .= "   AND T2.entexamyear  = T1.entexamyear ";
#        $query .= "   AND T2.examno       = T1.examno ";
#        $query .= "   AND T2.testdiv      = '".$model->testdiv."' ";                //今回受付





#-- sunagawa -----------------------------

#        $query  = "INSERT INTO entexam_desire_dat ";
#        $query .= "SELECT '".$model->ObjYear."'";
#        $query .= "      ,'".$model->applicantdiv."'";
#        $query .= "      ,'".$model->testdiv."'";
#        $query .= "      ,T1.examno ";
#        $query .= "      ,T1.applicant_div ";               //志願　1:有り/2:無し
#        $query .= "      ,COALESCE(T1.examnee_div,'1') ";   //受験　1:有り/2:欠席
#        $query .= "      ,'".STAFFCD."'";
#        $query .= "      ,SYSDATE()";
#        $query .= " FROM (";
#        /* ｎ回目の基礎データに登録されている志願者 */
#        $query .= "     SELECT W1.examno,'1' as applicant_div,W2.examnee_div ";
#        $query .= "       FROM entexam_applicantbase_dat W1 ";
#        $query .= "       LEFT OUTER JOIN (SELECT examno,'2' as examnee_div";
#        $query .= "                          FROM entexam_applicantbase_dat ";
#        $query .= "                         WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                           AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                           AND testdiv      = '".$model->testdiv."' ";
#        $query .= "                           AND examno NOT IN (SELECT examno";
#        $query .= "                                                FROM ENTEXAM_RECEPT_DAT ";
#        $query .= "                                               WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                                                 AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                                                 AND testdiv      = '".$model->testdiv."' ";
#        $query .= "                                              ) ";
#        $query .= "                        ) W2 ";
#        $query .= "                    ON W1.examno = W2.examno ";
#        $query .= "      WHERE W1.entexamyear  = '".$model->ObjYear."' ";
#        $query .= "        AND W1.applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "        AND W1.testdiv      = '".$model->testdiv."' ";
# 
#        $query .= " UNION ";
#        /* ｎ-１回目の不合格者、欠席者 */
#        $query .= "     SELECT W1.examno,'1' as applicant_div,W2.examnee_div ";
#        $query .= "       FROM entexam_applicantbase_dat W1 ";
#        $query .= "       LEFT OUTER JOIN (SELECT examno,'2' as examnee_div ";
#        $query .= "                          FROM entexam_applicantbase_dat ";
#        $query .= "                         WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                           AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                           AND testdiv      = '".($model->testdiv - 1)."' ";
#        $query .= "                           AND (judgement = '4' OR COALESCE(judgement,'') = '') ";
#        $query .= "                           AND examno NOT IN (SELECT examno ";
#        $query .= "                                                FROM entexam_recept_dat ";
#        $query .= "                                               WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                                                 AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                                                 AND testdiv      = '".$model->testdiv."' ";
#        $query .= "                                              ) ";
#        $query .= "                        ) W2 ";
#        $query .= "                    ON W1.examno = W2.examno ";
#        $query .= "      WHERE W1.entexamyear  = '".$model->ObjYear."' ";
#        $query .= "        AND W1.applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "        AND W1.testdiv      = '".($model->testdiv - 1)."' ";            //前回
#        $query .= "        AND (W1.judgement = '4' OR COALESCE(W1.judgement,'') = '') ";   //不合格、欠席
# 
#        $query .= " UNION ";
#        /* ｎ-１回目合格者でかつｎ回目に受付している者 */
#        $query .= "     SELECT W1.examno,'1' as applicant_div,W3.examnee_div ";
#        $query .= "       FROM entexam_recept_dat W2,";
#        $query .= "            entexam_applicantbase_dat W1 ";
#        $query .= "       LEFT OUTER JOIN (SELECT examno,'2' as examnee_div ";
#        $query .= "                          FROM entexam_applicantbase_dat ";
#        $query .= "                         WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                           AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                           AND testdiv      = '".($model->testdiv - 1)."' ";
#        $query .= "                           AND judgement IN ('1','2','3') ";
#        $query .= "                           AND examno NOT IN (SELECT examno ";
#        $query .= "                                                FROM entexam_recept_dat ";
#        $query .= "                                               WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "                                                 AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "                                                 AND testdiv      = '".$model->testdiv."' ";
#        $query .= "                                              ) ";
#        $query .= "                      ) W3 ";
#        $query .= "                  ON W1.examno = W3.examno ";
#        $query .= "      WHERE W1.entexamyear  = '".$model->ObjYear."' ";
#        $query .= "        AND W1.applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "        AND W1.testdiv      = '".($model->testdiv - 1)."' ";    //前回
#        $query .= "        AND W1.judgement    IN ('1','2','3') ";                 //合格、繰上合格、特別合格
#        $query .= "        AND W2.entexamyear  = W1.entexamyear ";
#        $query .= "        AND W2.applicantdiv = W1.applicantdiv ";
#        $query .= "        AND W2.examno       = W1.examno ";
#        $query .= "        AND W2.testdiv      = '".$model->testdiv."' ";          //今回受付



#-- nozawa -----------------------------
#        $query  = "INSERT INTO entexam_desire_dat ";
#        $query .= "SELECT '".$model->ObjYear."'";
#        $query .= "      ,'".$model->applicantdiv."'";
#        $query .= "      ,'".$model->testdiv."'";
#        $query .= "      ,T1.examno ";
#        $query .= "      ,'1'";         //志願　1:有り/2:無し
#        $query .= "      ,'2'";         //受験　1:有り/2:欠席
#        $query .= "      ,'".STAFFCD."'";
#        $query .= "      ,SYSDATE()";
#        $query .= " FROM (";
#        /* ｎ回目の基礎データに登録されている志願者 */
#        $query .= "SELECT examno,'1' as applicant_div ";
#        $query .= "  FROM entexam_applicantbase_dat ";
#        $query .= " WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "   AND testdiv      = '".$model->testdiv."' ";
#        $query .= " UNION ";
#        /* ｎ-１回目の不合格者、欠席者 */
#        $query .= "SELECT examno,'1' as applicant_div";
#        $query .= "  FROM entexam_applicantbase_dat ";
#        $query .= " WHERE entexamyear  = '".$model->ObjYear."' ";
#        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
#        $query .= "   AND testdiv      = '".($model->testdiv - 1)."' ";         //前回
#        $query .= "   AND (judgement = '4' OR COALESCE(judgement,'') = '') ";   //不合格、欠席
#        $query .= " UNION ";
#        /* ｎ-１回目合格者でかつｎ回目に受付している者 */
#        $query .= "SELECT T1.examno,'1' as applicant_div ";
#        $query .= "  FROM entexam_applicantbase_dat T1 ";
#        $query .= "      ,entexam_recept_dat T2 ";
#        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
#        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
#        $query .= "   AND T1.testdiv      = '".($model->testdiv - 1)."'";       //前回
#        $query .= "   AND T1.judgement IN ('1','2','3') ";                      //合格、繰上合格、特別合格
#        $query .= "   AND T2.entexamyear  = T1.entexamyear ";
#        $query .= "   AND T2.examno       = T1.examno ";
#        $query .= "   AND T2.testdiv      = '".$model->testdiv."' ";            //今回受付


        $query .= " ) T1";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
