<?php

require_once('for_php7.php');

class knjz041_course_reflectionQuery extends Query {

    //課程一覧取得
    function selectQuery($model, $coursecd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        if ($coursecd) {
            $query .= " WHERE ";
            $query .= "     COURSECD = '".$coursecd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     COURSECD ";

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

        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {

                //学校課程コードチェック
                $query = knjz041_course_reflectionQuery::selectQuery($model, $array[$i]);
                $school_course = $db->getOne($query);

                if (!$school_course) {
                    //教育委員会の課程マスタを参照
                    $query = knjz041_course_reflectionQuery::selectQuery($model, $array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["COURSECD"][TEXT]     = $array[$i];
                    $data["COURSENAME"][TEXT]   = $edboard["COURSENAME"];
                    $data["COURSEABBV"][TEXT]   = $edboard["COURSEABBV"];
                    $data["COURSEENG"][TEXT]    = $edboard["COURSEENG"];
                    $data["S_PERIODCD"][TEXT]   = $edboard["S_PERIODCD"];
                    $data["E_PERIODCD"][TEXT]   = $edboard["E_PERIODCD"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $db->query(Query::insertSQL($data, "COURSE_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz041_course_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["COURSECD"][TEXT]            = $array[$i];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_COURSE_DAT");
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
