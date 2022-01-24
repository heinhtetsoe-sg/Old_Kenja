<?php

require_once('for_php7.php');

class knjz051_major_reflectionQuery extends Query {

    //学科一覧取得
    function selectQuery($course_major="") {
        if ($course_major) list($coursecd, $majorcd) = preg_split("/-/", $course_major);

        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.*, ";
        $query .= "     T2.COURSENAME ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1, ";
        $query .= "     COURSE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.COURSECD = T2.COURSECD ";
        if ($course_major) {
            $query .= " AND T1.COURSECD = '".$coursecd."' ";
            $query .= " AND T1.MAJORCD  = '".$majorcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //学科コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {

                //学校学科コードチェック
                $query = knjz051_major_reflectionQuery::selectQuery($array[$i]);
                $school_major = $db->getOne($query);

                if (!$school_major) {
                    //教育委員会の学科マスタを参照
                    $query = knjz051_major_reflectionQuery::selectQuery($array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["COURSECD"][TEXT]     = $edboard["COURSECD"];
                    $data["MAJORCD"][TEXT]      = $edboard["MAJORCD"];
                    $data["MAJORNAME"][TEXT]    = $edboard["MAJORNAME"];
                    $data["MAJORABBV"][TEXT]    = $edboard["MAJORABBV"];
                    $data["MAJORENG"][TEXT]     = $edboard["MAJORENG"];
                    $data["MAJORBANKCD"][TEXT]  = $edboard["MAJORBANKCD"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $db->query(Query::insertSQL($data, "MAJOR_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz051_major_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["COURSECD"][TEXT]            = $edboard["COURSECD"];
                    $data2["MAJORCD"][TEXT]             = $edboard["MAJORCD"];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_MAJOR_DAT");
                    $db2->query($query);
                }
            }
        }
        $db->commit();
        $db2->commit();

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
