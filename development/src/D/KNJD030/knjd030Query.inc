<?php

require_once('for_php7.php');

class knjd030Query extends Query {

    function selectQuery($model)
    {
        $query = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.CHAIRCD, ";
        $query .= "    T1.HR_NAMEABBV, ";
        $query .= "    T1.HR_NAME, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.NAME_SHOW, ";
        $query .= "    T1.SEX, ";
        $query .= "    T2.SCORE, ";
        $query .= "    T2.ATTEND_FLG, ";
        $query .= "    T4.OPERATION_DATE, ";
        $query .= "    T4.SUBCLASSCD, ";
        $query .= "    T4.TESTKINDCD, ";
        $query .= "    T4.TESTITEMCD, ";
        $query .= "    T4.TESTITEMNAME, ";
        $query .= "    T4.TESTKINDNAME, ";
        $query .= "    T4.SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        ST1.SCHREGNO, ";
        $query .= "        MIN(ST1.CHAIRCD) AS CHAIRCD, ";
        $query .= "        ST4.HR_NAME, ";
        $query .= "        ST4.HR_NAMEABBV, ";
        $query .= "        ST2.ATTENDNO, ";
        $query .= "        ST3.NAME_SHOW, ";
        $query .= "        MEISYOU_GET(ST3.SEX,'Z002',1) AS SEX ";
        $query .= "    FROM ";
// テーブル変更に伴う修正 CHAIR_STD_DATの適用開始日付の生徒のデータを取得 -------
/* 2004/06/04 y.arakaki
        $query .= "        (SELECT WK1.schregno, WK1.chaircd, WK1.year, WK1.semester FROM chair_std_dat WK1
                             WHERE WK1.year     = '" .$model->field["YEAR"] ."'
                               AND WK1.semester = '" .$model->field["SEMESTER"] ."'
                               AND WK1.chaircd IN ('" .implode($model->field["CHAIRCD"],"','") ."')";
        if ($model->field["APPDATE"][0] != "")
            $query .= "        AND WK1.appdate IN ('" .implode($model->field["APPDATE"],"','") ."')";
*/
        $query .= "        (SELECT WK1.schregno, WK1.chaircd, WK1.year, WK1.semester FROM chair_std_dat WK1
                             WHERE WK1.year     = '" .$model->field["YEAR"] ."'
                               AND WK1.semester = '" .$model->field["SEMESTER"] ."'
                               AND WK1.chaircd IN ('" .implode(array_unique($model->field["CHAIRCD"]),"','") ."')";
        if ($model->field["APPDATE"][0] != "")
            $query .= "        AND WK1.appdate IN ('" .implode(array_unique($model->field["APPDATE"]),"','") ."')";

        $query .= " ) ST1, ";
        $query .= "        SCHREG_REGD_DAT  ST2, ";
        $query .= "        SCHREG_BASE_MST  ST3, ";
        $query .= "        SCHREG_REGD_HDAT ST4  ";
        $query .= "    WHERE ";
        $query .= "        ST1.YEAR     = ST2.YEAR AND ";
        $query .= "        ST1.YEAR     = ST4.YEAR AND ";
        $query .= "        ST1.SCHREGNO = ST2.SCHREGNO AND ";
        $query .= "        ST1.SCHREGNO = ST3.SCHREGNO AND ";
        $query .= "        ST1.SEMESTER = ST2.SEMESTER AND ";
        $query .= "        ST1.SEMESTER = ST4.SEMESTER AND ";
        $query .= "        ST2.GRADE    = ST4.GRADE AND ";
        $query .= "        ST2.HR_CLASS = ST4.HR_CLASS AND ";
        $query .= "        ( ";
        $query .= "         NOT (ST3.GRD_DIV IN ('1','2','3') AND ST3.GRD_DATE <= '".CTRL_DATE."') OR ";   //1:卒業/2:退学/3:転学
        $query .= "         ST3.GRD_DATE IS NULL OR ";
        $query .= "         ST3.GRD_DIV  IS NULL ";
        $query .= "        ) ";
        $query .= "    GROUP BY ";  
        $query .= "        ST1.SCHREGNO, ";
        $query .= "        ST4.HR_NAMEABBV, ";
        $query .= "        ST4.HR_NAME, ";
        $query .= "        ST2.ATTENDNO, ";
        $query .= "        ST3.NAME_SHOW, ";
        $query .= "        ST3.SEX ";
        $query .= "    ) T1  LEFT OUTER JOIN (";
        $query .= "    SELECT DISTINCT ";
        $query .= "          ST1.SCHREGNO, ";
        $query .= "          COALESCE(ST1.SCORE,0) AS SCORE, ";
        $query .= "          ST1.UPDATED, ";
//        $query .= "          ST1.CHAIRCD, ";
        $query .= "          ST1.ATTEND_FLG, ";
        $query .= "          ST1.SUBCLASSCD, ";
        $query .= "          ST1.TESTKINDCD, ";
        $query .= "          ST1.TESTITEMCD ";
        $query .= "      FROM ";
        $query .= "          TESTSCORE_DAT ST1 ";
        $query .= "      WHERE ";
        $query .= "          ST1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "          ST1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "          ST1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "          ST1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "          ST1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."'  ";
//        $query .= "      ) T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "      ) T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT OUTER JOIN ( ";
        $query .= "      SELECT DISTINCT ";
        $query .= "             ST1.YEAR, ";
        $query .= "             ST1.CHAIRCD, ";
        $query .= "             ST1.OPERATION_DATE, ";
        $query .= "             ST3.TESTKINDCD, ";
        $query .= "             ST2.TESTITEMCD, ";
        $query .= "             ST2.TESTITEMNAME, ";
        $query .= "             ST3.TESTKINDNAME, ";
        $query .= "             ST4.SUBCLASSCD, ";
        $query .= "             ST4.SUBCLASSNAME ";
        $query .= "     FROM ";
        $query .= "          TESTSCORE_HDAT ST1, ";
        $query .= "          TESTITEM_MST ST2, ";
        $query .= "          TESTKIND_MST ST3, ";
        $query .= "          SUBCLASS_MST ST4 ";
        $query .= "     WHERE ";
        $query .= "          ST1.YEAR        = '" .$model->field["YEAR"] ."' AND ";
        $query .= "          ST1.SEMESTER    = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "          ST1.TESTKINDCD  = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "          ST1.TESTITEMCD  = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "          ST2.YEAR       = ST1.YEAR AND ";
        $query .= "          ST2.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "          ST2.TESTKINDCD = ST1.TESTKINDCD AND ";
        $query .= "          ST2.TESTITEMCD = ST1.TESTITEMCD AND ";
        $query .= "          ST2.TESTKINDCD = ST3.TESTKINDCD AND ";
        $query .= "          ST2.SUBCLASSCD = ST4.SUBCLASSCD ";
        $query .= "     ) T4 ON T1.CHAIRCD = T4.CHAIRCD ";
        $query .= "WHERE ";
        $query .= "    NOT EXISTS (SELECT ";
        $query .= "                    w1.SCHREGNO ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_TRANSFER_DAT w1, ";
        $query .= "                    TESTSCORE_HDAT w2 ";
        $query .= "                WHERE ";
        $query .= "                    not(w1.TRANSFERCD IN ('1','2','3')) AND ";      //1:留学/2:休学/3:出停
        $query .= "                    w1.SCHREGNO        = T1.SCHREGNO AND ";
        $query .= "                    w2.CHAIRCD         = T1.CHAIRCD AND ";
//        $query .= "                    w2.OPERATION_DATE <= w1.TRANSFER_SDATE AND ";
        $query .= "                    w2.OPERATION_DATE  BETWEEN w1.TRANSFER_SDATE ";
        $query .= "                                           AND w1.TRANSFER_EDATE AND ";
        $query .= "                    w2.YEAR            = '" .$model->field["YEAR"] ."' AND ";
        $query .= "                    w2.SEMESTER        = '" .CTRL_SEMESTER."' AND ";
        $query .= "                    w2.TESTKINDCD      = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "                    w2.TESTITEMCD      = '" .$model->field["TESTITEMCD"] ."' ";
        $query .= "                ) ";
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //番号順
                        $query .= "T1.HR_NAMEABBV,T1.ATTENDNO ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "SCORE":       //席次順
                        if ($order > 0){
                            $query .= "ATTEND_FLG DESC,SCORE DESC,T1.HR_NAMEABBV,T1.ATTENDNO ";
                        }else{
                            $query .= "ATTEND_FLG DESC,SCORE,T1.HR_NAMEABBV,T1.ATTENDNO ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= "ORDER BY ";
            $query .= "    T1.HR_NAMEABBV, ";
            $query .= "    T1.ATTENDNO ASC ";
        }
        //echo $query;
        return $query;
    }
    
