<?php

require_once('for_php7.php');

class knjl071aQuery extends Query
{

    //受験校種(J,H)を取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= "     AND NAMECD2 = '{$model->applicantdiv}' ";

        return $query;
    }

    //志望コースコンボ
    public function getCourseCmb($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L{$model->schoolKind}58' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //事前判定コースコンボ
    public function getJizenHanteiCmb($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L{$model->schoolKind}57' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //事前専願コースコンボ
    public function getJizenSenganCmb($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L{$model->schoolKind}58' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV  = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校コンボ
    public function getFinschoolMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     FS_CD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME_ABBV AS LABEL, ";
        $query .= "     NULL AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST FINS ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "         AND BASE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "         AND BASE.TESTDIV        = '1' ";
        $query .= "         AND BASE.FS_CD          = FINS.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     EXISTS ( ";
        $query .= "           SELECT ";
        $query .= "               'X' ";
        $query .= "           FROM ";
        $query .= "               ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "           WHERE ";
        $query .= "               RECEPT.ENTEXAMYEAR        = BASE.ENTEXAMYEAR ";
        $query .= "               AND RECEPT.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "               AND RECEPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "               AND RECEPT.EXAMNO         = BASE.EXAMNO ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待生区分取得
    public function getHonordiv($model)
    {
        $query  = " SELECT ";
        $query .= "     HONORDIV AS VALUE, ";
        $query .= "     HONORDIV || ':' || HONORDIV_NAME AS LABEL, ";
        $query .= "     VALUE(CLUB_FLG, '0') AS CLUB_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待区分マスタ取得
    public function getHonordivMst($model, $arrHonorType = null)
    {
        $query  = " SELECT ";
        $query .= "     HONOR_MST.HONORDIV AS VALUE, ";
        $query .= "     HONOR_MST.HONORDIV || ':' || HONOR_MST.HONORDIV_ABBV AS LABEL, ";
        $query .= "     HONOR_MST.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST HONOR_MST ";
        $query .= " WHERE ";
        $query .= "     HONOR_MST.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND HONOR_MST.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($arrHonorType) {
            $strHonorType  = "'";
            $strHonorType .= implode("','", $arrHonorType);
            $strHonorType .= "'";
            $query .= "     AND HONOR_MST.HONOR_TYPE IN (" . $strHonorType . ") ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待の優先順位取得
    public function getHonordivMstPriority($model)
    {
        $query  = " SELECT ";
        $query .= "     HONORDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     PRIORITY, ";
        $query .= "     HONORDIV ";

        return $query;
    }

    //クラブ取得
    public function getClubcd($schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     CLUB_M.CLUBCD AS VALUE, ";
        $query .= "     CLUB_M.CLUBCD || ':' || CLUB_M.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB_M ";
        $query .= " WHERE ";
        $query .= "     CLUB_M.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= " AND CLUB_M.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.ABBV1 AS SEX_ABBV, ";
        $query .= "     BASE.FS_CD, "; //出身学校コード
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     L006.NAME1 AS SHDIV_NAME, ";
        $query .= "     L058.ABBV1 AS COURSE_NAME, ";
        $query .= "     RCPT.TOTAL1 AS TOTAL, ";
        $query .= "     B029.REMARK7 AS KAKUTEI_TOKUTAI, "; //確定特待
        $query .= "     HNR7.HONORDIV_ABBV AS KAKUTEI_TOKUTAI_NAME, "; //確定特待 略称
        $query .= "     B029.REMARK1 AS JIZEN_TOKUTAI, "; //事前特待
        $query .= "     B029.REMARK2 AS TOKUTAI_SINSEI, "; //特待申請
        $query .= "     B029.REMARK3 AS SIKAKU_KATSUYO, "; //資格活用
        $query .= "     B029.REMARK7 AS KAKUTEI_TOKUTAI, "; //確定特待
        $query .= "     HNR1.HONORDIV_ABBV AS JIZEN_TOKUTAI_NAME, "; //事前特待 略称
        $query .= "     HNR2.HONORDIV_ABBV AS TOKUTAI_SINSEI_NAME, "; //特待申請 略称
        $query .= "     HNR3.HONORDIV_ABBV AS SIKAKU_KATSUYO_NAME, "; //資格活用 略称
        $query .= "     HNR1.CLUB_FLG AS CLUB_FLG1, ";
        $query .= "     HNR2.CLUB_FLG AS CLUB_FLG2, ";
        $query .= "     HNR3.CLUB_FLG AS CLUB_FLG3, ";
        $query .= "     B029.REMARK4 AS CLUB_CD, ";
        $query .= "     CLUB.CLUBNAME AS CLUB_NAME, ";
        $query .= "     B029.REMARK6 AS DESIREDIV, "; //事前判定
        $query .= "     B029.REMARK8 AS JIZEN_SENGAN, "; //事前専願
        $query .= "     B029.REMARK9 AS HONOR_REMARK, "; //特待備考
        $query .= "     NM_COURSE.ABBV1 AS DESIREDIV_NAME, "; //事前判定コース
        $query .= "     NM_JS_COURSE.ABBV1 AS JIZEN_SENGAN_NAME, "; //事前専願コース
        $query .= "     RCPT.EXAMNO "; //志願者SEQ
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ";
        $query .= "          ON B029.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B029.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B029.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B029.SEQ            = '029' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B005 ";
        $query .= "          ON B005.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND B005.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND B005.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND B005.SEQ            = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ";
        $query .= "          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND R006.TESTDIV        = RCPT.TESTDIV ";
        $query .= "         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ";
        $query .= "         AND R006.RECEPTNO       = RCPT.RECEPTNO ";
        $query .= "         AND R006.SEQ            = '006' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ";
        $query .= "          ON L006.YEAR       = R006.ENTEXAMYEAR ";
        $query .= "         AND L006.NAMECD1    = 'L006' ";
        $query .= "         AND L006.NAMECD2    = R006.REMARK1 ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ";
        $query .= "          ON L058.YEAR       = R006.ENTEXAMYEAR ";
        $query .= "         AND L058.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '58' ";
        $query .= "         AND L058.NAMECD2    = R006.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR1 ";
        $query .= "          ON HNR1.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR1.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR1.HONORDIV       = B029.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR2 ";
        $query .= "          ON HNR2.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR2.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR2.HONORDIV       = B029.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR3 ";
        $query .= "          ON HNR3.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR3.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR3.HONORDIV       = B029.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_HONORDIV_MST HNR7 ";
        $query .= "          ON HNR7.ENTEXAMYEAR    = B029.ENTEXAMYEAR ";
        $query .= "         AND HNR7.APPLICANTDIV   = B029.APPLICANTDIV ";
        $query .= "         AND HNR7.HONORDIV       = B029.REMARK7 ";
        $query .= "     LEFT JOIN CLUB_MST CLUB ";
        $query .= "          ON CLUB.SCHOOLCD       = '".SCHOOLCD."' ";
        $query .= "         AND CLUB.SCHOOL_KIND    = VALUE(L003.NAMESPARE3,'H') ";
        $query .= "         AND CLUB.CLUBCD         = B029.REMARK4 ";
        $query .= "     LEFT JOIN NAME_MST NM_COURSE ";
        $query .= "           ON NM_COURSE.NAMECD1 = 'L{$model->schoolKind}57' ";
        $query .= "          AND NM_COURSE.NAMECD2 = B029.REMARK6 ";
        $query .= "     LEFT JOIN V_NAME_MST NM_JS_COURSE ";
        $query .= "          ON NM_JS_COURSE.YEAR       = B029.ENTEXAMYEAR ";
        $query .= "         AND NM_JS_COURSE.NAMECD1    = 'L{$model->schoolKind}58' ";
        $query .= "         AND NM_JS_COURSE.NAMECD2    = B029.REMARK8 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '{$model->examType}' ";
        if ($model->syussinSchool != "ALL") {
            $query .= "     AND BASE.FS_CD = '".$model->syussinSchool."' ";
        }
        if ($model->sibouCourse != "ALL") {
            $query .= "     AND R006.REMARK2 = '".$model->sibouCourse."' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     CASE WHEN RCPT.TOTAL1 IS NULL THEN -1 ELSE RCPT.TOTAL1 END DESC, ";
        } elseif ($model->sort == "3") {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //存在チェック
    public function checkBaseDetail($model, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '" . $model->ObjYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
        if ($seq) {
            $query .= "     AND SEQ          = '".$seq."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //英検見なし得点取得
    public function getEikenMinasiTokuten($model, $honordiv)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR           = '" . $model->ObjYear . "' ";
        $query .= "     AND NAMESPARE1 = '" . $model->applicantdiv . "' ";
        $query .= "     AND NAMECD1    = 'L055' ";
        $query .= "     AND NAMESPARE3 = '" . $honordiv . "' ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック(SEQ:029)
        $detail029 = array();
        $seq029 = "029";
        $result = $db->query(knjl071aQuery::checkBaseDetail($model, $seq029));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detail029[$row["EXAMNO"]] = $row["CNT"];
        }
        $result->free();

        //存在チェック(SEQ:005)
        $detail005 = array();
        $seq005 = "005";
        $result = $db->query(knjl071aQuery::checkBaseDetail($model, $seq005));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detail005[$row["EXAMNO"]] = $row["CNT"];
        }
        $result->free();

        //更新
        foreach ($model->setReceptNo as $key => $ReceptNo) {
            $examno = $model->arrExamNo[$ReceptNo];

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ:029)
            $data = array();
            $data["REMARK7"][TEXT]      = $model->arrInput[$ReceptNo]["KAKUTEI_TOKUTAI"]; //確定特待
            $data["REMARK1"][TEXT]      = $model->arrInput[$ReceptNo]["JIZEN_TOKUTAI"]; //事前特待
            $data["REMARK2"][TEXT]      = $model->arrInput[$ReceptNo]["TOKUTAI_SINSEI"]; //特待申請
            $data["REMARK3"][TEXT]      = $model->arrInput[$ReceptNo]["SIKAKU_KATSUYO"]; //資格活用
            $data["REMARK6"][TEXT]      = $model->arrInput[$ReceptNo]["DESIREDIV"];
            $data["REMARK8"][TEXT]      = $model->arrInput[$ReceptNo]["JIZEN_SENGAN"]; //事前専願
            $data["REMARK4"][TEXT]      = $model->arrInput[$ReceptNo]["CLUB_CD"];
            $data["REMARK9"][TEXT]      = $model->arrInput[$ReceptNo]["HONOR_REMARK"]; //特待備考
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($detail029[$examno] > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
                $where .= "   AND SEQ           = $seq029 ";

                $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq029;

                $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
            }

            //英検見なし得点取得 特待区分コードは資格活用から取得
            $eikenMinasiTokuten = $db->getOne(knjl071aQuery::getEikenMinasiTokuten($model, $model->arrInput[$ReceptNo]["SIKAKU_KATSUYO"]));

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ:005)
            $data = array();
            $data["REMARK1"][TEXT]      = $eikenMinasiTokuten;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($detail005[$examno] > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
                $where .= "   AND SEQ           = $seq005 ";

                $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq005;

                $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
