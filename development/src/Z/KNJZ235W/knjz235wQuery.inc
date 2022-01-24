<?php

require_once('for_php7.php');
class knjz235wQuery extends Query
{

    //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
    public function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        return $query;
    }

    //校種を取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV3 ";

        return $query;
    }

    //学期情報を取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";

        return $query;
    }
    
    //学期情報を取得
    public function getSemesterAll()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= "   , SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //最終学期情報を取得
    public function getSemesterMax()
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";

        return $query;
    }

    //パターン取得
    public function getPatern()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMESPARE3 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //主に使用する様式
    public function getPatternUseMainly($namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= "     , NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMECD2 IN (".$namecd2.") ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //主に使用する様式(pattern D429LM)
    public function getPatternUseMainlyLM($model, $div)
    {
        //宮城仕様 NAMECD2規則
        //知的 => 100番台連番
        //準ずる教育 => 200番台連番 + 校種
        $from = ($model->field2["TYOUHYOU_PATTERN"] == "1") ? "100" : "200";
        $to   = ($model->field2["TYOUHYOU_PATTERN"] == "1") ? "199" : "299";

        $query  = "  WITH NMST_A035 AS ( ";
        $query .= "    SELECT ";
        $query .= "      NAMECD1, ";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1, ";
        $query .= "      SUBSTRING(NAMECD2, 1, 3) AS SEQ, ";
        $query .= "      SUBSTRING(NAMECD2, 4, 1) AS SCHKIND ";
        $query .= "    FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND NAMECD1 = 'A035' ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        if ($div == "cnt") {
            $query .= "    COUNT(*) ";
        } else {
            $query .= "    T1.NAMECD2 AS VALUE, ";
            $query .= "    T1.NAME1 AS LABEL, ";
            $query .= "    SEQ, ";
            $query .= "    SCHKIND, ";
            $query .= "    ROW_NUMBER() OVER() AS RADIO_VALUE ";
        }
        $query .= "  FROM ";
        $query .= "    NMST_A035 T1 ";
        $query .= "    LEFT JOIN NAME_MST T2 ";
        $query .= "      ON T2.NAMECD1 = T1.NAMECD1 ";
        $query .= "      AND T2.NAMECD2 = T1.SEQ ";
        $query .= "  WHERE ";
        $query .= "    T1.NAMECD1 = 'A035' ";
        $query .= "    AND T1.SEQ BETWEEN '{$from}' AND '{$to}' ";
        if ($div != "cnt") {
            $query .= "  ORDER BY ";
            $query .= "    T1.SEQ, ";
            $query .= "    CASE WHEN T2.ABBV3 IS NOT NULL THEN T2.ABBV3 ELSE '0' END ";
        }

        return $query;
    }

    //主に使用する様式(pattern D429LM) のNAMECD2からラジオボタンのVALUEを取得
    public function getPatternUseMainlyLMRadioVal($db, $model, $namecd2)
    {
        $query = knjz235wQuery::getPatternUseMainlyLM($model, "data");
        $result = $db->query($query);
        $radioVal = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($namecd2 != $row["VALUE"]) {
                continue;
            }
            $radioVal = $row["RADIO_VALUE"];
        }

        return $radioVal;
    }

    //データ取得
    public function getHreportConditionDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->schoolKind != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
        $query .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
        $query .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
        $query .= "     COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) ";

        return $query;
    }

    //データ有無チェック
    public function checkExistData($model, $year, $seq, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "pre_data") {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' ";
        if ($model->schoolKind != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($flg == "") {
            $query .= " AND GRADE       = '".sprintf("%02d", "")."' ";
            $query .= " AND COURSECD    = '".sprintf("%01d", "")."' ";
            $query .= " AND MAJORCD     = '".sprintf("%03d", "")."' ";
            $query .= " AND COURSECODE  = '".sprintf("%04d", "")."' ";
            if ($model->pattern == 'D429') {
                if ($model->field2["STATUS"] == "1") {
                    $seqD429 = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429 = sprintf("%03d", intval($seq) + 200);
                }
                $query .= " AND SEQ         = '".$seqD429."' ";
            } elseif ($model->pattern == 'D429BD') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429BD = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429BD = sprintf("%03d", intval($seq) + 200);
                }
                $query .= " AND SEQ         = '".$seqD429BD."' ";
            } elseif ($model->pattern == 'D429LM') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429LM = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429LM = sprintf("%03d", intval($seq) + 200);
                }
                $query .= " AND SEQ         = '".$seqD429LM."' ";
            } else {
                $query .= " AND SEQ         = '".$seq."' ";
            }
        }

        return $query;
    }

    //月取得
    public function getMedexamDerMonthDat()
    {
        $query  = " SELECT ";
        $query .= "     M1.SEMESTER || M1.MONTH as VALUE, ";
        $query .= "     S1.SEMESTERNAME || '：' || M1.MONTH || '月' as LABEL ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_MONTH_DAT M1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR     = M1.YEAR ";
        $query .= "                              AND S1.SEMESTER = M1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     M1.YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     M1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     M1.MONTH ";
        $query .= " ORDER BY ";
        $query .= "     M1.SEMESTER, ";
        $query .= "     case when int(M1.MONTH) < 4 then int(M1.MONTH) + 12 ";
        $query .= "          else int(M1.MONTH) ";
        $query .= "     end ";

        return $query;
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 1; $i <= 22; $i++) {
            $seq = sprintf("%03d", $i);

            //存在チェック
            $query = knjz235wQuery::checkExistData($model, CTRL_YEAR, $seq);
            $flg = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新
            $db->query(knjz235wQuery::updateHreportConditionDat($model, $seq, $flg));
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //更新 -- HREPORT_CONDITION_DAT
    public function updateHreportConditionDat($model, $seq, $flg)
    {
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            if ($model->schoolKind != "") {
                $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }
            $data["GRADE"][TEXT]        = sprintf("%02d", "");
            $data["COURSECD"][TEXT]     = sprintf("%01d", "");
            $data["MAJORCD"][TEXT]      = sprintf("%03d", "");
            $data["COURSECODE"][TEXT]   = sprintf("%04d", "");

            if ($model->pattern == 'D429') {
                if ($model->field2["STATUS"] == "1") {
                    $seqD429 = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429 = sprintf("%03d", intval($seq) + 200);
                }
                $data["SEQ"][TEXT]          = $seqD429;
            } elseif ($model->pattern == 'D429BD') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429BD = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429BD = sprintf("%03d", intval($seq) + 200);
                }
                $data["SEQ"][TEXT]          = $seqD429BD;
            } elseif ($model->pattern == 'D429LM') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429LM = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429LM = sprintf("%03d", intval($seq) + 200);
                }
                $data["SEQ"][TEXT]          = $seqD429LM;
            } else {
                $data["SEQ"][TEXT]          = $seq;
            }
        }
        if ($model->pattern == 'D429LM') {
            if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                if ($seq == "001" || $seq == "013") {
                    $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
                } elseif ($seq == "002" || $seq == "003" || $seq == "004" || $seq == "008" || $seq == "009" || $seq == "012") {
                    $data["REMARK1"][TEXT] = $model->field["SEQ".$seq];
                }
            } else {
                if ($seq == "001" || $seq == "013") {
                    $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
                } elseif ($seq == "002" || $seq == "003" || $seq == "004" || $seq == "008" || $seq == "012") {
                    $data["REMARK1"][TEXT] = $model->field["SEQ".$seq];
                }
            }
        } elseif ($model->pattern == 'D429BD') {
            if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                if ($seq == "001") {
                    $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
                } elseif ($seq == "002" || $seq == "004" || $seq == "011") {
                    $data["REMARK1"][TEXT] = $model->field["SEQ".$seq];
                }
            } else {
                if ($seq == "001") {
                    $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
                } elseif ($seq == "002" || $seq == "004") {
                    $data["REMARK1"][TEXT] = $model->field["SEQ".$seq];
                } elseif ($seq == "010") {
                    $data["REMARK1"][TEXT]  = ($model->field["SEQ002"] == "205") ? "" : $model->field["SEQ".$seq."_1"];
                    $data["REMARK10"][TEXT] = $model->field["SEQ".$seq."_2"];
                } elseif ($seq == "008" || $seq == "011") {
                    $data["REMARK1"][TEXT] = ($model->field["SEQ002"] == "205") ? "1" : $model->field["SEQ".$seq];
                }
            }
        } elseif ($model->pattern == 'D429') {
            if ($seq == "001" || $seq == "003") {
                $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
            } elseif ($seq == "002") {
                if ($model->field2["STATUS"] == "2") {
                    $data["REMARK1"][TEXT]  = $model->frmPatern;
                }
                $data["REMARK10"][TEXT] = $model->field["SEQ".$seq];
            } elseif ($seq == "005") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."_1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."_2"];
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        } elseif ($model->pattern == 'D185E') {
            if ($seq == "002" || $seq == "006") {
                for ($i=0; $i < get_count($model->semesterList); $i++) {
                    $semester = $model->semesterList[$i]["SEMESTER"];
                    if ($seq == "002" && $semester == $model->semesterMax) {
                        continue;
                    }
                    $data["REMARK".$semester][TEXT]  = $model->field["SEQ".$seq.$semester];
                }
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        } elseif ($model->pattern == 'D181H' || $model->pattern == 'D181A') {
            if ($seq == "004") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        } elseif ($model->pattern == 'D186V') {
            if ($seq == "011") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        } else {
            if ($seq == "006") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
                $data["REMARK3"][TEXT]  = $model->field["SEQ".$seq."3"];
                $data["REMARK4"][TEXT]  = $model->field["SEQ".$seq."4"];
            } elseif ($seq == "015") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "HREPORT_CONDITION_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            if ($model->schoolKind != "") {
                $where .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $where .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $where .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $where .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
            $where .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
            $where .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
            $where .= "     COURSECODE  = '".sprintf("%04d", "")."' AND ";

            if ($model->pattern == 'D429LM') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429LM = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429LM = sprintf("%03d", intval($seq) + 200);
                }
                $where .= "     SEQ         = '".$seqD429LM."' ";
            } elseif ($model->pattern == 'D429BD') {
                if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
                    $seqD429BD = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429BD = sprintf("%03d", intval($seq) + 200);
                }
                $where .= "     SEQ         = '".$seqD429BD."' ";
            } elseif ($model->pattern == 'D429') {
                if ($model->field2["STATUS"] == "1") {
                    $seqD429 = sprintf("%03d", intval($seq) + 100);
                } else {
                    $seqD429 = sprintf("%03d", intval($seq) + 200);
                }
                $where .= "     SEQ         = '".$seqD429."' ";
            } else {
                $where .= "     SEQ         = '".$seq."' ";
            }

            $query = Query::updateSQL($data, "HREPORT_CONDITION_DAT", $where);
        }

        return $query;
    }

    //前年度のデータを今年度にINSERT
    public function &copyInsert($db, $model, $Row)
    {
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".$Row["SCHOOLCD"]."' AND ";
            $query .= "     SCHOOL_KIND = '".$Row["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     GRADE       = '".$Row["GRADE"]."' AND ";
        $query .= "     COURSECD    = '".$Row["COURSECD"]."' AND ";
        $query .= "     MAJORCD     = '".$Row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE  = '".$Row["COURSECODE"]."' AND ";
        $query .= "     SEQ         = '".$Row["SEQ"]."' ";
        $db->query($query);

        //追加
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = $Row["SCHOOLCD"];
            $data["SCHOOL_KIND"][TEXT]  = $Row["SCHOOL_KIND"];
        }
        $data["GRADE"][TEXT]        = $Row["GRADE"];
        $data["COURSECD"][TEXT]     = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]      = $Row["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $Row["COURSECODE"];
        $data["SEQ"][TEXT]          = $Row["SEQ"];
        $data["REMARK1"][TEXT]      = $Row["REMARK1"];
        $data["REMARK2"][TEXT]      = $Row["REMARK2"];
        $data["REMARK3"][TEXT]      = $Row["REMARK3"];
        $data["REMARK4"][TEXT]      = $Row["REMARK4"];
        $data["REMARK5"][TEXT]      = $Row["REMARK5"];
        $data["REMARK6"][TEXT]      = $Row["REMARK6"];
        $data["REMARK7"][TEXT]      = $Row["REMARK7"];
        $data["REMARK8"][TEXT]      = $Row["REMARK8"];
        $data["REMARK9"][TEXT]      = $Row["REMARK9"];
        $data["REMARK10"][TEXT]     = $Row["REMARK10"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_CONDITION_DAT");
        $db->query($query);

        $db->commit();
    }
}
