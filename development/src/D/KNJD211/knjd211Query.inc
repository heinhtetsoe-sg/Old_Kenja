<?php

// kanji=漢字
// $Id: knjd211Query.inc 56580 2017-10-22 12:35:29Z maeshiro $

class knjd211query extends Query {

    //事前処理チェック（コースグループ設定）
    function ChecktoStart($model, $flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   COURSE_GROUP_DAT ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND GRADE='".$model->grade."' ";
        //COURSE_SEQが設定されているデータがあるかどうかをチェック ---> なければエラー
        if ($flg == 1) 
            $query .= "    AND COURSE_SEQ IS NOT NULL ";
        //COURSE_SEQが１つでも設定されていないデータがあるかどうかをチェック ---> あればエラー
        if ($flg == 2) 
            $query .= "    AND COURSE_SEQ IS NULL ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //存在チェック（在籍データ）
    function ChecktoSchregno($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   SCHREG_REGD_DAT ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //処理学年
    function GetGrade()
    {
        $query  = " SELECT DISTINCT GRADE FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' ";
        return $query;
    }

    //処理種別(成績)
    function GetName($model)
    {
        $query  = " SELECT NAMECD2, NAME1 FROM V_NAME_MST  ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'Z004' ";
        if ($model->grade != "03") //３年時相対評価
            $query .= "    AND NAMECD2 NOT IN ('9001','9002') ";
        $query .= " ORDER BY NAMECD2 ";
        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // １．RECORD_RANK_DAT
        //追加処理-----RECORD_RANK_DATにデータがない生徒全員を追加(※生徒一人につき３レコード作成)
        knjd211query::InsertQuery($db, $model);
        //更新処理-----合計・平均・席次を抽出し、RECORD_RANK_DATを更新
        knjd211query::UpdateQuery($db, $model);

        // ２．RECORD_CLASS_DAT---NO001
        //追加処理-----RECORD_CLASS_DATにデータがない生徒全員を追加(※生徒一人につき９レコード作成)
        knjd211query::InsertClassQuery($db, $model);
        //更新処理-----KIN_RECORD_DATからRECORD_CLASS_DATへコピー
        knjd211query::UpdateClassQuery($db, $model);

        // ３．RECORD_CLASS_AVERAGE_DAT---NO001
        //追加処理-----RECORD_CLASS_AVERAGE_DATにデータがないクラスを追加(※１クラスにつき５４レコード作成)
        knjd211query::InsertClassAverageQuery($db, $model);
        //更新処理-----クラス合計・クラス平均・コース平均を抽出し、RECORD_CLASS_AVERAGE_DATを更新
        knjd211query::UpdateClassAverageQuery($db, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /* ----------↓---------- １．RECORD_RANK_DAT ----------↓---------- */

    /*                  */
    /* 生徒データの追加 */
    /*                  */

    function InsertQuery(&$db, $model) 
    {
        $query  = "";

        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, YEAR ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ) ";
        $query .= " ,RANK AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_RANK_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY SCHREGNO ) ";

        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '1' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  NOT EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " UNION ";
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '2' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  NOT EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " UNION ";
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '3' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  NOT EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";

        $query .= " ORDER BY SCHREGNO, RANK_DIV ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = " INSERT INTO RECORD_RANK_DAT(YEAR, SCHREGNO, RANK_DIV) ";
            $query .= " VALUES('".$row["YEAR"]."', '".$row["SCHREGNO"]."', '".$row["RANK_DIV"]."') ";

            $db->query($query);
        }
        return;
    }//InsertQuery

    /*                  */
    /* 席次データの更新 */
    /*                  */

    function UpdateQuery(&$db, $model) 
    {
        //合計・平均・席次等をクリア
        $query  = "";

        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, YEAR ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ) ";
        $query .= " ,RANK AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_RANK_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY SCHREGNO ) ";

        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '1' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " UNION ";
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '2' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " UNION ";
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '3' AS RANK_DIV ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";

        $query .= " ORDER BY SCHREGNO, RANK_DIV ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE record_rank_dat ";
            $query .= "   SET ".$model->field[$model->exam]."_total = NULL";
            $query .= "      ,".$model->field[$model->exam]."_avg   = NULL";
            $query .= "      ,".$model->field[$model->exam]."_rank  = NULL";
            $query .= "      ,registercd    = '".STAFFCD."' ";
            $query .= "      ,updated       = SYSDATE() ";
            $query .= " WHERE year          = '".CTRL_YEAR."' ";
            $query .= "   AND schregno      = '".$row["SCHREGNO"]."' ";
            $query .= "   AND rank_div      = '".$row["RANK_DIV"]."'";

            $db->query($query);
        }

        //異動対象日付---2005.10.12Add
        $date = str_replace("/","-",$model->date);

        //合計・平均・席次等を算出
        $query  = "";

        /* 在籍 */
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,W1.HR_CLASS,ATTENDNO,COURSE_SEQ,W1.YEAR ";
        $query .= "     FROM   SCHREG_REGD_DAT W1, COURSE_GROUP_DAT W2, SEMESTER_MST W3 ";//2005.10.12
        $query .= "     WHERE  W1.YEAR='".CTRL_YEAR."' AND W1.SEMESTER='".$model->semes[$model->exam]."' AND W1.GRADE='".$model->grade."' AND ";
        $query .= "            W1.YEAR=W2.YEAR AND W1.GRADE=W2.GRADE AND W1.COURSECODE=W2.COURSECODE ";
                               //2005.10.12Add----------↓----------
        $query .= "            AND W1.YEAR=W3.YEAR AND W1.SEMESTER=W3.SEMESTER ";
                               //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                               //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END) OR ";
        $query .= "                                 (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END)) ) ";
                               //留学(1)・休学(2)者
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                  S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END  ";
        $query .= "                                     BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ) ) ";
                               //2005.10.12Add----------↑----------

        /* 成績 */
        $query .= " ,KIN_REC AS ( ";
        $query .= "     SELECT SCHREGNO, ";
        $query .= "            SUBSTR(SUBCLASSCD,1,2) AS CLASSCD, ";
        $query .= "            INT(ROUND(AVG(FLOAT(INT(".$model->field[$model->exam].")))*10,0)/10) AS SCORE ";
        $query .= "     FROM   KIN_RECORD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            ".$model->field[$model->exam]." IS NOT NULL ";
        // 遡及入力された成績は除外
        if ($model->exam == "0103" || $model->exam == "0203") 
            $query .= "    AND ".$model->field[$model->exam]."_FLG NOT IN ('1') ";
        $query .= "     GROUP BY SCHREGNO, SUBSTR(SUBCLASSCD,1,2) ) ";

        /* ３科 合計・平均 */
        $query .= " ,X3GH AS ( ";
        $query .= "     SELECT W1.SCHREGNO, ";
        $query .= "            COUNT(*) AS CNT3, ";
        $query .= "            SUM(SCORE) AS X3G, ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS X3H ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD IN ('01','03','05') ";
        $query .= "     GROUP BY W1.SCHREGNO ) ";

        /* ５科 合計・平均 */
        $query .= " ,X5GH AS ( ";
        $query .= "     SELECT W1.SCHREGNO, ";
        $query .= "            COUNT(*) AS CNT5, ";
        $query .= "            SUM(SCORE) AS X5G, ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS X5H ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD <= '05' ";
        $query .= "     GROUP BY W1.SCHREGNO ) ";

        /* ９科 合計・平均 */
        $query .= " ,X9GH AS ( ";
        $query .= "     SELECT W1.SCHREGNO, ";
        $query .= "            COUNT(*) AS CNT9, ";
        $query .= "            SUM(SCORE) AS X9G, ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS X9H ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD <= '09' ";
        $query .= "     GROUP BY W1.SCHREGNO ) ";

        /* ３科 席次 */
        $query .= " ,X3R AS ( ";
        $query .= "     SELECT W1.SCHREGNO,COURSE_SEQ, ";
        $query .= "            RANK() OVER (PARTITION BY COURSE_SEQ ORDER BY AVG(FLOAT(SCORE)) DESC) AS X3R ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD IN ('01','03','05') ";
        $query .= "     GROUP BY W1.SCHREGNO,COURSE_SEQ ";
        $query .= "     HAVING 3 = COUNT(*) ) ";

        /* ５科 席次 */
        $query .= " ,X5R AS ( ";
        $query .= "     SELECT W1.SCHREGNO,COURSE_SEQ, ";
        $query .= "            RANK() OVER (PARTITION BY COURSE_SEQ ORDER BY AVG(FLOAT(SCORE)) DESC) AS X5R ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD <= '05' ";
        $query .= "     GROUP BY W1.SCHREGNO,COURSE_SEQ ";
        $query .= "     HAVING 5 = COUNT(*) ) ";

        /* ９科 席次 */
        $query .= " ,X9R AS ( ";
        $query .= "     SELECT W1.SCHREGNO,COURSE_SEQ, ";
        $query .= "            RANK() OVER (PARTITION BY COURSE_SEQ ORDER BY AVG(FLOAT(SCORE)) DESC) AS X9R ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO AND ";
        $query .= "            CLASSCD <= '09' ";
        $query .= "     GROUP BY W1.SCHREGNO,COURSE_SEQ ";
        $query .= "     HAVING 9 = COUNT(*) ) ";

        /* メイン */
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '1' AS RANK_DIV, ";
        $query .= "        T2.X3G AS KEI, T2.X3H AS HEI, T5.X3R AS RNK ";
        $query .= " FROM   SCHNO T1 ";
        $query .= "        LEFT JOIN X3GH T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        $query .= "        LEFT JOIN X3R T5 ON T5.SCHREGNO=T1.SCHREGNO ";
        $query .= " UNION ";
        $query .= " SELECT T1.YEAR, T1.SCHREGNO, '2' AS RANK_DIV, ";
        $query .= "        T3.X5G AS KEI, T3.X5H AS HEI, T6.X5R AS RNK ";
        $query .= " FROM   SCHNO T1 ";
        $query .= "        LEFT JOIN X5GH T3 ON T3.SCHREGNO=T1.SCHREGNO ";
        $query .= "        LEFT JOIN X5R T6 ON T6.SCHREGNO=T1.SCHREGNO ";
        //１学期中間・２学期中間の場合は、更新しない。
        if ($model->exam != "0101" && $model->exam != "0201") {
            $query .= " UNION ";
            $query .= " SELECT T1.YEAR, T1.SCHREGNO, '3' AS RANK_DIV, ";
            $query .= "        T4.X9G AS KEI, T4.X9H AS HEI, T7.X9R AS RNK ";
            $query .= " FROM   SCHNO T1 ";
            $query .= "        LEFT JOIN X9GH T4 ON T4.SCHREGNO=T1.SCHREGNO ";
            $query .= "        LEFT JOIN X9R T7 ON T7.SCHREGNO=T1.SCHREGNO ";
        }
        $query .= "  ";
        $query .= " ORDER BY SCHREGNO, RANK_DIV ";

        $result = $db->query($query); 

        //合計・平均・席次項目の更新
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE record_rank_dat ";
            $query .= "   SET ".$model->field[$model->exam]."_total = ".($row["KEI"] != "" ? $row["KEI"] : "NULL");
            $query .= "      ,".$model->field[$model->exam]."_avg   = ".($row["HEI"] != "" ? $row["HEI"] : "NULL");
            $query .= "      ,".$model->field[$model->exam]."_rank  = ".($row["RNK"] != "" ? $row["RNK"] : "NULL");
            $query .= "      ,registercd    = '".STAFFCD."' ";
            $query .= "      ,updated       = SYSDATE() ";
            $query .= " WHERE year          = '".CTRL_YEAR."' ";
            $query .= "   AND schregno      = '".$row["SCHREGNO"]."' ";
            $query .= "   AND rank_div      = '".$row["RANK_DIV"]."'";

            $db->query($query);
        }

