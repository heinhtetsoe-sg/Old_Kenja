<?php

require_once('for_php7.php');

class knje071dQuery extends Query
{

    //卒業可能学年チェック
    public function checkGrdGrade($grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' AND ";
        $query .= "     '".$grade."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //テーブルのフィールド確認
    public function getTableColumn($tabname, $colname)
    {
        $query  = " SELECT T1.COLUMN_NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TABLE_NAME = '".$tabname."' AND T1.COLUMN_NAME = '".$colname."' ";

        return $query;
    }

    //発行リスト
    public function getCertifIndex($schregno, $model)
    {
        $query  = " WITH GRDBASE AS (";
        $query .= "     SELECT ";
        $query .= "     FISCALYEAR(T1.GRD_DATE) AS YEAR, ";
        $query .= "     T1.* ";
        $query .= "     FROM GRD_BASE_MST T1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CERTIF_INDEX ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT T1 ";
        $query .= "     INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                                  AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
        $query .= "    INNER JOIN AFT_GRAD_COURSE_DAT TAFT ON TAFT.YEAR = T1.YEAR ";
        $query .= "                                  AND TAFT.SEQ = INT(T2.REMARK13) ";
        $query .= "                                  AND TAFT.DECISION = '5' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T2.TYPE = '1' ";
        $query .= "     AND T2.REMARK13 IS NOT NULL ";
        if ('1' == $model->Properties["certif_no_8keta"]) {
            $query .= "     AND T2.REMARK1 IS NULL ";
        } else {
            $query .= "     AND T1.CERTIF_NO IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CERTIF_INDEX ";

        return $query;
    }


    //生徒一覧
    public function getList($model, $schregno = "")
    {
        $certifIndexex = "";
        $comma = "";
        $model->printData = $model->printData ? $model->printData : array();
        foreach ($model->printData as $pd) {
            $work = preg_split("/-/", $pd);
            $certifIndexes .= $comma . "'".$work[3]."'";
            $comma = ",";
        }

        $query  = " WITH GRDBASE AS (";
        $query .= "     SELECT ";
        $query .= "     FISCALYEAR(T1.GRD_DATE) AS YEAR, ";
        $query .= "     T1.* ";
        $query .= "     FROM GRD_BASE_MST T1 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     '0' || '-' || T1.YEAR || '-' || REGD.GRADE || '-' || REGD.HR_CLASS || '-' || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE,  ";
        $query .= "     T1.SCHREGNO || ' ' || VALUE(REGDH.HR_NAME, '') || ' ' || VALUE(REGD.ATTENDNO, '   ') || '番 ' || VALUE(SCHBASE.NAME_SHOW, '') AS LABEL, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     SCHBASE.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT T1 ";
        $query .= "     INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                                  AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
        $query .= "     INNER JOIN SCHREG_BASE_MST SCHBASE ON SCHBASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GRDBASE ON GRDBASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND REGD.YEAR = T1.YEAR ";
        $query .= "                                  AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR     = REGD.YEAR ";
        $query .= "                                  AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "                                  AND REGDH.GRADE    = REGD.GRADE ";
        $query .= "                                  AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "    INNER JOIN AFT_GRAD_COURSE_DAT TAFT ON TAFT.YEAR = T1.YEAR ";
        $query .= "                                  AND TAFT.SEQ = INT(T2.REMARK13) ";
        $query .= "                                  AND TAFT.DECISION = '5' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.TYPE = '1' ";
        $query .= "     AND T2.REMARK13 IS NOT NULL ";
        $query .= "     AND GRDBASE.SCHREGNO IS NULL ";
        $query .= "     AND ( ";
        if ($model->cmd == 'print') {
            $query .= "     T1.CERTIF_INDEX IN (".$certifIndexes.") OR ";
        }
        if ('1' == $model->Properties["certif_no_8keta"]) {
            $query .= "     T2.REMARK1 IS NULL ";
        } else {
            $query .= "     T1.CERTIF_NO IS NULL ";
        }
        $query .= "    )  ";
        if ($schregno) {
            $query .= "     AND ('0-' || T1.SCHREGNO IN ('".implode("','", $schregno)."') OR '1-' || T1.SCHREGNO IN ('".implode("','", $schregno)."' )) ";
        }
        $query .= " UNION ALL ";
        $query .= " SELECT DISTINCT ";
        $query .= "     '1' || '-' || VALUE(GRDBASE.YEAR,'') || '-' || VALUE(GRDBASE.GRD_GRADE, '') || '-' || VALUE(GRDBASE.GRD_HR_CLASS,'') || '-' || VALUE(GRDBASE.GRD_ATTENDNO, '') || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || VALUE(GRDH.HR_NAME, '') || '（卒） ' || VALUE(GRDBASE.GRD_ATTENDNO, '   ') || '番 ' || VALUE(GRDBASE.NAME_SHOW, '') AS LABEL, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(GRDH.HR_NAME, '　　　　　') || '（卒）' AS HR_NAME, ";
        $query .= "     GRDBASE.GRD_ATTENDNO AS ATTENDNO, ";
        $query .= "     GRDBASE.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT T1 ";
        $query .= "     INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                                  AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
        $query .= "     INNER JOIN GRDBASE ON GRDBASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GRD_REGD_HDAT GRDH ON GRDH.YEAR     = GRDBASE.YEAR ";
        $query .= "                                  AND GRDH.SEMESTER = GRDBASE.GRD_SEMESTER ";
        $query .= "                                  AND GRDH.GRADE    = GRDBASE.GRD_GRADE ";
        $query .= "                                  AND GRDH.HR_CLASS = GRDBASE.GRD_HR_CLASS ";
        $query .= "     INNER JOIN AFT_GRAD_COURSE_DAT TAFT ON TAFT.YEAR = T1.YEAR ";
        $query .= "                                  AND TAFT.SEQ = INT(T2.REMARK13) ";
        $query .= "                                  AND TAFT.DECISION = '5' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.TYPE = '1' ";
        $query .= "     AND T2.REMARK13 IS NOT NULL ";
        $query .= "     AND ( ";
        if ($model->cmd == 'print') {
            $query .= "     T1.CERTIF_INDEX IN (".$certifIndexes.") OR ";
        }
        if ('1' == $model->Properties["certif_no_8keta"]) {
            $query .= "     T2.REMARK1 IS NULL ";
        } else {
            $query .= "     T1.CERTIF_NO IS NULL ";
        }
        $query .= "    )  ";
        if ($schregno) {
            $query .= "     AND ('0-' || T1.SCHREGNO IN ('".implode("','", $schregno)."') OR '1-' || T1.SCHREGNO IN ('".implode("','", $schregno)."' )) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //近大チェック
    public function checkKindai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }

    //職員一覧取得
    public function getStaffList()
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST AS T1, ";
        $query .= "     STAFF_YDAT AS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = T2.STAFFCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //--- 証明書発行
    public function getUpdateQuery($db, $year, $index, $name, $certifno, $date, $certif_no_div, $model)
    {
        //echo " getUpdateQuery: ".$index.",".$name.",".$certifno.",".$date."<br>";
        if ($model->Properties["certif_no_8keta"] == "1") {
            $data = array();
            $data["ISSUERNAME"][TEXT]           = $name;
            $data["ISSUECD"][TEXT] = "1";
            $data["ISSUEDATE"][TEXT] = str_replace("/", "-", $date);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
            $query = Query::updateSQL($data, "certif_issue_dat", $where);
            $db->query($query);
        } else {
            $data = array();
            $data["ISSUERNAME"][TEXT]           = $name;
            $data["ISSUECD"][TEXT] = "1";
            if ($certif_no_div == 0) {
                $data["CERTIF_NO"][NUMBER] = $certifno;
            }
            $data["ISSUEDATE"][TEXT] = str_replace("/", "-", $date);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
            $query = Query::updateSQL($data, "certif_issue_dat", $where);
            $db->query($query);
        }

        $data = array();
        if ($model->Properties["certif_no_8keta"] == "1") {
            $data["REMARK1"][TEXT]              = $certifno;
        }
        $data["REMARK2"][TEXT]              = $model->field["SEKI"];
        $data["REMARK3"][TEXT]              = $model->field["KANJI"];
        $data["REMARK4"][TEXT]              = $model->field["MIRISYU"];
        $data["REMARK5"][TEXT]              = $model->field["RISYU"];
        $data["REMARK6"][TEXT]              = $model->field["FORM6"];
        $data["REMARK7"][TEXT]              = str_replace("/", "-", $date);
        $data["REMARK8"][TEXT]              = "on" == $model->field["HYOTEI"] ? "1" : "";
        $data["REMARK9"][TEXT]              = $model->field["SONOTAJUUSYO"];
        $data["REMARK10"][TEXT]             = "on" == $model->field["COMMENT"] ? "1"  : "";
        $data["REMARK11"][TEXT]             = $model->field["tyousasyoNotPrintAnotherAttendrec"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where = " WHERE YEAR = '" .$year ."' AND CERTIF_INDEX = '" .$index ."'";
        $query = Query::updateSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT", $where);
        $db->query($query);
    }

    //発行番号印刷不可をチェック
    public function getCertifSchool($db, $year, $certifKindcd)
    {
        $query  = " SELECT ";
        $query .= "     CERTIF_KINDCD, ";
        $query .= "     CERTIF_NO "; //0:印刷あり 1:印刷なし
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='".$year."' AND ";
        $query .= "     CERTIF_KINDCD='".$certifKindcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row["CERTIF_NO"];
    }

    //証明書最終番号情報をチェック
    public function get_certif_maxnumchk($db, $year, $certifIndex)
    {
        $query  = " SELECT ";
        $query .= "  COALESCE(CERTIF_NO,0) as NUMBER ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '" .$year ."' AND ";
        $query .= "  CERTIF_INDEX = '" .$certifIndex."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row["NUMBER"];
    }

    //証明書最終番号情報を取得
    public function get_certif_maxnumber($db, $year, $model, $certifIndex)
    {
        if ($model->Properties["certif_no_8keta"] == "1") {
            $keta2 = substr($year, 2, 2);
            $query = knje071dQuery::getBumonCeritfDiv($model, $year, $certifIndex);
            $issueRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $keta3 = $issueRow["BUMON"];
            $keta4 = $issueRow["CERTIF_DIV"];
            $query  = " SELECT ";
            $query .= "  COALESCE(MAX(INTEGER(REMARK1)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .$year."'";
            $query .= "  AND REMARK1 like '{$keta2}{$keta3}{$keta4}%' ";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["NUMBER"] = $row["NUMBER"] == 1 ? $keta2.$keta3.$keta4.sprintf("%04d", $row["NUMBER"]) : $row["NUMBER"];
            $row["NUMBER"] = sprintf("%08d", $row["NUMBER"]);
        } else {
            $query  = " SELECT ";
            $query .= "  COALESCE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_ISSUE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .$cd1 ."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        return $row["NUMBER"];
    }

    //証明書最終番号情報を取得
    public function getBumonCeritfDiv($model, $year, $certifIndex)
    {
        $query .= " SELECT ";
        $query .= "     CISSUE.YEAR, ";
        $query .= "     CISSUE.CERTIF_INDEX, ";
        $query .= "     CISSUE.SCHREGNO, ";
        $query .= "     CISSUE.CERTIF_KINDCD, ";
        $query .= "     CISSUE.GRADUATE_FLG, ";
        $query .= "     CASE WHEN VALUE(CISSUE.GRADUATE_FLG, '0') = '1' ";
        $query .= "          THEN G005_G.NAME2 ";
        $query .= "          ELSE G005_R.NAME2 ";
        $query .= "     END AS BUMON, ";
        $query .= "     CKIND.CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT AS CISSUE ";
        $query .= "     LEFT JOIN CERTIF_KIND_MST CKIND ON CISSUE.CERTIF_KINDCD = CKIND.CERTIF_KINDCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '{$year}' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CISSUE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST G005_R ON G005_R.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_R.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_R.NAMESPARE2 = REGD.COURSECD ";
        $query .= "          AND G005_R.NAMESPARE3 = REGD.MAJORCD ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    GREGD.* ";
        $query .= "                FROM ";
        $query .= "                   (SELECT ";
        $query .= "                        GREGD_S.YEAR, ";
        $query .= "                        GREGD_S.SCHREGNO, ";
        $query .= "                        MAX(GREGD_S.SEMESTER) AS SEMESTER ";
        $query .= "                    FROM ";
        $query .= "                        (SELECT SCHREGNO, MAX(YEAR) AS YEAR FROM GRD_REGD_DAT GROUP BY SCHREGNO) YMAX, ";
        $query .= "                        GRD_REGD_DAT GREGD_S ";
        $query .= "                    WHERE ";
        $query .= "                        YMAX.YEAR = GREGD_S.YEAR ";
        $query .= "                        AND YMAX.SCHREGNO = GREGD_S.SCHREGNO ";
        $query .= "                    GROUP BY ";
        $query .= "                        GREGD_S.YEAR, ";
        $query .= "                        GREGD_S.SCHREGNO ";
        $query .= "                    ) GREGDYS, ";
        $query .= "                    GRD_REGD_DAT GREGD ";
        $query .= "                WHERE ";
        $query .= "                    GREGDYS.YEAR = GREGD.YEAR ";
        $query .= "                    AND GREGDYS.SEMESTER = GREGD.SEMESTER ";
        $query .= "                    AND GREGDYS.SCHREGNO = GREGD.SCHREGNO ";
        $query .= "                 ) AS GREGD ON CISSUE.SCHREGNO = GREGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_G.NAMESPARE2 = GREGD.COURSECD ";
        $query .= "          AND G005_G.NAMESPARE3 = GREGD.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     CISSUE.YEAR = '{$year}' ";
        $query .= "     AND CISSUE.CERTIF_INDEX = '{$certifIndex}' ";
        return $query;
    }

    //証明書発行者情報を取得
    public function getissuername($db, $staffcd)
    {
        $query  = " SELECT ";
        $query .= "  STAFFNAME_SHOW as NAME ";
        $query .= " FROM ";
        $query .= "  STAFF_MST ";
        $query .= " WHERE ";
        $query .= "  STAFFCD = '" .$staffcd."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row["NAME"];
    }
}
