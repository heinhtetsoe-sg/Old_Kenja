<?php

require_once('for_php7.php');

class knjx_z069query extends Query
{

    //年度一覧
    public function getYearSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= "     , CH_CLS.TRGTGRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CH_CLS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = CH_CLS.YEAR ";
        $query .= "         AND GDAT.GRADE = CH_CLS.TRGTGRADE ";
        $query .= " WHERE ";
        $query .= "     CH_CLS.YEAR || CH_CLS.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目一覧
    public function getSubclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS LABEL ";
        $query .= "     , V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V_SUB ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT CH_CLS ";
        $query .= "         ON CH_CLS.YEAR = V_SUB.YEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = V_SUB.YEAR ";
        $query .= "         AND GDAT.GRADE = CH_CLS.TRGTGRADE ";
        $query .= "         AND GDAT.SCHOOL_KIND = V_SUB.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     V_SUB.YEAR || CH_CLS.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE"]) {
            $query .= "     AND CH_CLS.TRGTGRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期チェック
    public function getPypElementDat($data)
    {
        $query  = " SELECT ";
        $query .= "     ELEMENT_CD ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$data["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$data["SEMESTER"]}' ";
        $query .= "     AND GRADE = '{$data["GRADE"]}' ";
        $query .= "     AND CLASSCD = '{$data["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$data["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD = '{$data["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD = '{$data["SUBCLASSCD"]}' ";
        $query .= "     AND ELEMENT_DIV = '{$data["ELEMENT_DIV"]}' ";

        return $query;
    }

    //学期チェック
    public function checkSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$year."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学年チェック
    public function checkGrade($year, $semester, $grade)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CH_CLS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = CH_CLS.YEAR ";
        $query .= "         AND GDAT.GRADE = CH_CLS.TRGTGRADE ";
        $query .= " WHERE ";
        $query .= "     CH_CLS.YEAR = '".$year."' ";
        $query .= "     AND CH_CLS.SEMESTER = '".$semester."' ";
        $query .= "     AND CH_CLS.TRGTGRADE = '".$grade."' ";

        return $query;
    }

    //科目チェック
    public function checkSubclass($year, $semester, $grade, $classcd, $schoolKind, $curriculumCd, $subclassCd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V_SUB ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT CH_CLS ";
        $query .= "         ON CH_CLS.YEAR = V_SUB.YEAR ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = V_SUB.YEAR ";
        $query .= "         AND GDAT.GRADE = CH_CLS.TRGTGRADE ";
        $query .= "         AND GDAT.SCHOOL_KIND = V_SUB.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     V_SUB.YEAR = '".$year."' ";
        $query .= "     AND CH_CLS.SEMESTER = '".$semester."' ";
        $query .= "     AND CH_CLS.TRGTGRADE = '".$grade."' ";
        $query .= "     AND V_SUB.CLASSCD = '".$classcd."' ";
        $query .= "     AND V_SUB.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "     AND V_SUB.CURRICULUM_CD = '".$curriculumCd."' ";
        $query .= "     AND V_SUB.SUBCLASSCD = '".$subclassCd."' ";

        return $query;
    }

    //評価の要素コードチェック
    public function checkElementCd($elementDiv, $elementCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ELEMENT_DIV = '".$elementDiv."' ";
        $query .= "     AND ELEMENT_CD = '".$elementCd."' ";

        return $query;
    }

    //評価の要素データチェック
    public function checkPypElementCd($year, $semester, $grade, $classcd, $schoolKind, $curriculumCd, $subclassCd, $elementDiv, $elementCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= "     AND CLASSCD = '".$classcd."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "     AND CURRICULUM_CD = '".$curriculumCd."' ";
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND ELEMENT_DIV = '".$elementDiv."' ";
        $query .= "     AND ELEMENT_CD = '".$elementCd."' ";

        return $query;
    }


    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /*********************/
            /* PYP_ELEMENT_DAT */
            /*********************/
            $data = array();
            $query = knjx_z069Query::checkPypElementCd($data_arr[$i]["YEAR"], $data_arr[$i]["SEMESTER"], $data_arr[$i]["GRADE"], $data_arr[$i]["CLASSCD"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["CURRICULUM_CD"], $data_arr[$i]["SUBCLASSCD"], $data_arr[$i]["ELEMENT_DIV"], $data_arr[$i]["ELEMENT_CD"]);
            $dataCnt = $db->getOne($query);

            //データセット
            $data["SORT"][TEXT]             = $data_arr[$i]["SORT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                $data["ELEMENT_DIV"][TEXT]      = $data_arr[$i]["ELEMENT_DIV"];
                $data["ELEMENT_CD"][TEXT]      = $data_arr[$i]["ELEMENT_CD"];
                $query = Query::insertSQL($data, "PYP_ELEMENT_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR          = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND SEMESTER      = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "     AND GRADE         = '".$data_arr[$i]["GRADE"]."' ";
                $where .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $where .= "     AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $where .= "     AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                $where .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                $where .= "     AND ELEMENT_DIV   = '".$data_arr[$i]["ELEMENT_DIV"]."' ";
                $where .= "     AND ELEMENT_CD   = '".$data_arr[$i]["ELEMENT_CD"]."' ";

                $query = Query::updateSQL($data, "PYP_ELEMENT_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /************************/
    /**  ＣＳＶ削除で使用  **/
    /************************/
    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /*********************/
            /* PYP_ELEMENT_DAT */
            /*********************/
            $query  = " DELETE FROM ";
            $query .= "     PYP_ELEMENT_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR          = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND SEMESTER      = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND GRADE         = '".$data_arr[$i]["GRADE"]."' ";
            $query .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "     AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
            $query .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            $query .= "     AND ELEMENT_DIV   = '".$data_arr[$i]["ELEMENT_DIV"]."' ";
            $query .= "     AND ELEMENT_CD    = '".$data_arr[$i]["ELEMENT_CD"]."' ";
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , GRADE ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";
        $query .= "     , SORT ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE"]) {
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        }
        if ($model->field["SUBCLASSCD"]) {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , ELEMENT_DIV ";
        $query .= "     , ELEMENT_CD ";

        return $query;
    }
}
