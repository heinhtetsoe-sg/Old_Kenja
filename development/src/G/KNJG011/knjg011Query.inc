<?php

require_once('for_php7.php');

class knjg011Query extends Query 
{
    //中高一貫区分取得
    function getNameMst()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    function getBumonCeritfDiv($model)
    {
        $query  = " WITH REGD_ALL AS ( ";
        $query .= " SELECT ";
        $query .= "     '0' AS GRADUATE_FLG, YEAR, T1.SEMESTER, SCHREGNO, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '1' AS GRADUATE_FLG, YEAR, T1.SEMESTER, SCHREGNO, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ";
        $query .= " FROM ";
        $query .= "     GRD_REGD_DAT T1 ";
        $query .= " ), REGD AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     W1.GRADUATE_FLG, ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.COURSECD, ";
        $query .= "     W1.MAJORCD, ";
        $query .= "     CASE WHEN W2.GRD_DIV = '1' THEN '1' ELSE '0' END AS GRD_DIV_FLG ";
        $query .= " FROM ";
        $query .= "     REGD_ALL W1 ";
        $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN SCHREG_ENT_GRD_HIST_DAT W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     W1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     W1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     CKIND.CERTIF_KINDCD, ";
        $query .= "     T1.GRADUATE_FLG, ";
        $query .= "     T1.COURSECD, T1.MAJORCD, ";
        $query .= "     CASE WHEN VALUE(T1.GRADUATE_FLG, '0') = '1' THEN G005_G.NAME2 ELSE VALUE(G005_R.NAME2, G005_R_2.NAME2) END AS BUMON, ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     VALUE(G008.NAME2, G008_2.NAME2) ";
        } else {
            $query .= "     '' ";
        }
        $query .= "      AS CERTIF_SCHOOL_KIND_NUM, ";
        $query .= "     CKIND.CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST CKIND ";
        $query .= "     LEFT JOIN REGD T1 ON T1.GRADE = '".$model->field["GRADE"]."' AND T1.GRD_DIV_FLG = '1' ";
        $query .= "     LEFT JOIN NAME_MST G005_R ON G005_R.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_R.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND VALUE(T1.GRADUATE_FLG, '0') = '0' ";
        $query .= "          AND G005_R.NAMESPARE2 = T1.COURSECD ";
        $query .= "          AND G005_R.NAMESPARE3 = T1.MAJORCD ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     LEFT JOIN NAME_MST G008 ON G008.NAMECD1 = 'G008' ";
            $query .= "          AND G008.NAME1 = CKIND.CERTIF_SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN REGD T1_2 ON T1_2.GRADE = '".$model->field["GRADE"]."' AND T1_2.GRD_DIV_FLG = '0' ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     LEFT JOIN NAME_MST G008_2 ON G008_2.NAMECD1 = 'G008' ";
            $query .= "          AND G008_2.NAME1 = CKIND.CERTIF_SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST G005_R_2 ON G005_R_2.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_R_2.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND VALUE(T1_2.GRADUATE_FLG, '0') = '0' ";
        $query .= "          AND G005_R_2.NAMESPARE2 = T1_2.COURSECD ";
        $query .= "          AND G005_R_2.NAMESPARE3 = T1_2.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND T1.GRADUATE_FLG = '1' ";
        $query .= "          AND G005_G.NAMESPARE2 = T1.COURSECD ";
        $query .= "          AND G005_G.NAMESPARE3 = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     CKIND.CERTIF_KINDCD = '".$model->field["CERTIF_KINDCD"]."'  ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADUATE_FLG DESC, CKIND.CERTIF_DIV  ";
        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($model, $div) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     NAME_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T3.NAMECD1  = 'A023' AND ";
        $query .= "     T1.GRADE BETWEEN T3.NAMESPARE2 AND T3.NAMESPARE3 ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //証明書種類取得
    function getCertifKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CERTIF_KINDCD AS VALUE, ";
        $query .= "     T1.CERTIF_KINDCD || '：' || T2.KINDNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_YDAT T1, ";
        $query .= "     CERTIF_KIND_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CERTIF_KINDCD = T2.CERTIF_KINDCD AND ";
        $query .= "     T1.CERTIF_KINDCD IN('001', '022', '023') ";
        if ($model->Properties["use_certif_kind_mst_school_kind"] == "1") {
            $query .= "     AND (T2.CERTIF_SCHOOL_KIND IS NULL OR T2.CERTIF_SCHOOL_KIND IN ( ";
            $query .= "         SELECT IT2.SCHOOL_KIND ";
            $query .= "         FROM SCHREG_REGD_DAT IT1 ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT IT2 ON IT2.YEAR = IT1.YEAR AND IT2.GRADE = IT1.GRADE ";
            $query .= "         WHERE IT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "           AND IT1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "      )) ";
        }
        $query .= " ORDER BY  ";
        $query .= "     VALUE ";

