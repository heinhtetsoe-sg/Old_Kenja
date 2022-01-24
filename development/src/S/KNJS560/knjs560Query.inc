<?php

require_once('for_php7.php');

class knjs560query extends Query
{
    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CVS科目取得
    public function getSubclassCd($model, $subclassName)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     substr(MAX(SUBCLASSCD), 1, 2) || '-' || 'P' || '-' || '2' || '-' || MAX(SUBCLASSCD) AS MAX_CD, ";
            $query .= "     substr(MIN(SUBCLASSCD), 1, 2) || '-' || 'P' || '-' || '2' || '-' || MIN(SUBCLASSCD) AS MIN_CD ";
        } else {
            $query .= "     MAX(SUBCLASSCD) AS MAX_CD, ";
            $query .= "     MIN(SUBCLASSCD) AS MIN_CD ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUBCLASSNAME = '".$subclassName."' ";

        return $query;
    }

    //CVS出版社取得
    public function getIssuecompanyCd($model, $issuecompanyName)
    {
        $query  = " SELECT ";
        $query .= "     MAX(T1.ISSUECOMPANYCD) AS MAX_CD, ";
        $query .= "     MIN(T1.ISSUECOMPANYCD) AS MIN_CD ";
        $query .= " FROM ";
        $query .= "     ISSUECOMPANY_MST T1, ";
        $query .= "     ISSUECOMPANY_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ISSUECOMPANYABBV = '".$issuecompanyName."' ";
        $query .= "     AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.ISSUECOMPANYCD = T2.ISSUECOMPANYCD ";

        return $query;
    }

    //CVS更新
    public function delInsQueryCsv($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->data_arr as $key => $setVal) {
            $delQuery  = " DELETE FROM UNIT_MST ";
            $delQuery .= "     WHERE ";
            $delQuery .= "         YEAR = '{$setVal["YEAR"]}' ";
            $delQuery .= "         AND GRADE = '{$setVal["GRADE"]}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $delQuery .= "         AND CLASSCD          = '".substr($setVal["SUBCLASSCD"], 0, 2)."' ";
                $delQuery .= "         AND SCHOOL_KIND      = '".substr($setVal["SUBCLASSCD"], 3, 1)."' ";
                $delQuery .= "         AND CURRICULUM_CD    = '".substr($setVal["SUBCLASSCD"], 5, 1)."' ";
                $delQuery .= "         AND SUBCLASSCD       = '".substr($setVal["SUBCLASSCD"], 7, 6)."' ";
            } else {
                $delQuery .= "         AND SUBCLASSCD = '{$setVal["SUBCLASSCD"]}' ";
            }
            $delQuery .= "         AND ISSUECOMPANYCD = '{$setVal["ISSUECOMPANYCD"]}' ";

            $db->query($delQuery);
        }
        
        $seq = 1;
        foreach ($model->data_arr as $key => $setVal) {
            $data = array();

            $data["YEAR"][TEXT]                   = $setVal["YEAR"];
            $data["GRADE"][TEXT]                  = $setVal["GRADE"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]                = substr($setVal["SUBCLASSCD"], 0, 2);
                $data["SCHOOL_KIND"][TEXT]            = substr($setVal["SUBCLASSCD"], 3, 1);
                $data["CURRICULUM_CD"][TEXT]          = substr($setVal["SUBCLASSCD"], 5, 1);
                $data["SUBCLASSCD"][TEXT]             = substr($setVal["SUBCLASSCD"], 7, 6);
            } else {
                $data["SUBCLASSCD"][TEXT]             = $setVal["SUBCLASSCD"];
            }
            $data["ISSUECOMPANYCD"][TEXT]         = $setVal["ISSUECOMPANYCD"];
            $data["SEQ"][NUMBER]                  = $seq;
            $data["SYUPPAN"][TEXT]                = $setVal["SYUPPAN"];
            $data["DIV"][TEXT]                    = $setVal["DIV"];
            $data["ALLOTMENT_MONTH"][TEXT]        = $setVal["ALLOTMENT_MONTH"];
            $data["L_TITOL"][NUMBER]              = $setVal["L_TITOL"];
            $data["UNIT_L_NAME"][TEXT]            = $setVal["UNIT_L_NAME"];
            $data["UNIT_M_NAME"][TEXT]            = $setVal["UNIT_M_NAME"];
            $data["UNIT_S_NAME"][TEXT]            = $setVal["UNIT_S_NAME"];
            $data["UNIT_DATA"][TEXT]              = $setVal["UNIT_DATA"];
            $data["ALLOTMENT_TIME"][TEXT]         = $setVal["ALLOTMENT_TIME"];
            $data["UNIT_ARRIVAL_TARGET"][TEXT]    = $setVal["UNIT_ARRIVAL_TARGET"];
            $data["UNIT_DIV"][TEXT]               = $setVal["UNIT_DIV"];
            $data["TEXT_PAGE"][TEXT]              = $setVal["TEXT_PAGE"];
            $data["REGISTERCD"][TEXT]             = STAFFCD;
            $data["UPDATED"][NUMBER]              = "sysdate()";
            
            $query = Query::insertSQL($data, "UNIT_MST");
            $db->query($query);
            
            $seq++;
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return;
    }
}
