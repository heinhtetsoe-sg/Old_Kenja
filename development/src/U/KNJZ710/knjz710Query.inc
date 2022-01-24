<?php

require_once('for_php7.php');

class knjz710Query extends Query {

    //年度
    function getYear($div = "MOCKYEAR") {
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
    function getMockcd($model) {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '".$model->mockcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //業者コード
    function getCompanyCd($mockCd) {
        $query  = " SELECT ";
        $query .= "     COMPANYCD ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '{$mockCd}' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        $query .= " AND ";
        $query .= "     GRADE = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試科目
    function getMockSubclassMst() {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     MOCK_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     MOCK_SUBCLASS_CD NOT LIKE '2%' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //MOCK_SCHOOL_COMP_DATの科目名とコード取得
    function getKyouka($year, $mockcd, $grade)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.SUBCLASS_CD, ";
        $query .= "    t1.SUBCLASS_NAME, ";
        $query .= "    t2.ROW ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT t1 left join (SELECT ";
        $query .= "                                            SUBCLASS_NAME, ";
        $query .= "                                            MAX(ROW_NO) as ROW ";
        $query .= "                                        FROM ";
        $query .= "                                            MOCK_SCHOOL_COMP_DAT ";
        $query .= "                                        WHERE ";
        $query .= "                                            YEAR = '".$year."' AND ";
        $query .= "                                            MOCKCD = '".$mockcd."' AND ";
        $query .= "                                            GRADE = '".$grade."' ";
        $query .= "                                        GROUP BY ";
        $query .= "                                            SUBCLASS_NAME ";
        $query .= "                                        ) t2 on t1.SUBCLASS_NAME = t2.SUBCLASS_NAME ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".$year."' AND ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t1.GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "    t2.ROW ";
        
        return $query;
    }

    //模試科目名(ベネッセ)
    function getMockSubclassTitleBene($model, $seq) {
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
    function getMockSubclassTitleSundai($model, $seq) {
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
    function getMockSubclassTitleZkai($model, $seq) {
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

    //模試科目コード取得
    function getMockSubclassCd($year, $mockcd, $grade, $field_cnt) {
        $query  = "SELECT MOCK_SUBCLASS_CD ";
        $query .= "  FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$year."' ";
        $query .= "   AND MOCKCD    = '".$mockcd."' ";
        $query .= "   AND GRADE     = '".$grade."' ";
        $query .= "   AND FIELD_CNT = ".$field_cnt." ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    t2.MOCKNAME1, ";
        $query .= "    t1.GRADE ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT t1 left join MOCK_MST t2 on t1.MOCKCD = t2.MOCKCD ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".$model->mockyear."' ";
        $query .= " ORDER BY ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    t1.GRADE ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $mockcd, $grade) {

        $query  = "SELECT * FROM MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE YEAR      = '".$year."' ";
        $query .= "   AND MOCKCD    = '".$mockcd."' ";
        $query .= "   AND GRADE     = '".$grade."' ";

        return $query;
    }
    
    //コピー用年度取得
    function getCopyYear($mock)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    YEAR as VALUE, ";
        $query .= "    YEAR as LABEL ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "    MOCKCD LIKE '%".$mock."' ";
        $query .= " ORDER BY ";
        $query .= "    YEAR ";
        
        return $query;
    }
    
    //割り当て科目取得
    function getSubCd($copyyear, $copymockcd, $subclassname, $mode="0")
    {
        if($mode == "0"){
            $query  = " SELECT DISTINCT ";
            $query .= "    SUBCLASS_CD ";
        }else{
            $query  = " SELECT ";
            $query .= "    COUNT(SUBCLASS_CD) ";
        }
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "    SUBCLASS_NAME = '".$subclassname."' AND ";
        $query .= "    MOCKCD = '".$copymockcd."' AND ";
        $query .= "    YEAR = '".$copyyear."' ";
        
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        

        //DELETE
        /*$query  = "DELETE FROM MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";
        $db->query($query);

        $query  = "DELETE FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";
        $db->query($query);

        //INSERT
        $data = array();
        $data["YEAR"][TEXT]         = $model->mockyear;
        $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["FIELD_CNT"][NUMBER]  = $model->field["FIELD_CNT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $query = Query::insertSQL($data, "MOCK_CSV_SCORE_FIELD_DAT");
        $db->query($query);*/

        for ($count = 1; $count < $model->kamokuCnt; $count++) {
            $data = array();
            
            //hiddenに入れた模試の科目名取得
            $subclass = VARS::post("subclassname".$count);
            
            $data["SUBCLASS_CD"][TEXT] = $model->field2["MOCK_SUBCLASS_CD".$count];
            
            $where = " WHERE YEAR = '".$model->mockyear."' AND MOCKCD = '".$model->mockcd."' AND GRADE = '".$model->grade."' AND SUBCLASS_NAME = '".$subclass."' ";
            
            $query = Query::updateSQL($data, "MOCK_SCHOOL_COMP_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->mockcd."' ";
        $query .= "   AND GRADE     = '".$model->grade."' ";
        $db->query($query);

        $query  = "DELETE FROM MOCK_CSV_SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->mockcd."' ";
        $query .= "   AND GRADE     = '".$model->grade."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return $result;
    }

    //コピーチェック
    function getCopyCheck($model) {
        $query  = " SELECT COUNT(*) FROM MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE YEAR = '".$model->mockyear."' ";
        $query .= "   AND SUBSTR(MOCKCD,2,4) = '".$model->mockyear."'";
        return $query;

    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MOCK_CSV_SCORE_FIELD_DAT
        $query  = "INSERT INTO MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,2,4) = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) IN (SELECT MOCKCD FROM MOCK_MST) ";
        $query .= ") ";
        $db->query($query);

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

        if ($model->companyCd == "00000002") {

            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_SUNDAI_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND MOSI_CD = (SELECT MAX(MOSI_CD) FROM MOCK_CSV_SUNDAI_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                $db->query($query);
            }

            $query = knjz710Query::getSundaiSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjz710Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["DEVIATION"], $row["RANK"], $row["CNT"]);
                } else {
                    knjz710Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                }
            }
            $result->free();

        } else if ($model->companyCd == "00000001") {

            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_BENE_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND KYOUZAICD = (SELECT MAX(KYOUZAICD) FROM MOCK_CSV_BENE_SCORE_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                
                $db->query($query);
            }

            $query = knjz710Query::getBeneSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjz710Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["ALL_DEV"], $row["ALL_RANK"], $row["CNT"]);
                } else {
                    knjz710Query::getMockDelIns($db, $model, $row, "1", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], $row["GTZ"], $row["ALL_DEV"], $row["ALL_RANK"], "");
                    knjz710Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], $row["GTZ"], $row["SCHOOL_DEV"], $row["SCHOOL_RANK"], "");
                }
            }
            $result->free();
        } else if ($model->companyCd == "00000003") {

            for ($field_no = 1; $field_no <= $model->field["FIELD_CNT"]; $field_no++) {
                $query  = " UPDATE MOCK_CSV_ZKAI_SCORE_DAT SET MOCK_SUBCLASS_CD = '{$model->field2["MOCK_SUBCLASS_CD".$field_no]}' ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->mockyear}' ";
                $query .= "     AND MOSI_CD = (SELECT MAX(MOSI_CD) FROM MOCK_CSV_ZKAI_HEAD_HDAT WHERE YEAR = '{$model->mockyear}' AND MOCKCD = '{$model->mockcd}') ";
                $query .= "     AND SEQ = '{$field_no}' ";
                $query .= "  ";
                $db->query($query);
            }

            $query = knjz710Query::getZkaiSore($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["MOCK_SUBCLASS_CD"] == "999999") {
                    knjz710Query::getMockDelIns($db, $model, $row, "2", "02", "1", "999999", "", "", $row["DEVIATION"], $row["RANK"], $row["CNT"]);
                } else {
                    knjz710Query::getMockDelIns($db, $model, $row, "2", "02", "1", $row["MOCK_SUBCLASS_CD"], $row["SCORE"], "", $row["DEVIATION"], $row["RANK"], "");
                }
            }
            $result->free();
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getSundaiSore($model) {
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
        $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
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

    function getBeneSore($model) {
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
        $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
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

    function getZkaiSore($model) {
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
        $query .= "          AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "          AND REGD.HR_CLASS = right('00' || T1.HR_CLASS, 3) ";
        $query .= "          AND REGD.ATTENDNO = right('00' || T1.ATTENDNO, 3) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->mockyear}' ";
        $query .= "     AND T1.MOCKCD = '{$model->mockcd}' ";
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

    //MOCK更新
    function getMockDelIns($db, $model, $updData, $range, $rankDiv, $mockDiv, $mockSubclass, $score, $gtz, $deviaton, $rank, $cnt)
    {
        if($updData["SCHREGNO"] != ""){
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
            $data["RANK"][NUMBER]             = $rank;
            $data["CNT"][NUMBER]              = $cnt;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_RANGE_DAT");
            $db->query($query);
        }
    }
}
?>
