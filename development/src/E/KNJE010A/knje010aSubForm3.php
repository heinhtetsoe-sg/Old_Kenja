<?php

require_once('for_php7.php');

class knje010aSubForm3
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knje010aindex.php", "", "sel");
        $arg["jscript"] = "";
        $db = Query::dbCheckOut();

        if ($model->Properties["useMaruA_avg"] == "") {
            $arg["UnUseMaruA_avg"] = 1;
        }

        //「備考2」の表示制御
        if ($model->Properties["useHexamRemark2Flg"] == 1) {
            $arg["useHexamRemark2Flg"] = 1;
        }

        //生徒一覧
        $opt_left = $opt_right = array();

        $array = explode(",", $model->replace_data["selectdata"]);

        //リストが空であれば置換処理選択時の生徒を加える
        if ($array[0]=="") $array[0] = $model->schregno;

        $query = knje010aQuery::selectQuery($model);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //生徒情報


        $result = $db->query(knje010aQuery::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (!in_array($row["SCHREGNO"], $array)){
                $opt_right[]  = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[] = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            }
        }

        $result->free();

        //チェックボックス
        for ($i=0;$i<4;$i++)
        {
            $extra = "";
            if ($i==3) {
                $extra = "onClick=\"return check_all(this);\"";
            }

            $objForm->ae(array("type"       => "checkbox",
                                "name"      => "RCHECK".$i,
                                "value"     => "1",
                                "extrahtml" => $extra,
                                "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
            $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
        }

        //備考1
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->remark_gyou, ($model->remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["REMARK"]);
        $arg["data"]["REMARK_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark_gyou}行まで)";

        //備考2
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2", $model->remark2_gyou, ($model->remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["REMARK2"]);
        $arg["data"]["REMARK2_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark2_gyou}行まで)";

        //特記事項なしチェックボックス
        $extra  = " id=\"NO_COMMENTS\" onclick=\"return CheckRemark();\"";
        $arg["data"]["NO_COMMENTS"] = knjCreateCheckBox($objForm, "NO_COMMENTS", "1", $extra, "");

        //特記事項なしラベル
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["data"]["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        //学習成績概評チェックボックス
        $extra  = ($Row["COMMENTEX_A_CD"] == "1") ? "checked" : "";
        $arg["data"]["COMMENTEX_A_CD"] = knjCreateCheckBox($objForm, "COMMENTEX_A_CD", "1", $extra, "");


        //年組名
        $hr_name = $db->getOne(knje010aQuery::getHR_Name($model));

        Query::dbCheckIn($db);

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('replace_update')\"" ) );

        //戻るボタン
        $link = REQUESTROOT."/E/KNJE010A/knje010aindex.php?cmd=back&ini2=1";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );

        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");

        //対象生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //その他の生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        $arg["info"] = array("TOP"        =>  sprintf("%d年度  %s  対象クラス  %s",
                                                CTRL_YEAR,$model->control_data["学期名"][CTRL_SEMESTER],$hr_name),
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "E_APPDATE") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );

        knjCreateHidden($objForm, "SCHREGNO", $model->replace_data["selectdata"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje010aSubForm3.html", $arg);
    }
}
?>
