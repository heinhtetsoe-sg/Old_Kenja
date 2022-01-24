<?php

require_once('for_php7.php');

class knjh438Query extends Query
{

    //年度
    function getYear($div = "MOCKYEAR")
    {
        if ($div == "MOCKYEAR") {
            $query .= " SELECT DISTINCT ";
            $query .= "     SUBSTR(MOCKCD,2,4) AS LABEL, ";
            $query .= "     SUBSTR(MOCKCD,2,4) AS VALUE ";
            $query .= " FROM ";
            $query .= "     MOCK_MST ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     YEAR AS LABEL, ";
            $query .= "     YEAR AS VALUE ";
            $query .= " FROM ";
            $query .= "     MOCK_CSV_SCORE_FIELD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = SUBSTR(MOCKCD,2,4) ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //模試データ
    function getMockcd($model)
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD,2,4) = '".$model->mockyear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //業者コード
    function getCompanyCd($mockCd)
    {
        $query  = " SELECT ";
        $query .= "     COMPANYCD ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '{$mockCd}' ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query .= " SELECT ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     GRADE_NAME2 ";
        } else {
            $query .= "     GRADE_NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        $query .= " AND  ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     GRADE_CD = '".$model->field["GRADE"]."'  ";
        } else {
            $query .= "     GRADE = '".$model->field["GRADE"]."'  ";
        }

        return $query;
    }

