<?php

require_once('for_php7.php');

class knja055Query extends Query {

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T1.YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }

    //Z010取得
    function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00'";

        return $query;
    }

    //卒業生の人数
    function get_grd_student($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     GRD_DATE BETWEEN '".$model->Sdate."' AND '".$model->Edate."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT L1 ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = L1.YEAR AND L2.GRADE = L1.GRADE AND L2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= " WHERE L1.YEAR = '".$model->Year."' ";
                $query .= " ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT L1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = L1.YEAR AND L2.GRADE = L1.GRADE AND L2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " WHERE L1.YEAR = '".$model->Year."' ";
            $query .= " ) ";
        }

        return $query;
    }

    //卒業生学籍番号
    function get_grd_schno($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     GRD_DATE BETWEEN '".$model->Sdate."' AND '".$model->Edate."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT L1 ";
                $query .= " INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = L1.YEAR AND L2.GRADE = L1.GRADE AND L2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= " WHERE L1.YEAR = '".$model->Year."' ";
                $query .= " ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT L1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = L1.YEAR AND L2.GRADE = L1.GRADE AND L2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " WHERE L1.YEAR = '".$model->Year."' ";
            $query .= " ) ";
        }
        return $query;
    }
    //学校区分・学年数上限・創立年度を取得
    function getSch_Information($year)
    {
        $query .= " SELECT ";
        $query .= "     schooldiv,grade_hval,integer(foundedyear) as founde ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$year."' ";
        return $query;
    }

    //MAX学期取得
    function getMaxSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER < '9'";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $maxSemester = $db->getOne(knja055Query::getMaxSemester($model->Year));
        $db->autoCommit(false);
        //卒業生データ移行(17テーブル移行)

        $tableArray["GRD_ADDRESS_DAT"]                    = "SCHREG_ADDRESS_DAT";       //卒業生住所履歴データ
        $tableArray["GRD_ATTENDREC_DAT"]                  = "SCHREG_ATTENDREC_DAT";     //卒業生出欠記録データ
        $tableArray["GRD_GUARDIAN_DAT"]                   = "GUARDIAN_DAT";             //卒業生保護者データ
        $tableArray["GRD_GUARDIAN_ADDRESS_DAT"]           = "GUARDIAN_ADDRESS_DAT";     //卒業生保護者住所データ
        if ($model->Properties["useGuardian2"] == '1') {
            $tableArray["GRD_GUARDIAN2_DAT"]                  = "GUARDIAN2_DAT";            //卒業生保護者２データ
            $tableArray["GRD_GUARDIAN2_ADDRESS_DAT"]          = "GUARDIAN2_ADDRESS_DAT";    //卒業生保護者２住所データ
        }
        $tableArray["GRD_GUARANTOR_ADDRESS_DAT"]          = "GUARANTOR_ADDRESS_DAT";    //卒業保証人データ
        $tableArray["GRD_HEXAM_EMPREMARK_DAT"]            = "HEXAM_EMPREMARK_DAT";      //卒業生調査書就職用所見テーブル
        $tableArray["GRD_HEXAM_ENTREMARK_DAT"]            = "HEXAM_ENTREMARK_DAT";      //卒業生調査書進学用所見テーブル
        $tableArray["GRD_HEXAM_ENTREMARK_HDAT"]           = "HEXAM_ENTREMARK_HDAT";     //卒業生調査書進学用所見データ    
        $tableArray["GRD_HTRAINREMARK_DAT"]               = "HTRAINREMARK_DAT";         //卒業生指導要録所見データ    
        $tableArray["GRD_HTRAINREMARK_HDAT"]                = "HTRAINREMARK_HDAT";      //指導要録所見データ
        $tableArray["GRD_MEDEXAM_DET_DAT"]                = "MEDEXAM_DET_DAT";          //卒業生健康診断履歴データ
        $tableArray["GRD_MEDEXAM_HDAT"]                   = "MEDEXAM_HDAT";             //卒業生健康診断履歴ヘッダデータ
        $tableArray["GRD_MEDEXAM_TOOTH_DAT"]              = "MEDEXAM_TOOTH_DAT";        //卒業生歯口腔
        $tableArray["GRD_REGD_DAT"]                       = "SCHREG_REGD_DAT";          //卒業生在籍データ
        $tableArray["GRD_STUDYRECREMARK_DAT"]             = "STUDYRECREMARK_DAT";       //卒業生学習記録備考データ
        $tableArray["GRD_STUDYREC_DAT"]                   = "SCHREG_STUDYREC_DAT";      //卒業生学習記録データ
        $tableArray["GRD_TRANSFER_DAT"]                   = "SCHREG_TRANSFER_DAT";      //卒業生異動データ

        foreach ($tableArray as $grdTableName => $tableName) {

            $delSql = "DELETE FROM {$grdTableName} WHERE SCHREGNO in $model->schno ";
            $db->query($delSql);

            $query  = "  SELECT ";
            $query .= "    COLUMN_NAME ";
            $query .= "  FROM ";
            $query .= "    SYSIBM.COLUMNS ";
            $query .= "  WHERE ";
            $query .= "    TABLE_NAME = '{$grdTableName}' ";
            $query .= "  ORDER BY ";
            $query .= "    ORDINAL_POSITION ";

            $result = $db->query($query);
            $insertSql = "INSERT INTO {$grdTableName} SELECT ";
            $insSep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["COLUMN_NAME"] == 'REGISTERCD') {
                    $insertSql .= $insSep.STAFFCD;
                } else if ($row["COLUMN_NAME"] == 'UPDATED') {
                    $insertSql .= $insSep."sysdate()";
                } else {
                    $insertSql .= $insSep.$row["COLUMN_NAME"];
                }
                $insSep = ",";
            }
            $insertSql .= " FROM ";
            $insertSql .= "     {$tableName} ";
            $insertSql .= " WHERE ";
            $insertSql .= "     SCHREGNO in $model->schno ";
            $db->query($insertSql);
        }



        //卒業生在籍ヘッダデータ
            $query  = " DELETE FROM grd_regd_hdat ";
            $query .= "  WHERE ";
            $query .= "     exists(SELECT DISTINCT ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             FROM ";
            $query .= "                 schreg_regd_dat ";
            $query .= "             WHERE ";
            $query .= "                 SCHREGNO in $model->schno ";
            $query .= "             GROUP BY ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             ) ";
            $query .= "    AND year = '".$model->Year."' ";
        $db->query($query);

            $query  = " INSERT INTO grd_regd_hdat ";
            $query .= " SELECT ";
            $query .= "     YEAR,SEMESTER,GRADE,HR_CLASS,HR_NAME,HR_NAMEABBV,GRADE_NAME,HR_CLASS_NAME1,HR_CLASS_NAME2,HR_FACCD,TR_CD1,TR_CD2,TR_CD3,SUBTR_CD1,SUBTR_CD2,SUBTR_CD3,CLASSWEEKS,CLASSDAYS,'".STAFFCD."',sysdate() ";
            $query .= "   FROM ";
            $query .= "     schreg_regd_hdat ";
            $query .= "  WHERE ";
            $query .= "     exists(SELECT DISTINCT ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             FROM ";
            $query .= "                 schreg_regd_dat ";
            $query .= "             WHERE ";
            $query .= "                 SCHREGNO in $model->schno ";
            $query .= "             GROUP BY ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             ) ";
            $query .= "    AND year = '".$model->Year."' ";
        $db->query($query);

        //卒業生基礎データ
        $query  = "DELETE FROM GRD_BASE_MST ";
        $query .= "WHERE  SCHREGNO IN $model->schno ";

        $db->query($query);
        
        $hasField = array();
        $result = $db->query(" SELECT COLUMN_NAME FROM SYSIBM.COLUMNS WHERE TABLE_NAME = 'SCHREG_BASE_MST' ");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hasField[$row["COLUMN_NAME"]] = "1";
        }
        $result->free();

        $z010 = $db->getOne(knja055Query::getZ010());

        $query  = "INSERT INTO grd_base_mst ";
        $query .= "SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.finish_date, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.ent_date, ";
        $query .= "     T1.ent_div, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($hasField["ENT_ADDR2"] == "1") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($hasField["GRD_ADDR2"] == "1") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        if ($z010 == 'sundaikoufu') {
            $query .= "     T1.GRD_TERM, ";
        } else {
            $query .= "     '".$model->term."', ";
        }
        $query .= "     '".$maxSemester."', ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.AREACD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.ADDR_FLG, ";
        $query .= "     T2.ADDR1_ENG, ";
        $query .= "     T2.ADDR2_ENG, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.FAXNO, ";
        $query .= "     T2.EMAIL, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.AREACD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.FAXNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "  FROM ";
        $query .= "     schreg_base_mst T1 ";
        $query .= "         LEFT JOIN (SELECT * FROM schreg_address_dat ";
        $query .= "                     WHERE (schregno,issuedate) ";
        $query .= "                        IN (select schregno,max(issuedate) from schreg_address_dat group by schregno)) T2 ";
        $query .= "           ON T1.schregno = T2.schregno ";
        $query .= "         LEFT JOIN (SELECT grade, hr_class, attendno, schregno ";
        $query .= "                      FROM schreg_regd_dat ";
        $query .= "                     WHERE year      = '".$model->Year."' ";
        $query .= "                       AND semester  = '".$maxSemester."') T3 ";
        $query .= "           ON T1.schregno = T3.schregno ";
        $query .= " WHERE T1.SCHREGNO IN $model->schno ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
