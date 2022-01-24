<?php

require_once('for_php7.php');

class knjz422Query extends Query {

    //アンケート件名取得
    function getQuestionNairecd() {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD || ':' || L1.QUESTIONNAIRENAME AS LABEL, ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     INNER JOIN QUESTIONNAIRE_MST L1 ON L1.QUESTIONNAIRECD = T1.QUESTIONNAIRECD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //回答入力形式取得
    function getPattern() {
        $query  = " SELECT ";
        $query .= "     '1' || ':' || 'ラジオボタン' AS LABEL, ";
        $query .= "     '1' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' || ':' || 'チェックボックス' AS LABEL, ";
        $query .= "     '2' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3' || ':' || 'テキスト(小)' AS LABEL, ";
        $query .= "     '3' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '4' || ':' || 'テキスト(中)' AS LABEL, ";
        $query .= "     '4' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '5' || ':' || 'テキスト(大)' AS LABEL, ";
        $query .= "     '5' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //対象データ取得
    function getQuestionFormatDat($model)    
    {
        $query  = "SELECT ";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            $query .= "     L$i.QUESTION_CONTENTS AS QUESTION_CONTENTS$i, ";
            $query .= "     L$i.ANSWER_PATTERN AS ANSWER_PATTERN$i, ";
            $query .= "     L$i.ANSWER_SELECT_COUNT AS ANSWER_SELECT_COUNT$i, ";
        }
        $query .= "     T1.QUESTIONNAIRECD ";
        $query .= " FROM ";
        $query .= "     QUESTION_FORMAT_DAT T1 ";
        for ($i = 1; $i <= $model->koumoku; $i++ ) {
            $query .= "     LEFT JOIN QUESTION_FORMAT_DAT L$i ON L$i.YEAR = T1.YEAR";
            $query .= "                                      AND L$i.QUESTIONNAIRECD = T1.QUESTIONNAIRECD";
            $query .= "                                      AND L$i.QUESTIONITEM_NO = $i ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.QUESTIONNAIRECD = '".$model->field["QUESTIONNAIRECD"]."' " ;

        return $query;
    }
        
    //INSERT    
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  = " DELETE FROM ";
        $query .= "     QUESTION_FORMAT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND QUESTIONNAIRECD = '".$model->field["QUESTIONNAIRECD"]."' ";
        $db->query($query);

        for ($i = 1; $i <= $model->koumoku; $i++) {
            if ($model->field["QUESTION_CONTENTS".$i] != "" && $model->field["ANSWER_PATTERN".$i] != "") {
                $data = array();
                $data["YEAR"][TEXT]                             = CTRL_YEAR;
                $data["QUESTIONNAIRECD"][TEXT]                  = $model->field["QUESTIONNAIRECD"];
                $data["QUESTIONITEM_NO"][NUMBER]                = $i;
                $data["QUESTION_CONTENTS"][TEXT]                = $model->field["QUESTION_CONTENTS".$i];
                $data["ANSWER_PATTERN"][TEXT]                   = $model->field["ANSWER_PATTERN".$i];
                $data["ANSWER_SELECT_COUNT"][NUMBER]            = $model->field["ANSWER_SELECT_COUNT".$i];
                $data["REGISTERCD"][TEXT]                       = STAFFCD;
                $data["UPDATED"][FUNC]                          = "sysdate()";
                $query = Query::insertSQL($data, "QUESTION_FORMAT_DAT");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
    
}
?>
