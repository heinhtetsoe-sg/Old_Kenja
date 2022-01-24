<?php

require_once('for_php7.php');

class knjl020kQuery extends Query {
    
    //受付No最小値と最大値取得
    function GetAcceptno(&$model)
    {
        $query  = " SELECT ";
        $query .= " 	MIN(acceptno) AS acceptno_min, ";
        $query .= " 	MAX(acceptno) AS acceptno_max ";
        $query .= " FROM ";
        $query .= "		entexam_consultation_hdat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."'";
        $query .= "     AND TESTDIV = '".$model->testdiv."'";
        return $query;
    }

    //事前相談ヘッダデータ取得
    function selectQuery(&$model)
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     entexam_consultation_hdat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year ."' AND ";
        $query .= "     testdiv     = '".$model->testdiv ."' AND ";
        if ($model->cmd == "pre_update" || $model->cmd == "back1"){
            $query .= "     INTEGER(acceptno) < " .((int) $model->acceptno) ." ";
        }else if ($model->cmd == "next_update" || $model->cmd == "next1"){
            $query .= "     INTEGER(acceptno) > " .((int) $model->acceptno) ." ";
        }else{
            $query .= "     INTEGER(acceptno) = " .((int) $model->acceptno) ." ";
        }
        $query .= " ORDER BY ";
        if ($model->cmd == "pre_update" || $model->cmd == "back1"){
            $query .= "     INTEGER(acceptno) DESC, ";
        }else{
            $query .= "     INTEGER(acceptno) , ";
        }
        $query .= "     create_date DESC ";

