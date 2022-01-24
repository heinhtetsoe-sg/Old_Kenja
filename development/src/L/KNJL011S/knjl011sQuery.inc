<?php

require_once('for_php7.php');

class knjl011sQuery extends Query 
{
    //最終学期を取得
    function getSemesterLast() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";

        $db = Query::dbCheckOut();
        $rtnVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnVal;
    }

    //学校コード・・・境高校かどうかを判定
    function getSchoolname() {
        $query  = "SELECT ";
        $query .= "    NAME1, NAME2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = 'Z010' AND ";
        $query .= "    NAMECD2 = '00' ";
        return $query;
    }

    //推薦区分
    function getSuisenKubun($year) {
        $query  = "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' ";
        $query .= "    AND NAMECD1 = 'L026' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";
        return $query;
    }

    //学力検査受検希望教科
    function getTestSubcd($year) {
        $query  = "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' ";
        $query .= "    AND NAMECD1 = 'L009' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";
        return $query;
    }

    //学科マスタ（大）
    function getMajorLMst() {
        $query  = "SELECT ";
        $query .= "    MAJORLCD AS VALUE, ";
        $query .= "    MAJORLCD || ':' || MAJORLNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_MAJORL_MST ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";
        return $query;
    }

    //学科マスタ（小）
    function getMajorSMst($majorlcd) {
        $query  = "SELECT ";
        $query .= "    MAJORSCD AS VALUE, ";
        $query .= "    MAJORSCD || ':' || MAJORSNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_MAJORS_MST ";
        $query .= "WHERE ";
        $query .= "    MAJORLCD = '{$majorlcd}' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";
        return $query;
    }

    //最後の受検番号＋１を取得
    function getMaxExamnoQuery($year)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT  VALUE(MAX(SMALLINT(EXAMNO)), 0) + 1 AS EXAMNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE   ENTEXAMYEAR = '".$year."' ";
        $max_examno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_examno;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolcd($year, $finschool_type)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         T1.DISTRICTCD, N1.NAME1 AS DISTRICTCD_NAME, N2.NAME1 AS FS_NATPUBPRIDIV_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z003' AND N1.NAMECD2=T1.DISTRICTCD ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L001' AND N2.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T2.YEAR = '$year' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "    AND  T1.FINSCHOOL_TYPE = '$finschool_type' ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //出身学校マスタより校種を取得
    function getFinschoolType($year, $finschoolcd) {
        $query  = " SELECT  T1.FINSCHOOL_TYPE ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T2.YEAR = '$year' ";
        $query .= "    AND  T2.FINSCHOOLCD = '$finschoolcd' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $cd2 = "")
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1, ";
        $query .= "      NAME2, ";
        $query .= "      ABBV1, ";
        $query .= "      NAMESPARE1, ";
        $query .= "      NAMESPARE2 ";
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$year."' ";
        $query .= "      AND NAMECD1 = '".$cd."' ";
        if ($cd2) {
            $query .= "      AND NAMECD2 = '".$cd2."' ";
        }
        return $query;
    }

    //最小受験番号
    function getMinExam($applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.APPLICANTDIV = '".$applicantDiv."' ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        T1.APPLICANTDIV,
                        T1.TESTDIV2,
                        T1.SHIFT_DESIRE_FLG,
                        T1.EXAMNO,
                        T1.NAME,
                        T1.NAME_KANA,
                        T1.SEX,
                        T1.ERACD,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.BIRTHDAY,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T1.FS_AREA_CD,
                        T1.FS_ERACD,
                        N1.NAME1 as FS_WNAME,
                        T1.FS_Y,
                        T1.FS_M,
                        T1.FS_GRDDIV,
                        T1.REMARK1,
                        T1.REMARK2,
                        T2.ZIPCD,
                        T2.ADDRESS1,
                        T2.ADDRESS2,
                        T2.TELNO,
                        T2.GNAME,
                        T2.GKANA,
                        T2.GZIPCD,
                        T2.GADDRESS1,
                        T2.GADDRESS2,
                        T2.GTELNO,
                        T3.NAME1,
                        T4.CONFIDENTIAL_RPT01,T4.CONFIDENTIAL_RPT02,T4.CONFIDENTIAL_RPT03,T4.CONFIDENTIAL_RPT04,T4.CONFIDENTIAL_RPT05,T4.CONFIDENTIAL_RPT06,
                        T4.CONFIDENTIAL_RPT07,T4.CONFIDENTIAL_RPT08,T4.CONFIDENTIAL_RPT09,T4.CONFIDENTIAL_RPT10,T4.CONFIDENTIAL_RPT11,T4.CONFIDENTIAL_RPT12,
                        T4.TOTAL3,
                        T4.TOTAL5,
                        T4.TOTAL_ALL AS TOTAL9,
                        T4.AVERAGE_ALL,
                        T4.ABSENCE_DAYS AS ABSENCE_DAYS1,
                        T4.ABSENCE_DAYS2,
                        T4.ABSENCE_DAYS3,
                        W1.MAJORLCD AS MAJORLCD1,
                        W2.MAJORLCD AS MAJORLCD2,
                        W3.MAJORLCD AS MAJORLCD3,
                        W4.MAJORLCD AS MAJORLCD4,
                        W5.MAJORLCD AS MAJORLCD5,
                        W6.MAJORLCD AS MAJORLCD6,
                        W1.MAJORSCD AS MAJORSCD1,
                        W2.MAJORSCD AS MAJORSCD2,
                        W3.MAJORSCD AS MAJORSCD3,
                        W4.MAJORSCD AS MAJORSCD4,
                        W5.MAJORSCD AS MAJORSCD5,
                        W6.MAJORSCD AS MAJORSCD6,
                        H1.HOPEFLG AS HOPEFLG1,
                        H2.HOPEFLG AS HOPEFLG2,
                        H3.HOPEFLG AS HOPEFLG3,
                        H4.HOPEFLG AS HOPEFLG4,
                        H5.HOPEFLG AS HOPEFLG5,
                        H1.INC_HOPEFLG AS INC_HOPEFLG1,
                        H2.INC_HOPEFLG AS INC_HOPEFLG2,
                        H3.INC_HOPEFLG AS INC_HOPEFLG3,
                        H4.INC_HOPEFLG AS INC_HOPEFLG4,
                        H5.INC_HOPEFLG AS INC_HOPEFLG5
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                        LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.EXAMNO = T2.EXAMNO
                        LEFT JOIN V_NAME_MST T3 ON T1.ENTEXAMYEAR = T3.YEAR AND T1.ERACD = T3.NAMECD2 AND T3.NAMECD1 = 'L007'
                        LEFT JOIN V_NAME_MST N1 ON T1.ENTEXAMYEAR = N1.YEAR AND T1.FS_ERACD = N1.NAMECD2 AND N1.NAMECD1 = 'L007'
                        LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND T1.EXAMNO = T4.EXAMNO
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W1 ON W1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W1.EXAMNO = T1.EXAMNO AND W1.WISHNO='1'
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W2 ON W2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W2.EXAMNO = T1.EXAMNO AND W2.WISHNO='2'
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W3 ON W3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W3.EXAMNO = T1.EXAMNO AND W3.WISHNO='3'
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W4 ON W4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W4.EXAMNO = T1.EXAMNO AND W4.WISHNO='4'
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W5 ON W5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W5.EXAMNO = T1.EXAMNO AND W5.WISHNO='5'
                        LEFT JOIN ENTEXAM_APPLICANTWISH_DAT W6 ON W6.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W6.EXAMNO = T1.EXAMNO AND W6.WISHNO='6'
                        LEFT JOIN ENTEXAM_APPLICANTHOPE_DAT H1 ON H1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND H1.EXAMNO = T1.EXAMNO AND H1.TESTSUBCLASSCD='1'
                        LEFT JOIN ENTEXAM_APPLICANTHOPE_DAT H2 ON H2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND H2.EXAMNO = T1.EXAMNO AND H2.TESTSUBCLASSCD='2'
                        LEFT JOIN ENTEXAM_APPLICANTHOPE_DAT H3 ON H3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND H3.EXAMNO = T1.EXAMNO AND H3.TESTSUBCLASSCD='3'
                        LEFT JOIN ENTEXAM_APPLICANTHOPE_DAT H4 ON H4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND H4.EXAMNO = T1.EXAMNO AND H4.TESTSUBCLASSCD='4'
                        LEFT JOIN ENTEXAM_APPLICANTHOPE_DAT H5 ON H5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND H5.EXAMNO = T1.EXAMNO AND H5.TESTSUBCLASSCD='5'
                    WHERE
                        T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データ重複チェック
    function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                       T1.EXAMNO,
                       T2.NAME1
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        V_NAME_MST T2
                    ON
                        T1.ENTEXAMYEAR = T2.YEAR AND
                        T1.APPLICANTDIV = T2.NAMECD2 AND
                        T2.NAMECD1 = 'L003'
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO = '".$model->examno."' AND
                        APPLICANTDIV <> '".$model->applicantdiv."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //該当日付取得
    function CheckDate($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L007' AND
                        NAMECD2 = '".$model->field["ERACD"]. "'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }


    //--- INSERT
    //--- UPDATE
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生年月日の変換(和暦→西暦)
        $result = $db->query(knjl011sQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        $birthday = "";
        if (isset($model->field["ERACD"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["ERACD"] == $eracd[$i]) {
                    $syear = ((int)$defyear[$i]) + ((int)$model->field["BIRTH_Y"] - 1);
                    $birthday = $syear. "-".sprintf("%02d",$model->field["BIRTH_M"])."-".sprintf("%02d",$model->field["BIRTH_D"]);
                    break;
                }
            }
        }

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = '1';
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["TESTDIV1"][TEXT]      = '1';
        $data["TESTDIV2"][TEXT]      = $model->field["TESTDIV2"];//追検査

        $data["SHIFT_DESIRE_FLG"][TEXT] = $model->field["SHIFT_DESIRE_FLG"];//推薦区分

        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = $model->field["FS_AREA_CD"];
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        if ($model->cmd != "add") {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        if (0 == $db->getOne($query)) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."' ";
            $query  = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            $db->query($query);
        }

        //志望区分（順位）
        $query  = " DELETE FROM ENTEXAM_APPLICANTWISH_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        $db->query($query);
        for ($m = 1; $m <= 6; $m++) {
            if (strlen($model->field["MAJORLCD" .$m]) < 1) continue;
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["WISHNO"][TEXT]       = $m;
            $data["MAJORLCD"][TEXT]     = $model->field["MAJORLCD" .$m];
            $data["MAJORSCD"][TEXT]     = $model->field["MAJORSCD" .$m];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTWISH_DAT");
            $db->query($query);
        }
        //希望教科
        $query  = " DELETE FROM ENTEXAM_APPLICANTHOPE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        $db->query($query);
        for ($m = 1; $m <= 5; $m++) {
            $name    = "HOPEFLG" .$m;
            $nameInc = "INC_HOPEFLG" .$m;
            if (strlen($model->field[$name]) < 1 && strlen($model->field[$nameInc]) < 1) continue;
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["TESTSUBCLASSCD"][TEXT]   = $m;
            $data["HOPEFLG"][TEXT]          = strlen($model->field[$name]) ? "1" : "";
            $data["INC_HOPEFLG"][TEXT]      = strlen($model->field[$nameInc]) ? "1" : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTHOPE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTHOPE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTHOPE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTWISH_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTWISH_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);
/***
        //ENTEXAM_DESIRE_DAT
        $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_SCORE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DETAIL_DAT ";
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT・・・鳥取は未使用か？つまりコメントにするべきか？
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);
***/
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>