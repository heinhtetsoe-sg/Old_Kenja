<?php

require_once('for_php7.php');

class knjl013cquery extends Query {

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

    //受験番号を配列にセット（１件も無い場合、存在エラー）
    function existCheck($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT T1.EXAMNO ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV = '".$model->testdiv."' ";
        //後期入試の場合、前期入試の合格者を除く
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
        $query .= " ORDER BY T1.EXAMNO ";

        $examno = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $examno[] = $row["EXAMNO"];
        }

        Query::dbCheckIn($db);

        return $examno;
    }

    //重複データチェック(同じ受験番号が違う入試制度、入試区分に在る場合、重複エラー）
    function existCheck2($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT RECEPTNO ";
        $query .= "      ,EXAMNO ";
        $query .= "      ,N1.NAME1 AS APPLICANTDIV  ";
        $query .= "      ,N2.NAME1 AS TESTDIV  ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1  ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.APPLICANTDIV  ";
        $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T1.TESTDIV  ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",$model->examno)."') ";
        $query .= "   AND (APPLICANTDIV != '".$model->applicantdiv."' OR TESTDIV != '".$model->testdiv."')";
        $query .= " ORDER BY RECEPTNO ";

        $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $existdata;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除処理（一旦すべてを削除する）
        $query  = "DELETE FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $db->query($query);

        $query  = "DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $db->query($query);

        //追加処理
        foreach ($model->examno as $key => $val)
        {
            //受付番号の値
            if ($model->recept_div == "1") {
                //受験番号をコピー
                $receptno = $val;
            } else {
                //連番(受験番号順)
                $receptno = $key + 1;
                $receptno = sprintf("%04d",$receptno);
            }

            $query  = "INSERT INTO entexam_recept_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'1'";
            $query .= "        ,'".sprintf("%04d",$receptno)."'";
            $query .= "        ,'".$val."'";
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
