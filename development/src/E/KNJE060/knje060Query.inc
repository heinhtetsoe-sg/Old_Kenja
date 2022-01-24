<?php

require_once('for_php7.php');

class knje060query extends Query {
    function selectQuery($model)
    {
        $query = "SELECT MAX(T1.SUMDATE) SUMDATE1,";
        $query .= "       MAX(T2.SUMDATE) SUMDATE2 ";
        $query .= "  FROM SCHREG_REGD_DAT  T3 LEFT OUTER JOIN ";
        $query .= "       ATTEND_SEMES_DAT T1 ON T1.COPYCD   = '0' AND ";
        $query .= "                              T1.YEAR     = T3.YEAR AND ";
        $query .= "                              T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                              T1.SEMESTER = T3.SEMESTER ";
        $query .= "       LEFT OUTER JOIN ";
        $query .= "       SCHREG_ATTENDREC_DAT T2 ON T2.SCHOOLCD = '0' AND ";
        $query .= "                                  T2.YEAR     = T3.YEAR AND ";
        $query .= "                                  T2.SCHREGNO = T3.SCHREGNO  ";
        $query .= " WHERE T3.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "   AND T3.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "   AND T3.ANNUAL    = '" .$model->annual ."' ";

        return $query;
    }
    //学年（年次）コンボボックス用
    function selectQueryAnnual($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "   MIN(T1.YEAR) AS YEAR, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   SCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "   T2.YEAR = '" .$model->year ."' AND ";
        $query .= "   T2.SCHOOLDIV = '0' ";  //学年制
        $query .= " GROUP BY ";
        $query .= "   T1.ANNUAL ";
        $query .= " UNION    ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.YEAR AS YEAR, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   SCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "   T2.YEAR = '" .$model->year ."' AND ";
        $query .= "   T2.SCHOOLDIV = '1' ";  //単位制
        $query .= " ORDER BY ";
        $query .= "   1, ";
        $query .= "   2 ";

        return $query;
    }

    /*
         @(f)

         機能      : 学籍学習記録データ作成（自動作成の場合）

    */
    function addRecord_Schreg_Studyrec_Dat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //----------------------------------------------------------------------------------
        //学籍学習記録データの削除
        $query = " DELETE FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        //生成範囲（処理年度のみ）
        if ($model->field["RANGE"] == 1){
            $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        }else{
            $query .= "   T1.YEAR <= '" .CTRL_YEAR ."' AND ";
        }
        $query .= "   T1.SCHOOLCD = '0' AND ";
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W ";
        $query .= "           WHERE ";
        $query .= "             W.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "             W.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W.ANNUAL = '" .$model->annual  ."' AND ";
        $query .= "             W.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";

        $result = $db->query($query);

