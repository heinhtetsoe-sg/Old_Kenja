<?php

require_once('for_php7.php');
class knjx_d132hquery extends Query
{

    //年度一覧
    public function getYearSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->schoolkind) {
            $query .= "     AND T1.GRADE IN(SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = SUBSTR('".$model->field["YEAR"]."',1,4) AND ";
            $query .= "                         SCHOOL_KIND = '".$model->schoolkind."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

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
        $query .= "         YEAR      = '".$year."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学籍番号
    public function checkSchoolKind($model, &$data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = T2.YEAR ";
        $query .= "     AND T1.YEAR        = '".$data["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER    = '".$data["SEMESTER"]."' ";
        $query .= "     AND T1.SCHREGNO    = '".$data["SCHREGNO"]."' ";
        $query .= "     AND T1.GRADE       = T2.GRADE ";
        $query .= "     AND T2.SCHOOL_KIND = '".$model->schoolkind."' ";

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

            /*********************/
            /* HREPORTREMARK_DAT */
            /*********************/
            $data = array();
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            //データセット
            $data["COMMUNICATION"][TEXT]    = $data_arr[$i]["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            for ($div = 1; $div <= 2; $div++) {
                $setDiv = sprintf("%02d", $div);
                for ($code = 1; $code <= 2; $code++) {
                    $setCode = sprintf("%02d", $code);
                    if ($div == "2" && $model->schoolkind == "J") {
                        break;
                    }
                    if ($div == "2" && $code == "2") {
                        break;
                    }
                    //項目
                    $data = array();
                    $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]     = $data_arr[$i]["SEMESTER"];
                    $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                    $data["DIV"][TEXT]          = $setDiv;
                    $data["CODE"][TEXT]         = $setCode;
                    if ($div == "1" && $code == "1") {
                        $setRemark1 = $data_arr[$i]["REMARK1_01_01"];//委員会・係・部活動の成果
                    } elseif ($div == "1" && $code == "2") {
                        $setRemark1 = $data_arr[$i]["REMARK1_01_02"];//資格取得・検定・表彰など
                    } else {
                        $setRemark1 = $data_arr[$i]["REMARK1_02_01"];//研究活動テーマ
                    }
                    $data["REMARK1"][TEXT]      = $setRemark1;
                    $data["REGISTERCD"][TEXT]   = STAFFCD ;
                    $data["UPDATED"][FUNC]      = "sysdate()";
                    //存在チェック
                    $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT";
                    $query .= " WHERE ";
                    $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                    $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "     AND DIV      = '".$setDiv."' ";
                    $query .= "     AND CODE     = '".$setCode."' ";

                    if (0 < $db->getOne($query)) {
                        //更新
                        $where  = " WHERE ";
                        $where .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
                        $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "     AND DIV      = '".$setDiv."' ";
                        $where .= "     AND CODE     = '".$setCode."' ";
                        $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                    } else {
                        //追加
                        $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
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

            /*********************/
            /* HREPORTREMARK_DAT */
            /*********************/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ((DIV = '01' AND CODE IN ('01', '02')) ";
            if ($model->schoolkind == "H") {
                $query .= "      OR (DIV = '02' AND CODE IN ('01')) ";
            }
            $query .= "          ) ";
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
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     D01_01.REMARK1 AS REMARK1_01_01, ";
        $query .= "     D01_02.REMARK1 AS REMARK1_01_02, ";
        $query .= "     D02_01.REMARK1 AS REMARK1_02_01, ";
        $query .= "     HREP.COMMUNICATION, ";
        $query .= "     '".$model->lastColumn ."' AS ".$model->lastColumn ." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT HREP ON HREP.YEAR     = REGD.YEAR ";
        $query .= "                                     AND HREP.SEMESTER = REGD.SEMESTER ";
        $query .= "                                     AND HREP.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_01 ON D01_01.YEAR     = REGD.YEAR ";
        $query .= "                                              AND D01_01.SEMESTER = REGD.SEMESTER ";
        $query .= "                                              AND D01_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                              AND D01_01.DIV      = '01' ";
        $query .= "                                              AND D01_01.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_02 ON D01_02.YEAR     = REGD.YEAR ";
        $query .= "                                              AND D01_02.SEMESTER = REGD.SEMESTER ";
        $query .= "                                              AND D01_02.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                              AND D01_02.DIV      = '01' ";
        $query .= "                                              AND D01_02.CODE     = '02' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D02_01 ON D02_01.YEAR     = REGD.YEAR ";
        $query .= "                                              AND D02_01.SEMESTER = REGD.SEMESTER ";
        $query .= "                                              AND D02_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                              AND D02_01.DIV      = '02' ";
        $query .= "                                              AND D02_01.CODE     = '01' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
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
}
