<?php

require_once('for_php7.php');

class knje080aQuery extends Query
{

    //年組取得（権限チェック）
    public function getAuth($model, $year, $semester)
    {
        //参照・更新可
        if (in_array(AUTHORITY, array(DEF_REFERABLE, DEF_UPDATABLE, DEF_REFER_RESTRICT, DEF_UPDATE_RESTRICT))) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            if ($model->field["REGDDIV"] == "2") {
                $query .= "     GRD_REGD_HDAT T1 ";
                $query .= " INNER JOIN ";
                $query .= "     SEMESTER_MST T2 ";
                $query .= "         ON T2.YEAR = T1.YEAR ";
                $query .= "        AND T2.SEMESTER = T1.SEMESTER ";
                $query .= " INNER JOIN ";
                $query .= "     GRD_REGD_DAT GREGD ";
                $query .= "         ON GREGD.YEAR = T1.YEAR ";
                $query .= "        AND GREGD.SEMESTER = T1.SEMESTER ";
                $query .= "        AND GREGD.GRADE = T1.GRADE ";
                $query .= "        AND GREGD.HR_CLASS = T1.HR_CLASS ";
                $query .= " INNER JOIN ";
                $query .= "     SCHREG_REGD_GDAT GDAT ";
                $query .= "         ON GDAT.YEAR = T1.YEAR ";
                $query .= "        AND GDAT.GRADE = T1.GRADE ";
                $query .= " INNER JOIN ";
                $query .= "     SCHREG_ENT_GRD_HIST_DAT T4 ";
                $query .= "         ON T4.SCHREGNO = GREGD.SCHREGNO ";
                $query .= "        AND T4.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
                $query .= "        AND VALUE(T4.GRD_DIV, '') <> '4' ";
                $query .= "        AND T4.GRD_DATE BETWEEN T2.SDATE AND T2.EDATE ";
            } else {
                $query .= "     SCHREG_REGD_HDAT T1 ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$year."' AND ";
            $query .= "     T1.SEMESTER = '".$semester."' ";
            //参照・更新可（制限付き）
            if (in_array(AUTHORITY, array(DEF_REFER_RESTRICT, DEF_UPDATE_RESTRICT))) {
                $query .= " AND (T1.TR_CD1     = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD2     = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD3     = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuthGrdYear()
    {
        //参照・更新可
        if (in_array(AUTHORITY, array(DEF_REFERABLE, DEF_UPDATABLE, DEF_REFER_RESTRICT, DEF_UPDATE_RESTRICT))) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.YEAR || '-' || T1.SEMESTER AS VALUE, ";
            $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     GRD_REGD_HDAT T1 ";
            $query .= " INNER JOIN ";
            $query .= "     SEMESTER_MST T2 ";
            $query .= "         ON T2.YEAR = T1.YEAR ";
            $query .= "        AND T2.SEMESTER = T1.SEMESTER ";
            $query .= " INNER JOIN ";
            $query .= "     GRD_REGD_DAT GREGD ";
            $query .= "         ON GREGD.YEAR = T1.YEAR ";
            $query .= "        AND GREGD.SEMESTER = T1.SEMESTER ";
            $query .= "        AND GREGD.GRADE = T1.GRADE ";
            $query .= "        AND GREGD.HR_CLASS = T1.HR_CLASS ";
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT GDAT ";
            $query .= "         ON GDAT.YEAR = T1.YEAR ";
            $query .= "        AND GDAT.GRADE = T1.GRADE ";
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_ENT_GRD_HIST_DAT T4 ";
            $query .= "         ON T4.SCHREGNO = GREGD.SCHREGNO ";
            $query .= "        AND T4.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
            $query .= "        AND VALUE(T4.GRD_DIV, '') <> '4' ";
            $query .= "        AND T4.GRD_DATE BETWEEN T2.SDATE AND T2.EDATE ";
            //参照・更新可（制限付き）
            if (in_array(AUTHORITY, array(DEF_REFER_RESTRICT, DEF_UPDATE_RESTRICT))) {
                $query .= " WHERE ";
                $query .= "     (T1.TR_CD1     = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD2     = '".STAFFCD."' OR ";
                $query .= "      T1.TR_CD3     = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "      T1.SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE DESC ";
        }

        return $query;
    }


    //生徒一覧
    public function getList($model, $schregno = "")
    {
        $query  = " SELECT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REDGH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME_SHOW ";
        $query .= " FROM ";
        if ($model->field["REGDDIV"] == "2") {
            $query .= "     GRD_REGD_DAT REGD ";
            $query .= " INNER JOIN ";
            $query .= "     SEMESTER_MST T2 ";
            $query .= "         ON T2.YEAR = REGD.YEAR ";
            $query .= "        AND T2.SEMESTER = REGD.SEMESTER ";
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT GDAT ";
            $query .= "         ON GDAT.YEAR = REGD.YEAR ";
            $query .= "        AND GDAT.GRADE = REGD.GRADE ";
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_ENT_GRD_HIST_DAT T4 ";
            $query .= "         ON T4.SCHREGNO = REGD.SCHREGNO ";
            $query .= "        AND T4.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
            $query .= "        AND VALUE(T4.GRD_DIV, '') <> '4' ";
            $query .= "        AND T4.GRD_DATE BETWEEN T2.SDATE AND T2.EDATE ";
        } elseif ($model->field["REGDDIV"] == "1") {
            $query .= "     SCHREG_REGD_DAT REGD ";
        }
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REDGH ON REDGH.YEAR  = REGD.YEAR ";
        $query .= "                                  AND REDGH.SEMESTER = REGD.SEMESTER ";
        $query .= "                                  AND REDGH.GRADE    = REGD.GRADE ";
        $query .= "                                  AND REDGH.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        if ($model->field["REGDDIV"] == "2") {
            list($year, $semester) = explode("-", $model->field["GRD_YEAR_SEMESTER"]);
            $query .= "     REGD.YEAR     = '".$year."' AND ";
            $query .= "     REGD.SEMESTER = '".$semester."' AND ";
        } elseif ($model->field["REGDDIV"] == "1") {
            $query .= "     REGD.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     REGD.SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        if ($schregno) {
            $query .= "     BASE.SCHREGNO IN ('".implode("','", $schregno)."') ";
        } else {
            $query .= "     REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員取得
    public function getSemesterField($seme, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$seme."' ";

        return $query;
    }

    //証明書最終番号情報を取得
    public function getBumonCeritfDiv1($model, $year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     DISTINCT ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     CASE WHEN '".$model->field["REGDDIV"]."' = '2' AND GREGD.SCHREGNO IS NOT NULL ";
        $query .= "          THEN G005_G.NAME2 ";
        $query .= "          ELSE G005_R.NAME2 ";
        $query .= "     END AS BUMON ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
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
        $query .= "                 ) AS GREGD ON REGD.SCHREGNO = GREGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_G.NAMESPARE2 = GREGD.COURSECD ";
        $query .= "          AND G005_G.NAMESPARE3 = GREGD.MAJORCD ";
        $query .= "     WHERE ";
        $query .= "              REGD.YEAR = '{$year}' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        return $query;
    }

    //証明書最終番号情報を取得
    public function getBumonCeritfDiv2($model, $certifKindcd)
    {
        $query  = "";
        $query .= " SELECT ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     G008.NAME2 ";
        } else {
            $query .= "     '' ";
        }
        $query .= "      AS CERTIF_SCHOOL_KIND_NUM, ";
        $query .= "     CKIND.CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST CKIND ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     LEFT JOIN NAME_MST G008 ON G008.NAMECD1 = 'G008' ";
            $query .= "          AND G008.NAME1 = CKIND.CERTIF_SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     CKIND.CERTIF_KINDCD = '{$certifKindcd}' ";
        return $query;
    }

    //証明書最終番号情報を取得
    //certif_no_8keta == 1の場合、使用する発行番号を生成して返す
    public function getCertifMaxnumber($db, $cd1, $model)
    {
        $rows = array();
        if ($model->Properties["certif_no_8keta"] == "1") {
            $yearSimo2 = substr(CTRL_YEAR, 2, 2);
            $courseBumon = array();
            $query = knje080aQuery::getBumonCeritfDiv1($model, CTRL_YEAR);
            $result = $db->query($query);
            while ($issueRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $bumon = $issueRow["BUMON"];
                $bumon = $bumon ? $bumon : "0";
                $courseBumon[$issueRow["COURSECD"].$issueRow["MAJORCD"]] = $bumon;
            }
            $result->free();

            $query = knje080aQuery::getBumonCeritfDiv2($model, $cd1);
            $result = $db->query($query);
            while ($issueRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $certifDiv = $issueRow["CERTIF_DIV"];
                $certifKindcd = $issueRow["CERTIF_KINDCD"];
                $certifSchoolKindNum = $issueRow["CERTIF_SCHOOL_KIND_NUM"];
            }
            $result->free();
            $certifDiv = $certifDiv ? $certifDiv: "0";

            $renbanMae = ""; // 連番より前の部
            $renbanAto = ""; // 連場より後の部
            $passSEQ = false;
            $seqKeta = 4; // 連番の桁数
            $totalKeta = 8; // 発行番号の桁
//            if ($model->Properties["certif_no_format"]) {
//                $formatsplit = preg_split("/\s*[+]\s*/", $model->Properties["certif_no_format"]); // " + "でsplit
//                $totalKeta = 0;
//                foreach ($formatsplit as $s) {
//                    $s = trim($s);
//                    $add = "";
//                    if (preg_match("/^YEAR(:[0-9]+)?$/", $s)) {
//                        // 年度。
//                        $simoKeta = 0;
//                        // ':'後の数値が年度の下桁数
//                        $split2 = preg_split("/:/", $s);
//                        if ($split2 != '') {
//                            $simoKeta = $split2[1];
//                        }
//                        // 指定なしは4桁。5以上は指定なしと同様に4桁。
//                        if ($simoKeta == '' || $simoKeta > 4) {
//                            $simoKeta = strlen(CTRL_YEAR);
//                        }
//                        $yearSimo = substr(CTRL_YEAR, strlen(CTRL_YEAR) - (int)$simoKeta, $simoKeta);
//                        $add .= $yearSimo;
//                        $totalKeta += $simoKeta;
//                    } elseif ($s == "BUMON") {
//                        // 部門
//                        $add.= $bumon;
//                        $totalKeta += 1;
//                    } elseif ($s == "CERTIF_DIV") {
//                        // 証明書区分
//                        $add = $certifDiv;
//                        $totalKeta += 1;
//                    } elseif (preg_match("/^SEQ(:[0-9]+)?$/", $s)) {
//                        // 連番。':'後の数値が桁数。指定なしは4桁
//                        $split2 = preg_split("/:/", $s);
//                        if ($split2 != '' && $split2[1]) {
//                            $seqKeta = $split2[1];
//                        }
//                        $totalKeta += $seqKeta;
//                        $passSEQ = true;
//                    } elseif ($s == "SCHOOL_KIND") {
//                        $add = $certifSchoolKindNum;
//                        $totalKeta += 1;
//                    }
//                    if ($add) {
//                        if ($passSEQ) {
//                            $renbanAto .= $add;
//                        } else {
//                            $renbanMae .= $add;
//                        }
//                    }
//                }
//            } else {
//                // 賢者のフォーマット : 年度下2桁、部門1桁、証明書区分1桁、連番4桁
//                // "YEAR:2 + BUMON + CERTIF_DIV + SEQ:4";
//                $renbanMae = $yearSimo2.$bumon.$certifDiv;
//            }
            foreach ($courseBumon as $courseMajor => $bumon) {
                $renbanMae = $yearSimo2.$bumon.$certifDiv;
                $renbanAto = "";

                $query  = " SELECT ";
                $query .= "  MAX(REMARK1) as NUMBER ";
                $query .= " FROM ";
                $query .= "  CERTIF_DETAIL_EACHTYPE_DAT ";
                $query .= " WHERE ";
                $query .= "  YEAR = '" .CTRL_YEAR ."'";
                $query .= "  AND REMARK1 like '{$renbanMae}%{$renbanAto}' ";
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $maxSEQ = 0;
                if ($row["NUMBER"]) {
                    $maxSEQ = substr($row["NUMBER"], strlen($renbanMae), $seqKeta); // 連番部を抽出
                    if ($seqKeta > 0 && $maxSEQ == pow(10, $seqKeta) - 1) {
                        $maxSEQ = 0;
                    }
                }
                $row["NUMBER"] = sprintf($renbanMae."%0".$seqKeta."d".$renbanAto, $maxSEQ + 1);
                $row["NUMBER"] = sprintf("%0".$totalKeta."d", $row["NUMBER"]);
                $rows["NUMBER_{$courseMajor}"] = $row["NUMBER"];
            }
        } else {
            $query  = " SELECT ";
            $query .= "  COALESCE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_ISSUE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .CTRL_YEAR ."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rows["NUMBER"] = $row["NUMBER"];
        }
        return $rows;
    }
}
