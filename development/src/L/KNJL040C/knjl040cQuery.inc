<?php

require_once('for_php7.php');

class knjl040cQuery extends Query {

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
    function GetName($namecd1,$ObjYear,$order = "")
    {
        $query = "SELECT namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ".$order;
        return $query;
    }

    //試験会場
    function getExamHall($model)
    {
        $query  = "SELECT s_receptno, e_receptno, examhall_name FROM entexam_hall_dat ";
        $query .= " WHERE testdiv   = '".$model->testdiv."'";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.receptno";
        $query .= "      ,T2.examno";
        $query .= "      ,substr(T2.name,1,30)      AS name";
        $query .= "      ,substr(T2.name_kana,1,60) AS name_kana ";
        $query .= "      ,T2.sex AS sexcd";
        $query .= "      ,N1.NAME2 AS sex ";
        $query .= "  FROM  entexam_recept_dat T1 ";
        $query .= "       ,entexam_applicantbase_dat T2 ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T2.sex ";
        $query .= " WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "   AND T1.examno       = T2.examno ";
        $query .= "   AND T1.entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND T1.testdiv      = '".$model->testdiv."'";
        $query .= "   AND T1.receptno BETWEEN '".$model->receptno."' AND '".$model->e_receptno."'";
        $query .= " ORDER BY T1.receptno";
        return $query;
    }

