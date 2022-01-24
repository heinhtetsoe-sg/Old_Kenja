<?php

require_once('for_php7.php');

class knja128jQuery extends Query {
    //１レコード取得
    function getTrainRow($model, $flg) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     L1.TOTALSTUDYACT, ";
        $query .= "     L1.VIEWREMARK, ";
        $query .= "     L1.TOTALSTUDYVAL, ";
        $query .= "     L2.REMARK1 AS INDEPENDENT_REMARK, ";
        $query .= "     L3.REMARK1 AS ENT_DISABILITY_REMARK, ";
        $query .= "     L1.TOTALREMARK, ";
        $query .= "     L1.ATTENDREC_REMARK, ";
        $query .= "     L1.SPECIALACTREMARK, ";
        $query .= "     L4.REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DAT L1 ON T1.YEAR        = L1.YEAR ";
        $query .= "                                  AND T1.SCHREGNO    = L1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ON T1.YEAR        = L2.YEAR ";
        $query .= "                                          AND T1.SCHREGNO    = L2.SCHREGNO ";
        $query .= "                                          AND L2.HTRAIN_SEQ  = '001' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L3 ON T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "                                           AND L3.HTRAIN_SEQ = '001' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L4 ON T1.YEAR       = L4.YEAR ";
        $query .= "                                          AND T1.SCHREGNO   = L4.SCHREGNO ";
        $query .= "                                          AND L4.HTRAIN_SEQ = '004', ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        if ($flg != 'sanshou') {
            $query .= "     T1.YEAR         = '".$model->exp_year."' AND ";
        }
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     T2.NAMECD1      = 'A023' AND ";
        $query .= "     T2.NAME1        = 'J' AND ";
        $query .= "     T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
        if ($flg == 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR ";
        }

        return $query;
    }

    //存在チェック -- HTRAINREMARK_DAT
    function checkHtrainremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_DETAIL2_DAT
    function checkHtrainremarkDetail2Dat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '".$seq."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_DETAIL2_HDAT
    function checkHtrainremarkDetail2Hdat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '".$seq."' ";

        return $query;
    }

    //年次の取得
    function getAnnual($model) {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja128jQuery::getAnnual($model));
        //存在チェック --HTRAINREMARK_DAT
        $chk_dat = $db->getOne(knja128jQuery::checkHtrainremarkDat($model));

        $data = array();
        if ($chk_dat > 0) {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_DAT
        $chk_detailD = $db->getOne(knja128jQuery::checkHtrainremarkDetail2Dat($model, "001"));

        $data = array();
        if ($chk_detailD > 0) {
            $data["REMARK1"][TEXT]      = $model->field["INDEPENDENT_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ  = '001' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
        } else {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]   = '001';
            $data["REMARK1"][TEXT]      = $model->field["INDEPENDENT_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_HDAT
        $chk_detailH = $db->getOne(knja128jQuery::checkHtrainremarkDetail2Hdat($model, "001"));

        $data = array();
        if ($chk_detailH > 0) {
            $data["REMARK1"][TEXT]      = $model->field["ENT_DISABILITY_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ  = '001' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where));
        } else {
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]   = '001';
            $data["REMARK1"][TEXT]      = $model->field["ENT_DISABILITY_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT"));
        }
        
        //存在チェック --HTRAINREMARK_DETAIL2_DAT
        $chk_detailD_004 = $db->getOne(knja128jQuery::checkHtrainremarkDetail2Dat($model, "004"));

        $data = array();
        if ($chk_detailD_004 > 0) {

            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR       = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO   = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ = '004' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));

        } else {
        
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]           = '004';
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //行動記録の取得
    function getBehavior($model) {
		$query  = " SELECT ";
		$query .= "     DIV, ";
		$query .= "     CODE, ";
		$query .= "     ANNUAL, ";
		$query .= "     RECORD ";
		$query .= " FROM ";
		$query .= "     BEHAVIOR_DAT ";
		$query .= " WHERE ";
		$query .= "     YEAR        = '".$model->exp_year."' AND ";
		$query .= "     SCHREGNO    = '".$model->schregno."' ";
		$query .= " ORDER BY ";
		$query .= "     DIV, ";
		$query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery2($model) {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja128jQuery::getAnnual($model));

        //行動の記録・特別活動の記録（削除）
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('1', '2') ";

        $db->query($query);

        //行動の記録・特別活動の記録（追加）
        for ($j=1; $j<3; $j++) {
            $cnt = ($j == "1") ? "11" : "4";

            for ($i=1; $i<$cnt; $i++) {
                $ival = $j . sprintf("%02d", $i);
                if ($model->record["RECORD"][$ival] != "1") continue;

                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $j;
                $data["CODE"][TEXT]         = sprintf("%02d", $i);
                $data["ANNUAL"][TEXT]       = $annual;
                $data["RECORD"][TEXT]       = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
            }
        }

        //存在チェック
        $chk_dat = $db->getOne(knja128jQuery::checkHtrainremarkDat($model));

        //特別活動の記録の観点
        $data = array();
        if ($chk_dat > 0) {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ANNUAL"][TEXT]               = $annual;
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
