<?php

require_once('for_php7.php');
class knjx_d139iquery extends Query
{

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEARSEMESTER"]."' ";
        if ($model->schoolkind) {
            $query .= "     AND T1.GRADE IN(SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "                         SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //学校種別チェック
    public function checkSchoolKind($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$data["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$data["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T2.SCHOOL_KIND = '".$model->schoolkind."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr, $recactcnt)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //BEHAVIOR_SEMES_DAT : del->ins
            $query  = " DELETE FROM BEHAVIOR_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "   AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "   AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            for ($ncnt = 1; $ncnt <= $recactcnt; $ncnt++) {
                $data = array();
                $data["YEAR"][TEXT] = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT] = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT] = $data_arr[$i]["SCHREGNO"];
                $data["RECORD"][TEXT] = $data_arr[$i]["RECACT".$ncnt];
                $data["CODE"][TEXT] = sprintf("%02d", $ncnt);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
                $db->query($query);
            }

            //1 - HREPORTREMARK_DETAIL_DAT : del->ins
            $query  = " DELETE FROM HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "   AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "   AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "   AND DIV = '01' ";
            $query .= "   AND CODE = '01' ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]       = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]   = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]   = $data_arr[$i]["SCHREGNO"];
            $data["DIV"][TEXT]        = '01';
            $data["CODE"][TEXT]       = '01';
            $data["REMARK1"][TEXT]    = $data_arr[$i]["TOTALSTUDY_TITLE"];
            $data["REMARK2"][TEXT]    = $data_arr[$i]["MORAL_TITLE"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";
            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);

            //1 - HREPORTREMARK_DETAIL_DAT (semester 9) : del->ins
            $query  = " DELETE FROM HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "   AND SEMESTER = '9' ";
            $query .= "   AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "   AND DIV = '01' ";
            $query .= "   AND CODE = '01' ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]       = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]   = "9";
            $data["SCHREGNO"][TEXT]   = $data_arr[$i]["SCHREGNO"];
            $data["DIV"][TEXT]        = '01';
            $data["CODE"][TEXT]       = '01';
            $data["REMARK3"][TEXT]    = $data_arr[$i]["REMARK_TCTITLE"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";
            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);

            //2 - HREPORTREMARK_DETAIL_DAT : del->ins
            $query  = " DELETE FROM HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "   AND SEMESTER = '9' ";
            $query .= "   AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "   AND DIV = '02' ";
            $query .= "   AND CODE = '01' ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]       = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]   = "9";
            $data["SCHREGNO"][TEXT]   = $data_arr[$i]["SCHREGNO"];
            $data["DIV"][TEXT]        = '02';
            $data["CODE"][TEXT]       = '01';
            $data["REMARK1"][TEXT]    = $data_arr[$i]["ACTCLB_1ST"];
            $data["REMARK2"][TEXT]    = $data_arr[$i]["ACTCLB_2ND"];
            $data["REMARK3"][TEXT]    = $data_arr[$i]["ACTCLB_3RD"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";
            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);

