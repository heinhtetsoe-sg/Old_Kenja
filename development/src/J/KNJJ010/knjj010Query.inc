<?php

require_once('for_php7.php');

class knjj010Query extends Query {

    //部クラブ年度一覧取得
    function selectQuery($model)
    {
        $query =  "select m.CLUBCD, m.CLUBNAME from club_ydat d, club_mst m ";
        $query .= "where d.CLUBCD = m.CLUBCD ";
        $query .= "and d.YEAR = '".$model->year."'"; 
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND m.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND m.SCHOOL_KIND = '".$model->schKind."'";
            $query .= "  AND m.SCHOOLCD    = d.SCHOOLCD ";
            $query .= "  AND m.SCHOOL_KIND = d.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND m.SCHOOLCD = '".SCHOOLCD."'";
            $query .= "  AND m.SCHOOL_KIND = '".SCHOOLKIND."'";
            $query .= "  AND m.SCHOOLCD = d.SCHOOLCD ";
            $query .= "  AND m.SCHOOL_KIND = d.SCHOOL_KIND ";
        }
        $query .= "order by m.CLUBCD";
        return $query;
    }

    //年度一覧取得
    function selectYearQuery()
    {
        $query  = "select distinct YEAR from club_ydat ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " where ";
            $query .= "      SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " where ";
            $query .= "      SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query .= " order by year desc";
        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //部クラブ一覧取得
    function selectClassQuery($clubcd_array,$model)
    {
        $query  = "SELECT cm.CLUBCD, cm.CLUBNAME ";
        $query .= "FROM club_mst cm ";
        $query .= "WHERE NOT EXISTS (SELECT 'X' ";                          /* NO001↓ */
        $query .= "                  FROM club_ydat cy";
        $query .= "                  WHERE cy.year = '".$model->year."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                  AND cy.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "                  AND cy.SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                  AND cy.SCHOOLCD = '".SCHOOLCD."'";
            $query .= "                  AND cy.SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query .= "                  AND cy.CLUBCD = cm.CLUBCD)";           /* NO001↑ */
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "   AND cm.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "   AND cm.SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND cm.SCHOOLCD = '".SCHOOLCD."'";
            $query .= "   AND cm.SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query .= " ORDER BY cm.CLUBCD ";
        return $query;
        
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //部クラブコードを配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from club_ydat where YEAR = '" .$model->year ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "   AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "   AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "   AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $db->query($query);
        
        if ($array[0] != ""){
            //部クラブ年度データの登録
          if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
              $query = "INSERT INTO club_ydat(YEAR, SCHOOLCD, SCHOOL_KIND, CLUBCD,REGISTERCD,UPDATED)";
          } else {
              $query = "INSERT INTO club_ydat(YEAR, CLUBCD,REGISTERCD,UPDATED)";
          }
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";                               //年度
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $query.= ",'" .SCHOOLCD."'";
                    $query.= ",'".$model->schKind."'";
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query.= ",'" .SCHOOLCD."'";
                    $query.= ",'".SCHOOLKIND."'";
                }
                $query.= ",'" .$array[$i] ."'";                                 //部クラブコード
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付
    
            }
            $db->query($query);
        } else {
            $model->year = "";
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
