<?php

require_once('for_php7.php');

class knjf017Query extends Query {
    //学校を判断
    function getNameMstZ010() {
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model, $grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ         = '001' AND ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = ( SELECT ";
        $query .= "                         SCHOOL_KIND ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_GDAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                         GRADE = '".$grade."') ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model, $div) {
        $query  = " SELECT DISTINCT ";
        if ($div == "grade") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
            $query .= "     T2.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";
        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        if ($div == "hr_class") {
            $query .= " AND T1.GRADE  = '".$model->grade."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //生徒リストtoリスト
    function getStudent($model, $div) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "      ON T2.YEAR     = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE    = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($div == "right") {
            $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->hr_class."' AND ";
            $query .= "     T1.SCHREGNO NOT IN ('".str_replace(",", "','", $model->selectdata)."') ";
        } else {
            $query .= "     T1.GRADE = '".$model->grade."' AND ";
            $query .= "     T1.SCHREGNO IN ('".str_replace(",", "','", $model->selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //種類
    function getInputForm() {
        $query  = " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('1','入力項目1') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','入力項目2') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','入力項目3') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4','入力項目4') ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1, $flg = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($flg == "3") {
            $query .= "     ABBV1 || '　' || NAME1 AS LABEL ";
        } else if ($flg == "CD_NASHI") {
            $query .= "     NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || '　' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($flg == "1") {
            $query .= "     AND NAMESPARE3 = '1' ";
        }
        if ($flg == "2") {
            $query .= "     AND NAMECD2 = '98' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //視力チェック
    function getVisionMark($model) {
        $query .= " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->z010name1 == "kumamoto") {
            $query .= "     NAMECD1 = 'F011' ";
        } else {
            $query .= "     NAMECD1 = 'F017' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }

    //データ存在チェック -- MEDEXAM_HDAT
    function checkMedexamHdat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //更新 -- MEDEXAM_HDAT
    function getUpdateMedexamHdat($db, $model, $schregno) {
        //データ存在チェック
        $dcnt = $db->getOne(knjf017Query::checkMedexamHdat($model, $schregno));

        if (($model->input_form == "1" && $model->field["RCHECK0"] == "1") || $dcnt == 0) {
            $model->field["DATE"] = ($model->field["DATE"]) ? $model->field["DATE"] : CTRL_DATE;

            $data = array();
            if (!$dcnt) {
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]     = $schregno;
            }
            $data["DATE"][TEXT]         = str_replace("/", "-", $model->field["DATE"]);
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if (!$dcnt) {
                $query = Query::insertSQL($data, "MEDEXAM_HDAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "     SCHREGNO = '".$schregno."' ";
                $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
                $db->query($query);
            }
        }
    }

    //テーブル情報取得
    function getTableInfo() {
        $query  = " SELECT ";
        $query .= "     COLUMN_NAME AS FIELD, ";
        $query .= "     DATA_TYPE AS TYPE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = 'MEDEXAM_DET_DAT' AND ";
        $query .= "     COLUMN_NAME NOT IN ('REGISTERCD', 'UPDATED') ";

        return $query;
    }

    //データ存在チェック -- MEDEXAM_DET_DAT
    function checkMedexamDetDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //更新 -- MEDEXAM_DET_DAT
    function getUpdateMedexamDetDat($db, $model, $schregno, $tmpData) {
        if (get_count($tmpData["det"]) > 0) {
            //テーブル情報取得
            $info = array();
            $result = $db->query(knjf017Query::getTableInfo());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (in_array($row["TYPE"], array("CHARACTER VARYING", "DATE"))) {
                    $type = TEXT;
                } else {
                    $type = NUMBER;
                }
                $info[$row["FIELD"]] = $type;
            }
            $result->free();

            //データ存在チェック
            $dcnt = $db->getOne(knjf017Query::checkMedexamDetDat($model, $schregno));

            $data = array();
            if (!$dcnt) {
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]     = $schregno;
            }
            foreach ($tmpData["det"] as $field => $val) {
                $data[$field][$info[$field]]    = $val;
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if (!$dcnt) {
                $query = Query::insertSQL($data, "MEDEXAM_DET_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' ";
                $query = Query::updateSQL($data, "MEDEXAM_DET_DAT", $where);
                $db->query($query);
            }
        }
    }

    //データ存在チェック -- MEDEXAM_DET_DETAIL_DAT
    function checkMedexamDetDetailDat($model, $schregno, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DET_SEQ     = '".$seq."' ";

        return $query;
    }

    //更新 -- MEDEXAM_DET_DETAIL_DAT
    function getUpdateMedexamDetDetailDat($db, $model, $schregno, $tmpData, $seq) {
        if (get_count($tmpData[$seq]) > 0) {
            //データ存在チェック
            $dcnt = $db->getOne(knjf017Query::checkMedexamDetDetailDat($model, $schregno, $seq));

            $data = array();
            if (!$dcnt) {
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["DET_SEQ"][TEXT]      = $seq;
            }
            foreach ($tmpData[$seq] as $field => $val) {
                $data[$field][TEXT]     = $val;
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if (!$dcnt) {
                $query = Query::insertSQL($data, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' AND ";
                $where .= "     DET_SEQ     = '".$seq."' ";
                $query = Query::updateSQL($data, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            }
        }
    }

    //追加・更新
    function &getUpdateQuery($model, $detail) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $seq_array = $tmpData = array();
        foreach ($model->setField as $key => $array) {
            if ($model->field["RCHECK".$key] == "1") {
                foreach ($array as $field) {
                    if (in_array($field, array_keys($detail))) {
                        list ($seq, $dfield) = $detail[$field];
                        $tmpData[$seq][$dfield] = $model->field[$field];
                        if (!in_array($seq, $seq_array)) $seq_array[] = $seq;
                    } else if ($field == "DATE") {
                        continue;
                    } else {
                        $tmpData["det"][$field] = $model->field[$field];
                    }
                }
            }
        }

        $schArray = explode(',', $model->selectdata);
        foreach ($schArray as $schregno) {
            //更新 -- MEDEXAM_HDAT
            knjf017Query::getUpdateMedexamHdat($db, $model, $schregno);
            //更新 -- MEDEXAM_DET_DAT
            knjf017Query::getUpdateMedexamDetDat($db, $model, $schregno, $tmpData);
            //更新 -- MEDEXAM_DET_DETAIL_DAT
            if ($seq_array[0]) {
                foreach ($seq_array as $seq) {
                    knjf017Query::getUpdateMedexamDetDetailDat($db, $model, $schregno, $tmpData, $seq);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
