<?php

require_once('for_php7.php');

class knja080bQuery extends Query {
    //(New)ヘッダのクラス取得
    function getNewHrClass($model)
    {
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR+1;
            $semes = $model->min_semester;    //最小学期
        }else{
            $year = CTRL_YEAR;
            $semes = CTRL_SEMESTER+1;
        }

        //新クラス取得
        $query  = "";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= " SELECT ";
            $query .= "     '1:' || T1.GHR_CD || '-' || 'XXX' AS VALUE, ";
            $query .= "     T1.GHR_NAME AS HR_NAME, ";
            $query .= "     T2.STAFFNAME_SHOW AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".$year."' ";
            $query .= "     AND T1.SEMESTER = '".$semes."' ";
            $query .= " UNION ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "   '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T2.STAFFNAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '" .$year ."' AND ";
        $query .= "   T1.SEMESTER = '" .$semes ."' ";
        //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
        //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             NAME_MST N1 ";
        $query .= "         WHERE ";
        $query .= "                 N1.NAMECD1 = 'A032' ";
        $query .= "             AND N1.NAMECD2 = T1.GRADE ";
        $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //(Old)ヘッダのクラス取得
    function getOldHrClass($model)
    {
        //旧クラス取得
        $query  = "";
        if (CTRL_SEMESTER != $model->control["学期数"]) { //最終学期じゃないとき
            //特別支援学校対応
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= " SELECT ";
                $query .= "     '1:' || T1.GHR_CD || '-' || 'XXX' AS VALUE, ";
                $query .= "     T1.GHR_NAME AS HR_NAME, ";
                $query .= "     T2.STAFFNAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
                $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                $query .= " UNION ";
            }
            $query .= " SELECT DISTINCT ";
            $query .= "   '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "   T1.HR_NAME, ";
            $query .= "   T2.STAFFNAME_SHOW AS NAME ";
            $query .= " FROM ";
            $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
            $query .= " WHERE ";
            $query .= "   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
            $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
            //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
            //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             NAME_MST N1 ";
            $query .= "         WHERE ";
            $query .= "                 N1.NAMECD1 = 'A032' ";
            $query .= "             AND N1.NAMECD2 = T1.GRADE ";
            $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
            $query .= "     ) ";
        } else { //最終学期のとき
            if ($model->control["学校区分"] == '0') { //学年制
                //特別支援学校対応
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                    $query .= " SELECT ";
                    $query .= "     '1:' || T1.GHR_CD || '-' || 'XXX' AS VALUE, ";
                    $query .= "     T1.GHR_NAME AS HR_NAME, ";
                    $query .= "     T2.STAFFNAME_SHOW AS NAME ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
                    $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
                    $query .= " WHERE ";
                    $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
                    $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT "; //最終学年以外は無条件表示
                $query .= "   '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "   T1.HR_NAME, ";
                $query .= "   T2.STAFFNAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
                $query .= " WHERE ";
                $query .= "   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
                $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
                $query .= "   NOT EXISTS(SELECT ";
                $query .= "               W1.SCHOOLDIV, ";
                $query .= "               W1.GRADE_HVAL ";
                $query .= "             FROM ";
                $query .= "               SCHOOL_MST W1 ";
                $query .= "             WHERE ";
                $query .= "               W1.YEAR      = T1.YEAR AND ";
                $query .= "               INTEGER(W1.GRADE_HVAL) = INTEGER(T1.GRADE) ";
                $query .= "             ) ";
                //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
                //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
                $query .= "     AND NOT EXISTS ( ";
                $query .= "         SELECT ";
                $query .= "             'X' ";
                $query .= "         FROM ";
                $query .= "             NAME_MST N1 ";
                $query .= "         WHERE ";
                $query .= "                 N1.NAMECD1 = 'A032' ";
                $query .= "             AND N1.NAMECD2 = T1.GRADE ";
                $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
                $query .= "     ) ";

                $query .= " UNION   ";
                $query .= " SELECT DISTINCT "; //最終学年は留年生がいるクラスのみ表示
                $query .= "   '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "   T1.HR_NAME, ";
                $query .= "   T3.STAFFNAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T3 ON T1.TR_CD1 = T3.STAFFCD, ";
                $query .= "   CLASS_FORMATION_DAT T2 ";
                $query .= " WHERE ";
                $query .= "   T1.YEAR           = '" .CTRL_YEAR ."' AND ";
                $query .= "   T1.SEMESTER       = '" .CTRL_SEMESTER ."' AND ";
                $query .= "   INTEGER(T2.YEAR)  = INTEGER(T1.YEAR) + 1 AND ";
                $query .= "   T2.SEMESTER       = '" .$model->min_semester ."' AND ";
                $query .= "   T2.REMAINGRADE_FLG= '1' AND ";
                $query .= "   T1.GRADE          = T2.OLD_GRADE AND ";
                $query .= "   T1.HR_CLASS       = T2.OLD_HR_CLASS AND ";
                $query .= "   EXISTS(SELECT ";
                $query .= "               W1.SCHOOLDIV, ";
                $query .= "               W1.GRADE_HVAL ";
                $query .= "             FROM ";
                $query .= "               SCHOOL_MST W1 ";
                $query .= "             WHERE ";
                $query .= "               W1.YEAR                = T1.YEAR AND ";
                $query .= "               INTEGER(W1.GRADE_HVAL) = INTEGER(T1.GRADE) ";
                $query .= "             ) ";
                //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
                //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
                $query .= "     AND NOT EXISTS ( ";
                $query .= "         SELECT ";
                $query .= "             'X' ";
                $query .= "         FROM ";
                $query .= "             NAME_MST N1 ";
                $query .= "         WHERE ";
                $query .= "                 N1.NAMECD1 = 'A032' ";
                $query .= "             AND N1.NAMECD2 = T1.GRADE ";
                $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
                $query .= "     ) ";
            } else {//単位制
                //特別支援学校対応
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                    $query .= " SELECT ";
                    $query .= "     '1:' || T1.GHR_CD || '-' || 'XXX' AS VALUE, ";
                    $query .= "     T1.GHR_NAME AS HR_NAME, ";
                    $query .= "     T2.STAFFNAME_SHOW AS NAME ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
                    $query .= "     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
                    $query .= " WHERE ";
                    $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
                    $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT ";
                $query .= "   '2:' || T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "   T1.HR_NAME, ";
                $query .= "   T3.STAFFNAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T3 ON T1.TR_CD1 = T3.STAFFCD, ";
                $query .= "   CLASS_FORMATION_DAT T2 ";
                $query .= " WHERE ";
                $query .= "   T1.YEAR           = '" .CTRL_YEAR ."' AND ";
                $query .= "   T1.SEMESTER       = '" .CTRL_SEMESTER ."' AND ";
                $query .= "   INTEGER(T2.YEAR)  = INTEGER(T1.YEAR) + 1 AND ";
                $query .= "   T2.SEMESTER       = '" .$model->min_semester ."' AND ";
                $query .= "   T1.GRADE          = T2.OLD_GRADE AND ";
                $query .= "   T1.HR_CLASS       = T2.OLD_HR_CLASS ";
                //SCHREG_REGD_HDATの内、名称マスタ「A032」の年組は対象外
                //TODO:複式クラスにいる生徒が、各学年でまとまってい年組　例：01-099, 02-099, 03-099
                $query .= "     AND NOT EXISTS ( ";
                $query .= "         SELECT ";
                $query .= "             'X' ";
                $query .= "         FROM ";
                $query .= "             NAME_MST N1 ";
                $query .= "         WHERE ";
                $query .= "                 N1.NAMECD1 = 'A032' ";
                $query .= "             AND N1.NAMECD2 = T1.GRADE ";
                $query .= "             AND N1.NAME1 = T1.HR_CLASS ";
                $query .= "     ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //編成済み生徒名の取得（左のリスト）
    function GetNewStudent($model)
    {
        list($div, $cd) = explode(":", $model->newclass);
        if ($div == "1") {
            list($ghr_cd, $dummy) = explode("-", $cd);
        } else {
            list($grade, $hr_class) = explode("-", $cd);
        }
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR+1;
            $semes = $model->min_semester;
        }else{
            $year = CTRL_YEAR;
            $semes = CTRL_SEMESTER+1;
        }
        $query = " SELECT ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.OLD_GRADE, ";
        $query .= "     T1.OLD_HR_CLASS, ";
        $query .= "     T1.OLD_ATTENDNO, ";
        $query .= "     T1.REMAINGRADE_FLG, ";
        if ((CTRL_SEMESTER == $model->control["学期数"])) {
            $query .= "     CASE WHEN T1.OLD_GRADE IS NOT NULL THEN L1.NAME_SHOW ELSE L2.NAME END AS NAME ";
        } else {
            $query .= "   L1.NAME_SHOW AS NAME ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T3.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "         T3.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "         T1.OLD_GRADE = T3.GRADE AND ";
        $query .= "         T1.OLD_HR_CLASS = T3.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN FRESHMAN_DAT L2 ON T1.YEAR = L2.ENTERYEAR AND T1.SCHREGNO = L2.SCHREGNO ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "   LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T1.SCHREGNO AND G1.YEAR = T1.YEAR AND G1.SEMESTER = T1.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '" .$year ."' ";
        $query .= "   AND T1.SEMESTER = '" .$semes ."' ";
        //複式・年組コンボ
        if ($div == "1") {
            //1:複式
            $query .= "   AND G1.GHR_CD = '" .$ghr_cd ."' ";
        } else {
            //2:年組
            $query .= "   AND T1.GRADE = '" .$grade ."' ";
            $query .= "   AND T1.HR_CLASS = '" .$hr_class ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.OLD_GRADE, ";
        $query .= "   T1.OLD_HR_CLASS, ";
        $query .= "   T1.OLD_ATTENDNO ";

        return $query;
    }
    
    //編成される生徒名の取得（右のリスト）
    function GetOldStudent($model)
    {
        list($div, $cd) = explode(":", $model->oldclass);
        if ($div == "1") {
            list($ghr_cd, $dummy) = explode("-", $cd);
        } else {
            list($grade, $hr_class) = explode("-", $cd);
        }

        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            //新入生読込
            if ($cd == "00-000" || $cd == "00-001"){
                $query = " SELECT ";
                $query .= "   T3.HR_NAME, ";
                $query .= "   T1.SCHREGNO, ";
                $query .= "   T1.OLD_GRADE, ";
                $query .= "   T1.OLD_HR_CLASS, ";
                $query .= "   T1.OLD_ATTENDNO AS ATTENDNO, ";
                $query .= "   T1.REMAINGRADE_FLG, ";
                $query .= "   case when T1.OLD_GRADE is not null then B2.NAME_SHOW else T2.NAME end as NAME ";
                $query .= " FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 LEFT OUTER JOIN SCHREG_REGD_HDAT T3  ";
                $query .= "   ON T3.YEAR = '" .CTRL_YEAR ."' AND  ";
                $query .= "   T3.SEMESTER = '" .CTRL_SEMESTER ."' AND  ";
                $query .= "   T1.OLD_GRADE = T3.GRADE AND  ";
                $query .= "   T1.OLD_HR_CLASS = T3.HR_CLASS LEFT OUTER JOIN FRESHMAN_DAT T2  ";
                $query .= "   ON T1.SCHREGNO = T2.SCHREGNO AND  ";
                $query .= "   T1.YEAR = T2.ENTERYEAR ";
                $query .= "   LEFT OUTER JOIN SCHREG_BASE_MST B2 ON T1.SCHREGNO = B2.SCHREGNO ";
                $query .= " WHERE ";
                if ($cd == "00-000") {
                    $query .= "   T1.GRADE = '01' AND ";
                    $query .= "   T1.REMAINGRADE_FLG = '0' AND ";
                } else {
                    $query .= "   T1.GRADE = '04' AND ";
                    $query .= "   T1.REMAINGRADE_FLG = '0' AND ";
                    $query .= "   T1.OLD_GRADE IS NULL AND ";
                }
                $query .= "   T1.YEAR = '" .(CTRL_YEAR+1) ."' AND ";
                $query .= "   T1.SEMESTER = '" .$model->min_semester ."' AND ";
                $query .= "   (T1.HR_CLASS IS NULL OR T1.HR_CLASS = '') AND ";
                $query .= "   T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
                $query .= " ORDER BY ";
                $query .= "   OLD_GRADE, ";
                $query .= "   OLD_HR_CLASS, ";
                $query .= "   OLD_ATTENDNO ";
            }else{
                $query = " SELECT ";
                $query .= "   T3.HR_NAME, ";
                $query .= "   T1.SCHREGNO, ";
                $query .= "   T1.OLD_GRADE, ";
                $query .= "   T1.OLD_HR_CLASS, ";
                $query .= "   T1.OLD_ATTENDNO AS ATTENDNO, ";
                $query .= "   T1.REMAINGRADE_FLG, ";
                $query .= "   T2.NAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 LEFT OUTER JOIN SCHREG_REGD_HDAT T3  ";
                $query .= "   ON T3.YEAR   = '" .CTRL_YEAR ."' AND  ";
                $query .= "   T3.SEMESTER  = '" .CTRL_SEMESTER ."' AND  ";
                $query .= "   T1.OLD_GRADE = T3.GRADE AND  ";
                $query .= "   T1.OLD_HR_CLASS = T3.HR_CLASS LEFT OUTER JOIN SCHREG_BASE_MST T2  ";
                $query .= "   ON T1.SCHREGNO = T2.SCHREGNO ";
                //特別支援学校対応
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                    $query .= "   LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T1.SCHREGNO AND G1.YEAR = '".CTRL_YEAR."' AND G1.SEMESTER = '".CTRL_SEMESTER."' ";
                }
                $query .= " WHERE ";
                $query .= "   T1.YEAR         = '" .(CTRL_YEAR+1) ."' AND ";
                $query .= "   T1.SEMESTER     = '" .$model->min_semester ."'  AND ";
                //複式・年組コンボ
                if ($div == "1") {
                    //1:複式
                    $query .= "   G1.GHR_CD = '" .$ghr_cd ."' AND ";
                } else {
                    //2:年組
                    $query .= "   T1.OLD_GRADE    = '" .$grade ."' AND ";
                    $query .= "   T1.OLD_HR_CLASS = '" .$hr_class ."' AND ";
                }
                $query .= "   ( T1.HR_CLASS IS NULL OR T1.HR_CLASS = '') AND ";
                $query .= "   T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
                $query .= " ORDER BY ";
                $query .= "   OLD_GRADE, ";
                $query .= "   OLD_HR_CLASS, ";
                $query .= "   OLD_ATTENDNO ";
            }
        }else{
            $query = " SELECT ";
            $query .= "   ST3.HR_NAME, ";
            $query .= "   ST1.SCHREGNO, ";
            $query .= "   ST1.GRADE, ";
            $query .= "   ST1.HR_CLASS, ";
            $query .= "   ST1.ATTENDNO, ";
            $query .= "   ST2.NAME_SHOW AS NAME, ";
            $query .= "   ST4.REMAINGRADE_FLG ";
            $query .= " FROM ";
            $query .= "   SCHREG_REGD_DAT ST1 ";
            //特別支援学校対応
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "   LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = ST1.SCHREGNO AND G1.YEAR = ST1.YEAR AND G1.SEMESTER = ST1.SEMESTER ";
            }
            $query .= "   LEFT OUTER JOIN CLASS_FORMATION_DAT ST4  ";
            $query .= "   ON ST1.SCHREGNO = ST4.SCHREGNO AND ST4.YEAR = '" .CTRL_YEAR ."' AND ST4.SEMESTER = '" .(CTRL_SEMESTER+1) ."', ";
            $query .= "   SCHREG_BASE_MST ST2, ";
            $query .= "   SCHREG_REGD_HDAT ST3 ";
            $query .= " WHERE ";
            $query .= "   ST1.SCHREGNO = ST2.SCHREGNO AND ";
            //HR割振り済みの生徒は省く
            $query .= "   ST1.SCHREGNO NOT IN ( SELECT SCHREGNO FROM CLASS_FORMATION_DAT ";
            $query .= "                          WHERE YEAR =  '" .CTRL_YEAR ."'";
            $query .= "                            AND SEMESTER = '" .(CTRL_SEMESTER+1) ."'";
            $query .= "                            AND (HR_CLASS IS NOT NULL OR HR_CLASS <> '')) AND ";

            $query .= "   ST1.SCHREGNO NOT IN ('" .implode($model->schregno ,"','"). "') AND ";

            $query .= "   ST1.YEAR = '".CTRL_YEAR ."' AND ";
            $query .= "   ST1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
            //複式・年組コンボ
            if ($div == "1") {
                //1:複式
                $query .= "   G1.GHR_CD = '" .$ghr_cd ."' AND ";
            } else {
                //2:年組
                $query .= "   ST1.GRADE = '".$grade ."' AND ";
                $query .= "   ST1.HR_CLASS = '".$hr_class ."' AND ";
            }
            $query .= "   ST1.YEAR = ST3.YEAR AND ";
            $query .= "   ST1.SEMESTER = ST3.SEMESTER AND ";
            $query .= "   ST1.GRADE = ST3.GRADE AND ";
            $query .= "   ST1.HR_CLASS = ST3.HR_CLASS ";
            $query .= " ORDER BY ";
            $query .= "   ST1.GRADE, ";
            $query .= "   ST1.HR_CLASS, ";
            $query .= "   ST1.ATTENDNO ";

        }
        return $query;
    }

