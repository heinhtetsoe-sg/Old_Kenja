<?php

require_once('for_php7.php');

class knja133kQuery extends Query
{

    //Z010
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'K' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'K' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T2.TR_CD1    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD2    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD3    = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getHrClassAuth($model, $flg, $selectdata = "")
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND = 'K' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T2.TR_CD1    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD2    = '".STAFFCD."' ";
            $query .= "  OR  T2.TR_CD3    = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "  OR  T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        if ($flg == 'list' && $selectdata) {
            $query .= "     AND T2.GRADE || T2.HR_CLASS NOT IN('".implode("','", $selectdata)."') ";
        }
        if ($flg == 'select' && $selectdata) {
            $query .= "     AND T2.GRADE || T2.HR_CLASS IN('".implode("','", $selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //幼児一覧取得
    public function getStudentList($model, $flg, $selectdata = "")
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        if ($flg == 'list' && $selectdata) {
            $query .= "     AND T1.SCHREGNO NOT IN('".implode("','", $selectdata)."') ";
        }
        if ($flg == 'select' && $selectdata) {
            $query .= "     AND T1.SCHREGNO IN('".implode("','", $selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //幼児一覧取得
    public function getHrclassStudentList($hrclass = "")
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode("','", $hrclass)."') ";
        return $query;
    }

    //データ取得
    public function getHtrainremarkDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ取得
    public function getOpinionsDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.OPINION ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1, ";
        $query .= "     ATTEST_OPINIONS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.CHAGE_OPI_SEQ = T2.SEQ ";

        return $query;
    }

    //年度取得
    public function getOpinionsWk($schregno)
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //行動記録の取得
    public function getBehavior($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DIV IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //学習記録データ取得
    public function getStudyRec($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    public function getAttendRec($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //INSERT
    public function &getInsertSql($db, $model)
    {

        $select_data = "";
        if ($model->field["OUTPUT"] == 2 && $model->select_data != "") {
            $hrclass = ($model->select_data) ? explode(',', $model->select_data) : array();
            $query = knja133kQuery::getHrclassStudentList($hrclass);
            $result = $db->query($query);
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $select_data = $select_data.$sep.$row["VALUE"];
                $sep = ",";
            }
            $result->free();
        } else {
            $select_data = $model->select_data;
        }
        $array = explode(",", $select_data);

        $db->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                $query  = " DELETE FROM ATTEST_OPINIONS_UNMATCH WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);

                $year = $db->getCol(knja133kQuery::getOpinionsWk($array[$i]));

                foreach ($year as $key => $val) {
                    $remark = $db->getRow(knja133kQuery::getHtrainremarkDat($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //BEHAVIOR_DAT
                    $behavior = "";
                    $resultb = $db->query(knja133kQuery::getBehavior($val, $array[$i]));
                    while ($rowb = $resultb->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $behavior .= $rowb["DIV"].$rowb["CODE"].$rowb["ANNUAL"];
                    }

                    //SCHREG_STUDYREC_DAT
                    $query = knja133kQuery::getStudyRec($val, $array[$i]);
                    $result = $db->query($query);
                    $study = "";
                    while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        } else {
                            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        }
                    }

                    //SCHREG_ATTENDREC_DAT
                    $attend = $db->getRow(knja133kQuery::getAttendRec($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //現在データのHASHを取得
                    $makeHash = $model->makeHash($remark, $behavior, $study, $attend);

                    $opinion = $db->getRow(knja133kQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
                    $hash_opinion = $opinion["OPINION"];

                    if ($makeHash != $hash_opinion) {
                        $data["YEAR"][TEXT]                 = $val;
                        $data["SCHREGNO"][TEXT]             = $array[$i];
                        $data["FLG"][TEXT]                  = "1";
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "sysdate()";

                        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_UNMATCH"));
                    }
                }
            }
        }

        $db->commit(); //トランザクションをコミットする。
        return;
    }
}
