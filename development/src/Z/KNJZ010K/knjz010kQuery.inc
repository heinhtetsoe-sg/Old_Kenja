<?php

require_once('for_php7.php');

class knjz010kQuery extends Query {

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
        $query  = " SELECT DISTINCT ENTEXAMYEAR ";
        $query .= " FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR > '" .$model->year. "'";
        return $query;
    }

    //データ取得
    function selectQuery($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.COURSENAME, ";
        $query .= "     T3.MAJORNAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " ON  T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "     T1.COURSECD = T2.COURSECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_MAJOR_MST T3 ";
        $query .= " ON  T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.COURSECD = T3.COURSECD AND ";
        $query .= "     T1.MAJORCD = T3.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD ";

        return $query;
    }

    //課程学科
    function selectTotalcd($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.MAJORNAME, ";
        $query .= "     T2.COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " ON  T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.COURSECD = T2.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year. "'";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }

    //１レコード取得
    function getRow($year,$examcoursecd,$coursecd,$majorcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' AND";
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
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_ABBV"][TEXT]  = $fields["EXAMCOURSE_ABBV"];
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

#        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $fields["EXAMCOURSECD"];
        $data["EXAMCOURSE_NAME"][TEXT]  = $fields["EXAMCOURSE_NAME"];
        $data["EXAMCOURSE_ABBV"][TEXT]  = $fields["EXAMCOURSE_ABBV"];
        $data["EXAMCOURSE_MARK"][TEXT]  = $fields["EXAMCOURSE_MARK"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = " WHERE entexamyear   = '".$fields["ENTEXAMYEAR"]."'";
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
        $query .= "         AND coursecd     = '".$fields["COURSECD"]."'";
        $query .= "         AND majorcd      = '".$fields["MAJORCD"]."'";
        $query .= "         AND examcoursecd = '".$fields["EXAMCOURSECD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM ENTEXAM_COURSE_MST WHERE ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //学科マスタに次年度のデータが登録されているか確認
    function NextYearExist($model)
    {
        $flg = false;
        $db     = Query::dbCheckOut();

        $query  = " SELECT COURSECD, MAJORCD ";
        $query .= " FROM V_MAJOR_MST ";
        $query .= " WHERE YEAR = '".$model->year."'";

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query2  = " SELECT COURSECD, MAJORCD ";
            $query2 .= " FROM V_MAJOR_MST ";
            $query2 .= " WHERE YEAR = '".$model->NextYear."' AND ";
            $query2 .= "       COURSECD = '".$row["COURSECD"]."' AND";
            $query2 .= "       MAJORCD = '" .$row["MAJORCD"]."'";

            $res = $db->getRow($query2, DB_FETCHMODE_ASSOC);
            if (is_array($res)) {
                $flg = true;
                break;
            }
        }
        Query::dbCheckIn($db);
        return $flg;
    }
       
    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->NextYear."'";

        $db->query($query);

        $query  = " SELECT COURSECD, MAJORCD ";
        $query .= " FROM V_MAJOR_MST ";
        $query .= " WHERE YEAR = '".$model->NextYear."'";

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query2  = " INSERT INTO ENTEXAM_COURSE_MST(";
            $query2 .= " ENTEXAMYEAR,";
            $query2 .= " COURSECD,";
            $query2 .= " MAJORCD,";
            $query2 .= " EXAMCOURSECD,";
            $query2 .= " EXAMCOURSE_NAME,";
            $query2 .= " EXAMCOURSE_ABBV,";
            $query2 .= " EXAMCOURSE_MARK,";
            $query2 .= " CAPACITY,";
            $query2 .= " REGISTERCD,";
            $query2 .= " UPDATED)";
            $query2 .= " SELECT";
            $query2 .= " '$model->NextYear',";
            $query2 .= " COURSECD,";
            $query2 .= " MAJORCD,";
            $query2 .= " EXAMCOURSECD,";
            $query2 .= " EXAMCOURSE_NAME,";
            $query2 .= " EXAMCOURSE_ABBV,";
            $query2 .= " EXAMCOURSE_MARK,";
            $query2 .= " CAPACITY,";
            $query2 .= " '" .STAFFCD. "', sysdate()";
            $query2 .= " FROM ENTEXAM_COURSE_MST";
            $query2 .= " WHERE ";
            $query2 .= "     ENTEXAMYEAR = '".$model->year."' AND ";
            $query2 .= "     COURSECD = '".$row["COURSECD"]."' AND";
            $query2 .= "     MAJORCD = '".$row["MAJORCD"]."'";
            
            $db->query($query2);
        }
        $result->free();
        $db->commit();
        Query::dbCheckIn($db);

        return;
     }
}
?>