    //更新(一度すべて削除してから、必須項目が入力されているレコードのみ追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //受験番号が志願者マスタに存在するかチェック
        foreach ($model->examno as $val)
        {
            if (!strlen($val)) continue;
            $query  = "SELECT * FROM entexam_applicantbase_dat ";
            $query .= " WHERE entexamyear = '".$model->ObjYear."'";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND testdiv = '".$model->testdiv."'";
            $query .= "   AND examno      = '".$val."'";
            if ((int)$db->getOne($query) == "0") {
                $model->setWarning("MSG303","受験番号：".$val);
                Query::dbCheckIn($db);
                return false;
            }
            //前期入試の合格者は後期入試を受験できない。
            if ($model->applicantdiv == "1" && $model->testdiv == "2") {
                $query  = "SELECT * FROM entexam_applicantbase_dat ";
                $query .= " WHERE entexamyear = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv = '".$model->testdiv."'";
                $query .= "   AND examno      = '".$val."'";
                $checkdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (strlen($checkdata["RECOM_EXAMNO"])) {
                    $query  = " SELECT COUNT(*) ";
                    $query .= "   FROM ENTEXAM_APPLICANTBASE_DAT T2 ";
                    $query .= "        LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
                    $query .= "                                       AND R1.APPLICANTDIV = T2.APPLICANTDIV ";
                    $query .= "                                       AND R1.TESTDIV = T2.TESTDIV ";
                    $query .= "                                       AND R1.EXAMNO = T2.EXAMNO ";
                    $query .= "  WHERE T2.ENTEXAMYEAR = '".$model->ObjYear."' ";
                    $query .= "    AND T2.APPLICANTDIV = '".$model->applicantdiv."' ";
                    $query .= "    AND T2.TESTDIV   = '1' ";//前期入試　A日程
                    $query .= "    AND T2.JUDGEMENT = '1' ";//合格者
                    $query .= "    AND T2.EXAMNO = '".$checkdata["RECOM_EXAMNO"]."' ";//受験番号
                    //カレッジ中学B日程の場合、特色やA日程の合格者を除く。但し、第2志望合格者は、B日程受験可
                    if ($model->isCollege) {
                        $query .= "    AND NOT (VALUE(T2.TESTDIV,'') = '1' AND VALUE(T2.SHDIV,'') IN ('8','B') AND VALUE(R1.JUDGEDIV,'') = '9') "; //第2志望合格者とは、専S選抜・総合や併S選抜・総合で総合に合格したもの
                    //五條の場合、前期専願Ⅱおよび前期併願でAB合格したものは、後期受験可
                    } else if ($model->isGojou) {
                        $query .= "    AND NOT (VALUE(T2.TESTDIV,'') = '1' AND VALUE(T2.SHDIV,'') IN ('2','7') AND VALUE(R1.JUDGEDIV,'') = '9') ";
                    //和歌山中学の場合、前期G合格者で入学手続済は、後期受験可
                    } else {
                        $query .= "    AND NOT (VALUE(T2.TESTDIV,'') = '1' AND VALUE(R1.JUDGEDIV,'') = '9') ";
                    }
                    if ($db->getOne($query) > 0) {
                        if ($model->isCollege) {
                            $model->setWarning("MSG901","受験番号：".$val."\\n"."A日程入試の合格者はB日程入試を受験できません。\\nA日程受験番号：".$checkdata["RECOM_EXAMNO"]);
                        } else {
                            $model->setWarning("MSG901","受験番号：".$val."\\n"."前期入試の合格者は後期入試を受験できません。\\n前期受験番号：".$checkdata["RECOM_EXAMNO"]);
                        }
                        Query::dbCheckIn($db);
                        return false;
                    }
                    //カレッジ中学B日程の場合、特色やA日程の合格者を除く。但し、第2志望合格者は、B日程受験可
                    if ($model->isCollege) {
                        $query  = " SELECT COUNT(*) ";
                        $query .= "   FROM ENTEXAM_APPLICANTBASE_DAT T2 ";
                        $query .= "        LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
                        $query .= "                                       AND R1.APPLICANTDIV = T2.APPLICANTDIV ";
                        $query .= "                                       AND R1.TESTDIV = T2.TESTDIV ";
                        $query .= "                                       AND R1.EXAMNO = T2.EXAMNO ";
                        $query .= "  WHERE T2.ENTEXAMYEAR = '".$model->ObjYear."' ";
                        $query .= "    AND T2.APPLICANTDIV = '".$model->applicantdiv."' ";
                        $query .= "    AND T2.TESTDIV   = '7' ";//特色
                        $query .= "    AND T2.JUDGEMENT = '1' ";//合格者
                        $query .= "    AND T2.EXAMNO = '".$checkdata["RECOM_EXAMNO"]."' ";//受験番号
                        $query .= "    AND NOT (VALUE(T2.TESTDIV,'') = '7' AND VALUE(T2.SHDIV,'') IN ('8','B') AND VALUE(R1.JUDGEDIV,'') = '9') "; //第2志望合格者とは、専S選抜・総合や併S選抜・総合で総合に合格したもの
                        if ($db->getOne($query) > 0) {
                            $model->setWarning("MSG901","受験番号：".$val."\\n"."特色入試の合格者はB日程入試を受験できません。\\n特色受験番号：".$checkdata["RECOM_EXAMNO"]);
                            Query::dbCheckIn($db);
                            return false;
                        }
                    //五條の場合、前期又は自己推薦の合格者を除く
                    } else if ($model->isGojou) {
                        $query  = " SELECT COUNT(*) ";
                        $query .= "   FROM ENTEXAM_APPLICANTBASE_DAT T2 ";
                        $query .= "  WHERE T2.ENTEXAMYEAR = '".$model->ObjYear."' ";
                        $query .= "    AND T2.APPLICANTDIV = '".$model->applicantdiv."' ";
                        $query .= "    AND T2.TESTDIV   = '7' ";//自己推薦入試
                        $query .= "    AND T2.JUDGEMENT = '1' ";//合格者
                        $query .= "    AND T2.EXAMNO = '".$checkdata["RECOM_EXAMNO"]."' ";//受験番号
                        if ($db->getOne($query) > 0) {
                            $model->setWarning("MSG901","受験番号：".$val."\\n"."自己推薦入試の合格者は後期入試を受験できません。\\n自己推薦受験番号：".$checkdata["RECOM_EXAMNO"]);
                            Query::dbCheckIn($db);
                            return false;
                        }
                    }
                }
            }
        }

