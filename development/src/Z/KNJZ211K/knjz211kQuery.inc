<?php
class knjz211kQuery extends Query {

    //デフォルト値を取得
    function getDefultData($year, $asses)
    {
        $query  = " SELECT                            ";
        $query .= "     *                             ";
        $query .= " FROM                              ";
        $query .= "     type_asses_mst                ";
        $query .= " WHERE                             ";
        $query .= "     year = '" . $year . "' ";
        if ($asses != "") {
            $query .= "   AND type_asses_cd = '" . $asses . "' ";
        }
        $query .= " ORDER BY                          ";
        $query .= "     type_asses_level              ";

        return $query;
    }

    //評定データ情報抽出
    function getAssesQuery($year, $asses = "")
    {
        $query  = " SELECT                          ";
        $query .= "     *                           ";
        $query .= " FROM                            ";
        $query .= "   type_asses_hdat               ";
        $query .= " WHERE                           ";
        $query .= "   year = '" . $year . "' ";
        if ($asses != "") {
            $query .= "   AND type_asses_cd = '" . $asses . "' ";
        }
        $query .= " ORDER BY                        ";
        $query .= "   type_asses_cd                 ";
        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($year, $asses = "")
    {
        $query  = " SELECT                               ";
        $query .= "   *                                  ";
        $query .= " FROM                                 ";
        $query .= "   type_asses_mst                     ";
        $query .= " WHERE                                ";
        $query .= "   year = '" . $year . "'      ";
        if ($asses != "") {
            $query .= "   AND type_asses_cd = '" . $asses . "' ";
        }
        $query .= " ORDER BY                             ";
        $query .= "   type_asses_cd                      ";

        return $query;
    }

    function &getCopyQuery($year)
    {
        $delete_fields = array("DELETE_mst"  => "type_asses_mst",
                               "DELETE_hdat" => "type_asses_hdat");
        $insert_fields = array("INSERT_mst"  => "type_asses_mst",
                               "INSERT_hdat" => "type_asses_hdat");
        $result_flg = true;
        $query = "";

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach($delete_fields as $key => $value)
        {
            $query  = " DELETE FROM      ";
            $query .= "   $value         ";
            $query .= " WHERE            ";
            $query .= "   year = '$year' ";

            $result = $db->query($query);

            if ($result != DB_OK) {
                DB::errorMessage($result);
                $db->rollback();
            } else {
                $db->commit();
            }
        }
        foreach($insert_fields as $key => $value)
        {
            $query  = " INSERT INTO                    ";
            $query .= "   $value                       ";
            $query .= " SELECT                         ";
            $query .= "   '$year', type_asses_cd,      ";
            if (substr($value, -3) == "mst") {
                $query .= "   type_asses_level,                        ";
                $query .= "   type_asses_high, type_asses_low,         ";
            } else if (substr($value, -3) == "dat") {
                $query .= "   type_group_ave_high, type_group_ave_low, ";
            }
            $query .= "   registercd, sysdate()        ";
            $query .= " FROM                           ";
            $query .= "   $value                       ";
            $query .= " WHERE                          ";
            $query .= "   year = '" . ($year - 1) . "' ";

            $result = $db->query($query);
            if ($result != DB_OK) {
                DB::errorMessage($result);
                $db->rollback();
                $db->autoCommit(true);
                Query::dbCheckIn($db);
                $result_flg = false;
            }
        }
        
        if ($result_flg) {
            $db->commit();
            $db->autoCommit(true);
            Query::dbCheckIn($db);
        }

        return $result_flg;
    }

    //delete処理
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM                                          ";
        $query .= "   type_asses_mst                                     ";
        $query .= " WHERE                                                ";
        $query .= "   year          = '" . $fields["YEAR"] . "'          ";
        $query .= " AND                                                  ";
        $query .= "   type_asses_cd = '" . $fields["TYPE_ASSES_CD"] . "' ";
        $db->query($query);

        $query  = " DELETE FROM                                          ";
        $query .= "   type_asses_hdat                                    ";
        $query .= " WHERE                                                ";
        $query .= "   year          = '" . $fields["YEAR"] . "'          ";
        $query .= " AND                                                  ";
        $query .= "   type_asses_cd = '" . $fields["TYPE_ASSES_CD"] . "' ";
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //insert処理
    function &getInsertQuery($fields)
    {
        //最大値、最小値を取得
        $max = $fields["TYPE_ASSES_HIGH"];
        $cnt = $fields["TBL_COUNT"];
        //db open
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        for ($i = 1; $i <= $cnt; $i++)
        {
            $ASSESSLOW  = "ASSESSLOW".($i);

            $data["YEAR"][TEXT]                 = $fields["YEAR"];
            $data["TYPE_ASSES_CD"][TEXT]        = $fields["TYPE_ASSES_CD"];
            $data["TYPE_ASSES_LEVEL"][TEXT]     = ($i);
            $data["TYPE_ASSES_LOW"][NUMBER]     = $fields[$ASSESSLOW];
            $data["TYPE_ASSES_HIGH"][NUMBER]    = ($i == $cnt) ? $max : (($fields["ASSESSLOW".($i + 1)])-1);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $query = Query::insertSQL($data, "type_asses_mst");

            $db->query($query);
        }
        $db->commit();
        $db->autoCommit(true);
        
        $data = array();
        $data["YEAR"][TEXT]                     = $fields["YEAR"];
        $data["TYPE_ASSES_CD"][TEXT]            = $fields["TYPE_ASSES_CD"];
        $data["TYPE_GROUP_AVE_HIGH"][NUMBER]    = $fields["TYPE_GROUP_AVE_HIGH"];
        $data["TYPE_GROUP_AVE_LOW"][NUMBER]     = $fields["TYPE_GROUP_AVE_LOW"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";
        $query = Query::insertSQL($data, "type_asses_hdat");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return true;
    }
}
?>
