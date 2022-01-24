<?php

require_once('for_php7.php');

class knjf323Query extends Query {
    //年度一覧取得
    function getYearList($model) {
        $query  = " WITH THIS_YEAR (YEAR) AS (VALUES('".CTRL_YEAR."')) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT YEAR FROM MEDEXAM_CARE_HDAT WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT YEAR FROM MEDEXAM_CARE_DAT WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT YEAR FROM THIS_YEAR ";
        $query .= " ) ";
        $query .= " SELECT YEAR || '年度' AS LABEL, YEAR AS VALUE FROM MAIN ORDER BY VALUE DESC ";

        return $query;
    }

    //データを取得（メイン画面）
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     S1.BIRTHDAY, ";
        $query .= "     S3.NAME2 AS SEX, ";
        $query .= "     S1.BLOODTYPE, ";
        $query .= "     S1.BLOOD_RH, ";
        $query .= "     S4.MEDICAL_HISTORY1, ";
        $query .= "     S4.MEDICAL_HISTORY2, ";
        $query .= "     S4.MEDICAL_HISTORY3, ";
        $query .= "     S4.DIAGNOSIS_NAME, ";
        $query .= "     S4.GUIDE_DIV, ";
        $query .= "     S4.JOINING_SPORTS_CLUB, ";
        $query .= "     M1.CARE_FLG AS CARE_FLG01, ";
        $query .= "     M2.CARE_FLG AS CARE_FLG02, ";
        $query .= "     M3.CARE_FLG AS CARE_FLG03, ";
        $query .= "     M4.CARE_FLG AS CARE_FLG04, ";
        $query .= "     M5.CARE_FLG AS CARE_FLG05, ";
        $query .= "     M6.CARE_FLG AS CARE_FLG06 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ";
        $query .= "          ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "          ON T1.YEAR     = S2.YEAR ";
        $query .= "         AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "         AND T1.GRADE    = S2.GRADE ";
        $query .= "         AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST S3 ";
        $query .= "          ON S3.NAMECD1  = 'Z002' ";
        $query .= "         AND S3.NAMECD2  = S1.SEX ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DAT S4 ";
        $query .= "          ON T1.YEAR     = S4.YEAR ";
        $query .= "         AND T1.SCHREGNO = S4.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M1 ";
        $query .= "          ON T1.YEAR     = M1.YEAR ";
        $query .= "         AND T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "         AND M1.CARE_DIV = '01' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M2 ";
        $query .= "          ON T1.YEAR     = M2.YEAR ";
        $query .= "         AND T1.SCHREGNO = M2.SCHREGNO ";
        $query .= "         AND M2.CARE_DIV = '02' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M3 ";
        $query .= "          ON T1.YEAR     = M3.YEAR ";
        $query .= "         AND T1.SCHREGNO = M3.SCHREGNO ";
        $query .= "         AND M3.CARE_DIV = '03' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M4 ";
        $query .= "          ON T1.YEAR     = M4.YEAR ";
        $query .= "         AND T1.SCHREGNO = M4.SCHREGNO ";
        $query .= "         AND M4.CARE_DIV = '04' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M5 ";
        $query .= "          ON T1.YEAR     = M5.YEAR ";
        $query .= "         AND T1.SCHREGNO = M5.SCHREGNO ";
        $query .= "         AND M5.CARE_DIV = '05' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M6 ";
        $query .= "          ON T1.YEAR     = M6.YEAR ";
        $query .= "         AND T1.SCHREGNO = M6.SCHREGNO ";
        $query .= "         AND M6.CARE_DIV = '06' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $label="") {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($label) {
            $query .= "     ".$label." || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス名称取得
    function getHrName($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //ヘッダデータ取得
    function getCareHData($model, $year, $care_div) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_CARE_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV = '".$care_div."' ";

        return $query;
    }

