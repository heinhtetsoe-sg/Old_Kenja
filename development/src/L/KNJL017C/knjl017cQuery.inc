<?php

require_once('for_php7.php');

class knjl017cQuery extends Query 
{
    //都道府県マスタよりデータを取得
    function getPrefMst()
    {
        $query  = " select PREF_CD,PREF_NAME from PREF_MST order by PREF_CD ";
        return $query;
    }

    //MAX受験番号＋１を取得
    function getMaxExamnoQuery($model)
    {
        //MAX受験番号(初期値)
        $defExamno = knjl017cQuery::getDefExamno($model);

        $db = Query::dbCheckOut();
        $query  = " SELECT  VALUE(MAX(INT(T1.EXAMNO)), {$defExamno}) + 1 AS EXAMNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE   T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND   T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND   T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";

        $max_examno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_examno;
    }

    //MAX受験番号(初期値)を取得
    function getDefExamno($model) {
        //入試制度フラグ
        $isJunior        = ($model->field["APPLICANTDIV"] == "1")                ? true : false;
        //入試区分フラグ
        $isJuniorZenki   = ( $isJunior    && $model->field["TESTDIV"] == "1")    ? true : false;
        $isJuniorKouki   = ( $isJunior    && $model->field["TESTDIV"] == "2")    ? true : false;
        $isJuniorNaibu   = ( $isJunior    && $model->field["TESTDIV"] == "6")    ? true : false;

        //MAX受験番号(初期値)
        $defExamno = 0;

        if ($isJunior) {
            if ($isJuniorZenki) {
                $defExamno = 1000;
            } else if ($isJuniorKouki) {
                $defExamno = 2000;
            } else if ($isJuniorNaibu) {
                $defExamno = 6000;
            }
        } else {
                $defExamno = 8000;
        }
        return $defExamno;
    }

    //最小受験番号
    function getMinExam($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$applicantDiv."' ";

        return $query;
    }

    //教育委員会取得
    function getEdboard()
    {
        $query  = " SELECT ";
        $query .= "     EDBOARDCD AS VALUE, ";
        $query .= "     EDBOARDCD || ':' || EDBOARDNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
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
    function getFinschoolcd($year, $finschoolcd)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         T1.DISTRICTCD, N1.NAME1 AS DISTRICTCD_NAME, N2.NAME1 AS FS_NATPUBPRIDIV_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z003' AND N1.NAMECD2=T1.DISTRICTCD ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L001' AND N2.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T2.YEAR = '$year' ";
        $query .= "    AND  T2.FINSCHOOLCD = '$finschoolcd' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //塾マスタよりデータを取得
    function getPrischoolcd($year, $prischoolcd)
    {
        $query  = " SELECT  T1.PRISCHOOLCD, T1.PRISCHOOL_NAME ";
        $query .= "   FROM  PRISCHOOL_MST T1, ";
        $query .= "         PRISCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '$year' ";
        $query .= "    AND  T2.PRISCHOOLCD = '$prischoolcd' ";
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
                       ,NAMESPARE1
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
                        T1.TESTDIV,
                        T1.EXAMNO,
                        T1.SHDIV,
                        CASE WHEN T1.SHIFT_DESIRE_FLG IN ('1','3') THEN T1.SHIFT_DESIRE_FLG END AS SHIFT_DESIRE_FLG1,
                        CASE WHEN T1.SHIFT_DESIRE_FLG IN ('2','3') THEN T1.SHIFT_DESIRE_FLG END AS SHIFT_DESIRE_FLG2,
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
                        T1.FS_ERACD,
                        N1.NAME1 as FS_WNAME,
                        T1.FS_Y,
                        T1.FS_M,
                        T1.PICTURE_ERACD,
                        N2.NAME1 as PICTURE_WNAME,
                        T1.PICTURE_Y,
                        T1.PICTURE_M,
                        T1.PRISCHOOLCD,
                        T1.REMARK1,
                        T1.REMARK2,
                        T1.RECOM_EXAMNO,
                        T1.PRE_RECEPTNO,
                        T1.PERSONAL_FLG,
                        T1.SUB_ORDER,
                        T2.FAMILY_REGISTER,
                        T2.PREF_CD,
                        T2.GPREF_CD,
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
                        T2.RELATIONSHIP,
                        T2.EDBOARDCD,
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
                    LEFT OUTER JOIN V_NAME_MST N1 ON T1.ENTEXAMYEAR = N1.YEAR AND T1.FS_ERACD = N1.NAMECD2 AND N1.NAMECD1 = 'L007'
                    LEFT OUTER JOIN V_NAME_MST N2 ON T1.ENTEXAMYEAR = N2.YEAR AND T1.PICTURE_ERACD = N2.NAMECD2 AND N2.NAMECD1 = 'L007'
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
        $result = $db->query(knjl017cQuery::get_calendarno($model->year));
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
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]     = '1';
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
//      $data["FS_NATPUBPRIDIV"][TEXT] = $model->field["FS_NATPUBPRIDIV"];
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_AREA_CD"][TEXT]    = $model->field["FS_AREA_CD"];
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["PICTURE_ERACD"][TEXT] = $model->field["PICTURE_ERACD"];
        $data["PICTURE_Y"][TEXT]     = $model->field["PICTURE_Y"]? sprintf("%02d",$model->field["PICTURE_Y"]) : "";
        $data["PICTURE_M"][TEXT]     = $model->field["PICTURE_M"]? sprintf("%02d",$model->field["PICTURE_M"]) : "";
        $data["PRISCHOOLCD"][TEXT]   = $model->field["PRISCHOOLCD"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["RECOM_EXAMNO"][TEXT]  = $model->field["RECOM_EXAMNO"];
        $shift_desire_flg = "";
        if (strlen($model->field["SHIFT_DESIRE_FLG1"]) && strlen($model->field["SHIFT_DESIRE_FLG2"])) {
            $shift_desire_flg = "3"; //3:両方
        } else if (strlen($model->field["SHIFT_DESIRE_FLG1"])) {
            $shift_desire_flg = "1"; //1:五條
        } else if (strlen($model->field["SHIFT_DESIRE_FLG2"])) {
            $shift_desire_flg = "2"; //2:和歌山
        }
        $data["SHIFT_DESIRE_FLG"][TEXT] = $shift_desire_flg;
        $data["PRE_RECEPTNO"][TEXT]     = $model->field["PRE_RECEPTNO"];
        $data["PERSONAL_FLG"][TEXT]     = $model->field["PERSONAL_FLG"];
        $data["SUB_ORDER"][TEXT]     = $model->field["SUB_ORDER"];
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
        $data["FAMILY_REGISTER"][TEXT]  = $model->field["FAMILY_REGISTER"];
        $data["PREF_CD"][TEXT]          = $model->field["PREF_CD"];
        $data["GPREF_CD"][TEXT]         = $model->field["GPREF_CD"];
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
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["EDBOARDCD"][TEXT]     = $model->field["EDBOARDCD"];
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