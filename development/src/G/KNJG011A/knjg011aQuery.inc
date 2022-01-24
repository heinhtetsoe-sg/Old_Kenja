<?php

require_once('for_php7.php');

class knjg011aQuery extends Query 
{

    //学年取得（権限チェック）
    function getGrade($model) {
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

    //部門取得
    function getBumon($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     G005_G.NAME2 AS VALUE, ";
        $query .= "     G005_G.NAME2 || '-' || G005_G.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "          AND G005_G.NAMESPARE1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "          AND G005_G.NAMESPARE2 = T2.COURSECD ";
        $query .= "          AND G005_G.NAMESPARE3 = T2.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = T2.COURSECD ";
        $query .= "     AND T1.MAJORCD = T2.MAJORCD ";
        $query .= " ORDER BY  ";
        $query .= "     VALUE ";

        return $query;
    }

    //証明書種類取得
    function getCertifKind()
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
        $query .= " ORDER BY  ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCertifDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     CERTIF_DIV ";
        $query .= " FROM ";
        $query .= "     CERTIF_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     CERTIF_KINDCD = '{$model->field["CERTIF_KINDCD"]}' ";

        return $query;
    }

    //MAX発行番号を取得
    function getCertifNo($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(INT(REMARK1)+1),1) AS CERTIF_NO_MAX ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TYPE = '1' ";
        $query .= "     AND REMARK1 LIKE '{$model->certifNoKotei}%' ";

        return $query;
    }

    //発行対象人数を取得
    function getSchregNumber($model, $grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS SCH_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT W1 ";
        $query .= "     INNER JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        $query .= "           AND G005_G.NAME2 = '{$model->field["BUMON"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND G005_G.NAMESPARE1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "           AND G005_G.NAMESPARE2 = W1.COURSECD ";
        $query .= "           AND G005_G.NAMESPARE3 = W1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
                $query .= "           AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
            $query .= "           AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHREG_BASE_MST W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     W1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     W1.GRADE = '".$grade."' AND ";
        $query .= "     W2.GRD_DIV = '1' AND ";
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
    function &getInsertQuery($db, $model, $max_index, $staffname)
    {
        $db->autoCommit(false);

        //発行対象者を配列にセット（クラス、出席番号順）
        $opt = array();
        $reccnt = 0;
        $result = $db->query(knjg011aQuery::getSchregList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("CERTIF_INDEX"   => (int)$max_index + $reccnt,
                           "SCHREGNO"       => $row["SCHREGNO"],
                           "CERTIF_NO"      => $model->certifNoKotei.sprintf("%04d", ((int)$model->field["CERTIF_NO_MAX"] * 1 + $reccnt)),
                           "REMARK1"        => $model->certifNoKotei.sprintf("%04d", ((int)$model->field["CERTIF_NO_MAX"] * 1 + $reccnt)));
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
                $data["ISSUEDATE"][TEXT]        = str_replace("/","-",CTRL_DATE);   //発行日付
                $data["REGISTERCD"][TEXT]       = STAFFCD;                          //登録者コード
                $data["UPDATED"][NUMBER]        = "sysdate()";                      //更新日付

                $query = Query::insertSQL($data, "certif_issue_dat");
                $db->query($query);

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

        $db->commit();
        return;
    }

    //発行対象者を取得（クラス、出席番号順）
    function getSchregList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     W1.HR_CLASS, ";
        $query .= "     W1.ATTENDNO, ";
        $query .= "     W1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT W1 ";
        $query .= "     INNER JOIN NAME_MST G005_G ON G005_G.NAMECD1 = 'G005' ";
        $query .= "           AND G005_G.NAME2 = '{$model->field["BUMON"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND G005_G.NAMESPARE1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND G005_G.NAMESPARE1 = '".SCHOOLKIND."' ";
        }
        $query .= "           AND G005_G.NAMESPARE2 = W1.COURSECD ";
        $query .= "           AND G005_G.NAMESPARE3 = W1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
                $query .= "          AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ";
            $query .= "          AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHREG_BASE_MST W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     W1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     W1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "     W2.GRD_DIV = '1' AND ";
        $query .= "     W2.SCHREGNO = W1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     W1.HR_CLASS, ";
        $query .= "     W1.ATTENDNO ";

        return $query;
    }
}
?>
