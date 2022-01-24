<?php

require_once('for_php7.php');

class knjz040kQuery extends Query {

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = " SELECT entexamyear FROM entexam_judgecomp_mst ";
        $query .= "  GROUP BY entexamyear ";
        $query .= "  ORDER BY entexamyear DESC";
        return $query;
    }  

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(entexamyear) ";
        $query .= " FROM entexam_judgecomp_mst ";
        $query .= " WHERE entexamyear >= '" .$model->year. "'";
        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.coursecd || T1.majorcd || T1.examcoursecd as totalcd, ";
        $query .= "     T1.cmp_coursecd || T1.cmp_majorcd || T1.cmp_examcoursecd as cmp_totalcd, ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T1.shdiv, ";
        $query .= "     T1.judgement, ";
        $query .= "     T1.cmp_coursecd, ";
        $query .= "     T1.cmp_majorcd, ";
        $query .= "     T1.cmp_examcoursecd, ";
        $query .= "     T2.examcourse_name, ";
        $query .= "     T3.examcourse_name as cmp_examcourse_name, ";
        $query .= "     T4.abbv1 as s_name, ";
        $query .= "     T5.name1 as h_name, ";
        $query .= "     T6.name1 as s_judge_name, ";
        $query .= "     T7.name1 as h_judge_name ";
        $query .= " FROM ";
        $query .= "     entexam_judgecomp_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     entexam_course_mst T2 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T2.entexamyear AND ";
        $query .= "     T1.coursecd = T2.coursecd AND ";
        $query .= "     T1.majorcd = T2.majorcd AND ";
        $query .= "     T1.examcoursecd = T2.examcoursecd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     entexam_course_mst T3 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T3.entexamyear AND ";
        $query .= "     T1.cmp_coursecd = T3.coursecd AND ";
        $query .= "     T1.cmp_majorcd = T3.majorcd AND ";
        $query .= "     T1.cmp_examcoursecd = T3.examcoursecd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T4 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T4.year AND ";
        $query .= "     T1.shdiv = T4.namecd2 AND ";
        $query .= "     T4.namecd1 = 'L006' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T5 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T5.year AND ";
        $query .= "     T1.judgement = T5.namecd2 AND ";
        $query .= "     T5.namecd1 = 'L002' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T6 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T6.year AND ";
        $query .= "     T1.s_judgement = T6.namecd2 AND ";
        $query .= "     T6.namecd1 = 'L002' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T7 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T7.year AND ";
        $query .= "     T1.h_judgement = T7.namecd2 AND ";
        $query .= "     T7.namecd1 = 'L002' ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->year."'";

        return $query;
    }

    //受験コース取得
    function getExamCourse($year)
    {
        $query  = " SELECT coursecd, ";
        $query .= "        majorcd,";
        $query .= "        examcoursecd, ";
        $query .= "        examcourse_name ";
        $query .= "   FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear    = '".$year."' ";

        return $query;
    }

    //名称取得
    function getName($year, $namecd)
    {
        $query  = " SELECT namecd2,name1 ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year    = '".$year."' ";
        $query .= "    AND namecd1 = '".$namecd."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT T1.*, ";
        $query .= "        T1.coursecd || T1.majorcd || T1.examcoursecd AS totalcd, ";
        $query .= "        T1.cmp_coursecd || T1.cmp_majorcd || T1.cmp_examcoursecd AS cmp_totalcd ";
        $query .= "   FROM entexam_judgecomp_mst T1 ";
        $query .= "  WHERE T1.entexamyear = '".$model->year."' ";

        if($flg == 1){
            $query .= " AND T1.coursecd         = '".$model->coursecd."' ";
            $query .= " AND T1.majorcd          = '".$model->majorcd."' ";
            $query .= " AND T1.examcoursecd     = '".$model->examcoursecd."' ";
            $query .= " AND T1.shdiv            = '".$model->shdiv."' ";
            $query .= " AND T1.judgement        = '".$model->judgement."' ";
            $query .= " AND T1.cmp_coursecd     = '".$model->cmp_coursecd."' ";
            $query .= " AND T1.cmp_majorcd      = '".$model->cmp_majorcd."' ";
            $query .= " AND T1.cmp_examcoursecd = '".$model->cmp_examcoursecd."' ";
        }elseif($flg == 2){
            $query .= " AND T1.coursecd         = '".$model->field["COURSECD"]."' ";
            $query .= " AND T1.majorcd          = '".$model->field["MAJORCD"]."' ";
            $query .= " AND T1.examcoursecd     = '".$model->field["EXAMCOURSECD"]."' ";
            $query .= " AND T1.shdiv            = '".$model->field["SHDIV"]."' ";
            $query .= " AND T1.judgement        = '".$model->field["JUDGEMENT"]."' ";
            $query .= " AND T1.cmp_coursecd     = '".$model->field["CMP_COURSECD"]."' ";
            $query .= " AND T1.cmp_majorcd      = '".$model->field["CMP_MAJORCD"]."' ";
            $query .= " AND T1.cmp_examcoursecd = '".$model->field["CMP_EXAMCOURSECD"]."' ";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $data["ENTEXAMYEAR"][TEXT]      = $fields["YEAR"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["SHDIV"][TEXT]            = $fields["SHDIV"];
        $data["JUDGEMENT"][TEXT]        = $fields["JUDGEMENT"];
        $data["CMP_COURSECD"][TEXT]     = $fields["CMP_COURSECD"];
        $data["CMP_MAJORCD"][TEXT]      = $fields["CMP_MAJORCD"];
        $data["CMP_EXAMCOURSECD"][TEXT] = $fields["CMP_EXAMCOURSECD"];
        $data["S_JUDGEMENT"][TEXT]      = $fields["S_JUDGEMENT"];
        $data["H_JUDGEMENT"][TEXT]      = $fields["H_JUDGEMENT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "entexam_judgecomp_mst");
        return $query;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $query  = " DELETE FROM entexam_judgecomp_mst ";
        $query .= " WHERE entexamyear   = '".$fields["YEAR"]."' ";
        $query .= "   AND coursecd      = '".$fields["COURSECD"]."' ";
        $query .= "   AND majorcd       = '".$fields["MAJORCD"]."' ";
        $query .= "   AND examcoursecd  = '".$fields["EXAMCOURSECD"]."' ";
        $query .= "   AND shdiv         = '".$fields["SHDIV"]."' ";
        $query .= "   AND judgement     = '".$fields["JUDGEMENT"]."' ";
        $query .= "   AND cmp_coursecd      = '".$fields["CMP_COURSECD"]."' ";
        $query .= "   AND cmp_majorcd       = '".$fields["CMP_MAJORCD"]."' ";
        $query .= "   AND cmp_examcoursecd  = '".$fields["CMP_EXAMCOURSECD"]."' ";

        return $query;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM entexam_judgecomp_mst WHERE entexamyear = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $flg = false;
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      entexam_judgecomp_mst T1,";
        $query .= "      v_name_mst T2,";
        $query .= "      v_name_mst T3";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear    = '".$model->year."' AND";
        $query .= "      T1.shdiv          = T2.namecd2 AND";
        $query .= "      T2.namecd1        = 'L006' AND";
        $query .= "      T2.year           = '".$model->NextYear."' AND";
        $query .= "      T1.judgement      = T3.namecd2 AND";
        $query .= "      T3.namecd1        = 'L002' AND";
        $query .= "      T3.year           = '".$model->NextYear."' AND";
        $query .= "     (coursecd,majorcd,examcoursecd) IN (SELECT coursecd,majorcd,examcoursecd ";
        $query .= "                                           FROM entexam_course_mst ";
        $query .= "                                          WHERE entexamyear = '".$model->NextYear."' ";
        $query .= "                                        ) AND ";
        $query .= "     (cmp_coursecd,cmp_majorcd,cmp_examcoursecd) IN (SELECT coursecd,majorcd,examcoursecd ";
        $query .= "                                                       FROM entexam_course_mst ";
        $query .= "                                                      WHERE entexamyear = '".$model->NextYear."' ";
        $query .= "                                                    ) AND ";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM entexam_judgecomp_mst W1";
        $query .= "                   WHERE W1.entexamyear      = '".$model->NextYear."' ";
        $query .= "                     AND W1.coursecd         = T1.coursecd ";
        $query .= "                     AND W1.majorcd          = T1.majorcd ";
        $query .= "                     AND W1.examcoursecd     = T1.examcoursecd ";
        $query .= "                     AND W1.shdiv            = T1.shdiv ";
        $query .= "                     AND W1.judgement        = T1.judgement ";
        $query .= "                     AND W1.cmp_coursecd     = T1.cmp_coursecd ";
        $query .= "                     AND W1.cmp_majorcd      = T1.cmp_majorcd ";
        $query .= "                     AND W1.cmp_examcoursecd = T1.cmp_examcoursecd ";
        $query .= "                 )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO entexam_judgecomp_mst";
            $query .= "     ( entexamyear,";
            $query .= "       coursecd,";
            $query .= "       majorcd,";
            $query .= "       examcoursecd,";
            $query .= "       shdiv,";
            $query .= "       judgement,";
            $query .= "       cmp_coursecd,";
            $query .= "       cmp_majorcd,";
            $query .= "       cmp_examcoursecd,";
            $query .= "       s_judgement,";
            $query .= "       h_judgement,";
            $query .= "       registercd,";
            $query .= "       updated )";
            $query .= "     SELECT '$model->NextYear',";
            $query .= "             coursecd,";
            $query .= "             majorcd,";
            $query .= "             examcoursecd,";
            $query .= "             shdiv,";
            $query .= "             judgement,";
            $query .= "             cmp_coursecd,";
            $query .= "             cmp_majorcd,";
            $query .= "             cmp_examcoursecd,";
            $query .= "             s_judgement,";
            $query .= "             h_judgement,";
            $query .= "             '".STAFFCD."', ";
            $query .= "             sysdate() ";
            $query .= "       FROM entexam_judgecomp_mst T1 ";
            $query .= "      WHERE entexamyear = '".$model->year."' ";
            $query .= "        AND (coursecd,majorcd,examcoursecd) IN (SELECT coursecd,majorcd,examcoursecd";
            $query .= "                                                  FROM entexam_course_mst";
            $query .= "                                                 WHERE entexamyear = '".$model->NextYear."' ";
            $query .= "                                               ) ";
            $query .= "        AND (cmp_coursecd,cmp_majorcd,cmp_examcoursecd) IN (SELECT coursecd,majorcd,examcoursecd";
            $query .= "                                                              FROM entexam_course_mst";
            $query .= "                                                             WHERE entexamyear = '".$model->NextYear."' ";
            $query .= "                                                           ) ";
            $query .= "        AND (shdiv) IN (SELECT namecd2 ";
            $query .= "                          FROM v_name_mst ";
            $query .= "                         WHERE year = '".$model->NextYear."' ";
            $query .= "                           AND namecd1 = 'L006' ";
            $query .= "                       ) ";
            $query .= "        AND (judgement) IN (SELECT namecd2 ";
            $query .= "                              FROM v_name_mst";
            $query .= "                             WHERE year = '".$model->NextYear."' ";
            $query .= "                               AND namecd1 = 'L002' ";
            $query .= "                           ) ";
            $query .= "        AND NOT EXISTS (SELECT 'X'";
            $query .= "                          FROM entexam_judgecomp_mst W1";
            $query .= "                         WHERE W1.entexamyear        = '".$model->NextYear."' ";
            $query .= "                           AND W1.coursecd           = T1.coursecd ";
            $query .= "                           AND W1.majorcd            = T1.majorcd ";
            $query .= "                           AND W1.examcoursecd       = T1.examcoursecd ";
            $query .= "                           AND W1.shdiv              = T1.shdiv ";
            $query .= "                           AND W1.judgement          = T1.judgement ";
            $query .= "                           AND W1.cmp_coursecd       = T1.cmp_coursecd ";
            $query .= "                           AND W1.cmp_majorcd        = T1.cmp_majorcd ";
            $query .= "                           AND W1.cmp_examcoursecd   = T1.cmp_examcoursecd ";
            $query .= "                        )";

            $db->query($query);
            $db->commit();
        }
        Query::dbCheckIn($db);
        return $flg;
    }

}
?>
