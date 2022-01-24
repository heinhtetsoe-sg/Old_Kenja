<?php

require_once('for_php7.php');
class knjx190query extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学校名取得の取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        if ($model->mode == 'grd') {
            $table = 'GRD_REGD_HDAT';
        } else {
            $table = 'SCHREG_REGD_HDAT';
        }

        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM {$table} T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "        INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = 'H' ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        if ($model->mode == 'grd') {
            $table = 'GRD_REGD_HDAT';
        } else {
            $table = 'SCHREG_REGD_HDAT';
        }

        $query  = " SELECT DISTINCT T1.GRADE, T1.HR_CLASS, T1.HR_NAME ";
        $query .= " FROM {$table} T1, ";
        $query .= "      SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.YEAR = T2.YEAR ";
        $query .= "   AND T1.GRADE = T2.GRADE ";
        $query .= "   AND T2.SCHOOL_KIND = 'H' ";
        if (($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) && $model->mode != 'grd') {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS";

        return $query;
    }

    //年次の取得
    public function getAnnual($schregno, $year)
    {
        if ($model->mode == 'grd') {
            $table = 'GRD_REGD_DAT';
        } else {
            $table = 'SCHREG_REGD_DAT';
        }

        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     YEAR     = '{$year}' ";

        return $query;
    }

    //マスタの追加(CSVデータより読込)
    public function insertQueryCsv($model, &$data_arr)
    {
        if ($model->mode == 'grd') {
            $table_ent  = 'GRD_HEXAM_ENTREMARK_DAT';
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent  = 'HEXAM_ENTREMARK_SEQ_DAT';
            } else {
                $table_ent  = 'HEXAM_ENTREMARK_DAT';
            }
        }
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $data["PATTERN_SEQ"][TEXT]      = $data_arr[$i]["PATTERN_SEQ"];
            }
            $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
            $data["ATTENDREC_REMARK"][TEXT] = $data_arr[$i]["ATTENDREC_REMARK"];
            $data["SPECIALACTREC"][TEXT]    = $data_arr[$i]["SPECIALACTREC"];
            if ("1" != $model->tyousasyo2020) {
                $data["TRAIN_REF"][TEXT]        = $data_arr[$i]["TRAIN_REF"];
                $data["TRAIN_REF1"][TEXT]       = $data_arr[$i]["TRAIN_REF1"];
                $data["TRAIN_REF2"][TEXT]       = $data_arr[$i]["TRAIN_REF2"];
                $data["TRAIN_REF3"][TEXT]       = $data_arr[$i]["TRAIN_REF3"];
            }

            if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $model->tyousasyo2020) {
                $data["TOTALSTUDYACT"][TEXT] = $data_arr[$i]["TOTALSTUDYACT"];
                $data["TOTALSTUDYVAL"][TEXT] = $data_arr[$i]["TOTALSTUDYVAL"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     {$table_ent} ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $query .= "     AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
            }
            if (1 > $db->getOne($query)) {
                $query = Query::insertSQL($data, $table_ent);
            } else {
                $where  = " WHERE YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                $where .= "       SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $where .= "       AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
                }

                $query = Query::updateSQL($data, $table_ent, $where);
            }
            $db->query($query);

            //HEXAM_ENTREMARK_DATにある生徒はそれも更新する
            if ($model->mode == 'grd') {
                if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_SEQ_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR        = '{$data_arr[$i]["YEAR"]}' AND ";
                    $query .= "     SCHREGNO    = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                    $query .= "     PATTERN_SEQ = '1' ";
                    if (0 < $db->getOne($query)) {
                        $where  = " WHERE YEAR        = '{$data_arr[$i]["YEAR"]}' AND ";
                        $where .= "       SCHREGNO    = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                        $where .= "       PATTERN_SEQ = '1' ";

                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_SEQ_DAT", $where);
                        $db->query($query);
                    }
                } else {
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    if (0 < $db->getOne($query)) {
                        $where  = " WHERE YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                        $where .= "       SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_DAT", $where);
                        $db->query($query);
                    }
                }
            }

            if ("1" == $model->tyousasyo2020) {
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $table_ent2 = "HEXAM_ENTREMARK_TRAINREF_SEQ_DAT";
                } else {
                    $table_ent2 = "HEXAM_ENTREMARK_TRAINREF_DAT";
                }

                for ($j = 1; $j <= 6; $j++) {
                    $seq = "10".$j;
                    $data = array();
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $data["PATTERN_SEQ"][TEXT]      = $data_arr[$i]["PATTERN_SEQ"];
                    }
                    $data["TRAIN_SEQ"][TEXT]        = $seq;
                    $data["REMARK"][TEXT]           = $data_arr[$i]["TRAIN_REF".$j];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     {$table_ent2} ";
                    $query .= " WHERE ";
                    $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                    $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $query .= "     AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
                    }
                    $query .= "     AND TRAIN_SEQ= '{$seq}' ";
                    if (1 > $db->getOne($query)) {
                        $query = Query::insertSQL($data, $table_ent2);
                    } else {
                        $where  = " WHERE YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                        $where .= "       AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                        if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                            $where .= "       AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
                        }
                        $where .= "       AND TRAIN_SEQ = '{$seq}' ";

                        $query = Query::updateSQL($data, $table_ent2, $where);
                    }
                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除(CSVデータより読込)
    public function deleteQueryCsv($model, &$data_arr)
    {
        if ($model->mode == 'grd') {
            $table_ent  = 'GRD_HEXAM_ENTREMARK_DAT';
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent  = 'HEXAM_ENTREMARK_SEQ_DAT';
            } else {
                $table_ent  = 'HEXAM_ENTREMARK_DAT';
            }
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     {$table_ent} ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $query .= "     AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
            }
            $db->query($query);

            if ($model->mode == 'grd') {
                if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_SEQ_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR        = '{$data_arr[$i]["YEAR"]}' AND ";
                    $query .= "     SCHREGNO    = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                    $query .= "     PATTERN_SEQ = '1' ";
                } else {
                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                }

                $db->query($query);
            }

            if ("1" == $model->tyousasyo2020) {
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $table_ent2 = "HEXAM_ENTREMARK_TRAINREF_SEQ_DAT";
                } else {
                    $table_ent2 = "HEXAM_ENTREMARK_TRAINREF_DAT";
                }

                for ($j = 1; $j <= 6; $j++) {
                    $seq = "10".$j;

                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     {$table_ent2} ";
                    $query .= " WHERE ";
                    $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' AND ";
                    if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $query .= "     PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' AND ";
                    }
                    $query .= "     TRAIN_SEQ = '{$seq}' ";

                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($db, $model)
    {
        if ($model->mode == 'grd') {
            $table_regd  = 'GRD_REGD_DAT';
            $table_base  = 'GRD_BASE_MST';
            $table_ent   = 'GRD_HEXAM_ENTREMARK_DAT';
            $table_regdH = 'GRD_REGD_HDAT';
        } else {
            $table_regd  = 'SCHREG_REGD_DAT';
            $table_base  = 'SCHREG_BASE_MST';
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent   = 'HEXAM_ENTREMARK_SEQ_DAT';
            } else {
                $table_ent   = 'HEXAM_ENTREMARK_DAT';
            }
            $table_regdH = 'SCHREG_REGD_HDAT';
        }

        $query  = " WITH GRADE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT T1, ";
        $query .= "         {$table_regd} T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.GRADE = T2.GRADE AND ";
        $query .= "         T1.SCHOOL_KIND = 'H' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.ANNUAL, ";
        if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     T3.PATTERN_SEQ, ";
        }
        $query .= "     T3.ATTENDREC_REMARK, ";
        $query .= "     T3.SPECIALACTREC, ";
        if ("1" == $model->tyousasyo2020) {
            $query .= "     '' AS TRAIN_REF1, ";
            $query .= "     '' AS TRAIN_REF2, ";
            $query .= "     '' AS TRAIN_REF3, ";
            $query .= "     '' AS TRAIN_REF4, ";
            $query .= "     '' AS TRAIN_REF5, ";
            $query .= "     '' AS TRAIN_REF6, ";
        } else {
            $query .= "     T3.TRAIN_REF, ";
            $query .= "     T3.TRAIN_REF1, ";
            $query .= "     T3.TRAIN_REF2, ";
            $query .= "     T3.TRAIN_REF3, ";
        }
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $model->tyousasyo2020) {
            $query .= "     T3.TOTALSTUDYACT, ";
            $query .= "     T3.TOTALSTUDYVAL, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     {$table_regd} T1 ";
        $query .= "     LEFT JOIN {$table_base} T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GRADE T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN {$table_ent}  T3 ON  T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND T3.YEAR     = T4.YEAR ";
        $query .= "     LEFT JOIN {$table_regdH} T5 ON  T5.YEAR     = T1.YEAR ";
        $query .= "                                 AND T5.SEMESTER = T1.SEMESTER ";
        $query .= "                                 AND T5.GRADE    = T1.GRADE ";
        $query .= "                                 AND T5.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR   = T1.YEAR ";
        $query .= "                                   AND G1.GRADE  = T1.GRADE ";
        $query .= "                                   AND G1.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= "     AND (T5.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T5.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T5.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "          T5.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T5.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T5.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        $rows = array();
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ("1" == $model->tyousasyo2020) {
                $query  = " SELECT INT(TRAIN_SEQ) - 100 AS TRAIN_SEQ, REMARK ";
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $query .= " FROM HEXAM_ENTREMARK_TRAINREF_SEQ_DAT ";
                } else {
                    $query .= " FROM HEXAM_ENTREMARK_TRAINREF_DAT ";
                }
                $query .= " WHERE ";
                $query .= "   YEAR = '".$row["YEAR"]."' ";
                $query .= "   AND SCHREGNO = '".$row["SCHREGNO"]."' ";
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $query .= "   AND PATTERN_SEQ = '".$row["PATTERN_SEQ"]."' ";
                }
                $query .= "   AND TRAIN_SEQ BETWEEN '101' AND '106' ";
                $result2 = $db->query($query);
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["TRAIN_REF".$row2["TRAIN_SEQ"]] = $row2["REMARK"];
                }
                $result2->free();
            }
            $rows[] = $row;
        }
        $result->free();

        return $rows;
    }

    public function countSchregNo($schregno)
    {
        if ($model->mode == 'grd') {
            $table = 'GRD_BASE_MST';
        } else {
            $table = 'SCHREG_BASE_MST';
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}'";

        return $query;
    }

    public function checkGrade($schregno, $year)
    {
        if ($model->mode == 'grd') {
            $table = 'GRD_REGD_DAT';
        } else {
            $table = 'SCHREG_REGD_DAT';
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     {$table} T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'H' AND ";
        $query .= "     T2.SCHREGNO = '{$schregno}' AND ";
        $query .= "     T2.YEAR = '{$year}'";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
