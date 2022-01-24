<?php

require_once('for_php7.php');

class knje156aQuery extends Query
{
    //年組リスト
    public function getHrClassAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='{$model->year}'";
        $query .= "AND SEMESTER='{$model->semester}' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" . STAFFCD . "' ";
            $query .= " OR TR_CD2 = '" . STAFFCD . "' ";
            $query .= " OR TR_CD3 = '" . STAFFCD . "') ";
        }

        return $query;
    }

    //登録データ
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAME || '-' || T1.ATTENDNO || '番' AS ATTEND, ";
        $query .= "     T3.NAME_SHOW AS NAME, ";
        $query .= "     VAL1.RECORD AS REC1, ";
        $query .= "     VAL2.RECORD AS REC2, ";
        $query .= "     VAL3.RECORD AS REC3, ";
        $query .= "     VAL4.RECORD AS REC4, ";
        $query .= "     VAL5.RECORD AS REC5, ";
        $query .= "     VAL6.RECORD AS REC6, ";
        $query .= "     VAL7.RECORD AS REC7, ";
        $query .= "     VAL8.RECORD AS REC8, ";
        $query .= "     VAL9.RECORD AS REC9, ";
        $query .= "     VAL10.RECORD AS REC10, ";
        $query .= "     VAL11.RECORD AS REC11, ";
        $query .= "     VAL12.RECORD AS REC12, ";
        $query .= "     VAL13.RECORD AS REC13 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T3 ";
        $query .= "     ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL1 ";
        $query .= "     ON T1.YEAR = VAL1.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL1.SCHREGNO ";
        $query .= "     AND VAL1.DIV = '1' ";
        $query .= "     AND VAL1.CODE = '01' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL2 ";
        $query .= "     ON T1.YEAR = VAL2.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL2.SCHREGNO ";
        $query .= "     AND VAL2.DIV = '1' ";
        $query .= "     AND VAL2.CODE = '02' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL3 ";
        $query .= "     ON T1.YEAR = VAL3.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL3.SCHREGNO ";
        $query .= "     AND VAL3.DIV = '1' ";
        $query .= "     AND VAL3.CODE = '03' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL4 ";
        $query .= "     ON T1.YEAR = VAL4.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL4.SCHREGNO ";
        $query .= "     AND VAL4.DIV = '1' ";
        $query .= "     AND VAL4.CODE = '04' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL5 ";
        $query .= "     ON T1.YEAR = VAL5.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL5.SCHREGNO ";
        $query .= "     AND VAL5.DIV = '1' ";
        $query .= "     AND VAL5.CODE = '05' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL6 ";
        $query .= "     ON T1.YEAR = VAL6.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL6.SCHREGNO ";
        $query .= "     AND VAL6.DIV = '1' ";
        $query .= "     AND VAL6.CODE = '06' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL7 ";
        $query .= "     ON T1.YEAR = VAL7.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL7.SCHREGNO ";
        $query .= "     AND VAL7.DIV = '1' ";
        $query .= "     AND VAL7.CODE = '07' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL8 ";
        $query .= "     ON T1.YEAR = VAL8.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL8.SCHREGNO ";
        $query .= "     AND VAL8.DIV = '1' ";
        $query .= "     AND VAL8.CODE = '08' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL9 ";
        $query .= "     ON T1.YEAR = VAL9.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL9.SCHREGNO ";
        $query .= "     AND VAL9.DIV = '1' ";
        $query .= "     AND VAL9.CODE = '09' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL10 ";
        $query .= "     ON T1.YEAR = VAL10.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL10.SCHREGNO ";
        $query .= "     AND VAL10.DIV = '1' ";
        $query .= "     AND VAL10.CODE = '10' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL11 ";
        $query .= "     ON T1.YEAR = VAL11.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL11.SCHREGNO ";
        $query .= "     AND VAL11.DIV = '2' ";
        $query .= "     AND VAL11.CODE = '01' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL12 ";
        $query .= "     ON T1.YEAR = VAL12.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL12.SCHREGNO ";
        $query .= "     AND VAL12.DIV = '2' ";
        $query .= "     AND VAL12.CODE = '02' ";
        $query .= " LEFT JOIN ";
        $query .= "     BEHAVIOR_DAT VAL13 ";
        $query .= "     ON T1.YEAR = VAL13.YEAR ";
        $query .= "     AND T1.SCHREGNO = VAL13.SCHREGNO ";
        $query .= "     AND VAL13.DIV = '2' ";
        $query .= "     AND VAL13.CODE = '03' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.HR_CLASS= '{$model->hrClass}' ";
        $query .= "  ";

        return $query;
    }
    //登録データ
    public function deleteBehaviorDat($model, $schregno)
    {
        $query .= " DELETE ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO='{$schregno}' ";
        $query .= "     AND ((DIV = '1' AND CODE IN ('01','02','03','04','05','06','07','08','09','10'))  ";
        $query .= "     OR (DIV = '2' AND CODE IN ('01','02','03'))) ";

        return $query;
    }
    //登録データ
    public function insertBehaviorDat($db, $model, $row, $annual)
    {
        for ($i = 1; $i < 14; $i++) {
            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SCHREGNO"][TEXT]     = $row['SCHREGNO'];
            $data["DIV"][TEXT]          = ($i <= 10) ? '1' : '2';
            $data["CODE"][TEXT]         = sprintf("%02d", ($i<=10)?$i:($i-10));
            $data["ANNUAL"][TEXT]       = $annual;
            $data["RECORD"][TEXT]       = $row['REC'.$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
        }
    }

    //年次の取得
    public function getAnnual($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '{$schregno}' AND ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     SEMESTER    = '{$model->semester}' ";

        return $query;
    }

    //特別活動の記録の観点更新
    public function updateSpecialActRemark($db, $model)
    {
        if ($model->specialActRemark == '') {
            return;
        }
        $data["SPECIALACTREMARK"][TEXT]     = $model->specialActRemark;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= "     AND VALUE(SPECIALACTREMARK,'') = '' ";
        $where .= "     AND SCHREGNO IN ('".implode("','", $model->schregnos)."') ";

        $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
    }
    //学級活動更新
    public function updateClassAct($db, $model)
    {
        if ($model->classAct == '') {
            return;
        }
        $data["CLASSACT"][TEXT]     = $model->classAct;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= "     AND VALUE(CLASSACT,'') = '' ";
        $where .= "     AND SCHREGNO IN ('".implode("','", $model->schregnos)."') ";

        $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
    }
    //生徒会活動更新
    public function updateStudentAct($db, $model)
    {
        if ($model->studentAct == '') {
            return;
        }
        $data["STUDENTACT"][TEXT]     = $model->studentAct;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= "     AND VALUE(STUDENTACT,'') = '' ";
        $where .= "     AND SCHREGNO IN ('".implode("','", $model->schregnos)."') ";

        $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
    }
    //学校行事更新
    public function updateSchoolEvent($db, $model)
    {
        if ($model->schoolEvent == '') {
            return;
        }
        $data["SCHOOLEVENT"][TEXT]     = $model->schoolEvent;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->year}' ";
        $where .= "     AND VALUE(SCHOOLEVENT,'') = '' ";
        $where .= "     AND SCHREGNO IN ('".implode("','", $model->schregnos)."') ";

        $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
    }
}
