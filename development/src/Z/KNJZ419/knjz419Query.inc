<?php

require_once('for_php7.php');

class knjz419Query extends Query {

    //課程年度一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD || ' ' || L1.QUESTIONNAIRENAME AS LABEL, ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     QUESTIONNAIRE_MST L1 ON L1.QUESTIONNAIRECD = T1.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.QUESTIONNAIRECD ";

        return $query;
    }

    //課程年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //課程一覧取得
    function selectCourseQuery($model) {
        $query  = " SELECT ";
        $query .= "     QUESTIONNAIRECD || ' ' || QUESTIONNAIRENAME AS LABEL, ";
        $query .= "     QUESTIONNAIRECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_MST ";
        $query .= " WHERE ";
        $query .= "     QUESTIONNAIRECD NOT IN (SELECT ";
        $query .= "                                 QUESTIONNAIRECD ";
        $query .= "                             FROM ";
        $query .= "                                 QUESTIONNAIRE_YDAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR = '{$model->year}' ";
        $query .= "                             ) ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //課程コード配列に格納
        $questionnairecdArray = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE FROM QUESTIONNAIRE_YDAT WHERE YEAR = '{$model->year}'";
        $db->query($query);

        $query = "INSERT INTO QUESTIONNAIRE_YDAT (YEAR, QUESTIONNAIRECD, REGISTERCD, UPDATED)";
        $query.= "VALUES";
        if ($questionnairecdArray[0] != "") {
            for ($i = 0; $i < get_count($questionnairecdArray); $i++) {
                $sp = ",";
                if ($i == get_count($questionnairecdArray)-1) {
                    $sp = "";
                }
                $query.= "('{$model->year}' ";                //年度
                $query.= ",'{$questionnairecdArray[$i]}' ";   //課程コード
                $query.= ",'" .STAFFCD ."' ";                 //STAFFCD
                $query.= ",sysdate())" .$sp;                //更新日付
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
