<?php

require_once('for_php7.php');

class knjl018aQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験種別コンボ
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度一覧
    function getNendo() {
        $query .= " select distinct ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE";
        $query .= " from ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //生徒情報
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "    T1.RECEPTNO AS VALUE, ";
        $query .= "    T1.RECEPTNO, ";
        $query .= "    T1.RECEPTNO || ' - ' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    ENTEXAM_RECEPT_DAT T1 ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "      ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "      ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "      AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "      AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "      AND T3.SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     T1.EXAM_TYPE = '".$model->exam_type."' AND ";
        $query .= "    (COALESCE(T3.REMARK1, '') != '".$model->preischoolcd."' OR COALESCE(T3.REMARK3, '') != '".$model->preischoolClassCd."' ";
        $query .= "        AND COALESCE(T3.REMARK4, '') != '".$model->preischoolcd."' OR COALESCE(T3.REMARK5, '') != '".$model->preischoolClassCd."' ";
        $query .= "        AND COALESCE(T3.REMARK6, '') != '".$model->preischoolcd."' OR COALESCE(T3.REMARK7, '') != '".$model->preischoolClassCd."') ";
        $query .= "    AND ( ";
        $query .= "      (T3.REMARK1 IS NULL AND T3.REMARK3 IS NULL) ";
        $query .= "        OR (T3.REMARK4 IS NULL AND T3.REMARK5 IS NULL) ";
        $query .= "        OR (T3.REMARK6 IS NULL AND T3.REMARK7 IS NULL) ";
        $query .= "    ) ";

        return $query;
    }

    //左側のリストの生徒情報
    function getSelectStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.RECEPTNO AS VALUE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.RECEPTNO || ' - ' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "       AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "       AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "       ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "       AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "       AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "       AND T3.SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     T1.EXAM_TYPE = '".$model->exam_type."' AND ";
        $query .= "     (COALESCE(T3.REMARK1, '') = '".$model->preischoolcd."' AND COALESCE(T3.REMARK3, '') = '".$model->preischoolClassCd."' ";
        $query .= "     OR COALESCE(T3.REMARK4, '') = '".$model->preischoolcd."' AND COALESCE(T3.REMARK5, '') = '".$model->preischoolClassCd."' ";
        $query .= "     OR COALESCE(T3.REMARK6, '') = '".$model->preischoolcd."' AND COALESCE(T3.REMARK7, '') = '".$model->preischoolClassCd."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";

        return $query;
    }

    //塾名
    function getPriSchoolName($priSchoolCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
        
        return $query;
    }

    //登録チェック
    function chkDataQuery($model, $param) {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ANOTHER_PRISCHOOL_HIST_DAT ";
        $query .= " WHERE SCHREGNO = '" . $param['SCHREGNO'] . "'";
        $query .= "   AND SCHOOL_KIND = '" . $param['SCHOOL_KIND'] . "' ";
        $query .= "   AND PRISCHOOLCD = '" . $param['PRISCHOOLCD'] . "' ";
        $query .= "   AND PRISCHOOL_CLASS_CD = '" . $param['PRISCHOOL_CLASS_CD'] . "' ";
        $query .= "   AND REGISTERCD = '" . $param['REGISTERCD'] . "' ";

        $retval =$db->getOne($query);
        Query::dbCheckIn($db);
        return $retval;
    }

    //新規挿入
    function insertQuery($model, $schregNo) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["PRISCHOOLCD"][TEXT]          = $model->preischoolcd;
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->preischoolClassCd;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ANOTHER_PRISCHOOL_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //削除
    function deleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " UPDATE ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " SET REMARK1 = NULL, ";
        $query .= "     REMARK3 = NULL ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND SEQ = '008' ";
        $query .= "   AND REMARK1 = '{$model->preischoolcd}' AND REMARK3 = '{$model->preischoolClassCd}' ";

        $db->query($query);

        $query  = " UPDATE ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " SET REMARK4 = NULL, ";
        $query .= "     REMARK5 = NULL ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND SEQ = '008' ";
        $query .= "   AND REMARK4 = '{$model->preischoolcd}' AND REMARK5 = '{$model->preischoolClassCd}' ";

        $db->query($query);

        $query  = " UPDATE ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " SET REMARK6 = NULL, ";
        $query .= "     REMARK7 = NULL ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND SEQ = '008' ";
        $query .= "   AND REMARK6 = '{$model->preischoolcd}' AND REMARK7 = '{$model->preischoolClassCd}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    
    function checkUpdateBseDatil($model, $arryreceptno) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     CASE WHEN W1.EXAMNO IS NULL THEN -1 ";
        $query .= "          WHEN W1.REMARK1 IS NULL THEN 1 ";
        $query .= "          WHEN W1.REMARK4 IS NULL THEN 2 ";
        $query .= "          WHEN W1.REMARK6 IS NULL THEN 3 ";
        $query .= "          ELSE 0 END AS SPACE_CD ";
        $query .= " FROM  ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT W1 ";
        $query .= "      ON W1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND W1.EXAMNO = T1.EXAMNO ";
        $query .= "     AND W1.SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE = '{$model->exam_type}' ";
        $query .= "   AND T1.RECEPTNO IN ('" .implode($arryreceptno, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "   T1.RECEPTNO";

        $result = $db->query($query);
        $ret = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $rcrd = array();
            foreach ($row as $key => $val) {
                $rcrd[$key] = $val;
            }
            $ret[] = $rcrd;
        }
        
        Query::dbCheckIn($db);
        
        return $ret;
    }

    function insertBaseDetailDat($model, $examno){
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $examno;
        $data["SEQ"][TEXT]          = '008';
        $data["REMARK1"][TEXT]      = $model->preischoolcd;
        $data["REMARK2"][TEXT]      = "";
        $data["REMARK3"][TEXT]      = $model->preischoolClassCd;
        $data["REMARK4"][TEXT]      = "";
        $data["REMARK5"][TEXT]      = "";
        $data["REMARK6"][TEXT]      = "";
        $data["REMARK7"][TEXT]      = "";
        $data["REMARK8"][TEXT]      = "";
        $data["REMARK9"][TEXT]      = "";
        $data["REMARK10"][TEXT]     = "";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    function updateBaseDetailDat($model, $examno, $upd_no){
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();

        if ($upd_no == 1) {
            $data["REMARK1"][TEXT]      = $model->preischoolcd;
            $data["REMARK3"][TEXT]      = $model->preischoolClassCd;
        } else if ($upd_no == 2) {
            $data["REMARK4"][TEXT]      = $model->preischoolcd;
            $data["REMARK5"][TEXT]      = $model->preischoolClassCd;
        } else if ($upd_no == 3) {
            $data["REMARK6"][TEXT]      = $model->preischoolcd;
            $data["REMARK7"][TEXT]      = $model->preischoolClassCd;
        } else {
            return false;
        }
        $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
        $where .= " AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= " AND EXAMNO = '{$examno}' ";
        $where .= " AND SEQ = '008' ";
        
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
    
    function chkPriSchCode($model){
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ret = false;
        $row = array();
        
        $query = " SELECT ";
        $query .= "    M1.PRISCHOOLCD, ";
        $query .= "    CASE WHEN M2.PRISCHOOL_CLASS_CD IS NULL THEN '       ' ELSE M2.PRISCHOOL_CLASS_CD END AS PRISCHOOL_CLASS_CD ";
        $query .= " FROM ";
        $query .= "   PRISCHOOL_MST M1 ";
        $query .= "   LEFT JOIN PRISCHOOL_CLASS_MST M2 ";
        $query .= "     ON M2.PRISCHOOLCD = M1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "   M1.PRISCHOOLCD = '{$model->preischoolcd}' ";
        //UTF-8の半角文字変換した上で、trimする。
        if (trim(str_replace( "\xc2\xa0", " ", $model->preischoolClassCd )) != "") {
            $query .= "   AND M2.PRISCHOOL_CLASS_CD = '{$model->preischoolClassCd}' ";
        } else {
            $query .= "   AND M2.PRISCHOOL_CLASS_CD IS NULL ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (get_count($row) > 0 )  {
            $ret = true;
        }

        Query::dbCheckIn($db);
        return $ret;
    }

}
?>