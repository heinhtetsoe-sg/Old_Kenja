<?php
class knjl711hQuery extends Query
{
    //名称マスタよりデータを取得
    public function get_name_cd($year, $namecd1, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
   
    //入試設定マスタ取得
    public function getEntSettingMst($year, $applicantDiv, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ VALUE, ";
        $query .= "     SEQ || ':' || NAME1 LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$year}' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND SETTING_CD      = '{$settingCd}' ";
        if ($seq != "") {
            $query .= "     AND SEQ             = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getTestDiv($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL,  ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";


        return $query;
    }

    //入試種別取得
    public function getKindDiv($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     KINDDIV || ':' || VALUE(KINDDIV_NAME, '') AS LABEL, ";
        $query .= "      KINDDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格マスタ取得
    public function getQualifiedMst($year, $applicantDiv, $qualifiedCd = "")
    {
        $query  = "     SELECT ";
        $query .= "         QUALIFIED_JUDGE_CD || ':' || VALUE(QUALIFIED_ABBV, '') AS LABEL, ";
        $query .= "         QUALIFIED_JUDGE_CD AS VALUE, ";
        $query .= "         QUALIFIED_CD, ";
        $query .= "         QUALIFIED_JUDGE_CD, ";
        $query .= "         PLUS_POINT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_QUALIFIED_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$year}' ";
        $query .= "         AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($qualifiedCd) {
            $query .= "         AND QUALIFIED_CD = '{$qualifiedCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function get_calendarno($year, $namecd2 = "")
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
    public function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FINSCHOOL.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.EMAIL, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     SEQ005.REMARK2 AS CONCURRENT_APP_FLG, ";
        $query .= "     SEQ005.REMARK3 AS MULTI_APP_FLG, ";
        $query .= "     SEQ005.REMARK4 AS OVER_AGE_FLG, ";
        $query .= "     SEQ031.REMARK1 AS QUALIFIED_ENG_CD, ";
        $query .= "     SEQ031.REMARK2 AS QUALIFIED_KANJI_CD, ";
        $query .= "     SEQ031.REMARK3 AS QUALIFIED_MATH_CD, ";
        $query .= "     SEQ031.REMARK4 AS QUALIFIED_READING_CD, ";
        $query .= "     SEQ031.REMARK5 AS ATHLETIC_PRIZE_CD, ";
        $query .= "     SEQ031.REMARK6 AS CULTURE_PRIZE_CD, ";
        $query .= "     SEQ031.REMARK7 AS SCH_COUNCIL_PRIZE_CD, ";
        $query .= "     SEQ031.REMARK10 AS PLUS_POINT_TOTAL, ";
        $query .= "     SEQ033.REMARK1 AS BIKOU_REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO         = BASE.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON FINSCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005 ";
        $query .= "          ON SEQ005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ005.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ005.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ031 ";
        $query .= "          ON SEQ031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ031.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ031.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ031.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";

        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR    = '".$model->year."' AND ";
        $query .= "     BASE.APPLICANTDIV   = '".$model->applicantDiv."' AND ";
        $query .= "     BASE.TESTDIV        = '".$model->testDiv."' AND ";
        $query .= "     SEQ005.REMARK1      = '".$model->kindDiv."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     BASE.EXAMNO < '".$model->examNo."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     BASE.EXAMNO > '".$model->examNo."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "     BASE.EXAMNO = '".$model->examNo."'";
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
    //出身学校所在地
    public function getFinLocationMst($cd)
    {
        $query  = " SELECT ";
        $query .= "     DISTRICT_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " WHERE ";
        $query .= "     DISTRICTCD = '{$cd}' ";
        return $query;
    }

    //存在チェック
    public function getExistdataCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV    = '".$model->applicantDiv."' ";
        $query .= "     AND EXAMNO          = '".$model->examNo."' ";
        return $query;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["PLUS_POINT_TOTAL"] == "") { //加点欄が空欄の場合は加点合計を自動算出
            //加点算出
            $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv);
            $result = $db->query($query);
            $plusTotal = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $plusTotal += ($row["QUALIFIED_CD"] == "01" && $row["QUALIFIED_JUDGE_CD"] == $model->field["QUALIFIED_ENG_CD"]) ? $row["PLUS_POINT"] : 0; //資格1
                $plusTotal += ($row["QUALIFIED_CD"] == "02" && $row["QUALIFIED_JUDGE_CD"] == $model->field["QUALIFIED_KANJI_CD"]) ? $row["PLUS_POINT"] : 0; //資格2
                $plusTotal += ($row["QUALIFIED_CD"] == "03" && $row["QUALIFIED_JUDGE_CD"] == $model->field["QUALIFIED_MATH_CD"]) ? $row["PLUS_POINT"] : 0; //資格3
                $plusTotal += ($row["QUALIFIED_CD"] == "04" && $row["QUALIFIED_JUDGE_CD"] == $model->field["QUALIFIED_READING_CD"]) ? $row["PLUS_POINT"] : 0; //資格4
                $plusTotal += ($row["QUALIFIED_CD"] == "05" && $row["QUALIFIED_JUDGE_CD"] == $model->field["ATHLETIC_PRIZE_CD"]) ? $row["PLUS_POINT"] : 0; //運動
                $plusTotal += ($row["QUALIFIED_CD"] == "06" && $row["QUALIFIED_JUDGE_CD"] == $model->field["CULTURE_PRIZE_CD"]) ? $row["PLUS_POINT"] : 0; //文化
                $plusTotal += ($row["QUALIFIED_CD"] == "07" && $row["QUALIFIED_JUDGE_CD"] == $model->field["SCH_COUNCIL_PRIZE_CD"]) ? $row["PLUS_POINT"] : 0; //生徒会
            }
            $model->field["PLUS_POINT_TOTAL"] = $plusTotal;
        }

        //ENTEXAM_APPLICANTBASE_DAT
        knjl711hQuery::updateApplicantBase($db, $model);

        //ENTEXAM_APPLICANTADDRL_DAT
        knjl711hQuery::updateApplicantAddr($db, $model);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005
        knjl711hQuery::updateBaseDetail005($db, $model);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ031
        knjl711hQuery::updateBaseDetail031($db, $model);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033
        knjl711hQuery::updateBaseDetail033($db, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function updateApplicantBase($db, $model)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantDiv;
        $data["EXAMNO"][TEXT]        = $model->examNo;
        $data["TESTDIV"][TEXT]       = $model->testDiv;
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"])) ? sprintf("%07s", $model->field["FS_CD"]) : "";
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $where .= "   AND EXAMNO        = '".$model->examNo."' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        $db->query($query);
    }

    public function updateApplicantAddr($db, $model)
    {
        //UPDATE --ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $where .= "   AND EXAMNO        = '".$model->examNo."' ";
        
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        $db->query($query);
    }

    public function updateBaseDetail005($db, $model)
    {
        $seq = "005";
       
        $data = array();
        $data["REMARK2"][TEXT]              = $model->field["CONCURRENT_APP_FLG"];
        $data["REMARK3"][TEXT]              = $model->field["MULTI_APP_FLG"];
        $data["REMARK4"][TEXT]              = $model->field["OVER_AGE_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = knjl711hQuery::getCountDetail($model, $seq);
        $existsCnt = $db->getOne($query);
        if ($existsCnt == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantDiv;
            $data["EXAMNO"][TEXT]           = $model->examNo;
            $data["SEQ"][TEXT]              = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where = knjl711hQuery::getWhereDetail($model, $seq);
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);
    }

    public function updateBaseDetail031($db, $model)
    {
        $seq = "031";

        $data = array();
        $data["REMARK1"][TEXT]    = $model->field["QUALIFIED_ENG_CD"];
        $data["REMARK2"][TEXT]    = $model->field["QUALIFIED_KANJI_CD"];
        $data["REMARK3"][TEXT]    = $model->field["QUALIFIED_MATH_CD"];
        $data["REMARK4"][TEXT]    = $model->field["QUALIFIED_READING_CD"];
        $data["REMARK5"][TEXT]    = $model->field["ATHLETIC_PRIZE_CD"];
        $data["REMARK6"][TEXT]    = $model->field["CULTURE_PRIZE_CD"];
        $data["REMARK7"][TEXT]    = $model->field["SCH_COUNCIL_PRIZE_CD"];
        $data["REMARK10"][TEXT]   = $model->field["PLUS_POINT_TOTAL"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = knjl711hQuery::getCountDetail($model, $seq);
        $existsCnt = $db->getOne($query);
        if ($existsCnt == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantDiv;
            $data["EXAMNO"][TEXT]           = $model->examNo;
            $data["SEQ"][TEXT]              = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where = knjl711hQuery::getWhereDetail($model, $seq);
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);
    }

    public function updateBaseDetail033($db, $model)
    {
        $seq = "033";

        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["BIKOU_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = knjl711hQuery::getCountDetail($model, $seq);
        $existsCnt = $db->getOne($query);
        if ($existsCnt == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantDiv;
            $data["EXAMNO"][TEXT]           = $model->examNo;
            $data["SEQ"][TEXT]              = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where = knjl711hQuery::getWhereDetail($model, $seq);
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);
    }

    public function getCountDetail($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantDiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examNo}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    public function getWhereDetail($model, $seq)
    {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantDiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examNo}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $query .= "   AND EXAMNO        = '".$model->examNo."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $query .= "   AND EXAMNO        = '".$model->examNo."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $query .= "   AND EXAMNO        = '".$model->examNo."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $query .= "   AND EXAMNO        = '".$model->examNo."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "            AND EXAMNO       = '".$model->examNo."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        $query .= "   AND EXAMNO        = '".$model->examNo."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
