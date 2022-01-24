<?php

require_once('for_php7.php');

class knjd131nQuery extends Query {

    //対象データ取得
    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.REMARK AS SEM1_01_REMARK, ";
        $query .= "     L2.REMARK AS SEM1_02_REMARK, ";
        $query .= "     L3.REMARK AS SEM2_01_REMARK, ";
        $query .= "     L4.REMARK AS SEM2_02_REMARK, ";
        $query .= "     L5.REMARK AS SEM3_02_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTESTREMARK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                 AND L1.SEMESTER = '1' ";
        $query .= "                                 AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L1.TESTKINDCD = '01' ";
        $query .= "                                 AND L1.TESTITEMCD = '01' ";
        $query .= "                                 AND L1.CLASSCD = '00' ";
        $query .= "                                 AND L1.SCHOOL_KIND = '00' ";
        $query .= "                                 AND L1.CURRICULUM_CD = '00' ";
        $query .= "                                 AND L1.SUBCLASSCD = '000000' ";
        $query .= "                                 AND L1.DIV = '1' ";
        $query .= "     LEFT JOIN HTESTREMARK_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                 AND L2.SEMESTER = '1' ";
        $query .= "                                 AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L2.TESTKINDCD = '02' ";
        $query .= "                                 AND L2.TESTITEMCD = '01' ";
        $query .= "                                 AND L2.CLASSCD = '00' ";
        $query .= "                                 AND L2.SCHOOL_KIND = '00' ";
        $query .= "                                 AND L2.CURRICULUM_CD = '00' ";
        $query .= "                                 AND L2.SUBCLASSCD = '000000' ";
        $query .= "                                 AND L2.DIV = '1' ";
        $query .= "     LEFT JOIN HTESTREMARK_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "                                 AND L3.SEMESTER = '2' ";
        $query .= "                                 AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L3.TESTKINDCD = '01' ";
        $query .= "                                 AND L3.TESTITEMCD = '01' ";
        $query .= "                                 AND L3.CLASSCD = '00' ";
        $query .= "                                 AND L3.SCHOOL_KIND = '00' ";
        $query .= "                                 AND L3.CURRICULUM_CD = '00' ";
        $query .= "                                 AND L3.SUBCLASSCD = '000000' ";
        $query .= "                                 AND L3.DIV = '1' ";
        $query .= "     LEFT JOIN HTESTREMARK_DAT L4 ON L4.YEAR = T1.YEAR ";
        $query .= "                                 AND L4.SEMESTER = '2' ";
        $query .= "                                 AND L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L4.TESTKINDCD = '02' ";
        $query .= "                                 AND L4.TESTITEMCD = '01' ";
        $query .= "                                 AND L4.CLASSCD = '00' ";
        $query .= "                                 AND L4.SCHOOL_KIND = '00' ";
        $query .= "                                 AND L4.CURRICULUM_CD = '00' ";
        $query .= "                                 AND L4.SUBCLASSCD = '000000' ";
        $query .= "                                 AND L4.DIV = '1' ";
        $query .= "     LEFT JOIN HTESTREMARK_DAT L5 ON L5.YEAR = T1.YEAR ";
        $query .= "                                 AND L5.SEMESTER = '3' ";
        $query .= "                                 AND L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND L5.TESTKINDCD = '02' ";
        $query .= "                                 AND L5.TESTITEMCD = '01' ";
        $query .= "                                 AND L5.CLASSCD = '00' ";
        $query .= "                                 AND L5.SCHOOL_KIND = '00' ";
        $query .= "                                 AND L5.CURRICULUM_CD = '00' ";
        $query .= "                                 AND L5.SUBCLASSCD = '000000' ";
        $query .= "                                 AND L5.DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.YEAR     = '{$model->exp_year}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }
    
    //学期名称取得
    function getSemesterName($semster) {
        $query  =" SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '{$semster}' ";

        return $query;
    }
    
