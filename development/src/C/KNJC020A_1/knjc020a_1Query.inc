<?php

require_once('for_php7.php');

class knjc020a_1Query extends Query
{
    //Chair情報取得
    function getTargetChair($model)
    {
        $query = "SELECT
                    T1.chaircd, T2.chairname
                  FROM (SELECT t1.chaircd
                          FROM sch_chr_dat t1, chair_stf_dat t2
                         WHERE t1.executedate = DATE('".$model->executedate."')
                         AND   t1.periodcd  = '".$model->priod."'
                         AND   t2.year = '".CTRL_YEAR."'
                         AND   t2.semester = '".$model->semester."'
                         AND   t2.staffcd = '".$model->t_Staffcd."'
                         AND   t1.chaircd = t2.chaircd ) T1
                    LEFT JOIN (SELECT t1.chaircd, t1.chairname
                                 FROM chair_dat t1
                                 WHERE t1.year = '".CTRL_YEAR."'
                                 AND   t1.semester = '".$model->semester."')T2
                           ON T1.chaircd = T2.chaircd
                  UNION
                  SELECT
                    T1.chaircd, T2.chairname
                  FROM (SELECT t1.chaircd
                          FROM SCH_STF_DAT t1
                         WHERE t1.executedate = DATE('".$model->executedate."')
                         AND   t1.periodcd  = '".$model->priod."'
                         AND   t1.staffcd = '".$model->t_Staffcd."') T1
                    LEFT JOIN (SELECT t1.chaircd, t1.chairname
                                 FROM chair_dat t1
                                 WHERE t1.year = '".CTRL_YEAR."'
                                 AND   t1.semester = '".$model->semester."')T2
                           ON T1.chaircd = T2.chaircd
                  ORDER BY chaircd, chairname ";
        return $query;
    }

