<?php

require_once('for_php7.php');
class knjx180query extends Query
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

    public function getSchoolNm()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT T1.GRADE,T1.HR_CLASS,T1.HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= "ORDER BY T1.GRADE,T1.HR_CLASS";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新対象テーブルチェック
        $check_HTRAINREMARK_DETAIL_DAT = false;
        $check_HTRAINREMARK_DETAIL2_DAT = false;
        $check_HTRAINREMARK_DETAIL2_DAT_005 = false;
        $check_HTRAINREMARK_TRAINREF_DAT = false;
        foreach ($model->fieldSize as $key => $val) {
            if (in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                $check_HTRAINREMARK_DETAIL_DAT = true;
            }
            if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                $check_HTRAINREMARK_DETAIL2_DAT = true;
            }
            if (in_array($key, array("REMARK1_005"))) {
                $check_HTRAINREMARK_DETAIL2_DAT_005 = true;
            }
            if (in_array($key, array("TRAIN_REF4", "TRAIN_REF5", "TRAIN_REF6"))) {
                $check_HTRAINREMARK_TRAINREF_DAT = true;
            }
        }
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            $check_HTRAINREMARK_DETAIL_DAT = false;
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット(HTRAINREMARK_DAT)
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
            foreach ($model->fieldSize as $key => $val) {
                if (!in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3", "REMARK1_003", "REMARK2_003", "REMARK1_005", "TRAIN_REF4", "TRAIN_REF5", "TRAIN_REF6"))) {
                    $data[$key][TEXT]    = $data_arr[$i][$key];
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HTRAINREMARK_DAT");
            } else {
                $where  = "WHERE YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "  AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
            }
            $db->query($query);

            if ($check_HTRAINREMARK_DETAIL_DAT) {
                //データセット(HTRAINREMARK_DETAIL_DAT)
                $data = array();
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                foreach ($model->fieldSize as $key => $val) {
                    if (in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                        $data[$key][TEXT]    = $data_arr[$i][$key];
                    }
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                    $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL_DAT");
                } else {
                    $where  = "WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                    $where .= "      AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            if ($check_HTRAINREMARK_DETAIL2_DAT) {
                if ($model->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
                    //データセット(HTRAINREMARK_DETAIL2_DAT)
                    $data = array();
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    foreach ($model->fieldSize as $key => $val) {
                        if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                            list($fieldName, $seq) = explode('_', $key);
                            if ("REMARK1_003" == $key) {
                                $data["TOTALSTUDYACT2"][TEXT] = $data_arr[$i][$key];
                            } elseif ("REMARK2_003" == $key) {
                                $data["TOTALSTUDYVAL2"][TEXT] = $data_arr[$i][$key];
                            }
                        }
                    }
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_HDAT");
                    } else {
                        $where  = " WHERE ";
                        $where .= "       SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_HDAT", $where);
                    }
                    $db->query($query);
                } else {
                    //データセット(HTRAINREMARK_DETAIL2_DAT)
                    $data = array();
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["HTRAIN_SEQ"][TEXT]       = '003';
                    foreach ($model->fieldSize as $key => $val) {
                        if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                            list($fieldName, $seq) = explode('_', $key);
                            $data[$fieldName][TEXT] = $data_arr[$i][$key];
                        }
                    }
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '003'")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
                    } else {
                        $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND HTRAIN_SEQ    = '003' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
                    }
                    $db->query($query);
                }
            }

            if ($check_HTRAINREMARK_DETAIL2_DAT_005) {
                //データセット(HTRAINREMARK_DETAIL2_DAT)
                $data = array();
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["HTRAIN_SEQ"][TEXT]       = '005';
                foreach ($model->fieldSize as $key => $val) {
                    if (in_array($key, array("REMARK1_005"))) {
                        list($fieldName, $seq) = explode('_', $key);
                        $data[$fieldName][TEXT] = $data_arr[$i][$key];
                    }
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND HTRAIN_SEQ = '005'")) {
                    $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
                } else {
                    $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "   AND HTRAIN_SEQ    = '005' ";

                    $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
                }
                $db->query($query);
            }
            if ($check_HTRAINREMARK_TRAINREF_DAT) {
                //データセット(HTRAINREMARK_TRAINREF_DAT)
                for ($q = 1; $q <= 6; $q++) {
                    $trainSeq = sprintf("%03d", 100 + $q);
                    $field = "TRAIN_REF".$q;

                    $data = array();
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["TRAIN_SEQ"][TEXT]        = $trainSeq;
                    foreach ($model->fieldSize as $key => $val) {
                        if (in_array($key, array($field))) {
                            $data["REMARK"][TEXT] = $data_arr[$i][$field];
                        }
                    }
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_TRAINREF_DAT WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND TRAIN_SEQ = '".$trainSeq."'")) {
                        $query = Query::insertSQL($data, "HTRAINREMARK_TRAINREF_DAT");
                    } else {
                        $where  = " WHERE YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                        $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "   AND TRAIN_SEQ    = '".$trainSeq."' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_TRAINREF_DAT", $where);
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

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除対象テーブルチェック
        $check_HTRAINREMARK_DETAIL_DAT = false;
        $check_HTRAINREMARK_DETAIL2_DAT = false;
        $check_HTRAINREMARK_DETAIL2_DAT_005 = false;
        $check_HTRAINREMARK_TRAINREF_DAT = false;
        foreach ($model->fieldSize as $key => $val) {
            if (in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                $check_HTRAINREMARK_DETAIL_DAT = true;
            }
            if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                $check_HTRAINREMARK_DETAIL2_DAT = true;
            }
            if (in_array($key, array("REMARK1_005"))) {
                $check_HTRAINREMARK_DETAIL2_DAT_005 = true;
            }
            if (in_array($key, array("TRAIN_REF4", "TRAIN_REF5", "TRAIN_REF6"))) {
                $check_HTRAINREMARK_TRAINREF_DAT = true;
            }
        }
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            $check_HTRAINREMARK_DETAIL_DAT = false;
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //HTRAINREMARK_DAT
            $query  = " DELETE FROM HTRAINREMARK_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "       AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $db->query($query);

            //HTRAINREMARK_DETAIL_DAT
            if ($check_HTRAINREMARK_DETAIL_DAT) {
                $query  = " DELETE FROM HTRAINREMARK_DETAIL_DAT ";
                $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "       AND YEAR = '".$data_arr[$i]["YEAR"]."' ";
                $db->query($query);
            }

            //HTRAINREMARK_DETAIL2_DAT
            if ($check_HTRAINREMARK_DETAIL2_DAT) {
                if ($model->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
                    $data = array();
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    foreach ($model->fieldSize as $key => $val) {
                        if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                            if ("REMARK1_003" == $key) {
                                $data["TOTALSTUDYACT2"][TEXT] = null;
                            } elseif ("REMARK2_003" == $key) {
                                $data["TOTALSTUDYVAL2"][TEXT] = null;
                            }
                        }
                    }
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";
                    if (1 <= $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ")) {
                        $where  = " WHERE ";
                        $where .= "       SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                        $query = Query::updateSQL($data, "HTRAINREMARK_HDAT", $where);
                    }
                    $db->query($query);
                } else {
                    $query  = " DELETE FROM HTRAINREMARK_DETAIL2_DAT ";
                    $query .= " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "   AND YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "   AND HTRAIN_SEQ    = '003' ";
                    $db->query($query);
                }
            }

            //HTRAINREMARK_DETAIL2_DAT
            if ($check_HTRAINREMARK_DETAIL2_DAT_005) {
                $query  = " DELETE FROM HTRAINREMARK_DETAIL2_DAT ";
                $query .= " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "   AND YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "   AND HTRAIN_SEQ    = '005' ";
                $db->query($query);
            }

            //HTRAINREMARK_TRAINREF_DAT
            if ($check_HTRAINREMARK_TRAINREF_DAT) {
                $query  = " DELETE FROM HTRAINREMARK_TRAINREF_DAT ";
                $query .= " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query .= "   AND YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                $query .= "   AND TRAIN_SEQ     BETWEEN '101' AND '106' ";
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
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $field = "";
        foreach ($model->fieldSize as $key => $val) {
            if (in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                $check_HTRAINREMARK_DETAIL_DAT = true;
            }
            if (in_array($key, array("TRAIN_REF4", "TRAIN_REF5", "TRAIN_REF6"))) {
                $check_HTRAINREMARK_TRAINREF_DAT = true;
            }
        }
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            $check_HTRAINREMARK_DETAIL_DAT = false;
        }
        foreach ($model->fieldSize as $key => $val) {
            if ($check_HTRAINREMARK_DETAIL_DAT && in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                $field .= "L1.".$key.",";
            } elseif (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                list($fieldName, $seq) = explode('_', $key);
                if ($model->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
                    if ("REMARK1_003" == $key) {
                        $field .= "L2.TOTALSTUDYACT2 AS ".$fieldName.",";
                    } elseif ("REMARK2_003" == $key) {
                        $field .= "L2.TOTALSTUDYVAL2 AS ".$fieldName.",";
                    }
                } else {
                    $field .= "L2.".$fieldName.",";
                }
            } elseif (in_array($key, array("REMARK1_005"))) {
                if ("REMARK1_005" == $key) {
                    $field .= "L3.REMARK1 AS ".$key.",";
                }
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF1"))) {
                $field .= "L4_1.REMARK AS ".$key.",";
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF2"))) {
                $field .= "L4_2.REMARK AS ".$key.",";
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF3"))) {
                $field .= "L4_3.REMARK AS ".$key.",";
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF4"))) {
                $field .= "L4_4.REMARK AS ".$key.",";
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF5"))) {
                $field .= "L4_5.REMARK AS ".$key.",";
            } elseif ($check_HTRAINREMARK_TRAINREF_DAT && in_array($key, array("TRAIN_REF6"))) {
                $field .= "L4_6.REMARK AS ".$key.",";
            } else {
                $field .= "T3.".$key.",";
            }
        }

        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     ".$field." ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT RH ON RH.YEAR = T1.YEAR ";
        $query .= "     AND RH.SEMESTER = T1.SEMESTER ";
        $query .= "     AND RH.GRADE = T1.GRADE ";
        $query .= "     AND RH.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                         AND T1.SCHREGNO = L1.SCHREGNO ";
        if ($model->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
            $query .= "     LEFT JOIN HTRAINREMARK_HDAT L2 ";
            $query .= "              ON T1.SCHREGNO     = L2.SCHREGNO ";
        } else {
            $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ";
            $query .= "              ON T1.YEAR         = L2.YEAR ";
            $query .= "             AND T1.SCHREGNO     = L2.SCHREGNO ";
            $query .= "             AND L2.HTRAIN_SEQ   = '003' ";
        }
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L3 ";
        $query .= "              ON T1.YEAR         = L3.YEAR ";
        $query .= "             AND T1.SCHREGNO     = L3.SCHREGNO ";
        $query .= "             AND L3.HTRAIN_SEQ   = '005' ";
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_1 ON T1.YEAR = L4_1.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_1.SCHREGNO ";
            $query .= "                                             AND L4_1.TRAIN_SEQ = '101' ";
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_2 ON T1.YEAR = L4_2.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_2.SCHREGNO ";
            $query .= "                                             AND L4_2.TRAIN_SEQ = '102' ";
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_3 ON T1.YEAR = L4_3.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_3.SCHREGNO ";
            $query .= "                                             AND L4_3.TRAIN_SEQ = '103' ";
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_4 ON T1.YEAR = L4_4.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_4.SCHREGNO ";
            $query .= "                                             AND L4_4.TRAIN_SEQ = '104' ";
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_5 ON T1.YEAR = L4_5.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_5.SCHREGNO ";
            $query .= "                                             AND L4_5.TRAIN_SEQ = '105' ";
            $query .= "     LEFT JOIN HTRAINREMARK_TRAINREF_DAT L4_6 ON T1.YEAR = L4_6.YEAR ";
            $query .= "                                             AND T1.SCHREGNO = L4_6.SCHREGNO ";
            $query .= "                                             AND L4_6.TRAIN_SEQ = '106' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        } else {
            $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) {
            $query .= " AND (RH.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "      RH.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "      RH.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "      RH.SUBTR_CD3 = '". STAFFCD ."')";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //項目名取得
    public function getItemName($year)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'A041' AND ";
        $query .= "     NAMECD2 = '01' ";

        return $query;
    }
}
