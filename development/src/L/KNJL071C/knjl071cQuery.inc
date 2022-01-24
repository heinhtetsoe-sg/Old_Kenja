<?php

require_once('for_php7.php');

class knjl071cQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
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
        $query = "SELECT namecd2, name1, name2, abbv1, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'";

        return $query;
    }
    
    //一覧表示
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.EXAMNO, ";
//        $query .= "     T1.NAME, ";
//        $query .= "     T1.NAME_KANA, ";
//        $query .= "     T1.SEX, ";
//        $query .= "     N1.NAME2 AS SEX_NAME, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     T2.RECEPTNO, ";
        if ($model->isCollege && $model->applicantdiv == "1" && $model->testdiv == "1") {
            $query .= "     T2.AVARAGE1 AS TOTAL4, ";
            $query .= "     T2.TOTAL_RANK1 AS TOTAL_RANK4, ";
        } else {
            $query .= "     T2.TOTAL4 AS TOTAL4, ";
            $query .= "     T2.TOTAL_RANK4 AS TOTAL_RANK4, ";
        }
        $query .= "     T2.JUDGEDIV, ";
        $query .= "     N2.NAME1 AS JUDGEDIV_NAME, ";
        $query .= "     N2.ABBV1 AS JUDGEDIV_ABBV, ";
        $query .= "     N2.NAMESPARE1 AS SUC_JUDGEDIV, ";
        $query .= "     N3.NAME1 AS ZENKI_HANTEI ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "            ON N1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "           AND N1.NAMECD1 = 'Z002' ";
        $query .= "           AND N1.NAMECD2 = T1.SEX ";
        $query .= "    INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "            ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "           AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "           AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "            ON N2.YEAR    = T2.ENTEXAMYEAR ";
        $query .= "           AND N2.NAMECD1 = 'L013' ";
        $query .= "           AND N2.NAMECD2 = T2.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T3 ";
        $query .= "            ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "           AND T3.APPLICANTDIV = '1' ";
        $query .= "           AND T3.TESTDIV      = '1' ";
        $query .= "           AND T3.EXAMNO       = T1.RECOM_EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ";
        $query .= "            ON N3.YEAR    = T3.ENTEXAMYEAR ";
        $query .= "           AND N3.NAMECD1 = 'L013' ";
        $query .= "           AND N3.NAMECD2 = T3.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        //五條中学は、専併区分は参照しない
        if (!$model->isGojou || $model->applicantdiv != "1" && !$model->isCollege) {
            $query .= "     T1.SHDIV        = '".$model->shdiv."' AND ";
        }
        $query .= "     T1.EXAMNO BETWEEN '".$model->examno."' AND '".$model->e_examno."'  ";
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:受験番号順で表示 2:成績順で表示
        if ($model->sort == "2") $query .= "     value(TOTAL4,-1) DESC, ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //MAX/MIN受験番号を取得
    function getMaxExamno($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     max(EXAMNO) as MAX_EXAMNO, ";
        $query .= "     min(EXAMNO) as MIN_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR ='".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV='".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV     ='".$model->testdiv."' ";
        //五條中学は、専併区分は参照しない
        if (!$model->isGojou || $model->applicantdiv != "1" && !$model->isCollege) {
            $query .= "     AND SHDIV       ='".$model->shdiv."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //受験コースを取得
    function getCourseQuery($year, $applicantdiv, $testdiv) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR ='{$year}' AND ";
        $query .= "     APPLICANTDIV='{$applicantdiv}' AND ";
        $query .= "     TESTDIV     ='{$testdiv}' ";

        return $query;
    }

    //移行合格（移行希望）チェック
    function shiftCheck($year, $examno)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$year."' AND
                        SHIFT_DESIRE_FLG = '1' AND
                        EXAMNO      = '".$examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //特併合格チェック
    function slideCheck($year, $examno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' AND ";
        $query .= "     EXAMNO      = '{$examno}' AND ";
        $query .= "     SLIDE_FLG   = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //名称マスタの取得（判定コード）
        $opt_judge = array();
        $result = $db->query(knjl071cQuery::GetName("L013",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_judge[$row["NAMECD2"]] = $row["NAMESPARE1"];//合格扱い
        }

        //更新前のデータを取得
        $cur_data = array();
        $result    = $db->query(knjl071cQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cur_data[$row["EXAMNO"]] = $row["JUDGEDIV"];
        }

        foreach ($model->score as $examno => $val)
        {
            //更新前の値が変更されてない場合は何もしない。つまり、変更データのみ更新する。
            if ($cur_data[$examno] == $val["JUDGEDIV"]) {
                continue;
            } else {
                //条件
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       TESTDIV       = '".$model->testdiv."' AND";
                $where .= "       EXAMNO        = '".$examno."'";
                //受付データ
                $data = array();
                $data["JUDGEDIV"][TEXT]     = $val["JUDGEDIV"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
                //基礎データ
                $tmp_data = array();
                $tmp_data["SUC_COURSECD"] = "";
                $tmp_data["SUC_MAJORCD"] = "";
                $tmp_data["SUC_COURSECODE"] = "";
                $tmp_data["SPECIAL_MEASURES"] = "";
                $tmp_data["PROCEDUREDIV"] = "";
                $tmp_data["ENTDIV"] = "";
                //「1:合格」扱い
                if ($opt_judge[$val["JUDGEDIV"]] == "1") {
                    //「7:特併合格(スライド合格)」の場合、JUDGEMENTはそのまま「7:特併合格(スライド合格)」をセット
                    $tmp_data["JUDGEMENT"]      = ($model->isGojou && $val["JUDGEDIV"] == "7") ? $val["JUDGEDIV"] : $opt_judge[$val["JUDGEDIV"]];
                    //「7:特進併願合格(スライド合格)」の場合、合格コースは入試区分が「7:特進文系」のコースをセット
                    $testdiv = ($model->isGojou && $val["JUDGEDIV"] == "7") ? "7" : $model->testdiv;
                    $query = knjl071cQuery::getCourseQuery($model->ObjYear, $model->applicantdiv, $testdiv);
                    $courseRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $tmp_data["SUC_COURSECD"]   = $courseRow["COURSECD"];
                    $tmp_data["SUC_MAJORCD"]    = $courseRow["MAJORCD"];
                    $tmp_data["SUC_COURSECODE"] = $courseRow["EXAMCOURSECD"];
                //その他
                } else {
                    //「JUDGEDIVと同じ」とする。但し「4:移行合格」は「2:不合格」とする。
                    $tmp_data["JUDGEMENT"] = ($val["JUDGEDIV"] == "4") ? "2" : $val["JUDGEDIV"];
                }
                $data = array();
                $data["JUDGEMENT"][TEXT]        = $tmp_data["JUDGEMENT"];
                $data["SUC_COURSECD"][TEXT]     = $tmp_data["SUC_COURSECD"];
                $data["SUC_MAJORCD"][TEXT]      = $tmp_data["SUC_MAJORCD"];
                $data["SUC_COURSECODE"][TEXT]   = $tmp_data["SUC_COURSECODE"];
                $data["SPECIAL_MEASURES"][TEXT] = $tmp_data["SPECIAL_MEASURES"];
                $data["PROCEDUREDIV"][TEXT]     = $tmp_data["PROCEDUREDIV"];
                $data["ENTDIV"][TEXT]           = $tmp_data["ENTDIV"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>