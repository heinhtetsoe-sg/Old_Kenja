<?php

require_once('for_php7.php');

class knjh400_everyQuery extends Query {
    //生徒情報
    function getSchregData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t2.HR_NAME, ";
        $query .= "     t3.ATTENDNO, ";
        $query .= "     t1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t3  ";
        $query .= "     left join SCHREG_BASE_MST t1 on t3.SCHREGNO = t1.SCHREGNO  ";
        $query .= "     left join SCHREG_REGD_HDAT t2 on t3.GRADE = t2.GRADE and t3.HR_CLASS = t2.HR_CLASS and t3.YEAR = t2.YEAR and t3.SEMESTER = t2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     t3.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     t3.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     t3.SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    
    //入力者取得
    function getUpdateStaff($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.REGISTERCD, ";
        $query .= "     t2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         REGISTERCD ";
        $query .= "     FROM ";
        $query .= "         EVERYDAY_FINDINGS_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     ) t1 left join STAFF_MST t2 on t1.REGISTERCD = t2.STAFFCD ";
        
        return $query;
    }
    
    //タグ検索を含むとき
    function getSearchTag($schregno, $search)
    {
        //タグを編集する
        //文字置換
        $hash = array("#", "＃");
        $search["TAGSEARCH"] = str_replace($hash, "", $search["TAGSEARCH"]);
        $search["TAGSEARCH"] = str_replace(" ", "　", $search["TAGSEARCH"]);

        $tag = array_filter(explode("　", $search["TAGSEARCH"]), "strlen");     //空の配列を削除する
        $count = get_count($tag);
        
        if($search["SEARCHRADIO"] == "2"){  //前方一致
            foreach($tag as $key => $val){
                if($val != ""){
                    $tag[$key] = $val."%";
                }
            }
        }else if($search["SEARCHRADIO"] == "3"){    //部分一致
            foreach($tag as $key => $val){
                if($val != ""){
                    $tag[$key] = "%".$val."%";
                }
            }
        }
        
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.TAG ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.STAFFNAME_SHOW     ";
        $query .= "     FROM ";
        $query .= "         EVERYDAY_FINDINGS_DAT t1 ";
        $query .= "         left join STAFF_MST t2 on t1.REGISTERCD = t2.STAFFCD ";
        $query .= "     WHERE ";
        $query .= "         (t1.SCHREGNO, t1.RECNO) IN ";
        if($search["SEARCH"] == "1"){   //またはのとき
            $query .= "                                     (SELECT DISTINCT ";
            $query .= "                                         SCHREGNO, ";
            $query .= "                                         RECNO ";
            $query .= "                                     FROM ";
            $query .= "                                         EVERYDAY_FINDINGS_TAG_DAT ";
            $query .= "                                     WHERE ";
            $query .= "                                         SCHREGNO = '".$schregno."' AND ";
            $query .= "                                         ( ";
            foreach($tag as $key => $val){
                if($key != 0){
                    $query .= "                                         OR ";
                }
                $query .= "                                             TAG like '".$val."' ";
            }
            $query .= "                                         ) ";
            if($search["FROM_DATE"] != ""){
                if($search["TO_DATE"] == ""){
                    $search["TO_DATE"] = CTRL_DATE;     //TOまで指定していなかったらCTRL_DATEまでで検索
                }
                $query .= "                                     AND ";
                $query .= "                                         FIND_DATE BETWEEN '".$search["FROM_DATE"]."' AND '".$search["TO_DATE"]."' ";
            }
            if($search["STAFF"] != ""){
                $query .= "                                     AND ";
                $query .= "                                         REGISTERCD = '".$search["STAFF"]."' ";
            }
            $query .= "                                     ) ";
        }else{  //かつのとき
            $query .= "                                    (SELECT ";
            $query .= "                                         t1.* ";
            $query .= "                                     FROM ";
            $query .= "                                         ( ";
            $query .= "                                         SELECT DISTINCT ";
            $query .= "                                             SCHREGNO, ";
            $query .= "                                             RECNO ";
            $query .= "                                         FROM ";
            $query .= "                                             EVERYDAY_FINDINGS_TAG_DAT ";
            $query .= "                                         WHERE ";
            $query .= "                                             SCHREGNO = '".$schregno."' ";
            if($search["FROM_DATE"] != ""){
                if($search["TO_DATE"] == ""){
                    $search["TO_DATE"] = CTRL_DATE;     //TOまで指定していなかったらCTRL_DATEまでで検索
                }
                $query .= "                                           AND ";
                $query .= "                                               FIND_DATE BETWEEN '".$search["FROM_DATE"]."' AND '".$search["TO_DATE"]."' ";
            }
            if($search["STAFF"] != ""){
                $query .= "                                           AND ";
                $query .= "                                               REGISTERCD = '".$search["STAFF"]."' ";
            }
            $query .= "                                         ) t1 ";
            foreach($tag as $key => $val){
                $query .= "                                         left join (SELECT ";
                $query .= "                                                         RECNO, ";
                $query .= "                                                         COUNT(*) as CNT".$key." ";
                $query .= "                                                     FROM ";
                $query .= "                                                         EVERYDAY_FINDINGS_TAG_DAT ";
                $query .= "                                                     WHERE ";
                $query .= "                                                         SCHREGNO = '".$schregno."' AND ";
                $query .= "                                                         TAG like '".$val."' ";
                $query .= "                                                     GROUP BY ";
                $query .= "                                                         RECNO ";
                $query .= "                                                     ) c".$key." on t1.RECNO = c".$key.".RECNO ";
            }
            $query .= "                                     WHERE  ";
            $and = "";
            foreach($tag as $key => $val){
                $query .= "                                     ".$and."  ";
                $query .= "                                         CNT".$key." > 0 ";
                $and = "AND";
            }
            $query .= "                                     ) ";
        }
        $query .= "     ) a1 ";
        $query .= "     left join EVERYDAY_FINDINGS_TAG_DAT a2 on a1.SCHREGNO = a2.SCHREGNO and a1.RECNO = a2.RECNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.FIND_DATE DESC, a1.FIND_TIME DESC, a1.RECNO DESC, a2.SEQNO ASC ";
        
        return $query;
    }
    
