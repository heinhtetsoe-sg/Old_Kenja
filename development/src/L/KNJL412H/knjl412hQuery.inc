<?php
class knjl412hQuery extends Query
{

    //名称マスタよりデータを取得
    public function getNameCd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     ABBV1, ";
        $query .= "     ABBV2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        if ($cd == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SETTING_CD   = '".$settingCd."' ";
        if ($seq) {
            $query .= "     AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //ENTEXAM_EXAMTYPE_MSTより取得
    public function getExamtypeMst($model, $hopeNum, $examType = "")
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND REMARK1      = '".$hopeNum."' ";
        if ($examType) {
            $query .= " AND EXAM_TYPE = '".$examType."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ENTEXAM_COURSE_MSTより取得
    public function getCourseMst($model, $testdiv, $course = "")
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$testdiv."' ";
        if ($course) {
            $query .= " AND EXAMCOURSECD  = '".$course."' ";
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
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV AS TEST_L_DIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_ERACD, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     BD008.REMARK1 AS PRISCHOOLCD, ";
        $query .= "     BD008.REMARK4 AS PRISCHOOL_CLASS_CD, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GRELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.EMERGENCYTELNO, ";
        $query .= "     T2.EMERGENCYCALL, ";
        $query .= "     BD009.REMARK2 AS USERSEQ, ";
        $query .= "     BD009.REMARK3 AS MAIL_LOGIN, ";
        $query .= "     BD009.REMARK4 AS MAIL_APP, ";
        $query .= "     BD005.REMARK1 AS SEQ005_REAMRK1, ";
        $query .= "     BD005.REMARK2 AS SEQ005_REAMRK2, ";
        $query .= "     BD005.REMARK3 AS SEQ005_REAMRK3, ";
        $query .= "     BD005.REMARK4 AS SEQ005_REAMRK4, ";
        $query .= "     BD005.REMARK5 AS SEQ005_REAMRK5, ";
        $query .= "     BD005.REMARK6 AS SEQ005_REAMRK6 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " INNER JOIN (SELECT DISTINCT ENTEXAMYEAR, APPLICANTDIV, EXAMNO, RECEPTNO ";
        $query .= "               FROM ENTEXAM_RECEPT_DAT) RECEPT ";
        $query .= "         ON RECEPT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "        AND RECEPT.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "        AND RECEPT.EXAMNO       = T1.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "        ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "       AND T1.EXAMNO      = T2.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ";
        $query .= "        ON BD009.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "       AND BD009.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "       AND BD009.EXAMNO       = T1.EXAMNO ";
        $query .= "       AND BD009.SEQ          = '009' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "        ON BD005.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "       AND BD005.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "       AND BD005.EXAMNO       = T1.EXAMNO ";
        $query .= "       AND BD005.SEQ          = '005' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD008 ";
        $query .= "        ON BD008.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "       AND BD008.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "       AND BD008.EXAMNO       = T1.EXAMNO ";
        $query .= "       AND BD008.SEQ          = '008' ";
        $query .= " LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "        ON FIN.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " RECEPT.RECEPTNO < '".$model->receptno."' ";
            $query.= " ORDER BY ";
            $query.= "     RECEPT.RECEPTNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " RECEPT.RECEPTNO > '".$model->receptno."' ";
            $query.= " ORDER BY ";
            $query.= "     RECEPT.RECEPTNO ASC ";
        } else {
            $query.= " RECEPT.RECEPTNO = '".$model->receptno."' ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //資格マスタ
    public function getQualifiedMst()
    {
        $query  = " SELECT ";
        $query .= "     QUALIFIED_CD || ' ' || QUALIFIED_NAME AS LABEL, ";
        $query .= "     QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     CONDITION_DIV = '2' "; //設定区分’2’のデータをコンボ表示する。
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //資格マスタ(MANAGEMENT_FLG)
    public function getManagementFlg($qualifiedCd)
    {
        $query  = " SELECT ";
        $query .= "     MANAGEMENT_FLG ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     AND QUALIFIED_CD = '".$qualifiedCd."' ";

        return $query;
    }

    //級・段位(QUALIFIED_RESULT_MST)
    public function getRankResultMst($model, $qualifiedCd)
    {
        $query  = " SELECT ";
        $query .= "     RESULT_CD || ' ' || RESULT_NAME AS LABEL, ";
        $query .= "     RESULT_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->year."' ";
        $query .= "     AND QUALIFIED_CD = '".$qualifiedCd."' ";
        $query .= "     AND CERT_FLG     = 'T' ";//正式フラグが立っているもの

        return $query;
    }
    
    //選択した級・段位
    public function getSelectedRank($model, $qualifiedCd)
    {
        $query .= " SELECT ";
        $query .= "     H312.NAMECD2 AS VALUE, ";
        $query .= "     H312.NAMECD2 || ' ' || H312.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT T1 ";
        $query .= "     INNER JOIN V_NAME_MST H312 ";
        $query .= "             ON H312.NAMECD2 = T1.RANK ";
        $query .= "            AND H312.YEAR    = '".$model->year."' ";
        $query .= "            AND H312.NAMECD1 = 'H312' ";
        $query .= " WHERE ";
        $query .= "     T1.QUALIFIED_CD = '" . $qualifiedCd . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //選択した級・段位（QUALIFIED_RANK_DAT に1件も無かった場合）
    public function getSelectedRank2($model)
    {
        $query .= " SELECT ";
        $query .= "     H312.NAMECD2 AS VALUE, ";
        $query .= "     H312.NAMECD2 || ' ' || H312.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST H312 ";
        $query .= " WHERE ";
        $query .= "     H312.YEAR    = '".$model->year."' ";
        $query .= " AND H312.NAMECD1 = 'H312' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
    
        return $query;
    }

    //出願内容の取得
    public function getHopeInfo($model, $hopeNum)
    {
        $query  = " SELECT ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.EXAM_TYPE, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     RD016.REMARK1 AS HOPE_COURSE1, ";
        $query .= "     RD016.REMARK2 AS HOPE_TESTDIV1, ";
        $query .= "     RD017.REMARK1 AS HOPE_COURSE2, ";
        $query .= "     RD017.REMARK2 AS HOPE_TESTDIV2, ";
        $query .= "     RD018.REMARK1 AS HOPE_COURSE3, ";
        $query .= "     RD018.REMARK2 AS HOPE_TESTDIV3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD016 ";
        $query .= "            ON RD016.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD016.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD016.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD016.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD016.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD016.SEQ          = '016' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD017 ";
        $query .= "            ON RD017.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD017.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD017.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD017.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD017.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD017.SEQ          = '017' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD018 ";
        $query .= "            ON RD018.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD018.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD018.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD018.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD018.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD018.SEQ          = '018' ";
        $query .= " WHERE ";
        $query .= "       R1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND R1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND R1.TESTDIV      = '".$hopeNum."' ";
        $query .= "   AND R1.EXAMNO       = '".$model->examno."' ";
        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME, ";
        $query .= "         CASE WHEN L4.NAME1 IS NOT NULL ";
        $query .= "              THEN '(' || L4.NAME1 || ')' ";
        $query .= "              ELSE '' ";
        $query .= "         END FINSCHOOL_TYPE_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "         LEFT JOIN NAME_MST L4 ON  L4.NAMECD1 = 'L019' ";
        $query .= "                               AND L4.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //存在チェック
    public function existdata($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     EXAMNO = '".$model->examno."' ";
        return $query;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用情報取得(和暦→西暦)
        $result = $db->query(knjl412hQuery::getCalendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d", $model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d", $model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(NAME_KANA) FROM ENTEXAM_APPLICANTBASE_DAT " .$where);
        //UPDATE
        $data = array();
        $data["NAME_KANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query.= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["GNAME"][TEXT]            = $model->field["GNAME"];
        $data["GKANA"][TEXT]            = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]           = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]        = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]        = $model->field["GADDRESS2"];
        $data["EMERGENCYTELNO"][TEXT]   = $model->field["EMERGENCYTELNO"];
        $data["EMERGENCYCALL"][TEXT]    = $model->field["EMERGENCYCALL"];
        $data["GRELATIONSHIP"][TEXT]    = $model->field["GRELATIONSHIP"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //半角カナを全角カナに変換し再度更新
        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."' ";
        $kana = $db->getOne(" SELECT TRANSLATE_HK_K(GKANA) FROM ENTEXAM_APPLICANTADDR_DAT " .$where);
        //UPDATE
        $data = array();
        $data["GKANA"][TEXT] = $kana;
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ ： 005
        $seq = "005";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK1"][TEXT]      = $model->field["SEQ005_REAMRK1"];
        $data["REMARK2"][TEXT]      = $model->field["SEQ005_REAMRK2"];
        $data["REMARK3"][TEXT]      = $model->field["SEQ005_REAMRK3"];
        $data["REMARK4"][TEXT]      = $model->field["SEQ005_REAMRK4"];
        $data["REMARK5"][TEXT]      = $model->field["SEQ005_REAMRK5"];
        $data["REMARK6"][TEXT]      = $model->field["SEQ005_REAMRK6"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."' ";
        $where .= "   AND SEQ          = '".$seq."' ";
        
        //件数の取得
        $getCnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where);
        if ($getCnt != 0) {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ ： 009
        $seq = "009";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK2"][TEXT]      = $model->field["USERSEQ"];
        $data["REMARK3"][TEXT]      = $model->field["MAIL_LOGIN"];
        $data["REMARK4"][TEXT]      = $model->field["MAIL_APP"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO      = '".$model->examno."' ";
        $where .= "   AND SEQ         = '".$seq."' ";
        
        //件数の取得
        $getCnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where);
        if ($getCnt != 0) {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }
        $db->query($query);


        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ ： 008
        $seq = "008";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = $seq;
        $data["REMARK1"][TEXT]      = $model->field["PRISCHOOLCD"];
        $data["REMARK4"][TEXT]      = $model->field["PRISCHOOL_CLASS_CD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO      = '".$model->examno."' ";
        $where .= "   AND SEQ         = '".$seq."' ";
        
        //件数の取得
        $getCnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT " .$where);
        if ($getCnt != 0) {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }
        $db->query($query);

        //------------------------------出願内容（更新前に削除）----------------------
        //対象者の出願内容を取得
        $query  = " SELECT * FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($hopeCourseNum = 1; $hopeCourseNum <= $model->maxHopeCourseNum; $hopeCourseNum++) {
                //DELETE -- ENTEXAM_RECEPT_DETAIL_DAT SEQ ： 016 ～ 018
                $seq = ($hopeCourseNum == 1) ? "016" : (($hopeCourseNum == 2) ? "017" : "018");
                $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
                $query .= " WHERE ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
                $query .= "   AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
                $query .= "   AND TESTDIV      = '".$row["TESTDIV"]."' ";
                $query .= "   AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
                $query .= "   AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
                $query .= "   AND SEQ          = '".$seq."' ";
                $db->query($query);
            }
        }
        //DELETE -- ENTEXAM_RECEPT_DAT SEQ
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);
        

        //------------------------------出願内容-------------------------------------
        for ($hopeNum = 1; $hopeNum <= $model->maxHopeNum; $hopeNum++) {
            $hopeReceptno = "HOPE".$hopeNum."_RECEPTNO";
            $hopeExamType = "HOPE".$hopeNum."_EXAM_TYPE";

            //受験番号、受験型 いずれかが未入力の場合、処理を行わない
            if (!($model->field[$hopeReceptno]) || !($model->field[$hopeExamType])) {
                continue;
            }

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $hopeNum; //第１回 ～ 第５回
            $data["RECEPTNO"][TEXT]     = $model->field[$hopeReceptno];  //受験番号
            $data["EXAM_TYPE"][TEXT]    = $model->field[$hopeExamType]; //受験型
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT SEQ ： 016 ～ 018
            for ($hopeCourseNum = 1; $hopeCourseNum <= $model->maxHopeCourseNum; $hopeCourseNum++) {
                $field1 = "HOPE".$hopeNum."_COURSE".$hopeCourseNum;
                $field2 = "HOPE".$hopeNum."_TESTDIV".$hopeCourseNum;
                $seq = ($hopeCourseNum == 1) ? "016" : (($hopeCourseNum == 2) ? "017" : "018");
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]      = $hopeNum; //第１回 ～ 第５回
                $data["RECEPTNO"][TEXT]     = $model->field[$hopeReceptno];  //受験番号
                $data["EXAM_TYPE"][TEXT]    = $model->field[$hopeExamType]; //受験型
                $data["SEQ"][TEXT]          = $seq;
                $data["REMARK1"][TEXT]      = $model->field[$field1]; //希望コース
                $data["REMARK2"][TEXT]      = $model->field[$field2]; //出願区分
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