        return;
    }//UpdateQuery

    /* ----------↓---------- ２．RECORD_CLASS_DAT ----------↓---------- */

    /* 生徒データの追加(RECORD_CLASS_DAT) */
    function InsertClassQuery(&$db, $model) 
    {
        $query  = "";
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ) ";
        $query .= " ,RANK AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_CLASS_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY SCHREGNO ) ";

        $query .= " SELECT T1.SCHREGNO ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  NOT EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " ORDER BY SCHREGNO ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //各教科
            for ($i = 1; $i < 10; $i++) {
                $query  = " INSERT INTO RECORD_CLASS_DAT(YEAR, CLASSCD, SCHOOL_KIND, SCHREGNO) ";
                $query .= " VALUES('".CTRL_YEAR."', '".sprintf("%02d",$i)."', 'J', '".$row["SCHREGNO"]."') ";
                $db->query($query);
            }
        }
        return;
    }//InsertClassQuery

    /* データの更新(RECORD_CLASS_DAT) */
    function UpdateClassQuery(&$db, $model) 
    {
        //データをクリア
        $query  = "";
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ) ";
        $query .= " ,RANK AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_CLASS_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' ";
        $query .= "     GROUP BY SCHREGNO ) ";

        $query .= " SELECT T1.SCHREGNO ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.SCHREGNO = T1.SCHREGNO ) ";
        $query .= " ORDER BY SCHREGNO ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE record_class_dat ";
            $query .= "   SET ".$model->field[$model->exam]." = NULL";
            $query .= "      ,registercd    = '".STAFFCD."' ";
            $query .= "      ,updated       = SYSDATE() ";
            $query .= " WHERE year          = '".CTRL_YEAR."' ";
            $query .= "   AND schregno      = '".$row["SCHREGNO"]."' ";

            $db->query($query);
        }

        //異動対象日付
        $date = str_replace("/","-",$model->date);

        //KIN_RECORD_DATをRECORD_CLASS_DATにコピー
        $query  = "";

        /* 在籍 */
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT W1.SCHREGNO ";
        $query .= "     FROM   SCHREG_REGD_DAT W1, COURSE_GROUP_DAT W2, SEMESTER_MST W3 ";
        $query .= "     WHERE  W1.YEAR='".CTRL_YEAR."' AND W1.SEMESTER='".$model->semes[$model->exam]."' AND W1.GRADE='".$model->grade."' AND ";
        $query .= "            W1.YEAR=W2.YEAR AND W1.GRADE=W2.GRADE AND W1.COURSECODE=W2.COURSECODE ";
        $query .= "            AND W1.YEAR=W3.YEAR AND W1.SEMESTER=W3.SEMESTER ";
                               //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                               //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END) OR ";
        $query .= "                                 (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END)) ) ";
                               //留学(1)・休学(2)者
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                  S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END  ";
        $query .= "                                     BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ) ) ";

        /* 成績 */
        $query .= " ,KIN_REC AS ( ";
        $query .= "     SELECT SCHREGNO, ";
        $query .= "            SUBSTR(SUBCLASSCD,1,2) AS CLASSCD, ";
        $query .= "            INT(ROUND(AVG(FLOAT(INT(".$model->field[$model->exam].")))*10,0)/10) AS SCORE ";
        $query .= "     FROM   KIN_RECORD_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            ".$model->field[$model->exam]." IS NOT NULL ";
        // 遡及入力された成績は除外
        if ($model->exam == "0103" || $model->exam == "0203") 
            $query .= "    AND ".$model->field[$model->exam]."_FLG NOT IN ('1') ";
        $query .= "     GROUP BY SCHREGNO, SUBSTR(SUBCLASSCD,1,2) ) ";

        /* メイン */
        $query .= " SELECT T1.SCHREGNO, T2.CLASSCD, T2.SCORE ";
        $query .= " FROM   SCHNO T1 ";
        $query .= "        INNER JOIN KIN_REC T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        $query .= " ORDER BY CLASSCD, SCHREGNO ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE record_class_dat ";
            $query .= "   SET ".$model->field[$model->exam]." =  ".($row["SCORE"] != "" ? $row["SCORE"] : "NULL");
            $query .= "      ,registercd    = '".STAFFCD."' ";
            $query .= "      ,updated       = SYSDATE() ";
            $query .= " WHERE year          = '".CTRL_YEAR."' ";
            $query .= "   AND classcd       = '".$row["CLASSCD"]."'";
            $query .= "   AND SCHOOL_KIND   = 'J'";
            $query .= "   AND schregno      = '".$row["SCHREGNO"]."' ";

            $db->query($query);
        }

        return;
    }//UpdateClassQuery

    /* ----------↓---------- ３．RECORD_CLASS_AVERAGE_DAT ----------↓---------- */

    /* 生徒データの追加(RECORD_CLASS_AVERAGE_DAT) */
    function InsertClassAverageQuery(&$db, $model) 
    {
        $query  = "";
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT GRADE, HR_CLASS ";
        $query .= "     FROM   SCHREG_REGD_HDAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->semes[$model->exam]."' AND GRADE='".$model->grade."' ) ";
        $query .= " ,RANK AS ( ";
        $query .= "     SELECT HR_CLASS ";
        $query .= "     FROM   RECORD_CLASS_AVERAGE_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND GRADE='".$model->grade."' ";
        $query .= "     GROUP BY HR_CLASS ) ";

        $query .= " SELECT T1.GRADE, T1.HR_CLASS ";
        $query .= " FROM   SCHNO T1 ";
        $query .= " WHERE  NOT EXISTS(SELECT 'X' FROM RANK T2 WHERE T2.HR_CLASS = T1.HR_CLASS ) ";
        $query .= " ORDER BY HR_CLASS ";

        $result = $db->query($query); 

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //1:クラス合計,2:クラス平均,3:コース平均
            for ($calcdiv = 1; $calcdiv < 4; $calcdiv++) {
                //各教科
                for ($i = 1; $i < 10; $i++) {
                    $db->query(knjd211query::getClassAverageQuery($row, sprintf("%02d",$i), $calcdiv));
                }
                //３科
                $db->query(knjd211query::getClassAverageQuery($row, "3T", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "3A", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "3C", $calcdiv));
                //５科
                $db->query(knjd211query::getClassAverageQuery($row, "5T", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "5A", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "5C", $calcdiv));
                //９科
                $db->query(knjd211query::getClassAverageQuery($row, "9T", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "9A", $calcdiv));
                $db->query(knjd211query::getClassAverageQuery($row, "9C", $calcdiv));
            }
        }
        return;
    }//InsertClassAverageQuery

    //RECORD_CLASS_AVERAGE_DAT2
    function getClassAverageQuery($row, $classcd, $calcdiv) 
    {
        $query  = "";
        $query .= " INSERT INTO RECORD_CLASS_AVERAGE_DAT(YEAR, GRADE, HR_CLASS, CLASSCD, SCHOOL_KIND, CALC_DIV) ";
        $query .= " VALUES('".CTRL_YEAR."', '".$row["GRADE"]."', '".$row["HR_CLASS"]."', '".$classcd."', 'J' , '".$calcdiv."') ";

        return $query;
    }

    /* データの更新(RECORD_CLASS_AVERAGE_DAT) */
    function UpdateClassAverageQuery(&$db, $model) 
    {
        //データをクリア
        $query  = "UPDATE record_class_average_dat ";
        $query .= "   SET ".$model->field[$model->exam]." = NULL";
        $query .= "      ,registercd    = '".STAFFCD."' ";
        $query .= "      ,updated       = SYSDATE() ";
        $query .= " WHERE year          = '".CTRL_YEAR."' ";
        $query .= "   AND grade         = '".$model->grade."' ";

        $db->query($query);

        //異動対象日付
        $date = str_replace("/","-",$model->date);

        //1:クラス合計,2:クラス平均,3:コース平均等を算出
        $query  = "";

        /* 在籍 */
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO, W1.HR_CLASS, COURSE_SEQ ";
        $query .= "     FROM   SCHREG_REGD_DAT W1, COURSE_GROUP_DAT W2, SEMESTER_MST W3 ";
        $query .= "     WHERE  W1.YEAR='".CTRL_YEAR."' AND W1.SEMESTER='".$model->semes[$model->exam]."' AND W1.GRADE='".$model->grade."' AND ";
        $query .= "            W1.YEAR=W2.YEAR AND W1.GRADE=W2.GRADE AND W1.COURSECODE=W2.COURSECODE ";
        $query .= "            AND W1.YEAR=W3.YEAR AND W1.SEMESTER=W3.SEMESTER ";
                               //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                               //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END) OR ";
        $query .= "                                 (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END)) ) ";
                               //留学(1)・休学(2)者
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                            WHERE S1.SCHREGNO = W1.SCHREGNO AND ";
        $query .= "                                  S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                                     CASE WHEN W3.EDATE < '".$date."' THEN W3.EDATE ";
        $query .= "                                     ELSE '".$date."' END  ";
        $query .= "                                     BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ) ) ";

        /* 各教科 */
        /* 成績１:RECORD_CLASS_DAT */
        $query .= " ,KIN_REC AS ( ";
        $query .= "     SELECT CLASSCD, SCHREGNO, ";
        $query .= "            ".$model->field[$model->exam]." AS SCORE ";
        $query .= "     FROM   RECORD_CLASS_DAT W1 ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            ".$model->field[$model->exam]." IS NOT NULL ) ";
        /* 1:クラス合計・2:クラス平均 */
        $query .= " ,CLS_GH AS ( ";
        $query .= "     SELECT CLASSCD, HR_CLASS, ";
        $query .= "            SUM(SCORE) AS CLS_G, ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS CLS_H ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO ";
        $query .= "     GROUP BY CLASSCD, HR_CLASS ) ";
        /* 3:コース平均 */
        $query .= " ,COS_GH AS ( ";
        $query .= "     SELECT CLASSCD, COURSE_SEQ, ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS COS_H ";
        $query .= "     FROM   SCHNO W1,KIN_REC W2 ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO ";
        $query .= "     GROUP BY CLASSCD, COURSE_SEQ ) ";

        /* ３・５・９科 */
        /* 成績２:RECORD_RANK_DAT */
        $query .= " ,KIN_REC2 AS ( ";
        $query .= "     SELECT SCHREGNO, ";
        $query .= "            CASE WHEN RANK_DIV = '1' THEN 3 ";
        $query .= "                 WHEN RANK_DIV = '2' THEN 5 ";
        $query .= "                 WHEN RANK_DIV = '3' THEN 9 END AS DIV, ";
        $query .= "            ".$model->field[$model->exam]."_total AS SCORE_G ";
        $query .= "     FROM   RECORD_RANK_DAT W1 ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            RANK_DIV IN('1','2','3') AND ";
        $query .= "            ".$model->field[$model->exam]."_rank IS NOT NULL ) ";
        /* 1:クラス合計・2:クラス平均 */
        $query .= " ,CLS_GH_TAC AS (  ";
        $query .= "     SELECT DIV, HR_CLASS,  ";
        $query .= "            COUNT(*) AS CLS_CNT,  ";
        $query .= "            SUM(SCORE_G) AS CLS_G_T,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE_G))*10,0)/10,5,1) AS CLS_H_T,  ";
        $query .= "            DECIMAL(ROUND((FLOAT(SUM(SCORE_G))/DIV)*10,0)/10,5,1) AS CLS_G_A,  ";
        $query .= "            DECIMAL(ROUND((AVG(FLOAT(SCORE_G))/DIV)*10,0)/10,5,1) AS CLS_H_A  ";
        $query .= "     FROM   SCHNO W1,KIN_REC2 W2  ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO  ";
        $query .= "     GROUP BY DIV, HR_CLASS )  ";
        /* 3:コース平均 */
        $query .= " ,COS_GH_TAC AS (  ";
        $query .= "     SELECT DIV, COURSE_SEQ,  ";
        $query .= "            COUNT(*) AS COS_CNT,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(SCORE_G))*10,0)/10,5,1) AS COS_H_T,  ";
        $query .= "            DECIMAL(ROUND((AVG(FLOAT(SCORE_G))/DIV)*10,0)/10,5,1) AS COS_H_A  ";
        $query .= "     FROM   SCHNO W1,KIN_REC2 W2  ";
        $query .= "     WHERE  W1.SCHREGNO=W2.SCHREGNO  ";
        $query .= "     GROUP BY DIV, COURSE_SEQ )  ";

        /* 在籍２ */
        /* クラス・コースにグループ化 */
        $query .= " ,SCHNO2 AS ( ";
        $query .= "     SELECT HR_CLASS, COURSE_SEQ ";
        $query .= "     FROM   SCHNO ";
        $query .= "     GROUP BY HR_CLASS, COURSE_SEQ ) ";

        /* メイン */
                //各教科(01～09)
                    //1:クラス合計
        $query .= " SELECT T1.HR_CLASS, T2.CLASSCD, '1' AS CALC_DIV, T2.CLS_G AS REC ";
        $query .= " FROM   SCHNO2 T1 ";
        $query .= "        INNER JOIN CLS_GH T2 ON T2.HR_CLASS=T1.HR_CLASS ";
                    //2:クラス平均
        $query .= " UNION ";
        $query .= " SELECT T1.HR_CLASS, T2.CLASSCD, '2' AS CALC_DIV, T2.CLS_H AS REC ";
        $query .= " FROM   SCHNO2 T1 ";
        $query .= "        INNER JOIN CLS_GH T2 ON T2.HR_CLASS=T1.HR_CLASS ";
                    //3:コース平均
        $query .= " UNION ";
        $query .= " SELECT T1.HR_CLASS, T2.CLASSCD, '3' AS CALC_DIV, T2.COS_H AS REC ";
        $query .= " FROM   SCHNO2 T1 ";
        $query .= "        INNER JOIN COS_GH T2 ON T2.COURSE_SEQ=T1.COURSE_SEQ ";
                //３・５・９科合計(T)
                    //1:クラス合計
        $query .= " UNION ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3T'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5T'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9T' END AS CLASSCD,  ";
        $query .= "        '1' AS CALC_DIV, T2.CLS_G_T AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //2:クラス平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3T'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5T'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9T' END AS CLASSCD,  ";
        $query .= "        '2' AS CALC_DIV, T2.CLS_H_T AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //3:コース平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3T'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5T'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9T' END AS CLASSCD,  ";
        $query .= "        '3' AS CALC_DIV, T2.COS_H_T AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN COS_GH_TAC T2 ON T2.COURSE_SEQ=T1.COURSE_SEQ  ";
                //３・５・９科平均(A)
                    //1:クラス合計
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3A'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5A'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9A' END AS CLASSCD,  ";
        $query .= "        '1' AS CALC_DIV, T2.CLS_G_A AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //2:クラス平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3A'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5A'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9A' END AS CLASSCD,  ";
        $query .= "        '2' AS CALC_DIV, T2.CLS_H_A AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //3:コース平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3A'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5A'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9A' END AS CLASSCD,  ";
        $query .= "        '3' AS CALC_DIV, T2.COS_H_A AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN COS_GH_TAC T2 ON T2.COURSE_SEQ=T1.COURSE_SEQ  ";
                //３・５・９科人数(C)
                    //1:クラス合計
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3C'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5C'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9C' END AS CLASSCD,  ";
        $query .= "        '1' AS CALC_DIV, T2.CLS_CNT AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //2:クラス平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3C'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5C'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9C' END AS CLASSCD,  ";
        $query .= "        '2' AS CALC_DIV, T2.CLS_CNT AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN CLS_GH_TAC T2 ON T2.HR_CLASS=T1.HR_CLASS  ";
                    //3:コース平均
        $query .= " UNION  ";
        $query .= " SELECT T1.HR_CLASS,  ";
        $query .= "        CASE WHEN T2.DIV = 3 THEN '3C'  ";
        $query .= "             WHEN T2.DIV = 5 THEN '5C'  ";
        $query .= "             WHEN T2.DIV = 9 THEN '9C' END AS CLASSCD,  ";
        $query .= "        '3' AS CALC_DIV, T2.COS_CNT AS REC  ";
        $query .= " FROM   SCHNO2 T1  ";
        $query .= "        INNER JOIN COS_GH_TAC T2 ON T2.COURSE_SEQ=T1.COURSE_SEQ  ";

        $query .= " ORDER BY HR_CLASS, CLASSCD, CALC_DIV ";

        $result = $db->query($query); 

        //1:クラス合計,2:クラス平均,3:コース平均項目の更新
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = "UPDATE record_class_average_dat ";
            $query .= "   SET ".$model->field[$model->exam]." = ".($row["REC"] != "" ? $row["REC"] : "NULL");
            $query .= "      ,registercd    = '".STAFFCD."' ";
            $query .= "      ,updated       = SYSDATE() ";
            $query .= " WHERE year          = '".CTRL_YEAR."' ";
            $query .= "   AND grade         = '".$model->grade."' ";
            $query .= "   AND hr_class      = '".$row["HR_CLASS"]."' ";
            $query .= "   AND classcd       = '".$row["CLASSCD"]."' ";
            $query .= "   AND SCHOOL_KIND   = 'J' ";
            $query .= "   AND calc_div      = '".$row["CALC_DIV"]."'";

            $db->query($query);
        }

        return;
    }//UpdateClassAverageQuery

} 
?>