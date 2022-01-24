<?php

require_once('for_php7.php');

class knjl020kQuery extends Query {
    
    //事前相談ヘッダデータ取得
    function selectQuery(&$model)
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV     = '".$model->testdiv ."' AND ";
        if ($model->cmd == "pre_update" || $model->cmd == "back1"){
            $query .= "     INTEGER(ACCEPTNO) < " .((int) $model->acceptno) ." ";
        }else if ($model->cmd == "next_update" || $model->cmd == "next1"){
            $query .= "     INTEGER(ACCEPTNO) > " .((int) $model->acceptno) ." ";
        }else{
            $query .= "     INTEGER(ACCEPTNO) = " .((int) $model->acceptno) ." ";
        }
        $query .= " ORDER BY ";
        if ($model->cmd == "pre_update" || $model->cmd == "back1"){
            $query .= "     INTEGER(ACCEPTNO) DESC, ";
        }else{
            $query .= "     INTEGER(ACCEPTNO) , ";
        }
        $query .= "     CREATE_DATE DESC ";

        return $query;
    }
    //事前相談志望データ取得
    function selectQuery_Consultation_dat(&$model)
    {
        $query = " SELECT ";
        $query .= "     DATADIV, ";
        $query .= "     WISHNO, ";
        $query .= "     SHDIV, ";
        $query .= "     JUDGEMENT, ";
        $query .= "     ORG_SHDIV, ";
        $query .= "     ORG_MAJORCD, ";
        $query .= "     ORG_JUDGEMENT, ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS COURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(ACCEPTNO) = " .((int) $model->acceptno) ." ";
        
        return $query;
    }
    //名称
    function GetName($year, $namecd1)
    {
        $query = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$year ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";

        return $query;
    }
    //出身学校名称
    function selectQueryFinschool(&$model)
    {
        $query = " SELECT ";
        $query .= "     t1.FINSCHOOLCD AS FS_CD, ";
        $query .= "     t1.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     V_FINSCHOOL_MST t1";
        $query .= " WHERE t1.YEAR  = '".$model->year ."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.FINSCHOOLCD ";

#        $query = " SELECT DISTINCT ";
#        $query .= "     t2.FS_CD, ";
#        $query .= "     t1.FINSCHOOL_NAME ";
#        $query .= " FROM ";
#        $query .= "     FINSCHOOL_MST t1, ";
#        $query .= "     ENTEXAM_CONSULTATION_HDAT t2 ";
#        $query .= " WHERE ";
#        $query .= "     t2.ENTEXAMYEAR  = '".$model->year ."' AND ";
#        $query .= "     t2.TESTDIV      = '".$model->testdiv ."' AND ";
#        $query .= "     t1.FINSCHOOLCD  = t2.FS_CD ";
#        $query .= " ORDER BY ";
#        $query .= "     FS_CD ";

        return $query;
    }
    //出身塾名称
    function selectQueryPrischool(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     t2.PS_CD, ";
        $query .= "     t1.PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST t1, ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT t2 ";
        $query .= " WHERE ";
        $query .= "     t2.ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     t2.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     t1.PRISCHOOLCD = t2.PS_CD ";
        $query .= " ORDER BY ";
        $query .= "     PS_CD ";
        
        return $query;
    }
    //コース
    function selectQueryCourse(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.EXAMCOURSECD, ";
        $query .= "     t1.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST t1 ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->year ."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.EXAMCOURSECD ";

        return $query;
    }
    //事前相談ヘッダデータ登録
    function insert(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (strlen($model->create_date) == 0){
            //作成日付
            $model->create_date = date("Y-m-d");
        }
        //ヘッダデータ登録
        $query = " INSERT INTO ENTEXAM_CONSULTATION_HDAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " TESTDIV, ";
        $query .= " ACCEPTNO, ";
        $query .= " CREATE_DATE, ";
        $query .= " PS_UPDATED, ";
        $query .= " PS_ACCEPTNO, ";
        $query .= " FS_UPDATED, ";
        $query .= " FS_ACCEPTNO, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= "    ( ";
        $query .= " '".$model->year ."', ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        knjl020kQuery::update_consultation($db, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
    //事前相談ヘッダデータ更新
    function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjl020kQuery::update_consultation($db, $model);
        $db->commit();
        Query::dbCheckIn($db);
    }
    //事前相談ヘッダデータ更新
    function update_consultation(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT ";
        $query .= " SET ";
        $query .= "     ACCEPTNO    = '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= "     NAME        = '" .Query::addquote($model->field["NAME"]) ."', ";
        $query .= "     NAME_KANA   = '" .Query::addquote($model->field["NAME_KANA"]) ."', ";
        $query .= "     SEX         = '" .$model->field["SEX"] ."', ";
        $query .= "     FS_UPDATED         = DATE(SYSDATE()), ";
        $query .= "     PS_UPDATED         = DATE(SYSDATE()), ";
        $query .= "     FS_ACCEPTNO = '" .$model->acceptno ."',";
        $query .= "     PS_ACCEPTNO = '" .$model->acceptno ."',";
        $query .= "     PS_CD = " .(($model->field["PS_CD"])? "'" .$model->field["PS_CD"] ."'" : "null") .",";
        for($i = 0; $i < 5; $i++){
            if (is_numeric($model->field["PS_ITEM"][$i])){
                $query .= "     PS_ITEM".($i+1) ." = " .$model->field["PS_ITEM"][$i] .",";
            }else{
                $query .= "     PS_ITEM".($i+1) ." = null,";
            }
        }        
        $query .= "     FS_CD = " .(($model->field["FS_CD"])? "'" .$model->field["FS_CD"] ."'" : "null") .",";
        for($i = 0; $i < 5; $i++){
            if (is_numeric($model->field["FS_ITEM"][$i])){
                $query .= "     FS_ITEM".($i+1) ." = " .$model->field["FS_ITEM"][$i] .",";
            }else{
                $query .= "     FS_ITEM".($i+1) ." = null,";
            }
        }        
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     ACCEPTNO = '" .$model->acceptno ."' AND";
        $query .= "     CREATE_DATE = '" .$model->create_date ."' ";

        $db->query($query);

        $db->expectError(DB_ERROR_CONSTRAINT);

        for($i = 0; $i < 4; $i++){

            $query = " DELETE FROM ENTEXAM_CONSULTATION_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
            $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
            $query .= "     INTEGER(ACCEPTNO) = " .(int)$model->acceptno ." AND ";
            $query .= "     WISHNO = '" .($i+1) ."' ";
    
            $result = $db->query($query);

            for($j = 1; $j <= 2; $j++){
                //$j=1:出身学校 $j=2:塾
                $prefix = ($j == 1)? "F_" : "P_";

                $query = " INSERT INTO ENTEXAM_CONSULTATION_DAT( ";
                $query .= " ENTEXAMYEAR, ";
                $query .= " TESTDIV, ";
                $query .= " ACCEPTNO, ";
                $query .= " DATADIV, ";
                $query .= " WISHNO, ";
                $query .= " SHDIV, ";
                $query .= " COURSECD, ";
                $query .= " MAJORCD, ";
                $query .= " EXAMCOURSECD, ";
                $query .= " JUDGEMENT, ";
                $query .= " ORG_SHDIV, ";
                $query .= " ORG_MAJORCD, ";
                $query .= " ORG_JUDGEMENT, ";
                $query .= " REGISTERCD ";
                $query .= " ) ";
                $query .= " VALUES ";
                $query .= "  ( ";
                $query .= " '".$model->year ."', ";
                $query .= " '" .$model->testdiv ."', ";
                $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
                $query .= " '".$j."', ";
                $query .= " '" .($i+1) ."', ";
                if ($model->field2[$prefix ."SHDIV"][$i]){
                    $query .= " '".$model->field2[$prefix ."SHDIV"][$i]."', ";
                }else{
                    $query .= " null, ";
                }
                if ($model->field2[$prefix ."COURSE"][$i]){
                    $query .= " '".substr($model->field2[$prefix ."COURSE"][$i],0,1)."', ";
                    $query .= " '".substr($model->field2[$prefix ."COURSE"][$i],1,3)."', ";
                    $query .= " '".substr($model->field2[$prefix ."COURSE"][$i],4,4)."', ";
                }else{
                    $query .= " null, ";
                    $query .= " null, ";
                    $query .= " null, ";
                }
                if ($model->field2[$prefix ."JUDGEMENT"][$i]){
                    $query .= " '".$model->field2[$prefix ."JUDGEMENT"][$i]."', ";
                }else{
                    $query .= " null, ";
                }
                if ($model->org["ORG_SHDIV"][$j .",".($i+1)]){
                    $query .= " '".$model->org["ORG_SHDIV"][$j .",".($i+1)]."', ";
                }else{
                    $query .= " null, ";
                }
                if ($model->org["ORG_MAJORCD"][$j .",".($i+1)]){
                    $query .= " '".$model->org["ORG_MAJORCD"][$j .",".($i+1)]."', ";
                }else{
                    $query .= " null, ";
                }
                if ($model->org["ORG_JUDGEMENT"][$j .",".($i+1)]){
                    $query .= " '".$model->org["ORG_JUDGEMENT"][$j .",".($i+1)]."', ";
                }else{
                    $query .= " null, ";
                }
                $query .= " '".STAFFCD ."' ";
                $query .= " ) ";

                $result = $db->query($query);
            }
        }
        //エラー有効
        $db->popExpect();

        return;
    }
    //事前相談ヘッダデータ削除
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = " DELETE FROM ENTEXAM_CONSULTATION_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(ACCEPTNO) = " .(int) $model->acceptno;

        $db->query($query);

        $query = " DELETE FROM ENTEXAM_CONSULTATION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(ACCEPTNO) = " .(int) $model->acceptno;

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //事前相談ヘッダデータコピー
    function copy(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
    
        $query = " UPDATE ";
        $query .= "     ENTEXAM_CONSULTATION_HDAT ";
        $query .= " SET ";
        $query .= "     (NAME,NAME_KANA,SEX, ";
        if ($model->datadiv == "1"){
            $query .= "      FS_UPDATED,FS_ACCEPTNO,FS_CD,FS_ITEM1,FS_ITEM2,FS_ITEM3,FS_ITEM4,FS_ITEM5, ";
        }else{
            $query .= "      PS_UPDATED,PS_ACCEPTNO,PS_CD,PS_ITEM1,PS_ITEM2,PS_ITEM3,PS_ITEM4,PS_ITEM5, ";
        }
        $query .= "      REGISTERCD,UPDATED)=  ";
        $query .= " (SELECT ";
        $query .= "         ST1.NAME, ";
        $query .= "         ST1.NAME_KANA, ";
        $query .= "         ST1.SEX, ";
        if ($model->datadiv == "1"){
            $query .= "         DATE(SYSDATE()), ";
            $query .= "         '" .$model->acceptno2 ."', ";
            $query .= "         ST1.FS_CD, ";
            $query .= "         ST1.FS_ITEM1, ";
            $query .= "         ST1.FS_ITEM2, ";
            $query .= "         ST1.FS_ITEM3, ";
            $query .= "         ST1.FS_ITEM4, ";
            $query .= "         ST1.FS_ITEM5, ";
        }else{
            $query .= "         DATE(SYSDATE()), ";
            $query .= "         '" .$model->acceptno2 ."', ";
            $query .= "         ST1.PS_CD, ";
            $query .= "         ST1.PS_ITEM1, ";
            $query .= "         ST1.PS_ITEM2, ";
            $query .= "         ST1.PS_ITEM3, ";
            $query .= "         ST1.PS_ITEM4, ";
            $query .= "         ST1.PS_ITEM5, ";
        }
        $query .= "         '".STAFFCD ."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_CONSULTATION_HDAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "         ST1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "         INTEGER(ST1.ACCEPTNO) = " .(int)$model->acceptno2 ." AND ";
        $query .= "         ST1.CREATE_DATE = (SELECT ";
        $query .= "                             MAX(w.CREATE_DATE) ";
        $query .= "                         FROM ";
        $query .= "                             ENTEXAM_CONSULTATION_HDAT w ";
        $query .= "                         WHERE ";
        $query .= "                             w.ENTEXAMYEAR = ST1.ENTEXAMYEAR AND ";
        $query .= "                             w.TESTDIV = ST1.TESTDIV AND ";
        $query .= "                             w.ACCEPTNO = ST1.ACCEPTNO ";
        $query .= "                         ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(ACCEPTNO) = " .(int)$model->acceptno ." AND ";
        $query .= "     CREATE_DATE = '" .$model->create_date ."' ";

        $db->query($query);

        $query = " DELETE FROM ENTEXAM_CONSULTATION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     DATADIV = '" .$model->datadiv ."' AND ";
        $query .= "     INTEGER(ACCEPTNO) = " .(int) $model->acceptno;

        $db->query($query);

        $db->expectError(DB_ERROR);

        $query = " INSERT INTO ENTEXAM_CONSULTATION_DAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " TESTDIV, ";
        $query .= " ACCEPTNO, ";
        $query .= " DATADIV, ";
        $query .= " WISHNO, ";
        $query .= " ORG_SHDIV, ";
        $query .= " ORG_MAJORCD, ";
        $query .= " ORG_JUDGEMENT, ";
        $query .= " SHDIV, ";
        $query .= " COURSECD, ";
        $query .= " MAJORCD, ";
        $query .= " EXAMCOURSECD, ";
        $query .= " JUDGEMENT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         TESTDIV, ";
        $query .= "         '" .$model->acceptno ."', ";
        $query .= "         DATADIV, ";
        $query .= "         WISHNO, ";
        $query .= "         ORG_SHDIV, ";
        $query .= "         ORG_MAJORCD, ";
        $query .= "         ORG_JUDGEMENT, ";
        $query .= "         SHDIV, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         EXAMCOURSECD, ";
        $query .= "         JUDGEMENT, ";
        $query .= "         '".STAFFCD ."' ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_CONSULTATION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->year ."' AND ";
        $query .= "         TESTDIV     = '" .$model->testdiv ."' AND ";
        $query .= "         ACCEPTNO    = '" .$model->acceptno2 ."' AND ";
        $query .= "         DATADIV     = '" .$model->datadiv ."' ";
        $query .= "     ) ";

        $db->query($query);
        //エラー有効
        $db->popExpect();
        $db->commit();
        Query::dbCheckIn($db);
    }
} 
?>
