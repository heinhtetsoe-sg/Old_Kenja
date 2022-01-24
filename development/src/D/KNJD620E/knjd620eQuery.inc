<?php

require_once('for_php7.php');

class knjd620equery extends Query
{
    //学期コンボ
    public function getSemesterList()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //考査種別取得
    public function getTestItem($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER  = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCORE_DIV IN ('08', '09') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        //考査種別
        list($semester, $testkindCd, $testitemCd, $scoreDiv) = preg_split("/-/", $model->field["TESTKINDCD"]);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /************/
            /* デリート */
            /************/
            $query  = " DELETE FROM ";
            $query .= "     RECORD_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR                = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER        = '{$semester}' ";
            $query .= "     AND TESTKINDCD      = '{$testkindCd}' ";
            $query .= "     AND TESTITEMCD      = '{$testitemCd}' ";
            $query .= "     AND SCORE_DIV       = '{$scoreDiv}' ";
            $query .= "     AND SCHREGNO        = '{$data_arr[$i]["SCHREGNO"]}' ";
            $query .= "     AND CLASSCD         = '{$data_arr[$i]["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND     = 'H' ";
            $query .= "     AND CURRICULUM_CD   = '{$data_arr[$i]["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD      = '{$data_arr[$i]["SUBCLASSCD"]}' ";
            $db->query($query);

            /**************/
            /* インサート */
            /**************/
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $semester;
            $data["TESTKINDCD"][TEXT]    = $testkindCd;
            $data["TESTITEMCD"][TEXT]    = $testitemCd;
            $data["SCORE_DIV"][TEXT]     = $scoreDiv;
            $data["CLASSCD"][TEXT]       = $data_arr[$i]["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = 'H';
            $data["CURRICULUM_CD"][TEXT] = $data_arr[$i]["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]    = $data_arr[$i]["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
            $data["SCORE"][TEXT]         = $data_arr[$i]["SCORE"];

            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
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
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
