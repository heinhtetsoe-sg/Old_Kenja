<?php

require_once('for_php7.php');
class knjx_e015query extends Query
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
    
    //学期チェック
    public function checkSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    // 指導上参考となる諸事項
    public function getHexamEntremarkTrainrefDat($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND TRAIN_SEQ = '".$seq."' ";
        return $query;
    }

    // 備考
    public function getHexamEntremarkLearningDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_LEARNING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "        INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = 'H' ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT T1.GRADE, T1.HR_CLASS, T1.HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1, ";
        $query .= "      SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "   AND T1.YEAR = T2.YEAR ";
        $query .= "   AND T1.GRADE = T2.GRADE ";
        //校種対応
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOL_KIND != "") {
            $query .= "     T2.SCHOOL_KIND IN ('".SCHOOLKIND."') ";
        }
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "  AND ( ";
            $query .= "       '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "       OR ";
            $query .= "       T1.GRADE IN ";
            $query .= "           (SELECT ";
            $query .= "                FIELD2 ";
            $query .= "            FROM ";
            $query .= "                STAFF_DETAIL_MST ST ";
            $query .= "            WHERE ";
            $query .= "                ST.YEAR = T1.YEAR ";
            $query .= "                AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                AND ST.FIELD1 = '0200') ";
            $query .= "      ) ";
        }
        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS";

        return $query;
    }

    //生徒のクラス
    public function getGradeHrclassSql($model, $year, $schregno)
    {
        $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON ";
        $query .= "     T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ON ";
        $query .= "         T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE T3.SCHREGNO = '{$schregno}' ";
        $query .= "   AND T1.YEAR = '{$year}' ";
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "  AND ( ";
            $query .= "       '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "       OR ";
            $query .= "       T1.GRADE IN ";
            $query .= "           (SELECT ";
            $query .= "                FIELD2 ";
            $query .= "            FROM ";
            $query .= "                STAFF_DETAIL_MST ST ";
            $query .= "            WHERE ";
            $query .= "                ST.YEAR = '{$year}' ";
            $query .= "                AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                AND ST.FIELD1 = '0200') ";
            $query .= "      ) ";
        }
        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS";

        return $query;
    }

    //年次の取得
    public function getAnnual($schregno, $year)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     YEAR     = '{$year}' ";

        return $query;
    }

    //マスタの追加(CSVデータより読込)
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= 6; $j++) {
                //データセット
                $data = array();
                $data["REMARK"][TEXT]           = $data_arr[$i]['SEQ00'.$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query  = " SELECT ";
                $query .= "     * ";
                $query .= " FROM ";
                $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                $query .= "     AND TRAIN_SEQ = '00{$j}' ";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($row)) {
                    if ($model->field["SHORI_MEI"] == "3") {
                        $remark = $row["REMARK"];
                        if (substr($remark, -1) != "\n") {
                            $remark .= "\n";
                        }
                        $data["REMARK"][TEXT] = $remark . $data_arr[$i]['SEQ00'.$j];
                    }
                    $where  = "WHERE ";
                    $where .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                    $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
                    $where .= "     AND TRAIN_SEQ = '00{$j}' ";

                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT", $where);
                } else {
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["TRAIN_SEQ"][TEXT]        = '00'.$j;
                    $query = Query::insertSQL($data, 'HEXAM_ENTREMARK_TRAINREF_DAT');
                }
                $db->query($query);
            }

            if ($model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
                //データセット(まなびの記録)
                $data = array();
                $data["REMARK"][TEXT]           = $data_arr[$i]['REMARK'];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query  = " SELECT ";
                $query .= "     * ";
                $query .= " FROM ";
                $query .= "     HEXAM_ENTREMARK_LEARNING_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($row)) {
                    if ($model->field["SHORI_MEI"] == "3") {
                        $remark = $row["REMARK"];
                        if (substr($remark, -1) != "\n") {
                            $remark .= "\n";
                        }
                        $data["REMARK"][TEXT] = $remark . $data_arr[$i]['REMARK'];
                    }
                    $where  = "WHERE ";
                    $where .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                    $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_LEARNING_DAT", $where);
                } else {
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $query = Query::insertSQL($data, 'HEXAM_ENTREMARK_LEARNING_DAT');
                }
                $db->query($query);
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
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
            $db->query($query);

            if ($model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
                //まなびの記録
                $query  = " DELETE ";
                $query .= " FROM ";
                $query .= "     HEXAM_ENTREMARK_LEARNING_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR     = '{$data_arr[$i]["YEAR"]}' AND ";
                $query .= "     SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";
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
    public function selectMainQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     T2.REMARK AS SEQ001, ";
        $query .= "     T3.REMARK AS SEQ002, ";
        $query .= "     T4.REMARK AS SEQ003, ";
        $query .= "     T5.REMARK AS SEQ004, ";
        $query .= "     T6.REMARK AS SEQ005, ";
        $query .= "     T7.REMARK AS SEQ006, ";
        if ($model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
            $query .= "     LEARNING.REMARK AS REMARK, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T2 ";
        $query .= "     ON REGD.YEAR = T2.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T2.TRAIN_SEQ = '001' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T3 ";
        $query .= "     ON REGD.YEAR = T3.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T3.SCHREGNO ";
        $query .= "     AND T3.TRAIN_SEQ = '002' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4 ";
        $query .= "     ON REGD.YEAR = T4.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T4.SCHREGNO ";
        $query .= "     AND T4.TRAIN_SEQ = '003' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T5 ";
        $query .= "     ON REGD.YEAR = T5.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T5.SCHREGNO ";
        $query .= "     AND T5.TRAIN_SEQ = '004' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T6 ";
        $query .= "     ON REGD.YEAR = T6.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T6.SCHREGNO ";
        $query .= "     AND T6.TRAIN_SEQ = '005' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T7 ";
        $query .= "     ON REGD.YEAR = T7.YEAR ";
        $query .= "     AND REGD.SCHREGNO = T7.SCHREGNO ";
        $query .= "     AND T7.TRAIN_SEQ = '006' ";
        if ($model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
            $query .= " LEFT JOIN HEXAM_ENTREMARK_LEARNING_DAT LEARNING ON REGD.YEAR     = LEARNING.YEAR ";
            $query .= "                                                AND REGD.SCHREGNO = LEARNING.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR || REGD.SEMESTER ='{$model->field["YEAR"]}' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->schoolkind) {
            $query .= "     AND REGD.GRADE IN(SELECT ";
            $query .= "                           GRADE ";
            $query .= "                       FROM ";
            $query .= "                           SCHREG_REGD_GDAT ";
            $query .= "                       WHERE ";
            $query .= "                               YEAR        = SUBSTR('".$model->field["YEAR"]."',1,4) ";
            $query .= "                           AND SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";
        
        return $query;
    }

    //存在チェック(学籍基礎データ)
    public function getExistsSchregBase($schregno)
    {
        $query   = " SELECT ";
        $query  .= "   COUNT(*) ";
        $query  .= " FROM ";
        $query  .= "   SCHREG_BASE_MST ";
        $query  .= " WHERE ";
        $query  .= "   SCHREGNO = '{$schregno}' ";

        return $query;
    }
}