    //データ取得
    function getCareData($model, $year, $care_div) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO, ";
        $query .= "     CARE_KIND, ";
        $query .= "     CARE_ITEM, ";
        $query .= "     CARE_SEQ, ";
        $query .= "     CARE_REMARK1, ";
        $query .= "     CARE_REMARK2 ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_CARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV = '".$care_div."' ";
        $query .= " ORDER BY ";
        $query .= "     CARE_KIND, ";
        $query .= "     CARE_ITEM, ";
        $query .= "     CARE_SEQ ";

        return $query;
    }

    //データ取得
    function getMedexamData($model, $table, $flg="") {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->cmd == "update2") {
                /******************/
                /*  MEDEXAM_HDAT  */
                /******************/
                //データ取得
                $hdat = $db->getRow(knjf323Query::getMedexamData($model, "MEDEXAM_HDAT", "data"), DB_FETCHMODE_ASSOC);

                if (!$hdat["DATE"]) {
                    $data = array();
                    if (!$hdat["SCHREGNO"]) {
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["SCHREGNO"][TEXT]     = $model->schregno;
                    }
                    $data["DATE"][TEXT]         = CTRL_DATE;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    if (!$hdat["SCHREGNO"]) {
                        $query = Query::insertSQL($data, "MEDEXAM_HDAT");
                    } else {
                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                        $where .= "     SCHREGNO    = '".$model->schregno."' ";

                        $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
                    }
                    $db->query($query);
                }

                /*********************/
                /*  MEDEXAM_DET_DAT  */
                /*********************/
                //データ有無チェック
                $checkD = $db->getOne(knjf323Query::getMedexamData($model, "MEDEXAM_DET_DAT"));

                $data = array();
                if (!$checkD) {
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]     = $model->schregno;
                }
                $data["MEDICAL_HISTORY1"][TEXT]         = $fields["MEDICAL_HISTORY1"];
                $data["MEDICAL_HISTORY2"][TEXT]         = $fields["MEDICAL_HISTORY2"];
                $data["MEDICAL_HISTORY3"][TEXT]         = $fields["MEDICAL_HISTORY3"];
                $data["DIAGNOSIS_NAME"][TEXT]           = $fields["DIAGNOSIS_NAME"];
                $data["GUIDE_DIV"][TEXT]                = $fields["GUIDE_DIV"];
                $data["JOINING_SPORTS_CLUB"][TEXT]      = $fields["JOINING_SPORTS_CLUB"];
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][NUMBER]                = "sysdate()";

                if (!$checkD) {
                    $query = Query::insertSQL($data, "MEDEXAM_DET_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SCHREGNO    = '".$model->schregno."' ";

                    $query = Query::updateSQL($data, "MEDEXAM_DET_DAT", $where);
                }
                $db->query($query);

        } else {
            //アレルギー疾患
            for ($i=1; $i <= 6 ; $i++) {
                $care_div = sprintf("%02d", $i);

                //存在チェック
                $check = $db->getOne(knjf323Query::getCareHData($model, CTRL_YEAR, $care_div));

                $data = array();
                if (!$check) {
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]     = $model->schregno;
                    $data["CARE_DIV"][TEXT]     = $care_div;
                }
                $data["CARE_FLG"][TEXT]     = ($fields["CARE_FLG".$care_div] == 1) ? 1 : 0;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                if (!$check) {
                    $query = Query::insertSQL($data, "MEDEXAM_CARE_HDAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
                    $where .= "     CARE_DIV    = '".$care_div."' ";

                    $query = Query::updateSQL($data, "MEDEXAM_CARE_HDAT", $where);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    // UPDATE
    function &getSubUpdateQuery($model, $fields, $care_div) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- MEDEXAM_CARE_DAT
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_CARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV    = '".$care_div."' ";
        $db->query($query);

        //種別
        for ($i=1; $i <= 2 ; $i++) {
            //項目
            for ($j=1; $j <= 6 ; $j++) {
                //SEQ
                for ($k=0; $k <= 12 ; $k++) {

                    $cd = sprintf("%02d", $i)."_".sprintf("%02d", $j)."_".sprintf("%02d", $k);

                    if ($care_div == '04' && $i == 1 && $j == 3) {
                        $fields["TEXT".$cd] = $sep = "";
                        for ($l=1; $l <= 3 ; $l++) {
                            if ($fields["REASON".sprintf("%02d", $k)."_".$l]) {
                                $fields["TEXT".$cd] .= $sep.$l;
                                $sep = ",";
                            }
                        }
                    } else if ($care_div == '06' && $i == 1 && $j == 1 && $k == 2) {
                        $fields["TEXT".$cd] = $sep = "";
                        $season = array(1 => '春', 2 => '夏', 3 => '秋', 4 => '冬');
                        for ($l=1; $l <= 4 ; $l++) {
                            if ($fields["REASON".sprintf("%02d", $k)."_".$l]) {
                                $fields["TEXT".$cd] .= $sep.$season[$l];
                                $sep = ",";
                            }
                        }
                    }

                    //備考のみ項目で入力なし、またはチェックなしのときは対象外
                    if (($k == 0 && strlen($fields["TEXT".$cd]) > 0) || ($k != 0 && strlen($fields["CHECK".$cd]) > 0)) {
                        //追加 -- MEDEXAM_CARE_DAT
                        $data = array();
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        $data["SCHREGNO"][TEXT]     = $model->schregno;
                        $data["CARE_DIV"][TEXT]     = $care_div;
                        $data["CARE_KIND"][TEXT]    = sprintf("%02d", $i);
                        $data["CARE_ITEM"][TEXT]    = sprintf("%02d", $j);
                        $data["CARE_SEQ"][TEXT]     = sprintf("%02d", $k);
                        $data["CARE_REMARK1"][TEXT] = $fields["TEXT".$cd];
                        $data["CARE_REMARK2"][TEXT] = $fields["DETAIL".$cd];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][NUMBER]    = "sysdate()";

                        $query = Query::insertSQL($data, "MEDEXAM_CARE_DAT");
                        $db->query($query);
                    }
                }
            }
        }

        //更新 -- MEDEXAM_CARE_HDAT
        $data = array();
        if (in_array($care_div, array('01', '04'))) {
            $data["EMERGENCYNAME"][TEXT]    = $fields["EMERGENCYNAME"];
            $data["EMERGENCYTELNO"][TEXT]   = $fields["EMERGENCYTELNO"];
            $data["EMERGENCYNAME2"][TEXT]   = $fields["EMERGENCYNAME2"];
            $data["EMERGENCYTELNO2"][TEXT]  = $fields["EMERGENCYTELNO2"];
        }
        $data["DATE"][TEXT]             = str_replace("/", "-", $fields["DATE"]);
        $data["DOCTOR"][TEXT]           = $fields["DOCTOR"];
        $data["HOSPITAL"][TEXT]         = $fields["HOSPITAL"];
        if ($care_div == '04') {
            $data["REMARK"][TEXT]           = $fields["REMARK"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $where .= "     CARE_DIV    = '".$care_div."' ";

        $query = Query::updateSQL($data, "MEDEXAM_CARE_HDAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //前年度のデータを今年度にINSERT
    function &getSubCopyQuery($db, $model, $care_div) {
        $db->autoCommit(false);

        $pre_year  = (int)$model->field["YEAR"] - 1;
        $this_year = $model->field["YEAR"];

        /***********************/
        /*  MEDEXAM_CARE_HDAT  */
        /***********************/
        //削除
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_CARE_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$this_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV    = '".$care_div."' ";
        $db->query($query);

        //前年度のデータ取得
        $result = $db->query(knjf323Query::getCareHData($model, $pre_year, $care_div));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]             = $this_year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CARE_DIV"][TEXT]         = $care_div;
            $data["CARE_FLG"][TEXT]         = $row["CARE_FLG"];
            $data["EMERGENCYNAME"][TEXT]    = $row["EMERGENCYNAME"];
            $data["EMERGENCYTELNO"][TEXT]   = $row["EMERGENCYTELNO"];
            $data["EMERGENCYNAME2"][TEXT]   = $row["EMERGENCYNAME2"];
            $data["EMERGENCYTELNO2"][TEXT]  = $row["EMERGENCYTELNO2"];
            $data["DATE"][TEXT]             = $row["DATE"];
            $data["DOCTOR"][TEXT]           = $row["DOCTOR"];
            $data["HOSPITAL"][TEXT]         = $row["HOSPITAL"];
            $data["REMARK"][TEXT]           = $row["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "MEDEXAM_CARE_HDAT");
            $db->query($query);
        }

        /**********************/
        /*  MEDEXAM_CARE_DAT  */
        /**********************/
        //削除
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_CARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$this_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV    = '".$care_div."' ";
        $db->query($query);

        //前年度のデータ取得
        $result = $db->query(knjf323Query::getCareData($model, $pre_year, $care_div));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]             = $this_year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CARE_DIV"][TEXT]         = $care_div;
            $data["CARE_KIND"][TEXT]        = $row["CARE_KIND"];
            $data["CARE_ITEM"][TEXT]        = $row["CARE_ITEM"];
            $data["CARE_SEQ"][TEXT]         = $row["CARE_SEQ"];
            $data["CARE_REMARK1"][TEXT]     = $row["CARE_REMARK1"];
            $data["CARE_REMARK2"][TEXT]     = $row["CARE_REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "MEDEXAM_CARE_DAT");
            $db->query($query);
        }

        $db->commit();
        return;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S1.HR_NAME || T1.ATTENDNO || '番' AS ATTENDNO, ";
        $query .= "     S2.NAME, ";
        $query .= "     L1.ABBV1 AS SEX, ";
        $query .= "     S2.BIRTHDAY, ";
        $query .= "     M1.CARE_DIV, ";
        $query .= "     M1.CARE_FLG, ";
        $query .= "     M1.EMERGENCYNAME, ";
        $query .= "     M1.EMERGENCYTELNO, ";
        $query .= "     M1.EMERGENCYNAME2, ";
        $query .= "     M1.EMERGENCYTELNO2, ";
        $query .= "     M1.DATE, ";
        $query .= "     M1.DOCTOR, ";
        $query .= "     M1.HOSPITAL, ";
        $query .= "     M1.REMARK, ";
        $query .= "     M2.CARE_KIND, ";
        $query .= "     M2.CARE_ITEM, ";
        $query .= "     M2.CARE_SEQ, ";
        $query .= "     M2.CARE_REMARK1, ";
        $query .= "     M2.CARE_REMARK2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S1 ";
        $query .= "          ON T1.YEAR     = S1.YEAR ";
        $query .= "         AND T1.SEMESTER = S1.SEMESTER ";
        $query .= "         AND T1.GRADE    = S1.GRADE ";
        $query .= "         AND T1.HR_CLASS = S1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S2 ";
        $query .= "          ON T1.SCHREGNO = S2.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1  = 'Z002' ";
        $query .= "         AND L1.NAMECD2  = S2.SEX ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M1 ";
        $query .= "          ON T1.YEAR     = M1.YEAR ";
        $query .= "         AND T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_DAT M2 ";
        $query .= "          ON T1.YEAR     = M2.YEAR ";
        $query .= "         AND T1.SCHREGNO = M2.SCHREGNO ";
        $query .= "         AND M2.CARE_DIV = M1.CARE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     M1.CARE_DIV, ";
        $query .= "     M2.CARE_KIND, ";
        $query .= "     M2.CARE_ITEM, ";
        $query .= "     M2.CARE_SEQ ";

        return $query;
    }
}
?>
