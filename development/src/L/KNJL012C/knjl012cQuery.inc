<?php

require_once('for_php7.php');

class knjl012cquery extends Query {

    //学校コードを取得：カレッジを判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
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

        $query  = "INSERT INTO entexam_desire_dat ";
        $query .= "SELECT '".$model->ObjYear."'";
        $query .= "      ,'".$model->applicantdiv."'";
        $query .= "      ,'".$model->testdiv."'";
        $query .= "      ,'1' AS exam_type ";
        $query .= "      ,T1.examno ";
        $query .= "      ,'1' AS applicant_div ";         //志願　1:有り/2:無し
        $query .= "      ,T1.examnee_div";                //受験　1:有り/2:欠席
        $query .= "      ,'".STAFFCD."'";
        $query .= "      ,SYSDATE()";
        $query .= " FROM (";
        $query .= "SELECT T1.examno,CASE value(W1.receptno,'') WHEN '' THEN '2' ELSE '1' END AS examnee_div";    //受付が無ければ欠席(2)で登録
        $query .= "  FROM entexam_applicantbase_dat T1";
        $query .= "       LEFT OUTER JOIN entexam_recept_dat W1 ";
        $query .= "                    ON W1.entexamyear  = T1.entexamyear ";
        $query .= "                   AND W1.applicantdiv = T1.applicantdiv ";
        $query .= "                   AND W1.testdiv      = T1.testdiv ";
        $query .= "                   AND W1.examno       = T1.examno ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."' ";
        //後期入試の場合、前期入試の合格者を除く(カレッジ以外)
        if ($model->applicantdiv == "1" && $model->testdiv == "2") {
            $query .= "   AND NOT EXISTS( ";
            $query .= "       SELECT 'X' ";
            $query .= "         FROM ENTEXAM_APPLICANTBASE_DAT T2 ";
            $query .= "              LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
            $query .= "                                             AND R1.APPLICANTDIV = T2.APPLICANTDIV ";
            $query .= "                                             AND R1.TESTDIV = T2.TESTDIV ";
            $query .= "                                             AND R1.EXAMNO = T2.EXAMNO ";
            $query .= "        WHERE T2.ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "          AND T2.APPLICANTDIV = '".$model->applicantdiv."' ";
            //カレッジ中学B日程の場合、特色やA日程の合格者を除く。但し、第2志望合格者は、B日程受験可
            if ($model->isCollege) {
                $query .= "          AND T2.TESTDIV   in ('1','7') ";//1:A日程 7:特色
                $query .= "          AND NOT (VALUE(T2.TESTDIV,'') in ('1','7') AND VALUE(T2.SHDIV,'') IN ('8','B') AND VALUE(R1.JUDGEDIV,'') = '9') "; //第2志望合格者とは、専S選抜・総合や併S選抜・総合で総合に合格したもの
            //五條の場合、前期又は自己推薦の合格者を除く
            } else if ($model->isGojou) {
                $query .= "          AND T2.TESTDIV   in ('1','7') ";//1:前期 7:自己推薦
                //五條の場合、前期専願Ⅱおよび前期併願でAB合格したものは、後期受験可
                $query .= "          AND NOT (VALUE(T2.TESTDIV,'') = '1' AND VALUE(T2.SHDIV,'') IN ('2','7') AND VALUE(R1.JUDGEDIV,'') = '9') ";
            } else {
                $query .= "          AND T2.TESTDIV   = '1' ";//前期入試
                //和歌山中学の場合、前期G合格者で入学手続済は、後期受験可
                $query .= "          AND NOT (VALUE(T2.TESTDIV,'') = '1' AND VALUE(R1.JUDGEDIV,'') = '9') ";
            }
            $query .= "          AND T2.JUDGEMENT = '1' ";//合格者
            $query .= "          AND T2.EXAMNO = T1.RECOM_EXAMNO ";//受験番号
            $query .= "       ) ";
        }
        $query .= " ) T1";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
