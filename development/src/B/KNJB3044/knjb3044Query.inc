<?php

require_once('for_php7.php');

class knjb3044Query extends Query {

    // 年度学期取得
    function getYearSemester($model) {

        $query  = " SELECT ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER AS VALUE, ";
        $query .= "     SEME.YEAR || '年度 ' || SEME.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 科目展開表
    function getSchPtrnPreHdat($model) {
        $query  = " SELECT ";
        $query .= "     PRESEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_PRE_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     PRESEQ ";

        return $query;
    }

    // 時間割カウント
    function getSchChrCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";

        return $query;
    }

    // 講座データ存在チェック
    function getChairDatCnt($model) {

        $query = "";
        $query .= " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";

        return $query;
    }









    // 基本時間割
    function getSchPtrnHdat($model) {
        $query  = " SELECT ";
        $query .= "     BSCSEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     BSCSEQ ";

        return $query;
    }






    //データ作成
    function delInsQuery($db, $model) {

        // 講座情報を作成するためのデータ
        $query = knjb3044Query::getCreateChairData($model);
        $result = $db->query($query);

        $preChairDat = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $hrClass = $row["GRADE"]."-".$row["HR_CLASS"];

            $preChairDat[$subclassCd][$hrClass][] = $row;
        }
        $result->free();

        // 講座情報
        knjb3044Query::getDelInsChairDat($db, $model, $preChairDat);

        // 講座クラス
        knjb3044Query::getDelInsChairClsDat($db, $model, $preChairDat);

        // 講座受講生
        knjb3044Query::getDelInsChairStdDat($db, $model, $preChairDat);

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


    // 講座情報作成用のデータ取得
    function getCreateChairData($model) {

        $query = "";
        $query .= " WITH MAXRIREKI AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , MAX(RIREKI_CODE) MAX_RIREKI ";
        $query .= "     , SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , SCHREGNO ";
        $query .= " ) ";
        $query .= " , RISYU AS ( ";
        $query .= "   SELECT ";
        $query .= "     STDSEL.* ";
        $query .= "   FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT STDSEL ";
        $query .= "     INNER JOIN MAXRIREKI ";
        $query .= "       ON STDSEL.YEAR = MAXRIREKI.YEAR ";
        $query .= "       AND STDSEL.SEMESTER = MAXRIREKI.SEMESTER ";
        $query .= "       AND STDSEL.RIREKI_CODE = MAXRIREKI.MAX_RIREKI ";
        $query .= "       AND STDSEL.SCHREGNO = MAXRIREKI.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     STDSEL.YEAR = '{$model->year}' ";
        $query .= "     AND STDSEL.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " , CREDIT AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "     , GRADE ";
        $query .= "     , COURSECODE ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , CREDITS ";
        $query .= "   FROM ";
        $query .= "     CREDIT_MST ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ) ";
        $query .= " , PRE AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , GRADE ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "     , COURSECODE ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     SCH_PTRN_PRE_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND PRESEQ = ".$model->field["PRESEQ"]." ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , GRADE ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "     , COURSECODE ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= " ) ";
        $query .= " , REGD AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , REGD.YEAR ";
        $query .= "     , REGD.SEMESTER ";
        $query .= "     , REGD.GRADE ";
        $query .= "     , REGD.COURSECD ";
        $query .= "     , REGD.MAJORCD ";
        $query .= "     , REGD.COURSECODE ";
        $query .= "     , REGD.HR_CLASS ";
        $query .= "     , REGDH.HR_NAME ";
        $query .= "     , REGDH.HR_NAMEABBV ";
        $query .= "     , REGD.ATTENDNO ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "       ON REGD.YEAR = REGDH.YEAR ";
        $query .= "       AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "       AND REGD.GRADE = REGDH.GRADE ";
        $query .= "       AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   PRE.CLASSCD ";
        $query .= "   , PRE.SCHOOL_KIND ";
        $query .= "   , PRE.CURRICULUM_CD ";
        $query .= "   , PRE.SUBCLASSCD ";
        $query .= "   , SUB.SUBCLASSNAME ";
        $query .= "   , SUB.SUBCLASSABBV ";
        $query .= "   , CREDIT.CREDITS ";
        $query .= "   , REGD.SCHREGNO ";
        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGD.HR_NAME ";
        $query .= "   , REGD.HR_NAMEABBV ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= " FROM ";
        $query .= "   PRE ";
        $query .= "   INNER JOIN RISYU ";
        $query .= "     ON PRE.YEAR = RISYU.YEAR ";
        $query .= "     AND PRE.SEMESTER = RISYU.SEMESTER ";
        $query .= "     AND PRE.CLASSCD = RISYU.CLASSCD ";
        $query .= "     AND PRE.SCHOOL_KIND = RISYU.SCHOOL_KIND ";
        $query .= "     AND PRE.CURRICULUM_CD = RISYU.CURRICULUM_CD ";
        $query .= "     AND PRE.SUBCLASSCD = RISYU.SUBCLASSCD ";