            //HREPORTREMARK_DAT : cnt->ins or upd
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            $data = array();
            //データセット
            $data["TOTALSTUDYTIME"][TEXT]   = $data_arr[$i]["TOTALSTUDY_DETAIL"];
            $data["REMARK1"][TEXT]          = $data_arr[$i]["MORAL_DETAIL"];
            $data["ATTENDREC_REMARK"][TEXT] = $data_arr[$i]["REMARK_ATTEND"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            //HREPORTREMARK_DAT : cnt->ins or upd
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            $data = array();
            //データセット
            $data["COMMUNICATION"][TEXT]    = $data_arr[$i]["REMARK_TCDETAIL"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = "9";
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '9' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     BEHAVIOR_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DIV      = '01' ";
            $query .= "     AND CODE     = '01' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DIV      = '01' ";
            $query .= "     AND CODE     = '01' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND DIV      = '02' ";
            $query .= "     AND CODE     = '01' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
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
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model, $recactcnt)
    {
        $field = "";
        $lj_str = "";
        foreach ($model->fieldSize as $key => $val) {
            $lj_substr = "";
            for ($ncnt = 1; $ncnt <= $recactcnt; $ncnt++) {
                if ("RECACT".$ncnt == $key) { //BEHAVIOR_SEMES_DAT.RECORD : year,semester,schregno,CODE=$ncnt
                    $tableSoeji = " RA".$ncnt.".RECORD AS " . $key . " ";
                    $lj_substr = " LEFT JOIN BEHAVIOR_SEMES_DAT RA".$ncnt." ON RA".$ncnt.".YEAR = T1.YEAR AND RA".$ncnt.".SEMESTER = T1.SEMESTER AND RA".$ncnt.".SCHREGNO = T1.SCHREGNO AND RA".$ncnt.".CODE = '".sprintf("%02d", $ncnt)."' ";
                }
            }
            if ("TOTALSTUDY_TITLE" == $key) {  //HREPORTREMARK_DETAIL_DAT.REMARK1 : year,semester,schregno,div/code=0101
                $tableSoeji = " T4.REMARK1 AS " . $key . " ";
            } elseif ("MORAL_TITLE" == $key) {       //HREPORTREMARK_DETAIL_DAT.REMARK2 : year,semester,schregno,div/code=0101
                $tableSoeji = " T4.REMARK2 AS " . $key . " ";
            } elseif ("REMARK_TCTITLE" == $key) {    //HREPORTREMARK_DETAIL_DAT.REMARK3 : year,semester,schregno,div/code=0101
                $tableSoeji = " T8.REMARK3 AS " . $key . " ";
            } elseif ("ACTCLB_1ST" == $key) {        //HREPORTREMARK_DETAIL_DAT.REMARK1 : year,schregno,semester=9,div/code=0201
                $tableSoeji = " T5.REMARK1 AS " . $key . " ";
            } elseif ("ACTCLB_2ND" == $key) {        //HREPORTREMARK_DETAIL_DAT.REMARK2 : year,schregno,semester=9,div/code=0201
                $tableSoeji = " T5.REMARK2 AS " . $key . " ";
            } elseif ("ACTCLB_3RD" == $key) {        //HREPORTREMARK_DETAIL_DAT.REMARK3 : year,schregno,semester=9,div/code=0201
                $tableSoeji = " T5.REMARK3 AS " . $key . " ";
            } elseif ("TOTALSTUDY_DETAIL" == $key) { //HREPORTREMARK_DAT.TOTALSTUDYTIME   : year,semester,schregno
                $tableSoeji = " T6.TOTALSTUDYTIME AS " . $key . " ";
            } elseif ("MORAL_DETAIL" == $key) {      //HREPORTREMARK_DAT.REMARK1          : year,semester,schregno
                $tableSoeji = " T6.REMARK1 AS " . $key . " ";
            } elseif ("REMARK_ATTEND" == $key) {     //HREPORTREMARK_DAT.ATTENDREC_REMARK : year,semester,schregno
                $tableSoeji = " T6.ATTENDREC_REMARK AS " . $key . " ";
            } elseif ("REMARK_TCDETAIL" == $key) {   //HREPORTREMARK_DAT.COMMUNICATION    : year,semester,schregno
                $tableSoeji = " T7.COMMUNICATION AS " . $key . " ";
            } elseif (strpos($key, "RECACT") === false) {
                $tableSoeji = "";
            }
            $field .= $tableSoeji.",";
            $lj_str .= $lj_substr;
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T6 ";
        $query .= "           ON T6.YEAR = T1.YEAR ";
        $query .= "          AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T6.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T4 ";
        $query .= "           ON T4.YEAR = T1.YEAR ";
        $query .= "          AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T4.DIV      = '01' ";
        $query .= "          AND T4.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T8 ";
        $query .= "           ON T8.YEAR = T1.YEAR ";
        $query .= "          AND T8.SEMESTER = '9' ";
        $query .= "          AND T8.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T8.DIV      = '01' ";
        $query .= "          AND T8.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT T5 ";
        $query .= "           ON T5.YEAR = T1.YEAR ";
        $query .= "          AND T5.SEMESTER = '9' ";
        $query .= "          AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T5.DIV      = '02' ";
        $query .= "          AND T5.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T7 ";
        $query .= "           ON T7.YEAR = T1.YEAR ";
        $query .= "          AND T7.SEMESTER = '9' ";
        $query .= "          AND T7.SCHREGNO = T1.SCHREGNO ";
        $query .= "     ".$lj_str." ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEARSEMESTER"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->schoolkind) {
            $query .= "     AND T1.GRADE IN(SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "                         SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }
}