    //平均点などを求める
    function getAvgScoreQuery($model)
    {
/*  2004/06/23 arakaki
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS SUBJECT, ";
        $query .= "   SUM(INTEGER(T2.ATTEND_FLG)) AS EXAMINEE, ";
        $query .= "   CASE FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) ";
        $query .= "       WHEN 0 THEN 0 ";
        $query .= "       ELSE FLOAT(SUM(T2.SCORE)) / FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) END AS AVG_SCORE, ";
        $query .= "   T1.PERFECT ";
        $query .= " FROM ";
        $query .= "   TESTSCORE_HDAT T1 LEFT OUTER JOIN ";
        $query .= "   TESTSCORE_DAT T2 ";
        $query .= " ON ";
        $query .= "   T1.YEAR       = T2.YEAR AND ";
        $query .= "   T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "   T2.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   T1.TESTKINDCD = T2.TESTKINDCD AND ";
        $query .= "   T1.TESTITEMCD = T2.TESTITEMCD AND ";
        $query .= "   T1.CHAIRCD    = T2.CHAIRCD ";
        $query .= "   , (SELECT DISTINCT schregno
                           FROM chair_std_dat
                          WHERE year     = '". $model->field["YEAR"] ."'
                            AND semester = '". $model->field["SEMESTER"] ."'
                            AND chaircd  IN ('" .implode($model->field["CHAIRCD"],"','") ."') ";
        if ($model->field["APPDATE"][0] != "")
            $query .= "     AND appdate IN ('". implode($model->field["APPDATE"],"','") ."') ";
        $query .= "   ) T3 ";                   
        $query .= " WHERE ";
        $query .= "   T2.schregno   = T3.schregno AND";
        $query .= "   T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "   T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "   T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "   T1.CHAIRCD    IN ('" .implode($model->field["CHAIRCD"],"','") ."') ";
        $query .= " GROUP BY ";
        $query .= "   T1.PERFECT ";
2004/06/23 arakaki */

        $query  = " SELECT ";
        $query .= "   COUNT(*) AS SUBJECT, ";
        $query .= "   SUM(INTEGER(T2.ATTEND_FLG)) AS EXAMINEE, ";
        $query .= "   CASE FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) ";
        $query .= "       WHEN 0 THEN 0 ";
        $query .= "       ELSE FLOAT(SUM(T2.SCORE)) / FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) END AS AVG_SCORE, ";
        $query .= "   T1.PERFECT ";
        $query .= " FROM ";
        $query .= "     TESTSCORE_HDAT T1  ";
        $query .= " LEFT OUTER JOIN  ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     TT1.YEAR        AS YEAR, ";
        $query .= "     TT1.SEMESTER    AS SEMESTER, ";
        $query .= "     TT1.ATTEND_FLG  AS ATTEND_FLG, ";
        $query .= "     TT1.TESTKINDCD  AS TESTKINDCD, ";
        $query .= "     TT1.TESTITEMCD  AS TESTITEMCD,  ";
        $query .= "     TT1.CHAIRCD     AS CHAIRCD, ";
        $query .= "     TT1.SCORE       AS SCORE ";
        $query .= " FROM ";
        $query .= "     TESTSCORE_DAT TT1, ";
        $query .= "     CHAIR_STD_DAT TT2 ";
        $query .= " WHERE  ";
        $query .= "     TT1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "     TT1.SEMESTER   = '". $model->field["SEMESTER"] ."' AND ";
        $query .= "     TT1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "     TT1.YEAR = TT2.YEAR AND ";
        $query .= "     TT1.SEMESTER = TT2.SEMESTER AND ";
        $query .= "     TT1.SCHREGNO = TT2.SCHREGNO AND ";
        $query .= "     TT2.CHAIRCD    IN ('" .implode($model->field["CHAIRCD"],"','") ."') ";
        if ($model->field["APPDATE"][0] != "")
            $query .= "     AND TT2.APPDATE IN ('". implode($model->field["APPDATE"],"','") ."') ";
        $query .= " )T2  ";
        $query .= " ON  T1.YEAR       = T2.YEAR  ";
        $query .= " AND T1.SEMESTER   = T2.SEMESTER  ";
        $query .= " AND T1.TESTKINDCD = T2.TESTKINDCD  ";
        $query .= " AND T1.TESTITEMCD = T2.TESTITEMCD  ";
        $query .= " AND T1.CHAIRCD    = T2.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "     T1.SEMESTER   = '". $model->field["SEMESTER"] ."' AND ";
        $query .= "     T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "     T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "     T1.CHAIRCD    IN ('" .implode($model->field["CHAIRCD"],"','") ."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.PERFECT ";


        //echo $query;
        return $query;
    }

/*
     機能      ：得点入力画面編集対象データの書込み処理

     戻り値    ：True:成功、False:失敗

     機能説明  ：得点入力画面より編集した情報のみを配列でサーバに渡し更新する
*/
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //先に更新データを削除
        $query  = "DELETE FROM testscore_dat";
        $query .= " WHERE year       = '".$model->field["YEAR"]."'";
        $query .= "   AND semester   = '".$model->field["SEMESTER"]."'";
        $query .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."'";
        $query .= "   AND testkindcd = '".$model->field["TESTKINDCD"]."'";
        $query .= "   AND testitemcd = '".$model->field["TESTITEMCD"]."'";
//        $query .= "   AND schregno IN ('" .implode($model->schregno,"','") ."')";      
        $query .= "   AND schregno IN ('" .implode($model->schregno,"','") ."')";      

