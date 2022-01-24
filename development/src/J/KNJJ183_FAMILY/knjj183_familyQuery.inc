<?php

require_once('for_php7.php');

class knjj183_familyQuery extends Query {
    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($model) {
        $query  =" SELECT ";
        $query .="     SCHREGNO, ";
        $query .="     NAME_SHOW AS NAME ";
        $query .=" FROM ";
        $query .="     SCHREG_BASE_MST ";
        $query .=" WHERE ";
        $query .="     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //家族番号取得
    function getFamilyNo($model) {
        $query  = " SELECT ";
        $query .= "     BASE_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND BASE_SEQ = '009' ";

        return $query;
    }

    //学籍住所データよりデータを取得
    function getAddress_all($model) {
        if ($model->Properties["useFamilyDat"] == '1') {
            $query  = " SELECT ";
            $query .= "     FAMILY_NO, ";
            $query .= "     RELANO          AS RELA_NO, ";
            $query .= "     RELANAME        AS RELA_NAME, ";
            $query .= "     RELAKANA        AS RELA_KANA, ";
            $query .= "     RELASEX         AS RELA_SEX, ";
            $query .= "     RELABIRTHDAY    AS RELA_BIRTHDAY, ";
            $query .= "     RELATIONSHIP    AS RELA_RELATIONSHIP, ";
            $query .= "     OCCUPATION      AS RELA_OCCUPATION, ";
            $query .= "     RELA_SCHREGNO, ";
            $query .= "     REGD_GRD_FLG, ";
            $query .= "     RELA_GRADE, ";
            $query .= "     TYOUSHI_FLG, ";
            $query .= "     REGIDENTIALCD   AS RELA_REGIDENTIALCD, ";
            $query .= "     REMARK          AS RELA_REMARK ";
            $query .= " FROM ";
            $query .= "     FAMILY_DAT ";
            $query .= " WHERE ";
            $query .= "     FAMILY_NO = '{$model->familyNo}' ";
            $query .= " ORDER BY ";
            $query .= "     CAST(RELANO AS INTEGER) ";
        } else {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     RELANO          AS RELA_NO, ";
            $query .= "     RELANAME        AS RELA_NAME, ";
            $query .= "     RELAKANA        AS RELA_KANA, ";
            $query .= "     RELASEX         AS RELA_SEX, ";
            $query .= "     RELABIRTHDAY    AS RELA_BIRTHDAY, ";
            $query .= "     RELATIONSHIP    AS RELA_RELATIONSHIP, ";
            $query .= "     OCCUPATION      AS RELA_OCCUPATION, ";
            $query .= "     RELA_SCHREGNO, ";
            $query .= "     REGD_GRD_FLG, ";
            $query .= "     RELA_GRADE, ";
            $query .= "     REGIDENTIALCD   AS RELA_REGIDENTIALCD, ";
            $query .= "     REMARK          AS RELA_REMARK ";
            $query .= " FROM ";
            $query .= "     SCHREG_RELA_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     CAST(RELANO AS INTEGER) ";
        }

        return $query;
    }

    //卒業した兄弟姉妹取得
    function getGrdBrother($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     L1.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A003' AND L1.NAMECD2 = T1.GRD_DIV ";
        $query .= " WHERE ";
        $query .= "     GRD_DIV IS NOT NULL AND ";
        $query .= "     GRD_DIV != '4' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     RELA_SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_RELA_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHREGNO = '".$model->schregno."' ";
        $query .= "                 ) ";

        return $query;
    }

    //家族情報の取得
    function getFamilyDat($model, $rela_no) {
        $query  = " SELECT ";
        $query .= "     RELANO          AS RELA_NO, ";
        $query .= "     RELANAME        AS RELA_NAME, ";
        $query .= "     RELAKANA        AS RELA_KANA, ";
        $query .= "     RELASEX         AS RELA_SEX, ";
        $query .= "     RELABIRTHDAY    AS RELA_BIRTHDAY, ";
        $query .= "     RELATIONSHIP    AS RELA_RELATIONSHIP, ";
        $query .= "     OCCUPATION      AS RELA_OCCUPATION, ";
        $query .= "     RELA_SCHREGNO, ";
        $query .= "     REGD_GRD_FLG, ";
        $query .= "     RELA_GRADE, ";
        $query .= "     REGIDENTIALCD   AS RELA_REGIDENTIALCD, ";
        $query .= "     TYOUSHI_FLG, ";
        $query .= "     REMARK          AS RELA_REMARK, ";
        $query .= "     UPDATED         AS RELA_UPDATED ";
        $query .= " FROM ";
        $query .= "     FAMILY_DAT ";
        $query .= " WHERE ";
        $query .= "     FAMILY_NO  = '{$model->familyNo}' ";
        if (strlen($rela_no)) {
            $query .= "     AND RELANO = '{$rela_no}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RELANO ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //名称マスタから取得
    function get_name_mst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 IN ('Z002','H201','H202','A020','H200') AND ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年一覧取得
    function getSchregRegdGdat($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //家族番号取得
    function getMaxFamilyNo($model) {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     MAX(BASE_REMARK1) AS MAX_FAMILY_NO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     BASE_SEQ = '009' ";

        $retFamily = $db->getOne($query);
        $retFamily = $retFamily ? $retFamily + 1 : "900000001";
        Query::dbCheckIn($db);
        return $retFamily;
    }

    //自身の家族データ登録用データ取得
    function getSetFamilyData($model) {
        $db = Query::dbCheckOut();
        $query  = "";
        $query .= "SELECT DISTINCT";
        $query .= "    SM.SCHREGNO, ";
        $query .= "    SM.NAME, ";
        $query .= "    SM.NAME_KANA, ";
        $query .= "    SM.SEX, ";
        $query .= "    SM.BIRTHDAY, ";
        $query .= "    SD.GRADE, ";
        $query .= "    SD.HR_CLASS, ";
        $query .= "    HR_NAME AS GRD_CLS, ";
        $query .= "    G1.GUARD_TELNO AS GUARD_TELNO, ";
        $query .= "    SD.ATTENDNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST  SM ";
        $query .= "    LEFT JOIN GUARDIAN_DAT  G1 ON SM.SCHREGNO = G1.SCHREGNO ";
        if ($model->Properties["useGuardian2"] == '1') {
            $query .= "    LEFT JOIN GUARDIAN2_DAT G2 ON SM.SCHREGNO = G2.SCHREGNO ";
        }
        $query .= "    LEFT JOIN SCHREG_BASE_DETAIL_MST BM ON SM.SCHREGNO = BM.SCHREGNO ";
        $query .= "                                       AND BM.BASE_SEQ = '009', ";
        $query .= "        SCHREG_REGD_DAT  SD, ";
        $query .= "        SCHREG_REGD_HDAT SH ";
        $query .= "WHERE ";
        $query .= "        SM.SCHREGNO = SD.SCHREGNO ";
        $query .= "    AND SD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND SD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND SH.YEAR     = SD.YEAR ";
        $query .= "    AND SH.SEMESTER = SD.SEMESTER ";
        $query .= "    AND SH.GRADE    = SD.GRADE ";
        $query .= "    AND SH.HR_CLASS = SD.HR_CLASS ";
        $query .= " AND SM.SCHREGNO = '{$model->schregno}' ";

        $result = $db->query($query);
        $retData = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retData["RELA_NAME"]       = $row["NAME"];
            $retData["RELA_KANA"]       = $row["NAME_KANA"];
            $retData["RELA_SEX"]        = $row["SEX"];
            $retData["RELA_BIRTHDAY"]   = str_replace("/", "-", $row["BIRTHDAY"]);
            $retData["RELA_SCHREGNO"]   = $row["SCHREGNO"];
            $retData["REGD_GRD_FLG"]    = "1";
            $retData["RELA_GRADE"]      = $row["GRADE"];
        }

        Query::dbCheckIn($db);
        return $retData;
    }

    //家族情報データ・連番の最大値を取得
    function getFamily_Maxno($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(INT(RELANO)) AS MAX ";
        $query .= " FROM ";
        $query .= "     FAMILY_DAT ";
        $query .= " WHERE ";
        $query .= "     FAMILY_NO = '".$model->familyNo."' ";
        $rela_max = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rela_max;
    }

    //家族情報データ追加
    function insertFamilyData($model, $fields, $rela_no) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if ($model->cmd == "apply") {
            $query = "SELECT COUNT(*) AS CNT FROM FAMILY_DAT WHERE FAMILY_NO = '{$model->familyNo}' AND RELA_SCHREGNO = '{$fields["RELA_SCHREGNO"]}'";
            $dataCnt = $db->getOne($query);
            if ($dataCnt > 0) {
                $db->commit(); // トランザクションをコミットする。
                Query::dbCheckIn($db);
                return;
            }
        }

        $data = array();
        $data["FAMILY_NO"][TEXT]            = $model->familyNo;
        $data["RELANO"][TEXT]               = $rela_no;
        $data["RELANAME"][TEXT]             = $fields["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $fields["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $fields["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/","-",$fields["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $fields["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $fields["RELA_OCCUPATION"];
        if ($model->cmd == "apply") {
            $data["RELA_SCHREGNO"][TEXT]        = $fields["RELA_SCHREGNO"];
        }
        $data["REGD_GRD_FLG"][TEXT]         = $fields["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]           = $fields["RELA_GRADE"];
        $data["REGIDENTIALCD"][TEXT]        = $fields["RELA_REGIDENTIALCD"];
        $data["TYOUSHI_FLG"][TEXT]          = $fields["TYOUSHI_FLG"];
        $data["REMARK"][TEXT]               = $fields["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data,"FAMILY_DAT");
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ更新
    function updateFamilyData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/","-",$model->field["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $model->field["RELA_OCCUPATION"];
        $data["RELA_SCHREGNO"][TEXT]        = $model->field["RELA_SCHREGNO"];
        $data["REGD_GRD_FLG"][TEXT]         = $model->field["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]           = $model->field["RELA_GRADE"];
        $data["REGIDENTIALCD"][TEXT]        = $model->field["RELA_REGIDENTIALCD"];
        $data["TYOUSHI_FLG"][TEXT]          = $model->field["TYOUSHI_FLG"];
        $data["REMARK"][TEXT]               = $model->field["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE FAMILY_NO = '{$model->familyNo}' AND RELANO = '".$model->rela_no."' ";
        $query = Query::updateSQL($data,"FAMILY_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ削除
    function deleteFamilyData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM FAMILY_DAT WHERE FAMILY_NO = '{$model->familyNo}' AND RELANO = '".$model->rela_no."' ";
        $db->query($query);

        $query  = " DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '{$model->field["RELA_SCHREGNO"]}' AND BASE_SEQ = '009' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //家族番号
    function addSchregDetail009($model, $schregNo, $familyNo) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '{$schregNo}' AND BASE_SEQ = '009' ";
        $db->query($query);

        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["BASE_SEQ"][TEXT]         = "009";
        $data["BASE_REMARK1"][TEXT]     = $familyNo;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
