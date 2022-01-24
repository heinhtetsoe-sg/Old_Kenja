<?php

require_once('for_php7.php');

class knjz281_job_reflectionQuery extends Query {

    //職名一覧取得
    function selectQuery($jobcd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        if ($jobcd) {
            $query .= " WHERE ";
            $query .= "     JOBCD = '".$jobcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     JOBCD ";

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

        //職名コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {

                //学校職名コードチェック
                $query = knjz281_job_reflectionQuery::selectQuery($array[$i]);
                $school_job = $db->getOne($query);

                if (!$school_job) {
                    //教育委員会の職名マスタを参照
                    $query = knjz281_job_reflectionQuery::selectQuery($array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["JOBCD"][TEXT]        = $edboard["JOBCD"];
                    $data["JOBNAME"][TEXT]      = $edboard["JOBNAME"];
                    $data["BASE_JOBNAME"][TEXT] = $edboard["BASE_JOBNAME"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $db->query(Query::insertSQL($data, "JOB_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz281_job_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["JOBCD"][TEXT]               = $edboard["JOBCD"];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_JOB_DAT");
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
