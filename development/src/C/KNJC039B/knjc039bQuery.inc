<?php

require_once('for_php7.php');

class knjc039bQuery extends Query {

    //学期コンボ
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種コンボ
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     GDAT.YEAR           = HDAT.YEAR ";
        $query .= "                     AND HDAT.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "                     AND GDAT.GRADE      = HDAT.GRADE ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "                     AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //組コンボ
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND G1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //集計単位コンボ
    function getCollectionCd($model) {
        $query  = " SELECT ";
        $query .= "     COLLECTION_CD || ':' || COLLECTION_NAME AS LABEL, ";
        $query .= "     COLLECTION_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //備考生成エラー処理　集計データが既に存在するか？
    function getCollectionDataCnt($model) {
        $query  = "";
        //在籍データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND G1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"] != $model->field["HR_CLASS_ALL"]) {
            $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= " ) ";
        //集計データ
        $query .= " , T_ATTEND_REASON AS ( ";
        $query .= "     SELECT ";
        $query .= "         C1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_REASON_COLLECTION_DAT C1 ";
        $query .= "     WHERE ";
        $query .= "         C1.YEAR = '{$model->year}' ";
        $query .= "         AND C1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND C1.COLLECTION_CD = '{$model->field["COLLECTION_CD"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(T1.SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_REGD R1 ";
        $query .= "     LEFT JOIN T_ATTEND_REASON T1 ON T1.SCHREGNO = R1.SCHREGNO ";

        return $query;
    }

    //校種ごとの出欠コードがあるか
    function getNamecdCnt($year, $cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";

        return $query;
    }

    //備考生成エラー処理　生成対象データが存在するか？
    function getExistsDataCnt($model) {
        $query  = "";
        //在籍データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND G1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"] != $model->field["HR_CLASS_ALL"]) {
            $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= "         AND EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_HDAT HDAT ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR         = HDAT.YEAR ";
        $query .= "                         AND T1.SEMESTER = HDAT.SEMESTER ";
        $query .= "                         AND T1.GRADE    = HDAT.GRADE ";
        $query .= "                         AND T1.HR_CLASS = HDAT.HR_CLASS ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "                         AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= "                     ) ";
        $query .= " ) ";
        //集計単位マスタ
        $query .= " , T_FROMTO AS ( ";
        $query .= "     SELECT ";
        $query .= "         C1.FROM_DATE, ";
        $query .= "         C1.TO_DATE ";
        $query .= "     FROM ";
        $query .= "         ATTEND_REASON_COLLECTION_MST C1 ";
        $query .= "     WHERE ";
        $query .= "         C1.YEAR = '{$model->year}' ";
        $query .= "         AND C1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND C1.COLLECTION_CD = '{$model->field["COLLECTION_CD"]}' ";
        $query .= " ) ";
        //出欠データ
        $query .= " , T_ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.DI_CD || '-' || VALUE(L1.SUBL_CD,'000') || '-' || VALUE(L2.SUBM_CD,'000') AS VALUE ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAY_DAT T1 ";
        $query .= "         INNER JOIN T_FROMTO C1 ON T1.ATTENDDATE BETWEEN C1.FROM_DATE AND C1.TO_DATE ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBL_DAT L1 ";
        $query .= "                  ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "                 AND T1.ATTENDDATE   = L1.ATTENDDATE ";
        $query .= "                 AND T1.DI_CD        = L1.DI_CD ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBM_DAT L2 ";
        $query .= "                  ON T1.SCHREGNO     = L2.SCHREGNO ";
        $query .= "                 AND T1.ATTENDDATE   = L2.ATTENDDATE ";
        $query .= "                 AND T1.DI_CD        = L2.DI_CD ";
        $query .= "                 AND L1.SUBL_CD      = L2.SUBL_CD ";
        $query .= " ) ";
        //名称マスタ　有効な出欠コード・名称を取得
        $query .= " , T_NMCD AS ( ";
        $query .= "     SELECT ";
        $query .= "         C1.NAMECD2 || '-' || VALUE(C6.NAMECD2,'000') || '-' || VALUE(C7.NAMECD2,'000') AS VALUE, ";
        $query .= "         CASE WHEN VALUE(C7.NAMECD2,'000') != '000' THEN C7.NAME1 WHEN VALUE(C6.NAMECD2,'000') != '000' THEN C6.NAME1 ELSE C1.NAME1 END AS LABEL ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST C1 ";
        $query .= "         LEFT JOIN V_NAME_MST C6 ON C6.NAMECD1='{$model->C006}' AND C1.YEAR=C6.YEAR AND C1.NAMECD2=C6.NAMESPARE1 ";
        $query .= "         LEFT JOIN V_NAME_MST C7 ON C7.NAMECD1='{$model->C007}' AND C6.YEAR=C7.YEAR AND C6.NAMECD2=C7.NAMESPARE1 ";
        $query .= "     WHERE ";
        $query .= "         C1.YEAR='{$model->year}' ";
        $query .= "         AND C1.NAMECD1='{$model->C001}' ";
        $query .= " ) ";
        //出欠データ2
        $query .= " , T_ATTEND2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         N1.VALUE, ";
        $query .= "         N1.LABEL ";
        $query .= "     FROM ";
        $query .= "         T_ATTEND T1 ";
        $query .= "         INNER JOIN T_NMCD N1 ON N1.VALUE = T1.VALUE ";
        $query .= " ) ";

        //生成対象データ
        $query .= " SELECT ";
        $query .= "     COUNT(T1.ATTENDDATE) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_REGD R1 ";
        $query .= "     LEFT JOIN T_ATTEND2 T1 ON T1.SCHREGNO = R1.SCHREGNO ";

        return $query;
    }


    //備考生成処理
    function getAttendReasonCollectionDatSql($model) {
        $query  = "";
        //在籍データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND G1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($model->field["GRADE"] != $model->field["GRADE_ALL"]) {
            $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"] != $model->field["HR_CLASS_ALL"]) {
            $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= "         AND EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_HDAT HDAT ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR         = HDAT.YEAR ";
        $query .= "                         AND T1.SEMESTER = HDAT.SEMESTER ";
        $query .= "                         AND T1.GRADE    = HDAT.GRADE ";
        $query .= "                         AND T1.HR_CLASS = HDAT.HR_CLASS ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "                         AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= "                     ) ";
        $query .= " ) ";
        //集計単位マスタ
        $query .= " , T_FROMTO AS ( ";
        $query .= "     SELECT ";
        $query .= "         C1.FROM_DATE, ";
        $query .= "         C1.TO_DATE ";
        $query .= "     FROM ";
        $query .= "         ATTEND_REASON_COLLECTION_MST C1 ";
        $query .= "     WHERE ";
        $query .= "         C1.YEAR = '{$model->year}' ";
        $query .= "         AND C1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "         AND C1.COLLECTION_CD = '{$model->field["COLLECTION_CD"]}' ";
        $query .= " ) ";
        //出欠データ
        $query .= " , T_ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.DI_CD || '-' || VALUE(L1.SUBL_CD,'000') || '-' || VALUE(L2.SUBM_CD,'000') AS VALUE ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAY_DAT T1 ";
        $query .= "         INNER JOIN T_FROMTO C1 ON T1.ATTENDDATE BETWEEN C1.FROM_DATE AND C1.TO_DATE ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBL_DAT L1 ";
        $query .= "                  ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "                 AND T1.ATTENDDATE   = L1.ATTENDDATE ";
        $query .= "                 AND T1.DI_CD        = L1.DI_CD ";
        $query .= "         LEFT JOIN ATTEND_DAY_SUBM_DAT L2 ";
        $query .= "                  ON T1.SCHREGNO     = L2.SCHREGNO ";
        $query .= "                 AND T1.ATTENDDATE   = L2.ATTENDDATE ";
        $query .= "                 AND T1.DI_CD        = L2.DI_CD ";
        $query .= "                 AND L1.SUBL_CD      = L2.SUBL_CD ";
        $query .= " ) ";
        //名称マスタ　有効な出欠コード・名称を取得
        $query .= " , T_NMCD AS ( ";
        $query .= "     SELECT ";
        $query .= "         C1.NAMECD2 || '-' || VALUE(C6.NAMECD2,'000') || '-' || VALUE(C7.NAMECD2,'000') AS VALUE, ";
        $query .= "         CASE WHEN VALUE(C7.NAMECD2,'000') != '000' THEN C7.NAME1 WHEN VALUE(C6.NAMECD2,'000') != '000' THEN C6.NAME1 ELSE C1.NAME1 END AS LABEL ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST C1 ";
        $query .= "         LEFT JOIN V_NAME_MST C6 ON C6.NAMECD1='{$model->C006}' AND C1.YEAR=C6.YEAR AND C1.NAMECD2=C6.NAMESPARE1 ";
        $query .= "         LEFT JOIN V_NAME_MST C7 ON C7.NAMECD1='{$model->C007}' AND C6.YEAR=C7.YEAR AND C6.NAMECD2=C7.NAMESPARE1 ";
        $query .= "     WHERE ";
        $query .= "         C1.YEAR='{$model->year}' ";
        $query .= "         AND C1.NAMECD1='{$model->C001}' ";
        $query .= " ) ";
        //出欠データ2
        $query .= " , T_ATTEND2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         N1.VALUE, ";
        $query .= "         N1.LABEL ";
        $query .= "     FROM ";
        $query .= "         T_ATTEND T1 ";
        $query .= "         INNER JOIN T_NMCD N1 ON N1.VALUE = T1.VALUE ";
        $query .= " ) ";

        //生成対象データ
        $query .= " SELECT ";
        $query .= "     R1.SCHREGNO, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.LABEL, ";
        $query .= "     COUNT(T1.ATTENDDATE) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_REGD R1 ";
        $query .= "     LEFT JOIN T_ATTEND2 T1 ON T1.SCHREGNO = R1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     R1.SCHREGNO, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.LABEL ";
        $query .= " ORDER BY ";
        $query .= "     R1.SCHREGNO, ";
        $query .= "     T1.VALUE ";

        return $query;
    }

    //備考生成処理
    function updAttendReasonCollectionDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $updArray = array();        //出欠備考あり
        $updZeroArray = array();    //出欠備考なし
        $query = knjc039bQuery::getAttendReasonCollectionDatSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["CNT"] > 0 && strlen($row["LABEL"])) {
                $updArray[$row["SCHREGNO"]][$row["VALUE"]] = $row["LABEL"] . $row["CNT"] . "日";
            } else if ($row["CNT"] == 0) {
                $updZeroArray[$row["SCHREGNO"]] = "";
            }
        }
        $result->free();

        //出欠備考あり生徒
        foreach ($updArray as $schno => $arr) {
            $setRemark = $tmpRemark = $seq = "";
            foreach ($arr as $cd => $remark) {
                $tmpRemark .= $seq . $remark;
                $seq = " ";
                if (strlen($tmpRemark) <= 150) $setRemark = $tmpRemark; //フィールドサイズ以内ならセット
            }
            $query = knjc039bQuery::getDeleteAttendReasonCollectionDat($model, $schno);
            $db->query($query);
            $query = knjc039bQuery::getInsertAttendReasonCollectionDat($model, $schno, $setRemark);
            $db->query($query);
        }

        //出欠備考なし生徒
        foreach ($updZeroArray as $schno => $setRemark) {
            $query = knjc039bQuery::getDeleteAttendReasonCollectionDat($model, $schno);
            $db->query($query);
            $query = knjc039bQuery::getInsertAttendReasonCollectionDat($model, $schno, $setRemark);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //ATTEND_REASON_COLLECTION_DATのレコードを削除。
    function getDeleteAttendReasonCollectionDat($model, $schno) {
        $query  = "";
        $query .= " DELETE FROM ATTEND_REASON_COLLECTION_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR            = '{$model->year}' ";
        $query .= "     AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND COLLECTION_CD   = '{$model->field["COLLECTION_CD"]}' ";
        $query .= "     AND SCHREGNO        = '{$schno}' ";
        return $query;
    }

    //ATTEND_REASON_COLLECTION_DATのレコードを追加
    function getInsertAttendReasonCollectionDat($model, $schno, $remark) {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
        $data["COLLECTION_CD"][TEXT]    = $model->field["COLLECTION_CD"];
        $data["SCHREGNO"][TEXT]         = $schno;
        $data["ATTEND_REMARK"][TEXT]    = $remark;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_REASON_COLLECTION_DAT");
        return $query;
    }
}
?>
