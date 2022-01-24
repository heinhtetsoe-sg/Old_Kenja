<?php

require_once('for_php7.php');
class knjx_h211query extends Query
{

    //学年一覧コンボ
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //存在チェック（在籍）
    public function checkRegdDat($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック（学籍番号）
    public function checkRegdBaseMst($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック（寮コード）
    public function checkDomiCd($model, $year, $domiCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     DOMITORY_YDAT T1, ";
        $query .= "     DOMITORY_MST T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.DOMI_CD = T2.DOMI_CD ";
        $query .= "     AND T1.DOMI_CD = '".$domiCd."' ";

        return $query;
    }

    public function checkDomitoryHistDat($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '".$data["SCHREGNO"]."' ";
        if ($data["DOMI_OUTDAY"] != "") {
            //内側から始まる
            $query .= "      AND ((DOMI_ENTDAY    <= '".$data["DOMI_ENTDAY"]."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".$data["DOMI_ENTDAY"]."')) ";
            //内側で止まる
            $query .= "           OR (DOMI_ENTDAY <= '".$data["DOMI_OUTDAY"]."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY >= '".$data["DOMI_OUTDAY"]."')) ";
            //範囲を包含
            $query .= "           OR (DOMI_ENTDAY >= '".$data["DOMI_ENTDAY"]."' AND ( DOMI_OUTDAY IS NULL OR DOMI_OUTDAY <= '".$data["DOMI_OUTDAY"]."'))) ";
        } else {
            $query .= "      AND (DOMI_OUTDAY IS NULL OR DOMI_OUTDAY > '".$data["DOMI_ENTDAY"]."' ) ";
        }

        return $query;
    }

    public function checkRegdDatDate($model, $year, $schregno, $checkDate)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "          ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND '".$checkDate."' BETWEEN T2.SDATE AND T2.EDATE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     SCHREG_DOMITORY_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DOMI_CD      = '".$data_arr[$i]["DOMI_CD"]."' ";
            $query .= "     AND DOMI_ENTDAY  = '".$data_arr[$i]["DOMI_ENTDAY"]."'";

            $dataCnt = $db->getOne($query);

            $data["DOMI_OUTDAY"][TEXT]    = $data_arr[$i]["DOMI_OUTDAY"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "         SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "     AND DOMI_CD      = '".$data_arr[$i]["DOMI_CD"]."' ";
                $where .= "     AND DOMI_ENTDAY  = '".$data_arr[$i]["DOMI_ENTDAY"]."' ";

                $query = Query::updateSQL($data, "SCHREG_DOMITORY_HIST_DAT", $where);
            } else {
                $data["SCHREGNO"][TEXT]       = $data_arr[$i]["SCHREGNO"];
                $data["DOMI_CD"][TEXT]        = $data_arr[$i]["DOMI_CD"];
                $data["DOMI_ENTDAY"][TEXT]    = $data_arr[$i]["DOMI_ENTDAY"];
                $data["REMARK"][TEXT]         = "";

                $query = Query::insertSQL($data, "SCHREG_DOMITORY_HIST_DAT");
            }

            $db->query($query);

            $cnt++;
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
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_DOMITORY_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DOMI_CD     = '".$data_arr[$i]["DOMI_CD"]."' ";
            $query .= "     AND DOMI_ENTDAY = '".$data_arr[$i]["DOMI_ENTDAY"]."' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
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

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     DISTINCT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     DD1.DOMI_CD, ";
        $query .= "     DM1.DOMI_NAME, ";
        $query .= "     DD1.DOMI_ENTDAY, ";
        $query .= "     DD1.DOMI_OUTDAY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_DOMITORY_HIST_DAT DD1 ON DD1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN DOMITORY_MST DM1 ON DM1.DOMI_CD = DD1.DOMI_CD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' ";
                $query .= "     FROM SCHREG_REGD_DAT I1 ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ";
                $query .= "     WHERE I1.SCHREGNO = T1.SCHREGNO ";
                $query .= "       AND I1.YEAR = '".CTRL_YEAR."' ";
                $query .= "       AND (I2.SCHOOL_KIND IS NULL OR I2.SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."')) ";
                $query .= " ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' ";
            $query .= "     FROM SCHREG_REGD_DAT I1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ";
            $query .= "     WHERE I1.SCHREGNO = T1.SCHREGNO ";
            $query .= "       AND I1.YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND (I2.SCHOOL_KIND IS NULL OR I2.SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }
}
