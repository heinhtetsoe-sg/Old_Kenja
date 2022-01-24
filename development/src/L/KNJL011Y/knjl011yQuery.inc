<?php

require_once('for_php7.php');

class knjl011yQuery extends Query
{

    //MAX受験番号＋１を取得
    public function getMaxExamnoQuery($model)
    {
        //MAX受験番号(初期値)
        $defExamno = knjl011yQuery::getDefExamno($model);
        //入試制度フラグ
        $isJunior       = ($model->field["APPLICANTDIV"] == "1")          ? true : false;
        //入試区分フラグ
        $isJuniorSuisen = ( $isJunior && $model->field["TESTDIV"] == "1") ? true : false;
        $isJuniorTekisei = ($isJunior && $model->field["TESTDIV"] == "5") ? true : false;
        $isHighSuisen   = (!$isJunior && $model->field["TESTDIV"] == "2") ? true : false;
        //帰国生フラグ
        $isHighKikoku   = (!$isJunior && $model->field["INTERVIEW_ATTEND_FLG"] == "1") ? true : false;

        $db = Query::dbCheckOut();
        $query  = " SELECT  VALUE(MAX(INT(T1.EXAMNO)), {$defExamno}) + 1 AS EXAMNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT T1 ";
        if ($isHighSuisen) {
            $query .= "     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W1 ";
            $query .= "         ON  W1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "         AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND W1.TESTDIV      = T1.TESTDIV ";
            $query .= "         AND W1.EXAMNO       = T1.EXAMNO ";
        }
        $query .= " WHERE   T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND   T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND   T1.EXAMNO NOT LIKE '6%' "; //内部生は除く
        if ($isHighKikoku) {
            $query .= "   AND   VALUE(T1.INTERVIEW_ATTEND_FLG, '0')  = '1' "; //帰国生のみ
        } else {
            $query .= "   AND   VALUE(T1.INTERVIEW_ATTEND_FLG, '0') != '1' "; //帰国生除く
        }
        if ($isJunior) {
            if ($isJuniorSuisen) {
                $query .= "   AND   T1.TESTDIV = '1' ";
            } elseif ($isJuniorTekisei) {
                $query .= "   AND   T1.TESTDIV = '5' ";
            } else {
                $query .= "   AND   T1.TESTDIV NOT IN ('1','5') ";
            }
        } else {
            $query .= "   AND   T1.TESTDIV   = '{$model->field["TESTDIV"]}' ";
            $query .= "   AND   T1.DESIREDIV = '{$model->field["DESIREDIV"]}' ";
            if ($isHighSuisen) {
                $query .= "   AND   W1.RECOM_KIND = '{$model->field["RECOM_KIND"]}' ";
            }
        }
        $max_examno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_examno;
    }