        return $query;
    }

    //WHERE句（校種で絞る）
    function getWhereSchoolKind($model) {
        $where  = "     AND SCHREGNO in ( ";
        $where .= "                     SELECT ";
        $where .= "                         SCHREGNO ";
        $where .= "                     FROM ";
        $where .= "                         SCHREG_REGD_DAT REGD ";
        $where .= "                         LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $where .= "                              ON GDAT.YEAR  = REGD.YEAR ";
        $where .= "                             AND GDAT.GRADE = REGD.GRADE ";
        $where .= "                     WHERE ";
        $where .= "                         REGD.YEAR        = '" .CTRL_YEAR."' ";
        $where .= "                     AND REGD.SEMESTER    = '" .CTRL_SEMESTER."' ";
        $where .= "                     AND GDAT.SCHOOL_KIND = '{$model->schoolKind}' ";
        $where .= "                     ) ";

        return $where;
    }

    //MAX発行番号を取得
    function getCertifNo($db, $model, $certifNoSyudou = "")
    {
        $number = "";
        if ($certifNoSyudou == "1") {
            $query  = " SELECT ";
            $query .= "     VALUE(MAX(INT(REMARK1)+1),1) AS CERTIF_NO_MAX ";
            $query .= " FROM ";
            $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND TYPE = '1' ";
            //校種で絞る
            $query .= knjg011Query::getWhereSchoolKind($model);
            $number = $db->getOne($query);
        } else if ($model->Properties["certif_no_8keta"] == "1") {
            $keta2 = substr(CTRL_YEAR, 2, 2);
            $query = knjg011Query::getBumonCeritfDiv($model);
            $issueRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $cd1 = CTRL_YEAR;
            $yearSimo2 = substr($cd1, 2, 2);
            $bumon  = $issueRow["BUMON"];
            $bumon  = $bumon  ? $bumon  : "0";
            $certifDiv = $issueRow["CERTIF_DIV"];
            $certifDiv = $certifDiv ? $certifDiv : "0";
            $certifKindcd = $issueRow["CERTIF_KINDCD"];
            $certifSchoolKindNum = $issueRow["CERTIF_SCHOOL_KIND_NUM"];

            $renbanMae = ""; // 連番より前の部
            $renbanAto = ""; // 連場より後の部
            $passSEQ = false;
            $seqKeta = 4; // 連番の桁数
            $totalKeta = 8; // 発行番号の桁
            if ($model->Properties["certif_no_format"]) {
                $formatsplit = preg_split("/\s*[+]\s*/", $model->Properties["certif_no_format"]); // " + "でsplit
                $totalKeta = 0;
                foreach ($formatsplit as $s) {
                    $s = trim($s);
                    $add = "";
                    if (preg_match("/^YEAR(:[0-9]+)?$/", $s)) {
                        // 年度。
                        $simoKeta = 0;
                        // ':'後の数値が年度の下桁数
                        $split2 = preg_split("/:/", $s);
                        if ($split2 != '') {
                            $simoKeta = $split2[1];
                        }
                        // 指定なしは4桁。5以上は指定なしと同様に4桁。
                        if ($simoKeta == '' || $simoKeta > 4) {
                            $simoKeta = strlen($cd1);
                        }
                        $yearSimo = substr($cd1, strlen($cd1) - (int)$simoKeta, $simoKeta);
                        $add .= $yearSimo;
                        $totalKeta += $simoKeta;
                    } else if ($s == "BUMON") {
                        // 部門
                        $add.= $bumon;
                        $totalKeta += 1;
                    } else if ($s == "CERTIF_DIV") {
                        // 証明書区分
                        $add = $certifDiv;
                        $totalKeta += 1;
                    } else if (preg_match("/^SEQ(:[0-9]+)?$/", $s)) {
                        // 連番。':'後の数値が桁数。指定なしは4桁
                        $split2 = preg_split("/:/", $s);
                        if ($split2 != '' && $split2[1]) {
                            $seqKeta = $split2[1];
                        }
                        $totalKeta += $seqKeta;
                        $passSEQ = true;
                    } else if ($s == "SCHOOL_KIND") {
                        $add = $certifSchoolKindNum;
                        $totalKeta += 1;
                    }
                    if ($add) {
                        if ($passSEQ) {
                            $renbanAto .= $add;
                        } else {
                            $renbanMae .= $add;
                        }
                    }
                }

            } else {
                // 賢者のフォーマット : 年度下2桁、部門1桁、証明書区分1桁、連番4桁
                // "YEAR:2 + BUMON + CERTIF_DIV + SEQ:4";
                $renbanMae = $yearSimo2.$bumon.$certifDiv;
            }

            $query  = " SELECT ";
            $query .= "  MAX(REMARK1) as NUMBER ";
            $query .= " FROM ";
            $query .= "  CERTIF_DETAIL_EACHTYPE_DAT ";
            $query .= " WHERE ";
            $query .= "  YEAR = '" .$cd1 ."'";
            $query .= "  AND REMARK1 like '{$renbanMae}%{$renbanAto}' ";
            //校種で絞る
            $query .= knjg011Query::getWhereSchoolKind($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $maxSEQ = 0;
            if ($row["NUMBER"]) {
                $maxSEQ = substr($row["NUMBER"], strlen($renbanMae), $seqKeta); // 連番部を抽出
                if ($seqKeta > 0 && $maxSEQ == pow(10, $seqKeta) - 1) {
                    $maxSEQ = 0;
                }
            }
            $number = sprintf($renbanMae."%0".$seqKeta."d".$renbanAto, (int)$maxSEQ + 1);
            $number = sprintf("%0".$totalKeta."d", $number);
        } else {
            $number = knjg011Query::getCertifIssueDatCertifNoMax($db, $model);
        }
        return $number;
    }

    function getCertifIssueDatCertifNoMax($db, $model) {
        $query  = " SELECT ";
        $query .= "  VALUE(MAX(INTEGER(CERTIF_NO)+1),1) as NUMBER ";
        $query .= " FROM ";
        $query .= "  CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR."' ";
        //校種で絞る
        $query .= knjg011Query::getWhereSchoolKind($model);

        return $db->getOne($query);
    }

    //発行対象人数を取得
    function getSchregNumber($model, $grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS SCH_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT W1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHREG_BASE_MST W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     W1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     W1.GRADE = '".$grade."' AND ";
        $query .= "     W2.GRD_DIV = '1' AND ";             //卒業区分
        $query .= "     W2.SCHREGNO = W1.SCHREGNO ";

        return $query;
    }

    //MAX申請番号を取得
    function getCertifIndex()
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(INT(CERTIF_INDEX)+1),1) AS CERTIF_INDEX_MAX ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //発行者を取得
    function getStaffData()
    {
        $query  = " SELECT ";
        $query .= "     STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //一括発行
    function &getInsertQuery($db, $model, $max_index, $max_no, $staffname, $max_detail_no, $certif_issue_dat_certif_no_max)
    {
        $db->autoCommit(false);

        //発行対象者を配列にセット（クラス、出席番号順）
        $opt = array();
        $reccnt = 0;
        $result = $db->query(knjg011Query::getSchregList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("CERTIF_INDEX"   => (int)$max_index + (int)$reccnt,
                           "SCHREGNO"       => $row["SCHREGNO"],
                           "CERTIF_NO"      => (int)$max_no + (int)$reccnt,
                           "REMARK1"        => (int)$max_detail_no + (int)$reccnt,
                           "CERTIF_ISSUE_DAT_CERTIF_NO_MAX" => (int)$certif_issue_dat_certif_no_max + (int)$reccnt);
            $reccnt++;
        }

        //発行対象者をInsert
        if ($reccnt > 0) {
            for ($i = 0; $i < get_count($opt); $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;                        //年度
                $data["CERTIF_INDEX"][TEXT]     = $opt[$i]["CERTIF_INDEX"];         //申請番号
                $data["SCHREGNO"][TEXT]         = $opt[$i]["SCHREGNO"];             //学籍番号
                $data["CERTIF_KINDCD"][TEXT]    = $model->field["CERTIF_KINDCD"];   //証明書種類コード
                $data["GRADUATE_FLG"][TEXT]     = "1";                              //既卒区分 (1:卒業生)
                $data["APPLYDATE"][TEXT]        = str_replace("/","-",CTRL_DATE);   //証明書申請日付
                $data["ISSUERNAME"][TEXT]       = $staffname;                       //証明書発行者名
                $data["ISSUECD"][TEXT]          = "1";                              //証明書発行区分 (1:印刷可)
                if ($model->Properties["certif_no_8keta"] == "1") {
                    $data["CERTIF_NO"][NUMBER]      = $opt[$i]["CERTIF_ISSUE_DAT_CERTIF_NO_MAX"]; //発行番号
                } else {
                    $data["CERTIF_NO"][NUMBER]      = $opt[$i]["CERTIF_NO"];            //発行番号
                }
                $data["ISSUEDATE"][TEXT]        = str_replace("/","-",CTRL_DATE);   //発行日付
                $data["REGISTERCD"][TEXT]       = STAFFCD;                          //登録者コード
                $data["UPDATED"][NUMBER]        = "sysdate()";                      //更新日付

                $query = Query::insertSQL($data, "certif_issue_dat");
                $db->query($query);

                if ($model->Properties["certifNoSyudou"] == "1" || $model->Properties["certif_no_8keta"] == "1") {
                    $data = array();
                    $data["YEAR"][TEXT]             = CTRL_YEAR;                    //年度
                    $data["CERTIF_INDEX"][TEXT]     = $opt[$i]["CERTIF_INDEX"];     //申請番号
                    $data["SCHREGNO"][TEXT]         = $opt[$i]["SCHREGNO"];         //学籍番号
                    $data["TYPE"][TEXT]             = "1";
                    $data["REMARK1"][TEXT]          = $opt[$i]["REMARK1"];          //発行番号
                    $data["REGISTERCD"][TEXT]       = STAFFCD;                      //登録者コード
                    $data["UPDATED"][NUMBER]        = "sysdate()";                  //更新日付

                    $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        return;
    }

    //発行対象者を取得（クラス、出席番号順）
    function getSchregList($model)
    {
        $query  = " SELECT ";
        $query .= "     W1.HR_CLASS, ";
        $query .= "     W1.ATTENDNO, ";
        $query .= "     W1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT W1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHREG_BASE_MST W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     W1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     W1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "     W2.GRD_DIV = '1' AND ";         //卒業区分
        $query .= "     W2.SCHREGNO = W1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     W1.HR_CLASS, ";
        $query .= "     W1.ATTENDNO ";

        return $query;
    }
}
?>
