<?php

require_once('for_php7.php');
class knjx_d135jquery extends Query
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

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1     = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        return $query;
    }

    //学期制取得
    public function getMaxSemester()
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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
            $data["ATTENDREC_REMARK"][TEXT]    = $data_arr[$i]["ATTENDREC_REMARK"];
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
            /* HREPORTREMARK_DAT(9学期) */
            /****************************/
            $data = array();
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'";
            $dataCnt = $db->getOne($query);

            //データセット
            $data["REMARK1"][TEXT]      = $data_arr[$i]["MORAL"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = "9";
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER = '9' ";
                $where .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            }
            $db->query($query);

            $semester = substr($model->field["YEAR"], 4, 1);
            $semester2 = ($model->Properties["tutisyoSougouHyoukaTunen"] == 1) ? "9" : $semester;
            
            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            for ($div = 1; $div <= 4; $div++) {
                $setDiv = sprintf("%02d", $div);
                for ($code = 1; $code <= 2; $code++) {
                    $setCode = sprintf("%02d", $code);
                    if ($div == "2" && $code == "2") {
                        break;
                    }
                    if ($div == "3" && $code == "2") {
                        break;
                    }
                    if ($div == "4" && $code == "2") {
                        break;
                    }
                    //項目
                    $data = array();
                    $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                    
                    if ($div == "1" && $code == "1") {
                        $inSemester = $semester2;
                    } elseif ($div == "1" && $code == "2") {
                        $inSemester = $semester2;
                    } elseif ($div == "2" && $code == "1") {
                        $inSemester = $semester;
                    } elseif ($div == "3" && $code == "1") {
                        $inSemester = '9';
                    } elseif ($div == "4" && $code == "1") {
                        if ($model->maxSemester == "3" && $model->Properties["tutisyoShokenntunen"] == "1") {
                            $inSemester = '9';
                        } else {
                            $inSemester = $semester;
                        }
                    }
                    $data["SEMESTER"][TEXT]     = $inSemester;
                    $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                    $data["DIV"][TEXT]          = $setDiv;
                    $data["CODE"][TEXT]         = $setCode;
                    if ($div == "1" && $code == "1") {
                        $setRemark1 = $data_arr[$i]["DETAIL_01_01_REMARK1"];//総合的な学習の時間（ 追記 ）学習活動
                    } elseif ($div == "1" && $code == "2") {
                        $setRemark1 = $data_arr[$i]["DETAIL_01_02_REMARK1"];//総合的な学習の時間（ 追記 ）評価
                    } elseif ($div == "2" && $code == "1") {
                        $setRemark1 = $data_arr[$i]["DETAIL_02_01_REMARK1"];//特別活動
                    } elseif ($div == "3" && $code == "1") {
                        $setRemark1 = $data_arr[$i]["DETAIL_03_01_REMARK1"];//部活動（ 追記 ）
                    } elseif ($div == "4" && $code == "1") {
                        $setRemark1 = $data_arr[$i]["DETAIL_04_01_REMARK1"];//所見
                    }
            
                    $data["REMARK1"][TEXT]      = $setRemark1;
                    $data["REGISTERCD"][TEXT]   = STAFFCD ;
                    $data["UPDATED"][FUNC]      = "sysdate()";
                    //存在チェック
                    $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT";
                    $query .= " WHERE ";
                    $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "     AND SEMESTER = '".$inSemester."' ";
                    $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "     AND DIV      = '".$setDiv."' ";
                    $query .= "     AND CODE     = '".$setCode."' ";
                    if (0 < $db->getOne($query)) {
                        //更新
                        $where  = " WHERE ";
                        $where .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "     AND SEMESTER = '".$inSemester."' ";
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
        $semester = substr($model->field["YEAR"], 4, 1);
        $semester2 = ($model->Properties["tutisyoSougouHyoukaTunen"] == 1) ? "9" : $semester;
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
            /* HREPORTREMARK_DAT(9学期) */
            /****************************/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);

            /****************************/
            /* HREPORTREMARK_DETAIL_DAT */
            /****************************/
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$semester2."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ((DIV = '01' AND CODE IN ('01', '02')) ";
            $query .= "          ) ";
            $db->query($query);
            
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ((DIV = '02' AND CODE IN ('01')) ";
            $query .= "          ) ";
            $db->query($query);
            
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ((DIV = '03' AND CODE IN ('01')) ";
            $query .= "          ) ";
            $db->query($query);
            
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            if ($model->maxSemester == "3" && $model->Properties["tutisyoShokenntunen"] == "1") {
                $query .= "     AND SEMESTER = '9' ";
            } else {
                $query .= "     AND SEMESTER = '".$semester."' ";
            }
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ((DIV = '04' AND CODE IN ('01')) ";
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
        $semester = substr($model->field["YEAR"], 4, 1);
        $semester2 = ($model->Properties["tutisyoSougouHyoukaTunen"] == 1) ? "9" : $semester;
    
        $query .= " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SEMESTER, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     D01_01.REMARK1 AS DETAIL_01_01_REMARK1, ";
        $query .= "     D01_02.REMARK1 AS DETAIL_01_02_REMARK1, ";
        $query .= "     D02_01.REMARK1 AS DETAIL_02_01_REMARK1, ";
        $query .= "     D03_01.REMARK1 AS DETAIL_03_01_REMARK1, ";
        $query .= "     D04_01.REMARK1 AS DETAIL_04_01_REMARK1, ";
        if ($model->schoolName == "kyoto") {
            $query .= "     HREP9.REMARK1  AS MORAL, ";
        }
        $query .= "     HREP.ATTENDREC_REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN HREPORTREMARK_DAT HREP ON HREP.YEAR     = REGD.YEAR ";
        $query .= "                                  AND HREP.SEMESTER = REGD.SEMESTER ";
        $query .= "                                  AND HREP.SCHREGNO = REGD.SCHREGNO ";
        $query .= " LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_01 ON D01_01.YEAR     = REGD.YEAR ";
        $query .= "                                          AND D01_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                          AND D01_01.DIV      = '01' ";
        $query .= "                                          AND D01_01.CODE     = '01' ";
        $query .= "                                          AND D01_01.SEMESTER = '{$semester2}' ";
        $query .= " LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_02 ON D01_02.YEAR     = REGD.YEAR ";
        $query .= "                                          AND D01_02.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                          AND D01_02.DIV      = '01' ";
        $query .= "                                          AND D01_02.CODE     = '02' ";
        $query .= "                                          AND D01_02.SEMESTER = '{$semester2}' ";
        $query .= " LEFT JOIN HREPORTREMARK_DETAIL_DAT D02_01 ON D02_01.YEAR     = REGD.YEAR ";
        $query .= "                                          AND D02_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                          AND D02_01.DIV      = '02' ";
        $query .= "                                          AND D02_01.CODE     = '01' ";
        $query .= "                                          AND D02_01.SEMESTER = '{$semester}' ";
        $query .= " LEFT JOIN HREPORTREMARK_DETAIL_DAT D03_01 ON D03_01.YEAR     = REGD.YEAR ";
        $query .= "                                          AND D03_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                          AND D03_01.DIV      = '03' ";
        $query .= "                                          AND D03_01.CODE     = '01' ";
        $query .= "                                          AND D03_01.SEMESTER = '9' ";
        $query .= " LEFT JOIN HREPORTREMARK_DETAIL_DAT D04_01 ON D04_01.YEAR     = REGD.YEAR ";
        $query .= "                                          AND D04_01.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                          AND D04_01.DIV      = '04' ";
        $query .= "                                          AND D04_01.CODE     = '01' ";
        if ($model->maxSemester == "3" && $model->Properties["tutisyoShokenntunen"] == "1") {
            $query .= "                                          AND D04_01.SEMESTER = '9' ";
        } else {
            $query .= "                                          AND D04_01.SEMESTER = '{$semester}' ";
        }
        if ($model->schoolName == "kyoto") {
            $query .= " LEFT JOIN HREPORTREMARK_DAT HREP9 ON HREP9.YEAR     = REGD.YEAR ";
            $query .= "                                  AND HREP9.SEMESTER = '9' ";
            $query .= "                                  AND HREP9.SCHREGNO = REGD.SCHREGNO ";
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
}
