<?php

require_once('for_php7.php');

class knjz010jQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT entexamyear ";
        $query .= "  FROM entexam_course_mst ";
        $query .= " ORDER BY entexamyear DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->year. "'";
        return $query;
    }

    //データ取得
    function selectQuery($year)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.*,";
        $query .= "      T3.NAME1,";
        $query .= "      T2.COURSENAME,";
        $query .= "      T2.MAJORNAME";
        $query .= "  FROM";
        $query .= "      ENTEXAM_COURSE_MST T1 ";
        $query .= "      LEFT OUTER JOIN V_COURSE_MAJOR_MST T2 ";
        $query .= "                   ON T1.ENTEXAMYEAR = T2.YEAR AND T1.COURSECD = T2.COURSECD AND T1.MAJORCD = T2.MAJORCD";
        $query .= "      LEFT OUTER JOIN V_NAME_MST T3 ";
        $query .= "                   ON T1.ENTEXAMYEAR = T3.YEAR AND T1.TESTDIV = T3.NAMECD2 AND T3.NAMECD1 = 'L004'";
        $query .= "  WHERE";
        $query .= "      T1.ENTEXAMYEAR = '".$year."'";
        $query .= "  ORDER BY";
        $query .= "      T1.TESTDIV,";
        $query .= "      T1.COURSECD,";
        $query .= "      T1.MAJORCD,";
        $query .= "      T1.EXAMCOURSECD";
        return $query;
    }

    //入試区分
    function selectTestdiv($year)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.NAMECD2,";
        $query .= "      T1.NAME1";
        $query .= "  FROM";
        $query .= "      V_NAME_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.YEAR = '".$year. "' AND ";
        $query .= "      T1.NAMECD1 = 'L004'";
        $query .= "  ORDER BY";
        $query .= "      T1.NAMECD2 ";
        return $query;
    }

    //課程学科
    function selectTotalcd($year)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.COURSECD,";
        $query .= "      T1.MAJORCD,";
        $query .= "      T1.MAJORNAME,";
        $query .= "      T1.COURSENAME";
        $query .= "  FROM";
        $query .= "      V_COURSE_MAJOR_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.YEAR = '".$year. "'";
        $query .= "  ORDER BY";
        $query .= "      T1.COURSECD,";
        $query .= "      T1.MAJORCD";
        return $query;
    }

    //１レコード取得
    function getRow($year,$examcoursecd,$testdiv,$coursecd,$majorcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' AND";
        $query .= "     TESTDIV      = '".$testdiv."' AND ";
        $query .= "     COURSECD     = '".$coursecd."' AND ";
        $query .= "     MAJORCD      = '".$majorcd."' AND ";
        $query .= "     EXAMCOURSECD = '".$examcoursecd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = "1";
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_COURSE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["APPLICANTDIV"][TEXT]     = "1";
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE entexamyear   = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND testdiv       = '".$fields["TESTDIV"]."'";
        $where .= "   AND coursecd      = '".$fields["COURSECD"]."'";
        $where .= "   AND majorcd       = '".$fields["MAJORCD"]."'";
        $where .= "   AND examcoursecd  = '".$fields["EXAMCOURSECD"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_COURSE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM entexam_course_mst ";
        $query .= "       WHERE entexamyear  = '".$fields["ENTEXAMYEAR"] ."'";
        $query .= "         AND testdiv      = '".$fields["TESTDIV"]."'";
        $query .= "         AND coursecd     = '".$fields["COURSECD"]."'";
        $query .= "         AND majorcd      = '".$fields["MAJORCD"]."'";
        $query .= "         AND examcoursecd = '".$fields["EXAMCOURSECD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }


    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $flg    = false;
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      ENTEXAM_COURSE_MST T1";
        $query .= "  WHERE";
        $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "      TESTDIV IN ";
        $query .= "      (SELECT NAMECD2";
        $query .= "         FROM V_NAME_MST";
        $query .= "         WHERE YEAR    = '".$model->NextYear."' AND";
        $query .= "               NAMECD1 = 'L004'";
        $query .= "      )  AND";
        $query .= "      (COURSECD,MAJORCD) IN ";
        $query .= "      (SELECT COURSECD,MAJORCD";
        $query .= "         FROM V_MAJOR_MST";
        $query .= "         WHERE YEAR = '".$model->NextYear."'";
        $query .= "      )  AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM ENTEXAM_COURSE_MST W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
        $query .= "                         W1.TESTDIV      = T1.TESTDIV AND";
        $query .= "                         W1.COURSECD     = T1.COURSECD AND";
        $query .= "                         W1.MAJORCD      = T1.MAJORCD AND";
        $query .= "                         W1.EXAMCOURSECD = T1.EXAMCOURSECD";
        $query .= "                 )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO ENTEXAM_COURSE_MST";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     APPLICANTDIV,";
            $query .= "     TESTDIV,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     EXAMCOURSE_NAME,";
            $query .= "     EXAMCOURSE_MARK,";
            $query .= "     CAPACITY,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "      '$model->NextYear',";
            $query .= "      APPLICANTDIV,";
            $query .= "      TESTDIV,";
            $query .= "      COURSECD,";
            $query .= "      MAJORCD,";
            $query .= "      EXAMCOURSECD,";
            $query .= "      EXAMCOURSE_NAME,";
            $query .= "      EXAMCOURSE_MARK,";
            $query .= "      CAPACITY,";
            $query .= "      '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      ENTEXAM_COURSE_MST T1";
            $query .= "  WHERE";
            $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
            $query .= "      TESTDIV IN ";
            $query .= "      (SELECT NAMECD2";
            $query .= "         FROM V_NAME_MST";
            $query .= "         WHERE YEAR    = '".$model->NextYear."' AND";
            $query .= "               NAMECD1 = 'L004'";
            $query .= "      )  AND";
            $query .= "      (COURSECD,MAJORCD) IN ";
            $query .= "      (SELECT COURSECD,MAJORCD";
            $query .= "         FROM V_MAJOR_MST";
            $query .= "         WHERE YEAR = '".$model->NextYear."'";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM ENTEXAM_COURSE_MST W1";
            $query .= "                   WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
            $query .= "                         W1.TESTDIV      = T1.TESTDIV AND";
            $query .= "                         W1.COURSECD     = T1.COURSECD AND";
            $query .= "                         W1.MAJORCD      = T1.MAJORCD AND";
            $query .= "                         W1.EXAMCOURSECD = T1.EXAMCOURSECD";
            $query .= "                 )";

            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);

        return $flg;
     }

}
?>
