<?php

require_once('for_php7.php');

class knjf016Query extends Query {
    //年組取得(実クラス)
    function getGhrCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_CD AS VALUE, ";
        $query .= "     T1.GHR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_CD ";

        return $query;
    }

    //年組取得(FI複式)
    function getFiGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.GRADE IN (SELECT ";
                $query .= "                          REGD_G.GRADE ";
                $query .= "                      FROM ";
                $query .= "                          SCHREG_REGD_GDAT REGD_G ";
                $query .= "                      WHERE ";
                $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' ";
                $query .= "                          AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                          REGD_G.GRADE ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT REGD_G ";
            $query .= "                      WHERE ";
            $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' ";
            $query .= "                          AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     SEMESTER    = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得(実クラス)
    function getMedexamListSS($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS REGD_SCHREGNO, ";
        $query .= "     T1.GHR_ATTENDNO AS ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.SEX, ";
        $query .= "     L2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DET_NO_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SCHREGNO = T1.SCHREGNO AND L2.NO = '{$model->no}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        $query .= "     T1.GHR_CD   = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_ATTENDNO ";

        return $query;
    }

    //データ一覧取得(FI複式)
    function getMedexamListFi($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS REGD_SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.SEX, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     L2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN MEDEXAM_DET_NO_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SCHREGNO = T1.SCHREGNO AND L2.NO = '{$model->no}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ一覧取得
    function getMedexamList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS REGD_SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.SEX, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     L2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN MEDEXAM_DET_NO_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SCHREGNO = T1.SCHREGNO AND L2.NO = '{$model->no}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //健康診断テーブルの存在チェック
    function checkMedexamData($model, $table, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     SCHREGNO    = '{$schregno}' ";
        if ($table == "MEDEXAM_DET_NO_DAT") {
            $query .= " AND NO      = '{$model->no}' ";
        }

        return $query;
    }

    //健康診断身体回数データ
    function getUpdateMedexamDetNoDat($db, $model, $g) {
        $data = array();
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["NO"][TEXT]                   = $model->no;
        $data["DATE"][TEXT]                 = str_replace("/", "-", $model->fields["DATE"][$g]);
        $data["HEIGHT"][NUMBER]             = $model->fields["HEIGHT"][$g];
        $data["WEIGHT"][NUMBER]             = $model->fields["WEIGHT"][$g];
        $data["SITHEIGHT"][NUMBER]          = $model->fields["SITHEIGHT"][$g];
        $data["R_BAREVISION_MARK"][TEXT]    = $model->fields["R_BAREVISION_MARK"][$g];
        $data["L_BAREVISION_MARK"][TEXT]    = $model->fields["L_BAREVISION_MARK"][$g];
        $data["R_VISION_MARK"][TEXT]        = $model->fields["R_VISION_MARK"][$g];
        $data["L_VISION_MARK"][TEXT]        = $model->fields["L_VISION_MARK"][$g];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = knjf016Query::checkMedexamData($model, "MEDEXAM_DET_NO_DAT", $schregno);
        if ($db->getOne($query) > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR        = '{$model->year}' AND ";
            $where .= "     SCHREGNO    = '{$schregno}' AND ";
            $where .= "     NO          = '{$model->no}' ";
            $query = Query::updateSQL($data, "MEDEXAM_DET_NO_DAT", $where);
            $db->query($query);
        } else if ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all NULL）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
            $query = Query::insertSQL($data, "MEDEXAM_DET_NO_DAT");
            $db->query($query);
        }
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //健康診断身体回数データ
            knjf016Query::getUpdateMedexamDetNoDat($db, $model, $g);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
