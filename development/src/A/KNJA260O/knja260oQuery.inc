<?php

require_once('for_php7.php');

class knja260oQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA260O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学期取得
    function getSelectSeme($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst where year='" .$year ."' order by semester";

        return $query;
    }

    //最終学期取得
    function getSelectSemester($year)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT MAX(semester) AS GAKKI_MAX FROM schreg_regd_hdat where year='" .$year ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //年度取得
    function getSelectYear()
    {
        $query = "SELECT distinct year FROM schreg_regd_hdat ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .$model->field["YEAR"] ."' ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "AND SEMESTER='".$model->field["GAKKI"] ."'";
        }else {
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //科目取得
    function getSelectSubClass1($model, $semester, $div = "")
    {
        $query  = "SELECT DISTINCT ";
        if ($div == "DATE") {
            $query .= "    t1.SCHREGNO ";
        } else {
            $query .= "    t1.subclasscd, ";
            $query .= "    t2.subclassname ";
        }
        $query .= "FROM ";
        $query .= "    RECORD_DAT t1, ";
        $query .= "    subclass_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" .$model->field["YEAR"] ."' AND ";
        $query .= "    t1.subclasscd=t2.subclasscd AND ";
        $query .= "    t1.schregno IN (SELECT ";
        $query .= "                        schregno ";
        $query .= "                    FROM ";
        $query .= "                        schreg_regd_dat ";
        $query .= "                    WHERE ";
        $query .= "                        year='" .$model->field["YEAR"] ."' AND ";
        if ($semester != "9"){
            $query .= "                        semester='" .$semester ."' AND ";
        }else {
            $query .= "                        semester='" .CTRL_SEMESTER."' AND ";
        }
        $query .= "                        grade || hr_class IN ('".implode($model->select_data["selectdata"],"','")."') ";
        $query .= "                    ) ";
        if ($div == "") {
            $query .= "ORDER BY ";
            $query .= "    t1.subclasscd ";
        }

        return $query;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ";
        $query .= "    MONTH, ";
        $query .= "    MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM ";
        $query .= "    ATTEND_SEMES_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR='".$model->field["YEAR"]."' ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "    AND SEMESTER = '".$model->field["GAKKI"]."' ";
        }else {
            $query .= "    AND SEMESTER <= '".CTRL_SEMESTER."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    SEMESTER, ";
        $query .= "    MONTH ";
        $query .= "ORDER BY ";
        $query .= "    2, ";
        $query .= "    3 ";

        return $query;
    }

    //CVS作成用のQUERY（テスト毎）
    function selectCsvQuery1($model, $testdi, $absent, $semester, $month, $attend_sdate)
    {
        //科目コードのIN文作成
        $sep = "";
        $subclasscdInState = "";
        for ($i = 0; $i < get_count($model->opt_subclass); $i++) {
            $subclasscdInState .= $sep."'".$model->opt_subclass[$i]."'";
            $sep = ",";
        }
        if ($subclasscdInState == "") {
            $subclasscdInState = "''";
        }

        //対象生徒
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' AND ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "    SEMESTER = '".$model->field["GAKKI"]."' AND ";
        }else {
            $query .= "    SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "    GRADE || HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ";
        $query .= ") ";

        //出欠データ
        $query .= knja260oQuery::getAttendData($model, $semester, $month, $attend_sdate, $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"]);
        $query .= ", ATTEND_T AS ( ";
        $query .= knja260oQuery::GetAttendData2($absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"]);
        $query .= ") ";

        //メインデータ
        $query .= "SELECT ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show,'".$testdi."' ";
        if ($model->field["GAKKI"]=="1"){
            if ($model->field["TESTKINDCD"] == "01"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_INTR_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_INTR_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM1_INTR_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "03"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_TERM_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "04"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_TERM_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM1_TERM_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "05"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM1_TERM2_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "06"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_TERM2_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM1_TERM2_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM1_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM1_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }
        }else if ($model->field["GAKKI"]=="2"){
            if ($model->field["TESTKINDCD"] == "01"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_INTR_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_INTR_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM2_INTR_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "03"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_TERM_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "04"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_TERM_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM2_TERM_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "05"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM2_TERM2_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "06"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_TERM2_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM2_TERM2_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM2_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM2_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }
        }else if ($model->field["GAKKI"]=="3"){
            if ($model->field["TESTKINDCD"] == "01"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM3_INTR_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "02"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM3_INTR_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(99999) ELSE m1.SEM3_INTR_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "03"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN m1.SEM3_TERM_SCORE ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else if ($model->field["TESTKINDCD"] == "04"){
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM3_TERM_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM3_TERM_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }else {
                for($i=0;$i<get_count($model->opt_subclass);$i++){
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.SEM3_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.SEM3_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                    $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
                }
            }
        } else {
            for($i=0;$i<get_count($model->opt_subclass);$i++){
                $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN CASE m1.GRAD_VALUE_DI WHEN '=' THEN int(8888) WHEN '-' THEN int(9999) ELSE m1.GRAD_VALUE END ELSE NULL END ) as tokuten" .$i ." ";
                $query .= "	,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
            }
        }
        $query .= "FROM ";
        $query .= "    RECORD_DAT m1 ";
        $query .= "    LEFT JOIN ATTEND_T L1 ON L1.SCHREGNO = m1.SCHREGNO ";
        $query .= "         AND L1.SUBCLASSCD = m1.SUBCLASSCD, ";
        $query .= "    SCH_T m2, ";
        $query .= "    schreg_base_mst m3 ";
        $query .= "WHERE ";
        $query .= "    m2.year = m1.year AND ";
        $query .= "    m2.schregno = m1.schregno AND ";
        $query .= "    m3.schregno = m2.schregno ";
        $query .= "GROUP BY ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show ";
        $query .= "ORDER BY ";
        $query .= "    m2.grade,m2.hr_class,m2.attendno ";

        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov()
    {
        return "SELECT ABSENT_COV, ABSENT_COV_LATE FROM SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."'";
    }

    //端数処理
    function getAttendData($model, $semester, $month, $attend_sdate, $absent_cov, $absent_cov_late)
    {
        $query  = ",ATTEND_SUBCLASS AS ( ";
        $query .= "    SELECT * ";
        $query .= "    FROM   ATTEND_SUBCLASS_DAT ";
        $query .= "    WHERE  YEAR='".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER <= '".$semester."' ";
        $query .= "      AND  MONTH IN ('" .implode($month, "','") ."') ";
        $query .= "    ) ";
        $query .= ",SCHEDULE AS( ";
        $query .= "    SELECT EXECUTEDATE, PERIODCD, CHAIRCD  ";
        $query .= "    FROM   SCH_CHR_DAT  ";
        $query .= "    WHERE  EXECUTEDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "      AND  PERIODCD != '0'  ";
        $query .= "    GROUP BY EXECUTEDATE, PERIODCD, CHAIRCD  ";
        $query .= "    ) ";
        $query .= ",T_attend_dat AS( ";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD,W1.DI_CD  ";
        $query .= "     FROM   ATTEND_DAT W1  ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ";
        $query .= "                           WHERE  T7.SCHREGNO = W1.SCHREGNO  ";
        $query .= "                             AND  T7.TRANSFERCD IN('1','2')  ";
        $query .= "                             AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ";
        $query .= "    ) ";
        $query .= ",T_attend AS( ";
        $query .= "SELECT S1.SCHREGNO  ";
        $query .= "      ,S1.SUBCLASSCD  ";
        $query .= "      ,S1.SEMESTER  ";
        $query .= "      ,COUNT(*)  AS LESSON  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '15' THEN 1 ELSE 0 END)  AS  LATE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ";
        $query .= "FROM  ";
        $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD, T2.SEMESTER ";
        $query .= "  FROM   SCHEDULE T1  ";
        $query .= "        ,CHAIR_STD_DAT T2  ";
        $query .= "        ,CHAIR_DAT T3  ";
        $query .= "        ,SCH_T T4  ";
        $query .= "  WHERE  T1.CHAIRCD  = T3.CHAIRCD  ";
        $query .= "    AND  T3.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "    AND  T2.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T2.SEMESTER = T3.SEMESTER  ";
        $query .= "    AND  T2.CHAIRCD  = T1.CHAIRCD  ";
        $query .= "    AND  T4.SCHREGNO = T2.SCHREGNO  ";
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ";
        $query .= "                    WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ";
        $query .= "                            T5.PERIODCD = T1.PERIODCD AND  ";
        $query .= "                            T5.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "                            T5.GRADE = T4.GRADE AND  ";
        $query .= "                            T5.HR_CLASS = T4.HR_CLASS AND  ";
        $query .= "                            T5.COUNTFLG = '0') ";
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ";
        $query .= "                    WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ";
        $query .= "                          ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                           (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ";
        $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD, T2.SEMESTER)S1  ";
        $query .= " LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";
        $query .= "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ";
        $query .= "    ) ";
        $query .= ",ATTEND_SUM AS( ";
        $query .= "    SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ";
        $query .= "           SUM(W1.LESSON) LESSON, ";
        $query .= "           SUM(W1.ABSENT) ABSENT, ";
        $query .= "           SUM(W1.SUSPEND) SUSPEND, ";
        $query .= "           SUM(W1.MOURNING) MOURNING, ";
        $query .= "           SUM(W1.SICK) SICK, ";
        $query .= "           SUM(W1.NOTICE) NOTICE, ";
        $query .= "           SUM(W1.NONOTICE) NONOTICE, ";
        $query .= "           SUM(W1.NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(W1.LATE) LATE, ";
        $query .= "           SUM(W1.EARLY) EARLY ";
        $query .= "    FROM   ATTEND_SUBCLASS W1, SCH_T W0 ";
        $query .= "    WHERE  W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "    GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "    SELECT W2.SCHREGNO,W2.SUBCLASSCD,W2.SEMESTER, ";
        $query .= "           W2.LESSON, ";
        $query .= "           W2.ABSENT, ";
        $query .= "           W2.SUSPEND, ";
        $query .= "           W2.MOURNING, ";
        $query .= "           W2.SICK, ";
        $query .= "           W2.NOTICE, ";
        $query .= "           W2.NONOTICE, ";
        $query .= "           W2.NURSEOFF, ";
        $query .= "           W2.LATE, ";
        $query .= "           W2.EARLY ";
        $query .= "     FROM   T_attend W2 ";
        $query .= "    ) ";
        //学期毎に清算
        $query .= ",ATTEND_SUM2 AS ( ";
        $query .= "    SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ";
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        $query .= "           SUM(SUSPEND) SUSPEND, ";
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK)),4,1) as NOTICE_LATE ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK)) as NOTICE_LATE ";
        } else {
            $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) as NOTICE_LATE ";
        }
        $query .= "    FROM   ATTEND_SUM ";
        $query .= "    GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ";
        $query .= "    ) ";
        //年間で清算
        $query .= ",ATTEND_SUM3 AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        $query .= "        SUBCLASSCD, ";
        $query .= "        SUM(NOTICE_LATE) NOTICE_LATE1 ";
        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK)),4,1) as NOTICE_LATE2 ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK)) as NOTICE_LATE2 ";
        } else {
            $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) as NOTICE_LATE2 ";
        }
        $query .= "    FROM ";
        $query .= "        ATTEND_SUM2 ";
        $query .= "    GROUP BY ";
        $query .= "        SCHREGNO,SUBCLASSCD ";
        $query .= "    ) ";

        return $query;
    }

    //出欠メイン
    function GetAttendData2($absent_cov, $absent_cov_late)
    {
        $query .= "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T2.SUBCLASSCD, ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "    NOTICE_LATE1 AS NOTICE_LATE ";
        } else {
            $query .= "    NOTICE_LATE2 AS NOTICE_LATE ";
        }
        $query .= "FROM ";
        $query .= "    SCH_T T1, ";
        $query .= "    ATTEND_SUM3 T2 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }
}
?>
