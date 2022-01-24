<?php

require_once('for_php7.php');

class knjp050kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp050kindex.php", "", "edit");

        //警告メッセージが表示される場合、またはリロードした場合
        if (isset($model->warning) || $model->cmd == "reload" || $model->cmd == "change2") {
            $Row =& $model->field;
        } else {
            $Row = knjp050kQuery::getRow($model,1);
        }

        $db = Query::dbCheckOut();

        $arg["EXPENSE_GRP_CD"] = ($model->exp_grpcd != "") ? $model->exp_grpcd.":".$model->name : "";

        //入金予定データチェック(起動時のみ)
        if ($model->boot_flg == true) {
            //更新処理の権限があるかチェック
            $authority = $db->getOne(knjp050kQuery::getAuthority());

            if ($authority == 0) {
                $model->money_change_flg = "disabled";
                $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は許可されていません。','');";
            }

            if ($model->money_change_flg == "") {
                //入金済みデータの件数
                $paid_money  = $db->getOne(knjp050kQuery::getPaidMoney($model->year, "money_paid_m_dat"));
                $paid_money += $db->getOne(knjp050kQuery::getPaidMoney($model->year, "money_paid_s_dat"));

                if ($paid_money != 0) {
                    $model->money_change_flg = "disabled";
                    $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','入金済みデータが存在します。');";
                }
            }

            if ($model->money_change_flg == "") {
                //減免等により、生徒毎に内容が変更された入金予定データ
                $paid_money  = $db->getOne(knjp050kQuery::M_MoneyChange($model->year));
                $paid_money += $db->getOne(knjp050kQuery::S_MoneyChange($model->year));

                if ($paid_money != 0) {
                    $model->money_change_flg = "disabled";
                    $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','減免処理等により入金予定データが変更されています。');";
                } else {
                    //対象年度に設定されている費目グループコードを取得
                    $result = $db->query(knjp050kQuery::get_Grpcd($model->year));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        //費目グループ中分類データにあって入金予定中分類データに無いものをチェック
                        $result2 = $db->query(knjp050kQuery::Check_Grp_MData($model->year,$row["EXPENSE_GRP_CD"],"LEFT OUTER JOIN"));
                        while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                        {
                            if ($row2["GRP_M_CD"] != $row2["DUE_M_CD"]) {
                               $model->money_change_flg = "disabled";
                               $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','減免処理等により入金予定データが変更されています。');";
                               break 2;
                            }
                        }
                        //入金予定中分類データにあって費目グループ中分類データに無いものをチェック
                        $result2 = $db->query(knjp050kQuery::Check_Grp_MData($model->year,$row["EXPENSE_GRP_CD"],"RIGHT OUTER JOIN"));
                        while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                        {
                            if ($row2["GRP_M_CD"] != $row2["DUE_M_CD"]) {
                               $model->money_change_flg = "disabled";
                               $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','減免処理等により入金予定データが変更されています。');";
                               break 2;
                            }
                        }

                        //費目グループ小分類データにあって入金予定小分類データに無いものをチェック
                        $result2 = $db->query(knjp050kQuery::Check_Grp_SData($model->year,$row["EXPENSE_GRP_CD"]));
                        while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                        {
                            //小分類の性別の設定が無い、または小分類の性別と生徒の性別が一致していて、小分類コードが一致しない時
                            if (($row2["SCD_SEX"] == "" || $row2["SCD_SEX"] == $row2["BASE_SEX"]) && 
                                 $row2["GRP_S_CD"] != $row2["DUE_S_CD"]) {
                               $model->money_change_flg = "disabled";
                               $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','減免処理等により入金予定データが変更されています。');";
                               break 2;
                            }
                        }
                        //入金予定小分類データにあって費目グループ小分類データに無いものをチェック
                        $result2 = $db->query(knjp050kQuery::Check_Grp_SData2($model->year,$row["EXPENSE_GRP_CD"]));
                        while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC))
                        {
                            //小分類の性別と生徒の性別が一致しない、または小分類コードが一致しない時
                            if (($row2["SCD_SEX"] != "" && $row2["SCD_SEX"] != $row2["BASE_SEX"]) ||
                                 $row2["GRP_S_CD"] != $row2["DUE_S_CD"]) {
                               $model->money_change_flg = "disabled";
                               $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','減免処理等により入金予定データが変更されています。');";
                               break 2;
                            }
                        }
                    }                    
                }
            }

            if ($model->money_change_flg == "") {
                //分納が設定されている入金予定データ
                $paid_money  = $db->getOne(knjp050kQuery::getInstChage($model->year));

                if ($paid_money != 0) {
                    $model->money_change_flg = "disabled";
                    $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','分納が設定されているデータが存在します。');";
                }
            }

            //起動時に設定したフラグを初期化
            $model->boot_flg = "";
        }


        //費目グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_GRP_CD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXPENSE_GRP_CD"]));        
        
        //費目グループ名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_GRP_NAME",
                            "size"        => 25,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["EXPENSE_GRP_NAME"] ));

        $arg["VAL"]["EXPENSE_GRP_CD"] = $objForm->ge("EXPENSE_GRP_CD")."：".$objForm->ge("EXPENSE_GRP_NAME");

        //学年コンボ
        $opt_grade = array();

        $result = $db->query(knjp050kQuery::get_Grade());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grade[] = array("label" => $row["GRADE"], "value" => $row["GRADE"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE2",
                            "extrahtml"   => "onChange=\"btn_submit('change2')\"",
                            "value"       => $model->grade2,
                            "options"     => $opt_grade
                            ));
        $arg["GRADE"] = $objForm->ge("GRADE2");

        //起動時に設定
        if(!isset($model->grade2)) { 
            $model->grade2 = $opt_grade[0]["value"];
        }


        //割当クラス一覧取得
        if (isset($model->warning) || $model->cmd == "reload") {
            //費目中分類割当一覧の値が変更してリロードした時
            $result = $db->query(knjp050kQuery::ReloadSelectClass($model));
        } else {
            $result = $db->query(knjp050kQuery::GetSelectClass($model));
        }
        
        $opt_left = $opt_right = $tempcd = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $label = $row["GRADE"].$row["HR_CLASS"]."：".htmlspecialchars($row["HR_NAMEABBV"]);
            $opt_left[]  = array("label" => $label, "value" => $row["HR_CLASS"]);
            
            $tempcd[] = $row["HR_CLASS"];
        }

        //クラス一覧取得
        $result = $db->query(knjp050kQuery::GetClass($model,$tempcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $label = $row["GRADE"].$row["HR_CLASS"]."：".htmlspecialchars($row["HR_NAMEABBV"]);
            $opt_right[]  = array("label" => $label, "value" => $row["HR_CLASS"]);
        }

        //ＨＲクラスに関するオブジェクトを作成                
        $reftPart    = $this->CreateCombo($objForm, "left_classcd", "left", $opt_left, "ondblclick=\"move1('right','left_classcd','right_classcd',1,1);\"");
        $rightPart   = $this->CreateCombo($objForm, "right_classcd", "left", $opt_right, "ondblclick=\"move1('left','left_classcd','right_classcd',1,1);\"");
        $sel_add_all = $this->CreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move1('sel_add_all','left_classcd','right_classcd',1,1);\"");
        $sel_add     = $this->CreateBtn($objForm, "sel_add", "＜", "onclick=\"return move1('left','left_classcd','right_classcd',1,1);\"");
        $sel_del     = $this->CreateBtn($objForm, "sel_del", "＞", "onclick=\"return move1('right','left_classcd','right_classcd',1,1);\"");
        $sel_del_all = $this->CreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move1('sel_del_all','left_classcd','right_classcd',1,1);\"");
        $arg["class"] = array( "LEFT_LIST"   => "割当クラス一覧",
                               "RIGHT_LIST"  => "クラス一覧",
                               "LEFT_PART"   => $reftPart,
                               "RIGHT_PART"  => $rightPart,
                               "SEL_ADD_ALL" => $sel_add_all,
                               "SEL_ADD"     => $sel_add,
                               "SEL_DEL"     => $sel_del,
                               "SEL_DEL_ALL" => $sel_del_all);                    

        //費目中分類割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //費目中分類割当一覧の値が変更してリロードした時
            $result = $db->query(knjp050kQuery::ReloadSelectMcd($model));
        } else {
            $result = $db->query(knjp050kQuery::GetSelectMcd($model));
        }

        $opt_left = $opt_right = $tempcd = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {              
            $label = $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"]."：".htmlspecialchars($row["EXPENSE_M_NAME"]);
            $opt_left[]  = array("label" => $label, "value" => $row["EXPENSE_M_CD"]);

            $tempcd[] = $row["EXPENSE_M_CD"];
        }

        //費目中分類一覧
        $result = $db->query(knjp050kQuery::GetMcd($model,$tempcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $label = $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"]."：".htmlspecialchars($row["EXPENSE_M_NAME"]);
            $opt_right[]  = array("label" => $label, "value" => $row["EXPENSE_M_CD"]);
        }


        //費目中分類合計金額
        $man_money = $woman_money = "";
        $result = $db->query(knjp050kQuery::GetMcdMoney($model,$tempcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["EXPENSE_S_EXIST_FLG"] == "0") {
                $man_money   += $row["EXPENSE_M_MONEY"];
                $woman_money += $row["EXPENSE_M_MONEY"];
            }
        }
        
#        if (!isset($model->warning) && $model->cmd != "change2" && $model->cmd != "reload") {
#            //入金済みになった入金予定データがあるかチェック
#            $paid_m_money = $db->getOne(knjp050kQuery::CheckMoney($model->year,$tempcd));
# 
#            $disabled = ($paid_m_money != 0) ? "disabled" : "";
#        }

        //費目中分類に関するオブジェクトを作成                
        $arg["expmcd"] = array( "LEFT_LIST"   => "費目中分類割当一覧",
                                "RIGHT_LIST"  => "費目中分類一覧",
                                "LEFT_PART"   => $this->CreateCombo($objForm, "left_expmcd", "left", $opt_left, "ondblclick=\"move1('right','left_expmcd','right_expmcd',1,2);\""),
                                "RIGHT_PART"  => $this->CreateCombo($objForm, "right_expmcd", "left", $opt_right, "ondblclick=\"move1('left','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_ADD_ALL" => $this->CreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return move1('sel_add_all2','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_ADD"     => $this->CreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL"     => $this->CreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL_ALL" => $this->CreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return move1('sel_del_all2','left_expmcd','right_expmcd',1,2);\"")
                              );


        //費目小分類割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //費目中分類割当一覧の値が変更してリロードした時
            $result = $db->query(knjp050kQuery::ReloadSelectScd($model,$tempcd));
        } else {
            $result = $db->query(knjp050kQuery::GetSelectScd($model,$tempcd));
        }

        $opt_left = $opt_right = $tempcd2 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $label = $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].$row["EXPENSE_S_CD"]."：".htmlspecialchars($row["EXPENSE_S_NAME"]);
            $val = $row["EXPENSE_S_CD"].$row["EXPENSE_M_CD"];

            $opt_left[]  = array("label" => $label, "value" => $val);
            $tempcd2[] = $row["EXPENSE_S_CD"];
        }

        //費目小分類一覧
        $result = $db->query(knjp050kQuery::GetScd($model,$tempcd,$tempcd2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $label = $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].$row["EXPENSE_S_CD"]."：".htmlspecialchars($row["EXPENSE_S_NAME"]);
            $val = $row["EXPENSE_S_CD"].$row["EXPENSE_M_CD"];
            
            $opt_right[]  = array("label" => $label, "value" => $val);
        }


        //費目小分類合計金額
        $man_money2 = $woman_money2 = "";
        $result = $db->query(knjp050kQuery::GetScdMoney($model,$tempcd,$tempcd2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //中分類合計金額にも値を追加する
            if ($row["SEX"] == "") {
               $man_money    += $row["EXPENSE_S_MONEY"];
               $woman_money  += $row["EXPENSE_S_MONEY"];
               $man_money2   += $row["EXPENSE_S_MONEY"];
               $woman_money2 += $row["EXPENSE_S_MONEY"];
            } else if($row["SEX"] == "1") {
               $man_money    += $row["EXPENSE_S_MONEY"];
               $man_money2   += $row["EXPENSE_S_MONEY"];
            } else if($row["SEX"] == "2") {
               $woman_money  += $row["EXPENSE_S_MONEY"];
               $woman_money2 += $row["EXPENSE_S_MONEY"];
            }
        }
       
        //費目小分類に関するオブジェクトを作成
        $arg["expscd"] = array( "LEFT_LIST"   => "費目小分類割当一覧",
                                "RIGHT_LIST"  => "費目小分類一覧",
                                "LEFT_PART"   => $this->CreateCombo($objForm, "left_expscd", "left", $opt_left, "ondblclick=\"move1('right','left_expscd','right_expscd',1,3);\""),
                                "RIGHT_PART"  => $this->CreateCombo($objForm, "right_expscd", "left", $opt_right, "ondblclick=\"move1('left','left_expscd','right_expscd',1,3);\""),
                                "SEL_ADD_ALL" => $this->CreateBtn($objForm, "sel_add_all3", "≪", "onclick=\"return move1('sel_add_all3','left_expscd','right_expscd',1,3);\""),
                                "SEL_ADD"     => $this->CreateBtn($objForm, "sel_add3", "＜", "onclick=\"return move1('left','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL"     => $this->CreateBtn($objForm, "sel_del3", "＞", "onclick=\"return move1('right','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL_ALL" => $this->CreateBtn($objForm, "sel_del_all3", "≫", "onclick=\"return move1('sel_del_all3','left_expscd','right_expscd',1,3);\"")
                              );

        //中分類合計金額をカンマ区切りにする
        $man_money   = is_numeric($man_money) ? number_format($man_money) : "";
        $woman_money = is_numeric($woman_money) ? number_format($woman_money) : "";
        $arg["expmcd"]["MCD_MONEY"] = "合計 男子：".$man_money."円"."　女子：".$woman_money."円";
        
        //小分類合計金額をカンマ区切りにする
        $man_money2   = is_numeric($man_money2) ? number_format($man_money2) : "";
        $woman_money2 = is_numeric($woman_money2) ? number_format($woman_money2) : "";
        $arg["expscd"]["SCD_MONEY"] = "合計 男子：".$man_money2."円"."　女子：".$woman_money2."円";


        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => " $model->money_change_flg onclick=\"return doSubmit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => " $model->money_change_flg onclick=\"return doSubmit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => " $model->money_change_flg onclick=\"return doSubmit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );
                            
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata3") );


        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knjp050kindex.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp050kForm2.html", $arg);
    }


    //コンボボックス作成
    function CreateCombo(&$objForm, $name, $value, $opt, $extra)
    {
        $objForm->ae( array("type"        => "select",
                            "name"        => $name,
                            "size"        => "15",
                            "extrahtml"   => $extra." multiple style=\"WIDTH:100%; HEIGHT:130px\"",
                            "value"       => $value,
                            "options"     => $opt ) );
        return $objForm->ge($name);
    }
    
    //ボタン作成
    function CreateBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

}
?>
