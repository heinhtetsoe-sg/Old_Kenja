<?php

require_once('for_php7.php');
class knjx191query extends Query
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

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        if ($model->mode == 'grd') {
            $table_ent  = 'GRD_HEXAM_ENTREMARK_HDAT';
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent  = 'HEXAM_ENTREMARK_SEQ_HDAT';
            } else {
                $table_ent  = 'HEXAM_ENTREMARK_HDAT';
            }
        }
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $data["PATTERN_SEQ"][TEXT]      = $data_arr[$i]["PATTERN_SEQ"];
            }
            $data["COMMENTEX_A_CD"][TEXT]   = $data_arr[$i]["COMMENTEX_A_CD"];
            if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->tyousasyo2020) {
                $data["TOTALSTUDYACT"][TEXT]    = $data_arr[$i]["TOTALSTUDYACT"];
                $data["TOTALSTUDYVAL"][TEXT]    = $data_arr[$i]["TOTALSTUDYVAL"];
            }
            if ("1" != $model->tyousasyo2020) {
                $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            }
            if ($model->Properties["useHexamRemark2Flg"] == 1) {
                $data["REMARK2"][TEXT]          = $data_arr[$i]["REMARK2"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     {$table_ent} ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $query .= "     AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
            }
            if (1 > $db->getOne($query)) {
                $query = Query::insertSQL($data, $table_ent);
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $where .= "    AND PATTERN_SEQ = '".$data_arr[$i]["PATTERN_SEQ"]."' ";
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
                    $query .= "     HEXAM_ENTREMARK_SEQ_HDAT ";
                    $query .= " WHERE ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    $query .= "     AND PATTERN_SEQ = '1' ";
                    if (0 < $db->getOne($query)) {
                        $where  = " WHERE SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                        $where .= "     AND PATTERN_SEQ = '1' ";

                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_SEQ_HDAT", $where);
                        $db->query($query);
                    }
                } else {
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_HDAT ";
                    $query .= " WHERE ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    if (0 < $db->getOne($query)) {
                        $where  = " WHERE SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_HDAT", $where);
                        $db->query($query);
                    }
                }
            }

            if ("1" == $model->tyousasyo2020) {
                $data = array();
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     HEXAM_ENTREMARK_REMARK_HDAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                if (0 < $db->getOne($query)) {
                    $where  = " WHERE SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_REMARK_HDAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HEXAM_ENTREMARK_REMARK_HDAT");
                }
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        if ($model->mode == 'grd') {
            $table_ent  = 'GRD_HEXAM_ENTREMARK_HDAT';
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent  = 'HEXAM_ENTREMARK_SEQ_HDAT';
            } else {
                $table_ent  = 'HEXAM_ENTREMARK_HDAT';
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
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $query .= "     AND PATTERN_SEQ = '{$data_arr[$i]["PATTERN_SEQ"]}' ";
            }
            $db->query($query);

            if ($model->mode == 'grd') {
                if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_SEQ_HDAT ";
                    $query .= " WHERE ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    $query .= "     AND PATTERN_SEQ = '1' ";
                } else {
                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     HEXAM_ENTREMARK_HDAT ";
                    $query .= " WHERE ";
                    $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                }

                $db->query($query);
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
            $table_ent   = 'GRD_HEXAM_ENTREMARK_HDAT';
            $table_regdH = 'GRD_REGD_HDAT';
        } else {
            $table_regd  = 'SCHREG_REGD_DAT';
            $table_base  = 'SCHREG_BASE_MST';
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table_ent   = 'HEXAM_ENTREMARK_SEQ_HDAT';
            } else {
                $table_ent   = 'HEXAM_ENTREMARK_HDAT';
            }
            $table_regdH = 'SCHREG_REGD_HDAT';
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.COMMENTEX_A_CD, ";
        if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     T3.PATTERN_SEQ, ";
        }
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $model->tyousasyo2020) {
            $query .= "     T3.TOTALSTUDYACT, ";
            $query .= "     T3.TOTALSTUDYVAL, ";
        }
        if ("1" == $model->tyousasyo2020) {
            $query .= "     '' AS REMARK, ";
        } else {
            $query .= "     T3.REMARK, ";
        }
        if ($model->Properties["useHexamRemark2Flg"] == 1) {
            $query .= "     T3.REMARK2, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     {$table_regd} T1 ";
        $query .= "     LEFT JOIN {$table_base} T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN {$table_ent} T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN {$table_regdH} T4 ON  T4.YEAR     = T1.YEAR ";
        $query .= "                                 AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "                                 AND T4.GRADE    = T1.GRADE ";
        $query .= "                                 AND T4.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR   = T1.YEAR ";
        $query .= "                                   AND G1.GRADE  = T1.GRADE ";
        $query .= "                                   AND G1.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= "     AND (T4.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T4.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T4.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "          T4.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ("1" == $model->tyousasyo2020) {
                $query = " SELECT REMARK FROM HEXAM_ENTREMARK_REMARK_HDAT WHERE SCHREGNO = '".$row["SCHREGNO"]."' ";
                $row["REMARK"] = $db->getOne($query);
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
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
}
