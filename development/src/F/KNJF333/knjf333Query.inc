<?php

require_once('for_php7.php');

class knjf333Query extends Query {

    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "      ABBV1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= "      AND NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst() {
        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf333Query::z010Abbv1();
        $z010Abbv1 = $db->getOne($query);

        $retSchool = "";
        if ($z010Abbv1 == "1" || $z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query  = " SELECT ";
            $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
            $query .= " FROM ";
            $query .= "      V_SCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "      YEAR    = '".CTRL_YEAR."' ";

            $retSchool = $db->getOne($query);
        } else {
            $retSchool = "000000000000";
        }

        Query::dbCheckIn($db);

        return $retSchool;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "      REGDG.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "      SCHREG_REGD_DAT REGD ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ";
        $query .= "             AND REGD.GRADE = REGDG.GRADE ";
        $query .= " WHERE ";
        $query .= "      REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      AND REGD.SCHREGNO = '".$model->schregno."' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        $query .= " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         Z002.NAME2 AS SEX_NAME, ";
        $query .= "         HOKEN.AGE, ";
        $query .= "         RTRIM(CAST(HOKEN.AGE AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_HOKENSITU_HDAT HOKEN ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = HOKEN.YEAR ";
        $query .= "               AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "               AND REGD.SCHREGNO = HOKEN.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "               AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "               AND REGD.GRADE = REGDH.GRADE ";
        $query .= "               AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON HOKEN.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "         INNER JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "               AND HOKEN.SEX = Z002.NAMECD2 ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ";
        $query .= "               AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     WHERE ";
        $query .= "         HOKEN.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND HOKEN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGDG.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         Z002.NAME2, ";
        $query .= "         HOKEN.AGE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX_NAME, ";
        $query .= "     T1.AGE, ";
        $query .= "     T1.AGE_NAME ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            $query .= "     ,L{$divSeq}.INT_VAL AS IDATA{$divSeq} ";
            $query .= "     ,L{$divSeq}.CHAR_VAL AS CDATA{$divSeq} ";
        }
        $query .= " FROM ";
        $query .= "     SCH_T T1 ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            $query .= "     LEFT JOIN MEDEXAM_DISEASE_HOKENSITU_DAT L{$divSeq} ON L{$divSeq}.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "          AND L{$divSeq}.YEAR = '".CTRL_YEAR."' ";
            $query .= "          AND L{$divSeq}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ有無
    function getExistData($model) {

        $query  = "";
        $query .= "     SELECT ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_HOKENSITU_HDAT HOKEN ";
        $query .= "     WHERE ";
        $query .= "         HOKEN.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND HOKEN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND HOKEN.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //データ表示
    function getSchInfoAdd($model) {

        $query  = "";
        $query .= "     SELECT ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         BASE.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.SEX, ";
        $query .= "         Z002.NAME2 AS SEX_NAME, ";
        $query .= "         CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS AGE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST BASE ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "               AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "               AND REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "               AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "               AND REGD.GRADE = REGDH.GRADE ";
        $query .= "               AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         INNER JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "               AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     WHERE ";
        $query .= "         BASE.SCHREGNO = '".$model->schregno."' ";
        $query .= "         AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "         AND BASE.SEX IS NOT NULL ";

        return $query;
    }

    //データ表示
    function getSchInfoUpd($model) {

        $setBaseBirthDay = CTRL_YEAR."-04-01";
        $query  = "";
        $query .= " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.SEX, ";
        $query .= "         Z002.NAME2 AS SEX_NAME, ";
        $query .= "         CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR('{$setBaseBirthDay}' - BASE.BIRTHDAY) END AS AGE ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_HOKENSITU_HDAT HOKEN ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = HOKEN.YEAR ";
        $query .= "               AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "               AND REGD.SCHREGNO = HOKEN.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "               AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "               AND REGD.GRADE = REGDH.GRADE ";
        $query .= "               AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON HOKEN.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "         INNER JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "               AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     WHERE ";
        $query .= "         HOKEN.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND HOKEN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND HOKEN.SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.SEX, ";
        $query .= "         Z002.NAME2, ";
        $query .= "         CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR('{$setBaseBirthDay}' - BASE.BIRTHDAY) END ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.SEX_NAME, ";
        $query .= "     T1.AGE ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            $query .= "     ,L{$divSeq}.INT_VAL AS IDATA{$divSeq} ";
            $query .= "     ,L{$divSeq}.CHAR_VAL AS CDATA{$divSeq} ";
        }
        $query .= " FROM ";
        $query .= "     SCH_T T1 ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            $query .= "     LEFT JOIN MEDEXAM_DISEASE_HOKENSITU_DAT L{$divSeq} ON L{$divSeq}.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "          AND L{$divSeq}.YEAR = '".CTRL_YEAR."' ";
            $query .= "          AND L{$divSeq}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //固定コンボ
    function getKoteiCmb($koumokuArray) {

        $uionSet = "";
        $query .= " WITH VAL_T(VALUE, LABEL) AS ( ";
        foreach ($koumokuArray as $key => $val) {
            $query .= $uionSet." VALUES('{$key}', '{$key}:{$val}') ";
            $uionSet = " UNION ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS VALUE, ";
        $query .= "     CASE WHEN T1.GAITOUNASI_FLG = '1' THEN '該当無し報告済み' ELSE '生徒報告済み' END || '　' || CHAR(T1.EXECUTE_DATE) AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_HOKENSITU_DAT T1";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    // DELETE
    function getDeleteDatQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DISEASE_HOKENSITU_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR     = '" .CTRL_YEAR. "' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $db->query($query);
        Query::dbCheckIn($db);
    }

    // DELETE
    function getDeleteHdatQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DISEASE_HOKENSITU_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR     = '" .CTRL_YEAR. "' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $db->query($query);
        Query::dbCheckIn($db);
    }

    // INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        if ($model->schregno) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["AGE"][NUMBER]            = $model->field["AGE"];
            $data["SEX"][TEXT]              = $model->field["SEX"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "MEDEXAM_DISEASE_HOKENSITU_HDAT");
            $db->query($query);

            foreach ($model->dataFieldType as $divSeq => $divType) {
                list($dataDiv, $seq) = preg_split("/_/", $divSeq);

                $data = array();
                $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DATA_DIV"][TEXT]         = $dataDiv;
                $data["SEQ"][TEXT]              = $seq;
                if ($divType == "I") {
                    $data["INT_VAL"][NUMBER]        = $model->field[$divSeq];
                } else {
                    $data["CHAR_VAL"][TEXT]         = $model->field[$divSeq];
                }
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][NUMBER]      = "sysdate()";

                $query = Query::insertSQL($data, "MEDEXAM_DISEASE_HOKENSITU_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getHokenDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_HOKENSITU_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getHokenHDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_HOKENSITU_HDAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //DELETE
        //県側・疾病等結果一覧DAT
        $query  = "DELETE FROM MEDEXAM_DISEASE_HOKENSITU_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $db2->query($query);

        //県側・疾病等結果一覧HDAT
        $query  = "DELETE FROM MEDEXAM_DISEASE_HOKENSITU_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_HOKENSITU_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE      = '".str_replace("/","-",$model->execute_date)."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        if ($model->field["SCH_NASI"] == "") {
            //学校側から県側へコピーするデータ取得（疾病等結果一覧）
            $query = knjf333Query::getHokenHDat($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //県側・疾病等結果一覧テーブル
                //キー
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $row["YEAR"];
                $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
                $data["AGE"][NUMBER]                = $row["AGE"];
                $data["SEX"][TEXT]                  = $row["SEX"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            =" sysdate()";
                $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_HOKENSITU_HDAT");
                $db2->query($query);
            }
            $result->free();

            //学校側から県側へコピーするデータ取得（疾病等結果一覧）
            $query = knjf333Query::getHokenDat($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //県側・疾病等結果一覧テーブル
                //キー
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $row["YEAR"];
                $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
                $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
                $data["SEQ"][TEXT]                  = $row["SEQ"];
                $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
                $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            =" sysdate()";
                $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_HOKENSITU_DAT");
                $db2->query($query);
            }
            $result->free();
        }

        //県側、学校側・報告テーブル
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][TEXT]         = str_replace("/","-",$model->execute_date);
        $data["GAITOUNASI_FLG"][TEXT]       = $model->field["SCH_NASI"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_DISEASE_HOKENSITU_DAT");
        $db->query($query);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV
    function selectCsvQuery($model) {
        $query  = "";
        $query .= " WITH SCH_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         Z002.NAME2 AS SEX_NAME, ";
        $query .= "         HOKEN.AGE, ";
        $query .= "         RTRIM(CAST(HOKEN.AGE AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_HOKENSITU_HDAT HOKEN ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = HOKEN.YEAR ";
        $query .= "               AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "               AND REGD.SCHREGNO = HOKEN.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "               AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "               AND REGD.GRADE = REGDH.GRADE ";
        $query .= "               AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON HOKEN.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "         INNER JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "               AND HOKEN.SEX = Z002.NAMECD2 ";
        $query .= "     WHERE ";
        $query .= "         HOKEN.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND HOKEN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGDH.HR_NAME, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HOKEN.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         Z002.NAME2, ";
        $query .= "         HOKEN.AGE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.HR_NAME || '-' || T1.ATTENDNO || '番' AS HR_ATTEND, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.AGE_NAME, ";
        $query .= "     T1.SEX_NAME ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            if ($divType == "I") {
                $query .= "     ,L{$divSeq}.INT_VAL AS IDATA{$divSeq} ";
            } else {
                $query .= "     ,L{$divSeq}.CHAR_VAL AS CDATA{$divSeq} ";
            }
        }
        $query .= " FROM ";
        $query .= "     SCH_T T1 ";
        foreach ($model->dataFieldType as $divSeq => $divType) {
            $query .= "     LEFT JOIN MEDEXAM_DISEASE_HOKENSITU_DAT L{$divSeq} ON L{$divSeq}.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "          AND L{$divSeq}.YEAR = '".CTRL_YEAR."' ";
            $query .= "          AND L{$divSeq}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

}
?>