    //時間割情報取得
    function getTimeTable($model)
    {

        //テスト項目マスタの集計フラグの表
        $query  = " WITH TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG "; //0：集計しない 0以外：集計する
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.EXECUTEDATE = DATE('".$model->executedate."') ";
        $query .= "         AND T2.YEAR       = T1.YEAR ";
        if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "         AND T2.SCORE_DIV  = '01' ";
        } else if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        } else {
        }
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG ";
        $query .= " ) ";

        $query .=" SELECT
                    T1.PERIODCD,T1.CHAIRCD,T1.EXECUTED,T1.ATTESTOR,
                    T3.CHAIRNAME,T4.STAFFCD,T4.STAFFNAME_SHOW,T5.HR_NAMEABBV AS TARGETCLASS, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          T6.CLASSCD, T6.SCHOOL_KIND, T6.CURRICULUM_CD, ";
        }
        $query .=" T6.SUBCLASSCD,T6.SUBCLASSNAME,T6.SUBCLASSABBV,T7.FACCD,T8.FACILITYNAME,
                    T9.STAFFNAME_SHOW AS ATTESTOR_NAME,
                    T3.TRGTGRADE, T3.TRGTCLASS,
                    CASE WHEN T1.DATADIV='2' THEN TEST.COUNTFLG ELSE T10.COUNTFLG END AS COUNTFLG,
                    VALUE(N1.ABBV1, '　') || CASE WHEN N1.ABBV1 IS NOT NULL
                                                  THEN ':'
                                                  ELSE ''
                                             END
                    AS LESSON_NAME
                FROM
                    SCH_CHR_DAT T1 LEFT JOIN chair_stf_dat T2 ON T1.year = T2.year
                                                             AND T1.semester = T2.semester
                                                             AND T1.chaircd  = T2.chaircd
                                   LEFT JOIN 
                                            (SELECT k1.year, k1.semester, k2.chaircd, k2.chairname, ";
                   if ($model->Properties["useCurriculumcd"] == "1") {
                       $query .= "          k2.CLASSCD, k2.SCHOOL_KIND, k2.CURRICULUM_CD, ";
                   }
                   $query .= "              k2.subclasscd, k2.groupcd, k1.trgtgrade, k1.trgtclass
                                               FROM chair_cls_dat k1, chair_dat k2
                                              WHERE k1.year       = k2.year
                                                AND k1.semester   = k2.semester
                                                AND k1.chaircd    = '0000000'
                                                AND k1.year       = '".CTRL_YEAR."'
                                                AND k1.semester   = '".$model->semester."'
                                                AND k1.groupcd    = k2.groupcd
                                            UNION
                                             SELECT t1.year, t1.semester, t2.chaircd, t2.chairname, ";
                   if ($model->Properties["useCurriculumcd"] == "1") {
                       $query .= "           t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ";
                   }
                   $query .= "               t2.subclasscd, t2.groupcd, t1.trgtgrade, t1.trgtclass
                                               FROM chair_cls_dat t1, chair_dat t2
                                              WHERE t1.year       = t2.year
                                                AND t1.semester   = t2.semester
                                                AND t1.groupcd    = t2.groupcd
                                                AND t1.year       = '".CTRL_YEAR."'
                                                AND t1.semester   = '".$model->semester."'
                                                AND t1.chaircd    = t2.chaircd ) T3
                                                              ON T1.year     = T3.year
                                                             AND T1.semester = T3.semester
                                                             AND T1.chaircd  = T3.chaircd
                                   LEFT JOIN v_staff_mst T4   ON T1.year     = T4.year
                                                             AND T2.staffcd  = T4.staffcd
                                   LEFT JOIN schreg_regd_hdat T5 ON T1.year      = T5.year
                                                                AND T1.semester  = T5.semester
                                                                AND T3.trgtgrade = T5.grade
                                                                AND T3.trgtclass = T5.hr_class
                                   LEFT JOIN subclass_mst T6  ON T3.subclasscd   = T6.subclasscd ";
                                     if ($model->Properties["useCurriculumcd"] == "1") {
                                         $query .= "          AND T3.CLASSCD = T6.CLASSCD ";
                                         $query .= "          AND T3.SCHOOL_KIND = T6.SCHOOL_KIND ";
                                         $query .= "          AND T3.CURRICULUM_CD = T6.CURRICULUM_CD ";
                                     }
        $query .="                 LEFT JOIN sch_fac_dat T7   ON T1.year        = T7.year
                                                             AND T1.executedate = T7.executedate
                                                             AND T1.chaircd     = T7.chaircd
                                   LEFT JOIN facility_mst T8  ON T7.faccd    = T8.faccd
                                   LEFT JOIN v_staff_mst T9   ON T1.year     = T9.year
                                                             AND T1.attestor = T9.staffcd
                                   LEFT JOIN SCH_CHR_COUNTFLG T10 ON T1.EXECUTEDATE = T10.EXECUTEDATE
                                        AND T1.PERIODCD = T10.PERIODCD
                                        AND T1.CHAIRCD = T10.CHAIRCD
                                        AND T3.TRGTGRADE = T10.GRADE
                                        AND T3.TRGTCLASS = T10.HR_CLASS
                                   LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B002'
                                        AND T10.LESSON_MODE = N1.NAMECD2
                                   LEFT JOIN TEST_COUNTFLG TEST
                                       ON  TEST.EXECUTEDATE = T1.EXECUTEDATE
                                       AND TEST.PERIODCD    = T1.PERIODCD
                                       AND TEST.CHAIRCD     = T1.CHAIRCD
                WHERE
                        T1.year = '".CTRL_YEAR."'
                    AND T1.executedate = DATE('".$model->executedate."')
                    AND T1.semester  = '".$model->semester."'
                    AND T1.periodcd  = '".$model->priod."'
                ORDER BY T1.periodcd,TARGETCLASS ";
        return $query;
    }

    //学期取得
    function getTerm($day){
        return " VALUES TERM_GET('".CTRL_YEAR."',DATE('".$day."'),'N') ";
    }

    //テスト時間割判定
    function checkSchTest($model){
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE ='".$model->executedate."' AND ";
        $query .= "     PERIODCD    ='".$model->priod."' AND ";
        $query .= "     CHAIRCD     ='".$model->chaircd."' AND ";
        $query .= "     DATADIV     = '2' ";

        return $query;
    }

    //テスト集計フラグ取得
    function getTestCountflg($model){

        $query  = " SELECT ";
        $query .= "     T2.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ".$model->Properties["useTestCountflg"]." T2 ";
        $query .= "     ON  T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.TESTKINDCD = T2.TESTKINDCD ";
        $query .= "     AND T1.TESTITEMCD = T2.TESTITEMCD ";
        if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        } else if($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "     AND T2.SCORE_DIV = '01' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE  = '".$model->executedate."' AND ";
        $query .= "     T1.PERIODCD     = '".$model->priod."' AND ";
        $query .= "     T1.CHAIRCD      = '".$model->chaircd."' ";

        return $query;
    }

    //アップデート
    function &getUpdateSchChrQuery(&$model)
    {
        $ary = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if($model->cmd == "execute"){
            $execute = "1" ;
        }else{
            $execute = ($model->executed_flg)?  "0" : "1" ;
        }
        $data["ATTESTOR"][TEXT] = STAFFCD ;
        $data["EXECUTED"][TEXT] = $execute;
        $data["UPDATED"][FUNC] = "sysdate()";

        $where  = " WHERE EXECUTEDATE = DATE('";
        $where .=  str_replace("/","-",$model->executedate) ;
        $where .= "') AND PERIODCD = '";
        $where .=  $model->priod ;
        $where .= "'  AND YEAR = '";
        $where .=  CTRL_YEAR ;
        $where .= "'  AND CHAIRCD = '";
        $where .=  $model->chaircd;
        $where .= "' ";

        $query = Query::updateSQL($data, "SCH_CHR_DAT", $where);
        $db->query($query);

        if($execute == "0"){
            knjc020a_1Query::delete_attenddata($model, $db);
        }

        $row = $db->getRow(knjc020a_1Query::getGroupCd($model), DB_FETCHMODE_ASSOC);

        knjc020a_1Query::deleteInsHrateDat($model, $db, $row, "DEL");
        if($execute == "1"){
            knjc020a_1Query::deleteInsHrateDat($model, $db, $row, "INS");
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //入力取消前処理 情報を削除
    function delete_attenddata(&$model, $db)
    {

        $query  = " DELETE FROM ATTEND_DAT ";
        $query .= " WHERE ATTENDDATE = DATE('".$model->executedate."') ";
        $query .= " AND PERIODCD   =  '".$model->priod."' ";
        $query .= " AND SCHREGNO IN (";
        $query .= " SELECT SCHREGNO FROM CHAIR_STD_DAT ";
        $query .= " WHERE YEAR  =  '".CTRL_YEAR."' ";
        $query .= " AND CHAIRCD = '".$model->chaircd."') ";

        $results = $db->query($query);
        return $results;
    }

    //更新用郡コード取得SQL
    function getGroupCd($model)
    {
        $query  = "WITH SCH_CHR AS ( ";
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_DAT ";
        $query .= "WHERE ";
        $query .= "    EXECUTEDATE = DATE('".str_replace("/","-",$model->executedate)."') ";
        $query .= "    AND PERIODCD = '".$model->priod."' ";
        $query .= "    AND CHAIRCD = '".$model->chaircd."' ";
        $query .= "    AND YEAR = '".CTRL_YEAR."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    T2.EXECUTEDATE, ";
        $query .= "    T2.PERIODCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.SEMESTER, ";
        $query .= "    T1.GROUPCD, ";
        $query .= "    T1.CHAIRCD ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT T1, ";
        $query .= "    SCH_CHR T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";

        return $query;
    }

    //削除、追加処理
    function deleteInsHrateDat($model, $db, $row, $mode)
    {
        $db->autoCommit(false);

        $result = $db->query(knjc020a_1Query::getChrClass($row));
        while ($delData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($mode == "DEL") {
                $query  = "DELETE FROM SCH_CHR_HRATE_DAT ";
                $query .= "WHERE ";
                $query .= "    EXECUTEDATE = '".$row["EXECUTEDATE"]."' ";
                $query .= "    AND PERIODCD = '".$row["PERIODCD"]."' ";
                $query .= "    AND CHAIRCD = '".$row["CHAIRCD"]."' ";
            } else {
                $data["EXECUTEDATE"][DATE] = $row["EXECUTEDATE"];   //出席確認者
                $data["PERIODCD"][TEXT]    = $row["PERIODCD"];      //処理学期
                $data["CHAIRCD"][TEXT]     = $row["CHAIRCD"];       //処理学期
                $data["GRADE"][TEXT]       = $delData["TRGTGRADE"]; //処理学期
                $data["HR_CLASS"][TEXT]    = $delData["TRGTCLASS"]; //処理学期
                $data["EXECUTED"][TEXT]    = "1" ;                  //実施フラグ
                $data["ATTESTOR"][TEXT]    = STAFFCD ;              //出席確認者
                $data["REGISTERCD"][TEXT]  = STAFFCD ;              //更新者
                $data["UPDATED"][FUNC]     = "sysdate()";           //更新日付
                $query = Query::insertSQL($data, "SCH_CHR_HRATE_DAT");
            }
            $db->query($query);
            $db->commit();
        }
        $result->free();

    }

    //更新用年組コード取得SQL
    function getChrClass($row)
    {
        $query  = "SELECT ";
        $query .= "    TRGTGRADE, ";
        $query .= "    TRGTCLASS ";
        $query .= "FROM ";
        $query .= "    CHAIR_CLS_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$row["YEAR"]."' ";
        $query .= "    AND SEMESTER = '".$row["SEMESTER"]."' ";
        if ($row["GROUPCD"] > 0) {
            $query .= "    AND GROUPCD = '".$row["GROUPCD"]."' ";
        } else {
            $query .= "    AND CHAIRCD = '".$row["CHAIRCD"]."' ";
        }

        return $query;
    }

    //DELETE前にデータを取得
    function &getSchChrCountflgQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_COUNTFLG ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".$model->executedate."' ";
        $query .= "     AND PERIODCD = '".$model->priod."' ";
        $query .= "     AND CHAIRCD = '".$model->chaircd."' ";

        return $query;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $query  = "DELETE FROM SCH_CHR_COUNTFLG ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".$model->executedate."' ";
        $query .= "     AND PERIODCD = '".$model->priod."' ";
        $query .= "     AND CHAIRCD = '".$model->chaircd."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $grade, $hrClass, $lessonMode)
    {

        $data = array();
        $data["EXECUTEDATE"][DATE] = $model->executedate;
        $data["PERIODCD"][TEXT] = $model->priod;
        $data["CHAIRCD"][TEXT] = $model->chaircd;
        $data["GRADE"][TEXT] = $grade;
        $data["HR_CLASS"][TEXT] = $hrClass;
        $data["COUNTFLG"][TEXT] = "0";
        $data["LESSON_MODE"][TEXT] = $lessonMode ? $lessonMode : "00";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "sysdate()";

        $query = Query::insertSQL($data, "SCH_CHR_COUNTFLG");

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model, $gradeHr)
    {
        $data = array();
        $data["COUNTFLG"][TEXT] = "1";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "sysdate()";

        $where  = " WHERE ";
        $where .= "     EXECUTEDATE = '".$model->executedate."' ";
        $where .= "     AND PERIODCD = '".$model->priod."' ";
        $where .= "     AND CHAIRCD = '".$model->chaircd."' ";
        $where .= "     AND GRADE || HR_CLASS = '".$gradeHr."' ";

        $query = Query::updateSQL($data, "SCH_CHR_COUNTFLG", $where);

        return $query;
    }

    //入力取消前に情報をチェック
    function chk_attenddata(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) AS COUNT FROM ATTEND_DAT ";
        $query .= " WHERE ATTENDDATE = DATE('".$model->executedate."') ";
        $query .= " AND PERIODCD   =  '".$model->priod."' ";
        $query .= " AND SCHREGNO IN (";
        $query .= " SELECT SCHREGNO FROM CHAIR_STD_DAT ";
        $query .= " WHERE YEAR  =  '".CTRL_YEAR."' ";
        $query .= " AND CHAIRCD = '".$model->chaircd."') ";

        return $query;
    }
}
?>
