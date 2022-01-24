<?php

require_once('for_php7.php');

class knjf305query extends Query {

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
        $query .= "     VALUE DESC";
        
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
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";

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
        $query .= "     YEAR    = '".$model->field["YEAR"]."' ";
        $query .= " AND NAMECD1 = 'C900' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "           AND G1.GRADE = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$gakki,$grade) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR='{$year}'";
            $query .= " AND SEMESTER='{$gakki}'";
            $query .= " AND GRADE    = '{$grade}' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '{$year}' ";
            $query .= "     AND SEMESTER = '{$gakki}' ";
            $query .= "     AND GRADE    = '{$grade}' ";
            $query .= "     AND (TR_CD1  = '".STAFFCD."' ";
            $query .= "     OR   TR_CD2  = '".STAFFCD."' ";
            $query .= "     OR   TR_CD3  = '".STAFFCD."') ";
        }

        return $query;
    }

    //作成済みの一覧コンボ
    function getDataRireki($model) {
        $query  = " WITH T_NEW (DISEASECD, GRADE, HR_CLASS, ACTION_S_DATE, VALUE, LABEL) AS ( ";
        $query .= "     VALUES('000', '00', '000', DATE('2099-04-01'), '0_new', '新規') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ACTION_S_DATE, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.LABEL ";
        $query .= " FROM ";
        $query .= "     T_NEW T1 ";
        $query .= " UNION ALL ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ACTION_S_DATE, ";
        $query .= "     CHAR(T1.ACTION_S_DATE) || ':' || T1.GRADE || ':' || T1.HR_CLASS || ':' || T1.DISEASECD AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.ACTION_S_DATE),'-','/'))) || '　' || ";
        $query .= "     CASE WHEN T1.GRADE = '99' AND T1.HR_CLASS = '999' THEN '学校　 ' WHEN T1.GRADE <> '99' AND T1.HR_CLASS = '999' THEN L3.GRADE_NAME1 ELSE L2.HR_NAME END || '　' || ";
        $query .= "     L1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION4_DAT T1";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.GRADE = T1.GRADE ";
        $query .= "                              AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "                              AND L2.YEAR = T1.YEAR ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT L3 ON L3.GRADE = T1.GRADE ";
        $query .= "                              AND L3.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR      = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     ACTION_S_DATE DESC, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     DISEASECD ";

        return $query;
    }

    //県への報告履歴
    function getReport($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     T1.ACTION_S_DATE, ";
        $query .= "     CHAR(T1.EXECUTE_DATE) || ':' || T1.GRADE || ':' || T1.HR_CLASS || ':' || T1.DISEASECD || ':' || CHAR(T1.ACTION_S_DATE) AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.EXECUTE_DATE),'-','/'))) || '　' || CASE WHEN T1.GRADE = '99' AND T1.HR_CLASS = '999' THEN '学校　 ' WHEN T1.GRADE <> '99' AND T1.HR_CLASS = '999' THEN L3.GRADE_NAME1 ELSE L2.HR_NAME END || '　' || L1.NAME1 || '　' || RTRIM(CHAR(REPLACE(CHAR(T1.ACTION_S_DATE),'-','/'))) AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION4_DAT T1";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.GRADE = T1.GRADE ";
        $query .= "                              AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "                              AND L2.YEAR = T1.YEAR ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT L3 ON L3.GRADE = T1.GRADE ";
        $query .= "                              AND L3.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR      = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.DISEASECD, ";
        $query .= "     T1.ACTION_S_DATE DESC ";

        return $query;
    }

    //データの取得
    function getAddition4Dat($model, $flg)
    {
        $query  = " SELECT  ";
        if ($flg === 'COUNT') {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L1.NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION4_DAT T1 ";
        $query .= " LEFT JOIN V_NAME_MST L1 ON L1.NAMECD2 = T1.DISEASECD ";
        $query .= "                        AND L1.NAMECD1 = 'C900' ";
        $query .= "                        AND L1.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND T1.YEAR              = '".$model->field["YEAR"]."' ";
        $query .= " AND T1.DISEASECD         = '".$model->field["DISEASECD"]."' ";
        $query .= " AND T1.GRADE             = '".$model->field["GRADE"]."' ";
        $query .= " AND T1.HR_CLASS          = '".$model->field["HR_CLASS"]."' ";
        $query .= " AND T1.ACTION_S_DATE     = '".str_replace("/","-",$model->field["ACTION_S_DATE"])."' ";

        return $query;
    }
    
    /* データ更新処理 */
    //MEDEXAM_DISEASE_ADDITION4_DATを更新する
    function updateDat($model) {
    
        $db = Query::dbCheckOut();
        //閉鎖区分により学年、クラスの値をセット
        if ($model->field["HEISA_DIV"] === '2' || $model->field["HEISA_DIV"] === '3') {
            $model->field["HR_CLASS"] = '999';
        }
        if ($model->field["HEISA_DIV"] === '3') {
            $model->field["GRADE"] = '99';
        }
        //データの確認
        $dat_count = $db->getOne(knjf305Query::getAddition4Dat($model, "COUNT"));
        
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]       = $model->schoolcd;
        $data["YEAR"][TEXT]                   = $model->field["YEAR"];
        $data["DISEASECD"][TEXT]              = $model->field["DISEASECD"];
        $data["GRADE"][TEXT]                  = $model->field["GRADE"];
        $data["HR_CLASS"][TEXT]               = $model->field["HR_CLASS"];
        $data["PATIENT_COUNT"][NUMBER]        = $model->field["PATIENT_COUNT"];
        $data["ABSENCE_COUNT"][NUMBER]        = $model->field["ABSENCE_COUNT"];
        $data["PRESENCE_COUNT"][NUMBER]       = $model->field["PRESENCE_COUNT"];
        $data["ACTION_S_DATE"][TEXT]          = str_replace("/","-",$model->field["ACTION_S_DATE"]);
        $data["ACTION_E_DATE"][TEXT]          = str_replace("/","-",$model->field["ACTION_E_DATE"]);
        $data["SYMPTOM01"][TEXT]              = $model->field["SYMPTOM01"];
        $data["SYMPTOM01_REMARK"][TEXT]       = $model->field["SYMPTOM01_REMARK"];
        $data["SYMPTOM02"][TEXT]              = $model->field["SYMPTOM02"];
        $data["SYMPTOM03"][TEXT]              = $model->field["SYMPTOM03"];
        $data["SYMPTOM04"][TEXT]              = $model->field["SYMPTOM04"];
        $data["SYMPTOM05"][TEXT]              = $model->field["SYMPTOM05"];
        $data["SYMPTOM06"][TEXT]              = $model->field["SYMPTOM06"];
        $data["SYMPTOM07"][TEXT]              = $model->field["SYMPTOM07"];
        $data["SYMPTOM08"][TEXT]              = $model->field["SYMPTOM08"];
        $data["SYMPTOM09"][TEXT]              = $model->field["SYMPTOM09"];
        $data["SYMPTOM10"][TEXT]              = $model->field["SYMPTOM10"];
        $data["SYMPTOM11"][TEXT]              = $model->field["SYMPTOM11"];
        $data["SYMPTOM12"][TEXT]              = $model->field["SYMPTOM12"];
        $data["SYMPTOM12_REMARK"][TEXT]       = $model->field["SYMPTOM12_REMARK"];
        $data["REMARK"][TEXT]                 = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD ;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($dat_count > 0) {
            $where  = " WHERE ";
            $where .= "      EDBOARD_SCHOOLCD  = '" .$model->schoolcd."' ";
            $where .= " AND  YEAR              = '" .$model->field["YEAR"]."' ";
            $where .= " AND  DISEASECD         = '" .$model->field["DISEASECD"]."' ";
            $where .= " AND  GRADE             = '" .$model->field["GRADE"]."' ";
            $where .= " AND  HR_CLASS          = '" .$model->field["HR_CLASS"]."' ";
            $where .= " AND  ACTION_S_DATE     = '".str_replace("/","-",$model->field["ACTION_S_DATE"])."' ";
            
            $query = Query::updateSQL($data, "MEDEXAM_DISEASE_ADDITION4_DAT", $where);//更新
        } else {
            $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION4_DAT");        //追加
        }
        $db->query($query);
        Query::dbCheckIn($db);
        
        return;
    }
    
    /* 報告処理 */
    //データの削除
    function DeleteAddtion4Dat($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
    
        //県側MEDEXAM_DISEASE_ADDITION4_DAT削除
        $query  = " DELETE FROM  ";
        $query .= "     MEDEXAM_DISEASE_ADDITION4_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->field["YEAR"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $query .= " AND GRADE     = '" .$model->field["GRADE"]."' ";
        $query .= " AND HR_CLASS  = '" .$model->field["HR_CLASS"]."' ";
        $query .= " AND ACTION_S_DATE = '".str_replace("/","-",$model->field["ACTION_S_DATE"])."' ";
        
        $db2->query($query);

        //県側、学校側REPORT_DISEASE_ADDITION4_DAT削除（報告日で削除）
        $query  = " DELETE FROM  ";
        $query .= "     REPORT_DISEASE_ADDITION4_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= " AND YEAR      = '".$model->field["YEAR"]."' ";
        $query .= " AND DISEASECD = '".$model->field["DISEASECD"]."' ";
        $query .= " AND GRADE     = '" .$model->field["GRADE"]."' ";
        $query .= " AND HR_CLASS  = '" .$model->field["HR_CLASS"]."' ";
        $query .= " AND ACTION_S_DATE = '".str_replace("/","-",$model->field["ACTION_S_DATE"])."' ";
        $db->query($query);
        $db2->query($query);
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
        
    //県側MEDEXAM_DISEASE_ADDITION3_DAT追加 + 学校側報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
    function setAddtion4Dat($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $query = knjf305Query::getAddition4Dat($model, "LIST");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjf305Query::updateAddtion4Dat($model, $db, $db2, $row);
        }
        $result->free();
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        
        return ;
    }
        
    //県側　MEDEXAM_DISEASE_ADDITION4_DAT追加、学校側　報告テーブル（REPORT_DISEASE_ADDITION4_DAT）追加
    function updateAddtion4Dat($model, $db, $db2, $row) {
        //県側　MEDEXAM_DISEASE_ADDITION4_DAT追加
        $data["EDBOARD_SCHOOLCD"][TEXT]      = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]                  = $row["YEAR"];
        $data["DISEASECD"][TEXT]             = $row["DISEASECD"];
        $data["GRADE"][TEXT]                 = $row["GRADE"];
        $data["HR_CLASS"][TEXT]              = $row["HR_CLASS"];
        $data["ACTION_S_DATE"][TEXT]         = $row["ACTION_S_DATE"];
        $data["ACTION_E_DATE"][TEXT]         = $row["ACTION_E_DATE"];
        $data["PATIENT_COUNT"][NUMBER]       = $row["PATIENT_COUNT"];
        $data["ABSENCE_COUNT"][NUMBER]       = $row["ABSENCE_COUNT"];
        $data["PRESENCE_COUNT"][NUMBER]      = $row["PRESENCE_COUNT"];
        $data["SYMPTOM01"][TEXT]             = $row["SYMPTOM01"];
        $data["SYMPTOM01_REMARK"][TEXT]      = $row["SYMPTOM01_REMARK"];
        $data["SYMPTOM02"][TEXT]             = $row["SYMPTOM02"];
        $data["SYMPTOM03"][TEXT]             = $row["SYMPTOM03"];
        $data["SYMPTOM04"][TEXT]             = $row["SYMPTOM04"];
        $data["SYMPTOM05"][TEXT]             = $row["SYMPTOM05"];
        $data["SYMPTOM06"][TEXT]             = $row["SYMPTOM06"];
        $data["SYMPTOM07"][TEXT]             = $row["SYMPTOM07"];
        $data["SYMPTOM08"][TEXT]             = $row["SYMPTOM08"];
        $data["SYMPTOM09"][TEXT]             = $row["SYMPTOM09"];
        $data["SYMPTOM10"][TEXT]             = $row["SYMPTOM10"];
        $data["SYMPTOM11"][TEXT]             = $row["SYMPTOM11"];
        $data["SYMPTOM12"][TEXT]             = $row["SYMPTOM12"];
        $data["SYMPTOM12_REMARK"][TEXT]      = $row["SYMPTOM12_REMARK"];
        $data["REMARK"][TEXT]                = $row["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD ;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION4_DAT");
        $db2->query($query);
        
        //県側、学校側　報告テーブル（REPORT_DISEASE_ADDITION4_DAT）追加
        $data2["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
        $data2["YEAR"][TEXT]                 = $row["YEAR"];
        $data2["DISEASECD"][TEXT]            = $row["DISEASECD"];
        $data2["GRADE"][TEXT]                = $row["GRADE"];
        $data2["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
        $data2["ACTION_S_DATE"][TEXT]        = $row["ACTION_S_DATE"];
        $data2["EXECUTE_DATE"][TEXT]         = str_replace("/","-",$model->field["EXECUTE_DATE"]);
        $data2["REGISTERCD"][TEXT]    = STAFFCD ;
        $data2["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data2, "REPORT_DISEASE_ADDITION4_DAT");
        $db->query($query);
        $db2->query($query);
        
        return;
    }

}
?>