    function &getUpdateQuery($model)
    {
        //近代フラグ取得
        $kindai_flg = knja080bQuery::getNameMstCount();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($div, $cd) = explode(":", $model->newclass);
        list($grade, $hr_class) = explode("-", $cd);

        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            
            $query = " UPDATE ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " SET ";
            $query .= "   GRADE     = '" .$grade ."', ";
            $query .= "   HR_CLASS  = '" .$hr_class ."' ";
            //近代フラグをチェック
            if ($kindai_flg != 0) {
                //HRクラスコース設定からデータ取得＆設定
                $kindaiRow = $db->getRow(knja080bQuery::getSchRegdDitailHdat(CTRL_YEAR+1, $model->min_semester, $grade, $hr_class), DB_FETCHMODE_ASSOC);
                $query .= "   ,COURSECD   = '" .$kindaiRow["COURSECD"] ."' ";
                $query .= "   ,MAJORCD    = '" .$kindaiRow["MAJORCD"] ."' ";
                $query .= "   ,COURSECODE = '" .$kindaiRow["COURSECODE"] ."' ";
            }
            $query .= " WHERE ";
            $query .= "   YEAR      = '" .(CTRL_YEAR+1) ."' AND ";
            $query .= "   SEMESTER  = '" .$model->min_semester ."' AND ";
            $query .= "   SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata) ."') ";

            $db->query($query);
            $array = explode(",", $model->selectdata);
            //移動前の生徒と移動後の生徒を求める
            $array = array_diff($model->schregno, array_intersect($model->schregno, $array));

            //右リストの組をNULLにする
            $query = " UPDATE ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " SET ";
            $query .= "   HR_CLASS  = NULL ";
            $query .= " WHERE ";
            $query .= "   YEAR      = '" .(CTRL_YEAR+1) ."' AND ";
            $query .= "   SEMESTER  = '" .$model->min_semester ."' AND ";
            $query .= "   SCHREGNO IN ('" .implode($array, "','") ."') ";

            $db->query($query);
        }else{
            //現在処理学期が１、２学期の場合の処理
            $query = " DELETE FROM ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR      = '". CTRL_YEAR ."' AND ";
            $query .= "   SEMESTER  = '" .(CTRL_SEMESTER+1) ."' AND ";
            $query .= "   SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";

            $db->query($query);

            //現在の年組の生徒を削除する
            $query = " DELETE FROM ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '". CTRL_YEAR ."' AND ";
            $query .= "   SEMESTER = '" .(CTRL_SEMESTER+1) ."' AND ";
            $query .= "   GRADE    = '" .$grade ."' AND ";
            $query .= "   HR_CLASS = '" .$hr_class ."' ";

            $db->query($query);

            $query = " INSERT INTO CLASS_FORMATION_DAT ( ";
            $query .= " SCHREGNO, ";
            $query .= " YEAR, ";
            $query .= " SEMESTER, ";
            $query .= " GRADE, ";
            $query .= " HR_CLASS, ";
            $query .= " OLD_GRADE, ";
            $query .= " OLD_HR_CLASS, ";
            $query .= " OLD_ATTENDNO, ";
            $query .= " COURSECD, ";
            $query .= " MAJORCD, ";
            $query .= " COURSECODE, ";
            $query .= " UPDATED ";
            $query .= " ) ";
            $query .= "   (SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '". CTRL_YEAR. "' , ";
            $query .= "     '". (CTRL_SEMESTER+1) ."' , ";
            $query .= "     '" .$grade ."' , ";
            $query .= "     '" .$hr_class ."' , ";
            $query .= "     GRADE , ";
            $query .= "     HR_CLASS , ";
            $query .= "     ATTENDNO , ";
            //近代フラグをチェック
            if ($kindai_flg != 0) {
                //HRクラスコース設定からデータ取得＆設定
                $kindaiRow = $db->getRow(knja080bQuery::getSchRegdDitailHdat(CTRL_YEAR, CTRL_SEMESTER+1, $grade, $hr_class), DB_FETCHMODE_ASSOC);
                $query .= "   '" .$kindaiRow["COURSECD"] ."', ";
                $query .= "   '" .$kindaiRow["MAJORCD"] ."', ";
                $query .= "   '" .$kindaiRow["COURSECODE"] ."', ";
            } else {
                $query .= "     COURSECD , ";
                $query .= "     MAJORCD , ";
                $query .= "     COURSECODE , ";
            }
            $query .= "     SYSDATE() ";
            $query .= "   FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= "   WHERE ";
            $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') AND ";
            $query .= "     YEAR = '". CTRL_YEAR. "' AND ";
            $query .= "     SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "   )  ";

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //指定年組がGHRの時の更新処理
    function getUpdateQueryGHR($model) {
        //変数
        $ctrl_year = CTRL_YEAR;
        $ctrl_semester = CTRL_SEMESTER;
        $next_year = CTRL_YEAR + 1;
        $next_semester = CTRL_SEMESTER + 1;

        //近代フラグ取得
        $kindai_flg = knja080bQuery::getNameMstCount();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($div, $cd) = explode(":", $model->newclass);
        list($ghr_cd, $dummy) = explode("-", $cd);

        //左リストの生徒のループ
        $schnoArray = explode(",", $model->selectdata);
        foreach ($schnoArray as $schno) {
            if ($ctrl_semester == $model->control["学期数"]) { //最終学期のとき
                //CLASS_FORMATION_DAT
                //名称マスタ「A032」の年組をセットする
                $query = knja080bQuery::getGradeHrClassNameMst("CLASS_FORMATION_DAT", $schno, $next_year, $model->min_semester);
                $rowA032 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //名称マスタ「A032」の組がない時、なにもしない（右リストに戻る）
                if ($rowA032["HR_CLASS"] == "") continue;
                //UPDATE
                $query = knja080bQuery::getUpdClassFormationDat($db, $schno, $next_year, $model->min_semester, $rowA032["GRADE"], $rowA032["HR_CLASS"], $kindai_flg, "INS");
                $db->query($query);

                //SCHREG_REGD_GHR_DAT
                //DELETE
                $query = knja080bQuery::getDelSchregRegdGhrDat($schno, $next_year, $model->min_semester);
                $db->query($query);
                //INSERT
                $query = knja080bQuery::getInsSchregRegdGhrDat($schno, $next_year, $model->min_semester, $ghr_cd);
                $db->query($query);
            } else {
                //CLASS_FORMATION_DAT
                $query = knja080bQuery::getGradeHrClassNameMst("SCHREG_REGD_DAT", $schno, $ctrl_year, $ctrl_semester);
                $rowA032 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //名称マスタ「A032」の組がない時、なにもしない（右リストに戻る）
                if ($rowA032["HR_CLASS"] == "") continue;
                //DELETE
                $query = knja080bQuery::getDelClassFormationDat($schno, $ctrl_year, $next_semester);
                $db->query($query);
                //INSERT
                $query = knja080bQuery::getSchregRegdDat($schno, $ctrl_year, $ctrl_semester);
                $schregRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $query = knja080bQuery::getInsClassFormationDat($db, $schno, $ctrl_year, $next_semester, $rowA032["GRADE"], $rowA032["HR_CLASS"], $schregRow, $kindai_flg);
                $db->query($query);

                //SCHREG_REGD_GHR_DAT
                //DELETE
                $query = knja080bQuery::getDelSchregRegdGhrDat($schno, $ctrl_year, $next_semester);
                $db->query($query);
                //INSERT
                $query = knja080bQuery::getInsSchregRegdGhrDat($schno, $ctrl_year, $next_semester, $ghr_cd);
                $db->query($query);
            }
        }//foreach

        //移動前の生徒と移動後の生徒を求める
        $array = explode(",", $model->selectdata);
        $array = array_diff($model->schregno, array_intersect($model->schregno, $array));
        //左から右へ移動した生徒のループ
        foreach ($array as $schno) {
            if ($ctrl_semester == $model->control["学期数"]) { //最終学期のとき
                //CLASS_FORMATION_DAT
                //名称マスタ「A032」の年組をセットする
                $query = knja080bQuery::getGradeHrClassNameMst("CLASS_FORMATION_DAT", $schno, $next_year, $model->min_semester);
                $rowA032 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //名称マスタ「A032」の組がない時、なにもしない（左リストに戻る）
                if ($rowA032["HR_CLASS"] == "") continue;
                //UPDATE
                $query = knja080bQuery::getUpdClassFormationDat($db, $schno, $next_year, $model->min_semester, $rowA032["GRADE"], $rowA032["HR_CLASS"], $kindai_flg, "DEL");
                $db->query($query);

                //SCHREG_REGD_GHR_DAT
                //DELETE
                $query = knja080bQuery::getDelSchregRegdGhrDat($schno, $next_year, $model->min_semester);
                $db->query($query);
            } else {
                //CLASS_FORMATION_DAT
                $query = knja080bQuery::getGradeHrClassNameMst("SCHREG_REGD_DAT", $schno, $ctrl_year, $ctrl_semester);
                $rowA032 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //名称マスタ「A032」の組がない時、なにもしない（左リストに戻る）
                if ($rowA032["HR_CLASS"] == "") continue;
                //DELETE
                $query = knja080bQuery::getDelClassFormationDat($schno, $ctrl_year, $next_semester);
                $db->query($query);

                //SCHREG_REGD_GHR_DAT
                //DELETE
                $query = knja080bQuery::getDelSchregRegdGhrDat($schno, $ctrl_year, $next_semester);
                $db->query($query);
            }
        }//foreach

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
    function getGradeHrClassNameMst($table, $schno, $year, $semester) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     N1.NAME1 AS HR_CLASS ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A032' AND N1.NAMECD2 = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO  = '{$schno}' ";
        $query .= "     AND T1.YEAR      = '{$year}' ";
        $query .= "     AND T1.SEMESTER  = '{$semester}' ";
        return $query;
    }
    function getUpdClassFormationDat($db, $schno, $year, $semester, $grade, $hr_class, $kindai_flg, $updFlg) {
        $query  = " UPDATE ";
        $query .= "   CLASS_FORMATION_DAT ";
        $query .= " SET ";
        if ($updFlg == "DEL") {
            $query .= "   HR_CLASS  = NULL ";
        } else {
            $query .= "   GRADE     = '{$grade}', "; //TODO:なぜ?
            $query .= "   HR_CLASS  = '{$hr_class}' ";
        }
        //近代フラグをチェック
        if ($kindai_flg != 0) {
            //HRクラスコース設定からデータ取得＆設定
            $kindaiRow = $db->getRow(knja080bQuery::getSchRegdDitailHdat($year, $semester, $grade, $hr_class), DB_FETCHMODE_ASSOC);
            $query .= "   ,COURSECD   = '" .$kindaiRow["COURSECD"] ."' ";
            $query .= "   ,MAJORCD    = '" .$kindaiRow["MAJORCD"] ."' ";
            $query .= "   ,COURSECODE = '" .$kindaiRow["COURSECODE"] ."' ";
        }
        $query .= " WHERE ";
        $query .= "         SCHREGNO  = '{$schno}' ";
        $query .= "     AND YEAR      = '{$year}' ";
        $query .= "     AND SEMESTER  = '{$semester}' ";
        return $query;
    }
    function getDelClassFormationDat($schno, $year, $semester) {
        $query  = " DELETE FROM ";
        $query .= "     CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO  = '{$schno}' ";
        $query .= "     AND YEAR      = '{$year}' ";
        $query .= "     AND SEMESTER  = '{$semester}' ";
        return $query;
    }
    function getInsClassFormationDat($db, $schno, $year, $semester, $grade, $hr_class, $schregRow, $kindai_flg) {
        $query  = " INSERT INTO CLASS_FORMATION_DAT ( ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     OLD_GRADE, ";
        $query .= "     OLD_HR_CLASS, ";
        $query .= "     OLD_ATTENDNO, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " VALUES ( ";
        $query .= "     '{$schno}', ";
        $query .= "     '{$year}', ";
        $query .= "     '{$semester}', ";
        $query .= "     '{$grade}', ";
        $query .= "     '{$hr_class}', ";
        $query .= "     '".$schregRow["GRADE"]."', ";
        $query .= "     '".$schregRow["HR_CLASS"]."', ";
        $query .= "     '".$schregRow["ATTENDNO"]."', ";
        //近代フラグをチェック
        if ($kindai_flg != 0) {
            //HRクラスコース設定からデータ取得＆設定
            $kindaiRow = $db->getRow(knja080bQuery::getSchRegdDitailHdat($year, $semester, $grade, $hr_class), DB_FETCHMODE_ASSOC);
            $query .= "   '" .$kindaiRow["COURSECD"] ."', ";
            $query .= "   '" .$kindaiRow["MAJORCD"] ."', ";
            $query .= "   '" .$kindaiRow["COURSECODE"] ."', ";
        } else {
            $query .= "   '".$schregRow["COURSECD"]."', ";
            $query .= "   '".$schregRow["MAJORCD"]."', ";
            $query .= "   '".$schregRow["COURSECODE"]."', ";
        }
        $query .= "     SYSDATE() ";
        $query .= " ) ";
        return $query;
    }
    function getSchregRegdDat($schno, $year, $semester) {
        $query  = " SELECT * FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO  = '{$schno}' ";
        $query .= "     AND YEAR      = '{$year}' ";
        $query .= "     AND SEMESTER  = '{$semester}' ";
        return $query;
    }
    function getDelSchregRegdGhrDat($schno, $year, $semester) {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO  = '{$schno}' ";
        $query .= "     AND YEAR      = '{$year}' ";
        $query .= "     AND SEMESTER  = '{$semester}' ";
        return $query;
    }
    function getInsSchregRegdGhrDat($schno, $year, $semester, $ghr_cd) {
        $query  = " INSERT INTO SCHREG_REGD_GHR_DAT ( ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GHR_CD, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " VALUES ( ";
        $query .= "     '{$schno}', ";
        $query .= "     '{$year}', ";
        $query .= "     '{$semester}', ";
        $query .= "     '{$ghr_cd}', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " ) ";
        return $query;
    }

    //ヘッダのクラス取得
    function GetClass($year, $semester)
    {
        $query  = "SELECT T1.grade, INTEGER(T1.hr_class) AS hr_class, T1.tr_cd1, T2.STAFFNAME_SHOW AS name";
        $query .= "  FROM schreg_regd_hdat T1";
        $query .= "       LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1   = T2.staffcd";
        $query .= " WHERE T1.year     = '".$year."'";
        $query .= "   AND T1.semester = '".$semester."'";
        $query .= " ORDER BY grade, hr_class";

        return $query;
    }

    //次年度の最小の学期を求める
    function getNextYearMinSemes($model){
        $query = " SELECT ";
        $query .= "   MIN(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".(CTRL_YEAR+1) ."' ";

        return $query;
    }

    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        $query .= "    NAMESPARE1, ";
        $query .= "    NAMESPARE2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";
        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $rtnRow;
    }
    
    function getNameMstCount() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z013' AND ";
        $query .= "    NAMECD2='00' AND ";
        $query .= "    NAME1='1' ";
       
        $db = Query::dbCheckOut();
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }
    
    function getSchRegdDitailHdat($year, $semester, $grade, $hr_class) {
        
        $query  = " SELECT ";
        $query .= "      COURSECD";
        $query .= "     ,MAJORCD";
        $query .= "     ,COURSECODE";
        $query .= " FROM schreg_regd_diteil_hdat ";
        $query .= " WHERE year   = '".$year."'";
        $query .= " AND semester = '".$semester."'";
        $query .= " AND grade    = '".$grade."'";
        $query .= " AND hr_class = '".$hr_class."'"; 
        
        return $query;
    }
}
?>
