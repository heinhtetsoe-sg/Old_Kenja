<?php
class knjx_hexam_entremark_trainref_selectQuery extends Query
{
    //年度コンボ
    public function getYear($schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR AS LABEL, ";
        $query .= "     T1.YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "   AND T1.ANNUAL = T2.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //調査書6分割取得
    public function getHexamTrainRef($model)
    {
        $query  = "     SELECT ";
        $query .= "         TRAIN_SEQ, ";
        $query .= "         REMARK ";
        $query .= "     FROM ";
        // プロパテイーtyousasyo_shokenTable_Seq = 1 の場合 HEXAM_ENTREMARK_TRAINREF_SEQ_DAT とする
        if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
            $query .= "         HEXAM_ENTREMARK_TRAINREF_SEQ_DAT ";
        } else {
            $query .= "         HEXAM_ENTREMARK_TRAINREF_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRAIN_SEQ BETWEEN '101' AND '106' ";
        // プロパテイーtyousasyo_shokenTable_Seq = 1 の場合 PATTERN_SEQ = 1を指定する
        if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
            $query .= "         AND PATTERN_SEQ = '1'";
        }
        $query .= "     ORDER BY ";
        $query .= "         TRAIN_SEQ ";

        return $query;
    }

    //名前
    public function getName($schregno)
    {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //学年コンボ
    public function getGrade($model)
    {
        $query  = " WITH SCHKIND AS (";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "             ON T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.GRADE = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "         AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     ), YEAR_GRADE AS ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             T1.YEAR, ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.GRADE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     ) ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR || '年度 ' || CASE ";
        $query .= "             WHEN T3.GRADE_CD IS NOT NULL ";
        $query .= "                 THEN CAST(INT (T3.GRADE_CD) AS CHAR (1)) || '学年 ' ";
        $query .= "             ELSE '' ";
        $query .= "             END AS LABEL, ";
        $query .= "         T1.YEAR AS VALUE ";
        $query .= "     FROM ";
        // プロパテイーtyousasyo_shokenTable_Seq = 1 の場合 HEXAM_ENTREMARK_TRAINREF_SEQ_DAT とする
        if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
            $query .= "         HEXAM_ENTREMARK_TRAINREF_SEQ_DAT T1 ";
        } else {
            $query .= "         HEXAM_ENTREMARK_TRAINREF_DAT T1 ";
        }
        $query .= "         INNER JOIN YEAR_GRADE T2 ";
        $query .= "             ON T2.YEAR      = T1.YEAR ";
        $query .= "             AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "             ON T3.YEAR          = T1.YEAR ";
        $query .= "             AND T3.GRADE        = T2.GRADE ";
        $query .= "         INNER JOIN SCHKIND T4 ";
        $query .= "             ON T4.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND T1.TRAIN_SEQ BETWEEN '101' AND '106' ";
        // プロパテイーtyousasyo_shokenTable_Seq = 1 の場合 PATTERN_SEQ = 1を指定する
        if ($model->Properties['tyousasyo_shokenTable_Seq'] === '1') {
            $query .= "         AND T1.PATTERN_SEQ = '1'";
        }
        $query .= "     ORDER BY ";
        $query .= "         VALUE ";

        return $query;
    }
}