        //重複データチェック(同じ受験番号が違う入試制度、入試区分で登録できない）
        $query  = "SELECT receptno";
        $query .= "      ,examno";
        $query .= "      ,N1.NAME1 AS applicantdiv ";
        $query .= "      ,N2.NAME1 AS testdiv ";
        $query .= "  FROM entexam_recept_dat T1 ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.applicantdiv ";
        $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T1.testdiv ";
        $query .= " WHERE entexamyear = '".$model->ObjYear."'";
        $query .= "   AND examno      IN ('".implode("','",$model->examno)."')";
        $query .= "   AND (applicantdiv != '".$model->applicantdiv."' OR testdiv != '".$model->testdiv."')";
        $query .= " ORDER BY receptno";
        $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($existdata)) {
            $msg  = "この受験番号は既に登録されています。　　\\n\\n";
            $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
            $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
            $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"]."\\n";
            $msg .= "入試区分 ： ".$existdata["TESTDIV"];
            $model->setWarning("MSG302", $msg);
            $db->rollback();
            Query::dbCheckIn($db);
            return false;
        }

        //更新前のデータを取得
        $cur_data = array();
        $result    = $db->query(knjl040cQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cur_data[$row["RECEPTNO"]] = $row["EXAMNO"];
        }

        $idx = 0;
        $db->autoCommit(false);

        for ($receptno = (int)$model->receptno; $receptno <= (int)$model->e_receptno; $receptno++)
        {
            //更新前の値が変更されてない場合は何もしない
            if ( $cur_data[(sprintf("%04d",$receptno))] == $model->examno[$idx]) {
                $idx++;
                continue;

            } else {

                //同じ受験番号で違う受付データには登録できない
                $query  = "SELECT receptno";
                $query .= "      ,examno";
                $query .= "      ,N1.NAME1 AS applicantdiv ";
                $query .= "      ,N2.NAME1 AS testdiv ";
                $query .= "  FROM entexam_recept_dat T1 ";
                $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.applicantdiv ";
                $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T1.testdiv ";
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND examno       = '".$model->examno[$idx]."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND receptno    <> '".sprintf("%04d",$receptno)."'";
                $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if (is_array($existdata)) {
                    $msg  = "この受験番号は既に登録されています。　　\\n\\n";
                    $msg .= "座席番号 ： ".$existdata["RECEPTNO"]."\\n";
                    $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
                    $msg .= "入試制度 ： ".$existdata["APPLICANTDIV"]."\\n";
                    $msg .= "入試区分 ： ".$existdata["TESTDIV"];
                    $model->setWarning("MSG302", $msg);
                    $db->rollback();
                    Query::dbCheckIn($db);
                    return false;
                }

                $query  = "DELETE FROM entexam_recept_dat";
                $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND receptno     = '".sprintf("%04d",$receptno)."'";
                $db->query($query);

                //ENTEXAM_SCORE_DAT
                $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                $query .= "  WHERE entexamyear = '".$model->ObjYear."'";
                $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                $query .= "   AND testdiv      = '".$model->testdiv."'";
                $query .= "   AND receptno     = '".sprintf("%04d",$receptno)."'";
                $db->query($query);

                //受験番号が入力されていたら更新
                if (strlen($model->examno[$idx])) {
                    $query  = "INSERT INTO entexam_recept_dat";
                    $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
                    $query .= " VALUES ('".$model->ObjYear."'";
                    $query .= "        ,'".$model->applicantdiv."'";
                    $query .= "        ,'".$model->testdiv."'";
                    $query .= "        ,'1'";
                    $query .= "        ,'".sprintf("%04d",$receptno)."'";
                    $query .= "        ,'".$model->examno[$idx]."'";
                    $query .= "        ,'".STAFFCD."'";
                    $query .= "        ,SYSDATE()";
                    $query .= ")";
                    $db->query($query);
                }
                $idx++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>