        //学籍学習記録データの作成
        $query = " INSERT INTO SCHREG_STUDYREC_DAT( ";
        $query .= " SCHOOLCD, ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " ANNUAL, ";
//        $query .= " COURSE_PK, ";
        $query .= " CLASSCD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " CLASSNAME, ";
        $query .= " CLASSABBV, ";
        $query .= " CLASSNAME_ENG, ";
        $query .= " CLASSABBV_ENG, ";
        $query .= " SUBCLASSES, ";
        $query .= " SUBCLASSNAME, ";
        $query .= " SUBCLASSABBV, ";
        $query .= " SUBCLASSNAME_ENG, ";
        $query .= " SUBCLASSABBV_ENG, ";
        $query .= " VALUATION, ";
        $query .= " GET_CREDIT, ";
        $query .= " ADD_CREDIT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "     '0', ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
//        $query .= "     T1.COURSE_PK, ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T2.GRADINGCLASSCD, ";
        $query .= "     T3.CLASSNAME, ";
        $query .= "     T3.CLASSABBV, ";
        $query .= "     T3.CLASSNAME_ENG, ";
        $query .= "     T3.CLASSABBV_ENG, ";
        $query .= "     T3.SUBCLASSES, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T4.SUBCLASSABBV, ";
        $query .= "     T4.SUBCLASSNAME_ENG, ";
        $query .= "     T4.SUBCLASSABBV_ENG, ";
        if ($model->field["REPLACE"]){
            $query .= "     CASE T2.VALUATION WHEN 1 THEN 2 ELSE T2.VALUATION END AS VALUATION, ";
        }else{
            $query .= "     T2.VALUATION, ";
        }
        $query .= "     T2.GET_CREDIT, ";
        $query .= "     T2.ADD_CREDIT, ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= "   FROM ";
        $query .= "     ( ";
        $query .= " SELECT DISTINCT ";
//        $query .= "   T1.COURSE_PK, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        //生成範囲（処理年度のみ）
        if ($model->field["RANGE"] == 1){
            $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        }else{
            $query .= "   T1.YEAR <= '" .CTRL_YEAR ."' AND ";
        }
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.ANNUAL = '" .$model->annual ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";
        $query .= "     ) T1, ";
        $query .= "     RECORDGRADE_DAT T2, ";
        $query .= "     CLASS_MST T3, ";
        $query .= "     SUBCLASS_MST T4 ";
        $query .= "   WHERE ";
        $query .= "     T2.COPYCD = '0' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T2.TAKESEMES = (SELECT ";
        $query .= "                       MAX(TAKESEMES) ";
        $query .= "                     FROM ";
        $query .= "                       RECORDGRADE_DAT ";
        $query .= "                     WHERE ";
        $query .= "                       COPYCD = '0' AND ";
        $query .= "                       YEAR   = T2.YEAR AND ";
        $query .= "                       GRADINGCLASSCD   = T2.GRADINGCLASSCD AND ";
        $query .= "                       SCHREGNO   = T2.SCHREGNO ";
        $query .= "                     ) AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.ANNUAL = T2.ANNUAL AND ";
        $query .= "     SUBSTR(T2.GRADINGCLASSCD,1,2) = T3.CLASSCD AND ";
        $query .= "     T2.GRADINGCLASSCD = T4.SUBCLASSCD ";
        $query .= "   )  ";

        $result = $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
    /*
        @(f)

        機能      : 学籍出欠記録データ作成処理

    */
    function addRecord_Schreg_Attendrec_Dat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //学籍出欠記録データ削除
        $query = " DELETE FROM ";
        $query .= "   SCHREG_ATTENDREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        //生成範囲（処理年度のみ）
        if ($model->field["RANGE"] == 1){
            $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        }else{
            $query .= "   T1.YEAR <= '" .CTRL_YEAR ."' AND ";
        }
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W ";
        $query .= "           WHERE ";
        $query .= "             W.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "             W.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W.ANNUAL = '" .$model->annual  ."' AND ";
        $query .= "             W.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";

        $result = $db->query($query);

        //学籍出欠記録データ作成
        $query = " INSERT INTO SCHREG_ATTENDREC_DAT( ";
        $query .= " SCHOOLCD, ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " ANNUAL, ";
        $query .= " SUMDATE, ";
        $query .= " CLASSDAYS, ";
        $query .= " OFFDAYS, ";
        $query .= " ABSENT, ";
        $query .= " SUSPEND, ";
        $query .= " MOURNING, ";
        $query .= " ABROAD, ";
        $query .= " SICK, ";
        $query .= " REQUIREPRESENT, ";
        $query .= " ACCIDENTNOTICE, ";
        $query .= " NOACCIDENTNOTICE, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "     '0', ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     MAX(T2.SUMDATE), ";                         //集計日付
        $query .= "     SUM(COALESCE(T2.CLASSDAYS, 0)) AS CLASSDAYS, ";
        $query .= "     0, ";
        $query .= "     SUM(COALESCE(T2.ABSENT, 0)) AS ABSENT, ";
        $query .= "     SUM(COALESCE(T2.SUSPEND, 0)) AS SUSPEND, ";
        $query .= "     SUM(COALESCE(T2.MOURNING, 0)) AS MOURNING, ";
        $query .= "     0, ";
        $query .= "     SUM(COALESCE(T2.SICK, 0)) AS SICK, ";
        $query .= "     0, ";
        $query .= "     SUM(COALESCE(T2.ACCIDENTNOTICE, 0)) AS ACCIDENTNOTICE, ";
        $query .= "     SUM(COALESCE(T2.NOACCIDENTNOTICE, 0)) AS NOACCIDENTNOTICE, ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= "   FROM ";
        $query .= "     ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        //生成範囲（処理年度のみ）
        if ($model->field["RANGE"] == 1){
            $query .= "   T1.YEAR = '" .CTRL_YEAR ."' AND ";
        }else{
            $query .= "   T1.YEAR <= '" .CTRL_YEAR ."' AND ";
        }
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.ANNUAL = '" .$model->annual ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";
        $query .= "     ) T1 LEFT OUTER JOIN ATTEND_SEMES_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR = T2.YEAR ";
//        $query .= "   WHERE ";
//        $query .= "     T2.COPYCD = '0' ";
        $query .= "   GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL ";
        $query .= "   ) ";

