<?php

require_once('for_php7.php');

class knjd210query extends Query {
    function selectQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   MEISYOU_GET(T2.SEX,'Z002',2) AS SEX, ";
        $query .= "   T4.TAKESEMES, ";
        $query .= "   T4.GRADINGCLASSCD, ";
        $query .= "   T4.OLD_TMPVAL, ";
        $query .= "   T4.NEW_TMPVAL, ";
        $query .= "   T4.VALUATION, ";
        $query .= "   T4.GET_CREDIT, ";
        $query .= "   T4.ADD_CREDIT, ";
        $query .= "   T4.REMARK, ";
        if (is_numeric($model->control["学期数"])){
            for ($i = 0; $i < $model->control["学期数"]; $i++){
                $query .= "   T6.VAL".($i+1) .", ";
            }
        }
        $query .= "   T6.AVGVAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   SCHREG_BASE_MST T2, ";
        $query .= "   SCHREG_REGD_HDAT T3, ";
        $query .= "   (SELECT ";
        $query .= "     ST1.YEAR, ";
        $query .= "     ST1.TAKESEMES, ";
        $query .= "     ST1.GRADINGCLASSCD, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.OLD_TMPVAL, ";
        $query .= "     ST1.NEW_TMPVAL, ";
        $query .= "     ST1.VALUATION, ";
        $query .= "     ST1.GET_CREDIT, ";
        $query .= "     ST1.ADD_CREDIT, ";
        $query .= "     ST2.REMARK ";
        $query .= "   FROM ";
        $query .= "     RECORDGRADE_DAT ST1 LEFT OUTER JOIN STUDYCLASSREMARK_DAT ST2  ";
        $query .= "     ON ST1.YEAR = ST2.YEAR AND  ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO AND  ";
        $query .= "     ST2.CLASSCD = SUBSTR(ST2.SUBCLASSCD,1,2) AND  ";
        $query .= "     ST2.SUBCLASSCD = ST1.GRADINGCLASSCD ";
        $query .= "   WHERE ";
        $query .= "     ST1.COPYCD = '0' ";
        $query .= "   ) T4 LEFT OUTER JOIN (SELECT ";
        $query .= "       ST1.YEAR, ";
        $query .= "       ST1.SCHREGNO, ";
        $query .= "       ST1.GRADINGCLASSCD, ";
        if (is_numeric($model->control["学期数"])){
            for ($i = 0; $i < $model->control["学期数"]; $i++){
                $query .= "       MAX(CASE ST1.SEMESTER WHEN '" .($i+1) ."' THEN COALESCE(ST1.VALUATION, 0) END) AS VAL".($i+1) .", ";
            }
        }
/*        $query .= "       MAX(CASE ST1.SEMESTER WHEN '2' THEN COALESCE(ST1.VALUATION, 0) END) AS VAL2, ";
        $query .= "       MAX(CASE ST1.SEMESTER WHEN '3' THEN COALESCE(ST1.VALUATION, 0) END) AS VAL3, ";
*/        
        $query .= "       AVG(COALESCE(ST1.VALUATION, 0)) AS AVGVAL ";
        $query .= "     FROM ";
        $query .= "       RECORDSEMES_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "       ST1.COPYCD = '0' ";
        $query .= "     GROUP BY ";
        $query .= "       ST1.YEAR, ";
        $query .= "       ST1.SCHREGNO, ";
        $query .= "       ST1.GRADINGCLASSCD ";
        $query .= "     ) T6 ON T4.YEAR = T6.YEAR AND  ";
        $query .= "       T4.SCHREGNO = T6.SCHREGNO AND  ";
        $query .= "       T4.GRADINGCLASSCD = T6.GRADINGCLASSCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T1.YEAR = T3.YEAR AND ";
        $query .= "   T1.YEAR = T4.YEAR AND ";
        $query .= "   T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "   T1.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "   T1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "   T4.GRADINGCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   EXISTS(SELECT DISTINCT ";
        $query .= "           W1.SCHREGNO, ";
        $query .= "           W2.TAKESEMES ";
        $query .= "         FROM ";
        $query .= "           CHAIR_STD_DAT W1, ";
        $query .= "           CHAIR_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "           W1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "           W1.YEAR = W2.YEAR AND ";
        $query .= "           W1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "           W1.SEMESTER = W2.SEMESTER AND  ";
        $query .= "           W1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        if ($model->field["APPDATE"][0] != "")
            $query .= "       W1.appdate IN ('" .implode($model->field["APPDATE"],"','") ."') AND ";
        $query .= "           W1.CHAIRCD = W2.CHAIRCD AND ";
        $query .= "           W2.TAKESEMES = T4.TAKESEMES AND ";
        $query .= "           W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         ) ";

        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //番号順
                        $query .= " T3.HR_NAMEABBV,INTEGER(T1.ATTENDNO)";
                        $query .= ($order > 0)? " ASC " : " DESC ";
                        break 2;
                    case "RANK":        //席次順
                        if ($order > 0){
                            $query .= "T4.VALUATION DESC,T3.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }else{
                            $query .= "T4.VALUATION     ,T3.HR_NAMEABBV,INTEGER(T1.ATTENDNO) ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= " ORDER BY ";
            $query .= "  T2.HR_NAMEABBV,INTEGER(T2.ATTENDNO)";
        }
        return $query;
    }
    /*  新規にレコードを作成する。*/
    function addNewRecord(&$db, $model)
    {
        for ($i = 0; $i < get_count($model->field["CHAIRCD"]); $i++){
            $query = " INSERT INTO RECORDGRADE_DAT( ";
            $query .= " COPYCD, ";
            $query .= " YEAR, ";
            $query .= " TAKESEMES, ";
            $query .= " GRADINGCLASSCD, ";
            $query .= " SCHREGNO, ";
            $query .= " ANNUAL, ";
            $query .= " REGISTERCD ";
            $query .= " ) ";
            $query .= " ( SELECT DISTINCT ";
            $query .= "   '0' AS COPYCD, ";
            $query .= "   ST1.YEAR, ";
            $query .= "   ST2.TAKESEMES, ";
            $query .= "   '" .$model->field["SUBCLASSCD"] ."', ";
            $query .= "   ST1.SCHREGNO, ";
            $query .= "   ST3.ANNUAL, ";
            $query .= "   '" .STAFFCD ."' ";
            $query .= " FROM ";
            $query .= "   CHAIR_STD_DAT ST1, ";
            $query .= "   CHAIR_DAT ST2, ";
            $query .= "   SCHREG_REGD_DAT ST3 ";
            $query .= " WHERE ";
            $query .= "   ST1.YEAR = '" .$model->field["YEAR"] ."' AND ";
            $query .= "   ST1.YEAR = ST2.YEAR AND ";
            $query .= "   ST1.YEAR = ST3.YEAR AND ";
            $query .= "   ST1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
            $query .= "   ST1.SEMESTER = ST2.SEMESTER AND ";
            $query .= "   ST1.SEMESTER = ST3.SEMESTER AND ";
            $query .= "   ST1.CHAIRCD = '" .$model->field["CHAIRCD"][$i] ."' AND ";
            if ($model->field["APPDATE"][0] != "")
                $query .= "   ST1.appdate IN ('".implode($model->field["APPDATE"],"','")."') AND ";
            $query .= "   ST1.CHAIRCD = ST2.CHAIRCD AND ";
            $query .= "   ST1.SCHREGNO = ST3.SCHREGNO AND ";
            $query .= "   NOT EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "               FROM ";
            $query .= "                 RECORDGRADE_DAT W ";
            $query .= "               WHERE ";
            $query .= "                 W.COPYCD = '0' AND ";
            $query .= "                 W.YEAR = ST1.YEAR AND ";
            $query .= "                 W.TAKESEMES = ST2.TAKESEMES AND ";
            $query .= "                 W.GRADINGCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
            $query .= "                 W.SCHREGNO = ST1.SCHREGNO ";
            $query .= "               ) AND ";
            $query .= "     NOT EXISTS (SELECT ";
            $query .= "                   'X' ";
            $query .= "                 FROM ";
            $query .= "                   SCHREG_TRANSFER_DAT W2 ";
            $query .= "                 WHERE ";
            $query .= "                   W2.SCHREGNO = ST1.SCHREGNO AND ";
            $query .= "                   W2.TRANSFERCD IN ('1','2','3') AND "; //1:留学/2:休学/3出停
            $query .= "                   DATE('" .CTRL_DATE ."') BETWEEN W2.TRANSFER_SDATE AND ";
            $query .= "                   W2.TRANSFER_EDATE ";
            $query .= "                 ) AND ";
            $query .= "     EXISTS (SELECT ";    //1:卒業/2:退学/3:転学
            $query .= "                   'X' ";
            $query .= "                 FROM ";
            $query .= "                   SCHREG_BASE_MST W2 ";
            $query .= "                 WHERE ";
            $query .= "                   W2.SCHREGNO = ST1.SCHREGNO AND ";
            $query .= "                   ( ";
            $query .= "                    (W2.GRD_DIV IN ('1','2','3') AND W2.GRD_DATE > DATE('" .CTRL_DATE ."')) OR ";
            $query .= "                    W2.GRD_DATE IS NULL OR ";
            $query .= "                    W2.GRD_DIV  IS NULL ";
            $query .= "                   ) ";
            $query .= "              ) ";
            $query .= "   ) ";
            //echo $query;
            $result = $db->query($query);
        }
        //ヘッダデータ作成
        $query = " INSERT INTO RECORDGRADE_HDAT( ";
        $query .= " COPYCD, ";
        $query .= " YEAR, ";
        $query .= " CHAIRCD, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= "  (SELECT DISTINCT ";
        $query .= "     '0', ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "     T1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "     T1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "               FROM ";
        $query .= "                 RECORDGRADE_HDAT W1 ";
        $query .= "               WHERE ";
        $query .= "                 W1.COPYCD = '0' AND ";
        $query .= "                 T1.YEAR = W1.YEAR AND ";
        $query .= "                 T1.CHAIRCD = W1.CHAIRCD ";
        $query .= "               ) ";
        $query .= "   )  ";

        $result = $db->query($query);

    }
    /*  仮評価処理 */
    function estimate(&$db, $model)
    {
        $query = " UPDATE ";
        $query .= "   RECORDGRADE_DAT T ";
        $query .= " SET ";
        $query .= "   (OLD_TMPVAL, ";
        $query .= "     NEW_TMPVAL, ";
        $query .= "     VALUATION, ";
        $query .= "     GET_CREDIT, ";
        $query .= "     UPDATED) = (SELECT ";
        $query .= "       T.VALUATION, ";
        $query .= "       T3.ASSESSLEVEL, ";
        $query .= "       CASE WHEN T.VALUATION IS NULL THEN T3.ASSESSLEVEL ELSE T.VALUATION END, ";
        $query .= "       CASE WHEN T.GET_CREDIT IS NULL THEN (CASE T3.ASSESSLEVEL WHEN 1 THEN 0 ELSE COALESCE(T2.CREDITS, 0) END) ELSE T.GET_CREDIT END AS CREDITS, ";
        $query .= "       SYSDATE() ";
        $query .= "     FROM ";
        $query .= "       (SELECT ";
        $query .= "         ST1.YEAR, ";
        $query .= "         ST1.GRADINGCLASSCD, ";
        $query .= "         ST2.COURSECD, ";
        $query .= "         ST2.MAJORCD, ";
        $query .= "         ST2.GRADE, ";
        $query .= "         ST2.ANNUAL, ";
        $query .= "         ST2.COURSECODE, ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ROUND(AVG(COALESCE(ST1.VALUATION, 0)), 0) AS VALUATION ";
        $query .= "       FROM ";
        $query .= "         RECORDSEMES_DAT ST1, ";
        $query .= "         SCHREG_REGD_DAT ST2 ";
        $query .= "       WHERE ";
        $query .= "         ST1.COPYCD = '0' AND ";
        $query .= "         ST1.YEAR = ST2.YEAR AND ";
        $query .= "         ST2.SEMESTER = (SELECT ";
        $query .= "                           MAX(SEMESTER) ";
        $query .= "                         FROM ";
        $query .= "                           SCHREG_REGD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                           SCHREGNO = ST1.SCHREGNO AND ";
        $query .= "                           YEAR = ST1.YEAR ";
        $query .= "                         )  AND ";
        $query .= "         ST1.SCHREGNO = ST2.SCHREGNO AND ";
        $query .= "         ST1.GRADINGCLASSCD = '" .$model->field["SUBCLASSCD"] ."' ";
        $query .= "       GROUP BY ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST2.COURSECD, ";
        $query .= "         ST2.MAJORCD, ";
        $query .= "         ST2.GRADE, ";
        $query .= "         ST2.ANNUAL, ";
        $query .= "         ST2.COURSECODE, ";
        $query .= "         ST1.YEAR, ";
        $query .= "         ST1.GRADINGCLASSCD ";
        $query .= "       ) T1 LEFT OUTER JOIN CREDIT_MST T2 ON T1.YEAR = T2.YEAR AND  ";
        $query .= "                                             T1.COURSECD = T2.COURSECD AND  ";
        $query .= "                                             T1.MAJORCD = T2.MAJORCD AND  ";
        $query .= "                                             T1.COURSECODE = T2.COURSECODE AND  ";
        $query .= "                                             T1.GRADE = T2.GRADE AND  ";
        $query .= "                                             T2.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2) AND  ";
        $query .= "                                             T2.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."', ";
        $query .= "       ASSESS_MST T3 ";
        $query .= "     WHERE ";
        $query .= "       T1.YEAR = T.YEAR AND ";
        $query .= "       T1.SCHREGNO = T.SCHREGNO AND ";
        $query .= "       T1.GRADINGCLASSCD = T.GRADINGCLASSCD AND ";
//        $query .= "       T3.ASSESSCD = '1' AND ";
        $query .= "       T3.ASSESSCD = '3' AND ";
        $query .= "       T1.VALUATION BETWEEN T3.ASSESSLOW AND ";
        $query .= "       T3.ASSESSHIGH ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "   T.COPYCD = '0' AND ";
        $query .= "   T.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T.GRADINGCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   EXISTS(SELECT DISTINCT ";
        $query .= "           W1.SCHREGNO, ";
        $query .= "           W2.TAKESEMES ";
        $query .= "         FROM ";
        $query .= "           CHAIR_STD_DAT W1, ";
        $query .= "           CHAIR_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "           W1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "           W1.YEAR = W2.YEAR AND ";
        $query .= "           W1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "           W1.SEMESTER = W2.SEMESTER AND  ";
        $query .= "           W1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        if ($model->field["APPDATE"][0] != "")
            $query .= "           W1.APPDATE IN ('" .implode($model->field["APPDATE"],"','") ."') AND ";
        $query .= "           W1.CHAIRCD = W2.CHAIRCD AND ";
        $query .= "           W2.TAKESEMES = T.TAKESEMES AND ";
        $query .= "           W1.SCHREGNO = T.SCHREGNO ";
        $query .= "         ) ";

        $result = $db->query($query);
    }
    /* 期末データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++){
            $val      = (is_numeric($model->data["VALUATION"][$i]))? $model->data["VALUATION"][$i] : "NULL";
            $g_credit = (is_numeric($model->data["GET_CREDIT"][$i]))? $model->data["GET_CREDIT"][$i] : "NULL";
            $a_credit = (is_numeric($model->data["ADD_CREDIT"][$i]))? $model->data["ADD_CREDIT"][$i] : "NULL";
            
            //成績学年末データの更新
            $query = "UPDATE RECORDGRADE_DAT T1 SET ";
            $query .= "       T1.VALUATION     =  " .$val .",";
            $query .= "       T1.GET_CREDIT    =  " .$g_credit  .",";
            $query .= "       T1.ADD_CREDIT    =  " .$a_credit .",";
            $query .= "       T1.REGISTERCD    =  '" .STAFFCD ."',";
            $query .= "       T1.UPDATED       =  SYSDATE() ";

            $query .= " WHERE COPYCD        = '0'";
            $query .= "   AND T1.YEAR          = '" .$model->field["YEAR"] ."'";
            $query .= "   AND T1.TAKESEMES     = '" .$model->data["TAKESEMES"][$i] ."'";
            $query .= "   AND T1.GRADINGCLASSCD = '" .$model->data["GRADINGCLASSCD"][$i] ."'";
            $query .= "   AND T1.SCHREGNO      = '" .$model->data["SCHREGNO"][$i] ."'";

            $result = $db->query($query);
        
            if ($model->org["REMARK"][$i] != $model->data["REMARK"][$i]){
                $query = " DELETE FROM ";
                $query .= "   STUDYCLASSREMARK_DAT ";
                $query .= " WHERE ";
                $query .= "   YEAR      = '" .$model->field["YEAR"] ."' AND ";
                $query .= "   SCHREGNO  = '" .$model->data["SCHREGNO"][$i] ."' AND ";
                $query .= "   CLASSCD   = SUBSTR('" .$model->data["GRADINGCLASSCD"][$i] ."',1,2) AND ";
                $query .= "   SUBCLASSCD = '" .$model->data["GRADINGCLASSCD"][$i] ."' ";

                $result = $db->query($query);

                if ($model->data["REMARK"][$i] != '' && strlen($model->data["REMARK"][$i]) <= 60){
                    $query = " INSERT INTO STUDYCLASSREMARK_DAT ( ";
                    $query .= " YEAR , ";
                    $query .= " SCHREGNO , ";
                    $query .= " CLASSCD , ";
                    $query .= " SUBCLASSCD , ";
                    $query .= " REMARK , ";
                    $query .= " REGISTERCD ";
                    $query .= " ) ";
                    $query .= " VALUES ";
                    $query .= " ( ";
                    $query .= " '" .$model->field["YEAR"] ."' , ";
                    $query .= " '" .$model->data["SCHREGNO"][$i] ."' , ";
                    $query .= " SUBSTR('" .$model->data["GRADINGCLASSCD"][$i] ."',1,2), ";
                    $query .= " '" .$model->data["GRADINGCLASSCD"][$i] ."' , ";
                    $query .= " '" .Query::addquote($model->data["REMARK"][$i]) ."' , ";
                    $query .= " '" .STAFFCD ."' ";
                    $query .= " ) ";

                    $result = $db->query($query);
                }
            }
        }
        //成績期末ヘッダデータの更新
        $query = "UPDATE RECORDGRADE_HDAT SET ";
        $query .= "       AVGMOD_FLG    = " .(($model->data["AVGMOD_FLG"] == 1)? "'1'" : "NULL") .",";
        $query .= "       RELATIVED_FLG    = " .(($model->data["RELATIVED_FLG"] == 1)? "'1'" : "NULL") .",";
        $query .= "       REGISTERCD    =  '" .STAFFCD ."',";
        $query .= "       UPDATED       =  SYSDATE() ";

        $query .= " WHERE COPYCD        = '0'";
        $query .= "   AND YEAR          = '" .$model->field["YEAR"] ."'";
        $query .= "   AND CHAIRCD       IN ('" .implode($model->field["CHAIRCD"],"','") ."')";

        $result = $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
    /*  削除処理  */
    function delete($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM ";
        $query .= "   RECORDGRADE_DAT T1 ";
        $query .= "WHERE ";
        $query .= "   T1.COPYCD = '0' AND ";
        $query .= "   T1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T1.GRADINGCLASSCD || '-' || T1.SCHREGNO IN ('" .implode($model->checked, "','") ."') AND ";
        $query .= "   EXISTS(SELECT ";
        $query .= "           * ";
        $query .= "         FROM ";
        $query .= "           CHAIR_DAT W1, ";
        $query .= "           CHAIR_STD_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "           W1.YEAR = T1.YEAR AND ";
        $query .= "           W1.YEAR = W2.YEAR AND ";
        $query .= "           W1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "           W1.SEMESTER = W2.SEMESTER AND ";
        $query .= "           W1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        if ($model->field["APPDATE"][0] != "")
            $query .= "       W2.APPDATE IN ('" .implode($model->field["APPDATE"],"','") ."') AND ";
        $query .= "           W1.CHAIRCD = W2.CHAIRCD AND ";
        $query .= "           W2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "           W1.TAKESEMES = T1.TAKESEMES ";
        $query .= "         ) ";

        $result = $db->query($query);

        //備考データ削除
        $query = " DELETE FROM ";
        $query .= "   STUDYCLASSREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   CLASSCD = SUBSTR(SUBCLASSCD,1,2) AND ";
        $query .= "   SUBCLASSCD || '-' || SCHREGNO IN ('" .implode($model->checked, "','") ."') ";

        $result = $db->query($query);

        //ヘッダデータ削除
        $query = " DELETE FROM ";
        $query .= "   RECORDGRADE_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.COPYCD = '0' AND ";
        $query .= "   T1.YEAR = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "   NOT EXISTS(SELECT ";
        $query .= "               * ";
        $query .= "             FROM ";
        $query .= "               RECORDGRADE_DAT W1, ";
        $query .= "               RECORDSEMES_DAT W2, ";
        $query .= "               CHAIR_DAT       W3 ";
        $query .= "             WHERE ";
        $query .= "               W1.COPYCD = '0' AND ";
        $query .= "               W1.COPYCD = W2.COPYCD AND ";
        $query .= "               W1.YEAR = T1.YEAR AND ";
        $query .= "               W1.YEAR = W2.YEAR AND ";
        $query .= "               W1.YEAR = W3.YEAR AND ";
        $query .= "               W1.TAKESEMES = W3.TAKESEMES AND ";
        $query .= "               W2.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "               W2.SEMESTER = W3.SEMESTER AND ";
        $query .= "               W1.GRADINGCLASSCD = W2.GRADINGCLASSCD AND ";
        $query .= "               W1.SCHREGNO = W2.SCHREGNO AND ";
        $query .= "               W2.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "               W2.CHAIRCD  = T1.CHAIRCD AND ";
        $query .= "               W2.CHAIRCD  = W3.CHAIRCD ";
        $query .= "             ) ";

        $result = $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
    /*
        @(f)

         機能      ：成績項目別得点データより仮評価の平均点を求める(期末専用)

         戻り値    ：True:成功、False:失敗

         引数      ：0:学年平均, 1:クラス平均

         機能説明  ：成績項目別得点データより仮評価の平均点を求める(期末専用)

         備考      ：なし

         新規      ：
    */
    function getAverage(&$model)
    {
        $db = Query::dbCheckOut();

        $query = "   SELECT DISTINCT ";
        $query .= "     ST2.GRADE ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT ST1, ";
        $query .= "     SCHREG_REGD_DAT ST2 ";
        $query .= "   WHERE ";
        $query .= "     ST1.YEAR     =   '" .$model->field["YEAR"] ."' AND ";
        $query .= "     ST1.CHAIRCD  IN ('" .implode($model->field["CHAIRCD"], "','") ."') AND ";
        if ($model->field["APPDATE"][0] != "")
            $query .= " ST1.APPDATE IN ('" .implode($model->field["APPDATE"], "','") ."') AND ";
        $query .= "     ST2.YEAR     =   ST1.YEAR AND ";
        $query .= "     ST2.SEMESTER =   '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "     ST2.SCHREGNO =   ST1.SCHREGNO ";

        $result = $db->query($query);
        $num = $result->numRows();
        //学年が混在していない
        if ($num == 1){
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $grade = $row["GRADE"];

            $query = " SELECT ";
            $query .= "   AVG(T3.VALUATION) ";
            $query .= " FROM ";
            $query .= "   SCHREG_REGD_DAT T1, ";
            $query .= "   RECORDSEMES_DAT   T2, ";
            $query .= "   RECORDGRADE_DAT T3, ";
            $query .= "   CHAIR_DAT T4 ";
            $query .= " WHERE ";
            $query .= "   T1.YEAR        =  '" .$model->field["YEAR"] ."' AND ";
            $query .= "   T1.SEMESTER    =  '" .$model->field["SEMESTER"] ."' AND ";
            $query .= "   T1.GRADE       =  '" .$grade ."' AND ";
            $query .= "   T2.COPYCD      =  '0' AND ";
            $query .= "   T2.YEAR        =  T1.YEAR AND ";
            $query .= "   T2.SEMESTER    =  T1.SEMESTER AND ";
            $query .= "   T2.SUBCLASSCD  = '" .$model->field["SUBCLASSCD"] ."' AND ";
            $query .= "   T2.SCHREGNO    =  T1.SCHREGNO AND ";
            $query .= "   T3.YEAR = T1.YEAR AND ";
            $query .= "   T3.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "   T3.GRADINGCLASSCD = T2.GRADINGCLASSCD AND ";
            $query .= "   T3.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "   T4.YEAR = T1.YEAR AND ";
            $query .= "   T4.SEMESTER = T1.SEMESTER AND ";
            $query .= "   T4.TAKESEMES = T3.TAKESEMES AND ";
            $query .= "   T4.CHAIRCD IN ('" .implode($model->field["CHAIRCD"], "','") ."') ";

            $row = $db->getRow($query);
            if (is_array($row)){
                $model->average["GRADE"] = $row[0];
            }
        }else if ($num > 1){
            $model->average["GRADE"] = 0;
            Model::setWarning("MSG915","\\r\\n学年が混在しています。");        
            Query::dbCheckIn($db);
            return false;
        }
        Query::dbCheckIn($db);
        
        return true;
    }
    //学年を求める
    function selectGradeQuery($model)
    {
        $query = "   SELECT DISTINCT ";
        $query .= "     ST2.GRADE ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT ST1, ";
        $query .= "     SCHREG_REGD_DAT ST2 ";
        $query .= "   WHERE ";
        $query .= "     ST1.YEAR     =   '" .$model->field["YEAR"] ."' AND ";
        $query .= "     ST1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"], "','") ."') AND ";
        if ($model->field["APPDATE"][0] != "")
            $query .= " ST1.appdate IN ('" .implode($model->field["APPDATE"], "','") ."') AND ";
        $query .= "     ST2.YEAR     =   ST1.YEAR AND ";
        $query .= "     ST2.SEMESTER =   '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "     ST2.SCHREGNO =   ST1.SCHREGNO ";

        return $query;
    }
    //相対評価マスタ
    function selectAssessQuery($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "   T1.ASSESSLEVEL, ";
        $query .= "   INTEGER(T1.ASSESSLOW) AS ASSESSLOW, ";
        $query .= "   INTEGER(T1.ASSESSHIGH) AS ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "   RELATIVEASSESS_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   T1.ASSESSCD = '3' AND ";
        $query .= "   T1.GRADE = '" .$model->grade ."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.ASSESSLEVEL DESC ";

        return $query;
    }

    //相対評定マスタ.評定上限の最大値を取得
    function selectMaxAssessQuery($model)
    {
        $query = " SELECT ";
        $query .= "   MAX(ASSESSLEVEL) AS ASSESSLEVEL, ";
        $query .= "   MAX(ASSESSHIGH)  AS ASSESSHIGH, ";
        $query .= "   MIN(ASSESSLOW)   AS ASSESSLOW ";
        $query .= " FROM ";
        $query .= "   RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "   SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   ASSESSCD = '3' AND ";
        $query .= "   GRADE = '" .$model->grade ."' ";

        return $query;
    }


    //相対評価処理実行
    function execAssess($model){

        $db = Query::dbCheckOut();

        //相対評定マスタ.評定上限の最大値を求める
        $query = knjd210Query::selectMaxAssessQuery($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $assesslevel_max = $row["ASSESSLEVEL"];
        $assesshigh_max = $row["ASSESSHIGH"];
        $assesslow_min = $row["ASSESSLOW"];

        //相対評価の上限、下限を求める
        $arr_l = array();
        $arr_h = array();

        for ($i = 0; $i < $assesslevel_max; $i++){
            if($i == 0){
                $arr_l[] = (int) $model->data["ASSESSLOW"][$i];
                $arr_h[] = (int) $assesshigh_max;
            } else if($i == ($assesslevel_max-1)){
                $arr_l[] = (int) $assesslow_min;
                if ((int) $model->data["ASSESSLOW"][$i-1]-1 < $assesslow_min ){
                    $arr_h[] = (int) $assesslow_min;
                } else {
                    $arr_h[] = (int) $model->data["ASSESSLOW"][$i-1]-1;
                }
            } else {
                $arr_l[] = (int) $model->data["ASSESSLOW"][$i];
                if ((int) $model->data["ASSESSLOW"][$i-1]-1 < $assesslow_min ){
                    $arr_h[] = (int) $assesslow_min;
                } else {
                    $arr_h[] = (int) $model->data["ASSESSLOW"][$i-1]-1;
                }
            }
        }

        $arr_l[] = 0;

        $db->autoCommit(false);
        
        for ($i = 0; $i < $assesslevel_max; $i++){
            $query = " UPDATE ";
            $query .= "   RELATIVEASSESS_MST ";
            $query .= " SET ";
            $query .= "   (ASSESSLOW,ASSESSHIGH,UPDATED) = (".$arr_l[$i].",".$arr_h[$i].",SYSDATE()) ";
            $query .= " WHERE ";
            $query .= "   GRADE         = '" .$model->grade ."' AND ";
            $query .= "   SUBCLASSCD    = '" .$model->field["SUBCLASSCD"] ."' AND ";
            $query .= "   ASSESSCD      = '3' AND ";
            $query .= "   ASSESSLEVEL   = " .(($assesslevel_max)-$i);

            $result = $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
    //評価の最大値
    function selectMaxAssessLevelQuery($model)
    {
        $query = " SELECT ";
        $query .= "   ASSESSLEVELCNT ";
        $query .= " FROM ";
        $query .= "   ASSESS_HDAT ";
        $query .= " WHERE ";
        $query .= "   ASSESSCD = '3' ";

        return $query;
    }
}
?>
