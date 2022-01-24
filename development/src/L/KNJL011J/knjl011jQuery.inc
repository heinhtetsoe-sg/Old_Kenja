<?php

require_once('for_php7.php');

class knjl011jQuery extends Query 
{

    //最後の受験番号＋１を取得
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
    function getFinschoolcd($year)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         T1.FINSCHOOL_DISTCD, N1.NAME1 AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L001' AND N1.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T2.YEAR = '".$year."' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //特別事情区分
    function getSpecialReasonDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L017' ";

        return $query;
    }

    //塾マスタよりデータを取得
    function getPrischoolcd($year)
    {
        $query  = " SELECT  T1.PRISCHOOLCD, T1.PRISCHOOL_NAME ";
        $query .= "   FROM  PRISCHOOL_MST T1, ";
        $query .= "         PRISCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '".$year."' ";
        $query .= "    AND  T2.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= " ORDER BY T1.PRISCHOOLCD ";
        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1
                       ,ABBV1
                       ,NAMESPARE2
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = '".$cd."'
                 ";
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
                        T1.EXAMNO,
                        T1.TESTDIV1,
                        T1.TESTDIV2,
                        T1.TESTDIV3,
                        T1.TESTDIV4,
                        T1.TESTDIV5,
                        T1.TESTDIV6,
                        T1.SPECIAL_REASON_DIV,
                        T1.TESTDIV1 AS EXAM_TYPE1,
                        T1.TESTDIV2 AS EXAM_TYPE2,
                        T1.TESTDIV3 AS EXAM_TYPE3,
                        T1.TESTDIV4 AS EXAM_TYPE4,
                        T1.TESTDIV5 AS EXAM_TYPE5,
                        T1.TESTDIV6 AS EXAM_TYPE6,
                        T1.RECEPTDATE,
                        T1.NAME,
                        T1.NAME_KANA,
                        T1.SEX,
                        T1.ERACD,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.BIRTHDAY,
                        T1.FS_NATPUBPRIDIV,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T1.FS_AREA_CD,
                        T1.FS_GRDYEAR,
                        T1.PRISCHOOLCD,
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
                        T2.GFAXNO,
                        T2.RELATIONSHIP,
                        T2.EMERGENCYCALL,
                        T2.EMERGENCYTELNO,
                        T3.NAME1,
                        T4.confidential_rpt01,T4.confidential_rpt02,T4.confidential_rpt03,T4.confidential_rpt04,T4.confidential_rpt05,T4.confidential_rpt06,
                        T4.confidential_rpt07,T4.confidential_rpt08,T4.confidential_rpt09,T4.confidential_rpt10,T4.confidential_rpt11,T4.confidential_rpt12,
                        T4.average5,T4.average_all,T4.absence_days
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

    //受付日範囲取得
    function CheckReceptDate($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        NAME1,
                        ABBV2,
                        ABBV3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L003' AND
                        NAMECD2 = '".$model->field["APPLICANTDIV"]. "'";
        
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
        $result = $db->query(knjl011jQuery::get_calendarno($model->year));
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

//        //生年月日の変換(西暦→和暦)
//        $result = $db->query(knjl011jQuery::get_calendarno($model->year));
//        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
//        {
//            $eracd[] = $row["NAMECD2"];
//            $defyear[] = $row["NAMESPARE1"];
//            $defsdate[] = $row["NAMESPARE2"];
//            $defedate[] = $row["NAMESPARE3"];
//        }
//        $result->free();
//
//        $w_eracd = $w_year = $mon = $day = "";
//        if (strlen($model->field["BIRTHDAY"])) {
//            list($s_year, $mon, $day) = explode("/", $model->field["BIRTHDAY"]);
//            for ($i=0; $i<get_count($eracd); $i++) {
//                if ($defsdate[$i] <= $model->field["BIRTHDAY"] && $model->field["BIRTHDAY"] <= $defedate[$i]) {
//                    $w_eracd = $eracd[$i];
//                    $w_year = ((int)$s_year + 1) - ((int)$defyear[$i]);
//                    $w_year = sprintf("%02d",$w_year);
//                    break;
//                }
//            }
//        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = '1';
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["TESTDIV1"][TEXT]      = (strlen($model->field["TESTDIV1"]) ? $model->field["EXAM_TYPE1"] : "");
        $data["TESTDIV2"][TEXT]      = (strlen($model->field["TESTDIV2"]) ? $model->field["EXAM_TYPE2"] : "");
        $data["TESTDIV3"][TEXT]      = (strlen($model->field["TESTDIV3"]) ? $model->field["EXAM_TYPE3"] : "");
        $data["TESTDIV4"][TEXT]      = (strlen($model->field["TESTDIV4"]) ? $model->field["EXAM_TYPE4"] : "");
        $data["TESTDIV5"][TEXT]      = (strlen($model->field["TESTDIV5"]) ? $model->field["EXAM_TYPE5"] : "");
        $data["TESTDIV6"][TEXT]      = (strlen($model->field["TESTDIV6"]) ? $model->field["EXAM_TYPE6"] : "");
        $data["SPECIAL_REASON_DIV"][TEXT] = $model->field["SPECIAL_REASON_DIV"];
        $data["RECEPTDATE"][TEXT]    = str_replace("/","-",$model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
//        $data["ERACD"][TEXT]         = $w_eracd;
//        $data["BIRTH_Y"][TEXT]       = $w_year;
//        $data["BIRTH_M"][TEXT]       = $mon;
//        $data["BIRTH_D"][TEXT]       = $day;
//        $data["BIRTHDAY"][TEXT]      = str_replace("/","-",$model->field["BIRTHDAY"]);
        $data["FS_NATPUBPRIDIV"][TEXT] = $model->field["FS_NATPUBPRIDIV"];
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = $model->field["FS_AREA_CD"];
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["PRISCHOOLCD"][TEXT]   = $model->field["PRISCHOOLCD"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        if ($this->cmd != "add") {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        }
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        if ($this->cmd != "add") {
            $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $query.= "       EXAMNO = '".$model->examno."'";
            $db->query($query);
        }

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
        $data["GFAXNO"][TEXT]           = $model->field["GFAXNO"];
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["EMERGENCYCALL"][TEXT]    = $model->field["EMERGENCYCALL"];
        $data["EMERGENCYTELNO"][TEXT]   = $model->field["EMERGENCYTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
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
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
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

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>