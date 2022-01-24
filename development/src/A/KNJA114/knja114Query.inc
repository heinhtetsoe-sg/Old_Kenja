<?php

require_once('for_php7.php');

class knja114Query extends Query {
/************ 左画面 ***************/
    //学期名取得
    function getSemesterName() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (   TR_CD1    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3    = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD3 = '" .STAFFCD ."') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //システム名コンボ
    function getSystemName() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SYSTEMID AS VALUE, ";
        $query .= "     SYSTEM_NAME AS LABEL, ";
        $query .= "     SHOWORDER ";
        $query .= " FROM ";
        $query .= "     OTHER_SYSTEM_MST ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO AS GRADE_HR_CLASS, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     '{$model->systemId}' AS SYSTEMID, ";
        $query .= "     SYST.LOGINID, ";
        $query .= "     SYST.PASSWORD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_OTHER_SYSTEM_USER_DAT SYST ON SYST.SYSTEMID = '".$model->systemId."' ";
        $query .= "                                                AND REGD.SCHREGNO = SYST.SCHREGNO  ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->gradeHrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

/************ 右画面 ***************/
    //１レコード取得
    function getRow($model, $systemCd) {
        $query  = " SELECT ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO AS GRADE_HR_CLASS, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     SYST.LOGINID, ";
        $query .= "     SYST.PASSWORD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_OTHER_SYSTEM_USER_DAT SYST ON SYST.SYSTEMID = '".$systemCd."' ";
        $query .= "                                                AND REGD.SCHREGNO = SYST.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$model->schregNo."' ";

        return $query;
    }

/******** 追加更新処理  ********/
    //存在チェック
    function checkCnt($systemCd, $schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_OTHER_SYSTEM_USER_DAT ";
        $query .= " WHERE ";
        $query .= "         SYSTEMID   = '".$systemCd."' ";
        $query .= "     AND SCHREGNO   = '".$schregNo."' ";

        return $query;
    }

    //存在チェック
    function schregnoCount($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック
    function systemidCount($systemid) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     OTHER_SYSTEM_MST ";
        $query .= " WHERE ";
        $query .= "         SYSTEMID = '".$systemid."' ";

        return $query;
    }

    //--- INSERT
    function &getDeleteInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $getCount  = $db->getOne(knja114Query::checkCnt($model->systemId, $model->schregNo));

        $data = array();
        $data["LOGINID"][TEXT]          = $fields["LOGINID"];
        $data["PASSWORD"][TEXT]         = ($fields["PASSWORD"] == $fields["UME_PASSWORD"]) ? $fields["HID_PASSWORD"]: $fields["PASSWORD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($getCount == 0) {
            $data["SYSTEMID"][TEXT]         = $model->systemId;
            $data["SCHREGNO"][TEXT]         = $model->schregNo;
            $query = Query::insertSQL($data, "SCHREG_OTHER_SYSTEM_USER_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE SYSTEMID   = '".$model->systemId."'";
            $where .= "   AND SCHREGNO   = '".$model->schregNo."'";
            $query = Query::updateSQL($data, "SCHREG_OTHER_SYSTEM_USER_DAT", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SCHREG_OTHER_SYSTEM_USER_DAT ";
        $query .= "       WHERE SYSTEMID   = '".$model->systemId."' ";
        $query .= "         AND SCHREGNO   = '".$model->schregNo."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

/******** CSV処理  ********/
    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.GRADE,";
        $query .= "     REGD.HR_CLASS,";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     '{$model->systemId}' AS SYSTEMID, ";
        $query .= "     SYST.LOGINID, ";
        $query .= "     SYST.PASSWORD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_OTHER_SYSTEM_USER_DAT SYST ON SYST.SYSTEMID = '".$model->systemId."' ";
        $query .= "                                                AND REGD.SCHREGNO = SYST.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->gradeHrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //存在チェック
    function getCountData($systemCd, $schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_OTHER_SYSTEM_USER_DAT ";
        $query .= " WHERE ";
        $query .= "         SYSTEMID   = '".$systemCd."' ";
        $query .= "     AND SCHREGNO   = '".$schregNo."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, $data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データセット
            $data["SYSTEMID"][TEXT]             = $data_arr[$i]["SYSTEMID"];
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["LOGINID"][TEXT]              = $data_arr[$i]["LOGINID"];
            $data["PASSWORD"][TEXT]             = $data_arr[$i]["PASSWORD"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";
            $queryCnt = knja114Query::getCountData($data_arr[$i]["SYSTEMID"], $data_arr[$i]["SCHREGNO"]);
            if (1 > $db->getOne($queryCnt)) {
                $query = Query::insertSQL($data, "SCHREG_OTHER_SYSTEM_USER_DAT");
            } else {
                $where  = " WHERE SYSTEMID   = '".$data_arr[$i]["SYSTEMID"]."' ";
                $where .= "   AND SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "SCHREG_OTHER_SYSTEM_USER_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
?>