        $db->query($query);

        //成績項目別得点データの登録
        for ($i = 0,$sum = $exam = 0; $i < get_count($model->field["SCORE"]); $i++) {


        //異動情報のある生徒は削除
        $schregno = $model->schregno[$i];
        $oparation_d = $model->operation_date;
        $query = knjd030Query::Get_NameQuery($schregno,$oparation_d);
        $transfer = $db->getOne($query);

            if(!$transfer){
            $score = $model->field["SCORE"][$i];

            if (is_numeric($score)){
               $attend_flg = 1;        //得点が入っていれば受検区分を"1"とする。
               //合計を求める
               $sum += $score;
               //対象人数
               $exam++;
            }else{
               $attend_flg = 0;
               $score = 0;
            }

                $data = array();
                $data["YEAR"][TEXT]       = $model->field["YEAR"];
                $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
                $data["SUBCLASSCD"][TEXT] = $model->field["SUBCLASSCD"];
                $data["TESTKINDCD"][TEXT] = $model->field["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT] = $model->field["TESTITEMCD"];
                $data["SCHREGNO"][TEXT]   = $model->schregno[$i];
                $data["CHAIRCD"][TEXT]    = $model->chaircd[$i];             //受講クラスコード
                $data["ATTEND_FLG"][TEXT] = $attend_flg;
                $data["SCORE"][NUMBER]    = $score;
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "SYSDATE()";

                $query = Query::insertSQL($data, "TESTSCORE_DAT");
                $db->query($query);
            }
        }

        $db->query(knjd030Query::Up_Testscore_H($model));
        //echo knjd030Query::Up_Testscore_H($model);
        $db->commit();
        Query::dbCheckIn($db);
        return true;        
    }

