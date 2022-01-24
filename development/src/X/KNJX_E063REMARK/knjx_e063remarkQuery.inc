<?php

require_once('for_php7.php');
class knjx_e063remarkquery extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = '{$model->getPrgId}' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //学校名取得の取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学期名称取得
    public function getSemesterName($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";

        return $query;
    }

    //年組名称取得
    public function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //存在チェック（学籍番号）
    public function checkSchregNo($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //存在チェック（教育課程）
    public function checkSchregStudyrecDat($data)
    {
        list($classcd, $school_kind) = explode('-', $data["CLASSCD"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$data["YEAR"]."' AND ";
        $query .= "     SCHREGNO        = '".$data["SCHREGNO"]."' AND ";
        $query .= "     ANNUAL          = '".$data["ANNUAL"]."' AND ";
        $query .= "     CLASSCD         = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$data["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$data["SUBCLASSCD"]."' ";

        return $query;
    }

    //マスタの追加(CSVデータより読込)
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            list($classcd, $school_kind) = explode('-', $data_arr[$i]["CLASSCD"]);

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     STUDYRECREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     CLASSCD         = '".$classcd."' AND ";
            $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            $query .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            $flg = (1 > $db->getOne($query)) ? true : false;

            //データセット
            $data = array();
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($flg) {
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];

                $query = Query::insertSQL($data, "STUDYRECREMARK_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR            = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "     CLASSCD         = '".$classcd."' AND ";
                $where .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' ";

                $query = Query::updateSQL($data, "STUDYRECREMARK_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]       = 'KNJX_E063';
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "         T1.GRADE    = '".$model->grade."' AND ";
        $query .= "         T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " ), STUDYREC0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ANNUAL, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         CASE T1.SCHOOLCD WHEN '0' THEN L1.PROV_FLG ELSE NULL END AS PROV_FLG0, ";
        }
        $query .= "         CASE T1.SCHOOLCD WHEN '0' THEN T1.VALUATION ELSE NULL END AS VALUATION0, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '0' THEN T1.GET_CREDIT ELSE NULL END AS GET_CREDIT0, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '0' THEN T1.ADD_CREDIT ELSE NULL END AS ADD_CREDIT0, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '0' THEN T1.COMP_CREDIT ELSE NULL END AS COMP_CREDIT0, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         CASE T1.SCHOOLCD WHEN '1' THEN L1.PROV_FLG ELSE NULL END AS PROV_FLG1, ";
        }
        $query .= "         CASE T1.SCHOOLCD WHEN '1' THEN T1.VALUATION ELSE NULL END AS VALUATION1, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '1' THEN T1.GET_CREDIT ELSE NULL END AS GET_CREDIT1, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '1' THEN T1.ADD_CREDIT ELSE NULL END AS ADD_CREDIT1, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '1' THEN T1.COMP_CREDIT ELSE NULL END AS COMP_CREDIT1, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         CASE T1.SCHOOLCD WHEN '2' THEN L1.PROV_FLG ELSE NULL END AS PROV_FLG2, ";
        }
        $query .= "         CASE T1.SCHOOLCD WHEN '2' THEN T1.VALUATION ELSE NULL END AS VALUATION2, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '2' THEN T1.GET_CREDIT ELSE NULL END AS GET_CREDIT2, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '2' THEN T1.ADD_CREDIT ELSE NULL END AS ADD_CREDIT2, ";
        $query .= "         CASE T1.SCHOOLCD WHEN '2' THEN T1.COMP_CREDIT ELSE NULL END AS COMP_CREDIT2 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         INNER JOIN SCHINFO T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         LEFT JOIN STUDYREC_PROV_FLG_DAT L1 ";
            $query .= "              ON T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "             AND T1.YEAR             = L1.YEAR ";
            $query .= "             AND T1.SCHREGNO         = L1.SCHREGNO ";
            $query .= "             AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
            $query .= "             AND T1.CLASSCD          = L1.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        }
        $query .= " ), STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         SUM(PROV_FLG0) AS PROV_FLG0, ";
        }
        $query .= "         SUM(VALUATION0) AS VALUATION0, ";
        $query .= "         SUM(GET_CREDIT0) AS GET_CREDIT0, ";
        $query .= "         SUM(ADD_CREDIT0) AS ADD_CREDIT0, ";
        $query .= "         SUM(COMP_CREDIT0) AS COMP_CREDIT0, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         SUM(PROV_FLG1) AS PROV_FLG1, ";
        }
        $query .= "         SUM(VALUATION1) AS VALUATION1, ";
        $query .= "         SUM(GET_CREDIT1) AS GET_CREDIT1, ";
        $query .= "         SUM(ADD_CREDIT1) AS ADD_CREDIT1, ";
        $query .= "         SUM(COMP_CREDIT1) AS COMP_CREDIT1, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         SUM(PROV_FLG2) AS PROV_FLG2, ";
        }
        $query .= "         SUM(VALUATION2) AS VALUATION2, ";
        $query .= "         SUM(GET_CREDIT2) AS GET_CREDIT2, ";
        $query .= "         SUM(ADD_CREDIT2) AS ADD_CREDIT2, ";
        $query .= "         SUM(COMP_CREDIT2) AS COMP_CREDIT2 ";
        $query .= "     FROM ";
        $query .= "         STUDYREC0 ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || VALUE(T3.CLASSNAME,'') AS CLASSNAME, ";
        $query .= "     T1.SUBCLASSCD || ':' || CASE WHEN L1.SUBCLASSNAME IS NOT NULL THEN L1.SUBCLASSNAME ELSE VALUE(T1.SUBCLASSNAME,'') END AS SUBCLASSNAME, ";
        $query .= "     L2.REMARK, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     T1.PROV_FLG0, ";
        }
        $query .= "     T1.VALUATION0, ";
        $query .= "     T1.GET_CREDIT0, ";
        $query .= "     T1.ADD_CREDIT0, ";
        $query .= "     T1.COMP_CREDIT0, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     T1.PROV_FLG1, ";
        }
        $query .= "     T1.VALUATION1, ";
        $query .= "     T1.GET_CREDIT1, ";
        $query .= "     T1.ADD_CREDIT1, ";
        $query .= "     T1.COMP_CREDIT1, ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     T1.PROV_FLG2, ";
        }
        $query .= "     T1.VALUATION2, ";
        $query .= "     T1.GET_CREDIT2, ";
        $query .= "     T1.ADD_CREDIT2, ";
        $query .= "     T1.COMP_CREDIT2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     STUDYREC T1 ";
        $query .= "     INNER JOIN SCHINFO T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN CLASS_MST T3 ";
        $query .= "          ON T1.CLASSCD          = T3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T3.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "          ON T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= "         AND T1.CLASSCD          = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "     LEFT JOIN STUDYRECREMARK_DAT L2 ";
        $query .= "          ON T1.YEAR             = L2.YEAR ";
        $query .= "         AND T1.SCHREGNO         = L2.SCHREGNO ";
        $query .= "         AND T1.CLASSCD          = L2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = L2.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
}
