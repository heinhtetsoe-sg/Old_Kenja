<?php
class knjl510iQuery extends Query
{

    //名称マスタ
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
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

    //１レコード取得
    public function getSelectQuery($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     BD032.REMARK1 AS CSV_BIRTHDAY, ";
        $query .= "     B1.SEX, ";
        $query .= "     BD031.REMARK5 AS SEQ031_REMARK5, ";
        $query .= "     B1.FS_CD AS FINSCHOOLCD, ";
        $query .= "     B1.FS_GRDYEAR, ";
        $query .= "     B1.FS_ERACD, ";
        $query .= "     B1.FS_Y, ";
        $query .= "     B1.FS_M, ";
        $query .= "     B1.FS_GRDDIV, ";
        $query .= "     B1.FS_DAY, ";
        $query .= "     ADDR1.GNAME, ";
        $query .= "     ADDR1.GKANA, ";
        $query .= "     ADDR1.GZIPCD, ";
        $query .= "     ADDR1.GADDRESS1, ";
        $query .= "     ADDR1.GADDRESS2, ";
        $query .= "     ADDR1.GTELNO, ";
        $query .= "     BD031.REMARK1 AS HOPE_COURSE1, ";
        $query .= "     BD031.REMARK2 AS HOPE_COURSE2, ";
        $query .= "     BD031.REMARK3 AS HOPE_COURSE3, ";
        $query .= "     BD031.REMARK4 AS HOPE_COURSE4, ";
        $query .= "     B1.DORMITORY_FLG, ";
        $query .= "     BD031.REMARK6 AS SEQ031_REMARK6, ";
        $query .= "     BD012.REMARK1 AS ALT_EXAMNO1, ";
        $query .= "     BD012.REMARK2 AS ALT_EXAMNO2, ";
        $query .= "     BD012.REMARK3 AS ALT_EXAMNO3 ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ";
        $query .= "            ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "           AND ADDR1.EXAMNO      = B1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "            ON BD031.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD031.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD031.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD031.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD012 ";
        $query .= "            ON BD012.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD012.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD012.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD012.SEQ          = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD032 ";
        $query .= "            ON BD032.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD032.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD032.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD032.SEQ          = '032' ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($model->cmd == "back1") {
            $query .= " AND B1.EXAMNO < '".$examno."' ";
            $query.= " ORDER BY ";
            $query.= "     B1.EXAMNO DESC ";
        } elseif ($model->cmd == "next1") {
            $query .= " AND B1.EXAMNO > '".$examno."' ";
            $query.= " ORDER BY ";
            $query.= "     B1.EXAMNO ASC ";
        } else {
            $query .= " AND B1.EXAMNO = '".$examno."' ";
        }
        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得
    public function getEntexamSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND SETTING_CD   = '".$namecd1."' ";
        if ($seq) {
            $query .= " AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //ENTEXAM_GENERAL_MSTよりデータを取得
    public function getGeneralMst($model, $generalDiv, $generalCd = "")
    {
        $query  = " SELECT ";
        $query .= "   *, ";
        $query .= "   GENERAL_CD || ':' || VALUE(GENERAL_NAME, '') AS LABEL,  ";
        $query .= "   GENERAL_CD AS VALUE ";
        $query .= "   ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_GENERAL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND TESTDIV      = '0' ";
        $query .= " AND GENERAL_DIV  = '".$generalDiv."' ";
        if ($generalCd) {
            $query .= " AND GENERAL_CD = '".$generalCd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   GENERAL_CD ";
        return $query;
    }

    //コースに対応する類別・学科を取得
    public function getCourseDetailInfoQuery($model)
    {
        $query  = "   SELECT ";
        $query .= "       G1.GENERAL_CD, ";
        $query .= "       G1.GENERAL_NAME, ";
        $query .= "       G1.REMARK1 AS HOPE_CLASSIFICATION, ";
        $query .= "       G2.REMARK1 AS TESTDIV0 ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_GENERAL_MST G1 ";
        $query .= "       LEFT JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "           ON G2.ENTEXAMYEAR     = G1.ENTEXAMYEAR ";
        $query .= "           AND G2.APPLICANTDIV   = G1.APPLICANTDIV ";
        $query .= "           AND G2.TESTDIV        = '0' ";
        $query .= "           AND G2.GENERAL_DIV    = '01' ";
        $query .= "           AND G2.GENERAL_CD     = G1.REMARK1 ";
        $query .= "   WHERE ";
        $query .= "       G1.ENTEXAMYEAR        = '".$model->year."' ";
        $query .= "       AND G1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "       AND G1.TESTDIV        = '0' ";
        $query .= "       AND G1.GENERAL_DIV    = '02' ";
        $query .= "       AND G2.REMARK1        = '{$model->field["TESTDIV0"]}' "; //学科

        return $query;
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

    //ENTEXAM_APPLICANTBASE_DATの存在チェック
    public function getExamnoExist($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     B1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND B1.EXAMNO       = '".$examno."' ";
            
        return $query;
    }

    //和暦変換
    public function getWarekiCd($model, $setDay)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = 'L007' ";
        $query .= " AND '{$setDay}' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $model->field["FINSCHOOLCD"] = (strlen($model->field["FINSCHOOLCD"])) ? sprintf("%07s", $model->field["FINSCHOOLCD"]) : "";

        //生年月日セット（西暦→和暦）
        $birthRow = $db->getRow(knjl510iQuery::getWarekiCd($model, $model->field["BIRTHDAY"]), DB_FETCHMODE_ASSOC);
        $eraCd    = $birthRow['NAMECD2']; //元号
        list($bYear, $bMonth, $bDay) = explode('/', $model->field["BIRTHDAY"]);
        $eraYear  = sprintf("%02d", $bYear - $birthRow['NAMESPARE1'] + 1);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["TESTDIV0"][TEXT]         = $model->field["TESTDIV0"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]            = $eraCd;
        $data["BIRTH_Y"][TEXT]          = $eraYear;
        $data["BIRTH_M"][TEXT]          = $bMonth;
        $data["BIRTH_D"][TEXT]          = $bDay;
        $data["BIRTHDAY"][DATE]         = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["FS_CD"][TEXT]            = $model->field["FINSCHOOLCD"];
        $data["FS_GRDYEAR"][TEXT]       = $model->field["FS_GRDYEAR"];
        $data["DORMITORY_FLG"][TEXT]    = $model->field["DORMITORY_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd == "add") {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SHDIV"][TEXT]            = "1";
            $data["DESIREDIV"][TEXT]        = "1";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        } else {
            //更新
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
            $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= " AND EXAMNO       = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ZIPCD"][TEXT]            = $model->field["GZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["GADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["GADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["GTELNO"];
        $data["GNAME"][TEXT]            = $model->field["GNAME"];
        $data["GKANA"][TEXT]            = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]           = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]        = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]        = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]           = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$model->examno."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTADDR_DAT ".$where);

        if ($cnt == 0) {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        }
        $db->query($query);

        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT
        knjl510iQuery::getUpdateEntexamApplicantbaseDetailDat($db, $model, "031");
        knjl510iQuery::getUpdateEntexamApplicantbaseDetailDat($db, $model, "012");

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //UPDATE -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function &getUpdateEntexamApplicantbaseDetailDat($db, $model, $seq)
    {
        if ($seq == "") {
            return;
        }
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$model->examno."' ";
        $where .= " AND SEQ          = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = $seq;
        if ($seq == "031") {
            $age = ($model->year."0401" - str_replace("/", "", $model->field["BIRTHDAY"])) / 10000; //年齢取得

            $data["REMARK1"][TEXT]  = $model->field["HOPE_COURSE1"];    //第一志望
            $data["REMARK2"][TEXT]  = $model->field["HOPE_COURSE2"];    //第二志望
            $data["REMARK3"][TEXT]  = $model->field["HOPE_COURSE3"];    //第三志望
            $data["REMARK4"][TEXT]  = $model->field["HOPE_COURSE4"];    //第四志望
            $data["REMARK5"][TEXT]  = $age > 16 ? "1" : "";             //過年度
            $data["REMARK6"][TEXT]  = $model->field["SEQ031_REMARK6"];  //奨学希望
            $data["REMARK7"][TEXT]  = $model->field["HOPE_CLASSIFICATION1"];    //第一志望 類別
            $data["REMARK8"][TEXT]  = $model->field["HOPE_CLASSIFICATION2"];    //第二志望 類別
            $data["REMARK9"][TEXT]  = $model->field["HOPE_CLASSIFICATION3"];    //第三志望 類別
            $data["REMARK10"][TEXT] = $model->field["HOPE_CLASSIFICATION4"];    //第四志望 類別
        } elseif ($seq == "012") {
            $data["REMARK1"][TEXT]  = $model->field["ALT_EXAMNO1"];     //他受験番号 専願
            $data["REMARK2"][TEXT]  = $model->field["ALT_EXAMNO2"];     //他受験番号 前期
            $data["REMARK3"][TEXT]  = $model->field["ALT_EXAMNO3"];     //他受験番号 後期
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt == 0) {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);
        }
    }
}
