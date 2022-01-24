<?php

require_once('for_php7.php');

class knjl011oQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = '".$cd."'
                    ORDER BY
                        NAMECD2
                 ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //名称
    function testDayCheck($ObjYear,$testdivArray)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     TESTDAY AS NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "     TESTDAY in (SELECT ";
        $query .= "                         TESTDAY ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_TESTDIV_MST ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "                         TESTDIV IN ('" . implode("','", $testdivArray) . "') ";
        $query .= "                     GROUP BY ";
        $query .= "                         TESTDAY ";
        $query .= "                     HAVING ";
        $query .= "                         1 < COUNT(*) ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV ";

        $testDayCheck = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $testDayCheck[$row["NAMECD2"]] = $row["NAME1"];
        }

        Query::dbCheckIn($db);
        return $testDayCheck;
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

    //塾マスタよりデータを取得
    function getPrischoolcd($year, $prischoolcd)
    {
        $query  = " SELECT  T1.PRISCHOOLCD, T1.PRISCHOOL_NAME ";
        $query .= "   FROM  PRISCHOOL_MST T1, ";
        $query .= "         PRISCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '{$year}' ";
        $query .= "    AND  T2.PRISCHOOLCD = '{$prischoolcd}' ";
        $query .= "    AND  T2.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= " ORDER BY T1.PRISCHOOLCD ";
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();



        $query = "  SELECT
                        T1.APPLICANTDIV,
                        T1.EXAMNO,
                        T1.SHDIV,
                        T1.TESTDIV0,
                        T1.TESTDIV1,
                        T1.TESTDIV2,
                        T1.TESTDIV3,
                        T1.TESTDIV4,
                        T1.TESTDIV5,
                        T1.TESTDIV6,
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
                        T1.FS_GRDYEAR,
                        T1.REMARK1,
                        T1.REMARK2,
                        T1.RECOM_EXAMNO1,
                        T1.RECOM_EXAMNO2,
                        T1.RECOM_EXAMNO3,
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
                        T4.confidential_rpt01,T4.confidential_rpt02,T4.confidential_rpt03,T4.confidential_rpt04,T4.confidential_rpt05,T4.confidential_rpt06,
                        T4.confidential_rpt07,T4.confidential_rpt08,T4.confidential_rpt09,T4.confidential_rpt10,T4.confidential_rpt11,T4.confidential_rpt12,
                        T4.average5,T4.average_all,T4.absence_days,
                        L1.REMARK1 AS PS_CD,
                        L1.REMARK2 AS CLASS_ROOM,
                        L1.REMARK3 AS PS_CD2,
                        L1.REMARK4 AS CLASS_ROOM2
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        ENTEXAM_APPLICANTADDR_DAT T2
                    ON
                        T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND
                        T1.EXAMNO = T2.EXAMNO
                    LEFT OUTER JOIN
                        V_NAME_MST T3
                    ON
                        T1.ENTEXAMYEAR = T3.YEAR AND
                        T1.ERACD = T3.NAMECD2 AND
                        T3.NAMECD1 = 'L007'
                    LEFT OUTER JOIN 
                        entexam_applicantconfrpt_dat T4
                    ON
                        T1.entexamyear = T4.entexamyear AND
                        T1.examno      = T4.examno
                    LEFT OUTER JOIN
                        ENTEXAM_APPLICANTBASE_DETAIL_DAT L1
                    ON
                        T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND
                        T1.EXAMNO = L1.EXAMNO AND
                        L1.SEQ = '008'
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
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011oQuery::get_calendarno($model->year));
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

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;

        //入試区分
        $testdivMin = "";
        for ($code = 0; $code <= 6; $code++) {
            $fieldname      = "TESTDIV" .$code;
            $data[$fieldname][TEXT] = strlen($model->field[$fieldname]) ? $code : "";
            //入試区分の最小値を取得
            if ($testdivMin == "" && strlen($model->field[$fieldname])) $testdivMin = $code;
        }
        //出願区分 入試区分の最小値をセット
        $data["TESTDIV"][TEXT] = $testdivMin;

        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d",$model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["RECOM_EXAMNO1"][TEXT] = $model->field["RECOM_EXAMNO1"];
        $data["RECOM_EXAMNO2"][TEXT] = $model->field["RECOM_EXAMNO2"];
        $data["RECOM_EXAMNO3"][TEXT] = $model->field["RECOM_EXAMNO3"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

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

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //塾名、教室名をセット
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO  = '".$model->examno."' ";
        $query .= " AND SEQ  = '008'";
        $db->query($query);
        
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "008";
        if ($model->field["PS_CD"]) {
            $data["REMARK1"][TEXT]      = sprintf("%07d",$model->field["PS_CD"]);
        }
        $data["REMARK2"][TEXT]      = $model->field["CLASS_ROOM"];
        if ($model->field["PS_CD2"]) {
            $data["REMARK3"][TEXT]      = sprintf("%07d",$model->field["PS_CD2"]);
        }
        $data["REMARK4"][TEXT]      = $model->field["CLASS_ROOM2"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011oQuery::get_calendarno($model->year));
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

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;

        //入試区分
        $testdivMin = "";
        for ($code = 0; $code <= 6; $code++) {
            $fieldname      = "TESTDIV" .$code;
            $data[$fieldname][TEXT] = strlen($model->field[$fieldname]) ? $code : "";
            //入試区分の最小値を取得
            if ($testdivMin == "" && strlen($model->field[$fieldname])) $testdivMin = $code;
        }
        //出願区分 入試区分の最小値をセット
        $data["TESTDIV"][TEXT] = $testdivMin;

        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d",$model->field["FS_AREA_CD"]) : "");//2005.12.28 minei  
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["RECOM_EXAMNO1"][TEXT] = $model->field["RECOM_EXAMNO1"];
        $data["RECOM_EXAMNO2"][TEXT] = $model->field["RECOM_EXAMNO2"];
        $data["RECOM_EXAMNO3"][TEXT] = $model->field["RECOM_EXAMNO3"];
        $data["registercd"][TEXT]    = STAFFCD;
        $data["updated"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---INSERT
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

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //塾名、教室名をセット
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO  = '".$model->examno."' ";
        $query .= " AND SEQ  = '008'";
        $db->query($query);
        
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "008";
        if ($model->field["PS_CD"]) {
            $data["REMARK1"][TEXT]      = sprintf("%07d",$model->field["PS_CD"]);
        }
        $data["REMARK2"][TEXT]      = $model->field["CLASS_ROOM"];
        if ($model->field["PS_CD2"]) {
            $data["REMARK3"][TEXT]      = sprintf("%07d",$model->field["PS_CD2"]);
        }
        $data["REMARK4"][TEXT]      = $model->field["CLASS_ROOM2"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_DESIRE_DAT
        $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND EXAMNO  = '".$model->examno."' ";
        $query .= " AND SEQ  = '008'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //兄弟受験データを取得
    function getBrotherInfo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO AS BROTHER_EXAMNO, ";
        $query .= "     T1.NAME AS BROTHER_NAME, ";
        $query .= "     L1.SEQ AS BROTHER_SEQ, ";
        $query .= "     L1.REMARK1 AS BROTHER_REMARK1, ";
        $query .= "     L1.REMARK2 AS BROTHER_REMARK2, ";
        $query .= "     L1.REMARK3 AS BROTHER_REMARK3, ";
        $query .= "     L1.REMARK4 AS BROTHER_REMARK4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L1.SEQ = '{$model->brother["BROTHER_SEQ"]}' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.EXAMNO      = '{$model->brother["BROTHER_EXAMNO"]}' ";
        return $query;
    }

    //更新処理(兄弟受験画面)
    function getBrotherUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //del
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '{$model->year}' ";
        $query.= "       AND EXAMNO = '{$model->brother["BROTHER_EXAMNO"]}' ";
        $query.= "       AND SEQ = '{$model->brother["BROTHER_SEQ"]}' ";
        $db->query($query);

        //ins
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->brother["BROTHER_EXAMNO"];
        $data["SEQ"][TEXT]          = $model->brother["BROTHER_SEQ"];
        $data["REMARK1"][TEXT]      = $model->brother["BROTHER_REMARK1"];
        $data["REMARK2"][TEXT]      = $model->brother["BROTHER_REMARK2"];
        $data["REMARK3"][TEXT]      = $model->brother["BROTHER_REMARK3"];
        $data["REMARK4"][TEXT]      = $model->brother["BROTHER_REMARK4"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $insFlg = false;
        for ($i = 1; $i <= 4; $i++) {
            if (strlen($model->brother["BROTHER_REMARK".$i])) $insFlg = true;
        }

        if ($insFlg) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


}
?>