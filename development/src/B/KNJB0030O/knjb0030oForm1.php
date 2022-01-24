<?php
class knjb0030oForm1
{
    function main(&$model)
    {
        //権限チェック
       if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
       }

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjb0030oindex.php", "", "edit");

        //最終学期かを判定
        $year_control = (trim(CTRL_SEMESTER) == trim($model->control["学期数"]))? true : false ;

        //１学期（前期）かを判定
        $year_control2 = (trim(CTRL_SEMESTER) == "1")? true : false ;

        $arg["Show_control"] = true;

        $db     = Query::dbCheckOut();
        
        //char_datの確認
        $query = knjb0030oQuery::getChairDatcount($model->term);
        $getCount = $db->getOne($query);
        knjCreateHidden($objForm, "CHAIR_DAT_COUNT", $getCount);
        
        $result = $db->query(knjb0030oQuery::SelectComb_seme());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            /* 最終学期の場合、対象年度リストに次の年度の１学期（前期）を追加 */
            //対象
            if ($row["YEAR"]==CTRL_YEAR){
                $opt[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTERNAME"],
                               "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
            if($year_control && $row["YEAR"]==CTRL_YEAR+1 && $row["SEMESTER"]=="1"){
                $opt[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTERNAME"],
                               "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
            /* １学期（前期）の場合、参照学期リストに前の年度の１～３学期（前～後期）を追加 */
            //参照
            if ($year_control2 && $row["YEAR"]==CTRL_YEAR-1){
                $opt2[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTERNAME"],
                                "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
            if ($row["YEAR"]==CTRL_YEAR){
                $opt2[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTERNAME"],
                                "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象年度コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "term",
                            "size"      => "1",
                            "extrahtml" => "onChange=\"btn_submit('list')\"",
                            "value"     => $model->term,
                            "options"   => $opt));
        $arg["term"] = $objForm->ge("term");

        //参照年度コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "term2",
                            "size"      => "1",
                            "value"     => $model->term2,
                            "options"   => $opt2));
        $arg["term2"] = $objForm->ge("term2");

        //コピーボタン
        $objForm->ae( array("type"      =>    "button",
                            "name"      =>    "btn_copy",
                            "value"     =>    "左の学期のデータをコピー",
                            "extrahtml" =>    "onClick=\"return btn_submit('copy');\""));
//                            "extrahtml" =>    "onClick=\"return btn_submit('copy');\" style=\"width:165px\""));
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //チェックボックス
        $extraCheck = (strlen($model->check) || $model->defFlg == "on") ? "checked " : "";
        $objForm->ae( array("type"      =>    "checkbox",
                            "name"      =>    "check",
                            "value"     =>    "1",
                            "extrahtml" =>    $extraCheck ."onClick=\"disCheckGrddiv()\" id=\"btn_check\"" ));

        $arg["btn_check"] = $objForm->ge("check");

        //卒業生・退学者・転学者も含むチェックボックス
        $extraCheckGrddiv  = (strlen($model->checkGrddiv) || $model->defFlg == "on") ? "checked " : "";
        $extraCheckGrddiv .= (strlen($extraCheck)) ? "" : "disabled ";
        $objForm->ae( array("type"      =>    "checkbox",
                            "name"      =>    "checkGrddiv",
                            "value"     =>    "1",
                            "extrahtml" =>    $extraCheckGrddiv ."id=\"checkGrddiv\"" ));
        $arg["checkGrddiv"] = $objForm->ge("checkGrddiv");

        $model->defFlg = "off";

        if (!isset($model->group)) $model->group = "1";

        //絞込みラジオボタン（教科:1・群:2）
        $opt_group[0]=1;
        $opt_group[1]=2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "btn_group".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "group",
                                "extrahtml"  => "onclick=\"btn_submit('list')\" id=\"$name\"",
                                "value"      => $model->group,
                                "options"    => $opt_group));
            $arg[$name] = $objForm->ge("group",$i);
        }

        if ($model->sort_name == $model->fields["sort_name"]) {
            $model->asc_or_desc = $model->asc_or_desc * -1;
        } else {
            $model->asc_or_desc = 1;
        }

        //初期化
        if (!$model->fields["sort_name"]) {
            if ($model->group == '1') { //group 1:教科 2:群
                $model->fields["sort_name"] = "subclass_name";
            } else {
                $model->fields["sort_name"] = "group_name";
            }
            $model->asc_or_desc = 1;
        }

        if ($model->asc_or_desc == 1) {
            $sankaku = '▲';
            $asc_or_desc = "ASC";
        } else {
            $sankaku = '▼';
            $asc_or_desc = "DESC";
        }

        if ( $model->group == "1" ){
            $arg["group_name"] = "教科";
        } else {
            if ($model->fields["sort_name"] == "group_name") {
                $arg["group_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>群名称</font>", "", array("cmd"=>"sort", "sort_name"=>"group_name")) .$sankaku;
            } else {
                $arg["group_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>群名称</font>", "", array("cmd"=>"sort", "sort_name"=>"group_name"));
            }
        }

        if ($model->fields["sort_name"] == "url_name") {
            $arg["url_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>講座コード</font>", "", array("cmd"=>"sort", "sort_name"=>"url_name")) .$sankaku;
        } else {
            $arg["url_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>講座コード</font>", "", array("cmd"=>"sort", "sort_name"=>"url_name"));
        }
        if ($model->fields["sort_name"] == "subclass_name") {
            $arg["subclass_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>科目</font>", "", array("cmd"=>"sort", "sort_name"=>"subclass_name")) .$sankaku;
        } else {
            $arg["subclass_name"] = View::alink("knjb0030oindex.php", "<font color='#ffffff'>科目</font>", "", array("cmd"=>"sort", "sort_name"=>"subclass_name"));
        }

        $model->sort_name = $model->fields["sort_name"];

        $db     = Query::dbCheckOut();
        $result = $db->query(knjb0030oQuery::SelectList_test($model->term, $model->group, $model, $asc_or_desc));
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //一度に表示する講座一覧の上限5000件を超えたら、ワーニングメッセージ表示する。
            if ($i>5000){
                $i = $i-1;
                    $arg["datalimit"] = "DataLimitError('". $i ."');";
                break;
            }
            array_walk($row, "htmlspecialchars_array");

            $row["URL"] = View::alink("knjb0030oindex.php", $row["CHAIRCD"], "target=right_frame",
                                         array("cmd"      => "edit",
                                               "CHAIRCD"  => $row["CHAIRCD"],
                                               "term"     => $model->term));
            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";  //#ccffff
            //更新後この行が画面の先頭に来るようにする
            if ($row["CHAIRCD"] == $model->chaircd) {
                $row["CHAIRNAME"] = ($row["CHAIRNAME"]) ? $row["CHAIRNAME"] : "　";
                $row["CHAIRNAME"] = "<a name=\"target\">{$row["CHAIRNAME"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row;
            $i++;
        }

        $result->free();
        Query::dbCheckIn($db);

        //コピーボタン押し下げ時のチェック用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year_semester",
                            "value"     => CTRL_YEAR. "-" .CTRL_SEMESTER
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR."年度 "
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTERNAME
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "list" && VARS::get("ed") != "1")
            $arg["reload"] = "window.open('knjb0030oindex.php?cmd=edit&init=1','right_frame');";

        View::toHTML($model, "knjb0030oForm1.html", $arg); 
    }
}
?>