    //タグ検索を含まないとき
    function getSearch($schregno, $search)
    {
        $query  = " SELECT ";
        $query .= "     a1.*, ";
        $query .= "     a2.TAG ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t2.STAFFNAME_SHOW     ";
        $query .= "     FROM ";
        $query .= "         EVERYDAY_FINDINGS_DAT t1 ";
        $query .= "         left join STAFF_MST t2 on t1.REGISTERCD = t2.STAFFCD ";
        $query .= "     WHERE ";
        $query .= "         t1.SCHREGNO = '".$schregno."' ";
        if($search["FROM_DATE"] != ""){
            if($search["TO_DATE"] == ""){
                $search["TO_DATE"] = CTRL_DATE;     //TOまで指定していなかったらCTRL_DATEまでで検索
            }
            $query .= "     AND ";
            $query .= "         t1.FIND_DATE BETWEEN '".$search["FROM_DATE"]."' AND '".$search["TO_DATE"]."' ";
        }
        if($search["STAFF"] != ""){
            $query .= "     AND  ";
            $query .= "         t1.REGISTERCD = '".$search["STAFF"]."' ";
        }
        $query .= "     ) a1 ";
        $query .= "     left join EVERYDAY_FINDINGS_TAG_DAT a2 on a1.SCHREGNO = a2.SCHREGNO and a1.RECNO = a2.RECNO ";
        $query .= " ORDER BY ";
        $query .= "     a1.FIND_DATE DESC, a1.FIND_TIME DESC, a1.RECNO DESC, a2.SEQNO ASC ";
        
        return $query;
    }

    
    
    //画面右側
    //選択したデータ取得
    function getChoiceData($schregno, $recno)
    {
        $query  = " SELECT ";
        $query .= "     REPLACE(CAST(t1.FIND_DATE AS VARCHAR(10)), '-', '/') as DATE, ";
        $query .= "     SUBSTR(t1.FIND_TIME, 1, 2) as H_TIME, ";
        $query .= "     SUBSTR(t1.FIND_TIME, 4, 2) as M_TIME, ";
        $query .= "     t1.TITLE, ";
        $query .= "     t1.TEXT, ";
        $query .= "     t1.REGISTERCD, ";
        $query .= "     t2.TAG ";
        $query .= " FROM ";
        $query .= "     EVERYDAY_FINDINGS_DAT t1  ";
        $query .= "     left join EVERYDAY_FINDINGS_TAG_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.RECNO = t2.RECNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     t1.RECNO = ".$recno." ";
        $query .= " ORDER BY ";
        $query .= "     t2.SEQNO ";
        
        return $query;
    }
    
    //入力者氏名取得
    function getStaffName($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffcd."' ";
        
        return $query;
    }
    
    //頻出タグ取得
    function getTagCnt($schregno="")
    {
        $query  = " SELECT ";
        $query .= "     TAG ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         COUNT(*) as CNT, ";
        $query .= "         TAG ";
        $query .= "     FROM ";
        $query .= "         EVERYDAY_FINDINGS_TAG_DAT ";
        if($schregno != ""){    //左画面に出すところのみ対象生徒の頻出を取得
            $query .= "     WHERE ";
            $query .= "         SCHREGNO = '".$schregno."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         TAG ";
        $query .= "     ) T1 ";
        $query .= " ORDER BY ";
        $query .= "     CNT DESC  ";
        $query .= " fetch first 5 rows only ";
        
        return $query;
    }
    
    //RECNOの最大値取得
    function getMaxRecNo($schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(RECNO) ";
        $query .= " FROM ";
        $query .= "     EVERYDAY_FINDINGS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    
    //削除用Query
    function delQuery($schregno, $recno, $table)
    {
        $query  = " DELETE FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     RECNO = ".$recno." ";
        
        return $query;
    }
}
?>
