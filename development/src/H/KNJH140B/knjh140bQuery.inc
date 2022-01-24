<?php

require_once('for_php7.php');
class knjh140bQuery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJH140B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from schreg_regd_hdat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct grade,hr_class,hr_name ";
        $query .= "from schreg_regd_hdat T1 ";
        $query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= "order by grade,hr_class";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " WITH REGD_DAT AS ( ";
        $query .= "     SELECT T1.YEAR AS REGYEAR,T1.SCHREGNO AS REGSCH,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T2.HR_NAME ";
        $query .= "     FROM SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T1.GRADE = T2.GRADE ";
        $query .= "         AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "     WHERE  T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"]) {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["GRD_CHECK"] == "1") {
            $query .= "     AND T1.SCHREGNO NOT IN ( ";
            $query .= "             SELECT  T1.SCHREGNO ";
            $query .= "             FROM    SCHREG_REGD_DAT T1, ";
            $query .= "                     SEMESTER_MST T2 ";
            $query .= "             WHERE   T1.YEAR = T2.YEAR AND ";
            $query .= "                     T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "                     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
            $query .= "                     EXISTS (SELECT 'X' FROM SCHREG_BASE_MST S1 ";
            $query .= "                             WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "                                   (S1.GRD_DIV IN ('1','2','3') AND ";
            $query .= "                                   S1.GRD_DATE <= T2.EDATE))) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ) ";
        if ($model->target == 1) {
            $query .= " SELECT ";
            $query .= "     '1',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ";
            $query .= "     T1.RELANO,T1.RELANAME,T1.RELAKANA,T1.RELASEX,T1.RELABIRTHDAY, ";
            $query .= "     T1.OCCUPATION,T1.REGIDENTIALCD,T1.RELATIONSHIP,T1.RELA_SCHREGNO,T1.REGD_GRD_FLG,T1.RELA_GRADE,T1.REMARK,'".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     REGD_DAT LEFT JOIN SCHREG_RELA_DAT T1 ON REGSCH = T1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
        } elseif ($model->target == 2) {
            $query .= " SELECT ";
            $query .= "     '2',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T1.NAME, ";
            $query .= "     T1.EMERGENCYCALL,T1.EMERGENCYNAME,T1.EMERGENCYRELA_NAME,T1.EMERGENCYTELNO, ";
            $query .= "     T1.EMERGENCYCALL2,T1.EMERGENCYNAME2,T1.EMERGENCYRELA_NAME2,T1.EMERGENCYTELNO2,'".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     REGD_DAT LEFT JOIN SCHREG_BASE_MST T1 ON REGSCH = T1.SCHREGNO ";
        } elseif ($model->target == 3) {
            $query .= " SELECT ";
            $query .= "     '3',REGYEAR,GRADE,HR_CLASS,HR_NAME,ATTENDNO,REGSCH,T2.NAME, ";
            $query .= "     T1.GO_HOME_GROUP_NO,T1.COMMUTE_HOURS,T1.COMMUTE_MINUTES,T1.HOWTOCOMMUTECD, ";
            $query .= "     T1.JOSYA_1,'' AS JOSYANAME_1,T1.ROSEN_1,'' AS ROSENNAME_1,T1.GESYA_1,'' AS GESYANAME_1,T1.FLG_1,T1.JOSYA_2,'' AS JOSYANAME_2,T1.ROSEN_2,";
            $query .= "     VALUE(CASE WHEN T1.HOWTOCOMMUTECD = '1' AND T1.FLG_2 = '3' THEN T3.BUS_NAME END, '') AS ROSENNAME_2,T1.GESYA_2,'' AS GESYANAME_2,T1.FLG_2, ";
            $query .= "     T1.JOSYA_3,'' AS JOSYANAME_3,T1.ROSEN_3,'' AS ROSENNAME_3,T1.GESYA_3,'' AS GESYANAME_3,T1.FLG_3,T1.JOSYA_4,'' AS JOSYANAME_4,T1.ROSEN_4,'' AS ROSENNAME_4,T1.GESYA_4,'' AS GESYANAME_4,T1.FLG_4, ";
            $query .= "     T1.JOSYA_5,'' AS JOSYANAME_5,T1.ROSEN_5,'' AS ROSENNAME_5,T1.GESYA_5,'' AS GESYANAME_5,T1.FLG_5,T1.JOSYA_6,'' AS JOSYANAME_6,T1.ROSEN_6,'' AS ROSENNAME_6,T1.GESYA_6,'' AS GESYANAME_6,T1.FLG_6, ";
            $query .= "     T1.JOSYA_7,'' AS JOSYANAME_7,T1.ROSEN_7,'' AS ROSENNAME_7,T1.GESYA_7,'' AS GESYANAME_7,T1.FLG_7,'".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     REGD_DAT LEFT JOIN SCHREG_ENVIR_DAT T1 ON REGSCH = T1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
            $query .= "     LEFT JOIN CHILDCARE_BUS_YMST T3 ON T3.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
            $query .= "         AND T3.COURSE_CD = T1.ROSEN_2 ";
        } elseif ($model->target == 4) {
            $query .= " SELECT ";
            $query .= "     '4',REGYEAR,REGSCH,GRADE,HR_CLASS,ATTENDNO,T2.NAME,T1.DETAIL_SDATE,T1.DETAIL_EDATE,T1.BICYCLE_CD,T1.BICYCLE_NO,T1.CONTENT,T1.REMARK,'".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     REGD_DAT LEFT JOIN SCHREG_DETAILHIST_DAT T1 ON REGYEAR = T1.YEAR AND REGSCH = T1.SCHREGNO ";
            $query .= "     AND T1.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
            $query .= "     AND T1.DETAIL_DIV = '3' ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
        } elseif ($model->target == 5) {
            $query .= " SELECT ";
            $query .= "     '5',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ";
            $query .= "     T1.SEND_RELATIONSHIP,T1.SEND_NAME,T1.SEND_KANA,T1.SEND_SEX,T1.SEND_ZIPCD,T1.SEND_ADDR1,T1.SEND_ADDR2,T1.SEND_TELNO,T1.SEND_JOBCD,T1.PUBLIC_OFFICE,'".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     REGD_DAT LEFT JOIN SCHREG_SEND_ADDRESS_DAT T1 ON REGSCH = T1.SCHREGNO AND T1.DIV = '1' ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGYEAR,GRADE,HR_CLASS,ATTENDNO ";

        return $query;
    }

    //路線名、駅名取得
    public function getStationNetInfo($name, $cd_name, $cd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ".$name." ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     ".$cd_name." = '".$cd."'";

        return $query;
    }

    //学年一覧取得
    public function getGradeList($year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //実行
    public function insertQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            switch ($model->target) {
                case 1:
                    $table = "SCHREG_RELA_DAT";
                    $query  = " SELECT COUNT(*) FROM ".$table ;
                    $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= " AND RELANO  = '".$data_arr[$i]["RELANO"]."' ";
                    break;
                case 2:
                    $table  = "SCHREG_BASE_MST";
                    $query  = " SELECT COUNT(*) FROM ".$table ;
                    $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    break;
                case 3:
                    $table = "SCHREG_ENVIR_DAT";
                    $query  = " SELECT COUNT(*) FROM ".$table ;
                    $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                    break;
                case 4:
                    $table = "SCHREG_DETAILHIST_DAT";
                    $query  = " SELECT COUNT(*) FROM ".$table ;
                    $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= " AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= " AND DETAIL_DIV  = '3' ";
                    $query .= " AND DETAIL_SDATE    = '".$data_arr[$i]["DETAIL_SDATE"]."' ";
                    break;
                case 5:
                    $table = "SCHREG_SEND_ADDRESS_DAT";
                    $query  = " SELECT COUNT(*) FROM ".$table ;
                    $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND DIV = '1' ";
                    break;
            }
            $tblcnt = $db->getOne($query);
            $where = "";
            $data = array();
            switch ($model->target) {
                case 1:
                    if ($tblcnt > 0) {
                        $data["RELANAME"][TEXT]     = $data_arr[$i]["RELANAME"];
                        $data["RELAKANA"][TEXT]     = $data_arr[$i]["RELAKANA"];
                        $data["RELASEX"][TEXT]      = $data_arr[$i]["RELASEX"];
                        if (!$data_arr[$i]["RELABIRTHDAY"]) {
                            $data["RELABIRTHDAY"][FUNC] = null;
                        } else {
                            $data["RELABIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RELABIRTHDAY"])."')";
                        }
                        $data["OCCUPATION"][TEXT]   = $data_arr[$i]["OCCUPATION"];
                        $data["REGIDENTIALCD"][TEXT]= $data_arr[$i]["REGIDENTIALCD"];
                        $data["RELATIONSHIP"][TEXT] = $data_arr[$i]["RELATIONSHIP"];
                        $data["RELA_SCHREGNO"][TEXT]= $data_arr[$i]["RELA_SCHREGNO"];
                        $data["REGD_GRD_FLG"][TEXT] = $data_arr[$i]["REGD_GRD_FLG"];
                        $data["RELA_GRADE"][TEXT]   = $data_arr[$i]["RELA_GRADE"];
                        $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];
                        $where = "  WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND RELANO = '".$data_arr[$i]["RELANO"]."' ";
                        $insup = "up";
                    } else {
                        $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                        $data["RELANO"][TEXT]       = $data_arr[$i]["RELANO"];
                        $data["RELANAME"][TEXT]     = $data_arr[$i]["RELANAME"];
                        $data["RELAKANA"][TEXT]     = $data_arr[$i]["RELAKANA"];
                        $data["RELASEX"][TEXT]      = $data_arr[$i]["RELASEX"];
                        if (!$data_arr[$i]["RELABIRTHDAY"]) {
                            $data["RELABIRTHDAY"][FUNC] = null;
                        } else {
                            $data["RELABIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RELABIRTHDAY"])."')";
                        }
                        $data["OCCUPATION"][TEXT]   = $data_arr[$i]["OCCUPATION"];
                        $data["REGIDENTIALCD"][TEXT]= $data_arr[$i]["REGIDENTIALCD"];
                        $data["RELATIONSHIP"][TEXT] = $data_arr[$i]["RELATIONSHIP"];
                        $data["RELA_SCHREGNO"][TEXT]= $data_arr[$i]["RELA_SCHREGNO"];
                        $data["REGD_GRD_FLG"][TEXT] = $data_arr[$i]["REGD_GRD_FLG"];
                        $data["RELA_GRADE"][TEXT]   = $data_arr[$i]["RELA_GRADE"];
                        $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];
                        $insup = "ins";
                    }

                    break;
                case 2:
                    if ($tblcnt > 0) {
                        $data["EMERGENCYCALL"][TEXT]        = $data_arr[$i]["EMERGENCYCALL"];
                        $data["EMERGENCYNAME"][TEXT]        = $data_arr[$i]["EMERGENCYNAME"];
                        $data["EMERGENCYRELA_NAME"][TEXT]   = $data_arr[$i]["EMERGENCYRELA_NAME"];
                        $data["EMERGENCYTELNO"][TEXT]       = $data_arr[$i]["EMERGENCYTELNO"];
                        $data["EMERGENCYCALL2"][TEXT]       = $data_arr[$i]["EMERGENCYCALL2"];
                        $data["EMERGENCYNAME2"][TEXT]       = $data_arr[$i]["EMERGENCYNAME2"];
                        $data["EMERGENCYRELA_NAME2"][TEXT]  = $data_arr[$i]["EMERGENCYRELA_NAME2"];
                        $data["EMERGENCYTELNO2"][TEXT]      = $data_arr[$i]["EMERGENCYTELNO2"];
                        $where = "  WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $insup = "up";
                    } else {
                        $insup = "exit";
                    }

                    break;
                case 3:
                    if ($tblcnt > 0) {
                        $data["GO_HOME_GROUP_NO"][TEXT]     = $data_arr[$i]["GO_HOME_GROUP_NO"];
                        $data["COMMUTE_HOURS"][TEXT]        = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_MINUTES"][TEXT]      = $data_arr[$i]["COMMUTE_MINUTES"];
                        $data["HOWTOCOMMUTECD"][TEXT]       = $data_arr[$i]["HOWTOCOMMUTECD"];
                        $data["JOSYA_1"][TEXT]              = $data_arr[$i]["JOSYA_1"];
                        $data["ROSEN_1"][TEXT]              = $data_arr[$i]["ROSEN_1"];
                        $data["GESYA_1"][TEXT]              = $data_arr[$i]["GESYA_1"];
                        $data["FLG_1"][TEXT]                = $data_arr[$i]["FLG_1"];
                        $data["JOSYA_2"][TEXT]              = $data_arr[$i]["JOSYA_2"];
                        $data["ROSEN_2"][TEXT]              = $data_arr[$i]["ROSEN_2"];
                        $data["GESYA_2"][TEXT]              = $data_arr[$i]["GESYA_2"];
                        $data["FLG_2"][TEXT]                = $data_arr[$i]["FLG_2"];
                        $data["JOSYA_3"][TEXT]              = $data_arr[$i]["JOSYA_3"];
                        $data["ROSEN_3"][TEXT]              = $data_arr[$i]["ROSEN_3"];
                        $data["GESYA_3"][TEXT]              = $data_arr[$i]["GESYA_3"];
                        $data["FLG_3"][TEXT]                = $data_arr[$i]["FLG_3"];
                        $data["JOSYA_4"][TEXT]              = $data_arr[$i]["JOSYA_4"];
                        $data["ROSEN_4"][TEXT]              = $data_arr[$i]["ROSEN_4"];
                        $data["GESYA_4"][TEXT]              = $data_arr[$i]["GESYA_4"];
                        $data["FLG_4"][TEXT]                = $data_arr[$i]["FLG_4"];
                        $data["JOSYA_5"][TEXT]              = $data_arr[$i]["JOSYA_5"];
                        $data["ROSEN_5"][TEXT]              = $data_arr[$i]["ROSEN_5"];
                        $data["GESYA_5"][TEXT]              = $data_arr[$i]["GESYA_5"];
                        $data["FLG_5"][TEXT]                = $data_arr[$i]["FLG_5"];
                        $data["JOSYA_6"][TEXT]              = $data_arr[$i]["JOSYA_6"];
                        $data["ROSEN_6"][TEXT]              = $data_arr[$i]["ROSEN_6"];
                        $data["GESYA_6"][TEXT]              = $data_arr[$i]["GESYA_6"];
                        $data["FLG_6"][TEXT]                = $data_arr[$i]["FLG_6"];
                        $data["JOSYA_7"][TEXT]              = $data_arr[$i]["JOSYA_7"];
                        $data["ROSEN_7"][TEXT]              = $data_arr[$i]["ROSEN_7"];
                        $data["GESYA_7"][TEXT]              = $data_arr[$i]["GESYA_7"];
                        $data["FLG_7"][TEXT]                = $data_arr[$i]["FLG_7"];
                        $where = "  WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $insup = "up";
                    } else {
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["GO_HOME_GROUP_NO"][TEXT]     = $data_arr[$i]["GO_HOME_GROUP_NO"];
                        $data["COMMUTE_HOURS"][TEXT]        = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_HOURS"][TEXT]        = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_MINUTES"][TEXT]      = $data_arr[$i]["COMMUTE_MINUTES"];
                        $data["HOWTOCOMMUTECD"][TEXT]       = $data_arr[$i]["HOWTOCOMMUTECD"];
                        $data["JOSYA_1"][TEXT]              = $data_arr[$i]["JOSYA_1"];
                        $data["ROSEN_1"][TEXT]              = $data_arr[$i]["ROSEN_1"];
                        $data["GESYA_1"][TEXT]              = $data_arr[$i]["GESYA_1"];
                        $data["FLG_1"][TEXT]                = $data_arr[$i]["FLG_1"];
                        $data["JOSYA_2"][TEXT]              = $data_arr[$i]["JOSYA_2"];
                        $data["ROSEN_2"][TEXT]              = $data_arr[$i]["ROSEN_2"];
                        $data["GESYA_2"][TEXT]              = $data_arr[$i]["GESYA_2"];
                        $data["FLG_2"][TEXT]                = $data_arr[$i]["FLG_2"];
                        $data["JOSYA_3"][TEXT]              = $data_arr[$i]["JOSYA_3"];
                        $data["ROSEN_3"][TEXT]              = $data_arr[$i]["ROSEN_3"];
                        $data["GESYA_3"][TEXT]              = $data_arr[$i]["GESYA_3"];
                        $data["FLG_3"][TEXT]                = $data_arr[$i]["FLG_3"];
                        $data["JOSYA_4"][TEXT]              = $data_arr[$i]["JOSYA_4"];
                        $data["ROSEN_4"][TEXT]              = $data_arr[$i]["ROSEN_4"];
                        $data["GESYA_4"][TEXT]              = $data_arr[$i]["GESYA_4"];
                        $data["FLG_4"][TEXT]                = $data_arr[$i]["FLG_4"];
                        $data["JOSYA_5"][TEXT]              = $data_arr[$i]["JOSYA_5"];
                        $data["ROSEN_5"][TEXT]              = $data_arr[$i]["ROSEN_5"];
                        $data["GESYA_5"][TEXT]              = $data_arr[$i]["GESYA_5"];
                        $data["FLG_5"][TEXT]                = $data_arr[$i]["FLG_5"];
                        $data["JOSYA_6"][TEXT]              = $data_arr[$i]["JOSYA_6"];
                        $data["ROSEN_6"][TEXT]              = $data_arr[$i]["ROSEN_6"];
                        $data["GESYA_6"][TEXT]              = $data_arr[$i]["GESYA_6"];
                        $data["FLG_6"][TEXT]                = $data_arr[$i]["FLG_6"];
                        $data["JOSYA_7"][TEXT]              = $data_arr[$i]["JOSYA_7"];
                        $data["ROSEN_7"][TEXT]              = $data_arr[$i]["ROSEN_7"];
                        $data["GESYA_7"][TEXT]              = $data_arr[$i]["GESYA_7"];
                        $data["FLG_7"][TEXT]                = $data_arr[$i]["FLG_7"];
                        $insup = "ins";
                    }

                    break;
                case 4:
                    if ($tblcnt > 0) {
                        $data["DETAIL_EDATE"][TEXT]         = $data_arr[$i]["DETAIL_EDATE"];
                        $data["BICYCLE_NO"][TEXT]           = $data_arr[$i]["BICYCLE_NO"];
                        $data["CONTENT"][TEXT]              = $data_arr[$i]["CONTENT"];
                        $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
                        $where  = " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= " AND DETAIL_DIV = '3' AND DETAIL_SDATE = '".$data_arr[$i]["DETAIL_SDATE"]."' ";
                        $insup = "up";
                    } else {
                        $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["DETAIL_DIV"][TEXT]           = "3";
                        $data["DETAIL_SDATE"][TEXT]         = $data_arr[$i]["DETAIL_SDATE"];
                        $data["DETAIL_EDATE"][TEXT]         = $data_arr[$i]["DETAIL_EDATE"];
                        $data["BICYCLE_CD"][TEXT]           = $data_arr[$i]["BICYCLE_CD"];
                        $data["BICYCLE_NO"][TEXT]           = $data_arr[$i]["BICYCLE_NO"];
                        $data["CONTENT"][TEXT]              = $data_arr[$i]["CONTENT"];
                        $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
                        $insup = "ins";
                    }

                    break;
                case 5:
                    if ($tblcnt > 0) {
                        $data["DIV"][TEXT]                  = '1';
                        $data["SEND_RELATIONSHIP"][TEXT]    = $data_arr[$i]["SEND_RELATIONSHIP"];
                        $data["SEND_NAME"][TEXT]            = $data_arr[$i]["SEND_NAME"];
                        $data["SEND_KANA"][TEXT]            = $data_arr[$i]["SEND_KANA"];
                        $data["SEND_SEX"][TEXT]             = $data_arr[$i]["SEND_SEX"];
                        $data["SEND_ZIPCD"][TEXT]           = $data_arr[$i]["SEND_ZIPCD"];
                        $data["SEND_ADDR1"][TEXT]           = $data_arr[$i]["SEND_ADDR1"];
                        $data["SEND_ADDR2"][TEXT]           = $data_arr[$i]["SEND_ADDR2"];
                        $data["SEND_TELNO"][TEXT]           = $data_arr[$i]["SEND_TELNO"];
                        $data["SEND_JOBCD"][TEXT]           = $data_arr[$i]["SEND_JOBCD"];
                        $data["PUBLIC_OFFICE"][TEXT]        = $data_arr[$i]["PUBLIC_OFFICE"];
                        $where  = "  WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "    AND DIV      = '1' ";
                        $insup = "up";
                    } else {
                        $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
                        $data["DIV"][TEXT]                  = '1';
                        $data["SEND_RELATIONSHIP"][TEXT]    = $data_arr[$i]["SEND_RELATIONSHIP"];
                        $data["SEND_NAME"][TEXT]            = $data_arr[$i]["SEND_NAME"];
                        $data["SEND_KANA"][TEXT]            = $data_arr[$i]["SEND_KANA"];
                        $data["SEND_SEX"][TEXT]             = $data_arr[$i]["SEND_SEX"];
                        $data["SEND_ZIPCD"][TEXT]           = $data_arr[$i]["SEND_ZIPCD"];
                        $data["SEND_ADDR1"][TEXT]           = $data_arr[$i]["SEND_ADDR1"];
                        $data["SEND_ADDR2"][TEXT]           = $data_arr[$i]["SEND_ADDR2"];
                        $data["SEND_TELNO"][TEXT]           = $data_arr[$i]["SEND_TELNO"];
                        $data["SEND_JOBCD"][TEXT]           = $data_arr[$i]["SEND_JOBCD"];
                        $data["PUBLIC_OFFICE"][TEXT]        = $data_arr[$i]["PUBLIC_OFFICE"];
                        $insup = "ins";
                    }

                    break;
            }
            if ($insup == "ins") {
                $query = Query::insertSQL($data, $table);
                $db->query($query);
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, $table, $where);
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