        $query .= "   INNER JOIN CREDIT ";
        $query .= "     ON PRE.YEAR = CREDIT.YEAR ";
        $query .= "     AND PRE.COURSECD = CREDIT.COURSECD ";
        $query .= "     AND PRE.MAJORCD = CREDIT.MAJORCD ";
        $query .= "     AND PRE.GRADE = CREDIT.GRADE ";
        $query .= "     AND PRE.COURSECODE = CREDIT.COURSECODE ";
        $query .= "     AND PRE.CLASSCD = CREDIT.CLASSCD ";
        $query .= "     AND PRE.SCHOOL_KIND = CREDIT.SCHOOL_KIND ";
        $query .= "     AND PRE.CURRICULUM_CD = CREDIT.CURRICULUM_CD ";
        $query .= "     AND PRE.SUBCLASSCD = CREDIT.SUBCLASSCD ";
        
        $query .= "   INNER JOIN REGD ";
        $query .= "     ON PRE.YEAR = REGD.YEAR ";
        $query .= "     AND PRE.SEMESTER = REGD.SEMESTER ";
        $query .= "     AND PRE.GRADE = REGD.GRADE ";
        $query .= "     AND PRE.COURSECD = REGD.COURSECD ";
        $query .= "     AND PRE.MAJORCD = REGD.MAJORCD ";
        $query .= "     AND PRE.COURSECODE = REGD.COURSECODE ";
        $query .= "     AND RISYU.SCHREGNO = REGD.SCHREGNO ";

        $query .= "   INNER JOIN V_SUBCLASS_MST SUB ";
        $query .= "     ON PRE.YEAR = SUB.YEAR ";
        $query .= "     AND PRE.CLASSCD = SUB.CLASSCD ";
        $query .= "     AND PRE.SCHOOL_KIND = SUB.SCHOOL_KIND ";
        $query .= "     AND PRE.CURRICULUM_CD = SUB.CURRICULUM_CD ";
        $query .= "     AND PRE.SUBCLASSCD = SUB.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "   PRE.CLASSCD ";
        $query .= "   , PRE.SCHOOL_KIND ";
        $query .= "   , PRE.CURRICULUM_CD ";
        $query .= "   , PRE.SUBCLASSCD ";

        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";

        return $query;
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

    // 講座情報登録
    function getDelInsChairDat($db, $model, $preChairDat) {
        // $query  = " DELETE FROM ";
        // $query .= "     CHAIR_DAT ";
        // $query .= " WHERE YEAR = '{$model->year}' ";
        // $query .= "   AND SEMESTER = '{$model->semester}' ";
        // $db->query($query);

        // 科目毎
        foreach ($preChairDat as $subclassCd => $subclassArray) {

            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclassCd);

            $query  = " DELETE FROM ";
            $query .= "     CHAIR_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";
            $query .= "   AND CLASSCD = '{$classCd}' ";
            $query .= "   AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "   AND CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "   AND SUBCLASSCD = '{$subclassCd}' ";
            $db->query($query);

            $hrClassCnt = 1;

            // HRクラス毎
            foreach ($subclassArray as $hrClass => $hrClassArray) {

                $subclassName = $hrClassArray[0]["SUBCLASSABBV"];
                $hrClassName = $hrClassArray[0]["HR_NAMEABBV"];
                $credits = $hrClassArray[0]["CREDITS"];

                $chairCd = substr($subclassCd, 0, 5).sprintf("%02d", $hrClassCnt);
                $chairName = $subclassName. " ". $hrClassName;

                $query  = " DELETE FROM ";
                $query .= "     CHAIR_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->year}' ";
                $query .= "     AND SEMESTER = '{$model->semester}' ";
                $query .= "     AND CHAIRCD = '{$chairCd}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]           = $model->year;
                $data["SEMESTER"][TEXT]       = $model->semester;

                $data["CHAIRCD"][TEXT]        = $chairCd;
                $data["GROUPCD"][TEXT]        = '0000';
                $data["CLASSCD"][TEXT]        = $classCd;
                $data["SCHOOL_KIND"][TEXT]    = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]  = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]     = $subclassCd;
                $data["CHAIRNAME"][TEXT]      = $chairName;
                $data["CHAIRABBV"][TEXT]      = trim(mb_substr($chairName, 0, 3));
                $data["TAKESEMES"][TEXT]      = '0';
                $data["LESSONCNT"][TEXT]      = $credits;
                $data["COUNTFLG"][TEXT]       = '1';

                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "sysdate()";

                $query = Query::insertSQL($data, "CHAIR_DAT");
                $db->query($query);