    //更新対象確認用
    function getHtestremarkDat($model, $semester, $testkindcd) {
        $query  =" SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTESTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR     = '{$model->exp_year}' ";
        $query .= " AND SEMESTER = '".$semester."' ";
        $query .= " AND TESTKINDCD = '".$testkindcd."' ";
        $query .= " AND TESTITEMCD = '01' ";
        $query .= " AND CLASSCD = '00' ";
        $query .= " AND SCHOOL_KIND = '00' ";
        $query .= " AND CURRICULUM_CD = '00' ";
        $query .= " AND SUBCLASSCD = '000000' ";
        $query .= " AND DIV = '1' ";

        return $query;
    }
    
    /* 更新処理 */
    function update(&$db, $model) {
        
        //1学期中間
        $count1 = $db->getOne(knjd131nQuery::getHtestremarkDat($model, "1", "01"));
        $data = array();
        $data["REMARK"][TEXT]            = $model->field["SEM1_01_REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
        if ($count1 == 0) {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "1";
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["TESTKINDCD"][TEXT]    = "01";
            $data["TESTITEMCD"][TEXT]    = "01";
            $data["CLASSCD"][TEXT]       = "00";
            $data["SCHOOL_KIND"][TEXT]   = "00";
            $data["CURRICULUM_CD"][TEXT] = "00";
            $data["SUBCLASSCD"][TEXT]    = "000000";
            $data["DIV"][TEXT]           = "1";
            
            $query = Query::insertSQL($data, "HTESTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
            $where .= " AND YEAR     = '{$model->exp_year}' ";
            $where .= " AND SEMESTER = '1' ";
            $where .= " AND TESTKINDCD = '01' ";
            $where .= " AND TESTITEMCD = '01' ";
            $where .= " AND CLASSCD = '00' ";
            $where .= " AND SCHOOL_KIND = '00' ";
            $where .= " AND CURRICULUM_CD = '00' ";
            $where .= " AND SUBCLASSCD = '000000' ";
            $where .= " AND DIV = '1' ";

            $query = Query::updateSQL($data, "HTESTREMARK_DAT", $where);
        }
        $db->query($query);
        
        //1学期期末
        $count2 = $db->getOne(knjd131nQuery::getHtestremarkDat($model, "1", "02"));
        $data = array();
        $data["REMARK"][TEXT]            = $model->field["SEM1_02_REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
        if ($count2 == 0) {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "1";
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["TESTKINDCD"][TEXT]    = "02";
            $data["TESTITEMCD"][TEXT]    = "01";
            $data["CLASSCD"][TEXT]       = "00";
            $data["SCHOOL_KIND"][TEXT]   = "00";
            $data["CURRICULUM_CD"][TEXT] = "00";
            $data["SUBCLASSCD"][TEXT]    = "000000";
            $data["DIV"][TEXT]           = "1";
            
            $query = Query::insertSQL($data, "HTESTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
            $where .= " AND YEAR     = '{$model->exp_year}' ";
            $where .= " AND SEMESTER = '1' ";
            $where .= " AND TESTKINDCD = '02' ";
            $where .= " AND TESTITEMCD = '01' ";
            $where .= " AND CLASSCD = '00' ";
            $where .= " AND SCHOOL_KIND = '00' ";
            $where .= " AND CURRICULUM_CD = '00' ";
            $where .= " AND SUBCLASSCD = '000000' ";
            $where .= " AND DIV = '1' ";

            $query = Query::updateSQL($data, "HTESTREMARK_DAT", $where);
        }
        $db->query($query);
        
        //2学期中間
        $count3 = $db->getOne(knjd131nQuery::getHtestremarkDat($model, "2", "01"));
        $data = array();
        $data["REMARK"][TEXT]            = $model->field["SEM2_01_REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
        if ($count3 == 0) {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "2";
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["TESTKINDCD"][TEXT]    = "01";
            $data["TESTITEMCD"][TEXT]    = "01";
            $data["CLASSCD"][TEXT]       = "00";
            $data["SCHOOL_KIND"][TEXT]   = "00";
            $data["CURRICULUM_CD"][TEXT] = "00";
            $data["SUBCLASSCD"][TEXT]    = "000000";
            $data["DIV"][TEXT]           = "1";
            
            $query = Query::insertSQL($data, "HTESTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
            $where .= " AND YEAR     = '{$model->exp_year}' ";
            $where .= " AND SEMESTER = '2' ";
            $where .= " AND TESTKINDCD = '01' ";
            $where .= " AND TESTITEMCD = '01' ";
            $where .= " AND CLASSCD = '00' ";
            $where .= " AND SCHOOL_KIND = '00' ";
            $where .= " AND CURRICULUM_CD = '00' ";
            $where .= " AND SUBCLASSCD = '000000' ";
            $where .= " AND DIV = '1' ";

            $query = Query::updateSQL($data, "HTESTREMARK_DAT", $where);
        }
        $db->query($query);
        
        //2学期期末
        $count4 = $db->getOne(knjd131nQuery::getHtestremarkDat($model, "2", "02"));
        $data = array();
        $data["REMARK"][TEXT]            = $model->field["SEM2_02_REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
        if ($count4 == 0) {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "2";
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["TESTKINDCD"][TEXT]    = "02";
            $data["TESTITEMCD"][TEXT]    = "01";
            $data["CLASSCD"][TEXT]       = "00";
            $data["SCHOOL_KIND"][TEXT]   = "00";
            $data["CURRICULUM_CD"][TEXT] = "00";
            $data["SUBCLASSCD"][TEXT]    = "000000";
            $data["DIV"][TEXT]           = "1";
            
            $query = Query::insertSQL($data, "HTESTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
            $where .= " AND YEAR     = '{$model->exp_year}' ";
            $where .= " AND SEMESTER = '2' ";
            $where .= " AND TESTKINDCD = '02' ";
            $where .= " AND TESTITEMCD = '01' ";
            $where .= " AND CLASSCD = '00' ";
            $where .= " AND SCHOOL_KIND = '00' ";
            $where .= " AND CURRICULUM_CD = '00' ";
            $where .= " AND SUBCLASSCD = '000000' ";
            $where .= " AND DIV = '1' ";

            $query = Query::updateSQL($data, "HTESTREMARK_DAT", $where);
        }
        $db->query($query);
        
        //3学期期末
        $count5 = $db->getOne(knjd131nQuery::getHtestremarkDat($model, "3", "02"));
        $data = array();
        $data["REMARK"][TEXT]            = $model->field["SEM3_02_REMARK"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";
        if ($count5 == 0) {
            $data["YEAR"][TEXT]          = $model->exp_year;
            $data["SEMESTER"][TEXT]      = "3";
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["TESTKINDCD"][TEXT]    = "02";
            $data["TESTITEMCD"][TEXT]    = "01";
            $data["CLASSCD"][TEXT]       = "00";
            $data["SCHOOL_KIND"][TEXT]   = "00";
            $data["CURRICULUM_CD"][TEXT] = "00";
            $data["SUBCLASSCD"][TEXT]    = "000000";
            $data["DIV"][TEXT]           = "1";
            
            $query = Query::insertSQL($data, "HTESTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO  = '{$model->schregno}' ";
            $where .= " AND YEAR     = '{$model->exp_year}' ";
            $where .= " AND SEMESTER = '3' ";
            $where .= " AND TESTKINDCD = '02' ";
            $where .= " AND TESTITEMCD = '01' ";
            $where .= " AND CLASSCD = '00' ";
            $where .= " AND SCHOOL_KIND = '00' ";
            $where .= " AND CURRICULUM_CD = '00' ";
            $where .= " AND SUBCLASSCD = '000000' ";
            $where .= " AND DIV = '1' ";
            
            $query = Query::updateSQL($data, "HTESTREMARK_DAT", $where);
        }
        $db->query($query);

    }
}
?>
