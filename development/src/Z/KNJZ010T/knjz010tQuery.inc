<?php

require_once('for_php7.php');

class knjz010tQuery extends Query {

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
    function selectQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     L1.NAME1 AS APPLICANT_NAME, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     L2.MAJORLNAME, ";
        $query .= "     L2.MAJORSNAME, ";
        $query .= "     T1.CAPACITY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "                 AND L1.NAMECD1 = 'L003' ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST L2 ON L2.MAJORCD = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }

    //入試制度
    function selectApplicantdiv($year) {
        $query  = "  SELECT";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1 ";
        $query .= "  FROM";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = 'L003' ";
        $query .= "  ORDER BY ";
        $query .= "      NAMECD2 ";
        return $query;
    }

    //課程学科
    function getMajorcd($year) {
        $query  = " SELECT ";
        $query .= "     MAJORCD || ':' || MAJORLNAME || ' ' || MAJORSNAME AS LABEL, ";
        $query .= "     MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_MAJOR_MST ";

        return $query;
    }

    //１レコード取得
    function getRow($year,$applicantdiv,$majorcd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' AND";
        $query .= "     APPLICANTDIV = '".$applicantdiv."' AND ";
        $query .= "     MAJORCD      = '".$majorcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = "1";
        $data["COURSECD"][TEXT]         = "1";
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = "0000";
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_COURSE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE entexamyear   = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND applicantdiv  = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND majorcd       = '".$fields["MAJORCD"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_COURSE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM entexam_course_mst ";
        $query .= "       WHERE entexamyear  = '".$fields["ENTEXAMYEAR"] ."'";
        $query .= "         AND applicantdiv = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND majorcd      = '".$fields["MAJORCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }


    //次年度のデータを作成
    function &getCopyYearQuery($model) {
        $flg    = false;
        $db = Query::dbCheckOut();

        $query  = "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      ENTEXAM_COURSE_MST T1";
        $query .= "  WHERE";
        $query .= "      ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "      APPLICANTDIV IN ";
        $query .= "      (SELECT NAMECD2";
        $query .= "         FROM NAME_MST";
        $query .= "      )  AND";
        $query .= "      (MAJORCD) IN ";
        $query .= "      (SELECT MAJORCD";
        $query .= "         FROM V_ENTEXAM_MAJOR_MST";
        $query .= "      )  AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM ENTEXAM_COURSE_MST W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
        $query .= "                         W1.APPLICANTDIV = T1.APPLICANTDIV AND";
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
            $query .= "      APPLICANTDIV IN ";
            $query .= "      (SELECT NAMECD2";
            $query .= "         FROM NAME_MST";
            $query .= "      )  AND";
            $query .= "      (MAJORCD) IN ";
            $query .= "      (SELECT MAJORCD";
            $query .= "         FROM V_ENTEXAM_MAJOR_MST";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM ENTEXAM_COURSE_MST W1";
            $query .= "                   WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
            $query .= "                         W1.APPLICANTDIV = T1.APPLICANTDIV AND";
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