    //MAX受験番号(初期値)を取得
    public function getDefExamno($model)
    {
        //入試制度フラグ
        $isJunior        = ($model->field["APPLICANTDIV"] == "1")                ? true : false;
        //入試区分フラグ
        $isJuniorSuisen  = ( $isJunior    && $model->field["TESTDIV"] == "1")    ? true : false;
        $isJuniorTekisei = ( $isJunior    && $model->field["TESTDIV"] == "5")    ? true : false;
        $isHighGakutoku  = (!$isJunior    && $model->field["TESTDIV"] == "1")    ? true : false;
        $isHighSuisen    = (!$isJunior    && $model->field["TESTDIV"] == "2")    ? true : false;
        //志望区分フラグ
        $isHighFuTokusin = (!$isJunior    && $model->field["DESIREDIV"] == "1")  ? true : false;
        $isHighFuSingaku = (!$isJunior    && $model->field["DESIREDIV"] == "2")  ? true : false;
        $isHighEiTokusin = (!$isJunior    && $model->field["DESIREDIV"] == "3")  ? true : false;
        //推薦区分フラグ
        $isHighGakuryoku = ($isHighSuisen && $model->field["RECOM_KIND"] == "1") ? true : false;
        $isHighSports    = ($isHighSuisen && $model->field["RECOM_KIND"] == "2") ? true : false;
        //帰国生フラグ
        $isHighKikoku    = (!$isJunior && $model->field["INTERVIEW_ATTEND_FLG"] == "1") ? true : false;

        //MAX受験番号(初期値)
        $defExamno = 0;

        if ($isHighKikoku) {
            if ($isHighGakutoku) {
                if ($isHighFuTokusin) {
                    $defExamno = 82100;
                } elseif ($isHighFuSingaku) {
                    $defExamno = 82300;
                } elseif ($isHighEiTokusin) {
                    $defExamno = 82200;
                } else {
                    $defExamno = 82400;
                }
            } elseif ($isHighSuisen) {
                if ($isHighFuTokusin) {
                    $defExamno = 83100;
                } elseif ($isHighFuSingaku) {
                    $defExamno = 83300;
                } elseif ($isHighEiTokusin) {
                    $defExamno = 83200;
                } else {
                    $defExamno = 83400;
                }
            } else {
                if ($isHighFuTokusin) {
                    $defExamno = 81100;
                } elseif ($isHighFuSingaku) {
                    $defExamno = 81300;
                } elseif ($isHighEiTokusin) {
                    $defExamno = 81200;
                } else {
                    $defExamno = 81400;
                }
            }
        } else {
            if ($isJunior) {
                if ($isJuniorSuisen) {
                    $defExamno = 71000; //推薦
                } elseif ($isJuniorTekisei) {
                    $defExamno = 73000; //適性
                } else {
                    $defExamno = 72000; //一般
                }
            } else {
                if ($isHighGakutoku) {
                    if ($isHighFuTokusin) {
                        $defExamno = 30000;
                    } elseif ($isHighFuSingaku) {
                        $defExamno = 35000;
                    } elseif ($isHighEiTokusin) {
                        $defExamno = 40000;
                    } else {
                        $defExamno = 45000;
                    }
                } elseif ($isHighSuisen) {
                    if ($isHighFuSingaku) {
                        if ($isHighGakuryoku) {
                            $defExamno = 51000;
                        } else {
                            $defExamno = 53000;
                        }
                    } elseif ($isHighFuTokusin) {
                        if ($isHighGakuryoku) {
                            $defExamno = 50000;
                        } else {
                        }
                    } elseif ($isHighEiTokusin) {
                        if ($isHighGakuryoku) {
                            $defExamno = 52000; //未確定のため、ダミー52XXXとしておく
                        } else {
                        }
                    } else {
                        if ($isHighGakuryoku) {
                            $defExamno = 54000;
                        } elseif ($isHighSports) {
                            $defExamno = 56000;
                        } else {
                            $defExamno = 58000;
                        }
                    }
                } else {
                    if ($isHighFuSingaku) {
                        $defExamno = 10000;
                    } elseif ($isHighFuTokusin) {
                        $defExamno = 11000;
                    } elseif ($isHighEiTokusin) {
                        $defExamno = 21000;
                    } else {
                        $defExamno = 20000;
                    }
                }
            }
        }
        return $defExamno;
    }

    //最小受験番号
    public function getMinExam($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$applicantDiv."' ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT  T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolcd($year, $finschoolcd)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         N1.NAME1 AS CHIKU_NAME, N2.NAME1 AS RITSU_NAME ";
        $query .= "   FROM  FINSCHOOL_YDAT T2, ";
        $query .= "         FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z003' AND N1.NAMECD2=T1.DISTRICTCD ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L001' AND N2.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T2.YEAR = '$year' ";
        $query .= "    AND  T2.FINSCHOOLCD = '$finschoolcd' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //高校マスタより併願校データを取得
    public function getFinHighschoolcd($finHighSchoolcd)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, ";
        $query .= "         N1.NAME1 AS CHIKU_NAME, N2.NAME1 AS RITSU_NAME ";
        $query .= "   FROM  FINHIGHSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z003' AND N1.NAMECD2=T1.DISTRICTCD ";
        $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L001' AND N2.NAMECD2=T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '$finHighSchoolcd' ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //名称マスタよりデータを取得
    public function get_name_cd($year, $cd, $cd2 = "")
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1, ";
        $query .= "      NAME2, ";
        $query .= "      ABBV1, ";
        $query .= "      NAMESPARE1, ";
        $query .= "      NAMESPARE2 ";
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$year."' ";
        $query .= "      AND NAMECD1 = '".$cd."' ";
        if ($cd2) {
            $query .= "      AND NAMECD2 = '".$cd2."' ";
        }
        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function get_calendarno($year)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }

