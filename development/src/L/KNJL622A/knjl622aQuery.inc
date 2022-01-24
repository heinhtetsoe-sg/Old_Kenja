<?php
class knjl622aQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1, '') AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //コース
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "     EXAMCOURSECD || ':' || VALUE(EXAMCOURSE_ABBV, '') AS LABEL, ";
        $query .= "     EXAMCOURSECD AS VALUE";
        $query .= " FROM";
        $query .= "     ENTEXAM_COURSE_MST";
        $query .= " WHERE";
        $query .= "     APPLICANTDIV='{$model->field["APPLICANTDIV"]}'";
        $query .= "     AND TESTDIV='{$model->field["TESTDIV"]}'";
        $query .= "     AND ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //選抜方式
    function getSenbatu() {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1, '') AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L071' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //会場
    function getHall($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     EXAMHALLCD AS LABEL ";
        $query .= "   , APPLICANTDIV || '-' || TESTDIV || '-' || EXAMHALLCD AS VALUE";
        $query .= "   , ENTEXAMYEAR ";
        $query .= "   , APPLICANTDIV ";
        $query .= "   , TESTDIV ";
        $query .= "   , EXAM_TYPE ";
        $query .= "   , EXAMHALLCD ";
        $query .= "   , EXAMHALL_NAME ";
        $query .= "   , CAPA_CNT ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }

    //データ選択
    function selectQuery($model) {
        $query  = "";

        $query .= " SELECT ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.RECEPTNO ";
        $query .= "   , T1.EXAMNO ";
        $query .= "   , T2.NAME ";
        $query .= "   , T1.APPLICANTDIV";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , D3.REMARK1 AS COURSE1 ";
        $query .= "   , D3.REMARK2 AS COURSE2 ";
        $query .= "   , D3.REMARK6 AS SENBATU ";
        $query .= "   , D1.REMARK1 AS EXAMHALLCD ";
        $query .= "   , D1.REMARK2 AS EXAMSEATCD ";
        $query .= "   , D1.REMARK1 || VALUE(D1.REMARK2, '') AS HALLSEATCD ";
        $query .= "   , D2.REMARK1 AS RESERVE1 ";
        $query .= "   , D2.REMARK2 AS RESERVE2 ";
        $query .= "   , D2.REMARK3 AS RESERVE3 ";
        $query .= "   , D2.REMARK4 AS RESERVE4 ";
        $query .= "   , D2.REMARK5 AS RESERVE5 ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "     ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D1 ";
        $query .= "     ON T1.ENTEXAMYEAR = D1.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = D1.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = D1.EXAM_TYPE ";
        $query .= "     AND T1.RECEPTNO = D1.RECEPTNO ";
        $query .= "     AND D1.SEQ = '010' ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D2 ";
        $query .= "     ON T1.ENTEXAMYEAR = D2.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D2.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = D2.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = D2.EXAM_TYPE ";
        $query .= "     AND T1.RECEPTNO = D2.RECEPTNO ";
        $query .= "     AND D2.SEQ = '009' ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D3 ";
        $query .= "     ON T1.ENTEXAMYEAR = D3.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D3.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = D3.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = D3.EXAM_TYPE ";
        $query .= "     AND T1.RECEPTNO = D3.RECEPTNO ";
        $query .= "     AND D3.SEQ = '007' ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND T1.EXAM_TYPE = '1' ";

        if ($model->field["EXAMNO_FROM"]) {
            $query .= "   AND T1.EXAMNO >= '{$model->field["EXAMNO_FROM"]}' ";
        }
        if ($model->field["EXAMNO_TO"]) {
            $query .= "   AND T1.EXAMNO <= '{$model->field["EXAMNO_TO"]}' ";
        }
        if ($model->field["EXAMHALLCD"]) {
            list ($applicantDiv, $testDiv, $examHallCd) = split('-', $model->field["EXAMHALLCD"]);
            $query .= "   AND D1.REMARK1 = '{$examHallCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    // 座席番号の重複チェック
    function getDupHallSeat($model, $hall, $seat) {
        $query  = "";
        $query .= " SELECT COUNT(*) AS DUPCNT ";
        $query .= " FROM ENTEXAM_RECEPT_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND T1.EXAM_TYPE = '1' ";
        $query .= "   AND T1.SEQ = '010' ";
        $query .= "   AND T1.RECEPTNO NOT IN ('".implode("','", $model->line["RECEPTNO"])."') ";
        $query .= "   AND T1.REMARK1 = '{$hall}' ";
        $query .= "   AND T1.REMARK2 = '{$seat}' ";

        return $query;
    }

    //データ更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //追加(座席番号)
        for ($i=0; $i < count($model->line["RECEPTNO"]); $i++) { 
            $receptNo   = $model->line["RECEPTNO"][$i];
            $hallSeatCd = $model->line["HALLSEATCD"][$i];

            $hallCd = substr($hallSeatCd, 0, 2);
            $seatCd = substr($hallSeatCd, 2);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]     = "1";
            $data["RECEPTNO"][TEXT]      = $receptNo;
            $data["SEQ"][TEXT]           = "010";

            $data["REMARK1"][TEXT]       = $hallCd;
            $data["REMARK2"][TEXT]       = $seatCd;

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptNo, '010'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE = '1' ";
                $where .= "   AND SEQ = '010' ";
                $where .= "   AND RECEPTNO = '{$receptNo}' ";
        
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }

        //追加(備考)
        for ($i=0; $i < count($model->line["RECEPTNO"]); $i++) { 
            $receptNo = $model->line["RECEPTNO"][$i];
            $reserve1 = $model->line["RESERVE1"][$i];
            $reserve2 = $model->line["RESERVE2"][$i];
            $reserve3 = $model->line["RESERVE3"][$i];
            $reserve4 = $model->line["RESERVE4"][$i];
            $reserve5 = $model->line["RESERVE5"][$i];

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]     = "1";
            $data["RECEPTNO"][TEXT]      = $receptNo;
            $data["SEQ"][TEXT]           = "009";

            $data["REMARK1"][TEXT]       = $reserve1;
            $data["REMARK2"][TEXT]       = $reserve2;
            $data["REMARK3"][TEXT]       = $reserve3;
            $data["REMARK4"][TEXT]       = $reserve4;
            $data["REMARK5"][TEXT]       = $reserve5;

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptNo, '009'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE = '1' ";
                $where .= "   AND SEQ = '009' ";
                $where .= "   AND RECEPTNO = '{$receptNo}' ";
        
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }
        
        //追加(コース)
        for ($i=0; $i < count($model->line["RECEPTNO"]); $i++) { 
            $receptNo = $model->line["RECEPTNO"][$i];
            $course1 = $model->line["COURSE1"][$i];
            $course2 = $model->line["COURSE2"][$i];
            if ($model->field["APPLICANTDIV"] == '2') {
                $senbatu = $model->line["SENBATU"][$i];
            }

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]     = "1";
            $data["RECEPTNO"][TEXT]      = $receptNo;
            $data["SEQ"][TEXT]           = "007";

            $data["REMARK1"][TEXT]       = $course1;
            $data["REMARK2"][TEXT]       = $course2;
            
            if ($model->field["APPLICANTDIV"] == '2') {
                $data["REMARK6"][TEXT]   = $senbatu;
            }

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "SYSDATE()";

            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptNo, '007'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE = '1' ";
                $where .= "   AND SEQ = '007' ";
                $where .= "   AND RECEPTNO = '{$receptNo}' ";
        
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    function getReceDetailCnt($model, $field, $receptNo, $seq) {
        $query  = "";
        $query .= " SELECT {$field} ";
        $query .= " FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= "   AND SEQ = '{$seq}' ";
        $query .= "   AND RECEPTNO = '{$receptNo}' ";

        return $query;
    }
    
    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $year           = $data_arr[$i]["ENTEXAMYEAR"];
            $applicantdiv   = $data_arr[$i]["APPLICANTDIV"];
            $testdiv        = $data_arr[$i]["TESTDIV"];
            $examtype       = "1";
            $receptno       = $db->getOne(knjl622aQuery::getReceptNo($data_arr[$i]));
            $remark1        = $data_arr[$i]["REMARK1"];
            $remark2        = $data_arr[$i]["REMARK2"];
            $remark3        = $data_arr[$i]["REMARK3"];
            $remark4        = $data_arr[$i]["REMARK4"];
            $remark5        = $data_arr[$i]["REMARK5"];
            $seatno         = $data_arr[$i]["SEATNO"];
            
            $course1        = $data_arr[$i]["COURSE1"];
            $course2        = $data_arr[$i]["COURSE2"];
            $senbetu        = $data_arr[$i]["SENBATU"];
            
            //予備
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $year;
            $data["APPLICANTDIV"][TEXT]     = $applicantdiv;
            $data["TESTDIV"][TEXT]          = $testdiv;
            $data["EXAM_TYPE"][TEXT]        = $examtype;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["SEQ"][TEXT]         = "009";
            $data["REMARK1"][TEXT] = $remark1;
            $data["REMARK2"][TEXT] = $remark2;
            $data["REMARK3"][TEXT] = $remark3;
            $data["REMARK4"][TEXT] = $remark4;
            $data["REMARK5"][TEXT] = $remark5;
            

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptno, '009'));

            if (0 == $detailCnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                
            } else {
            
                $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
                $where .= "   AND APPLICANTDIV   = '{$applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '{$examtype}' ";
                $where .= "   AND RECEPTNO       = '{$receptno}' ";
                $where .= "   AND SEQ = '009' ";
                
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                
                
            }
            $db->query($query);
            
            //コース
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $year;
            $data["APPLICANTDIV"][TEXT]     = $applicantdiv;
            $data["TESTDIV"][TEXT]          = $testdiv;
            $data["EXAM_TYPE"][TEXT]        = $examtype;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["SEQ"][TEXT]              = "007";
            $data["REMARK1"][TEXT]          = $course1;
            $data["REMARK2"][TEXT]          = $course2;
            if ($model->field["APPLICANTDIV"] == '2') {
                $data["REMARK6"][TEXT]      = $senbetu;
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptno, '007'));

            if (0 == $detailCnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                
            } else {
            
                $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
                $where .= "   AND APPLICANTDIV   = '{$applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '{$examtype}' ";
                $where .= "   AND RECEPTNO       = '{$receptno}' ";
                $where .= "   AND SEQ = '007' ";
                
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                
                
            }
            $db->query($query);
            
            //座席
            $data2 = array();
            $data2["ENTEXAMYEAR"][TEXT]      = $year;
            $data2["APPLICANTDIV"][TEXT]     = $applicantdiv;
            $data2["TESTDIV"][TEXT]          = $testdiv;
            $data2["EXAM_TYPE"][TEXT]        = $examtype;
            $data2["RECEPTNO"][TEXT]         = $receptno;
            $data2["SEQ"][TEXT]         = "010";
            $data2["REMARK1"][TEXT] = substr($seatno,0,2);
            $data2["REMARK2"][TEXT] = substr($seatno,2,4);
            
            $data2["REGISTERCD"][TEXT]       = STAFFCD;
            $data2["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
            $where .= "   AND APPLICANTDIV   = '{$applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$testdiv}' ";
            $where .= "   AND EXAM_TYPE      = '{$examtype}' ";
            $where .= "   AND RECEPTNO       = '{$receptno}' ";
            
            $detailCnt = $db->getOne(knjl622aQuery::getReceDetailCnt($model, 'COUNT(*)', $receptno, '010'));
            
            if (0 == $detailCnt) {
                $query = Query::insertSQL($data2, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
                $where .= "   AND APPLICANTDIV   = '{$applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '{$examtype}' ";
                $where .= "   AND RECEPTNO       = '{$receptno}' ";
                $where .= "   AND SEQ = '010' ";
                $query = Query::updateSQL($data2, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                
            }
            $db->query($query);
            
            $cnt++;
            
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
    
    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //名称マスタ登録コードチェック
    function nameCntSql($model, $namecd, $setcd, $where="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //受験番号の取得
    function getReceptNo($data, $cnt="") {
        $year           = $data["ENTEXAMYEAR"];
        $applicantdiv   = $data["APPLICANTDIV"];
        $testdiv        = $data["TESTDIV"];
        $examtype       = "1";
        $seatno         = $data["SEATNO"];
        $examno         = $data["EXAMNO"];

        $query .= "     SELECT ";
        if ($cnt) {
            $query .= "         COUNT(*) ";
        } else {
            $query .= "         R1.RECEPTNO ";
        }
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$testdiv}' ";
        $query .= "         AND R1.EXAM_TYPE    = '{$examtype}' ";
        $query .= "         AND R1.EXAMNO       = '{$examno}' ";
        return $query;
    }
    
    //座席番号の重複チェック
     function seatCntSql($data) {
        $year           = $data["ENTEXAMYEAR"];
        $applicantdiv   = $data["APPLICANTDIV"];
        $testdiv        = $data["TESTDIV"];
        $examtype       = "1";
        $seatno         = $data["SEATNO"];
        $receptno       = $data["EXAMNO"];
        $receptno .= $testdiv;
        
        $remark1 = substr($seatno,0,2);
        $remark2 = substr($seatno,2,4);
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$examtype}' ";
        $query .= "     AND RECEPTNO != '{$receptno}' ";
        $query .= "     AND SEQ  = '010' ";
        $query .= "     AND REMARK1 = '{$remark1}' ";
        $query .= "     AND REMARK2 = '{$remark2}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

}
?>