        $result = $db->query($query);

        //学籍出欠記録データ作成(休学)
        $query = " UPDATE ";
        $query .= "   SCHREG_ATTENDREC_DAT T1 ";
        $query .= " SET ";
        $query .= "   (OFFDAYS) = (SELECT ";
        $query .= "                 COUNT(DISTINCT ST2.EXECUTEDATE) AS OFFDAYS ";
        $query .= "               FROM ";
        $query .= "                 CHAIR_STD_DAT ST1, ";
        $query .= "                 SCH_CHR_DAT   ST2, ";
        $query .= "                 (SELECT ";
        $query .= "                   SCHREGNO, ";
        $query .= "                   TRANSFER_SDATE, ";
        $query .= "                   TRANSFER_EDATE ";
        $query .= "                 FROM ";
        $query .= "                   SCHREG_TRANSFER_DAT ";
        $query .= "                 WHERE ";
        $query .= "                   FISCALYEAR(TRANSFER_SDATE) = '" .CTRL_YEAR ."' AND ";
        $query .= "                   TRANSFERCD = '6' AND ";
        $query .= "                   LAST_DAY(DATE('".$model->data["SUMDATE1"]."')) BETWEEN TRANSFER_SDATE AND ";
        $query .= "                   TRANSFER_EDATE ";
        $query .= "                 ) ST3 ";
        $query .= "               WHERE ";
        $query .= "                 ST1.YEAR     = T1.YEAR AND ";
        $query .= "                 ST1.YEAR     = ST2.YEAR AND ";
        $query .= "                 ST1.YEAR     = ST2.YEAR AND ";
        $query .= "                 ST1.CHAIRCD  = ST2.CHAIRCD AND ";
        $query .= "                 ST1.SCHREGNO = ST3.SCHREGNO AND ";
        $query .= "                 ST1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ST2.EXECUTEDATE BETWEEN ST3.TRANSFER_SDATE AND ";
        $query .= "                 ST3.TRANSFER_EDATE ";
        $query .= "               ) ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '0' AND ";
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.ANNUAL = '" .$model->annual ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";

        $result = $db->query($query);

        //学籍出欠記録データ作成(留学)
        $query = " UPDATE ";
        $query .= "   SCHREG_ATTENDREC_DAT T1 ";
        $query .= " SET ";
        $query .= "   (ABROAD) = (SELECT ";
        $query .= "                 COALESCE(SUM(ST1.ABROAD_CLASSDAYS),0) ABROAD ";
        $query .= "               FROM ";
        $query .= "                 SCHREG_TRANSFER_DAT ST1 ";
        $query .= "               WHERE ";
        $query .= "                 FISCALYEAR(ST1.TRANSFER_SDATE) = T1.YEAR AND ";
        $query .= "                 ST1.SCHREGNO   = T1.SCHREGNO AND ";
        $query .= "                 ST1.TRANSFERCD = '2' ";
        $query .= "               ) ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '0' AND ";
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.ANNUAL = '" .$model->annual ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";

        $result = $db->query($query);
        
        //要出席日数、出席日数の算出
        $query = " UPDATE ";
        $query .= "   SCHREG_ATTENDREC_DAT T1 ";
        $query .= " SET ";
        $query .= "   REQUIREPRESENT = CLASSDAYS - OFFDAYS - (SUSPEND + MOURNING + ABROAD), ";
        $query .= "   PRESENT = CLASSDAYS - OFFDAYS - (SUSPEND + MOURNING+ABROAD) - (SICK + ACCIDENTNOTICE + NOACCIDENTNOTICE), ";
        $query .= "   UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '0' AND ";
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             SCHREG_REGD_DAT W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.ANNUAL = '" .$model->annual ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";

