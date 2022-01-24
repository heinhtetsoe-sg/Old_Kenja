<?php

require_once('for_php7.php');

class knjh455Query extends Query {

    function getMockMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "     T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.COMPANYCD = '00000002' ";
        $query .= "     AND SUBSTR(T1.MOCKCD,2,4) = '{$model->field["YEAR"]}' ";
        $query .= "     AND '0' || SUBSTR(T1.MOCKCD,7,1) = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";


        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJH455' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    function getYear($properties)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if($properties["useSchool_KindField"] == "1"){
            $query .= " WHERE ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_CD AS VALUE, ";
        $query .= "     GRADE_NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if($model->Properties["useSchool_KindField"] == "1"){
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //MOCK_RANK_RANGE_DAT
    function getMockRankRangeDat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    function getHopeField($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_HOPE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    function getScoreField($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SCORE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //MOCK_SCHOOL_COMP_DATのIMPORT_NOの最大値取得
    function getImportNo()
    {
        $query  = " SELECT ";
        $query .= "    MAX(IMPORT_NO) ";
        $query .= " FROM ";
        $query .= "    MOCK_SCHOOL_COMP_DAT ";
        
        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MOCK_SCHOOL_COMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

                $data = array();

                $data["IMPORT_NO"][NUMBER]      = $data_arr[$i]["IMPORT_NO"];
                $data["ROW_NO"][NUMBER]         = $data_arr[$i]["ROW_NO"];
                
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["GAKKONAME"][TEXT]        = $data_arr[$i]["GAKKONAME"];
                $data["GAKKOCODE"][TEXT]        = $data_arr[$i]["GAKKOCODE"];
                $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                $data["MOCKCD"][TEXT]           = $data_arr[$i]["MOCKCD"];
                $data["MOCKNAME"][TEXT]         = $data_arr[$i]["MOCKNAME"];
                $data["SUBCLASS_CD"][TEXT]      = "";
                $data["SUBCLASS_NAME"][TEXT]    = $data_arr[$i]["SUBCLASS_NAME"];
                $data["EXAM_COUNT"][NUMBER]     = $data_arr[$i]["EXAM_COUNT"];
                $data["AVERAGE"][NUMBER]        = $data_arr[$i]["AVERAGE"];
                $data["STD_DEVIATION"][NUMBER]  = $data_arr[$i]["STD_DEVIATION"];
                $data["AVG_DEVIATION"][NUMBER]  = $data_arr[$i]["AVG_DEVIATION"];
                $data["PERFECT"][NUMBER]        = $data_arr[$i]["PERFECT"];
                $data["SIMPLE_80"][NUMBER]      = $data_arr[$i]["SIMPLE_80"];
                $data["SIMPLE_75"][NUMBER]      = $data_arr[$i]["SIMPLE_75"];
                $data["SIMPLE_70"][NUMBER]      = $data_arr[$i]["SIMPLE_70"];
                $data["SIMPLE_65"][NUMBER]      = $data_arr[$i]["SIMPLE_65"];
                $data["SIMPLE_60"][NUMBER]      = $data_arr[$i]["SIMPLE_60"];
                $data["SIMPLE_55"][NUMBER]      = $data_arr[$i]["SIMPLE_55"];
                $data["SIMPLE_50"][NUMBER]      = $data_arr[$i]["SIMPLE_50"];
                $data["SIMPLE_45"][NUMBER]      = $data_arr[$i]["SIMPLE_45"];
                $data["SIMPLE_40"][NUMBER]      = $data_arr[$i]["SIMPLE_40"];
                $data["SIMPLE_35"][NUMBER]      = $data_arr[$i]["SIMPLE_35"];
                $data["SIMPLE_30"][NUMBER]      = $data_arr[$i]["SIMPLE_30"];
                $data["SIMPLE_LOW"][NUMBER]     = $data_arr[$i]["SIMPLE_LOW"];
                $data["TOTAL_80"][NUMBER]       = $data_arr[$i]["TOTAL_80"];
                $data["TOTAL_75"][NUMBER]       = $data_arr[$i]["TOTAL_75"];
                $data["TOTAL_70"][NUMBER]       = $data_arr[$i]["TOTAL_70"];
                $data["TOTAL_65"][NUMBER]       = $data_arr[$i]["TOTAL_65"];
                $data["TOTAL_60"][NUMBER]       = $data_arr[$i]["TOTAL_60"];
                $data["TOTAL_55"][NUMBER]       = $data_arr[$i]["TOTAL_55"];
                $data["TOTAL_50"][NUMBER]       = $data_arr[$i]["TOTAL_50"];
                $data["TOTAL_45"][NUMBER]       = $data_arr[$i]["TOTAL_45"];
                $data["TOTAL_40"][NUMBER]       = $data_arr[$i]["TOTAL_40"];
                $data["TOTAL_35"][NUMBER]       = $data_arr[$i]["TOTAL_35"];
                $data["TOTAL_30"][NUMBER]       = $data_arr[$i]["TOTAL_30"];
                $data["TOTAL_LOW"][NUMBER]      = $data_arr[$i]["TOTAL_LOW"];


                $query = Query::insertSQL($data, "MOCK_SCHOOL_COMP_DAT");
                $db->query($query);


            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;

    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $query  = " DELETE FROM MOCK_SCHOOL_COMP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
            $query .= "     AND MOCKCD = '{$data_arr[$i]["MOCKCD"]}' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }

    function getTitleMockCompany($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H313' ";
        $query .= "     AND NAMESPARE1 = '{$model->field["COMPANY"]}' ";

        return $query;
    }

    function getTitleCsvDiv($model) {
        $query .= " WITH MAIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('得点データ', '1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('希望データ', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     VALUE = '{$model->field["CSV_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getTitleMockMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.MOCKCD = '{$model->field["MOCKCD"]}' ";

        return $query;
    }

    //学年
    function getTitleGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }
}
?>