        return $query;
    }

    //事前相談データ取得
    function selectQuery_Consultation_dat(&$model)
    {
        $query = " SELECT ";
        $query .= "     datadiv, ";
        $query .= "     wishno, ";
        $query .= "     shdiv, ";
        $query .= "     judgement, ";
        $query .= "     org_shdiv, ";
        $query .= "     org_majorcd, ";
        $query .= "     org_judgement, ";
        $query .= "     coursecd || majorcd || examcoursecd AS course ";
        $query .= " FROM ";
        $query .= "     entexam_consultation_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear       = '" .$model->year ."' AND ";
        $query .= "     testdiv           = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(acceptno) =  " .((int) $model->acceptno) ." ";
        
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

        $query  = " SELECT FS_CD,  ";
        $query .= "        FINSCHOOL_NAME  ";
        $query .= " FROM ( ";
        $query .= "    SELECT FINSCHOOLCD    AS FS_CD, ";
        $query .= "           FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= "      FROM V_FINSCHOOL_MST ";
        $query .= "     WHERE YEAR='".$model->year ."'  ";
        $query .= "   UNION  ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           t1.FS_CD          AS FS_CD,  ";
        $query .= "           t2.FINSCHOOL_NAME AS FINSCHOOL_NAME   ";
        $query .= "      FROM  ";
        $query .= "            ENTEXAM_CONSULTATION_HDAT         t1  ";
        $query .= "            LEFT OUTER JOIN V_FINSCHOOL_MST   t2  ";
        $query .= "            ON   t2.YEAR        = t1.ENTEXAMYEAR  ";
        $query .= "            AND  t2.FINSCHOOLCD = t1.FS_CD  ";
        $query .= "     WHERE  ";
        $query .= "            t1.ENTEXAMYEAR      = '".$model->year ."'     AND  ";
        $query .= "            t1.TESTDIV          = '".$model->testdiv ."'  AND  ";
        $query .= "            t1.FS_CD            IS NOT NULL AND  ";
        $query .= "            t2.FINSCHOOL_NAME   IS NULL  ";
        $query .= "      ) T01 ";
        $query .= "ORDER BY FS_CD  ";

#2006/01/30
#        $query = " SELECT ";
#        $query .= "     t1.finschoolcd AS fs_cd, ";
#        $query .= "     t1.finschool_name ";
#        $query .= " FROM ";
#        $query .= "     v_finschool_mst t1";
#        $query .= " WHERE t1.year  = '".$model->year ."' ";
#        $query .= " ORDER BY ";
#        $query .= "     t1.finschoolcd ";

        return $query;
    }

    //出身塾名称    #2006/01/18 SQL変更
    function selectQueryPrischool(&$model)
    {
        $query  = " SELECT PS_CD, ";
        $query .= "       PRISCHOOL_NAME ";
        $query .= "  FROM (";
        $query .= "     SELECT PRISCHOOLCD    AS PS_CD, ";
        $query .= "            PRISCHOOL_NAME AS PRISCHOOL_NAME ";
        $query .= "       FROM V_PRISCHOOL_MST ";
        $query .= "      WHERE YEAR='".$model->year ."' ";
        $query .= "    UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "            t1.PS_CD          AS PS_CD, ";
        $query .= "            t2.PRISCHOOL_NAME AS PRISCHOOL_NAME  ";
        $query .= "       FROM ";
        $query .= "             ENTEXAM_CONSULTATION_HDAT   t1 ";
        $query .= "             LEFT OUTER JOIN V_PRISCHOOL_MST   t2 ";
        $query .= "             ON   t2.YEAR        = t1.ENTEXAMYEAR ";
        $query .= "             AND  t2.PRISCHOOLCD = t1.PS_CD ";
        $query .= "      WHERE ";
        $query .= "             t1.ENTEXAMYEAR      = '". $model->year ."'    AND ";
        $query .= "             t1.TESTDIV          = '" .$model->testdiv ."' AND ";
        $query .= "             t1.PS_CD            IS NOT NULL AND ";
        $query .= "             t2.PRISCHOOL_NAME   IS NULL ";
        $query .= "       ) T01";
        $query .= " ORDER BY PS_CD ";
        #echo $query;

#2006/01/18
#        $query = " SELECT DISTINCT ";
#        $query .= "     t2.ps_cd, ";
#        $query .= "     t1.prischool_name ";
#        $query .= " FROM ";
#        $query .= "     prischool_mst             t1, ";
#        $query .= "     entexam_consultation_hdat t2 ";
#        $query .= " WHERE ";
#        $query .= "     t2.entexamyear  = '". $model->year ."' AND ";
#        $query .= "     t2.testdiv      = '" .$model->testdiv ."' AND ";
#        $query .= "     t1.prischoolcd  = t2.ps_cd ";
#        $query .= " ORDER BY ";
#        $query .= "     ps_cd ";

        return $query;
    }

    //コース
    function selectQueryCourse(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd, ";
        $query .= "     t1.examcourse_mark ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear = '".$model->year ."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, ";
        $query .= "     t1.majorcd, ";
        $query .= "     t1.examcoursecd ";

        return $query;
    }
    //事前相談ヘッダデータ登録
    function insert(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (strlen($model->create_date) == 0){
            $model->create_date = date("Y-m-d");//作成日付
        }

        //ヘッダデータ登録
        $query = " INSERT INTO entexam_consultation_hdat( ";
        $query .= " entexamyear, ";
        $query .= " testdiv, ";
        $query .= " acceptno, ";
        $query .= " create_date, ";
        $query .= " ps_updated, ";
        $query .= " ps_acceptno, ";
        $query .= " fs_updated, ";
        $query .= " fs_acceptno, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= "    ( ";
        $query .= " '" .$model->year ."', ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '" .$model->create_date ."', ";
        $query .= " '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= " '" .STAFFCD ."' ";
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


    //
    function update_consultation(&$db, &$model)
    {
        //事前相談ヘッダデータ更新
        $query = " UPDATE ";
#        $query .= "     entexam_consultation_hdat ";       2005/08/12 arakaki
        $query .= "     entexam_consultation_hdat AS T01";
        $query .= " SET ";
        $query .= "     acceptno    = '" .sprintf("%04d", $model->acceptno) ."', ";
        $query .= "     name        = '" .Query::addquote($model->field["NAME"]) ."', ";
        $query .= "     name_kana   = '" .Query::addquote($model->field["NAME_KANA"]) ."', ";
        $query .= "     sex         = '" .$model->field["SEX"] ."', ";
//2005.08.10 minei
#        $query .= "     fs_updated  = DATE(SYSDATE()), ";
#        $query .= "     ps_updated  = DATE(SYSDATE()), ";
#        $query .= "     fs_acceptno = '" .$model->acceptno ."',";
#        $query .= "     ps_acceptno = '" .$model->acceptno ."',";
        $query .= "     fs_updated  = CASE WHEN fs_updated  IS NULL THEN DATE(SYSDATE()) ELSE fs_updated  END ,";      #2005/08/12 arakaki
        $query .= "     ps_updated  = CASE WHEN ps_updated  IS NULL THEN DATE(SYSDATE()) ELSE ps_updated  END ,";      #2005/08/12 arakaki
        $query .= "     fs_acceptno = CASE WHEN fs_acceptno IS NULL THEN acceptno        ELSE fs_acceptno END ,";      #2005/08/12 arakaki
        $query .= "     ps_acceptno = CASE WHEN ps_acceptno IS NULL THEN acceptno        ELSE ps_acceptno END ,";      #2005/08/12 arakaki

        $query .= "     ps_cd       =  " .(($model->field["PS_CD"])? "'" .$model->field["PS_CD"] ."'" : "null") .",";

        for($i = 0; $i < 5; $i++){
            if (is_numeric($model->field["PS_ITEM"][$i])){
                $query .= "     ps_item".($i+1) ." = " .$model->field["PS_ITEM"][$i] .",";
            }else{
                $query .= "     ps_item".($i+1) ." = null,";
            }
        }

        $query .= "     fs_cd = " .(($model->field["FS_CD"])? "'" .$model->field["FS_CD"] ."'" : "null") .",";

        for($i = 0; $i < 5; $i++){
            if (is_numeric($model->field["FS_ITEM"][$i])){
                $query .= "     fs_item".($i+1) ." = " .$model->field["FS_ITEM"][$i] .",";
            }else{
                $query .= "     fs_item".($i+1) ." = null,";
            }
        }

        $query .= "     updated  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year ."' AND ";
        $query .= "     testdiv     = '".$model->testdiv ."' AND ";
        $query .= "     acceptno    = '".$model->acceptno ."' AND";
        $query .= "     create_date = '".$model->create_date ."' ";

        $db->query($query);

        $db->expectError(DB_ERROR_CONSTRAINT);


        //事前相談データ作成
        for($i = 0; $i < 4; $i++){

            $query = " DELETE FROM entexam_consultation_dat ";
            $query .= " WHERE ";
            $query .= "     entexamyear       = '".$model->year ."' AND ";
            $query .= "     testdiv           = '".$model->testdiv ."' AND ";
            $query .= "     INTEGER(acceptno) =  ".(int)$model->acceptno ." AND ";
            $query .= "     wishno            = '" .($i+1) ."' ";

            $result = $db->query($query);

            for($j = 1; $j <= 2; $j++){
                //$j=1:出身学校 $j=2:塾
                $prefix = ($j == 1)? "F_" : "P_";

                $query = " INSERT INTO entexam_consultation_dat( ";
                $query .= " entexamyear, ";
                $query .= " testdiv, ";
                $query .= " acceptno, ";
                $query .= " datadiv, ";
                $query .= " wishno, ";
                $query .= " shdiv, ";
                $query .= " coursecd, ";
                $query .= " majorcd, ";
                $query .= " examcoursecd, ";
                $query .= " judgement, ";
                $query .= " org_shdiv, ";
                $query .= " org_majorcd, ";
                $query .= " org_judgement, ";
                $query .= " registercd ";
                $query .= " ) ";
                $query .= " VALUES ";
                $query .= "  ( ";
                $query .= " '".$model->year ."', ";
                $query .= " '".$model->testdiv ."', ";
                $query .= " '".sprintf("%04d", $model->acceptno) ."', ";
                $query .= " '".$j."', ";
                $query .= " '".($i+1) ."', ";
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

        $query = " DELETE FROM entexam_consultation_hdat ";
        $query .= " WHERE ";
        $query .= "     entexamyear       = '" .$model->year ."' AND ";
        $query .= "     testdiv           = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(acceptno) =  " .(int) $model->acceptno;

        $db->query($query);

        $query = " DELETE FROM entexam_consultation_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear       = '" .$model->year ."' AND ";
        $query .= "     testdiv           = '" .$model->testdiv ."' AND ";
        $query .= "     INTEGER(acceptno) =  " .(int) $model->acceptno;

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //事前相談コピー
    function copy(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //事前相談ヘッダデータ更新
        $query = " UPDATE ";
        $query .= "     entexam_consultation_hdat ";
        $query .= " SET (";
//2005.08.10 minei
#        $query .= "     (NAME,NAME_KANA,SEX, ";
        //出身学校
        if ($model->datadiv == "1"){
            $query .= "      fs_updated,fs_acceptno,fs_cd,fs_item1,fs_item2,fs_item3,fs_item4,fs_item5, ";
        //塾
        }else{
            $query .= "      ps_updated,ps_acceptno,ps_cd,ps_item1,ps_item2,ps_item3,ps_item4,ps_item5, ";
        }
        $query .= "      registercd,updated)=  ";
        $query .= " (SELECT ";
//2005.08.10 minei
#        $query .= "         ST1.NAME, ";
#        $query .= "         ST1.NAME_KANA, ";
#        $query .= "         ST1.SEX, ";
        if ($model->datadiv == "1"){//出身学校
//2005.08.10 minei
#            $query .= "         DATE(SYSDATE()), ";
#            $query .= "         '" .$model->acceptno2 ."', ";
            $query .= "         st1.fs_updated, ";
            $query .= "         st1.fs_acceptno, ";
            $query .= "         st1.fs_cd, ";
            $query .= "         st1.fs_item1, ";
            $query .= "         st1.fs_item2, ";
            $query .= "         st1.fs_item3, ";
            $query .= "         st1.fs_item4, ";
            $query .= "         st1.fs_item5, ";
        }else{                      //塾
//2005.08.10 minei
#            $query .= "         DATE(SYSDATE()), ";
#            $query .= "         '" .$model->acceptno2 ."', ";
            $query .= "         st1.ps_updated, ";
            $query .= "         st1.ps_acceptno, ";
            $query .= "         st1.ps_cd, ";
            $query .= "         st1.ps_item1, ";
            $query .= "         st1.ps_item2, ";
            $query .= "         st1.ps_item3, ";
            $query .= "         st1.ps_item4, ";
            $query .= "         st1.ps_item5, ";
        }
        $query .= "         '".STAFFCD ."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         entexam_consultation_hdat st1 ";
        $query .= "     WHERE ";
        $query .= "         st1.entexamyear       = '".$model->year ."' AND ";
        $query .= "         st1.testdiv           = '".$model->testdiv ."' AND ";
        $query .= "         INTEGER(st1.acceptno) = " .(int)$model->acceptno2  ; //コピー元受付No
//2005.08.10 minei
#        $query .= "         st1.create_date       = (SELECT MAX(w.create_date) ";
#        $query .= "                                    FROM entexam_consultation_hdat w ";
#        $query .= "                                   WHERE ";
#        $query .= "                                         w.entexamyear = st1.entexamyear AND ";
#        $query .= "                                         w.testdiv     = st1.testdiv AND ";
#        $query .= "                                         w.acceptno    = st1.acceptno ";
#        $query .= "                                 ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     entexamyear         = '". $model->year ."' AND ";
        $query .= "     testdiv             = '". $model->testdiv ."' AND ";
        $query .= "     INTEGER(acceptno)   = " .(int)$model->acceptno ." ";
//2005.08.10 minei
#        $query .= "     INTEGER(acceptno)   = " .(int)$model->acceptno ." AND ";
#        $query .= "     create_date         = '". $model->create_date ."' ";

        $db->query($query);


        //事前相談データの削除
        $query = " DELETE FROM entexam_consultation_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear       = '".$model->year ."' AND ";
        $query .= "     testdiv           = '" .$model->testdiv ."' AND ";
        $query .= "     datadiv           = '" .$model->datadiv ."' AND ";  //1:出身学校 2:塾
        $query .= "     INTEGER(acceptno) = " .(int) $model->acceptno;

        $db->query($query);
        $db->expectError(DB_ERROR);

        $query = " INSERT INTO entexam_consultation_dat( ";
        $query .= " entexamyear, ";
        $query .= " testdiv, ";
        $query .= " acceptno, ";
        $query .= " datadiv, ";
        $query .= " wishno, ";
        $query .= " org_shdiv, ";
        $query .= " org_majorcd, ";
        $query .= " org_judgement, ";
        $query .= " shdiv, ";
        $query .= " coursecd, ";
        $query .= " majorcd, ";
        $query .= " examcoursecd, ";
        $query .= " judgement, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "         entexamyear, ";
        $query .= "         testdiv, ";
        $query .= "         '" .$model->acceptno ."', ";
        $query .= "         datadiv, ";
        $query .= "         wishno, ";
        $query .= "         org_shdiv, ";
        $query .= "         org_majorcd, ";
        $query .= "         org_judgement, ";
        $query .= "         shdiv, ";
        $query .= "         coursecd, ";
        $query .= "         majorcd, ";
        $query .= "         examcoursecd, ";
        $query .= "         judgement, ";
        $query .= "         '".STAFFCD ."' ";
        $query .= "     FROM ";
        $query .= "         entexam_consultation_dat ";
        $query .= "     WHERE ";
        $query .= "         entexamyear = '". $model->year ."' AND ";
        $query .= "         testdiv     = '". $model->testdiv ."' AND ";  //1:出身学校 2:塾
        $query .= "         acceptno    = '". $model->acceptno2 ."' AND ";//コピー元受付No
        $query .= "         datadiv     = '". $model->datadiv ."' ";
        $query .= "     ) ";

        $db->query($query);
        //エラー有効
        $db->popExpect();
        $db->commit();
        Query::dbCheckIn($db);
    }
} 
?>