    //志望区分
    public function getDesirediv($year, $applicantDiv, $testDiv, $desireDiv, $wishno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     L1.EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testDiv}' ";
        if (0 < strlen($desireDiv)) {
            $query .= " AND T1.DESIREDIV    = '{$desireDiv}' ";
        }
        $query .= "     AND T1.WISHNO       = '{$wishno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV ";
        return $query;
    }

    //受付データ不合格カウント取得（３回受験は出来ない）
    public function getReceptDatCount($model, $jadgeDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1 = 'L013' ";
        $query .= "                              AND L013.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND T1.EXAMNO       = '{$model->examno}' ";
        if ($jadgeDiv == '1') {
            $query .= "     AND L013.NAMESPARE1 = '1' ";
        } else {
            $query .= "     AND T1.JUDGEDIV     = '2' "; // 不合格
        }

        return $query;
    }

    //メインデータ取得
    public function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT 
                        T1.APPLICANTDIV,
                        T1.TESTDIV,
                        T1.DESIREDIV,
                        T1.SHDIV,
                        T1.SH_SCHOOLCD,
                        W1.RECOM_KIND,
                        W2.DESIREDIV as GENERAL_DESIREDIV,
                        W2.SHDIV     as GENERAL_SHDIV,
                        W3.DESIREDIV as GENERAL_DESIREDIV2,
                        W3.SHDIV     as GENERAL_SHDIV2,
                        W4.DESIREDIV as GENERAL_DESIREDIV3,
                        W4.SHDIV     as GENERAL_SHDIV3,
                        value(R1.JUDGEDIV,'2') as JUDGEDIV,
                        T1.EXAMNO,
                        T1.NAME,
                        T1.NAME_KANA,
                        T1.SEX,
                        T1.ERACD,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.BIRTHDAY,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T1.FS_ERACD,
                        N1.NAME1 as FS_WNAME,
                        T1.FS_Y,
                        T1.FS_M,
                        T1.FS_GRDYEAR,
                        T1.FS_GRDDIV,
                        T1.REMARK1,
                        T1.REMARK2,
                        T1.SELECT_SUBCLASS_DIV,
                        T1.GENERAL_FLG,
                        T1.GENERAL_FLG2,
                        T1.GENERAL_FLG3,
                        T1.SLIDE_FLG,
                        T1.INTERVIEW_ATTEND_FLG,
                        T1.SHIFT_DESIRE_FLG,
                        T1.SPORTS_FLG,
                        T1.DORMITORY_FLG,
                        T1.RECOM_ITEM1,
                        T1.RECOM_ITEM2,
                        T1.RECOM_ITEM3,
                        T1.RECOM_ITEM4,
                        T2.ZIPCD,
                        T2.ADDRESS1,
                        T2.ADDRESS2,
                        T2.TELNO,
                        T2.GNAME,
                        T2.GKANA,
                        T2.GZIPCD,
                        T2.GADDRESS1,
                        T2.GADDRESS2,
                        T2.GTELNO,
                        T2.RELATIONSHIP,
                        T3.NAME1,
                        T4.CONFIDENTIAL_RPT01,T4.CONFIDENTIAL_RPT02,T4.CONFIDENTIAL_RPT03,T4.CONFIDENTIAL_RPT04,T4.CONFIDENTIAL_RPT05,T4.CONFIDENTIAL_RPT06,
                        T4.CONFIDENTIAL_RPT07,T4.CONFIDENTIAL_RPT08,T4.CONFIDENTIAL_RPT09,T4.CONFIDENTIAL_RPT10,T4.CONFIDENTIAL_RPT11,T4.CONFIDENTIAL_RPT12,
                        T4.TOTAL3,
                        T4.TOTAL5,
                        T4.TOTAL_ALL AS TOTAL9,
                        smallint(T4.AVERAGE_ALL) as AVERAGE_ALL,
                        T4.AVERAGE5,
                        T4.ABSENCE_DAYS AS ABSENCE_DAYS1,
                        T4.ABSENCE_DAYS2,
                        W5_005.REMARK1 AS SPORTS_FLG2,
                        T4.ABSENCE_DAYS3
                    FROM 
                        ENTEXAM_APPLICANTBASE_DAT T1 
                        LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.EXAMNO = T2.EXAMNO 
                        LEFT JOIN V_NAME_MST T3 ON T1.ENTEXAMYEAR = T3.YEAR AND T1.ERACD = T3.NAMECD2 AND T3.NAMECD1 = 'L007' 
                        LEFT JOIN V_NAME_MST N1 ON T1.ENTEXAMYEAR = N1.YEAR AND T1.FS_ERACD = N1.NAMECD2 AND N1.NAMECD1 = 'L007' 
                        LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND T1.EXAMNO = T4.EXAMNO 
                        LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W1 ON W1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W1.APPLICANTDIV = T1.APPLICANTDIV AND W1.TESTDIV = T1.TESTDIV AND W1.EXAMNO = T1.EXAMNO 
                        LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W2 ON W2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W2.APPLICANTDIV = T1.APPLICANTDIV AND W2.TESTDIV <> T1.TESTDIV AND W2.TESTDIV = '{$model->general_testdiv}' AND W2.EXAMNO = T1.EXAMNO 
                        LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W3 ON W3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W3.APPLICANTDIV = T1.APPLICANTDIV AND W3.TESTDIV <> T1.TESTDIV AND W3.TESTDIV = '{$model->general_testdiv2}' AND W3.EXAMNO = T1.EXAMNO 
                        LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W4 ON W4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W4.APPLICANTDIV = T1.APPLICANTDIV AND W4.TESTDIV <> T1.TESTDIV AND W4.TESTDIV = '{$model->general_testdiv3}' AND W4.EXAMNO = T1.EXAMNO 
                        LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R1.APPLICANTDIV = T1.APPLICANTDIV AND R1.TESTDIV = T1.TESTDIV AND R1.EXAMNO = T1.EXAMNO 
                        LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT W5_005 ON W5_005.ENTEXAMYEAR = T1.ENTEXAMYEAR AND W5_005.EXAMNO = T1.EXAMNO AND W5_005.SEQ = '005' 
                    WHERE 
                        T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データ重複チェック
    public function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                       T1.EXAMNO,
                       T2.NAME1
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        V_NAME_MST T2
                    ON
                        T1.ENTEXAMYEAR = T2.YEAR AND
                        T1.APPLICANTDIV = T2.NAMECD2 AND
                        T2.NAMECD1 = 'L003'
                    WHERE
                        T1.ENTEXAMYEAR = '".$model->year."' AND
                        T1.EXAMNO = '".$model->examno."' AND
                        T1.APPLICANTDIV <> '".$model->applicantdiv."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    public function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //該当日付取得
    public function CheckDate($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L007' AND
                        NAMECD2 = '".$model->field["ERACD"]. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    public function getCountDetailDat($model, $seq)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND EXAMNO = '".$model->examno."' AND SEQ = '".$seq."' ";
        return $query;
    }

    //--- INSERT
    //--- UPDATE
    public function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl011yQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //生年月日の変換(西暦→和暦)
        $w_eracd = $w_year = $mon = $day = "";
        if (strlen($model->field["BIRTHDAY"])) {
            list($s_year, $mon, $day) = explode("/", $model->field["BIRTHDAY"]);
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($defsdate[$i] <= $model->field["BIRTHDAY"] && $model->field["BIRTHDAY"] <= $defedate[$i]) {
                    $w_eracd = $eracd[$i];
                    $w_year = ((int)$s_year + 1) - ((int)$defyear[$i]);
                    $w_year = sprintf("%02d", $w_year);
                    break;
                }
            }
        }

        //卒業日の変換(西暦→和暦)
        $fs_eracd = $fs_y = $fs_date = "";
        if (strlen($model->field["FS_GRDYEAR"]) && strlen($model->field["FS_M"])) {
            $fs_date = sprintf("%04d", $model->field["FS_GRDYEAR"])."/".sprintf("%02d", $model->field["FS_M"])."/01";
            list($fs_year, $fs_mon, $fs_day) = explode("/", $fs_date);
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($defsdate[$i] <= $fs_date && $fs_date <= $defedate[$i]) {
                    $fs_eracd = $eracd[$i];
                    $fs_y = ((int)$fs_year + 1) - ((int)$defyear[$i]);
                    $fs_y = sprintf("%02d", $fs_y);
                    break;
                }
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["DESIREDIV"][TEXT]     = $model->field["DESIREDIV"];

        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["SH_SCHOOLCD"][TEXT]   = ($model->field["SHDIV"] == "2") ? $model->field["SH_SCHOOLCD"] : "";

        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["DORMITORY_FLG"][TEXT] = $model->field["DORMITORY_FLG"];

        $data["ERACD"][TEXT]         = $w_eracd;
        $data["BIRTH_Y"][TEXT]       = $w_year;
        $data["BIRTH_M"][TEXT]       = $mon;
        $data["BIRTH_D"][TEXT]       = $day;
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]       = $model->field["FS_NAME"];
        $data["FS_ERACD"][TEXT]      = $fs_eracd;
        $data["FS_Y"][TEXT]          = $fs_y;
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d", $model->field["FS_M"]) : "";
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"]? sprintf("%04d", $model->field["FS_GRDYEAR"]) : "";
        $data["FS_DAY"][TEXT]        = str_replace("/", "-", $fs_date);
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];

        $data["SELECT_SUBCLASS_DIV"][TEXT] = (0 < strlen($model->field["GENERAL_FLG"])) ? $model->field["SELECT_SUBCLASS_DIV"] : ""; //特進チャレンジ
        $data["GENERAL_FLG"][TEXT]   = $model->field["GENERAL_FLG"];
        $data["GENERAL_FLG2"][TEXT]  = $model->field["GENERAL_FLG2"];
        $data["GENERAL_FLG3"][TEXT]  = $model->field["GENERAL_FLG3"];
        $data["SLIDE_FLG"][TEXT]     = $model->field["SLIDE_FLG"];
        $data["SHIFT_DESIRE_FLG"][TEXT] = $model->field["SHIFT_DESIRE_FLG"]; //特別判定
        $data["SPORTS_FLG"][TEXT]    = $model->field["SPORTS_FLG"];
        $data["INTERVIEW_ATTEND_FLG"][TEXT] = $model->field["INTERVIEW_ATTEND_FLG"]; //帰国生

        $data["RECOM_ITEM1"][TEXT]   = $model->field["RECOM_ITEM1"];
        $data["RECOM_ITEM2"][TEXT]   = $model->field["RECOM_ITEM2"];
        $data["RECOM_ITEM3"][TEXT]   = $model->field["RECOM_ITEM3"];
        $data["RECOM_ITEM4"][TEXT]   = $model->field["RECOM_ITEM4"];

        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        if ($this->cmd != "add") {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        if (0 == $db->getOne($query)) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."' ";
            $query  = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            $db->query($query);
        }

        //志望データ
        $query  = " DELETE FROM ENTEXAM_APPLICANTDESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        $db->query($query);
        //基礎と同じレコードを作成
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["DESIREDIV"][TEXT]    = $model->field["DESIREDIV"];
        $data["RECOM_KIND"][TEXT]   = $model->field["RECOM_KIND"];
        $data["SHDIV"][TEXT]        = $model->field["SHDIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
        $db->query($query);
        //一般入試希望のレコードを作成
        if (0 < strlen($model->field["GENERAL_FLG"])) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->general_testdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["DESIREDIV"][TEXT]    = $model->field["GENERAL_DESIREDIV"];
            $data["RECOM_KIND"][TEXT]   = "";
            $data["SHDIV"][TEXT]        = $model->field["GENERAL_SHDIV"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
            $db->query($query);
        }
        //一般入試希望のレコードを作成
        if (0 < strlen($model->field["GENERAL_FLG2"])) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->general_testdiv2;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["DESIREDIV"][TEXT]    = $model->field["GENERAL_DESIREDIV2"];
            $data["RECOM_KIND"][TEXT]   = "";
            $data["SHDIV"][TEXT]        = $model->field["GENERAL_SHDIV2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
            $db->query($query);
        }
        //適性検査型希望のレコードを作成
        if (0 < strlen($model->field["GENERAL_FLG3"])) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $model->general_testdiv3;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["DESIREDIV"][TEXT]    = $model->field["GENERAL_DESIREDIV3"];
            $data["RECOM_KIND"][TEXT]   = "";
            $data["SHDIV"][TEXT]        = $model->field["GENERAL_SHDIV3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");
            $db->query($query);
        }

        //T特奨
        $cnt = $db->getOne(knjl011yQuery::getCountDetailDat($model, "005"));
        $data = array();
        $data["REMARK1"][TEXT]    = $model->field["SPORTS_FLG2"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        if ($cnt > 0) {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."' ";
            $where .= "       AND SEQ     = '005' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "005";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTACTIVITY_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTACTIVITY_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTDESIRE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTDESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_INTERVIEW_DAT
        $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."' AND";
        $query .= "       SEQ         = '005'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //氏名を取得
    public function getExamName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
        $query .= "     T1.EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' ";
        return $query;
    }

    //欠席フラグを取得
    public function getAttendFlg($model)
    {
        $namecd1 = ($model->attend["ATTEND_APPLICANTDIV"] == "2") ? "L004" : "L024";
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV || ':' || L1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T1.JUDGEDIV as ATTEND_FLG, ";
        $query .= "     T1.HONORDIV as ATTEND_REASON, ";
        $query .= "     L2.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '{$namecd1}' AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L013' AND L2.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
        $query .= "     T1.EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' AND ";
        $query .= "     value(L2.NAMESPARE1,'0') = '0' "; //合格は表示しない
        $query .= " ORDER BY ";
        $query .= "     T1.TESTDIV ";
        return $query;
    }

    //更新処理(欠席フラグ)
    public function &getAttendUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $arrAttendFlg = array();
        $query = knjl011yQuery::getAttendFlg($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrAttendFlg[$row["TESTDIV"]] = $row["NAMESPARE1"];
        }
        $result->free();

        if (0 < get_count($arrAttendFlg)) {
            foreach ($arrAttendFlg as $testdiv => $namespare1) {
                //合格の場合、上書きしない
                if ($namespare1 == "1") {
                    continue;
                }
                //項目
                $name = "ATTEND_FLG" .$testdiv;
                $nameReason = "ATTEND_REASON" .$testdiv;
                $data = array();
                $data["JUDGEDIV"][TEXT]     = $model->attend[$name];
                $data["HONORDIV"][TEXT]     = $model->attend[$nameReason];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
                $where .= "     TESTDIV         = '{$testdiv}' AND ";
                $where .= "     EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' ";
                $where .= " AND value(JUDGEDIV,'4') = '4' ";//合否区分が「欠席」「ブランク」のみ更新
                //UPDATE
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
                //項目
                $data = array();
                $data["JUDGEMENT"][TEXT]    = $model->attend[$name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
                $where .= "     EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' AND ";
                $where .= "     value(JUDGEMENT,'0') NOT IN ('1','3','5') ";//合格の場合、上書きしない
                $where .= " AND value(JUDGEMENT,'4') = '4' ";//合否区分が「欠席」「ブランク」のみ更新
                //UPDATE
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}