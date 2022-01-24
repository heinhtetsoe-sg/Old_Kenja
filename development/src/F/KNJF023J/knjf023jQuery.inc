<?php

require_once('for_php7.php');

class knjf023jQuery extends Query
{
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
            $query .= "     DT002.TOOTH_REMARK1 AS OTHERDISEASECD2, ";
            $query .= "     DT002.TOOTH_REMARK2 AS OTHERDISEASE2, ";
            $query .= "     L3.DENTISTREMARKCD, ";
            $query .= "     L3.DENTISTREMARK, ";
            $query .= "     L3.DENTISTREMARKDATE, ";
            $query .= "     L3.DENTISTTREATCD, ";
            $query .= "     L3.DENTISTTREAT, ";
            $query .= "     DT005.TOOTH_REMARK5 AS DENTISTTREATCD2, ";
            $query .= "     DT005.TOOTH_REMARK6 AS DENTISTTREAT2";
        } else {
            $cnt = 0;
            foreach ($model->shisiki as $key => $val) {
                $query .= ($cnt == 0) ? "" : ",";
                $query .= "     L3.".$key ;
                $cnt++;
            }
        }
        $query .= "    , DT005.TOOTH_REMARK1 AS DENTISTREMARKCD2 ";
        $query .= "    , DT005.TOOTH_REMARK2 AS DENTISTREMARK2 ";
        $query .= "    , DT005.TOOTH_REMARK3 AS DENTISTREMARKCD3 ";
        $query .= "    , DT005.TOOTH_REMARK4 AS DENTISTREMARK3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "            ON L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_HDAT L2 ";
        $query .= "            ON L2.YEAR         = T1.YEAR ";
        $query .= "           AND L2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     LEFT JOIN V_MEDEXAM_TOOTH_DAT L3 ";
        $query .= "            ON L3.YEAR         = T1.YEAR ";
        $query .= "           AND L3.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT002 ";
        $query .= "            ON DT002.YEAR      = T1.YEAR ";
        $query .= "           AND DT002.SCHREGNO  = T1.SCHREGNO ";
        $query .= "           AND DT002.TOOTH_SEQ = '002' ";
        $query .= "     LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
        $query .= "            ON DT005.YEAR      = T1.YEAR ";
        $query .= "           AND DT005.SCHREGNO  = T1.SCHREGNO ";
        $query .= "           AND DT005.TOOTH_SEQ = '005' ";
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

    //名称マスタ取得(ビューを使わずに取得)
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($model->screen == "1") {
            $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL ";
        } else {
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."'";

        return $query;
    }

    //名称マスタ(所見欄を無効にするNAMECD2を取得)
    public function getNameMstDisableCodes($model, $setInNamecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "        YEAR       = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1    = '{$setInNamecd1}' ";
        $query .= "    AND NAMESPARE2 = '1' ";
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
        $data["TOOTH_DATE"][TEXT]       = str_replace("/", "-", $model->fields["TOOTH_DATE"][$g]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

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
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $model->fields["SCHREGNO"][$g];
        }
        if ($model->screen == "1") {
            $data["JAWS_JOINTCD"][TEXT]         = $model->fields["JAWS_JOINTCD"][$g];
            $data["JAWS_JOINTCD2"][TEXT]        = $model->fields["JAWS_JOINTCD2"][$g];
            $data["PLAQUECD"][TEXT]             = $model->fields["PLAQUECD"][$g];
            $data["GUMCD"][TEXT]                = $model->fields["GUMCD"][$g];
            $data["CALCULUS"][TEXT]             = $model->fields["CALCULUS"][$g];
            $data["ORTHODONTICS"][TEXT]         = ($model->fields["ORTHODONTICS"][$g]) ? 1 : 0;
            foreach ($model->cntItem as $key => $val) {
                $data[$key][NUMBER]             = $model->fields[$key][$g];
            }
            $data["OTHERDISEASECD"][TEXT]       = $model->fields["OTHERDISEASECD"][$g];
            $data["OTHERDISEASE"][TEXT]         = $model->fields["OTHERDISEASE"][$g];
            $data["DENTISTREMARKCD"][TEXT]      = $model->fields["DENTISTREMARKCD"][$g];
            $data["DENTISTREMARK"][TEXT]        = $model->fields["DENTISTREMARK"][$g];
            $data["DENTISTREMARKDATE"][TEXT]    = str_replace("/", "-", $model->fields["DENTISTREMARKDATE"][$g]);
            $data["DENTISTTREATCD"][TEXT]       = $model->fields["DENTISTTREATCD"][$g];
            $data["DENTISTTREAT"][TEXT]         = $model->fields["DENTISTTREAT"][$g];
        } else {
            foreach ($model->shisiki as $key => $val) {
                $data[$key][TEXT]   = $model->fields[$key][$g];
            }
            foreach ($model->cntItem as $key => $val) {
                $data[$key][NUMBER] = $model->totalFields[$key][$g];
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
            $data["YEAR"][TEXT]            = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]        = $model->fields["SCHREGNO"][$g];
            $data["TOOTH_SEQ"][TEXT]       = $seq;
        }
        if ($seq == "002") {
            $data["TOOTH_REMARK1"][TEXT]   = $model->fields["OTHERDISEASECD2"][$g];
            $data["TOOTH_REMARK2"][TEXT]   = $model->fields["OTHERDISEASE2"][$g];
        } elseif ($seq == "005") {
            $data["TOOTH_REMARK1"][TEXT]   = $model->fields["DENTISTREMARKCD2"][$g];
            $data["TOOTH_REMARK2"][TEXT]   = $model->fields["DENTISTREMARK2"][$g];
            $data["TOOTH_REMARK3"][TEXT]   = $model->fields["DENTISTREMARKCD3"][$g];
            $data["TOOTH_REMARK4"][TEXT]   = $model->fields["DENTISTREMARK3"][$g];
            $data["TOOTH_REMARK5"][TEXT]   = $model->fields["DENTISTTREATCD2"][$g];
            $data["TOOTH_REMARK6"][TEXT]   = $model->fields["DENTISTTREAT2"][$g];
        } else {
            $data["TOOTH_REMARK1"][TEXT]   = $model->fields["OTHERDISEASECD2"][$g];
            $data["TOOTH_REMARK2"][TEXT]   = $model->fields["OTHERDISEASE2"][$g];
        }
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "MEDEXAM_TOOTH_DETAIL_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR           = '".CTRL_YEAR."' AND ";
            $where .= "     SCHREGNO       = '".$model->fields["SCHREGNO"][$g]."' ";
            $where .= "     AND TOOTH_SEQ  = '{$seq}' ";

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
            $query = knjf023jQuery::checkExistData("MEDEXAM_HDAT", $model->fields["SCHREGNO"][$g]);
            $flgH = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新 -- MEDEXAM_HDAT
            //データを入力していない（all　null）生徒は、レコードを作成しない。
            if ($model->inputFlg[$g] == "OK" || $flgH == "update") {
                $db->query(knjf023jQuery::updateMedexamHdat($model, $g, $flgH));
            }

            //存在チェック -- MEDEXAM_TOOTH_DAT
            $query = knjf023jQuery::checkExistData("MEDEXAM_TOOTH_DAT", $model->fields["SCHREGNO"][$g]);
            $flgD = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新 -- MEDEXAM_TOOTH_DAT
            //データを入力していない（all　null）生徒は、レコードを作成しない。
            if ($model->inputFlg[$g] == "OK" || $flgD == "update") {
                $db->query(knjf023jQuery::updateMedexamToothDat($model, $g, $flgD));
            }

            //-- MEDEXAM_TOOTH_DETAIL_DAT
            if ($model->screen == "1") {
                $seq = "002";
                //存在チェック
                $query = knjf023jQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023jQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }

                $seq = "005";
                //存在チェック
                $query = knjf023jQuery::checkExistData("MEDEXAM_TOOTH_DETAIL_DAT", $model->fields["SCHREGNO"][$g], $seq);
                $flg = ($db->getOne($query) > 0) ? "update" : "insert";
                //更新
                //データを入力していない（all　null）生徒は、レコードを作成しない。
                if ($model->inputFlg[$g] == "OK" || $flg == "update") {
                    $db->query(knjf023jQuery::updateMedexamToothDetailDat($model, $g, $flg, $seq));
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
