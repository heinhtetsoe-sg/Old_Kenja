<?php

require_once('for_php7.php');


class knjj010_2Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj010_2index.php", "", "edit");

        $db     = Query::dbCheckOut();

        //校種コンボ
        if (!$model->Properties["useClubMultiSchoolKind"] && $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj010_2Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schKind, $extra, 1);
        }

        $query  = "select * from club_mst ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query .= "order by CLUBCD";
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            $row["SDATE"] = str_replace("-","/",$row["SDATE"]);
            //更新後この行にスクロールバーを移動させる
            if ($row["CLUBCD"] == $model->clubcd) {
                $row["CLUBNAME"] = ($row["CLUBNAME"]) ? $row["CLUBNAME"] : "　";
                $row["CLUBNAME"] = "<a name=\"target\">{$row["CLUBNAME"]}</a><script>location.href='#target';</script>";
            }

            //種目
            $club_item = "";
            $resultI = $db->query(knjj010_2Query::getClubItemDat($model, $row["CLUBCD"]));
            while ($rowI = $resultI->fetchRow(DB_FETCHMODE_ASSOC)) {
                $club_item .= ($club_item != "" && $rowI["ITEMNAME"]) ? "," : "";
                $club_item .= $rowI["ITEMNAME"];
            }
            $row["CLUB_ITEM"] = $club_item;

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj010_2Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
