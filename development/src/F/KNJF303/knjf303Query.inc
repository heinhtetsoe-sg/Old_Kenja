<?php

require_once('for_php7.php');

class knjf303query extends Query {

    //委員会
    function getUseIinkai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND ABBV1 IN ('1', '2') ";

        return $query;
    }

    //年度の取得
    function getYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        
        return $query;
    }

    //学校コードの取得
    function getSchoolcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        return $query;
    }

    //作成済みの一覧コンボ
    function getDataRireki($model) {

        $year = ($model->year) ? $model->year : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "      MAX(SUSPEND_DIRECT_DATE) AS SUSPEND_DIRECT_DATE, ";
        $query .= "     '0' AS TOTAL_DIV, ";
        $query .= "     '000' AS DISEASECD, ";
        $query .= "     '0_new' AS VALUE, ";
        $query .= "     '新規' AS LABEL ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SUSPEND_DIRECT_DATE, ";
        $query .= "     T1.TOTAL_DIV, ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     CHAR(T1.SUSPEND_DIRECT_DATE) || ':' || T1.TOTAL_DIV || ':' || T1.DISEASECD AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.SUSPEND_DIRECT_DATE),'-','/'))) || '　' || ";
        $query .= "     CASE TOTAL_DIV WHEN '1' THEN '出席停止' WHEN '2' THEN '欠席　　' WHEN '3' THEN '登校　　' ELSE '' END || '　' || ";
        $query .= "     L1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT T1";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     SUSPEND_DIRECT_DATE DESC, ";
        $query .= "     TOTAL_DIV, ";
        $query .= "     DISEASECD ";

        return $query;
    }

    //県への報告履歴
    function getReport($model, $Row)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SUSPEND_DIRECT_DATE, ";
        $query .= "     T1.TOTAL_DIV, ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     CHAR(T1.EXECUTE_DATE) || ':' || T1.TOTAL_DIV || ':' || CHAR(T1.SUSPEND_DIRECT_DATE) || ':' ||  T1.DISEASECD  || ':' || T1.GRADE || ':' || T1.HR_CLASS AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.EXECUTE_DATE),'-','/'))) || '　' || CASE TOTAL_DIV WHEN '1' THEN '出席停止' WHEN '2' THEN '欠席' WHEN '3' THEN '登校' ELSE '' END  || '　' || RTRIM(CHAR(REPLACE(CHAR(T1.SUSPEND_DIRECT_DATE),'-','/'))) || '　' ||  L1.NAME1  || '　' || L2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION3_DAT T1";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.GRADE = T1.GRADE ";
        $query .= "                              AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "                              AND L2.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR      = '".$model->year."' ";
        $query .= " AND T1.TOTAL_DIV = '".$Row["TOTAL_DIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.TOTAL_DIV, ";
        $query .= "     T1.SUSPEND_DIRECT_DATE DESC, ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }
    

    //データの取得
    function getAdditionData($model)
    {
        $setdata_array = array();
        $setdata_array = explode(":", $model->data_div);
        
        $query  = " SELECT  ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR      = '".$model->year."' ";
        $query .= " AND T1.SUSPEND_DIRECT_DATE = '".$setdata_array[0]."' ";
        $query .= " AND T1.TOTAL_DIV = '".$setdata_array[1]."' ";
        $query .= " AND T1.DISEASECD = '".$setdata_array[2]."' ";

        return $query;
    }

    //理由（疾患名）の取得
    function getDiseasecd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = 'C900' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }


    //全体のデータの取得
    function selectQuery($model, $Row) {
        $query .= "  WITH DATA_LIST AS ( ";
        $query .= "      SELECT DISTINCT ";
        $query .= "          T1.EDBOARD_SCHOOLCD, ";
        $query .= "          T1.YEAR, ";
        $query .= "          T1.SUSPEND_DIRECT_DATE, ";
        $query .= "          T1.DISEASECD, ";
        $query .= "          T1.GRADE, ";
        $query .= "          T1.HR_CLASS, ";
        $query .= "          L2.COUNT AS TEISHI_COUNT, ";
        $query .= "          L3.COUNT AS KESSEKI_COUNT, ";
        $query .= "          L4.COUNT AS TOUKOU_COUNT ";
        $query .= "      FROM ";
        $query .= "          MEDEXAM_DISEASE_ADDITION3_COUNT_DAT T1 ";
        $query .= "          LEFT JOIN V_SCHOOL_MST L1 ON T1.EDBOARD_SCHOOLCD = L1.KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= "          LEFT JOIN MEDEXAM_DISEASE_ADDITION3_COUNT_DAT L2 ON L2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= "                                AND L2.YEAR = T1.YEAR ";
        $query .= "                                AND L2.SUSPEND_DIRECT_DATE = T1.SUSPEND_DIRECT_DATE ";
        $query .= "                                AND L2.DISEASECD = T1.DISEASECD ";
        $query .= "                                AND L2.GRADE = T1.GRADE ";
        $query .= "                                AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                AND L2.TOTAL_DIV = '1' ";
        $query .= "          LEFT JOIN MEDEXAM_DISEASE_ADDITION3_COUNT_DAT L3 ON L3.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= "                                AND L3.YEAR = T1.YEAR ";
        $query .= "                                AND L3.SUSPEND_DIRECT_DATE = T1.SUSPEND_DIRECT_DATE ";
        $query .= "                                AND L3.DISEASECD = T1.DISEASECD ";
        $query .= "                                AND L3.GRADE = T1.GRADE ";
        $query .= "                                AND L3.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                AND L3.TOTAL_DIV = '2' ";
        $query .= "          LEFT JOIN MEDEXAM_DISEASE_ADDITION3_COUNT_DAT L4 ON L4.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= "                                AND L4.YEAR = T1.YEAR ";
        $query .= "                                AND L4.SUSPEND_DIRECT_DATE = T1.SUSPEND_DIRECT_DATE ";
        $query .= "                                AND L4.DISEASECD = T1.DISEASECD ";
        $query .= "                                AND L4.GRADE = T1.GRADE ";
        $query .= "                                AND L4.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                AND L4.TOTAL_DIV = '3' ";
        $query .= "      WHERE ";
        $query .= "          T1.YEAR= '".$model->year."' ";
        $query .= "      AND T1.SUSPEND_DIRECT_DATE = '".str_replace("/","-",$Row["SUSPEND_DIRECT_DATE"])."' ";
        $query .= "      AND T1.DISEASECD = '".$Row["DISEASECD"]."' ";
        $query .= "  ), ZAISEKI_COUNT AS ( ";
        $query .= "      SELECT ";
        $query .= "          T1.YEAR, ";
        $query .= "          T1.GRADE, ";
        $query .= "          T1.HR_CLASS, ";
        $query .= "          L2.HR_NAME, ";
        $query .= "          COUNT(*) AS ZAISEKI_COUNT ";
        $query .= "      FROM ";
        $query .= "          DATA_LIST T1 ";
        $query .= "          LEFT JOIN SCHREG_REGD_DAT  L1 ON L1.GRADE = T1.GRADE ";
        $query .= "                                      AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                      AND L1.YEAR = T1.YEAR ";
        $query .= "          LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.GRADE = T1.GRADE ";
        $query .= "                                       AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                       AND L2.YEAR = T1.YEAR ";
        $query .= "      WHERE ";
        $query .= "          L1.SEMESTER = L2.SEMESTER ";
        $query .= "      AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      GROUP BY ";
        $query .= "          T1.YEAR, ";
        $query .= "          T1.GRADE, ";
        $query .= "          T1.HR_CLASS, ";
        $query .= "          L2.HR_NAME ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      T1.*, ";
        $query .= "      L1.HR_NAME, ";
        $query .= "      L1.ZAISEKI_COUNT ";
        $query .= "  FROM ";
        $query .= "      DATA_LIST T1 ";
        $query .= "      LEFT JOIN ZAISEKI_COUNT L1 ON L1.GRADE = T1.GRADE ";
        $query .= "                                AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= "                                AND L1.YEAR = T1.YEAR ";
        
        return $query;
    }

    //データの取得
    function countAddition3Dat($model, $flg)
    {
        $query  = " SELECT  ";
        if ($flg === 'COUNT') {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L1.NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT T1 ";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR      = '".$model->year."' ";
        $query .= " AND T1.SUSPEND_DIRECT_DATE = '". str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= " AND T1.TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= " AND T1.DISEASECD = '".$model->field["DISEASECD"]."' ";

        return $query;
    }
    
    /* データ更新処理 */
    //MEDEXAM_DISEASE_ADDITION3_DATを更新する
    function updateDat($model) {
    
        $db = Query::dbCheckOut();
        //データの確認
        $dat_count = $db->getOne(knjf303Query::countAddition3Dat($model, "COUNT"));
        
        $data = array();
        $data["DISEASECD_REMARK"][TEXT]     = $model->field["DISEASECD_REMARK"];
        $data["SUSPEND_S_DATE"][TEXT]       = str_replace("/","-",$model->suspend_direct_date);//出席停止を指示した日と同一
        $data["SUSPEND_E_DATE"][TEXT]       = str_replace("/","-",$model->field["SUSPEND_E_DATE"]);
        $data["SUSPEND_REMARK"][TEXT]       = $model->field["SUSPEND_REMARK"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
        
        $data["REGISTERCD"][TEXT]           = STAFFCD ;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($dat_count > 0) {
            $where  = " WHERE ";
            $where .= "      EDBOARD_SCHOOLCD    = '" .$model->schoolcd."' ";
            $where .= " AND  YEAR                = '" .$model->year."' ";
            $where .= " AND  SUSPEND_DIRECT_DATE = '".str_replace("/","-",$model->suspend_direct_date)."' ";
            $where .= " AND  TOTAL_DIV           = '" .$model->field["TOTAL_DIV"]."' ";
            $where .= " AND  DISEASECD           = '" .$model->field["DISEASECD"]."' ";
            
            $query = Query::updateSQL($data, "MEDEXAM_DISEASE_ADDITION3_DAT", $where);//更新
        } else {
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SUSPEND_DIRECT_DATE"][TEXT]  = str_replace("/","-",$model->suspend_direct_date);
            $data["TOTAL_DIV"][TEXT]            = $model->field["TOTAL_DIV"];
            $data["DISEASECD"][TEXT]            = $model->field["DISEASECD"];

            $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION3_DAT");        //追加
        }
        $db->query($query);
        Query::dbCheckIn($db);
        
        return;
    }
    
    //MEDEXAM_DISEASE_ADDITION3_COUNT_DATデータが存在するか
    function getAddtion3CountDatCnt($model, $grade_hr_class) {
        $query  = "SELECT  ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_COUNT_DAT ";
        $query .= "WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR     = '".$model->year."' ";
        $query .= "     AND SUSPEND_DIRECT_DATE = '".str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= "     AND TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= "     AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $query .= "     AND ((GRADE || '-' || HR_CLASS) = '{$grade_hr_class}' ) ";

        return $query;
    }
    
    //MEDEXAM_DISEASE_ADDITION3_COUNT_DATを更新する(基本別画面の更新反映のデータのみだが、追加処理も念のため追加)
    function updateCountDat($model, $flg) {
    
        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($model->data["GRADE_HR_CLASS"]); $i++) {
            $grade_hr_class = $model->data["GRADE_HR_CLASS"][$i];
            $grade_hr_class_set = array();
            $grade_hr_class_set = explode("-", $grade_hr_class);

            //MEDEXAM_DISEASE_ADDITION3_COUNT_DATのデータ有無の確認
            $query = knjf303Query::getAddtion3CountDatCnt($model, $model->data["GRADE_HR_CLASS"][$i]);
            $cnt = get_count($db->getCol($query));

            $data = array();
            if ($cnt == 0) {
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->year;
                $data["SUSPEND_DIRECT_DATE"][TEXT]  = str_replace("/","-",$model->suspend_direct_date);
                $data["TOTAL_DIV"][TEXT]            = $model->field["TOTAL_DIV"];
                $data["DISEASECD"][TEXT]            = $model->field["DISEASECD"];
                $data["GRADE"][TEXT]                = $grade_hr_class_set[0];
                $data["HR_CLASS"][TEXT]             = $grade_hr_class_set[1];
            }
            
            if ($flg == "TEISHI") {
                $data["COUNT"][NUMBER]       = $model->fields["TEISHI_COUNT"][$i];
            } else if ($flg == "KESSEKI") {
                $data["COUNT"][NUMBER]       = $model->fields["KESSEKI_COUNT"][$i];
            } else if ($flg == "TOUKOU") {
                $data["COUNT"][NUMBER]       = $model->fields["TOUKOU_COUNT"][$i];
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "      EDBOARD_SCHOOLCD    = '".$model->schoolcd."' ";
                $where .= " AND  YEAR                = '".$model->year."' ";
                $where .= " AND  SUSPEND_DIRECT_DATE = '".str_replace("/","-",$model->suspend_direct_date)."' ";
                $where .= " AND  TOTAL_DIV           = '".$model->field["TOTAL_DIV"]."' ";
                $where .= " AND  DISEASECD           = '".$model->field["DISEASECD"]."' ";
                $where .= " AND  GRADE               = '".$grade_hr_class_set[0]."' ";
                $where .= " AND  HR_CLASS            = '".$grade_hr_class_set[1]."' ";
                //更新
                $query = Query::updateSQL($data, "MEDEXAM_DISEASE_ADDITION3_COUNT_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION3_COUNT_DAT");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return;
    }
    
    /* 報告処理 */
    //データの削除
    function DeleteAddtion3Dat($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
    
        //県側MEDEXAM_DISEASE_ADDITION3_COUNT_DAT削除
        $query  = " DELETE FROM  ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_COUNT_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->year."' ";
        $query .= " AND SUSPEND_DIRECT_DATE = '". str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= " AND TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $db2->query($query);

        //県側MEDEXAM_DISEASE_ADDITION3_DAT削除
        $query  = " DELETE FROM  ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->year."' ";
        $query .= " AND SUSPEND_DIRECT_DATE = '". str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= " AND TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $db2->query($query);

        //県側、学校側REPORT_DISEASE_ADDITION3_DAT削除（報告日で削除）
        $query  = " DELETE FROM  ";
        $query .= "     REPORT_DISEASE_ADDITION3_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->year."' ";
        $query .= " AND SUSPEND_DIRECT_DATE = '". str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= " AND TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $db->query($query);
        $db2->query($query);
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //学校側からデータの取得
    function getAddtion3Dat($model, $flg)
    {
        $query  = " SELECT  ";
        $query .= "     * ";
        $query .= " FROM ";
        if ($flg === 'COUNT') {
            $query .= "     MEDEXAM_DISEASE_ADDITION3_COUNT_DAT ";
        } else {
            $query .= "     MEDEXAM_DISEASE_ADDITION3_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->year."' ";
        $query .= " AND SUSPEND_DIRECT_DATE = '". str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= " AND TOTAL_DIV = '".$model->field["TOTAL_DIV"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        
        return $query;
    }
    
    //県側MEDEXAM_DISEASE_ADDITION3_DAT追加
    function setAddtion3Dat($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $query = knjf303Query::getAddtion3Dat($model, "DAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjf303Query::updateAddtion3Dat($model, $db2, $row);
        }
        $result->free();
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        
        return ;
    }
    
    //県側MEDEXAM_DISEASE_ADDITION3_COUNT_DAT追加 + 学校側報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
    function setAddtion3Dat2($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $query = knjf303Query::getAddtion3Dat($model, "COUNT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjf303Query::updateAddtion3CountDat($model, $db, $db2, $row);
        }
        $result->free();
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        
        return ;
    }
    
    
    //県側　MEDEXAM_DISEASE_ADDITION3_DAT追加
    function updateAddtion3Dat($model, $db2, $row) {
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]                 = $row["YEAR"];
        $data["SUSPEND_DIRECT_DATE"][TEXT]  = $row["SUSPEND_DIRECT_DATE"];
        $data["TOTAL_DIV"][TEXT]            = $row["TOTAL_DIV"];
        $data["DISEASECD"][TEXT]            = $row["DISEASECD"];
        $data["DISEASECD_REMARK"][TEXT]     = $row["DISEASECD_REMARK"];
        $data["SUSPEND_S_DATE"][TEXT]       = $row["SUSPEND_S_DATE"];
        $data["SUSPEND_E_DATE"][TEXT]       = $row["SUSPEND_E_DATE"];
        $data["SUSPEND_REMARK"][TEXT]       = $row["SUSPEND_REMARK"];
        $data["REMARK1"][TEXT]              = $row["REMARK1"];
        $data["REMARK2"][TEXT]              = $row["REMARK2"];
        $data["REMARK3"][TEXT]              = $row["REMARK3"];
        $data["REGISTERCD"][TEXT]    = STAFFCD ;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION3_DAT");
        $db2->query($query);
        
        return;
    }
    
    //県側　MEDEXAM_DISEASE_ADDITION3_COUNT_DAT追加、学校側　報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
    function updateAddtion3CountDat($model, $db, $db2, $row) {
        //県側　MEDEXAM_DISEASE_ADDITION3_COUNT_DAT追加
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]                 = $row["YEAR"];
        $data["SUSPEND_DIRECT_DATE"][TEXT]  = $row["SUSPEND_DIRECT_DATE"];
        $data["TOTAL_DIV"][TEXT]            = $row["TOTAL_DIV"];
        $data["DISEASECD"][TEXT]            = $row["DISEASECD"];
        $data["GRADE"][TEXT]                = $row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
        $data["COUNT"][NUMBER]              = $row["COUNT"];
        $data["REGISTERCD"][TEXT]    = STAFFCD ;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION3_COUNT_DAT");
        $db2->query($query);
        
        //県側、学校側　報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
        $data2["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
        $data2["YEAR"][TEXT]                 = $row["YEAR"];
        $data2["SUSPEND_DIRECT_DATE"][TEXT]  = $row["SUSPEND_DIRECT_DATE"];
        $data2["TOTAL_DIV"][TEXT]            = $row["TOTAL_DIV"];
        $data2["DISEASECD"][TEXT]            = $row["DISEASECD"];
        $data2["GRADE"][TEXT]                = $row["GRADE"];
        $data2["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
        $data2["EXECUTE_DATE"][TEXT]         = str_replace("/","-",$model->field["EXECUTE_DATE"]);
        $data2["REGISTERCD"][TEXT]    = STAFFCD ;
        $data2["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data2, "REPORT_DISEASE_ADDITION3_DAT");
        $db->query($query);
        $db2->query($query);
        
        return;
    }
}
?>
