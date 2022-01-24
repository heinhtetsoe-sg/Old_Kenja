<?php

require_once('for_php7.php');

class knjz030hQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT entexamyear ";
        $query .= "  FROM entexam_wishdiv_mst ";
        $query .= " ORDER BY entexamyear DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM entexam_wishdiv_mst ";
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->examyear. "'";
        return $query;
    }

    //左画面リスト
    function Listdata($examyear)
    {
        $query  = " SELECT ";
        $query .= "     T1.applicantdiv, ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name, ";
        $query .= "     T3.name1 ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst T1 ";
        $query .= " LEFT OUTER JOIN v_name_mst T3 ON T1.entexamyear=T3.year and T1.applicantdiv=T3.namecd2 and T3.namecd1='L003', ";
        $query .= "     (SELECT applicantdiv,coursecd,majorcd,examcoursecd,examcourse_name ";
        $query .= "        FROM entexam_course_mst ";
        $query .= "       WHERE entexamyear = '".$examyear."') T2 ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear  = '".$examyear."' AND ";
        $query .= "     T1.applicantdiv = T2.applicantdiv AND ";
        $query .= "     T1.coursecd     = T2.coursecd AND ";
        $query .= "     T1.majorcd      = T2.majorcd AND ";
        $query .= "     T1.examcoursecd = T2.examcoursecd AND ";
        $query .= "     T1.wishno = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.applicantdiv, ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno ";
        return $query;
    }

    //入試制度
    function selectApplicantdiv($examyear)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.NAMECD2,";
        $query .= "      T1.NAME1";
        $query .= "  FROM";
        $query .= "      V_NAME_MST T1";
        $query .= "  WHERE";
        $query .= "      T1.YEAR = '".$examyear. "' AND ";
        $query .= "      T1.NAMECD1 = 'L003'";
        $query .= "  ORDER BY";
        $query .= "      T1.NAMECD2 ";
        return $query;
    }

    //右画面選択コース一覧
    function getChCourse($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name, ";
        $query .= "     T2.examcourse_mark ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst T1  ";
        $query .= " LEFT OUTER JOIN (SELECT * FROM entexam_course_mst WHERE entexamyear = '".$model->examyear."') T2 ";
        $query .= "              ON T2.applicantdiv = T1.applicantdiv ";
        $query .= "             AND T2.coursecd     = T1.coursecd ";
        $query .= "             AND T2.majorcd      = T1.majorcd ";
        $query .= "             AND T2.examcoursecd = T1.examcoursecd ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear  = '".$model->examyear."' AND ";
        $query .= "     T1.applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     T1.desirediv    = '".$model->desirediv."' AND ";
        $query .= "     T1.wishno       = '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name, ";
        $query .= "     T2.examcourse_mark ";
        $query .= " ORDER BY ";
        $query .= "     T1.wishno ";
        return $query;
    }

    //右画面コース一覧
    function getCourse($model)
    {
        $query  = " SELECT ";
        $query .= "     coursecd, ";
        $query .= "     majorcd, ";
        $query .= "     examcoursecd, ";
        $query .= "     examcourse_name, ";
        $query .= "     examcourse_mark ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->examyear."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     (applicantdiv,coursecd,majorcd,examcoursecd) NOT IN(SELECT applicantdiv,coursecd,majorcd,examcoursecd ";
        $query .= "                                              FROM entexam_wishdiv_mst ";
        $query .= "                                             WHERE entexamyear   = '".$model->examyear."' ";
        $query .= "                                               AND applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "                                               AND desirediv     = '".$model->desirediv."' ";
        $query .= "                                               AND wishno        = '1' ) ";
        $query .= " GROUP BY ";
        $query .= "     coursecd, ";
        $query .= "     majorcd, ";
        $query .= "     examcoursecd, ";
        $query .= "     examcourse_name , ";
        $query .= "     examcourse_mark ";
        return $query;
    }

    //次年度作成
    function &getCopyQuery($model)
    {
        $flg    = false;
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "";
        $query .= "  SELECT";
        $query .= "      *";
        $query .= "  FROM";
        $query .= "      entexam_wishdiv_mst T1";
        $query .= "  WHERE";
        $query .= "      ENTEXAMYEAR = '".$model->examyear."' AND";
        $query .= "      (COURSECD,MAJORCD) IN ";
        $query .= "      (SELECT COURSECD,MAJORCD";
        $query .= "         FROM V_MAJOR_MST";
        $query .= "        WHERE YEAR = '".($model->examyear+1)."'";
        $query .= "      )  AND";
        $query .= "      NOT EXISTS (SELECT 'X'";
        $query .= "                    FROM entexam_wishdiv_mst W1";
        $query .= "                   WHERE W1.ENTEXAMYEAR  = '".($model->examyear+1)."' AND";
        $query .= "                         W1.applicantdiv = T1.applicantdiv AND";
        $query .= "                         W1.desirediv    = T1.desirediv AND";
        $query .= "                         W1.wishno       = T1.wishno";
        $query .= "                 )";
        $db->query($query);

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;

            $db->autoCommit(false);
            $query  = "";
            $query .= " INSERT INTO entexam_wishdiv_mst";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     APPLICANTDIV,";
            $query .= "     TESTDIV,";
            $query .= "     DESIREDIV,";
            $query .= "     WISHNO,";
            $query .= "     COURSECD,";
            $query .= "     MAJORCD,";
            $query .= "     EXAMCOURSECD,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= "  SELECT";
            $query .= "      '".($model->examyear + 1)."',";
            $query .= "     applicantdiv,";
            $query .= "     testdiv,";
            $query .= "     desirediv,";
            $query .= "     wishno,";
            $query .= "     coursecd,";
            $query .= "     majorcd,";
            $query .= "     examcoursecd,";
            $query .= "      '".STAFFCD."', sysdate()";
            $query .= "  FROM";
            $query .= "      entexam_wishdiv_mst T1";
            $query .= "  WHERE";
            $query .= "      ENTEXAMYEAR = '".$model->examyear."' AND";
            $query .= "      (APPLICANTDIV,COURSECD,MAJORCD,EXAMCOURSECD) IN ";
            $query .= "      (SELECT APPLICANTDIV,COURSECD,MAJORCD,EXAMCOURSECD";
            $query .= "         FROM ENTEXAM_COURSE_MST";
            $query .= "        WHERE ENTEXAMYEAR = '".($model->examyear + 1)."'";
            $query .= "      )  AND";
            $query .= "      NOT EXISTS (SELECT 'X'";
            $query .= "                    FROM entexam_wishdiv_mst W1";
            $query .= "                   WHERE W1.entexamyear  = '".($model->examyear+1)."' AND";
            $query .= "                         W1.applicantdiv = T1.applicantdiv AND";
            $query .= "                         W1.desirediv    = T1.desirediv AND";
            $query .= "                         W1.wishno       = T1.wishno";
            $query .= "                 )";

            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);
       return $flg;
    }

    function getCntDesirediv($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->examyear."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     desirediv    = '".$model->desirediv."' ";
        return $query;
    }

    function getChCntCourse($model)
    {
        $selectdata = str_replace(",", "','", $model->selectdata);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->examyear."' AND ";
        $query .= "     applicantdiv = '".$model->applicantdiv."' AND ";
        $query .= "     desirediv    = '".$model->desirediv."' AND ";
        $query .= "     (suc_coursecd,suc_majorcd,suc_coursecode) IN (SELECT coursecd,majorcd,examcoursecd ";
        $query .= "                                                     FROM entexam_wishdiv_mst ";
        $query .= "                                                    WHERE entexamyear   = '".$model->examyear."' ";
        $query .= "                                                      AND applicantdiv  = '".$model->applicantdiv."' ";
        $query .= "                                                      AND desirediv     = '".$model->desirediv."' ";
        $query .= "                                                      AND coursecd || majorcd || examcoursecd NOT IN ('".$selectdata."') ) ";
        return $query;
    }


    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->examyear."' AND";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     DESIREDIV    = '".$model->desirediv."' AND ";
        $query .= "     WISHNO       = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }



    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM entexam_wishdiv_mst ";
        $query .= "       WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "         AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "         AND desirediv   = '".$model->field["DESIREDIV"]."' ";
        $query .= "         AND wishno       = '1' ";
        $db->query($query);

        if($model->selectdata){
            $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]       = "1";
            $data["DESIREDIV"][TEXT]    = $model->field["DESIREDIV"];
            $data["WISHNO"][TEXT]       = 1;
            $data["COURSECD"][TEXT]     = substr("$model->selectdata",0,1);
            $data["MAJORCD"][TEXT]      = substr("$model->selectdata",1,3);
            $data["EXAMCOURSECD"][TEXT] = substr("$model->selectdata",4);
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, entexam_wishdiv_mst);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
