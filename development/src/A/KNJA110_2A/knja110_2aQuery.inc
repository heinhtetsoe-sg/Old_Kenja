<?php

require_once('for_php7.php');

class knja110_2aQuery extends Query
{

    //起動チェック
    public function checkToStart($db)
    {
        foreach (array('Z002','H201','A020') as $val) { //性別、続柄、地区がマスタに存在するかチェック！
            $query = "SELECT COUNT(*) AS a FROM v_name_mst WHERE namecd1 = '".$val."' AND year = '".CTRL_YEAR."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    public function getSchregnoName($model)
    {
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$model->schregno."'";
        return $query;
    }

    //家族番号取得
    public function getFamilyNo($model)
    {
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
    public function getAddressAll($model)
    {
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     ISSUEDATE  AS GUARD_ISSUEDATE, ";
            $query .= "     EXPIREDATE AS GUARD_EXPIREDATE, ";
            $query .= "     GUARD_ZIPCD, ";
            $query .= "     GUARD_ADDR1, ";
            $query .= "     GUARD_ADDR2, ";
            $query .= "     GUARD_ADDR_FLG, ";
            $query .= "     GUARD_TELNO, ";
            $query .= "     GUARD_TELNO2, ";
            $query .= "     GUARD_FAXNO, ";
            $query .= "     GUARD_E_MAIL ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        } elseif ($model->infoDiv == "4") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     ISSUEDATE  AS GUARANTOR_ISSUEDATE, ";
            $query .= "     EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
            $query .= "     GUARANTOR_ZIPCD, ";
            $query .= "     GUARANTOR_ADDR1, ";
            $query .= "     GUARANTOR_ADDR2, ";
            $query .= "     GUARANTOR_ADDR_FLG, ";
            $query .= "     GUARANTOR_TELNO ";
            $query .= " FROM ";
            $query .= "     GUARANTOR_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        } elseif ($model->infoDiv == "5") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     DIV AS SEND_DIV, ";
            $query .= "     SEND_RELATIONSHIP, ";
            $query .= "     SEND_NAME, ";
            $query .= "     SEND_KANA, ";
            $query .= "     SEND_SEX, ";
            $query .= "     SEND_ZIPCD, ";
            $query .= "     SEND_AREACD, ";
            $query .= "     SEND_ADDR1, ";
            $query .= "     SEND_ADDR2, ";
            $query .= "     SEND_ADDR_FLG, ";
            $query .= "     SEND_TELNO, ";
            $query .= "     SEND_TELNO2, ";
            $query .= "     SEND_JOBCD, ";
            $query .= "     PUBLIC_OFFICE AS SEND_PUBLIC_OFFICE ";
            $query .= " FROM ";
            $query .= "     SCHREG_SEND_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     DIV ";
        } elseif ($model->Properties["useFamilyDat"] == '1' && $model->infoDiv == "6") {
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
        } elseif ($model->infoDiv == "6") {
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
        } else {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCHREG_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        }

        return $query;
    }

    //卒業した兄弟姉妹取得
    public function getGrdBrother($model)
    {
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

    //最新の有効開始日付を取得
    public function getMaxIssuedate($model)
    {
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $table = " GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        } elseif ($model->infoDiv == "4") {
            $table = " GUARANTOR_ADDRESS_DAT ";
        } else {
            $table = " SCHREG_ADDRESS_DAT ";
        }

        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }


    //listの地区コード表示
    public function listAreaCd()
    {
        return "SELECT * FROM v_name_mst WHERE namecd1 = 'A020' AND year = '".CTRL_YEAR."'";
    }

    //備忘録
    public function getDetail009R2($schregno)
    {
        $query  = " SELECT ";
        $query .= "     BASE_REMARK2 AS DETAIL_009_R2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= "     AND BASE_SEQ = '009' ";

        return $query;
    }

    //期間開始日付のリンクをクリックした学籍住所データを単票形式で表示
    public function getRowAddress($schregno, $issuedate)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        if (strlen($issuedate)) {
            $query .= "     AND ISSUEDATE = '" . str_replace("/", "-", $issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    public function getCntAddress($schregno, $issuedate)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $issuedate) . "' ";

        return $query;
    }

    public function getCntGAddress($model, $schregno, $guard_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    //保証人住所データ取得
    public function getGuarantorAddressDat($schregno, $issuedate, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ISSUEDATE   = '".str_replace("/", "-", $issuedate)."' ";

        return $query;
    }

    //その他住所データ取得
    public function getSchregSendAddressDat($schregno, $send_div, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DIV         = '".$send_div."' ";

        return $query;
    }

    public function getGuardianAddressDat($model, $schregno, $guard_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    //保護者情報の取得
    public function getGuardianAddr($model, $schregno, $guard_issuedate, $flg = "")
    {
        $guardian2 = "";
        if ($flg == "copy") {
            //コピーボタン:Hiddenで保持用
            $guardian2 = ($model->infoDiv == "2") ? "2" : "";
        } elseif ($flg == "copy2") {
            //コピーボタン:Hiddenで保持用
            $guardian2 = "2";
        } else {
            //データ表示:通常
            $guardian2 = $model->guardian2;
        }

        $query  = " SELECT ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     L1.GUARD_ZIPCD, ";
        $query .= "     L1.GUARD_ADDR1, ";
        $query .= "     L1.GUARD_ADDR2, ";
        $query .= "     L1.GUARD_ADDR_FLG, ";
        $query .= "     L1.GUARD_TELNO, ";
        $query .= "     L1.GUARD_TELNO2, ";
        $query .= "     L1.GUARD_FAXNO, ";
        $query .= "     L1.GUARD_E_MAIL, ";
        $query .= "     T1.GUARD_JOBCD, ";
        $query .= "     T1.GUARD_WORK_NAME, ";
        $query .= "     T1.GUARD_WORK_TELNO, ";
        $query .= "     L1.ISSUEDATE AS GUARD_ISSUEDATE, ";
        $query .= "     L1.EXPIREDATE AS GUARD_EXPIREDATE, ";
        $query .= "     L1.UPDATED AS GUARD_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$guardian2}_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN{$guardian2}_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($guard_issuedate)) {
            $query .= "     AND L1.ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //保証人情報の取得
    public function getGuarantorAddr($model, $schregno, $issuedate)
    {
        $query  = " SELECT ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T1.GUARANTOR_NAME, ";
        $query .= "     T1.GUARANTOR_KANA, ";
        $query .= "     T1.GUARANTOR_REAL_NAME, ";
        $query .= "     T1.GUARANTOR_REAL_KANA, ";
        $query .= "     T1.GUARANTOR_SEX, ";
        $query .= "     L1.GUARANTOR_ZIPCD, ";
        $query .= "     L1.GUARANTOR_ADDR1, ";
        $query .= "     L1.GUARANTOR_ADDR2, ";
        $query .= "     L1.GUARANTOR_ADDR_FLG, ";
        $query .= "     L1.GUARANTOR_TELNO, ";
        $query .= "     T1.GUARANTOR_JOBCD, ";
        $query .= "     T1.PUBLIC_OFFICE, ";
        $query .= "     L1.ISSUEDATE AS GUARANTOR_ISSUEDATE, ";
        $query .= "     L1.EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
        $query .= "     L1.UPDATED AS GUARANTOR_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARANTOR_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($issuedate)) {
            $query .= "     AND L1.ISSUEDATE = '".str_replace("/", "-", $issuedate)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //その他情報の取得
    public function getSendAddr($model, $schregno, $send_div)
    {
        $query  = " SELECT ";
        $query .= "     DIV AS SEND_DIV, ";
        $query .= "     SEND_RELATIONSHIP, ";
        $query .= "     SEND_NAME, ";
        $query .= "     SEND_KANA, ";
        $query .= "     SEND_SEX, ";
        $query .= "     SEND_ZIPCD, ";
        $query .= "     SEND_AREACD, ";
        $query .= "     SEND_ADDR1, ";
        $query .= "     SEND_ADDR2, ";
        $query .= "     SEND_ADDR_FLG, ";
        $query .= "     SEND_TELNO, ";
        $query .= "     SEND_TELNO2, ";
        $query .= "     SEND_JOBCD, ";
        $query .= "     PUBLIC_OFFICE AS SEND_PUBLIC_OFFICE, ";
        $query .= "     UPDATED AS SEND_UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";
        if (strlen($send_div)) {
            $query .= "     AND DIV = '".$send_div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     DIV ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //家族情報の取得
    public function getRelaDat($model, $schregno, $rela_no)
    {
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
        $query .= "     REMARK          AS RELA_REMARK, ";
        $query .= "     UPDATED         AS RELA_UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";
        if (strlen($rela_no)) {
            $query .= "     AND RELANO = '".$rela_no."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RELANO ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //家族情報の取得
    public function getFamilyDat($model, $rela_no)
    {
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

    //家族情報の取得（確定ボタン）
    public function getSchDataKakutei($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE_M.SCHREGNO   AS RELA_SCHREGNO, ";
        $query .= "     BASE_M.NAME       AS RELA_NAME, ";
        $query .= "     BASE_M.NAME_KANA  AS RELA_KANA, ";
        $query .= "     BASE_M.SEX        AS RELA_SEX, ";
        $query .= "     BASE_M.BIRTHDAY   AS RELA_BIRTHDAY, ";
        $query .= "     '1'               AS REGD_GRD_FLG, ";
        $query .= "     REGD_D.GRADE      AS RELA_GRADE";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE_M ";
        $query .= " JOIN ";
        $query .= "     SCHREG_REGD_DAT REGD_D ON REGD_D.SCHREGNO = BASE_M.SCHREGNO ";
        $query .= "                           AND REGD_D.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                           AND REGD_D.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     BASE_M.SCHREGNO = '{$model->field["RELA_SCHREGNO"]}' ";

        return $query;
    }

    //親族データを挿入
    public function addSchregFamilyDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja110_2aQuery::getDetail009R2($model->stucd);
        $detail009R2 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = "SELECT BASE_REMARK1 FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '{$model->stucd}' AND BASE_SEQ = '009' ";
        $setFamilyNo = $db->getOne($query);

        if (is_array($detail009R2)) {
            $data["BASE_REMARK1"][TEXT]     = $setFamilyNo;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '{$model->stucd}' ";
            $where .= "     AND BASE_SEQ = '009' ";

            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $data["SCHREGNO"][TEXT]         = $model->stucd;
            $data["BASE_SEQ"][TEXT]         = "009";
            $data["BASE_REMARK1"][TEXT]     = $setFamilyNo;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return $setFamilyNo;
    }

    //名称マスタから取得
    public function getNNMst()
    {
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
    public function getSchregRegdGdat($model)
    {
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

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     ISSUEDATE, ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        list($last_issuedate, $last_expiredate) = $db->getRow($query);
        if ($last_issuedate) {
            $issuedate = str_replace('-', '', $model->field["ISSUEDATE"]);
            $last_day = date("Y-m-d", strtotime("-1 day", strtotime($issuedate)));

            $data = array();
            $data["EXPIREDATE"][TEXT]     = $last_day;

            $where  = "WHERE SCHREGNO  = '{$model->schregno}'";
            $where .= "  AND ISSUEDATE = '{$last_issuedate}'";

            $query = Query::updateSQL($data, "SCHREG_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["ISSUEDATE"][TEXT]      = $model->field["ISSUEDATE"];
        $data["EXPIREDATE"][TEXT]     = $model->field["EXPIREDATE"];
        $data["ZIPCD"][TEXT]          = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]         = $model->field["AREACD"];
        $data["ADDR1"][TEXT]          = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]          = $model->field["ADDR2"];
        $data["ADDR_FLG"][TEXT]       = $model->field["ADDR_FLG"];
        $data["ADDR1_ENG"][TEXT]      = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]      = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]          = $model->field["TELNO"];
        $data["TELNO_MEMO"][TEXT]     = $model->field["TELNO_MEMO"];
        $data["TELNO2"][TEXT]         = $model->field["TELNO2"];
        $data["TELNO2_MEMO"][TEXT]    = $model->field["TELNO2_MEMO"];
        $data["FAXNO"][TEXT]          = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]          = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "schreg_address_dat");
        $db->query($query);

        //急用連絡先を更新
        knja110_2aQuery::updateEmergencyInfo($db, $model, "update");

        //SCHREG_BASE_DETAIL_MST(SEQ:012)更新
        knja110_2aQuery::updateSchregBaseDetailMst($db, $model, '012');

        //SCHREG_BASE_DETAIL_MST(SEQ:013)更新
        knja110_2aQuery::updateSchregBaseDetailMst($db, $model, '013');

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateModel($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["EXPIREDATE"]."')";
        $data["ZIPCD"][TEXT]                = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]               = $model->field["AREACD"];
        $data["ADDR1"][TEXT]                = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                = $model->field["ADDR2"];
        $data["ADDR_FLG"][TEXT]             = $model->field["ADDR_FLG"];
        $data["ADDR1_ENG"][TEXT]            = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]            = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]                = $model->field["TELNO"];
        $data["TELNO_MEMO"][TEXT]           = $model->field["TELNO_MEMO"];
        $data["TELNO2"][TEXT]               = $model->field["TELNO2"];
        $data["TELNO2_MEMO"][TEXT]          = $model->field["TELNO2_MEMO"];
        $data["FAXNO"][TEXT]                = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]                = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = "WHERE SCHREGNO  = '".$model->schregno."'";
        $where .= "  AND ISSUEDATE = date('".str_replace("/", "-", $model->issuedate)."')";

        $query = Query::updateSQL($data, "SCHREG_ADDRESS_DAT", $where);
        $db->query($query);

        //急用連絡先を更新(2005/10/20 追加)
        knja110_2aQuery::updateEmergencyInfo($db, $model, "update");

        //SCHREG_BASE_DETAIL_MST(SEQ:012)更新
        knja110_2aQuery::updateSchregBaseDetailMst($db, $model, '012');

        //SCHREG_BASE_DETAIL_MST(SEQ:013)更新
        knja110_2aQuery::updateSchregBaseDetailMst($db, $model, '013');

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //保護者データをインサート
    public function insertGuardiandat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knja110_2aQuery::makeGuardianHistDat($model, $db);

        //GUARDIAN_ADDRESS_DAT の追加
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        $max_issuedate = $db->getOne($query);
        if ($max_issuedate) {
            //現在ある最後の終了有効期間を入力のあった開始日付の前日とする
            $to_day = str_replace('-', '', $model->field["GUARD_ISSUEDATE"]);
            $last_day = date("Y-m-d", strtotime("-1 day", strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["ISSUEDATE"][FUNC]      = "date('".$model->field["GUARD_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]     = "date('".$model->field["GUARD_EXPIREDATE"]."')";
        $data["GUARD_ZIPCD"][TEXT]    = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]    = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]    = $model->field["GUARD_ADDR2"];
        $data["GUARD_ADDR_FLG"][TEXT] = $model->field["GUARD_ADDR_FLG"];
        $data["GUARD_TELNO"][TEXT]    = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]   = $model->field["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]    = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]   = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_ADDRESS_DAT");
        $db->query($query);

        // 保護者情報
        $query = knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]      = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]  = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT] = $model->field["GUARD_WORK_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保護者データをアップデート
    public function updateGuardianDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knja110_2aQuery::makeGuardianHistDat($model, $db);

        $data = array();
        $data["ISSUEDATE"][FUNC]        = "date('".$model->field["GUARD_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]       = "date('".$model->field["GUARD_EXPIREDATE"]."')";
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_ADDR_FLG"][TEXT]   = $model->field["GUARD_ADDR_FLG"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $model->field["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guard_issuedate)."')";

        $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_ADDRESS_DAT", $where);
        $db->query($query);

        // 保護者情報
        $query = knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]      = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]  = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT] = $model->field["GUARD_WORK_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保証人住所データ追加
    public function insertGuarantorData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARANTOR_HIST_DAT更新
        knja110_2aQuery::makeGuarantorHistDat($model, $db);

        //GUARANTOR_ADDRESS_DATのMAX開始日付取得
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        $max_issuedate = $db->getOne($query);
        if ($max_issuedate) {
            //現在ある最後の終了有効期間を入力のあった開始日付の前日とする
            $to_day = str_replace('-', '', $model->field["GUARANTOR_ISSUEDATE"]);
            $last_day = date("Y-m-d", strtotime("-1 day", strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data, "GUARANTOR_ADDRESS_DAT", $where);
            $db->query($query);
        }

        //GUARANTOR_ADDRESS_DAT追加
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["GUARANTOR_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["GUARANTOR_EXPIREDATE"]."')";
        $data["GUARANTOR_ZIPCD"][TEXT]      = $model->field["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]      = $model->field["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]      = $model->field["GUARANTOR_ADDR2"];
        $data["GUARANTOR_ADDR_FLG"][TEXT]   = $model->field["GUARANTOR_ADDR_FLG"];
        $data["GUARANTOR_TELNO"][TEXT]      = $model->field["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "GUARANTOR_ADDRESS_DAT");
        $db->query($query);

        //GUARDIAN_DAT更新（保証人住所）
        $query = knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]  = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]    = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保証人住所データ更新
    public function updateGuarantorData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARANTOR_HIST_DAT更新
        knja110_2aQuery::makeGuarantorHistDat($model, $db);

        //GUARANTOR_ADDRESS_DAT更新
        $data = array();
        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["GUARANTOR_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["GUARANTOR_EXPIREDATE"]."')";
        $data["GUARANTOR_ZIPCD"][TEXT]      = $model->field["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]      = $model->field["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]      = $model->field["GUARANTOR_ADDR2"];
        $data["GUARANTOR_ADDR_FLG"][TEXT]   = $model->field["GUARANTOR_ADDR_FLG"];
        $data["GUARANTOR_TELNO"][TEXT]      = $model->field["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guarantor_issuedate)."')";
        $query = Query::updateSQL($data, "GUARANTOR_ADDRESS_DAT", $where);
        $db->query($query);

        //GUARDIAN_DAT更新（保証人住所）
        $query = knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]  = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]    = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //その他住所データ追加
    public function insertSendAddressData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["DIV"][TEXT]                  = $model->field["SEND_DIV"];
        $data["SEND_RELATIONSHIP"][TEXT]    = $model->field["SEND_RELATIONSHIP"];
        $data["SEND_NAME"][TEXT]            = $model->field["SEND_NAME"];
        $data["SEND_KANA"][TEXT]            = $model->field["SEND_KANA"];
        $data["SEND_SEX"][TEXT]             = $model->field["SEND_SEX"];
        $data["SEND_ZIPCD"][TEXT]           = $model->field["SEND_ZIPCD"];
        $data["SEND_AREACD"][TEXT]          = $model->field["SEND_AREACD"];
        $data["SEND_ADDR1"][TEXT]           = $model->field["SEND_ADDR1"];
        $data["SEND_ADDR2"][TEXT]           = $model->field["SEND_ADDR2"];
        $data["SEND_ADDR_FLG"][TEXT]        = $model->field["SEND_ADDR_FLG"];
        $data["SEND_TELNO"][TEXT]           = $model->field["SEND_TELNO"];
        $data["SEND_TELNO2"][TEXT]          = $model->field["SEND_TELNO2"];
        $data["SEND_JOBCD"][TEXT]           = $model->field["SEND_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]        = $model->field["SEND_PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_SEND_ADDRESS_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //その他住所データ更新
    public function updateSendAddressData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SEND_RELATIONSHIP"][TEXT]    = $model->field["SEND_RELATIONSHIP"];
        $data["SEND_NAME"][TEXT]            = $model->field["SEND_NAME"];
        $data["SEND_KANA"][TEXT]            = $model->field["SEND_KANA"];
        $data["SEND_SEX"][TEXT]             = $model->field["SEND_SEX"];
        $data["SEND_ZIPCD"][TEXT]           = $model->field["SEND_ZIPCD"];
        $data["SEND_AREACD"][TEXT]          = $model->field["SEND_AREACD"];
        $data["SEND_ADDR1"][TEXT]           = $model->field["SEND_ADDR1"];
        $data["SEND_ADDR2"][TEXT]           = $model->field["SEND_ADDR2"];
        $data["SEND_ADDR_FLG"][TEXT]        = $model->field["SEND_ADDR_FLG"];
        $data["SEND_TELNO"][TEXT]           = $model->field["SEND_TELNO"];
        $data["SEND_TELNO2"][TEXT]          = $model->field["SEND_TELNO2"];
        $data["SEND_JOBCD"][TEXT]           = $model->field["SEND_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]        = $model->field["SEND_PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND DIV = '".$model->send_div."' ";
        $query = Query::updateSQL($data, "SCHREG_SEND_ADDRESS_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ取得
    public function getSchregRelaDat($schregno, $rela_no, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     RELANO      = '".$rela_no."' ";

        return $query;
    }

    //家族情報データ・連番の最大値を取得
    public function getRelaMaxno($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(INT(RELANO)) AS MAX ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        $rela_max = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rela_max;
    }

    //家族情報データ追加
    public function insertRelaData($model, $rela_no)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["RELANO"][TEXT]               = $rela_no;
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/", "-", $model->field["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $model->field["RELA_OCCUPATION"];
        $data["RELA_SCHREGNO"][TEXT]        = $model->field["RELA_SCHREGNO"];
        $data["REGD_GRD_FLG"][TEXT]         = $model->field["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]           = $model->field["RELA_GRADE"];
        $data["REGIDENTIALCD"][TEXT]        = $model->field["RELA_REGIDENTIALCD"];
        $data["REMARK"][TEXT]               = $model->field["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_RELA_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ更新
    public function updateRelaData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/", "-", $model->field["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $model->field["RELA_OCCUPATION"];
        $data["RELA_SCHREGNO"][TEXT]        = $model->field["RELA_SCHREGNO"];
        $data["REGD_GRD_FLG"][TEXT]         = $model->field["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]           = $model->field["RELA_GRADE"];
        $data["REGIDENTIALCD"][TEXT]        = $model->field["RELA_REGIDENTIALCD"];
        $data["REMARK"][TEXT]               = $model->field["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND RELANO = '".$model->rela_no."' ";
        $query = Query::updateSQL($data, "SCHREG_RELA_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ削除
    public function deleteRelaData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHREG_RELA_DAT WHERE SCHREGNO = '{$model->schregno}' AND RELANO = '".$model->rela_no."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //家族番号取得
    public function getMaxFamilyNo($model)
    {
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
    public function getSetFamilyData($model)
    {
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
    public function getFamilyMaxno($model)
    {
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
    public function insertFamilyData($model, $fields, $rela_no)
    {
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
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/", "-", $fields["RELA_BIRTHDAY"]);
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

        $query = Query::insertSQL($data, "FAMILY_DAT");
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ更新
    public function updateFamilyData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/", "-", $model->field["RELA_BIRTHDAY"]);
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
        $query = Query::updateSQL($data, "FAMILY_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ削除
    public function deleteFamilyData($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM FAMILY_DAT WHERE FAMILY_NO = '{$model->familyNo}' AND RELANO = '".$model->rela_no."' ";
        $db->query($query);

        $data["BASE_REMARK1"][TEXT]     = null;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '{$model->schregno}' ";
        $where .= "     AND BASE_SEQ = '009' ";

        $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //家族番号
    public function addSchregDetail009($model, $schregNo, $familyNo)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja110_2aQuery::getDetail009R2($schregNo);
        $detail009R2 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($detail009R2)) {
            $data["BASE_REMARK1"][TEXT]     = $familyNo;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '{$schregNo}' ";
            $where .= "     AND BASE_SEQ = '009' ";

            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["BASE_SEQ"][TEXT]         = "009";
            $data["BASE_REMARK1"][TEXT]     = $familyNo;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //GUARANTOR_HIST_DAT更新
    public function makeGuarantorHistDat($model, $db)
    {
        $make_hist_flg = false;

        if ($model->field["GUARANTOR_RELATIONSHIP_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_NAME_FLG"]         == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_KANA_FLG"]         == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_REAL_NAME_FLG"]    == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_REAL_KANA_FLG"]    == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_SEX_FLG"]          == '1') {
            $make_hist_flg = true;
        }

        if ($make_hist_flg) {
            $query = knja110_2aQuery::getLastExpireDate($model, $model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knja110_2aQuery::getEntDate($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d", strtotime("1 day", strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d", strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d", strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE"]);

            $query  = " INSERT INTO GUARANTOR_HIST_DAT ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '{$start_day}' AS ISSUEDATE, ";
            $query .= "     '{$e_appdate}' AS EXPIREDATE, ";
            $query .= "     GUARANTOR_RELATIONSHIP, ";
            $query .= "     GUARANTOR_NAME, ";
            $query .= "     GUARANTOR_KANA, ";
            $query .= "     GUARANTOR_REAL_NAME, ";
            $query .= "     GUARANTOR_REAL_KANA, ";
            $query .= "     GUARANTOR_SEX, ";
            $query .= "     '{$model->field["GUARANTOR_RELATIONSHIP_FLG"]}' AS GUARANTOR_RELATIONSHIP_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_NAME_FLG"]}'         AS GUARANTOR_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_KANA_FLG"]}'         AS GUARANTOR_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_NAME_FLG"]}'    AS GUARANTOR_REAL_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_KANA_FLG"]}'    AS GUARANTOR_REAL_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_SEX_FLG"]}'          AS GUARANTOR_SEX_FLG, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " FROM ";
            $query .= "     GUARDIAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";

            $db->query($query);

            $query = "VALUES(DATE(sysdate()))";
            $sysDate = $db->getOne($query);

            if ($sysDate < $e_appdate) {
                $saisyuArray = explode('-', $e_appdate);
                $saisyuYear  = $saisyuArray[0];
                $saisyuMonth = $saisyuArray[1];
                $saisyuDay   = $saisyuArray[2];

                $time = mktime(0, 0, 0, $saisyuMonth, $saisyuDay + 1, $saisyuYear); //一日後を取得
                $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
                $edate = "9999-12-31";

                $data = array();
                $data["SCHREGNO"][TEXT]                     = $model->schregno;
                $data["ISSUEDATE"][DATE]                    = $sdate;
                $data["EXPIREDATE"][DATE]                   = $edate;
                $data["GUARANTOR_RELATIONSHIP"][TEXT]       = $model->field["GUARANTOR_RELATIONSHIP"];
                $data["GUARANTOR_NAME"][TEXT]               = $model->field["GUARANTOR_NAME"];
                $data["GUARANTOR_KANA"][TEXT]               = $model->field["GUARANTOR_KANA"];
                $data["GUARANTOR_REAL_NAME"][TEXT]          = $model->field["GUARANTOR_REAL_NAME"];
                $data["GUARANTOR_REAL_KANA"][TEXT]          = $model->field["GUARANTOR_REAL_KANA"];
                $data["GUARANTOR_SEX"][TEXT]                = $model->field["GUARANTOR_SEX"];
                $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP_FLG"];
                $data["GUARANTOR_NAME_FLG"][TEXT]           = $model->field["GUARANTOR_NAME_FLG"];
                $data["GUARANTOR_KANA_FLG"][TEXT]           = $model->field["GUARANTOR_KANA_FLG"];
                $data["GUARANTOR_REAL_NAME_FLG"][TEXT]      = $model->field["GUARANTOR_REAL_NAME_FLG"];
                $data["GUARANTOR_REAL_KANA_FLG"][TEXT]      = $model->field["GUARANTOR_REAL_KANA_FLG"];
                $data["GUARANTOR_SEX_FLG"][TEXT]            = $model->field["GUARANTOR_SEX_FLG"];
                $data["REGISTERCD"][TEXT]                   = STAFFCD;
                $data["UPDATED"][FUNC]                      = "SYSDATE()";

                $yoyakuQuery = Query::insertSQL($data, "GUARANTOR_HIST_DAT");
                $db->query($yoyakuQuery);
            }
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $guardian_dat_count = $db->getOne($query);

        $data = array();
        if (!$guardian_dat_count) {
            $data["SCHREGNO"][TEXT] = $model->schregno;
        }
        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        if ($guardian_dat_count > 0) {
            $where = "WHERE SCHREGNO = '{$model->schregno}'";
            $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "GUARDIAN_DAT");
        }
        $db->query($query);
    }

    //住所履歴の最新取得
    public function getMaxAddress($model)
    {
        if ($model->infoDiv == "4") {
            $table = " GUARANTOR_ADDRESS_DAT ";
        } else {
            $table = " GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        }

        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .=       $table." T1 ";
        $query .= " WHERE ";
        $query .= "     MAX_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND MAX_T.ISSUEDATE = T1.ISSUEDATE ";

        return $query;
    }

    //GUARDIAN_HIST_DAT の更新
    public function makeGuardianHistDat($model, $db)
    {
        $make_hist_flg = false;

        if ($model->field["RELATIONSHIP_FLG"]    == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_NAME_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_KANA_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_REAL_NAME_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_REAL_KANA_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_SEX_FLG"]       == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_BIRTHDAY_FLG"]  == '1') {
            $make_hist_flg = true;
        }

        if ($make_hist_flg) {
            $query = knja110_2aQuery::getLastExpireDate($model, $model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knja110_2aQuery::getEntDate($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d", strtotime("1 day", strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d", strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d", strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE"]);

            $query  = " INSERT INTO GUARDIAN{$model->guardian2}_HIST_DAT ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '{$start_day}' AS ISSUEDATE, ";
            $query .= "     '{$e_appdate}' AS EXPIREDATE, ";
            $query .= "     RELATIONSHIP, ";
            $query .= "     GUARD_NAME, ";
            $query .= "     GUARD_KANA, ";
            $query .= "     GUARD_REAL_NAME, ";
            $query .= "     GUARD_REAL_KANA, ";
            $query .= "     GUARD_SEX, ";
            $query .= "     GUARD_BIRTHDAY, ";
            $query .= "     '{$model->field["RELATIONSHIP_FLG"]}'    AS RELATIONSHIP_FLG, ";
            $query .= "     '{$model->field["GUARD_NAME_FLG"]}'      AS GUARD_NAME_FLG, ";
            $query .= "     '{$model->field["GUARD_KANA_FLG"]}'      AS GUARD_KANA_FLG, ";
            $query .= "     '{$model->field["GUARD_REAL_NAME_FLG"]}' AS GUARD_REAL_NAME_FLG, ";
            $query .= "     '{$model->field["GUARD_REAL_KANA_FLG"]}' AS GUARD_REAL_KANA_FLG, ";
            $query .= "     '{$model->field["GUARD_SEX_FLG"]}'       AS GUARD_SEX_FLG, ";
            $query .= "     '{$model->field["GUARD_BIRTHDAY_FLG"]}'  AS GUARD_BIRTHDAY_FLG, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";

            $db->query($query);

            $query = "VALUES(DATE(sysdate()))";
            $sysDate = $db->getOne($query);

            if ($sysDate < $e_appdate && $e_appdate != "9999-12-31") {
                $saisyuArray = explode('-', $e_appdate);
                $saisyuYear  = $saisyuArray[0];
                $saisyuMonth = $saisyuArray[1];
                $saisyuDay   = $saisyuArray[2];

                $time = mktime(0, 0, 0, $saisyuMonth, $saisyuDay + 1, $saisyuYear); //一日後を取得
                $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
                $edate = "9999-12-31";

                $data = array();
                $data["SCHREGNO"][TEXT]            = $model->schregno;
                $data["RELATIONSHIP"][TEXT]        = $model->field["RELATIONSHIP"];
                $data["GUARD_NAME"][TEXT]          = $model->field["GUARD_NAME"];
                $data["GUARD_KANA"][TEXT]          = $model->field["GUARD_KANA"];
                $data["GUARD_REAL_NAME"][TEXT]     = $model->field["GUARD_REAL_NAME"];
                $data["GUARD_REAL_KANA"][TEXT]     = $model->field["GUARD_REAL_KANA"];
                $data["GUARD_SEX"][TEXT]           = $model->field["GUARD_SEX"];
                $data["GUARD_BIRTHDAY"][TEXT]      = $model->field["GUARD_BIRTHDAY"];
                $data["RELATIONSHIP_FLG"][TEXT]    = $model->field["RELATIONSHIP_FLG"];
                $data["GUARD_NAME_FLG"][TEXT]      = $model->field["GUARD_NAME_FLG"];
                $data["GUARD_KANA_FLG"][TEXT]      = $model->field["GUARD_KANA_FLG"];
                $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->field["GUARD_REAL_NAME_FLG"];
                $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->field["GUARD_REAL_KANA_FLG"];
                $data["GUARD_SEX_FLG"][TEXT]       = $model->field["GUARD_SEX_FLG"];
                $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->field["GUARD_BIRTHDAY_FLG"];

                $data["ISSUEDATE"][DATE]  = $sdate;
                $data["EXPIREDATE"][DATE] = $edate;

                $yoyakuQuery = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_HIST_DAT");
                $db->query($yoyakuQuery);
            }
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $guardian_dat_count = $db->getOne($query);
        if ($guardian_dat_count > 0) {
            $data = array();
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $model->field["GUARD_BIRTHDAY"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where = "WHERE SCHREGNO = '{$model->schregno}'";

            $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_DAT", $where);
        } else {
            $data = array();
            $data["SCHREGNO"][TEXT]        = $model->schregno;
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $model->field["GUARD_BIRTHDAY"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_DAT");
        }
        $db->query($query);
    }

    //DELETE -- SCHREG_ADDRESS_DAT
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM schreg_address_dat WHERE schregno = '".$model->schregno."' AND issuedate = date('".str_replace("/", "-", $model->issuedate)."')";
        $db->query($query);

        $dataCnt = $db->getOne("SELECT COUNT(*) FROM SCHREG_ADDRESS_DAT WHERE SCHREGNO = '".$model->schregno."'");
        if ($dataCnt == 0) {
            knja110_2aQuery::updateEmergencyInfo($db, $model, "delete");

            $query  = " DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$model->schregno."' AND BASE_SEQ IN ('012', '013') ";
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- GUARDIAN_ADDRESS_DAT
    public function &getDeleteGuardianAddressDat($model)
    {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM GUARDIAN{$model->guardian2}_ADDRESS_DAT WHERE schregno = '{$model->schregno}' AND issuedate = date('".str_replace("/", "-", $model->guard_issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- GUARANTOR_ADDRESS_DAT
    public function &deleteGuarantorAddressDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- 保証人住所データ
        $query  = " DELETE FROM GUARANTOR_ADDRESS_DAT WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guarantor_issuedate)."') ";
        $db->query($query);

        //更新 -- 保護者データ
        $query = knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- SCHREG_SEND_ADDRESS_DAT
    public function &deleteSchregSendAddressDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- その他住所データ
        $query  = " DELETE FROM SCHREG_SEND_ADDRESS_DAT WHERE SCHREGNO = '{$model->schregno}' AND DIV = '".$model->send_div."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //生徒の緊急連絡先等の情報取得
    public function getEmergencyInfo($schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T2.BASE_REMARK3 AS E_TELNO_MEMO, ";
        $query .= "     T2.BASE_REMARK1 AS EMERGENCYTELNO_2, ";
        $query .= "     T2.BASE_REMARK4 AS E_TELNO_MEMO_2, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     T2.BASE_REMARK5 AS E_TELNO_MEMO2, ";
        $query .= "     T2.BASE_REMARK2 AS EMERGENCYTELNO2_2, ";
        $query .= "     T2.BASE_REMARK6 AS E_TELNO_MEMO2_2, ";
        $query .= "     T3.BASE_REMARK1 AS PRIORITY1, ";
        $query .= "     T3.BASE_REMARK2 AS PRIORITY2, ";
        $query .= "     T3.BASE_REMARK3 AS PRIORITY3, ";
        $query .= "     T3.BASE_REMARK4 AS PRIORITY4, ";
        $query .= "     T3.BASE_REMARK5 AS PRIORITY5, ";
        $query .= "     T3.BASE_REMARK6 AS PRIORITY6 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T2 ";
        $query .= "          ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T2.BASE_SEQ = '012' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T3 ";
        $query .= "          ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "         AND T3.BASE_SEQ = '013' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //生徒の緊急連絡先情報を更新
    public function updateEmergencyInfo(&$db, &$model, $flg)
    {
        $data = array();

        $data["EMERGENCYCALL"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYCALL"] : "";
        $data["EMERGENCYNAME"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYNAME"] : "";
        $data["EMERGENCYRELA_NAME"][TEXT]  = ($flg == "update")? $model->field["EMERGENCYRELA_NAME"] : "";
        $data["EMERGENCYTELNO"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYTELNO"] : "";

        $data["EMERGENCYCALL2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYCALL2"] : "";
        $data["EMERGENCYNAME2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYNAME2"] : "";
        $data["EMERGENCYRELA_NAME2"][TEXT] = ($flg == "update")? $model->field["EMERGENCYRELA_NAME2"] : "";
        $data["EMERGENCYTELNO2"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYTELNO2"] : "";

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);
        $db->query($query);
    }

    //SCHREG_BASE_DETAIL_MST更新
    public function updateSchregBaseDetailMst(&$db, &$model, $seq)
    {
        //データの存在チェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     BASE_SEQ = '".$seq."' ";
        $flg = $db->getOne($query);

        $data = array();
        if (!$flg) {
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["BASE_SEQ"][TEXT]         = $seq;
        }
        if ($seq == '012') {
            $data["BASE_REMARK1"][TEXT]     = $model->field["EMERGENCYTELNO_2"];
            $data["BASE_REMARK2"][TEXT]     = $model->field["EMERGENCYTELNO2_2"];
            $data["BASE_REMARK3"][TEXT]     = $model->field["E_TELNO_MEMO"];
            $data["BASE_REMARK4"][TEXT]     = $model->field["E_TELNO_MEMO_2"];
            $data["BASE_REMARK5"][TEXT]     = $model->field["E_TELNO_MEMO2"];
            $data["BASE_REMARK6"][TEXT]     = $model->field["E_TELNO_MEMO2_2"];
        } elseif ($seq == '013') {
            for ($i = 1; $i <= 6; $i++) {
                $data["BASE_REMARK".$i][TEXT]   = $model->field["PRIORITY".$i];
            }
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        if ($flg) {
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     BASE_SEQ = '".$seq."' ";

            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }
        $db->query($query);
    }

    //最終履歴の終了日取得
    public function getLastExpireDate($model, $schregno)
    {
        if ($model->infoDiv == "4") {
            $table = " GUARANTOR_HIST_DAT ";
        } else {
            $table = " GUARDIAN{$model->guardian2}_HIST_DAT ";
        }

        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴がなかったら入学日付を取得する
    public function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 || ' ' || T1.NAME1 AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getGuardianHist($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     L1.NAME1 AS RELATION_NAME, ";
        $query .= "     L2.NAME1 AS GUARD_SEX_NAME, ";
        $query .= "     T1.RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARD_NAME_FLG, ";
        $query .= "     T1.GUARD_KANA_FLG, ";
        $query .= "     T1.GUARD_REAL_NAME_FLG, ";
        $query .= "     T1.GUARD_REAL_KANA_FLG, ";
        $query .= "     T1.GUARD_SEX_FLG, ";
        $query .= "     T1.GUARD_BIRTHDAY_FLG ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H201' ";
        $query .= "          AND T1.RELATIONSHIP = L1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z002' ";
        $query .= "          AND T1.GUARD_SEX = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    public function getGuardianHistData($model, $schregNo, $issueData)
    {
        $issueData = str_replace("/", "-", $issueData);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     T1.RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARD_NAME_FLG, ";
        $query .= "     T1.GUARD_KANA_FLG, ";
        $query .= "     T1.GUARD_REAL_NAME_FLG, ";
        $query .= "     T1.GUARD_REAL_KANA_FLG, ";
        $query .= "     T1.GUARD_SEX_FLG, ";
        $query .= "     T1.GUARD_BIRTHDAY_FLG ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.ISSUEDATE = '".$issueData."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    public function getGuardianHistDateCheck($model, $table = "")
    {
        $issueData = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($table) {
            $query .=       $table." T1 ";
        } else {
            $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.ISSUEDATE <> '".$issueData."' ";
        $query .= "     AND (T1.ISSUEDATE BETWEEN '".$issueData."' AND '".$expireDate."' ";
        $query .= "          OR ";
        $query .= "          T1.EXPIREDATE BETWEEN '".$issueData."' AND '".$expireDate."') ";

        return $query;
    }

    public function getDelHistQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ISSUEDATE = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    public function getInsHistQuery($model)
    {
        $data["SCHREGNO"][TEXT]            = $model->schregno;
        $data["ISSUEDATE"][DATE]           = $model->histField["ISSUEDATE"];
        $data["EXPIREDATE"][DATE]          = $model->histField["EXPIREDATE"];
        $data["RELATIONSHIP"][TEXT]        = $model->histField["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]          = $model->histField["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]          = $model->histField["GUARD_KANA"];
        $data["GUARD_REAL_NAME"][TEXT]     = $model->histField["GUARD_REAL_NAME"];
        $data["GUARD_REAL_KANA"][TEXT]     = $model->histField["GUARD_REAL_KANA"];
        $data["GUARD_SEX"][TEXT]           = $model->histField["GUARD_SEX"];
        $data["GUARD_BIRTHDAY"][DATE]      = $model->histField["GUARD_BIRTHDAY"];

        $data["RELATIONSHIP_FLG"][TEXT]    = $model->histField["RELATIONSHIP_FLG"]    ? $model->histField["RELATIONSHIP_FLG"]    : "0";
        $data["GUARD_NAME_FLG"][TEXT]      = $model->histField["GUARD_NAME_FLG"]      ? $model->histField["GUARD_NAME_FLG"]      : "0";
        $data["GUARD_KANA_FLG"][TEXT]      = $model->histField["GUARD_KANA_FLG"]      ? $model->histField["GUARD_KANA_FLG"]      : "0";
        $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->histField["GUARD_REAL_NAME_FLG"] ? $model->histField["GUARD_REAL_NAME_FLG"] : "0";
        $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->histField["GUARD_REAL_KANA_FLG"] ? $model->histField["GUARD_REAL_KANA_FLG"] : "0";
        $data["GUARD_SEX_FLG"][TEXT]       = $model->histField["GUARD_SEX_FLG"]       ? $model->histField["GUARD_SEX_FLG"]       : "0";
        $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->histField["GUARD_BIRTHDAY_FLG"]  ? $model->histField["GUARD_BIRTHDAY_FLG"]  : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_HIST_DAT");

        return $query;
    }

    public function getGuardHistUpd($model, $schregno, $table)
    {
        $query  = " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table." T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    public function getGuardUpdFromHist($model, $data, $schregno, $table)
    {
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = "WHERE SCHREGNO = '{$schregno}' ";
        $query = Query::updateSQL($data, $table, $where);

        return $query;
    }

    //保証人履歴データ取得
    public function getGuarantorHistDat($model, $issuedate = "")
    {
        $query  = " SELECT ";
        $query .= "     *";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        if ($issuedate) {
            $query .= " AND ISSUEDATE   = '".str_replace("/", "-", $issuedate)."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        }

        return $query;
    }

    //保護者データ取得
    public function getGuardianDat($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";

        return $query;
    }

    //保証人履歴データ削除
    public function deleteGuarantorHistDat($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     ISSUEDATE   = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    //保証人履歴データ追加
    public function insertGuarantorHistDat($model)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $model->schregno;
        $data["ISSUEDATE"][DATE]                = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $data["EXPIREDATE"][DATE]               = str_replace("/", "-", $model->histField["EXPIREDATE"]);
        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->histField["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->histField["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->histField["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->histField["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->histField["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->histField["GUARANTOR_SEX"];

        $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT] = $model->histField["GUARANTOR_RELATIONSHIP_FLG"] ? $model->histField["GUARANTOR_RELATIONSHIP_FLG"] : "0";
        $data["GUARANTOR_NAME_FLG"][TEXT]       = $model->histField["GUARANTOR_NAME_FLG"]      ? $model->histField["GUARANTOR_NAME_FLG"]      : "0";
        $data["GUARANTOR_KANA_FLG"][TEXT]       = $model->histField["GUARANTOR_KANA_FLG"]      ? $model->histField["GUARANTOR_KANA_FLG"]      : "0";
        $data["GUARANTOR_REAL_NAME_FLG"][TEXT]  = $model->histField["GUARANTOR_REAL_NAME_FLG"] ? $model->histField["GUARANTOR_REAL_NAME_FLG"] : "0";
        $data["GUARANTOR_REAL_KANA_FLG"][TEXT]  = $model->histField["GUARANTOR_REAL_KANA_FLG"] ? $model->histField["GUARANTOR_REAL_KANA_FLG"] : "0";
        $data["GUARANTOR_SEX_FLG"][TEXT]        = $model->histField["GUARANTOR_SEX_FLG"]       ? $model->histField["GUARANTOR_SEX_FLG"]       : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARANTOR_HIST_DAT");

        return $query;
    }

    //在籍している兄弟姉妹取得
    public function getRegdBrother($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     N1.NAMESPARE2 AS GRD_GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1 = 'A023' ";
        $query .= "             AND T1.GRADE BETWEEN N1.NAME2 AND N1.NAME3, ";
        $query .= "     SCHREG_REGD_HDAT T2, ";
        $query .= "     SCHREG_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     (T3.GRD_DIV IS NULL OR T3.GRD_DIV = '4') AND ";
        $query .= "     T1.SCHREGNO IN (SELECT ";
        $query .= "                         RELA_SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_RELA_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = '".$model->schregno."' ";
        $query .= "                     ) ";

        return $query;
    }

    //兄弟姉妹へ住所データをコピー
    public function copyAddressToBrother($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //コピーする住所データ取得（MAX開始日付）
        $fromAddr = $db->getRow(knja110_2aQuery::getRowAddress($model->schregno, ""), DB_FETCHMODE_ASSOC);

        //在籍している兄弟姉妹
        $brosInfo = array();
        $result = $db->query(knja110_2aQuery::getRegdBrother($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $brosInfo[$row["SCHREGNO"]] = $row;
        }
        $result->free();

        foreach ($brosInfo as $schregno => $bros) {
            //コピー先の住所データ取得
            $toAddr = $db->getRow(knja110_2aQuery::getRowAddress($schregno, ""), DB_FETCHMODE_ASSOC);

            if (strlen($toAddr["SCHREGNO"]) && $toAddr["ISSUEDATE"]) {
                //コピー元のMAX住所データの終了日付を変更する
                $data = array();
                $data["EXPIREDATE"][DATE]       = date("Y-m-d", strtotime("-1 day", strtotime($fromAddr["ISSUEDATE"])));
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$schregno."' AND ";
                $where .= "     ISSUEDATE   = '".$toAddr["ISSUEDATE"]."' ";

                $query = Query::updateSQL($data, "SCHREG_ADDRESS_DAT", $where);
                $db->query($query);

                //コピー先の終了日付
                $expiredate = $toAddr["EXPIREDATE"];
            } else {
                //卒業年度の3/31
                $expiredate = ($brosInfo[$schregno]["GRD_GRADE"] - $brosInfo[$schregno]["GRADE"] + 1 + CTRL_YEAR).'-03-31';
            }

            //コピー
            $data = array();
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["ISSUEDATE"][DATE]        = $fromAddr["ISSUEDATE"];
            $data["EXPIREDATE"][DATE]       = $expiredate;
            $data["ZIPCD"][TEXT]            = $fromAddr["ZIPCD"];
            $data["AREACD"][TEXT]           = $fromAddr["AREACD"];
            $data["ADDR1"][TEXT]            = $fromAddr["ADDR1"];
            $data["ADDR2"][TEXT]            = $fromAddr["ADDR2"];
            $data["ADDR_FLG"][TEXT]         = $fromAddr["ADDR_FLG"];
            $data["ADDR1_ENG"][TEXT]        = $fromAddr["ADDR1_ENG"];
            $data["ADDR2_ENG"][TEXT]        = $fromAddr["ADDR2_ENG"];
            $data["TELNO"][TEXT]            = $fromAddr["TELNO"];
            $data["TELNO_MEMO"][TEXT]       = $fromAddr["TELNO_MEMO"];
            $data["TELNO2"][TEXT]           = $fromAddr["TELNO2"];
            $data["TELNO2_MEMO"][TEXT]      = $fromAddr["TELNO2_MEMO"];
            $data["FAXNO"][TEXT]            = $fromAddr["FAXNO"];
            $data["EMAIL"][TEXT]            = $fromAddr["EMAIL"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_ADDRESS_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //備忘録
    public function updDetail009R2Query($model, $db)
    {
        $query = knja110_2aQuery::getDetail009R2($model->schregno);
        $detail009R2 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($detail009R2)) {
            $data["BASE_REMARK2"][TEXT]     = $model->field["DETAIL_009_R2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND BASE_SEQ = '009' ";

            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        } else {
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["BASE_SEQ"][TEXT]         = "009";
            $data["BASE_REMARK2"][TEXT]     = $model->field["DETAIL_009_R2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        }

        return $query;
    }
}
