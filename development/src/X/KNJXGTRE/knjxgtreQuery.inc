<?php

require_once('for_php7.php');

class knjxgtreQuery extends Query {
    //科目ツリー用のSQL
    function selectQuerySubclass($model)
    {
        $query = "";

        $query .= "SELECT DISTINCT ";
        $query .= "  T3.CLASSCD, ";
        $query .= "  T3.CLASSNAME, ";
        $query .= "  T4.SUBCLASSCD, ";
        $query .= "  T4.SUBCLASSNAME, ";
        $query .= "  T6.TESTITEMCD, ";
        $query .= "  T6.TESTKINDCD, ";
        $query .= "  T6.TESTITEMNAME ";
        $query .= "FROM ";
        $query .= "     (";
        $query .= "       SELECT";
        $query .= "           YEAR,";
        $query .= "           SEMESTER,";
        $query .= "           CHAIRCD,";
        $query .= "           SUBCLASSCD";
        $query .= "       FROM";
        $query .= "           CHAIR_DAT";
        $query .= "       WHERE";
        $query .= "           YEAR       = '" .CTRL_YEAR ."' AND";
        $query .= "           SEMESTER   = '" .$model->semester ."' AND";
        //$query .= "           SUBCLASSCD < '5000'";
        $query .= "           SUBSTR(SUBCLASSCD,1,2) < '90'";
        $query .= "     ) T1 LEFT OUTER JOIN (SELECT ";
        $query .= "            ST1.YEAR, ";
        $query .= "            ST1.SEMESTER, ";
        $query .= "            ST1.CHAIRCD, ";
        $query .= "            ST1.TESTITEMCD, ";
        $query .= "            ST1.TESTKINDCD, ";
        $query .= "            ST2.TESTITEMNAME, ";
        $query .= "            ST2.SUBCLASSCD ";
        $query .= "          FROM ";
        $query .= "            TESTSCORE_HDAT ST1, ";
        $query .= "            TESTITEM_MST ST2 ";
        $query .= "            ,CHAIR_DAT ST3";
        $query .= "          WHERE ";
        $query .= "            ST1.YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "            ST1.SEMESTER   = '" .$model->semester ."' AND ";
        $query .= "            ST1.year       = ST2.year AND
                               ST3.subclasscd = ST2.subclasscd AND 
                               ST1.chaircd    = ST3.chaircd AND 
                               ST1.year       = ST3.year AND 
                               ST1.semester   = ST3.semester AND";
        $query .= "            ST1.TESTITEMCD = ST2.TESTITEMCD AND ";
        $query .= "            ST1.TESTKINDCD = ST2.TESTKINDCD ";
        $query .= "          ) T6 ON T1.YEAR       = T6.YEAR AND  ";
        $query .= "                  T1.SEMESTER   = T6.SEMESTER AND  ";
        $query .= "                  T1.CHAIRCD    = T6.CHAIRCD AND  ";
        $query .= "                  T1.SUBCLASSCD = T6.SUBCLASSCD, ";
        $query .= "     (SELECT";
        $query .= "         YEAR,";
        $query .= "         SEMESTER,";
        $query .= "         CHAIRCD,";
        $query .= "         STAFFCD";
        $query .= "      FROM";
        $query .= "         CHAIR_STF_DAT ";
        $query .= "      WHERE";
        $query .= "           YEAR     = '" .CTRL_YEAR ."' AND";
        $query .= "           SEMESTER = '" .$model->semester ."'";
        $query .= "     ) T2,";
        $query .= "     CLASS_MST     T3, ";
        $query .= "     SUBCLASS_MST  T4, ";
        $query .= "     (SELECT";
        $query .= "         SST1.YEAR,";
        $query .= "         SST1.CHAIRCD,";
        $query .= "         MIN(EXECUTEDATE) SDATE,";
        $query .= "         MAX(EXECUTEDATE) EDATE";
        $query .= "     FROM";
        $query .= "         SCH_CHR_DAT SST1    ,";
        $query .= "         (SELECT";
        $query .= "             SDATE,";
        $query .= "             EDATE";
        $query .= "         FROM";
        $query .= "             SEMESTER_MST";
        $query .= "         WHERE";
        $query .= "             YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             SEMESTER = '" .$model->semester ."'";
        $query .= "         ) SST2";
        $query .= "     WHERE";
        $query .= "         SST1.PERIODCD > '0' AND";
        $query .= "         SST1.EXECUTEDATE BETWEEN SST2.SDATE AND";
        $query .= "         SST2.EDATE";
        $query .= "     GROUP BY";
        $query .= "         SST1.CHAIRCD,";
        $query .= "         SST1.YEAR";
        $query .= "     ) T5 ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR     = T2.YEAR AND ";
        $query .= "  T1.SEMESTER = T2.SEMESTER AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT){
            $query .= "  T2.STAFFCD  = '" .STAFFCD ."' AND ";        
        }elseif ($model->auth == DEF_NOAUTH){        //権限なし(表示不可)
            $query .= "  T2.STAFFCD  IS NULL AND ";
        }
/*        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_REFER_RESTRICT){          //参照可能(自分の受持ちクラスのみ)
            $query .= "  T2.STAFFCD  = '" .STAFFCD ."' AND ";
        }elseif ($model->auth == DEF_NOAUTH){        //権限なし(表示不可)
            $query .= "  T2.STAFFCD  IS NULL AND ";
        }
*/        $query .= "  T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "  T1.CHAIRCD  = T5.CHAIRCD AND ";

/*        if ($model->auth == DEF_UPDATE_RESTRICT ){   //制限付き更新可(自分の受持ち教科の全てのクラス)
            $query .= " SUBSTR(T1.SUBCLASSCD,1,2) IN (";
            $query .= "                               SELECT DISTINCT ";
            $query .= "                                   SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .= "                               FROM ";
            $query .= "                                   CHAIR_DAT     T1, ";
            $query .= "                                   CHAIR_STF_DAT T2 ";
            $query .= "                               WHERE ";
            $query .= "                                   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
            $query .= "                                   T1.SEMESTER = '" .$model->semester ."' AND ";
            $query .= "                                   T1.YEAR     = T2.YEAR AND ";
            $query .= "                                   T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "                                   T1.CHAIRCD  = T2.CHAIRCD AND ";
            $query .= "                                   T2.STAFFCD  = '" .STAFFCD ."' ";
            $query .= "                              ) AND ";
        }
*/        $query .= "  SUBSTR(T1.SUBCLASSCD,1,2) = T3.CLASSCD AND ";
        $query .= "  T1.SUBCLASSCD             = T4.SUBCLASSCD ";
        $query .= "ORDER BY ";
        $query .= "  T3.CLASSCD, ";
        $query .= "  T4.SUBCLASSCD, ";
        $query .= "  T6.TESTITEMCD, ";
        $query .= "  T6.TESTKINDCD ";