    function Up_Testscore_H($model)
    {

        $data["YEAR"][TEXT]          = $model->field["YEAR"];
        $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]    = $model->field["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]    = $model->field["TESTITEMCD"];
        $data["CHAIRCD"][TEXT]       = $model->chaircd[0]; 
        $data["OPERATION_FLG"][TEXT] = "1";                   //実施済み
        $data["UPDATED"][FUNC]       = "SYSDATE()";
        $where  = " WHERE ";
        $where .= " YEAR       = '".$model->field["YEAR"]."' AND ";
        $where .= " SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
        $where .= " TESTKINDCD = '".$model->field["TESTKINDCD"]."' AND ";
        $where .= " TESTITEMCD = '".$model->field["TESTITEMCD"]."' AND ";
        $where .= " CHAIRCD    = '".$model->chaircd[0]."' ";

        $query = Query::updateSQL($data, "TESTSCORE_HDAT",$where);
        return $query;
}

    function Get_NameQuery($cd,$od)
    {

        $query  = " SELECT ";
        $query .= " T2.NAME1 ";
        $query .= " FROM ";
        $query .= " SCHREG_TRANSFER_DAT T1, ";
        $query .= " NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= " T1.SCHREGNO   = '".$cd."' AND ";
        $query .= " T1.TRANSFERCD = T2.NAMECD2 AND ";
        $query .= " T2.NAMECD1='A004' AND ";
        $query .= " TRANSFERCD IN ('1','2','3') AND ";     //1:留学/2:休学/3:出停
        $query .= " '".$od."' between TRANSFER_SDATE AND ";
        $query .= " TRANSFER_EDATE ";

        return $query;

    }

}
?>
