<?php
class knjl511iQuery extends Query
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
        $query .= "     BD012.REMARK3 AS ALT_EXAMNO3, ";
        $query .= "     COND009.REMARK2 AS CON009_REMARK2, ";
        $query .= "     COND009.REMARK3 AS CON009_REMARK3, ";
        $query .= "     COND009.REMARK1 AS CON009_REMARK1, ";
        $query .= "     COND004.REMARK1 AS CON004_REMARK1, ";
        $query .= "     COND004.REMARK2 AS CON004_REMARK2, ";
        $query .= "     COND005.REMARK5 AS CON005_REMARK5 ";
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
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT COND004 ";
        $query .= "            ON COND004.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND COND004.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND COND004.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND COND004.SEQ          = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT COND005 ";
        $query .= "            ON COND005.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND COND005.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND COND005.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND COND005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT COND009 ";
        $query .= "            ON COND009.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND COND009.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND COND009.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND COND009.SEQ          = '009' ";
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

    //部活マスタよりデータを取得
    public function getClubMst($clubcd = "")
    {
        $query  = " SELECT ";
        $query .= "   *, ";
        $query .= "   CLUBCD || ':' || VALUE(CLUBNAME, '') AS LABEL,  ";
        $query .= "   CLUBCD AS VALUE ";
        $query .= "   ";
        $query .= " FROM ";
        $query .= "   CLUB_MST T1 ";
        if ($clubcd) {
            $query .= " WHERE ";
            $query .= "   CLUBCD = '".$clubcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   CLUBCD ";
        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新 ENTEXAM_APPLICANTCONFRPT_DAT
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR       = '".$model->year."' ";
        $where .= "     AND APPLICANTDIV  = '".$model->applicantdiv."' ";
        $where .= "     AND EXAMNO        = '".$model->examno."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTCONFRPT_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt == 0) {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            $db->query($query);
        }

        //更新 ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $seq = "009";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$model->examno."' ";
        $where .= " AND SEQ          = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK2"][TEXT]          = $model->field["CON009_REMARK2"];  //特待コード
        $data["REMARK3"][TEXT]          = $model->field["CON009_REMARK3"];  //特待理由コード
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt == 0) {
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
