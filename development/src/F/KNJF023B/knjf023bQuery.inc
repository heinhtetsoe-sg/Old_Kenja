<?php

require_once('for_php7.php');

class knjf023bQuery extends Query
{
    //最小学年（校種毎）
    public function getMinGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 AS MIN_GRADE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '{$model->getSchKind}' ";

        return $query;
    }
    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報取得
    public function getSchToothInfo($model)
    {
        list($grade, $hr_class) = explode('-', $model->grade_hr_class);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     L2.TOOTH_DATE, ";
        if ($model->screen == "1") {
            $query .= "     L3.JAWS_JOINTCD, ";
            $query .= "     L3.JAWS_JOINTCD2, ";
            $query .= "     L3.JAWS_JOINTCD3, ";
            $query .= "     L3.PLAQUECD, ";
            $query .= "     L3.GUMCD, ";
            $query .= "     L3.CALCULUS, ";
            $query .= "     L3.ORTHODONTICS, ";
            $query .= "     L3.BABYTOOTH, ";
            $query .= "     L3.REMAINBABYTOOTH, ";
            $query .= "     L3.TREATEDBABYTOOTH, ";
            $query .= "     L3.BRACK_BABYTOOTH, ";
            $query .= "     L3.ADULTTOOTH, ";
            $query .= "     L3.REMAINADULTTOOTH, ";
            $query .= "     L3.TREATEDADULTTOOTH, ";
            $query .= "     L3.LOSTADULTTOOTH, ";
            $query .= "     L3.BRACK_ADULTTOOTH, ";
            $query .= "     L3.OTHERDISEASECD, ";
            $query .= "     L3.OTHERDISEASE, ";
            if ($model->z010 == "miyagiken") {
                $query .= "     L3.OTHERDISEASECD3, ";
                $query .= "     L3.OTHERDISEASECD4, ";
            }
            if ($model->Properties["printKenkouSindanIppan"] != "2") {
                $query .= "     L3.OTHERDISEASECD2, ";
                $query .= "     L3.OTHERDISEASE2, ";
            }
            $query .= "     L3.DENTISTREMARKCD, ";
            $query .= "     L3.DENTISTREMARK, ";
            $query .= "     L3.DENTISTREMARKDATE, ";
            $query .= "     L3.DENTISTTREATCD, ";
            $query .= "     L3.DENTISTTREAT, ";
            $query .= "     L3.DENTISTTREAT2, ";
            $query .= "     L3.DENTISTTREAT3, ";
            $query .= "     L3.DENTISTREMARK_CO, ";
            $query .= "     L3.DENTISTREMARK_GO, ";
            $query .= "     L3.DENTISTREMARK_G ";
        } else {
            $cnt = 0;
            foreach ($model->shisiki as $key => $val) {
                $query .= ($cnt == 0) ? "" : ",";
                $query .= "     L3.".$key ;
                $cnt++;
            }
        }
        $query .= "    , DT002.TOOTH_REMARK3 AS OTHERDISEASE_REMARK1 ";
        $query .= "    , DT002.TOOTH_REMARK4 AS OTHERDISEASE_REMARK2 ";
        $query .= "    , DT002.TOOTH_REMARK5 AS OTHERDISEASE_REMARK3 ";
        $query .= "    , DT002.TOOTH_REMARK6 AS OTHERDISEASE_REMARK4 ";
        $query .= "    , DT003.TOOTH_REMARK3 AS OTHERDISEASECD3 ";
        $query .= "    , DT003.TOOTH_REMARK4 AS OTHERDISEASECD4 ";
        $query .= "    , DT003.TOOTH_REMARK5 AS OTHERDISEASE3 ";
        $query .= "    , DT003.TOOTH_REMARK6 AS OTHERDISEASE4 ";
        $query .= "    , DT005.TOOTH_REMARK1 AS DENTISTREMARK_REMARK1 ";
        $query .= "    , DT005.TOOTH_REMARK2 AS DENTISTREMARK_REMARK2 ";
        $query .= "    , DT005.TOOTH_REMARK3 AS DENTISTREMARK_REMARK3 ";
        $query .= "    , DT005.TOOTH_REMARK4 AS DENTISTREMARK_REMARK4 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_HDAT L2 ON L2.YEAR = T1.YEAR AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN V_MEDEXAM_TOOTH_DAT L3 ON L3.YEAR = T1.YEAR AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT002 ";
        $query .= "           ON DT002.YEAR      = T1.YEAR ";
        $query .= "          AND DT002.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT002.TOOTH_SEQ = '002' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT003 ";
        $query .= "           ON DT003.YEAR      = T1.YEAR ";
        $query .= "          AND DT003.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT003.TOOTH_SEQ = '003' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
        $query .= "           ON DT005.YEAR      = T1.YEAR ";
        $query .= "          AND DT005.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT005.TOOTH_SEQ = '005' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = '".$grade."' AND ";
        $query .= "     T1.HR_CLASS = '".$hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($model->screen == "1") {
            if ($model->z010 == "musashinohigashi" && ($namecd1 == "F511" || $namecd1 == "F510" || $namecd1 == "F520" || $namecd1 == "F513")) {
                $query .= "     NAME1 AS LABEL ";
            } else {
                $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL ";
            }
        } else {
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."'";

        return $query;
    }

    //名称マスタ(予備２取得)
    public function getNameMstSpare2($model, $setInNamecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 IN ({$setInNamecd1}) ";
        $query .= "     AND NAMESPARE2 = '1' ";
        return $query;
    }

    //名称マスタ取得(Z010)
    public function getZ010($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //データ有無チェック
    public function checkExistData($table, $schregno, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";
        if ($seq) {
            $query .= "     AND TOOTH_SEQ = '{$seq}' ";
        }

        return $query;
    }

    //更新 -- MEDEXAM_HDAT
    public function updateMedexamHdat($model, $g, $flg)
    {
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]     = $model->fields["SCHREGNO"][$g];
        }
        $data["TOOTH_DATE"][TEXT]   = str_replace("/", "-", $model->fields["TOOTH_DATE"][$g]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "MEDEXAM_HDAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->fields["SCHREGNO"][$g]."' ";

            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        }

        return $query;
    }

    //更新 -- MEDEXAM_TOOTH_DAT
    public function updateMedexamToothDat($model, $g, $flg)
    {
        //「学校歯科医・日付」が設定されていないとき、「健康診断実施日付」をセットする
        //近大は「学校歯科医・日付」が設定されていないとき、nullのまま更新する
        if ($model->z010 !== 'KINDAI' && $model->z010 !== 'KINJUNIOR') {
            if (!$model->fields["DENTISTREMARKDATE"][$g]) {
                $model->fields["DENTISTREMARKDATE"][$g] = $model->fields["TOOTH_DATE"][$g];
            }
        }
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $model->fields["SCHREGNO"][$g];
        }
        if ($model->screen == "1") {
            $data["JAWS_JOINTCD"][TEXT]         = $model->fields["JAWS_JOINTCD"][$g];
            $data["JAWS_JOINTCD3"][TEXT]        = $model->fields["JAWS_JOINTCD3"][$g];
            $data["JAWS_JOINTCD2"][TEXT]        = $model->fields["JAWS_JOINTCD2"][$g];
            $data["PLAQUECD"][TEXT]             = $model->fields["PLAQUECD"][$g];
            $data["GUMCD"][TEXT]                = $model->fields["GUMCD"][$g];
            $data["CALCULUS"][TEXT]             = $model->fields["CALCULUS"][$g];
            $data["ORTHODONTICS"][TEXT]         = ($model->fields["ORTHODONTICS"][$g]) ? 1 : 0;
            foreach ($model->cntItem as $key => $val) {
                $data[$key][NUMBER]   = $model->fields[$key][$g];
            }
            $data["OTHERDISEASECD"][TEXT]       = $model->fields["OTHERDISEASECD"][$g];
            $data["OTHERDISEASE"][TEXT]         = $model->fields["OTHERDISEASE"][$g];
            $data["DENTISTREMARKCD"][TEXT]      = $model->fields["DENTISTREMARKCD"][$g];
            $data["DENTISTREMARK"][TEXT]        = $model->fields["DENTISTREMARK"][$g];
            $data["DENTISTREMARKDATE"][TEXT]    = str_replace("/", "-", $model->fields["DENTISTREMARKDATE"][$g]);
            $data["DENTISTTREATCD"][TEXT]       = $model->fields["DENTISTTREATCD"][$g];
            $data["DENTISTTREAT"][TEXT]         = $model->fields["DENTISTTREAT"][$g];

            $data["DENTISTTREAT"][TEXT]         = $model->fields["DENTISTTREAT"][$g];
            $data["DENTISTREMARK_CO"][TEXT]     = $model->fields["DENTISTREMARK_CO"][$g];
            $data["DENTISTREMARK_GO"][TEXT]     = $model->fields["DENTISTREMARK_GO"][$g];
            $data["DENTISTREMARK_G"][TEXT]      = $model->fields["DENTISTREMARK_G"][$g];
        } else {
            foreach ($model->shisiki as $key => $val) {
                $data[$key][TEXT]   = $model->fields[$key][$g];
            }
            foreach ($model->cntItem as $key => $val) {
                $data[$key][NUMBER]   = $model->totalFields[$key][$g];
            }
        }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->fields["SCHREGNO"][$g]."' ";

            $query = Query::updateSQL($data, "MEDEXAM_TOOTH_DAT", $where);
        }

        return $query;
    }

    //更新 -- MEDEXAM_TOOTH_DETAIL_DAT
    public function updateMedexamToothDetailDat($model, $g, $flg, $seq)
    {
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $model->fields["SCHREGNO"][$g];
            $data["TOOTH_SEQ"][TEXT]        = $seq;
        }
        if ($seq == "003") {
            $data["TOOTH_REMARK3"][TEXT]        = $model->fields["OTHERDISEASECD3"][$g];
            $data["TOOTH_REMARK4"][TEXT]        = $model->fields["OTHERDISEASECD4"][$g];
            $data["TOOTH_REMARK5"][TEXT]        = $model->fields["OTHERDISEASE3"][$g];
            $data["TOOTH_REMARK6"][TEXT]        = $model->fields["OTHERDISEASE4"][$g];
        } elseif ($seq == "002") {
            $data["TOOTH_REMARK1"][TEXT]        = $model->fields["OTHERDISEASECD2"][$g];
            $data["TOOTH_REMARK2"][TEXT]        = $model->fields["OTHERDISEASE2"][$g];
            if ($model->is_f020_otherdisese_hyouji) {
                $data["TOOTH_REMARK3"][TEXT]   = $model->fields["OTHERDISEASE_REMARK1"][$g];
                $data["TOOTH_REMARK4"][TEXT]   = $model->fields["OTHERDISEASE_REMARK2"][$g];
                $data["TOOTH_REMARK5"][TEXT]   = $model->fields["OTHERDISEASE_REMARK3"][$g];
                $data["TOOTH_REMARK6"][TEXT]   = $model->fields["OTHERDISEASE_REMARK4"][$g];
            }
        } elseif ($seq == "005") {
            $data["TOOTH_REMARK1"][TEXT]   = $model->fields["DENTISTREMARK_REMARK1"][$g];
            $data["TOOTH_REMARK2"][TEXT]   = $model->fields["DENTISTREMARK_REMARK2"][$g];
            $data["TOOTH_REMARK3"][TEXT]   = $model->fields["DENTISTREMARK_REMARK3"][$g];
            $data["TOOTH_REMARK4"][TEXT]   = $model->fields["DENTISTREMARK_REMARK4"][$g];
        } elseif ($seq == "001") {
            $data["TOOTH_REMARK2"][TEXT]   = $model->fields["DENTISTTREAT2"][$g];
            $data["TOOTH_REMARK3"][TEXT]   = $model->fields["DENTISTTREAT3"][$g];
        } else {
            $data["TOOTH_REMARK1"][TEXT]        = $model->fields["OTHERDISEASECD2"][$g];
            $data["TOOTH_REMARK2"][TEXT]        = $model->fields["OTHERDISEASE2"][$g];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DETAIL_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO    = '".$model->fields["SCHREGNO"][$g]."' ";
            $where .= "     AND TOOTH_SEQ = '{$seq}' ";

            $query = Query::updateSQL($data, "MEDEXAM_TOOTH_DETAIL_DAT", $where);
        }

        return $query;
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //存在チェック -- MEDEXAM_HDAT
            $query = knjf023bQuery::checkExistData("MEDEXAM_HDAT", $model->fields["SCHREGNO"][$g]);
            $flgH = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新 -- MEDEXAM_HDAT
            //データを入力していない（all　null）生徒は、レコードを作成しない。
            if ($model->inputFlg[$g] == "OK" || $flgH == "update") {
                $db->query(knjf023bQuery::updateMedexamHdat($model, $g, $flgH));
            }

            //存在チェック -- MEDEXAM_TOOTH_DAT
            $query = knjf023bQuery::checkExistData("MEDEXAM_TOOTH_DAT", $model->fields["SCHREGNO"][$g]);
            $flgD = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新 -- MEDEXAM_TOOTH_DAT
            //データを入力していない（all　null）生徒は、レコードを作成しない。
            if ($model->inputFlg[$g] == "OK" || $flgD == "update") {
                $db->query(knjf023bQuery::updateMedexamToothDat($model, $g, $flgD));
            }

            //-- MEDEXAM_TOOTH_DETAIL_DAT
            if ($model->Properties["printKenkouSindanIppan"] != "2" && $model->screen == "1") {
                $seq = "002";
                //存在チェック
                $query = knjf023bQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023bQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }
            }
            if ($model->Properties["printKenkouSindanIppan"] == "2" && $model->screen == "1") {
                $seq = "001";
                //存在チェック
                $query = knjf023bQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023bQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }
            }

            if ($model->z010 == "miyagiken" || $model->is_f020_otherdisese_hyouji2) {
                $seq = "003";
                //存在チェック
                $query = knjf023bQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023bQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }
            }

            if ($model->is_f020_dentistremark_hyouji) {
                $seq = "005";
                //存在チェック
                $query = knjf023bQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023bQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }
}