                $hrClassCnt++;
            }
        }

        return true;
    }

    // 講座受講クラスデータ登録
    function getDelInsChairClsDat($db, $model, $preChairDat) {
        // $query  = " DELETE FROM ";
        // $query .= "     CHAIR_CLS_DAT ";
        // $query .= " WHERE ";
        // $query .= "     YEAR = '{$model->year}' ";
        // $query .= "     AND SEMESTER = '{$model->semester}' ";
        // $db->query($query);

        // 科目毎
        foreach ($preChairDat as $subclassCd => $subclassArray) {

            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclassCd);

            $query  = " DELETE FROM ";
            $query .= "     CHAIR_CLS_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";
            $query .= "   AND CHAIRCD IN ( ";
            $query .= "     SELECT CHAIRCD ";
            $query .= "     FROM CHAIR_DAT ";
            $query .= "     WHERE YEAR = '{$model->year}' ";
            $query .= "       AND SEMESTER = '{$model->semester}' ";
            $query .= "       AND CLASSCD = '{$classCd}' ";
            $query .= "       AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "       AND CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "       AND SUBCLASSCD = '{$subclassCd}' ";
            $query .= "   ) ";
            $db->query($query);

            $hrClassCnt = 1;

            // HRクラス毎
            foreach ($subclassArray as $hrClass => $hrClassArray) {

                $chairCd = substr($subclassCd, 0, 5).sprintf("%02d", $hrClassCnt);
                list($grade, $hrClassCd) = explode("-", $hrClass);

                $query  = " DELETE FROM ";
                $query .= "     CHAIR_CLS_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->year}' ";
                $query .= "     AND SEMESTER = '{$model->semester}' ";
                $query .= "     AND CHAIRCD = '{$chairCd}' ";
                $query .= "     AND TRGTGRADE = '{$grade}' ";
                $query .= "     AND TRGTCLASS = '{$hrClassCd}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["SEMESTER"][TEXT]     = $model->semester;
                $data["CHAIRCD"][TEXT]      = $chairCd;
                $data["GROUPCD"][TEXT]      = '0000';
                $data["TRGTGRADE"][TEXT]    = $grade;
                $data["TRGTCLASS"][TEXT]    = $hrClassCd;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CHAIR_CLS_DAT");
                $db->query($query);

                $hrClassCnt++;
            }
        }

        return true;
    }

    // 講座受講生徒データ登録
    function getDelInsChairStdDat($db, $model, $preChairDat) {

        // 年度学期の情報取得
        $appDate = "";
        $appEndDate = "";

        $query = " SELECT * ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $appDate = $row["SDATE"];
            $appEndDate = $row["EDATE"];
        }


        // $query  = " DELETE FROM ";
        // $query .= "     CHAIR_STD_DAT ";
        // $query .= " WHERE ";
        // $query .= "     YEAR = '{$model->year}' ";
        // $query .= "     AND SEMESTER = '{$model->semester}' ";
        // $db->query($query);

        // 科目毎
        foreach ($preChairDat as $subclassCd => $subclassArray) {

            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $subclassCd);

            $query  = " DELETE FROM ";
            $query .= "     CHAIR_STD_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";
            $query .= "   AND CHAIRCD IN ( ";
            $query .= "     SELECT CHAIRCD ";
            $query .= "     FROM CHAIR_DAT ";
            $query .= "     WHERE YEAR = '{$model->year}' ";
            $query .= "       AND SEMESTER = '{$model->semester}' ";
            $query .= "       AND CLASSCD = '{$classCd}' ";
            $query .= "       AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "       AND CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "       AND SUBCLASSCD = '{$subclassCd}' ";
            $query .= "   ) ";
            $db->query($query);


            $hrClassCnt = 1;
            // HRクラス毎
            foreach ($subclassArray as $hrClass => $hrClassArray) {

                $chairCd = substr($subclassCd, 0, 5).sprintf("%02d", $hrClassCnt);

                // 受講生毎(SCHREGNO)
                foreach ($hrClassArray as $schregNo => $value) {

                    $query  = " DELETE FROM ";
                    $query .= "     CHAIR_STD_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR = '{$model->year}' ";
                    $query .= "     AND SEMESTER = '{$model->semester}' ";
                    $query .= "     AND CHAIRCD = '{$chairCd}' ";
                    $query .= "     AND SCHREGNO = '{$value["SCHREGNO"]}' ";
                    $db->query($query);

                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["CHAIRCD"][TEXT]      = $chairCd;
                    $data["SCHREGNO"][TEXT]     = $value["SCHREGNO"];
                    $data["APPDATE"][TEXT]      = $appDate;
                    $data["APPENDDATE"][TEXT]   = $appEndDate;
                    // $data["ROW"][TEXT]          = $value["ATTENDNO"];

                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                    $db->query($query);
                }
                $hrClassCnt++;
            }
        }

        return true;
    }

}
?>
