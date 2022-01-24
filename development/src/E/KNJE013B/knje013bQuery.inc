<?php

require_once('for_php7.php');
class knje013bquery extends Query
{
    public function getGrade($model)
    {
        $query .= " SELECT ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL, ";
        $query .= "     GDAT.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " INNER JOIN V_NAME_MST MST ";
        $query .= "     ON MST.YEAR = GDAT.YEAR ";
        $query .= "     AND MST.NAMECD1 = 'A023' ";
        $query .= "     AND MST.NAME1 = 'H' ";
        $query .= "     AND GDAT.GRADE BETWEEN MST.NAMESPARE2 AND MST.NAMESPARE3 ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '" . substr($model->field["YEAR"], 0, 4) . "' ";
        $query .= "     AND GDAT.SCHOOL_KIND = 'H' ";

        return $query;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX186' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getYearSeme()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER  AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    public function getHrClass($model)
    {
        $query .= " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学籍番号
    public function schregNoCntSql($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //学期
    public function semesterCntSql($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //GHR_CD
    public function ghrCdCntSql($year, $semester, $ghrCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GHR_CD   = '{$ghrCd}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->field['OUTPUTTYPE'] == '1') {
                //データセット
                $data = array();
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
                $data["TOTALSTUDYACT"][TEXT]    = $data_arr[$i]["TOTALSTUDYACT"];
                $data["TOTALSTUDYVAL"][TEXT]    = $data_arr[$i]["TOTALSTUDYVAL"];
                $data["SPECIALACTREMARK"][TEXT] = $data_arr[$i]["SPECIALACTREC"];
                $data["ATTENDREC_REMARK"][TEXT] = $data_arr[$i]["ATTENDREC_REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."'") > 0) {
                    $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "   AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";

                    $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HTRAINREMARK_DAT");
                }
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["TRAIN_REF1"][TEXT]       = $data_arr[$i]["REMARK101"].$data_arr[$i]["REMARK102"];
                $data["TRAIN_REF2"][TEXT]       = $data_arr[$i]["REMARK103"].$data_arr[$i]["REMARK104"];
                $data["TRAIN_REF3"][TEXT]       = $data_arr[$i]["REMARK105"].$data_arr[$i]["REMARK106"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."'") > 0) {
                    $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "   AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";

                    $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL_DAT");
                }
                $db->query($query);
                $cnt++;
            }
            if ($model->field['OUTPUTTYPE'] == '2') {
                if ($key == $data_arr[$i]["SCHREGNO"]) {
                    $data_arr[$i]["TOTALSTUDYACT"] = $totalstudyact;
                    $data_arr[$i]["TOTALSTUDYVAL"] = $totalstudyval;
                } else {
                    $key = $data_arr[$i]["SCHREGNO"];
                    $totalstudyact = $data_arr[$i]["TOTALSTUDYACT"];
                    $totalstudyval = $data_arr[$i]["TOTALSTUDYVAL"];
                }
                //データセット
                $data = array();
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["TOTALSTUDYACT"][TEXT]    = $data_arr[$i]["TOTALSTUDYACT"];
                $data["TOTALSTUDYVAL"][TEXT]    = $data_arr[$i]["TOTALSTUDYVAL"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if ($db->getOne("SELECT COUNT(*) FROM HEXAM_ENTREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'") > 0) {
                    $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_HDAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HEXAM_ENTREMARK_HDAT");
                }
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
                $data["SPECIALACTREC"][TEXT]    = $data_arr[$i]["SPECIALACTREMARK"];
                $data["ATTENDREC_REMARK"][TEXT] = $data_arr[$i]["ATTENDREC_REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if ($db->getOne("SELECT COUNT(*) FROM HEXAM_ENTREMARK_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."'") > 0) {
                    $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "   AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";

                    $query = Query::updateSQL($data, "HEXAM_ENTREMARK_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HEXAM_ENTREMARK_DAT");
                }
                $db->query($query);

                $seqs = array('101', '103', '105');
                for ($j = 0; $j < get_count($seqs); $j++) {
                    if ($seqs[$j] == '101') {
                        $trainRef = $data_arr[$i]["TRAIN_REF1"];
                    }
                    if ($seqs[$j] == '103') {
                        $trainRef = $data_arr[$i]["TRAIN_REF2"];
                    }
                    if ($seqs[$j] == '105') {
                        $trainRef = $data_arr[$i]["TRAIN_REF3"];
                    }
                    $data = array();
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["REMARK"][TEXT]           = $trainRef;
                    $data["TRAIN_SEQ"][TEXT]        = $seqs[$j];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    if ($db->getOne("SELECT COUNT(*) FROM HEXAM_ENTREMARK_TRAINREF_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."' AND TRAIN_SEQ = '".$seqs[$j]."'") > 0) {
                        $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND TRAIN_SEQ = '".$seqs[$j]."' ";

                        $query = Query::updateSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT", $where);
                    } else {
                        $query = Query::insertSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT");
                    }
                    $db->query($query);
                }
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /************************/
    /**  ＣＳＶ削除で使用  **/
    /************************/
    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->field['OUTPUTTYPE'] == '1') {
                $query  = " DELETE FROM HTRAINREMARK_DAT ";
                $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $db->query($query);

                $query  = " DELETE FROM HTRAINREMARK_DETAIL_DAT ";
                $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $db->query($query);
                $cnt++;
            }
            if ($model->field['OUTPUTTYPE'] == '2') {
                $query  = " DELETE FROM HEXAM_ENTREMARK_HDAT ";
                $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $db->query($query);

                $query  = " DELETE FROM HEXAM_ENTREMARK_DAT ";
                $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $db->query($query);

                $seqs = array('101','102','103','104','105','106');
                for ($j = 0; $j < get_count($seqs); $j++) {
                    $query  = " DELETE FROM HEXAM_ENTREMARK_TRAINREF_DAT ";
                    $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND TRAIN_SEQ     = '".$seqs[$j]."' ";
                    $db->query($query);
                }
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query .= " WITH MAIN1 AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR || REGD.SEMESTER = '{$model->field['YEAR']}' ";
        $query .= "     AND REGD.GRADE = '{$model->field['GRADE']}' ";
        if ($model->field['HR_CLASS'] != '') {
            $query .= "     AND REGD.HR_CLASS = '{$model->field['HR_CLASS']}' ";
        }
        $query .= " ), MAIN2 AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.ANNUAL ";
        $query .= " FROM ";
        $query .= "     MAIN1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON MAIN1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     AND REGD.GRADE < '{$model->field['GRADE']}' ";
        $query .= " INNER JOIN V_NAME_MST MST ";
        $query .= "     ON MST.YEAR = MAIN1.YEAR ";
        $query .= "     AND MST.NAMECD1 = 'A023' ";
        $query .= "     AND MST.NAME1 = 'H' ";
        $query .= "     AND REGD.GRADE BETWEEN MST.NAME2 AND MST.NAME3 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.YEAR, ";
        $query .= "     BASE.GRADE, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.ANNUAL, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL, ";
        $query .= "     T1.SPECIALACTREMARK, ";
        $query .= "     T2.TRAIN_REF1, ";
        $query .= "     '' AS DUMMY1, ";
        $query .= "     T2.TRAIN_REF2, ";
        $query .= "     '' AS DUMMY2, ";
        $query .= "     T2.TRAIN_REF3, ";
        $query .= "     '' AS DUMMY3, ";
        $query .= "     T1.ATTENDREC_REMARK, ";
        $query .= "     '{$model->lastColumn}' AS {$model->lastColumn} ";
        $query .= " FROM ";
        $query .= "     MAIN2 BASE ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "     ON GDAT.YEAR = BASE.YEAR ";
        $query .= "     AND GDAT.GRADE = BASE.GRADE ";
        $query .= " LEFT JOIN HTRAINREMARK_DAT T1 ";
        $query .= "     ON T1.YEAR = BASE.YEAR ";
        $query .= "     AND T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN HTRAINREMARK_DETAIL_DAT T2 ";
        $query .= "     ON T2.YEAR = BASE.YEAR ";
        $query .= "     AND T2.SCHREGNO = BASE.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     ANNUAL ";

        return $query;
    }
    //CSVデータ出力
    public function selectMainQuery2($model)
    {
        $query .= " SELECT ";
        $query .= "     BASE.YEAR, ";
        $query .= "     BASE.GRADE, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.ANNUAL, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL, ";
        $query .= "     T2.SPECIALACTREC, ";
        $query .= "     T3.REMARK AS REMARK101, ";
        $query .= "     T4.REMARK AS REMARK102, ";
        $query .= "     T5.REMARK AS REMARK103, ";
        $query .= "     T6.REMARK AS REMARK104, ";
        $query .= "     T7.REMARK AS REMARK105, ";
        $query .= "     T8.REMARK AS REMARK106, ";
        $query .= "     T2.ATTENDREC_REMARK, ";
        $query .= "     '{$model->lastColumn}' AS {$model->lastColumn} ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT BASE ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_HDAT T1 ";
        $query .= "     ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_DAT T2 ";
        $query .= "     ON T2.YEAR = BASE.YEAR ";
        $query .= "     AND T2.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T3 ";
        $query .= "     ON T3.YEAR = BASE.YEAR ";
        $query .= "     AND T3.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T3.TRAIN_SEQ = '101' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4 ";
        $query .= "     ON T4.YEAR = BASE.YEAR ";
        $query .= "     AND T4.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T4.TRAIN_SEQ = '102' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T5 ";
        $query .= "     ON T5.YEAR = BASE.YEAR ";
        $query .= "     AND T5.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T5.TRAIN_SEQ = '103' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T6 ";
        $query .= "     ON T6.YEAR = BASE.YEAR ";
        $query .= "     AND T6.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T6.TRAIN_SEQ = '104' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T7 ";
        $query .= "     ON T7.YEAR = BASE.YEAR ";
        $query .= "     AND T7.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T7.TRAIN_SEQ = '105' ";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T8 ";
        $query .= "     ON T8.YEAR = BASE.YEAR ";
        $query .= "     AND T8.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     AND T8.TRAIN_SEQ = '106' ";
        $query .= " WHERE ";
        $query .= "     BASE.YEAR || BASE.SEMESTER = '{$model->field['YEAR']}' ";
        $query .= "     AND BASE.GRADE = '{$model->field['GRADE']}' ";
        if ($model->field['HR_CLASS'] != '') {
            $query .= "     AND BASE.HR_CLASS = '{$model->field['HR_CLASS']}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     ANNUAL ";

        return $query;
    }
}