    //模試科目
    function getMockSubclassMst()
    {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     MOCK_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試科目名(ベネッセ)
    function getMockSubclassTitleBene($model, $seq)
    {
        $query .= " SELECT ";
        $query .= "     T2.TSCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_BENE_SCORE_HEAD_HDAT T1, ";
        $query .= "     MOCK_CSV_BENE_SCORE_HEAD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.KYOUZAICD = T2.KYOUZAICD ";
        $query .= "     AND T2.SEQ = '{$seq}' ";

        return $query;
    }

    //模試科目名(駿台)
    function getMockSubclassTitleSundai($model, $seq)
    {
        $query .= " SELECT ";
        $query .= "     T2.TSCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SUNDAI_HEAD_HDAT T1, ";
        $query .= "     MOCK_CSV_SUNDAI_SCORE_HEAD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.MOSI_CD = T2.MOSI_CD ";
        $query .= "     AND T2.SEQ = '{$seq}' ";

        return $query;
    }

    //模試科目名(Z会)
    function getMockSubclassTitleZkai($model, $seq)
    {
        $query .= " SELECT ";
        $query .= "     T2.TSCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_ZKAI_HEAD_HDAT T1, ";
        $query .= "     MOCK_CSV_ZKAI_SCORE_HEAD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.MOSI_CD = T2.MOSI_CD ";
        $query .= "     AND T2.SEQ = '{$seq}' ";

        return $query;
    }

    //模試科目名(河合)
    function getMockSubclassTitleKawai($model, $seq)
    {
        $query .= " SELECT ";
        $query .= "     T2.TSCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_KAWAI_HEAD_HDAT T1, ";
        $query .= "     MOCK_CSV_KAWAI_SCORE_HEAD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.MOSI_CD = T2.MOSI_CD ";
        $query .= "     AND T2.SEQ = '".sprintf("%03d", $seq)."' ";

        return $query;
    }

    //模試科目コード取得
    function getMockSubclassCd($year, $mockcd, $grade, $field_cnt)
    {
        $query  = "SELECT MOCK_SUBCLASS_CD ";
        $query .= "  FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$year."' ";
        $query .= "   AND MOCKCD    = '".$mockcd."' ";
        $query .= "   AND GRADE     = '".$grade."' ";
        $query .= "   AND FIELD_CNT = ".$field_cnt." ";

        return $query;
    }

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.FIELD_CNT, ";
        $query .= "     L1.MOCKNAME1, ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     L2.GRADE_NAME2 AS GRADE_NAME1 ";
        } else {
            $query .= "     L2.GRADE_NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SCORE_FIELD_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_MST L1 ON L1.MOCKCD = T1.MOCKCD ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE_CD = T1.GRADE ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->mockyear."' ";
        $query .= "     AND SUBSTR(T1.MOCKCD,2,4) = '".$model->mockyear."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND L2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND ";
            $query .= "     L2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //コピー用年度取得
    function getCopyYear($mock)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    YEAR as VALUE, ";
        $query .= "    YEAR as LABEL ";
        $query .= " FROM ";
        $query .= "    MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD LIKE '%".$mock."' ";
        $query .= " AND ";
        $query .= "    FIELD_CNT IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "    YEAR ";

        return $query;
    }
    //割り当て科目取得
    function getSubCd($copyyear, $copymockcd, $fieldno, $mode = "0")
    {
        if ($mode == "0") {
            $query  = " SELECT DISTINCT ";
            $query .= "    MOCK_SUBCLASS_CD ";
        } else {
            $query  = " SELECT ";
            $query .= "    COUNT(MOCK_SUBCLASS_CD) ";
        }
        $query .= " FROM ";
        $query .= "    MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE";
        $query .= "    FIELD_CNT = ".$fieldno." AND ";
        $query .= "    MOCKCD = '".$copymockcd."' AND ";
        $query .= "    YEAR = '".$copyyear."' ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $mockcd, $grade)
    {

        $query  = "SELECT * FROM ";
        $query .= "    MOCK_CSV_SCORE_FIELD_DAT t1 ";
        $query .= "    left join MOCK_MST t2 on t1.MOCKCD = t2.MOCKCD";
        $query .= " WHERE t1.YEAR      = '".$year."' ";
        $query .= "   AND t1.MOCKCD    = '".$mockcd."' ";
        $query .= "   AND t1.GRADE     = '".$grade."' ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";
        $db->query($query);

        for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->mockyear;
            $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
            $data["GRADE"][TEXT]        = $model->field["GRADE"];
            $data["FIELD_CNT"][NUMBER]      = $field_no;
            $data["MOCK_SUBCLASS_CD"][TEXT] = $model->field2["MOCK_SUBCLASS_CD".$field_no];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "MOCK_CSV_SUBCLASS_REPLACE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return $result;
    }

    //コピーチェック
    function getCopyCheck($model)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        MOCKCD, ";
        $query .= "        YEAR ";
        $query .= "    FROM ";
        $query .= "        MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".$model->mockyear."' AND ";
        $query .= "        SUBSTR(MOCKCD,2,4) = '".$model->mockyear."' ";
        $query .= "    GROUP BY ";
        $query .= "        MOCKCD, ";
        $query .= "        YEAR ";
        $query .= "    ) ";
        $query .= " GROUP BY ";
        $query .= "    YEAR ";
        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MOCK_CSV_SUBCLASS_REPLACE_DAT
        $query  = "INSERT INTO MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,2,4) = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) IN (SELECT MOCKCD FROM MOCK_MST) ";
        $query .= ") ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //MOCK追加
    function insertQueryMock($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if ($model->companyCd == "00000001") {
            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_SUNDAI_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND MOSI_CD = (SELECT MAX(MOSI_CD) FROM MOCK_CSV_SUNDAI_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                $db->query($query);
            }

            $query = knjh438Query::getSundaiSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["DEVIATION"], $row["RANK"], $row["CNT"]);
                } else {
                    knjh438Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                }
            }
            $result->free();
        } elseif ($model->companyCd == "00000002") {
            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_BENE_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND KYOUZAICD = (SELECT MAX(KYOUZAICD) FROM MOCK_CSV_BENE_SCORE_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";

                $db->query($query);
            }

            $query = knjh438Query::getBeneSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["ALL_DEV"], $row["ALL_RANK"], $row["CNT"]);
                } else {
                    knjh438Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], $row["GTZ"], $row["ALL_DEV"], $row["ALL_RANK"], "");
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], $row["GTZ"], $row["SCHOOL_DEV"], $row["SCHOOL_RANK"], "");
                }
            }
            $result->free();
        } elseif ($model->companyCd == "00000003") {
            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_ZKAI_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND MOSI_CD = (SELECT MAX(T1.MOSI_CD) ";
                $query .= "                    FROM MOCK_CSV_ZKAI_HEAD_HDAT T1 ";
                $query .= "                    INNER JOIN MOCK_CSV_ZKAI_HDAT T2 ON ";
                $query .= "                        T2.YEAR = T1.YEAR ";
                $query .= "                        AND T2.MOSI_CD = T1.MOSI_CD ";
                $query .= "                        AND T2.MOCKCD = T1.MOCKCD ";
                $query .= "                    WHERE T1.YEAR = '{$model->mockyear}' AND T1.MOCKCD = '{$model->mockcd}' ";
                $query .= "                    )";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                $db->query($query);
            }

            $query = knjh438Query::getZkaiSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["DEVIATION"], $row["RANK"], $row["CNT"]);
                } else {
                    knjh438Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                }
            }
            $result->free();
            knjh438Query::mockRankInOutCdJoretsuDelIns($db, $model, "02", "1");
        } elseif ($model->companyCd == "00000004") {
            $query = "SELECT MAX(MOSI_CD) FROM MOCK_CSV_KAWAI_HEAD_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}' ";
            $moshiCd = $db->getOne($query);

            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_KAWAI_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                //$query .= "     AND MOSI_CD = (SELECT MAX(MOSI_CD) FROM MOCK_CSV_KAWAI_HEAD_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND MOSI_CD = '{$moshiCd}' ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                $db->query($query);
            }

            $query = knjh438Query::getKawaiSore($model);
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", "", "", $row["CNT"]);
                } else {
                    knjh438Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["LEVEL"], 0);
                    knjh438Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["LEVEL"], 0);
                }
            }

            $result->free();
            knjh438Query::mockRankInOutCdJoretsuDelIns($db, $model, "02", "1");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getSundaiSore($model)
    {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT, ";
        $query .= "     MAX(REGD.SCHREGNO) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SUNDAI_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
        } else {
            $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE_CD = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND ";
            $query .= "         REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SCORE.MOCK_SUBCLASS_CD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.DEVIATION, ";
        $query .= "     SCORE.RANK, ";
        $query .= "     0 AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN MOCK_CSV_SUNDAI_SCORE_DAT SCORE ON T1.YEAR = SCORE.YEAR ";
        $query .= "          AND T1.MOSI_CD = SCORE.MOSI_CD ";
        $query .= "          AND T1.EXAMNO = SCORE.EXAMNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
        $query .= "     0 AS SCORE, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    function getBeneSore($model)
    {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.KYOUZAICD, ";
        $query .= "     T1.BENEID, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT, ";
        $query .= "     MAX(REGD.SCHREGNO) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_BENE_SCORE_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
        } else {
            $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE_CD = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND ";
            $query .= "         REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.KYOUZAICD, ";
        $query .= "     T1.BENEID, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SCORE.MOCK_SUBCLASS_CD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.GTZ, ";
        $query .= "     SCORE.ALL_DEV, ";
        $query .= "     SCORE.SCHOOL_DEV, ";
        $query .= "     SCORE.ALL_RANK, ";
        $query .= "     SCORE.SCHOOL_RANK, ";
        $query .= "     0 AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN MOCK_CSV_BENE_SCORE_DAT SCORE ON T1.YEAR = SCORE.YEAR ";
        $query .= "          AND T1.KYOUZAICD = SCORE.KYOUZAICD ";
        $query .= "          AND T1.BENEID = SCORE.BENEID ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
        $query .= "     0 AS SCORE, ";
        $query .= "     '' AS GTZ, ";
        $query .= "     T1.DEVIATION AS ALL_DEV, ";
        $query .= "     0 AS SCHOOL_DEV, ";
        $query .= "     T1.RANK AS ALL_RANK, ";
        $query .= "     0 AS SCHOOL_RANK, ";
        $query .= "     T1.CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    function getZkaiSore($model)
    {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT, ";
        $query .= "     MAX(REGD.SCHREGNO) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_ZKAI_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
        } else {
            $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE_CD = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND ";
            $query .= "         REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SCORE.MOCK_SUBCLASS_CD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.DEVIATION, ";
        $query .= "     SCORE.RANK, ";
        $query .= "     0 AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN MOCK_CSV_ZKAI_SCORE_DAT SCORE ON T1.YEAR = SCORE.YEAR ";
        $query .= "          AND T1.MOSI_CD = SCORE.MOSI_CD ";
        $query .= "          AND T1.HR_CLASS = SCORE.HR_CLASS ";
        $query .= "          AND T1.ATTENDNO = SCORE.ATTENDNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
        $query .= "     0 AS SCORE, ";
        $query .= "     T1.DEVIATION, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    function getKawaiSore($model)
    {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_KAWAI_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
        } else {
            $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        //駿台甲府学園用
        if ($model->CHECK_SCHOOL == 'sundaikoufu') {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE_CD = '{$model->field["GRADE"]}' ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD.YEAR = REGD_G.YEAR AND REGD.GRADE = REGD_G.GRADE AND REGD_G.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND ";
            $query .= "         REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOSI_CD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SCORE.MOCK_SUBCLASS_CD, ";
        $query .= "     SCORE.SCORE, ";
        $query .= "     SCORE.DEVIATION, ";
        $query .= "     SCORE.LEVEL, ";
        $query .= "     NULL AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN MOCK_CSV_KAWAI_SCORE_DAT SCORE ";
        $query .= "           ON T1.YEAR    = SCORE.YEAR ";
        $query .= "          AND T1.MOSI_CD = SCORE.MOSI_CD ";
        $query .= "          AND T1.HR_CLASS = SCORE.HR_CLASS ";
        $query .= "          AND T1.ATTENDNO = SCORE.ATTENDNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
        $query .= "     0 AS SCORE, ";
        $query .= "     0 AS DEVIATION, ";
        $query .= "     '' AS LEVEL, ";
        $query .= "     NULL AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    //MOCK更新
    function getMockDelIns($db, $model, $updData, $range, $rankDiv, $mockDiv, $mockSubclass, $score, $gtz, $deviaton, $rank, $cnt)
    {
        if ($updData["SCHREGNO"] != "") {
            $query  = " DELETE FROM MOCK_RANK_RANGE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->mockyear}' ";
            $query .= "     AND MOCKCD = '{$model->mockcd}' ";
            $query .= "     AND SCHREGNO = '{$updData["SCHREGNO"]}' ";
            $query .= "     AND MOCK_SUBCLASS_CD = '{$mockSubclass}' ";
            $query .= "     AND RANK_RANGE = '{$range}' ";
            $query .= "     AND RANK_DIV = '{$rankDiv}' ";
            $query .= "     AND MOCKDIV = '{$mockDiv}' ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]               = $model->mockyear;
            $data["MOCKCD"][TEXT]             = $model->mockcd;
            $data["SCHREGNO"][TEXT]           = $updData["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]   = $mockSubclass;
            $data["RANK_RANGE"][TEXT]         = $range;
            $data["RANK_DIV"][TEXT]           = $rankDiv;
            $data["MOCKDIV"][TEXT]            = $mockDiv;
            $data["SCORE"][NUMBER]            = $score;
            $data["GTZ"][TEXT]                = $gtz;
            $data["DEVIATION"][NUMBER]        = $deviaton;
            if ($model->companyCd == "00000004") {
                $data["LEVEL"][TEXT]              = $rank;
            } else {
                $data["RANK"][NUMBER]             = $rank;
            }
            $data["CNT"][NUMBER]              = $cnt;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_RANGE_DAT");
            $db->query($query);
        }
    }

    //MOCK更新
    function mockRankInOutCdJoretsuDelIns($db, $model, $rankDiv, $mockDiv)
    {
        $query  = " DELETE FROM MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mockyear}' ";
        $query .= "     AND MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND RANK_RANGE = '3' ";
        $query .= "     AND RANK_DIV = '{$rankDiv}' ";
        $query .= "     AND MOCKDIV = '{$mockDiv}' ";
        $db->query($query);

        $query  = " INSERT INTO MOCK_RANK_RANGE_DAT ";
        $query .= " SELECT ";
        $query .= "     MRANGE.YEAR, ";
        $query .= "     MRANGE.MOCKCD, ";
        $query .= "     MRANGE.SCHREGNO, ";
        $query .= "     MRANGE.MOCK_SUBCLASS_CD, ";
        $query .= "     '3' AS RANK_RANGE, ";
        $query .= "     MRANGE.RANK_DIV, ";
        $query .= "     MRANGE.MOCKDIV, ";
        $query .= "     MRANGE.SCORE, ";
        $query .= "     MRANGE.GTZ, ";
        $query .= "     MRANGE.DEVIATION, ";
        $query .= "     RANK() OVER(PARTITION BY MRANGE.MOCKCD, MRANGE.MOCK_SUBCLASS_CD ORDER BY MRANGE.SCORE DESC) AS RANK, ";
        $query .= "     CNT_T.CNT, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate(), ";
        $query .= "     MRANGE.LEVEL ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_RANGE_DAT MRANGE ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             MRANGE2.MOCKCD, ";
        $query .= "             MRANGE2.MOCK_SUBCLASS_CD, ";
        $query .= "             COUNT(*) AS CNT ";
        $query .= "         FROM ";
        $query .= "             MOCK_RANK_RANGE_DAT MRANGE2 ";
        $query .= "         WHERE ";
        $query .= "             MRANGE2.YEAR = '{$model->mockyear}' ";
        $query .= "             AND MRANGE2.MOCKCD = '{$model->mockcd}' ";
        $query .= "             AND MRANGE2.RANK_RANGE = '2' ";
        $query .= "             AND MRANGE2.RANK_DIV = '{$rankDiv}' ";
        $query .= "             AND MRANGE2.MOCKDIV = '{$mockDiv}' ";
        $query .= "             AND MRANGE2.SCHREGNO IN (SELECT BASE.SCHREGNO FROM SCHREG_BASE_MST BASE WHERE BASE.INOUTCD = '0') ";
        $query .= "             AND MRANGE2.SCORE IS NOT NULL ";
        $query .= "         GROUP BY ";
        $query .= "             MRANGE2.MOCKCD, ";
        $query .= "             MRANGE2.MOCK_SUBCLASS_CD ";
        $query .= "     ) CNT_T ON MRANGE.MOCKCD = CNT_T.MOCKCD ";
        $query .= "     AND MRANGE.MOCK_SUBCLASS_CD = CNT_T.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     MRANGE.YEAR = '{$model->mockyear}' ";
        $query .= "     AND MRANGE.MOCKCD = '{$model->mockcd}' ";
        $query .= "     AND MRANGE.RANK_RANGE = '2' ";
        $query .= "     AND MRANGE.RANK_DIV = '{$rankDiv}' ";
        $query .= "     AND MRANGE.MOCKDIV = '{$mockDiv}' ";
        $query .= "     AND MRANGE.SCHREGNO IN (SELECT BASE2.SCHREGNO FROM SCHREG_BASE_MST BASE2 WHERE BASE2.INOUTCD = '0') ";
        $query .= "     AND MRANGE.SCORE IS NOT NULL ";
        $db->query($query);
    }

    //学校名取得
    function getCheck_School()
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
}
?>
