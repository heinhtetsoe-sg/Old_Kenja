<?php

require_once('for_php7.php');

class knjb3024Query extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目時間割
    function getSchPtrnSubHdat($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_SUBCLASS_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //基本時間割
    function getSchPtrnHdat($model) {
        $query  = " SELECT ";
        $query .= "     BSCSEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " ORDER BY ";
        $query .= "     BSCSEQ ";

        return $query;
    }

    //時間割カウント
    function getSchChrCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";

        return $query;
    }

    //データ作成
    function delInsQuery($db, $model) {
        $setBscSeq = knjb3024Query::getBscSeq($db, $model);

        $query .= " SELECT ";
        $query .= "     PTRN.WEEK_CD || '-' || PTRN.PERIODCD || '-' || PTRN.STAFFCD AS WEEK_PERI_STF, ";
        $query .= "     PTRN.GRADE || '-' || PTRN.HR_CLASS AS GHR, ";
        $query .= "     REGDH.HR_NAMEABBV, ";
        $query .= "     PTRN.CLASSCD || '-' || PTRN.SCHOOL_KIND || '-' ||  PTRN.CURRICULUM_CD || '-' ||  PTRN.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_SUBCLASS_DAT PTRN ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON PTRN.YEAR = REGDH.YEAR ";
        $query .= "          AND REGDH.SEMESTER = '1' ";
        $query .= "          AND PTRN.GRADE = REGDH.GRADE ";
        $query .= "          AND PTRN.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON PTRN.CLASSCD = SUBM.CLASSCD ";
        $query .= "          AND PTRN.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "          AND PTRN.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "          AND PTRN.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     PTRN.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     PTRN.SEQ = {$model->field["SEQ"]} ";
        $query .= " ORDER BY ";
        $query .= "     PTRN.STAFFCD, ";
        $query .= "     PTRN.WEEK_CD, ";
        $query .= "     PTRN.PERIODCD, ";
        $query .= "     PTRN.GRADE, ";
        $query .= "     PTRN.HR_CLASS ";

        $result = $db->query($query);
        //$ptrnKoma[科目][曜日-校時-職員] = 対象クラス(01-001/01-002)
        $ptrnKoma = array();
        $ptrnSubClassAbbv = array();
        $ghrAbbv = array();
        $sep = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = $ptrnKoma[$row['SUBCLASSCD']][$row['WEEK_PERI_STF']] ? '/' : '';
            $ptrnKoma[$row['SUBCLASSCD']][$row['WEEK_PERI_STF']] .= $sep.$row['GHR'];
            $ptrnSubClassAbbv[$row['SUBCLASSCD']] = $row['SUBCLASSABBV'];
            $ghrAbbv[$row['GHR']] = $row['HR_NAMEABBV'];
        }
        $result->free();

        $subClassGhrsChair = array();
        foreach ($ptrnKoma as $subclassCd => $valArray) {
            foreach ($valArray as $weekPeriStf => $ghrs) {
                $dataCnt = get_count($subClassGhrsChair[$subclassCd]);
                list($classCd, $schoolKind, $curri, $setSubclassCd) = explode("-", $subclassCd);
                if ($dataCnt == 0) {
                    $subClassGhrsChair[$subclassCd][$ghrs] = substr($setSubclassCd, 0, 5)."01";
                } else if ($subClassGhrsChair[$subclassCd][$ghrs] > 0) {
                    continue;
                } else {
                    $subClassGhrsChair[$subclassCd][$ghrs] = substr($setSubclassCd, 0, 5).sprintf("%02d", $dataCnt + 1);
                }
            }
        }

        //基本時間割ヘッダ
        knjb3024Query::getDelInsSchPtrnHdat($db, $model, $setBscSeq);

        //基本時間割
        knjb3024Query::getDelInsSchPtrnDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair);

        //講座
        knjb3024Query::getDelInsChairDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair, $ptrnSubClassAbbv, $ghrAbbv);

        //講座職員
        knjb3024Query::getDelInsChairStfDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair);

        //講座クラス
        knjb3024Query::getDelInsChairClsDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair);
    }

    function getBscSeq($db, $model) {
        if ($model->field["BSCSEQ"] != '0') {
            return $model->field["BSCSEQ"];
        } else {
            $query  = " SELECT ";
            $query .= "     MAX(BSCSEQ) AS BSCSEQ ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
            $maxBscSeq = $db->getOne($query);
            $maxBscSeq = $maxBscSeq > 0 ? $maxBscSeq + 1 : 1;
            return $maxBscSeq;
        }
    }

    function getDelInsSchPtrnHdat($db, $model, $setBscSeq) {
        $query  = " DELETE FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND BSCSEQ = {$setBscSeq} ";
        $db->query($query);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_SUBCLASS_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEQ = {$model->field["SEQ"]} ";
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]     = CTRL_YEAR;
            $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
            $data["BSCSEQ"][NUMBER] = $setBscSeq;
            $data["TITLE"][TEXT]    = $row["TITLE"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_HDAT");
            $db->query($query);
        }
        $result->free();

        return true;
    }

    function getDelInsSchPtrnDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair) {
        $query  = " DELETE FROM ";
        $query .= "     SCH_PTRN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND BSCSEQ = {$setBscSeq} ";
        $db->query($query);

        foreach ($ptrnKoma as $subclassCd => $weekPeriStf) {
            foreach ($weekPeriStf as $key => $ghrs) {
                list($dayCd, $peri, $stf) = explode("-", $key);
                $setChairCd = $subClassGhrsChair[$subclassCd][$ghrs];

                $query  = " DELETE FROM ";
                $query .= "     SCH_PTRN_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
                $query .= "     AND BSCSEQ = {$setBscSeq} ";
                $query .= "     AND DAYCD = '{$dayCd}' ";
                $query .= "     AND PERIODCD = '{$peri}' ";
                $query .= "     AND CHAIRCD = '{$setChairCd}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
                $data["BSCSEQ"][NUMBER]   = $setBscSeq;
                $data["DAYCD"][TEXT]      = $dayCd;
                $data["PERIODCD"][TEXT]   = $peri;
                $data["CHAIRCD"][TEXT]    = $setChairCd;
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "SCH_PTRN_DAT");
                $db->query($query);
            }
        }

        return true;
    }

    function getDelInsChairDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair, $ptrnSubClassAbbv, $ghrAbbv) {
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $db->query($query);

        foreach ($ptrnKoma as $subclassCd => $weekPeriStf) {
            foreach ($weekPeriStf as $key => $ghrs) {
                $setChairCd = $subClassGhrsChair[$subclassCd][$ghrs];

                $query  = " DELETE FROM ";
                $query .= "     CHAIR_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
                $query .= "     AND CHAIRCD = '{$setChairCd}' ";
                $db->query($query);

                $ghrArray = explode("/", $ghrs);
                $multipleGhr = get_count($ghrArray) > 1 ? "※" : "";
                $setChairName = $ptrnSubClassAbbv[$subclassCd].$ghrAbbv[$ghrArray[0]].$multipleGhr;
                list($classCd, $schoolKind, $curri, $setSubclassCd) = explode("-", $subclassCd);
                $data = array();
                $data["YEAR"][TEXT]           = CTRL_YEAR;
                $data["SEMESTER"][TEXT]       = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]        = $setChairCd;
                $data["GROUPCD"][TEXT]        = '0000';
                $data["CLASSCD"][TEXT]        = $classCd;
                $data["SCHOOL_KIND"][TEXT]    = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]  = $curri;
                $data["SUBCLASSCD"][TEXT]     = $setSubclassCd;
                $data["CHAIRNAME"][TEXT]      = $setChairName;
                $data["CHAIRABBV"][TEXT]      = $setChairName;
                $data["TAKESEMES"][TEXT]      = '0';
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "sysdate()";

                $query = Query::insertSQL($data, "CHAIR_DAT");
                $db->query($query);
            }
        }

        return true;
    }

    function getDelInsChairStfDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair) {
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $db->query($query);

        foreach ($ptrnKoma as $subclassCd => $weekPeriStf) {
            foreach ($weekPeriStf as $key => $ghrs) {
                list($dayCd, $peri, $stf) = explode("-", $key);
                $setChairCd = $subClassGhrsChair[$subclassCd][$ghrs];

                $query  = " DELETE FROM ";
                $query .= "     CHAIR_STF_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
                $query .= "     AND CHAIRCD = '{$setChairCd}' ";
                $query .= "     AND STAFFCD = '{$stf}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["CHAIRCD"][TEXT]      = $setChairCd;
                $data["STAFFCD"][TEXT]      = $stf;
                $data["CHARGEDIV"][NUMBER]  = 1;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CHAIR_STF_DAT");
                $db->query($query);
            }
        }

        return true;
    }

    function getDelInsChairClsDat($db, $model, $setBscSeq, $ptrnKoma, $subClassGhrsChair) {
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_CLS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $db->query($query);

        foreach ($ptrnKoma as $subclassCd => $weekPeriStf) {
            foreach ($weekPeriStf as $key => $ghrs) {
                $setChairCd = $subClassGhrsChair[$subclassCd][$ghrs];
                $ghrArray = explode("/", $ghrs);

                foreach ($ghrArray as $key => $setGhr) {
                    list($grade, $hrClass) = explode("-", $setGhr);
                    $query  = " DELETE FROM ";
                    $query .= "     CHAIR_CLS_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR = '".CTRL_YEAR."' ";
                    $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
                    $query .= "     AND CHAIRCD = '{$setChairCd}' ";
                    $query .= "     AND TRGTGRADE = '{$grade}' ";
                    $query .= "     AND TRGTCLASS = '{$hrClass}' ";
                    $db->query($query);

                    $data = array();
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                    $data["CHAIRCD"][TEXT]      = $setChairCd;
                    $data["GROUPCD"][TEXT]      = '0000';
                    $data["TRGTGRADE"][TEXT]    = $grade;
                    $data["TRGTCLASS"][TEXT]    = $hrClass;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $query = Query::insertSQL($data, "CHAIR_CLS_DAT");
                    $db->query($query);
                }
            }
        }

        return true;
    }

}
?>
