<?php

require_once('for_php7.php');

class knjl011mQuery extends Query
{

    //国公立区分マスタ
    function getNatpubpriMst() {
        $query  = "SELECT ";
        $query .= "    NATPUBPRI_CD AS VALUE, ";
        $query .= "    NATPUBPRI_CD || ':' || NATPUBPRI_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_NATPUBPRI_MST ";
        $query .= "ORDER BY ";
        $query .= "    NATPUBPRI_CD ";
        return $query;
    }

    //所在地区分マスタ
    function getAreaDivMst($natpubpriCd) {
        $query  = "SELECT ";
        $query .= "    AREA_DIV_CD AS VALUE, ";
        $query .= "    AREA_DIV_CD || ':' || AREA_DIV_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_AREA_DIV_MST ";
        $query .= "WHERE ";
        $query .= "    NATPUBPRI_CD = '{$natpubpriCd}' ";
        $query .= "ORDER BY ";
        $query .= "    AREA_DIV_CD ";
        return $query;
    }

    //所在地マスタ
    function getAreaMst($natpubpriCd, $areaDivCd) {
        $query  = "SELECT ";
        $query .= "    AREA_CD AS VALUE, ";
        $query .= "    AREA_CD || ':' || AREA_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_AREA_MST ";
        $query .= "WHERE ";
        $query .= "    NATPUBPRI_CD = '{$natpubpriCd}' ";
        $query .= "    AND AREA_DIV_CD = '{$areaDivCd}' ";
        $query .= "ORDER BY ";
        $query .= "    AREA_CD ";
        return $query;
    }

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
                        T1.NAME,
                        T1.NAME_KANA,
                        T3.NAME1,
                        T1.ERACD,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T1.FS_AREA_DIV,
                        T1.FS_NATPUBPRIDIV,
                        T1.FS_AREA_CD,
                        T1.REMARK1,
                        T2.GNAME,
                        T2.GKANA
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                        LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR
                                                              AND T2.EXAMNO = T1.EXAMNO
                        LEFT JOIN V_NAME_MST T3 ON T1.ENTEXAMYEAR = T3.YEAR AND T1.ERACD = T3.NAMECD2 AND T3.NAMECD1 = 'L007'
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
        $result = $db->query(knjl011mQuery::get_calendarno($model->year));
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

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = '1';
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = '1';
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"]? sprintf("%02d",$model->field["BIRTH_Y"]) : "";
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"]? sprintf("%02d",$model->field["BIRTH_M"]) : "";
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"]? sprintf("%02d",$model->field["BIRTH_D"]) : "";
        $data["BIRTHDAY"][TEXT]      = $birthday;
        $data["SEX"][TEXT]           = '1'; //武蔵は男子校のため、固定１をセット
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_NATPUBPRIDIV"][TEXT] = $model->field["FS_NATPUBPRIDIV"];
        $data["FS_AREA_DIV"][TEXT]   = $model->field["FS_AREA_DIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["FS_AREA_CD"][TEXT]    = $model->field["FS_AREA_CD"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($this->cmd != "add") {
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
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
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

        //ENTEXAM_RECEPT_DAT ---レコードのみ作成
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = '1';
        $data["TESTDIV"][TEXT]       = '1';
        $data["EXAM_TYPE"][TEXT]     = '1';
        $data["RECEPTNO"][TEXT]      = $model->examno;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query  = " SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO = '".$model->examno."' ";
        if (0 == $db->getOne($query)) {
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
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

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>