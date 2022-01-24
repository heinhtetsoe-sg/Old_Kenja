<?php

require_once('for_php7.php');

class knjl011aQuery extends Query
{

    //新規ボタンで、志願者SEQ(Hで始まる以下連番)を取得
    public function getMaxExamno($db, $model)
    {
//        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO LIKE 'H%' ";
        $examno = $db->getOne($query);
//        Query::dbCheckIn($db);

        if (strlen($examno)) {
            $examno = str_replace("H", "", $examno);
            $examno++;
        } else {
            $examno = "1";
        }

        return "H".sprintf("%06d", $examno);
    }

    //名称マスタよりデータを取得
    public function getNameCd($year, $namecd1, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //英語見なし得点
    public function getEiken($year, $applicantdiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L055' ";
        $query .= "     AND NAMESPARE1 = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験校種(J,H)を取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= "     AND NAMECD2 = '{$model->applicantdiv}' ";

        return $query;
    }

    //特待生区分取得
    public function getHonordiv($model, $honordiv = "")
    {
        $query  = " SELECT ";
        $query .= "     HONORDIV AS VALUE, ";
        $query .= "     HONORDIV || ':' || HONORDIV_NAME AS LABEL, ";
        $query .= "     VALUE(CLUB_FLG, '0') AS CLUB_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if (strlen($honordiv)) {
            $query .= "     AND HONORDIV = '{$honordiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //クラブ取得
    public function getClubcd($model)
    {
        $query  = " SELECT ";
        $query .= "     CLUB_M.CLUBCD AS VALUE, ";
        $query .= "     CLUB_M.CLUBCD || ':' || CLUB_M.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB_M ";
        $query .= " WHERE ";
        $query .= "     CLUB_M.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= " AND CLUB_M.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     RECEPT.RECEPTNO AS SEARCH_EXAMNO, ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     BASE.FS_ERACD, ";
        $query .= "     BASE.FS_Y, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     BASE.FS_GRDDIV, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.EMERGENCYTELNO, ";
        $query .= "     SEQ008.REMARK1 AS PRISCHOOLCD1, ";
        $query .= "     SEQ008.REMARK3 AS PRISCHOOL_CLASS_CD1, ";
        $query .= "     SEQ008.REMARK4 AS PRISCHOOLCD2, ";
        $query .= "     SEQ008.REMARK5 AS PRISCHOOL_CLASS_CD2, ";
        $query .= "     SEQ008.REMARK6 AS PRISCHOOLCD3, ";
        $query .= "     SEQ008.REMARK7 AS PRISCHOOL_CLASS_CD3, ";
        $query .= "     SEQ029.REMARK1 AS HONORDIV1, ";
        $query .= "     SEQ029.REMARK2 AS HONORDIV2, ";
        $query .= "     SEQ029.REMARK3 AS HONORDIV3, ";
        $query .= "     SEQ029.REMARK4 AS CLUB_CD, ";
        $query .= "     SEQ005.REMARK1 AS SEQ005_R1, ";
        $query .= "     TSTM.TESTDIV_NAME AS PASS_TESTDIV, ";
        $query .= "     SEQ030.REMARK2 AS PASS_RECEPTNO, ";
        $query .= "     L012.NAME1 AS ENT_COURSE, ";
        $query .= "     CASE WHEN SEQ022.REMARK2 = '1' THEN '辞退' END AS JITAI_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005 ";
        $query .= "          ON SEQ005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ005.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ005.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ008 ";
        $query .= "          ON SEQ008.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ008.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ008.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ008.SEQ          = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ029 ";
        $query .= "          ON SEQ029.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ029.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ029.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ029.SEQ          = '029' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ030 ";
        $query .= "          ON SEQ030.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ030.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ030.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ030.SEQ          = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ";
        $query .= "          ON SEQ022.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ022.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ022.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ022.SEQ          = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TSTM ";
        $query .= "          ON SEQ030.ENTEXAMYEAR  = TSTM.ENTEXAMYEAR ";
        $query .= "         AND SEQ030.APPLICANTDIV = TSTM.APPLICANTDIV ";
        $query .= "         AND SEQ030.REMARK1      = TSTM.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "          AND RECEPT.EXAM_TYPE = '1' ";
        $query .= "          AND BASE.EXAMNO = RECEPT.EXAMNO ";

        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";

        if ($model->cmd == "back" || $model->cmd == "back1") { //「 << 」ボタン
            $query.= "     AND RECEPT.RECEPTNO < '".$model->search_examno."'";
            $query.= " ORDER BY ";
            $query.= "     RECEPT.RECEPTNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") { //「 >> 」ボタン
            $query.= "     AND RECEPT.RECEPTNO > '".$model->search_examno."'";
            $query.= " ORDER BY ";
            $query.= "     RECEPT.RECEPTNO ASC ";
        } elseif ($model->cmd == 'reference') { //検索ボタン
            $query.= "     AND RECEPT.RECEPTNO = '".$model->search_examno."'";
        } elseif ($model->cmd == 'reference2') { //かな検索ボタン
            $query.= "     AND BASE.EXAMNO = '".$model->examno."'";
        } else { //その他(更新後表示など)
            $query.= "     AND BASE.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //塾名
    public function getPriSchoolName($priSchoolCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    public function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
        return $query;
    }

    /***** 追加更新処理（上画面） *****/
    //存在チェック
    public function getExistdataCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";
        return $query;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = "1";
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"];
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"];
        $data["BIRTHDAY"][TEXT]      = $model->field["BIRTHDAY"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"])) ? sprintf("%07s", $model->field["FS_CD"]) : "";
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = sprintf("%02d", $model->field["FS_Y"]);
        $data["FS_M"][TEXT]          = sprintf("%02d", $model->field["FS_M"]);
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO       = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "   AND EXAMNO       = '".$model->examno."' ";
            $db->query($query);
        }
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["GNAME"][TEXT]            = $model->field["GNAME"];
        $data["GKANA"][TEXT]            = $model->field["GKANA"];
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["EMERGENCYTELNO"][TEXT]   = $model->field["EMERGENCYTELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount005 = $db->getOne(knjl011aQuery::getCountDetail($model, "005"));
        $getCount008 = $db->getOne(knjl011aQuery::getCountDetail($model, "008"));
        $getCount029 = $db->getOne(knjl011aQuery::getCountDetail($model, "029"));
        //where
        $getWhere005 = knjl011aQuery::getWhereDetail($model, "005");
        $getWhere008 = knjl011aQuery::getWhereDetail($model, "008");
        $getWhere029 = knjl011aQuery::getWhereDetail($model, "029");

        //塾・教室(PRISCHOOL_MST、PRISCHOOL_CLASS_MST)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["PRISCHOOLCD1"];
        $data["REMARK3"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD1"];
        $data["REMARK4"][TEXT]              = $model->field["PRISCHOOLCD2"];
        $data["REMARK5"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD2"];
        $data["REMARK6"][TEXT]              = $model->field["PRISCHOOLCD3"];
        $data["REMARK7"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount008 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "008";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere008);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getCountDetail($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    public function getWhereDetail($model, $seq)
    {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //--- DELETE
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "            AND EXAMNO       = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "            AND EXAMNO       = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_INTERVIEW_DAT
        $query  = "DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /***** 表示処理（下画面） *****/

    //受験種別コンボ
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //志望コースコンボ
    public function getCourseCmb($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L{$model->schoolKind}58' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //受験科目取得
    public function getExamType($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //リスト取得
    public function getReceptList($model)
    {
        $query  = " SELECT ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R2.REMARK1 AS SHDIV, ";
        $query .= "     R2.REMARK2 AS DESIREDIV, ";
        $query .= "     R2.REMARK3 AS SUBCLASS_TYPE, ";
        $query .= "     M1.TESTDIV_NAME AS TEST_NAME, ";
        $query .= "     M1.TEST_DATE1 AS TEST_DATE, ";
        $query .= "     N1.NAME1 AS SHDIV_NAME, ";
        $query .= "     N2.NAME1 AS DESIREDIV_NAME, ";
        $query .= "     M2.EXAMTYPE_NAME AS SUBCLASS_TYPE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R2 ";
        $query .= "          ON R2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND R2.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND R2.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND R2.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND R2.SEQ          = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST M1 ";
        $query .= "          ON M1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND M1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND M1.TESTDIV      = R1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_MST M2 ";
        $query .= "          ON M2.ENTEXAMYEAR  = R2.ENTEXAMYEAR ";
        $query .= "         AND M2.APPLICANTDIV = R2.APPLICANTDIV ";
        $query .= "         AND M2.EXAM_TYPE    = R2.REMARK3 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L006' ";
        $query .= "         AND N1.NAMECD2 = R2.REMARK1 ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "          ON N2.YEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND N2.NAMECD1 = 'L{$model->schoolKind}58' ";
        $query .= "         AND N2.NAMECD2 = R2.REMARK2 ";
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        $query .= "     AND R1.EXAMNO       = '{$model->examno}' ";
        $query .= " ORDER BY ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO ";
        return $query;
    }

    /***** 追加更新処理（下画面） *****/
    //重複チェック
    //追加ボタンを押した時、同じ試験には追加できません。
    public function getReceptCheck1($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        return $query;
    }
    //重複・存在チェック
    public function getReceptCheck2($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        return $query;
    }

    //追加・更新
    public function &getUpdateQuery2($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_RECEPT_DAT
        //where
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $where .= "     AND EXAM_TYPE       = '1' ";
        $where .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        //cnt
        $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT ".$where);

        $data = array();
        $data["EXAMNO"][TEXT]               = $model->examno;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->field2["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = "1";
            $data["RECEPTNO"][TEXT]         = $model->field2["RECEPTNO"];
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_RECEPT_DETAIL_DAT
        //seq
        $seq = "006";
        //where
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $where .= "     AND EXAM_TYPE       = '1' ";
        $where .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        $where .= "     AND SEQ             = '{$seq}' ";
        //cnt
        $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_RECEPT_DETAIL_DAT ".$where);

        //専併区分、志望コース、受験型
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field2["SHDIV"];
        $data["REMARK2"][TEXT]              = $model->field2["DESIREDIV"];
        $data["REMARK3"][TEXT]              = $model->field2["SUBCLASS_TYPE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->field2["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = "1";
            $data["RECEPTNO"][TEXT]         = $model->field2["RECEPTNO"];
            $data["SEQ"][TEXT]              = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function getDeleteQuery2($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_RECEPT_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        $db->query($query);

        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->field2["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$model->field2["RECEPTNO"]}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