        $result = $db->query($query);
        
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //学籍学習記録データの削除
    function deleteQuerySchreg_StudyRec_Dat($data)
    {
        $query .= " DELETE FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD  = '" .$data["SCHOOLCD"] ."' AND ";
        $query .= "   YEAR      = '" .$data["YEAR"] ."' AND ";
        $query .= "   SCHREGNO  = '" .sprintf("%08d",$data["SCHREGNO"]) ."' AND ";
        $query .= "   ANNUAL    = '" .sprintf("%02d",$data["ANNUAL"]) ."' AND ";
        $query .= "   CLASSCD   = '" .sprintf("%02d",$data["CLASSCD"]) ."' AND ";
        $query .= "   SUBCLASSCD   = '" .sprintf("%06d",$data["SUBCLASSCD"]) ."' ";

        return $query;
    }

    //科目数の取得
    function getSubclasses($data)
    {
        $query .= " SELECT SUBCLASSES ";
        $query .= " FROM CLASS_MST ";
        $query .= " WHERE ";
        $query .= " CLASSCD   = '" .sprintf("%02d",$data["CLASSCD"]) ."' ";

        return $query;
    }

    //学籍学習記録データの作成（ＣＳＶデータより読込）
    function addQuerySchreg_StudyRec_Dat($data)
    {

        $datas = array();
        $datas["SCHOOLCD"][TEXT]         = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d",$data["SCHREGNO"]);
        $datas["ANNUAL"][TEXT]           = sprintf("%02d",$data["ANNUAL"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d",$data["CLASSCD"]);
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d",$data["SUBCLASSCD"]);
        $datas["CLASSNAME"][TEXT]        = $data["CLASSNAME"];
        $datas["CLASSABBV"][TEXT]        = $data["CLASSABBV"];
        $datas["CLASSNAME_ENG"][TEXT]    = $data["CLASSNAME_ENG"];
        $datas["CLASSABBV_ENG"][TEXT]    = $data["CLASSABBV_ENG"];
        $datas["SUBCLASSES"][NUMBER]       = ((is_numeric($data["SUBCLASSES"]))? $data["SUBCLASSES"] : "NULL");
        $datas["SUBCLASSNAME"][TEXT]     = $data["SUBCLASSNAME"];
        $datas["SUBCLASSABBV"][TEXT]     = $data["SUBCLASSABBV"];
        $datas["SUBCLASSNAME_ENG"][TEXT] = $data["SUBCLASSNAME_ENG"];
        $datas["SUBCLASSABBV_ENG"][TEXT] = $data["SUBCLASSABBV_ENG"];
        $datas["VALUATION"][NUMBER]      = ((is_numeric($data["VALUATION"]))? $data["VALUATION"] : "NULL");
        $datas["GET_CREDIT"][NUMBER]     = ((is_numeric($data["GET_CREDIT"]))? $data["GET_CREDIT"] : "NULL");
        $datas["ADD_CREDIT"][NUMBER]     = ((is_numeric($data["ADD_CREDIT"]))? $data["ADD_CREDIT"] : "NULL");
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($datas, "SCHREG_STUDYREC_DAT");
        
        return $query;
    }

    function deleteStudyclassremark($data){

        $query  = " DELETE FROM STUDYCLASSREMARK_DAT ";
        $query .= " WHERE YEAR = '".$data["YEAR"]."' AND ";
        $query .= " SCHREGNO = '".sprintf("%08d",$data["SCHREGNO"])."' AND ";
        $query .= " CLASSCD = '".sprintf("%02d",$data["CLASSCD"])."' AND ";
        $query .= " SUBCLASSCD  = '".sprintf("%06d",$data["SUBCLASSCD"])."' ";
        return $query;
    }

    function insertStudyclassremark($data){

        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d",$data["SCHREGNO"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d",$data["CLASSCD"]);
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d",$data["SUBCLASSCD"]);
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($datas, "STUDYCLASSREMARK_DAT");
        return $query;
    }
}
?>