        return $query;
    }
    //学期を取得する
    function selectQuerySemester()
    {
        $query = "SELECT ";
        $query .= "  YEAR, ";
        $query .= "  SEMESTER, ";
        $query .= "  SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "  SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "  SEMESTER <= (SELECT ";
        $query .= "                SEMESTERDIV ";
        $query .= "              FROM ";
        $query .= "                SCHOOL_MST ";
        $query .= "              WHERE ";
        $query .= "                YEAR = '".CTRL_YEAR."' ";
        $query .= "              ) ";

        return $query;
    }
    //受持ちの講座を取得
    function selectQueryChair($model){
        $query = "";

        $query .= "SELECT DISTINCT ";
        $query .= "  T1.CHAIRCD, ";
        $query .= "  T1.SUBCLASSCD, ";
        $query .= "  T1.GROUPCD, ";
        $query .= "  T1.CHAIRNAME, ";
        $query .= "  CASE T2.CHARGEDIV WHEN 1 THEN '*' END CHARGEDIV, ";
        $query .= "  T3.SDATE, ";
        $query .= "  T3.EDATE, ";
        $query .= "  CASE WHEN DATE('".str_replace("/", "-", CTRL_DATE)."') BETWEEN T3.SDATE AND T3.EDATE THEN 1 ELSE 0 END DATE_FLG, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T1.TRGTCOUNT, ";
        $query .= "  T1.TRGTGRADE,";
        $query .= "  T1.TRGTCLASS,";
        $query .= "  T5.STAFFCD,";
        if ($model->appd == "1") {
            $query .= " T4.appdate,";
        }
        $query .= "  T5.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "(SELECT TT1.year,TT1.semester,TT1.chaircd,TT1.trgtgrade,TT1.trgtclass,TT1.trgtcount,
                           TT2.hr_nameabbv, TT3.groupcd,TT3.chairname,TT3.subclasscd
                       FROM (SELECT WK1.year, WK1.semester, WK1.chaircd, 
                                   SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade,
                                   SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,3) AS trgtclass,
                                    COUNT(WK1.trgtgrade || WK1.trgtclass) AS trgtcount
                              FROM (SELECT  k1.year, k1.semester, k2.chaircd, 
                                            k1.trgtgrade, k1.trgtclass 
                                      FROM chair_cls_dat k1, chair_dat k2
                                     WHERE k1.year       = k2.year
                                       AND  k1.semester  = k2.semester
                                       AND k1.chaircd    = '0000000'
                                       AND k1.year       = '".CTRL_YEAR."'
                                       AND k1.semester   = '".$model->semester."'
                                       AND k1.groupcd    = k2.groupcd
                                       AND k2.subclasscd = '".$model->subclasscd."' 
                                     UNION
                                    SELECT t1.year, t1.semester, t2.chaircd, 
                                            t1.trgtgrade, t1.trgtclass
                                      FROM chair_cls_dat t1, chair_dat t2
                                     WHERE t1.year       = t2.year
                                       AND t1.semester   = t2.semester
                                       AND t1.groupcd    = t2.groupcd
                                       AND t1.year       = '".CTRL_YEAR."'
                                       AND t1.semester   = '".$model->semester."'
                                       AND t1.chaircd    = t2.chaircd
                                       AND t2.subclasscd = '".$model->subclasscd."' ) WK1 
                             GROUP BY WK1.year, WK1.semester, WK1.chaircd 
                             ) TT1,
                             schreg_regd_hdat TT2,
                             chair_dat        TT3
                     WHERE TT1.year      = TT2.year
                       AND TT1.semester  = TT2.semester
                       AND TT1.trgtgrade = TT2.grade
                       AND TT1.trgtclass = TT2.hr_class
                       AND TT1.year      = TT3.year
                       AND TT1.semester  = TT3.semester
                       AND TT1.chaircd   = TT3.chaircd
                  ) T1,";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     (SELECT SST1.YEAR,";
        $query .= "             SST1.CHAIRCD,";
        $query .= "             MIN(EXECUTEDATE) SDATE,";
        $query .= "             MAX(EXECUTEDATE) EDATE";
        $query .= "        FROM SCH_CHR_DAT SST1,";
        $query .= "            (SELECT SDATE,";
        $query .= "                    EDATE";
        $query .= "               FROM SEMESTER_MST";
        $query .= "              WHERE YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "                    SEMESTER = '" .$model->semester ."'";
        $query .= "            ) SST2";
        $query .= "       WHERE SST1.PERIODCD > '0' AND";
        $query .= "             SST1.EXECUTEDATE BETWEEN SST2.SDATE AND";
        $query .= "             SST2.EDATE";
        $query .= "     GROUP BY SST1.CHAIRCD,";
        $query .= "              SST1.YEAR";
        $query .= "     ) T3, ";
        if ($model->appd == "1") {
            $query .= "   chair_std_dat T4,";
        }
        $query .= "       STAFF_MST T5 ";
        $query .= " WHERE T1.YEAR     = T2.YEAR AND ";
        $query .= "       T1.YEAR     = T3.YEAR AND ";
        $query .= "       T1.SEMESTER = T2.SEMESTER AND ";
        if (is_array($model->chaircd) && $model->cmd == "left"){
            $query .= "  T1.CHAIRCD IN ('" .implode($model->chaircd, "','") ."') AND";
        }else if (!is_array($model->chaircd) && $model->cmd == "left"){
            $query .= "  T1.CHAIRCD IS NULL AND";
        }else if ($model->disp == "TEST" && $model->cmd == "main"){
            $query .= "  T1.CHAIRCD IN (SELECT ";
            $query .= "     CHAIRCD ";
            $query .= " FROM ";
            $query .= "     TESTSCORE_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '" .CTRL_YEAR ."' AND ";
            $query .= "     SEMESTER   = '" .$model->semester ."' AND ";
            $query .= "     TESTKINDCD = '" .$model->testkindcd ."' AND ";
            $query .= "     TESTITEMCD = '" .$model->testitemcd ."' ";
            $query .= ") AND ";
        }
        $query .= "  T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "  T1.CHAIRCD = T3.CHAIRCD AND ";
        if ($model->appd == "1") {
            $query .= "  T1.year     = T4.year AND
                         T1.semester = T4.semester AND
                         T1.chaircd  = T4.chaircd AND ";
        }
        if (is_array($model->appdate) && $model->appd == "1") { //正確に絞るために講座ｺｰﾄﾞと開始日付をくっ付ける
            $query .= "  T1.chaircd || '-' || CHAR(T4.appdate) in ('".implode($model->appdate,"','")."') AND";
        }
        
        $query .= "  T2.STAFFCD = T5.STAFFCD ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT){ //更新、参照可能（制限付）
            $query .= "  AND  T2.STAFFCD = '" .STAFFCD ."'";
        }elseif ($model->auth == DEF_NOAUTH){                                           //権限なし
            $query .= "  AND  T2.STAFFCD IS NULL";
        }
/*        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_REFER_RESTRICT){ //参照可能
            $query .= "  AND  T2.STAFFCD = '" .STAFFCD ."'";
        }elseif ($model->auth == DEF_NOAUTH){     //権限なし
            $query .= "  AND  T2.STAFFCD IS NULL";
        }
*/        $query .= "ORDER BY ";
        $query .= "    T1.TRGTGRADE,";
        $query .= "    T1.TRGTCLASS";

        //echo $query;
        return $query;
    }
    //対象クラスを求める
    function selectQueryChairClsDat($chaircd, $semester, $subclasscd)
    {
        $query = "SELECT DISTINCT ";
        $query .= "    T1.TRGTGRADE, ";
        $query .= "    T1.TRGTCLASS, ";
        $query .= "    T2.HR_NAMEABBV ";
        $query .= "FROM ";
        $query .= "    (SELECT  k1.year, k1.semester, k2.chaircd, 
                                            k1.trgtgrade, k1.trgtclass 
                                      FROM chair_cls_dat k1, chair_dat k2
                                     WHERE k1.year       = k2.year
                                       AND  k1.semester  = k2.semester
                                       AND k1.chaircd    = '0000000'
                                       AND k1.year       = '".CTRL_YEAR."'
                                       AND k1.semester   = '".$semester."'
                                       AND k1.groupcd    = k2.groupcd
                                       AND k2.subclasscd = '".$subclasscd."' 
                                     UNION
                                    SELECT t1.year, t1.semester, t2.chaircd, 
                                            t1.trgtgrade, t1.trgtclass
                                      FROM chair_cls_dat t1, chair_dat t2
                                     WHERE t1.year       = t2.year
                                       AND t1.semester   = t2.semester
                                       AND t1.groupcd    = t2.groupcd
                                       AND t1.year       = '".CTRL_YEAR."'
                                       AND t1.semester   = '".$semester."'
                                       AND t1.chaircd    = t2.chaircd
                                       AND t2.subclasscd = '".$subclasscd."' ) T1, ";
        $query .= "    SCHREG_REGD_HDAT T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "    T1.SEMESTER  = '" .$semester ."' AND ";
        $query .= "    T1.YEAR      = T2.YEAR AND ";
        $query .= "    T1.SEMESTER  = T2.SEMESTER AND ";
        $query .= "    T1.TRGTGRADE = T2.GRADE AND ";
        $query .= "    T1.TRGTCLASS = T2.HR_CLASS AND ";
        $query .= "    T1.CHAIRCD   = '" .$chaircd ."' ";

        return $query;
    }

}
?>